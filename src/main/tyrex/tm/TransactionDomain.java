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
 * $Id: TransactionDomain.java,v 1.1 2000/08/28 19:01:51 mohammed Exp $
 */


package tyrex.tm;


import java.io.PrintWriter;
import java.util.Enumeration;
import java.security.AccessController;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.Inactive;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.xa.Xid;
import tyrex.interceptor.TransactionInterceptor;
import tyrex.resource.ResourcePool;
import tyrex.resource.ResourcePoolManager;
import tyrex.resource.ResourcePoolManagerImpl;
import tyrex.resource.ResourceLimits;
import tyrex.resource.ResourceTimeoutException;
import tyrex.server.RemoteTransactionServer;
import tyrex.util.Messages;


/**
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/08/28 19:01:51 $
 */
public class TransactionDomain
    implements ResourcePool, RemoteTransactionServer
{


    // IMPLEMENTATION NOTES:
    //
    //   All access to transactions must be synchronzied against
    //   the transaction object itself to prevent changes to the
    //   transaction as it is being committed/rolledback.
    //
    //   All transactions have unique, non-repeatable global
    //   identifiers. If synchronizing against the transaction
    //   object itself, there is no need to synchronized access
    //   to _txTable.



    /**
     * The default timeout for transactions (unless specified otherwise)
     * is 30 seconds.
     */
    public static final int DefaultTimeout = 30;


    /**
     * The default transaction domain. The default transaction domain is
     * always known to exist.
     */
    public static final String DefaultDomain = "default";


    /**
     * A table of all transactions created with this factory.
     * We use the global identifier as the unique key, and no two
     * transactions will share the same global identifier.
     * There are special requirements for using this table, see
     * {@link XidHashtable} for more info. Specifically:
     * <ul>
     * <li>We should not add and remove the same transaction
     * at once, so all access should be synchronized on the
     * transaction object, not the hashtable
     * <li>The enumerators are thread-safe and allow us to
     * remove as we enumerate
     * </ul>
     */
    private XidHashtable         _txTable = new XidHashtable();


    private ResourcePoolManager  _poolManager;
    

    private ResourceLimits       _limits;


    private TransactionInterceptor[]        _interceptors;


    private boolean              _threadTerminate;


    private int                  _txTimeout;


    private PrintWriter          _logWriter;


    private Thread               _background;


    private boolean              _nestedTx;


    private String               _domainName;


    private TransactionManagerImpl   _txManager;


    private UserTransaction      _userTx;


    public TransactionDomain( String domainName, ResourceLimits limits )
    {
	_domainName = domainName;
	_poolManager = new ResourcePoolManagerImpl( limits );
	_poolManager.manage( this, false );
	_limits = limits;
	_interceptors = new TransactionInterceptor[ 0 ];
	_txManager = new TransactionManagerImpl( this );
	_userTx = new UserTransactionImpl( _txManager );
    }

    /**
     * Return the internal type of the transaction manager
     * 
     * @return the internal type of the transaction manager
     * @see TransactionManagerImpl
     * @see TransactionImpl
     */
    final TransactionManagerImpl internalGetTransactionManager()
    {
    return _txManager;
    }

    public TransactionManager getTransactionManager()
    {
    return internalGetTransactionManager();
    }


    public UserTransaction getUserTransaction()
    {
	return _userTx;
    }

    
    public void setResourceLimits( ResourceLimits limits )
    {
    ResourcePoolManager newManager;

	synchronized ( _poolManager ) {

	    newManager = new ResourcePoolManagerImpl( limits );
	    _poolManager.unmanage();
	    _poolManager = newManager;
	    newManager.manage( this, false );
	    _limits = limits;
	}
    }


    public ResourceLimits getResourceLimits()
    {
	return _limits;
    }
    

    public void setThreadTerminate( boolean terminate )
    {
	_threadTerminate = terminate;
    }


    public boolean getThreadTerminate()
    {
	return _threadTerminate;
    }


    public void setTransactionTimeout( int timeout )
    {
	_txTimeout = ( timeout > 0 ? timeout : DefaultTimeout );
    }


    public int getTransactionTimeout()
    {
	return _txTimeout;
    }


    public void setNestedTransaction( boolean nestedTx )
    {
	_nestedTx = nestedTx;
    }


    public boolean getNestedTransactions()
    {
	return _nestedTx;
    }


    public void setLogWriter( PrintWriter logWriter )
    {
	if ( logWriter == null )
	    throw new IllegalArgumentException( "Argument 'logWriter' is null" );
	_logWriter = logWriter;
    }


    public PrintWriter getLogWriter()
    {
	return _logWriter;
    }


    public synchronized void addInterceptor( TransactionInterceptor interceptor )
    {
	TransactionInterceptor[] newInterceptors;

	for ( int i = 0 ; i < _interceptors.length ; ++i ) {
	    if ( _interceptors[ i ] == interceptor )
		return;
	}
	newInterceptors = new TransactionInterceptor[ _interceptors.length + 1 ];
	System.arraycopy( _interceptors, 0, newInterceptors, 0, _interceptors.length );
	newInterceptors[ _interceptors.length ] = interceptor;
	_interceptors = newInterceptors;
    }


    public synchronized void removeInterceptor( TransactionInterceptor interceptor )
    {
	TransactionInterceptor[] newInterceptors;

	for ( int i = 0 ; i < _interceptors.length ; ++i ) {
	    if ( _interceptors[ i ] == interceptor ) {
		_interceptors[ i ] = _interceptors[ _interceptors.length - 1 ];
		newInterceptors = new TransactionInterceptor[ _interceptors.length - 1 ];
		System.arraycopy( _interceptors, 0, newInterceptors, 0, _interceptors.length - 1 );
		_interceptors = newInterceptors;
		break;
	    }
	}
    }


    public TransactionInterceptor[] listInterceptors()
    {
	return (TransactionInterceptor[]) _interceptors.clone();
    }


    public byte[] createRemoteTransaction()
        throws SystemException
    {
        return createTransaction( null, null ).getXid().getGlobalTransactionId();
    }


    /**
     * Creates a new transaction. If <tt>parent</tt> is not null,
     * the transaction is nested within its parent. If <tt>activate</tt>
     * is true (and not nested) the transaction will be activated
     * for the current thread. Throws a {@link SystemException} if
     * we have reached the quota for new transactions or active
     * transactions, or the server has not been started.
     *
     * @param parent The parent transaction
     * @param activate True to activate the transaction with the
     *   current thread
     * @param timeout The default timeout for the new transaction,
     *   specified in seconds
     * @return The newly created transaction
     * @throws SystemException Reached the quota for new transactions
     */
    TransactionImpl createTransaction(  TransactionImpl parent,
				                        Thread thread)
	    throws SystemException
    {
    	TransactionHolder txh;
    	XidImpl           xid;
    
    	// Create a new transaction with a new Xid, or a nested
    	// transaction which is a branch in the parent transaction.
    	if ( parent == null )
    	    xid = new XidImpl();
    	else
    	    xid = parent.getXid().newBranch();
    	txh = new TransactionHolder();
    	txh.tx = new TransactionImpl( xid, parent, this );
    	txh.started = System.currentTimeMillis();
    	txh.timeout = _txTimeout * 1000;
    
    	// Nested transactions are not registered directly
    	// with the transaction server. They are not considered
    	// new creation/activation and are not subject to timeout.
    	if ( parent != null )
    	    return txh.tx;
    
    	synchronized ( _poolManager ) {
    	    // Ask the pool manager whether we can create this new
    	    // transaction before we register it. (A non-registered
    	    // transaction does not consume resources)
    	    // At this point we might get a SystemException.
    	    try {
    		    _poolManager.canCreateNew();
    	    } catch ( ResourceTimeoutException except ) {
    		    throw new SystemException( Messages.message( "tyrex.server.txCreateExceedsQuota" ) );
    	    }
    	    for ( int i = 0 ; i < _interceptors.length ; ++i ) {
        		try {
        		    _interceptors[ i ].begin( xid );
        		} catch ( Throwable except ) {
        		    // XXX Report error
        		}
    	    }

            try {
    		    _poolManager.canActivate();
    	    } catch ( ResourceTimeoutException except ) {
    		    throw new SystemException( Messages.message( "tyrex.server.txActiveExceedsQuota" ) );
    	    }

    	    // If we were requested to activate the transaction,
    	    // ask the pool manager whether we can activate it,
    	    // then associate it with the current thread.
    	    if ( thread != null ) {
        		int i = 0;
        
        		try {
        		    for ( i = 0 ; i < _interceptors.length ; ++i ) {
        			    _interceptors[ i ].resume( xid, thread );
        		    }
        		    txh.threads = new Thread[ 1 ];
        		    txh.threads[ 0 ] = thread;
        		} catch ( InvalidTransactionException except ) {
        		    while ( i-- > 0 ) {
        			    _interceptors[ i ].suspend( xid, thread );
        		    }
        		} catch ( Throwable except ) {
        		    // XXX Report error
        		}
    	    }
    
    	    // The table has not yet seen this gxid, so we trust the
    	    // put will be properly synchronized and do not need to
    	    // synchronize the table.
    	    // Nested transactions are not registered directly
    	    // with the transaction server.
    	    _txTable.put( xid.getGlobalTransactionId(), txh );
    	}
    	return txh.tx;
    }


    /**
     * Creates a new transaction to represent a remote OTS
     * transaction, but does not activate it yet. Throws a {@link
     * SystemException} if we have reached the quota for new
     * transactions or the server has not been started.
     * <p>
     * The newly created transaction will have a non-native Xid,
     * therefore it cannot be distributed across two machines using
     * the RMI interface but only through OTS propagation context.
     *
     * @param pgContext The OTS propagation context
     * @return The newly created transaction
     * @throws SystemException Reached the quota for new transactions
     * @see TransactionFactoryImpl
     * @see PropagationContext
     */
    TransactionImpl recreateTransaction( PropagationContext pgContext )
	throws SystemException
    {
	TransactionHolder txh;
	XidImpl           xid;

	// Create a new transaction based on the propagation context.
	xid = new XidImpl();
	txh = new TransactionHolder();
	try {
	    txh.tx = new TransactionImpl( xid, pgContext, this );
	} catch ( Inactive except ) {
	    throw new SystemException( Messages.message( "tyrex.tx.inactive" ) );
	}
	txh.started = System.currentTimeMillis();
	txh.timeout = pgContext.timeout;

	synchronized ( _poolManager ) {
	    // Ask the pool manager whether we can create this new
	    // transaction before we register it. (A non-registered
	    // transaction does not consume resources)
	    // At this point we might get a SystemException.
	    try {
		_poolManager.canCreateNew();
	    } catch ( ResourceTimeoutException except ) {
		throw new SystemException( Messages.message( "tyrex.server.txCreateExceedsQuota" ) );
	    }
	    for ( int i = 0 ; i < _interceptors.length ; ++i ) {
		try {
		    _interceptors[ i ].begin( xid );
		} catch ( Throwable except ) {
		    // XXX Report error
		}
	    }
	    // The table has not yet seen this gxid, so we trust the
	    // put will be properly synchronized and do not need to
	    // synchronize the table.
	    _txTable.put( xid.getGlobalTransactionId(), txh );
	}
	return txh.tx;
    }


    /**
     * Called by {@link RemoteUserTransaction} to obtain a transaction
     * created on this server remotely through it's global identifier.
     * Returns the transaction if it was found, throws an {@link
     * InvalidTransactionException} if the transaction has timed out
     * or cannot be located.
     * <p>
     * In a future version this method will be able to locate the
     * transaction on a remote server and provide a local interface
     * to the remote transaction.
     *
     * @param gxid The transaction global identifier
     * @return The transaction
     * @throws InvalidTransactionException The transaction cannot be
     *   located, the identifier is invalid, or the transaction has
     *   timed out
     */
    TransactionImpl getTransaction( byte[] gxid )
	throws InvalidTransactionException
    {
	TransactionHolder txh;
	int               i;

	if ( gxid.length == XidImpl.GLOBAL_XID_LENGTH ) {
	    if ( ! XidImpl.isLocal( gxid ) )
		    throw new InvalidTransactionException( Messages.message( "tyrex.server.originateElsewhere" ) );
	    txh = (TransactionHolder) _txTable.get( gxid );
	    if ( txh == null || txh.tx == null )
		throw new InvalidTransactionException( Messages.message( "tyrex.server.txRemoteMissing" ) );
	    return txh.tx;
	} else
	    throw new InvalidTransactionException( Messages.message( "tyrex.server.xidIllegalFormat" ) );
    }


    /**
     * Called by {@link TransactionImpl#forget} to forget about the
     * transaction once it has been commited/rolledback. The
     * transaction will no longer be available to {@link
     * #getTransaction}. The transaction's association and global
     * identifier are forgotten as well as all thread associated with
     * it. Subsequent calls to {@link #getTransaction} and {@link
     * #getControl} will not be able to locate the transaction.
     *
     * @param tx The transaction to forget about
     */
    void forgetTransaction( TransactionImpl tx )
    {
	TransactionHolder txh;
	int               i;

	// This method is synchronized so we know that the same
	// transaction identifier will not be removed twice at the
	// same time and do not need to synchronize the table.
	// We take into account that the transaction might have been
	// removed before.
	synchronized ( tx ) {
	    txh = (TransactionHolder) _txTable.remove( tx.getXid().getGlobalTransactionId() );
	    // If there were any threads associated with the transaction,
	    // we forget about them.
	    if ( txh != null ) {
		if ( txh.threads != null && txh.threads.length > 0 ) {
		    for ( i = 0 ; i < txh.threads.length ; ++i ) {
			for ( int j = 0 ; j < _interceptors.length ; ++j ) {
			    try {
				_interceptors[ j ].suspend( txh.tx.getXid(), txh.threads[ i ] );
			    } catch ( Throwable except ) {
				// XXX Report error
			    }
			}
			txh.threads[ i ] = null;
		    }
		    txh.threads = null;
		}
		txh.tx = null;
	    }
	}
	// We notify the pool managed the a resource has been
	// released so threads waiting to create a new transaction
	// might proceed.
	_poolManager.released();
    }


    /**
     * Called to terminate a transaction in progress. If the
     * transaction already completed, it will simply be forgotten
     * and removed from the transaction table. If the transaction is
     * active, it will be rolled back with a timed-out flag and all
     * threads associated with it will be terminated. Unlike normal
     * transaction termination through {@link #forgetTransaction} this
     * method assumes something went wrong and the transaction (or
     * it's resources) are no longer useable.
     *
     * @param txh The holder the thread to terminate
     */
    private void terminateTransaction( TransactionHolder txh )
    {
	int i;

	// Synchronize against the transaction to prevent any chance
	// of timeout during a commit/rollback.
	synchronized ( txh.tx ) {
	    Xid      xid;

	    // We need the Xid upfront since timedOut() will cause
	    // us to forget all about this transaction.
	    xid = txh.tx.getXid();

	    // It is possible that we are looking at a transaction
	    // that just completed, or one that has been left in
	    // the transaction table on purpose (see run() ).
	    if ( txh.tx.getStatus() != Status.STATUS_ACTIVE &&
		 txh.tx.getStatus() != Status.STATUS_MARKED_ROLLBACK ) {
		_txTable.remove( xid.getGlobalTransactionId() );
		_poolManager.released();
		if ( txh.threads != null && txh.threads.length > 0 ) {
		    for ( i = 0 ; i < txh.threads.length ; ++i ) {
			for ( int j = 0 ; j < _interceptors.length ; ++j ) {
			    try {
				_interceptors[ j ].suspend( txh.tx.getXid(), txh.threads[ i ] );
			    } catch ( Throwable except ) {
				// XXX Report error
			    }
			}
			txh.threads[ i ] = null;
		    }
		    txh.threads = null;
		}
		txh.tx = null;
	    }
	    else {
		try {
		    Thread[] threads = null;

		    logMessage( Messages.format( "tyrex.server.timeoutTerminate", xid.toString() ) );
		    if ( txh.threads != null )
			threads = (Thread[]) txh.threads.clone();
		    try {
			for ( int j = 0 ; j < _interceptors.length ; ++j ) {
			    try {
				_interceptors[ j ].completed( xid, Heuristic.TimedOut );
			    } catch ( Throwable except ) {
				// XXX Report error here
			    }
			}
			// This call will cause all the XA resources to
			// die, will forget the transaction and all its
			// association by calling TransactionImpl.forget()
			// and forgetTransaction().
			txh.tx.timedOut();
		    } catch ( Exception except ) {
			logMessage( Messages.format( "tyrex.server.timeoutTerminateError", xid.toString(), except ) );
		    }

		    // Sadly we have no way of checking out whether we
		    // are in the middle of some important code. This will
		    // terminate the thread correctly, since the thread is
		    // not supposed to do any synchronization, and hopefully
		    // will not damage the resources.
		    if ( threads != null && _threadTerminate ) {
			for ( i = 0 ; i < threads.length ; ++i ) {
			    try {
				for ( int j = 0 ; j < _interceptors.length ; ++j ) {
				    try {
					_interceptors[ j ].suspend( xid, txh.threads[ i ] );
				    } catch ( Throwable except ) {
				        // XXX Report error
				    }
				}
				threads[ i ].stop( new TransactionTimeoutException() );
			    } catch ( Exception except ) {
				// This will throw a security exception if we
				// have no permission to stop the thread.
			    }
			}
		    }
		} catch ( Exception except ) {
		    logMessage( Messages.format( "tyrex.server.timeoutTerminateError", txh.tx.toString(), except ) );
		}
	    }
	}
    }


    /**
     * Called by {@link Tyrex} to terminate a transaction in progress.
     * If the transaction already completed, it will simply be forgotten
     * and removed from the transaction table. If the transaction is
     * active, it will be rolled back with a timed-out flag and all
     * threads associated with it will be terminated.
     *
     * @param tx The transaction to terminate
     * @throws InvalidTransactionException The transaction did not originate
     *   on this server, or has already been terminated
     */
    void terminateTransaction( Transaction tx )
	throws InvalidTransactionException
    {
	TransactionHolder txh;

	if ( ! ( tx instanceof TransactionImpl ) )
	    throw new InvalidTransactionException( Messages.message( "tyrex.server.originateElsewhere" ) );
	txh = (TransactionHolder) _txTable.get( ( (TransactionImpl) tx ).getXid().getGlobalTransactionId() );
	if ( txh == null )
	    throw new InvalidTransactionException( Messages.message( "tyrex.tx.inactive" ) );
	terminateTransaction( txh );
    }


    /**
     * Called by {@link TransactionManager#setTransactionTimeout} to
     * change the timeout of the transaction and all the resources
     * enlisted with that transaction.
     *
     * @param tx The transaction
     * @param timeout The new timeout in seconds, zero to use the
     *   default timeout for all new transactions.
     * @see TransactionManager#setTransactionTimeout
     */
    void setTransactionTimeout( TransactionImpl tx, int timeout )
    {
	TransactionHolder txh;

	// For zero we use the default timeout for all new transactions.
	if ( timeout <= 0 )
	    timeout = _txTimeout;
	// Change the timeout for the transaction so the background
	// thread will not attempt to kill it (if it didn't already).
	// We then ask the transaction to change the timeout on all
	// the resources enlisted with it.
	synchronized ( tx ) {
	    txh = (TransactionHolder) _txTable.get( tx.getXid().getGlobalTransactionId() );
	    if ( txh != null ) {
		txh.timeout = timeout * 1000;
		if ( txh.tx != null )
		    txh.tx.internalSetTransactionTimeout( timeout );
	    }
	}
    }


    /**
     * Called by {@link ControlImpl} to obtain the timeout on a
     * transaction for the purpose of the propagation context.
     *
     * @param tx The transaction
     * @return The transaction's timeout in seconds
     */
    int getTransactionTimeout( TransactionImpl tx )
    {
	TransactionHolder txh;

	txh = (TransactionHolder) _txTable.get( tx.getXid().getGlobalTransactionId() );
	if ( txh == null )
	    return _txTimeout;
	else
	    return (int) ( txh.timeout ) / 1000;
    }


    /**
     * Called by {@link TransactionImpl#resume} to associate the
     * transaction with the thread. This will allow us to terminate
     * the thread when the transaction times out. If the transaction
     * has not been associated with any thread before, it now becomes
     * active. We ask the pool manager whether we can activate the
     * transaction and if a timeout occurs, we throw a system exception.
     *
     * @param tx The transaction
     * @param thread The thread to associate with the transaction
     * @throws SystemException The pool manager does not allow us to
     *   active the transaction
     */
    void enlistThread( TransactionImpl tx, Thread thread )
        throws SystemException
    {
	TransactionHolder txh;
	Xid               xid;

	synchronized ( tx ) {
	    xid = tx.getXid();
	    txh = (TransactionHolder) _txTable.get( xid.getGlobalTransactionId() );
	    if ( txh != null ) {
		int i = 0;

		try {
		    for ( i = 0 ; i < _interceptors.length ; ++i ) {
			_interceptors[ i ].resume( xid, thread );
		    }
		    if ( txh.threads == null ) {
			// We ask the pool manager whether we can activate
			// another transaction. If we cannot, a timeout
			// exception will cause us to throw a system exception.
			txh.threads = new Thread[ 1 ];
			txh.threads[ 0 ] = thread;
		    } else {
			// We use arrays because supposedly they are faster
			// than vectors. We don't expect to reach this point
			// too much, only when two threads share the same
			// transaction.
			Thread[] newList;

			newList = new Thread[ txh.threads.length + 1 ];
			System.arraycopy( txh.threads, 0, newList, 0, txh.threads.length );
			newList[ txh.threads.length ] = thread;
			txh.threads = newList;
		    }
		} catch ( InvalidTransactionException except ) {
		    while ( i-- > 0 ) {
			_interceptors[ i ].suspend( xid, thread );
		    }
		} catch ( Throwable except ) {
		    // XXX Report error
		}
	    }
	}
    }


    /**
     * Called by {@link TransactionImpl#resume} to dissociate the
     * transaction from the thread. If the transaction has only been
     * associated with this one thread, it becomes inactive.
     *
     * @param tx The transaction
     * @param thread The thread to dissociate from the transaction
     * @see enlistThread
     */
    void delistThread( TransactionImpl tx, Thread thread )
    {
	TransactionHolder txh;
	Xid               xid;

	synchronized ( tx ) {
	    xid = tx.getXid();
	    txh = (TransactionHolder) _txTable.get( xid.getGlobalTransactionId() );
	    for ( int i = 0 ; i < _interceptors.length ; ++i ) {
		try {
		    _interceptors[ i ].suspend( xid, thread );
		} catch ( Throwable except ) {
		    // XXX Report error
		}
	    }
	    if ( txh != null && txh.threads != null ) {
		// If only one thread has been associated with the
		// transaction and this is the thread we are
		// dissociating, then the thread becomes inactive.
		// Notify the pool manager that another transaction
		// can become active.
		if ( txh.threads.length == 1 ) {
		    if ( txh.threads[ 0 ] == thread ) {
			txh.threads = null;
		    }
		} else {
		    // We use arrays because supposedly they are faster
		    // than vectors. We don't expect to reach this point
		    // too much, only when two threads share the same
		    // transaction.
		    Thread[] newList;

		    for ( int i = 0 ; i < txh.threads.length ; ++i ) {
			if ( txh.threads[ i ] == thread ) {
			    txh.threads[ i ] = txh.threads[ txh.threads.length - 1 ];
			    newList = new Thread[ txh.threads.length - 1 ];
			    System.arraycopy( txh.threads, 0, newList, 0, txh.threads.length - 1 );
			    txh.threads = newList;
			    return;
			}
		    }
		}
	    }
	}
    }


    /**
     * Called by {@link TransactionImpl} to check whether the thread
     * is an owner of the transaction. Only owners are allowed to
     * commit/rollback a transaction. Owners are all the threads
     * previously associated with the transaction and the background
     * thread.
     *
     * @param tx The transaction
     * @param thread The thread asking to commit/rollback
     * @return True if the thread is an owner of the transaction
     */
    boolean isOwner( TransactionImpl tx, Thread thread )
    {
	TransactionHolder txh;
	int               i;

	// The background thread can terminate any transaction.
	if ( thread == _background )
	    return true;
	txh = (TransactionHolder) _txTable.get( tx.getXid().getGlobalTransactionId() );
	if ( txh != null && txh.threads != null ) {
	    for ( i = 0 ; i < txh.threads.length ; ++i )
		if ( txh.threads[ i ] == thread )
		    return true;
	}
	return false;
    }


    /**
     * Returns an enumeration of all the transactions currently
     * registered with the server. Each entry is described using the
     * {@link TransactionStatus} object providing information about
     * the transaction, its resources, timeout and active state.
     * Some of that information is only current to the time the list
     * was produced.
     *
     * @return List of all transactions currently registered
     */
    TransactionStatus[] listTransactions()
    {
	Enumeration          enum;
	TransactionStatus[]  txsList;
	TransactionHolder    txh;
	int                  index;

	txsList = new TransactionStatus[ _txTable.size() ];
	index = 0;
	enum = _txTable.elements();
	while ( enum.hasMoreElements() && index < txsList.length ) {
	    txh = (TransactionHolder) enum.nextElement();
	    if ( txh.tx != null ) {
		txsList[ index ] = new TransactionStatus( txh.tx, txh.started + txh.timeout, txh.threads != null );
		++index;
	    }
	}
	if ( index < txsList.length ) {
	    TransactionStatus[] newList;

	    newList = new TransactionStatus[ index ];
	    System.arraycopy( txsList, 0, newList, 0, index );
	    return newList;
	} else
	    return txsList;
    }


    /**
     * Returns information about the specified transactions using a
     * {@link TransactionStatus} object. Provides information about
     * the transaction, its resources, timeout and active state.
     * Some of that information is only current to the time the
     * request was made.
     *
     * @param tx The transaction
     * @return Information about that transaction, null if the
     *   transaction is no longer available
     */
    TransactionStatus getTransactionStatus( Transaction tx )
    {
	TransactionStatus txs;
	TransactionHolder txh;

	txh = (TransactionHolder) _txTable.get( ( (TransactionImpl) tx ).getXid().getGlobalTransactionId() );
	if ( txh != null && txh.tx != null ) {
	    txs = new TransactionStatus( txh.tx, txh.started + txh.timeout, txh.threads != null );
	    return txs;
	}
	return null;
    }


    void notifyCompletion( XidImpl xid, int heuristic )
    {
	int i;

	for ( i = 0 ; i < _interceptors.length ; ++i ) {
	    try {
		_interceptors[ i ].completed( xid, heuristic );
	    } catch ( Throwable except ) {
		// XXX Report error here
	    }
	}
    }


    void notifyCommit( XidImpl xid )
	throws RollbackException
    {
	int i;

	for ( i = 0 ; i < _interceptors.length ; ++i ) {
	    try {
		_interceptors[ i ].commit( xid );
	    } catch ( RollbackException except ) {
		throw except;
	    } catch ( Throwable except ) {
		// XXX Report error
	    }
	}
    }


    void notifyRollback( XidImpl xid )
    {
	int i;

	for ( i = 0 ; i < _interceptors.length ; ++i ) {
	    try {
		_interceptors[ i ].rollback( xid );
	    } catch ( Throwable except ) {
		// XXX Report error
	    }
	}
    }


    public void shutdown()
    {
	Enumeration       enum;
	TransactionHolder txh;

	// Manually terminate all the transactions that have
	// not timed out yet.
	enum = _txTable.elements();
	while ( enum.hasMoreElements() ) {
	    txh = (TransactionHolder) enum.nextElement();
	    try {
		terminateTransaction( txh );
	    } catch ( Exception except ) { }
	}

    }


    /**
     * Background thread that looks for transactions that have timed
     * out and terminates them. Will be running in a low priority for
     * as long as the server is active, monitoring the transaction
     * table and terminating threads in progress.
     * <p>
     * A transaction might time out while a client is not accessing
     * it. When the client tries to lookup the transaction, no
     * transaction is there to indicate the possibility of a timeout.
     * (This could happen, e.g. immediately after the transaction
     * thread has been terminated.) To ease the pain, the transaction
     * is actually removed in two phases: first terminated, then
     * after a wait period, forgotten about. All client accesses
     * between termination and forgetting will yield a transaction
     * that has rolled back.
     * <p>
     * This thread is terminated by interrupting it. The caller
     * must have {@link TyrexPermission.Server#Shutdown}
     * permission to interrupt it.
     */
    public void run()
    {
	TransactionHolder txh;
	Enumeration       enum;
	long              timeout;
	TransactionImpl   tx;

	while ( true ) {
	    try {
		Thread.sleep( _limits.getCheckEvery() * 1000 );
        		
        // Our enumerator is guaranteed to remain valid even if
		// we just removed the entry we were looking at or some
		// other thread removed the next entry (that entry will
		// be marked so we will not time it out.
		timeout = System.currentTimeMillis();
		enum = _txTable.elements();
		while ( enum.hasMoreElements() ) {
		    txh = (TransactionHolder) enum.nextElement();
		    if ( ( txh.started + txh.timeout ) < timeout ) {
			// If the transaction is active at this point,
			// we remember it and reinstantiate it after
			// terminateTransaction will eradicate it.
			tx = txh.tx;
			if ( tx.getStatus() != Status.STATUS_ACTIVE &&
			     tx.getStatus() != Status.STATUS_MARKED_ROLLBACK )
			    tx = null;
			terminateTransaction( txh );
			if ( tx != null ) {
			    txh.tx = tx;
			    txh.timeout = _limits.getCheckEvery() * 1000;
			    _txTable.put( tx.getXid().getGlobalTransactionId(), txh );
			}
		    }
		}
	    } catch ( InterruptedException except ) {
		// Thread interruption will cause the thread to die
		// if the caller has sufficient privileges.
		try {
		    AccessController.checkPermission( TyrexPermission.Server.Shutdown );
		    return;
		} catch ( SecurityException except2 ) { }
	    }
	}
    }


    /**
     * Called to print the textual messages into the log writer
     * specified for this server.
     */
    void logMessage( String message )
    {
	if ( _logWriter != null )
	    _logWriter.println( message );
    }


    //----------------------
    // ResourcePool methods
    //----------------------

    public int getActiveCount()
    {
	return _txTable.size();
    }


    public int getPooledCount()
    {
	return 0; // There is not transction pool
    }


    public void releasePooled( int count )
    {
	// Do nothing. There is no transaction pool
    }


    /**
     * Holds information about a transaction, its projected timeout and
     * the threads associated with it. All transactions managed by this
     * server are held inside a class like this.
     */
    static class TransactionHolder
    {

	/**
	 * The transaction associated with this thread, or null if
	 * there is no transaction in progress. Note, a transaction
	 * might be associated with the thread but inactive.
	 */
	TransactionImpl tx;

	/**
	 * If the transaction is a local transaction it will be
	 * associated with any number of currently running threads.
	 * This association is required in order to stop the running
	 * threads on timeout.
	 */
	Thread[]      threads;

	/**
	 * Indicates when the transaction will timeout as milliseconds
	 * from its beginning.
	 */
	long       timeout;

	/**
	 * Indicates when the transaction started as current sysem time.
	 */
	long       started;

    }


}
