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
 * $Id: TransactionInterceptor.java,v 1.1 2000/04/10 20:53:07 arkin Exp $
 */


package tyrex.interceptor;


import javax.transaction.RollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.xa.Xid;


/**
 * The interceptor interface allows an external engine to hook up into
 * the transaction monitor and either record the outcome of
 * transactions or affect them. The interceptor is notified when
 * transactions are created, attempt to commit, rolled back, and when
 * they are resumed or suspended from threads. The interceptor may
 * affect the outcome of an attempt to commit or resume a transaction.
 * The interceptor is notified of the outcome of each transaction in
 * the form of a heuristic decision made regarding the transaction and
 * all it's resources.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/04/10 20:53:07 $
 */
public interface TransactionInterceptor
{


    /**
     * Called to indicate that a transaction has begun. The
     * transactions's identifier is provided.
     *
     * @param Xid The transaction identifier
     */
    public void begin( Xid xid );


    /**
     * Called to indicate that a transaction has been asked to commit.
     * Called prior to committing the transaction, and the outcome
     * of this method call might affect the result. If the method
     * throws a {@link RollbackException} or marks the transaction
     * for rollback, the transaction will not commit.
     *
     * @param xid The transaction identifier
     * @throws RollbackException Thrown to indicate that the
     *   transaction must not commit
     */
    public void commit( Xid xid )
	throws RollbackException;


    /**
     * Called to indicate that a transaction has been asked to
     * rollback. Called prior to rolling back the transaction.
     * All exceptions are ignored.
     *
     * @param xid The transaction identifier
     */
    public void rollback( Xid xid );


    /**
     * Called to indicate that a heuristic decision has been made
     * regarding this transaction and the outcome of such a
     * decision. Called after the transaction had been completed
     * and prior to forgetting about the transaction.
     *
     * @param xid The transaction identifier
     * @param heuristic The heuristic decision
     */
    public void completed( Xid xid, int heuristic );


    /**
     * Called to indicate that a transaction is been resumed in
     * the specified thread. The thread is now associated with
     * the transaction. May affect the outcome of this operation
     * by throwing a {@link InvalidTransactionException} to prevent
     * the thread from resuming.
     *
     * @param xid The transaction identifier
     * @param thread The associated thread
     * @throws InvalidTransactionException Prevents the thread
     *   from being associated with this transaction
     */
    public void resume( Xid xid, Thread thread )
	throws InvalidTransactionException;


    /**
     * Called to indicate that a transaction has been suspended
     * from the specified thread, and the thread is no longer
     * associated with the transaction.
     *
     * @param xid The transaction identifier
     * @param thread The associated thread
     */
    public void suspend( Xid xid, Thread thread );


}
