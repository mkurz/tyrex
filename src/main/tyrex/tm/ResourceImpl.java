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
 * $Id: ResourceImpl.java,v 1.2 2000/09/08 23:06:13 mohammed Exp $
 */


package tyrex.tm;


import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions._ResourceImplBase;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.Vote;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.INVALID_TRANSACTION;
import javax.transaction.*;
import tyrex.util.Messages;


/**
 * Implements an OTS interface on top of {@link TransactionImpl} so
 * we can register a local transaction as a resource in a global
 * transaction. Requires that we use the OTS Java/IDL mapping.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/09/08 23:06:13 $
 */
public final class ResourceImpl
    extends _ResourceImplBase
    implements Resource, Heuristic
{


    /**
     * This is the local transaction that we wish to expose as an OTS
     * resource.
     */
    private TransactionImpl  _tx;


    /**
     * Construct a new OTS resource for the underlying transaction
     * object. The transaction must have been created as a subordinate
     * with the parent transaction's Xid.
     */
    ResourceImpl( TransactionImpl tx )
    {
	super();
	_tx = tx;
    }


    /**
     * Convenience method rolling back the local transaction
     * and making sure that the transaction is forgotten.
     */
    private void rollbackAndForget()
    {
        try {
            _tx.internalRollback();
        } finally {
            try {
            _tx.forgetOnRollback();
            } catch ( IllegalStateException except ) { }
        }
    }

    public synchronized Vote prepare()
    	throws HeuristicMixed, HeuristicHazard
    {
	try {
	    _tx.prepare();
	} catch ( IllegalStateException except ) {
	    throw new INVALID_TRANSACTION( except.getMessage() );
	} catch ( RollbackException except ) {
        rollbackAndForget();
	    throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
	}
	switch ( _tx.getHeuristic() ) {
	case Heuristic.ReadOnly:
	    // No need for us to participate in commit/rollback.
	    // We must call after_completion on all synchronizations
	    // and forget about this transaction and move on.
	    try {
		_tx.forgetReadOnly();
	    } catch ( IllegalStateException except ) { }
	    return Vote.VoteReadOnly;
	case Heuristic.Rollback:
	    // No need for us to participate in commit/rollback.
	    // We must call after_completion on all synchronizations
	    // and forget about this transaction and move on.
	    rollbackAndForget();
	    return Vote.VoteRollback;
	case Heuristic.Commit:
	    // We can commit any number of resources.
	    return Vote.VoteCommit;
	case Heuristic.Mixed:
	    rollbackAndForget();
	    throw new HeuristicMixed();
	case Heuristic.Hazard:
	default:
	    rollbackAndForget();
	    throw new HeuristicHazard();
	}
    }


    public synchronized void rollback()
        throws HeuristicCommit, HeuristicMixed, HeuristicHazard
    {
	// We do not know what the rollback heuristics are
	// since we don't collect them. Though we could do
	// so in the future.
	try {
	    _tx.internalRollback();
	} catch ( IllegalStateException except ) {
	    throw new INVALID_TRANSACTION( except.getMessage() );
	} finally {
	    // Whatever the outcome, we forget about the transaction.
	    try {
		_tx.forgetOnRollback();
	    } catch ( IllegalStateException except ) { }
	}

	switch ( _tx.getHeuristic() ) {
	    case Heuristic.ReadOnly:
	    case Heuristic.Rollback:
		break;
	    case Heuristic.Commit:
		throw new HeuristicCommit();
	    case Heuristic.Mixed:
		throw new HeuristicMixed();
	    case Heuristic.Hazard:
	    default:
		throw new HeuristicHazard();
	    }
	}


    public synchronized void commit()
        throws NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
	try {
	    _tx.internalCommit(false);
	} catch ( IllegalStateException except ) {
	    throw new NotPrepared();
	} finally {
        try {
		_tx.forgetOnCommit();
	    } catch ( IllegalStateException except ) { }
    }
    
	switch ( _tx.getHeuristic() ) {
        case Heuristic.ReadOnly:
        case Heuristic.Commit:
            break;
        case Heuristic.Rollback:
            throw new HeuristicRollback();
        case Heuristic.Mixed:
            throw new HeuristicMixed();
        case Heuristic.Hazard:
        default:
            throw new HeuristicHazard();
        }
    }
    

    public synchronized void commit_one_phase()
        throws HeuristicHazard
    {
    boolean canUseOnePhaseCommit = _tx.canUseOnePhaseCommit();

    try {
        if ( !canUseOnePhaseCommit ) {
            _tx.prepare();
        }
        else {
            _tx.endResources();
        }
    } catch ( IllegalStateException except ) {
        throw new INVALID_TRANSACTION( except.getMessage() );
    } catch ( RollbackException except ) {
        rollbackAndForget();
        throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
    }
    
    if ( canUseOnePhaseCommit || _tx.getHeuristic() == Commit ) {
        try {
            _tx.internalCommit(canUseOnePhaseCommit);
        } finally { 
            try {
                _tx.forgetOnCommit();
            } catch ( IllegalStateException e ) { }
        }
    } else if ( _tx.getHeuristic() == ReadOnly ) {
        try {
                _tx.forgetReadOnly();
        } catch ( IllegalStateException e ) { }    
    } else {
        rollbackAndForget();
    }
    
	switch ( _tx.getHeuristic() ) {
    case Heuristic.Hazard:
    throw new HeuristicHazard();
    case Heuristic.Rollback:
    throw new TRANSACTION_ROLLEDBACK( Messages.message( "tyrex.tx.heuristicRollback" ) );
    }
	}


    public synchronized void forget()
    {
    try {
        switch (_tx.getStatus()) {
            case Status.STATUS_ACTIVE:
            case Status.STATUS_NO_TRANSACTION:
            case Status.STATUS_UNKNOWN:
            case Status.STATUS_MARKED_ROLLBACK:
                // no rollback or commit has occurred
                break;

            case Status.STATUS_COMMITTED:
            case Status.STATUS_COMMITTING:
                _tx.forgetOnCommit();
                break;

            case Status.STATUS_PREPARED:
            case Status.STATUS_PREPARING:
                 _tx.forgetOnCommit();
                break;

            case Status.STATUS_ROLLEDBACK:
            case Status.STATUS_ROLLING_BACK:
                _tx.forgetOnRollback();
                break;

            default:
                // unknown status ignore
                break;
        }
	} catch ( IllegalStateException except ) {
	    // Can we do anything about this?
	}
    }


    public boolean equals( Object other )
    {
	if ( other == this )
	    return true;
	if ( other instanceof ResourceImpl &&
	     ( (ResourceImpl) other )._tx == _tx )
	    return true;
	return false;
    }


    public int hashCode()
    {
	return _tx.hashCode();
    }


    public String toString()
    {
	return _tx.toString();
    }



}
