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
 * $Id: PoolManager.java,v 1.2 2000/01/17 22:21:39 arkin Exp $
 */


package tyrex.util;


import java.io.Serializable;


/**
 * Manages active and pooled resource utilization for a resource
 * manager. The resource manager must implement the {@link
 * PooledResources} interface.
 * <p>
 * The pool manager has methods to set and access its properties,
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
 * @version $Revision: 1.2 $ $Date: 2000/01/17 22:21:39 $
 * @see PooledResources
 */
public final class PoolManager
    implements PooledResources, Runnable, Serializable
{


    /**
     * Defines the upper limit on available resources (active and
     * pooled). The default is no upper limit.
     */
    private int         _upperLimit = -1;


    /**
     * Defines the desired number of available resources at all time.
     * The pool may, but is not recommended, to fall below this size.
     * The default is zero.
     */
    private int         _desiredSize = 0;


    /**
     * Defines the upper limit on concurrently active resources.
     * The default is no upper limit.
     */
    private int         _activeLimit = -1;


    /**
     * Defines the time out waiting to activate a pooled resource or
     * create a new resource. This value is specified in milliseconds.
     * The default is ten seconds.
     */
    private int         _waitTimeout = 10 * 1000;


    /**
     * The prune factor ranges from 0.0 to 1.0 and defines the
     * percentage of resources that should be removed from the pool
     * in each iteration. The default is 10%.
     */
    private float       _pruneFactor = 0.1F;


    /**
     * Interval between checks for and request to release pooled
     * resources. This value is specified in milliseconds.
     * The default is ten seconds.
     */
    private int         _checkEvery = 10 * 1000;


    /**
     * The actual pool implementation managed by this pool manager.
     */
    private transient PooledResources  _pooled;


    /**
     * A background thread used for releasing pooled resources.
     */
    private transient Thread  _background;


    /**
     * Default constructor creates a new pool manager with no
     * associated pool. Must call {@link #manage} on this pool manager
     * before using it.
     */
    public PoolManager()
    {
    }


    /**
     * Constructs a new pool manager for use with the specified pool.
     * If <tt>background</tt> is true, creates a background thread to
     * release pooled resources above the desired size.
     *
     * @param pooled The pooled resource to manage
     * @param background True if resource releasing background thread
     *   is required
     */
    public PoolManager( PooledResources pooled, boolean background )
    {
	manage( pooled, background );
    }


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
    public synchronized void manage( PooledResources pooled, boolean background )
    {
	if ( pooled == null )
	    throw new NullPointerException( "Argument 'pooled' is null" );
	if ( _pooled != null )
	    throw new IllegalStateException( Messages.message( "tyrex.pool.alreadyManaging" ) );
	_pooled = pooled;
	if ( _background == null && background ) {
	    _background = new Thread( this );
	    _background.setName( Messages.message( "tyrex.pool.daemonName" ) );
	    _background.setPriority( Thread.MIN_PRIORITY );
	    _background.setDaemon( true );
	    _background.start();
	}
    }


    /**
     * Called when this pool manager is no longer managing the pooled
     * resource. Will terminate the background thread, if one is active.
     */
    public void unmanage()
    {
	if ( _background != null ) {
	    _background.interrupt();
	    try {
		_background.join();
	    } catch ( InterruptedException except ) { }
	    _background = null;
	    _pooled = null;
	}
    }


    /**
     * Sets the upper limit on available (active and pooled) resources.
     * No new resources will be created as long as the number of active
     * and pooled resources reaches this limit. The default is no upper
     * limit (-1).
     *
     * @param upperLimit Maximum number of available resources
     */
    public void setUpperLimit( int upperLimit )
    {
	_upperLimit = upperLimit;
    }


    /**
     * Returns the upper limit on available resources. Negative value
     * implies no upper limit.
     *
     * @return The upper limit on available resources
     */
    public int getUpperLimit()
    {
	return _upperLimit;
    }


    /**
     * Sets the upper limit on active resources. No resources will
     * be activated when the number of active resources reaches this
     * limit. If the upper limit is higher than the active limit,
     * more resources can be pooled, but not used concurrently.
     * The default is no upper limit (-1).
     *
     * @param activeLimit Maximum number of resources used
     *   concurrently
     */
    public void setActiveLimit( int activeLimit )
    {
	_activeLimit = activeLimit;
    }


    /**
     * Returns the upper limit on active resources. Negative value
     * implies no upper limit.
     *
     * @return The upper limit on active resources
     */
    public int getActiveLimit()
    {
	return _activeLimit;
    }


    /**
     * Sets the desired size of the pool. The pool should not attempt
     * to decrease below the desired size and should remain at
     * off-peak times at this size. The default is zero.
     *
     * @param desiredSize The desired size of the pool
     */
    public void setDesiredSize( int desiredSize )
    {
	if ( desiredSize < 0 )
	    throw new IllegalArgumentException( "Argument 'desiredSize' is negative" );
	_desiredSize = desiredSize;
    }


    /**
     * Returns the desired size of the pool.
     *
     * @return The desired size of the pool
     */
    public int getDesiredSize()
    {
	return _desiredSize;
    }


    /**
     * Sets the timeout waiting to activate a resource or create a new
     * resource if the pool is empty. When the upper or active limit
     * has been reached, attempts to activate or create a new resource
     * will be blocked until one is available, or until the timeout
     * has passed, in which case a {@link TimeoutException} is thrown.
     * Specified in seconds, use zero for immediate timeout, use {@link
     * Integer.MAX_VALUE} to wait forever. The default is zero.
     *
     * @param waitTimeout The timeout waiting to activate a resource
     *   or create a new one, specified in seconds
     */
    public void setWaitTimeout( int waitTimeout )
    {
	if ( waitTimeout < 0 )
	    throw new IllegalArgumentException( "Argument 'waitTimeout' is negative" );
	_waitTimeout = waitTimeout * 1000;
    }


    /**
     * Returns the timeout waiting to activate a resource or create
     * a new one. Specified in seconds.
     *
     * @return The timeout waiting to activate a resource or create
     *   a new one, specified in seconds
     */
    public int getWaitTimeout()
    {
	return ( _waitTimeout / 1000 );
    }
    
    
    /**
     * Sets the wait period between checks that reduce the pool size.
     * The pool size will be checked periodically, and if it exceeds
     * the desired size, it will be reduced by the prune factor.
     * Specified in seconds. The default is one minute.
     *
     * @param checkEvery The wait period between checks to reduce the
     *   pool size, specified in seconds
     * @see #setDesiredSize
     * @see #setPruneFactor
     */
    public void setCheckEvery( int checkEvery )
    {
	if ( checkEvery < 0 )
	    throw new IllegalArgumentException( "Argument 'checkEvery' is negative" );
	_checkEvery = checkEvery * 1000;
    }

    
    /**
     * Returns the wait period between checks to reduce the pool
     * size, specified in seconds.
     *
     * @return The wait period between checks to reduce the pool
     *   size, specified in seconds
     */
    public int getCheckEvery()
    {
	return ( _checkEvery / 1000 );
    }


    /**
     * Sets the prune factor, a value between 0.0 and 1.0, that
     * determines the percentage of pooled resources to release at
     * every check point. Used in combination with the check every
     * period. For example, a check every period of 1 minute and a
     * prune factor of 0.25 will release all pooled resources (less
     * the desired size) after four minutes. The default is 0.1
     * (10%).
     *
     * @param pruneFactor The prune factor, a value between 0.0
     *   and 1.0
     */
    public void setPruneFactor( float pruneFactor )
    {
	if ( pruneFactor < 0 )
	    throw new IllegalArgumentException( "Argument 'pruneFactor' is negative" );
	_pruneFactor = pruneFactor;
    }


    /**
     * Returns the prune factor, a value between 0.0 and 1.0
     *
     * @return The prune factor, a value between 0.0 and 1.0
     */
    public float getPruneFactor()
    {
	return _pruneFactor;
    }
    

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
     * @throws TimeoutException A timeout has occured waiting to
     *   create a new resource
     * @see #setWaitTimeout
     * @see #released
     */
    public void canCreateNew()
	throws TimeoutException
    {
	long timeout;
	long current;
	int  poolSize;
	
	if ( _upperLimit < 0 )
	    return;

	// Synchronization is required so we can be notified
	// by released().
	synchronized ( _pooled ) {
	    if ( ( _pooled.getPooledCount() + _pooled.getActiveCount() ) < _upperLimit )
		return;
	    // If a timeout was specified, wait until the timeout
	    // has been reached, or a resource has been released,
	    // whichever comes first.
	    if ( _waitTimeout > 0 ) {
		current = System.currentTimeMillis();
		timeout = current + _waitTimeout;
		while ( current < timeout ) {
		    try {
			_pooled.wait( timeout - current );
		    } catch ( InterruptedException except ) { }
		    poolSize = _pooled.getPooledCount();
		    if ( ( _pooled.getPooledCount() + _pooled.getActiveCount() ) < _upperLimit )
			return;
		    current = System.currentTimeMillis();
		}
	    }
	    throw new TimeoutException( Messages.message( "tyrex.pool.timeoutCreate" ) );
	}
    }
    

    /**
     * Called by the pool to determine if a pooled resource can be
     * activated. If the active limit has been reach, this call will
     * block until the active countl goes below the active limit, or
     * the timeout has occured. If the timeout has passed, will throw
     * an {@link TimeoutException}.
     *
     * @throws TimeoutException A timeout has occured waiting to
     *   activate a resource
     * @see #setWaitTimeout
     * @see #released
     */
    public void canActivate()
	throws TimeoutException
    {
	long timeout;
	long current;
	
	if ( _activeLimit < 0 )
	    return;

	// Synchronization is required so we can be notified
	// by released().
	synchronized ( _pooled ) {
	    if ( _pooled.getActiveCount() < _activeLimit )
		return;
	    // If a timeout was specified, wait until the timeout
	    // has been reached, or a resource has been released,
	    // whichever comes first.
	    if ( _waitTimeout > 0 ) {
		current = System.currentTimeMillis();
		timeout = current + _waitTimeout;
		while ( current < timeout ) {
		    try {
			_pooled.wait( timeout - current );
		    } catch ( InterruptedException except ) { }
		    if ( _pooled.getActiveCount() < _activeLimit )
			return;
		    current = System.currentTimeMillis();
		}
	    }
	    throw new TimeoutException( Messages.message( "tyrex.pool.timeoutActivate" ) );
	}
    }


    /**
     * Called to notify the pool manager that an active resource has
     * been de-activated or released, notifying waiting threads that
     * they may activate a resource or create a new one.
     *
     * @see #canCreateNew
     * @see #canActivate
     */
    public void released()
    {
	synchronized ( _pooled ) {
	    _pooled.notify();
	}
    }


    public int getActiveCount()
    {
	return _pooled.getActiveCount();
    }


    public int getPooledCount()
    {
	return _pooled.getPooledCount();
    }


    public void releasePooled( int size )
    {
	if ( _pooled != null )
	    _pooled.releasePooled( size );
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
		Thread.sleep( _checkEvery );
		try {
		    // Get the total resource count, deduct the
		    // desired size, and apply the prune factor
		    // to determine how many resources to release.
		    // We add one to the result to compensate for
		    // fractions, otherwise, we might never reach
		    // the desired size.
		    size = _pooled.getPooledCount();
		    if ( size > _desiredSize ) {
			size = (int) ( ( size - _desiredSize ) * _pruneFactor ) + 1;
			_pooled.releasePooled( size );
		    }
		} catch ( Exception except ) { }
	    }
	} catch ( InterruptedException except ) { }
    }


}




