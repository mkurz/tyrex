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
import tyrex.connector.ConnectionException;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ManagedConnectionFactory;

///////////////////////////////////////////////////////////////////////////////
// ConnectionTransactionManager
///////////////////////////////////////////////////////////////////////////////

/**
 * This interface decouples management of connection
 * transactions from the {@link ConnectionManager}.
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
public interface ConnectionTransactionManager 
{
    /**
     * Enlist the specified managed connection from
     * the specified managed connection factory
     * in the transaction framework of the application
     * server.
     *
     * <p>
     * The ConnectionTransactionManager may return a
     * connection handle that will allow the managed connection
     * to be enlisted automatically in transactions whenever the
     * handle is used.
     *
     * @param connectionHandle the connection handle from the
     *      managed connection
     * @param managedConnection the managed connection
     * @param managedConnectionFactory the managed connection factory
     *      that produced the specified managed connection
     * @return the connection handle from the managed connection
     * @throws ConnectionException if there is a problem
     */
    Object enlist(Object connectionHandle,
                  ManagedConnection managedConnection,
                  ManagedConnectionFactory managedConnectionFactory)
        throws ConnectionException;


    /**
     * Delist the specified managed connection because the
     * connection handle to the managed connection has been closed.
     * <P>
     * In the case of a managed connection tbat is taking part
     * in a local transaction it can't reused until the local
     * transaction has either been commited or rolled back.
     * <P)
     * In the case of a managed connection that is taking part in
     * an XA transaction (IPC or 2PC) the XA resource is ended
     * successfully and the managed connection can be reused.
     *
     * @param managedConnection the managed connection
     * @param managedConnectionFactory the managed connection factory
     *      that produced the specified managed connection
     * @return True if the specified managed connection from the
     * specified managed connection factory can be reused.
     */
    boolean delist(ManagedConnection managedConnection,
                   ManagedConnectionFactory managedConnectionFactory)
        throws ConnectionException;


    /**
     * Return true if the specified managed connection from the
     * specified managed connection factory can be shared.
     *
     * @param managedConnection the managed connection
     * @param managedConnectionFactory the managed connection factory
     *      that produced the specified managed connection
     * @return True if the specified managed connection from the
     * specified managed connection factory can be shared.
     * @throw ConnectionException if there is a problem determining
     *      whether a connection can be shared.
     */
    boolean canBeShared(ManagedConnection managedConnection,
                        ManagedConnectionFactory managedConnectionFactory)
        throws ConnectionException;
    


    /**
     * Discard the specified managed connection because an error
     * has occurred in it.
     * <P>
     * In the case of a managed connection tbat is taking part
     * in a local transaction the local transaction should be rolled back.
     * <P)
     * In the case of a managed connection that is taking part in
     * an XA transaction (IPC or 2PC) the XA resource is ended
     * unsuccessfully.
     *
     * @param managedConnection the managed connection
     * @param managedConnectionFactory the managed connection factory
     *      that produced the specified managed connection
     * @throws ConnectionException if there is a problem disacrding the
     *      managed connection
     */
    void discard(ManagedConnection managedConnection,
                 ManagedConnectionFactory managedConnectionFactory)
        throws ConnectionException;


    /**
     * Set the log writer for the ConnectionTransactionManager.
     *
     * @param logWriter the new log writer. Can be null.
     */
    void setLogWriter(PrintWriter logWriter);


    /**
     * Get the log writer used in the ConnectionTransactionManager.
     *
     * @return the log writer used in the ConnectionTransactionManager.
     *      Can be null.
     */
    PrintWriter getLogWriter();


    /**
     * Add the connection transaction listener for the ConnectionTransactionManager.
     *
     * @param listener the connection transaction listener
     */
    void addListener(ConnectionTransactionListener listener);


    /**
     * Remove the connection transaction listener for the ConnectionTransactionManager.
     *
     * @param listener the connection transaction listener
     */
    void removeListener(ConnectionTransactionListener listener);


    /**
     * This interface informs when a managed connection
     * is no longer involved in a local transaction.
     */
    public interface ConnectionTransactionListener
    {
        /**
         * This method is called when the managed connection from the
         * specified managed connection factory is no longer involved 
         * in a local transaction.
         *
         * @param managedConnection the managed connection
         * @param managedConnectionFactory the managed connection factory
         *      that produced the specified managed connection
         */
        void notInLocalTransaction(ManagedConnection managedConnection,
                                   ManagedConnectionFactory managedConnectionFactory);
    }
}
