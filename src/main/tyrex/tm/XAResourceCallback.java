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
 *    permission of Intalio Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Technologies. Exolab is a registered
 *    trademark of Intalio Technologies.
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
 * Copyright 2001 (C) Intalio Technologies Inc. All Rights Reserved.
 *
 */


package tyrex.tm;

import javax.transaction.xa.Xid;

/////////////////////////////////////////////////////////////////////
// XAResourceCallback
/////////////////////////////////////////////////////////////////////

/**
 * This interface defines methods that inform when the XA resource 
 * associated with this callback has been enlisted in a transaction,
 * i.e. javax.transaction.XA.XAResource.start(javax.transaction.XA.XAResource.TMSTART)
 * has been called, delisted from the transaction, i.e.
 * javax.transaction.XA.XAResource.end(javax.transaction.XA.XAResource.TMSUCCESS 
 * or javax.transaction.XA.XAResource.TMFAIL) has been called, 
 * committed/rolled back, i.e.javax.transaction.XA.XAResource.commit()
 * or javax.transaction.XA.XAResource.rollback(). It is assumed that
 * a XAResourceCallback is associated with only one XA Resource.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public interface XAResourceCallback {

    /**
     * Called when the XA resource associated with this callback 
     * has been enlisted in a transaction,i.e. 
     * javax.transaction.XA.XAResource.start(javax.transaction.XA.XAResource.TMNOFLAGS)
     * has been called.
     *
     * @param xid the xid that was used to enlist the XA resource
     *      (required)
     */
    void enlist(Xid xid);

    /**
     * Called when the XA resource associated with this callback 
     * has been delisted from a transaction,i.e. 
     * javax.transaction.XA.XAResource.end(javax.transaction.XA.XAResource.TMFAIL)
     * has been called.
     *
     * @param xid the xid that was used to enlist the XA resource
     *      (required)
     * @see #enlist
     */
    void fail(Xid xid);

    /**
     * Called when the XA resource associated with this callback 
     * has been committed/rolledback in a transaction,i.e. 
     * javax.transaction.XA.XAResource.commit() or
     * javax.transaction.XA.XAResource.rollback()
     * has been called.
     *
     * @param xid the xid that was used to enlist the XA resource
     *      (required)
     * @param commit True if the XA resource has been committed.
     *      False if the XA resource has been rolled back.
     * @see #enlist
     */
    void boundary(Xid xid, boolean commit);
}
