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
 * $Id: TransactionStatus.java,v 1.3 2001/02/27 00:34:07 arkin Exp $
 */


package tyrex.tm;


import org.omg.CosTransactions.Control;
import javax.transaction.Transaction;
import javax.transaction.xa.Xid;


/**
 * Provides information about a transaction. Used by {@link Tyrex} to
 * provide information about a transcation to code that is not part
 * of the transaction server. This information is only current for
 * the time it was obtained from {@link Tyrex}.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2001/02/27 00:34:07 $
 * @see Tyrex
 */
public abstract class TransactionStatus
{


    /**
     * Returns the underlying transaction.
     *
     * @return The underlying transaction
     */
    public abstract Transaction getTransaction();


    /**
     * Returns the control interface of the underlying transaction.
     *
     * @return The control interface
     */
    public abstract Control getControl();


    /**
     * Returns the timeout for the tranasction. This is the system clock
     * at which the transaction will time out.
     *
     * @return The timeout for the tranasction
     */
    public abstract long getTimeout();


    /**
     * Returns the start time of the tranasction.
     *
     * @return The start time of the tranasction
     */
    public abstract long getStarted();


    /**
     * Returns the status of the transaction.
     *
     * @return The status of the transaction
     * @see javax.transaction.Status
     */
    public abstract int getStatus();


    /**
     * Returns the Xid of the transaction.
     *
     * @return The Xid of the transaction
     */
    public abstract Xid getXid();


    /**
     * Returns a textual description of all the resources enlisted
     * with this transaction.
     *
     * @return A textual description of all the enlisted resources
     */
    public abstract String[] listResources();


    /**
     * Returns true if the transaction is currently associated with
     * any (one or more) threads.
     *
     * @return True if the transaction is currently associated with
     * one or more threads
     */
    public abstract boolean isInThread();


}
