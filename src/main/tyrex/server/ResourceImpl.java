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
 * $Id: ResourceImpl.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.server;


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
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
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


    public synchronized Vote prepare()
    	throws HeuristicMixed, HeuristicHazard
    {
	try {
	    _tx.prepare();
	} catch ( IllegalStateException except ) {
	    throw new INVALID_TRANSACTION( except.getMessage() );
	} catch ( RollbackException except ) {
	    throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
	}
	switch ( _tx.getHeuristic() ) {
	case HEURISTIC_READONLY:
	    // No need for us to participate in commit/rollback.
	    // We must call after_completion on all synchronizations
	    // and forget about this transaction and move on.
	    try {
		_tx.forget();
	    } catch ( IllegalStateException except ) { }
	    return Vote.VoteReadOnly;
	case HEURISTIC_ROLLBACK:
	    // No need for us to participate in commit/rollback.
	    // We must call after_completion on all synchronizations
	    // and forget about this transaction and move on.
	    _tx.internalRollback();
	    try {
		_tx.forget();
	    } catch ( IllegalStateException except ) { }
	    return Vote.VoteRollback;
	case HEURISTIC_COMMIT:
	    // We can commit any number of resources.
	    return Vote.VoteCommit;
	case HEURISTIC_MIXED:
	    _tx.internalRollback();
	    try {
		_tx.forget();
	    } catch ( IllegalStateException except ) { }
	    throw new HeuristicMixed();
	case HEURISTIC_HAZARD:
	default:
	    _tx.internalRollback();
	    try {
		_tx.forget();
	    } catch ( IllegalStateException except ) { }
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
	}
	try {
	    switch ( _tx.getHeuristic() ) {
	    case HEURISTIC_READONLY:
	    case HEURISTIC_ROLLBACK:
		break;
	    case HEURISTIC_COMMIT:
		throw new HeuristicCommit();
	    case HEURISTIC_MIXED:
		throw new HeuristicMixed();
	    case HEURISTIC_HAZARD:
	    default:
		throw new HeuristicHazard();
	    }
	} finally {
	    // Whatever the outcome, we forget about the transaction.
	    try {
		_tx.forget();
	    } catch ( IllegalStateException except ) { }
	}
    }


    public synchronized void commit()
        throws NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
	try {
	    _tx.internalCommit();
	} catch ( IllegalStateException except ) {
	    throw new NotPrepared();
	}
	switch ( _tx.getHeuristic() ) {
	case HEURISTIC_READONLY:
	case HEURISTIC_COMMIT:
	    break;
	case HEURISTIC_ROLLBACK:
	    throw new HeuristicRollback();
	case HEURISTIC_MIXED:
	    throw new HeuristicMixed();
	case HEURISTIC_HAZARD:
	default:
	    throw new HeuristicHazard();
	}
    }
    

    public synchronized void commit_one_phase()
        throws HeuristicHazard
    {
	try {
	    _tx.prepare();
	} catch ( IllegalStateException except ) {
	    throw new INVALID_TRANSACTION( except.getMessage() );
	} catch ( RollbackException except ) {
	    _tx.internalRollback();
	    try {
		_tx.forget();
	    } catch ( IllegalStateException except2 ) { }
	    throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
	}
	if ( _tx.getHeuristic() == HEURISTIC_COMMIT ) {
	    try {
		_tx.internalCommit();
	    } catch ( IllegalStateException except ) { }
	} else if ( _tx.getHeuristic() != HEURISTIC_READONLY ) {
	    _tx.internalRollback();
	}
	try {
	    switch ( _tx.getHeuristic() ) {
	    case HEURISTIC_HAZARD:
		throw new HeuristicHazard();
	    case HEURISTIC_ROLLBACK:
		throw new TRANSACTION_ROLLEDBACK( Messages.message( "tyrex.tx.heuristicRollback" ) );
	    }
	} finally {
	    try {
		_tx.forget();
	    } catch ( IllegalStateException except ) { }
	}
    }


    public synchronized void forget()
    {
	try {
	    _tx.forget();
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
