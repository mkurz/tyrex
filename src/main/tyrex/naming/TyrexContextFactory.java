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
 * $Id: TyrexContextFactory.java,v 1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.naming;


import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.CompositeName;
import javax.naming.NotContextException;
import javax.naming.NoPermissionException;
import javax.naming.spi.InitialContextFactory;


/**
 * Implements a context factory for Tyrex contexts. When set properly
 * {@link javax.naming.InitialContext} will return a {@link
 * TyrexContext} referencing the named path in the memory model.
 * <p>
 * To use this context factory the JNDI properties file must include
 * the following properties:
 * <pre>
 * java.naming.factory.initial=tyrex.naming.TyrexContextFactory
 * java.naming.provider.url=
 * </pre>
 * An empty URL will return a context identical to the contents
 * of <tt>java:</tt>, thus:
 * <pre>
 * ctx = new InitialContext();
 * ctx.lookup( "comp/env/xyz" );
 * ctx.lookup( "java:/comp/env/xyz" );
 * </pre>
 * are equivalent. The returned context is read-only.
 * <p>
 * Any other URL will return a context to that path in the object tree,
 * with <tt>root:</tt> returning a path to the root. The returned context
 * is  read/write. The caller must have the {@link TyrexContextPermission}
 * permission on the path in order to acquire the context:
 * 
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see TyrexContext
 * @see JavaContext
 */
public final class TyrexContextFactory
    extends Constants
    implements InitialContextFactory
{


    /**
     * The root of the object tree. All context not starting with
     * <tt>java:</tt> will retrieve a memory binding from this root
     * based on the specified path, and <tt>root:</tt> will return
     * the actual root.
     */
    public static MemoryBinding  _root = new MemoryBinding();


    public synchronized static MemoryBinding getBindings( String path )
        throws NamingException
    {
	MemoryBinding  binding;
	MemoryBinding  newBinding;
	CompositeName  name;
	int            i;

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


    public Context getInitialContext( Hashtable env )
        throws NamingException
    {
	String url;

	if ( env.get( Context.PROVIDER_URL ) == null )
	    url = null;
	else {
	    url = env.get( Context.PROVIDER_URL ).toString();
	    if ( url.length() == 0 || url.equals( URL.Java ) ) {
		url = null;
	    }
	}
	if ( url == null ) {
	    return new JavaContext( env );
	} else {
	    if ( url.equals( URL.Root ) )
		url = "";
	    return new TyrexContext( getBindings( url ), env );
	}
    }


}
