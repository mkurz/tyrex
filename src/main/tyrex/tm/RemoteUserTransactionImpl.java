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
 * $Id: RemoteUserTransactionImpl.java,v 1.2 2000/09/08 23:06:13 mohammed Exp $
 */


package tyrex.tm;


import java.util.Hashtable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.transaction.*;
import javax.transaction.xa.*;
import tyrex.client.RemoteUserTransaction;
import tyrex.client.ClientUserTransaction;


/**
 * Implements the {@link RemoteUserTransaction} interface between a
 * {@link ClientUserTransaction} and {@link TransactionServer}.
 * Provides all the services of a {@link UserTransaction} but instead
 * of acting on the transaction associated with the current thread,
 * acts on the transaction identifier passed to the method.
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/09/08 23:06:13 $
 * @see RemoteUserTransaction
 * @see TransactionServer#createRemoteTransaction
 * @see TransactionServer#getTransaction
 */
public class RemoteUserTransactionImpl
    extends UnicastRemoteObject
    implements RemoteUserTransaction
{


    private TransactionDomain _txDomain;


    public RemoteUserTransactionImpl( TransactionDomain txDomain )
	throws RemoteException
    {
	super();
	if ( txDomain == null )
	    throw new IllegalArgumentException( "Argument 'txDomain' is null" );
	_txDomain = txDomain;
    }


    public byte[] begin()
	throws SystemException
    {
	    return _txDomain.createTransaction( null, null ).getXid().getGlobalTransactionId();
    }


    public void commit( byte[] gxid )
	throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	       IllegalStateException, SystemException
    {
	TransactionImpl tx;

	try {
	    tx = _txDomain.getTransaction( gxid );
	} catch ( InvalidTransactionException except ) {
	    throw new SystemException( except.getMessage() );
	}
	// We must enlist this thread with the transaction before getting
	// to rollback, otherwise we get a security exception due to thread
	// not being owner of the transaction. We will get a security
	// exception if we cannot activate the transaction.
	_txDomain.enlistThread( tx, Thread.currentThread() );
	try {
	    tx.commit();
	}  finally {
	    _txDomain.delistThread( tx, Thread.currentThread() );
	}
    }


    public void rollback( byte[] gxid )
	throws IllegalStateException, SystemException
    {
	TransactionImpl tx;

	try {
	    tx = _txDomain.getTransaction( gxid );
	} catch ( InvalidTransactionException except ) {
	    throw new SystemException( except.getMessage() );
	}
	// We must enlist this thread with the transaction before getting
	// to rollback, otherwise we get a security exception due to thread
	// not being owner of the transaction. We will get a security
	// exception if we cannot activate the transaction.
	_txDomain.enlistThread( tx, Thread.currentThread() );
	try {
	    tx.rollback();
	} finally {
	    _txDomain.delistThread( tx, Thread.currentThread() );
	}
    }


    public void setRollbackOnly( byte[] gxid )
	throws IllegalStateException, SystemException
    {
	Transaction tx;

	try {
	    tx = _txDomain.getTransaction( gxid );
	} catch ( InvalidTransactionException except ) {
	    throw new SystemException( except.getMessage() );
	}
	tx.setRollbackOnly();
    }


    public int getStatus( byte[] gxid )
	throws SystemException
    {
	Transaction tx;

	try {
	    tx = _txDomain.getTransaction( gxid );
	} catch ( InvalidTransactionException except ) {
	    throw new SystemException( except.getMessage() );
	}
	if ( tx == null )
	    return Status.STATUS_NO_TRANSACTION;
	return tx.getStatus();
    }


    public void setTransactionTimeout( byte[] gxid, int seconds )
	throws SystemException
    {
	TransactionImpl tx;

	try {
	    tx = _txDomain.getTransaction( gxid );
	} catch ( InvalidTransactionException except ) {
	    throw new SystemException( except.getMessage() );
	}
	if ( tx != null )
	    _txDomain.setTransactionTimeout( tx, seconds );
    }

    
}

