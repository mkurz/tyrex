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
 * $Id: ResourceManager.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.server;


import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.xa.XAResource;


/**
 * Defines a temporary interface to allow a resource manager to hook
 * into the transaction manager for the purpose of enlisting and
 * discarding resources. We expect such an API to be available in
 * future versions of EJB. In the mean time, this one provides a
 * way to decouple the resource manager from the transaction manager.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see EnlistedResource
 */
public final class ResourceManager
    implements PrivilegedAction
{


    /**
     * Called by a resource to enlist itself with the currently
     * running transactions. If there is no currently running
     * transaction, the resource will be enlisted with the current
     * thread and with the transaction when one is associated with
     * the thread.
     * <p>
     * Once enlisted in this manner, the resource will be notified
     * when it has been delisted, so it may automatically re-enlist
     * itself upon subsequent use.
     *
     * @param xaRes The XA resource
     * @param enlisted The resource as an enlisted resource
     */
    public static void enlistResource( XAResource res, EnlistedResource enlisted )
	throws SystemException, RollbackException
    {
	( (TransactionManagerImpl) AccessController.doPrivileged( new ResourceManager() ) ).enlistResource( res, enlisted );
    }
    

    
    /**
     * Called by a resource to delist itself with the currently
     * running transactions when the resource is closed due to
     * an error. The resource must not be used after this call
     * is made. Normal closure should never be reported in this
     * manner.
     *
     * @param xaRes The XA resource
     */
    public static void discardResource( XAResource res )
    {
	( (TransactionManagerImpl) AccessController.doPrivileged( new ResourceManager() ) ).discardResource( res );
    }
    

    private ResourceManager()
    {
    }


    public Object run()
    {
	return Tyrex.getTransactionManager();
    }


}
