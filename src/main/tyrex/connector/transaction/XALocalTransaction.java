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
 */


package tyrex.connector.transaction;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import tyrex.connector.ConnectionException;
import tyrex.connector.LocalTransaction;
import tyrex.tm.XidImpl;

///////////////////////////////////////////////////////////////////////////////
// XALocalTransaction
///////////////////////////////////////////////////////////////////////////////

/**
 * Implementation of {@link LocalTransaction} that uses an
 * XAResource.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public final class XALocalTransaction 
    implements LocalTransaction
{
    /**
     * The xa resource
     */
    private final XAResource xaResource;

    /**
     * The current XID
     */
    private Xid xid = null;

    /**
     * The listener for XALocalTransaction changes
     */
    private final XALocalTransactionListener listener;


    /**
     * Create the XALocalTransaction with the
     * the specified xa resource.
     *
     * @param xaResource the xa resource.
     *      Cannot be null.
     * @param listener the xa local transaction listener.
     *      Cannot be null.
     */
    public XALocalTransaction(XAResource xaResource, XALocalTransactionListener listener)
    {
        if (null == xaResource) {
            throw new IllegalArgumentException("The argument 'xaResource' is null.");
        }
        if (null == listener) {
            throw new IllegalArgumentException("The argument 'listener' is null.");    
        }

        this.xaResource = xaResource;
        this.listener = listener;
    }

    public synchronized void begin() 
        throws ConnectionException
    {
        if (null != xid) {
            throw new ConnectionException("Transaction in process: Nested transactions are not supported.");
        }

        xid = new XidImpl();

        try {
            xaResource.start(xid, XAResource.TMNOFLAGS);

            try {
                listener.beginCalled();
            }
            catch (Exception e){

            }
        }
        catch(XAException e) {
            throw new ConnectionException("Failed to begin transaction", e);
        }
    }

    public synchronized void commit() 
        throws ConnectionException
    {
        if (null == xid) {
            throw new ConnectionException("No transaction in process.");
        }

        try {
            xaResource.end(xid, XAResource.TMSUCCESS);
            xaResource.commit(xid, true);

            try {
                listener.commitCalled();
            }
            catch (Exception e){

            }
        }
        catch(XAException e) {
            handleXAException("Failed to commit transaction", e);
        }
        finally {
            xid = null;
        }
    }

    /**
     * Handle the specified xaException by releasing the resources in the
     * xaResource if the xaException specifies a heuristic exception, and
     * finally throwing a connection exception with the specified message
     * and specified xaException
     *
     * @param exceptionMesssage the message of the connection exception
     * @param xaException the xa exception that occurred.
     */
    private void handleXAException(String exceptionMesssage, XAException xaException)
        throws ConnectionException
    {
        String forgetException = null;

        try { 
            int errorCode = xaException.errorCode;

            if ((errorCode == XAException.XA_HEURCOM) ||
                (errorCode == XAException.XA_HEURHAZ) ||
                (errorCode == XAException.XA_HEURMIX) ||
                ((errorCode >= XAException.XA_RBBASE) && (errorCode <= XAException.XA_RBEND))) {
                xaResource.forget(xid);
            }
        }
        catch (Exception e) {
            forgetException = e.toString();
        }

        throw new ConnectionException(exceptionMesssage + 
                                      (null == forgetException ? "" : "(Forget failed: " + forgetException + ")"), 
                                      xaException);
    }

    public synchronized void rollback() 
        throws ConnectionException
    {
        if (null == xid) {
            throw new ConnectionException("No transaction in process.");
        }

        try {
            xaResource.end(xid, XAResource.TMSUCCESS);
            xaResource.rollback(xid);

            try {
                listener.rollbackCalled();
            }
            catch (Exception e){

            }
        }
        catch(XAException e) {
            handleXAException("Failed to rollback transaction", e);
        }
        finally {
            xid = null;
        }
    }

    /**
     * Forget the resources if a heuristic exception occurred,
     * as indicated by the error code of the specified xa
     * exception.
     *
     * @param xaException the xa exception 
     * @throws XAException if there is an error in forgetting
     *      the resources.
     */
    private void forget(XAException xaException)
        throws XAException
    {
        int errorCode = xaException.errorCode;

        if ((errorCode == XAException.XA_HEURCOM) ||
            (errorCode == XAException.XA_HEURHAZ) ||
            (errorCode == XAException.XA_HEURMIX) ||
            ((errorCode >= XAException.XA_RBBASE) && (errorCode <= XAException.XA_RBEND))) {
            xaResource.forget(xid);
        }
    }

    public static interface XALocalTransactionListener
    {
        /**
         * Begin called
         */
        void beginCalled();

        /**
         * This method is called when commit is called on the
         * XALocalTransaction
         */
        void commitCalled();

        /**
         * This method is called when rollback is called on
         * the XALocalTransaction
         */
        void rollbackCalled();
    }
}
