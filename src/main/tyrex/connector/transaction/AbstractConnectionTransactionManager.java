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
 * Copyright 2000 (C) Intalio Inc. All Rights Reserved.
 *
 */


package tyrex.connector.transaction;

import java.io.PrintWriter;
import tyrex.connector.ConnectionException;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ManagedConnectionFactory;
import tyrex.connector.transaction.ConnectionTransactionManager.ConnectionTransactionListener;

///////////////////////////////////////////////////////////////////////////////
// ConnectionTransactionManager
///////////////////////////////////////////////////////////////////////////////

/**
 * This interface decouples management of connection
 * transactions from the {@link ConnectionManager}.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public abstract class AbstractConnectionTransactionManager
    implements ConnectionTransactionManager 
{
    /**
     * The listeners
     */
    private ConnectionTransactionListener[] listeners;


    /**
     * The log writer. Can be null.
     */
    private PrintWriter logWriter;


    /**
     * Create the AbstractConnectionTransactionManager
     */
    public AbstractConnectionTransactionManager()
    {

    }

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
     * @param info Any optional information for creating the connection
     * @return the connection handle from the managed connection
     * @throws ConnectionException if there is a problem
     */
    public abstract Object enlist(Object connectionHandle,
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
    public abstract boolean delist(ManagedConnection managedConnection,
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
    public abstract boolean canBeShared(ManagedConnection managedConnection,
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
    public abstract void discard(ManagedConnection managedConnection,
                                 ManagedConnectionFactory managedConnectionFactory)
        throws ConnectionException;


    /**
     * Set the log writer for the ConnectionTransactionManager.
     *
     * @param logWriter the new log writer. Can be null.
     */
    public final void setLogWriter(PrintWriter logWriter)
    {
        this.logWriter = logWriter;
    }


    /**
     * Get the log writer used in the ConnectionTransactionManager.
     *
     * @return the log writer used in the ConnectionTransactionManager.
     *      Can be null.
     */
    public final PrintWriter getLogWriter()
    {
        return logWriter;
    }

    
    /**
     * Add the connection transaction listener for the ConnectionTransactionManager.
     *
     * @param listener the connection transaction listener
     */
    public final synchronized void addListener(ConnectionTransactionListener listener)
    {
        if (null == listeners) {
            listeners = new ConnectionTransactionListener[]{listener};    
        }
        else {
            // create the new array
            ConnectionTransactionListener[] newListeners = new ConnectionTransactionListener[listeners.length + 1];
            // copy the existing list
            System.arraycopy(listeners, 0, newListeners, 0, listeners.length);
            // add the new listener
            newListeners[listeners.length] = listener;
            // set to the new array
            listeners = newListeners;
            // reset
            newListeners = null;
        }
    }


    /**
     * Remove the connection transaction listener for the ConnectionTransactionManager.
     *
     * @param listener the connection transaction listener
     */
    public final synchronized void removeListener(ConnectionTransactionListener listener)
    {
        if (null != listeners) {
            if (listeners.length == 1) {
                if (listeners[0] == listener) {
                    listeners = null;    
                }
            }
            else {
                // loop over the array looking for a match
                for (int i = listeners.length; --i >= 0;) {
                    if (listeners[i] == listener) {
                        // create the new array
                        ConnectionTransactionListener[] newListeners = new ConnectionTransactionListener[listeners.length - 1];
                        // set the last item to the index
                        listeners[i] = listeners[listeners.length - 1];
                        // copy the existing list
                        System.arraycopy(listeners, 0, newListeners, 0, listeners.length - 1);
                        // set to the new array
                        listeners = newListeners;
                        // reset
                        newListeners = null;            
                    }
                }
            }
        }
    }


    /**
     * Tell the listeners that the specified managed connection
     * from the specified managed connection factory is no longer
     * in a local transaction.
     *
     * @param managedConnection the managed connection
     * @param managedConnectionFactory the managed connection factory
     *      that produced the specified managed connection
     * @return True if the specified managed connection from the
     * specified managed connection factory is involved in a transaction.
     */
    protected final synchronized void informNotInLocalTransaction(ManagedConnection managedConnection,
                                                                  ManagedConnectionFactory managedConnectionFactory)
    {
        if (null != listeners) {
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].notInLocalTransaction(managedConnection,
                                                   managedConnectionFactory);
            }
        }
    }
}
