/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio. Exolab is a registered
 *    trademark of Intalio.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2000 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: EnvContext.java,v 1.6 2000/09/08 23:05:19 mohammed Exp $
 */


package tyrex.naming;


import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Dictionary;
import java.util.StringTokenizer;
import java.security.AccessController;
import javax.naming.Reference;
import javax.naming.NamingException;
import javax.naming.LinkRef;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.CompositeName;
import javax.naming.NotContextException;
import javax.naming.NameNotFoundException;
import javax.naming.OperationNotSupportedException;
import javax.naming.spi.NamingManager;
import tyrex.util.FastThreadLocal;


/**
 * An environment naming context implementation. This is a read only,
 * serializable, bound-to-thread JNDI service provider that implements
 * the full J2EE requirements. This object is also used to set the
 * contents of the ENC for the current thread.
 * <p>
 * This context is not constructed directly but generally through the
 * application performing a URL lookup on the <tt>java:</tt> namespace.
 * Such requests are materizlied through {@link tyrex.naming.java.javaURLContextFactory}
 * which directs them to an instance of {@link EnvContext}.
 * <p>
 * To comply with J2EE requirements, the environment context is a
 * read-only namespace, heirarchial and supporting links, can bind
 * non-persistent objects (like factories, services), and can be
 * serialized as part of a bean's activation/passivation.
 * <p>
 * In order to set the contents of the ENC, the container will construct
 * a {@link MemoryContext} that is not shared, populate it with all the
 * relevant information (including empty contexts), and call {@link
 * #setEnvContext} on the current thread. The same memory context may be
 * assigned to any number of threads.
 * <p>
 * {@link #setEnvContext} may be called for each new context on the same
 * thread, and must be balanced with a call to {@link #unsetEnvContext}.
 * The ENC state is undefined outside the boundaries of these methods
 * calles, and any attempt to use it will likley result in an error.
 * <p>
 * The following example places an instance of <tt>UserTransaction</tt>
 * and a JDBC connector adaptor in the ENC of the current thread:
 * <pre>
 * MemoryContext root;
 * MemoryContext ctx;
 *
 * <font color="red">// Construct a non-shared memory context</font>
 * root = new MemoryContext();
 * <font color="red">// Create comp/UserTransaction and comp/env/mydb</font>
 * ctx = root.createSubcontext( "comp" );
 * ctx.bind( "UserTransaction", ut );
 * ctx = ctx.createSubcontext( "env" );
 * ctx = ctx.createSubcontext( "jdbc" );
 * ctx = ctx.bind( "mydb", jdbcAdapter );
 * . . .
 * <font color="red">// Make the context visible to the current thread</font>
 * EnvContext.setEnvContext( root );
 * . . .
 * <font color="red">// Application code</font>
 * InitialContext ctx;
 *
 * ctx = new InitialContext();
 * ds = (DataSource) ctx.lookup( "comp/env/mydb" );
 * . . .
 * <font color="red">// Make the context invisible</font>
 * EnvContext.unsetEnvContext();
 * </pre>
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.6 $ $Date: 2000/09/08 23:05:19 $
 */
