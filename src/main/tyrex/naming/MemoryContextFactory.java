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
 * $Id: MemoryContextFactory.java,v 1.4 2000/09/08 23:05:19 mohammed Exp $
 */


package tyrex.naming;


import java.util.Hashtable;
import java.util.Enumeration;
import java.security.AccessController;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.CompositeName;
import javax.naming.NotContextException;
import javax.naming.NoPermissionException;
import javax.naming.spi.InitialContextFactory;


/**
 * Implements a context factory for {@link MemoryContext}. When set properly
 * {@link javax.naming.InitialContext} will return a {@link
 * MemoryContext} referencing the named path in the shared memory space.
 * <p>
 * To use this context factory the JNDI properties file must include
 * the following properties:
 * <pre>
 * java.naming.factory.initial=tyrex.naming.MemoryContextFactory
 * java.naming.provider.url=
 * </pre>
 * Any URL will return a context to that path in the object tree,
 * relative to the same shared root. The returned context is read/write.
 * The caller must have the {@link NamingPermission} permission on the
 * path in order to acquire the context.
 * 
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $ $Date: 2000/09/08 23:05:19 $
 * @see MemoryContext
 * @see JavaContext
 */
public final class MemoryContextFactory
    implements InitialContextFactory
{


    /**
     * The shared root of the binding tree.
     */
    private static final MemoryBinding  _root = new MemoryBinding();


    /**
     * Returns a binding in the specified path. If the binding does
     * not exist, the full path is created and a new binding is returned.
     * The binding is always obtained from the shared root.
     *
     * @param path The path
     * @return The memory binding for the path
     * @throws NamingException Name is invalid or caller does not have
     *  adequate permission to access shared memory context
     */
    synchronized static MemoryBinding getBindings( String path )
        throws NamingException
    {
	MemoryBinding  binding;
	MemoryBinding  newBinding;
	CompositeName  name;
	int            i;

        try {
	    AccessController.checkPermission( NamingPermission.Shared );
	} catch ( SecurityException except ) {
	    throw new NoPermissionException( "Caller has no permission to reset environment naming context" );
	}

	name = new CompositeName( path );
	binding = _root;
	for ( i = 0 ; i < name.size() ; ++i ) {
	    if ( name.get( i ).length() > 0 ) {
		try {
		    newBinding = (MemoryBinding) binding.get( name.get( i ) );
		    if ( newBinding == null ) {
			newBinding = new MemoryBinding();
			binding.put( name.get( i ), newBinding );
		    }
		    binding = newBinding;
		} catch ( ClassCastException except ) {
		    throw new NotContextException( path + " does not specify a context" );
		}
	    }
	}
	return binding;
    }


    /**
     * Returns an initial context based on the {@link Context.PROVIDER_URL}
     * environment attribute. If this attribute is missing or an empty
     * string, the root will be returned. If not, the specified context
     * will be returned relative to the root.
     */
    public Context getInitialContext( Hashtable env )
        throws NamingException
    {
	if ( env.get( Context.PROVIDER_URL ) == null )
	    return new MemoryContext( env );
	else {
            String url;

	    url = env.get( Context.PROVIDER_URL ).toString();
	    return new MemoryContext( getBindings( url ), env );
	}
    }


}

