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
 * $Id: NamePasswordCallback.java,v 1.4 2001/03/12 19:20:18 arkin Exp $
 */


package tyrex.security;


import javax.security.auth.callback.Callback;


/**
 * Callback for name/password authentication. Can be used by a login module
 * to request the user name, password and realm in order to authenticate.
 * <p>
 * This information will be supplied by a non-GUI capable container.
 * <p>
 * The realm is supplied by the login module, and may be ignored, used to
 * obtain a suitable name/password, or determine that no name/password is
 * available for that login module.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $ $Date: 2001/03/12 19:20:18 $
 */
public class NamePasswordCallback
    implements Callback
{


    private String  _name;
    
    
    private char[]  _password;
    
    
    private String  _realm;
    
    
    /**
     * Sets the retrieved name.
     *
     * @param name The retrieved name (may be null)
     */
    public void setName( String name )
    {
        _name = name;
    }
    
    
    /**
     * Returns the retrieved name.
     *
     * @return The retrieved name (may be null)
     */
    public String getName()
    {
        return _name;
    }
    
    
    /**
     * Sets the retrieved password.
     *
     * @param name The retrieved password (may be null)
     */
    public void setPassword( char[] password )
    {
        _password = password;
    }
    
    
    /**
     * Returns the retrieved password.
     *
     * @return The retrieved password (may be null)
     */
    public char[] getPassword()
    {
        return _password;
    }
    
    
    /**
     * Sets the realm.
     *
     * @param name The realm (may be null)
     */
    public void setRealm( String realm )
    {
        _realm = realm;
    }
    
    
    /**
     * Returns the realm.
     *
     * @return The realm (may be null)
     */
    public String getRealm()
    {
        return _realm;
    }
    
    
}
