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
 * $Id: JDBCConnectionInfo.java,v 1.1 2000/04/13 22:13:19 arkin Exp $
 */


package tyrex.connector.jdbc;


/**
 * Information passed to the connection factory to create a connection
 * with a specified user name or password. These are the only
 * properties that {@link DataSource} allows the application to set
 * when opening a new connection. The remaining properties have to be
 * set in the {@link JDBCConnectionFactory}.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/04/13 22:13:19 $
 */
public final class JDBCConnectionInfo
{


    /**
     * The user name of ths connection.
     */
    private String  _userName;


    /**
     * The password of this connection.
     */
    private String  _password;


    /**
     * Construct new properties with the specified user name and password.
     */
    JDBCConnectionInfo( String userName, String password )
    {
        _userName = userName == null ? "" : userName;
        _password = password == null ? "" : password;
    }


    String getUserName()
    {
        return _userName;
    }


    String getPassword()
    {
        return _password;
    }


    public boolean equals( Object other )
    {
        if ( other == this )
            return true;
        return ( other instanceof JDBCConnectionInfo &&
                 ( (JDBCConnectionInfo) other )._userName.equals( _userName ) &&
                 ( (JDBCConnectionInfo) other )._password.equals( _password ) );
    }


    public int hashCode()
    {
        return _userName.hashCode();
    }


}

