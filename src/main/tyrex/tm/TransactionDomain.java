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
 * Copyright 2000, 2001 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: TransactionDomain.java,v 1.5 2001/02/27 00:34:07 arkin Exp $
 */


package tyrex.tm;


import java.io.PrintWriter;
import java.util.HashMap;
import org.omg.CORBA.ORB;
import org.omg.CosTransactions.TransactionFactory;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.SystemException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.xa.Xid;
import tyrex.resource.ResourceLimits;
import tyrex.tm.impl.TransactionDomainImpl;


/**
 * A transaction domain provides centralized management for transactions.
 * A transaction domain defines the policy for all transactions created
 * from that domain, such as default timeout, maximum number of open
 * transactions, IIOP support, and journaling. The application obtains
 * a transaction manager or user transaction object from the transaction
 * domain.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $ $Date: 2001/02/27 00:34:07 $
 */
public abstract class TransactionDomain
{


    /**
     * The name of the default transaction domain.
     */
    public static final String DEFAULT_DOMAIN = "default";


    /**
     * The default timeout for all transactions, specified in seconds.
     * This value is used unless the transaction domain, or transaction manager
     * are requested to use a different value. The default value is 120 seconds.
     */
    public static final int    DEFAULT_TIMEOUT = 120;


    /**
     * A hash map of all transaction domains.
     */
    private static final HashMap            _domains = new HashMap();

    
    /**
     * Returns a transaction domain with the specified name. Returns null if
     * no transaction domain with that name was created.
     *
     * @param name The name of the transaction domain
     * @return The transaction domain, or if no such domain
     */
    public static synchronized TransactionDomain getDomain( String name )
    {
        if ( name == null || name.trim().length() == 0 )
            throw new IllegalArgumentException( "Argument name is null or an empty string" );
        return (TransactionDomain) _domains.get( name );
    }


    /**
     * Creates a new transaction domain with the specified name.
     * If a transaction domain with the same name exists, it is
     * returned, otherwise a new transaction domain is created.
     *
     * @param name The name of the transaction domain
     * @param config The domain configuration object
     * @return A new transaction domain
     * @throw SystemException An error occured while attempting
     * to create the domain
     */
    public synchronized static TransactionDomain createDomain( String name, DomainConfig config )
        throws SystemException
    {
        TransactionDomain domain;

        if ( name == null || name.trim().length() == 0 )
            throw new IllegalArgumentException( "Argument name is null or an empty string" );
        domain = (TransactionDomain) _domains.get( name );
        if ( domain == null ) {
            domain = new TransactionDomainImpl( name, config );
            _domains.put( name, domain );
        }
        return domain;
    }


    /**
     * Returns a transaction manager for this transaction domain.
     * The transaction managed can be used to being, commit and rollback
     * transactions in this domain only. Calling this method multiple
     * times will return the same instance of the transaction manager.
     *
     * @return The transaction manager for this domain
     */
    public abstract TransactionManager getTransactionManager();


    /**
     * Returns a user transaction for this transaction domain.
     * The user transaction can be used to being, commit and rollback
     * transactions in this domain only. Calling this method multiple
     * times will return the same instance of the user transaction.
     *
     * @return The user transaction for this domain
     */
    public abstract UserTransaction getUserTransaction();


    /**
     * Returns an OTS transaction factory for this transaction domain.
     * The transaction factory can be used to create and re-create
     * OTS transactions in this domain only. Calling this method
     * multiple times will return the same instance of the
     * transaction factory.
     *
     * @return The transaction factory for this domain
     */
    public abstract TransactionFactory getTransactionFactory();


    /**
     * Returns the resource limits associated with this transaction domain.
     * The resource limits dictate the maximum number of transactions that
     * can be opened at once, the default timeout for each transaction,
     * and other resource properties.
     *
     * @return The resource limits assocaited with this transaction domain
     */
    public abstract ResourceLimits getResourceLimits();
    

    /**
     * Sets the default timeout for all transactions created from this
     * transaction domain. The timeout value is specified in seconds.
     * Using zero will restore the implementation specific timeout.
     *
     * @param timeout The default timeout for all transactions,
     * specified in seconds
     */
    public abstract void setTransactionTimeout( int timeout );


    /**
     * Adds a transaction interceptor to this transaction domain.
     * The interceptor will be notified of all transactional activities
     * within this domain.
     *
     * @param interceptor The transaction interceptor
     */
    public abstract void addInterceptor( TransactionInterceptor interceptor );


    /**
     * Removes a transaction interceptor to this transaction domain.
     *
     * @param interceptor The transaction interceptor
     */
    public abstract void removeInterceptor( TransactionInterceptor interceptor );


    /**
     * Returns a transaction based on the transaction identifier.
     *
     * @param xid The transaction identifier
     * @return The transaction, or null if no such transaction exists
     */
    public abstract Transaction getTransaction( Xid xid );


    /**
     * Shuts down the transaction domain. After this method returns, the
     * transaction manager is no longer able to begin new transactions in
     * this domain.
     */
    public abstract void shutdown();


    /**
     * Returns the transaction currently associated with the given thread,
     * or null if the thread is not associated with any transaction.
     * <p>
     * This method is equivalent to calling {@link
     * TransactionManager#getTransaction getTransaction} from within the thread.
     *
     * @param thread The thread to lookup
     * @return The transaction currently associated with that thread
     */
    public abstract TransactionStatus getTransactionStatus( Thread thread );


    /**
     * Returns an enumeration of all the transactions currently registered
     * in this domain. Each entry is described using the {@link TransactionStatus}
     * object providing information about the transaction, its resources,
     * timeout and active state. Some of that information is only current to
     * the time the list was produced.
     *
     * @return List of all transactions currently registered
     */
    public abstract TransactionStatus[] listTransactions();


    /**
     * Terminates a transaction in progress. The transaction will be rolled
     * back with a timed-out flag and all threads associated with it will be
     * terminated.
     *
     * @param tx The transaction to terminate
     * @throws InvalidTransactionException The transaction did not originate
     * from this domain, or has already been terminated
     */
    public abstract void terminateTransaction( Transaction tx )
	throws InvalidTransactionException;


    /**
     * Convenience method. Dumps information about all the transactions
     * currently registered in this domain to the specified writer.
     */
    public abstract void dumpTransactionList( PrintWriter writer );


    /**
     * Convenience method. Dumps information about the current transaction
     * in this thread to the specified writer.
     */
    public abstract void dumpCurrentTransaction( PrintWriter writer );


}
