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
 * $Id: CoordinatorImpl.java,v 1.3 2001/03/17 03:34:54 arkin Exp $
 */


package tyrex.tm.impl;


import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions._CoordinatorImplBase;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.TransIdentity;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.SubtransactionAwareResource;
import org.omg.CosTransactions.NotSubtransaction;
import org.omg.CosTransactions.Synchronization;
import org.omg.CosTransactions.SynchronizationUnavailable;
import org.omg.CosTransactions.otid_t;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import javax.transaction.xa.Xid;


/**
 * Implements a {@link Coordinator} interface into a transaction.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2001/03/17 03:34:54 $
 */
final class CoordinatorImpl
    extends _CoordinatorImplBase
    implements Coordinator
{


    /**
     * The underlying transaction to which this coordinator serves
     * as an interface.
     */
    private final TransactionImpl    _tx;


    /**
     * The list of parents of this transaction, the immediate parent
     * at index 0 and the top level parent at index n-1. If this
     * transaction is a top level, this variable is null.
     */
    private final TransIdentity[]    _parents;


    /**
     * The control that returned this coordinator.
     */
    private final ControlImpl        _control;


    CoordinatorImpl( ControlImpl control )
    {
        if ( control == null )
            throw new IllegalArgumentException( "Argument control is null" );
        _control = control;
        _tx = _control._tx;
        _parents = _control._parents;
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
        if ( hash_transaction() == coord.hash_transaction() )
            return true;
        return false;
    }


    public boolean is_related_transaction( Coordinator coord )
    {
        if ( _parents == null )
            return is_same_transaction( coord );
        for ( int i = _parents.length ; i-- > 0 ; ++i )
            if ( _parents[ i ].coord.is_ancestor_transaction( coord ) )
                return true;
        return false;
    }


    public boolean is_ancestor_transaction( Coordinator coord )
    {
        return coord.is_descendant_transaction( this );
    }


    public boolean is_descendant_transaction( Coordinator coord )
    {
       if ( _parents == null )
           return false;
       for ( int i = _parents.length ; i-- > 0 ; )
           if ( _parents[ i ].coord.is_same_transaction( coord ) )
               return true;
       return false;
    }


    public boolean is_top_level_transaction( Coordinator coord )
    {
        return ( _parents == null );
    }


    public int hash_transaction()
    {
        return _tx._hashCode;
    }


    public int hash_top_level_tran()
    {
        if ( _parents == null )
            return _tx._hashCode;
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
        // !!! Should this create a new recovery object?
        return _control;
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
            tx = _tx._txDomain.createTransaction( _tx, 0 );
            return tx.getControl();
        } catch ( SystemException except ) {
            throw new Inactive();
        }
    }


    public PropagationContext get_txcontext()
    {
        return _control.getPropagationContext();
    }


    public Status replay_completion( Resource resource )
    {
        return get_status();
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
 

    /**
     * Synchronization wrapper necessary to feed an OTS synchronization
     * object into a JTA transaction.
     *
     * Caveat: TransactionImpl accepts multiple synchronization
     * registration and consolidates them based on reference equality.
     * This means that registering the same OTS synchronization twice
     * will lead to double notification.
     */
    private static class SynhronizationWrapper
        implements javax.transaction.Synchronization
    {
        
        
        /**
         * The OTS synchronization object to which JTA synchronization
         * events will be directed.
         */
        private final Synchronization _sync;
        
        
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
            _sync.after_completion( CoordinatorImpl.fromJTAStatus( status ) );
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
    private static class SubtransactionAwareWrapper
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
        private final SubtransactionAwareResource _aware;
        
        
        /**
         * The subtransaction which performed the registration.
         * This reference will be passed on the before-completion
         * notification.
         */
        private final Coordinator                 _parent;
        
        
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


}
