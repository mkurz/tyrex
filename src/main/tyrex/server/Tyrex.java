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
 * $Id: Tyrex.java,v 1.2 2000/01/17 22:13:59 arkin Exp $
 */


package tyrex.server;


import java.io.PrintWriter;
import java.util.Enumeration;
import java.security.AccessController;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.RollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;


/**
 * Provides access to the transaction server for the application
 * server. Provides a way to obtain an instance of the transaction
 * manager, user transaction interface, and perform certain
 * activities for which no well defined interface exists.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/01/17 22:13:59 $
 */
public final class Tyrex
{


    private static TransactionDomain    _txDomain =
        TransactionServer.getTransactionDomain( TransactionServer.DefaultDomain, true );


    /**
     * Returns an instance of the transaction manager. The caller must
     * have {@link TyrexPermission.Trasaction#Manager} permission.
     *
     * @return An instance of the transaction manager
     * @throws SecurityException Caller does not have permission
     */
    public static TransactionManager getTransactionManager()
    {
	AccessController.checkPermission( TyrexPermission.Transaction.Manager );
	return _txDomain.getTransactionManager();
    }


    /**
     * Returns an instance of the user transaction interface.
     *
     * @return An instance of the user transaction interface
     */
    public static UserTransaction getUserTransaction()
    {
	return _txDomain.getUserTransaction();
    }


    public static void recycleThread()
	throws RollbackException
    {
	AccessController.checkPermission( TyrexPermission.Transaction.Manager );
	( (TransactionManagerImpl) _txDomain.getTransactionManager() ).recycleThread();
    }


    public static void resumeGlobal( byte[] gxid )
	throws InvalidTransactionException, IllegalStateException, SystemException
    {
	Transaction tx;

	AccessController.checkPermission( TyrexPermission.Transaction.Manager );
	tx = _txDomain.getTransaction( gxid );
	_txDomain.getTransactionManager().resume( tx );
    }


    /**
     * Returns the transaction currently associated with the given
     * thread, or null if the thread is not associated with any
     * transaction. The caller must have {@link
     * TyrexPermission.Transaction#List} permission.
     * <p>
     * This method is equivalent to calling {@link
     * TransactionManager#getTransaction} from within the thread.
     *
     * @param thread The thread to lookup
     * @return The transaction currently associated with that thread
     */
    public static TransactionStatus getTransaction( Thread thread )
    {
	Transaction tx;

	AccessController.checkPermission( TyrexPermission.Transaction.List );
	tx =  ( (TransactionManagerImpl) _txDomain.getTransactionManager() ).getTransaction( thread );
	if ( tx != null )
	    return _txDomain.getTransactionStatus( tx );
	return null;
    }


    /**
     * Returns an enumeration of all the transactions currently
     * registered with the server. Each entry is described using the
     * {@link TransactionStatus} object providing information about
     * the transaction, its resources, timeout and active state.
     * Some of that information is only current to the time the list
     * was produced. The caller must have {@link
     * TyrexPermission.Transaction#List} permission.
     *
     * @return List of all transactions currently registered
     */
    public static TransactionStatus[] listTransactions()
    {
	AccessController.checkPermission( TyrexPermission.Transaction.List );
	return _txDomain.listTransactions();
    }


    /**
     * Terminates a transaction in progress. The transaction will be rolled
     * back with a timed-out flag and all threads associated with it will be
     * terminated. Caller must have {@link TyrexPermission.
     * Transaction#Terminate}
     * permission to terminate the transaction and {@link RuntimePermission}
     * <tt>stopThread</tt> permission to terminate any running thread.
     *
     * @param tx The transaction to terminate
     * @throws InvalidTransactionException The transaction did not originate
     *   on this server, or has already been terminated
     */
    public static void terminateTransaction( Transaction tx )
	throws InvalidTransactionException
    {
	AccessController.checkPermission( TyrexPermission.Transaction.Terminate );
	_txDomain.terminateTransaction( tx );
    }


    /**
     * Convenience method. Dumps information about all the transactions
     * currently registered with the server to the specified writer.
     */
    public static void dumpTransactionList( PrintWriter writer )
    {
	TransactionStatus[] txsList;
	String[]            resList;
	int                 i;
	int                 j;

	AccessController.checkPermission( TyrexPermission.Transaction.List );
	txsList = listTransactions();
	if ( txsList.length == 0 )
	    writer.println( "Server Transactions: No Transactions" );
	else  {
	    writer.println( "Server Transactions:" );
	    for ( i = 0 ; i < txsList.length ; ++i ) {
		writer.println( "TX: " + txsList[ i ].toString() + ( txsList[ i ].isInThread() ? " [In Thread]" : "" ) );
		writer.println( "TX: Timeout: " + txsList[ i ].getTimeout() );
		resList = txsList[ i ].listResources() ;
		for ( j = 0 ; j < resList.length ; ++j )
		    writer.println( "TX: Resource: " + resList[ j ] );
	    }
	}
    }


    /**
     * Convenience method. Dumps information about the current transaction
     * in this thread to the specified writer.
     */
    public static void dumpCurrentTransaction( PrintWriter writer )
    {
	TransactionStatus txs;
	String[]          resList;
	int               i;

	AccessController.checkPermission( TyrexPermission.Transaction.List );
	txs = getTransaction( Thread.currentThread() );
	if ( txs == null )
	    writer.println( "Current Transaction: No Transaction" );
	else {
	    writer.println( "Current Transaction:" );
	    writer.println( "TX: " + txs.toString() );
	    writer.println( "TX: Timeout: " + txs.getTimeout() );
	    resList = txs.listResources() ;
	    for ( i = 0 ; i < resList.length ; ++i )
		writer.println( "TX: Resource: " + resList[ i ] );
	}
    }



}

