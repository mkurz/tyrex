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
 * $Id: ControlImpl.java,v 1.3 2001/01/11 23:26:33 jdaniel Exp $
 */


package tyrex.tm;


import org.omg.CosTransactions.*;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.xa.Xid;


/**
 * Implements a {@link Control} interface into a transaction.
 * Transactions are implemented strictly by {@link TransactionImpl},
 * however when using the OTS API or communicating with other CORBA
 * servers it is necessary to use the control interface. This object
 * serves as lightweight adapter between the transaction and control
 * interface.
 * <p>
 * Control objects are produced directly only by {@link TransactionImpl}
 * and indirectly by {@link TransactionFactory}.
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2001/01/11 23:26:33 $
 * @see TransactionImpl
 *
 * Changes 
 *
 * J. Daniel : Changed code to be compliant with CORBA developing rules.
 */
public class ControlImpl
    extends _ControlImplBase
    implements org.omg.CosTransactions.Terminator, Coordinator, RecoveryCoordinator
{


    /**
     * The underlying transaction to which this control serves
     * as an interface.
     */
    private TransactionImpl    _tx;


    /**
     * The list of parents of this transaction, the immediate parent
     * at index 0 and the top level parent at index n-1. If this
     * transaction is a top level, this variable is null.
     */
    private TransIdentity[]    _parents;


    /**
     * The propagation context created from this control.
     * Set when the propagation context is first requested and
     * held for subsequent requests.
     */
    private PropagationContext _pgContext;

    // <------------------- CORBA Parts -------------------->
    /**
     * To be used as a CORBA object, the control object must be
     * activated by  its object adapter. In this implementation, we are
     * using BOA. To provide more flexibility we only used the ORB interface
     * to connect and disconnect CORBA objects.
     */
    private org.omg.CORBA.ORB _orb;
    
    /**
     * CORBA Reference to the terminator interface
     */
    private org.omg.CosTransactions.Terminator _terminator;
    
    /**
     * CORBA Reference to the coordinator interface
     */
    private org.omg.CosTransactions.Coordinator _coordinator;
    // </------------------- CORBA Parts -------------------->
    
    /**
     * Creates a new control for a transaction that has been imported
     * using the specified propagation context. The local transaction
     * has it's own Xid, but no parents. The control has the parent
     * list passed through the propagation.
     *
     * @param tx The local transaction
     * @param pgContext The propagation context
     */
    ControlImpl( TransactionImpl tx, PropagationContext pgContext )
    {
	_tx = tx;
	// We have one more parent than the propagation context,
	// the creator is a parent of ours and the transaction
	// has already been registered as one of its resources.
	_parents = new TransIdentity[ pgContext.parents.length + 1 ];
	System.arraycopy( pgContext.parents, 0, _parents, 1, pgContext.parents.length );
	_parents[ 0 ] = pgContext.current;
    }


    /**
     * Creates a new control for a local transaction that could be
     * used to propagate the transaction to a different server.
     */
    ControlImpl( TransactionImpl tx )
    {
	ControlImpl  parent;

	_tx = tx;
	// We need to create a list of parent identities based
	// on the parents of the transaction.
	if ( tx.getParent() != null ) {
	    tx = tx.getParent();
	    parent = tx.getControl();
	    if ( parent._parents == null ) {
		// Parent is a top-level one, this control has
		// one parent.
		_parents = new TransIdentity[ 1 ];
		_parents[ 0 ] = parent.get_identity();
	    } else {
		// Parent is not a top level one, copy the list
		// of its parents and add itself as the first one.
		_parents = new TransIdentity[ parent._parents.length + 1 ];
		System.arraycopy( parent._parents, 0, _parents, 1, parent._parents.length );
		_parents[ 0 ] = parent.get_identity();
	    }
	}
    }   

    public void setORB( org.omg.CORBA.ORB orb )
    {
        _orb = orb;
    }
    
    public org.omg.CosTransactions.Terminator get_terminator()
	throws Unavailable
    {
	int status;

	// This object is returned as the terminator, but only
	// if the transaction is active.
	status = _tx.getStatus();
	if ( status == javax.transaction.Status.STATUS_ACTIVE ||
	     status == javax.transaction.Status.STATUS_MARKED_ROLLBACK )
	    return terminator();
	throw new Unavailable();
    }
    
    /**
     * ----------------------------- CORBA ------------------------------------
     * This operation is used to return the CORBA reference for the terminator
     * interface. As an object implementation cannot inherit from several
     * CORBA skeletons, this class doesn't inherit from the Terminator 
     * interface skeleton. So, we need to use the delegation approach.
     * If the delegation object is not previously created this operation creates
     * it.
     */
    private org.omg.CosTransactions.Terminator terminator()
    {
        if ( _orb == null )
            return this;
        
        if ( _terminator != null )
            return _terminator;
        
        _terminator = new org.omg.CosTransactions.TerminatorTie( this );
        
        _orb.connect( _terminator );
        
        return _terminator;
    }

    public Coordinator get_coordinator()
	throws Unavailable
    {
	int status;

	// This object is returned as the coordinator, but only
	// if the transaction is active.
	status = _tx.getStatus();
	if ( status == javax.transaction.Status.STATUS_ACTIVE ||
	     status == javax.transaction.Status.STATUS_MARKED_ROLLBACK )
	    return coordinator();
	throw new Unavailable();
    }
    
    /**
     * ----------------------------- CORBA ------------------------------------
     * This operation is used to return the CORBA reference for the terminator
     * interface. As an object implementation cannot inherit from several
     * CORBA skeletons, this class doesn't inherit from the Terminator 
     * interface skeleton. So, we need to use the delegation approach.
     * If the delegation object is not previously created this operation creates
     * it.
     */
    private org.omg.CosTransactions.Coordinator coordinator()
    {
        if ( _orb == null )
            return this;
        
        if ( _coordinator != null )
            return _coordinator;
        
        _coordinator = new org.omg.CosTransactions.CoordinatorTie( this );
        
        _orb.connect( _coordinator );
        
        return _coordinator;
    }

    public void commit( boolean reportHeuristic )
	throws HeuristicMixed, HeuristicHazard
    {        
	// No heuristics are reported on subtransaction completion.
	if ( _parents != null )
	    reportHeuristic = false;
	try {
	    _tx.commit();
	} catch ( IllegalStateException except ) {
	    throw new INVALID_TRANSACTION( except.getMessage() );
	} catch ( SystemException except ) {
	    if ( reportHeuristic )
		throw new HeuristicHazard();
	} catch ( RollbackException except ) {
	    throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
	} catch ( HeuristicRollbackException except ) {
	    if ( reportHeuristic )
		throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
	} catch ( HeuristicMixedException except ) {
	    if ( reportHeuristic )
		throw new HeuristicMixed();
	} catch ( SecurityException except ) {
            except.printStackTrace();
	    if ( reportHeuristic )
		throw new HeuristicHazard();
	    throw new INVALID_TRANSACTION( except.toString() );
        } finally {
            deactivate();
        }
    }


    public void rollback()
    {
	try {
	    _tx.rollback();
	} catch ( IllegalStateException except ) {
	    throw new INVALID_TRANSACTION( except.getMessage() );
	} catch ( SystemException except ) {
	    throw new INVALID_TRANSACTION( except.toString() );
	} finally {
            deactivate();
        }
    }


    public Status get_parent_status()
    {
	if ( _parents == null )
	    return fromJTAStatus( _tx.getStatus() );
	else
	    return _parents[ 0 ].coord.get_status();
    }


    public Status get_status()
    {
	return fromJTAStatus( _tx.getStatus() );
    }


    public Status get_top_level_status()
    {
	if ( _parents == null )
	    return fromJTAStatus( _tx.getStatus() );
	else
	    return _parents[ _parents.length - 1 ].coord.get_status();
    }


    public boolean is_top_level_transaction()
    {
	return ( _parents == null );
    }


    public boolean is_same_transaction( Coordinator coord )
    {
	return ( ( coord instanceof ControlImpl ) &&
		 ( (ControlImpl) coord )._tx.equals( _tx ) );
    }


    public boolean is_related_transaction( Coordinator coord )
    {
	int i;

	if ( _parents == null )
	    return is_same_transaction( coord );
	for ( i = 0 ; i < _parents.length ; ++i )
	    if ( _parents[ i ].coord.is_ancestor_transaction( coord ) )
		return true;
	return false;
    }


    public boolean is_ancestor_transaction( Coordinator coord )
    {
	int i;

	if ( _parents == null )
	    return false;
	for ( i = 0 ; i < _parents.length ; ++i )
	    if ( _parents[ i ].coord.is_same_transaction( coord ) )
		return true;
	return false;
    }


    public boolean is_descendant_transaction( Coordinator coord )
    {
	return coord.is_ancestor_transaction( this );
    }


    public boolean is_top_level_transaction( Coordinator coord )
    {
	return ( _parents == null );
    }


    public int hash_transaction()
    {
	return _tx.hashCode();
    }


    public int hash_top_level_tran()
    {
	if ( _parents == null )
	    return _tx.hashCode();
	else
	    return _parents[ _parents.length - 1 ].coord.hash_transaction();
    }


    public RecoveryCoordinator register_resource( Resource resource )
	throws Inactive
    {
	try {
	    _tx.registerResource( resource );
	    if ( resource instanceof SubtransactionAwareResource )
		_tx.registerSynchronization( new SubtransactionAwareWrapper( (SubtransactionAwareResource) resource, this ) ); 
	} catch ( RollbackException except ) {
	    throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
	} catch ( IllegalStateException except ) {
	    throw new Inactive();
	} catch ( SystemException except ) {
	    throw new Inactive();
	}
	return this;
    }


    public void register_subtran_aware( SubtransactionAwareResource resource )
	throws Inactive, NotSubtransaction
    {
	if ( _parents == null )
	    throw new NotSubtransaction();
	try {
	    _tx.registerSynchronization( new SubtransactionAwareWrapper( resource, this ) ); 
	} catch ( RollbackException except ) {
	    throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
	} catch ( IllegalStateException except ) {
	    throw new Inactive();
	} catch ( SystemException except ) {
	    throw new Inactive();
	}
    }
    

    public void register_synchronization( Synchronization sync )
	throws Inactive, SynchronizationUnavailable
    {
	try {
	    _tx.registerSynchronization( new SynhronizationWrapper( sync ) );
	} catch ( RollbackException except ) {
	    throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
	} catch ( IllegalStateException except ) {
	    throw new Inactive();
	} catch ( SystemException except ) {
	    throw new Inactive();
	}
    }


    public void rollback_only()
	throws Inactive
    {
	try {
	    _tx.setRollbackOnly();
	} catch ( IllegalStateException except ) {
	    throw new Inactive();
	} catch ( SystemException except ) {
	    throw new Inactive();
	}
    }


    public String get_transaction_name()
    {
	return _tx.toString();
    }


    public Control create_subtransaction()
	throws SubtransactionsUnavailable, Inactive
    {
	TransactionImpl tx;
	TransIdentity[] parents;

	if ( _tx.getStatus() != javax.transaction.Status.STATUS_ACTIVE &&
	     _tx.getStatus() != javax.transaction.Status.STATUS_MARKED_ROLLBACK )
	    throw new Inactive();

	try {
	    tx = _tx.getTransactionDomain().createTransaction( _tx, null );
	    return tx.getControl();
	} catch ( SystemException except ) {
	    throw new Inactive();
	}
    }


    /**
     * ----------------------------- CORBA --------------------------------
     * This operation has been modified to return CORBA compliant values.
     */
    public synchronized PropagationContext get_txcontext()
    {
        if ( _pgContext != null )
            return _pgContext;
        
        if ( _orb == null )
        {
	    _pgContext = new PropagationContext( _tx.getTransactionDomain().getTransactionTimeout( _tx ),
                    get_identity(), _parents != null ? _parents : new TransIdentity[ 0 ], null );	    
        }
        else
        {
            org.omg.CORBA.Any any = _orb.create_any();
            
            _pgContext = new PropagationContext( _tx.getTransactionDomain().getTransactionTimeout( _tx ),
                    get_identity(), _parents != null ? _parents : new TransIdentity[ 0 ], any );
        }
        
        return _pgContext;
    }

    /**
     * ---------------------------- CORBA -----------------------------
     * This operation is used to deactivate all CORBA objects. It is used
     * when a transaction is ended ( rolled back or commited ).
     */
    private void deactivate()
    {
        if ( _orb != null )
        {
            _orb.disconnect( this );
            _orb.disconnect( _coordinator );
            _orb.disconnect( _terminator );
        }
    }
    
    public Status replay_completion( Resource resource )
    {
	return get_status();
    }


    TransIdentity get_identity()
    {
	Xid     xid;
	otid_t  otid;
	byte[]  branch;

	xid = _tx.getXid();
	branch = xid.getGlobalTransactionId();
	otid = new otid_t( xid.getFormatId(), branch.length, branch );
	return new TransIdentity( coordinator(), terminator(), otid );
    }


    /**
     * Return the transaction object which this control interface
     * represents.
     */
    TransactionImpl getTransaction()
    {
	return _tx;
    }


    /**
     * Convert JTA transaction statuc code into OTS transaction code.
     */
    static Status fromJTAStatus( int status )
    {
	switch ( status ) {
	case javax.transaction.Status.STATUS_ACTIVE:
	    return Status.StatusActive;
	case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
	    return Status.StatusMarkedRollback;
	case javax.transaction.Status.STATUS_COMMITTING:
	    return Status.StatusCommitting;
	case javax.transaction.Status.STATUS_COMMITTED:
	    return Status.StatusCommitted;
	case javax.transaction.Status.STATUS_ROLLING_BACK:
	    return Status.StatusRollingBack;
	case javax.transaction.Status.STATUS_ROLLEDBACK:
	    return Status.StatusRolledBack;
	case javax.transaction.Status.STATUS_PREPARED:
	    return Status.StatusPrepared;
	case javax.transaction.Status.STATUS_PREPARING:
	    return Status.StatusPreparing;
	case javax.transaction.Status.STATUS_NO_TRANSACTION:
	    return Status.StatusNoTransaction;
	case javax.transaction.Status.STATUS_UNKNOWN:
	default:
	    return Status.StatusUnknown;
	}
    }
 
}


