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
 */


package tyrex.tm;
     
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

///////////////////////////////////////////////////////////////////////////////
// TyrexTransaction
///////////////////////////////////////////////////////////////////////////////

/**
 *  JTA extensions for Transactions
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
public interface TyrexTransaction 
    extends Transaction
{

    /**
     * Perform an asynchronous commit on the transaction.
     *
     * @param callback the object that is registered
     *      to receive callbacks during the asynchronous
     *      commit. Can be null.
     * @throws SystemException if there is a problem
     *      associating the transaction with the
     *      new thread.
     * @throws SecurityException if the current thread is
     *      not allowed to rollback the transaction.
     * @throws RollbackException if the transaction has been
     *      marked for rollback
     */
    void asyncCommit( AsyncCompletionCallback callback )
        throws SystemException, SecurityException, RollbackException;

    /**
     * Perform an asynchronous rollback on the transaction.
     *
     * @param callback the object that is registered
     *      to receive callbacks during the asynchronous
     *      commit. Can be null.
     * @throws IllegalStateException if the transaction
     *      is not in the proper state to be rolled back
     * @throws SystemException if there is a problem
     *      associating the transaction with the
     *      new thread.
     * @throws SecurityException if the current thread is
     *      not allowed to rollback the transaction.
     */
    void asyncRollback( AsyncCompletionCallback callback )
        throws IllegalStateException, SystemException, SecurityException;
    

    /**
     * Change the timeout for the transaction's resources 
     * to the new value. 
     *
     * @param seconds The new timeout in seconds
     * @see TransactionDomain#setTransactionTimeout
     */
    void setTransactionTimeout( int seconds );


    /**
     * Return true if the transaction can be committed
     * using One-Phase Commit.
     *
     * @return true if the transaction can be committed
     *      using One-Phase Commit.
     */
    boolean canUseOnePhaseCommit();


    /**
     * Perform one-phase commit on the transaction
     *
     * @throws RollbackException Thrown to indicate that the transaction has been rolled back rather than committed.
     * @throws HeuristicMixedException Thrown to indicate that a heuristic decision was made and that some relevant updates have been committed while others have been rolled back.
     * @throws HeuristicRollbackException Thrown to indicate that a heuristic decision was made and that some relevant updates have been rolled back.
     * @throws SecurityException Thrown to indicate that the thread is not allowed to commit the transaction.
     * @throws IllegalStateException Thrown if the current thread is not associated with a transaction.
     * @throws SystemException Thrown if the transaction manager encounters an unexpected error condition
     */
    void onePhaseCommit()
        throws  RollbackException,
                HeuristicMixedException,
                HeuristicRollbackException,
                SecurityException,
                IllegalStateException,
                SystemException;
}
