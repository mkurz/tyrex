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
 * $Id: TransactionManagerImpl.java,v 1.2 2000/09/08 23:06:13 mohammed Exp $
 */


package tyrex.tm;


import java.io.PrintWriter;
import java.util.Enumeration;
import java.security.AccessController;
import javax.transaction.*;
import javax.transaction.xa.*;
import tyrex.util.FastThreadLocal;
import tyrex.util.Messages;


/**
 * Implements a local transaction manager. The transaction manager
 * allows the application server to manage transactions on the local
 * thread through the {@link TransactionManager} interface.
 * <p>
 * The application server interacts with the transaction manager
 * through the {@link TransactionManager} interface and the {@link
 * tyrex.jdbc.ServerDataSource} objects, which automatically register
 * themselves with the transaction manager. The transaction manager
 * is a singlton, the  application server may construct any number of
 * transaction manager or get a single instance through {@link
 * #getInstance}.
 * <p>
 * The application server may expose the transaction manager to the
 * application through the {@link UserTransactionImpl} which allows
 * limited control over transactions.
 * <p>
 * The transaction manager is an integral part of the transaction
 * server and shares the same list of remotely created transaction.
 * To associate the current thread with a remotely created
 * transaction, use:
 * <pre>
 * Tyrex.resumeGlobal( gxid );
 * </pre>
 * Where <tt>gxid</tt> is a global transaction identifier previously
 * created through {@link TransactionServer#createClientTransaction)
 * or {@link RemoteUserTransaction#begin}.
 * <p>
 * Nested transactions are supported if the server configuration
 * indicates so, but all nested transactions appear as flat
 * transactions to the resources and are not registered with the
 * transaction server.
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/09/08 23:06:13 $
 * @see Tyrex#recycleThread
 * @see TransactionDomain
 * @see TransactionImpl
 */
