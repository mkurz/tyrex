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
 * $Id: LDAPCredentials.java,v 1.4 2001/03/12 19:20:19 arkin Exp $
 */


package tyrex.security.ldap;


import javax.security.auth.Destroyable;


/**
 * Credentials for LDAP authentication. Can be used to authenticate an LDAP
 * connection. The LDAP URL can be used to specify the LDAP server (host and
 * port) for which these credentials apply.
 * <p>
 * The password is stored as an array of characters to prevent string interning
 * and allow it to be destroyed. LDAP credentials are considered private.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $ $Date: 2001/03/12 19:20:19 $
 */
public final class LDAPCredentials
    implements Destroyable
{


    private String  _dn;
    
    
    private char[]  _password;
    
    
    private String  _host;
    

    private int  _port;
    /**
     * Constructs a new credential with the given name and password.
     *
     * @param host The LDAP host
     * @param port The port on the host
     * @param dn The account DN
     * @param password The password, null if unkonwn
     */
    public LDAPCredentials( String host, int port, String dn, char[] password )
    {
        if ( host == null )
            throw new IllegalArgumentException( "Argument 'host' is null" );
        _host = host;
        _port = port;
        if ( dn == null )
            throw new IllegalArgumentException( "Argument 'dn' is null" );
        _dn = dn;
        _password = (char[]) password.clone();
    }
    
    
    /**
     * Returns the account DN.
     *
     * @return The account DN
     */
    public String getDN()
    {
        if ( _dn == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return _dn;
    }
    
    
    /**
     * Returns the LDAP Host.
     *
     * @return The LDAP Host
     */
    public String getHost()
    {
        if ( _dn == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return _host;
    }

    /**
     * Returns the LDAP port on the host.
     *
     * @param the LDAP port on the host.
     */
    public int getPort()
    {
        if ( _dn == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return _port;
    }
    
    
    /**
     * Returns the password. The password may be null.
     *
     * @return The password
     */
    public char[] getPassword()
    {
        if ( _dn == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return (char[]) _password.clone();
    }
    
    
    /**
     * Destroy the credentials.
     */
    public void destroy()
    {
        _dn = null;
        _host = null;
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
        return ( _dn == null );
    }
    
    
}