/**
 * Synchronization wrapper necessary to feed an OTS synchronization
 * object into a JTA transaction.
 *
 * Caveat: TransactionImpl accepts multiple synchronization
 * registration and consolidates them based on reference equality.
 * This means that registering the same OTS synchronization twice
 * will lead to double notification.
 */
class SynhronizationWrapper
    implements javax.transaction.Synchronization
{


    /**
     * The OTS synchronization object to which JTA synchronization
     * events will be directed.
     */
    private Synchronization _sync;


    /**
     * Construct a JTA synchronization listener which will direct
     * notifications to the underlying OTS synchronization object.
     *
     * @param sync The OTS synchronization object to which JTA
     *   synchronization events will be directed
     */
    SynhronizationWrapper( Synchronization sync )
    {
	_sync = sync;
    }

    
    public void afterCompletion( int status )
    {
	_sync.after_completion( ControlImpl.fromJTAStatus( status ) );
    }


    public void beforeCompletion()
    {
	_sync.before_completion();
    }   

}


/**
 * Synchronization wrapper necessary to feed an OTS subtransaction
 * aware resource into a JTA transaction as a synchronization.
 * {@link TransactionImpl} can notify of subtransaction commit and
 * rollback only through the sychronization mechanism. This happens
 * to match well to {@link SubtransactionAwareResource} needs.
 *
 * Caveat: TransactionImpl accepts multiple synchronization
 * registration and consolidates them based on reference equality.
 * This means that registering the same OTS subtransaction aware
 * resource twice will lead to double notification.
 */
