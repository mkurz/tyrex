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
 * $Id: TransactionFactoryImpl.java,v 1.4 2001/03/12 19:20:20 arkin Exp $
 */


package tyrex.tm.impl;


import javax.transaction.SystemException;
import javax.transaction.InvalidTransactionException;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.Environment;
import org.omg.CORBA.WrongTransaction;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.InvalidControl;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.PropagationContextHolder;
import org.omg.CosTransactions._TransactionFactoryImplBase;
import org.omg.CosTSPortability.Sender;
import org.omg.CosTSPortability.Receiver;


/**
 * Implements an OTS transaction factory and identification interfaces.
 * Allows the creation of new OTS transactions as well as the importing
 * of remote transactions.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $ $Date: 2001/03/12 19:20:20 $
 * @see TransactionImpl
 *
 * Changes 
 *
 * J. Daniel : Changed code to be compliant with CORBA developing rules.
 */
final class TransactionFactoryImpl
    extends _TransactionFactoryImplBase
    implements Sender, Receiver
{


    /**
     * The transaction domain to which this factory belongs.
     */
    private final TransactionDomainImpl  _txDomain;


    TransactionFactoryImpl( TransactionDomainImpl txDomain )
    {
        if ( txDomain == null )
            throw new IllegalArgumentException( "Argument 'txDomain' is null" );
        _txDomain = txDomain;
    }


    public Control create( int timeout )
    {
        TransactionImpl tx;
        
        // Create a new transaction and return the control
        // interface of that transaction.
        try {
            tx = _txDomain.createTransaction( null, null, timeout );
            return tx.getControl();
        } catch ( Exception except ) {
            throw new INVALID_TRANSACTION();
        }
    }


    public Control recreate( PropagationContext pgContext )
    {
        TransactionImpl tx;
        
        try {
            tx = _txDomain.recreateTransaction( pgContext );
            return tx.getControl();
        } catch ( Exception except ) {
            throw new INVALID_TRANSACTION();
        }
    }
    
    
    public void sending_request( int refId, PropagationContextHolder pgxh )
    {
        TransactionImpl txImpl;
        
        // Sender:
        // Request about to be sent. The server has to deliver the current
        // transaction context to the reciever.
        txImpl = (TransactionImpl) _txDomain._txManager.getTransaction();
        if ( txImpl == null )
            throw new TRANSACTION_REQUIRED();
        pgxh.value = txImpl.getControl().getPropagationContext();
    }


    public void received_request( int refId, PropagationContext pgContext )
    {
        ControlImpl control;

        // Receiver:
        // Request has been recieved. The propagation context is handed
        // for association with the thread. Need to create a local copy
        // of the transaction and resume the thread under its control.
        try {
            control = (ControlImpl) recreate( pgContext );
            try {
                _txDomain._txManager.resume( control.getTransaction() );
            } catch ( IllegalStateException except ) {
                throw new InvalidControl();
            } catch ( InvalidTransactionException except ) {
                throw new InvalidControl();
            } catch ( SystemException except ) {
                throw new INVALID_TRANSACTION( except.toString() );
            }
        } catch ( InvalidControl except ) {
            throw new INVALID_TRANSACTION();
        }
    }


    public void sending_reply( int refId, PropagationContextHolder pgxh )
    {
        TransactionImpl txImpl;

        // Receiver:
        // Reply about to be sent. Figure out if we're in the same
        // transaction level as the incoming request, if not,
        txImpl = (TransactionImpl) _txDomain._txManager.getTransaction();
        if ( txImpl == null ) {
            // The only possibility for not having an imported
            // transaction in the thread is that it has been
            // rolled back or timed out.
            throw new TRANSACTION_ROLLEDBACK();
        } else {
            if ( txImpl.getPropagationContext() == null ) {
                // If the top level transaction is not an imported
                // one, we rollback the top level and throw an
                // exception.
                while ( txImpl.getParent() != null )
                    txImpl = (TransactionImpl) txImpl.getParent();
                if ( txImpl.getPropagationContext() != null ) {
                    try {
                        txImpl.getPropagationContext().current.coord.rollback_only();
                    } catch ( Inactive except ) { }
                }
                throw new TRANSACTION_ROLLEDBACK();
            }
            pgxh.value = txImpl.getPropagationContext();
        }
    }


    public void received_reply( int refId, PropagationContext pgContext, Environment env )
        throws WrongTransaction
    {
        TransactionImpl txImpl;

        // Sender:
        // Reply has been recieved. If environment indicates an error,
        // or we did not get the same transaction back, mark the
        // transaction for rollback and throw an exception.
        if ( env.exception() != null ) {
            try {
                _txDomain._txManager.setRollbackOnly();
            } catch ( IllegalStateException except ) {
            } catch ( SystemException except ) {
                throw new INVALID_TRANSACTION( except.toString() );
            }
            throw new TRANSACTION_ROLLEDBACK();
        }
        // Make sure we got back the same transaction that we expected.
        // Note that an exception can be an error on both side, but
        // we do not deal with asynchronous transactions here.
        txImpl = (TransactionImpl) _txDomain._txManager.getTransaction();
        if ( txImpl == null || txImpl.getControl().getCoordinator() != pgContext.current.coord )
            throw new WrongTransaction();
    }


}
