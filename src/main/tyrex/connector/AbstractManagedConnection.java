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


package tyrex.connector;

import javax.transaction.xa.XAResource;
import tyrex.connector.transaction.XALocalTransaction;
import tyrex.connector.transaction.XALocalTransaction.XALocalTransactionListener;

///////////////////////////////////////////////////////////////////////////////
// AbstractManagedConnection
///////////////////////////////////////////////////////////////////////////////

/**
 * This class provides base functionality for implementations of
 * {@link ManagedConnection}
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public abstract class AbstractManagedConnection 
    implements ManagedConnection
{
    /**
     * Mode that tells AbstractManagedConnection to tell
     * connection event listeners that connection error
     * occurred
     */
    private static final int connectionErrorOccurred = 0;

    
    /**
     * Mode that tells AbstractManagedConnection to tell
     * connection event listeners that connection closed
     */
    private static final int connectionClosed = 1;
    
    
    /**
     * Mode that tells AbstractManagedConnection to tell
     * connection event listeners that local transaction
     * begun
     */
    private static final int localTransactionBegun = 2;

    
    /**
     * Mode that tells AbstractManagedConnection to tell
     * connection event listeners that local transaction
     * committed
     */
    private static final int localTransactionCommitted = 3;

    
    /**
     * Mode that tells AbstractManagedConnection to tell
     * connection event listeners that connection error
     * occurred
     */
    private static final int localTransactionRolledback = 4;

    /**
     * The array of connection event listeners
     */
    private transient ConnectionEventListener[] listeners = null;


    /**
     * The XA Resource 
     */
    private transient XAResource xaResource = null;


    /**
     * The listener for local transaction changes
     */
    private transient XALocalTransactionListener xaLocalTransactionListener = new LocalTransactionListener();
                                                                

    /**
     * True if the managed connection has been closed.
     */
    private boolean isClosed = false;


    /**
     * Default constructor
     */
    protected AbstractManagedConnection()
    {
        // do nothing
    }


    /**
     * Return true if the manage conenction has been closed.
     *
     * @return true if the manage conenction has been closed.
     */
    protected final boolean isClosed()
    {
        return isClosed;
    }


    /**
     * Check that the managed connection is not closed.
     *
     * @throws ConnectionException if the managed connection is
     *      already closed.
     */
    protected final void checkClosed()
        throws ConnectionException
    {
        if (isClosed()) {
            throw new ConnectionException("The managed conenction <" +
                                          toString() +
                                          "> is closed.");
        }
    }


    /**
     * Closes the connection. The connection manager calls this method
     * when the connection is removed from the pool and will not be
     * used anymore.
     *
     * @throws ConnectionException Reports an error that occured when
     *  attempting to close the connection
     */
    public synchronized final void close()
        throws ConnectionException
    {
        if (!isClosed()) {
            // set the close flag
            isClosed = true;
            // free the xa resource
            xaResource = null;
            performClose();            
        }
    }


    /**
     * Actual method that closes the connection. The connection 
     * manager calls this method
     * when the connection is removed from the pool and will not be
     * used anymore. This method is called at most once and should 
     * not be called directly.
     *
     * @throws ConnectionException Reports an error that occured when
     *  attempting to close the connection
     */
    protected abstract void performClose()
        throws ConnectionException;


    /**
     * Returns an <tt>XAResource</tt> object that will be used to
     * manage distributed transactions on this connection. This method
     * is called if the reported transaction type is {@link
     * #TRANSACTION_XA}.
     *
     * @return An <tt>XAResource</tt> for managing distributed transactions
     * @throws ConnectionException An error occured while taking to the
     *  connection, this connection should be discarded
     */
    public synchronized final XAResource getXAResource()
        throws ConnectionException
    {
        checkClosed();
        // The double-check idiom is not used because it may not work because
        // out-of-order execution of instructions ie the setting of the reference
        // can occur before the referenced object has been fully constructed. This
        // happens especially in a multi-processor environment

        if (null == xaResource) {
            xaResource = createXAResource();    
        }

        return xaResource;
    }

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
        throws ConnectionException
    {
        return new XALocalTransaction(getXAResource(), xaLocalTransactionListener);
    }


    /**
     * Create the XA resource to be used to manage distributed transactions 
     * on this connection. This method is only called once.
     * <BR>
     * The default implementation returns null.
     *
     * @return the XA resource to be used to manage distributed transactions
     * on this connection.
     * @throws ConnectionException if there is a problem creating the xa resource.
     */
    protected XAResource createXAResource()
        throws ConnectionException
    {
        return null;
    }

    // synchronized to prevent the listener array from changing while
    // add, removing or iterating
    public synchronized final void addConnectionEventListener( ConnectionEventListener listener )
    {
        if ( listener == null )
            throw new IllegalArgumentException( "Argument 'listener' is null" );
        if ( listeners == null ) {
            listeners = new ConnectionEventListener[ 1 ];
            listeners[ 0 ] = listener;
        } else {
            ConnectionEventListener[] newListeners;

            // Make sure same listener is not registered twice
            for ( int i = 0 ; i < listeners.length ; ++i )
                if ( listeners[ i ] == listener )
                    return;
            newListeners = new ConnectionEventListener[ listeners.length + 1 ];
            for ( int i = 0 ; i < listeners.length ; ++i )
                newListeners[ i ] = listeners[ i ];
            newListeners[ listeners.length ] = listener;
            listeners = newListeners;
        }
    }


    // synchronized to prevent the listener array from changing while
    // add, removing or iterating
    public synchronized final void removeConnectionEventListener( ConnectionEventListener listener )
    {
        if ( listener == null )
            throw new IllegalArgumentException( "Argument 'listener' is null" );
        // Do nothing if listener not registered
        if ( listeners == null )
            return;
        if ( listeners.length == 1 ) {
            if ( listeners[ 0 ] == listener )
                listeners = null;
            return;
        }
        for ( int i = 0 ; i < listeners.length ; ++i )
            if ( listeners[ i ] == listener ) {
                ConnectionEventListener[] newListeners;

                listeners[ i ] = listeners[ listeners.length - 1 ];
                newListeners = new ConnectionEventListener[ listeners.length - 1 ];
                for ( int j = 0 ; j < listeners.length - 1 ; ++j )
                    newListeners[ j ] = listeners[ j ];
                listeners = newListeners;
                return;
            }
    }

    /**
     *  Fire the connection event to any connection
     * event listeners informing them that the connection
     * has been closed.
     */
    protected final void fireConnectionClosedEvent()
    {
        fireConnectionEvent(connectionClosed, null);
    }

    /**
     * Fire a connection event to any connection event listeners
     * informing them that an error has occurred in the connection.
     *
     * @param e the exception that occurred
     */
    protected final void fireConnectionErrorOccurredEvent(Exception e)
    {
        fireConnectionEvent(connectionErrorOccurred, e);
    }

    /**
     *  Fire the connection event to any connection
     * event listeners informing them that the local
     * transaction has begun.
     */
    protected final void fireLocalTransactionBegunEvent()
    {
        fireConnectionEvent(localTransactionBegun, null);
    }

    /**
     *  Fire the connection event to any connection
     * event listeners informing them that the local
     * transaction has committed.
     */
    protected final void fireLocalTransactionCommittedEvent()
    {
        fireConnectionEvent(localTransactionCommitted, null);
    }

    /**
     *  Fire the connection event to any connection
     * event listeners informing them that the local
     * transaction has rolled back.
     */
    protected final void fireLocalTransactionRolledBackEvent()
    {
        fireConnectionEvent(localTransactionRolledback, null);
    }

    /**
     * Fire a connection event to any connection event listeners
     * informing them that an error has occurred in the connection.
     */
    private void fireConnectionEvent(final int mode, Exception e)
    {
        // the listeners are copied because a listener
        // can be removed upon receipt of the event
        // which may cause unpredictable behaviour during
        // iteration such as a null pointer exception because
        // the only listener has been removed

        // the copy of the listeners
        ConnectionEventListener[] copy = null;

        synchronized (this)
        {
            if ((null != listeners) && (listeners.length > 0)) {
                copy = (ConnectionEventListener[])listeners.clone();    
            }
        }
        if (null != copy) {
            // make the event
            ConnectionEvent event = new ConnectionEvent(this, e);

            for ( int i = 0 ; i < copy.length ; ++i ) {
                switch (mode) {
                    case connectionErrorOccurred:
                        copy[i].connectionErrorOccurred(event);
                        break;
                    case connectionClosed:
                        copy[i].connectionClosed(event);
                        break;
                    case localTransactionBegun:
                        copy[i].localTransactionBegun(event);
                        break;
                    case localTransactionCommitted:
                        copy[i].localTransactionCommitted(event);
                        break;
                    case localTransactionRolledback:
                        copy[i].localTransactionRolledback(event);
                        break;
                }
            }
        }
    }

    /**
     * The class that listens for changes in the 
     * {@link tyrex.connector.transaction.XALocalTransaction}
     * and calls the appropriate connection event listener method
     */
    private class LocalTransactionListener
        implements XALocalTransactionListener
    {
        /**
         * Begin called
         */
        public void beginCalled()
        {
            fireLocalTransactionBegunEvent();
        }

        /**
         * This method is called when commit is called on the
         * XALocalTransaction
         */
        public void commitCalled()
        {
            fireLocalTransactionCommittedEvent();
        }

        /**
         * This method is called when rollback is called on
         * the XALocalTransaction
         */
        public void rollbackCalled()
        {
            fireLocalTransactionRolledBackEvent();
        }
    }
}
