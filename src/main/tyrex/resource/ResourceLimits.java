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
 * Copyright 2000 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 */

package tyrex.resource;

import java.io.Serializable;

///////////////////////////////////////////////////////////////////////////////
// ResourceLimits
///////////////////////////////////////////////////////////////////////////////

/**
 * This class defines limits for managing resources.
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
public final class ResourceLimits implements Serializable
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
     * Create the ResourceLimits with default values.
     */
    public ResourceLimits()
    {
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
        return _checkEvery;
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
	    if (( pruneFactor < 0 ) || ( pruneFactor > 1.0))
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

}
