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
 * $Id: ResourcePoolManager.java,v 1.1 2000/01/17 22:19:14 arkin Exp $
 */


package tyrex.resource;


/**
 * Manages resource utilization for a resource pool or resource
 * factory. The resource pool or resource factory is associated with
 * this manager through {@link #manage}. It then calls {@link
 * #canCreateNew} and {@link #released} when attempting to create a new
 * resource and when returning a resource to the pool or disposing of a
 * resource.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/17 22:19:14 $
 * @see PooledResources
 */
public interface ResourcePoolManager
{


    /**
     * Called to associate this resource pool manager with a resource
     * pool or factory. If <tt>background</tt> is true, a background
     * thread will be created to track and release pooled resources.
     *
     * @param pool The resource pool or factory
     * @param background True if resource releasing background thread
     *   is required
     * @throws IllegalStateException If this resource pool manager is
     *   already associated with a pool
     */
    public void manage( ResourcePool pool, boolean background );


    /**
     * Called to dissociate this resource pool manager with the
     * resource pool or factory and terminate the bacgkround thread.
     *
     * @param pool The resource pool or factory
     * @throws IllegalArgumentException If this resource pool manager
     *   is not associated with the pool
     */
    public void unmanage( ResourcePool pool );


    /**
     * Called by the resource pool or factory before creating a new
     * resource. If the pool has reached its maximum capacity, as
     * determined by a call to {@link #getResourceCount} this method
     * will block until a resource becomes available, or the timeout
     * has passed. If the timeout has passed, this method will throw
     * a {@link ResourceTimeoutException}. This method must be called
     * with a lock on the resource pool or factory to prevent a race
     * condition.
     *
     * @throws TimeoutException The 
     */
    public void canCreateNew()
	throws ResourceTimeoutException;



    /**
     * Called to notify the resource pool manager that a resource has
     * been released, either to the pool or destroyed, and that a new
     * resource may be acquired or created.
     */
    public void released();


}





