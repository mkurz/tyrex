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
 */


package tyrex.connector.transaction;


import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import tyrex.connector.ConnectionException;
import tyrex.connector.ConnectionEvent;
import tyrex.connector.ConnectionEventListener;
import tyrex.connector.ConnectionEventListenerAdapter;
import tyrex.connector.LocalTransaction;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ManagedConnectionFactory;
import tyrex.tm.EnlistedResource;
import tyrex.tm.TyrexTransactionManager;

///////////////////////////////////////////////////////////////////////////////
// ConnectionTransactionManagerImpl
///////////////////////////////////////////////////////////////////////////////

/**
 * Implementation of ConnectionTransactionManager
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
public class ConnectionTransactionManagerImpl 
    extends AbstractConnectionTransactionManager
    implements ConnectionTransactionManager, EnlistedResourceListener
{
    /**
     * The transaction mediator
     */
    private /*final*/ TransactionMediator transactionMediator; // javac error


    /**
     * The transaction manager
     */
    private /*final*/ TransactionManager transactionManager; // javac error


    /**
     * The table of managed connections to {@link ManagedConnectionEntry entries}
     */
    private final Map entries = new HashMap();


    /**
     * The local transaction listener
     */
     private final LocalTransactionListener localTransactionListener = new LocalTransactionListener();


     /**
      * True if the TransactionManager can enlist connections as enlisted resources
      */
     private /*final*/ boolean canEnlistResources; // javac

    /**
     * Create the ConnectionTransactionManagerImpl with the
     * specified {@link TransactionMediator transaction mediator}
     * and transaction manager
     *
     * @param transactionMediator the transaction mediator
     * @param transactionManager the transaction manager.
     */
    public ConnectionTransactionManagerImpl(TransactionMediator transactionMediator,
                                            TransactionManager transactionManager)
    {
        this(transactionMediator, transactionManager, null);
    }


    /**
     * Create the ConnectionTransactionManagerImpl with the
     * specified arguments
     *
     * @param transactionMediator the transaction mediator
     * @param transactionManager the transaction manager.
     */
    public ConnectionTransactionManagerImpl(TransactionMediator transactionMediator,
                                            TransactionManager transactionManager, 
                                            PrintWriter logWriter)
    {
        if (null == transactionMediator) {
            throw new IllegalArgumentException("The argument 'transactionMediator' is null.");
        }

        if (null == transactionManager) {
            throw new IllegalArgumentException("The argument 'transactionManager' is null.");
        }

        this.transactionMediator = transactionMediator;
        this.transactionManager = transactionManager;
        this.canEnlistResources = transactionManager instanceof TyrexTransactionManager;
        setLogWriter(logWriter);
    }

    /**
     * Enlist the specified managed connection from
     * the specified managed connection factory
     * in the transaction framework of the application
     * server.
     *
     * <p>
     * The ConnectionTransactionManager may return a
     * connection handle that will allow the managed connection
     * to be enlisted automatically in transactions whenever the
     * handle is used.
     *
     * @param connectionHandle the connection handle from the
     *      managed connection
     * @param managedConnection the managed connection
     * @param managedConnectionFactory the managed connection factory
     *      that produced the specified managed connection
     * @return the connection handle from the managed connection
     * @throws ConnectionException if there is a problem
     */
    public final Object enlist(Object connectionHandle,
                         ManagedConnection managedConnection,
                         ManagedConnectionFactory managedConnectionFactory)
        throws ConnectionException
    {
        // get the transaction type
        TransactionType transactionType = getTransactionMediator().getTransactionType(managedConnectionFactory);
        if (TransactionType.localTransactionType != transactionType) {
            return enlistXATransaction(connectionHandle, 
                                       managedConnection,
                                       managedConnectionFactory,
                                       transactionType);
        }
        else {
            return enlistLocalTransaction(connectionHandle,
                                          managedConnection,
                                          managedConnectionFactory,
                                          transactionType);
            
        }
    }


    /**
     * 
     */
    protected Object enlistLocalTransaction(Object connectionHandle,
                                         ManagedConnection managedConnection,
                                         ManagedConnectionFactory managedConnectionFactory,
                                         TransactionType transactionType)
        throws ConnectionException
    {
        synchronized (managedConnection) {
            // get the entry
            ManagedConnectionEntry entry = getManagedConnectionEntry(managedConnection);
                        
            // if no entry create one
            if (null == entry) {
                // get the local transaction
                LocalTransaction localTransaction = managedConnection.getLocalTransaction();
                // add the local transaction listeners
                managedConnection.addConnectionEventListener(getLocalTransactionListener());

                entry = new ManagedConnectionEntry(localTransaction,
                                                   transactionType);

                addManagedConnectionEntry(managedConnection, entry);
            }
            
            return connectionHandle;
        }
    }


    /**
     * 
     */
    protected Object enlistXATransaction(Object connectionHandle,
                                         ManagedConnection managedConnection,
                                         ManagedConnectionFactory managedConnectionFactory,
                                         TransactionType transactionType)
        throws ConnectionException
    {
        synchronized (managedConnection) {
            // the handle to be returned
            Object handle = connectionHandle;
            // the xa resource
            XAResource xaResource = managedConnection.getXAResource();
            // get the current transaction
            Transaction transaction = getCurrentTransaction();
            // true if the resource got enlisted
            boolean isEnlisted = false;

            try {
                if (canEnlistResources()) {
                    EnlistedResource enlistedConnection = EnlistedResourceFactory.build(connectionHandle,
                                                                                        managedConnection,
                                                                                        this);
                    if (null != enlistedConnection) {
                        // add the enlisted resource to the transaction manager
                        ((TyrexTransactionManager)getTransactionManager()).enlistResource(xaResource, 
                                                                                          enlistedConnection);
                        handle = enlistedConnection;
                        isEnlisted = true;
                    }
                }
                if (!isEnlisted && (null != transaction)) {
                    transaction.enlistResource(xaResource);
                }
            }
            catch (Exception e) {
                throw new ConnectionException("Failed to enlist connection <" + 
                                              connectionHandle + 
                                              "> from the managed connection < " +
                                              managedConnection + 
                                              "> with transaction manager <" +
                                              transactionManager + ">.", e);
            }
    
            // get the entry
            ManagedConnectionEntry entry = getManagedConnectionEntry(managedConnection);
                        
            // if no entry create one
            if (null == entry) {
                entry = new ManagedConnectionEntry(xaResource,
                                                   transaction,
                                                   transactionType,
                                                   handle != connectionHandle);
                addManagedConnectionEntry(managedConnection, entry);
            }
            // double-chekc validation
            else if (entry.hasEnlistedResources) {
                throw new ConnectionException("Internal Error: Cannot share managed connections that are enlisted automatically in transactions.");    
            }
            else if (entry.transaction != transaction) {
                throw new ConnectionException("Internal Error: Cannot share managed connections that are not in the same transaction.");    
            }
            else if (entry.transactionType != transactionType) {
                throw new ConnectionException("Internal Error: Cannot share managed connections that are don't have the same transaction type.");    
            }
            
            return handle;
        }
    }


    /**
     * Delist the specified managed connection because the
     * connection handle to the managed connection has been closed.
     * <P>
     * In the case of a managed connection tbat is taking part
     * in a local transaction it can't reused until the local
     * transaction has either been commited or rolled back.
     * <P)
     * In the case of a managed connection that is taking part in
     * an XA transaction (IPC or 2PC) the XA resource is ended
     * successfully and the managed connection can be reused.
     *
     * @param managedConnection the managed connection
     * @param managedConnectionFactory the managed connection factory
     *      that produced the specified managed connection
     * @return True if the specified managed connection from the
     * specified managed connection factory can be reused.
     */
    public final boolean delist(ManagedConnection managedConnection,
                          ManagedConnectionFactory managedConnectionFactory)
        throws ConnectionException
    {
        return delist(managedConnection, false);
    }
    

    /**
     * Return true if the specified managed connection from the
     * specified managed connection factory can be shared.
     *
     * @param managedConnection the managed connection
     * @param managedConnectionFactory the managed connection factory
     *      that produced the specified managed connection
     * @return True if the specified managed connection from the
     * specified managed connection factory can be shared.
     * @throw ConnectionException if there is a problem determining
     *      whether a connection can be shared.
     */
    public boolean canBeShared(ManagedConnection managedConnection,
                               ManagedConnectionFactory managedConnectionFactory)
        throws ConnectionException
    {
        synchronized (managedConnection) {
            ManagedConnectionEntry entry = getManagedConnectionEntry(managedConnection);

            if (null != entry) {
                if (entry.transactionType == TransactionType.localTransactionType) {
                    return getTransactionMediator().canShareLocalTransactions(managedConnectionFactory);
                }
                return !entry.hasEnlistedResources && (entry.transaction == getCurrentTransaction());
            }

            return true;
        }
    }


    /**
     * Discard the specified managed connection because an error
     * has occurred in it. The managed connection will not be used
     * again.
     * <P>
     * In the case of a managed connection tbat is taking part
     * in a local transaction the local transaction should be rolled back.
     * <P)
     * In the case of a managed connection that is taking part in
     * an XA transaction (IPC or 2PC) the XA resource is ended
     * unsuccessfully.
     *
     * @param managedConnection the managed connection
     * @param managedConnectionFactory the managed connection factory
     *      that produced the specified managed connection
     * @throws ConnectionException if there is a problem disacrding the
     *      managed connection
     */
    public final void discard(ManagedConnection managedConnection,
                        ManagedConnectionFactory managedConnectionFactory)
        throws ConnectionException
    {
        delist(managedConnection, true);
    }


    /**
     * Return the current transaction associated with the transaction
     * manager. Can return null.
     *
     * @return the current transaction associated with the transaction
     *      manager. Can return null.
     * @throws ConnectionException if there is a problem returning the
     *      current transaction
     */
    protected final Transaction getCurrentTransaction()
        throws ConnectionException
    {
        try {
            // return the current transaction
            return transactionManager.getTransaction();
        }
        catch (SystemException e) {
            throw new ConnectionException("Failed to get the current transaction", e);
        }
    }


    /**
     * Return the managed connection entry for the
     * specified managed connection. If none exists
     * return null.
     *
     * @param managedConnection the managed connection
     */
    protected final ManagedConnectionEntry getManagedConnectionEntry(ManagedConnection managedConnection)
    {
        synchronized(entries)
        {
            return (ManagedConnectionEntry)entries.get(managedConnection);
        }
    }


    /**
     * Remove the managed connection entry for the
     * specified managed connection. If none exists
     * do nothing.
     *
     * @param managedConnection the managed connection
     */
    protected final void removeManagedConnectionEntry(ManagedConnection managedConnection)
    {
        synchronized(entries)
        {
            entries.remove(managedConnection);
        }
    }
    
    /**
     * Add the managed connection entry for the
     * specified managed connection. 
     *
     * @param managedConnection the managed connection
     * @param entry the managed connection entry
     */
    protected final void addManagedConnectionEntry(ManagedConnection managedConnection, 
                                                   ManagedConnectionEntry entry)
    {
        synchronized(entries)
        {
            entries.put(managedConnection, entry);
        }
    }
    
    /**
     * Delist the specified managed resource. 
     *
     * @param managedConnection the managed connection to be delisted
     * @param discard True if the managed connection is to be discarded 
     *      and not used again
     * @throws ConnectionException if there is a problem delisting 
     *      the managed connection.
     * @return True if the specified managed connection has been delisted.
     */
    private boolean delist(ManagedConnection managedConnection,
                           boolean discard)
        throws ConnectionException
    {
        synchronized (managedConnection) {
            ManagedConnectionEntry entry = getManagedConnectionEntry(managedConnection);

            if (null != entry) {
                if (entry.transactionType != TransactionType.localTransactionType) {
                    return delistXATransaction(managedConnection, entry, discard);
                }
                else {
                    return delistLocalTransaction(managedConnection, entry, discard);
                }
            }
        }
        /*
        else if (null != getLogWriter()){
            getLogWriter().println("Internal Error: Cannot find entry for managed connection <" +
                                   managedConnection +
                                   ">.");
        }*/

        return true;
    }

    /**
     * Delist the managed connection that is involved in
     * a local transaction
     * <P>
     * In the case of a managed connection tbat is not to be discarded 
     * it can't be delisted until the local
     * transaction has either been commited or rolled back.
     * <P>
     * In the case of a managed connection tbat is to be discarded
     * the local transaction should be rolled back.
     *
     * @param managedConnection the managed connection to be delisted
     * @param entry the managed connection entry
     * @param discard True if the managed connection is to be discarded 
     *      and not used again
     * @return True if the specified managed connection has been delisted.
     * @throws ConnectionException if there is a problem delisting the local
     *      transaction
     */
    protected boolean delistLocalTransaction(ManagedConnection managedConnection,
                                           ManagedConnectionEntry entry, 
                                           boolean discard)
        throws ConnectionException
    {
        if (null == entry.localTransaction) {
            // remove the entry
            removeManagedConnectionEntry(managedConnection);
            // remove the listener
            managedConnection.removeConnectionEventListener(getLocalTransactionListener());

            return true;
        }
        else if (discard) {
            try {
                entry.localTransaction.rollback();

                return true;
            }
            catch (Exception e) {
            
                throw new ConnectionException ("Exception occurred while trying delist the manged connection <" + 
                                               managedConnection + 
                                               "> in the local transaction <" +
                                               entry.localTransaction + ">",
                                               e);
            }
            finally {
                entry.localTransaction = null;
                // remove the entry
                removeManagedConnectionEntry(managedConnection);
                // remove the listener
                managedConnection.removeConnectionEventListener(getLocalTransactionListener());
            }
        }

        return false;
    }


    /**
     * Delist the xa resource.
     * <P)
     * In the case of a managed connection that is not to be discarded
     * the XA resource is ended successfully in its transaction and 
     * the managed connection can be reused.
     *
     * <P)
     * In the case of a managed connection that is to be discarded
     * the XA resource is ended unsuccessfully in its transaction.
     *
     * @param managedConnection the managed connection to be delisted
     * @param entry the managed connection entry
     * @param discard True if the managed connection is to be discarded
     *      and not used again
     * @return True if the managed connection has been delisted and
     *      can be used again.
     * @throws ConnectionException if there is a problem delisting the
     *      managed connection
     */
    protected boolean delistXATransaction(ManagedConnection managedConnection,
                                        ManagedConnectionEntry entry, 
                                        boolean discard)
        throws ConnectionException
    {
        if (null != entry.transaction) {
            try {
                // if we can delist from the transaction
                if ((entry.transaction.getStatus() == Status.STATUS_ACTIVE) || 
                    (entry.transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK)) {
                    
                    if (entry.hasEnlistedResources) {
                        if (discard) {
                            ((TyrexTransactionManager)getTransactionManager()).discardResource(entry.xaResource);    
                        }
                        else {
                            ((TyrexTransactionManager)getTransactionManager()).delistResource(entry.xaResource);    
                        }
    
                        return false;
                    }
    
                    return  entry.transaction.delistResource(entry.xaResource, 
                                                             discard ? XAResource.TMFAIL : XAResource.TMSUCCESS) &&
                            !entry.hasEnlistedResources;
                }
            }
            catch (Exception e) {
                if (!(e instanceof ConnectionException)) {
                    throw new ConnectionException ("Exception occurred while trying delist xa resource for <" + 
                                                   managedConnection + 
                                                   "> in the transaction <" +
                                                   entry.transaction + ">",
                                                   e);
                }
                throw (ConnectionException)e;
            }
            finally {
                entry.transaction = null;
            }
        }

        if (!entry.hasEnlistedResources || discard) {
            // remove the entry
            removeManagedConnectionEntry(managedConnection);    
            return true;
        }
        return false;
    }


    /**
     * Called by the enlisted resource to notify the listener
     * that the resource had been delisted from the transaction after the
     * transaction has either committed, rolledback or been suspended.
     *
     * @param managedConnection the managed connection asscoatied with the resource
     */
    public final void delisted(ManagedConnection managedConnection)
    {
        synchronized (managedConnection) {    
            // remove the transaction from the managed connection entry
            ManagedConnectionEntry entry = getManagedConnectionEntry(managedConnection);

            if (null != entry) {
                entry.transaction = null;
            }
            else if (null != getLogWriter()) {
                getLogWriter().println("Internal error: The entry for the managed connection <" +
                                  managedConnection +
                                  "> is not found.");
            }
        }
    }


    /**
     * Return the transaction mediator.
     *
     * @return the transaction mediator.
     */
    protected final TransactionMediator getTransactionMediator()
    {
        return transactionMediator;
    }


    /**
     * Return the transaction manager
     *
     * @return the transaction manager
     */
    protected final TransactionManager getTransactionManager()
    {
        return transactionManager;
    }


    /**
     * Return the local transaction listener
     *
     * @return the local transaction listener
     */
     protected final LocalTransactionListener getLocalTransactionListener()
     {
         return localTransactionListener;
     }


     /**
      * True if the TransactionManager can enlist connections as enlisted resources
      */
     protected final boolean canEnlistResources()
     {
         return canEnlistResources;
     }


    /**
     * Called by the enlisted resource to notify the listeners
     * that the resource had been enlisted in the specified transaction.
     *
     * @param managedConnection the managed connection asscoatied with the resource
     * @param tx the transaction that the resource has been 
     *      enlsited in.
     */
    public final void enlisted(ManagedConnection managedConnection, Transaction tx)
    {
        synchronized (managedConnection) {    
    
            // remove the transaction from the managed connection entry
            ManagedConnectionEntry entry = getManagedConnectionEntry(managedConnection);
    
            if (null != entry) {
                entry.transaction = tx;
            }
            else if (null != getLogWriter()) {
                getLogWriter().println("Internal error: The entry for the managed connection <" +
                                  managedConnection +
                                  "> is not found.");
            }
        }
    }

    /**
     * The local transaction for the specified
     * managed connection has either been committed or
     * rolledback.
     *
     * @param managedConnection
     */
    protected void localTransactionBoundary(ManagedConnection managedConnection)
    {
        synchronized (managedConnection) {
            // remove the transaction from the managed connection entry
            ManagedConnectionEntry entry = getManagedConnectionEntry(managedConnection);
    
            if (null != entry) {
                entry.localTransaction = null;
                try {
                    // delist it
                    delistLocalTransaction(managedConnection, entry, false);
                }
                catch (ConnectionException e) {
                    if (null != getLogWriter()) {
                        getLogWriter().println("Internal error: Failed to delist local transaction for <" +
                                               managedConnection +
                                               ">.");
                        e.printStackTrace(getLogWriter());
                    }
                }
            }
            else if (null != getLogWriter()) {
                getLogWriter().println("Internal error: The entry for the managed connection <" +
                                  managedConnection +
                                  "> is not found.");
            }
        }
    }
    

    /**
     * The local transaction listener class
     */
    private class LocalTransactionListener
        extends ConnectionEventListenerAdapter
        implements ConnectionEventListener
    {
        /**
         * Create the LocalTransactionListener
         */
        private LocalTransactionListener()
        {

        }

        /**
         * Called by the managed connection to inform a listener
         * that a local transaction has begun for the managed connection
         *
         * @param event The event
         */
        public void localTransactionBegun(ConnectionEvent event)
        {
            //localTransactionStarted(event.getManagedConnection());
        }
    
    
        /**
         * Called by the managed connection to inform a listener
         * that a local transaction has been committed for the managed connection
         *
         * @param event The event
         */
        public void localTransactionCommitted(ConnectionEvent event)
        {
            localTransactionBoundary(event.getManagedConnection());
        }
        
        
        /**
         * Called by the managed connection to inform a listener
         * that a local transaction has been rolled back for the managed connection
         *
         * @param event The event
         */
        public void localTransactionRolledback(ConnectionEvent event)
        {
            localTransactionBoundary(event.getManagedConnection());
        }
    }

    /**
     * Collects information about the transactions that a particular
     * managed connection is in.
     */
    protected static class ManagedConnectionEntry
    {
        /**
         * The xa resource
         */
        protected final XAResource xaResource;
        

        /**
         * The transaction
         */
        protected Transaction transaction;


        /**
         * The local transaction
         */
        protected LocalTransaction localTransaction;


        /**
         * The transaction type
         */
        protected final TransactionType transactionType;


        /**
         * True if {@link EnlistedResource} objects have been created
         * from the managed connection. 
         */
        protected final boolean hasEnlistedResources;


        /**
         * Create the ManagedConnectionEntry with the
         * specified arguments.
         *
         * @param xaResource the XA resource.
         * @param transaction the transaction
         * @param transactionType the transaction type
         */
        protected ManagedConnectionEntry(XAResource xaResource,
                                       Transaction transaction,
                                       TransactionType transactionType,
                                       boolean hasEnlistedResources)
        {
            this.xaResource = xaResource;
            this.transaction = transaction;
            this.transactionType = transactionType;
            this.hasEnlistedResources = hasEnlistedResources;
            this.localTransaction = null;
        }

        /**
         * Create the ManagedConnectionEntry with the
         * specified arguments.
         *
         * @param localTransaction the local transaction
         * @param transactionType the transaction type
         */
        protected ManagedConnectionEntry(LocalTransaction localTransaction,
                                       TransactionType transactionType)
        {
            this.xaResource = null;
            this.transaction = null;
            this.localTransaction = localTransaction;
            this.hasEnlistedResources = false;
            this.transactionType = transactionType;
        }
    }
}