class SubtransactionAwareWrapper
    implements javax.transaction.Synchronization
{


    // IMPLEMENTATION NOTES:
    //
    //   The OTS resource wants to know immediately as the
    //   subtransaction is being committed or rolledback.
    //
    //   In the current implementation, commit on a subtransaction
    //   not from its parent will only engage the preparation
    //   stage. Therefore the notification has to be done at the
    //   point of beforeCompletion.
    //
    //   Rollback will always be done fully and will report
    //   afterCompletion. Since commit will also report
    //   afterCompletion, we conditionally report only a rollback.


    /**
     * The subtransaction aware resource to which subtransaction
     * events will be directed.
     */
    private SubtransactionAwareResource _aware;


    /**
     * The subtransaction which performed the registration.
     * This reference will be passed on the before-completion
     * notification.
     */
    private Coordinator                 _parent;


    /**
     * Construct a JTA synchronization listener which will direct
     * notifications to the underlying OTS subtransaction aware
     * resource.
     *
     * @param aware The subtransaction aware resource to which
     *   subtransaction events will be directed.
     * @param parent The subtransaction which performed the
     *   registration
     */
    SubtransactionAwareWrapper( SubtransactionAwareResource aware,
				Coordinator parent )
    {
	_aware = aware;
	_parent = parent;
    }


    public void beforeCompletion()
    {
	// beforeCompletion will be called during preperation of the
	// subtransaction. We still get a chance to mark it for roll back.
	_aware.commit_subtransaction( _parent );
    }


    public void afterCompletion( int status )
    {
	// afterCompletion will be called after the subtransaction has
	// been rolledback as a subtransaction (rolledback status) or
	// after it has been commited as part of the parent transaction's
	// 2pc, in which case we have nothing new to notify.
	if ( status == javax.transaction.Status.STATUS_ROLLEDBACK )
	    _aware.rollback_subtransaction();
    }
    
}
