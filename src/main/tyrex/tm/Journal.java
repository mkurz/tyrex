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
 * $Id: Journal.java,v 1.1 2001/03/03 00:36:04 arkin Exp $
 */


package tyrex.tm;


import javax.transaction.SystemException;
import javax.transaction.xa.Xid;


/**
 * Abstract class for transactional journal.
 * <p>
 * This class supports journaling of two-phase commit transactions,
 * recording both prepare, commit, rollback and conslution of transactions.
 * <p>
 * A complete transaction is one for which <tt>XAResource.forget</tt> has
 * been called. The transaction manager is not interested in recovering
 * complete transactions. During journaling this is indicated by the
 * {@link #forget forget} method which closes the transaction chain.
 * During recovery this is indicated by the complete flag and the
 * {@link #completed completed} method.
 * <p>
 * During recovery, the outcome of every recovered transaction must be
 * recorded in the journal to close the transaction chain. Complete
 * transaction must not be recorded, as the chain has already been closed.
 * <p>
 * Transaction journaling is only required when the resource manager
 * participates in a distributed transaction. Local transactions are never
 * recovered and need not be recorded in the journal.
 * <p>
 * If the transaction completes by one-phase commit or rolling back in
 * lieu of two-phase commit, the transaction manager will not attempt
 * to recover the transaction. In this case, it is unnecessary to record
 * the transaction, unless a heuristic decision has been reached.
 * <p>
 * When performing two-phase commit, the outcome of the prepare stage
 * must be recorded, followed by the concluing commit or rollback of the
 * transaction. If an error occured before the transaction is forgotten,
 * the transaction should be recovered.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $
 */
public abstract class Journal
{


    /**
     * Records the outcome of transaction preparation. The decision code
     * indicates whether the transaction is read-only, requires commit or
     * has rolled back.
     * <p>
     * This method is called to record the outcome after preparing a
     * transaction. It must record a decision to commit or rollback the
     * transaction. This method may be skipped if the transaction is
     * identified as read-only or for local transactions.
     * <p>
     * If this method is called, {@link #commit commit} or {@link #rollback
     * rollback} must follow.
     * <p>
     * Valid values for the decision are <tt>XAResource.XA_OK</tt>,
     * <tt>XAResource.XA_RDONLY</tt>, or any of the heuristic codes
     * defined by <tt>XAException.XA_HEUR*</tt>.
     *
     * @param xid The transaction identifier
     * @param decision The outcome of the prepare stage
     * @throw SystemException An error occured while performing this operation
     */
    public abstract void prepare( Xid xid,int decision )
        throws SystemException;


    /**
     * Records a transaction commit.
     * <p>
     * This method is called when completing two-phase commit of a transaction
     * branch. This method may be skipped if the transaction is identified as
     * read-only or is a local transaction. 
     * <p>
     * If this method is called, {@link #forget forget} must follow.
     * <p>
     * If the transaction has properly committed, the heuristic decision will
     * be <tt>XAResource.XA_HEURCOM</tt>. Otherwise, use any of the heuristic
     * codes defined by <tt>XAException.XA_HEUR*</tt>.
     *
     * @param xid The transaction identifier
     * @param decision The heuristic decision
     * @throw SystemException An error occured while performing this opetion
     */
    public abstract void commit( Xid xid, int decision )
        throws SystemException;


    /**
     * Records a transaction rollback.
     * <p>
     * This method is called when completing two-phase commit of a transaction
     * branch. This method may be skipped for local transactions.
     * <p>
     * If this method is called, {@link #forget forget} must follow.
     *
     * @param xid The transaction identifier
     * @throw SystemException An error occured while performing this operation
     */
    public abstract void rollback( Xid xid)
        throws SystemException;


    /**
     * Forgets a heuristically complete transaction.
     * <p>
     * This method is called when completing two-phase commit to discard the
     * transaction and close the transaction chain.
     *
     * @param xid The transaction identifier
     * @throw SystemException An error occured while performing this operation
     */
    public abstract void forget( Xid xid)
        throws SystemException;


    /**
     * Called to initiate recovery. This method will return information
     * about all transactions that are subject to recovery.
     * <p>
     * A recoverable transaction is a transaction that has been processed
     * through two-phase commit but has not been completed. This method
     * will return the heuristic decision for the transaction as reached
     * by the transaction manager.
     * <p>
     * Transactions that have been completed are generally not returned
     * by this method. In addition it is possible that resource managers
     * will return additional transaction branchs for which the transaction
     * manager has not reached any heuristic decision.
     *
     * @return An array of zero of more recoverable transactions
     * @throw SystemException An error occured while performing this operation
     */
    public abstract RecoveredTransaction[] recover()
        throws SystemException;


    /**
     * Called to close the journal and release any resources held by the
     * journal.
     *
     * @throw SystemException An error occured while performing this operation
     */
    public abstract void close()
        throws SystemException;


    /**
     * Provides information about a recovered transaction. An object is
     * returned for each heuristically complete recoverable transaction
     * providing the transaction identifier and the heuristic decision.
     */
    public abstract static class RecoveredTransaction
    {


        /**
         * Returns the transaction identifier.
         *
         * @return The transaction identifier
         */
        public abstract Xid getXid();


        /**
         * Returns the transaction heuristic decision. Valid heuristic values are
         * <ul>
         * <li><tt>XAResource.XA_OK</tt> The transaction has been prepared</li>
         * <li><tt>XAResource.XA_RDONLY</tt> The transaction is read only</li>
         * <li><tt>XAResource.XA_HEURCOM</tt> The transaction has committed</li>
         * <li><tt>XAResource.XA_HEURRBK</tt> The transaction has rolled back</li>
         * <li><tt>XAResource.XA_HEUR*</tt> The transaction outcome is mixed
         * or hazard</li>
         * <li><tt>XAResource.XA_RB*</tt> The transaction has asked to roll back</li>
         * </ul>
         *
         * @return The transaction heuristic decision
         */
        public abstract int getHeuristic();


    }


}

