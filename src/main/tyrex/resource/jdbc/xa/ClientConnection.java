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
 * Original code is Copyright (c) 1999-2001, Intalio, Inc. All Rights Reserved.
 *
 * Contributions by MetaBoss team are Copyright (c) 2003-2004, Softaris Pty. Ltd. All Rights Reserved.
 *
 * $Id: ClientConnection.java,v 1.4 2004/12/13 22:49:40 metaboss Exp $
 */


package tyrex.resource.jdbc.xa;


import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;


/**
 * Encapsulates an application's view of an XA/pooled connection.
 * The XA connection is managed by the application server through it's
 * {@link javax.sql.XAConnection} interface. The underlying JDBC
 * connection is a standard JDBC connection. The application's
 * JDBC connection gives access to the underlying JDBC connection but
 * is managed by the application server. The application is given an
 * instance of this class and not the underlying connection directly.
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version 1.0
 * @see XAConnectionImpl
 * @see XADataSourceImpl
 * @see Connection
 */
final class ClientConnection
    extends TyrexConnection
    implements Connection
{


    /**
     * The pooled XA connection that created this client connection
     * and should be used to report closure and fatal errors.
     */
    private XAConnectionImpl  _xaConn;


    /**
     * The XA data source that created the {@link _xaConn}.
     * <p>
     * This used in the statements created by this connection
     * to synchronize so that queries and commits/rollbacks 
     * cannot occur simultaneously for the same XA data source.
     */
    private XADataSourceImpl _xaDataSource;


    /**
     * This identifier was handed on to use when we were created by
     * {@link XAConnection}. If since then the XA connection was asked
     * to create another connection or was closed, our identifier will
     * no longer be valid and any call to {@link
     * XAConnection#getUnderlying} will throw an exception. Previously,
     * the XA connection would hold a reference to use and tell us to
     * terminate, but that prevented ClientConnection from being
     * finalized.
     */
    private int               _clientId;




    /**
     * Construct a new client connection to provide access to the
     * underlying JDBC connection (<tt>underlying</tt>) on behalf of
     * an XA/pooled connection (<tt>xaConn<tt/>). The pooled connection
     * is required to notify of connection closure and fatal errors.
     *
     * @param xaConn The XA/pooled connection that created this
     *   client connection
     * @param xaDataSource the data source that created the xaConn
     * @param clientId A unique identifier handed to us by
     *   {@link XAConnection}
     * @param underlying The underlying JDBC connection
     */
    ClientConnection( XAConnectionImpl xaConn, XADataSourceImpl xaDataSource, int clientId )
    {
        _xaConn = xaConn;
        _xaDataSource = xaDataSource;
        _clientId = clientId;
    }


    public void setAutoCommit( boolean autoCommit )
        throws SQLException
    {
        // Cannot set auto-commit inside a transaction.
        if ( _xaConn.insideGlobalTx() )
            throw new SQLException( "Cannot commit/rollback a connection managed by the transaction manager" );
        try {
            super.setAutoCommit( autoCommit );
        } catch ( SQLException except ) {
            notifyError( except );
            throw except;
        }
    }


    public boolean getAutoCommit()
        throws SQLException
    {
        try {
            return super.getAutoCommit();
        } catch ( SQLException except ) {
            notifyError( except );
            throw except;
        }
    }


    public void commit()
        throws SQLException
    {
        // Cannot commit directly if we're inside a global transaction.
        if ( _xaConn.insideGlobalTx() )
            throw new SQLException( "Cannot commit/rollback a connection managed by the transaction manager" );
        // Cannot commit a read-only transaction.
        if ( isReadOnly() )
            throw new SQLException( "Cannot commit/rollback a read-only transaction" );

        // This only occurs if not inside a local transaction.
        try {
            super.commit();
        } catch ( SQLException except ) {
            notifyError( except );
            throw except;
        }
    }


    public void rollback()
        throws SQLException
    {
        // Cannot commit directly if we're inside a global transaction.
        if ( _xaConn.insideGlobalTx() )
            throw new SQLException( "Cannot commit/rollback a connection managed by the transaction manager" );
        // This only occurs if not inside a local transaction.
        try {
            super.rollback();
        } catch ( SQLException except ) {
            notifyError( except );
            throw except;
        }
    }


    protected void internalClose()
        throws SQLException
    {
        if ( !isClosed() ) { 
            // Notify the XA connection that we are no longer going
            // to be used. Whether the underlying connection is released,
            // held until the transaction terminates, etc is not
            // a concern of us.
            _xaConn.notifyClose( _clientId );
            _xaConn = null;
            _xaDataSource = null;
        }
    }


    public synchronized boolean isClosed()
    {
        // Simple way of determining if this connection is closed.
        // The actual connection is never closed, it is pooled.
        return ( _xaConn == null );
    }


    /**
     * Called by {@link XAConnectionImpl} to terminate this connection
     * by dissociating it from the underlying JDBC connection.
     * The application would call {@link #close} but {@link
     * XAConnectionImpl} cannot, since pooled connection requirements
     * will cause an inifinite loop. This method should not attempt
     * to notify either a closure or fatal error, but rather throw an
     * exception if it fails.
     */
    /* Deprecated: see XAConnection._clientId
    void terminate()
    {
    _xaConn = null;
    }
    */


    public String toString()
    {
        if (isClosed()) {
            return "XAConnection: Connection closed";    
        }
        try {
            return internalGetUnderlyingConnection().toString();
        } catch ( SQLException except ) {
            return "XAConnection: Connection closed";
        }
    }


    /**
     * Called when an exception is thrown by the underlying connection
     * to determine whether the exception is critical or not. If the
     * exception is critical, notifies the XA connection to forget
     * about this connection.
     *
     * @param except The exception thrown by the underlying
     *   connection
     */
    protected void notifyError( SQLException except )
    {
        if ( _xaConn != null )
            _xaConn.notifyError( _clientId, except );
    }


    /**
     * Called to retrieve the underlying JDBC connection. Actual JDBC
     * operations are performed against it. Throws an SQLException if
     * this connection has been closed.
     */
    protected Connection internalGetUnderlyingConnection()
        throws SQLException
    {
        // Must pass the client identifier so XAConnection can determine
        // whether we are still valid. If it tells us we're no longer
        // valid, we have little to do.
        try {
            return _xaConn.getUnderlying( _clientId );
        } catch ( SQLException except ) {
            _xaConn = null;
            _xaDataSource = null;
            throw except;
        }
    }


}



