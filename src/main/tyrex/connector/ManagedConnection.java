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
 * $Id: ManagedConnection.java,v 1.3 2000/08/28 19:01:48 mohammed Exp $
 */


package tyrex.connector;


import java.io.PrintWriter;
import javax.transaction.xa.XAResource;


/**
 * Interface for a managed connection. A managed connection is obtained
 * from a {@link ManagedConnectionFactory} by the {@link ConnectionManager}.
 * The connection manager pools the connection, enlists it in transactions,
 * and reports critical errors on connections through this interface.
 * <p>
 * <b>Notes:</b> This interface is modeled after <tt>javax.sql.XADataSource</tt>
 * but supports none-transactional resources as well as synchronization
 * transactional resources. The later interface is experimental and was
 * added to support resources that depend on XA resources (like Castor).
 * Further changes to these API are expected in the future.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/08/28 19:01:48 $
 */
public interface ManagedConnection
{


    /**
     * No transactions supported on this connection. The connection
     * will not be enlisted with any transaction.
     */
    public static final short TRANSACTION_NONE = 0;


    /**
     * Distributed transactions support on this connection through the
     * <tt>XAResource</tt> interface. The transaction manager will
     * enlist this resource through {@link #getXAResource}.
     */
    public static final short TRANSACTION_XA = 1;


    /**
     * Limited form of local transactions supported on this connection
     * through the {@link SynchronizationResource} interface. The
     * transaction manager will enlist this resource through {@link
     * #getSynchronizationResource}.
     */
    public static final short TRANSACTION_SYNCHRONIZATION = 2;


    /**
     * Registers a connection event listener for recieving closure and
     * error events on the connection.
     *
     * @param listener The connection event listener
     */
    public void addConnectionEventListener( ConnectionEventListener listener );


    /**
     * Deregisters a connection event listener.
     *
     * @param listener The connection event listener
     */
    public void removeConnectionEventListener( ConnectionEventListener listener );


    /**
     * Notifies the connection when it returns to the pool. If the
     * managed connection has any application proxies, it may disconnect
     * them at his point.
     *
     * @throws ConnectionException Reports an error that occured when
     *  attempting to pool the connection
     */
    //public void pool()
    //    throws ConnectionException;


    /**
     * Closes the connection. The connection manager calls this method
     * when the connection is removed from the pool and will not be
     * used anymore.
     *
     * @throws ConnectionException Reports an error that occured when
     *  attempting to close the connection
     */
    public void close()
        throws ConnectionException;


    /**
     * Obtains an application connection. This connection is handed to
     * the application through the connector adaptor, but remains subject
     * to the connection manager who can control it through this interface.
     *
     * @param info Any optional information for creating the connection
     * @return A connection
     * @throws ConnectionException An error occured while taking to the
     *  connection, this connection should be discarded
     */
    public Object getConnection( Object info )
        throws ConnectionException;


    /**
     * Returns an <tt>XAResource</tt> object that will be used to
     * manage distributed transactions on this connection. This method
     * is called if the reported transaction type is {@link
     * #TRANSACTION_XA}.
     * <BR>
     * This method always returns the same XAResource any number of
     * times it is called.
     *
     * @return An <tt>XAResource</tt> for managing distributed transactions
     * @throws ConnectionException An error occured while taking to the
     *  connection, this connection should be discarded
     */
    public XAResource getXAResource()
        throws ConnectionException;


    /**
     * Returns an <tt>LocalTransaction</tt> object that will be
     * used to manage local transactions on this connection. This method
     * is called if the reported transaction type is {@link
     * #TRANSACTION_SYNCHRONIZATION}.
     *
     * @return An <tt>LocalTransaction</tt> for managing local
     *  transactions
     * @throws ConnectionException An error occured while taking to the
     *  connection, this connection should be discarded
     */
    public LocalTransaction getLocalTransaction()
        throws ConnectionException;


    /**
     * Plugs an existing application connection into a managed connection.
     * This method is required to allow a connection to be used across
     * method invocations, by associating/dissociating the connection held
     * by the application with an actual managed connection.
     */
    //public void connect( Object connection )
    //    throws ConnectionException;


    /**
     * Returns the log writer for this managed connection.
     *
     * @return The log writer
     */
    public PrintWriter getLogWriter();


    /**
     * Sets the log writer for this managed connection.
     *
     * @param logWriter The log writer
     */
    public void setLogWriter( PrintWriter logWriter );
}



