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
 * $Id: Invoke.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.ejb;


import java.util.Hashtable;
import java.rmi.RemoteException;
import javax.ejb.SessionSynchronization;
import javax.ejb.deployment.ControlDescriptor;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.NotSupportedException;
import javax.transaction.TransactionRequiredException;

import javax.transaction.Synchronization;
import javax.transaction.Status;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import tyrex.server.Tyrex;
import tyrex.naming.TyrexContextFactory;


/**
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
 */
public class Invoke
{


    /**
     * A copy of the client transaction. If the this method is invoked
     * in the context of a client transaction and the client transaction
     * must be suspended, we hold it in this variable during invocation.
     */
    private Transaction               _clientTx;


    /**
     * An interface into the session object of a stateful session bean
     * that allows it to hold transaction inbetween method invocations.
     *
     * @see SessionTransaction
     */
    private SessionTransaction        _sessionTx;


    /**
     * A reference to the transaction manager.
     */
    private static TransactionManager  _txManager;


    /**
     * A reference to the <tt>java:/comp</tt> initial context that will
     * be used to hold the bean's user transaction interface.
     */
    private static Context            _beanCtx;



    /**
     * Static initializer must be called once before any method invocation.
     * Cannot place this in class static initializer, which gets executed
     * well before the transaction manager is configured.
     */
    static void init()
	throws Exception
    {
	Hashtable env;

	_txManager = Tyrex.getTransactionManager();
	env = new Hashtable();
	env.put( Context.INITIAL_CONTEXT_FACTORY, TyrexContextFactory.class );
	env.put( Context.PROVIDER_URL, "" );
	_beanCtx = new InitialContext( env );
    }


