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
 * $Id: JDBCManagedConnection.java,v 1.3 2000/08/28 19:01:48 mohammed Exp $
 */


package tyrex.connector.jdbc;


import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.sql.Connection;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import tyrex.connector.AbstractManagedConnection;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ConnectionException;
import tyrex.connector.ConnectionEventListener;
import tyrex.connector.SynchronizationResource;


/**
 * An adapter for JDBC XAConnection interface to expose
 * it as a managed connection. It produces an object of 
 * type {link JDBCConnectionHandle}
 * as a result of calling {@link #getConnection}.
 * <BR>
 * The last handle from a JDBC managed connection 
 * {@link #getConnection}
 * is valid ie previous handles become invalid when
 * a new handle is obtained. This is in keeping with the
 * semantics of JDBC pooled connection.
 * <BR>
 * This implementation currently uses a single java.sql.Connection
 * object as the basis of the {link JDBCConnectionHandle} objects
 * produced by this managed connection through the method
 * {link getConnection}.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/08/28 19:01:48 $
 */
public final class JDBCManagedConnection
    extends AbstractManagedConnection
    implements ManagedConnection, javax.sql.ConnectionEventListener
{


    private final XAConnection          xaConnection;

    private final Connection            connection;

    private WeakReference               currentHandleReference;
    
    private final JDBCConnectionInfo      info;
    

    public JDBCManagedConnection( XAConnection xaConnection, JDBCConnectionInfo info )
        throws ConnectionException
    {
        this.xaConnection = xaConnection;
        this.xaConnection.addConnectionEventListener( this );
        this.info = info;
        try {
            connection = xaConnection.getConnection();
            // turn off auto commit
            connection.setAutoCommit(false);
        } catch ( SQLException except ) {
            throw new ConnectionException( except );
        }
    }


    boolean isSameInfo( JDBCConnectionInfo info )
    {
        return ( ( this.info == null && info == null ) ||
                 ( this.info != null && this.info.equals( info ) ) );
    }


    //-------------------//
    // ManagedConnection //
    //-------------------//

    /*
    public void pool()
        throws ConnectionException
    {
        if ( connection == null )
            throw new ConnectionException( "Connection closed" );
        
    }
    */

    /**
     * Create the XA resource to be used to manage distributed transactions 
     * on this connection. This method is only called once.
     *
     * @return the XA resource to be used to manage distributed transactions
     * on this connection.
     * @throws ConnectionException if there is a problem creating the xa resource.
     */
    protected XAResource createXAResource()
        throws ConnectionException
    {
        try {
            return xaConnection.getXAResource();
        }
        catch(SQLException e) {
            throw new ConnectionException("Failed to get XA resource for <" +
                                          toString() +
                                          ">.");
        }
    }



    /**
     * Actual method that closes the connection. The connection 
     * manager calls this method
     * when the connection is removed from the pool and will not be
     * used anymore. This method is called at most once and should 
     * not be called directly.
     *
     * @throws ConnectionException Reports an error that occured when
     *  attempting to close the connection
     */
    protected void performClose()
        throws ConnectionException
    {
        // synchronization is needed to prevent the connection and current handle
        // reference from changing while closing the managed connection,
        // returning a connection or being notified that a connection
        // is being closed
        // synchronized by the close method in AbstractManagedConnection

        xaConnection.removeConnectionEventListener( this );
        try {
            xaConnection.close();
        } catch ( SQLException except ) {
            throw new ConnectionException( except );
        } finally {
            // disconnect the current handle
            disconnectCurrentConnection();
        }
    }


    // synchronized to prevent the connection and current handle
    // reference from changing while closing the managed connection,
    // returning a connection or being notified that a connection
    // is being closed
    public synchronized Object getConnection( Object info )
        throws ConnectionException
    {
        checkClosed();
        // disconnect the current handle
        disconnectCurrentConnection();
        // Ignore info, only properties affecting connection creation
        // from XAConnection were passed.
        Object handle = new JDBCConnectionHandle( this, connection );
        // set the new reference
        currentHandleReference = new WeakReference(handle);
        // return the handle
        return handle;
    }

    /**
     * Disconnect the last connection returned by this managed connection
     */
    private void disconnectCurrentConnection()
    {
        JDBCConnectionHandle currentHandle = (null == currentHandleReference) 
                                                ? null 
                                                : (JDBCConnectionHandle)currentHandleReference.get();

        if (null != currentHandle) {
            currentHandle.disconnect();
            // reset the var
            currentHandleReference = null;
        }
    }

    /*
    public XAResource getXAResource()
        throws ConnectionException
    {
        checkClosed();
        return _xaResource;
    }
    */

    /*
    public SynchronizationResource getSynchronizationResource()
        throws ConnectionException
    {
        checkClosed();
        return _syncResource;
    }
    */
    /*
    public void connect( Object connection )
        throws ConnectionException
    {
        checkClosed();
        if ( connection instanceof JDBCConnectionHandle ) {
            try {
                ( (JDBCConnectionHandle) connection ).connect( this, connection );
            } catch ( Exception except ) {
                throw new ConnectionException( except.getMessage() );
            }
        } else
            throw new ConnectionException( "Internal error: Not a ProxyConnection" );
    }
    */

    public PrintWriter getLogWriter()
    {
        // Not supported on XAConnection
        return null;
    }


    public void setLogWriter( PrintWriter logWriter )
    {
        // Not supported on XAConnection
    }


    //-----------------------------------//
    // javax.sql.ConnectionEventListener //
    //-----------------------------------//

    /**
     * The underlying java.sql.Connection has been closed externally 
     * ie not through JDBCManagedConnection so it cannot be reused.
     * This means that the JDBCManagedConnection cannot be reused.
     */
    public void connectionClosed( javax.sql.ConnectionEvent event )
    {
        // the underlying connection has been closed externally
        fireConnectionErrorOccurredEvent(new ConnectionException("The underlying java.sql.Connection used by the JDBCManagedConnection has been closed externally."));
    }


    /**
     * An error has occurred in the XA Connection.
     */
    public void connectionErrorOccurred( javax.sql.ConnectionEvent event )
    {
        fireConnectionErrorOccurredEvent(event.getSQLException());
    }


    /**
     * Called by the JDBCConnectionHandle to notify the JDBCManagedConnection
     * that the handle has been closed.
     */
    synchronized void notifyClosed()
    {   
        // synchronized to prevent the connection and current handle
        // reference from changing while closing the managed connection,
        // returning a connection or being notified that a connection
        // is being closed
    
        // reset the current handle
        currentHandleReference = null;

        fireConnectionClosedEvent();
    }


}

