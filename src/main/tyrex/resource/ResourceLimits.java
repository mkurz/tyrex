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
 * $Id: ResourceLimits.java,v 1.1 2000/01/17 22:19:14 arkin Exp $
 */


package tyrex.resource;


import java.io.Serializable;
import java.rmi.Remote;
import java.util.Hashtable;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.NamingException;
import tyrex.util.Messages;


/**
 * Specifies limits on resource utilization. Typically tied to a
 * resource factory or a resource pool. This class specifies the
 * limits but is not in itself a resource factory or resource pool.
 * <p>
 * The resource factory or pool will acquire or create new resources
 * for as long as the upper limit has not been reached. Once the upper
 * limit has been reached, attempts to acquire or create new resources
 * will be blocked until a resource has been released or the timeout
 * has elapsed.
 * <p>
 * <p>
 * In addition, a desired size specify the number of resources
 * that should be available from the pool at any given time, to
 * save creation of new resources in off-peak periods. While
 * resources are not heavily used, they are gradually released until
 * the desired size is reached, over a period of time specified
 * linearily using the prune factory and the check every duration.
 * <p>
 * The following properties are part of the resource limits, both the
 * JNDI binding name XML attribute names are specified:
 * <PRE>
 * JNDI/Bean    XML           See
 * -----------  ------------  ------------
 * upperLimit   upper-limit   {@link #setUpperLimit}
 * waitTimeout  wait-timeout  {@link #setWaitTimeout}
 * desiredSize  desired-size  {@link #setDesiredSize}
 * pruneFactor  prune-factor  {@link #setPruneFactor}
 * checkEvery   check-every   {@link #setCheckEvery}
 * </pre>
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/17 22:19:14 $
 */
public class ResourceLimits
    implements Serializable
{


    static class Names
    {
	public static final String UpperLimit = "upperLimit";
	public static final String WaitTimeout = "waitTimeout";
	public static final String DesiredSize = "desiredSize";
	public static final String PruneFactor = "pruneFactor";
	public static final String CheckEvery = "checkEvery";
    }


    /**
     * Defines the upper limit on available resources. The default is
     * no upper limit.
     */
    protected int         _upperLimit = -1;


    /**
     * Defines the desired number of available resources at all time.
     * The pool should attempt not to fall below this size.
     * The default is zero.
     */
    protected int         _desiredSize = 0;


    /**
     * Defines the time out waiting to acquire a pooled resource or
     * create a new resource. This value is specified in milliseconds.
     * The default is ten seconds.
     */
    protected int         _waitTimeout = 10 * 1000; // Ten seconds


    /**
     * The prune factor ranges from 0.0 to 1.0 and defines the
     * percentage of resources that should be removed from the pool
     * in each iteration. The default is 10%.
     */
    protected float       _pruneFactor = 0.1F; // 10%


    /**
     * Interval between checks for and request to release pooled
     * resources. This value is specified in milliseconds.
     * The default is ten seconds.
     */
    protected int         _checkEvery = 10 * 1000;


    /**
     * Sets the upper limit on available resources. No new resources
     * will be created as long as the number of resources reaches or
     * exceeds this limit. The default is no upper limit (-1).
     *
     * @param upperLimit Maximum number of available resources
     */
    public void setUpperLimit( int upperLimit )
    {
	_upperLimit = upperLimit;
    }


    /**
     * Returns the upper limit on resources. Negative value implies no
     * upper limit.
     *
     * @return The upper limit on available resources
     */
    public int getUpperLimit()
    {
	return _upperLimit;
    }


    /**
     * Sets the desired size of the pool. The pool should not attempt
     * to decrease below the desired size.The default is zero.
     *
     * @param desiredSize The desired size of the pool
     */
    public void setDesiredSize( int desiredSize )
    {
	if ( desiredSize < 0 )
	    throw new IllegalArgumentException( Messages.format( "tyrex.resource.argNegative", Names.DesiredSize ) );
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
     * Sets the timeout waiting to acquire a resource from the pool or
     * create a new resource. When the upper limit has been reached,
     * attempts to acquire or create a new resource will be blocked
     * until one is available, or until the timeout has passed, in which
     * case a {@link TimeoutException} is thrown. Specified in seconds,
     * use zero for immediate timeout, use {@link Integer.MAX_VALUE} to
     * wait forever. The default is zero.
     *
     * @param waitTimeout The timeout waiting to acquire a resource
     *   or create a new one, specified in seconds
     */
    public void setWaitTimeout( int waitTimeout )
    {
	if ( waitTimeout < 0 )
	    throw new IllegalArgumentException( Messages.format( "tyrex.resource.argNegative", Names.WaitTimeout ) );
	_waitTimeout = waitTimeout * 1000;
    }


    /**
     * Returns the timeout waiting to acquire a resource or create
     * a new one. Specified in seconds.
     *
     * @return The timeout waiting to acquire a resource or create
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
	    throw new IllegalArgumentException( Messages.format( "tyrex.resource.argNegative", Names.CheckEvery ) );
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
	    throw new IllegalArgumentException( Messages.format( "tyrex.resource.argNegative", Names.PruneFactor ) );
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
    

    public Reference getReference()
    {
	Reference ref;

	// We use same object as factory.
	ref = new Reference( getClass().getName(), getClass().getName(), null );
	ref.add( new StringRefAddr( Names.UpperLimit, Integer.toString( _upperLimit ) ) );
	ref.add( new StringRefAddr( Names.WaitTimeout, Integer.toString( _waitTimeout ) ) );
	ref.add( new StringRefAddr( Names.DesiredSize, Integer.toString( _desiredSize ) ) );
	ref.add( new StringRefAddr( Names.PruneFactor, Float.toString( _pruneFactor ) ) );
	ref.add( new StringRefAddr( Names.CheckEvery, Integer.toString( _checkEvery ) ) );
 	return ref;
    }


    public Object getObjectInstance( Object refObj, Name name, Context nameCtx, Hashtable env )
        throws NamingException
    {
	Reference ref;

	// Can only reconstruct from a reference.
	if ( refObj instanceof Reference ) {
	    ref = (Reference) refObj;
	    // Make sure reference is of datasource class.
	    if ( ref.getClassName().equals( getClass().getName() ) ) {
		ResourceLimits rl;

		try {
		    rl = (ResourceLimits) ref.getClass().newInstance();
		} catch ( Exception except ) {
		    throw new NamingException( except.toString() );
		}
		rl._upperLimit = Integer.parseInt( (String) ref.get( Names.UpperLimit ).getContent() );
		rl._waitTimeout = Integer.parseInt( (String) ref.get( Names.WaitTimeout ).getContent() );
		rl._desiredSize = Integer.parseInt( (String) ref.get( Names.DesiredSize ).getContent() );
		rl._pruneFactor = Float.parseFloat( (String) ref.get( Names.PruneFactor ).getContent() );
		rl._checkEvery = Integer.parseInt( (String) ref.get( Names.CheckEvery ).getContent() );
		return rl;
	    } else
		throw new NamingException( Messages.format( "tyrex.resource.badReference",
							    name.toString(), getClass().getName() ) );
	} else if ( refObj instanceof Remote )
	    return refObj;
	else
	    return null;
    }


}
