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
 * $Id: PoolManaged.java,v 1.2 2000/04/11 20:26:09 arkin Exp $
 */


package tyrex.connector.manager;


/**
 * Interface implemented by a resource pool that is managed by a
 * {@link PoolManager}. The pool must implement the {@link
 * #getPooledCount} and {@link #getActiveCount} methods and
 * optionally the {@link #releasePooled} method.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/04/11 20:26:09 $
 * @see PoolManager
 */
public interface PoolManaged
{


    /**
     * Called by the resource pool manager to obtain the number of
     * resources pooled and activated and determine whether a new
     * resource can be activated or created.
     *
     * @return The number of activated and pooled resources
     */ 
    public int getResourceCount();


    /**
     * Called by the resource pool manager to obtain the number of
     * resources pooled and determine whether pooled resources
     * should be released.
     *
     * @return The number of pooled resources
     */
    public int getPooledResourceCount();


    /**
     * Called by the resource pool manager when it determines that a
     * certain number of resources should be released from the pool,
     * based on the reply from a previous call to {@link
     * #getResourcePoolName}. The pool may remove any number of
     * resources it sees fit.
     *
     * @param count The number of pooled resources that should be
     *   released
     */
    public void releasePooled( int count );


}

