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
 * $Id: JavaContext.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.naming;


import java.io.Serializable;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Dictionary;
import java.security.AccessController;
import javax.naming.*;


/**
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
 */
public final class JavaContext
    extends Constants
    implements Context, Serializable
{


    private static ThreadLocal  _local = new ThreadLocal();


    private Hashtable           _env = new Hashtable();



    public JavaContext()
	throws NamingException
    {
	this ( null );
    }


    public JavaContext( Hashtable env )
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
	} else {
	    _env = new Hashtable();
	}
    }


    public static TyrexContext suspendContext()
	throws NamingException
    {
	TyrexContext old;

	try {
	    AccessController.checkPermission( Permission.JavaContext );
	} catch ( SecurityException except ) {
	    throw new NoPermissionException( "Caller has no permission to reset context" );
	}
	old = (TyrexContext) _local.get();
	_local.set( null );
	return old;
    }


    public static TyrexContext resumeContext( TyrexContext ctx )
	throws NamingException
    {
	MemoryBinding bindings;
	Hashtable     env;
	TyrexContext    old;

	try {
	    AccessController.checkPermission( Permission.JavaContext );
	} catch ( SecurityException except ) {
	    throw new NoPermissionException( "Caller has no permission to set context" );
	}
	bindings = ctx.getBindings();
	bindings.setContext( null, URL.Java );
	env = new Hashtable();
	env.put( TyrexContext.Environment.ReadOnly, "true" );
	ctx = new TyrexContext( bindings, env );
	old = (TyrexContext) _local.get();
	_local.set( ctx );
	return old;
    }


    public static Context getContext( Hashtable env, boolean create )
	throws NamingException
    {
	MemoryBinding bindings;
	Hashtable     cenv;
	TyrexContext    ctx;

	try {
	    AccessController.checkPermission( Permission.JavaContext );
	} catch ( SecurityException except ) {
	    throw new NoPermissionException( "Caller has no permission to get context" );
	}
	ctx = (TyrexContext) _local.get();
	if ( ctx == null ) {
	    if ( ! create )
		return null;
	    bindings = new MemoryBinding();
	    cenv = new Hashtable();
	    cenv.put( TyrexContext.Environment.ReadOnly, "true" );
	    ctx = new TyrexContext( bindings, cenv );
	    _local.set( ctx );
	} else {
	    bindings = ctx.getBindings();
	}
	env.put( TyrexContext.Environment.ReadOnly, "true" );
	return new TyrexContext( bindings, env );
    }


    //--------//
    // Lookup //
    //--------//

    public Object lookup( String name )
	throws NamingException
    {
	TyrexContext ctx;

	if ( ! name.startsWith( URL.Java ) )
	    throw new NamingException( "Internal error: context not accessed as java URL.Java" );
	ctx = (TyrexContext) _local.get();
	if ( ctx == null )
	    throw new NameNotFoundException( name + " not found" );
	return ctx.lookup( name.substring( 5 ) );
    }


    public Object lookup( Name name )
	throws NamingException
    {
	TyrexContext ctx;

	if ( name.isEmpty() || ! name.get( 0 ).equals( URL.Java ) )
	    throw new NamingException( "Internal error: context not accessed as java URL.Java" );
	ctx = (TyrexContext) _local.get();
	if ( ctx == null )
	    throw new NameNotFoundException( name + " not found" );
	return ctx.lookup( name.getSuffix( 1 ) );
    }


    public Object lookupLink( String name )
	throws NamingException
    {
	TyrexContext ctx;

	if ( ! name.startsWith( URL.Java ) )
	    throw new NamingException( "Internal error: context not accessed as java URL.Java" );
	ctx = (TyrexContext) _local.get();
	if ( ctx == null )
	    throw new NameNotFoundException( name + " not found" );
	return ctx.lookup( name.substring( 5 ) );
    }


    public Object lookupLink( Name name )
	throws NamingException
    {
	TyrexContext ctx;

	if ( name.isEmpty() || ! name.get( 0 ).equals( URL.Java ) )
	    throw new NamingException( "Internal error: context not accessed as java URL.Java" );
	ctx = (TyrexContext) _local.get();
	if ( ctx == null )
	    throw new NameNotFoundException( name + " not found" );
	return ctx.lookupLink( name.getSuffix( 1 ) );
    }


    //--------//
    // Lookup //
    //--------//


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
	TyrexContext ctx;

	if ( ! name.startsWith( URL.Java ) )
	    throw new NamingException( "Internal error: context not accessed as java URL.Java" );
	ctx = (TyrexContext) _local.get();
	if ( ctx == null )
	    throw new NameNotFoundException( name + " not found or java: not bound for this thread" );
	return ctx.list( name.substring( 5 ) );
    }


    public NamingEnumeration list( Name name )
	throws NamingException
    {
	TyrexContext ctx;

	if ( name.isEmpty() || ! name.get( 0 ).equals( URL.Java ) )
	    throw new NamingException( "Internal error: context not accessed as java URL.Java" );
	ctx = (TyrexContext) _local.get();
	if ( ctx == null )
	    throw new NameNotFoundException( name + " not found or java: not bound for this thread" );
	return ctx.list( name.getSuffix( 1 ) );
    }
    

    public NamingEnumeration listBindings( String name )
	throws NamingException
    {
	TyrexContext ctx;

	if ( ! name.startsWith( URL.Java ) )
	    throw new NamingException( "Internal error: context not accessed as java URL.Java" );
	ctx = (TyrexContext) _local.get();
	if ( ctx == null )
	    throw new NameNotFoundException( name + " not found or java: not bound for this thread" );
	return ctx.listBindings( name.substring( 5 ) );
    }


    public NamingEnumeration listBindings( Name name )
	throws NamingException
    {
	TyrexContext ctx;

	if ( name.isEmpty() || ! name.get( 0 ).equals( URL.Java ) )
	    throw new NamingException( "Internal error: context not accessed as java URL.Java" );
	ctx = (TyrexContext) _local.get();
	if ( ctx == null )
	    throw new NameNotFoundException( name + " not found or java: not bound for this thread" );
	return ctx.listBindings( name.getSuffix( 1 ) );
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
	TyrexContext ctx;

	if ( ! name.startsWith( URL.Java ) )
	    throw new NamingException( "Internal error: context not accessed as java URL.Java" );
	ctx = (TyrexContext) _local.get();
	if ( ctx == null )
	    throw new NameNotFoundException( name + " not found or java: not bound for this thread" );
	return ctx.getNameParser( name.substring( 5 ) );
    }


    public NameParser getNameParser( Name name )
	throws NamingException
    {
	TyrexContext ctx;

	if ( name.isEmpty() || ! name.get( 0 ).equals( URL.Java ) )
	    throw new NamingException( "Internal error: context not accessed as java URL.Java" );
	ctx = (TyrexContext) _local.get();
	if ( ctx == null )
	    throw new NameNotFoundException( name + " not found or java: not bound for this thread" );
	return ctx.getNameParser( name.getSuffix( 1 ) );
    }


    public Name composeName( Name name, Name prefix )
	throws NamingException
    {
	prefix = (Name) name.clone();
	return prefix.addAll( name );
    }


    public String composeName( String name, String prefix )
    {
	return prefix + Naming.Separator + name;
    }


    public String getNameInNamespace()
	throws NamingException
    {
	// This is always java:
	return URL.Java;
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
	_env = null;
    }


    public String toString()
    {
	return URL.Java;
    }


    void debug( PrintWriter writer )
    {
	TyrexContext ctx;

	ctx = (TyrexContext) _local.get();
	if ( ctx == null )
	    writer.println( "java: no thread association" );
	else {
	    writer.println( "java: for this thread:" );
	    ctx.debug( writer );
	}
    }


}


