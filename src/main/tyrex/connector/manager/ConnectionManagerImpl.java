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
 * $Id: ConnectionManagerImpl.java,v 1.2 2000/04/13 22:04:25 arkin Exp $
 */


package tyrex.connector.manager;


import java.io.PrintWriter;
import java.util.Vector;
import java.util.Hashtable;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ManagedConnectionFactory;
import tyrex.connector.ConnectionManager;
import tyrex.connector.ConnectionException;
import tyrex.connector.SynchronizationResource;
import tyrex.util.Messages;


/**
 *
 *
 * <b>Notes:</b>
 * <p>
 * <i>Retry policy</i> Pooled connection may expire after a certain duration,
 *  due to network connections going down, etc. When a connection is obtained
 *  from the pool, if the connection cannot be established properly, the
 *  connection manager will discard it and retry to obtain a different
 *  connection until it succeeds or fails with obtaining a non-pooled connection.
 *  The later case indicates permanent failure.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/04/13 22:04:25 $
 */
public class ConnectionManagerImpl
    implements ConnectionManager, ConnectionEventListener,
               PoolManaged
{


    /**
     * Holds a list of all the pooled resources that are available
     * for reuse by the application. Each resource is held inside
     * an {@link ResourcePoolEntry} that records the resource's
     * account. This cannot be implemented as a stack if we want
     * to pool resources for different accounts.
     */
    private final Vector     _pool = new Vector();


    /**
     * Holds a list of all the active resources that are being
     * used by the application. Each resource is held inside
     * an {@link ResourcePoolEntry} that records the resource's
     * account, using {@link PooledResource} as the key.
     */
    private final Hashtable _active = new Hashtable();


    /**
     * The underlying managed connection factory to use.
     */
    private final ManagedConnectionFactory  _factory;


    /**
     * The pool manager determines the policy for activating and
     * creating resources.
     */
    private final PoolManager               _poolManager;


    private final PrintWriter               _logWriter;


    private final ConnectorInfo             _connectorInfo;


    private final TransactionManager        _manager;


    public ConnectionManagerImpl( ManagedConnectionFactory factory,
                                  PoolManager poolManager, PrintWriter logWriter )
    {
        if ( factory == null || poolManager == null )
            throw new IllegalArgumentException( "Argument 'factory' or 'poolManager' is null" );
        _factory = factory;
        _poolManager = poolManager;
        _poolManager.setPoolManaged( this );
        _poolManager.setMaximum( factory.getMaxPool() );
        _poolManager.setMinimum( factory.getMinPool() );
        _logWriter = logWriter;
    }


    public synchronized Object getConnection( Object info )
        throws ConnectionException
    {
        ManagedConnection managed;
        Object            connection;
        ManagedEntry      entry;
        boolean           retry;
        Transaction       tx = null;

        managed = null;

        if ( _txManager != null )
            tx = _txManager.getTransaction();

        if ( _connectorInfo.getShareConnections() ) {
            // Connection sharing is enabled for this connector. If two components
            // request a connection from the same factory, it is acceptable to
            // return the same managed connection to both.
            ConnectionContext ctx;

            ctx = ConnectionContext.getCurrent();
            if ( ctx != null ) {
                managed = _factory.getManagedConnection( ctx.listConnections( this, tx ), info );
                if ( managed != null ) {
                    // A similar managed connection has been opened before,
                    // perfectly OK if we return it (it's listed in the transaction
                    // context, registered for synchronization, etc).
                    entry = (ManagedEntry) _active.get( managed );
                    if ( entry != null ) {
                        connection = managed.getConnection();
                        return connection;
                    } else {
                        // This is clearly an error. Might be due to improper error handling.
                        if ( _logWriter != null )
                            _logWriter.println( "Internal error: a managed connection appears in ConnectionContext and not in active list" );
                    }
                }
            }
        }


        // 
        entry.managed.addConnectionEventListener( this );
        entry.transaction = _txManager.getTransaction();
        if ( entry.transaction != null ) {
            entry.transaction.enlistResource( entry.xaResource );
            // Register for synchronization so we know when the transaction
            // completes and we can return it to the pool.
            entry.transaction.registerSynchronization( new ConnectionSynchronization( this, entry ) );
        }
        connection = entry.managed.getConnection( info );
        return connection;

        /*

        // Ask the pool manager whether we can activate the
        // given connection. If not, this will throw an exception.
        if ( ! _poolManager.canActivate() )
            throw new ConnectionException( Messages.format( "tyrex.resource.limitExceeded", toString() ) );
        if ( _pool.size() > 0 ) {
            retry = true;
            entry = (ManagedEntry) _pool.elementAt( _pool.size() - 1 );
            _pool.removeElementAt( _pool.size() - 1 );
        } else {
            retry = false;
            if ( ! _poolManager.canCreate() )
                throw new ConnectionException( Messages.format( "tyrex.resource.limitExceeded", toString() ) );
            entry = createManagedConnection();
        }

        try {
            Object connection;

            // Grab a connection and return it. If fails, this operation
            // might retry another pooled managed connection.
            connection = entry.managed.getConnection();
            // Record the connection as active and return the adapter
            _active.put( entry.managed, entry );
            return connection;

        } catch ( ConnectionException except ) {
            // If a fault occured talking to the connection, report it.
            // If this connection was just created, send an error back to
            // the application, if not, remove it from the pool and try
            // a second time.
            if ( _logWriter != null )
                _logWriter.println( Messages.format( "tyrex.resource.fault", toString(), entry.managed, except ) );
            if ( retry )
                return getConnection();
            else
                throw except;
        }
        */
    }



    /*
    private ManagedEntry createManagedConnection()
        throws ConnectionException
    {
        ManagedEntry      entry;
        ManagedConnection managed;

        managed = _factory.getManagedConnection();
        managed.setConnectionManager( this );
        switch ( managed.getTransactionType() ) {
        case ManagedConnection.TRANSACTION_NONE:
            entry = new ManagedEntry( managed );
            break;
        case ManagedConnection.TRANSACTION_XA:
            entry = new ManagedEntry( managed, managed.getXAResource() );
            break;
        case ManagedConnection.TRANSACTION_SYNCHRONIZATION:
            entry = new ManagedEntry( managed, managed.getSynchronizationResource() );
            break;
        default:
            throw new ConnectionException( "Internal error: Invalid transaction type" );
        }
        return entry;
    }
    */


    public synchronized void connectionClosed( ManagedConnection managed )
    {
        ManagedEntry entry;

        // Remove it from the list of active connections and possibly add
        // it to the list of pooled connections.
        entry = (ManagedEntry) _active.get( managed );
        if ( entry != null ) {
            -- entry.refCount;
            if ( entry.refCount == null ) {
                if ( entry.transaction == null )
                    releaseToPool( entry );
                else
                    // Enlisted with a transaction. Delist it from the transaction
                    // with a success flag, this will cause the transaction to
                    // commit/rollback but call XAResource.end().
                    entry.transaction.delistResource( entry.xaResource, XAResource.TMSUCCESS );
            }
        }
    }


    void releaseToPool( ManagedConnection managed )
    { 
        ManagedEntry entry;

        // Remove it from the list of active connections and possibly add
        // it to the list of pooled connections.
        entry = (ManagedEntry) _active.remove( managed );
        if ( entry != null ) {
            // Return the managed connection to the pool immediately.
            _pool.addElement( entry );
            // Notify all waiting threads that a new resource
            // is available to the pool and they might use it.
            _poolManager.released();
            if ( _logWriter != null )
                _logWriter.println( Messages.format( "tyrex.resource.returned", toString(), managed ) );
        }
    }



    public synchronized void connectionErrorOccurred( ManagedConnection managed, Exception except )
    {
        ManagedEntry entry;

        if ( _logWriter != null )
            _logWriter.println( Messages.format( "tyrex.resource.errorOccured", toString(), managed, except ) );

        // Remove it from the list of active connections but
        // do not add it to the pool.
        entry = (ManagedEntry) _active.remove( managed );
        if ( entry != null ) {
            if ( entry.transaction != null ) {
                try {
                    // Enlisted with a transaction. Delist it from the transaction
                    // with a fail flag, this will cause the transaction to discard
                    // this resource from the commit/rollback list.
                    entry.transaction.delistResource( entry.xaResource, XAResource.TMFAIL );
                } catch ( Exception except2 ) {
                    if ( _logWriter != null )
                        _logWriter.println( Messages.format( "tyrex.resource.fault", toString(), managed, except2 ) );
                }
            }

            // Notify all waiting threads that a new resource can be created.
            _poolManager.released();
	}
    }


    public int getPooledResourceCount()
    {
	return _pool.size();
    }


    public int getResourceCount()
    {
	return _active.size() + _pool.size();
    }


    public synchronized void releasePooled( int count )
    {
        for ( int i = 0 ; i < count && i < _pool.size() ; ++i ) {
            ManagedEntry entry;

            entry = (ManagedEntry) _pool.elementAt( 0 );
            _pool.removeElementAt( 0 );
            try {
                entry.managed.close();
            } catch ( ConnectionException except ) {
                if ( _logWriter != null )
                    _logWriter.println( Messages.format( "tyrex.resource.fault", toString(), entry.managed, except ) );
            }
        }
    }


    public void release()
    {
        _poolManager.unsetPoolManaged();
        releasePooled( _pool.size() );
    }


    public String toString()
    {
        return _factory.toString();
    }


    static class ManagedEntry
    {

        final ManagedConnection       managed;

        final XAResource              xaResource;

        final SynchronizationResource synchronization;

        ManagedEntry( ManagedConnection managed )
        {
            this.managed = managed;
            this.xaResource = null;
            this.synchronization = null;
        }

        ManagedEntry( ManagedConnection managed, XAResource xaResource )
        {
            this.managed = managed;
            this.xaResource = xaResource;
            this.synchronization = null;
        }

        ManagedEntry( ManagedConnection managed, SynchronizationResource synchronization )
        {
            this.managed = managed;
            this.xaResource = null;
            this.synchronization = synchronization;
        }

    }


    static class ConnectionSynchronization
        implements Synchronization
    {

        private ConnectionManager _manager;

        private ManagedConnection _managed;


        ConnectionSynchronization( ConnectionManager manager, ManagedConnection managed )
        {
            _manager = manager;
            _managed = managed;
        }


        public synchronized void beforeCompletion()
        {
            // Do nothing.
        }


        public synchronized void afterCompletion( int status )
        {
            // Notify manager to release managed connection back to pool.
            _manager.releaseToPool( _managed );
        }


    }


}




