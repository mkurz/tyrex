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
 * Copyright 1999-2001 (C) Intalio Inc. All Rights Reserved.
 *
 */


package tyrex.resource;


/**
 * Holds metrics associated with a connection pool.
 * <p>
 * This object records usage metrics for the connection pool, recording
 * such information as the accumulated number of connections created and
 * used, the average time a connection is used by the application or held
 * in the pool, etc.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $
 */
public class PoolMetrics
{


    /**
     * The accumulated time client connections have been used (ms).
     */
    private long     _accumUsedTime;

    
    /**
     * The accumulated time client connections have been unused (ms).
     */
    private long     _accumUnusedTime;


    /**
     * The accumulated count of managed connections created.
     */
    private int      _accumCreated;


    /**
     * The accumulated count of client connections used.
     */
    private int      _accumUsed;


    /**
     * The accumulated count of client connections released to
     * the pool (unused).
     */
    private int      _accumUnused;


    /**
     * The accumulated count of discarded managed connections.
     */
    private int      _accumDiscarded;


    /**
     * The accumulated count of errors in managed connections.
     */
    private int      _accumErrors;


    /**
     * The total number of connections in the pool, both used and unused.
     */
    protected int    _total;


    /**
     * The number of connections available in the pool (unused).
     */
    protected int    _available;


    /**
     * Returns the number of managed connections created during the
     * lifetime of this connector. This value is incremented once for
     * each managed connection created, but is not affected by the number
     * of client connections obtained from that managed connection.
     *
     * @return The number of managed connections created
     */
    public int getTotalCreated()
    {
        return _accumCreated;
    }


    /**
     * Returns the number of client connections used. This value is
     * incremented once for each client connection returned to the
     * application.
     * <p>
     * The ratio {@link #getAccumUsed getAccumUsed} / {@link getAccumCreated
     * #getAccumCreated} represents how many client connections are
     * obtained from each managed connections.
     *
     * @return The number of client connections used
     */
    public int getTotalUsed()
    {
        return _accumUsed;
    }


    /**
     * Returns the number of managed connections discarded. This value
     * is incremented each time a managed connection is discarded from
     * the pool after a period of inactivity, but is not affected by
     * the application releasing a client connection to the pool.
     *
     * @return The number of managed connections discarded
     */
    public int getTotalDiscarded()
    {
        return _accumDiscarded;
    }


    /**
     * Returns the number of managed connections discarded due to an
     * error in the connection.
     *
     * @return The number of erroneous connections
     */
    public int getTotalErrors()
    {
        return _accumErrors;
    }


    /**
     * Returns the current number of managed connections in use.
     * This represents the number of connections in the pool that
     * are currently used by the application.
     *
     * @return The current number of managed connections in use
     */
    public synchronized int getCurrentUsed()
    {
        return _total - _available;
    }


    /**
     * Returns the current number of managed connection not in use.
     * This represents the number of connections in the pool that are
     * available to application. Together with {@link #getCurrentUsed
     * getCurrentUsed} it represents the total number of connections
     * currently managed by the pool.
     *
     * @return The current number of unused managed connections
     */
    public int getCurrentUnused()
    {
        return _available;
    }


    /**
     * Return the average duration for using a connection. Returns the
     * average number of seconds connections are used by the application.
     *
     * @return The average duration for using a connection
     */
    public synchronized float getUsedAvgDuration()
    {
        return ( (float) _accumUsedTime / (float) _accumUsed ) / 10000;
    }
    

    /**
     * Returns the average duration for holding a connection in the pool.
     * Returns the average number of seconds connections are retained in
     * the pool when they are not used by the application.
     *
     * @return The average duration for holding an unused connection
     */
    public synchronized float getUnusedAvgDuration()
    {
        return ( (float) _accumUnusedTime / (float) _accumUnused ) / 10000;
    }


    /**
     * Returns the total number of connections in the pool.
     * The total number includes both used and available connections.
     *
     * @return The total number of connections in the pool
     */
    public int getTotal()
    {
        return _total;
    }


    /**
     * Returns the number of connections available to the pool.
     *
     * @return The number of available connections
     */
    public int getAvailable()
    {
        return _available;
    }


    /**
     * Called to reset this metrics object.
     */
    public synchronized void reset()
    {
        _accumUsedTime = 0;
        _accumUnusedTime = 0;
        _accumCreated = 0;
        _accumUsed = 0;
        _accumUnused = 0;
        _accumDiscarded = 0;
        _accumErrors = 0;
    }


    /**
     * Record a created managed connection.
     */
    protected synchronized void recordCreated()
    {
        ++_accumCreated;
        ++_total;
    }


    /**
     * Record a discarded managed connection.
     */
    protected synchronized void recordDiscard()
    {
        ++_accumDiscarded;
        --_total;
    }


    /**
     * Record an error release of a managed connection.
     */
    protected synchronized void recordError()
    {
        ++_accumErrors;
        --_total;
    }


    /**
     * Record the duration for using a connection.
     *
     * @param ms The duration is milliseconds
     */
    protected synchronized void recordUsedDuration( int ms )
    {
        ++_accumUsed;
        _accumUsedTime += ms;
    }


    /**
     * Record the duration for holding a connection.
     *
     * @param ms The duration is milliseconds
     */
    protected synchronized void recordUnusedDuration( int ms )
    {
        ++_accumUnused;
        _accumUnusedTime += ms;
    }


}
