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

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;


/**
 * This interface defines methods that allow a transaction manager to
 * be used by a container like an ejb container. It is an extension of 
 * {@link javax.transaction.TransactionManager}.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public interface TyrexTransactionManager 
    extends TransactionManager
{


    /**
     * Called to enlist a resource with the current thread.
     * The resource will be enlisted in the current transaction
     * or any future transaction associated with this thread.
     *
     * @param xaRes The XA resource
     */
    public void enlistResource( XAResource xaResource )
        throws SystemException, RollbackException;

    
    /**
     * Called to delist a resource from the current thread.
     * The resource will not be enlisted in any future transactions.
     * The delist flag must be {@link XAResource#TMSUCCESS} or
     * {@link XAResource#TMFAIL}.
     *
     * @param xaRes The XA resource
     * @param flag The delist flag
     */
    public void delistResource( XAResource xaResource, int flag )
        throws SystemException, RollbackException;
    

}
