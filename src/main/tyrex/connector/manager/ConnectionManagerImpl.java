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
 * $Id: ConnectionManagerImpl.java,v 1.3 2000/08/28 19:01:48 mohammed Exp $
 */


package tyrex.connector.manager;


import java.io.PrintWriter;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.NoSuchElementException;
import javax.security.auth.Subject;
import tyrex.connector.ConnectionEvent;
import tyrex.connector.ConnectionEventListener;
import tyrex.connector.ConnectionEventListenerAdapter;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ManagedConnectionFactory;
import tyrex.connector.ConnectionManager;
import tyrex.connector.ConnectionException;
import tyrex.connector.SynchronizationResource;
import tyrex.connector.conf.ConnectionManagerConfiguration;
import tyrex.connector.transaction.ConnectionTransactionManager;
import tyrex.resource.ResourceLimits;
import tyrex.resource.ResourcePool;
import tyrex.resource.ResourcePoolManager;
import tyrex.resource.ResourceTimeoutException;
import tyrex.tm.EnlistedResource;
import tyrex.util.ArrayEnumeration;
import tyrex.util.HashIntTable;
import tyrex.util.Messages;


/**
 * This implementation of ConnectionManager can only be used with
 * managed connections that have XA resources. In fact it is assumed
 * that is the case.
 *
 * <b>Notes:</b>
 * <p>
 * <i>Retry policy</i> Pooled connection may expire after a certain duration,
 *  due to network connections going down, etc. When a connection is obtained
 *  from the pool, if the connection cannot be established properly, the
 *  connection manager will discard it and retry to obtain a different
 *  connection until it succeeds or fails with obtaining a non-pooled connection.
 *  The later case indicates permanent failure.
 *  <p>
 *  Managed connections can be shared if 
 *  <ul>
 *  <li>
 *  1) If the connection handles CAN be enlisted with the transaction
 *      manager then the managed connections can be shared if the thread
 *      that is asking for the connection is the same as the one that 
 *      enlisted the current connection handle.
 *  <li>
 *  2) If the connection handle CANNOT be enlisted with the transaction
 *      manager then the managed connections can be shared if the transaction
 *      associated with the calling thread is the same as the transaction
 *      of the thread that created the current connection handle.
 *  </ul>
 *  <p>
 *  Assumptions:
 *  <ul>
 *  <li>
 *  1. The same managed connection always returns the same xa resource.
 *  <li>
 *  2. The same managed connection always returns a different connection
 *      handle.
 *  </ul>
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/08/28 19:01:48 $
 */
