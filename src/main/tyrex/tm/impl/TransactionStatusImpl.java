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
 * Copyright 1999-2001 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: TransactionStatusImpl.java,v 1.3 2001/03/12 19:20:20 arkin Exp $
 */


package tyrex.tm.impl;


import org.omg.CosTransactions.Control;
import javax.transaction.Transaction;
import javax.transaction.xa.Xid;
import tyrex.tm.TransactionStatus;


/**
 * Provides information about a transaction. Used by {@link Tyrex} to
 * provide information about a transcation to code that is not part
 * of the transaction server. This information is only current for
 * the time it was obtained from {@link Tyrex}.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2001/03/12 19:20:20 $
 * @see Tyrex
 */
final class TransactionStatusImpl
    extends TransactionStatus
{


    /**
     * The transaction for which status information is
     * provided.
     */
    private final TransactionImpl  _tx;


    TransactionStatusImpl( TransactionImpl tx )
    {
        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        _tx = tx;
    }


    public Transaction getTransaction()
    {
        return _tx;
    }


    public Control getControl()
    {
        return _tx.getControl();
    }


    public long getTimeout()
    {
        return _tx._timeout;
    }


    public long getStarted()
    {
        return _tx._started;
    }


    public int getStatus()
    {
        return _tx._status;
    }


    public Xid getXid()
    {
        return _tx._xid;
    }


    public String[] getResources()
    {
        return _tx.listResources();
    }


    public Thread[] getThreads()
    {
        return (Thread[]) _tx._threads.clone();
    }


    public String toString()
    {
        return _tx.toString();
    }


}