public final class EnvContext
    implements Context, Serializable
{


    private static FastThreadLocal _local = new FastThreadLocal();


    /**
     * Holds the bindings associated with this context. This field
     * is transient. If this context is serialized/deserialized, the
     * binding will be lost and will be re-acquired in {@link
     * #getBinding}.
     */
    private transient MemoryBinding  _bindings;


    /**
     * The environment attributes used to construct this context.
     * Will be passed on to all contexts constructed by this context.
     */
    private Hashtable                _env = new Hashtable();


    /**
     * The path of this context. Required in order to reassociate
     * with a binding when de-serialized.
     */
    private String                   _path;


    /**
     * Construct a new context for the root path.
     */
    public EnvContext( Hashtable env )
	throws NamingException
    {
	Enumeration enum;
	String      name;

	// Use addToEnvironment to duplicate the environment variables.
	// This takes care of setting certain flags appropriately.
	if ( env != null ) {
	    enum = env.keys();
	    while ( enum.hasMoreElements() ) {
		name = (String) enum.nextElement();
		addToEnvironment( name, env.get( name ) );
	    }
	} else
	    env = new Hashtable();
        _path = null;
    }


    /**
     * Construct a new context with the specified bindings and
     * environment attributes.
     */
    EnvContext( MemoryBinding bindings, Hashtable env )
	throws NamingException
    {
	Enumeration enum;
	String      name;

	_bindings = bindings;
        _path = _bindings.getName();
        if ( _path.length() == 0 )
            _path = null;

	// Use addToEnvironment to duplicate the environment variables.
	// This takes care of setting certain flags appropriately.
	if ( env != null ) {
	    enum = env.keys();
	    while ( enum.hasMoreElements() ) {
		name = (String) enum.nextElement();
		addToEnvironment( name, env.get( name ) );
	    }
	} else
	    env = new Hashtable();
    }


    //--------//
    // Lookup //
    //--------//


    public Object lookup( String name )
	throws NamingException
    {
	Object object;

	// This is a simple case optimization of the composite name lookup.
	// It only applies if we're looking for a simple name that is
	// directly reachable in this context, otherwise, we default to the
	// composite name lookup.
	object = getBindings().get( name );
	if ( object != null ) {
	    if ( object instanceof LinkRef ) {
		    // Found a link reference that we must follow. If the link
		    // starts with a '.' we use it's name to do a look underneath
		    // this context. Otherwise, we continue looking from some
		    // initial context.
		    String link;
		    
		    link = ( (LinkRef) object ).getLinkName();
		    if ( link.startsWith( "." ) )
			return lookup( link.substring( 1 ) );
		    else
                        return NamingManager.getInitialContext( _env ).lookup( link );
	    } else if ( object instanceof MemoryBinding ) {
		// If we found a subcontext, we must return a new context
		// to represent it and keep the environment set for this
		// context (e.g. read-only).
		return new EnvContext( (MemoryBinding) object, _env );
            } else if ( object instanceof Reference ) {
                // Reconstruct a reference.
                try {
                    return NamingManager.getObjectInstance( object, new CompositeName( name ), this, _env );
                } catch ( Exception except ) {
                    throw new NamingException( except.toString() );
                }
	    } else {
		// Simplest case, just return the bound object.
		return object;
	    }
	} else
            return internalLookup( new CompositeName( name ), true );
    }


    public Object lookup( Name name )
	throws NamingException
    {
        return internalLookup( name, true );
    }


    public Object lookupLink( String name )
	throws NamingException
    {
        return internalLookup( new CompositeName( name ), false );
    }


    public Object lookupLink( Name name )
	throws NamingException
    {
        return internalLookup( name, false );
    }


    private Object internalLookup( Name name, boolean resolveLinkRef )
	throws NamingException
    {
	String        simple;
	Object        object;
	MemoryBinding bindings;

	// Start this this context's direct binding. As we iterate through
	// composite names and links, we will change bindings all the time.
	bindings = getBindings();

	// This loop is executed for as long as name has more the on part.
	// At each iteration, the first part of the name is used to lookup
	// a subcontext and the second part passes on to the iteration.
	// If a link is found, the link's name is used in the next iteration
	// step.
	while ( true ) {

	    // If the first part of the name contains empty parts, we discard
	    // them and keep on looking in this context. If the name is empty,
	    // we create a new context similar to this one.
	    while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
		name = name.getSuffix( 1 );
	    if ( name.isEmpty() )
		return new EnvContext( bindings, _env );

	    // Simple is the first part of the name for a composite name,
	    // for looking up the subcontext, or the last part of the
	    // name for looking up the binding.
	    simple = name.get( 0 );

	    if ( name.size() > 1 ) {
		// Composite name, keep looking in subcontext until we
		// find the binding.
		object = bindings.get( simple );
		if ( object instanceof Context ) {
		    // Found an external context, keep looking in that context.
		    return ( (Context) object ).lookup( name.getSuffix( 1 ) );
		} else if ( object instanceof MemoryBinding ) {
		    // Found another binding level, keep looking in that one.
		    bindings = (MemoryBinding) object;
		} else {
		    // Could not find another level for this name part,
		    // must report that name part is not a subcontext.
		    throw new NotContextException( simple + " is not a subcontext" );
		}
		name = name.getSuffix( 1 );
	    } else {
		// At this point name.size() == 1 and simple == name.get( 0 ).
		object = bindings.get( simple );
		if ( object == null )
		    throw new NameNotFoundException( simple + " not found" );
		else if ( object instanceof LinkRef && resolveLinkRef ) {
		    // Found a link reference that we must follow. If the link
		    // starts with a '.' we use it's name to do a look underneath
		    // this context. Otherwise, we continue looking from some
		    // initial context.
		    String link;
		    
		    link = ( (LinkRef) object ).getLinkName();
		    if ( link.startsWith( "." ) ) {
			name = new CompositeName( link.substring( 1 ) );
			continue; // Reiterate
		    } else
                        return NamingManager.getInitialContext( _env ).lookup( link );
		} else if ( object instanceof MemoryBinding ) {
		    // If we found a subcontext, we must return a new context
		    // to represent it and keep the environment set for this
		    // context (e.g. read-only).
		    return new EnvContext( (MemoryBinding) object, _env );
                } else if ( object instanceof Reference ) {
                    // Reconstruct a reference
                    try {
                        return NamingManager.getObjectInstance( object, name,
                                                                new EnvContext( bindings, _env ), _env );
                    } catch ( Exception except ) {
                        throw new NamingException( except.getMessage() );
                    }
		} else {
		    // Simplest case, just return the bound object.
		    return object;
		}
	    }
	}
    }


    //---------//
    // Binding //
    //---------//


    public void bind( String name, Object value )
	throws NamingException
    {
	throw new OperationNotSupportedException( "Context is read-only" );
    }


    public void bind( Name name, Object value )
	throws NamingException
    {
	throw new OperationNotSupportedException( "Context is read-only" );
    }


    public void rebind( String name, Object value )
	throws NamingException
    {
	throw new OperationNotSupportedException( "Context is read-only" );
    }


    public void rebind( Name name, Object value )
	throws NamingException
    {
	throw new OperationNotSupportedException( "Context is read-only" );
    }


    public void unbind( String name )
	throws NamingException
    {
	throw new OperationNotSupportedException( "Context is read-only" );
    }


    public void unbind( Name name )
	throws NamingException
    {
	throw new OperationNotSupportedException( "Context is read-only" );
    }


    public void rename( String oldName, String newName )
	throws NamingException
    {
	throw new OperationNotSupportedException( "Context is read-only" );
    }


    public void rename( Name oldName, Name newName )
	throws NamingException
    {
	throw new OperationNotSupportedException( "Context is read-only" );
    }


    //---------//
    // Listing //
    //---------//


    public NamingEnumeration list( String name )
	throws NamingException
    {
	if ( name.length() == 0 )
	    return new MemoryBindingEnumeration( getBindings(), true, this );
	else
	    return list( new CompositeName( name ) );
    }


    public NamingEnumeration list( Name name )
	throws NamingException
    {
	Object        object;
	String        simple;
	MemoryBinding bindings;

	// If the first part of the name contains empty parts, we discard
	// them and keep on looking in this context.
	while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
	    name = name.getSuffix( 1 );
	if ( name.isEmpty() )
	    return new MemoryBindingEnumeration( getBindings(), true, this );
	
	// Simple is the first part of the name for a composite name,
	// for looking up the subcontext, or the last part of the
	// name for looking up the binding.
	simple = name.get( 0 );
	bindings = getBindings();
	
	while ( name.size() > 1 ) {
	    // Composite name, keep looking in subcontext until we
	    // find the binding.
	    object = bindings.get( simple );
	    if ( object instanceof Context ) {
		// Found an external context, keep looking in that context.
		return ( (Context) object ).list( name.getSuffix( 1 ) );
	    } else if ( object instanceof MemoryBinding ) {
		// Found another binding level, keep looking in that one.
		bindings = (MemoryBinding) object;
	    } else {
		// Could not find another level for this name part,
		// must report that name part is not a subcontext.
		throw new NotContextException( simple + " is not a subcontext" );
	    }
	    name = name.getSuffix( 1 );
	    simple = name.get( 0 );
	}

	// The end of the name is either '.' in which case list the
	// last bindings reached so far, or a name part in which case
	// lookup that binding and list it.
	if ( simple.length() == 0 )
	    return new MemoryBindingEnumeration( bindings, true, this );
	object = bindings.get( simple );
	if ( object instanceof Context )
	    return ( (Context) object ).list( "" );
	else if ( object instanceof MemoryBinding )
	    return new MemoryBindingEnumeration( (MemoryBinding) object, true, this );
	else
	    throw new NotContextException( simple + " is not a subcontext" );	    
    }
    

    public NamingEnumeration listBindings( String name )
	throws NamingException
    {
	if ( name.length() == 0 )
	    return new MemoryBindingEnumeration( getBindings(), false, this );
	else
	    return listBindings( new CompositeName( name ) );
    }


    public NamingEnumeration listBindings( Name name )
	throws NamingException
    {
	Object        object;
	String        simple;
	MemoryBinding bindings;

	// If the first part of the name contains empty parts, we discard
	// them and keep on looking in this context.
	while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
	    name = name.getSuffix( 1 );
	if ( name.isEmpty() )
	    return new MemoryBindingEnumeration( getBindings(), false, this );
	
	// Simple is the first part of the name for a composite name,
	// for looking up the subcontext, or the last part of the
	// name for looking up the binding.
	simple = name.get( 0 );
	bindings = getBindings();
	
	while ( name.size() > 1 ) {
	    // Composite name, keep looking in subcontext until we
	    // find the binding.
	    object = bindings.get( simple );
	    if ( object instanceof Context ) {
		// Found an external context, keep looking in that context.
		return ( (Context) object ).listBindings( name.getSuffix( 1 ) );
	    } else if ( object instanceof MemoryBinding ) {
		// Found another binding level, keep looking in that one.
		bindings = (MemoryBinding) object;
	    } else {
		// Could not find another level for this name part,
		// must report that name part is not a subcontext.
		throw new NotContextException( simple + " is not a subcontext" );
	    }
	    name = name.getSuffix( 1 );
	    simple = name.get( 0 );
	}

	// The end of the name is either '.' in which case list the
	// last bindings reached so far, or a name part in which case
	// lookup that binding and list it.
	if ( simple.length() == 0 )
	    return new MemoryBindingEnumeration( bindings, false, this );
	object = bindings.get( simple );
	if ( object instanceof Context )
	    return ( (Context) object ).listBindings( "" );
	else if ( object instanceof MemoryBinding )
	    return new MemoryBindingEnumeration( (MemoryBinding) object, false, this );
	else
	    throw new NotContextException( simple + " is not a subcontext" );	    
    }
    

    //-------------//
    // Subcontexts //
    //-------------//


    public Context createSubcontext( String name )
	throws NamingException
    {
	throw new OperationNotSupportedException( "Context is read-only" );
    }


    public Context createSubcontext( Name name )
	throws NamingException
    {
	throw new OperationNotSupportedException( "Context is read-only" );
    }

    
    public void destroySubcontext( String name )
	throws NamingException
    {
	throw new OperationNotSupportedException( "Context is read-only" );
    }


    public void destroySubcontext( Name name )
	throws NamingException
    {
	throw new OperationNotSupportedException( "Context is read-only" );
    }


    //--------------------//
    // Naming composition //
    //--------------------//


    public NameParser getNameParser( String name )
	throws NamingException
    {
	if ( name.length() == 0 )
	    return MemoryContext.DefaultNameParser;
	return getNameParser( new CompositeName( name ) );
    }


    public NameParser getNameParser( Name name )
	throws NamingException
    {
	String        simple;
	MemoryBinding bindings;
	Object        object;

	// If the first part of the name contains empty parts, we discard
	// them and keep on looking in this context.
	while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
	    name = name.getSuffix( 1 );
	if ( name.isEmpty() )
	    return MemoryContext.DefaultNameParser;
	
	// Simple is the first part of the name for a composite name,
	// for looking up the subcontext, or the last part of the
	// name for looking up the binding.
	simple = name.get( 0 );
	bindings = getBindings();
	
	while ( name.size() > 1 ) {
	    // Composite name, keep looking in subcontext until we
	    // find the binding.
	    object = bindings.get( simple );
	    if ( object instanceof Context ) {
		// Found an external context, keep looking in that context.
		return ( (Context) object ).getNameParser( name.getSuffix( 1 ) );
	    } else if ( object instanceof MemoryBinding ) {
		// Found another binding level, keep looking in that one.
		bindings = (MemoryBinding) object;
	    } else {
		// Could not find another level for this name part,
		// must report that name part is not a subcontext.
		throw new NotContextException( simple + " is not a subcontext" );
	    }
	    name = name.getSuffix( 1 );
	    simple = name.get( 0 );
	}

	return MemoryContext.DefaultNameParser;
    }


    public Name composeName( Name name, Name prefix )
	throws NamingException
    {
	prefix = (Name) name.clone();
	return prefix.addAll( name );
    }


    public String composeName( String name, String prefix )
    {
	return prefix + MemoryContext.NameSeparator + name;
    }


    public String getNameInNamespace()
	throws NamingException
    {
	return ( _path == null ? "" : _path );
    }
    

    //-------------//
    // Environment //
    //-------------//


    public Object addToEnvironment( String name, Object value )
	throws NamingException
    {
	return _env.put( name, value );
    }


    public Hashtable getEnvironment()
    {
	return _env;
    }


    public Object removeFromEnvironment( String name )
    {
	return _env.remove( name );
    }


    public void close()
    {
	_env.clear();
    }


    public String toString()
    {
	return ( _path == null ? "" : _path );
    }


    //-------------------//
    // Thread Management //
    //-------------------//


    /**
     * Sets the environment naming context for this thread with the
     * bindings represented by the particular root context. These
     * bindings will be exposed the next time an <tt>InitialContext</tt>
     * is created in this thread.
     * <p>
     * All calls to {@link #setEnvContext} and {@link #unsetEnvContext}
     * must be well balanced.
     * <p>
     * The caller must have permission {@link NamingPermission#ENC}.
     *
     * @param context The context
     * @throws IllegalArgumentException The context is not a root
     *  context
     * @throws SecurityException Caller does not have adequate
     *  permission to set/unset context
     */
    public static void setEnvContext( Context context )
        throws SecurityException, IllegalArgumentException
    {
        ThreadMemoryBinding thread;
        MemoryBinding       bindings;

        AccessController.checkPermission( NamingPermission.ENC );

        if ( ! ( context instanceof MemoryContext ) )
            throw new IllegalArgumentException( "The argument 'context' was not created from " +
                                                MemoryContextFactory.class.getName() );
        bindings = ( (MemoryContext) context ).getBindings();
        if ( ! bindings.isRoot() )
            throw new IllegalArgumentException( "The argument 'context' is not a root context" );
        thread = (ThreadMemoryBinding) _local.get();
        thread = new ThreadMemoryBinding( bindings, thread );
        _local.set( thread );
    }


    /**
     * Unsets the environment naming context for this thread that was
     * previously set with {@link #setEnvContext}. If a previous
     * bindings were used by a outer call to {@link #setEnvContext},
     * these bindings are restored.
     * <p>
     * The caller must have permission {@link NamingPermission#ENC}.
     *
     * @throws SecurityException Caller does not have adequate
     *  permission to set/unset context
     */
    public static void unsetEnvContext()
        throws SecurityException
    {
        ThreadMemoryBinding thread;

        AccessController.checkPermission( NamingPermission.ENC );

        thread = (ThreadMemoryBinding) _local.get();
        if ( thread != null )
            _local.set( thread.prev );
    }


    /**
     * Returns the bindings associated with this context. If the
     * context has been serialized and deserialized, the reference
     * will be null and must be reconstructed from the binding
     * held in memory. If the JNDI ENC was not populated for this
     * thread, a naming exception will be thrown.
     */
    private MemoryBinding getBindings()
        throws NamingException
    {
        if ( _bindings != null )
            return _bindings;
        
        MemoryBinding       bindings;
        ThreadMemoryBinding thread;
        StringTokenizer     tokenizer;
        String              token;
        Object              value;

        thread = (ThreadMemoryBinding) _local.get();
        if ( thread == null )
	    throw new NamingException( "Error: Environment naming context (java:comp) not bound to this thread" );
        bindings = thread.bindings;
        if ( _path != null ) {
            tokenizer = new StringTokenizer( _path, MemoryContext.NameSeparator );
            while ( tokenizer.hasMoreTokens() ) {
                token = tokenizer.nextToken();
                if ( token.length() > 0 ) {
                    value = (MemoryBinding) bindings.get( token );
                    if ( value == null || ! ( value instanceof MemoryBinding ) )
                        throw new NamingException( "Error: Environment naming context path java:" + _path +
                                                   " no longer bound to this thread" );
                    bindings = (MemoryBinding) value;
                }
            }
        }
        _bindings = bindings;
        return bindings;
    }


    /**
     * An association between the current thread and memory
     * binding for the ENC. A linked list is used to support
     * stack pop/push on the current thread.
     */
    static class ThreadMemoryBinding
    {

        final MemoryBinding       bindings;

        final ThreadMemoryBinding prev;

        ThreadMemoryBinding( MemoryBinding bindings, ThreadMemoryBinding prev )
        {
            this.bindings = bindings;
            this.prev = prev;
        }

    }


}



