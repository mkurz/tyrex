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
 * $Id: JDBCConnectionHandle.java,v 1.3 2000/09/08 23:04:44 mohammed Exp $
 */


package tyrex.connector.jdbc;


import java.io.PrintWriter;
import java.util.Map;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.DatabaseMetaData;
import java.sql.SQLWarning;
import java.sql.SQLException;

/**
 * A handle into the underlying connection. The application recieves this
 * handle to the underlying connection which is associated with any number
 * of underlying connections (or managed connections) during the life time
 * of the application. The application can release the handle by calling
 * the close() method. The underlying connection is managed by the
 * connection manager through the {@link JDBCManagedConnection}.
 * <BR>
 * The last handle from a JDBC managed connection 
 * {@link JDBCManagedConnection#getConnection}
 * is valid ie previous handles become invalid when
 * a new handle is obtained. This is in keeping with the
 * semantics of JDBC pooled connection.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/08 23:04:44 $
 */
public final class JDBCConnectionHandle
    implements Connection
{


    /**
     * The underlying connection.
     */
    private Connection  _connection;


    /**
     * True if this handle has been closed by the application.
     */
    private boolean     _closed = false;


    /**
     * The managed connection. Used to notify of closure and critical errors.
     */
    private JDBCManagedConnection _managed;


    /**
     * Constructs a new handle with the specified underlying connection.
     */
    JDBCConnectionHandle( JDBCManagedConnection managed, Connection connection )
    {
        _managed = managed;
        _connection = connection;
    }

    /**
     * Return the public interface that this class represents.
     *
     * @return the public interface that connector handle
     *      represents
     */
    public String getInterface()
    {
        return "java.sql.Connection";
    }


    //----------//
    // JDBC 2.0 //
    //---------//


    public void clearWarnings()
        throws SQLException
    {
        getConnection().clearWarnings();
    }


    protected void finalize()
        throws Throwable
    {
        if (null != _managed) {
            close();    
        }
    }


    public void close()
        throws SQLException
    {
        if ( _managed == null )
            throw new SQLException( "Connection has been closed" );
        _managed.notifyClosed();
        _managed = null;
        _connection = null;
    }


    public void commit()
        throws SQLException
    {
        throw new SQLException( "The commit method is not supported in a managed connection. Use UserTransaction to manage transactions" );
    }


    public Statement createStatement()
        throws SQLException
    {
        return getConnection().createStatement();
    }


    public Statement createStatement( int resultSetType, int resultSetConcurrency )
        throws SQLException
    {
        return getConnection().createStatement( resultSetType, resultSetConcurrency );
    }


    public boolean getAutoCommit()
        throws SQLException
    {
        return false;
    }


    public String getCatalog()
        throws SQLException
    {
        return getConnection().getCatalog();
    }


    public DatabaseMetaData getMetaData()
        throws SQLException
    {
        return getConnection().getMetaData();
    }


    public int getTransactionIsolation()
        throws SQLException
    {
        return getConnection().getTransactionIsolation();
    }


    public Map getTypeMap()
        throws SQLException
    {
        return getConnection().getTypeMap();
    }


    public SQLWarning getWarnings()
        throws SQLException
    {
        return getConnection().getWarnings();
    }


    public boolean isClosed()
        throws SQLException
    {
        return ( _managed == null );
    }


    public boolean isReadOnly()
        throws SQLException
    {
        return getConnection().isReadOnly();
    }


    public String nativeSQL( String sql )
        throws SQLException
    {
        return getConnection().nativeSQL( sql );
    }


    public CallableStatement prepareCall( String sql )
        throws SQLException
    {
        return getConnection().prepareCall( sql );
    }


    public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency )
        throws SQLException
    {
        return getConnection().prepareCall( sql, resultSetType, resultSetConcurrency );
    }


    public PreparedStatement prepareStatement( String sql )
        throws SQLException
    {
        return getConnection().prepareStatement( sql );
    }


    public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency )
        throws SQLException
    {
        return getConnection().prepareStatement( sql, resultSetType, resultSetConcurrency );
    }


    public void rollback()
        throws SQLException
    {
        throw new SQLException( "The rollback method is not supported in a managed connection. Use UserTransaction to manage transactions" );
    } 


    public void setAutoCommit( boolean autoCommit )
        throws SQLException
    {
        if ( autoCommit )
            throw new SQLException( "The setAutoCommit method is not supported in a managed connection. Use UserTransaction to manage transactions" );
    } 


    public void setCatalog( String catalog )
        throws SQLException
    {
        getConnection().setCatalog( catalog );
    }


    public void setReadOnly( boolean readOnly )
        throws SQLException
    {
        getConnection().setReadOnly( readOnly );
    }


    public void setTransactionIsolation( int level )
        throws SQLException
    {
        getConnection().setTransactionIsolation( level );
    }


    public void setTypeMap( Map typeMap )
        throws SQLException
    {
        getConnection().setTypeMap( typeMap );
    }


    //-------------------//
    // ManagedConnection //
    //------------------//


    /**
     * Returns the underlying connection. Report if this handle
     * has been closed by the application.
     */
    private Connection getConnection()
        throws SQLException
    {
        if ( _connection != null )
            return _connection;
        if ( _managed == null )
            throw new SQLException( "Connection has been closed" );
        else
            throw new SQLException( "Internal error: suspended connection being used by the application" );
    }


    /**
     * Associates this handle with a given underlying connection.
     *
     * @param managed The managed connection that is associating this handle
     * with a given underlying connection.
     * @param connection The underlying connection
     */
    /*void connect(JDBCManagedConnection managed, Connection connection)
    {
        if ( _managed == null )
            throw new IllegalStateException( "Connection has been closed" );
        if (_managed != managed) {
            throw new IllegalStateException("Internal Error: Connection associated with different managed connection.");
        }
        if ( _connection != null )
            throw new IllegalStateException( "Internal error: proxy connection already associated with underlying connection" );
        _connection = connection;
    }
    */

    /**
     * Dissociates this handle from any underlying connection and 
     * from its managed connection. This method should only be called
     * the managed connection of the handle. In all other cases 
     * {@link #close} should be called. The difference between this method
     * and close is that this method does not notify the managed connection
     * that the handle has been closed.
     */
    void disconnect()
    {
        if ( _managed == null )
            throw new IllegalStateException( "Connection has been closed" );
        if ( _connection == null )
            throw new IllegalStateException( "Internal error: proxy connection not associated with any underlying connection" );
        _connection = null;
        _managed = null;
    }
}

