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
 * $Id: Heuristic.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.server;


/**
 * Defines values for different heuristic decisions. During prepare,
 * commit and rollback the transaction manager will reach certain
 * heuristic decisions regarding the transaction and its resources.
 * <p>
 * If the transaction involves no resources, the heuristic decision
 * will be that the transaction is read-only and does not need commit
 * or rollback. If the transaction has been marked for roll back, the
 * heuristic decision will be to roll back the transaction. Otherwise,
 * if the transaction is being commited the heuristic decision is to
 * commit the transaction.
 * <p>
 * During prepare/commit/rollback, certain resources may not respond
 * properly and the heuristic decision about the transaction might
 * change. For example, attempting to commit a transaction that has one
 * resource that fails to prepare will change it's heuristic decision
 * to rollback.
 * <p>
 * In the event that two resources made conflicting decisions, the
 * heuristic decision will be mixed. This will cause the transaction
 * to attempt and roll back as many remaining resources as possible.
 * <p>
 * The heuristic outcome is made of a combination of any number of
 * flags, with certain flags taking precedence over others:
 * <ul>
 * <li>Transaction is read-only only if it's heuristic decision
 * is equal to {@link #HEURISTIC_READONLY}
 * <li>Transaction has made a commit decision only if it's heuristic
 * decision is equal to {@link #HEURISTIC_COMMIT}
 * <li>Transaction has made a rollback decision only if it's heuristic
 * decision is equal to {@link #HEURISTIC_ROLLBACK}
 * <li>Transaction has made a mixed decision if it's heuristic value
 * has the {@link #HEURISTIC_MIXED} flag set, or both the {@link
 * #HEURISTIC_COMMIT} and {@link #HEURISTIC_ROLLBACK} flags set
 * <li>Transaction has made a hazard decision if it's heuristic value
 * has the {@link #HEURISTIC_HAZARD} flag set
 * </ul>
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see Transaction
 * @see TransactionImpl#normalize
 */
interface Heuristic
{


    /**
     * Indicates that the transaction has no resources or has only
     * read-only resources. A read-only transaction does not need to
     * participate in the second phase.
     */
    public int HEURISTIC_READONLY = 0x00;


    /**
     * Indicates that all resources in the transaction (at least one,
     * excluding any read-only) have agreed to commit during the
     * preparation stage and the transaction could be commited in its
     * entirety.
     */
    public int HEURISTIC_COMMIT = 0x01;


    /**
     * Indicates that one or more resources in the transaction
     * (excluding read-only) could not be prepared or that an error
     * marks this transaction as faulty. The transaction should be
     * rolled back in its entirety.
     */
    public int HEURISTIC_ROLLBACK = 0x02;


    /**
     * Indicates that some resources have commited and others have
     * rolledback, the transaction should be rolled back as much as
     * possible, but could not definitely be rolled back.
     */
    public int HEURISTIC_MIXED = 0x04;


    /**
     * Indicates that resources have commited, or resources have
     * rolled back, but we don't know what was the exact outcome.
     */
    public int HEURISTIC_HAZARD = 0x08;


    /**
     * Special value to indicate the beginning of a transaction when
     * interacting with the recovery log.
     */
    public int BEGIN_TX = 0x0100;


}

