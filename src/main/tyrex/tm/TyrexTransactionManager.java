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
 */


package tyrex.tm;


import java.io.PrintWriter;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.SystemException;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;


/**
 * Tyrex extensions for {@link TransactionManager}. All Tyrex
 * transaction managers implement this interface, which supports
 * transaction resolving from an Xid, and means to obtain extended
 * transaction status.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public interface TyrexTransactionManager 
    extends TransactionManager
{


    /**
     * Returns a transaction based on the transaction identifier.
     *
     * @param xid The transaction identifier
     * @return The transaction, or null if no such transaction exists
     */
    public abstract Transaction getTransaction( Xid xid );


    /**
     * Returns the status of the transaction associated with the given thread.
     * Returns null if the thread is not associated with any transaction
     * created by this transaction manager.
     * <p>
     * This method is equivalent to calling {@link
     * TransactionManager#getTransaction getTransaction} from within the thread.
     *
     * @param thread The thread
     * @return Status of the transaction currently associated with that thread,
     * or null
     */
    public abstract TransactionStatus getTransactionStatus( Thread thread );


    /**
     * Returns the status of all active transactions created by this
     * transaction manager.
     * <p>
     * Each element of the array is a {@link TransactionStatus} providing
     * information about the transaction, its resources, state and timeout.
     *
     * @return Status of all transactions created by this transaction manager
     */
    public abstract TransactionStatus[] listTransactions();


    /**
     * Convenience method. Dumps information about all active transactions
     * created by this transaction manager.
     *
     * @param writer The writer to use
     */
    public abstract void dumpTransactionList( PrintWriter writer );


    /**
     * Convenience method. Dumps information about the transaction
     * associated with the current thread.
     *
     * @param writer The writer to use
     */
    public abstract void dumpCurrentTransaction( PrintWriter writer );


    /**
     * Called to enlist a resource with the current thread.
     * If this method is called within an active transaction,
     * the connection will be enlisted in that transaction.
     * The connection will be enlisted in any future transaction
     * associated with the same thread context.
     *
     * @param xaRes The XA resource
     * @throws SystemException The resource cannot be enlisted with
     * the current transaction
     */
    public abstract void enlistResource( XAResource xaResource )
        throws SystemException;


    /**
     * Called to delist a resource from the current thread.
     * If this method is called within an active transaction,
     * the connection will be delisted using the success flag.
     * The connection will not be enlisted in any future transaction
     * associated with the same thread context.
     *
     * @param xaRes The XA resource
     * @param flag The delist flag
     */
    public abstract void delistResource( XAResource xaResource, int flag );


}
