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
 * Copyright 2000, 2001 (C) Intalio Inc. All Rights Reserved.
 *
 */


package tyrex.tm.jdbc;


/**
 *
 * <ul>
 * <li><tt>maximum</tt> The maximum number of connections supported,
 * zero if no limit is placed on the connection pool.</li>
 * <li><tt>minimum</tt> The minimum number of connections that are
 * always available in the pool.</li>
 * <li><tt>initial</tt> The initial connection pool size.</li>
 * <li><tt>maxRetain</tt> The maximum time to retain an unused
 * connection (seconds), zero if no limit is placed on the pool.</li>
 * <li><tt>timeout</tt> The timeout when attempting to open a new
 * connection (in seconds), zero to give up immediately.</li>
 * <li><tt>twoPhase</tt> True if the JDBC driver supports distributed
 * transactions with two-phase commit. The default is true.</li>
 * <li><tt>trace</tt> True if the JDBC driver should write trace
 * information to the log.</li>
 * </ul>
 * 
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version $Revision: 1.1 $
 */
public final class Limits
{


    /**
     * The maximum number of connections supported, or zero.
     */
    protected int           _maximum;


    /**
     * The minimum number of connections required.
     */
    protected int           _minimum;


    /**
     * The initial pool size.
     */
    protected int           _initial;


    /**
     * The maximum time to retain an unused connection (in seconds),
     * or zero.
     */
    protected int           _maxRetain;


    /**
     * The timeout when attempting to open a new connection (in seconds),
     * or zero.
     */
    protected int           _timeout;


    /**
     * True if connections support two-phase commit.
     */
    protected boolean       _twoPhase;


    /**
     * True to enable tracing of connections.
     */
    protected boolean       _trace;


    /**
     * Sets the maximum number of connections supported. If this value is zero,
     * no limit is placed on the connection pool. 
     * <p>
     * If this value is non-zero, the connection pool will not allow more
     * connections to be opened than this upper limit.
     *
     * @param maximum The maximum number of connections supported, or zero
     */
    public void setMaximum( int maximum )
    {
        if ( maximum < 0 )
            maximum = 0;
        _maximum = maximum;
    }


    /**
     * Returns the maximum number of connections supported. If this value s zero,
     * no limit is placed on the connection pool. 
     *
     * @return The maximum number of connections supported, or zero
     */
    public int getMaximum()
    {
        return _maximum;
    }


    /**
     * Sets the minimum number of connections required. The connection pool
     * will not attempt to release unused connections when the pool size falls
     * below this threshold.
     * <p>
     * This threshold can be used to determine the number of connections that
     * are always available in the pool. However, the connection pool will not
     * attempt to create that number of connections directly.
     *
     * @param minimum The minimum number of connections required
     */
    public void setMinimum( int minimum )
    {
        if ( minimum < 0 )
            minimum = 0;
        _minimum = minimum;
    }


    /**
     * Returns the minimum number of connections required. The connection pool
     * will not attempt to release unused connections when the pool size falls
     * below this threshold.
     *
     * @return The minimum number of connections required
     */
    public int getMinimum()
    {
        return _minimum;
    }


    /**
     * Sets the initial connection pool size. When the JDBC driver is loaded,
     * the connection pool will attempt to create that number of connections.
     *
     * @param initial The initial pool size
     */
    public void setInitial( int initial )
    {
        if ( initial < 0 )
            initial = 0;
        _initial = initial;
    }


    /**
     * Returns the initial pool size. When the JDBC driver is loaded, the connection
     * pool will attempt to create that number of connections.
     *
     * @return The initial pool size
     */
    public int getInitial()
    {
        return _initial;
    }


    /**
     * Sets the maximum time to retain an unused connection. This is the longest
     * duration an unusued connection will be retained in the pool, specified
     * in seconds.
     * <p>
     * If this number if not zero, the connection pool will attempt to release
     * connections that have not been used for that amount of time. The connection
     * pool will not release connections if the pool size is equal to the value
     * specified by {@link #getMinimum getMinimum}.
     *
     * @param seconds The maximum time to retain an unused connection (in seconds),
     * or zero
     */
    public void setMaxRetain( int seconds )
    {
        if ( seconds < 0 )
            seconds = 0;
        _maxRetain = seconds;
    }


    /**
     * Returns the maximum time to retain an unused connection. This is the longest
     * duration an unusued connection will be retained in the pool, specified
     * in seconds.
     *
     * @return The maximum time to retain an unused connection (in seconds),
     * or zero
     */
    public int getMaxRetain()
    {
        return _maxRetain;
    }


    /**
     * Sets the timeout when attempting to open a new connection. This is the longest
     * duration to wait for a new connection to be available when the pool has reached
     * its maximum size, specified in seconds.
     * <p>
     * If this value is not zero, the connection pool will block up to this number of
     * milliseconds if it is unable to obtain an existing connection. The connection
     * pool is not able to obtain an existing connection if the connection pool size
     * has reached the maximum size as specified by {@link #getMaximum getMaximum}.
     *
     * @param timeout The timeout when attempting to open a new connection (in seconds),
     * or zero
     */
    public void setTimeout( int seconds )
    {
        if ( seconds < 0 )
            seconds = 0;
        _timeout = seconds;
    }


    /**
     * Returns the timeout when attempting to open a new connection. This is the longest
     * duration to wait for a new connection to be available when the pool has reached
     * its maximum size, specified in seconds.
     *
     * @return The timeout when attempting to open a new connection (in seconds),
     * or zero
     */
    public int getTimeout()
    {
        return _timeout;
    }


    /**
     * Sets the two-phase commit support flag. If this value is true, connections support
     * two-phase commit.
     * <p>
     * This flag is valid only if an XA data source is used. The default is always true.
     *
     * @param twoPhase True if connections support two-phase commit
     */
    public void setTwoPhase( boolean twoPhase )
    {
        _twoPhase = twoPhase;
    }


    /**
     * Returns the two-phase commit support flag. If this value is true, connections support
     * two-phase commit.
     *
     * @return True if connections support two-phase commit
     */
    public boolean getTwoPhase()
    {
        return _twoPhase;
    }


    /**
     * Sets the tracing flag. If this value is true, the JDBC driver will be
     * asked to write trace information to the log.
     *
     * @param trace True if JDBC driver should write trace information to
     * the log
     */
    public void setTrace( boolean trace )
    {
        _trace = trace;
    }


    /**
     * Returns the tracing flag. If this value is true, the JDBC driver will
     * be asked to write trace information to the log.
     *
     * @return True if JDBC driver should write trace information to
     * the log
     */
    public boolean getTrace()
    {
        return _trace;
    }


    public Object clone()
    {
        Limits clone;

        clone = new Limits();
        clone._maximum = _maximum;
        clone._minimum = _minimum;
        clone._initial = _initial;
        clone._maxRetain = _maxRetain;
        clone._timeout = _timeout;
        clone._twoPhase = _twoPhase;
        clone._trace = _trace;
        return clone;
    }


}
