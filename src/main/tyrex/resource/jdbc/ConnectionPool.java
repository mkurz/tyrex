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
 * Copyright 2000, 2001 (C) Intalio Inc. All Rights Reserved.
 *
 */


package tyrex.resource.jdbc;


import java.io.PrintWriter;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.lang.reflect.Array;
import org.apache.log4j.Category;
import java.sql.SQLException;
import java.sql.Connection;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.sql.XAConnection;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Status;
import javax.transaction.RollbackException;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;
import tyrex.tm.TyrexTransactionManager;
import tyrex.resource.ResourceLimits;
import tyrex.resource.PoolMetrics;
import tyrex.resource.Resource;
import tyrex.resource.ResourceException;
import tyrex.services.Clock;
import tyrex.util.Primes;
import tyrex.util.Configuration;
import tyrex.util.LoggerPrintWriter;


/**
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $
 */
final class ConnectionPool
    implements Resource, DataSource, ConnectionEventListener
{


    /**
     * The initial table size, unless a maximum number of connections
     * is specified.
     */
    public static final int TABLE_SIZE = 131;


    /**
     * The connector name.
     */
    private final String                   _name;


    /**
     * The pool metrics object.
     */
    private final PoolMetrics             _metrics;


    /**
     * The pool hash table. This table lists both available and used
     * connections.
     */
    private final PoolEntry[]             _pool;


    /**
     * The resource limits.
     */
    private final ResourceLimits          _limits;



    /**
     * The transaction manager used for enlisting connections.
     */
    private final TyrexTransactionManager _txManager;


    /**
     * The category used for writing log information.
     */
    private final Category                _category;


    /**
     * The log writer to use, if trace is enabled.
     */
    private final PrintWriter             _logWriter;


    /**
     * This XA resource is used for recovery.
     */
    private final XAResource              _xaResource;


    /**
     * The next time we expect to expire a connection.
     */
    private long                          _nextExpiration;


    /**
     * The data source to use for XA connections.
     */
    private final XADataSource            _xaDataSource;


    /**
     * The data source to use for pooled (non-XA) connections.
     */
    private final ConnectionPoolDataSource _poolDataSource;


    ConnectionPool( String name, ResourceLimits limits,
                    XADataSource xaDataSource,
                    ConnectionPoolDataSource poolDataSource,
                    TyrexTransactionManager txManager, Category category )
        throws ResourceException
    {
        PooledConnection         pooled = null;

        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        if ( xaDataSource == null && poolDataSource == null )
            throw new IllegalArgumentException( "Arguments xaDataSource and poolDataSource are null" );
        if ( txManager == null )
            throw new IllegalArgumentException( "Argument txManager is null" );
        if ( category == null )
            throw new IllegalArgumentException( "Argument category is null" );

        // Need to set all these variables before we attempt to
        // create the initial number of connections.
        _name = name;
        _xaDataSource = xaDataSource;
        _poolDataSource = poolDataSource;
        _metrics = new PoolMetrics();
        _category = category;
        _logWriter = new LoggerPrintWriter( _category, null );
        _txManager= txManager;

        try {
            // Clone object to prevent changes by caller from affecting the
            // behavior of the pool.
            if ( limits == null )
                _limits = new ResourceLimits();
            else {
                _limits = (ResourceLimits) limits.clone();
                if ( _limits.getTrace() ) {
                    if ( _xaDataSource != null )
                        _xaDataSource.setLogWriter( _logWriter );
                    else
                        _poolDataSource.setLogWriter( _logWriter );
                }
            }
            
            // Set the pool table to the optimum size based on the maximum
            // number of connections expected, or a generic size.
            if ( _limits.getMaximum() > 0 )
                _pool = new PoolEntry[ Primes.nextPrime( _limits.getMaximum() ) ];
            else
                _pool = new PoolEntry[ TABLE_SIZE ];
            
            // We need at least one pooled connection to obtain the
            // connection meta data and XA resource for recovery.
            // An exception occurs if we cannot create this connection,
            // or we can't get the XA resource.
            pooled = createPooledConnection( null, null );
            if ( _xaDataSource != null ) {
                if ( pooled instanceof XAConnection )
                    _xaResource = ( (XAConnection) pooled ).getXAResource();
                else
                    throw new ResourceException( "Connection of type " + pooled.getClass().getName() +
                                                 " does not support XA transactions" );
            } else
                _xaResource = null;
            allocate( pooled, null, null, false );
            
            // Allocate as many connection as specified for the initial size
            // (excluding the one we always create before we reach this point).
            for ( int i = _limits.getInitial() - 1 ; i-- > 0 ; ) {
                pooled = createPooledConnection( null, null );
                allocate( pooled, null, null, false );
            }
        } catch ( SQLException except ) {
            throw new ResourceException( except.toString() );
        }
        _category.info( "Created connection pool for data source " + name +
                        " with initial size " + _limits.getInitial() +
                        " and maximum size " + _limits.getMaximum() );
    }


    public PoolMetrics getPoolMetrics()
    {
        return _metrics;
    }


    public Object getClientFactory()
    {
        return this;
    }


    public Class getClientFactoryClass()
    {
        return DataSource.class;
    }


    public String toString()
    {
        return _name;
    }

    
    public XAResource getXAResource()
    {
        return _xaResource;
    }


    public void destroy()
    {
    }


    public void run()
    {
        long nextExpiration;
        long clock;

        while ( true ) {
            try {
                nextExpiration = _nextExpiration;
                // No next expiration time, wait until notified.
                if ( nextExpiration == 0 )
                    wait();
                else {
                    clock = Clock.clock();
                    // Pending expiration, attempt to expire.
                    // Otherwise, wait until next expiration time.
                    if ( clock > nextExpiration )
                        expire();
                    else
                        wait( nextExpiration - clock );
                }
            } catch ( InterruptedException except ) {
                // This is our queue to stop the thread.
                return;
            }
        }
    }


    //---------------------------------------------
    // Methods defined by DataSource
    //---------------------------------------------


    public Connection getConnection()
        throws SQLException
    {
        return getConnection( null, null );
    }
    
    
    public Connection getConnection( String user, String password )
        throws SQLException
    {
        Connection        connection;
        XAResource        xaResource;
        PoolEntry         entry;
        
        entry = allocate( user, password );
        // If connection supports XA resource, we need to enlist
        // it in this or any future transaction. If this fails,
        // the connection is unuseable.
        if ( entry._xaResource != null ) {
            try {
                _txManager.enlistResource( entry._xaResource );
            } catch ( Exception except ) {
                release( entry._pooled, false );
                throw new SQLException( "Error occured using connection " + entry._pooled + ": " + except );
            }
        }
        // Obtain the client connection and register this pool as
        // the event listener. If we failed, the connection is not
        // useable and we discard it and try again.
        try {
            connection = entry._pooled.getConnection();
            return connection;
        } catch ( Exception except ) {
            release( entry._pooled, false );
            throw new SQLException( "Error occured using connection " + entry._pooled + ": " + except );
        }
    }
    
    
    private synchronized PoolEntry allocate( String user, String password )
        throws SQLException
    {
        PooledConnection  pooled;
        PoolEntry         entry;
        long              clock;
        long              timeout;
        
        timeout = _limits.getTimeout() * 1000;
        // We repeat this loop until we either get a connection, or we time out.
        // We will keep getting notified as connections are made available to the
        // pool, or discarded.
        while ( true ) {
            
            // If any connections are available we keep trying to match an
            // existing connection. It's possible that a matched connection
            // will not be useable, so we repeat until one (or none) is found.
            while ( _metrics.getAvailable() > 0 ) {
                pooled = matchPooledConnections( user, password );
                // No matched connection, exit loop so we will attempt
                // to create a new one.
                if ( pooled == null )
                    break;
                // Pooled connection matched by connector. It is an error
                // if it returns a reserved connection.
                entry = reserve( pooled );
                if ( entry == null ) {
                    release( pooled, false );
                    _category.error( "Connector error: matchPooledConnetions returned an unavailable connection" );
                } else
                    return entry;
            }
            
            // No matched connections, need to create a new one.
            // If we have more room for a new connection, we create
            // a new connection.
            if ( _limits.getMaximum() == 0 && _metrics.getTotal() < _limits.getMaximum() ) {
                pooled = createPooledConnection( user, password );
                // Need to allocate the connection. It is an error if the
                // pooled connection is already in the pool.
                entry = allocate( pooled, user, password, true );
                if ( entry == null )
                    throw new SQLException( "Connector error: createPooledConnetion returned an existing connection" );
                else
                    return entry;
            }
            
            // If we have a connection we do not use (and cannot be matched),
            // release it and make room for a new connection to be created.
            if ( _metrics.getAvailable() > 0 && discardNext() ) {
                pooled = createPooledConnection( user, password );
                // Need to allocate the connection. It is an error if the
                // pooled connection is already in the pool.
                entry = allocate( pooled, user, password, true );
                if ( entry == null )
                    throw new SQLException( "Connector error: createPooledConnetion returned an existing connection" );
                else
                    return entry;
            }

            // If timeout is zero, we throw an exception. Otherwise,
            // we go to sleep until timeout occurs or until we are
            // able to create a new connection.
            if ( timeout <= 0 )
                throw new SQLException( "Cannot allocate new connection for " +
                                        _name + ": reached limit of " +
                                        _limits.getMaximum() + " connections" );
            clock = Clock.clock();
            try {
                wait ( timeout );
                timeout -= Clock.clock() - clock;
            } catch ( InterruptedException except ) {
                // If we were interrupted (asked to stop), we always
                // report a timeout.
                timeout = 0;
            }
            if ( timeout <= 0 )
                throw new SQLException( "Cannot allocate new connection for " +
                                        _name + ": reached limit of " +
                                        _limits.getMaximum() + " connections" );
        }
        // We never reach this point;
        // throw new ApplicationServerInternalException( "Internal error" );
    }


    private PooledConnection createPooledConnection( String user, String password )
        throws SQLException
    {
        if ( _xaDataSource != null ) {
            if ( user != null )
                return _xaDataSource.getXAConnection( user, password );
            else
                return _xaDataSource.getXAConnection();
        } else {
            if ( user != null )
                return _poolDataSource.getPooledConnection( user, password );
            else
                return _poolDataSource.getPooledConnection();
        }
    }


    public PrintWriter getLogWriter()
    {
        return _logWriter;
    }


    public void setLogWriter( PrintWriter logWriter )
    {
    }


    public int getLoginTimeout()
    {
        return 0;
    }


    public void setLoginTimeout( int seconds )
    {
    }


    //---------------------------------------------
    // Methods defined by ConnectionEventListener
    //---------------------------------------------


    public void connectionClosed( ConnectionEvent event )
    {
        PooledConnection pooled;
        
        // Connection closed. Place connection back in pool.
        try {
            pooled = (PooledConnection) event.getSource();
            if ( pooled != null ) {
                if ( ! release( pooled, true ) )
                    _category.error( "Connector error: connectionClosed called with invalid connection" );
            } else
                _category.error( "Connector error: connectionClosed called without reference to connection" );
        } catch ( ClassCastException except ) {
            _category.error( "Connector error: connectionClosed called without reference to connection" );
        }
    }
    
    
    public void connectionErrorOccurred( ConnectionEvent event )
    {
        PooledConnection pooled;
        
        // Connection error. Remove connection from pool.
        try {
            pooled = (PooledConnection) event.getSource();
            if ( pooled != null ) {
                if ( ! release( pooled, false ) )
                    _category.error( "Connector error: connectionClosed called with invalid connection" );
            } else
                _category.error( "Connector error: connectionErrorOccurred called without reference to connection" );
        } catch ( ClassCastException except ) {
            _category.error( "Connector error: connectionErrorOccurred called without reference to connection" );
        }
    }
    
    
    //---------------------------------------------
    // Methods used to manage the pool
    //---------------------------------------------


    /**
     * Allocates a new connection. This method adds a new connection to the pool.
     * If <tt>reserve</tt> is true, the connection is reserved (not available).
     * This method will obtain the <tt>XAResource</tt>, if required for using the
     * connection. The connection will be added exactly once. If the connection
     * already exists, this method returns null.
     *
     * @param pooled The pooled connection to allocate
     * @param user The user name or null
     * @param password The password or null
     * @param reserve True if the connection must be reserved
     * @return The connection entry
     * @throws SQLException An error occured with the pooled connection
     */
    private synchronized PoolEntry allocate( PooledConnection pooled, String user,
                                             String password, boolean reserve )
        throws SQLException
    {
        PoolEntry        entry;
        PoolEntry        next;
        int              hashCode;
        int              index;
        XAResource       xaResource = null;
        long             nextExpiration;

        if ( pooled == null )
            throw new IllegalArgumentException( "Argument pooled is null" );

        // If XA transactions used, must obtain these objects and record them
        // in the connection pool entry. If an exception occurs, we throw it to
        // the caller.
        if ( _xaDataSource != null ) {
            if ( pooled instanceof XAConnection )
                xaResource = ( (XAConnection) pooled ).getXAResource();
            else
                throw new SQLException( "Connection of type " + pooled.getClass().getName() +
                                        " does not support XA transactions" );
        }

        // This code assures that the connection does not exist in the pool
        // before adding it. It throws an exception if the connection is
        // alread in the pool.
        hashCode = pooled.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _pool.length;
        entry = _pool[ index ];
        if ( entry == null ) {
            entry = new PoolEntry( pooled, hashCode, xaResource, user, password );
            _pool[ index ] = entry;
        } else {
            if ( entry._hashCode == hashCode && entry._pooled.equals( pooled ) )
                return null;
            next = entry._nextEntry;
            while ( next != null ) {
                if ( next._hashCode == hashCode && next._pooled.equals( pooled ) )
                    return null;
                entry = next;
                next = entry._nextEntry;
            }
            next = new PoolEntry( pooled, hashCode, xaResource, user, password );
            entry._nextEntry = next;
            entry = next;
        }
        entry._pooled.addConnectionEventListener( this );
        // Record that a new connection has been created. This will increase
        // the total pool size. If not reserved, mark the connection as
        // available and increase the available count.
        _metrics.recordCreated();
        if ( ! reserve ) {
            entry._available = true;
            try {
                _metrics.changeAvailable( 1 );
            } catch ( IllegalStateException except ) {
                _category.error( "Internal error in connection pool " + this + ": " + except.getMessage() );
            }
        }
        // Calculate the next expiration time based on this connection.
        // If the next expiration time is soon, we notify the background
        // thread.
        if ( _limits.getMaxRetain() > 0 ) {
            nextExpiration = entry._timeStamp + ( _limits.getMaxRetain() * 1000 );
            if ( _nextExpiration == 0 || _nextExpiration > nextExpiration ) {
                _nextExpiration = nextExpiration;
                notifyAll();
            }
        }
        return entry;
    }


    /**
     * Reserves a connection. This method attempts to reserve a connection,
     * such that it is no longer available from the pool. If the connection
     * can be reserved, it is returned. If the connection does not exist
     * in the pool, or is already reserved, this method returns null.
     *
     * @param pooled The pooled connection to reserve
     * @return The connection entry if reserved, or null
     */
    private synchronized PoolEntry reserve( PooledConnection pooled )
    {
        PoolEntry entry;
        int       hashCode;
        int       index;
        long      clock;
        
        if ( pooled == null )
            return null;
        hashCode = pooled.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _pool.length;
        entry = _pool[ index ];
        while ( entry != null && entry._hashCode != hashCode &&
                ! entry._pooled.equals( pooled ) )
            entry = entry._nextEntry;
        if ( entry != null && entry._available ) {
            entry._available = false;
            try {
                _metrics.changeAvailable( -1 );
            } catch ( IllegalStateException except ) {
                _category.error( "Internal error in connection pool " + this + ": " + except.getMessage() );
            }
            clock = Clock.clock();
            _metrics.recordUnusedDuration( (int) ( clock - entry._timeStamp ) );
            entry._timeStamp = clock;
            return entry;
        }
        return null;
    }


    /**
     * Releases a connection. The connection is returned to the pool and
     * becomes available for subsequent use.
     * <p>
     * This method returns true if the connection was used and is now
     * available. It returns false if the connection was not found in
     * the pool.
     * <p>
     * The XA resource, if available, is delisted from the transaction
     * manager and dissociated from the thread context.
     * <p>
     * If <tt>success</tt> is false, it assumes the connection has
     * been released due to an error. There is no need to discard a
     * connection released with an error.
     *
     * @param pooled The pooled connection to release
     * @param success True if the connection is useable, false if
     * the connection is released due to an error
     * @return True if the connection has been released
     */
    private synchronized boolean release( PooledConnection pooled, boolean success )
    {
        PoolEntry entry;
        int       hashCode;
        int       index;
        long      clock;
        long      nextExpiration;

        if ( pooled == null )
            return false;
        hashCode = pooled.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _pool.length;
        entry = _pool[ index ];
        if ( entry != null ) {
            if ( hashCode == entry._hashCode && entry._pooled.equals( pooled ) ) {
                if ( entry._available )
                    return false;
            } else {
                entry = entry._nextEntry;
                while ( entry != null && hashCode != entry._hashCode &&
                        ! entry._pooled.equals( pooled ) )
                    entry = entry._nextEntry;
                if ( entry == null || entry._available ) 
                    return false;
            }
        }
        // If we reached this point, we have the connection entry
        // and the connection is not reserved. If an XA resource
        // is used, we need to delist it. If successful, we mark
        // another available connection, clean it up and notify
        // the pool that a new connection is available. Otherwise,
        // we discard the connection with an error.
        try {
            clock = Clock.clock();
            _metrics.recordUsedDuration( (int) ( clock - entry._timeStamp ) );
            entry._timeStamp = clock;
            entry._available = true;
            if ( entry._xaResource != null )
                _txManager.delistResource( entry._xaResource, success ? XAResource.TMSUCCESS : XAResource.TMFAIL );
            if ( success ) {
                try {
                    _metrics.changeAvailable( 1 );
                } catch ( IllegalStateException except ) {
                    _category.error( "Internal error in connection pool " + this + ": " + except.getMessage() );
                }

                // Calculate the next expiration time based on this connection.
                if ( _limits.getMaxRetain() > 0 ) {
                    nextExpiration = entry._timeStamp + ( _limits.getMaxRetain() * 1000 );
                    if ( _nextExpiration == 0 || _nextExpiration > nextExpiration )
                        _nextExpiration = nextExpiration;
                }
                // We notify any blocking thread that it can attempt to
                // get a new connection.
                notifyAll();
            } else
                discard( pooled, false );
        } catch ( Exception except ) {
            // An error occured when attempting to clean up the connection.
            // Log the error and discard the connection.
            _category.error( "Error attempting to release connection " + entry._pooled +
                             " by connection pool " + this, except );
            discard( entry._pooled, false );
        }
        return true;
    }


    /**
     * Discards one connection. One connection is removed from the pool,
     * allowing a different connection to be created in its place.
     * <p>
     * This method returns true if at least once connection has been
     * removed from the pool.
     *
     * @return True if at least one connection has been discarded
     */
    private synchronized boolean discardNext()
    {
        PoolEntry entry = null;
        PoolEntry next;
        long      clock;

        // We iterate over all entries in the pool until we find
        // one available entry. At the end of this iteration, entry
        // points to the available entry, or null if none was found.
        for ( int i = _pool.length ; i-- > 0 ; ) {
            entry = _pool[ i ];
            if ( entry != null ) {
                if ( entry._available ) {
                    _pool[ i ] = entry._nextEntry;
                    break;
                } else {
                    next = entry._nextEntry;
                    while ( next != null ) {
                        if ( next._available ) {
                            entry._nextEntry = next._nextEntry;
                            entry = next;
                            break;
                        }
                        entry = next;
                        next = next._nextEntry;
                    }
                    if ( next != null )
                        break;
                }
            }
            // Set entry to null, so it is null when we exit this loop.
            entry = null;
        }
        if ( entry == null )
            return false;

        // If we reached this point, we have the connection entry
        // and the connection is not reserved. We notify the pool,
        // such that it can create a new connection available.
        try {
            clock = Clock.clock();
            _metrics.recordUnusedDuration( (int) ( clock - entry._timeStamp ) );
            _metrics.recordDiscard();
            entry._pooled.removeConnectionEventListener( this );
            entry._pooled.close();
        } catch ( Exception except ) {
            // An error occured when attempting to destroy up the connection.
            // Log the error and discard the connection.
            _category.error( "Error attempting to destory connection " + entry._pooled +
                             " by connection pool " + this, except );
        }
        notifyAll();
        return true;
    }


    /**
     * Discards a connection. The connection is removed from the pool
     * and is no longer available.
     * <p>
     * This method returns true if the connection was available in the
     * pool and has been discarded, but will not discard a connection
     * that is currently in use.
     * <p>
     * If <tt>success</tt> is false, it assumes the connection has
     * been discarded due to an error.
     *
     * @param pooled The pooled connection to discard
     * @param success True if the connection is useable, false if
     * the connection is discarded due to an error
     * @return True if the connection has been discarded
     */
    private synchronized boolean discard( PooledConnection pooled, boolean success )
    {
        PoolEntry entry;
        PoolEntry next;
        int       hashCode;
        int       index;
        long      clock;

        if ( pooled == null )
            return false;
        hashCode = pooled.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _pool.length;
        entry = _pool[ index ];
        if ( entry == null )
            return false;
        if ( hashCode == entry._hashCode && entry._pooled.equals( pooled ) ) {
            _pool[ index ] = entry._nextEntry;
            if ( ! entry._available )
                return false;
        } else {
            next = entry._nextEntry;
            while ( next != null ) {
                if ( hashCode == next._hashCode && next._pooled.equals( pooled ) ) {
                    if ( ! next._available )
                        return false;
                    entry._nextEntry = next._nextEntry;
                    entry = next;
                    break;
                }
                entry = next;
                next = entry._nextEntry;
            }
            if ( next == null )
                return false;
        }
        // If we reached this point, we have the connection entry
        // and the connection is not reserved. We notify the pool,
        // such that it can create a new connection available.
        try {
            clock = Clock.clock();
            _metrics.recordUnusedDuration( (int) ( clock - entry._timeStamp ) );
            if ( success )
                _metrics.recordDiscard();
            else
                _metrics.recordError();
            entry._pooled.removeConnectionEventListener( this );
            entry._pooled.close();
        } catch ( Exception except ) {
            // An error occured when attempting to destroy up the connection.
            // Log the error and discard the connection.
            _category.error( "Error attempting to destory connection " + entry._pooled +
                             " by connection pool " + this, except );
        }
        // We notify any blocking thread that it can attempt to
        // get a new connection.
        notifyAll();
        return true;
    }


    /**
     * Called periodically to expire connections that have been
     * available in the pool for longer than maxRetain seconds.
     * This method returns the next expiration time, or zero if
     * no connection is expected to expire soon.
     *
     * @return The next expiration time, or zero if no connection
     * is expected to expire soon.
     */
    protected synchronized long expire()
    {
        PoolEntry  entry;
        PoolEntry  next;
        long       clock;
        long       oldest;
        long       nextExpiration;

        // Without maxRetain we do not attempt to expire connections.
        if ( _limits.getMaxRetain() == 0 )
            return 0;
        // We don't enter the loop if no connection is subject to expire.
        // We know a connection is about to expire if the system clock
        // minus max retain, is past the connection's timeStamp (true only
        // for available connections).
        clock = Clock.clock();
        if ( clock >= _nextExpiration ) {
            oldest = clock - ( _limits.getMaxRetain() * 1000 );
            nextExpiration = 0;
            for ( int i = _pool.length ; i-- > 0 ; ) {
                entry = null;
                next = _pool[ i ];
                while ( next != null ) {
                    if ( next._available ) {
                        if ( next._timeStamp <= oldest ) {
                            if ( entry == null )
                                _pool[ i ] = next._nextEntry;
                            else
                                entry._nextEntry = next._nextEntry;
                            _metrics.recordUnusedDuration( (int) ( clock - next._timeStamp ) );
                            _metrics.recordDiscard();
                            try {
                                next._pooled.removeConnectionEventListener( this );
                                next._pooled.close();
                            } catch ( Exception except ) {
                                // An error occured when attempting to destroy up the connection.
                                // Log the error and discard the connection.
                                _category.error( "Error attempting to destory connection " + next._pooled +
                                                 " by connection pool " + this, except );
                            }
                            next = next._nextEntry;
                        } else {
                            if ( nextExpiration == 0 || nextExpiration > next._timeStamp  )
                                nextExpiration = next._timeStamp;
                            entry = next;
                            next = next._nextEntry;
                        }
                    } else {
                        entry = next;
                        next = next._nextEntry;
                    }
                }
            }
            // We calculate the next expiraiton time base on the timeStamp,
            // so we need to add maxRetain to get the actual clock time.
            if ( nextExpiration != 0 )
                nextExpiration += ( _limits.getMaxRetain() * 1000 );
            _nextExpiration = nextExpiration;
        }
        // If no connection was subject to expire, we return the same
        // nextExpiration.
        return _nextExpiration;
    }


    /**
     * Returns the next available pooled connection that
     * mathces these criteria. If <tt>user</tt> is null, returns
     * a pooled connection that has no user name and password,
     * otherwise returns a pooled conneciton with the same user
     * name and password. Returns null if no matching connection
     * is found.
     *
     * @param user The user name, or null
     * @param password The password, or null
     * @return A matching connection, or null if no connection matched
     */
    private synchronized PooledConnection matchPooledConnections( String user, String password )
    {
        PoolEntry  entry;

        for ( int i = _pool.length ; i-- > 0 ; ) {
            entry = _pool[ i ];
            while ( entry != null ) {
                if ( user == null && entry._user == null )
                    return entry._pooled;
                else if ( user != null && entry._user != null && user.equals( entry._user ) &&
                          ( ( password == null && entry._password == null ) ||
                            ( password != null && entry._password != null &&
                              password.equals( entry._password ) ) ) )
                    return entry._pooled;
                entry = entry._nextEntry;
            }
        }
        return null;
    }
    
    
}
