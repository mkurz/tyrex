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
 * $Id: TransactionImpl.java,v 1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.server;


import java.io.PrintWriter;
import java.rmi.RemoteException;
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
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see EnlistedXAResource
 * @see TransactionManagerImpl
 * @see TransactionServer
 * @see ResourceImpl
 */
final class TransactionImpl
    implements Transaction, Status, Heuristic
{


    /**
     * Holds a list of all the synchronization objects.
     */
    private Synchronization[]    _syncs;


    /**
     * Holds a list of all the enlisted resources, each one
     * of type {@link EnlistedXAResource}.
     */
    private EnlistedXAResource[] _enlisted;


    /**
     * Holds a list of all the enlisted OTS resources.
     */
    private Resource[]           _resources;


    /**
     * The global Xid of this transaction. Each transaction
     * will have exactly one global Xid for as long as it
     * exists.
     */
    private XidImpl             _xid;


    /**
     * Holds the current status of the transaction.
     */
    private int                _status;


    /**
     * Held during a commit/rollback process to indicate that
     * an unexpected error occured. Will throw that exception
     * if there is no other more important exception to report
     * (e.g. RollbackException).
     */
    private SystemException   _sysError;


    /**
     * True if this transaction has been rolled back due to timeout.
     */
    private boolean             _timedOut;


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
    private PropagationContext     _pgContext;


    /**
     * The heuristic decision made by the transaction after a call to
     * {@link #prepare}, {@link #internalCommit}, {@link #internalRollback}.
     * Held in case the operation is repeated to return a consistent
     * heuristic decision. Defaults to read-only (i.e. no heuristic decision).
     */
    private int                    _heuristic = HEURISTIC_READONLY;


    /**
     * If this transaction is used through the OTS API, it will have
     * a control associated with it. The control is created when
     * needed and referenced from here. Most of the time, this
     * variable is null.
     */
    private ControlImpl           _control;




    /**
     * Hidden constructor used by {@link TransactionServer} to create
     * a new transaction. A transaction can only be created through
     * {@link TransactionServer} or {@link TransactionManager} which
     * take care of several necessary housekeeping duties.
     *
     * @param xid The Xid for this transaction
     * @param parent The parent of this transaction if this is a
     *   nested transaction, null if this is a top level transaction
     */
    TransactionImpl( XidImpl xid, TransactionImpl parent )
    {
	_xid = xid;
	_status = STATUS_ACTIVE;
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


    /**
     * Hidden constructor used by {@link TransactionServer} to create
     * a new transaction. A transaction can only be created through
     * {@link TransactionServer} or {@link TransactionManager} which
     * take care of several necessary housekeeping duties. This
     * transaction is created to import an OTS transaction using
     * the propagation context.
     *
     * @param xid The Xid for this transaction
     * @param pgContext The propagation context
     * @throws Inactive The parent transaction has rolled  back or
     *   is inactive
     */
    TransactionImpl( XidImpl xid, PropagationContext pgContext )
	throws Inactive
    {
	_xid = xid;
	_status = STATUS_ACTIVE;
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


    public synchronized void commit()
	throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
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

	// This is two phase commit.
	prepare();
	switch ( _heuristic ) {
	case HEURISTIC_READONLY:
	    // Read only resource does not need either commit, nor rollback
	    TransactionServer.logTransaction( _xid, _heuristic );
	    _status = STATUS_COMMITTED;
	    break;

	case HEURISTIC_ROLLBACK:
	case HEURISTIC_MIXED:
	case HEURISTIC_HAZARD:
	    // Transaction must be rolled back and an exception thrown to
	    // that effect.
	    _status = STATUS_MARKED_ROLLBACK;
	    internalRollback();
	    try {
		forget();
	    } catch ( IllegalStateException except ) { }
	    throw new HeuristicRollbackException( Messages.message( "tyrex.tx.rolledback" ) );

	case HEURISTIC_COMMIT:
	default:
	    internalCommit();
	    break;
	}

	// The transaction will now tell all it's resources to
	// forget about the transaction and will release all
	// held resources. Also notifies all the synchronization
	// objects that the transaction has completed with a status.
	try {
	    forget();
	} catch ( IllegalStateException e ) { }

	// If an error has been encountered during 2pc,
	// we must report it accordingly. I believe this
	// supercedes reporting a system error;
	switch ( _heuristic ) {
	case HEURISTIC_ROLLBACK:
	    // Transaction was completed rolled back at the
	    // request of one of it's resources. We don't
	    // get to this point if it has been marked for
	    // roll back.
	    throw new HeuristicRollbackException( Messages.message( "tyrex.tx.heuristicRollback" ) );
	case HEURISTIC_MIXED:
	    // Transaction has partially commited and partially
	    // rolledback.
	    throw new HeuristicMixedException( Messages.message( "tyrex.tx.heuristicMixed" ) );
	case HEURISTIC_HAZARD:
	    throw new HeuristicMixedException( Messages.message( "tyrex.tx.heuristicHazard" ) );
	case HEURISTIC_COMMIT:
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
	EnlistedXAResource xaRes;

	// --------------------------------
	// General state checks
	// --------------------------------

	// Proper notification for transactions that timed out.
	if ( _timedOut )
	    throw new IllegalStateException( Messages.message( "tyrex.tx.timedOut" ) );

	// Perform the rollback, pass IllegalStateException to
	// the caller, ignore the returned heuristics.
	internalRollback();
 
 	// The transaction will now tell all it's resources to
	// forget about the transaction and will release all
	// held resources. Also notifies all the synchronization
	// objects that the transaction has completed with a status.
	try {
	    forget();
	} catch ( IllegalStateException e ) { }
	
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
     * <li>{@link #HEURISTIC_READONLY} There were no resources for this
     * transaction, or all resources are read only -- there is no need to
     * commit/rollback this transaction
     * <li>{@link #HEURISTIC_COMMIT} All resources are prepared and those
     * with a false {@link EnlistedXAResource#readOnly} need to be commited.
     * <li>{@link #HEURISTIC_ROLLBACK} The transaction has been marked for
     * rollback, an error has occured or at least one resource failed to
     * prepare and there were no resources that commited
     * <li>{@link #HEURISTIC_MIXED} Some resources have already commited,
     * others have either rolledback or failed to commit, or we got an
     * error in the process: all resources must be rolledback
     * </ul>
     *
     * @throws IllegalStateException The transaction is not active or
     *   is in the process of being commited
     */
    protected void prepare()
	throws IllegalStateException, RollbackException
    {
	EnlistedXAResource xaRes;
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
	    _heuristic = HEURISTIC_ROLLBACK;
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

        // Check whether current thread is owner of this transaction,
        // or a special thread that is allowed to commit/rollback the
        // transaction. Must be performed after checking whether the
        // transaciton is active. Inactive transactions have no owners.
	if ( ! TransactionServer.isOwner( getTopLevel(), Thread.currentThread() ) )
	    throw new SecurityException( Messages.message( "tyrex.tx.threadNotOwner" ) );
  
	// We begin by having no heuristics at all, but during
	// the process we might reach a conclusion to have a
	// commit or rollback heuristic.
	_heuristic = HEURISTIC_READONLY;
	_status = STATUS_PREPARING;
	committing = 0;

	// First, notify all the synchronization objects that
	// we are about to complete a transaction. They might
	// decide to roll back the transaction, in which case
	// we'll do a rollback.
	// might decide to rollback the transaction.
	if ( _syncs != null ) {
	    TransactionManager tm;
	    Transaction        suspended;
	    Synchronization    sync;
	    
	    // If we are not running in the same thread as
	    // this transaction, need to make this transaction
	    // the current one before calling method.
	    tm = new TransactionManagerImpl();
	    suspended = null;
	    try {
		if ( tm.getTransaction() !=  this ) {
		    suspended = tm.suspend();
		    tm.resume( this );
		}
	    } catch ( Exception except ) {
		_status = STATUS_MARKED_ROLLBACK;
		error( except );
	    }
	    
	    for ( i = 0 ; i < _syncs.length ; ++i ) {
		// Do not notify of completion if we already
		// decided to roll back the transaction.
		if ( _status == STATUS_MARKED_ROLLBACK )
		    break;
		
		sync = _syncs[ i ];
		try {
		    sync.beforeCompletion();
		} catch ( Exception except ) {
		    error( except );
		    _status = STATUS_MARKED_ROLLBACK;
		}
	    }
	    
	    // Resume the previous transaction associated with
	    // the thread.
	    if ( suspended != null ) {
		try {
		    tm.suspend();
		} catch ( Exception except ) {
		    error( except );
		}
		try {
		    tm.resume( suspended );
		} catch ( Exception except ) {
		    _status = STATUS_MARKED_ROLLBACK;
		    error( except );
		}
	    }
	    
	    if ( _status == STATUS_MARKED_ROLLBACK ) {
		// Status was changed to rollback or an error occured,
		// either case we have a heuristic decision to rollback.
		_heuristic = HEURISTIC_ROLLBACK;
		return;
	    }
	}


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
		if ( _heuristic != HEURISTIC_READONLY && _heuristic != HEURISTIC_COMMIT )
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
			_heuristic = _heuristic | HEURISTIC_ROLLBACK;
			_resources[ i ] = null;
		    }
		} catch ( HeuristicMixed except ) {
		    // Resource indicated mixed/hazard heuristic, so the
		    // entire transaction is mixed heuristics.
		    _heuristic = _heuristic | HEURISTIC_MIXED;
		} catch ( HeuristicHazard except ) {
		    _heuristic = _heuristic | HEURISTIC_HAZARD;
		} catch ( Exception except ) {
		    if ( except instanceof TRANSACTION_ROLLEDBACK )
			_heuristic = _heuristic | HEURISTIC_ROLLBACK;
		    else {
			_heuristic = _heuristic | HEURISTIC_HAZARD;
			error( except );
		    }
		}
	    }	
	}


	// If there are any resources, perform two phase commit on them.
	// We always end these resources, even if we made a heuristic
	// decision not to commit this transaction.
	if ( _enlisted != null ) {

	    // Tell all the XAResources that their transaction
	    // has ended successfuly. Notice handling of
	    // suspended resources.
	    for ( i = 0 ; i < _enlisted.length ; ++i ) {
		xaRes = _enlisted[ i ];
		try {
		    xaRes.xa.end( xaRes.xid, XAResource.TMSUCCESS );
		} catch ( XAException except ) {
		    // Error occured, we won't be commiting this transaction.
		    _heuristic = _heuristic | HEURISTIC_ROLLBACK;
		    error( except );
		} catch ( Exception except ) {
		    // Error occured, we won't be commiting this transaction.
		    _heuristic = _heuristic | HEURISTIC_ROLLBACK;
		    error( except );
		}
	    }

	    // Prepare all the resources that we are about to commit.
	    // Shared resources do not need preparation, they will not
	    // be commited/rolledback directly. Read-only resources
	    // will not be commited/rolled back.
	    for ( i = 0 ; i < _enlisted.length ; ++i ) {

		// If at least one resource failed to prepare, we will
		// not prepare the remaining resources, but rollback.
		if ( _heuristic != HEURISTIC_READONLY && _heuristic != HEURISTIC_COMMIT )
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
		    if ( except.errorCode == XAException.XA_HEURMIX )
			_heuristic = _heuristic | HEURISTIC_MIXED; 
		    if ( except.errorCode == XAException.XA_HEURHAZ )
			_heuristic = _heuristic | HEURISTIC_HAZARD; 
		    else if ( ( except.errorCode == XAException.XA_HEURRB ||
				except.errorCode == XAException.XA_RBTIMEOUT ) )
			_heuristic = _heuristic | HEURISTIC_ROLLBACK;
		    else if ( except.errorCode == XAException.XA_HEURCOM )
			_heuristic = _heuristic | HEURISTIC_COMMIT;
		    else if ( except.errorCode >= XAException.XA_RBBASE &&
			      except.errorCode <= XAException.XA_RBEND )
			_heuristic = _heuristic | HEURISTIC_HAZARD;
		    else {
			// Any error will cause us to rollback the entire
			// transaction or at least the remaining part of it.
			_heuristic = _heuristic | HEURISTIC_HAZARD;
			error( except );
		    }
		}  catch ( Exception except ) {
		    // Any error will cause us to rollback the entire
		    // transaction or at least the remaining part of it.
		    _heuristic = _heuristic | HEURISTIC_HAZARD;
		    error( except );
		}
	    }
	}
	_status = STATUS_PREPARED;
	// We make a heuristic decision to commit only if we made no other
	// heuristic decision during perparation and we have at least
	// one resource interested in committing.
	if ( _heuristic == HEURISTIC_READONLY  && committing > 0 )
	    _heuristic = HEURISTIC_COMMIT;
	else
	    _heuristic = normalize( _heuristic );
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
     * <li>{@link #HEURISTIC_COMMIT} All resources were commited
     * successfuly
     * <li>{@link #HEURISTIC_ROLLBACK} No resources were commited
     * successfuly, all resources were rolledback successfuly
     * <li>{@link #HEURISTIC_MIXED} Some resources have commited,
     * others have rolled back
     * </ul>
     *
     * @throws IllegalStateException Transaction has not been prepared
     */
    protected void internalCommit()
	throws IllegalStateException
    {
	EnlistedXAResource xaRes;
	int                i;
	Resource           resource;

	// If already committed we just return. The previous heuristic
	// is still remembered.
	if ( _status == STATUS_COMMITTED || _status == STATUS_ROLLEDBACK )
	    return;

	// We should never reach this state unless transaction has been
	// prepared first.
	if ( _status != STATUS_PREPARED )
	    throw new IllegalStateException( Messages.message( "tyrex.tx.notPrepared" ) );

	// 2PC: Phase two - commit

	// Transaction has been prepared fully or partially.
	// If at least one resource requested roll back (or
	// indicated mixed heuristics) we'll roll back all resources.
	// We start as read-only until at least one resource indicates
	// it actually commited.
	_status = STATUS_COMMITTING;
	_heuristic = HEURISTIC_READONLY;

	// We deal with OTS (remote transactions and subtransactions)
	// first because we expect a higher likelyhood of failure over
	// there, and we can easly recover over here.
	if ( _resources != null ) {
	    for ( i = 0 ; i < _resources.length ; ++i ) {
		resource = _resources[ i ];
		if ( resource == null )
		    continue;
		try {
		    resource.commit();
		    // At least one resource commited, we are either
		    // commit or mixed.
		    _heuristic = _heuristic | HEURISTIC_COMMIT;
		} catch ( HeuristicMixed except ) {
		    _heuristic = _heuristic | HEURISTIC_MIXED;
		} catch ( HeuristicHazard except ) {
		    _heuristic = _heuristic | HEURISTIC_HAZARD;
		} catch ( HeuristicRollback except ) {
		    _heuristic = _heuristic | HEURISTIC_ROLLBACK;
		} catch ( Exception except ) {
		    if ( except instanceof TRANSACTION_ROLLEDBACK )
			_heuristic = _heuristic | HEURISTIC_ROLLBACK;
		    else {
			_heuristic = _heuristic | HEURISTIC_HAZARD;
			error( except );
		    }
		}
	    }
	}


	if (  _enlisted != null ) {
	    for ( i = 0 ; i < _enlisted.length ; ++i ) {
		xaRes = _enlisted[ i ];
		try {
		    // Shared resources and read-only resources
		    // are not commited.
		    if ( ! xaRes.shared && ! xaRes.readOnly ) {
			xaRes.xa.commit( xaRes.xid, false );
			// At least one resource commited, we are either
			// commit or mixed.
			_heuristic = _heuristic | HEURISTIC_COMMIT;
		    }
		} catch ( XAException except ) {
		    if ( except.errorCode == XAException.XA_HEURMIX )
			_heuristic = _heuristic | HEURISTIC_MIXED;
		    else if ( except.errorCode == XAException.XA_HEURHAZ )
			_heuristic = _heuristic | HEURISTIC_HAZARD;
		    else if ( except.errorCode == XAException.XA_HEURRB )
			_heuristic = _heuristic | HEURISTIC_ROLLBACK;
		    else if ( except.errorCode >= XAException.XA_RBBASE &&
			      except.errorCode <= XAException.XA_RBEND )
			_heuristic = _heuristic | HEURISTIC_ROLLBACK;
		    else {
			// This is an error in the resource manager which
			// we need to report.
			_heuristic = _heuristic | HEURISTIC_HAZARD;
			error( except );
		    }
		} catch ( Exception except ) {
		    // Any error will cause us to rollback the entire
		    // transaction or at least the remaining part of it.
		    _heuristic = _heuristic | HEURISTIC_HAZARD;
		    error( except );
		}
	    }
	}
	 
	_status = STATUS_COMMITTED;
	
	_heuristic = normalize( _heuristic );
	TransactionServer.logTransaction( _xid, _heuristic );
    }


    /**
     * Called to perform the actual rollback on the transaction.
     * Will force a rollback on all the enlisted resources and return
     * the heuristic decision of the rollback. Multiple calls are
     * supported.
     * <p>
     * The heuristic decision can be any of the following:
     * <ul>
     * <li>{@link #HEURISTIC_READONLY} There were no resources for this
     * transaction, or all resources are read only -- there is no need to
     * commit/rollback this transaction
     * <li>{@link #HEURISTIC_COMMIT} All resources have decided to commit
     * <li>{@link #HEURISTIC_ROLLBACK} All resources have rolled back
     * (except for read-only resources)
     * <li>{@link #HEURISTIC_MIXED} Some resources have already commited,
     * others have rolled back
     * </ul>
     *
     * @throws IllegalStateException The transaction is not active
     */
    protected void internalRollback()
    {
	int                i;
	EnlistedXAResource xaRes;
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
	    _heuristic = HEURISTIC_ROLLBACK;
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

        // Check whether current thread is owner of this transaction,
        // or a special thread that is allowed to commit/rollback the
        // transaction. Must be performed after checking whether the
        // transaciton is active. Inactive transactions have no owners.
	if ( ! TransactionServer.isOwner( getTopLevel(), Thread.currentThread() ) )
	    throw new SecurityException( Messages.message( "tyrex.tx.threadNotOwner" ) );

	// If we got to this point, we'll start rolling back the
	// transaction. Change the status immediately, so the
	// transaction cannot be altered by a synchronization
	// or XA resource. Our initial heuristic is read-only,
	// since unless there's at least one rollback resource,
	// we never truely rollback.
	_status = STATUS_ROLLING_BACK;
	_heuristic = HEURISTIC_READONLY;

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
		    _heuristic = _heuristic | HEURISTIC_ROLLBACK;
		    _resources[ i ] = null;
		} catch ( HeuristicMixed except ) {
		    _heuristic = _heuristic | HEURISTIC_MIXED;
		} catch ( HeuristicHazard except ) {
		    _heuristic = _heuristic | HEURISTIC_HAZARD;
		} catch ( HeuristicCommit except ) {
		    _heuristic = _heuristic | HEURISTIC_COMMIT;
		} catch ( Exception except ) {
		    _heuristic = _heuristic | HEURISTIC_HAZARD;
		    error( except );
		}
	    }
	}


	if ( _enlisted != null ) {
	    // Tell all the XAResources that their transaction
	    // has ended with a failure. Notice handling of
	    // suspended resource.
	    for ( i = 0 ; i < _enlisted.length ; ++i ) {
		xaRes = _enlisted[ i ];
		try {
		    xaRes.xa.end( xaRes.xid, XAResource.TMSUCCESS );
		} catch ( XAException except ) {
		    if ( except.errorCode <= XAException.XA_RBBASE ||
			 except.errorCode >= XAException.XA_RBEND ) {
			_heuristic = _heuristic | HEURISTIC_HAZARD;
			error( except );
		    }
		} catch ( Exception except ) {
		    _heuristic = _heuristic | HEURISTIC_HAZARD;
		    error( except );
		}
	    }
	    // Rollback each of the resources, regardless of
	    // error conditions. Shared resources do not require
	    // rollback.
	    for ( i = 0 ; i < _enlisted.length ; ++i ) {
		xaRes = _enlisted[ i ];
		try {
		    if ( ! xaRes.shared && ! xaRes.readOnly ) {
			xaRes.xa.rollback( xaRes.xid );
			// Initially we're readonly so we switch to rollback.
			// If we happen to be in commit, we switch to mixed.
			_heuristic = _heuristic | HEURISTIC_ROLLBACK;
		    }
		} catch ( XAException except ) {
		    if ( except.errorCode == XAException.XA_HEURMIX )
			_heuristic = _heuristic | HEURISTIC_MIXED;
		    else if ( except.errorCode == XAException.XA_HEURHAZ )
			_heuristic = _heuristic | HEURISTIC_HAZARD;
		    else if ( except.errorCode == XAException.XA_HEURCOM )
			_heuristic = _heuristic | HEURISTIC_COMMIT;
		    else if ( except.errorCode == XAException.XA_RDONLY )
			// Resource was read only, we don't care.
			    ;
		    else if ( except.errorCode <= XAException.XA_RBBASE ||
			      except.errorCode >= XAException.XA_RBEND ) {
			_heuristic = _heuristic | HEURISTIC_HAZARD;
			error( except );
		    }
		} catch ( Exception except ) {
		    _heuristic = _heuristic | HEURISTIC_HAZARD;
		    error( except );
		}
	    }

	}

	_status = STATUS_ROLLEDBACK;
	_heuristic = normalize( _heuristic );
	TransactionServer.logTransaction( _xid, _heuristic );
    }


    /**
     * Called to forget about the transaction at the end of either
     * a commit or rollback. This method servers three purposes.
     * First, it will tell all the resources to forget about the
     * transaction and release them. Second, it will notify all
     * the synchronziation objects that the transaction has completed
     * with the transaction's status. Last, it will release any
     * objects held by the transaction and dissocaite it from the
     * list of available transactions.
     * 
     * @throws IllegalStateException The transaction has not commited
     *   or rolledback yet
     */
    protected void forget()
	throws IllegalStateException
    {
	int                i;
	EnlistedXAResource xaRes;
	Transaction        suspended;
	TransactionManager tm;
	Synchronization    sync;
	Resource           resource;

	if ( _status != STATUS_COMMITTED && _status != STATUS_ROLLEDBACK )
	    throw new IllegalStateException( Messages.message( "tyrex.tx.cannotForget" ) );

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
		    error( except );
		} catch ( Exception except ) {
		    error( except );
		}
		xaRes.xa = null;
		xaRes.xid = null;
		_enlisted[ i ] = null;
	    }
	    _enlisted = null;
	}

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
	    _resources = null;
	}

	// Notify all the synchronization objects that the
	// transaction has completed with a status.
	if ( _syncs != null ) {

	    // If we are not running in the same thread as
	    // this transaction, need to make this transaction
	    // the current one before calling method.
	    tm = new TransactionManagerImpl();
	    suspended = null;
	    try {
		if ( tm.getTransaction() !=  this ) {
		    suspended = tm.suspend();
		    tm.resume( this );
		}
	    } catch ( Exception except ) {
		error( except );
	    }

	    for ( i = 0 ; i < _syncs.length ; ++i ) {
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
		try {
		    tm.suspend();
		} catch ( Exception except ) {
		    error( except );
		}
		try {
		    tm.resume( suspended );
		} catch ( Exception except ) {
		    error( except );
		}
	    }
	}

	// Only top level transaction is registered with the
	// transaction server and should be unlisted.
	if ( _parent == null )
	    TransactionServer.forgetTransaction( this );
    }


    public synchronized boolean delistResource( XAResource xa, int flag )
	throws IllegalStateException, SystemException
    {
	EnlistedXAResource xaRes;
	EnlistedXAResource shared;
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
		xaRes.suspended = true;
		return true;
	    } catch ( XAException except ) {
		return false;
	    } catch ( Exception except ) {
		throw new SystemException( except.toString() );
	    }

	case XAResource.TMSUCCESS:
	    // If we got the success flag, we have nothing further to do.
	    // If the transaction commits or updates the resource will be
	    // notified of the commit/rollback in the proper manner.
	    return true;


	case XAResource.TMFAIL:
	    // If we got the fail flag, the resource has failed (e.g.
	    // the JDBC connection died), we simply end the resource
	    // with a failure and remove it from the list. We will
	    // never need to commit or rollback this resource.
	    try {
		if ( _enlisted.length == 1 )
		    _enlisted = null;
		else {
		    EnlistedXAResource[] newList;

		    _enlisted[ i ] = _enlisted[ _enlisted.length - 1 ];
		    newList = new EnlistedXAResource[ _enlisted.length - 1 ];
		    System.arraycopy( _enlisted, 0, newList, 0, _enlisted.length - 1 );
		    _enlisted = newList;
		}
		xaRes.xa.end( xaRes.xid, XAResource.TMFAIL );
		xaRes.xa = null;
		xaRes.xid = null;
		return true;
	    } catch ( XAException except ) {
		return false;
	    } catch ( Exception except ) {
		throw new SystemException( except.toString() );
	    } finally {

		// If the resource is not shared, there might be another
		// resource sharing the same transaction, the other one
		// must become not shared (but only one of possible many).
		if ( ! xaRes.shared ) {
		    for ( i = 0 ; i < _enlisted.length ; ++i ) {
			shared = _enlisted[ i ];
			if ( shared.shared && shared.xid == xaRes.xid ) {
			    shared.shared = false;
			    break;
			}
		    }
		}

	    }
	default:
	    throw new IllegalArgumentException( Messages.message( "tyrex.tx.invalidFlag" ) );
	}
    }


    public synchronized boolean enlistResource( XAResource xa )
	throws IllegalStateException, SystemException, RollbackException
    {
	EnlistedXAResource xaRes;
	EnlistedXAResource shared;
	int                i;
	EnlistedXAResource[] newList;


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
		    if ( xaRes.suspended ) {
			try {
			    xaRes.xa.start( xaRes.xid, XAResource.TMRESUME );
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

	    // Check to see whether we have two resources sharing the same
	    // resource manager, in which case use one Xid for both.
	    for ( i = 0 ; i < _enlisted.length ; ++i ) {
		shared = _enlisted[ i ];
		try {
		    if ( shared.xa.isSameRM( xa ) ) {
			xaRes = new EnlistedXAResource();
			xaRes.xa = xa;
			xaRes.shared = true;
			xaRes.xid = shared.xid;
			try {
			    xaRes.xa.start( xaRes.xid, XAResource.TMJOIN );
			    newList = new EnlistedXAResource[ _enlisted.length + 1 ];
			    System.arraycopy( _enlisted, 0, newList, 0, _enlisted.length );
			    newList[ _enlisted.length ] = xaRes;
			    _enlisted = newList;
			    return true;
			} catch ( XAException except ) {
			    return false;
			} catch ( Exception except ) {
			    throw new SystemException( except.toString() );
			}
		    }
		} catch ( XAException except ) {
		    throw new SystemException( except.toString() );
		}
	    }
	}

	// If we got to this point, this is a new resource that
	// is being enlisted. We need to create a new branch Xid
	// and to enlist it.
	xaRes = new EnlistedXAResource();
	xaRes.xa = xa;
	xaRes.xid = _xid.newBranch();
	try {
	    xa.start( xaRes.xid, XAResource.TMNOFLAGS );
	    if ( _enlisted == null ) {
		_enlisted = new EnlistedXAResource[ 1 ];
		_enlisted[ 0 ] = xaRes;
	    } else {
		newList = new EnlistedXAResource[ _enlisted.length + 1 ];
		System.arraycopy( _enlisted, 0, newList, 0, _enlisted.length );
		newList[ _enlisted.length ] = xaRes;
		_enlisted = newList;
	    }
	    return true;
	} catch ( XAException except ) {
	    return false;
	} catch ( Exception except ) {
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
	TransactionServer.logMessage( _xid.toString() + " : " + except.toString() );
	if ( except instanceof RuntimeException &&
	     TransactionServer.getConfigure().getLogWriter() != null )
	    except.printStackTrace( TransactionServer.getConfigure().getLogWriter() );

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
	if ( ( heuristic & HEURISTIC_HAZARD ) != 0 )
	    return HEURISTIC_HAZARD;
	else if ( ( heuristic & HEURISTIC_MIXED ) != 0 )
	    return HEURISTIC_MIXED;
	else if ( heuristic == ( HEURISTIC_COMMIT | HEURISTIC_ROLLBACK ) )
	    return HEURISTIC_MIXED;
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
     * this method will return {@link #HEURISTIC_READONLY}.
     *
     * @return The heuristic decision of this transaction
     */
    int getHeuristic()
    {
	return _heuristic;
    }


    /**
     * Called by {@link TransactionServer} to change the timeout for
     * the transaction's resources to the new value. This might or
     * might not have an effect on the underlying resources.
     * All consistency checks are made by the server.
     *
     * @param secods The new timeout in seconds
     * @see TransactionServer#setTransactionTimeout
     */
    void setTransactionTimeout( int seconds )
    {
	EnlistedXAResource xaRes;
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

	// Perform the rollback, ignore the returned heuristics.
	internalRollback();
 
 	// The transaction will now tell all it's resources to
	// forget about the transaction and will release all
	// held resources. Also notifies all the synchronization
	// objects that the transaction has completed with a status.
	try {
	    forget();
	} catch ( IllegalStateException e ) { }
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
		if ( _enlisted[ i ].suspended )
		    resList[ index ] = resList[ index ] + " [suspended]";
		++index;
	    }
	if ( _resources != null )
	    for ( i = 0 ; i < _resources.length ; ++i ) {
		resList[ index ] = _resources[ i ].toString();
		++index;
	    }
	return resList;
    }


}


/**
 * Describes an {@link XAResource} enlisted with this transaction.
 * Each resource enlisted with the transaction will have such a record
 * until the transaction timesout or is forgetted. The only way to
 * delist a resource is if it fails.
 */
class EnlistedXAResource
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
     * If the resource has been suspended this flag will be set,
     * but the resource will not be removed from the list.
     * We will have to resume the transaction on the resource
     * before we can commit/rollback the transaction.
     */
    boolean suspended;
    
    
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



