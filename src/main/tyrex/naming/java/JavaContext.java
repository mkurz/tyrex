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
 * $Id: JavaContext.java,v 1.3 2000/09/08 23:05:19 mohammed Exp $
 */


package tyrex.naming.java;


import java.io.Serializable;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Dictionary;
import java.security.AccessController;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.LinkRef;
import javax.naming.CompositeName;
import javax.naming.NotContextException;
import javax.naming.NameNotFoundException;
import javax.naming.InitialContext;
import javax.naming.OperationNotSupportedException;
import javax.naming.NamingEnumeration;
import javax.naming.NameParser;
import javax.naming.Name;
import tyrex.naming.EnvContext;
import tyrex.naming.MemoryContext;


/**
 * The <tt>java:</tt> URL portion of the environment naming context.
 * This object is merely a proxy into {@link EnvContext} and must
 * be returned by {@link javaURLContextFactory} to properly deal with
 * the URL part of a name lookup.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/08 23:05:19 $
 */
public final class JavaContext
    implements Context, Serializable
{


    /**
     * The environment naming context URL (<tt>java:</tt>).
     */
    public static final String JavaURL = "java:";


    /**
     * The length of the URL.
     */
    private static final int JavaURLLength = 5;


    /**
     * The environment variables of this ENC. Generally ignored.
     */
    private Hashtable           _env = new Hashtable();



    /**
     * Create a new environment context.
     */
    public JavaContext()
	throws NamingException
    {
        _env = new Hashtable();
    }


    /**
     * Create a new environment context.
     */
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


    //--------//
    // Lookup //
    //--------//

    public Object lookup( String name )
	throws NamingException
    {
	Context ctx;

	if ( ! name.startsWith( JavaURL ) )
	    throw new NamingException( "Internal error: context not accessed as java JavaURL" );
	ctx = new EnvContext( _env );
	return ctx.lookup( name.substring( JavaURLLength ) );
    }


    public Object lookup( Name name )
	throws NamingException
    {
	Context ctx;

	if ( name.isEmpty() || ! name.get( 0 ).equals( JavaURL ) )
	    throw new NamingException( "Internal error: context not accessed as java JavaURL" );
	ctx = new EnvContext( _env );
	return ctx.lookup( name.getSuffix( 1 ) );
    }


    public Object lookupLink( String name )
	throws NamingException
    {
	Context ctx;

	if ( ! name.startsWith( JavaURL ) )
	    throw new NamingException( "Internal error: context not accessed as java JavaURL" );
	ctx = new EnvContext( _env );
	return ctx.lookup( name.substring( JavaURLLength ) );
    }


    public Object lookupLink( Name name )
	throws NamingException
    {
	Context ctx;

	if ( name.isEmpty() || ! name.get( 0 ).equals( JavaURL ) )
	    throw new NamingException( "Internal error: context not accessed as java JavaURL" );
	ctx = new EnvContext( _env );
	return ctx.lookupLink( name.getSuffix( 1 ) );
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
	Context ctx;

	if ( ! name.startsWith( JavaURL ) )
	    throw new NamingException( "Internal error: context not accessed as java JavaURL" );
	ctx = new EnvContext( _env );
	return ctx.list( name.substring( JavaURLLength ) );
    }


    public NamingEnumeration list( Name name )
	throws NamingException
    {
	Context ctx;

	if ( name.isEmpty() || ! name.get( 0 ).equals( JavaURL ) )
	    throw new NamingException( "Internal error: context not accessed as java JavaURL" );
	ctx = new EnvContext( _env );
	return ctx.list( name.getSuffix( 1 ) );
    }
    

    public NamingEnumeration listBindings( String name )
	throws NamingException
    {
	Context ctx;

	if ( ! name.startsWith( JavaURL ) )
	    throw new NamingException( "Internal error: context not accessed as java JavaURL" );
	ctx = new EnvContext( _env );
	return ctx.listBindings( name.substring( JavaURLLength ) );
    }


    public NamingEnumeration listBindings( Name name )
	throws NamingException
    {
	Context ctx;

	if ( name.isEmpty() || ! name.get( 0 ).equals( JavaURL ) )
	    throw new NamingException( "Internal error: context not accessed as java JavaURL" );
	ctx = new EnvContext( _env );
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
	Context ctx;

	if ( ! name.startsWith( JavaURL ) )
	    throw new NamingException( "Internal error: context not accessed as java JavaURL" );
	ctx = new EnvContext( _env );
	return ctx.getNameParser( name.substring( JavaURLLength ) );
    }


    public NameParser getNameParser( Name name )
	throws NamingException
    {
	Context ctx;

	if ( name.isEmpty() || ! name.get( 0 ).equals( JavaURL ) )
	    throw new NamingException( "Internal error: context not accessed as java JavaURL" );
	ctx = new EnvContext( _env );
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
	return prefix + MemoryContext.NameSeparator + name;
    }


    public String getNameInNamespace()
	throws NamingException
    {
	// This is always java:
	return JavaURL;
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
	return JavaURL;
    }


}


