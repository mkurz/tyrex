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


package tyrex.connector.transaction;

import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import javax.transaction.TransactionManager;
import tyrex.connector.ConnectionManager;
import tyrex.connector.conf.ConnectionManagerConfiguration;
import tyrex.tm.Tyrex;

///////////////////////////////////////////////////////////////////////////////
// ConnectionManagerFactory
///////////////////////////////////////////////////////////////////////////////

/**
 * This factory returns {@link tyrex.connector.ConnectionTransactionManager}
 * objects.
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
public final class ConnectionTransactionManagerFactory 
{
    /**
     * The key for the {@link TransactionMediator}
     * in the build configuration.
     */
    public static final String transactionMediatorKey = "transactionMediator";


    /**
     * The key for the java.util.TransactionManager in the
     * build configuration.
     */
    public static final String transactionManagerKey = "transactionManager";


    /**
     * The key for log writer of type java.io.PrintWriter
     * in the build configuration
     */
    public static final String logWriterKey = "logWriter"; 


    /**
     * Cannot create instances of ConnectionTransactionManagerFactory
     */
    private ConnectionTransactionManagerFactory()
    {

    }


    /**
     * Return the connection transaction manager created according
     * to the specified values.
     *
     * @param config the configuration used to build
     *      the connection transaction manager. Can be null.
     */
    public static ConnectionTransactionManager build(Map config)
    {
        TransactionMediator transactionMediator = null == config 
                                                    ? null 
                                                    : (TransactionMediator) config.get(transactionMediatorKey);
        if (null == transactionMediator) {
            transactionMediator = new SimpleTransactionMediator();    
        }

        return build(transactionMediator, config);
    }


    /**
     * Return the connection transaction manager created according
     * to the specified values.
     *
     * @param transactionMediator the transaction mediator. Cannot be null.
     * @param config the configuration used to build
     *      the connection transaction manager. Can be null.
     */
    public static ConnectionTransactionManager build(TransactionMediator transactionMediator,
                                                     Map config)
    {
        TransactionManager transactionManager = null == config 
                                                    ? null 
                                                    : (TransactionManager) config.get(transactionManagerKey);
        if (null == transactionManager) {
            transactionManager = (TransactionManager)AccessController.doPrivileged(new PrivilegedAction()
                                        {
                                            public Object run() 
                                            {
                                                return Tyrex.getTransactionManager();
                                            }
                                        });
        }

        return build(transactionMediator, transactionManager, config);
    }


    /**
     * Return the connection transaction manager created according
     * to the specified values.
     *
     * @param transactionMediator the transaction mediator. Cannot be null.
     * @param transactionManager the transaction manager. Cannot be null.
     * @param config the configuration used to build
     *      the connection transaction manager. Can be null.
     */
    public static ConnectionTransactionManager build(TransactionMediator transactionMediator,
                                                     TransactionManager transactionManager,
                                                     Map config)
    {
        if (null == transactionMediator) {
            throw new IllegalArgumentException("The argument 'transactionMediator' is null.");
        }
        if (null == transactionManager) {
            throw new IllegalArgumentException("The argument 'transactionManager' is null.");
        }
        return new ConnectionTransactionManagerImpl(transactionMediator,
                                                    transactionManager,
                                                    null == config ? null : (PrintWriter)config.get(logWriterKey));
    }
}
