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
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: MemoryContext.java,v 1.3 2000/04/12 02:37:16 arkin Exp $
 */


package tyrex.naming;


import java.io.PrintWriter;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Dictionary;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.CompositeName;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NameParser;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.NameNotFoundException;
import javax.naming.OperationNotSupportedException;
import javax.naming.InvalidNameException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.ContextNotEmptyException;


/**
 * An in-memory JNDI service provider. Binds objects into a namespace
 * held entirely in memory, supporting serializable, remoteable and
 * local objects. The in-memory service provider is particularly useful
 * for holding resource factories (the JNDI ENC) and exposing run-time
 * services and configuration objects.
 * <p>
 * An instance of {@link MemoryContext} constructed with no environment
 * attribute will use it's namespace and serve as the root of that namespace.
 * Such a namespace is no accessible except through the creating context,
 * and is garbage collected when all such contexts are no longer
 * referenced. If necessary the root context can be duplicated using
 * <tt>lookup( "" )</tt>.
 * <p>
 * If the environment attribute {@link Context.PROVIDER_URL} is set,
 * the context will reference a node in a namespace shared by all such
 * contexts. That tree is statically held in memory for the life time
 * of the virtual machine.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/04/12 02:37:16 $
 * @see MemoryContextFactory
 */
public final class MemoryContext
    implements Context
{


    /**
     * Environment attribute to set a context read-only. The value
     * must be a string equal to <tt>true</tt>. Once the context has
     * been set read-only, it cannot be reset to read-write.
     */
    public static final String ReadOnly = "readOnly";


    /**
     * The default name separator for this context is '/'.
     */
    public static final String  NameSeparator = "/";
    

    /**
     * The default name parser for this context.
     */    
    public static final NameParser DefaultNameParser =
        new NameParser() {
            
            public Name parse( String name )
            throws NamingException
            {
                // We only deal with the standard composite names.
                return new CompositeName( name );
            }
            
        };
    
    
    /**
     * Holds the bindings associated with this context. Multiple
     * contexts may share the same binding. The binding is selected
     * based on the {@link Context.PROVIDER_URL} attribute. The
     * context's name in the name space is know to the bindings.
     */
    private final MemoryBinding  _bindings;


    /**
     * The environment attributes used to construct this context.
     * Will be passed on to all contexts constructed by this context.
     */
    private final Hashtable     _env = new Hashtable();


    /**
     * True if this context has been set read-only. Once it has
     * been set read-only, it cannot revert to writable and all
     * contexts returns by this context are read-only.
     */
    private boolean             _readOnly;


    /**
     * Construct a new context with the specified environment
     * attributes. The environment property {@link Context.PROVIDER_URL}
     * names the underlying bindings. If the property is absent, the
     * returned context has it's own binding space which is not shared
     * with other contexts created in this manner.
     *
     * @param env The environment attributes
     * @throws NotContextException The attribute {@link
     *   Context.PROVIDER_URL} does not specify a context
     * @throws InvalidNameException The attribute {@link
     *   Context.PROVIDER_URL} is an invalid name
     */
    public MemoryContext( Hashtable env )
	throws NamingException
    {
	Enumeration enum;
	String      name;

	// Use addToEnvironment to duplicate the environment variables.
	// This takes care of setting certain flags appropriately.
	if ( env != null ) {
            if ( env.get( PROVIDER_URL ) != null )
                _bindings = MemoryContextFactory.getBindings( env.get( PROVIDER_URL ).toString() );
            else
                _bindings = new MemoryBinding();
	    enum = env.keys();
	    while ( enum.hasMoreElements() ) {
		name = (String) enum.nextElement();
		addToEnvironment( name, env.get( name ) );
	    }
	} else
            _bindings = new MemoryBinding();
    }


    /**
     * Construct a new context with the specified bindings and
     * environment attributes.
     */
    MemoryContext( MemoryBinding bindings, Hashtable env )
	throws NamingException
    {
	Enumeration enum;
	String      name;

	_bindings = bindings;

	// Use addToEnvironment to duplicate the environment variables.
	// This takes care of setting certain flags appropriately.
	if ( env != null ) {
	    enum = env.keys();
	    while ( enum.hasMoreElements() ) {
		name = (String) enum.nextElement();
		addToEnvironment( name, env.get( name ) );
	    }
	}
    }


    //--------//
    // Lookup //
    //--------//

    public Object lookup( String name )
	throws NamingException
    {
	Object obj;

	// This is a simple case optimization of the composite name lookup.
	// It only applies if we're looking for a simple name that is
	// directly reachable in this context, otherwise, we default to the
	// composite name lookup.
	obj = _bindings.get( name );
	if ( obj != null ) {
	    if ( obj instanceof LinkRef ) {
		    // Found a link reference that we must follow. If the link
		    // starts with a '.' we use it's name to do a look underneath
		    // this context. Otherwise, we continue looking from some
		    // initial context.
		    String link;
		    
		    link = ( (LinkRef) obj ).getLinkName();
		    if ( link.startsWith( "." ) ) {
			return lookup( link.substring( 1 ) );
		    } else {
			return lookupInitialContext( link );
		    }
	    } else if ( obj instanceof MemoryBinding ) {
		// If we found a subcontext, we must return a new context
		// to represent it and keep the environment set for this
		// context (e.g. read-only).
		return new MemoryContext( (MemoryBinding) obj, _env );
	    } else {
		// Simplest case, just return the bound object.
		return obj;
	    }
	} else {
	    return lookup( new CompositeName( name ) );
	}
    }


    public Object lookup( Name name )
	throws NamingException
    {
	String        simple;
	Object        obj;
	MemoryBinding bindings;

	// Start this this context's direct binding. As we iterate through
	// composite names and links, we will change bindings all the time.
	bindings = _bindings;

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
		return new MemoryContext( _bindings, _env );

	    // Simple is the first part of the name for a composite name,
	    // for looking up the subcontext, or the last part of the
	    // name for looking up the binding.
	    simple = name.get( 0 );

	    if ( name.size() > 1 ) {
		// Composite name, keep looking in subcontext until we
		// find the binding.
		obj = bindings.get( simple );
		if ( obj instanceof Context ) {
		    // Found an external context, keep looking in that context.
		    return ( (Context) obj ).lookup( name.getSuffix( 1 ) );
		} else if ( obj instanceof MemoryBinding ) {
		    // Found another binding level, keep looking in that one.
		    bindings = (MemoryBinding) obj;
		} else {
		    // Could not find another level for this name part,
		    // must report that name part is not a subcontext.
		    throw new NotContextException( simple + " is not a subcontext" );
		}
		name = name.getSuffix( 1 );
	    } else {
		// At this point name.size() == 1 and simple == name.get( 0 ).
		obj = bindings.get( simple );
		if ( obj == null )
		    throw new NameNotFoundException( simple + " not found" );
		else if ( obj instanceof LinkRef ) {
		    // Found a link reference that we must follow. If the link
		    // starts with a '.' we use it's name to do a look underneath
		    // this context. Otherwise, we continue looking from some
		    // initial context.
		    String link;
		    
		    link = ( (LinkRef) obj ).getLinkName();
		    if ( link.startsWith( "." ) ) {
			name = new CompositeName( link.substring( 1 ) );
			continue; // Reiterate
		    } else {
			return lookupInitialContext( link );
		    }
		} else if ( obj instanceof MemoryBinding ) {
		    // If we found a subcontext, we must return a new context
		    // to represent it and keep the environment set for this
		    // context (e.g. read-only).
		    return new MemoryContext( (MemoryBinding) obj, _env );
		} else {
		    // Simplest case, just return the bound object.
		    return obj;
		}
	    }

	}
    }


    public Object lookupLink( String name )
	throws NamingException
    {
	return lookupLink( new CompositeName( name ) );
    }


    public Object lookupLink( Name name )
	throws NamingException
    {
	String        simple;
	Object        obj;
	MemoryBinding bindings;

	// Start this this context's direct binding. As we iterate through
	// composite names and links, we will change bindings all the time.
	bindings = _bindings;

	// This loop is executed for as long as name has more the on part.
	// At each iteration, the first part of the name is used to lookup
	// a subcontext and the second part passes on to the iteration.
	while ( true ) {

	    // If the first part of the name contains empty parts, we discard
	    // them and keep on looking in this context. If the name is empty,
	    // we create a new context similar to this one.
	    while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
		name = name.getSuffix( 1 );
	    if ( name.isEmpty() )
		return new MemoryContext( _bindings, _env );

	    // Simple is the first part of the name for a composite name,
	    // for looking up the subcontext, or the last part of the
	    // name for looking up the binding.
	    simple = name.get( 0 );

	    if ( name.size() > 1 ) {
		// Composite name, keep looking in subcontext until we
		// find the binding.
		obj = bindings.get( simple );
		if ( obj instanceof Context ) {
		    // Found an external context, keep looking in that context.
		    return ( (Context) obj ).lookup( name.getSuffix( 1 ) );
		} else if ( obj instanceof MemoryBinding ) {
		    // Found another binding level, keep looking in that one.
		    bindings = (MemoryBinding) obj;
		} else {
		    // Could not find another level for this name part,
		    // must report that name part is not a subcontext.
		    throw new NotContextException( simple + " is not a subcontext" );
		}
		name = name.getSuffix( 1 );
	    } else {
		// At this point name.size() == 1 and simple == name.get( 0 ).
		obj = bindings.get( simple );
		if ( obj == null )
		    throw new NameNotFoundException( simple + " not found" );
		if ( obj instanceof MemoryBinding ) {
		    // If we found a subcontext, we must return a new context
		    // to represent it and keep the environment set for this
		    // context (e.g. read-only).
		    return new MemoryContext( (MemoryBinding) obj, _env );
		} else {
		    // Simplest case, just return the bound object.
		    return obj;
		}
	    }

	}
    }


    protected Object lookupInitialContext( String name )
	throws NamingException
    {
	InitialContext ctx;

	ctx = new InitialContext( _env );
	return ctx.lookup( name );
    }


    //--------//
    // Lookup //
    //--------//


    public void bind( String name, Object value )
	throws NamingException
    {
	bind( new CompositeName( name ), value );
    }


    public void bind( Name name, Object value )
	throws NamingException
    {
	String        simple;
	MemoryBinding bindings;
	Object        obj;

	if ( _readOnly )
	    throw new OperationNotSupportedException( "Context is read-only" );

        if ( value instanceof MemoryContext )
            value = ( (MemoryContext) value )._bindings;

	// If the first part of the name contains empty parts, we discard
	// them and keep on looking in this context.
	while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
	    name = name.getSuffix( 1 );
	if ( name.isEmpty() )
	    throw new InvalidNameException( "Cannot bind empty name" );
	
	// Simple is the first part of the name for a composite name,
	// for looking up the subcontext, or the last part of the
	// name for looking up the binding.
	simple = name.get( 0 );
	bindings = _bindings;
	
	while ( name.size() > 1 ) {
	    // Composite name, keep looking in subcontext until we
	    // find the binding.
	    obj = bindings.get( simple );
	    if ( obj instanceof Context ) {
		// Found an external context, keep looking in that context.
		( (Context) obj ).bind( name.getSuffix( 1 ), value );
		return;
	    } else if ( obj instanceof MemoryBinding ) {
		// Found another binding level, keep looking in that one.
		bindings = (MemoryBinding) obj;
	    } else {
		// Could not find another level for this name part,
		// must report that name part is not a subcontext.
		throw new NotContextException( simple + " is not a subcontext" );
	    }
	    name = name.getSuffix( 1 );
	    simple = name.get( 0 );
	}

	synchronized ( bindings ) {
	    // At this point name.size() == 1 and simple == name.get( 0 ).
	    if ( bindings.get( simple ) != null )
		    throw new NameAlreadyBoundException( simple + " already bound, use rebind instead" );
	    bindings.put( simple, value );
	}
    }


    public void rebind( String name, Object value )
	throws NamingException
    {
	rebind( new CompositeName( name ), value );
    }


    public void rebind( Name name, Object value )
	throws NamingException
    {
	String        simple;
	MemoryBinding bindings;
	Object        obj;

	if ( _readOnly )
	    throw new OperationNotSupportedException( "Context is read-only" );

        if ( value instanceof MemoryContext )
            value = ( (MemoryContext) value )._bindings;

	// If the first part of the name contains empty parts, we discard
	// them and keep on looking in this context.
	while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
	    name = name.getSuffix( 1 );
	if ( name.isEmpty() )
	    throw new InvalidNameException( "Cannot rebind empty name" );
	
	// Simple is the first part of the name for a composite name,
	// for looking up the subcontext, or the last part of the
	// name for looking up the binding.
	simple = name.get( 0 );
	bindings = _bindings;
	
	while ( name.size() > 1 ) {
	    // Composite name, keep looking in subcontext until we
	    // find the binding.
	    obj = bindings.get( simple );
	    if ( obj instanceof Context ) {
		// Found an external context, keep looking in that context.
		( (Context) obj ).rebind( name.getSuffix( 1 ), value );
		return;
	    } else if ( obj instanceof MemoryBinding ) {
		// Found another binding level, keep looking in that one.
		bindings = (MemoryBinding) obj;
	    } else {
		// Could not find another level for this name part,
		// must report that name part is not a subcontext.
		throw new NotContextException( simple + " is not a subcontext" );
	    }
	    name = name.getSuffix( 1 );
	    simple = name.get( 0 );
	}

	synchronized ( bindings ) {
	    // If the name is direct, we perform the rebinding in
	    // this context. This method is indempotent;
	    bindings.put( simple, value );
	}
    }


    public void unbind( String name )
	throws NamingException
    {
	unbind( new CompositeName( name ) );
    }


    public void unbind( Name name )
	throws NamingException
    {
	Object        obj;
	String        simple;
	MemoryBinding bindings;

	if ( _readOnly )
	    throw new OperationNotSupportedException( "Context is read-only" );

	// If the first part of the name contains empty parts, we discard
	// them and keep on looking in this context.
	while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
	    name = name.getSuffix( 1 );
	if ( name.isEmpty() )
	    throw new InvalidNameException( "Cannot unbind empty name" );
	
	// Simple is the first part of the name for a composite name,
	// for looking up the subcontext, or the last part of the
	// name for looking up the binding.
	simple = name.get( 0 );
	bindings = _bindings;
	
	while ( name.size() > 1 ) {
	    // Composite name, keep looking in subcontext until we
	    // find the binding.
	    obj = bindings.get( simple );
	    if ( obj instanceof Context ) {
		// Found an external context, keep looking in that context.
		( (Context) obj ).unbind( name.getSuffix( 1 ) );
		return;
	    } else if ( obj instanceof MemoryBinding ) {
		// Found another binding level, keep looking in that one.
		bindings = (MemoryBinding) obj;
	    } else {
		// Could not find another level for this name part,
		// must report that name part is not a subcontext.
		throw new NotContextException( simple + " is not a subcontext" );
	    }
	    name = name.getSuffix( 1 );
	    simple = name.get( 0 );
	}

	synchronized ( bindings ) {
	    // If the name is direct, we perform the unbinding in
	    // this context. This method is indempotent;
	    bindings.remove( simple );
	}
    }


    public void rename( String oldName, String newName )
	throws NamingException
    {
	rename( new CompositeName( oldName ), new CompositeName( newName ) );
    }


    public void rename( Name oldName, Name newName )
	throws NamingException
    {
	String        simple;
	MemoryBinding bindings;
	Object        obj;

	if ( _readOnly )
	    throw new OperationNotSupportedException( "Context is read-only" );

	// If the first part of the name contains empty parts, we discard
	// them and keep on looking in this context.
	while ( ! oldName.isEmpty() && oldName.get( 0 ).length() == 0 )
	    oldName = oldName.getSuffix( 1 );
	while ( ! newName.isEmpty() && newName.get( 0 ).length() == 0 )
	    newName = newName.getSuffix( 1 );
	if ( oldName.isEmpty() || newName.isEmpty() )
	    throw new InvalidNameException( "Cannot rename empty name" );
	
	// Simple is the first part of the name for a composite name,
	// for looking up the subcontext, or the last part of the
	// name for looking up the binding.
	simple = newName.get( 0 );
	bindings = _bindings;
	
	while ( newName.size() > 1 ) {
	    // Composite name, keep looking in subcontext until we
	    // find the binding.
	    obj = bindings.get( simple );
	    if ( obj instanceof Context ) {
		// Found an external context, keep looking in that context.
		( (Context) obj ).rename( newName.getSuffix( 1 ), oldName );
		return;
	    } else if ( obj instanceof MemoryBinding ) {
		// Found another binding level, keep looking in that one.
		bindings = (MemoryBinding) obj;
	    } else {
		// Could not find another level for this name part,
		// must report that name part is not a subcontext.
		throw new NotContextException( simple + " is not a subcontext" );
	    }
	    newName = newName.getSuffix( 1 );
	    simple = newName.get( 0 );
	}

	// At this point newName.size() == 1, simple == newName.get( 0 )
	// and the old name should be bound directly to bindings.
	synchronized ( bindings ) {
	    if ( bindings.get( simple ) != null )
		throw new NameAlreadyBoundException( simple + " already bound, use rebind to override" );
	    if ( oldName.size() == 1 ) {
		obj = bindings.remove( oldName.get( 0 ) );
		if ( obj == null )
		    throw new NameNotFoundException( oldName.get( 0 ) + " not found" );
	    } else {
		obj = lookup( oldName );
		unbind( oldName );
	    }
	    bindings.put( simple, obj );
	}
    }


    //---------//
    // Binding //
    //---------//


    public NamingEnumeration list( String name )
	throws NamingException
    {
	if ( name.length() == 0 )
	    return new MemoryBindingEnumeration( _bindings, true, this );
	else
	    return list( new CompositeName( name ) );
    }


    public NamingEnumeration list( Name name )
	throws NamingException
    {
	Object        obj;
	String        simple;
	MemoryBinding bindings;

	// If the first part of the name contains empty parts, we discard
	// them and keep on looking in this context.
	while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
	    name = name.getSuffix( 1 );
	if ( name.isEmpty() )
	    return new MemoryBindingEnumeration( _bindings, true, this );
	
	// Simple is the first part of the name for a composite name,
	// for looking up the subcontext, or the last part of the
	// name for looking up the binding.
	simple = name.get( 0 );
	bindings = _bindings;
	
	while ( name.size() > 1 ) {
	    // Composite name, keep looking in subcontext until we
	    // find the binding.
	    obj = bindings.get( simple );
	    if ( obj instanceof Context ) {
		// Found an external context, keep looking in that context.
		return ( (Context) obj ).list( name.getSuffix( 1 ) );
	    } else if ( obj instanceof MemoryBinding ) {
		// Found another binding level, keep looking in that one.
		bindings = (MemoryBinding) obj;
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
	obj = bindings.get( simple );
	if ( obj instanceof Context ) {
	    return ( (Context) obj ).list( "" );
	} else if ( obj instanceof MemoryBinding ) {
	    return new MemoryBindingEnumeration( (MemoryBinding) obj, true, this );
	} else {
	    throw new NotContextException( simple + " is not a subcontext" );	    
	}
    }
    

    public NamingEnumeration listBindings( String name )
	throws NamingException
    {
	if ( name.length() == 0 )
	    return new MemoryBindingEnumeration( _bindings, false, this );
	else
	    return listBindings( new CompositeName( name ) );
    }


    public NamingEnumeration listBindings( Name name )
	throws NamingException
    {
	Object        obj;
	String        simple;
	MemoryBinding bindings;

	// If the first part of the name contains empty parts, we discard
	// them and keep on looking in this context.
	while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
	    name = name.getSuffix( 1 );
	if ( name.isEmpty() )
	    return new MemoryBindingEnumeration( _bindings, false, this );
	
	// Simple is the first part of the name for a composite name,
	// for looking up the subcontext, or the last part of the
	// name for looking up the binding.
	simple = name.get( 0 );
	bindings = _bindings;
	
	while ( name.size() > 1 ) {
	    // Composite name, keep looking in subcontext until we
	    // find the binding.
	    obj = bindings.get( simple );
	    if ( obj instanceof Context ) {
		// Found an external context, keep looking in that context.
		return ( (Context) obj ).listBindings( name.getSuffix( 1 ) );
	    } else if ( obj instanceof MemoryBinding ) {
		// Found another binding level, keep looking in that one.
		bindings = (MemoryBinding) obj;
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
	obj = bindings.get( simple );
	if ( obj instanceof Context ) {
	    return ( (Context) obj ).listBindings( "" );
	} else if ( obj instanceof MemoryBinding ) {
	    return new MemoryBindingEnumeration( (MemoryBinding) obj, false, this );
	} else {
	    throw new NotContextException( simple + " is not a subcontext" );	    
	}
    }
    

    //-------------//
    // Subcontexts //
    //-------------//


    public Context createSubcontext( String name )
	throws NamingException
    {
	return createSubcontext( new CompositeName( name ) );
    }


    public Context createSubcontext( Name name )
	throws NamingException
    {
	Object        obj;
	String        simple;
	MemoryBinding bindings;
	MemoryBinding newBindings;

	if ( _readOnly )
	    throw new OperationNotSupportedException( "Context is read-only" );

	// If the first part of the name contains empty parts, we discard
	// them and keep on looking in this context.
	while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
	    name = name.getSuffix( 1 );
	if ( name.isEmpty() )
	    throw new InvalidNameException( "Subcontext name is empty" );
	
	// Simple is the first part of the name for a composite name,
	// for looking up the subcontext, or the last part of the
	// name for looking up the binding.
	simple = name.get( 0 );
	bindings = _bindings;
	
	while ( name.size() > 1 ) {
	    // Composite name, keep looking in subcontext until we
	    // find the binding.
	    obj = bindings.get( simple );
	    if ( obj instanceof Context ) {
		// Found an external context, keep looking in that context.
		return ( (Context) obj ).createSubcontext( name.getSuffix( 1 ) );
	    } else if ( obj instanceof MemoryBinding ) {
		// Found another binding level, keep looking in that one.
		bindings = (MemoryBinding) obj;
	    } else {
		// Could not find another level for this name part,
		// must report that name part is not a subcontext.
		throw new NotContextException( simple + " is not a subcontext" );
	    }
	    name = name.getSuffix( 1 );
	    simple = name.get( 0 );
	}

	synchronized ( bindings ) {
	    obj = bindings.get( simple );
	    // If subcontext already found, return a new context for
	    // that subcontext.
	    if ( obj != null ) {
		if ( obj instanceof Context ) {
		    return (Context) ( (Context) obj ).lookup( "" );
		} else if ( obj instanceof MemoryBinding ) {
		    return new MemoryContext( (MemoryBinding) obj, _env );
		} else {
		    throw new NameAlreadyBoundException( simple + " already bound" );
		}
	    }
	    // Create a new binding for the subcontex and return a
	    // new context.
	    newBindings = new MemoryBinding();
	    bindings.put( simple, newBindings );
	    return new MemoryContext( newBindings, _env );
	}
    }

    
    public void destroySubcontext( String name )
	throws NamingException
    {
	destroySubcontext( new CompositeName( name ) );
    }


    public void destroySubcontext( Name name )
	throws NamingException
    {
	Object        obj;
	String        simple;
	MemoryBinding bindings;

	if ( _readOnly )
	    throw new OperationNotSupportedException( "Context is read-only" );

	// If the first part of the name contains empty parts, we discard
	// them and keep on looking in this context.
	while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
	    name = name.getSuffix( 1 );
	if ( name.isEmpty() )
	    throw new InvalidNameException( "Subcontext name is empty" );
	
	// Simple is the first part of the name for a composite name,
	// for looking up the subcontext, or the last part of the
	// name for looking up the binding.
	simple = name.get( 0 );
	bindings = _bindings;
	
	while ( name.size() > 1 ) {
	    // Composite name, keep looking in subcontext until we
	    // find the binding.
	    obj = bindings.get( simple );
	    if ( obj instanceof Context ) {
		// Found an external context, keep looking in that context.
		( (Context) obj ).destroySubcontext( name.getSuffix( 1 ) );
		return;
	    } else if ( obj instanceof MemoryBinding ) {
		// Found another binding level, keep looking in that one.
		bindings = (MemoryBinding) obj;
	    } else {
		// Could not find another level for this name part,
		// must report that name part is not a subcontext.
		throw new NotContextException( simple + " is not a subcontext" );
	    }
	    name = name.getSuffix( 1 );
	    simple = name.get( 0 );
	}

	synchronized ( bindings ) {
	    obj = bindings.get( simple );
	    if ( obj == null )
		return;
	    if ( obj instanceof MemoryBinding ) {
		if ( ! ( (MemoryBinding) obj ).isEmpty() )
		    throw new ContextNotEmptyException( simple + " is not empty, cannot destroy" );
		( (MemoryBinding) obj ).destroy();
		bindings.remove( simple );
	    } else if ( obj instanceof Context ) {
		( (Context) obj ).close();
		bindings.remove( simple );
	    } else {
		throw new NotContextException( simple + " is not a subcontext" );
	    }
	}
    }


    //--------------------//
    // Naming composition //
    //--------------------//


    public NameParser getNameParser( String name )
	throws NamingException
    {
	if ( name.length() == 0 )
	    return DefaultNameParser;
	return getNameParser( new CompositeName( name ) );
    }


    public NameParser getNameParser( Name name )
	throws NamingException
    {
	String        simple;
	MemoryBinding bindings;
	Object        obj;

	// If the first part of the name contains empty parts, we discard
	// them and keep on looking in this context.
	while ( ! name.isEmpty() && name.get( 0 ).length() == 0 )
	    name = name.getSuffix( 1 );
	if ( name.isEmpty() )
	    return DefaultNameParser;
	
	// Simple is the first part of the name for a composite name,
	// for looking up the subcontext, or the last part of the
	// name for looking up the binding.
	simple = name.get( 0 );
	bindings = _bindings;
	
	while ( name.size() > 1 ) {
	    // Composite name, keep looking in subcontext until we
	    // find the binding.
	    obj = bindings.get( simple );
	    if ( obj instanceof Context ) {
		// Found an external context, keep looking in that context.
		return ( (Context) obj ).getNameParser( name.getSuffix( 1 ) );
	    } else if ( obj instanceof MemoryBinding ) {
		// Found another binding level, keep looking in that one.
		bindings = (MemoryBinding) obj;
	    } else {
		// Could not find another level for this name part,
		// must report that name part is not a subcontext.
		throw new NotContextException( simple + " is not a subcontext" );
	    }
	    name = name.getSuffix( 1 );
	    simple = name.get( 0 );
	}

	return DefaultNameParser;
    }


    public Name composeName( Name name, Name prefix )
	throws NamingException
    {
	prefix = (Name) name.clone();
	return prefix.addAll( name );
    }


    public String composeName( String name, String prefix )
    {
	return prefix + NameSeparator + name;
    }


    public String getNameInNamespace()
	throws NamingException
    {
	return _bindings.getName();
    }
    

    //-------------//
    // Environment //
    //-------------//


    public Object addToEnvironment( String name, Object value )
	throws NamingException
    {
	if ( name.equals( ReadOnly ) ) {
	    boolean readOnly;

	    readOnly = value.toString().equalsIgnoreCase( "true" );
	    if ( _readOnly && ! readOnly )
		throw new OperationNotSupportedException( "Context is read-only" );
	    _readOnly = readOnly;
	}
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
	if ( _readOnly )
	    return _bindings.getName() + " (read-only)";
	else
	    return _bindings.getName();
    }


    /**
     * Returns the bindings represented by this context.
     * Used when assigning a memory context into the ENC.
     */
    MemoryBinding getBindings()
    {
        return _bindings;
    }


    void debug( PrintWriter writer )
    {
	_bindings.debug( writer );
    }


}



