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

import java.sql.SQLException;

/////////////////////////////////////////////////////////////////////
// AbstractTyrexConnectionImpl
/////////////////////////////////////////////////////////////////////

/**
 * This class defines methods for managing {@link TyrexConnectionListener}
 * objects.
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
    private transient TyrexConnectionListener[] _listeners;


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
     *      listener storage by.
     */
    public AbstractTyrexConnectionImpl(int resizeAmount)
    {
        if (0 >= resizeAmount) {
            throw new IllegalArgumentException("The argument 'resizeAmount' must be positive.");
        }

        _resizeAmount = resizeAmount;
    }


    /**
     * Add the specified listener
     *
     * @param listener the listener
     * @throws SQLException if the connection is closed
     */
    public final synchronized void addListener(TyrexConnectionListener listener)
        throws SQLException
    {
        TyrexConnectionListener[] newListeners;
        int listenersLength;

        if (isClosed()) {
            throw new SQLException(toString() + " is closed.");
        }

        if (null != listener) {
            if (null == _listeners) {
                _listeners = new TyrexConnectionListener[]{listener};
                _freeIndex = 1;
            }

            // look for duplicates
            for (int i = _freeIndex; --i >= 0;) {
                if (_listeners[i] == listener) {
                    return;    
                }
            }

            listenersLength = _listeners.length;

            // if we have space then add the listener
            if (_freeIndex < listenersLength) {
                _listeners[_freeIndex++] = listener;    
            }
            // else rezise
            else {
                newListeners = new TyrexConnectionListener[listenersLength + _resizeAmount];
                System.arraycopy(_listeners, 0, newListeners, 0, listenersLength);
                // add the listener
                newListeners[listenersLength] = listener;
                _freeIndex = listenersLength + 1;
                // swap
                _listeners = newListeners;
                newListeners = null;
            }
        }
    }


    /**
     * Remove the specified listener
     *
     * @param listener the listener
     * @throws SQLException if the connection is closed.
     */
    public final synchronized void removeListener(TyrexConnectionListener listener)
        throws SQLException
    {
        if (isClosed()) {
            throw new SQLException(toString() + " is closed.");
        }

        if ((null != listener) &&
            (null != _listeners) &&
            (_freeIndex > 0)) {
            // if there is only one listener do a direct compare
            if (1 == _freeIndex) {
                if (_listeners[0] == listener) {
                    _listeners[0] = null;
                    --_freeIndex;
                }

                return;
            }

            // loop looking for a match
            for (int i = _freeIndex; --i >= 0;) {
                if (listener == _listeners[i]) {
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
     */
    protected synchronized final void notifyConnectionClosed()
    {
        for (int i = _freeIndex; --i >= 0;) {
            try {
                _listeners[i].connectionClosed();
            }
            catch(Exception e) {

            }
        }

        _listeners = null;
    }
}
