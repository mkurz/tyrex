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
 * $Id: TransactionImpl.java,v 1.35 2001/09/24 22:27:32 mohammed Exp $
 */


package tyrex.tm.impl;


import java.rmi.RemoteException;
import javax.transaction.Transaction;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;
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
import org.omg.CORBA.ORB;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import tyrex.tm.Heuristic;
import tyrex.tm.TyrexTransaction;
import tyrex.tm.xid.BaseXid;
import tyrex.tm.xid.XidUtils;
import tyrex.services.Clock;
import tyrex.util.Messages;


/**
 * Implements a global transaction. This transaction supports X/A
 * resources (see {@link XAResource}), can be part of an OTS global
 * transaction (see {@link ResourceImpl}) and can contain OTS
 * subtransactions (see {@link Resource}). Tightly integrated with
 * {@link TransactionManagerImpl} and {@link TransactionServer}.
 * <P>
 * Synchronizations are called in the reverse order in which
 * they are added.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.35 $ $Date: 2001/09/24 22:27:32 $
 * @see XAResourceHolder
 * @see TransactionManagerImpl
 * @see TransactionDomain
 * @see ResourceImpl
 */
final class TransactionImpl
    implements TyrexTransaction, Status
{


    /**
     * The transaction identifier. This must be of type {@link BaseXid}.
     */
    protected final BaseXid            _xid;


    /**
     * The transaction hash code derived from the transaction identifier.
     */
    protected final int                _hashCode;


    /**
     * Holds a list of all the synchronization objects. This array is null
     * if there are no synchronization. The array size may be larger than
     * the actual number of registered synchronization, in which case all
     * empty elements are consecutive at the end of the array.
     */
    private Synchronization[]          _syncs;


    /**
     * Reference to the first enlisted resource (single linked list).
     */
    private XAResourceHolder           _enlisted;


    /**
     * Reference to the first delisted resource (single linked list).
     */
    private XAResourceHolder           _delisted;


    /**
     * Holds a list of all the enlisted OTS resources. This array is null
     * if there are no resources and may contain empty entries.
     */
    private Resource[]                 _resources;


    /**
     * Holds the current status of the transaction.
     */
    protected int                      _status;


    /**
     * Held during a commit/rollback process to indicate that
     * an unexpected error occured. Will throw that exception
     * if there is no other more important exception to report
     * (e.g. RollbackException).
     */
    protected SystemException          _sysError;


    /**
     * True if this transaction has been rolled back due to timeout.
     */
    private boolean                    _timedOut;


    /**
     * If this transaction is a local recreation of a remote OTS
     * transaction, this variable will reference the propagation
     * context used to recreate this transaction. If this
     * transaction was created locally, this variable is null.
     */
    private final PropagationContext   _pgContext;


    /**
     * The heuristic decision made by the transaction after a call to
     * {@link #prepare}, {@link #internalCommit}, {@link #internalRollback}.
     * Held in case the operation is repeated to return a consistent
     * heuristic decision. Defaults to read-only (i.e. no heuristic decision).
     */
    protected int                      _heuristic = Heuristic.READONLY;


    /**
     * If this transaction is used through the OTS API, it will have
     * a control associated with it. The control is created when
     * needed and referenced from here. Most of the time, this
     * variable is null.
     */
    private ControlImpl                _control;


    /**
     * The domain to which this transaction belongs. The domain is notified
     * of the outcome of the transaction and any request to commit/rollback
     * the transaction.
     */
    protected final TransactionDomainImpl  _txDomain;


    /**
     * If this transaction is a subtransaction of some global
     * transaction, this variable will reference the parent transaction.
     * Subtransactions cannot commit or rollback directly, only as
     * nested subtransactions.
     */
    protected final TransactionImpl    _parent;


    /**
     * True if performing two-phase commit on the transaction. If this
     * flag is set, we have recorded the heuristic decision in the recovery
     * log and must complete by recording the transaction outcome and
     * forgeting about it.
     */
    private boolean                    _twoPhase;

    
    /**
     * Indicates when the transaction will timeout as system clock.
     */
    protected long                     _timeout;


    /**
     * Indicates when the transaction started as system clock.
     */
    protected final long               _started;


    /**
     * The next entry in the hashtable maintained by {@link TransactionDomain}.
     */
    protected TransactionImpl          _nextEntry;


    /**
     * Hidden constructor used by {@link TransactionDomain} to create
     * a new transaction. A transaction can only be created through
     * {@link TransactionDomain} or {@link TransactionManager} which
     * take care of several necessary housekeeping duties.
     *
     * @param xid The Xid for this transaction
     * @param parent The parent of this transaction if this is a
     * nested transaction, null if this is a top level transaction
     * @param txDomain The transaction domain
     * @param timeout The timeout for this transaction, in milliseconds
     */
    TransactionImpl( BaseXid xid, TransactionImpl parent,
                     TransactionDomainImpl txDomain, long timeout )
    {
        if ( xid == null )
            throw new IllegalArgumentException( "Argument xid is null" );
        if ( txDomain == null )
            throw new IllegalArgumentException( "Argument txDomain is null" );

        _xid = xid;
        _hashCode = xid.hashCode();
        _txDomain = txDomain;
        _pgContext = null;
        _parent = parent;
        _status = STATUS_ACTIVE;
        _started = Clock.clock();
        _timeout = _started + timeout;

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
        }
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
     * is inactive
     * @param txDomain The transaction domain
     * @param timeout The timeout for this transaction, in milliseconds
     */
    TransactionImpl( BaseXid xid, PropagationContext pgContext,
                     TransactionDomainImpl txDomain, long timeout )
        throws Inactive
    {
        ResourceImpl res;
    
        if ( xid == null )
            throw new IllegalArgumentException( "Argument xid is null" );
        if ( txDomain == null )
            throw new IllegalArgumentException( "Argument txDomain is null" );
        if ( pgContext == null )
            throw new IllegalArgumentException( "Argument pgContext is null" );

        _xid = xid;
        _hashCode = xid.hashCode();
        _txDomain = txDomain;
        _pgContext = pgContext;
        _parent = null;
        _status = STATUS_ACTIVE;
        _started = Clock.clock();
        _timeout = _started + timeout;
        
        // If this transaction is a local copy of a remote
        // transaction, we register it as a resource with the
        // remote transaction.
        try {
            res = new ResourceImpl( this );
            if ( _txDomain._orb != null )
                _txDomain._orb.connect( res );
            _pgContext.current.coord.register_resource( res );
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
     * Hidden constructor used by {@link TransactionDomain} to recreate
     * a transaction during recovery. The transaction state and heuristic
     * decision are restored from information available in the recovery log.
     *
     * @param xid The Xid for this transaction
     * @param heuristic The recorded heuristic decision
     * @param txDomain The transaction domain
     */
    TransactionImpl( BaseXid xid, int heuristic, TransactionDomainImpl txDomain )
    {
        if ( xid == null )
            throw new IllegalArgumentException( "Argument xid is null" );
        if ( txDomain == null )
            throw new IllegalArgumentException( "Argument txDomain is null" );
        _xid = xid;
        _hashCode = xid.hashCode();
        _txDomain = txDomain;
        _pgContext = null;
        _parent = null;
        _started = 0;
        _timeout = 0;

        // Determine the heuristic decision, status and system error for the transaction.
        switch ( heuristic ) {
        case XAResource.XA_OK:
        case XAResource.XA_RDONLY:
            // XA_OK/XA_RDONLY indicates transaction has been prepared
            // and is read to commit. We will need to make the transaction
            // active in order to reach a new heuristic decision again.
            _heuristic = Heuristic.COMMIT;
            _status = Status.STATUS_PREPARED;
            break;
        case XAException.XA_HEURCOM:
            // XA_HEURCOM indicates the transaction has committed.
            _heuristic = Heuristic.COMMIT;
            _status = Status.STATUS_COMMITTED;
            break;
        case XAException.XA_HEURRB:
            // XA_HEURRB indicates the transaction has rolledback.
            _heuristic = Heuristic.ROLLBACK;
            _status = Status.STATUS_ROLLEDBACK;
            break;
        case XAException.XA_HEURMIX:
            // XA_HEURMIX indicates mixed heuristic, we assume rollback.
            _heuristic = Heuristic.MIXED;
            _status = Status.STATUS_ROLLEDBACK;
            break;
        case XAException.XA_HEURHAZ:
            // XA_HEURHAZ indicates hazard heuristic, we assume rollback.
            _heuristic = Heuristic.HAZARD;
            _status = Status.STATUS_ROLLEDBACK;
            break;
        case XAException.XA_RBTIMEOUT:
            // XA_RBTIMEOUT indicates transaction rolled due to timeout.
            _heuristic = Heuristic.TIMEOUT;
            _status = Status.STATUS_ROLLEDBACK;
            break;
        default:
            // Any other XA_RB* value indicates transaction has rolled
            // back due to some error. We keep the error code handy.
            if ( heuristic >= XAException.XA_RBBASE && heuristic <= XAException.XA_RBEND )
                _heuristic = Heuristic.ROLLBACK;
            else
                _heuristic = Heuristic.OTHER;
            _sysError = new SystemException( Util.getHeuristic( heuristic ) );
            _status = Status.STATUS_ROLLEDBACK;
            break;
        }

        // This transaction is recovered, we must set the two-phase
        // commit flag as true in order to record the outcome of
        // the recovery in the tranaction journal.
        _twoPhase = true;
    }


    public String toString()
    {
        return _xid.toString();
    }


    public int hashCode()
    {
        return _hashCode;
    }


    public boolean equals( Object other )
    {
        // By design choice there will only be one transaction object
        // per unique transaction (identifier).
        return ( this == other );
    }


    //-------------------------------------------------------------------------
    // Methods defined in JTA Transaction
    //-------------------------------------------------------------------------


    public int getStatus()
    {
        // Easiest method to write!
        return _status;
    }

    
    public synchronized void registerSynchronization( Synchronization sync )
        throws RollbackException, IllegalStateException, SystemException
    {
        Synchronization[] syncs;
        int               length;

        if ( sync == null )
            throw new IllegalArgumentException( "Argument sync is null" );

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
            _syncs = new Synchronization[ 2 ];
            _syncs[ 0 ] = sync;
        } else {
            // In many cases we will get duplicity in synchronization
            // registration, but we don't want to fire duplicate events.
            syncs = _syncs;
            length = syncs.length;
            for ( int i = 0 ; i < length ; ++i ) {
                if ( syncs[ i ] == sync )
                    return;
                else if ( syncs[ i ] == null ) {
                    syncs[ i ] = sync;
                    return;
                }
            }
            syncs = new Synchronization[ length * 2 ];
            for ( int i = length ; i-- > 0 ; )
                syncs[ i ] = _syncs[ i ];
            syncs[ length ] = sync;
            _syncs = syncs;
        }
    }


    public void commit() // removed synchronized for locking between different thread
        throws  RollbackException, HeuristicMixedException, HeuristicRollbackException,
                SecurityException, SystemException
    {
        if ( _status == STATUS_MARKED_ROLLBACK ) {
            // Status was changed to rollback or an error occured,
            // either case we have a heuristic decision to rollback.
            try {
                rollback();
            } catch ( Exception except ) { }
            throw new RollbackException( Messages.message( "tyrex.tx.rolledback" ) );
        }
        if ( _status == STATUS_ROLLEDBACK || _status == STATUS_ROLLING_BACK )
            throw new RollbackException( Messages.message( "tyrex.tx.rolledback" ) );
        // If this is a subtransaction, it cannot commit directly,
        // only once the parent transaction commits through the
        // {@link #internalCommit} call. We simply do nothing after
        // preperation. The heuristic decision will be remembered.
        if ( _parent != null || _pgContext != null )
            return;
        commit( canUseOnePhaseCommit() );
    }


    public synchronized void rollback()
        throws IllegalStateException, SystemException
    {       
        // Perform the rollback, pass IllegalStateException to
        // the caller, ignore the returned heuristics.
        try {
            internalRollback();
        } finally {
            _txDomain.notifyRollback( this );
            // The transaction will now tell all it's resources to
            // forget about the transaction and will release all
            // held resources. Also notifies all the synchronization
            // objects that the transaction has completed with a status.
            try {
                forget( Heuristic.ROLLBACK );
            } catch ( IllegalStateException except ) {
                // This should never happen
                _txDomain._category.error( "Internal error", except );
            }
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


    public synchronized boolean enlistResource( XAResource xaResource )
        throws IllegalStateException, SystemException, RollbackException
    {
        XAResourceHolder resHolder;
        XAResourceHolder previousResHolder;
        
        if ( xaResource == null )
            throw new IllegalArgumentException( "Argument xaResource is null" );
        
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
            resHolder = _enlisted;
            previousResHolder = null;
            while ( resHolder != null ) {
                if ( resHolder._xaResource == xaResource ) {
                    if ( resHolder._endFlag == XAResource.TMSUSPEND ) {
                        try {
                            if (true) {
                                System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.start(XAResource.TMRESUME) " + xaResource + " with xid " + resHolder._xid);    
                            }

                            xaResource.start( resHolder._xid, XAResource.TMRESUME );
                            resHolder._endFlag = XAResource.TMNOFLAGS;

                            return true;
                        } catch ( XAException except ) {
                            except.printStackTrace();
                            throw new NestedSystemException( except );
                        } catch ( Exception except ) {
                            except.printStackTrace();
                            throw new NestedSystemException( except );
                        }
                    } else {
                        if (true) {
                            System.out.println(Thread.currentThread() + "Transaction " + toString() + " Transaction.enlist() " + xaResource + " with xid " + resHolder._xid + " failed because xaresource is alread enlisted.");    
                        }
                        return false;
                    }
                }
                previousResHolder = resHolder;
                resHolder = resHolder._nextHolder;
            }

            // check the delisted
            resHolder = _delisted;

            while ( resHolder != null ) {
                if ( resHolder._xaResource == xaResource ) {
                    if ( resHolder._endFlag == XAResource.TMSUCCESS ) {
                        try {
                            if (true) {
                                System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.start(XAResource.TMJOIN) " + xaResource + " with xid " + resHolder._xid + " because XAResource.end(TMSUCCESS) has already been called.");    
                            }

                            xaResource.start( resHolder._xid, XAResource.TMJOIN );
                            resHolder._endFlag = XAResource.TMNOFLAGS;

                            if ( null == previousResHolder ) {
                                _delisted = resHolder._nextHolder;    
                            } else {
                                previousResHolder._nextHolder = resHolder._nextHolder;
                            }

                            if ( null == _enlisted ) {
                                resHolder._nextHolder = null;
                            }
                            else {
                                resHolder._nextHolder = _enlisted._nextHolder;
                            }
                            
                            _enlisted = resHolder;    
                            return true;
                        } catch ( Exception except ) {
                            except.printStackTrace();
                            throw new NestedSystemException( except );
                        }
                    } else {
                        if (true) {
                            System.out.println(Thread.currentThread() + "Transaction " + toString() + " Transaction.enlist() " + xaResource + " with xid " + resHolder._xid + " failed because xaresource is failed.");    
                        }
                        return false;
                    }
                }
                resHolder = resHolder._nextHolder;
            }
        }
        return addNewResource( xaResource );
    }


    public synchronized boolean delistResource( XAResource xaResource, int flag )
        throws IllegalStateException, SystemException
    {
        XAResourceHolder resHolder;
        XAResourceHolder lastHolder;
    
        if ( xaResource == null )
            throw new IllegalArgumentException( "Argument xaResource is null" );

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
        
        // Look up the enlisted resource. If the resource is not
        // enlisted, return false.
        resHolder = _enlisted;
        lastHolder = null;
        while( resHolder != null ) {
            if ( resHolder._xaResource == xaResource )
                break;
            lastHolder = resHolder;
            resHolder = resHolder._nextHolder;
        }
        if ( resHolder == null )
            return false;

        switch ( flag ) {
        case XAResource.TMSUSPEND:
            // If the resource is being suspended, we simply suspend
            // it and return. The resource will be resumed when we
            // commit/rollback, i.e. it's not removed from the
            // list of enlisted resources.
            try {
                if (true) {
                    System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.end(XAResource.TMSUSPEND) " + resHolder._xaResource + " with xid " + resHolder._xid);    
                }

                xaResource.end( resHolder._xid, XAResource.TMSUSPEND );
                resHolder._endFlag = XAResource.TMSUSPEND;
                return true;
            } catch ( XAException except ) {
                return false;
            } catch ( Exception except ) {
                throw new NestedSystemException( except );
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
                if ( lastHolder == null )
                    _enlisted = resHolder._nextHolder;
                else
                    lastHolder._nextHolder = resHolder._nextHolder;
                
                if ( flag == XAResource.TMFAIL ) {
                    if (true) {
                        System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.end(XAResource.TMFAIL) called " + xaResource + " with xid " + resHolder._xid);    
                    }

                    xaResource.end( resHolder._xid, XAResource.TMFAIL );
                }
                else {
                    if (true) {
                        System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.end(XAResource.TMSUCCESS) called " + xaResource + " with xid " + resHolder._xid);    
                    }

                    xaResource.end( resHolder._xid, XAResource.TMSUCCESS );
                    resHolder._nextHolder = _delisted;
                    _delisted = resHolder;
                }
                resHolder._endFlag = flag;
                return true;
            } catch ( XAException except ) {
                return false;
            } catch ( Exception except ) {
                throw new NestedSystemException( except );
            } finally {
                // if this is resource failure set rollback
                if ( flag == XAResource.TMFAIL )
                    setRollbackOnly();    
            }
        default:
            throw new IllegalArgumentException( Messages.message( "tyrex.tx.invalidFlag" ) );
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
     * Change the timeout for the transaction's resources 
     * to the new value. 
     *
     * @param seconds The new timeout in seconds
     * @see TransactionDomain#setTransactionTimeout
     */
    public void setTransactionTimeout( int seconds )
    {
        // The call to the transaction domain eventually calls
        // internalSetTransactionTimeout, but also updates the
        // next timeout for the background thread.
        _txDomain.setTransactionTimeout( this, seconds );
    }


    //-------------------------------------------------------------------------
    // Extended methods supported by TyrexTransaction
    //-------------------------------------------------------------------------


    public synchronized void asyncCommit()
        throws SystemException, SecurityException, RollbackException
    {
        Thread thread;
        
        // Dissociated the tranaction from the current thread,
        // before embarking on asynchronous commit.
        suspendTransaction();
        if ( _status == STATUS_MARKED_ROLLBACK ) {
            // Status was changed to rollback or an error occured,
            // either case we have a heuristic decision to rollback.
            _heuristic = Heuristic.ROLLBACK;
            throw new RollbackException( Messages.message( "tyrex.tx.rolledback" ) );
        }
    
        thread = new Thread( new Runnable() {
                public void run() 
                {
                    boolean exceptionOccurred = false;
                    
                    synchronized ( TransactionImpl.this ) {
                        try {
                            TransactionImpl.this.commit();
                        } catch( Exception e ) {
                            exceptionOccurred = true;
                        }
                    }
                }
            } );
        // enlist the thread
        // _txDomain.enlistThread( this, ThreadContext.getThreadContext( thread ), thread );
        // start the thread
        thread.start();
    }


    public synchronized void asyncRollback()
        throws IllegalStateException, SystemException, SecurityException
    {
        Thread thread;
        
        // Dissociated the tranaction from the current thread,
        // before embarking on asynchronous commit.
        suspendTransaction();
        
        // If a system error occured during this stage, report it.
        if ( null != _sysError )
            throw _sysError;
        
        thread = new Thread( new Runnable() {
                public void run() 
                {
                    boolean exceptionOccurred = false;
                    
                    synchronized ( TransactionImpl.this ) {
                        try {
                            TransactionImpl.this.rollback();
                        } catch( Exception e ) {
                            exceptionOccurred = true;
                        }
                    }
                }
            } );
        // enlist the thread
        _txDomain.enlistThread( this, ThreadContext.getThreadContext( thread ), thread );
        // start the thread
        thread.start();
    }
    

    public  boolean canUseOnePhaseCommit()
    {
        // if there are more than one resource then no one phase
        if ( ( null != _resources ) && ( 1 < _resources.length ) )
            return false;    
        
        // if there are no _enlisted and _delisted then yes
        if ( ( null == _enlisted ) && ( null == _delisted ) )
            return true;
        
        // if there are a mix of xa resources and resources then  no
        if ( ( null != _resources ) && 
             ( ( null != _enlisted ) || ( null != _delisted ) ) )
            return false;
        
        // if there is only one enlisted resource then yes
        if ( ( null != _enlisted ) && 
             ( _enlisted._nextHolder != null ) && 
             ( null == _delisted ) )
            return true;    
        
        // if there is only one delisted resource then yes
        if ( ( null != _delisted ) && 
             ( _delisted._nextHolder != null ) && 
             ( null == _enlisted ) )
            return true;    
        
        // if the same resource manager is used for all the xa
        // resources then yes
        if ( null != _enlisted ) {
            if ( ! areXaResourcesShared( _enlisted, false ) )
                return false;
            if ( null != _delisted )
                return areXaResourcesShared( _delisted, true );    
            return true;
        }
        
        if ( null != _delisted )
            return areXaResourcesShared( _delisted, false );    
        
        return true;
    }


    public synchronized void onePhaseCommit()
        throws RollbackException, HeuristicMixedException,
               HeuristicRollbackException, SecurityException,
               IllegalStateException, SystemException
    {                
        Thread          thread;
        ThreadContext   context;
        
        thread = Thread.currentThread();
        context = ThreadContext.getThreadContext( thread );
        
        if ( _status == STATUS_MARKED_ROLLBACK ) {
            // Status was changed to rollback or an error occured,
            // either case we have a heuristic decision to rollback.
            try {
                rollback();
            } catch ( Exception except ) { }
            throw new RollbackException( Messages.message( "tyrex.tx.rolledback" ) );
        }
        if ( _status == STATUS_ROLLEDBACK || _status == STATUS_ROLLING_BACK )
            throw new RollbackException( Messages.message( "tyrex.tx.rolledback" ) );
        // If this is a subtransaction, it cannot commit directly,
        // only once the parent transaction commits through the
        // {@link #internalCommit} call. We simply do nothing after
        // preperation. The heuristic decision will be remembered.
        if ( _parent != null || _pgContext != null )
            return;
               
        commit( true );
       
        _txDomain.delistThread( context, thread );
    }


    public Transaction getParent()
    {
        return _parent;
    }


    public Transaction getTopLevel()
    {
        if ( _parent == null )
            return this;
        else
            return _parent.getTopLevel();
    }


    public long getTimeout()
    {
        return _timeout;
    }


    public long getStarted()
    {
        return _started;
    }


    public Xid getXid()
    {
        return _xid;
    }


    public synchronized Control getControl()
    {
        if ( _control == null ) {
            _control = new ControlImpl( this );
            if ( _txDomain._orb != null )
                _txDomain._orb.connect( _control );
        }
        return _control;
    }


    //-------------------------------------------------------------------------
    // Methods used by related classes
    //-------------------------------------------------------------------------


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
     * <li>{@link #Heuristic.READONLY} There were no resources for this
     * transaction, or all resources are read only -- there is no need to
     * commit/rollback this transaction
     * <li>{@link #Heuristic.COMMIT} All resources are prepared and those
     * with a false {@link XAResourceHolder#readOnly} need to be commited.
     * <li>{@link #Heuristic.ROLLBACK} The transaction has been marked for
     * rollback, an error has occured or at least one resource failed to
     * prepare and there were no resources that commited
     * <li>{@link #Heuristic.MIXED} Some resources have already commited,
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
        XAResourceHolder resHolder;
        int              committing;
        Resource         resource;
        int              decision;
        
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
            _heuristic = Heuristic.ROLLBACK;
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
    
        // Call before completion on all the registered synchronizations.
        // This happens before the transaction enters the PREPARED state,
        // so synchronizations can enlist new XA/OTS resources. This call
        // may affect the outcome of the transaction and cause it to be
        // marked as rollback-only.
        if ( _syncs != null ) {
            beforeCompletion();
            if ( _status == STATUS_MARKED_ROLLBACK ) {
                _heuristic = Heuristic.ROLLBACK;
                return;
            }
        }
         
        // We begin by having no heuristics at all, but during
        // the process we might reach a conclusion to have a
        // commit or rollback heuristic.
        _heuristic = Heuristic.READONLY;
        _status = STATUS_PREPARING;
        committing = 0;
    
        // We deal with OTS (remote transactions and subtransactions)
        // first because we expect a higher likelyhood of failure over
        // there, and we can easly recover over here.
        if ( _resources != null ) {
            // We are starting with a heuristic decision that is read-only,
            // and no need to commit. At the end we might reach a heuristic
            // decision to rollback, or mixed/hazard, but never commit.
            for ( int i = _resources.length ; i-- > 0 ; ) {
                
                // If at least one resource failed to prepare, we will
                // not prepare the remaining resources, but rollback.
                if ( _heuristic != Heuristic.READONLY && _heuristic != Heuristic.COMMIT )
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
                        _heuristic = _heuristic | Heuristic.ROLLBACK;
                        _resources[ i ] = null;
                    }
                } catch ( HeuristicMixed except ) {
                    // Resource indicated mixed/hazard heuristic, so the
                    // entire transaction is mixed heuristics.
                    _heuristic = _heuristic | Heuristic.MIXED;
                } catch ( HeuristicHazard except ) {
                    _heuristic = _heuristic | Heuristic.HAZARD;
                } catch ( Exception except ) {
                    if ( except instanceof TRANSACTION_ROLLEDBACK )
                        _heuristic = _heuristic | Heuristic.ROLLBACK;
                    else {
                        _heuristic = _heuristic | Heuristic.HAZARD;
                        error( except );
                    }
                }
            }
        }
        
        // If there are any resources, perform two phase commit on them.
        // We always end these resources, even if we made a heuristic
        // decision not to commit this transaction.
        resHolder = _enlisted;
        if ( resHolder != null ) {
            endEnlistedResourcesForCommit();
            
            // Prepare all the resources that we are about to commit.
            // Shared resources do not need preparation, they will not
            // be commited/rolledback directly. Read-only resources
            // will not be commited/rolled back.
            resHolder = _enlisted;
            while ( resHolder != null ) {
                // If at least one resource failed to prepare, we will
                // not prepare the remaining resources, but rollback.
                if ( _heuristic != Heuristic.READONLY && _heuristic != Heuristic.COMMIT )
                    break;
                try {
                    if ( ! resHolder._shared ) {
                        // We do not commit/rollback a read-only resource.
                        // If all resources are read only, we can return
                        // a read-only heuristic.
                        if (true) {
                            System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.prepare called " + resHolder._xaResource + " with xid " + resHolder._xid);    
                        }
                        if ( resHolder._xaResource.prepare( resHolder._xid ) == XAResource.XA_RDONLY ) {
                            if (true) {
                                System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.prepare called " + resHolder._xaResource + " with xid " + resHolder._xid + " voted read-only.");    
                            }
                            resHolder._readOnly = true;
                        }
                        else {
                            if (true) {
                                System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.prepare called " + resHolder._xaResource + " with xid " + resHolder._xid + " voted commit.");    
                            }

                            ++ committing;
                        }
                    }
                    
                    // Note: We will not commit read-only resources,
                    // but if we get a heuristic that requires rollback,
                    // we will rollback all resources including this one.
                    // An error does not change it's state to read-only,
                    // since we can call rollback more than once.
                } catch ( XAException except ) {
                    xaError( resHolder, except );
                }  catch ( Exception except ) {
                    // Any error will cause us to rollback the entire
                    // transaction or at least the remaining part of it.
                    _heuristic = _heuristic | Heuristic.HAZARD;
                    error( except );
                }
                resHolder = resHolder._nextHolder;
            }
        }
        
        resHolder = _delisted;
        if ( resHolder != null ) {
            // Prepare all the resources that we are about to commit.
            // Shared resources do not need preparation, they will not
            // be commited/rolledback directly. Read-only resources
            // will not be commited/rolled back.
            while ( resHolder != null ) {
                
                // If at least one resource failed to prepare, we will
                // not prepare the remaining resources, but rollback.
                if ( _heuristic != Heuristic.READONLY && _heuristic != Heuristic.COMMIT )
                    break;
                
                try {
                    if ( ! resHolder._shared ) {
                        // We do not commit/rollback a read-only resource.
                        // If all resources are read only, we can return
                        // a read-only heuristic.
                        if (true) {
                            System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.prepare called " + resHolder._xaResource + " with xid " + resHolder._xid);    
                        }

                        if ( resHolder._xaResource.prepare( resHolder._xid ) == XAResource.XA_RDONLY ) {
                            if (true) {
                                System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.prepare called " + resHolder._xaResource + " with xid " + resHolder._xid + " voted read-only.");    
                            }

                            resHolder._readOnly = true;
                        }
                        else {
                            if (true) {
                                System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.prepare called " + resHolder._xaResource + " with xid " + resHolder._xid + " voted commit.");    
                            }

                            ++ committing;
                        }
                    }
                    
                    // Note: We will not commit read-only resources,
                    // but if we get a heuristic that requires rollback,
                    // we will rollback all resources including this one.
                    // An error does not change it's state to read-only,
                    // since we can call rollback more than once.
                } catch ( XAException except ) {
                    xaError( resHolder, except );
                }  catch ( Exception except ) {
                    // Any error will cause us to rollback the entire
                    // transaction or at least the remaining part of it.
                    _heuristic = _heuristic | Heuristic.HAZARD;
                    error( except );
                }
                resHolder = resHolder._nextHolder;
            }
        }
        
        _status = STATUS_PREPARED;
        
        // We make a heuristic decision to commit only if we made no other
        // heuristic decision during perparation and we have at least
        // one resource interested in committing.
        if ( _heuristic == Heuristic.READONLY  && committing > 0 )
            _heuristic = Heuristic.COMMIT;
        else
            _heuristic = normalize( _heuristic );
        
        // Must mark transaction as two-phase in order to record decision
        // in recovery log.
        if ( _heuristic != Heuristic.READONLY ) {
            _twoPhase = true;
            if ( _txDomain._journal != null ) {
                switch ( _heuristic ) {
                case Heuristic.COMMIT:
                    decision = XAException.XA_HEURCOM;
                    break;
                case Heuristic.ROLLBACK:
                    decision = XAException.XA_HEURRB;
                    break;
                case Heuristic.MIXED:
                    decision = XAException.XA_HEURMIX;
                    break;
                case Heuristic.HAZARD:
                    decision = XAException.XA_HEURHAZ;
                    break;
                default:
                    decision = XAException.XA_RBOTHER;
                    break;
                }
                try {
                    _txDomain._journal.prepare( _xid, decision );
                } catch ( SystemException except ) {
                    error( except );
                }
            }
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
     * <li>{@link #Heuristic.COMMIT} All resources were commited
     * successfuly
     * <li>{@link #Heuristic.ROLLBACK} No resources were commited
     * successfuly, all resources were rolledback successfuly
     * <li>{@link #Heuristic.MIXED} Some resources have commited,
     * others have rolled back
     * </ul>
     *
     * @param onePhaseCommit True if one phase commit is to be used
     * @throws IllegalStateException Transaction has not been prepared
     */
    protected void internalCommit( boolean onePhaseCommit )
        throws IllegalStateException
    {
        Resource  resource;
        int       decision;
        
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
        _heuristic = Heuristic.READONLY;               
        
        // We deal with OTS (remote transactions and subtransactions)
        // first because we expect a higher likelyhood of failure over
        // there, and we can easly recover over here.
        if ( _resources != null ) {
            for ( int i = _resources.length ; i-- > 0 ; ) {
                resource = _resources[ i ];
                if ( resource == null )
                    continue;
                try {
                    if (onePhaseCommit)
                        resource.commit_one_phase();
                    else
                        resource.commit();
                    
                    // At least one resource commited, we are either
                    // commit or mixed.
                    _heuristic = _heuristic | Heuristic.COMMIT;
                } catch ( HeuristicMixed except ) {
                    _heuristic = _heuristic | Heuristic.MIXED;
                } catch ( HeuristicHazard except ) {
                    _heuristic = _heuristic | Heuristic.HAZARD;
                } catch ( HeuristicRollback except ) {
                    _heuristic = _heuristic | Heuristic.ROLLBACK;
                } catch ( Exception except ) {
                    if ( except instanceof TRANSACTION_ROLLEDBACK )
                        _heuristic = _heuristic | Heuristic.ROLLBACK;
                    else {
                        _heuristic = _heuristic | Heuristic.HAZARD;
                        error( except );
                    }
                }
            }
        }

        if ( _enlisted != null )
            commitXAResources( _enlisted, onePhaseCommit );
        if ( _delisted != null)
            commitXAResources( _delisted, onePhaseCommit );    
        
        _status = STATUS_COMMITTED;
        _heuristic = normalize( _heuristic );
        _txDomain.notifyCompletion( this, _heuristic );

        // We record the transaction only if two-phase commit,
        // or we didn't expect the heuristic decision.
        if ( _txDomain._journal != null ) {
            switch ( _heuristic ) {
            case Heuristic.COMMIT:
                decision = XAException.XA_HEURCOM;
                break;
            case Heuristic.ROLLBACK:
                decision = XAException.XA_HEURRB;
                _twoPhase = true;
                break;
            case Heuristic.MIXED:
                decision = XAException.XA_HEURMIX;
                _twoPhase = true;
                break;
            case Heuristic.HAZARD:
                decision = XAException.XA_HEURHAZ;
                _twoPhase = true;
                break;
            default:
                decision = XAException.XA_RBOTHER;
                _twoPhase = true;
                break;
            }
            if ( _twoPhase ) {
                try {
                    _txDomain._journal.commit( _xid, decision );
                } catch ( SystemException except ) {
                    error( except );
                }
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
     * <li>{@link #Heuristic.READONLY} There were no resources for this
     * transaction, or all resources are read only -- there is no need to
     * commit/rollback this transaction
     * <li>{@link #Heuristic.COMMIT} All resources have decided to commit
     * <li>{@link #Heuristic.ROLLBACK} All resources have rolled back
     * (except for read-only resources)
     * <li>{@link #Heuristic.MIXED} Some resources have already commited,
     * others have rolled back
     * </ul>
     *
     * @throws IllegalStateException The transaction is not active
     */
    protected void internalRollback()
    {
        Resource         resource;
        XAResourceHolder resHolder;

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
            _heuristic = Heuristic.ROLLBACK;
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
    
        // If we got to this point, we'll start rolling back the
        // transaction. Change the status immediately, so the
        // transaction cannot be altered by a synchronization
        // or XA resource. Our initial heuristic is read-only,
        // since unless there's at least one rollback resource,
        // we never truely rollback.
        _status = STATUS_ROLLING_BACK;
        _heuristic = Heuristic.READONLY;
        
        if ( _resources != null ) {
            // Tell all the OTS resources to rollback their transaction
            // regardless of state.
            for ( int i = _resources.length ; i-- > 0 ; ) {
                resource = _resources[ i ];
                if ( resource == null )
                    continue;
                try {
                    resource.rollback();
                    // Initially we're readonly so we switch to rollback.
                    // If we happen to be in commit, we switch to mixed.
                    _heuristic = _heuristic | Heuristic.ROLLBACK;
                    _resources[ i ] = null;
                } catch ( HeuristicMixed except ) {
                    _heuristic = _heuristic | Heuristic.MIXED;
                } catch ( HeuristicHazard except ) {
                    _heuristic = _heuristic | Heuristic.HAZARD;
                } catch ( HeuristicCommit except ) {
                    _heuristic = _heuristic | Heuristic.COMMIT;
                } catch ( Exception except ) {
                    _heuristic = _heuristic | Heuristic.HAZARD;
                    error( except );
                }
            }
        }
        
        resHolder = _enlisted;
        if ( resHolder != null ) {
            while ( resHolder != null ) {
                try {
                    endForTransactionBoundary( resHolder );
                } catch ( XAException except ) {
                    xaError( resHolder, except );
                } catch ( Exception except ) {
                    _heuristic = _heuristic | Heuristic.HAZARD;
                    error( except );
                }
                resHolder = resHolder._nextHolder;
            }
            rollbackXAResources( _enlisted );
        }
        
        if ( _delisted != null)
            rollbackXAResources( _delisted );
        
        _status = STATUS_ROLLEDBACK;
        _heuristic = normalize( _heuristic );
        _txDomain.notifyCompletion( this, _heuristic );
        
        // We record the transaction only if two-phase commit,
        // or we didn't expect the heuristic decision, or
        // the transaction has timed out.
        if ( _txDomain._journal != null ) {
            if ( _timedOut || _heuristic != Heuristic.ROLLBACK )
                _twoPhase = true;
            if ( _twoPhase ) {
                try {
                    _txDomain._journal.rollback( _xid );
                } catch ( SystemException except ) {
                    error( except );
                }
            }
        }
    }


    /**
     * Suspend the resources associated with the transaction.
     * <P>
     * The resources that are already suspended are not affected.
     */
    protected synchronized void suspendResources()
        throws IllegalStateException, SystemException
    {
        XAResourceHolder resHolder;
        
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

        // Look up the enlisted resource. If the resource is not
        // enlisted, return false.
        resHolder = _enlisted;
        while ( resHolder != null ) {
            // we simply suspend it. The resource will be resumed when we
            // commit/rollback, i.e. it's not removed from the
            // list of enlisted resources.
            if ( resHolder._endFlag == XAResource.TMNOFLAGS ) {
                try {
                    if (true) {
                        System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.end(XAResource.TMSUSPEND) called " + resHolder._xaResource + " with xid " + resHolder._xid);    
                    }

                    resHolder._xaResource.end( resHolder._xid, XAResource.TMSUSPEND );
                    resHolder._endFlag = XAResource.TMSUSPEND;
                } catch ( XAException except ) {
                    xaError( resHolder, except );
                    throw new NestedSystemException( except );
                } catch ( Exception except ) {
                    throw new NestedSystemException( except );
                }
            }
            resHolder = resHolder._nextHolder;
        }
    }


    /**
     * Resume previously suspended resources in the transaction and
     * enlist the new specified resources in the transaction.
     * The resources may already be enlisted.
     * <P>
     * Active resources (ie non-suspended) are not affected.
     *
     * @param xaResources The resources to be enlisted in the transaction,
     * may be bull
     */
    protected synchronized void resumeAndEnlistResources( XAResource[] xaResources )
        throws IllegalStateException, SystemException, RollbackException
    {
        /*
          This is not the cleanest way of performing the operation (this operation
          should be split in two) but it is the most efficient.
        */
        XAResourceHolder resHolder;
        XAResource       xaResource;

        if ( _enlisted != null ) {
        
            // Look if we alredy got the resource enlisted. If the
            // resource was suspended, we just resume it and go out.
            // If the resource was started, we do not enlist it a
            // second time.
            resHolder = _enlisted;
            while ( resHolder != null ) {
                if ( resHolder._endFlag == XAResource.TMSUSPEND ) {
                    try {
                        if (true) {
                            System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.start(XAResource.TMRESUME) " + resHolder._xaResource + " with xid " + resHolder._xid);    
                        }

                        resHolder._xaResource.start( resHolder._xid, XAResource.TMRESUME );
                        resHolder._endFlag = XAResource.TMNOFLAGS;
                    } catch ( XAException except ) {
                        xaError( resHolder, except );
                    } catch ( Exception except ) {
                        throw new NestedSystemException( except );
                    }
                }
                resHolder = resHolder._nextHolder;
            }
        
            if ( null != xaResources ) {
                for ( int i = xaResources.length ; i-- > 0 ; ) {
                    xaResource = xaResources[ i ];
                    resHolder = _enlisted;
                    while ( resHolder != null ) {
                        if ( resHolder._xaResource == xaResource )
                            break;
                        resHolder = resHolder._nextHolder;
                    }
                    if ( resHolder == null )
                        // new resource
                        addNewResource( xaResource );
                }
            }
        } else if ( null != xaResources ) {
            for ( int i = xaResources.length ; i-- > 0 ; )
                addNewResource( xaResources[ i ] );
        }
    }


    /**
     * Called to register an OTS resource with the transaction. Used
     * internally to perform nested transactions and exposed through
     * the OTS interface.
     *
     * @param res The OTS resource to register
     */
    protected synchronized void registerResource( Resource resource )
        throws IllegalStateException
    {
        Resource[] newResources;

        if ( resource == null )
            throw new IllegalArgumentException( "Argument resource is null" );

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
            _resources = new Resource[] { resource };
        } else {
            // It is less likely, but still possible to get
            // duplicity in resource registration.
            for ( int i = _resources.length ; i-- > 0 ; )
                if ( _resources[ i ] == resource )
                    return;
            newResources = new Resource[ _resources.length + 1 ];
            for ( int i = _resources.length ; i-- > 0 ; )
                newResources[ i ] = _resources[ i ];
            newResources[ _resources.length ] = resource;
            _resources = newResources;
        }    
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
    protected int normalize( int heuristic )
    {
        if ( ( heuristic & Heuristic.HAZARD ) != 0 )
            return Heuristic.HAZARD;
        else if ( ( heuristic & Heuristic.MIXED ) != 0 )
            return Heuristic.MIXED;
        else if ( ( heuristic & Heuristic.OTHER ) != 0 )
            return Heuristic.OTHER;    
        else if ( ( heuristic & ( Heuristic.COMMIT + Heuristic.ROLLBACK ) ) == Heuristic.COMMIT + Heuristic.ROLLBACK )
            return Heuristic.MIXED;
        else
            return heuristic;
    }


    /**
     * Returns the heuristic decision of this transaction after it
     * has been prepared, commited or rolledback. At all other times
     * this method will return {@link #Heuristic.READONLY}.
     *
     * @return The heuristic decision of this transaction
     */
    protected int getHeuristic()
    {
        return _heuristic;
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
    protected void internalSetTransactionTimeout( int seconds )
    {
        XAResourceHolder resHolder;
    
        resHolder = _enlisted;
        while ( resHolder != null ) {
            try {
                if (true) {
                    System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.setTransactionTimeout() " + resHolder._xaResource + " with seconds " + seconds);    
                }
                resHolder._xaResource.setTransactionTimeout( seconds );
            } catch ( XAException except  ) {
                // We could care less if we managed to set the
                // timeout on the resource. We have to assume it
                // might not timeout when we expect it anyway.
            }
            resHolder = resHolder._nextHolder;    
        }
    }


    /**
     * Indicates that the transaction has been rolled back due to time out.
     * Automatically performs a rollback on the transaction. We only
     * reach this state if the transaction is active.
     */
    protected synchronized void timedOut()
    {
        if ( ! _timedOut ) {
            // Let the rollback mechanism know that the transaction has failed.
            _timedOut = true;
            try {
                rollback();
            } catch ( Exception except ) { }
        }
    }
    

    /**
     * Returns true if the transaction has timed out and rolled back.
     */
    protected boolean getTimedOut()
    {
        return _timedOut;
    }


    /**
     * Returns the propagation context used to import this
     * transaction or null if the transaction was not imported.
     */
    protected PropagationContext getPropagationContext()
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
    protected synchronized String[] listResources()
    {
        String[]         resList;
        XAResourceHolder resHolder;
        int              index;

        resHolder = _enlisted;
        for ( index = 0 ; resHolder != null ; ++index )
            resHolder = resHolder._nextHolder;
        resList = new String[ index + ( _resources == null ? 0 : _resources.length ) ];
        
        resHolder = _enlisted;
        for ( index = 0 ; resHolder != null ; ++index ) {
            resList[ index ] = resHolder._xaResource.toString();
            if ( resHolder._endFlag != XAResource.TMNOFLAGS )
                resList[ index ] =  resList[ index ] +  "[" + 
                    ( ( resHolder._endFlag == XAResource.TMSUSPEND ? "suspended" 
                        : ( resHolder._endFlag == XAResource.TMSUCCESS ? "ended" : "failed" ) ) ) +  "]";
            resHolder = resHolder._nextHolder;
        }
        if ( _resources != null )
            for ( int i = _resources.length ; i-- > 0 ; ) {
                resList[ index ] = _resources[ i ].toString();
                ++index;
            }
        return resList;
    }
    

    /**
     * Called to end the resources as part of the one phase commit protocol.
     * On entry the status must be either {@link #STATUS_ACTIVE) or {@link
     * #STATUS_MARKED_ROLLBACK).
     * <p>
     * The heuristic decision can be any of the following:
     * <ul>
     * <li>{@link #Heuristic.COMMIT} All resources are ended successfully
     * need to be commited using one phase commit on the resources.
     * <li>{@link #Heuristic.ROLLBACK} The transaction has been marked for
     * rollback, an error has occured or at least one resource failed to
     * end
     * </ul>
     *
     * @throws IllegalStateException The transaction is not active or
     * is in the process of being commited, or prepared for two phase commit.
     */
    protected void endResources()
        throws IllegalStateException, RollbackException
    {
        int         committing;
        Resource    resource;
    
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
            _heuristic = Heuristic.ROLLBACK;
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
    
        // Call before completion on all the registered synchronizations.
        // This happens before the transaction enters the PREPARED state,
        // so synchronizations can enlist new XA/OTS resources. This call
        // may affect the outcome of the transaction and cause it to be
        // marked as rollback-only.
        if ( _syncs != null ) {
            beforeCompletion();
            if ( _status == STATUS_MARKED_ROLLBACK ) {
                _heuristic = Heuristic.ROLLBACK;
                return;
            }
        }

        // We begin by having no heuristics at all, but during
        // the process we might reach a conclusion to have a
        // commit or rollback heuristic.
        _status = STATUS_COMMITTING;
        _heuristic = Heuristic.READONLY;
       
        // We always end these resources, even if we made a heuristic
        // decision not to commit this transaction.
        if ( _enlisted != null )
            endEnlistedResourcesForCommit();

        // if the heuristic has not changed set it to commit
        if ( _heuristic == Heuristic.READONLY )
            _heuristic = Heuristic.COMMIT;    
        else
            _heuristic = normalize( _heuristic );
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
     * The <tt>ignoreHeuristic</tt> argument is used to determine what
     * heuristic outcomes cause forget to be called on the resources
     * associated with the transaction. For instance if transaction
     * commit has been called and the outcome is Heuristic.COMMIT then
     * resource forget should be not be called. In this case resource
     * forget should be called on all other heuristic outcomes like
     * Heuristic.ROLLBACK for instance. Similarly if transaction rollback
     * has been called and the outcome is Heuristic.ROLLBACK then resource
     * forget should not be called. In this case resource forget should
     * be called on all other heuristic outcomes like Heuristic.COMMIT.
     * <p>
     * Forget always gets called if we are doing two-phase commit.
     * 
     * @param ignoreHeuristic the heuristic to ignore
     * @throws IllegalStateException The transaction has not commited
     * or rolledback yet
     * @see HeuristicExceptions
     */
    protected void forget( int ignoreHeuristic )
        throws IllegalStateException
    {
        XAResourceHolder   resHolder;
        Transaction        suspended;
        Resource           resource;
        Synchronization    sync;
        
        if ( _status != STATUS_COMMITTED && _status != STATUS_ROLLEDBACK )
            throw new IllegalStateException( Messages.message( "tyrex.tx.cannotForget" ) );
        
        // only forget the resources if a heuristic exception occured
        if ( ( _heuristic != ignoreHeuristic && _twoPhase ) &&
             ( ( _heuristic == Heuristic.ROLLBACK ) ||
               ( _heuristic == Heuristic.COMMIT ) ||
               ( _heuristic == Heuristic.MIXED ) ||
               ( _heuristic == Heuristic.HAZARD ) ) ) {
            // Tell all the resources to forget about their
            // transaction. Shared resources do not require
            // such a notification.
            resHolder = _enlisted;
            while ( resHolder != null ) {
                try {
                    if ( ! resHolder._shared ) {
                        if (true) {
                            System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.forget() " + resHolder._xaResource + " with xid " + resHolder._xid);    
                        }
                        resHolder._xaResource.forget( resHolder._xid );
                    }
                } catch ( XAException except ) {
                    xaError( resHolder, except );
                } catch ( Exception except ) {
                    error( except );
                }
                resHolder = resHolder._nextHolder;
            }
        
            // Tell all the resources to forget about their
            // transaction. Shared resources do not require
            // such a notification.
            resHolder = _delisted;
            while ( resHolder != null ) {
                try {
                    if ( ! resHolder._shared ) {
                        if (true) {
                            System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.forget() " + resHolder._xaResource + " with xid " + resHolder._xid);    
                        }
                        resHolder._xaResource.forget( resHolder._xid );
                    }
                } catch ( XAException except ) {
                    xaError( resHolder, except );
                } catch ( Exception except ) {
                    error( except );
                }
                resHolder = resHolder._nextHolder;
            }

            if ( _resources != null ) {
                // Tell all the OTS resources to forget about their
                // transaction.
                for ( int i = _resources.length ; i-- > 0 ; ) {
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
            
            for ( int i = 0 ; i < _syncs.length ; ++i ) {
                sync = _syncs[ i ];
                if ( sync == null )
                    break;
                try {
                    sync.afterCompletion( _status );
                } catch ( Exception except ) {
                    error( except );
                }
            }
            _syncs = null;

            // Resume the previous transaction associated with
            // the thread.
            if ( suspended != null )
                resumeTransaction( suspended );
        }
        
        // If two-phase commit, must record completion of
        // transaction in journal.
        if ( _twoPhase && _txDomain._journal != null ) {
            try {
                _txDomain._journal.forget( _xid );
            } catch ( SystemException except ) {
                error( except );
            }
        }

        // Only top level transaction is registered with the
        // transaction server and should be unlisted.
        // This call must occur after the transaction is
        // recorded in the journal, since forgetTransaction
        // will close the journal for the last transaction
        // in a terminating domain.
        if ( _parent == null )
            _txDomain.forgetTransaction( this );
    }


    /**
     * Called during recovery to add a resource. This method is called
     * for every resource that reports a transaction branch during
     * recovery. The resource's Xid is preserved and the resource is
     * not started in the transaction. If an error occurs, the transaction
     * heuristic decision is changed to hazard (not knowing whether the
     * resource commits or rollsback).
     *
     * @param xaResource The XA resource
     * @param xid The Xid for the transaction branch
     */
    protected void addRecovery( XAResource xaResource, Xid xid )
    {
        XAResourceHolder resHolder;
    
        if ( xaResource == null )
            throw new IllegalArgumentException( "Argument xaResource is null" );
        if ( xid == null )
            throw new IllegalArgumentException( "Argument xid is null" );
    
        resHolder = new XAResourceHolder( xaResource, xid, false );
        resHolder._nextHolder = _enlisted;
        resHolder._endFlag = XAResource.TMSUCCESS;
        _enlisted = resHolder;
    }


    //-------------------------------------------------------------------------
    // Implementation details
    //-------------------------------------------------------------------------


    /**
     * Suspend the current transaction. This method is used when performing
     * asynchronous commit or roll back. It dissocaites the transaction from
     * the current thread allowing the thread to proceed while the transaction
     * is being committed/rolledback.
     *
     * @see #asyncCommit asyncCommit
     * @see #asyncRollback asyncRollback
     */
    private void suspendTransaction()
    {
        try {
            // Suspend if this transaction associated with the current thread.
            if ( _txDomain._txManager.getTransaction() == this )
                _txDomain._txManager.suspend();
        } catch ( Exception except ) {
            _status = STATUS_MARKED_ROLLBACK;
            error( except );
        }
    }


    /**
     * Return true if all the resources in the specified XA resource
     * holder list are shared.
     *
     * @param resHolder The XA resource holder list.
     * @param isPreviouslyShared True if other XA resources are shared.
     * @return True if all the resources in the specified
     * XA resource holder list are shared
     */
    private boolean areXaResourcesShared( XAResourceHolder resHolder, 
                                          boolean isPreviouslyShared )
    {
        // true if an XA resource in the specified XA resource
        // list is not shared
        // we can initialize the value of the variable to 
        // value of isPreviouslyShared but that dirties 
        // the logic a bit
        boolean isUnshared = false;

        while ( resHolder != null ) {
            if ( ! resHolder._shared ) {
                // we found another xa res that is not shared
                // with others
                if ( isUnshared || isPreviouslyShared)
                    return false;    
                isUnshared = true;
            }
            resHolder = resHolder._nextHolder;
        }
        return true;
    }


    /**
     * Commit transaction
     *
     * @param canUseOnePhaseCommit True if one-phase commit is used.
     * @throws RollbackException Thrown to indicate that the transaction
     * has been rolled back rather than committed.
     * @throws HeuristicMixedException Thrown to indicate that a heuristic
     * decision was made and that some relevant updates have been committed
     * while others have been rolled back.
     * @throws HeuristicRollbackException Thrown to indicate that a heuristic
     * decision was made and that some relevant updates have been rolled back.
     * @throws SecurityException Thrown to indicate that the thread is not
     * allowed to commit the transaction.
     * @throws IllegalStateException Thrown if the current thread is not
     * associated with a transaction.
     * @throws SystemException Thrown if the transaction manager encounters
     * an unexpected error condition
     */
    private void commit( boolean canUseOnePhaseCommit )
        throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
               SecurityException, SystemException
    {
        try {
            _txDomain.notifyCommit( this );
            
            if ( !canUseOnePhaseCommit )
                // This is two phase commit. Notify the domain about request
                // to commit transaction which might result in RollbackException.
                // If succeeded, attempt to prepare transaction and act based
                // no the return heuristic.
                prepare();
            else
                endResources();
        } catch ( RollbackException except ) {
            _heuristic = Heuristic.ROLLBACK;
        }
        
        // flag to tell forget what heuristic exceptions to look for
        switch ( _heuristic ) {
        case Heuristic.READONLY:
            try {
                // Read only resource does not need either commit, nor rollback
                _txDomain.notifyCompletion( this, _heuristic );
                _status = STATUS_COMMITTED;
                break;
            } finally { 
                // The transaction will now tell all it's resources to
                // forget about the transaction and will release all
                // held resources. Also notifies all the synchronization
                // objects that the transaction has completed with a status.
                try {
                    forget( Heuristic.READONLY );
                } catch ( IllegalStateException except ) { }
            }
            
        case Heuristic.ROLLBACK:
        case Heuristic.MIXED:
        case Heuristic.HAZARD:
        case Heuristic.OTHER:
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
                    forget( Heuristic.ROLLBACK );
                } catch ( IllegalStateException except ) { }
            }
            
        case Heuristic.COMMIT:
        default:
            try {
                internalCommit( canUseOnePhaseCommit );
            } finally { 
                // The transaction will now tell all it's resources to
                // forget about the transaction and will release all
                // held resources. Also notifies all the synchronization
                // objects that the transaction has completed with a status.
                try {
                    forget( Heuristic.COMMIT );
                } catch ( IllegalStateException except ) { }    
            }
        }
        
        // If an error has been encountered during 2pc,
        // we must report it accordingly. I believe this
        // supercedes reporting a system error;
        switch ( _heuristic ) {
        case Heuristic.ROLLBACK:
            // Transaction was completed rolled back at the
            // request of one of it's resources. We don't
            // get to this point if it has been marked for
            // roll back.
            throw new HeuristicRollbackException( Messages.message( "tyrex.tx.heuristicRollback" ) );
        case Heuristic.MIXED:
            // Transaction has partially commited and partially
            // rolledback.
            throw new HeuristicMixedException( Messages.message( "tyrex.tx.heuristicMixed" ) );
        case Heuristic.HAZARD:
            throw new HeuristicMixedException( Messages.message( "tyrex.tx.heuristicHazard" ) );
        case Heuristic.OTHER:
            // if there is a system error throw it
            if ( _sysError != null )
                throw _sysError;
            else
                throw new SystemException("Unknown exception occurred");
        case Heuristic.COMMIT:
        default:
            // Transaction completed successfuly, even if
            // a resource insisted on commiting it.
            // If a system error occured during this stage, report it.
            if ( _sysError != null )
                throw _sysError;
            break;
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
     * if this transaction is not the current transaction
     * of the current thread. Else return null.
     */
    private Transaction makeCurrentTransactionIfNecessary()
    {
        Transaction result = null;

        try {
            if ( _txDomain._txManager.getTransaction() != this ) {
                result = _txDomain._txManager.suspend();
                // could move this out of priviledged block
                // to make priviledged block smaller. not
                // a big deal
                _txDomain._txManager.internalResume( this );    
            }
        } catch ( Exception except ) {
            _status = STATUS_MARKED_ROLLBACK;
            error( except );
        }
        return result;
    }


    /**
     * Make the specified transaction the current 
     * transaction of the current thread.
     *
     * @param transaction the transaction to make as
     * the current transaction of the current thread.
     */
    private void resumeTransaction( final Transaction transaction )
    {
        try {
            _txDomain._txManager.suspend();
        } catch ( Exception except ) {
            error( except );
        }
        try {
            _txDomain._txManager.resume( transaction );
        } catch ( Exception except ) {
            _status = STATUS_MARKED_ROLLBACK;
            error( except );
        }
    }


    /**
     * Inform the Synchronization objects that the transaction
     * completion process is about to start.
     */
    private void beforeCompletion()
    {
        Transaction     suspended;
        Synchronization sync;

        // First, notify all the synchronization objects that
        // we are about to complete a transaction. They might
        // decide to roll back the transaction, in which case
        // we'll do a rollback.
        // might decide to rollback the transaction.
        if ( _syncs != null ) {
            
            // If we are not running in the same thread as
            // this transaction, need to make this transaction
            // the current one before calling method.
            // do in a priveledged block
            suspended = makeCurrentTransactionIfNecessary();
            
            // Do not notify of completion if we already
            // decided to roll back the transaction.
            if ( _status != STATUS_MARKED_ROLLBACK ) {
                for ( int i = 0 ; i < _syncs.length ; ++i ) {
                    sync = _syncs[ i ];
                    if ( sync == null )
                        break;
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
            if ( suspended != null )
                resumeTransaction( suspended );
        
            if ( _status == STATUS_MARKED_ROLLBACK ) {
                // Status was changed to rollback or an error occured,
                // either case we have a heuristic decision to rollback.
                _heuristic = Heuristic.ROLLBACK;
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
     * add {@link Heuristic.ROLLBACK} to the current
     * heuristic. The heuristic is not normalized.
     * @see #_enlisted
     * @see #_heuristic
     * @see #error
     */
    private void endEnlistedResourcesForCommit()
    {
        XAResourceHolder resHolder;

        // We always end these resources, even if we made a heuristic
        // decision not to commit this transaction.
        resHolder = _enlisted;
        while ( resHolder != null ) {
            // Tell all the XAResources that their transaction
            // has ended successfuly. 
            try {
                endForTransactionBoundary( resHolder );
            } catch ( Exception except ) {
                // Error occured, we won't be commiting this transaction.
                _heuristic |= Heuristic.ROLLBACK;
                error( except );
            }
            resHolder = resHolder._nextHolder;
        }
    }


    /**
     * End the work performed by the specified xa resource
     * successfully for a transaction boundary ie commit or rollback
     *
     * @param xaRes the xa resource holder
     * @throws XAException if there is a problem ending the work
     * @throws SystemException if the xa resource is not
     * in the proper state for its work to be ended.
     */
    private void endForTransactionBoundary( XAResourceHolder resHolder )
        throws SystemException, XAException
    {
        if ( ( resHolder._endFlag == XAResource.TMNOFLAGS ) || 
             ( resHolder._endFlag == XAResource.TMSUSPEND ) ) {
            if (resHolder._endFlag == XAResource.TMSUSPEND) {
                if (true) {
                    System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.start(XAResource.TMRESUME) " + resHolder._xaResource + " with xid " + resHolder._xid);    
                }
                resHolder._xaResource.start( resHolder._xid, XAResource.TMRESUME );
            }
            if (true) {
                System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.end(XAResource.TMSUCCESS) " + resHolder._xaResource + " with xid " + resHolder._xid);    
            }
            resHolder._xaResource.end( resHolder._xid, XAResource.TMSUCCESS );
            resHolder._endFlag = XAResource.TMSUCCESS;
        } else if ( resHolder._endFlag != XAResource.TMSUCCESS )
            throw new SystemException( "XA resource is not in the proper state to be ended" );
    }

     
    /**
     * Modify the current heuristic decision of the transaction according
     * to data from the specified exception that occurred.
     * <p>
     * The heuristic is not normalized.
     * 
     * @param resHolder The XA resource holder
     * @param except The XAException that occurred
     */
    private void xaError( XAResourceHolder resHolder, XAException except )
    {
        if ( except.errorCode == XAException.XA_HEURMIX ) {
            _heuristic = _heuristic | Heuristic.MIXED; 
            _txDomain._category.error( "XAResource " + resHolder._xaResource +
                                       " reported mixed heuristic on transaction branch " + resHolder._xid, except );
        } else if ( except.errorCode == XAException.XA_HEURHAZ ) {
            _heuristic = _heuristic | Heuristic.HAZARD; 
            _txDomain._category.error( "XAResource " + resHolder._xaResource +
                                       " reported hazard heuristic on transaction branch " + resHolder._xid, except );
        } else if ( except.errorCode == XAException.XA_RDONLY) {
            ; // ignore    
        } else if ( except.errorCode >= XAException.XA_RBBASE &&
                    except.errorCode <= XAException.XA_RBEND ) {
            _txDomain._category.error( "XAResource " + resHolder._xaResource +
                                       " reported rollback heuristic on transaction branch " + resHolder._xid, except );
            _heuristic = _heuristic | Heuristic.ROLLBACK;
        } else if ( except.errorCode == XAException.XA_HEURCOM ) {
            _heuristic = _heuristic | Heuristic.COMMIT;
            _txDomain._category.error( "XAResource " + resHolder._xaResource +
                                       " reported commit heuristic on transaction branch " + resHolder._xid, except );
        } else {
            // Any error will cause us to rollback the entire
            // transaction or at least the remaining part of it.
            _heuristic = _heuristic | Heuristic.OTHER;
            error( except );
            _txDomain._category.error( "XAResource " + resHolder._xaResource +
                                       " reported error " + Util.getXAException( except ) +
                                       " on transaction branch " + resHolder._xid, except );
        }
    }


    /**
     * Called to commit the specified XA resources.
     *
     * @param resHolder A list of XA resource holders
     * @param onePhaseCommit True if the XA resources are 
     * to be committed using one phase commit
     */
    private void commitXAResources( XAResourceHolder resHolder, boolean onePhaseCommit )
    {
        while ( resHolder != null ) {
            try {      
                // Shared resources and read-only resources
                // are not commited.
                if ( ! resHolder._shared && ! resHolder._readOnly ) {
                    if (true) {
                        System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.commit(" + onePhaseCommit + ") " + resHolder._xaResource + " with xid " + resHolder._xid);    
                    }

                    resHolder._xaResource.commit( resHolder._xid, onePhaseCommit );
                    // At least one resource commited, we are either
                    // commit or mixed.
                    _heuristic = _heuristic | Heuristic.COMMIT;
                }
            } catch ( XAException except ) {
                xaError( resHolder, except );
            } catch ( Exception except ) {
                // Any error will cause us to rollback the entire
                // transaction or at least the remaining part of it.
                _heuristic = _heuristic | Heuristic.HAZARD;
                error( except );
            }
            resHolder = resHolder._nextHolder;
        }
    }


    /**
     * Called to rollback the specified XA resources.
     *
     * @param resHolder A list of XA resource holders
     */
    private void rollbackXAResources( XAResourceHolder resHolder )
    {
        // Rollback each of the resources, regardless of
        // error conditions. Shared resources do not require
        // rollback.
        while ( resHolder != null ) {
            try {
                if ( ! resHolder._shared && ! resHolder._readOnly ) {
                    if (true) {
                        System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.rollback() " + resHolder._xaResource + " with xid " + resHolder._xid);    
                    }

                    resHolder._xaResource.rollback( resHolder._xid );
                    // Initially we're readonly so we switch to rollback.
                    // If we happen to be in commit, we switch to mixed.
                    _heuristic = _heuristic | Heuristic.ROLLBACK;
                }
            } catch ( XAException except ) {
                xaError( resHolder, except );
            } catch ( Exception except ) {
                _heuristic = _heuristic | Heuristic.HAZARD;
                error( except );
            }
            resHolder = resHolder._nextHolder;
        }
    }


    /**
     * Return true if the specified xa resource is shared
     * with the resources in the specified array. If the xa
     * resource is to be shared it is added to the list of
     * enlisted resources.
     *
     * @param xaResource the xa resource
     * @param resHolder A list of XA resource holders.
     * Can be null.
     * @return true if the specified xa resource is shared
     * with the resources in the specified array
     * @throws XAException if there is a problem letting the xa
     * resource join an existing transaction branch
     * @throws SystemException if there is a general problem
     * sharing the resource.
     */
    private boolean shareResource( XAResource xaResource, XAResourceHolder resHolder )
        throws XAException, SystemException
    {
        XAResourceHolder newResHolder;
        Xid              xid;
        boolean          differentBranches;
        XAResourceHelper helper;

        // Check to see whether we have two resources sharing the same
        // resource manager, in which case use one Xid for both.
        try {
            while ( resHolder != null ) {
                if ( !resHolder._shared && resHolder._xaResource.isSameRM( xaResource ) ) {
                    helper = XAResourceHelperManager.getHelper( xaResource );
                    differentBranches = helper.useDifferentBranchesForSharedResources();
                    if ( differentBranches ) {
                        newResHolder = new XAResourceHolder( xaResource, XAResourceHelperManager.getHelper( xaResource ).getXid( XidUtils.newBranch( resHolder._xid ) ), 
                                                             helper.treatDifferentBranchesForSharedResourcesAsShared() );
                    } else {
                        newResHolder = new XAResourceHolder( xaResource, resHolder._xid, true );
                    }
                    
                    if ( differentBranches ) { 
                        if (true) {
                            System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.start(XAResource.TMNOFLAGS) " + resHolder._xaResource + " with xid " + resHolder._xid + " with different branches");    
                        }

                        newResHolder._xaResource.start( newResHolder._xid, XAResource.TMNOFLAGS );
                    } else {
                        if ( XAResource.TMSUSPEND == resHolder._endFlag ) {
                            if (true) {
                                System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.start(XAResource.TMRESUME) " + resHolder._xaResource + " with xid " + resHolder._xid);    
                            }

                            resHolder._xaResource.start( resHolder._xid, XAResource.TMRESUME );
                            resHolder._endFlag = XAResource.TMNOFLAGS;
                        }
                        if (true) {
                            System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.start(XAResource.TMJOIN) " + resHolder._xaResource + " with xid " + resHolder._xid);    
                        }

                        newResHolder._xaResource.start( newResHolder._xid, XAResource.TMJOIN );
                    }
                    newResHolder._nextHolder = _enlisted;
                    _enlisted = newResHolder;
                    return true;
                }
                resHolder = resHolder._nextHolder;
            }
        } catch ( XAException except ) {
            // if this is an XA exception from the isSameRM 
            // method call then return it as a system exception
            if ( ( except.errorCode == XAException.XAER_RMERR ) || 
                 ( except.errorCode == XAException.XAER_RMFAIL ) )
                throw new NestedSystemException( except );    
            throw except;
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
     * @param xa The new resource
     * @return True if the resource has been added.
     */
    private boolean addNewResource( XAResource xaResource )
        throws SystemException, RollbackException
    {
        XAResourceHolder resHolder;
        Xid              xid;
    
        try {
            if ( shareResource( xaResource, _enlisted ) ||
                 shareResource( xaResource, _delisted ) )
                return true;    
        } catch ( XAException except ) {
            return false;
        }
    
        // If we got to this point, this is a new resource that
        // is being enlisted. We need to create a new branch Xid
        // and to enlist it.
        xid = _xid.newBranch();
        try {
            xid = XAResourceHelperManager.getHelper( xaResource ).getXid( xid );
        } catch ( XAException except ) {
            throw new NestedSystemException( except );
        }
        resHolder = new XAResourceHolder( xaResource, xid, false );
        
        try {
            if (true) {
                System.out.println(Thread.currentThread() + "Transaction " + toString() + " XAResource.start(XAResource.TMNOFLAGS) called " + xaResource + " with xid " + resHolder._xid);    
            }

            xaResource.start( xid, XAResource.TMNOFLAGS );
            resHolder._nextHolder = _enlisted;
            _enlisted = resHolder;
            return true;
        } catch ( XAException except ) {
            xaError( resHolder, except );
            return false;
        } catch ( Exception except ) {
            throw new NestedSystemException( except );
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
        Resource[] newResources;

        // Note about equality tests:
        //   We are called to remove a resource R1 previously created
        //   for same transaction, but are called with resource R2
        //   created for the same transaction. The only way to detect
        //   if the two are equivalent is through object equality.

        if ( _resources != null ) {
            if ( _resources.length == 1 && _resources[ 0 ].equals( resource ) )
                _resources = null;
            else
                for ( int i = _resources.length ; i-- > 0 ; ) {
                    if ( _resources[ i ].equals( resource ) ) {
                        _resources[ i ] = _resources[ _resources.length - 1 ];
                        newResources = new Resource[ _resources.length - 1 ];
                        System.arraycopy( _resources, 0, newResources,  0, _resources.length - 1 );
                        _resources = newResources;
                    }
                }
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
        if ( except instanceof RuntimeException )
            _txDomain._category.error( "Error " + except.toString() + " reported in transaction " + _xid, except );
        else
            _txDomain._category.error( "Error " + except.toString() + " reported in transaction " + _xid, except );
       
        // Record the first general exception as a system exception,
        // so it may be returned from commit/rollback.
        if ( _sysError == null ) {
            if ( except instanceof SystemException )
                _sysError = (SystemException) except;
            else
                // For any other error, we produce a SystemException
                // to wrap it up.
                _sysError = new NestedSystemException( except );
        }
    }
    

    /**
     * Describes an {@link XAResource} enlisted with this transaction.
     * Each resource enlisted with the transaction will have such a record
     * until the transaction timesout or is forgetted. The only way to
     * delist a resource is if it fails.
     */
    private static class XAResourceHolder
    {
    

        /**
         * The xid under which this resource is enlisted.
         * Generally each resource will have the same global Xid,
         * but a different branch, but shared resources will also
         * share the same branch. In some cases the Xid implementation
         * is dictated by the RM.
         */
        final Xid         _xid;
    

        /**
         * The enlisted XA resource.
         */
        final XAResource  _xaResource;
    

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
        int               _endFlag = XAResource.TMNOFLAGS;
        
        
        /**
         * A shared resource is one that shares it's transaction
         * branch with another resource (e.g. two JDBC connections
         * to the same database). Only one of the shared resources
         * must be commited or rolled back, although both should be
         * notified when the transaction terminates.
         */
        boolean           _shared;

    
        /**
         * This flag is used during 2pc to indicate whether the resource
         * should be commited/rolledback. Shared resources and those that
         * indicated they are read-only during preperation do not need
         * to be commited/rolledback.
         */
        boolean           _readOnly;


        /**
         * Reference to the next XA resource holder in a single linked-list
         * of either enlisted or delisted resources.
         */
        XAResourceHolder  _nextHolder;


        XAResourceHolder( XAResource xaResource, Xid xid, boolean shared )
        {
            _xaResource = xaResource;
            _xid = xid;
            _shared = shared;
        }
    

    }

    
}



