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
 * $Id: JDBCManagedConnectionFactory.java,v 1.1 2000/04/13 22:13:19 arkin Exp $
 */


package tyrex.connector.jdbc;


import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Enumeration;
import java.sql.SQLException;
import javax.sql.XADataSource;
import tyrex.connector.ManagedConnectionFactory;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ConnectionManager;
import tyrex.connector.ConnectionException;


/**
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/04/13 22:13:19 $
 */
public abstract class JDBCManagedConnectionFactory
    implements ManagedConnectionFactory, Serializable
{


    private transient XADataSource  _xaDataSource;


    private int                     _loginTimeout;


    private int                     _maxConn;


    private String                  _description;


    private PrintWriter             _logWriter;


    public String getDescription()
    {
        return _description;
    }


    public void setDescription( String description )
    {
        description = description;
    }


    public PrintWriter getLogWriter()
    {
        return _logWriter;
    }


    public void setLogWriter( PrintWriter logWriter )
    {
        _logWriter = logWriter;
    }


    public int getLoginTimeout()
    {
        return _loginTimeout;
    }


    public void setLoginTimeout( int timeout )
    {
        _loginTimeout = timeout;
    }


    public int getMaxConnection()
    {
        return _maxConn;
    }


    public void setMaxConnection( int maxConn )
    {
        _maxConn = maxConn;
    }


    public Object createConnectionFactory( ConnectionManager manager )
        throws ConnectionException
    {
        return new JDBCHandleFactory( manager );
    }


    protected abstract XADataSource createDataSource()
        throws ConnectionException;


    public ManagedConnection createManagedConnection( Object info )
        throws ConnectionException
    {
        try {
            if ( _xaDataSource == null ) {
                _xaDataSource = createDataSource();
                _xaDataSource.setLogWriter( _logWriter );
                _xaDataSource.setLoginTimeout( _loginTimeout );
            }
            if ( info == null || ! ( info instanceof JDBCConnectionInfo ) )
                return new JDBCManagedConnection( _xaDataSource.getXAConnection(), null );
            else {
                JDBCConnectionInfo jdbcInfo;

                jdbcInfo = (JDBCConnectionInfo) info;
                return new JDBCManagedConnection( _xaDataSource.getXAConnection( jdbcInfo.getUserName(), jdbcInfo.getPassword() ),
                                                  jdbcInfo );
            }
        } catch ( SQLException except ) {
            throw new ConnectionException( except );
        }
    }


    public ManagedConnection getManagedConnection( Enumeration enum, Object info )
        throws ConnectionException
    {
        ManagedConnection managed;

        if ( info == null || info instanceof JDBCConnectionInfo ) {
            while ( enum.hasMoreElements() ) {
                managed = (ManagedConnection) enum.nextElement();
                if ( managed instanceof JDBCManagedConnection &&
                     ( (JDBCManagedConnection) managed ).isSameInfo( (JDBCConnectionInfo) info ) )
                    return managed;
            }
        }
        return null;
    }


}