class ConnectionManagerImpl
    implements ConnectionManager
{


    /**
     * Holds a list of all the pooled resources that are available
     * for reuse by the application. Each resource is held inside
     * an {@link PoolEntry} that records the resource's
     * account. This cannot be implemented as a stack if we want
     * to pool resources for different accounts.
     */
    private final Map pools = new HashMap();


    /**
     * The print writer that writes a a log. Can be null.
     */
    private PrintWriter logWriter;


    /**
     * The background thread that prunes inactive managed connections
     * from the various resource pools associated with the
     * ConnectionManagerImpl
     */
    private ManagedConnectionReleaseThread managedConnectionReleaseThread = null;


    /**
     * Object used as a lock for accessing resources in the
     * managed connection release thread. A separate object
     * is used for a lock so that synchronization on the
     * thread is decoupled from synchronization on the
     * ConnectionManagerImpl ({@link #getPoolEntry}).
     */
    private final Object managedConnectionReleaseThreadLock = new Object();


    /**
     * The conenction transaction manager associated with the connection manager.
     * Can be null.
     */
    private /*final*/ ConnectionTransactionManager connectionTransactionManager; // javac error


    /**
     * The object resposible for configuring the ConnectionManagerImpl
     */
    private /*final*/ ConnectionManagerConfiguration configuration; // javac error


    /**
     * Create the ConnectionManagerImpl with  a
     * default connection manager configuration 
     * ({@link ConnectionManagerConfiguration}).
     * 
     */
    public ConnectionManagerImpl()
    {
        this(null, null, null);
    }

    /**
     * Create the ConnectionManagerImpl with the specified 
     * connection manager configuration.
     * If the specified connection manager configuration is null then a
     * default connection manager configuration 
     * ({@link ConnectionManagerConfiguration}) is used.
     * 
     * @param configuration the connection manager configuration. 
     *      Can be null.
     */
    public ConnectionManagerImpl(ConnectionManagerConfiguration configuration)
    {
        this(configuration, null, null);
    }


    /**
     * Create the ConnectionManagerImpl with the specified 
     * log writer.
     * A default connection manager configuration 
     * ({@link ConnectionManagerConfiguration}) is used.
     * 
     * @param logWriter the log writer. Can be null.
     */
    public ConnectionManagerImpl(PrintWriter logWriter)
    {
        this(null, logWriter);
    }


    /**
     * Create the ConnectionManagerImpl with the specified 
     * connection transaction manager.
     * A default connection manager configuration 
     * ({@link ConnectionManagerConfiguration}) is used.
     * 
     * @param connectionTransactionManager the connection transaction manager. 
     *      Can be null.
     */
    public ConnectionManagerImpl(ConnectionTransactionManager connectionTransactionManager)
    {
        this(null, connectionTransactionManager);
    }
    

    /**
     * Create the ConnectionManagerImpl with the specified 
     * connection manager configuration and log writer.
     * If the specified connection manager configuration is null then a
     * default connection manager configuration 
     * ({@link ConnectionManagerConfiguration}) is used.
     * 
     * @param configuration the connection manager configuration. 
     *      Can be null.
     * @param logWriter the log writer. Can be null.
     */
    public ConnectionManagerImpl(ConnectionManagerConfiguration configuration, 
                                 PrintWriter logWriter)
    {
        this(configuration, null, logWriter);
    }


    /**
     * Create the ConnectionManagerImpl with the specified 
     * connection manager configuration and transaction manager.
     * If the specified connection manager configuration is null then a
     * default connection manager configuration 
     * ({@link ConnectionManagerConfiguration}) is used.
     * 
     * @param configuration the connection manager configuration. 
     *      Can be null.
     * @param connectionTransactionManager the connection transaction manager. 
     *      Can be null.
     */
    public ConnectionManagerImpl(ConnectionManagerConfiguration configuration, 
                                 ConnectionTransactionManager connectionTransactionManager)
    {
        this(configuration, connectionTransactionManager, null);
    }


    /**
     * Create the ConnectionManagerImpl with the specified 
     * connection manager configuration, transaction manager and log writer.
     * If the specified connection manager configuration is null then a
     * default connection manager configuration 
     * ({@link ConnectionManagerConfiguration}) is used.
     * 
     * @param configuration the connection manager configuration. 
     *      Can be null.
     * @param connectionTransactionManager the connection transaction manager. 
     *      Can be null.
     * @param logWriter the log writer. Can be null.
     */
    public ConnectionManagerImpl(ConnectionManagerConfiguration configuration, 
                                 ConnectionTransactionManager connectionTransactionManager,
                                 PrintWriter logWriter)
    {
        this.configuration = (null == configuration) 
                                ? new ConnectionManagerConfiguration() 
                                : configuration;
        this.logWriter = logWriter;
        this.connectionTransactionManager = connectionTransactionManager;
    }


    //RM
    private static String getCurrentThreadPrettyName()
    {
        return "Thread[" + Thread.currentThread().getName() + "]";
    }


    /**
     * Return the subject associated with the current access
     * control context.
     *
     * @return the subject associated with the current access
     * control context.
     */
    private static Subject getSubject()
    {
        return Subject.getSubject(AccessController.getContext());
    }


    /**
     * Return the enumeration of active managed connections that can be
     * shared from the specified resource pool. If there are not any
     * that can be shared return null.
     * For now return those managed conections that are in the
     * same transaction as the specified transaction
     *
     * @param managedConnections the enumeration of
     *      managed connections.
     * @param transaction the current transaction. Can be null.
     * @return the enumeration of active managed connections that can be
     *      shared from the specified resource pool. If there are not any
     *      that can be shared return null.
     */
    private Enumeration getShareableConnections(Enumeration managedConnections,
                                                int numberOfManagedConnectionEntries)
    {
        /*
        // make an array to hold the managed connections
        Object[] connections = new Object[numberOfManagedConnectionEntries];
        // the current managed connection entry
        ManagedConnectionEntry entry = null;
        // the index in the conections array to add the next connection
        int index = 0;

        for (; managedConnectionEntries.hasMoreElements();) {
            // get the next managed connection entry
            entry = (ManagedConnectionEntry)managedConnectionEntries.nextElement();
            // if the transactions match
            if (transaction == entry.transaction) {
                connections[index++] = entry.managedConnection;    
            }
        }

        return 0 == index ? null : new ArrayEnumeration(connections, 0, index);*/
        return null;
    }


    public Object getConnection(ManagedConnectionFactory managedConnectionFactory, Object info)
        throws ConnectionException
    {
        if (null == managedConnectionFactory) {
            throw new ConnectionException("The argument 'managedConnectionFactory' is null.");    
        }
        // get the current subject
        Subject subject = getSubject();
        // the managed connection
        ManagedConnection managedConnection = null;
        // get the pool entry
        PoolEntry poolEntry = null;
        
        synchronized (pools) {
            poolEntry = getPoolEntry(managedConnectionFactory);

            if (null == poolEntry) {
                poolEntry = createPoolEntry(managedConnectionFactory);    
            }
        }
        // get the resource pool
        ResourcePoolImpl resourcePool = poolEntry.resourcePool;
        // synchronize on the resource pool because that is what the resource pool manager
        // will synchronize on. This will prevent deadlocks on the resource pool.
        synchronized (resourcePool) {
            if (configuration.canShareConnections(managedConnectionFactory) && 
                (resourcePool.getActiveCount() > 0)) {
                // get the managed connections in the same transaction
                Enumeration shareableActiveConnections = getShareableConnections(resourcePool.getActiveConnections(),
                                                                                 resourcePool.getActiveCount());
                // if there are shareable connections...
                if (null != shareableActiveConnections) {
                    managedConnection = managedConnectionFactory.getManagedConnection(subject, 
                                                                                      shareableActiveConnections, 
                                                                                      info);    
                }
            }
            
            if (null == managedConnection) {
                try {
                    //System.out.println("RM " + getCurrentThreadPrettyName() + " waiting for activation ");
                    // if we can activate a pooled connection 
                    poolEntry.resourcePoolManager.canActivate();
                    
                    // look for a connection among the pooled connections
                    if (0 != resourcePool.getPooledCount()) {
                        managedConnection = managedConnectionFactory.getManagedConnection(subject,
                                                                                          resourcePool.getPooledConnections(),
                                                                                          info);
                    }
                    
                    if (null == managedConnection) {
                        //System.out.println("RM " + getCurrentThreadPrettyName() + " wating for creating managedConnection ");
                        // now try to create a managed connection
                        poolEntry.resourcePoolManager.canCreateNew();
                        // create the managed connection
                        managedConnection = managedConnectionFactory.createManagedConnection(subject, info);

                        if (null == managedConnection) {
                            throw new ConnectionException("Unable to create a managed connection from the factory <" +
                                                          managedConnectionFactory + ">.");
                        }
                        //System.out.println("RM " + getCurrentThreadPrettyName() + " created managedConnection " + managedConnection);
                    }
                    else {
                        //System.out.println("RM " + getCurrentThreadPrettyName() + " using pooled managedConnection " + managedConnection);
                        // remove the connection from the pool so that it can be used
                        resourcePool.unpool(managedConnection);
                    }

                    // monitor the managed connection
                    managedConnection.addConnectionEventListener(poolEntry.listener);
                }
                catch(ResourceTimeoutException e) {
                    throw new ConnectionException(Messages.format("tyrex.resource.limitExceeded", 
                                                                  managedConnectionFactory.toString()));
                }
            }

            try {
                resourcePool.use(managedConnection);

                return connectionTransactionManager.enlist(managedConnection.getConnection(info),
                                                           managedConnection,
                                                           managedConnectionFactory);

                //System.out.println("using managed connection " + managedConnection);
            }
            catch(Exception e) {
                // discard the managed connection
                discard(managedConnection, managedConnectionFactory);
                
                if (e instanceof ConnectionException) {
                    throw (ConnectionException)e;    
                }
                else {
                    throw new ConnectionException(e);
                }
            }
        }
    }
    

    /**
     * Return the PoolEntry object associated with the specified
     * managed connection factory.
     *
     * @param managedConnectionFactory the managed connection factory
     * @return the PoolEntry object associated with the specified
     * managed connection factory. IF one does not exist return null.
     */
    private PoolEntry getPoolEntry(ManagedConnectionFactory managedConnectionFactory)
    {
        // The double-check idiom is not used because it may not work because
        // out-of-order execution of instructions ie the setting of the reference
        // can occur before the referenced object has been fully constructed. This
        // happens especially in a multi-processor environment
    
        synchronized (pools) {
            // get the pool
            return (PoolEntry)pools.get(managedConnectionFactory);
        }
    }

    /**
     * Create the PoolEntry object associated with the specified
     * managed connection factory.
     *
     * @param managedConnectionFactory the managed connection factory
     */
    private PoolEntry createPoolEntry(ManagedConnectionFactory managedConnectionFactory)
    {
        PoolEntry poolEntry = null;
        // true if connection manager impl is to manage the 
        // pruning of resources for this resource pool
        boolean doPrune = false;

        synchronized (pools) {
            //System.out.println("RM " + getCurrentThreadPrettyName() + " creating pool entry for " + managedConnectionFactory);
            // make a resource pool
            ResourcePoolImpl resourcePool = new ResourcePoolImpl(logWriter);
            // make the resource pool manager
            ResourcePoolManager resourcePoolManager = configuration.createResourcePoolManager(managedConnectionFactory);
            // true if connection manager impl is to manage the 
            // pruning of resources for this resource pool
            doPrune = configuration.getConnectionManagerPruneResponsibilty(managedConnectionFactory);
            // set the fields for the pool
            //resourcePool.setLogWriter(logWriter);
            //resourcePool.setDescription("factory = <" + managedConnectionFactory.toString() + ">");
            // make sure the manager is free
            resourcePoolManager.unmanage();
            // set the resource limits
            resourcePoolManager.setResourceLimits(configuration.createResourceLimits());
            // tell the manager to manage this pool
            resourcePoolManager.manage(resourcePool, !doPrune);
            // make the pool entry
            poolEntry = new PoolEntry(managedConnectionFactory,
                                      resourcePool,
                                      resourcePoolManager,
                                      new ManagedConnectionListener(managedConnectionFactory));
            
            // add it
            pools.put(managedConnectionFactory, poolEntry);
        }

        // is the connection manager responsible for pruning
        if (doPrune) {
            synchronized (managedConnectionReleaseThreadLock) {
                if (null == managedConnectionReleaseThread) {
                    managedConnectionReleaseThread = new ManagedConnectionReleaseThread();    
                }
                managedConnectionReleaseThread.include(poolEntry);
            }
        }

        // return the entry
        return poolEntry;
    }


    /**
     * Discard the specified managed connection
     *
     * @param managedConnection the managed connection
     */
    private void discard(ManagedConnection managedConnection,
                         ManagedConnectionFactory managedConnectionFactory)
    {
        try {
            connectionTransactionManager.discard(managedConnection, managedConnectionFactory);
        }
        catch(Exception e) {
            if (null != logWriter) {
                logWriter.print("Failed to discard managed connection <");
                logWriter.print(managedConnection);
                logWriter.print(">: ");
                logWriter.println(e.toString());
                e.printStackTrace(logWriter);
            }
        }
        finally {
            try {
                // close the connection
                managedConnection.close();
            }
            catch (Exception e) {
                if (null != logWriter) {
                    logWriter.println( "Failed to close managed connection <" + managedConnection + ">: ");
                    e.printStackTrace(logWriter);
                }
            }
        }
    }


    /**
     * The specified managed connection has been closed externally.
     * If the managed connection is not shared put the connection
     * back into the pool so that it can be used.
     * <BR>
     * If the managed connection is enlisted in a transaction it is
     * automatically delisted.
     *
     * @param managedConnection the managed connection that is no 
     *      longer used externally.
     */
    private void unuse(ManagedConnection managedConnection,
                       ManagedConnectionFactory managedConnectionFactory)
    {
        PoolEntry entry = getPoolEntry(managedConnectionFactory);

        if (null != entry) {
            try {
                if (entry.resourcePool.unuse(managedConnection)) {
                    connectionTransactionManager.delist(managedConnection, 
                                                        managedConnectionFactory);
                    // the managed connection has been returned to the pool
                    // remove the listener
                    managedConnection.removeConnectionEventListener(entry.listener);
                    // tell the resource manager
                    entry.resourcePoolManager.released();
                }
            }
            catch(Exception e) {
                try {
                    if (null != logWriter) {
                        logWriter.println( "Internal error: Could not return managed connection <" + 
                                            managedConnection.toString() + 
                                            "> to the resource pool.");
                        e.printStackTrace(logWriter);
                    }
                }
                finally {
                    discard(managedConnection, managedConnectionFactory);
                }
            }
        }
        else if (null != logWriter) {
            logWriter.println( "Internal error: Cannot find the managed entry for the specified managed connection <" +
                               managedConnection +
                               "> while trying to return the managed connection to the pool " +
                               "(ManagedConnectionPoolImpl#unuse).");
        }
    }
    

    /**
     * This class listens for changes in managed connections
     */
    private class ManagedConnectionListener 
        extends ConnectionEventListenerAdapter
        implements ConnectionEventListener
    {
        /**
         * The managed connection factory
         */
        private final ManagedConnectionFactory managedConnectionFactory;

        /**
         * Create the ManagedConnectionListener for all managed
         * connections created from the specified managed connection
         * factory.
         *
         * @param managedConnectionFactory the managedConnectionFactory
         */
        private ManagedConnectionListener(ManagedConnectionFactory managedConnectionFactory)
        {
            this.managedConnectionFactory = managedConnectionFactory;
        }

        /**
         * Called by a managed connection to inform the connection manager
         * that the application closed the connection. After this call the
         * connection manager may recycle the connection and hand it to a
         * different caller.
         *
         * @param connection The event
         */
        public void connectionClosed(ConnectionEvent event)
        {
            //System.out.println("RM " + getCurrentThreadPrettyName() + " received connection closed for " + managedConnection);
            // unuse the managed connection 
            unuse(event.getManagedConnection(), managedConnectionFactory);
        }
        

        /**
         * Called by a managed connection to inform the connection manager
         * that a critical error occured with the connection. After this
         * call the connection manager will not attempt to use the
         * connection and will properly discard it.
         *
         * @param event The connection event
         */
        public void connectionErrorOccurred(ConnectionEvent event)
        {
            // discard the managed connection
            discard(event.getManagedConnection(), managedConnectionFactory);
        }
    }
    
    
    /**
     * The resource pool for managed connections from the
     * same managed connection factory
     */
    private static class ResourcePoolImpl
        implements ResourcePool
    {
        /**
         * The pool of available managed connections.
         */                
        private final Vector pool = new Vector();
    

        /**
         * Holds a association of all the active managed connections
         * to the number of times the managed connection is being used.
         */
        private final HashIntTable active = new HashIntTable();


        /**
         * The log writer. Can be null
         */
        private final PrintWriter logWriter;


        /**
         * Create the ResourcePoolImpl with the specified arguments.
         *
         * @param logWriter the log writer
         */
        private ResourcePoolImpl(PrintWriter logWriter)
        {
            this.logWriter = logWriter;
        }
        
        /**
         * Called by the resource pool manager to obtain the number of pooled
         * resources. Pooled resources are not being used currently,
         * and may be available to a requesting caller.
         *
         * @return The number of pooled resources
         */ 
        public int getPooledCount()
        {
            return pool.size();
        }
    
    
        /**
         * Called by the pool manager to obtain the number of active
         * resources. Active resources are those that are currently in
         * used, and subject to the active upper limit.
         *
         * @return The number of active resources
         */
        public int getActiveCount()
        {
            return active.size();
        }
    

        /**
         * Called by the resource pool manager when it determines that a certain
         * number of resources should be released from the pool.
         * Active resources are never asked to be removed. The pool
         * may remove any number of resources it sees fit.
         * <BR>
         * The managed connections are automatically closed when they
         * are released from the pool.
         *
         * @param count The number of pooled resources that should be
         *   released
         */
        public synchronized void releasePooled(int count)
        {
            for (int i = 0; (i < count) && (i < pool.size()); ++i) {
                ManagedConnection managedConnection = (ManagedConnection)pool.get(0);
                pool.remove(0);
                try {
                    managedConnection.close();
                } 
                catch (ConnectionException e) {
                    if (null != logWriter)
                        logWriter.println( Messages.format("tyrex.resource.fault", toString(), managedConnection, e));
                }
            }
        }

        /**
         * Return the enumeration of pooled (inactive) connections.
         *
         * @return the enumeration of pooled (inactive) connections.
         */
        private Enumeration getActiveConnections()
        {
            return active.keys();
        }

        /**
         * Return the enumeration of pooled (inactive) connections.
         *
         * @return the enumeration of pooled (inactive) connections.
         */
        private Enumeration getPooledConnections()
        {
            return pool.elements();
        }

        /**
         * Remove the specified managed connection from the pool
         * of inactive managed connections and add it to the
         * active list.
         *
         * @param managedConnection the managed connection
         */
        private synchronized void unpool(ManagedConnection managedConnection)
        {
            pool.remove(managedConnection);
        }

        /**
         * Remove the specified managed connection from the
         * active pool and move it to the inactive pool
         *
         * @param managedConnection managed connection
         */
        private synchronized void pool(ManagedConnection managedConnection)
        {
            // add to the pool
            pool.add(managedConnection);
        }

        /**
         * Discard the specified managed connection from the resource pool.
         */
        private synchronized void discard(ManagedConnection managedConnection)
        {
            // remove the managed connection from the active connections
            active.remove(managedConnection);    
            // it should not be in the pooled connections but just in case
            pool.remove(managedConnection);
        }

        /**
         * The specified managed connection is in use by the application.
         * The same managed connection can be in use multiple times.
         * Assumes that the managed connection is not pooled.
         *
         * @param managedConnection the managed connection 
         */
        private synchronized void use(ManagedConnection managedConnection)
        {
            active.increment(managedConnection, 1);
        }

        /**
         * The specified managed connection is not use by the application.
         *
         * @param managedConnection the managed connection
         * @return true if the specified managed connection is not in use
         * by any applications.
         */
        private synchronized boolean unuse(ManagedConnection managedConnection)
        {
            return 0 == active.increment(managedConnection, -1);
        }
    }


    /**
     * This class defines the object to be stored as the value in the variable 
     * {@link ConnectionManagerImpl#pools}.
     * It contains the managed connection factory, the resource pool 
     * containing the managed conections from factory and the resource
     * pool manager responsible for managing the resource pool.
     */
    private static class PoolEntry
    {
        /**
         * The managed connection factory
         */
        private final ManagedConnectionFactory managedConnectionFactory;
        

        /**
         * The resource pool
         */
        private final ResourcePoolImpl resourcePool;
        
        /**
         * The object responsible for managing the resource pool
         */
        private final ResourcePoolManager resourcePoolManager;


        /**
         * The listener that monitors the managed connections
         * created from the managed connection factory. 
         */
        private final ManagedConnectionListener listener;
        

        /**
         * Create the PoolEntry with the specified managed connection
         * factory, resource pool, resource pool manager and
         * connection listener.
         *
         * @param managedConnectionFactory the managed conection factory
         * @param resourcePool the pool containing the managed connections
         * @param resourcePoolManager the object responsible for managing 
         *      the pool.
         */
        private PoolEntry(ManagedConnectionFactory managedConnectionFactory,
                          ResourcePoolImpl resourcePool,
                          ResourcePoolManager resourcePoolManager,
                          ManagedConnectionListener listener)
        {
            // set the fields
            this.managedConnectionFactory = managedConnectionFactory;
            this.resourcePool = resourcePool;
            this.resourcePoolManager = resourcePoolManager;
            this.listener = listener;
        }
    }


    /**
     * Thread for pruning the managed connections from the various
     * resource pools in the ConnectionManagerImpl
     */
    private class ManagedConnectionReleaseThread
        extends Thread
    {
        /**
         * The array of pool entries to be pruned
         */
        private PoolEntry[] poolEntries = null;

        /**
         * The array of longs that represent the last time
         * the corresponding pool entry was pruned. There
         * is a one-to-one correspondence between this array
         * and the poolEntries array.
         */
        private long[] lastTimes = null;
        
        
        /**
         * The amount of time in milliseconds for the 
         * {@link #managedConnectionReleaseThread
         * background thread} to wait for releasing inactive managed
         * connections from various resource pools. This number
         * is the minimum of the prune times for various resource managers
         * associated with this ConnectionManagerImpl.
         */
        private long checkEvery = Long.MAX_VALUE;
        

        /**
         * Create the ManagedConnectionReleaseThread
         * and start it automatically.
         */
        private ManagedConnectionReleaseThread()
        {
            super("ConnectionManagerImpl Daemon");
            setDaemon(true);
            setPriority(Thread.MIN_PRIORITY);
            start();
        }


        /**
         * Include the resource pool from specified pool entry to 
         * pruned by this thread.
         * <BR>
         * Assumed to be synchronized externally via 
         * {@link ConnectionManagerImpl#managedConnectionReleaseThreadLock}
         *
         * @param poolEntry the pool entry whose resource pool is to be pruned
         */
        private void include(PoolEntry poolEntry)
        {
            // regrow the arrays
            PoolEntry[] tempPoolEntries = new PoolEntry[null == poolEntries ? 1 : poolEntries.length + 1];
            long[] tempLastTimes = new long[tempPoolEntries.length];

            // copy the arrays
            for (int i = tempPoolEntries.length - 1; --i >= 0;) {
                tempPoolEntries[i] = poolEntries[i];
                tempLastTimes[i] = lastTimes[i];
            }
            // add the new items
            tempPoolEntries[tempPoolEntries.length - 1] = poolEntry;
            tempLastTimes[tempLastTimes.length - 1] = System.currentTimeMillis();
            // set the new arrays
            poolEntries = tempPoolEntries;
            lastTimes = tempLastTimes;
            // reset the temps
            tempPoolEntries = null;
            tempLastTimes = null;
            // set the check every time for the thread to be the minimum
            // of the current check every time and the check every time
            // of the resource manager of the pool entry
            this.checkEvery = Math.min(poolEntry.resourcePoolManager.getResourceLimits().getCheckEvery(), this.checkEvery);
        }


        /**
         * Loop over the resource pools associated with the ConnectionManagerImpl
         * and release any extra inactive pooled managed connections
        * <BR>
        * Synchronizes via 
        * {@link ConnectionManagerImpl#managedConnectionReleaseThreadLock}
        */
        public void run()
        {
            synchronized (ConnectionManagerImpl.this.managedConnectionReleaseThreadLock) {
                // the current pool entry
                PoolEntry poolEntry = null;
                // the resource limits for the resource pool manager
                ResourceLimits resourceLimits = null;
                // the current time
                long currentTime = 0;
                // the number of excess connections
                int numberOfExtraManagedConnections = 0;

                try {
                    while (true) {
    
                        ConnectionManagerImpl.this.managedConnectionReleaseThreadLock.wait(checkEvery);
    
                        // safety check
                        if (null == poolEntries) {
                            continue;    
                        }
                        
                        for (int i = 0; i < poolEntries.length; i++) {
                            try {
                                // get the current pool entry
                                poolEntry = poolEntries[i];
                                // get the resource limits for the resource pool manager
                                resourceLimits = poolEntry.resourcePoolManager.getResourceLimits();
                                // get the current time
                                currentTime = System.currentTimeMillis();
                                // if it's time to prune the pool do it
                                if ((currentTime - lastTimes[i]) > resourceLimits.getCheckEvery()) {
                                    // get the number of excess connections
                                    numberOfExtraManagedConnections =   poolEntry.resourcePool.getPooledCount() -
                                                                        resourceLimits.getDesiredSize();
                                    if (numberOfExtraManagedConnections > 0) {
                                        //RM System.out.println("RMThread[" + Thread.currentThread() + "]" + " for " + poolEntry + " releasing " + ((int)(numberOfExtraManagedConnections * resourceLimits.getPruneFactor()) + 1));
                                        poolEntry.resourcePool.releasePooled((int)(numberOfExtraManagedConnections * resourceLimits.getPruneFactor()) + 1);
                                    }
                                    lastTimes[i] = System.currentTimeMillis();
                                }
                            }
                            catch(Exception e) {
                                if (null != logWriter) {
                                    logWriter.println("Error occurred in pruning resources:");
                                    e.printStackTrace(logWriter);
                                }
                            }
                        }
                    }
                }
                catch (InterruptedException e) {
                    if (null != logWriter) {
                        logWriter.println("Pruning thread for <" + ConnectionManagerImpl.this.toString() + "> interrupted.");    
                    }
                }
            }
        }
    }
}




