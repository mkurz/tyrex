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
 * $Id: JDBCAdapter.java,v 1.1 2000/04/10 20:52:34 arkin Exp $
 */


package tyrex.connector.jdbc;


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import tyrex.connector.ConnectionAdapter;
import tyrex.connector.ConnectionManager;
import tyrex.connector.ConnectionException;


/**
 * An adapter for JDBC DataSource. This adapter is listed in the
 * JNDI ENC and obtains JDBC connections for the application.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/04/10 20:52:34 $
 */
public class JDBCAdapter
    implements DataSource, ConnectionAdapter
{


    private ConnectionManager  _manager;


    public void setConnectionManager( ConnectionManager manager )
    {
        if ( _manager != null )
            throw new IllegalStateException( "Internal error: ConnectionManager already set for this adapter" );
        _manager = manager;
    }


    public void unsetConnectionManager()
    {
        if ( _manager == null )
            throw new IllegalStateException( "Internal error: No ConnectionManager set for this adapter" );
        _manager = null;
    }


    public Connection getConnection()
        throws SQLException
    {
        if ( _manager == null )
            throw new SQLException( "Internal error: No connection manager available" );
        try {
            return (Connection) _manager.getConnection();
        } catch ( ConnectionException except ) {
            if ( except.getException() != null && except.getException() instanceof SQLException )
                throw (SQLException) except.getException();
            throw new SQLException( except.getMessage() );
        }
    }


    public Connection getConnection( String user, String password )
        throws SQLException
    {
        return null;
    }


    public PrintWriter getLogWriter()
    {
        // Return nothing. This property is set in XADataSource.
        return null;
    }


    public void setLogWriter( PrintWriter logWriter )
    {
        // Do nothing. This property is set in XADataSource.
    }


    public int getLoginTimeout()
    {
        // Return 0. This property is set in XADataSource.
        return 0;
    }


    public void setLoginTimeout( int timeout )
    {
        // Do nothing. This property is set in XADataSource.
    }


}

