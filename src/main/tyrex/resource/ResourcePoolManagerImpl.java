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
 * $Id: ResourcePoolManagerImpl.java,v 1.1 2000/01/17 22:19:14 arkin Exp $
 */


package tyrex.resource;


import tyrex.util.Messages;


/**
 * Manages resource utilization for a resource pool or resource
 * factory. The resource pool or resource factory is associated with
 * this manager through {@link #manage}. The resource limits are set
 * using the {@link ResourceLimits} parent class.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/17 22:19:14 $
 * @see PooledResources
 * @see ResourcePoolManager
 * @see ResourceLimits
 */
public class ResourcePoolManagerImpl
    extends ResourceLimits
    implements ResourcePoolManager, Runnable
{


    /**
     * The actual pool implementation managed by this pool manager.
     */
    private transient ResourcePool  _pool;


    /**
     * A background thread used for releasing pooled resources.
     */
    private transient Thread        _background;


    /**
     * Constructs a new pool manager for use with the specified pool.
     * If <tt>background</tt> is true, creates a background thread to
     * release pooled resources above the desired size.
     *
     * @param pool The resource pool to manage
     * @param background True if resource releasing background thread
     *   is required
     */
    public ResourcePoolManagerImpl( ResourcePool pool, boolean background )
    {
	manage( pool, background );
    }


    public ResourcePoolManagerImpl( ResourceLimits limits )
    {
	setUpperLimit( limits.getUpperLimit() );
	setWaitTimeout( limits.getWaitTimeout() );
	setDesiredSize( limits.getDesiredSize() );
	setPruneFactor( limits.getPruneFactor() );
	setCheckEvery( limits.getCheckEvery() );
    }


    public ResourcePoolManagerImpl()
    {
    }


    public synchronized void manage( ResourcePool pool, boolean background )
    {
	if ( pool == null )
	    throw new IllegalArgumentException( Messages.format( "tyrex.misc.nullArgument", "pool" ) );
	if ( _pool != null )
	    throw new IllegalStateException( Messages.format( "tyrex.resource.alreadyManaging",
							      _pool.getResourcePoolName() ) );
	_pool = pool;
	if ( _background == null && background ) {
	    _background = new Thread( this );
	    _background.setName( Messages.format( "tyrex.resource.daemonName", null ) );
	    _background.setPriority( Thread.MIN_PRIORITY );
	    _background.setDaemon( true );
	    _background.start();
	}
    }


    public synchronized void unmanage( ResourcePool pool )
    {
	if ( pool != _pool )
	    throw new IllegalArgumentException();
	if ( _background != null ) {
	    _background.interrupt();
	    try {
		_background.join();
	    } catch ( InterruptedException except ) { }
	    _background = null;
	    _pool = null;
	}
    }


    public void canCreateNew()
	throws ResourceTimeoutException
    {
	long timeout;
	long current;
	int  poolSize;
	
	if ( _upperLimit < 0 )
	    return;

	// Synchronization is required so we can be notified
	// by released().
	synchronized ( _pool ) {
	    if ( ( _pool.getResourceCount() ) < _upperLimit )
		return;
	    // If a timeout was specified, wait until the timeout
	    // has been reached, or a resource has been released,
	    // whichever comes first.
	    if ( _waitTimeout > 0 ) {
		current = System.currentTimeMillis();
		timeout = current + _waitTimeout;
		while ( current < timeout ) {
		    try {
			_pool.wait( timeout - current );
		    } catch ( InterruptedException except ) { }
		    if ( ( _pool.getResourceCount() ) < _upperLimit )
			return;
		    current = System.currentTimeMillis();
		}
	    }
	    throw new ResourceTimeoutException( Messages.format( "tyrex.resource.timeout",
                _pool.getResourcePoolName(), new Integer( _upperLimit ) ) );
	}
    }
    

    public void released()
    {
	synchronized ( _pool ) {
	    _pool.notify();
	}
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
		    size = _pool.getPooledResourceCount();
		    if ( size > _desiredSize ) {
			size = (int) ( ( size - _desiredSize ) * _pruneFactor ) + 1;
			_pool.releasePooled( size );
		    }
		} catch ( Exception except ) { }
	    }
	} catch ( InterruptedException except ) { }
    }


}

