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
 */


package tyrex.resource.jca;


import java.io.PrintWriter;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.lang.reflect.Array;
import org.apache.log4j.Category;
import javax.resource.ResourceException;
import javax.resource.NotSupportedException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ResourceAllocationException;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ApplicationServerInternalException;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import tyrex.tm.TyrexTransactionManager;
import tyrex.tm.impl.ThreadContext;
import tyrex.resource.Resource;
import tyrex.resource.PoolMetrics;
import tyrex.resource.PoolLimits;
import tyrex.services.Clock;
import tyrex.services.DaemonMaster;
import tyrex.util.Primes;
import tyrex.util.LoggerPrintWriter;


/**
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.7 $
 */
final class ConnectionPool
    extends PoolMetrics
    implements Resource, ConnectionManager, Set, ConnectionEventListener, Runnable
{


    /**
     * The initial table size, unless a maximum number of connections
     * is specified.
     */
    public static final int TABLE_SIZE = 131;


    /**
     * The connector name.
     */
    private final String                  _name;


    /**
     * The pool hash table. This table lists both available and used
     * connections.
     */
    private final PoolEntry[]             _pool;

 
    /**
     * The pool limits.
     */
    private final PoolLimits              _limits;


    /**
     * The transaction manager used for enlisting connections.
     */
    private final TyrexTransactionManager _txManager;


    /**
     * The connector loader.
     */
    private final ConnectorLoader         _loader;


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
     * The client connection factory.
     */
    private final Object                  _factory;


    /**
     * The next time we expect to expire a connection.
     */
    private long                          _nextExpiration;


    /**
     * True if this pool has been destroyed.
     */
    private boolean                       _destroyed;


    ConnectionPool( String name, PoolLimits limits, ConnectorLoader loader,
                    TyrexTransactionManager txManager, Category category )
        throws ResourceException
    {
        ManagedConnection         managed = null;
        ManagedConnectionMetaData metaData;
        StringBuffer              buffer;
        int                       maximum;
        int                       initial;

        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        if ( loader == null )
            throw new IllegalArgumentException( "Argument loader is null" );
        if ( txManager == null )
            throw new IllegalArgumentException( "Argument txManager is null" );
        if ( category == null )
            throw new IllegalArgumentException( "Argument category is null" );

        // Need to set all these variables before we attempt to
        // create the initial number of connections.
        _name = name;
        _loader = loader;
        _category = category;
        _txManager= txManager;

        // Clone object to prevent changes by caller from affecting the
        // behavior of the pool.
        if ( limits == null ) {
            _limits = new PoolLimits();
            _logWriter = null;
        } else {
            _limits = limits;
            if ( _limits.getTrace() ) {
                _logWriter = new LoggerPrintWriter( _category, null );
                _loader.setLogWriter( _logWriter );
            } else 
                _logWriter = null;
        }

        // Set the pool table to the optimum size based on the maximum
        // number of connections expected, or a generic size.
        maximum = _limits.getMaximum();
        if ( maximum > 0 )
            _pool = new PoolEntry[ Primes.nextPrime( maximum ) ];
        else
            _pool = new PoolEntry[ TABLE_SIZE ];

        // We need at least one managed connection to obtain the
        // connection meta data and XA resource for recovery.
        // An exception occurs if we cannot create this connection,
        // or we can't get the XA resource.
        managed = _loader.createManagedConnection( null, null );
        if ( _loader._xaSupported )
            _xaResource = managed.getXAResource();
        else
            _xaResource = null;
        allocate( managed, false );

        // Obtain the maximum number of connections reported by the
        // meta-data and if necessary, update the maximum number of
        // connections supported.
        metaData = managed.getMetaData();
        if ( metaData != null && metaData.getMaxConnections() > 0 &&
             metaData.getMaxConnections() < maximum ) {
            maximum = metaData.getMaxConnections();
            _limits.setMaximum( maximum );
        }

        // Allocate as many connection as specified for the initial size
        // (excluding the one we always create before we reach this point).
        initial = _limits.getInitial();
        if ( maximum > 0 && initial > maximum )
            initial = maximum;
        for ( int i = initial - 1 ; i-- > 0 ; ) {
            managed = _loader.createManagedConnection( null, null );
            allocate( managed, false );
        }
        _factory = _loader.createConnectionFactory( this );

        // This string is used to report the EIS product and version
        buffer = new StringBuffer();
        if ( metaData.getEISProductName() != null ) {
            buffer.append( metaData.getEISProductName() );
            if ( metaData.getEISProductVersion() != null )
                buffer.append( "  " ).append( metaData.getEISProductVersion() );
        } else
            buffer.append( _loader.toString() );
        if ( _logWriter != null ) {
            _logWriter.println( "Created connection pool for manager " + name +
                                " with initial size " + initial +
                                " and maximum limit " + maximum );
            _logWriter.println( buffer.toString() );
        }

        DaemonMaster.addDaemon( this, "Connection Pool " + name );
    }


    public PoolMetrics getPoolMetrics()
    {
        return this;
    }


    public PoolLimits getPoolLimits()
    {
        return _limits;
    }


    public Object getClientFactory()
    {
        return _factory;
    }


    public Class getClientFactoryClass()
    {
        return _factory.getClass();
    }


    public String toString()
    {
        return _name;
    }

    
    public XAResource getXAResource()
    {
        return _xaResource;
    }


    public synchronized void destroy()
    {
        PoolEntry entry;
        long      clock;

        if ( _destroyed )
            return;
        _destroyed = true;
        clock = Clock.clock();
        DaemonMaster.removeDaemon( this );
        for ( int i = _pool.length ; i-- > 0 ; ) {
            entry = _pool[ i ];
            while ( entry != null ) {
                if ( entry._available ) {
                    recordUnusedDuration( (int) ( clock - entry._timeStamp ) );
                    recordDiscard();
                }
                try {
                    entry._managed.removeConnectionEventListener( this );
                    entry._managed.destroy();
                } catch ( Exception except ) {
                    _category.error( "Error attempting to destory connection " + entry._managed +
                                     " by connection pool " + this, except );
                }
                entry._available = false;
                entry = entry._nextEntry;
            }
            _pool[ i ] = null;
        }
        _total = 0;
        _available = 0;
    }


    public synchronized void run()
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
    // Methods defined by ConnectionManager
    //---------------------------------------------


    public Object allocateConnection( ManagedConnectionFactory factory,
                                      ConnectionRequestInfo requestInfo )
        throws ResourceException
    {
        Object            connection;
        XAResource        xaResource;
        PoolEntry         entry;
        
        if ( _destroyed )
            throw new ResourceException( "Connection pool has been destroyed" );
        if ( factory != _loader.getConfigFactory() ) {
            _category.error( "Connector error: called allocateConnection with the wrong factory" );
            throw new ResourceAllocationException( "Connector error: called allocateConnection with the wrong factory" );
        }

        entry = allocate( requestInfo );
        // If connection supports XA resource, we need to enlist
        // it in this or any future transaction. If this fails,
        // the connection is unuseable.
        if ( entry._xaResource != null ) {
            try {
                _txManager.enlistResource( entry._xaResource );
            } catch ( Exception except ) {
                release( entry._managed, false );
                throw new ResourceAllocationException( "Error occured using connection " + entry._managed + ": " + except );
            }
        }
        // Obtain the client connection and register this pool as
        // the event listener. If we failed, the connection is not
        // useable and we discard it and try again.
        try {
            connection = _loader.getConnection( entry._managed,
                                                ThreadContext.getThreadContext().getSubject(),
                                                requestInfo );
            return connection;
        } catch ( Exception except ) {
            release( entry._managed, false );
            if (_category.isDebugEnabled()) {
                _category.debug("Error occured using connection " + entry._managed, except);
            }
            throw new ResourceAllocationException( "Error occured using connection " + entry._managed + ": " + except );
        }
    }


    private synchronized PoolEntry allocate( ConnectionRequestInfo requestInfo )
        throws ResourceException
    {
        ManagedConnection managed;
        PoolEntry         entry;
        long              clock;
        long              timeout;
        int               maximum;
        
        timeout = _limits.getTimeout() * 1000;
        // We repeat this loop until we either get a connection, or we time out.
        // We will keep getting notified as connections are made available to the
        // pool, or discarded.
        while ( true ) {

            // If any connections are available we keep trying to match an
            // existing connection. It's possible that a matched connection
            // will not be useable, so we repeat until one (or none) is found.
            while ( _available > 0 ) {
                managed = _loader.matchManagedConnections( this, ThreadContext.getThreadContext().getSubject(),
                                                           requestInfo );
                // No matched connection, exit loop so we will attempt
                // to create a new one.
                if ( managed == null )
                    break;
                // Managed connection matched by connector. It is an error
                // if it managed a reserved connection.
                entry = reserve( managed );
                if ( entry == null ) {
                    release( managed, false );
                    _category.error( "Connector error: matchManagedConnetions returned an unavailable connection" );
                } else
                    return entry;
            }

            // No matched connections, need to create a new one.
            // If we have more room for a new connection, we create
            // a new connection. Otherwise, if we have a connection we
            // do not use (and cannot be matched), release it and make
            // room for a new connection to be created.
            maximum = _limits.getMaximum();
            if ( maximum == 0 || _total < maximum ||
                 ( _available > 0 && discardNext() ) ) {
                managed = _loader.createManagedConnection( ThreadContext.getThreadContext().getSubject(),
                                                           requestInfo );
                // Need to allocate the connection. It is an error if the
                // managed connection is already in the pool.
                entry = allocate( managed, true );
                if ( entry == null )
                    throw new ResourceException( "Connector error: createManagedConnetion returned an existing connection" );
                else
                    return entry;
            }

            // If timeout is zero, we throw an exception. Otherwise,
            // we go to sleep until timeout occurs or until we are
            // able to create a new connection.
            if ( timeout <= 0 )
                throw new ResourceAllocationException( "Cannot allocate new connection for " +
                                                       _name + ": reached limit of " +
                                                       maximum + " connections" );
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
                throw new ResourceAllocationException( "Cannot allocate new connection for " +
                                                       _name + ": reached limit of " +
                                                       maximum + " connections" );
        }
        // We never reach this point;
        // throw new ApplicationServerInternalException( "Internal error" );
    }


    //---------------------------------------------
    // Methods defined by ConnectionEventListener
    //---------------------------------------------


    public void connectionClosed( ConnectionEvent event )
    {
        ManagedConnection managed;
        Object            connection;
        
        // Connection closed. Place connection back in pool.
        try {
            connection = event.getConnectionHandle();
            managed = (ManagedConnection) event.getSource();
            if ( managed != null ) {
                if ( ! release( managed, true ) )
                    _category.error( "Connector error: connectionClosed called with invalid connection" );
            } else
                _category.error( "Connector error: connectionClosed called without reference to connection" );
        } catch ( ClassCastException except ) {
            _category.error( "Connector error: connectionClosed called without reference to connection" );
        }
    }
    
    
    public void connectionErrorOccurred( ConnectionEvent event )
    {
        ManagedConnection managed;
        
        // Connection error. Remove connection from pool.
        try {
            managed = (ManagedConnection) event.getSource();
            if ( managed != null ) {
                if ( ! release( managed, false ) )
                    _category.error( "Connector error: connectionClosed called with invalid connection" );
            } else
                _category.error( "Connector error: connectionErrorOccurred called without reference to connection" );
        } catch ( ClassCastException except ) {
            _category.error( "Connector error: connectionErrorOccurred called without reference to connection" );
        }
    }
    
    
    public void localTransactionCommitted( ConnectionEvent event )
    {
        // Local transactions not supported by this manager.
    }
    
    
    public void localTransactionRolledback( ConnectionEvent event )
    {
        // Local transactions not supported by this manager.
    }
    

    public void localTransactionStarted( ConnectionEvent event )
    {
        // Local transactions not supported by this manager.
    }
                                        
                                        
    //---------------------------------------------
    // Methods used to manage the pool
    //---------------------------------------------

    /**
     * Appends a new connection. This method does not complain if the connection
     * pool is at maximum capacity, the connection cannot be used, or the
     * connection already exists in the pool.
     *
     * @param managed The managed connection to allocate
     * @param reserve True if the connection must be reserved
     * @return True if the managed connection has been added
     */
    private boolean append( ManagedConnection managed )
    {
        try {
            return ( allocate( managed, false ) != null );
        } catch ( ResourceException except ) {
            return false;
        }
    }


    /**
     * Allocates a new connection. This method adds a new connection to the pool.
     * If <tt>reserve</tt> is true, the connection is reserved (not available).
     * This method will obtain the <tt>XAResource</tt> or <tt>LocalTransaction</tt>,
     * if required for using the connection. The connection will be added exactly
     * once. If the connection already exists, this method returns null.
     *
     * @param managed The managed connection to allocate
     * @param reserve True if the connection must be reserved
     * @return The connection entry
     * @throws ResourceException An error occured with the managed connection
     */
    private synchronized PoolEntry allocate( ManagedConnection managed, boolean reserve )
        throws ResourceException
    {
        PoolEntry        entry;
        PoolEntry        next;
        int              hashCode;
        int              index;
        XAResource       xaResource = null;
        LocalTransaction localTx = null;
        long             nextExpiration;
        int              maxRetain;

        if ( managed == null )
            throw new IllegalArgumentException( "Argument managed is null" );

        // If XA or local transactions used, must obtain these objects and
        // record them in the connection pool entry. If an exception occurs,
        // we throw it to the caller.
        if ( _loader._xaSupported )
            xaResource = managed.getXAResource();
        else if ( _loader._localSupported )
            localTx = managed.getLocalTransaction();
        // This code assures that the connection does not exist in the pool
        // before adding it. It throws an exception if the connection is
        // alread in the pool.
        hashCode = managed.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _pool.length;
        entry = _pool[ index ];
        if ( entry == null ) {
            entry = new PoolEntry( managed, hashCode, xaResource, localTx );
            _pool[ index ] = entry;
        } else {
            if ( entry._hashCode == hashCode && entry._managed.equals( managed ) ) {
                _category.error( "Connector error: Allocated connection " + managed + " already in pool" );
                return null;
            }
            next = entry._nextEntry;
            while ( next != null ) {
                if ( next._hashCode == hashCode && next._managed.equals( managed ) ) {
                    _category.error( "Connector error: Allocated connection " + managed + " already in pool" );
                    return null;
                }
                entry = next;
                next = entry._nextEntry;
            }
            next = new PoolEntry( managed, hashCode, xaResource, localTx );
            entry._nextEntry = next;
            entry = next;
        }
        entry._managed.addConnectionEventListener( this );
        // Record that a new connection has been created. This will increase
        // the total pool size. If not reserved, mark the connection as
        // available and increase the available count.
        recordCreated();
        if ( ! reserve ) {
            entry._available = true; 
            _available += 1;
        }
        // Calculate the next expiration time based on this connection.
        // If the next expiration time is soon, we notify the background
        // thread.
        maxRetain = _limits.getMaxRetain();
        if ( maxRetain > 0 ) {
            nextExpiration = entry._timeStamp + ( maxRetain * 1000 );
            if ( _nextExpiration == 0 || _nextExpiration > nextExpiration ) {
                _nextExpiration = nextExpiration;
                notifyAll();
            }
        }
        if ( _logWriter != null )
            _logWriter.println( "Allocated new connection " + entry._managed );
        return entry;
    }


    /**
     * Reserves a connection. This method attempts to reserve a connection,
     * such that it is no longer available from the pool. If the connection
     * can be reserved, it is returned. If the connection does not exist
     * in the pool, or is already reserved, this method returns null.
     *
     * @param managed The managed connection to reserve
     * @return The connection entry if reserved, or null
     */
    private synchronized PoolEntry reserve( ManagedConnection managed )
    {
        PoolEntry entry;
        int       hashCode;
        int       index;
        long      clock;
        
        if ( managed == null )
            return null;
        hashCode = managed.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _pool.length;
        entry = _pool[ index ];
        while ( entry != null && entry._hashCode != hashCode &&
                ! entry._managed.equals( managed ) )
            entry = entry._nextEntry;
        if ( entry != null && entry._available ) {
            entry._available = false;
            _available -= 1;
            clock = Clock.clock();
            recordUnusedDuration( (int) ( clock - entry._timeStamp ) );
            entry._timeStamp = clock;
            if ( _logWriter != null )
                _logWriter.println( "Reusing connection " + entry._managed );
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
     * @param managed The managed connection to release
     * @param success True if the connection is useable, false if
     * the connection is released due to an error
     * @return True if the connection has been released
     */
    private synchronized boolean release( ManagedConnection managed, boolean success )
    {
        PoolEntry entry;
        int       hashCode;
        int       index;
        long      clock;
        long      nextExpiration;
        int       maxRetain;

        if ( managed == null )
            return false;
        hashCode = managed.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _pool.length;
        entry = _pool[ index ];
        if ( entry != null ) {
            if ( hashCode == entry._hashCode && entry._managed.equals( managed ) ) {
                if ( entry._available ) {
                    _category.error( "Connector error: Released connection " + managed + " not in pool" );
                    return false;
                }
            } else {
                entry = entry._nextEntry;
                while ( entry != null && hashCode != entry._hashCode &&
                        ! entry._managed.equals( managed ) )
                    entry = entry._nextEntry;
                if ( entry == null || entry._available ) {
                    _category.error( "Connector error: Released connection " + managed + " not in pool" );
                    return false;
                }
            }
        } else {
            _category.error( "Connector error: Released connection " + managed + " not in pool" );
            return false;
        }
        // If we reached this point, we have the connection entry
        // and the connection is not reserved. If an XA resource
        // is used, we need to delist it. If successful, we mark
        // another available connection, clean it up and notify
        // the pool that a new connection is available. Otherwise,
        // we discard the connection with an error.
        try {
            clock = Clock.clock();
            recordUsedDuration( (int) ( clock - entry._timeStamp ) );
            entry._timeStamp = clock;
            entry._available = true;
            if ( entry._xaResource != null )
                _txManager.delistResource( entry._xaResource, success ? XAResource.TMSUCCESS : XAResource.TMFAIL );
            if ( success ) {
                _available += 1;
                entry._managed.cleanup();

                // Calculate the next expiration time based on this connection.
                maxRetain = _limits.getMaxRetain();
                if ( maxRetain > 0 ) {
                    nextExpiration = entry._timeStamp + ( maxRetain * 1000 );
                    if ( _nextExpiration == 0 || _nextExpiration > nextExpiration )
                        _nextExpiration = nextExpiration;
                }
                // We notify any blocking thread that it can attempt to
                // get a new connection.
                notifyAll();
            } else
                discard( managed, false );
        } catch ( Exception except ) {
            // An error occured when attempting to clean up the connection.
            // Log the error and discard the connection.
            _category.error( "Error attempting to release connection " + entry._managed +
                             " by connection pool " + this, except );
            discard( entry._managed, false );
        }
        if ( _logWriter != null )
            _logWriter.println( "Released connection " + managed );
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
            recordUnusedDuration( (int) ( clock - entry._timeStamp ) );
            recordDiscard();
            entry._managed.removeConnectionEventListener( this );
            entry._managed.destroy();
        } catch ( Exception except ) {
            // An error occured when attempting to destroy up the connection.
            // Log the error and discard the connection.
            _category.error( "Error attempting to destory connection " + entry._managed +
                             " by connection pool " + this, except );
        }
        notifyAll();
        if ( _logWriter != null )
            _logWriter.println( "Discarded connection " + entry._managed );
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
     * @param managed The managed connection to discard
     * @param success True if the connection is useable, false if
     * the connection is discarded due to an error
     * @return True if the connection has been discarded
     */
    private synchronized boolean discard( ManagedConnection managed, boolean success )
    {
        PoolEntry entry;
        PoolEntry next;
        int       hashCode;
        int       index;
        long      clock;

        if ( managed == null )
            return false;
        hashCode = managed.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _pool.length;
        entry = _pool[ index ];
        if ( entry == null )
            return false;
        if ( hashCode == entry._hashCode && entry._managed.equals( managed ) ) {
            _pool[ index ] = entry._nextEntry;
            if ( ! entry._available )
                return false;
        } else {
            next = entry._nextEntry;
            while ( next != null ) {
                if ( hashCode == next._hashCode && next._managed.equals( managed ) ) {
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
            recordUnusedDuration( (int) ( clock - entry._timeStamp ) );
            if ( success )
                recordDiscard();
            else
                recordError();
            entry._managed.removeConnectionEventListener( this );
            entry._managed.destroy();
        } catch ( Exception except ) {
            // An error occured when attempting to destroy up the connection.
            // Log the error and discard the connection.
            _category.error( "Error attempting to destory connection " + entry._managed +
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
        int        maxRetain;

        // Without maxRetain we do not attempt to expire connections.
        maxRetain = _limits.getMaxRetain();
        if ( maxRetain == 0 )
            return 0;
        maxRetain = maxRetain * 1000;
        // We don't enter the loop if no connection is subject to expire.
        // We know a connection is about to expire if the system clock
        // minus max retain, is past the connection's timeStamp (true only
        // for available connections).
        clock = Clock.clock();
        if ( clock >= _nextExpiration ) {
            oldest = clock - maxRetain;
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
                            recordUnusedDuration( (int) ( clock - next._timeStamp ) );
                            recordDiscard();
                            try {
                                next._managed.removeConnectionEventListener( this );
                                next._managed.destroy();
                            } catch ( Exception except ) {
                                // An error occured when attempting to destroy up the connection.
                                // Log the error and discard the connection.
                                _category.error( "Error attempting to destory connection " + next._managed +
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
                nextExpiration += maxRetain;
            _nextExpiration = nextExpiration;
        }
        // If no connection was subject to expire, we return the same
        // nextExpiration.
        return _nextExpiration;
    }
    

    //---------------------------------------------
    // Methods defined by java.util.Set
    //---------------------------------------------


    public synchronized boolean add( Object object )
    {
        if ( object == null )
            throw new IllegalArgumentException( "Argument object is null" );
        if ( _destroyed )
            return false;
        if ( object instanceof ManagedConnection )
            return append( (ManagedConnection) object );
        else
            throw new ClassCastException( "Object not of type ManagedConnection" );
    }


    public synchronized boolean addAll( Collection collection )
    {
        Iterator iterator;
        Object   object;
        boolean  changed = false;
        
        if ( collection == null )
            throw new IllegalArgumentException( "Argument collection is null" );
        if ( _destroyed )
            return false;
        iterator = collection.iterator();
        while ( iterator.hasNext() ) {
            object = iterator.next();
            if ( object instanceof ManagedConnection ) {
                if ( append( (ManagedConnection) object ) )
                    changed = true;
            } else
                throw new ClassCastException( "Object not of type ManagedConnection" );
        }
        return changed;
    }


    public synchronized void clear()
    {
        Iterator iterator;
        Object   object;
        
        if ( _destroyed )
            return;
        iterator = iterator();
        while ( iterator.hasNext() ) {
            object = iterator.next();
            discard( (ManagedConnection) object, true );
        }
    }


    public synchronized boolean contains( Object object )
    {
        int       index;
        int       hashCode;
        PoolEntry entry;
        
        if ( object == null || _destroyed )
            return false;
        hashCode = object.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _pool.length;
        entry = _pool[ index ];
        while ( entry != null ) {
            if ( entry._hashCode == hashCode && entry._managed.equals( object ) )
                return entry._available;
            entry = entry._nextEntry;
        }
        return false;
    }
    
    
    public synchronized boolean containsAll( Collection collection )
    {
        Iterator iterator;
        
        if ( collection == null || _destroyed )
            throw new IllegalArgumentException( "Argument collection is null" );
        iterator = collection.iterator();
        while ( iterator.hasNext() ) {
            if ( ! contains( iterator.next() ) )
                return false;
        }
        return true;
    }
    

    public boolean equals( Object object )
    {
        if ( object == this )
            return true;
        if ( ! ( object instanceof Set ) )
            return false;
        if ( ( (Set) object ).size() != _available )
            return false;
        return containsAll( (Set) object );
    }
    
    
    public synchronized int hashCode()
    {
        int         hashCode;
        PoolEntry   entry;
        PoolEntry[] pool;
        
        pool = _pool;
        hashCode = 0;
        for ( int i = 0 ; i < pool.length ; ++i ) {
            entry = pool[ i ];
            while ( entry != null ) {
                if ( entry._available )
                    hashCode += entry._hashCode;
                entry = entry._nextEntry;
            }
        }
        return hashCode;
    }
    
    
    public boolean isEmpty()
    {
        // The set size is the number of available connections.
        return ( _available == 0 );
    }


    public Iterator iterator()
    {
        return new PoolIterator();
    }


    public synchronized boolean remove( Object object )
    {
        if ( object == null )
            throw new IllegalArgumentException( "Argument object is null" );
        if ( _destroyed )
            return false;
        if ( object instanceof ManagedConnection )
            return discard( (ManagedConnection) object, true );
        else
            throw new ClassCastException( "Object not of type ManagedConnection" );
    }


    public synchronized boolean removeAll( Collection collection )
    {
        Iterator iterator;
        Object   object;
        boolean  changed = false;
        
        if ( collection == null )
            throw new IllegalArgumentException( "Argument collection is null" );
        if ( _destroyed )
            return false;
        iterator = collection.iterator();
        while ( iterator.hasNext() ) {
            object = iterator.next();
            if ( object instanceof ManagedConnection ) {
                if ( discard( (ManagedConnection) object, true ) )
                    changed = true;
            } else
                throw new ClassCastException( "Object not of type ManagedConnection" );
        }
        return changed;
    }


    public synchronized boolean retainAll( Collection collection )
    {
        Iterator iterator;
        Object   object;
        boolean  changed = false;
        
        if ( collection == null )
            throw new IllegalArgumentException( "Argument collection is null" );
        if ( _destroyed )
            return false;
        iterator = iterator();
        while ( iterator.hasNext() ) {
            object = iterator.next();
            if ( ! collection.contains( object ) )
                if ( discard( (ManagedConnection) object, true ) )
                    changed = true;
        }
        return changed;
    }


    public int size()
    {
        // The set size is the number of available connections.
        return _available;
    }


    public synchronized Object[] toArray()
    {
        Object[]    array;
        int         index;
        PoolEntry   entry;
        PoolEntry[] pool;
        
        array = new Object[ _available ];
        index = 0;
        pool = _pool;
        for ( int i = 0 ; i < pool.length ; ++i ) {
            entry = pool[ i ];
            while ( entry != null ) {
                if ( entry._available )
                    array[ index++ ] = entry._managed;
                entry = entry._nextEntry;
            }
        }
        return array;
    }
    

    public synchronized Object[] toArray( Object[] array )
    {
        int         index;
        PoolEntry   entry;
        PoolEntry[] pool;
        
        if ( array == null )
            throw new IllegalArgumentException( "Argument array is null" );
        if ( array.length < _available )
            array = (Object[]) Array.newInstance( array.getClass().getComponentType(), _available );
        index = 0;
        pool = _pool;
        for ( int i = 0 ; i < pool.length ; ++i ) {
            entry = pool[ i ];
            while ( entry != null ) {
                if ( entry._available )
                    array[ index++ ] = entry._managed;
                entry = entry._nextEntry;
            }
        }
            return array;
    }


    /**
     * Iterator over the pool returns from the connection pool set.
     */
    private class PoolIterator
        implements Iterator
    {


        /**
         * Holds a reference to the next entry to be returned by
         * {@link next}. Becomes null when there are no more
         * entries in the pool.
         */
        private PoolEntry _entry;


        /**
         * Index to the current position in the pool. This is the
         * index where we retrieved {@link #_chain} from.
         */
        private int       _index = _pool.length;


        public boolean hasNext()
        {
            PoolEntry   entry;
            int         index;
            PoolEntry[] pool;
            
            synchronized ( ConnectionPool.this ) {
                entry = _entry;
                while ( entry != null ) {
                    if ( entry._available )
                        return true;
                    entry = entry._nextEntry;
                }
                pool = _pool;
                index = _index;
                while ( entry == null && index > 0 ) {
                    entry = pool[ --index ];
                    while ( entry != null ) {
                        if ( entry._available ) {
                            _entry = entry;
                            _index = index;
                            return true;
                        }
                        entry = entry._nextEntry;
                    }
                }
                _entry = null;
                _index = -1;
                return false;
            }
        }

        
        public Object next()
        {
            PoolEntry entry;

            synchronized ( ConnectionPool.this ) {
                if (!hasNext()) {
                    throw new NoSuchElementException( "No more elements in collection" );        
                }

                entry = _entry;
                _entry = entry._nextEntry;
                return entry._managed;
            }
            
        }
        
        
        public void remove()
        {
            throw new UnsupportedOperationException( "This set is immutable" );
        }


    }
    
    
}
