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
 * $Id: CurrentImpl.java,v 1.1 2000/08/28 19:01:51 mohammed Exp $
 */


package tyrex.tm;


import org.omg.CosTransactions.*;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.Current;
import org.omg.CORBA.DynamicImplementation;
import org.omg.CORBA.Environment;
import org.omg.CORBA.WrongTransaction;
import org.omg.CosTSPortability.Sender;
import org.omg.CosTSPortability.Receiver;
import javax.transaction.Transaction;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;


/**
 * Implements a {@link Current} interface into the transaction
 * manager. Transactions are managed strictly by {@link
 * TransactionManagerImpl}, however when using the OTS API or
 * communicating with other CORBA servers it is necessary to use
 * the current interface. This object serves as lightweight adapter
 * between the transaction manage and current interface.
 * <p>
 * XXX  Current is not fully implemented in this release, since
 * it does not extend a CORBA base implementation and I could not
 * find a suitable interface in the JTS package to extend.
 * No testing has been done to see if this implementation can be
 * used remotely through COS Naming.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/08/28 19:01:51 $
 * @see TransactionManagerImpl
 */
public final class CurrentImpl
    // This interface and implementation are not available in
    // the JTS JAR shipped by Sun.
    //extends org.omg.CosTransactions._CurrentImplBase
    implements Sender, Receiver
{


    /**
     * Reference to the transaction manager.
     */
    private TransactionManagerImpl        _txManager;


    /**
     * Reference to the transaction factory used for creating
     * new transactions and recreating transactions on import.
     */
    private TransactionFactory            _txFactory;


    /**
     * The default timeout for all newly created transactions,
     * specified in seconds. The default is zero, meaning no timeout.
     */
    private  int                          _timeout;


    /**
     * Private constructor. Use {@link #getInstance} instead.
     */
    public CurrentImpl( TransactionFactoryImpl txFactory, TransactionManagerImpl txManager )
    {
	if ( txFactory == null || txManager == null )
	    throw new IllegalArgumentException( "Argument 'txFactory' or 'txManager' is null" );
	_txFactory = txFactory;
	_txManager = txManager;
    }


    public void begin()
	throws SubtransactionsUnavailable
    {
	Transaction tx;

	try {
	    _txManager.begin();
	    // Unlike TransactionManager, Current uses set_timeout to specify
	    // the timeout for the next transaction to be created, and zero
	    // means no timeout, not the default value.
	    tx = _txManager.getTransaction();
	    _txManager.setTransactionTimeout( _timeout == 0 ? Integer.MAX_VALUE : _timeout );
	} catch ( NotSupportedException except ) {
	    // JTA not supported exception is OTS subtransactions
	    // not available.
	    throw new SubtransactionsUnavailable();
	} catch ( SystemException except ) {
	    throw new INVALID_TRANSACTION( except.toString() );
	}
    }


    public void commit( boolean reportHeuristics )
	throws NoTransaction, HeuristicMixed, HeuristicHazard
    {
	try {
	    _txManager.commit();
	} catch ( SecurityException except ) {
	    // Java SecurityException is OTS NO_PERMISSION
	    // and it's a runtime exception.
	    throw new NO_PERMISSION( except.getMessage() );
	} catch ( IllegalStateException except ) {
	    throw new NoTransaction();
	} catch ( RollbackException except ) {
	    throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
	} catch ( HeuristicRollbackException except ) {
	    if ( reportHeuristics )
		throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
	} catch ( HeuristicMixedException except ) {
	    if ( reportHeuristics )
		throw new HeuristicMixed();
	} catch ( SystemException except ) {
	    if ( reportHeuristics )
		throw new HeuristicHazard();
	    throw new INVALID_TRANSACTION( except.toString() );
	}
    }


    public void rollback()
	throws NoTransaction
    {
	try {
	    _txManager.rollback();
	} catch ( SecurityException except ) {
	    // Java SecurityException is OTS NO_PERMISSION
	    // and it's a runtime exception.
	    throw new NO_PERMISSION( except.getMessage() );
	} catch ( IllegalStateException except ) {
	    throw new NoTransaction();
	} catch ( SystemException except ) {
	    throw new INVALID_TRANSACTION( except.toString() );
	}
    }


    public void rollback_only()
	throws NoTransaction
    {
	try {
	    _txManager.setRollbackOnly();
	} catch ( IllegalStateException except ) {
	    throw new NoTransaction();
	} catch ( SystemException except ) {
	    throw new INVALID_TRANSACTION( except.toString() );
	}
    }


    public Status get_status()
    {
	return ControlImpl.fromJTAStatus( _txManager.getStatus() );
    }


    public String get_transaction_name()
    {
	Transaction tx;

	tx = _txManager.getTransaction();
	// If there is no transaction, must return an empty string.
	return ( tx == null ? "" : tx.toString() );
    }


    public void set_timeout( int seconds )
    {
	_timeout = seconds;
    }


    public Control get_control()
    {
	TransactionImpl tx;

	tx = (TransactionImpl) _txManager.getTransaction();
	return ( tx == null ? null : tx.getControl() );
    }


    public Control suspend()
    {
	TransactionImpl tx;

	tx = (TransactionImpl) _txManager.suspend();
	return ( tx == null ? null : tx.getControl() );
    }


    public void resume( Control which )
	throws InvalidControl
    {
	// In OTS the thread is dissociated from the transaction automatically,
	// and if which is null, it is not associated with any other transactions.
	suspend();
	if( which != null ) {
	    if ( ! ( which instanceof ControlImpl ) )
		throw new InvalidControl();
	    try {
		_txManager.resume( ( (ControlImpl) which ).getTransaction() );
	    } catch ( IllegalStateException except ) {
		throw new InvalidControl();
	    } catch ( InvalidTransactionException except ) {
		throw new InvalidControl();
	    } catch ( SystemException except ) {
		throw new INVALID_TRANSACTION( except.toString() );
	    }
	}
    }


    public void sending_request( int refId, PropagationContextHolder pgxh )
    {
	ControlImpl ctrl;

	// Sender:
	// Request about to be sent. The server has to deliver the current
	// transaction context to the reciever.
	ctrl = (ControlImpl) get_control();
	if ( ctrl == null )
	    throw new TRANSACTION_REQUIRED();
	pgxh.value = ctrl.get_txcontext();
    }


    public void received_request( int refId, PropagationContext pgContext )
    {
	// Receiver:
	// Request has been recieved. The propagation context is handed
	// for association with the thread. Need to create a local copy
	// of the transaction and resume the thread under its control.
	try {
	    resume( _txFactory.recreate( pgContext ) );
	} catch ( InvalidControl except ) {
	    throw new INVALID_TRANSACTION();
	}
    }


    public void sending_reply( int refId, PropagationContextHolder pgxh )
    {
	ControlImpl     ctrl;
	TransactionImpl tx;

	// Receiver:
	// Reply about to be sent. Figure out if we're in the same
	// transaction level as the incoming request, if not,
	ctrl = (ControlImpl) get_control();
	if ( ctrl == null ) {
	    // The only possibility for not having an imported
	    // transaction in the thread is that it has been
	    // rolled back or timed out.
	    throw new TRANSACTION_ROLLEDBACK();
	} else {
	    tx = ctrl.getTransaction();
	    if ( tx.getPropagationContext() == null ) {
		// If the top level transaction is not an imported
		// one, we rollback the top level and throw an
		// exception.
		while ( tx.getParent() != null )
		    tx = tx.getParent();
		if ( tx.getPropagationContext() != null ) {
		    try {
			tx.getPropagationContext().current.coord.rollback_only();
		    } catch ( Inactive except ) { }
		}
		throw new TRANSACTION_ROLLEDBACK();
	    }
	    pgxh.value = tx.getPropagationContext();
	}
    }


    public void received_reply( int refId, PropagationContext pgContext, Environment env )
	throws WrongTransaction
    {
	// Sender:
	// Reply has been recieved. If environment indicates an error,
	// or we did not get the same transaction back, mark the
	// transaction for rollback and throw an exception.
	if ( env.exception() != null ) {
	    try {
		rollback_only();
	    } catch ( NoTransaction except ) { }
	    throw new TRANSACTION_ROLLEDBACK();
	}
	// Make sure we got back the same transaction that we expected.
	// Note that an exception can be an error on both side, but
	// we do not deal with asynchronous transactions here.
	if ( pgContext.current.coord != get_control() )
	    throw new WrongTransaction();
    }


}
