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
 * Copyright 1999-2001 (C) Intalio Inc. All Rights Reserved.
 *
 */


package tyrex.resource;


import javax.transaction.xa.XAResource;


/**
 * Represents an installed resource.
 * <p>
 * An installed resource has a client factory that is made available to
 * the application, typically through JNDI, allowing it to create new
 * connections. The client factory type depends on the type of resource
 * in use.
 * <p>
 * An installed resource has a connection pool that manages utilization
 * of the resource. The connection pool metrics can be obtained from
 * {@link #getPoolMetrics}.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.7 $
 */
public interface Resource
{


    /**
     * Returns the pool metrics. The pool metrics object can be used
     * to collect statistical information about the connection pool.
     *
     * @return The pool metrics
     */
    public abstract PoolMetrics getPoolMetrics();


    /**
     * Returns the client connection factory. The client connection
     * factory is enlisted in the JNDI environment naming context for
     * access by the application.
     *
     * @return The client connection factory
     */
    public abstract Object getClientFactory();


    /**
     * Returns the client connection factory class. This the class or
     * interface that a client connection factory would implement.
     *
     * @return The client connection factory class
     */
    public abstract Class getClientFactoryClass();


    /**
     * Returns the XA resource interface. The XA resource is used to
     * manage transaction enlistment and recovery of the resource.
     * This method returns null if the resource does not support
     * XA transactions.
     *
     * @return The XA resource interface
     */
    public abstract XAResource getXAResource();


    /**
     * Returns the limits placed on the connection pool. This object
     * can be used to investigate the limits of the connection pool
     * and to change them at run time.
     *
     * @return The limits placed on the connection pool
     */
    public abstract PoolLimits getPoolLimits();


    /**
     * Called to destory the resource once it is no longer in use.
     * After successful return from this method, all open connections
     * are invalidated and no new connections can be obtained from
     * the pool.
     * <p>
     * The application server must render the connection factory
     * inaccessible to the application before calling this method.
     */
    public abstract void destroy();

    
}
