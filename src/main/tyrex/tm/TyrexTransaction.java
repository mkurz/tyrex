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
     

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;


/**
 * Tyrex extensions for {@link Transaction}. All Tyrex transactions
 * implement this interface which supports asynchronous commit and
 * rollback, and one phase commit.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public interface TyrexTransaction 
    extends Transaction
{


    /**
     * Perform an asynchronous commit on the transaction.
     *
     * @param callback The object that is registered to receive callbacks
     * during the asynchronous commit. May be null.
     * @throws SystemException A problem occured while associating the
     * transaction with the new thread.
     * @throws SecurityException The current thread is not allowed to
     * rollback the transaction
     * @throws RollbackException The transaction has been marked for rollback
     */
    public void asyncCommit( AsyncCompletionCallback callback )
        throws SystemException, SecurityException, RollbackException;


    /**
     * Perform an asynchronous rollback on the transaction.
     *
     * @param callback The object that is registered to receive callbacks
     * during the asynchronous commit. May be null.
     * @throws IllegalStateException The transaction is not in the proper
     * state to be rolled back
     * @throws SystemException A problem occured while associating the
     * transaction with the new thread.
     * @throws SecurityException The current thread is not allowed to
     * rollback the transaction.
     */
    public void asyncRollback( AsyncCompletionCallback callback )
        throws IllegalStateException, SystemException, SecurityException;
    

    /**
     * Change the timeout for the transaction to the new value. 
     *
     * @param seconds The new timeout in seconds
     */
    public void setTransactionTimeout( int seconds );


    /**
     * Return true if the transaction can be safely committed
     * using one-phase commit.
     *
     * @return True if the transaction can be safely committed
     * using one-phase commit
     */
    public boolean canUseOnePhaseCommit();


    /**
     * Perform one-phase commit on the transaction.
     *
     * @throws RollbackException Indicates that the transaction has been
     * rolled back rather than committed.
     * @throws HeuristicMixedException A heuristic decision was made and
     * that some relevant updates have been committed while others have
     * been rolled back.
     * @throws HeuristicRollbackException A heuristic decision was made
     * and that some relevant updates have been rolled back.
     * @throws SecurityException The thread is not allowed to commit the
     * transaction.
     * @throws IllegalStateException The current thread is not associated
     * with a transaction.
     * @throws SystemException The transaction manager encountered an
     * unexpected error condition
     */
    public void onePhaseCommit()
        throws RollbackException, HeuristicMixedException,
               HeuristicRollbackException, SecurityException,
               IllegalStateException, SystemException;


}
