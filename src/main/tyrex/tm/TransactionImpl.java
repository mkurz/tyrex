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
 * $Id: TransactionImpl.java,v 1.5 2000/12/19 02:21:36 mohammed Exp $
 */


package tyrex.tm;


import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.transaction.*;
import javax.transaction.xa.*;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.Vote;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import tyrex.util.Messages;


/**
 * Implements a global transaction. This transaction supports X/A
 * resources (see {@link XAResource}), can be part of an OTS global
 * transaction (see {@link ResourceImpl}) and can contain OTS
 * subtransactions (see {@link Resource}). Tightly integrated with
 * {@link TransactionManagerImpl} and {@link TransactionServer}.
 *
 * <P>
 * Synchronizations are called in the reverse order in which
 * they are added.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $ $Date: 2000/12/19 02:21:36 $
 * @see XAResourceHolder
 * @see TransactionManagerImpl
 * @see TransactionDomain
 * @see ResourceImpl
 */
final class TransactionImpl
    implements TyrexTransaction, Status, Heuristic
{

    /**
     * Holds a list of all the synchronization objects.
     */
    private Synchronization[]       _syncs;


    /**
     * Holds a list of all the enlisted resources, each one
     * of type {@link XAResourceHolder}.
     */
    private XAResourceHolder[]    _enlisted;


    /**
     * Holds a list of delisted resources, each one
     * of type {@link XAResourceHolder}.
     */
    private XAResourceHolder[]    _delisted;


    /**
     * Holds a list of all the enlisted OTS resources.
     */
    private Resource[]              _resources;


    /**
     * The global Xid of this transaction. Each transaction
     * will have exactly one global Xid for as long as it
     * exists.
     */
    private XidImpl                  _xid;


    /**
     * Holds the current status of the transaction.
     */
    private int                     _status;


    /**
     * Held during a commit/rollback process to indicate that
     * an unexpected error occured. Will throw that exception
     * if there is no other more important exception to report
     * (e.g. RollbackException).
     */
    private SystemException         _sysError;


    /**
     * True if this transaction has been rolled back due to timeout.
     */
    private boolean                 _timedOut;


    /**
     * If this transaction is a subtransaction of some global
     * transaction, this variable will reference the parent transaction.
     * Subtransactions cannot commit or rollback directly, only as
     * nested subtransactions.
     */
    private TransactionImpl         _parent;


    /**
     * If this transaction is a local recreation of a remote OTS
     * transaction, this variable will reference the propagation
     * context used to recreate this transaction. If this
     * transaction was created locally, this variable is null.
     */
    private PropagationContext      _pgContext;


    /**
     * The heuristic decision made by the transaction after a call to
     * {@link #prepare}, {@link #internalCommit}, {@link #internalRollback}.
     * Held in case the operation is repeated to return a consistent
     * heuristic decision. Defaults to read-only (i.e. no heuristic decision).
     */
    private int                    _heuristic = Heuristic.ReadOnly;


    /**
     * If this transaction is used through the OTS API, it will have
     * a control associated with it. The control is created when
     * needed and referenced from here. Most of the time, this
     * variable is null.
     */
    private ControlImpl           _control;


    /**
     * The domain to which this transaction belongs. The domain is notified
     * of the outcome of the transaction and any request to commit/rollback
     * the transaction.
     */
    private TransactionDomain     _txDomain;


    /**
     * Hidden constructor used by {@link TransactionDomain} to create
     * a new transaction. A transaction can only be created through
     * {@link TransactionDomain} or {@link TransactionManager} which
     * take care of several necessary housekeeping duties.
     *
     * @param xid The Xid for this transaction
     * @param parent The parent of this transaction if this is a
     *   nested transaction, null if this is a top level transaction
     * @param txDomain The transaction domain
     */
    TransactionImpl( XidImpl xid, TransactionImpl parent,
		     TransactionDomain txDomain )
    {
	_xid = xid;
	_status = STATUS_ACTIVE;
	_txDomain = txDomain;
	// If this transaction is a subtransaction we register it
	// as a resource in the parent transaction.
	if ( parent != null ) {
	    try {
		parent.registerResource( new ResourceImpl( this ) );
	    } catch ( IllegalStateException except ) {
		// The parent is being or has committed/rolledback,
		// so we did not register as a resource. Generally
		// this should not happen, but even if it does,
		// nothing breaks.
	    }
	    _parent = parent;
	}
    }


    TransactionDomain getTransactionDomain()
    {
	return _txDomain;
    }


    /**
     * Hidden constructor used by {@link TransactionDomain} to create
     * a new transaction. A transaction can only be created through
     * {@link TransactionDomain} or {@link TransactionManager} which
     * take care of several necessary housekeeping duties. This
     * transaction is created to import an OTS transaction using
     * the propagation context.
     *
     * @param xid The Xid for this transaction
     * @param pgContext The propagation context
     * @throws Inactive The parent transaction has rolled  back or
     *   is inactive
     * @param txDomain The transaction domain
     */
    TransactionImpl( XidImpl xid, PropagationContext pgContext,
		     TransactionDomain txDomain )
	throws Inactive
    {
	_xid = xid;
	_status = STATUS_ACTIVE;
	_txDomain = txDomain;
	// If this transaction is a local copy of a remote
	// transaction, we register it as a resource with the
	// remote transaction.
	_pgContext = pgContext;
	try {
	    _pgContext.current.coord.register_resource( new ResourceImpl( this ) );
	} catch ( RuntimeException except ) {
	    // Anything that the remote transaction throws us
	    // is considered an illegal state.
	    throw new Inactive();
	} catch ( Inactive except ) {
	    throw except;
	}
	_control = new ControlImpl( this, _pgContext );
    }

    /**
     * Suspend the current transaction. If an
     * error occurs the transaction is marked for
     * rollback.
     *
     * @see #asyncCommit
     * @see #asyncRollback
     */
    private void suspendTransaction()
    {
        AccessController.doPrivileged(new PrivilegedAction()
                {
                public java.lang.Object run() 
                {
                try {
                    // safety check
                    if ( _txDomain.getTransactionManager().getTransaction() ==  TransactionImpl.this ) {
                        _txDomain.getTransactionManager().suspend();
                    }
                } catch ( Exception except ) {
                _status = STATUS_MARKED_ROLLBACK;
                error( except );
                }
                return null;
                }
            });
    }


    /**
     * Perform an asynchronous commit on the transaction.
     * <p>
     * The current transaction is suspended from the current
     * thread so that a new transaction may be started in the
     * current transaction.
     *
     * @param callback the object that is registered
     *      to receive callbacks during the asynchronous
     *      commit. Can be null.
     * @throws SystemException if there is a problem
     *      associating the transaction with the
     *      new thread.
     * @throws SecurityException if the current thread is
     *      not allowed to rollback the transaction.
     * @throws RollbackException if the transaction has been
     *      marked for rollback
     */
    public synchronized void asyncCommit(AsyncCompletionCallback callback)
        throws SystemException, SecurityException, RollbackException
    {
        Thread thread;
         
        // Proper notification for transactions that timed out.
    	if ( _timedOut )
    	    throw new RollbackException( Messages.message( "tyrex.tx.timedOut" ) );
        
        securityCheck();

        suspendTransaction();

        if ( _status == STATUS_MARKED_ROLLBACK ) {
            // Status was changed to rollback or an error occured,
            // either case we have a heuristic decision to rollback.
            _heuristic = Heuristic.Rollback;

            throw new RollbackException( Messages.message( "tyrex.tx.rolledback" ) );
        }

        thread = new Thread(getAsyncCommitRunnable(callback));
        // enlist the thread
        _txDomain.enlistThread(this, thread);
        // start the thread
        thread.start();
    }

    /**
     * Return a runnable that will perform an asynchronous 
     * commit on the transaction when the runnable is
     * executed.
     *
     * @param callback the object that is registered
     *      to receive callbacks during the asynchronous
     *      commit. Can be null.
     */
    private Runnable getAsyncCommitRunnable(final AsyncCompletionCallback callback)
    {
        return new Runnable()
        {
            public void run() 
            {
                synchronized ( TransactionImpl.this )
                {
                    boolean exceptionOccurred = false;
    
                    if ( null != callback ) {
                        callback.beforeCompletion( TransactionImpl.this );    
                    }
    
                    try {
                        TransactionImpl.this.commit();
                    } catch( Exception e ) {
                        exceptionOccurred = true;
    
                        if ( null != callback ) {
                            callback.exceptionOccurred( TransactionImpl.this, e );    
                        }
                    }
    
                    if ( !exceptionOccurred &&
                         ( null != callback ) ) {
                        callback.afterCompletion( TransactionImpl.this );
                    }
                }
            }
        };
    }


    /**
     * Perform an asynchronous rollback on the transaction.
     * <p>
     * The current transaction is suspended from the current
     * thread so that a new transaction may be started in the
     * current transaction.
     *
     * @param callback the object that is registered
     *      to receive callbacks during the asynchronous
     *      commit. Can be null.
     * @throws IllegalStateException if the transaction
     *      is not in the proper state to be rolled back
     * @throws SystemException if there is a problem
     *      associating the transaction with the
     *      new thread.
     * @throws SecurityException if the current thread is
     *      not allowed to rollback the transaction.
     */
    public synchronized void asyncRollback( AsyncCompletionCallback callback )
        throws IllegalStateException, SystemException, SecurityException
    {
        Thread thread;
         
        // Proper notification for transactions that timed out.
	    if ( _timedOut )
	        throw new IllegalStateException( Messages.message( "tyrex.tx.timedOut" ) );

        securityCheck();

        suspendTransaction();

        // If a system error occured during this stage, report it.
	    if ( null != _sysError )
	        throw _sysError;

        thread = new Thread(getAsyncRollbackRunnable(callback));
        // enlist the thread
        _txDomain.enlistThread(this, thread);
        // start the thread
        thread.start();
    }

    /**
     * Return a runnable that will perform an asynchronous 
     * rollback on the transaction when the runnable is
     * executed.
     *
     * @param callback the object that is registered
     *      to receive callbacks during the asynchronous
     *      commit. Can be null.
     */
    private Runnable getAsyncRollbackRunnable( final AsyncCompletionCallback callback )
    {
        return new Runnable()
        {
            public void run() 
            {
                synchronized ( TransactionImpl.this )
                {
                    boolean exceptionOccurred = false;
    
                    if ( null != callback ) {
                        callback.beforeCompletion( TransactionImpl.this );    
                    }
    
                    try {
                        TransactionImpl.this.rollback();
                    } catch( Exception e ) {
                        exceptionOccurred = true;
    
                        if ( null != callback ) {
                            callback.exceptionOccurred( TransactionImpl.this, e );    
                        }
                    }
    
                    if ( !exceptionOccurred &&
                         ( null != callback ) ) {
                        callback.afterCompletion( TransactionImpl.this );
                    }
                }
            }
        };
    }

    /**
     * Return true if the transaction can be committed
     * using One Phase Commit.
     *
     * @return true if the transaction can be committed
     *      using One Phase Commit.
     */
    public boolean canUseOnePhaseCommit()
    {
        // if there are more than one resource then no one phase
        if ( ( null != _resources ) && ( 1 < _resources.length ) ) {
            return false;    
        }

        // if there are no _enlisted and _delisted then yes
        if ( ( null == _enlisted ) && ( null == _delisted ) ) {
            return true;
        }

        // if there are a mix of xa resources and resources then 
        // no
        if ( ( null != _resources ) && 
             ( ( null != _enlisted ) || ( null != _delisted ) ) ) {
            return false;
        }

        // if there is only one enlisted resource then yes
        if ( ( null != _enlisted ) && 
             ( 1 == _enlisted.length ) && 
             ( null == _delisted ) ) {
            return true;    
        }

        // if there is only one delisted resource then yes
        if ( ( null != _delisted ) && 
             ( 1 == _delisted.length ) && 
             ( null == _enlisted ) ) {
            return true;    
        }

        // if the same resource manager is used for all the xa
        // resources then yes
        if ( null != _enlisted ) {
            if ( !areXaResourcesShared( _enlisted, false ) ) {
                return false;
            }
            if ( null != _delisted ) {
                return areXaResourcesShared( _delisted, true );    
            }

            return true;
        }

        if ( null != _delisted ) {
            return areXaResourcesShared( _delisted, false );    
        }
        
        return true;
    }


    /**
     * Return true if all the resources in the specified
     * xa resource holder list are shared.
     *
     * @param xaResourceHolders the xa resource holder list. Assumed
     *      not to be null.
     * @param isPreviouslyShared true if other xa resources are
     *      shared.
     * @return true if all the resources in the specified
     *      xa resource holder list are shared.
     */
    private boolean areXaResourcesShared(XAResourceHolder[] xaResourceHolders, 
                                         boolean isPreviouslyShared)
    {
        // true if an xa res in the specified xa resource
        // list is not shared
        // we can initialize the value of the variable to 
        // value of isPreviouslyShared but that dirties 
        // the logic a bit
        boolean isUnshared = false;

        for ( int i = 0 ; i < xaResourceHolders.length ; i++ ) {
            if ( !xaResourceHolders[ i ].shared ) {
                // we found another xa res that is not shared
                // with others
                if ( isUnshared || isPreviouslyShared) {
                    return false;    
                }

                isUnshared = true;
            }
        }

        return true;
    }


    /**
     * Perform one-phase commit on the transaction
     *
     * @throws RollbackException Thrown to indicate that the transaction has been rolled back rather than committed.
     * @throws HeuristicMixedException Thrown to indicate that a heuristic decision was made and that some relevant updates have been committed while others have been rolled back.
     * @throws HeuristicRollbackException Thrown to indicate that a heuristic decision was made and that some relevant updates have been rolled back.
     * @throws SecurityException Thrown to indicate that the thread is not allowed to commit the transaction.
     * @throws IllegalStateException Thrown if the current thread is not associated with a transaction.
     * @throws SystemException Thrown if the transaction manager encounters an unexpected error condition
     */
    public synchronized void onePhaseCommit()
        throws  RollbackException,
                HeuristicMixedException,
                HeuristicRollbackException,
                SecurityException,
                IllegalStateException,
                SystemException
    {
        // --------------------------------
    	// General state checks
    	// --------------------------------
        // Proper notification for transactions that timed out.
    	if ( _timedOut )
    	    throw new RollbackException( Messages.message( "tyrex.tx.timedOut" ) );
    
    	// If this is a subtransaction, it cannot commit directly,
    	// only once the parent transaction commits through the
    	// {@link #internalCommit} call. We simply do nothing after
    	// preperation. The heuristic decision will be remembered.
    	if ( _parent != null || _pgContext != null )
    	    return;

        commit( true );

    }

    public synchronized void commit()
	    throws  RollbackException, HeuristicMixedException, HeuristicRollbackException,
	            SecurityException, SystemException
    {
        // --------------------------------
    	// General state checks
    	// --------------------------------
        // Proper notification for transactions that timed out.
    	if ( _timedOut )
    	    throw new RollbackException( Messages.message( "tyrex.tx.timedOut" ) );
    
    	// If this is a subtransaction, it cannot commit directly,
    	// only once the parent transaction commits through the
    	// {@link #internalCommit} call. We simply do nothing after
    	// preperation. The heuristic decision will be remembered.
    	if ( _parent != null || _pgContext != null )
    	    return;

        commit( canUseOnePhaseCommit() );
    }


    /**
     * Commit transaction
     *
     * @param canUseOnePhaseCommit True if one-phase commit is used.
     * @throws RollbackException Thrown to indicate that the transaction has been rolled back rather than committed.
     * @throws HeuristicMixedException Thrown to indicate that a heuristic decision was made and that some relevant updates have been committed while others have been rolled back.
     * @throws HeuristicRollbackException Thrown to indicate that a heuristic decision was made and that some relevant updates have been rolled back.
     * @throws SecurityException Thrown to indicate that the thread is not allowed to commit the transaction.
     * @throws IllegalStateException Thrown if the current thread is not associated with a transaction.
     * @throws SystemException Thrown if the transaction manager encounters an unexpected error condition
     */
    private void commit( boolean canUseOnePhaseCommit )
	    throws  RollbackException, HeuristicMixedException, HeuristicRollbackException,
	            SecurityException, SystemException
    {
        //RM
        //System.out.println("TransactionImpl:using 1PC " + canUseOnePhaseCommit);
        
        try {
        	_txDomain.notifyCommit( _xid );

        	if (!canUseOnePhaseCommit) {
                // This is two phase commit. Notify the domain about request
            	// to commit transaction which might result in RollbackException.
            	// If succeeded, attempt to prepare transaction and act based
            	// no the return heuristic.
                prepare();
            }
            else {
                endResources();
            }
        } catch ( RollbackException except ) {
        	    _heuristic = Heuristic.Rollback;
        }
        
        // flag to tell forget what heuristic exceptions to look for
        switch ( _heuristic ) {
            case Heuristic.ReadOnly:
                try {
                    // Read only resource does not need either commit, nor rollback
                    _txDomain.notifyCompletion( _xid, _heuristic );
                    _status = STATUS_COMMITTED;
                    break;
                } finally { 
                    // The transaction will now tell all it's resources to
                    // forget about the transaction and will release all
                    // held resources. Also notifies all the synchronization
                    // objects that the transaction has completed with a status.
        
                    try {
                        forgetReadOnly();
                    } catch ( IllegalStateException e ) { }
                }

            case Heuristic.Rollback:
            case Heuristic.Mixed:
            case Heuristic.Hazard:
            case Heuristic.Other:
            case Heuristic.Unknown:
            
                try {
                    // Transaction must be rolled back and an exception thrown to
                    // that effect.
                    _status = STATUS_MARKED_ROLLBACK;
                    internalRollback();
                    throw new HeuristicRollbackException( Messages.message( "tyrex.tx.rolledback" ) );
                } finally { 
                    // The transaction will now tell all it's resources to
                    // forget about the transaction and will release all
                    // held resources. Also notifies all the synchronization
                    // objects that the transaction has completed with a status.
        
                    try {
                        forgetOnRollback();
                    } catch ( IllegalStateException e ) { }
                }
        
            case Heuristic.Commit:
            default:
                try {
                    internalCommit(canUseOnePhaseCommit);
                } finally { 
                    // The transaction will now tell all it's resources to
                    // forget about the transaction and will release all
                    // held resources. Also notifies all the synchronization
                    // objects that the transaction has completed with a status.
                    try {
                    forgetOnCommit();
                    } catch ( IllegalStateException e ) { }    
                }
        }
         
        
        // If an error has been encountered during 2pc,
        // we must report it accordingly. I believe this
        // supercedes reporting a system error;
        switch ( _heuristic ) {
        case Heuristic.Rollback:
            // Transaction was completed rolled back at the
            // request of one of it's resources. We don't
            // get to this point if it has been marked for
            // roll back.
            throw new HeuristicRollbackException( Messages.message( "tyrex.tx.heuristicRollback" ) );
        case Heuristic.Mixed:
            // Transaction has partially commited and partially
            // rolledback.
            throw new HeuristicMixedException( Messages.message( "tyrex.tx.heuristicMixed" ) );
        case Heuristic.Hazard:
            throw new HeuristicMixedException( Messages.message( "tyrex.tx.heuristicHazard" ) );
        case Heuristic.Other:
        case Heuristic.Unknown:
            // if there is a system error throw it
            if ( _sysError != null )
                throw _sysError;
            else
                throw new SystemException("Unknown exception occurred");
        case Heuristic.Commit:
        default:
            // Transaction completed successfuly, even if
            // a resource insisted on commiting it.
            // If a system error occured during this stage, report it.
            if ( _sysError != null )
            throw _sysError;
            break;
        }
    }


    public synchronized void rollback()
	throws IllegalStateException, SystemException
    {
	int                i;
	XAResourceHolder xaRes;

	// --------------------------------
	// General state checks
	// --------------------------------

	// Proper notification for transactions that timed out.
	if ( _timedOut )
	    throw new IllegalStateException( Messages.message( "tyrex.tx.timedOut" ) );

	// Perform the rollback, pass IllegalStateException to
	// the caller, ignore the returned heuristics.
	try {
        _txDomain.notifyRollback( _xid );
	    internalRollback();
    }
    finally {
        // The transaction will now tell all it's resources to
    	// forget about the transaction and will release all
    	// held resources. Also notifies all the synchronization
    	// objects that the transaction has completed with a status.
    	try {
    	    forgetOnRollback();
    	} catch ( IllegalStateException e ) { }
    }
	
	// If this transaction is nested inside a parent transaction,
	// then we have just rolledback a section in the larger transaction,
	// but the larger transaction may still complete. We dissociate
	// this sub-transaction from it's parent.
	if ( _parent != null )
	    _parent.unregisterResource( new ResourceImpl( this ) );

	// If a system error occured during this stage, report it.
	if ( _sysError != null )
	    throw _sysError;

    }


    /**
     * Called to prepare the resource as part of the two phase commit protocol.
     * On entry the status must be either {@link #STATUS_ACTIVE) or {@link
     * #STATUS_MARKED_ROLLBACK).
     * <p>
     * All enlisted resources are notified that the transaction has ended,
     * and are they asked to prepare it. If a resource succeeds we will commit
     * it (unless we decide to rollback the entire transaction). If a resource
     * is read-only (or shared) we will mark it as read-only. If at least one
     * resource fails to prepare, or any other error is encountered, we stop
     * preparation and return a heuristic decision.
     * <p>
     * The heuristic decision can be any of the following:
     * <ul>
     * <li>{@link #Heuristic.ReadOnly} There were no resources for this
     * transaction, or all resources are read only -- there is no need to
     * commit/rollback this transaction
     * <li>{@link #Heuristic.Commit} All resources are prepared and those
     * with a false {@link XAResourceHolder#readOnly} need to be commited.
     * <li>{@link #Heuristic.Rollback} The transaction has been marked for
     * rollback, an error has occured or at least one resource failed to
     * prepare and there were no resources that commited
     * <li>{@link #Heuristic.Mixed} Some resources have already commited,
     * others have either rolledback or failed to commit, or we got an
     * error in the process: all resources must be rolledback
     * </ul>
     *
     * @throws IllegalStateException The transaction is not active or
     *   is in the process of being commited
     */
    void prepare()
	throws IllegalStateException, RollbackException
    {
	XAResourceHolder xaRes;
	int                i;
	int                committing;
	Resource           resource;

	// Proper notification for transactions that timed out.
	if ( _timedOut )
	    throw new RollbackException( Messages.message( "tyrex.tx.timedOut" ) );

	// Check the status of the transaction and act accordingly.
	switch ( _status ) {
	case STATUS_ACTIVE:
	    // Transaction is active, we'll commit it.
	    break;

	case STATUS_PREPARED:
	    // If this transaction has been prepared before, we do not
	    // prepare it a second time. It would have been prepared if this
	    // is a subtransaction that was commited directly and is now
	    // being commited by the parent transaction. The heuristic
	    // decision is remembered from the previous preparation.
	    return;

	case STATUS_COMMITTING:
	case STATUS_PREPARING:
	    // Transaction is in the middle of being commited.
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inCommit" ) );
	    
	case STATUS_MARKED_ROLLBACK:
	    // Transaction has been marked for roll-back, no preparation
	    // necessary.
	    _heuristic = Heuristic.Rollback;
	    return;
	    
	case STATUS_ROLLEDBACK:
	    // Transaction has been or is being rolled back.
	    throw new RollbackException( Messages.message( "tyrex.tx.rolledback" ) );

	case STATUS_ROLLING_BACK:
	    // Transaction has been or is being rolled back.
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inRollback" ) );
	    
	case STATUS_COMMITTED:
	case STATUS_NO_TRANSACTION:
	case STATUS_UNKNOWN:
	default:
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
	}

    securityCheck();
    
    // We begin by having no heuristics at all, but during
	// the process we might reach a conclusion to have a
	// commit or rollback heuristic.
	_heuristic = Heuristic.ReadOnly;
	_status = STATUS_PREPARING;
	committing = 0;

	beforeCompletion();
    
	// We deal with OTS (remote transactions and subtransactions)
	// first because we expect a higher likelyhood of failure over
	// there, and we can easly recover over here.
	if ( _resources != null ) {
	    // We are starting with a heuristic decision that is read-only,
	    // and no need to commit. At the end we might reach a heuristic
	    // decision to rollback, or mixed/hazard, but never commit.

	    for ( i = 0 ; i < _resources.length ; ++i ) {

		// If at least one resource failed to prepare, we will
		// not prepare the remaining resources, but rollback.
		if ( _heuristic != Heuristic.ReadOnly && _heuristic != Heuristic.Commit )
		    break;

		resource = _resources[ i ];
		if ( resource == null )
		    continue;
		try {
		    int vote;

		    vote = resource.prepare().value();
		    if ( vote == Vote._VoteReadOnly ) {
			// The resource is read-only, no need to commit/
			// rollback or even forget this one.
			_resources[ i ] = null;
		    } else if ( vote == Vote._VoteCommit ) {
			// If at least one resource decided to commit,
			// we still have no heuristic decision, but at
			// least we can commit.
			++ committing;
		    } else if ( vote == Vote._VoteRollback ) {
			// Our heuristic so far can only be read-only,
			// so the only decision was can make is to
			// rollback. We do not need to rollback this
			// resource or forget it.
			_heuristic = _heuristic | Heuristic.Rollback;
			_resources[ i ] = null;
		    }
		} catch ( HeuristicMixed except ) {
		    // Resource indicated mixed/hazard heuristic, so the
		    // entire transaction is mixed heuristics.
		    _heuristic = _heuristic | Heuristic.Mixed;
		} catch ( HeuristicHazard except ) {
		    _heuristic = _heuristic | Heuristic.Hazard;
		} catch ( Exception except ) {
		    if ( except instanceof TRANSACTION_ROLLEDBACK )
			_heuristic = _heuristic | Heuristic.Rollback;
		    else {
			_heuristic = _heuristic | Heuristic.Unknown;
			error( except );
		    }
		}
	    }	
	}

    // If there are any resources, perform two phase commit on them.
	// We always end these resources, even if we made a heuristic
	// decision not to commit this transaction.
	if ( _enlisted != null ) {

	    endEnlistedResourcesForCommit();

	    // Prepare all the resources that we are about to commit.
	    // Shared resources do not need preparation, they will not
	    // be commited/rolledback directly. Read-only resources
	    // will not be commited/rolled back.
	    for ( i = 0 ; i < _enlisted.length ; ++i ) {

		// If at least one resource failed to prepare, we will
		// not prepare the remaining resources, but rollback.
		if ( _heuristic != Heuristic.ReadOnly && _heuristic != Heuristic.Commit )
		    break;

		xaRes = _enlisted[ i ];
		if ( xaRes == null )
		    continue;
		try {
		    if ( ! xaRes.shared ) {
			// We do not commit/rollback a read-only resource.
			// If all resources are read only, we can return
			// a read-only heuristic.
			if ( xaRes.xa.prepare( xaRes.xid ) == XAResource.XA_RDONLY )
                xaRes.readOnly = true;
            else
			    ++ committing;
		    }
			
		    // Note: We will not commit read-only resources,
		    // but if we get a heuristic that requires rollback,
		    // we will rollback all resources including this one.
		    // An error does not change it's state to read-only,
		    // since we can call rollback more than once.
		} catch ( XAException except ) {
            xaExceptionOccurred( except );
		}  catch ( Exception except ) {
		    // Any error will cause us to rollback the entire
		    // transaction or at least the remaining part of it.
		    _heuristic = _heuristic | Heuristic.Unknown;
		    error( except );
		}
	    }
	}

    if ( null != _delisted ) {
        // Prepare all the resources that we are about to commit.
	    // Shared resources do not need preparation, they will not
	    // be commited/rolledback directly. Read-only resources
	    // will not be commited/rolled back.
	    for ( i = 0 ; i < _delisted.length ; ++i ) {

		// If at least one resource failed to prepare, we will
		// not prepare the remaining resources, but rollback.
		if ( _heuristic != Heuristic.ReadOnly && _heuristic != Heuristic.Commit )
		    break;

		xaRes = _delisted[ i ];
		if ( xaRes == null )
		    continue;
		try {
		    if ( ! xaRes.shared ) {
			// We do not commit/rollback a read-only resource.
			// If all resources are read only, we can return
			// a read-only heuristic.
			if ( xaRes.xa.prepare( xaRes.xid ) == XAResource.XA_RDONLY )
                xaRes.readOnly = true;
            else
			    ++ committing;
		    }
			
		    // Note: We will not commit read-only resources,
		    // but if we get a heuristic that requires rollback,
		    // we will rollback all resources including this one.
		    // An error does not change it's state to read-only,
		    // since we can call rollback more than once.
		} catch ( XAException except ) {
            xaExceptionOccurred( except );
		}  catch ( Exception except ) {
		    // Any error will cause us to rollback the entire
		    // transaction or at least the remaining part of it.
		    _heuristic = _heuristic | Heuristic.Unknown;
		    error( except );
		}
	    }    
    }

	_status = STATUS_PREPARED;
	// We make a heuristic decision to commit only if we made no other
	// heuristic decision during perparation and we have at least
	// one resource interested in committing.
	if ( _heuristic == Heuristic.ReadOnly  && committing > 0 )
	    _heuristic = Heuristic.Commit;
	else
	    _heuristic = normalize( _heuristic );
    }


    /**
     * Check whether current thread is owner of this transaction,
     * or a special thread that is allowed to commit/rollback the
     * transaction. Must be performed after checking whether the
     * transaciton is active. Inactive transactions have no owners.
     *
     * @throws SecurityException if the current thread is not allowed
     *      to commit/rollback this tranasaction.
     */
    private void securityCheck()
        throws SecurityException
    {
        if ( ! _txDomain.isOwner( getTopLevel(), Thread.currentThread() ) ) {
            throw new SecurityException( Messages.message( "tyrex.tx.threadNotOwner" ) );
        }
    }
    

    /**
     * If this transaction is not the current transaction
     * of the current thread then make this transaction
     * the transaction of the current thread and return
     * the original transaction of the current thread. Else
     * if transaction is the current transaction do nothing
     * and return null.
     *
     * @return return the transaction of the current thread
     *      if this transaction is not the current transaction
     *      of the current thread. Else return null.
     */
    private Transaction makeCurrentTransactionIfNecessary()
    {
        return (Transaction)AccessController.doPrivileged(new PrivilegedAction()
                {
                    public java.lang.Object run() 
                    {
                    Transaction result = null;
                    try {
                    if ( _txDomain.getTransactionManager().getTransaction() !=  TransactionImpl.this ) {
                        result = _txDomain.getTransactionManager().suspend();
                        // could move this out of priviledged block
                        // to make priviledged block smaller. not
                        // a big deal
                        _txDomain.internalGetTransactionManager().internalResume( TransactionImpl.this, false );    
                    }
                    } catch ( Exception except ) {
                    _status = STATUS_MARKED_ROLLBACK;
                    error( except );
                    }
                    return result;
                    }
            });
    }


    /**
     * Make the specified transaction the current 
     * transaction of the current thread.
     *
     * @param transaction the transaction to make as
     *      the current transaction of the current thread.
     */
    private void resumeTransaction( final Transaction transaction )
    {
        // do in one priveledged block
        AccessController.doPrivileged(new PrivilegedAction()
        {
            public java.lang.Object run() 
            {
            try {
            _txDomain.getTransactionManager().suspend();
            } catch ( Exception except ) {
                error( except );
            }
            try {
                _txDomain.getTransactionManager().resume( transaction );
            } catch ( Exception except ) {
                _status = STATUS_MARKED_ROLLBACK;
                error( except );
            }
            return null;
            }
        });
    }

    /**
     * Inform the Synchronization objects that the transaction
     * completion process is about to start.
     */
    private void beforeCompletion()

    {
    // First, notify all the synchronization objects that
	// we are about to complete a transaction. They might
	// decide to roll back the transaction, in which case
	// we'll do a rollback.
	// might decide to rollback the transaction.
	if ( _syncs != null ) {
        Synchronization    sync;
        int                i; 
        
        // If we are not running in the same thread as
        // this transaction, need to make this transaction
        // the current one before calling method.
        // do in a priveledged block
        Transaction suspended = makeCurrentTransactionIfNecessary();
    
        // Do not notify of completion if we already
        // decided to roll back the transaction.
        if ( _status != STATUS_MARKED_ROLLBACK ) {
            for ( i = _syncs.length ; --i >= 0  ; ) {
            sync = _syncs[ i ];
            try {
                sync.beforeCompletion();
            } catch ( Exception except ) {
                error( except );
                _status = STATUS_MARKED_ROLLBACK;
            }
            }
        }
        
        // Resume the previous transaction associated with
        // the thread.
        if ( suspended != null ) {
            resumeTransaction( suspended );
        }


        if ( _status == STATUS_MARKED_ROLLBACK ) {
        // Status was changed to rollback or an error occured,
        // either case we have a heuristic decision to rollback.
        _heuristic = Heuristic.Rollback;
        return;
        }
    }
    }


    /**
     * If there any enlisted resources end them with
     * with the flag XAResource.TMSUCCESS as the resources
     * are to be committed.
     * <P>
     * If an exception occurs set the system error and
     * add {@link Heuristic.Rollback} to the current
     * heuristic. The heuristic is not normalized.
     * @see #_enlisted
     * @see #_heuristic
     * @see #error
     */
    private void endEnlistedResourcesForCommit()
    {
    // We always end these resources, even if we made a heuristic
	// decision not to commit this transaction.
	if ( _enlisted != null ) {
        // Tell all the XAResources that their transaction
	    // has ended successfuly. 
	    for ( int i = 0 ; i < _enlisted.length ; ++i ) {
		try {
            endForTransactionBoundary( _enlisted[ i ] );
        } catch ( Exception except ) {
            // Error occured, we won't be commiting this transaction.
            _heuristic |= Heuristic.Rollback;
            error( except );
        }
        }
	}
    }


    /**
     * End the work performed by the specified xa resource
     *  successfully for a transaction boundary ie commit or rollback
     *
     * @param xaRes the xa resource holder
     * @throws XAException if there is a problem ending
     *      the work
     * @throws SystemException if the xa resource is not
     *      in the proper state for its work to be ended.
     */
    private void endForTransactionBoundary( XAResourceHolder xaRes )
        throws SystemException, XAException
    {
        if ( ( xaRes.endFlag == XAResource.TMNOFLAGS ) || 
             ( xaRes.endFlag == XAResource.TMSUSPEND ) ) {
            if (xaRes.endFlag == XAResource.TMSUSPEND) {
                XAResourceHelperManager.getHelper( xaRes.xa ).endSuspended( xaRes.xa, xaRes.xid );
            } else {
                xaRes.xa.end( xaRes.xid, XAResource.TMSUCCESS );
            }
            xaRes.endFlag = XAResource.TMSUCCESS;
        } else if ( xaRes.endFlag != XAResource.TMSUCCESS ) {
            throw new SystemException( "XA resource is not in the proper state to be ended" );
        }

    }
     
    /**
     * Called to end the resources as part of the one phase commit protocol.
     * On entry the status must be either {@link #STATUS_ACTIVE) or {@link
     * #STATUS_MARKED_ROLLBACK).
     * <p>
     * The heuristic decision can be any of the following:
     * <ul>
     * <li>{@link #Heuristic.Commit} All resources are ended successfully
     * need to be commited using one phase commit on the resources.
     * <li>{@link #Heuristic.Rollback} The transaction has been marked for
     * rollback, an error has occured or at least one resource failed to
     * end
     * </ul>
     *
     * @throws IllegalStateException The transaction is not active or
     *   is in the process of being commited, or prepared for two phase
     *   commit.
     */
    void endResources()
	throws IllegalStateException, RollbackException
    {
	XAResourceHolder xaRes;
	int                i;
	int                committing;
	Resource           resource;
    
    // Proper notification for transactions that timed out.
	if ( _timedOut )
	    throw new RollbackException( Messages.message( "tyrex.tx.timedOut" ) );

	// Check the status of the transaction and act accordingly.
	switch ( _status ) {
	case STATUS_ACTIVE:
	    // Transaction is active, we'll commit it.
	    break;

	case STATUS_PREPARED:
	    // logic error by user
        throw new IllegalStateException( Messages.message( "tyrex.tx.inOnePhaseCommit" ) );
	    

	case STATUS_COMMITTING:
	case STATUS_PREPARING:
	    // Transaction is in the middle of being commited.
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inCommit" ) );
	    
	case STATUS_MARKED_ROLLBACK:
	    // Transaction has been marked for roll-back, no preparation
	    // necessary.
	    _heuristic = Heuristic.Rollback;
	    return;
	    
	case STATUS_ROLLEDBACK:
	    // Transaction has been or is being rolled back.
	    throw new RollbackException( Messages.message( "tyrex.tx.rolledback" ) );

	case STATUS_ROLLING_BACK:
	    // Transaction has been or is being rolled back.
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inRollback" ) );
	    
	case STATUS_COMMITTED:
	case STATUS_NO_TRANSACTION:
	case STATUS_UNKNOWN:
	default:
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
	}

    securityCheck();
    
    // If we are not running in the same thread as
    // this transaction, need to make this transaction
    // the current one before calling method.
    // do in a priveledged block
    Transaction suspended = makeCurrentTransactionIfNecessary();
    
    // We begin by having no heuristics at all, but during
	// the process we might reach a conclusion to have a
	// commit or rollback heuristic.
    _status = STATUS_COMMITTING;
    _heuristic = Heuristic.ReadOnly;

    try {
        beforeCompletion();
    } finally {
        // Resume the previous transaction associated with
        // the thread.
        if ( suspended != null ) {
            resumeTransaction( suspended );
        }
    }
    
    // We always end these resources, even if we made a heuristic
	// decision not to commit this transaction.
	if ( _enlisted != null ) {
        endEnlistedResourcesForCommit();
    }
	// if the heuristic has not changed set it to commit
    if ( _heuristic == Heuristic.ReadOnly ) {
        _heuristic = Heuristic.Commit;    
    }
    else {
        _heuristic = normalize( _heuristic );
    }
    }


    /**
     * Modify the current heuristic decision of the transaction according
     * to data from the specified exception that occurred.
     * <p>
     * The heuristic is not normalized.
     * 
     * @param except the XAException that occurred
     */
    private void xaExceptionOccurred( XAException except )
    {
        //RM
        //Debug.printXAException(except );
    if ( except.errorCode == XAException.XA_HEURMIX )
        _heuristic = _heuristic | Heuristic.Mixed; 
    else if ( except.errorCode == XAException.XA_HEURHAZ )
        _heuristic = _heuristic | Heuristic.Hazard; 
    else if ( except.errorCode == XAException.XA_RDONLY) {
        ; // ignore    
    }
    //else if ( ( except.errorCode == XAException.XA_HEURRB ||
    //	except.errorCode == XAException.XA_RBTIMEOUT ) )
    //_heuristic = _heuristic | Heuristic.Rollback;
    else if ( except.errorCode >= XAException.XA_RBBASE &&
              except.errorCode <= XAException.XA_RBEND )
        _heuristic = _heuristic | Heuristic.Rollback; // Heuristic.Hazard
    else if ( except.errorCode == XAException.XA_HEURCOM )
        _heuristic = _heuristic | Heuristic.Commit;
    else {
        // Any error will cause us to rollback the entire
        // transaction or at least the remaining part of it.
        _heuristic = _heuristic | Heuristic.Other;
        error( except );
    }
    }

    /**
     * Performs the second part of the two phase commit, after a call
     * to {@link #prepare} returned a heuristic decision to commit.
     * Will attempt to commit on all the resources that are not read-only.
     * The end result is described in a heuristic decision. Multiple
     * calls are supported.
     * <p>
     * The heuristic decision can be any of the following:
     * <ul>
     * <li>{@link #Heuristic.Commit} All resources were commited
     * successfuly
     * <li>{@link #Heuristic.Rollback} No resources were commited
     * successfuly, all resources were rolledback successfuly
     * <li>{@link #Heuristic.Mixed} Some resources have commited,
     * others have rolled back
     * </ul>
     *
     * @param onePhaseCommit True if one phase commit is to be used
     * @throws IllegalStateException Transaction has not been prepared
     */
    void internalCommit(boolean onePhaseCommit)
	throws IllegalStateException
    {
	XAResourceHolder xaRes;
	int                i;
	Resource           resource;

    // If already committed we just return. The previous heuristic
	// is still remembered.
	if ( _status == STATUS_COMMITTED || _status == STATUS_ROLLEDBACK )
        return;
    

	// We should never reach this state unless transaction has been
	// prepared first.
	if ( ( !onePhaseCommit && ( _status != STATUS_PREPARED ) ) ||
         ( onePhaseCommit && ( _status != STATUS_COMMITTING ) ) )
	    throw new IllegalStateException( Messages.message( "tyrex.tx.notPrepared" ) );
    
	// 2PC: Phase two - commit

	// Transaction has been prepared fully or partially.
	// If at least one resource requested roll back (or
	// indicated mixed heuristics) we'll roll back all resources.
	// We start as read-only until at least one resource indicates
	// it actually commited.
	_status = STATUS_COMMITTING;
	_heuristic = Heuristic.ReadOnly;

	// We deal with OTS (remote transactions and subtransactions)
	// first because we expect a higher likelyhood of failure over
	// there, and we can easly recover over here.
	if ( _resources != null ) {
	    for ( i = 0 ; i < _resources.length ; ++i ) {
		resource = _resources[ i ];
		if ( resource == null )
		    continue;
		try {
            if (onePhaseCommit) {
                resource.commit_one_phase();
            }
            else {
                resource.commit();
            }
		    
		    // At least one resource commited, we are either
		    // commit or mixed.
		    _heuristic = _heuristic | Heuristic.TX_COMMITTED;
        } catch ( HeuristicMixed except ) {
		    _heuristic = _heuristic | Heuristic.Mixed;
		} catch ( HeuristicHazard except ) {
		    _heuristic = _heuristic | Heuristic.Hazard;
		} catch ( HeuristicRollback except ) {
		    _heuristic = _heuristic | Heuristic.Rollback;
		} catch ( Exception except ) {
		    if ( except instanceof TRANSACTION_ROLLEDBACK )
			_heuristic = _heuristic | Heuristic.Rollback;
		    else {
			_heuristic = _heuristic | Heuristic.Unknown;
			error( except );
		    }
		}
	    }
	}
    if (  _enlisted != null ) {
	    commitXAResources( _enlisted, onePhaseCommit );
	}

    if ( _delisted != null) {
        commitXAResources( _delisted, onePhaseCommit );    
    }
	 
	_status = STATUS_COMMITTED;
    
	_heuristic = normalize( _heuristic );
	_txDomain.notifyCompletion( _xid, _heuristic );
    }


    /**
     * Called to commit the specified xa resources.
     *
     * @param xaResources the list of xa resource holders
     * @param onePhaseCommit True if the xa resources are 
     *      to be committed using one phase commit
     */
    private void commitXAResources(XAResourceHolder[] xaResources, boolean onePhaseCommit)
    {
        XAResourceHolder xaRes;

        for ( int i = 0 ; i < xaResources.length ; ++i ) {
		xaRes = xaResources[ i ];
		try {      
            // Shared resources and read-only resources
		    // are not commited.
		    if ( ! xaRes.shared && ! xaRes.readOnly ) {
            xaRes.xa.commit( xaRes.xid, onePhaseCommit );
            // At least one resource commited, we are either
			// commit or mixed.
			_heuristic = _heuristic | Heuristic.Commit;
		    }
		} catch ( XAException except ) {
		    xaExceptionOccurred( except );
		} catch ( Exception except ) {
		    // Any error will cause us to rollback the entire
		    // transaction or at least the remaining part of it.
		    _heuristic = _heuristic | Heuristic.Unknown;
		    error( except );
		}
	    }
    }

    /**
     * Called to perform the actual rollback on the transaction.
     * Will force a rollback on all the enlisted resources and
     * delisted resources and return
     * the heuristic decision of the rollback. Multiple calls are
     * supported.
     * <p>
     * The heuristic decision can be any of the following:
     * <ul>
     * <li>{@link #Heuristic.ReadOnly} There were no resources for this
     * transaction, or all resources are read only -- there is no need to
     * commit/rollback this transaction
     * <li>{@link #Heuristic.Commit} All resources have decided to commit
     * <li>{@link #Heuristic.Rollback} All resources have rolled back
     * (except for read-only resources)
     * <li>{@link #Heuristic.Mixed} Some resources have already commited,
     * others have rolled back
     * </ul>
     *
     * @throws IllegalStateException The transaction is not active
     */
    void internalRollback()
    {
	int                i;
	XAResourceHolder xaRes;
	Resource           resource;

	// --------------------------------
	// General state checks
	// --------------------------------

	// Check the status of the transaction and act accordingly.
	switch ( _status ) {
	case STATUS_ACTIVE:
	case STATUS_MARKED_ROLLBACK:
	    // Transaction is active, we'll roll it back.
	    break;

	case STATUS_PREPARED:
	case STATUS_PREPARING:
	case STATUS_COMMITTING:
	    // Transaction is right now in the process of being commited.
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inCommit" ) );

	case STATUS_ROLLING_BACK:
	    // Transaction has been or is being rolled back, just leave.
	    _heuristic = Heuristic.Rollback;
	    return;
	    
	case STATUS_NO_TRANSACTION:
	case STATUS_UNKNOWN:
	default:
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );

	case STATUS_COMMITTED:
	case STATUS_ROLLEDBACK:
	    // If already rolled back or committed we just return.
	    // The previous heuristic is still remembered.
	    return;
	}

    securityCheck();

	// If we got to this point, we'll start rolling back the
	// transaction. Change the status immediately, so the
	// transaction cannot be altered by a synchronization
	// or XA resource. Our initial heuristic is read-only,
	// since unless there's at least one rollback resource,
	// we never truely rollback.
	_status = STATUS_ROLLING_BACK;
	_heuristic = Heuristic.ReadOnly;

    beforeCompletion();

	if ( _resources != null ) {
	    // Tell all the OTS resources to rollback their transaction
	    // regardless of state.
	    for ( i = 0 ; i < _resources.length ; ++i ) {
		resource = _resources[ i ];
		if ( resource == null )
		    continue;
		try {
		    resource.rollback();
		    // Initially we're readonly so we switch to rollback.
		    // If we happen to be in commit, we switch to mixed.
		    _heuristic = _heuristic | Heuristic.TX_ROLLEDBACK;
		    _resources[ i ] = null;
		} catch ( HeuristicMixed except ) {
		    _heuristic = _heuristic | Heuristic.Mixed;
		} catch ( HeuristicHazard except ) {
		    _heuristic = _heuristic | Heuristic.Hazard;
		} catch ( HeuristicCommit except ) {
		    _heuristic = _heuristic | Heuristic.Commit;
		} catch ( Exception except ) {
		    _heuristic = _heuristic | Heuristic.Unknown;
		    error( except );
		}
	    }
	}


	if ( _enlisted != null ) {
	    // Tell all the XAResources that their transaction
	    // has ended with a failure.
	    for ( i = 0 ; i < _enlisted.length ; ++i ) {
		try {
            endForTransactionBoundary( _enlisted[ i ] );
        } catch ( XAException except ) {
            xaExceptionOccurred( except );
        } catch ( Exception except ) {
            _heuristic = _heuristic | Heuristic.Unknown;
            error( except );
        }
        }
	    rollbackXAResources(_enlisted);
	}

    if ( _delisted != null) {
        rollbackXAResources( _delisted );
    }

	_status = STATUS_ROLLEDBACK;
	_heuristic = normalize( _heuristic );
	_txDomain.notifyCompletion( _xid, _heuristic );
    }


    /**
     * Called to rollback the specified xa resources.
     *
     * @param xaResourceHolders the array of xa resource holders
     */
    private void rollbackXAResources(XAResourceHolder[] xaResourceHolders)
    {
        XAResourceHolder xaResourceHolder;

        // Rollback each of the resources, regardless of
	    // error conditions. Shared resources do not require
	    // rollback.
	    for ( int i = 0 ; i < xaResourceHolders.length ; ++i ) {
		xaResourceHolder = xaResourceHolders[ i ];
		try {
		    if ( ! xaResourceHolder.shared && ! xaResourceHolder.readOnly ) {
			xaResourceHolder.xa.rollback( xaResourceHolder.xid );
			// Initially we're readonly so we switch to rollback.
			// If we happen to be in commit, we switch to mixed.
			_heuristic = _heuristic | Heuristic.Rollback;
		    }
		} catch ( XAException except ) {
		    xaExceptionOccurred( except );
		} catch ( Exception except ) {
		    _heuristic = _heuristic | Heuristic.Unknown;
            error( except );
		}
    }
    }

    /**
     * Called to forget about the transaction if the transaction
     * only contains read only resource. This method servers two purposes.
     * First, it will notify all
     * the synchronziation objects that the transaction has completed
     * with the transaction's status. Last, it will release any
     * objects held by the transaction and dissocaite it from the
     * list of available transactions.
     * 
     * @throws IllegalStateException The transaction has not commited
     *   or rolledback yet
     *
     */
    void forgetReadOnly()
	throws IllegalStateException
    {
    forget(Heuristic.ReadOnly);
    }



    /**
     * Called to forget about the transaction at the end of
     * a commit. This method servers three purposes.
     * First, it will tell all the resources to forget about the
     * transaction and release them if the appropriate heuristic 
     * exception occurred. Second, it will notify all
     * the synchronziation objects that the transaction has completed
     * with the transaction's status. Last, it will release any
     * objects held by the transaction and dissocaite it from the
     * list of available transactions.
     * 
     * @throws IllegalStateException The transaction has not commited
     *   or rolledback yet
     *
     * @see HeuristicExceptions
     */
    void forgetOnCommit()
	throws IllegalStateException
    {
    forget(Heuristic.Commit);
    }

    /**
     * Called to forget about the transaction at the end of
     * a commit. This method servers three purposes.
     * First, it will tell all the resources to forget about the
     * transaction and release them if the appropriate heuristic 
     * exception occurred. Second, it will notify all
     * the synchronziation objects that the transaction has completed
     * with the transaction's status. Last, it will release any
     * objects held by the transaction and dissocaite it from the
     * list of available transactions.
     * 
     * @throws IllegalStateException The transaction has not commited
     *   or rolledback yet
     *
     * @see HeuristicExceptions
     */
    void forgetOnRollback()
	throws IllegalStateException
    {
    forget(Heuristic.Rollback);
    }

    /**
     * Called to forget about the transaction at the end of either
     * a commit or rollback. This method servers three purposes.
     * First, it will tell all the resources to forget about the
     * transaction and release them if the appropriate heuristic 
     * exception occurred. Second, it will notify all
     * the synchronziation objects that the transaction has completed
     * with the transaction's status. Last, it will release any
     * objects held by the transaction and dissocaite it from the
     * list of available transactions.
     * <P>
     * The ignoreHeuristic argument is used to determine what heuristic
     * outcomes cause forget to be called on the resources associated
     * with the transaction. For instance if transaction commit has been called
     * and the outcome is Heuristic.Commit then resource forget should 
     * be not be called. In this case resource forget should be called
     * on all other heuristic outcomes like Heuristic.Rollback
     * for instance. Similarly if transaction rollback has been called
     * and the outcome is Heuristic.Rollback then resource forget should
     * not be called. In this case resource forget should be called
     * on all other heuristic outcomes like Heuristic.Commit.
     * 
     * @param ignoreHeuristic the heuristic to ignore
     * @throws IllegalStateException The transaction has not commited
     *   or rolledback yet
     *
     * @see HeuristicExceptions
     */
    private void forget(int ignoreHeuristic)
	throws IllegalStateException
    {
	int                i;
	XAResourceHolder xaRes;
	Transaction        suspended;
	TransactionManager tm;
	Synchronization    sync;
	Resource           resource;

	if ( _status != STATUS_COMMITTED && _status != STATUS_ROLLEDBACK )
	    throw new IllegalStateException( Messages.message( "tyrex.tx.cannotForget" ) );

    //RM
    //System.out.print("_heuristic ");
    //Debug.printHeuristic(_heuristic);
    
    // only forget the resources if a heuristic exception occured
    if ( ( _heuristic != ignoreHeuristic) &&
         ( ( _heuristic == Heuristic.Rollback ) ||
           ( _heuristic == Heuristic.Commit ) ||
           ( _heuristic == Heuristic.Mixed ) ||
           ( _heuristic == Heuristic.Hazard ) ) ) {
        if ( _enlisted != null ) {
    	    // Tell all the resources to forget about their
    	    // transaction. Shared resources do not require
    	    // such a notification.
    	    for ( i = 0 ; i < _enlisted.length ; ++i ) {
    		xaRes = _enlisted[ i ];
    		try {
    		    if ( ! xaRes.shared )
    			xaRes.xa.forget( xaRes.xid );
    		} catch ( XAException except ) {
                //RM
                //except.printStackTrace();
    		    error( except );
    		} catch ( Exception except ) {
    		    error( except );
    		}
    		xaRes.xa = null;
    		xaRes.xid = null;
    		_enlisted[ i ] = null;
    	    }
    	}

        _enlisted = null;
        
        if ( _delisted != null ) {
    	    // Tell all the resources to forget about their
    	    // transaction. Shared resources do not require
    	    // such a notification.
    	    for ( i = 0 ; i < _delisted.length ; ++i ) {
    		xaRes = _delisted[ i ];
    		try {
    		    if ( ! xaRes.shared )
    			xaRes.xa.forget( xaRes.xid );
    		} catch ( XAException except ) {
                //RM
                //except.printStackTrace();
    		    error( except );
    		} catch ( Exception except ) {
    		    error( except );
    		}
    		xaRes.xa = null;
    		xaRes.xid = null;
    		_delisted[ i ] = null;
    	    }
    	}
    
        _delisted = null;

    	if ( _resources != null ) {
    	    // Tell all the OTS resources to forget about their
    	    // transaction.
    	    for ( i = 0 ; i < _resources.length ; ++i ) {
    		resource = _resources[ i ];
    		if ( resource != null ) {
    		    try {
    			resource.forget();
    		    } catch ( Exception except ) {
    			error( except );
    		    }
    		    _resources[ i ] = null;
    		}
    	    }
    	}
    }

    // the resources are no longer associated with the transaction
    _enlisted = null;
    _resources = null;
    _delisted = null;

	// Notify all the synchronization objects that the
	// transaction has completed with a status.
	if ( _syncs != null ) {

	    // If we are not running in the same thread as
	    // this transaction, need to make this transaction
	    // the current one before calling method.
	    suspended = makeCurrentTransactionIfNecessary();

	    for ( i = _syncs.length ; --i >= 0  ; ) {
		sync = _syncs[ i ];
		try {
		    sync.afterCompletion( _status );
		} catch ( Exception except ) {
		    error( except );
		}
		_syncs[ i ] = null;
	    }
	    _syncs = null;
	    
	    // Resume the previous transaction associated with
	    // the thread.
	    if ( suspended != null ) {
		    resumeTransaction( suspended );
	    }
	}

	// Only top level transaction is registered with the
	// transaction server and should be unlisted.
	if ( _parent == null )
	    _txDomain.forgetTransaction( this );
    }


    /**
     * Suspend the resources associated with the transaction.
     * <P>
     * The resources that are already suspended are not affected.
     */
    synchronized void suspendResources()
    throws IllegalStateException, SystemException
    {
    XAResourceHolder xaRes;

	// Check the status of the transaction and act accordingly.
	switch ( _status ) {
	case STATUS_ACTIVE:
	case STATUS_MARKED_ROLLBACK:
	    // Transaction is active, we can suspend resources.
	    break;

	case STATUS_PREPARED:
	case STATUS_PREPARING:
	case STATUS_COMMITTING:
	    // Transaction is preparing, cannot suspend resource.
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inCommit" ) );

	case STATUS_ROLLEDBACK:
	case STATUS_ROLLING_BACK:
	case STATUS_COMMITTED:
	case STATUS_NO_TRANSACTION:
	case STATUS_UNKNOWN:
	default:
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
	}

	if ( _enlisted == null )
	    return;
	// Look up the enlisted resource. If the resource is not
	// enlisted, return false.
	xaRes = null;
	for (int i = 0 ; i < _enlisted.length ; ++i ) {
	    xaRes = _enlisted[ i ];
	    // we simply suspend it. The resource will be resumed when we
        // commit/rollback, i.e. it's not removed from the
        // list of enlisted resources.
        if (xaRes.endFlag == XAResource.TMNOFLAGS) {
            try {
            xaRes.xa.end( xaRes.xid, XAResource.TMSUSPEND );
            xaRes.endFlag = XAResource.TMSUSPEND;
            } /*catch ( XAException except ) {
                //RM do nothing
            }*/ catch ( Exception except ) {
            throw new SystemException( except.toString() );
            }
        }
    }
	}

    public synchronized boolean delistResource( XAResource xa, int flag )
	throws IllegalStateException, SystemException
    {
	XAResourceHolder xaRes;
	XAResourceHolder shared;
	int                i;
    
	// Check the status of the transaction and act accordingly.
	switch ( _status ) {
	case STATUS_ACTIVE:
	case STATUS_MARKED_ROLLBACK:
	    // Transaction is active, we can delist the resource.
	    break;

	case STATUS_PREPARED:
	case STATUS_PREPARING:
	case STATUS_COMMITTING:
	    // Transaction is preparing, cannot delist resource.
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inCommit" ) );

	case STATUS_ROLLEDBACK:
	case STATUS_ROLLING_BACK:
	case STATUS_COMMITTED:
	case STATUS_NO_TRANSACTION:
	case STATUS_UNKNOWN:
	default:
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
	}

	if ( _enlisted == null )
	    return false;
	// Look up the enlisted resource. If the resource is not
	// enlisted, return false.
	xaRes = null;
	for ( i = 0 ; i < _enlisted.length ; ++i ) {
	    xaRes = _enlisted[ i ];
	    if ( xaRes.xa == xa ) {
		break;
	    }
	}
	if ( i == _enlisted.length )
	    return false;

	switch ( flag ) {
	case XAResource.TMSUSPEND:
	    // If the resource is being suspended, we simply suspend
	    // it and return. The resource will be resumed when we
	    // commit/rollback, i.e. it's not removed from the
	    // list of enlisted resources.
	    try {
		xaRes.xa.end( xaRes.xid, XAResource.TMSUSPEND );
		xaRes.endFlag = XAResource.TMSUSPEND;
		return true;
	    } catch ( XAException except ) {
		return false;
	    } catch ( Exception except ) {
		throw new SystemException( except.toString() );
	    }

        case XAResource.TMSUCCESS:
        case XAResource.TMFAIL:
	    // If we got the fail flag, the resource has failed (e.g.
	    // the JDBC connection died), we simply end the resource
	    // with a failure and remove it from the list. We will
	    // never need to commit or rollback this resource.

        // If we got the success flag then end the resource with a
        // success, remove it from the enlisted list and add it to
        // the delisted list

	    try {
		if ( _enlisted.length == 1 )
		    _enlisted = null;
		else {
		    XAResourceHolder[] newList;

		    _enlisted[ i ] = _enlisted[ _enlisted.length - 1 ];
		    newList = new XAResourceHolder[ _enlisted.length - 1 ];
		    System.arraycopy( _enlisted, 0, newList, 0, _enlisted.length - 1 );
		    _enlisted = newList;
		}

        if ( flag == XAResource.TMFAIL ) {
            xaRes.xa.end( xaRes.xid, XAResource.TMFAIL );
    		xaRes.xa = null;
    		xaRes.xid = null;
        } else {
            xaRes.xa.end( xaRes.xid, XAResource.TMSUCCESS );
            if ( null == _delisted) {
                _delisted = new XAResourceHolder[]{ xaRes };    
            } else {
                XAResourceHolder[] newList;

    		    newList = new XAResourceHolder[ _delisted.length + 1 ];
    		    System.arraycopy( _delisted, 0, newList, 0, _delisted.length );
                newList[ _delisted.length ] = xaRes;
    		    _delisted = newList;
            }
        }

        xaRes.endFlag = flag;

		return true;
	    } catch ( XAException except ) {
		return false;
	    } catch ( Exception except ) {
		throw new SystemException( except.toString() );
	    } finally {
            // if this is resource failure set rollback
            if ( flag == XAResource.TMFAIL ) {
                setRollbackOnly();    
            }
        }
    default:
	    throw new IllegalArgumentException( Messages.message( "tyrex.tx.invalidFlag" ) );
	}
    }


    /**
     * Resume previously suspended resources in the transaction and
     * enlist the new specified resources in the transaction.
     * <P>
     * Active resources (ie non-suspended) are not affected.
     *
     * @param xas the resources to be enlisted in the transaction. The
     *      resources may already be enlisted.
     */
    synchronized void resumeAndEnlistResources(XAResource[] xas)
    throws IllegalStateException, SystemException, RollbackException
    {
    /*
    This is not the cleanest way of performing the operation (this operation
    should be split in two) but it is the most efficient.
    */
	XAResourceHolder xaRes;
    XAResource xa;
    int i;
    int j;
    int enlistedLength;
    
	// Proper notification for transactions that timed out.
	if ( _timedOut )
	    throw new RollbackException( Messages.message( "tyrex.tx.timedOut" ) );

	// Check the status of the transaction and act accordingly.
	switch ( _status ) {
	case STATUS_ACTIVE:
	    // Transaction is active, we can enlist the resource.
	    break;

	case STATUS_MARKED_ROLLBACK:
	    // Transaction marked for rollback, we cannot possibly enlist.
	    throw new RollbackException( Messages.message( "tyrex.tx.markedRollback" ) );

	case STATUS_PREPARED:
	case STATUS_PREPARING:
	case STATUS_COMMITTING:
	    // Transaction is preparing, cannot enlist resource.
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inCommit" ) );

	case STATUS_ROLLEDBACK:
	case STATUS_ROLLING_BACK:
	case STATUS_COMMITTED:
	case STATUS_NO_TRANSACTION:
	case STATUS_UNKNOWN:
	default:
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
	}

    if ( _enlisted != null ) {
        enlistedLength = _enlisted.length;

	    // Look if we alredy got the resource enlisted. If the
	    // resource was suspended, we just resume it and go out.
	    // If the resource was started, we do not enlist it a
	    // second time.
	    for ( i = 0 ; i < enlistedLength ; ++i ) {
		xaRes = _enlisted[ i ];
		if ( xaRes.endFlag == XAResource.TMSUSPEND ) {
			try {
			    xaRes.xa.start( xaRes.xid, XAResource.TMRESUME );
                xaRes.endFlag = XAResource.TMNOFLAGS;
			} catch ( XAException except ) {
			    //RM do nothing
			} catch ( Exception except ) {
			    throw new SystemException( except.toString() );
			}
		    }

        }

        if (null != xas) {
        outer:
        for ( i = 0 ; i < xas.length; ++i ) {
            xa = xas[i];
            for ( j = 0 ; j < enlistedLength ; ++j ) {
                if (_enlisted[j].xa == xa) {
                    continue outer;
                }
            }
            // new resource
            addNewResource(xa);
        }
        }
    }
    else if (null != xas){
        for ( i = 0 ; i < xas.length; i++) {
            addNewResource(xas[i]);    
        }
    }
    }

    public synchronized boolean enlistResource( XAResource xa )
	throws IllegalStateException, SystemException, RollbackException
    {
	XAResourceHolder xaRes;
	XAResourceHolder shared;
	int                i;
	XAResourceHolder[] newList;


	// Proper notification for transactions that timed out.
	if ( _timedOut )
	    throw new RollbackException( Messages.message( "tyrex.tx.timedOut" ) );

	// Check the status of the transaction and act accordingly.
	switch ( _status ) {
	case STATUS_ACTIVE:
	    // Transaction is active, we can enlist the resource.
	    break;

	case STATUS_MARKED_ROLLBACK:
	    // Transaction marked for rollback, we cannot possibly enlist.
	    throw new RollbackException( Messages.message( "tyrex.tx.markedRollback" ) );

	case STATUS_PREPARED:
	case STATUS_PREPARING:
	case STATUS_COMMITTING:
	    // Transaction is preparing, cannot enlist resource.
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inCommit" ) );

	case STATUS_ROLLEDBACK:
	case STATUS_ROLLING_BACK:
	case STATUS_COMMITTED:
	case STATUS_NO_TRANSACTION:
	case STATUS_UNKNOWN:
	default:
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
	}

	if ( _enlisted != null ) {
	    // Look if we alredy got the resource enlisted. If the
	    // resource was suspended, we just resume it and go out.
	    // If the resource was started, we do not enlist it a
	    // second time.
	    for ( i = 0 ; i < _enlisted.length ; ++i ) {
		xaRes = _enlisted[ i ];
		if ( xaRes.xa == xa ) {
		    if ( xaRes.endFlag == XAResource.TMSUSPEND ) {
			try {
			    xaRes.xa.start( xaRes.xid, XAResource.TMRESUME );
                xaRes.endFlag = XAResource.TMNOFLAGS;
			    return true;
			} catch ( XAException except ) {
			    return false;
			} catch ( Exception except ) {
			    throw new SystemException( except.toString() );
			}
		    } else
			return false;
		}
	    }
    }

	return addNewResource(xa);
    }


    /**
     * Return true if the specified xa resource is shared
     * with the resources in the specified array. If the xa
     * resource is to be shared it is added to the list of
     * enlisted resources.
     *
     * @param xa the xa resource
     * @param xaResourceHolders the array of xa resource holders.
     *      Can be null.
     * @return true if the specified xa resource is shared
     *      with the resources in the specified array
     * @throws XAException if there is a problem letting the xa
     *      resource join an existing transaction branch
     * @throws SystemException if there is a general problem
     *      sharing the resource.
     */
    private boolean shareResource(XAResource xa, XAResourceHolder[] xaResourceHolders)
        throws XAException, SystemException
    {
    if (null != xaResourceHolders) {
    XAResourceHolder shared;
    XAResourceHolder[] newList;
    // Check to see whether we have two resources sharing the same
    // resource manager, in which case use one Xid for both.
    try {
        for ( int i = 0 ; i < xaResourceHolders.length ; ++i ) {
        shared = xaResourceHolders[ i ];
        if ( shared.shared && shared.xa.isSameRM( xa ) ) {
            XAResourceHolder xaRes = new XAResourceHolder();
            xaRes.xa = xa;
            xaRes.shared = true;
            xaRes.xid = shared.xid;
            try {
                xaRes.xa.start( xaRes.xid, XAResource.TMJOIN );
                newList = new XAResourceHolder[ _enlisted.length + 1 ];
                System.arraycopy( _enlisted, 0, newList, 0, _enlisted.length );
                newList[ _enlisted.length ] = xaRes;
                _enlisted = newList;
                return true;
            } catch ( XAException except ) {
                throw except;
            } catch ( Exception except ) {
                throw new SystemException( except.toString() );
            }
        }
        }
    } catch ( XAException except ) {
        // if this is an XA exception from the isSameRM 
        // method call then return it as a system exception
        if ( ( except.errorCode == XAException.XAER_RMERR ) || 
             ( except.errorCode == XAException.XAER_RMFAIL ) ) {
            throw new SystemException( except.toString() );    
        }
        throw except;
    }
    }
    return false;
    }

    /**
     * Add the new resource to the transaction.
     * <P>
     * It is assumed that this resource has not been added
     * to the transaction before and that the transaction is
     * in the correct state for having new resources added to it.
     * <P>
     * This method is called by {@link #enlistResource} and
     * {@link #resumeAndEnlistResources}.
     *
     * @param xa the new resource
     * @return True if the resource has been added.
     */
    private boolean addNewResource(XAResource xa)
        throws SystemException, RollbackException
    {
    XAResourceHolder xaRes;
    XAResourceHolder shared;
    XAResourceHolder[] newList;

    try {
        if ( shareResource( xa, _enlisted ) ||
             shareResource( xa, _delisted ) ) {
            return true;    
        }
    } catch (XAException e) {
        return false;
    }


	// If we got to this point, this is a new resource that
	// is being enlisted. We need to create a new branch Xid
	// and to enlist it.
	xaRes = new XAResourceHolder();
	xaRes.xa = xa;
    
    try {
        xaRes.xid = XAResourceHelperManager.getHelper( xa ).getXid( _xid.newBranch() );
        xa.start( xaRes.xid, XAResource.TMNOFLAGS );
	    if ( _enlisted == null ) {
		_enlisted = new XAResourceHolder[ 1 ];
		_enlisted[ 0 ] = xaRes;
	    } else {
		newList = new XAResourceHolder[ _enlisted.length + 1 ];
		System.arraycopy( _enlisted, 0, newList, 0, _enlisted.length );
		newList[ _enlisted.length ] = xaRes;
		_enlisted = newList;
	    }
	    return true;
	} catch ( XAException except ) {
        // rm
        //except.printStackTrace();
        return false;
	} catch ( Exception except ) {
        //except.printStackTrace();
        throw new SystemException( except.toString() );
	}

    }

    public int getStatus()
    {
	// Easiest method to write!
	return _status;
    }

    
    public synchronized void registerSynchronization( Synchronization sync )
	throws RollbackException, IllegalStateException, SystemException
    {
	// Proper notification for transactions that timed out.
	if ( _timedOut )
	    throw new RollbackException( Messages.message( "tyrex.tx.timedOut" ) );

	// Check the status of the transaction and act accordingly.
	switch ( _status ) {
	case STATUS_ACTIVE:
	    // Transaction is active, we can register the synchronization.
	    break;

	case STATUS_MARKED_ROLLBACK:
	    // Transaction marked for rollback, we cannot possibly register..
	    throw new RollbackException( Messages.message( "tyrex.tx.markedRollback" ) );

	case STATUS_PREPARED:
	case STATUS_PREPARING:
	case STATUS_COMMITTING:
	    // Transaction is preparing, cannotr register.
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inCommit" ) );

	case STATUS_ROLLEDBACK:
	case STATUS_ROLLING_BACK:
	case STATUS_COMMITTED:
	case STATUS_NO_TRANSACTION:
	case STATUS_UNKNOWN:
	default:
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
	}

	if ( _syncs == null ) {
	    _syncs = new Synchronization[ 1 ];
	    _syncs[ 0 ] = sync;
	} else {
	    Synchronization[] newSyncs;
	    int               i;

	    // In many cases we will get duplicity in synchronization
	    // registration, but we don't want to fire duplicate events.
	    for ( i = 0 ; i < _syncs.length ; ++i )
		if ( _syncs[ i ] == sync )
		    return;
	    newSyncs = new Synchronization[ _syncs.length + 1 ];
	    System.arraycopy( _syncs, 0, newSyncs,  0, _syncs.length );
	    newSyncs[ _syncs.length ] = sync;
	    _syncs = newSyncs;
	}
    }


    /**
     * Called to register an OTS resource with the transaction. Used
     * internally to perform nested transactions and exposed through
     * the OTS interface.
     *
     * @param res The OTS resource to register
     */
    synchronized void registerResource( Resource resource )
	throws IllegalStateException
    {
	// Check the status of the transaction and act accordingly.
	switch ( _status ) {
	case STATUS_ACTIVE:
	case STATUS_MARKED_ROLLBACK:
	    // Transaction is active, or marked for rollback, 
	    // we can register this resource.
	    break;

	case STATUS_PREPARED:
	case STATUS_PREPARING:
	case STATUS_COMMITTING:
	    // Transaction is preparing, cannotr register.
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inCommit" ) );

	case STATUS_ROLLEDBACK:
	case STATUS_ROLLING_BACK:
	case STATUS_COMMITTED:
	case STATUS_NO_TRANSACTION:
	case STATUS_UNKNOWN:
	default:
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
	}

	if ( _resources == null ) {
	    _resources = new Resource[ 1 ];
	    _resources[ 0 ] = resource;
	} else {
	    Resource[] newResources;
	    int        i;

	    // It is less likely, but still possible to get
	    // duplicity in resource registration.
	    for ( i = 0 ; i < _resources.length ; ++i )
		if ( _resources[ i ] == resource )
		    return;
	    newResources = new Resource[ _resources.length + 1 ];
	    System.arraycopy( _resources, 0, newResources,  0, _resources.length );
	    newResources[ _resources.length ] = resource;
	    _resources = newResources;
	}
    }


    /**
     * Called by a subtransaction to dissociate itself from the parent
     * transaction when the subtransaction has been rolledback, to
     * prevent rolling back the parent transaction, see {@link
     * #rollback}.
     *
     * @param resource The subtransaction as a resource
     */
    private synchronized void unregisterResource( Resource resource )
    {
	// Note about equality tests:
	//   We are called to remove a resource R1 previously created
	//   for same transaction, but are called with resource R2
	//   created for the same transaction. The only way to detect
	//   if the two are equivalent is through object equality.

	if ( _resources != null ) {
	    if ( _resources.length == 1 && _resources[ 0 ].equals( resource ) )
		_resources = null;
	    else {
		Resource[] newResources;
		int        i;

		for ( i = 0 ; i < _resources.length ; ++i )
		    if ( _resources[ i ].equals( resource ) ) {
			_resources[ i ] = _resources[ _resources.length - 1 ];
			newResources = new Resource[ _resources.length - 1 ];
			System.arraycopy( _resources, 0, newResources,  0, _resources.length - 1 );
			_resources = newResources;
		    }
	    }
	}
    }


    public synchronized void setRollbackOnly()
	throws IllegalStateException, SystemException
    {
	// Check the status of the transaction and act accordingly.
	switch ( _status ) {
	case STATUS_ACTIVE:
	case STATUS_MARKED_ROLLBACK:
	    // Transaction is active, we'll mark it for roll back.
	    _status = STATUS_MARKED_ROLLBACK;
	    break;
	    
	case STATUS_PREPARED:
	case STATUS_PREPARING:
	case STATUS_COMMITTING:
	    // Transaction is right now in the process of being commited,
	    // cannot change it to roll back.
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inCommit" ) );

	case STATUS_ROLLEDBACK:
	case STATUS_ROLLING_BACK:
	    // Transaction has been or is being rolled back, just leave.
	    return;
	    
	case STATUS_COMMITTED:
	case STATUS_NO_TRANSACTION:
	case STATUS_UNKNOWN:
	default:
	    throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
	}
    }


    /**
     * Used internally to report erros occuring during a transaction
     * commit/rollback. The first general error to occur will be
     * recorded in {@link #_sysError} as a {@link SystemException},
     * and possibly thrown from {@link #commit} or {@link #rollback}.
     * {@link XAException} are also supported, but should not be
     * reported unless they are of an unspecified type (e.g. protocol,
     * communication, or other inconsistency error).
     *
     * @param except An error that occured during commit/rollback
     */
    private void error( Throwable except )
    {
	// If a remote exception, we would rather record the underlying
	// exception directly.
	if ( except instanceof RemoteException &&
	     ( (RemoteException) except ).detail != null )
	    except = ( (RemoteException) except ).detail;

	// In the event of an XAException, we would like to
	// provide a meaningful description of the exception.
	// The error code just doesn't cut it.
	if ( except instanceof XAException ) {
	    switch ( ( (XAException) except ).errorCode ) {
	    case XAException.XA_RBROLLBACK:
		except = new SystemException( Messages.message( "tyrex.xa.rbrollback" ) );
		break;
	    case XAException.XA_RBTIMEOUT:
		except = new SystemException( Messages.message( "tyrex.xa.rbtimeout" ) );
		break;
	    case XAException.XA_RBCOMMFAIL:
		except = new SystemException( Messages.message( "tyrex.xa.rbcommfail" ) );
		break;
	    case XAException.XA_RBDEADLOCK:
		except = new SystemException( Messages.message( "tyrex.xa.rbdeadlock" ) );
		break;
	    case XAException.XA_RBINTEGRITY:
		except = new SystemException( Messages.message( "tyrex.xa.rbintegrity" ) );
		break;
	    case XAException.XA_RBOTHER:
		except = new SystemException( Messages.message( "tyrex.xa.rbother" ) );
		break;
	    case XAException.XA_RBPROTO:
		except = new SystemException( Messages.message( "tyrex.xa.rbproto" ) );
		break;
	    case XAException.XA_HEURHAZ:
		except = new SystemException( Messages.message( "tyrex.xa.heurhaz" ) );
		break;
	    case XAException.XA_HEURCOM:
		except = new SystemException( Messages.message( "tyrex.xa.heurcom" ) );
		break;
	    case XAException.XA_HEURRB:
		except = new SystemException( Messages.message( "tyrex.xa.heurrb" ) );
		break;
	    case XAException.XA_HEURMIX:
		except = new SystemException( Messages.message( "tyrex.xa.heurmix" ) );
		break;
	    case XAException.XA_RDONLY:
		except = new SystemException( Messages.message( "tyrex.xa.rdonly" ) );
		break;
	    case XAException.XAER_NOTA:
		except = new SystemException( Messages.message( "tyrex.xa.nota" ) );
		break;
	    default:
		except = new SystemException( Messages.format( "tyrex.xa.unknown", except ) );
		break;
	    }
	}

	// Log the message with whatever logging mechanism we have.
	_txDomain.logMessage( _xid.toString() + " : " + except.toString() );
    if ( except instanceof RuntimeException &&
	     _txDomain.getLogWriter() != null )
	    except.printStackTrace( _txDomain.getLogWriter() );

	// Record the first general exception as a system exception,
	// so it may be returned from commit/rollback.
	if ( _sysError == null ) {
	    if ( except instanceof SystemException )
		_sysError = (SystemException) except;
	    else
		// For any other error, we produce a SystemException
		// to wrap it up.
		_sysError = new SystemException( except.toString() );
	}
    }


    /**
     * Returns a textual presentation of this transaction comprising
     * of the transaction's Xid and status.
     */
    public String toString()
    {
	String status;

	switch ( _status ) {
	case STATUS_ACTIVE:
	    status = " (Active)";
	    break;
	case STATUS_MARKED_ROLLBACK:
	    status = " (Marked for rollback)";
	    break;
	case STATUS_PREPARED:
	case STATUS_PREPARING:
	case STATUS_COMMITTING:
	    status = " (Commit in progress)";
	    break;
	case STATUS_COMMITTED:
	    status = " (Committed)";
	    break;
	case STATUS_ROLLEDBACK:
	    if ( _timedOut )
		status = " (Rolledback / Timedout)";
	    else
		status = " (Rolledback)";
	    break;
	case STATUS_ROLLING_BACK:
	    status = " (Rollback in progress)";
	    break;
	default:
	    // We should never end up in this state!
	    status = " (Unknown)";
	    break;
	}
	return _xid.toString() + status;
    }


    /**
     * Returns a unique hash code for this transaction.
     */
    public int hashCode()
    {
	return _xid.hashCode();
    }


    /**
     * Used for equality test on two transactions.
     *
     * @return True if the two objects are associated with the same
     *   global transaction
     */
    public boolean equals( Object other )
    {
	// By design choice there will only be one transaction object
	// per unique transaction (identifier).
	return ( this == other );
    }


    /**
     * Returns a normalized heuristic decision based on the supplied
     * non-normalized heuristic. A normalized heuristic has at most
     * one flag set it in. For example, if both commit and rollback
     * flags are set, the outcome is a mixed heuristic. If both commit
     * and mixed are set, the outcome is again a mixed heuristic.
     *
     * @param heuristic A non-normalized heuristic decision
     * @return A normalized heuristic decision
     */
    public int normalize( int heuristic )
    {
	if ( ( heuristic & Heuristic.Hazard ) != 0 )
	    return Heuristic.Hazard;
	else if ( ( heuristic & Heuristic.Mixed ) != 0 )
	    return Heuristic.Mixed;
    else if ( ( heuristic & Heuristic.Other ) != 0 ) {
        return Heuristic.Other;    
    }
    else if ( ( heuristic & Heuristic.Unknown ) != 0 ) {
        return Heuristic.Unknown;    
    }
	else if ( ( heuristic == ( Heuristic.Commit | Heuristic.Rollback ) ) ||
              ( heuristic == ( Heuristic.TX_COMMITTED | Heuristic.TX_ROLLEDBACK ) ))
	    return Heuristic.Mixed;
	else
	    return heuristic;
    }


    /**
     * Called to obtain the Xid associated with this transaction.
     * Used internally when a transaction must be identifier by it's
     * global transaction identifier.
     */
    XidImpl getXid()
    {
	return _xid;
    }


    /**
     * Returns the parent of this transaction.
     *
     * @return The parent of this transaction, null if the transaction
     *   is top-level or has been rolled back
     */
    TransactionImpl getParent()
    {
	return _parent;
    }


    /**
     * Returns the top level parent of this transaction, or this
     * transaction if this is a top level transaction.
     *
     * @return The top level transaction
     */ 
    TransactionImpl getTopLevel()
    {
	if ( _parent == null )
	    return this;
	else
	    return _parent.getTopLevel();
    }


    /**
     * Returns the heuristic decision of this transaction after it
     * has been prepared, commited or rolledback. At all other times
     * this method will return {@link #Heuristic.ReadOnly}.
     *
     * @return The heuristic decision of this transaction
     */
    int getHeuristic()
    {
	return _heuristic;
    }

    /**
     * Change the timeout for the transaction's resources 
     * to the new value. 
     *
     * @param seconds The new timeout in seconds
     * @see TransactionDomain#setTransactionTimeout
     */
    public void setTransactionTimeout( int seconds )
    {
        /**
         * The call to the transaction domain
         * eventually calls internalSetTransactionTimeout
         */

        _txDomain.setTransactionTimeout( this, seconds );
    }

    /**
     * Called by {@link TransactionDomain} to change the timeout for
     * the transaction's resources to the new value. This might or
     * might not have an effect on the underlying resources.
     * All consistency checks are made by the server.
     *
     * @param secods The new timeout in seconds
     * @see TransactionDomain#setTransactionTimeout
     */
    void internalSetTransactionTimeout( int seconds )
    {
	XAResourceHolder xaRes;
	int                i;

	if ( _enlisted != null ) {
	    for ( i = 0 ; i < _enlisted.length ; ++i ) {
		xaRes = _enlisted[ i ];
		try {
		    xaRes.xa.setTransactionTimeout( seconds );
		} catch ( XAException except  ) {
		    // We could care less if we managed to set the
		    // timeout on the resource. We have to assume it
		    // might not timeout when we expect it anyway.
		}
	    }
	}
    }


    /**
     * Indicates that the transaction has been rolled back due to time out.
     * Automatically performs a rollback on the transaction. We only
     * reach this state if the transaction is active.
     */
    synchronized void timedOut()
    {
	int      i;

	// Let the rollback mechanism know that the transaction has failed.
	_timedOut = true;
	// We notify all the XA resources that the transaction has failed,
	// so they will be broken if the application tries to use them.
	while ( _enlisted != null ) {
	    try {
		delistResource( _enlisted[ 0 ].xa, XAResource.TMFAIL );
	    } catch ( Exception except ) { }
	}

	try {
        // Perform the rollback, ignore the returned heuristics.
	    internalRollback();
    } finally {
        // The transaction will now tell all it's resources to
    	// forget about the transaction and will release all
    	// held resources. Also notifies all the synchronization
    	// objects that the transaction has completed with a status.
    	try {
    	    forgetOnRollback();
    	} catch ( IllegalStateException e ) { }
    }
    }


    /**
     * Returns true if the transaction has timed out and rolled back.
     */
    boolean getTimedOut()
    {
	return _timedOut;
    }


    /**
     * Called to obtain the {@link Control} interface for this
     * transaction. A {@link ControlImpl} object is created the
     * first time this method is called and returned in subsequent
     * invocations.
     *
     * @return The control interface to this transaction
     */
    synchronized ControlImpl getControl()
    {
	if ( _control == null )
	    _control = new ControlImpl( this );
	return _control;
    }


    /**
     * Returns the propagation context used to import this
     * transaction or null if the transaction was not imported.
     */
    PropagationContext getPropagationContext()
    {
	return _pgContext;
    }


    /**
     * Returns a listing of the resources associated with this
     * transaction. Provides an identification of all the XA
     * resources, OTS resources and subtransactions.
     *
     * @return Array of description of all resources enlisted
     *   with this transaction
     */
    synchronized String[] listResources()
    {
	int      i;
	int      index;
	String[] resList;

	resList = new String[ ( _enlisted == null ? 0 : _enlisted.length ) +
			      ( _resources == null ? 0 : _resources.length ) ];
	index = 0;
	if ( _enlisted != null ) 
	    for ( i = 0 ; i < _enlisted.length ; ++i ) {
		resList[ index ] = XidImpl.toString( _enlisted[ i ].xid.getBranchQualifier() );
		if ( _enlisted[ i ].endFlag != XAResource.TMNOFLAGS )
		    resList[ index ] =  resList[ index ] + 
                                " [" + 
                                ( ( _enlisted[ i ].endFlag == XAResource.TMSUSPEND 
                                    ? "suspended" 
                                    : ( _enlisted[ i ].endFlag == XAResource.TMSUCCESS ? "ended" : "failed" ) ) ) + 
                                "]";
		++index;
	    }
	if ( _resources != null )
	    for ( i = 0 ; i < _resources.length ; ++i ) {
		resList[ index ] = _resources[ i ].toString();
		++index;
	    }
	return resList;
    }




    /**
     * Describes an {@link XAResource} enlisted with this transaction.
     * Each resource enlisted with the transaction will have such a record
     * until the transaction timesout or is forgetted. The only way to
     * delist a resource is if it fails.
     */
    class XAResourceHolder
    {
    
	/**
	 * The xid under which this resource is enlisted.
	 * Generally each resource will have the same global Xid,
	 * but a different branch, but shared resources will also
	 * share the same branch.
	 */
	Xid  xid;
    
	/**
	 * The enlisted XA resource.
	 */
	XAResource xa;
    
	/**
	 * The flag that the xa resource was ended with.
     * If the xa resource has not been ended then the
     * value of the endFlag is XAResource.TMNOFLAGS. If
     * the resource has been suspended the flag is 
     * XAResource.TMSUSPEND.  IF the resource is resumed
     * the flag is reset to XAResource.TMNOFLAGS. 
     * If the resource has been dissociated from the 
     * transaction the flag is XAResource.TMSUCCESS.
     * If the resource has failed the flag is 
     * XAResource.TMFAIL. To sum up the valid values
     * are XAResource.TMNOFLAGS, XAResource.TMSUSPEND,
     * XAResource.TMSUCCESS, XAResource.TMFAIL.
	 */
	int endFlag = XAResource.TMNOFLAGS;
    
	/**
	 * A shared resource is one that shares it's transaction
	 * branch with another resource (e.g. two JDBC connections
	 * to the same database). Only one of the shared resources
	 * must be commited or rolled back, although both should be
	 * notified when the transaction terminates.
	 */
	boolean shared;
    
	/**
	 * This flag is used during 2pc to indicate whether the resource
	 * should be commited/rolledback. Shared resources and those that
	 * indicated they are read-only during preperation do not need
	 * to be commited/rolledback.
	 */
	boolean readOnly;
    
    }

    
}



