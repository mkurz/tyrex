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
 * $Id: TransactionManagerImpl.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.server;


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
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see Tyrex#recycleThread
 * @see TransactionServer
 * @see TransactionImpl
 */
final class TransactionManagerImpl
    implements TransactionManager, Status
{


    /**
     * Holds associations between threads and transactions.
     * Each entry is of type {@link TransactionImpl}.
     */
    private static FastThreadLocal  _txLocal = new FastThreadLocal();



    TransactionManagerImpl()
    {
	// Make sure the transaction server is up and running.
	TransactionServer.getInstance();
    }


    public void begin()
	throws NotSupportedException, SystemException
    {
	ThreadResources tres;
	TransactionImpl tx;
	int             i;

	tres = (ThreadResources) _txLocal.get();
	if ( tres == null ) {
	    tres = new ThreadResources();
	    _txLocal.set( tres );
	    tres.tx = TransactionServer.createTransaction( null, true );
	} else {
	    if ( tres.tx != null && tres.tx.getStatus() != STATUS_COMMITTED &&
		 tres.tx.getStatus() != STATUS_ROLLEDBACK ) {
		if ( ! TransactionServer.getConfigure().getNestedTransaction() )
		    throw new NotSupportedException( Messages.message( "tyrex.tx.noNested" ) );
		else {
		    // A nested transaction is not registered with
		    // the transaction server and the thread is not
		    // enlisted. Resources are not enlisted with
		    // this transaction, only the top-level one.
		    tres.tx = TransactionServer.createTransaction( tres.tx, false );
		    return; // XXX
		}
	    } else
		tres.tx = TransactionServer.createTransaction( null, true );
	}
	
	// If there are any resources associated with the transaction,
	// we need to enlist them with the transaction.
	if ( tres.xaList != null ) {
	    try {
		for ( i = 0 ; i < tres.xaList.length ; ++i )
		    tres.tx.enlistResource( tres.xaList[ i ] );
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
	    tres.tx.commit();
	} finally {
	    // This forgets about the transaction for the thread.
	    // We cannot forget about the resources, since a new
	    // transaction might be started on this thread.
	    // If this is a sub-transaction, we resume the parent
	    // transaction.
	    tres.tx = tres.tx.getParent();
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
	    tres.tx.rollback();
	} finally {
	    // This forgets about the transaction for the thread.
	    // We cannot forget about the resources, since a new
	    // transaction might be started on this thread.
	    // If this is a sub-transaction, we resume the parent
	    // transaction.
	    tres.tx = tres.tx.getParent();
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
	AccessController.checkPermission( TransactionServerPermission.Transaction.Manager );
	tres = (ThreadResources) _txLocal.get();
	return tres.tx;
    }


    public void resume( Transaction tx )
	throws InvalidTransactionException, IllegalStateException, SystemException
    {
	ThreadResources tres;
	int              status;
	int              i;

	// TransactionManager operation requires suitable permission.
	AccessController.checkPermission( TransactionServerPermission.Transaction.Manager );
	if ( ! ( tx instanceof TransactionImpl ) )
	    throw new InvalidTransactionException( Messages.message( "tyrex.tx.resumeForeign" ) );

	synchronized ( tx ) {
	    status = tx.getStatus();
	    if ( ( (TransactionImpl) tx ).getTimedOut() )
		throw new InvalidTransactionException( Messages.message( "tyrex.tx.timedOut" ) );
	    if ( status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK )
		throw new InvalidTransactionException( Messages.message( "tyrex.tx.inactive" ) );

	    tres = (ThreadResources) _txLocal.get();
	    if ( tres != null && tres.tx != null )
		throw new IllegalStateException( Messages.message( "tyrex.tx.resumeOverload" ) );
	    if ( tres == null ) {
		tres = new ThreadResources();
		_txLocal.set( tres );
	    }
	    tres.tx = (TransactionImpl) tx;

	    // It is our responsibility at this point to resume
	    // all the resources we associated with the transaction.
	    if ( tres.xaList != null ) {
		for ( i = 0 ; i < tres.xaList.length ; ++i ) {
		    try {
			tres.tx.enlistResource( tres.xaList[ i ] );
		    } catch ( RollbackException except ) { }
		}
	    }
	    
	    // Enlist the current thread with the transaction so it
	    // may timeout the thread. We always enlist the top
	    // level transaction.
	    TransactionServer.enlistThread( ( (TransactionImpl) tx ).getTopLevel(),
					    Thread.currentThread() );
	}
    }


    public Transaction suspend()
    {
	ThreadResources tres;
	TransactionImpl tx;
	int             i;

	// TransactionManager operation requires suitable permission.
	AccessController.checkPermission( TransactionServerPermission.Transaction.Manager );
	tres = (ThreadResources) _txLocal.get();
	if ( tres == null || tres.tx == null )
	    return null;

	tx = tres.tx;
	tres.tx = null;

	// Delist the current thread form the transaction so it
	// does not attempt to timeout. We always enlist the top
	// level transaction.
	TransactionServer.delistThread( tx.getTopLevel(), Thread.currentThread() );

	// We do not return an inactive transaction.
	if ( tx.getStatus() == STATUS_ACTIVE || tx.getStatus() == STATUS_MARKED_ROLLBACK ) {
	    // It is our responsibility at this point to suspend
	    // all the resources we associated with the transaction.
	    if ( tres.xaList != null ) {
		for ( i = 0 ; i < tres.xaList.length ; ++i ) {
		    try {
			tres.tx.delistResource( tres.xaList[ i ], XAResource.TMSUSPEND );
		    } catch ( SystemException except ) { }
		}
	    }
	    return tx;
	} else
	    return null;
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
	    TransactionServer.setTransactionTimeout( tres.tx.getTopLevel(), seconds );
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
	tres.add( xaRes );
	tres.add( enlisted );
    }


    /**
     * Called by a resource to delist itself with the currently
     * running transactions when the resource is closed due to
     * an error. The resource must not be used after this call
     * is made. Normal closure should never be reported in this
     * manner. The resource will be delisted when the transaction
     * terminates or {@link #releaseResources} is called.
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
	AccessController.checkPermission( TransactionServerPermission.Transaction.Manager );
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
		} else
		    TransactionServer.delistThread( tx, Thread.currentThread() );
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
	return ( tres == null ? null : tres.tx );
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
final class ThreadResources
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
     * The enlisted resources that have been opened before or during
     * the transaction and must be notified when the transaction
     * completes, or this thread is no longer used. Allows duplicate
     * entries.
     */
    EnlistedResource[] enlisted;


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
    void add( EnlistedResource enlist )
    {
	EnlistedResource[] newList;
	int                i;

	if ( enlisted == null ) {
	    enlisted = new EnlistedResource[ 1 ];
	    enlisted[ 0 ] = enlist;
	} else {
	    // Prevent duplicity.
	    for ( i = 0 ; i < enlisted.length ; ++i )
		if ( enlisted[ i ] == enlist )
		    return;
	    newList = new EnlistedResource[ enlisted.length + 1 ];
	    System.arraycopy( enlisted, 0, newList, 0, enlisted.length );
	    newList[ enlisted.length ] = enlist;
	    enlisted = newList;
	}
    }


    /**
     * Removes an XA resource from the associated list.
     */
    void remove( XAResource xaRes )
    {
	XAResource[] newList;
	int          i;

	if ( xaList != null ) {
	    if ( xaList.length == 1 && xaList[ 0 ] == xaRes )
		xaList = null;
	    else {
		for ( i = 0 ; i < xaList.length ; ++i )
		    if ( xaList[ i ] == xaRes ) {
			xaList[ i ] = xaList[ xaList.length - 1 ];
			newList = new XAResource[ xaList.length - 1 ];
			System.arraycopy( xaList, 0, newList, 0, xaList.length - 1 );
			xaList = newList;
		    }
	    }
	}
    }


}
