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
 * $Id: TransactionStatus.java,v 1.2 2000/01/17 22:13:59 arkin Exp $
 */


package tyrex.server;


import java.util.Date;
import org.omg.CosTransactions.Control;
import javax.transaction.Transaction;
import javax.transaction.xa.Xid;


/**
 * Provides information about a transaction. Used by {@link Tyrex} to
 * provide information about a transcation to code that is not part
 * of the transaction server. This information is only current for
 * the time it was obtained from {@link Tyrex}.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/01/17 22:13:59 $
 * @see Tyrex
 */
public final class TransactionStatus
{


    /**
     * The transaction for which status information is
     * provided.
     */
    private TransactionImpl  _tx;


    /**
     * The date/time at which the transaction will timeout.
     */
    private Date             _timeout;


    /**
     * True if the transaction is associated with one or more threads.
     */
    private boolean          _inThread;


    TransactionStatus( TransactionImpl tx, long timeout, boolean inThread )
    {
	_tx = tx;
	_timeout = new Date( timeout );
	_inThread = inThread;
    }


    /**
     * Returns the underlying transaction.
     */
    public Transaction getTransaction()
    {
	return _tx;
    }


    /**
     * Returns the control interface of the underlying transaction.
     */
    public Control getControl()
    {
	return _tx.getControl();
    }


    /**
     * Returns the timeout for the tranasction.
     */
    public Date getTimeout()
    {
	return _timeout;
    }


    /**
     * Returns the status of the transaction.
     *
     * @see javax.transaction.Status
     */
    public int getStatus()
    {
	return _tx.getStatus();
    }


    /**
     * Returns the Xid of the transaction.
     *
     * @see javax.transaction.xa.Xid
     */
    public Xid getXid()
    {
	return _tx.getXid();
    }


    /**
     * Returns a textual description of all the resources enlisted
     * with this transaction.
     */
    public String[] listResources()
    {
	return _tx.listResources();
    }


    /**
     * Returns true if the transaction is currently associated with
     * any (one or more) threads.
     */
    public boolean isInThread()
    {
	return _inThread;
    }


    public String toString()
    {
	return _tx.toString();
    }


}