    /**
     * Called prior to a method invocation to set the transaction.
     *
     * @param sync If the bean implements the {@link SessionSynchronization}
     *   then a reference to this interface
     * @param txType The transaction attribute of the bean/method
     */
    void preInvokeTx( SessionSynchronization sync, int txType )
	throws RemoteException
    {
	
	//try {
	    switch ( txType ) {

	    case ControlDescriptor.TX_BEAN_MANAGED: {
		// Bean managed transaction always suspends the client transaction.
		try {
		    _clientTx = _txManager.suspend();
		} catch ( SystemException except ) {
		    throw new RemoteException( except.toString() );
		}

		// If there was a transaction in the previous invocation of this
		// stateful bean, must resume it.
		if ( _sessionTx != null && _sessionTx.getTransaction() != null ) {
		    try {
			_txManager.resume( _sessionTx.getTransaction() );
		    } catch ( InvalidTransactionException except ) {
			// Most likely transaction has been rolledback or timed out.
			throw new RemoteException( except.getMessage() );
		    } catch ( IllegalStateException except ) {
			// This should never happen
			throw new RemoteException( "Internal error: TX_BEAN_MANAGED", except );
		    } catch ( SystemException except ) {
			throw new RemoteException( except.toString() );
		    }
		}
		
		// XXX This is where we expose BeanUserTransaction to the
		//     bean through it's context and the ENC.
		try {
		    _beanCtx.rebind( "comp/UserTransaction", new BeanUserTransaction() );
		} catch ( NamingException except ) {
		    throw new RemoteException( "Internal error: TX_BEAN_MANAGED", except );
		}
		break;
	    }

	    /* XXX Could not find this one in EJB 1.1 DD
	    case ControlDescriptor.TX_NEVER: {
		// Cannot run inside a transaction.
		if ( _txManager.getTransaction() != null )
		    throw new RemoteException( "Bean cannot be invoked in a global transaction" );
		break;
	    }
	    */

	    case ControlDescriptor.TX_MANDATORY: {
		// A client transaction is required to invoke this bean.
		// The client transaction is not suspended.
		Transaction tx;

		try {
		    tx = _txManager.getTransaction();
		} catch ( SystemException except ) {
		    throw new RemoteException( except.toString() );
		}
		if ( tx == null )
		    throw new TransactionRequiredException( "Bean invoked without global transaction" );

		if ( sync != null ) {
		    try {
			tx.registerSynchronization( new SynchronizationWrapper( sync ) );
			sync.afterBegin();
		    } catch ( RollbackException except ) {
			throw new RemoteException( except.getMessage() );
		    } catch ( SystemException except ) {
			throw new RemoteException( except.toString() );
		    }
		}
		break;
	    }

	    case ControlDescriptor.TX_SUPPORTS:
		// Nothing to do, whether we are inside or not inside a transaction.
		break;

	    case ControlDescriptor.TX_NOT_SUPPORTED: {
		// The bean cannot run inside a transaction context. If there is
		// such a global transaction, we suspend it during execution.
		try {
		    _clientTx = _txManager.suspend();
		} catch ( SystemException except ) {
		    throw new RemoteException( except.toString() );
		}
		break;
	    }
		
	    case ControlDescriptor.TX_REQUIRES_NEW: {
		// The bean must be invoked in the context of a new transaction,
		// and the client transaction (if any) must be suspended.
		try {
		    _clientTx = _txManager.suspend();
		} catch ( SystemException except ) {
		    throw new RemoteException( except.toString() );
		}

		try {
		    _txManager.begin();
		    if ( sync != null ) {
			_txManager.getTransaction().registerSynchronization( new SynchronizationWrapper( sync ) );
			sync.afterBegin();
		    }
		} catch ( NotSupportedException except ) {
		    // Should never happen, this is not a nested transaction
		    throw new RemoteException( "Internal error: TX_REQUIRED", except );
		} catch ( RollbackException except ) {
		    throw new RemoteException( except.getMessage() );
		} catch ( SystemException except ) {
		    throw new RemoteException( except.toString() );
		}
		break;
	    }

	    case ControlDescriptor.TX_REQUIRED: {
		// Bean must run inside a transaction, whether client or newly
		// created. If client specifies a transaction, we hold it in _clienTx,
		// but run inside it, so it must never be resumed, see matching case
		// in post invoke.
		try {
		    _clientTx = _txManager.getTransaction();
		} catch ( SystemException except ) {
		    throw new RemoteException( except.toString() );
		}

		if ( _clientTx == null ) {
		    try {
			_txManager.begin();
			if ( sync != null ) {
			    _txManager.getTransaction().registerSynchronization( new SynchronizationWrapper( sync ) );
			    sync.afterBegin();
			}
		    } catch ( NotSupportedException except ) {
			// Should never happen, this is not a nested transaction
			throw new RemoteException( "Internal error: TX_REQUIRED", except );
		    } catch ( SystemException except ) {
			throw new RemoteException( except.toString() );
		    } catch ( RollbackException except ) {
			throw new RemoteException( except.getMessage() );
		    }
		}
		break;
	    }
		
	    default:
		throw new RemoteException( "Internal error: transaction type not supported" );
	    }
	    //}
    }
    
    
    /**
     * Called immediately after a method invocation, whether or not the method
     * completed successfully.
     *
     * @param txType The transaction attribute of the bean/method
     * @param failed True if the method faild and the transaction must be
     *   rolled back
     */
    void postInvokeTx( int txType, boolean failed )
	throws RemoteException
    {
	try {
	    switch ( txType ) {
		
	    case ControlDescriptor.TX_BEAN_MANAGED: {
		// When returning from a bean managed transaction there are
		// three options: no transaction, stateless bean with transaction
		// (must be terminated), or statefull bean with transaction
		// (will be retained for subsequent invocation).
		Transaction tx;

		// XXX This is where we take away BeanUserTransaction from the
		//     bean's context and the ENC.
		try {
		    _beanCtx.unbind( "comp/UserTransaction" );
		} catch ( NamingException except ) {
		    throw new RemoteException( "Internal error: TX_BEAN_MANAGED", except );
		}

		try {
		    tx =  _txManager.suspend();
		    if ( tx.getStatus() != Status.STATUS_ACTIVE &&
			 tx.getStatus() != Status.STATUS_MARKED_ROLLBACK )
			tx = null;
		    if ( tx != null ) {
			if ( _sessionTx == null ) {
			    try {
				tx.rollback();
			    } catch ( SecurityException except ) {
				// Should never happen, we created the transaction
				throw new RemoteException( "Internal error: TX_BEAN_MANAGED", except );
			    } catch ( IllegalStateException except ) {
				// Should never happen, we are inside a transaction
				throw new RemoteException( "Internal error: TX_BEAN_MANAGED", except );
			    } catch ( SystemException except ) {
				throw new RemoteException( except.toString() );
			    }
			} else {
			    // Keep the bean along with the state for use
			    // in the next method invocation.
			    _sessionTx.setTransaction( tx );
			}
		    }
		} catch ( SystemException except ) {
		    throw new RemoteException( except.toString() );
		}
		break;
	    }

	    case ControlDescriptor.TX_REQUIRED: {
		// If there was a client transaction, _clientTx holds it, but the
		// bean was invoked in the context of that transaction and it must
		// not be resumed. If there was no client transaction a new one was
		// created and we fall through to TX_REQUIRES_NEW to complete it.
		if ( _clientTx != null ) {
		    _clientTx = null;
		    break;
		}
		// !!! FALL THROUGH TO TX_REQUIRES_NEW !!!
	    }

	    case ControlDescriptor.TX_REQUIRES_NEW: {
		// Must commit or rollback the transaction started for this
		// invocation based on the rollback status.
		Transaction tx;

		try {
		    tx = _txManager.getTransaction();
		    if ( tx.getStatus() == Status.STATUS_MARKED_ROLLBACK ) {
			tx.rollback();
		    } else {
			tx.commit();
		    }
		    // As far as we're concerned, all these are valid exception,
		    // i.e. the transaction will rollback and we have no responsibility
		    // to report it to the client.
		} catch ( RollbackException except ) {
		    throw new RemoteException( except.getMessage() );
		} catch ( HeuristicMixedException except ) {
		    throw new RemoteException( except.getMessage() );
		} catch ( HeuristicRollbackException except ) {
		    throw new RemoteException( except.getMessage() );
		} catch ( SecurityException except ) {
		    // Should never happen, we created the transaction
		    throw new RemoteException( "Internal error: TX_REQUIRED", except );
		} catch ( IllegalStateException except ) {
		    // Should never happen, we are inside a transaction
		    throw new RemoteException( "Internal error: TX_REQUIRED", except );
		} catch ( SystemException except ) {
		    throw new RemoteException( except.toString() );
		}
	    }

	    /* XXX Could not find this one in EJB 1.1 DD
	    case ControlDescriptor.TX_NEVER:
	    */
	    case ControlDescriptor.TX_MANDATORY:
	    case ControlDescriptor.TX_SUPPORTS:
	    case ControlDescriptor.TX_NOT_SUPPORTED:
	    default:
		// Nothing needs to happen in all these cases. The most that could
		// happen is resuming the client transaction which will happen next.
		break;

	    }
	} finally {
	    // If this method was invoked with a client transaction, we must
	    // restore the client transaction's association with the current
	    // thread. RMI doesn't really mind and it's just an overhead,
	    // but IIOP is sensitive.
	    if ( _clientTx != null ) {
		try {
		    _txManager.resume( _clientTx );
		} catch ( InvalidTransactionException except ) {
		    // Yes, this could happen if someone else is using the transaction
		    throw new RemoteException( except.getMessage() );
		} catch ( IllegalStateException except ) {
		    // This should never happen
		    throw new RemoteException( "Internal error: could not resume client transaction", except );
		} catch ( SystemException except ) {
		    throw new RemoteException( except.toString() );
		}
	    }
	}
    }



    /**
     * Synchronization wrapper used to present an EJB session synchronization
     * to a JTA transaction as a JTA synchronization.
     *
     * @see Synchronization
     * @see SessionSynchronization
     */
    class SynchronizationWrapper
	implements Synchronization
    {


	private SessionSynchronization  _sessionSync;


	SynchronizationWrapper( SessionSynchronization sessionSync )
	{
	    _sessionSync = sessionSync;
	}


	public void beforeCompletion()
	{
	    try {
		_sessionSync.beforeCompletion();
	    } catch ( RemoteException except ) {
		// We throw back any error occured inside the synchronization,
		// this will cause the transaction to rollback.
		try {
		    _txManager.setRollbackOnly();
		} catch ( Exception except2 ) { }
	    }
	}


	public void afterCompletion( int status )
	{
	    try {
		_sessionSync.afterCompletion( status == Status.STATUS_COMMITTED );
	    } catch ( RemoteException except ) {
		// We ignore any error occured inside the synchronization,
		// the transaction has already completed.
	    }
	}


    }


    /**
     * This interface is used to associate/obtain a transaction from
     * a stateful bean's context across method invocations.
     */
    interface SessionTransaction
    {
	public void setTransaction( Transaction tx );
	public Transaction getTransaction();
    }


}
