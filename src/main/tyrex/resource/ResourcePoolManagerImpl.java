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
 * $Id: ResourcePoolManagerImpl.java,v 1.4 2000/08/28 19:01:49 mohammed Exp $
 */


package tyrex.resource;

import tyrex.util.Messages;

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
 * @version $Revision: 1.4 $ $Date: 2000/08/28 19:01:49 $
 * @see PooledResources
 */
public final class ResourcePoolManagerImpl
    implements ResourcePoolManager, Runnable
{
    /**
     * The limits for managing the resources.
     */
    private ResourceLimits _resourceLimits;

    /**
     * The actual resource pool implementation managed by this 
     * resource pool manager.
     */
    private transient ResourcePool  _pool;


    /**
     * A background thread used for releasing pooled resources.
     */
    private transient Thread  _background;

    /**
     * The default resource limits
     */
    private static class DefaultResourceLimits
    {
        private static ResourceLimits defaultResourceLimits = new ResourceLimits();
    }

    /**
     * Default constructor creates a new resource pool manager with no
     * associated resource pool and default resource limits. 
     * Must call {@link #manage} on this pool manager before using it.
     * @see ResourceLimits
     */
    public ResourcePoolManagerImpl()
    {
        this(null);
    }

    /**
     * Creates a new resource pool manager with the specified resource limits
     * and with no associated resource pool. Must call {@link #manage} on 
     * this pool manager before using it.
     * If the resource limits is null then a default resource limits is used,
     * 
     * @param resourceLimits the limits for managing the resources. 
     * @see ResourceLimits
     */
    public ResourcePoolManagerImpl(ResourceLimits resourceLimits)
    {
        setResourceLimits(resourceLimits);
    }

    /**
     * Constructs a new pool manager for use with the specified pool.
     * Default resource limits are used.
     * If <tt>background</tt> is true, creates a background thread to
     * release pooled resources above the desired size.
     *
     * @param pool The resource pool to manage
     * @param background True if resource releasing background thread
     *   is required
     * @see ResourceLimits
     */
    public ResourcePoolManagerImpl( ResourcePool pool, boolean background )
    {
	    this(pool, null, background);
    }

    /**
     * Constructs a new pool manager for use with the specified pool.
     * If <tt>background</tt> is true, creates a background thread to
     * release pooled resources above the desired size.
     * If the specified resource limits is null then default resource
     * is used.
     *
     * @param pool The resource pool to manage
     * @param resourceLimits the limits for managing the resources. 
     * @param background True if resource releasing background thread
     *   is required
     * @see ResourceLimits
     */
    public ResourcePoolManagerImpl( ResourcePool pool, ResourceLimits resourceLimits, boolean background )
    {
	    setResourceLimits(resourceLimits);
        manage( pool, background );
    }

    /**
     * Set the resource limits for the resource pool manager.
     * If the specified argument is null then the default resource
     * limits is used.
     * @param resourceLimits the resource
     * @see ResourceLimits
     */
    public void setResourceLimits(ResourceLimits resourceLimits)
    {
        _resourceLimits = (null == resourceLimits) 
                            ? DefaultResourceLimits.defaultResourceLimits 
                            : resourceLimits;
    }

    /**
     * Return the resource limits associated with the resource pool manager.
     * The same copy used by the resource pool manager is returned so changes
     * to the resource limit object will affect the resource pool manager.
     * @return the resource limits used by the resource pool manager.
     * @see ResourceLimits
     */
    public ResourceLimits getResourceLimits()
    {
        return _resourceLimits;
    }

    /**
     * Return the pool managed by the manager. Can be null.
     *
     * @return the pool managed by the manager.
     */
    public ResourcePool getPool()
    {
        return _pool;
    }

    /**
     * Called to use this pool manager with a pooled resource. Must be
     * called if if the default constructor was used. If <tt>background</tt>
     * is true, creates a background thread to release pooled resources
     * above the desired size.
     *
     * @param pool The resource pool to manage
     * @param background True if resource releasing background thread
     *   is required
     * @throws IllegalStateException If this pool manager is already
     *   associated with a pool
     */
    public synchronized void manage( ResourcePool pool, boolean background )
    {
	if ( pool == null )
	    throw new NullPointerException( "Argument 'pool' is null" );
	if ( _pool != null )
	    throw new IllegalStateException( Messages.message( "tyrex.pool.alreadyManaging" ) );
	_pool = pool;
	if ( _background == null && background ) {
	    _background = new Thread( this );
	    _background.setName( Messages.message( "tyrex.pool.daemonName" ) );
	    _background.setPriority( Thread.MIN_PRIORITY );
	    _background.setDaemon( true );
	    _background.start();
	}
    }


    /**
     * Called when this resource pool manager is no longer managing the
     * resource pool. Will terminate the background thread, if one is active.
     */
    public void unmanage()
    {
	if ( _background != null ) {
	    _background.interrupt();
	    try {
		_background.join();
	    } catch ( InterruptedException except ) { }
	    _background = null;
	    _pool = null;
	}
    }
    

    /**
     * Called by the resource pool to determine if a new resource can be
     * created. If the upper limit has been reach, this call will
     * block until a resource becomes available, the resource pool goes below
     * the upper limit, or the timeout has occured. If the timeout
     * has passed, will throw an {@link TimeoutException}.
     * <p>
     * Do not call this method if you are planning on reusing an
     * element from the resource pool, call {@link #canActivate} instead.
     *
     * @throws ResourceTimeoutException A timeout has occured waiting to
     *   create a new resource
     * @see #setWaitTimeout
     * @see #released
     */
    public void canCreateNew()
	throws ResourceTimeoutException
    {
	long timeout;
	long current;
	int  poolSize;
	
	if ( _resourceLimits.getUpperLimit() < 0 )
	    return;

	// Synchronization is required so we can be notified
	// by released().
	synchronized ( _pool ) {
        if ( ( _pool.getPooledCount() + _pool.getActiveCount() ) < _resourceLimits.getUpperLimit() )
		return;
	    // If a timeout was specified, wait until the timeout
	    // has been reached, or a resource has been released,
	    // whichever comes first.
	    if ( _resourceLimits.getWaitTimeout() > 0 ) {
		current = System.currentTimeMillis();
		timeout = current + _resourceLimits.getWaitTimeout();
		while ( current < timeout ) {
		    try {
			_pool.wait( timeout - current );
		    } catch ( InterruptedException except ) { }
		    poolSize = _pool.getPooledCount();
		    if ( ( _pool.getPooledCount() + _pool.getActiveCount() ) < _resourceLimits.getUpperLimit() )
			return;
		    current = System.currentTimeMillis();
		}
	    }
	    throw new ResourceTimeoutException( Messages.message( "tyrex.pool.timeoutCreate" ) );
	}
    }
    

    /**
     * Called by the resource pool to determine if a pooled resource can be
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
    public void canActivate()
	throws ResourceTimeoutException
    {
	long timeout;
	long current;
	
	if ( _resourceLimits.getActiveLimit() < 0 )
	    return;

	// Synchronization is required so we can be notified
	// by released().
	synchronized ( _pool ) {
	    if ( _pool.getActiveCount() < _resourceLimits.getActiveLimit() )
		return;
	    // If a timeout was specified, wait until the timeout
	    // has been reached, or a resource has been released,
	    // whichever comes first.
	    if ( _resourceLimits.getWaitTimeout() > 0 ) {
		current = System.currentTimeMillis();
		timeout = current + _resourceLimits.getWaitTimeout();
		while ( current < timeout ) {
		    try {
			_pool.wait( timeout - current );
		    } catch ( InterruptedException except ) { }
		    if ( _pool.getActiveCount() < _resourceLimits.getActiveLimit() )
			return;
		    current = System.currentTimeMillis();
		}
	    }
	    throw new ResourceTimeoutException( Messages.message( "tyrex.pool.timeoutActivate" ) );
	}
    }


    /**
     * Called to notify the resource pool manager that an active resource has
     * been de-activated or released, notifying waiting threads that
     * they may activate a resource or create a new one.
     *
     * @see #canCreateNew
     * @see #canActivate
     */
    public void released()
    {
	synchronized ( _pool ) {
	    _pool.notify();
	}
    }


    public int getActiveCount()
    {
	return _pool.getActiveCount();
    }


    public int getPooledCount()
    {
	return _pool.getPooledCount();
    }


    public void releasePooled( int size )
    {
	if ( _pool != null )
	    _pool.releasePooled( size );
    }
    

    /**
     * Background thread responsible for releasing pools resources.
     *
     * @see #setDesiredSize
     * @see #setCheckEvery
     * @see #setPruneFactor
     */
    public void run()
    {
	int size;

	// We go to sleep for _checkEvery milliseconds.
	// The background thread is terminated by interrupting it.
	try {
	    while ( true ) {
		Thread.sleep( _resourceLimits.getCheckEvery() );
		try {
		    // Get the total resource count, deduct the
		    // desired size, and apply the prune factor
		    // to determine how many resources to release.
		    // We add one to the result to compensate for
		    // fractions, otherwise, we might never reach
		    // the desired size.
		    size = _pool.getPooledCount();
		    if ( size > _resourceLimits.getDesiredSize() ) {
			size = (int) ( ( size - _resourceLimits.getDesiredSize() ) * _resourceLimits.getPruneFactor() ) + 1;
			_pool.releasePooled( size );
		    }
		} catch ( Exception except ) { }
	    }
	} catch ( InterruptedException except ) { }
    }


}




