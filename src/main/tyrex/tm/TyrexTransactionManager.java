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
 */


package tyrex.tm;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;


///////////////////////////////////////////////////////////////////////////////
// TyrexTransactionManager
///////////////////////////////////////////////////////////////////////////////

/**
 * This interface defines methods that allow a
 * transaction manager to be used by a container like
 * an ejb container. It is an extension of 
 * javax.transaction.TransactionManager.
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
public interface TyrexTransactionManager 
    extends TransactionManager
{
    /**
     * Called by a resource to enlist itself with the currently
     * running transactions. JDBC connections created through
     * {@link tyrex.jdbc.ServerDataSource} will automatically enlist
     * themselves with the currently running transactions. If there
     * is not currently running transaction, the resource will be
     * enlisted with the current thread and with the transaction
     * when one is associated with the thread.
     *
     * @param xaRes The XA resource
     */
    public void enlistResource( XAResource xaRes )
	    throws SystemException, RollbackException;
    
    /**
     * Called by a resource to delist itself with the currently
     * running transactions. 
     *
     * @param xaRes The XA resource
     */
    public void delistResource( XAResource xaRes )
	    throws SystemException, RollbackException;
    
    /**
     * Called by a resource to delist itself with the currently
     * running transactions. Equivalent to
     * calling {link #delistResource(XAResource)} in the current
     * thread.
     *
     * @param xaRes The XA resource
     * @param thread the thread to delist the resource from
     */
    void delistResource( XAResource xaRes, Thread thread )
	    throws SystemException, RollbackException;
    
    /**
     * Called by a resource to enlist itself with the currently
     * running transactions. JDBC connections created through
     * {@link tyrex.jdbc.ServerDataSource} will automatically enlist
     * themselves with the currently running transactions. If there
     * is no currently running transaction, the resource will be
     * enlisted with the current thread and with the transaction
     * when one is associated with the thread.
     * <p>
     * Once enlisted in this manner, the resource will be notified
     * when it has been delisted, so it may automatically re-enlist
     * itself upon subsequent use.
     *
     * @param xaRes The XA resource
     * @param enlisted The resource as an enlisted resource
     */
    public void enlistResource( XAResource xaRes, EnlistedResource enlisted )
	    throws SystemException, RollbackException;

    
    /**
     * Called by a resource to delist itself with the currently
     * running transactions. 
     *
     * @param xaRes The XA resource
     * @param enlisted The resource as an enlisted resource
     */
    public void delistResource( XAResource xaRes, EnlistedResource enlisted )
	    throws SystemException, RollbackException;
    
    
    /**
     * Called by a resource to delist itself with the currently
     * running transactions. Equivalent to calling 
     * {link delistResource(XAResource, EnlistedResource)}.
     *
     * @param xaRes The XA resource
     * @param enlisted The resource as an enlisted resource
     * @param thread the thread to delist the resources from
     */
    void delistResource( XAResource xaRes, EnlistedResource enlisted, Thread thread )
	    throws SystemException, RollbackException;
    
    
    /**
     * Called by a resource to delist itself with the currently
     * running transactions when the resource is closed due to
     * an error. The resource must not be used after this call
     * is made. Normal closure should never be reported in this
     * manner. The resource will be delisted when the transaction
     * terminates or {@link #delistResource} is called.
     *
     * @param xaRes The XA resource
     */
    public void discardResource( XAResource xaRes );
    

    /**
     * Called by a resource to delist itself with the currently
     * running transactions when the resource is closed due to
     * an error. The resource must not be used after this call
     * is made. Normal closure should never be reported in this
     * manner. The resource will be delisted when the transaction
     * terminates or {@link #delistResource} is called.
     *
     * @param xaRes The XA resource
     * @param enlisted the resource as an enlisted resource
     */
    public void discardResource( XAResource xaRes, EnlistedResource enlisted );

	
    /**
     * Must be called by the application server after (or before) this
     * thread is being used on behalf of a bean. The thread is
     * associated with a list of resources that are relevant for the
     * previous invocation, but not the new one. If this method is not
     * called, memory consumption will simply increase over time.
     * <p>
     * If the thread is associated with an active transaction, the
     * transaction will be rolled back and a {@link RollbackException}
     * will be thrown.
     *
     * @throws RollbackException The thread is still associated
     *   with an active transaction, the transaction was rolled back
     */
    public void recycleThread()
	    throws RollbackException;


    /**
     * Set the timeout for the current transaction.
     *
     * @param seconds the number of seconds to set the
     *      timeout to
     */
    public void setTransactionTimeout( int seconds );
}
