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
 * $Id: SynchronizationResource.java,v 1.3 2000/09/08 23:04:44 mohammed Exp $
 */


package tyrex.connector;


import javax.transaction.Transaction;


/**
 * Interface for managing transactions through the <tt>Synchronization</tt>
 * interface. Synchronization resources exhibit the following properties:
 * <ul>
 * <li>They take part in two phase commit through the <tt>beforeCompletion</tt>
 *  and <tt>afterCompletion</tt> method calls
 * <li>They are not aware of distributed transactions and cannot access the
 *  XID global identifier
 * <li>They prepare before any <tt>XAResource</tt> and commit or rollback
 *  after all <tt>XAResource</tt> thus can use XA resources internall
 * <li>They may rollback the transaction by calling <tt>setRollbackOnly</tt>
 *  on the transaction
 * <li>They can be associated with any number of transactions during their
 *  life time through the {@link #setTransaction} and {@link #unsetTransaction}
 *  method calls but only with one transaction at any given time
 * </li>
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/08 23:04:44 $
 */
public interface SynchronizationResource
{


    /**
     * Called to associate the resource with the given transaction.
     * Once set the resource may call the following methods on the
     * transaction object:
     * <ul>
     * <li><tt>registerSynchronization</tt> - to register the resource
     *  for participation in two-phase commit
     * <li><tt>setRollbackOnly</tt> - to mark the transaction as
     *  rollback and prevent it's completion
     * <li><tt>enlistResource</tt> - to enlist an XAResource
     * <li><tt>delistResource</tt> - to delist an XAResource
     * </ul>
     * <p>
     * This method will be called at most once after this object
     * has been obtained or after any number of intervening calls
     * to {@link #unsetTransaction}.
     * <p>
     * The resource will enlist itself in the transaction by calling
     * <tt>registerSynchronization</tt> on the transaction.
     *
     * @param trans The transaction associated with this resource
     */
    public void setTransaction( Transaction trans );


    /**
     * Called to dissocaite the resource from the given transaction.
     * After this call the resource may be enlisted with any other
     * transaction through a call to {@link #setTransaction}.
     *
     * @param trans The transaction previously associated with this resource
     */
    public void unsetTransaction( Transaction trans );


}


