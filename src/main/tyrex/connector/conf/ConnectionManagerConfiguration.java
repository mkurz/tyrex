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
 */


package tyrex.connector.conf;

import tyrex.connector.ManagedConnectionFactory;
//import tyrex.connector.manager.ManagedConnectionPool;
//import tyrex.connector.manager.ManagedConnectionPoolImpl;
import tyrex.resource.ResourceLimits;
import tyrex.resource.ResourcePoolManager;
import tyrex.resource.ResourcePoolManagerImpl;

///////////////////////////////////////////////////////////////////////////////
// ConnectionManagerConfiguratorImpl
///////////////////////////////////////////////////////////////////////////////

/**
 * This class defines methods for configuring {@link SimpleConnectorManagerImpl}
 * 
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
public class ConnectionManagerConfiguration
{
    /**
     * Create a new ResourcePoolManagerImpl object for managing the pool of managed
     * connections created by the specified managed connection factory.
     * <BR>
     * Returns a {@link tyrex.resource.ResourceManagerImpl} object.
     * 
     * @param managedConnectionFactory the managed connection factory
     * @return the ResourcePoolManager for managing the pool of managed
     * connections created by the specified managed connection factory.
     */
    public ResourcePoolManager createResourcePoolManager(ManagedConnectionFactory managedConnectionFactory)
    {
        return new ResourcePoolManagerImpl();
    }


    /**
     * Create the {@link tyrex.resource.ResourceLimit resource limits} to be used
     * with the ResourcePoolManager. Return null if the default resource limits
     * are to be used.
     * <BR>
     * The default implementation returns null.
     *
     * @return the {@link tyrex.resource.ResourceLimit resource limits} to be used
     * with the ResourcePoolManager.
     */
    public ResourceLimits createResourceLimits()
    {
        return null;
    }


    /**
     * Create the {@link ManagedConnectionPool} for the connections produced
     * by the specified managed connection factory.
     * <BR>
     * Returns a {@link ManagedConnectionPoolImpl} object.
     *
     * @param managedConnectionFactory the managed connection factory
     * @return the {@link ManagedConnectionPool} for the connections produced
     * by the specified managed connection factory.
     */
    /*public ManagedConnectionPool createManagedConnectionPool(ManagedConnectionFactory managedConnectionFactory)
    {
        return new ManagedConnectionPoolImpl();
    }
    */

    /**
     * Return true if the connection manager is responsible for pruning any extra
     * inactive managed connections produced by the specified managed
     * connection factory.
     * <BR>
     * The default implementation is to return true -  the connection manager 
     * is resposible for any pruning.
     * 
     * @param managedConnectionFactory the managed connection factory
     *      that created the managed connections in the pool.
     * @return true
     */
    public boolean getConnectionManagerPruneResponsibilty(ManagedConnectionFactory managedConnectionFactory)
    {
        return true;
    }

    /**
     * Return true if the managed connections produced by the specified
     * managed connection factory can be shared.
     * <BR>
     * Return false as the default answer.
     * 
     * @param managedConnectionFactory the managed connection factory
     * @return false
     */
    public boolean canShareConnections(ManagedConnectionFactory managedConnectionFactory)
    {
        return false;
    }
}
