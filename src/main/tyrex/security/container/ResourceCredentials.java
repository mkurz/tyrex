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
 * Copyright 1999-2001 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: ResourceCredentials.java,v 1.4 2001/03/12 19:20:18 arkin Exp $
 */


package tyrex.security.container;


import javax.security.auth.Destroyable;


/**
 * Credentials for a resource manager authentication.
 * <p>
 * The password is stored as an array of characters to prevent string interning
 * and allow it to be destroyed. Resource credentials are considered private.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $ $Date: 2001/03/12 19:20:18 $
 */
public final class ResourceCredentials
    implements Destroyable
{


    private String  _name;
    
    
    private char[]  _password;
    
    
    private String  _resName;
    
    
    /**
     * Constructs a new credential with the given name and password.
     *
     * @param resName The resource name
     * @param user The name
     * @param password The password, null if unkonwn
     */
    public ResourceCredentials( String resName, String name, char[] password )
    {
        if ( resName == null )
            throw new IllegalArgumentException( "Argument 'resName' is null" );
        _resName = resName;
        if ( name == null )
            throw new IllegalArgumentException( "Argument 'name' is null" );
        _name = name;
        _password = (char[]) password.clone();
    }
    
    
    /**
     * Returns the name.
     *
     * @return The name
     */
    public String getName()
    {
        if ( _name == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return _name;
    }
    
    
    /**
     * Returns the resource name.
     *
     * @return The resource name
     */
    public String getResourceName()
    {
        if ( _name == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return _resName;
    }
    
    
    /**
     * Returns the password. The password may be null.
     *
     * @return The password
     */
    public char[] getPassword()
    {
        if ( _name == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return (char[]) _password.clone();
    }
    
    
    /**
     * Destroy the credentials.
     */
    public void destroy()
    {
        _name = null;
        _resName = null;
        if ( _password != null ) {
            for ( int i = 0 ; i < _password.length ; ++i )
                _password[ i ] = '\0';
            _password = null;
        }
    }
    
    
    /**
     * Returns true if these credentials have been destroyed.
     */
    public boolean isDestroyed()
    {
        return ( _name == null );
    }
    
    
}
