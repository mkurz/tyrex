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
 *    permission of Intalio Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Technologies. Exolab is a registered
 *    trademark of Intalio Technologies.
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
 * Copyright 2000 (C) Intalio Technologies Inc. All Rights Reserved.
 *
 */


package tyrex.jdbc;

import java.lang.ref.WeakReference;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Map;

/////////////////////////////////////////////////////////////////////
// AbstractTyrexConnectionImpl
/////////////////////////////////////////////////////////////////////

/**
 * This class defines base methods for implementing java.sql.Connection
 * so that an underlying java.sql.Connection may be pooled.
 * <P>
 * Subclasses are to implement {@link #isClosed}, {@link #close}, 
 * {@ #getUnderlyingConnection}. Implementation of {@link #close}
 * must call {@link #notifyConnectionClosed} in order to inform
 * listeners that the connection has been closed.
 * <P>
 * This class also defines base methods for managing {@link TyrexConnectionListener}
 * objects. The {@link TyrexConnectionListener listeners} are stored using
 * java.lang.ref.WeakReference object so that the 
 * {@link TyrexConnectionListener listeners} can be garbage collected.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public abstract class AbstractTyrexConnectionImpl 
    implements TyrexConnection
{
    /**
     * The default resize amount
     */
    public static final int DEFAULT_RESIZE_AMOUNT = 1;

    /**
     * The amount to resize the listener array by.
     */
    private final int _resizeAmount;

    /**
     * The array of listeners
     */
    private transient WeakReference[] _listeners;


    /**
     * The next free index in the array of listeners
     */
    private transient int _freeIndex;


    /**
     * Create the AbstractTyrexConnectionImpl using
     * the {@link #DEFAULT_RESIZE_AMOUNT}.
     */
    public AbstractTyrexConnectionImpl()
    {
        this(DEFAULT_RESIZE_AMOUNT);
    }

    /**
     * Create the AbstractTyrexConnectionImpl using
     * the the specified resize amount
     *
     * @param resizeAmount the amount to resize the 
     *      listener storage by. This must be greater than 0.
     */
    public AbstractTyrexConnectionImpl(int resizeAmount)
    {
        if (0 >= resizeAmount) {
            throw new IllegalArgumentException("The argument 'resizeAmount' must be positive.");
        }

        _resizeAmount = resizeAmount;
    }

    
    public synchronized Statement createStatement()
        throws SQLException
    {
	try {
            return new TyrexStatementImpl(getUnderlyingConnection().createStatement(), this);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized Statement createStatement(int resultSetType, int resultSetConcurrency)
        throws SQLException
    {
	try {
            return new TyrexStatementImpl(getUnderlyingConnection().createStatement(resultSetType, resultSetConcurrency),
                                      this);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized PreparedStatement prepareStatement(String sql)
        throws SQLException
    {
	try {
            return new TyrexPreparedStatementImpl(getUnderlyingConnection().prepareStatement(sql), this);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
        throws SQLException
    {
	try {
            return new TyrexPreparedStatementImpl(getUnderlyingConnection().prepareStatement(sql, resultSetType, resultSetConcurrency),
                                              this);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized CallableStatement prepareCall(String sql)
        throws SQLException
    {
	try {
            return new TyrexCallableStatementImpl(getUnderlyingConnection().prepareCall(sql),
                                              this);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
        throws SQLException
    {
	try {
            return new TyrexCallableStatementImpl(getUnderlyingConnection().prepareCall(sql, resultSetType, resultSetConcurrency),
                                              this);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized String nativeSQL(String sql)
        throws SQLException
    {
	try {
            return getUnderlyingConnection().nativeSQL(sql);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized DatabaseMetaData getMetaData()
        throws SQLException
    {
	try {
            return getUnderlyingConnection().getMetaData();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized void setCatalog( String catalog )
        throws SQLException
    {
	try {
            getUnderlyingConnection().setCatalog( catalog );
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized String getCatalog()
        throws SQLException
    {
	try {
            return getUnderlyingConnection().getCatalog();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized SQLWarning getWarnings()
        throws SQLException
    {
	try {
            return getUnderlyingConnection().getWarnings();
            }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized void clearWarnings()
        throws SQLException
    {
	try {
            getUnderlyingConnection().clearWarnings();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized Map getTypeMap()
        throws SQLException
    {
	try {
            return getUnderlyingConnection().getTypeMap();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized void setTypeMap(Map map)
        throws SQLException
    {
	try {
            getUnderlyingConnection().setTypeMap(map);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized void setAutoCommit(boolean autoCommit)
        throws SQLException
    {
        try {
            getUnderlyingConnection().setAutoCommit(autoCommit);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized boolean getAutoCommit()
        throws SQLException
    {
        try {
            return getUnderlyingConnection().getAutoCommit();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized void commit()
        throws SQLException
    {
        try {
            getUnderlyingConnection().commit();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }



    public synchronized void rollback()
        throws SQLException
    {
        try {
            getUnderlyingConnection().rollback();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized void setReadOnly(boolean readOnly)
        throws SQLException
    {
	try {
            getUnderlyingConnection().setReadOnly(readOnly);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized boolean isReadOnly()
        throws SQLException
    {
	try {
            return getUnderlyingConnection().isReadOnly();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }
    

    public synchronized void setTransactionIsolation(int level)
        throws SQLException
    {
	try {
            getUnderlyingConnection().setTransactionIsolation(level);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized int getTransactionIsolation()
        throws SQLException
    {
	try {
            return getUnderlyingConnection().getTransactionIsolation();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    /**
     * Called when an exception is thrown by the underlying connection.
     * <P>
     * The default implementation is to do nothing
     *
     * @param except The exception thrown by the underlying
     *   connection
     */
    protected void notifyError(SQLException exception)
    {

    }


    /**
     * Close this connection which may or may not close the
     * underlying connection. And notify any listeners that
     * the connection has been closed.
     *
     * @throws SQLException if there is a problem closing the connection 
     * @see #internalClose
     */
    public synchronized void close()
	throws SQLException
    {
        try {
            internalClose();
            notifyConnectionClosed();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    /**
     * Method that actually closes the connection.
     *
     * @throws SQLException if there is a problem closing the connection
     * @see #close
     */
    protected abstract void internalClose()
        throws SQLException;

    
    /**
     * Return true if the connection is closed.
     *
     * @return true if the connection is closed.
     */
    public abstract boolean isClosed();
    

    /**
     * Close the connection when it is being garbage collected.
     */
    protected void finalize()
	throws Throwable
    {
	if (!isClosed()) {
            close();
        }
    }
    

    /**
     * Return the underlying connection.
     *
     * @return the underlying connection
     * @throws SQLException if the connection is closed 
     *      or cannot be retrieved.
     * @see #internalGetUnderlyingConnection
     */
    private Connection getUnderlyingConnection()
        throws SQLException
    {
        if (isClosed()) {
            throw new SQLException("The connection is closed.");    
        }

        return internalGetUnderlyingConnection();
    }


    /**
     * Return the underlying connection.
     * <P>
     * The connection is not closed ie {@link #isClosed} returns
     * false.
     *
     * @return the underlying connection
     * @throws SQLException if the connection cannot be retrieved.
     * @see #getUnderlyingConnection
     */
    protected abstract Connection internalGetUnderlyingConnection()
        throws SQLException;
    

    /**
     * Add the specified listener
     *
     * @param listener the listener
     * @throws SQLException if the connection is closed
     */
    public synchronized void addListener(TyrexConnectionListener listener)
        throws SQLException
    {
        WeakReference[] newListeners;
        int listenersLength;
        Object currentListener;
        int i;
        int firstNullIndex;
        int numberOfNullEntries;

        if (isClosed()) {
            throw new SQLException(toString() + " is closed.");
        }

        if (null != listener) {
            if (null == _listeners) {
                _listeners = new WeakReference[]{new WeakReference(listener)};
                _freeIndex = 1;
            }
            else {
                // look for duplicates
                for (i = _freeIndex; --i >= 0;) {
                    currentListener = null == _listeners[i] ? null : _listeners[i].get();
                    if (null == currentListener) {
                        _listeners[i] = null;    
                    }
                    else if (currentListener == listener) {
                        return;    
                    }
                }
    
                listenersLength = _listeners.length;
    
                // if we are out of space move the non-null listeners
                // into the empty spaces, preserving the order
                // The empty weak references have been set to null
                // in the loop looking for duplicates above.
                if (_freeIndex == listenersLength) {
                    // set the first null index to a dummy value
                    firstNullIndex = -1;
                    // we start off with no null entries
                    numberOfNullEntries = 0;
                    for (i = 0; i < listenersLength; ++i) {
                        if (null == _listeners[i]) {
                            if (-1 == firstNullIndex) {
                                firstNullIndex = i;    
                            }
                            ++numberOfNullEntries;
                        }
                        else if (0 != numberOfNullEntries) {
                            // put the non-null entry in the null slot
                            _listeners[firstNullIndex] = _listeners[i];
                            
                            if (0 == --numberOfNullEntries) {
                                // no more null entries so reset
                                // first null index
                                firstNullIndex = -1;        
                            }
                            else {
                                ++firstNullIndex;
                            }
                            // there is a free slot at the end
                            --_freeIndex;
                        }
                    }
                    // subtract any null spots at the end
                    _freeIndex -= numberOfNullEntries;
                }
    
    
                // if we have space then add the listener
                if (_freeIndex < listenersLength) {
                    _listeners[_freeIndex++] = new WeakReference(listener);    
                }
                // else rezise
                else {
                    newListeners = new WeakReference[listenersLength + _resizeAmount];
                    System.arraycopy(_listeners, 0, newListeners, 0, listenersLength);
                    // add the listener
                    newListeners[listenersLength] = new WeakReference(listener);
                    _freeIndex = listenersLength + 1;
                    // swap
                    _listeners = newListeners;
                    newListeners = null;
                }
            }
        }
    }


    /**
     * Remove the specified listener
     *
     * @param listener the listener
     * @throws SQLException if the connection is closed.
     */
    public synchronized void removeListener(TyrexConnectionListener listener)
        throws SQLException
    {
        Object currentListener;

        if (isClosed()) {
            throw new SQLException(toString() + " is closed.");
        }

        if ((null != listener) &&
            (null != _listeners) &&
            (_freeIndex > 0)) {
            // if there is only one listener do a direct compare
            if (1 == _freeIndex) {
                currentListener = null == _listeners[0] ? null : _listeners[0].get();

                if ((null == currentListener) ||
                    (currentListener == listener)) {
                    _listeners[0] = null;
                    --_freeIndex;
                }

                return;
            }

            // loop looking for a match
            for (int i = _freeIndex; --i >= 0;) {
                currentListener = null == _listeners[i] ? null : _listeners[0].get();

                if (null == currentListener) {
                    _listeners[i] = null;    
                }
                else if (listener == currentListener) {
                    // need to preserve the ordering of the listeners

                    // the free index is moved to the left
                    --_freeIndex;

                    // if the last listener is removed
                    if (i == _freeIndex) {
                        _listeners[i] = null;    
                    }
                    // else shift the listeners to the left
                    else {
                        for (int j = _freeIndex; j > i; --j) {
                            _listeners[j - 1] = _listeners[j];
                            _listeners[j] = null;
                        }
                    }
                    return;
                }
            }
        }
    }

    /**
     * Notify that the listeners that connection has been closed.
     * <p>
     * The listeners are informed in terms of reverse add order ie
     * The last listener that was added is notified first.
     * <P>
     * This method can only called once with the current listeners. Successive
     * calls have no effect ie the listeners are only notified once that
     * the connection is closed.
     */
    private final void notifyConnectionClosed()
    {
        Object currentListener;

        for (int i = _freeIndex; --i >= 0;) {
            currentListener = null == _listeners[i] ? null : _listeners[i].get();

            if (null != currentListener) {
                try {
                    ((TyrexConnectionListener)currentListener).connectionClosed();
                }
                catch(Exception e) {
    
                }
            }
            else {
                _listeners[i] = null;
            }
        }
        _freeIndex = 0;
        _listeners = null;
    }
}
