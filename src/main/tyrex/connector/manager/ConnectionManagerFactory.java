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


package tyrex.connector.manager;

import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import tyrex.connector.ConnectionManager;
import tyrex.connector.conf.ConnectionManagerConfiguration;
import tyrex.connector.transaction.ConnectionTransactionManager;
import tyrex.connector.transaction.ConnectionTransactionManagerFactory;

///////////////////////////////////////////////////////////////////////////////
// ConnectionManagerFactory
///////////////////////////////////////////////////////////////////////////////

/**
 * This factory returns {@link tyrex.connector.ConnectionManager}
 * objects.
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
public final class ConnectionManagerFactory 
{

    /**
     * The key for log writer of type java.io.PrintWriter
     * in the build configuration
     */
    public static final String logWriterKey = "logWriter"; 

    /**
     * The key for connection manager configuration,
     * of type {@link tyrex.connector.conf.ConnectionManagerConfiguration}, in the build configuration
     */
    public static final String connectionManagerConfigKey = "connectionManagerConfig";


    /**
     * The key for the config, of type java.util.Map, 
     * used in building the connection transaction manager.
     */
    public static final String connectionTransactionManagerConfigKey = "connectionTransactionManagerConfig";

    /**
     * Cannot create instances of ConnectionManagerFactory
     */
    private ConnectionManagerFactory()
    {

    }

    /**
     * Return the connection manager created according
     * to the values in the configuration.
     *
     * @param config the configuration used to build
     *      the connection manager. Can be null.
     */
    public static ConnectionManager build(Map config)
    {
        // get the config for building the connection transaction
        // manager
        Map connectionTransactionManagerConfig = null == config 
                                                    ? null
                                                    : (Map)config.get(connectionTransactionManagerConfigKey);
        
        // if there is not a separate connection transaction
        // manager config use the exist config
        if (null == connectionTransactionManagerConfig) {
            connectionTransactionManagerConfig = config;    
        }
        
        return build(ConnectionTransactionManagerFactory.build(connectionTransactionManagerConfig),
                     config);
    }


    /**
     * Return the connection manager created according
     * to the values in the configuration.
     *
     * @param config the configuration used to build
     *      the connection manager. Can be null.
     */
    public static ConnectionManager build(ConnectionTransactionManager connectionTransactionManager,
                                           Map config)
    {
        return null == config 
            ? new ConnectionManagerImpl(connectionTransactionManager)
            : new ConnectionManagerImpl((ConnectionManagerConfiguration)config.get(connectionManagerConfigKey),
                                        connectionTransactionManager,
                                        (PrintWriter)config.get(logWriterKey));
    }
}