final class TransactionManagerImpl
    implements TransactionManager, Status, TyrexTransactionManager
{


    /**
     * Holds associations between threads and transactions.
     * Each entry is of type {@link TransactionImpl}.
     */
    private static FastThreadLocal  _txLocal = new FastThreadLocal();


    private TransactionDomain       _txDomain;


    


    TransactionManagerImpl( TransactionDomain txDomain )
    {
	if ( txDomain == null )
	    throw new IllegalArgumentException( "Argument 'txDomain' is null" );
	_txDomain = txDomain;
    }


    public void begin()
	throws NotSupportedException, SystemException
    {
	ThreadResources tres;
	TransactionImpl tx;

	tres = (ThreadResources) _txLocal.get();
	if ( tres == null ) {
	    tres = new ThreadResources();
	    _txLocal.set( tres );
	    tres.tx = _txDomain.createTransaction( null, Thread.currentThread() );
	} else {
	    if ( tres.tx != null && tres.tx.getStatus() != STATUS_COMMITTED &&
		     tres.tx.getStatus() != STATUS_ROLLEDBACK ) {
    		if ( ! _txDomain.getNestedTransactions() )
    		    throw new NotSupportedException( Messages.message( "tyrex.tx.noNested" ) );
    		else {
    		    // A nested transaction is not registered with
    		    // the transaction server and the thread is not
    		    // enlisted. Resources are not enlisted with
    		    // this transaction, only the top-level one.
    		    tres.tx = _txDomain.createTransaction( tres.tx, null );
    		    return; // XXX
    		}
	    } else
		    tres.tx = _txDomain.createTransaction( null, Thread.currentThread() );
	}
	
	// If there are any resources associated with the transaction,
	// we need to enlist them with the transaction.
	enlistResources( tres.tx, tres.xaList );
    enlistResources( tres.tx, tres.enlistedXAList );
    // tell the enlisted resources they are enlisted
    // in a new transaction.
    if ( null != tres.enlistedList ) {
        for ( int i = 0; i < tres.enlistedList.length; i++) {
            tres.enlistedList[ i ].enlisted( tres.tx );
        }
    }

    try {
        // register a synchronization to perform cleanup
        tres.tx.registerSynchronization( new TransactionManagerSynchronization() );
    } catch ( RollbackException e ) {
        // this should not happen
        throw new SystemException( e.toString() );
    }

    }


    /**
     * Enlist the XA resources in the specified transaction. The
     * transaction is assumed to be a top-level transaction.
     *
     * @param tx the top-level transaction
     * @param xaResources the array of XA resources to be enlisted
     *      in the transaction. Can be null.
     * @throws SystemException if there is a problem enlisting the 
     *      resources.
     */
    private void enlistResources(Transaction tx, XAResource[] xaResources)
        throws SystemException
    {
    if ( xaResources != null ) {
	    try {
		for ( int i = 0 ; i < xaResources.length ; ++i )
		    tx.enlistResource( xaResources[ i ] );
	    } catch ( Exception except ) {
		// Any error that occurs in the enlisting and we
		// cannot go on with the transaction. Must rollback
		// the enlisted resources, though.
		try {
		    rollback();
		} catch ( Exception e ) { }
		if ( except instanceof SystemException )
		    throw ( SystemException) except;
		else
		    throw new SystemException( except.toString() );
	    }
	}
    }


    public void commit()
	throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	       SecurityException, IllegalStateException, SystemException
    {
	ThreadResources tres;
	int             i;
	TransactionImpl parent;

	tres = (ThreadResources) _txLocal.get();
	if ( tres == null || tres.tx == null )
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );

    try {
	    tres.tx.getTopLevel().commit();
        //tres.tx.commit();
	} finally {
	    // This forgets about the transaction for the thread.
	    // We cannot forget about the resources, since a new
	    // transaction might be started on this thread.
	    // If this is a sub-transaction, we resume the parent
	    // transaction.
	    tres.tx = null; //tres.tx.getParent();
	}
    }


    public void rollback()
	throws IllegalStateException, SecurityException, SystemException
    {
	ThreadResources tres;
	int             i;
	TransactionImpl parent;

	tres = (ThreadResources) _txLocal.get();
	if ( tres == null || tres.tx == null )
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
    try {
	    //tres.tx.rollback();
        tres.tx.getTopLevel().rollback();
	} finally {
	    // This forgets about the transaction for the thread.
	    // We cannot forget about the resources, since a new
	    // transaction might be started on this thread.
	    // If this is a sub-transaction, we resume the parent
	    // transaction.
	    tres.tx = null; //tres.tx.getParent();
	}
    
    }


    public int getStatus()
    {
	ThreadResources tres;

	tres = (ThreadResources) _txLocal.get();
	if ( tres == null || tres.tx == null )
	    return Status.STATUS_NO_TRANSACTION;
	else
	    return tres.tx.getStatus();
    }


    public Transaction getTransaction()
    {
	ThreadResources tres;

	// TransactionManager operation requires suitable permission.
	AccessController.checkPermission( TyrexPermission.Transaction.Manager );
	tres = (ThreadResources) _txLocal.get();

    if ( ( null == tres ) || ( null == tres.tx ) ) {
        return null;    
    }
    // always return the toplevel transaction
	return  tres.tx.getTopLevel();
    }

    /**
     * Resume the specified transaction in the current thread.
     * <p>
     * The 
     *
     * @param tx the transaction
     * @throws InvalidTransactionException if the transaction has timed out,
     *      is foreign to transaction manager, if the transaction is not active
     *      or is not marked for rollback
     * @throws IllegalStateException if there is already a transaction associated
     *      eith the current thread.
     */
    public void resume( Transaction tx )
	throws InvalidTransactionException, IllegalStateException, SystemException
    {
    int              status;
    
	// TransactionManager operation requires suitable permission.
	AccessController.checkPermission( TyrexPermission.Transaction.Manager );
	if ( ! ( tx instanceof TransactionImpl ) )
	    throw new InvalidTransactionException( Messages.message( "tyrex.tx.resumeForeign" ) );

	synchronized ( tx ) {
	    status = tx.getStatus();
	    if ( status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK ) {
            throw new InvalidTransactionException( Messages.message( "tyrex.tx.inactive" ) );
        }

	    internalResume( ( TransactionImpl ) tx, true );
	}
    }

    
    /**
     * Called by the {@link TransactionImpl} to ensure that
     * the current thread has the specified transaction associated
     * with it irregardless of the status of the specified transaction.
     * <p>
     * This is done so that Synchronization objects will have this
     * transaction as the current transaction in the thread for the
     * beforeCompletion and afterCompletion method calls.
     *
     * @param tx the transaction
     * @param enlistThreadResources true if the resources associated
     *      with the thread are enlisted with the transaction.
     * @throws InvalidTransactionException if the transaction has
     *      timed out.
     * @throws IllegalStateException if the thread is already
     *      associated with a transaction
     * @throws SystemException if there is a problem enlisting
     *      the resources associated with the thread with the
     *      new thread.
     * @see TransactionImpl#beforeCompletion
     * @see TransactionImpl#forget(int)
     */
    void internalResume( TransactionImpl tx, boolean enlistThreadResources )
	throws InvalidTransactionException, IllegalStateException, SystemException
    {
    TransactionImpl toplevelTransaction;
	ThreadResources tres;
	int              i;

	synchronized ( tx ) {
	    if ( tx.getTimedOut() ) {
            throw new InvalidTransactionException( Messages.message( "tyrex.tx.timedOut" ) );
        }
	    

	    tres = (ThreadResources) _txLocal.get();
	    if ( tres != null && tres.tx != null )
		throw new IllegalStateException( Messages.message( "tyrex.tx.resumeOverload" ) );
	    if ( tres == null ) {
		tres = new ThreadResources();
		_txLocal.set( tres );
	    }
	    tres.tx = tx;

        toplevelTransaction = tres.tx.getTopLevel();

        if ( enlistThreadResources ) {
        try {
        toplevelTransaction.resumeAndEnlistResources(tres.xaList);
        } catch ( RollbackException except ) { }
        }

        // Enlist the current thread with the transaction so it
	    // may timeout the thread. We always enlist the top
	    // level transaction.
	    _txDomain.enlistThread( toplevelTransaction,
                                Thread.currentThread() );
	}
    }
    

    public Transaction suspend()
    {
	ThreadResources tres;
	TransactionImpl tx;
	int             i;

	// TransactionManager operation requires suitable permission.
	AccessController.checkPermission( TyrexPermission.Transaction.Manager );
	tres = (ThreadResources) _txLocal.get();
	if ( tres == null || tres.tx == null )
	    return null;

    // get the toplevel transaction
	tx = tres.tx.getTopLevel();
	tres.tx = null;

    synchronized ( tx ) {
	// Delist the current thread form the transaction so it
	// does not attempt to timeout. We always enlist the top
	// level transaction.
	_txDomain.delistThread( tx, Thread.currentThread() );

	// We do not return an inactive transaction.
	if ( tx.getStatus() == STATUS_ACTIVE || tx.getStatus() == STATUS_MARKED_ROLLBACK ) {
	    // It is our responsibility at this point to suspend
	    // all the resources we associated with the transaction.
        try {
            // the resources associated with the thread is
            // a subset of the resources associated with the 
            // transaction.
            tx.suspendResources();
        } catch ( SystemException except ) { }
        
	    delist( tres );

        return tx;
    
	} else
	    return null;
    }
    }
    

    public void setRollbackOnly()
	throws IllegalStateException, SystemException
    {
	ThreadResources tres;

	tres = (ThreadResources) _txLocal.get();
	if ( tres == null || tres.tx == null )
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
	tres.tx.setRollbackOnly();
    }


    public void setTransactionTimeout( int seconds )
    {
	ThreadResources tres;

	if ( seconds < 0 )
	    throw new IllegalArgumentException( Messages.message( "tyrex.tx.timeNegative" ) );
	tres = (ThreadResources) _txLocal.get();
	if ( tres != null && tres.tx != null )
	    _txDomain.setTransactionTimeout( tres.tx.getTopLevel(), seconds );
    }


    /**
     * Called by a resource to enlist itself with the currently
     * running transactions. JDBC connections created through
     * {@link tyrex.jdbc.ServerDataSource} will automatically enlist
     * themselves with the currently running transactions. If there
     * is not currently running transaction, the resource will be
     * enlisted with the current thread and with the transaction
     * when one is associated with the thread.
     *
     * @param xaRes The XA resource
     */
    public void enlistResource( XAResource xaRes )
	throws SystemException, RollbackException
    {
	ThreadResources tres;

	tres = (ThreadResources) _txLocal.get();
	if ( tres == null ) {
	    tres = new ThreadResources();
	    _txLocal.set( tres );
	}
	// If there is a transaction in progress, we enlist the
	// resource immediately and throw an exception if we
	// cannot do that. Otherwise, we just record the resource
	// for the current thread.
	// For the resources all transaction are flat: we always
	// enlist with the top level transaction.
	if ( tres.tx != null )
	    tres.tx.getTopLevel().enlistResource( xaRes );
	tres.add( xaRes );
    }


    /**
     * Called by a resource to delist itself with the currently
     * running transactions in the current thread. 
     *
     * @param xaRes The XA resource
     */
    public void delistResource( XAResource xaRes )
	throws SystemException, RollbackException
    {
        delistResource( xaRes, Thread.currentThread() );
    }

    /**
     * Called by a resource to delist itself with the currently
     * running transactions. Equivalent to
     * calling {link #delistResource(XAResource)} in the current
     * thread.
     *
     * @param xaRes The XA resource
     * @param thread the thread to delist the resource from
     */
    public void delistResource( XAResource xaRes, Thread thread )
	throws SystemException, RollbackException
    {
	ThreadResources tres;

	tres = (ThreadResources) _txLocal.get(thread);
	if ( tres != null ) {
        tres.remove( xaRes );

    	// If there is a transaction in progress, we delist the
    	// resource immediately and throw an exception if we
    	// cannot do that. Otherwise, we just remove the resource
    	// for the current thread.
    	// For the resources all transaction are flat: we always
    	// delist with the top level transaction.
    	if (tres.tx != null) {
            tres.tx.getTopLevel().delistResource( xaRes, XAResource.TMSUCCESS );
        }

    }
    }

    
    /**
     * Called by a resource to enlist itself with the currently
     * running transactions. JDBC connections created through
     * {@link tyrex.jdbc.ServerDataSource} will automatically enlist
     * themselves with the currently running transactions. If there
     * is no currently running transaction, the resource will be
     * enlisted with the current thread and with the transaction
     * when one is associated with the thread.
     * <p>
     * Once enlisted in this manner, the resource will be notified
     * when it has been delisted, so it may automatically re-enlist
     * itself upon subsequent use.
     *
     * @param xaRes The XA resource
     * @param enlisted The resource as an enlisted resource
     */
    public void enlistResource( XAResource xaRes, EnlistedResource enlisted )
	throws SystemException, RollbackException
    {
	ThreadResources tres;

	tres = (ThreadResources) _txLocal.get();
	if ( tres == null ) {
	    tres = new ThreadResources();
	    _txLocal.set( tres );
	}
	// If there is a transaction in progress, we enlist the
	// resource immediately and throw an exception if we
	// cannot do that. Otherwise, we just record the resource
	// for the current thread.
	// For the resources all transaction are flat: we always
	// enlist with the top level transaction.
	if ( tres.tx != null )
	    tres.tx.getTopLevel().enlistResource( xaRes );
	// tres.add( xaRes );
    // 
	tres.add( xaRes, enlisted );

    if ( null != tres.tx ) {
        enlisted.enlisted( tres.tx.getTopLevel() );
    }
    }


    /**
     * Called by a resource to delist itself with the currently
     * running transactions. 
     *
     * @param xaRes The XA resource
     * @param enlisted The resource as an enlisted resource
     */
    public void delistResource( XAResource xaRes, EnlistedResource enlisted )
	throws SystemException, RollbackException
    {
        delistResource( xaRes, enlisted, Thread.currentThread() );
    }

    
    /**
     * Called by a resource to delist itself with the currently
     * running transactions. Equivalent to calling 
     * {link delistResource(XAResource, EnlistedResource)}.
     *
     * @param xaRes The XA resource
     * @param enlisted The resource as an enlisted resource
     * @param thread the thread to delist the resources from
     */
    public void delistResource( XAResource xaRes, EnlistedResource enlisted, Thread thread )
	throws SystemException, RollbackException
    {
	ThreadResources tres;

	tres = (ThreadResources) _txLocal.get(thread);
	if ( tres != null ) {
    // If there is a transaction in progress, we delist the
	// resource immediately and throw an exception if we
	// cannot do that. Otherwise, we just remove the resource
	// for the current thread.
	// For the resources all transaction are flat: we always
	// delist with the top level transaction.
	if ( tres.tx != null )
	    tres.tx.getTopLevel().delistResource( xaRes, XAResource.TMSUCCESS );
	// tres.remove( xaRes );
    // delisted is called on enlisted in tres.remove
    tres.remove( xaRes, enlisted );
    }
    }


    /**
     * Called by a resource to delist itself with the currently
     * running transactions when the resource is closed due to
     * an error. The resource must not be used after this call
     * is made. Normal closure should never be reported in this
     * manner. The resource will be delisted when the transaction
     * terminates or {@link #delistResource} is called.
     *
     * @param xaRes The XA resource
     */
    public void discardResource( XAResource xaRes )
    {
	ThreadResources tres;

	tres = (ThreadResources) _txLocal.get();
	// If there is a transaction in progress, we delist the
	// resource immediately and throw an exception if we
	// cannot do that. We always delete it from the current
	// thread, whether successful or not.
	// for the current thread.
	if ( tres != null ) {
	    tres.remove( xaRes );
	    if ( tres.tx != null ) {
		// For the resources all transaction are flat: we always
		// enlist with the top level transaction.
		try {
		    tres.tx.getTopLevel().delistResource( xaRes, XAResource.TMFAIL );
		} catch ( SystemException except ) { }
	    }
	}
    }


    /**
     * Called by a resource to delist itself with the currently
     * running transactions when the resource is closed due to
     * an error. The resource must not be used after this call
     * is made. Normal closure should never be reported in this
     * manner. The resource will be delisted when the transaction
     * terminates or {@link #delistResource} is called.
     *
     * @param xaRes The XA resource
     * @param enlisted the resource as an enlisted resource
     */
    public void discardResource( XAResource xaRes, EnlistedResource enlisted )
    {
	ThreadResources tres;

	tres = (ThreadResources) _txLocal.get();
	// If there is a transaction in progress, we delist the
	// resource immediately and throw an exception if we
	// cannot do that. We always delete it from the current
	// thread, whether successful or not.
	// for the current thread.
	if ( tres != null ) {
	    //tres.remove( xaRes );
        try {
            // delist is called on enlisted in tres.remove
            tres.remove( xaRes, enlisted );
        } catch ( SystemException except ) { }
	    if ( tres.tx != null ) {
		// For the resources all transaction are flat: we always
		// enlist with the top level transaction.
		try {
		    tres.tx.getTopLevel().delistResource( xaRes, XAResource.TMFAIL );
		} catch ( SystemException except ) { }
	    }
    }
    }

    /**
     * Must be called by the application server after (or before) this
     * thread is being used on behalf of a bean. The thread is
     * associated with a list of resources that are relevant for the
     * previous invocation, but not the new one. If this method is not
     * called, memory consumption will simply increase over time.
     * <p>
     * If the thread is associated with an active transaction, the
     * transaction will be rolled back and a {@link RollbackException}
     * will be thrown.
     *
     * @throws RollbackException The thread is still associated
     *   with an active transaction, the transaction was rolled back
     */
    public void recycleThread()
	throws RollbackException
    {
	ThreadResources tres;
	TransactionImpl tx;
	int             i;

	// TransactionManager operation requires suitable permission.
	AccessController.checkPermission( TyrexPermission.Transaction.Manager );
	tres = (ThreadResources) _txLocal.get();
	if ( tres != null ) {
	    tx = tres.tx;
	    if ( tx != null ) {
    		tx = tx.getTopLevel();
            if ( tx.getStatus() == STATUS_ACTIVE || tx.getStatus() == STATUS_MARKED_ROLLBACK ) {
        		    try {
        			tx.rollback();
        		    } catch ( SystemException except ) { }
        		    _txLocal.set( null );
        		    throw new RollbackException( Messages.message( "tyrex.tx.recycleThreadRollback" ) );
            } else {
                _txDomain.delistThread( tx, Thread.currentThread() );
        	}
        } 
        _txLocal.set( null );
	}
    }


    /**
     * Returns the transaction currently associated with the given
     * thread, or null if the thread is not associated with any
     * transaction. This method is equivalent to calling {@link
     * TransactionManager#getTransaction} from within the thread.
     *
     * @param thread The thread to lookup
     * @return The transaction currently associated with that thread
     */
    Transaction getTransaction( Thread thread )
    {
	ThreadResources tres;

	tres = (ThreadResources) _txLocal.get( thread );
	return ( tres == null ? null : tres.tx.getTopLevel() );
    }
    
    /**
     * The transaction that has been executing in this
     * thread is complete.
     */
    private void transactionComplete()
    {
        ThreadResources tres = (ThreadResources) _txLocal.get( Thread.currentThread() );

        if ( null != tres ) {
            TransactionImpl tx = tres.tx;

            if ( null != tx ) {
                tres.tx = tx.getParent();
            }

            delist( tres );
        }
    }


    /**
     * Delist the enlisted resources contained in the
     * specified thread resources.
     *
     * @param tres the thread resources for the current
     *      thread. 
     */
    private void delist(ThreadResources tres)
    {
        if ( tres.enlistedList != null ) {
        for ( int i = 0; i < tres.enlistedList.length; i++) {
            try {
                tres.enlistedList[i].delisted();        
            } catch (Exception e) {
                // ignore
            }
        }
        tres.enlistedList = null;
        tres.enlistedXAList = null;
        }
    }

    /**
     * Synchronization used to inform the transaction manager
     * when a transaction is complete.
     */
    class TransactionManagerSynchronization 
        implements Synchronization
    {
        /**
         * Create the EnlistedResourceSynchronization with the
         * the specified thread resources
         */
        private TransactionManagerSynchronization()
        {
        }

        public void afterCompletion( int status ) 
        {
            TransactionManagerImpl.this.transactionComplete();
        }

        public void beforeCompletion() 
        {
            //do nothing
        }
    }
    

    /**
     * Identifies resources associated with the thread. Each thread must
     * have exactly one of these objects associated with it, listing the
     * transaction (if active), the XA resources enlisted with the
     * transaction, and the resources that should be notified on
     * completion. This object is required even before the transaction
     * start to enlist the resources.
     *
     * @see EnlistedResource
     */
    static class ThreadResources
    {
	
	/**
	 * The transaction associated with this thread, if the thread
	 * is in a transaction, or null if the thread is not in a
	 * transaction.
	 */
	TransactionImpl    tx;
	
	/**
	 * The XA resources that have been opened before or during the
	 * transaction and must be enlisted with the transaction when
	 * the transaction starts. Allows duplicate entries.
	 */
	XAResource[]       xaList;
	
    /**
     * The XA resources used with enlisted resources that have been 
     * opened before or during the transaction and must be enlisted 
     * with the transaction when * the transaction starts. 
     * Allows duplicate entries.
     */
    XAResource[]       enlistedXAList;

	/**
	 * The enlisted resources that have been opened before or during
	 * the transaction and must be notified when the transaction
	 * completes, or this thread is no longer used. Allows duplicate
	 * entries.
	 */
	EnlistedResource[] enlistedList;

    /**
	 * Adds an XA resource to the association list.
	 */
	void add( XAResource xaRes )
	{
	    XAResource[] newList;
	    int          i;
	    
	    if ( xaList == null ) {
		xaList = new XAResource[ 1 ];
		xaList[ 0 ] = xaRes;
	    } else {
		// Prevent duplicity.
		for ( i = 0 ; i < xaList.length ; ++i )
		    if ( xaList[ i ] == xaRes )
			return;
		newList = new XAResource[ xaList.length + 1 ];
		System.arraycopy( xaList, 0, newList, 0, xaList.length );
		newList[ xaList.length ] = xaRes;
		xaList = newList;
	    }
	}
	
    /**
	 * Adds an enlisted resource to the associated list.
	 */
	void add( XAResource xa, EnlistedResource enlist )
        throws SystemException
	{
	    EnlistedResource[]  newList;
        XAResource[]        newXAList;
	    int                 i;
	    
	    if ( enlistedList == null ) {
		enlistedList = new EnlistedResource[ 1 ];
		enlistedXAList = new XAResource[ 1 ];
        enlistedList[ 0 ] = enlist;
        enlistedXAList[ 0 ] = xa;
        } else {
		// Prevent duplicity.
		for ( i = 0 ; i < enlistedList.length ; ++i )
		    if ( enlistedList[ i ] == enlist ) {
                if (enlistedXAList[ i ] != xa) {
                    throw new SystemException("The xa resource <" + 
                                              xa +
                                              "> does not match the existing xa resource <" +
                                              enlistedXAList[ i ] +
                                              "> for the enlisted resource <" +
                                              enlist +
                                              ">.");
                }
                return;
            }
			
		newList = new EnlistedResource[ enlistedList.length + 1 ];
		System.arraycopy( enlistedList, 0, newList, 0, enlistedList.length );
		newList[ enlistedList.length ] = enlist;
		enlistedList = newList;

        newXAList = new XAResource[ enlistedXAList.length + 1 ];
		System.arraycopy( enlistedXAList, 0, newXAList, 0, enlistedXAList.length );
		newXAList[ enlistedXAList.length ] = xa;
		enlistedXAList = newXAList;
	    }
	}
	
	
	/**
	 * Removes an XA resource from the associated list.
     * <P>
     * If the XA resource exists in the XA list for enlisted
     * resources then remove all references to it and call
     * delisted on the matching enlisted resource.
     *
     * @param xaRes the XA resource
     */
	void remove( XAResource xaRes )
	{
	    XAResource[] newXAList;
	    int          i;
	    
	    if ( xaList != null ) {
		if ( xaList.length == 1 && xaList[ 0 ] == xaRes )
		    xaList = null;
		else {
		    for ( i = 0 ; i < xaList.length ; ++i )
			if ( xaList[ i ] == xaRes ) {
			    xaList[ i ] = xaList[ xaList.length - 1 ];
			    newXAList = new XAResource[ xaList.length - 1 ];
			    System.arraycopy( xaList, 0, newXAList, 0, xaList.length - 1 );
			    xaList = newXAList;
			}
		}
	    }

        if ( enlistedXAList != null ) {
            if ( enlistedXAList.length == 1 && enlistedXAList[ 0 ] == xaRes ) {
                try {
                    enlistedList[ 0 ].delisted();
                } catch (Exception e) {
                }
                enlistedList = null;
                enlistedXAList = null;
            }
            else {
                EnlistedResource[] newList;
                
                for ( i = 0 ; i < enlistedXAList.length ; ++i )
                if ( enlistedXAList[ i ] == xaRes ) {
                    try {
                        enlistedList[ i ].delisted();
                    } catch (Exception e) {
                    }

                    enlistedList[ i ] = enlistedList[ enlistedList.length - 1 ];
                    newList = new EnlistedResource[ enlistedList.length - 1 ];
                    System.arraycopy( enlistedList, 0, newList, 0, enlistedList.length - 1 );
                    enlistedList = newList;
    
                    enlistedXAList[ i ] = enlistedXAList[ enlistedXAList.length - 1 ];
                    newXAList = new XAResource[ enlistedXAList.length - 1 ];
                    System.arraycopy( enlistedXAList, 0, newXAList, 0, enlistedXAList.length - 1 );
                    enlistedXAList = newXAList;
                }
            }
        }    
    }


    /**
	 * Removes an enlisted resource from the associated list.
     * <P>
     * Delisted is called on the specified enlisted resource
     *
     * @param xa the xa resource
     * @param enlist the enlisted resource.
	 */
	void remove( XAResource xa, EnlistedResource enlist )
    throws SystemException
	{
	    EnlistedResource[]  newList;
        XAResource[]        newXAList;
	    int                 i;
	    
	    if ( enlistedList != null ) {
		if ( enlistedList.length == 1 && enlistedList[ 0 ] == enlist ) {
            if (enlistedXAList[ 0 ] != xa) {
                throw new SystemException("The xa resource <" + 
                                              xa +
                                              "> does not match the existing xa resource <" +
                                              enlistedXAList[ 0 ] +
                                              "> for the enlisted resource <" +
                                              enlist +
                                              ">.");
            }

            try {
                    enlistedList[ 0 ].delisted();
            } catch (Exception e) {
            }

            enlistedList = null;
            enlistedXAList = null;
        }
		else {
		    for ( i = 0 ; i < enlistedList.length ; ++i )
			if ( enlistedList[ i ] == enlist ) {
                if (enlistedXAList[ i ] != xa) {
                    throw new SystemException("The xa resource <" + 
                                                  xa +
                                                  "> does not match the existing xa resource <" +
                                                  enlistedXAList[ i ] +
                                                  "> for the enlisted resource <" +
                                                  enlist +
                                                  ">.");
                }

                try {
                    enlistedList[ i ].delisted();
                } catch (Exception e) {
                }

			    enlistedList[ i ] = enlistedList[ enlistedList.length - 1 ];
			    newList = new EnlistedResource[ enlistedList.length - 1 ];
			    System.arraycopy( enlistedList, 0, newList, 0, enlistedList.length - 1 );
			    enlistedList = newList;

                enlistedXAList[ i ] = enlistedXAList[ enlistedXAList.length - 1 ];
			    newXAList = new XAResource[ enlistedXAList.length - 1 ];
			    System.arraycopy( enlistedXAList, 0, newXAList, 0, enlistedXAList.length - 1 );
			    enlistedXAList = newXAList;
			}
		}
	    }
	}


    }


}
