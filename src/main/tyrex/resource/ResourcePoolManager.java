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
 * $Id: ResourcePoolManager.java,v 1.3 2000/08/28 19:01:49 mohammed Exp $
 */


package tyrex.resource;

import java.io.Serializable;

/**
 * Manages active and pooled resource utilization for a resource
 * manager. The resource manager must implement the {@link
 * ResourcePool} interface.
 * <p>
 * The resource pool manager has methods to set and access its properties,
 * can be serialized and stored in a JNDI directory. The resource
 * manager abides to these limitations by calling the {@link
 * #canCreateNew} and {@link #canActivate} methods. The pool manager
 * may be created with a background thread to automatically release
 * pooled resources.
 * <p>
 * Resources fall into two categories: active and pooled. There is
 * an upper limit on the number of resources that is available at
 * any given time (active and pooled), and a separate upper limit
 * for the number of active resources. Some implementations may
 * allow certain resources to be pooled, but not concurrently used
 * (typically, when not all pooled resources are equivalent).
 * <p>
 * In addition, a desired size specify the number of resources
 * that should be available from the pool at any given time, to
 * save creation of new resources in off-peak periods. While
 * resources are not heavily used, they are gradually released until
 * the desired size is reached, over a period of time specified
 * linearily using the prune factory and the check every duration.
 * <p>
 * The following properties are part of the pool manager definition,
 * though not all of them apply to every type of pool:
 * <PRE>
 * JNDI/Bean    XML           See
 * -----------  ------------  ------------
 * upperLimit   upper-limit   {@link #setUpperLimit}
 * activeLimit  active-limit  {@link #setActiveLimit}
 * waitTimeout  wait-timeout  {@link #setWaitTimeout}
 * desiredSize  desired-size  {@link #setDesiredSize}
 * pruneSize    prune-size    {@link #setPruneSize}
 * checkEvery   check-every   {@link #setCheckEvery}
 * </pre>
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/08/28 19:01:49 $
 * @see PooledResources
 */
public interface ResourcePoolManager
    extends ResourcePool, Serializable
{
    /**
     * Return the pool managed by the manager. Can be null.
     *
     * @return the pool managed by the manager.
     */
    ResourcePool getPool();

    /**
     * Called to use this pool manager with a pooled resource. Must be
     * called if if the default constructor was used. If <tt>background</tt>
     * is true, creates a background thread to release pooled resources
     * above the desired size.
     *
     * @param pooled The pooled resource to manage
     * @param background True if resource releasing background thread
     *   is required
     * @throws IllegalStateException If this pool manager is already
     *   associated with a pool
     */
    void manage( ResourcePool pool, boolean background );

    /**
     * Called when this resource pool manager is no longer managing the
     * resource pool. Will terminate the background thread, if one is active.
     */
    void unmanage();

    /**
     * Set the resource limits for the resource pool manager.
     * If the specified argument is null then the default resource
     * limits is used.
     * @param resourceLimits the resource
     * @see ResourceLimits
     */
    void setResourceLimits(ResourceLimits resourceLimits);


    /**
     * Return the resource limits associated with the resource pool manager.
     * The same copy used by the resource pool manager is returned so changes
     * to the resource limit object will affect the resource pool manager.
     * @return the resource limits used by the resource pool manager.
     * @see ResourceLimits
     */
    ResourceLimits getResourceLimits();


    /**
     * Called by the pool to determine if a new resource can be
     * created. If the upper limit has been reach, this call will
     * block until a resource becomes available, the pool goes below
     * the upper limit, or the timeout has occured. If the timeout
     * has passed, will throw an {@link TimeoutException}.
     * <p>
     * Do not call this method if you are planning on reusing an
     * element from the pool, call {@link #canActivate} instead.
     *
     * @throws ResourceTimeoutException A timeout has occured waiting to
     *   create a new resource
     * @see #setWaitTimeout
     * @see #released
     */
    void canCreateNew()
	    throws ResourceTimeoutException;
    
    /**
     * Called by the pool to determine if a pooled resource can be
     * activated. If the active limit has been reach, this call will
     * block until the active countl goes below the active limit, or
     * the timeout has occured. If the timeout has passed, will throw
     * an {@link TimeoutException}.
     *
     * @throws ResourceTimeoutException A timeout has occured waiting to
     *   activate a resource
     * @see #setWaitTimeout
     * @see #released
     */
    void canActivate()
	    throws ResourceTimeoutException;
    
    /**
     * Called to notify the pool manager that an active resource has
     * been de-activated or released, notifying waiting threads that
     * they may activate a resource or create a new one.
     *
     * @see #canCreateNew
     * @see #canActivate
     */
    void released();
}




