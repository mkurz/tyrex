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


package tyrex.tm;


/**
 * Holds metrics associated with a transaction domain.
 * <p>
 * This object records usage metrics for transaction, recording such information
 * as the accumulated number of transactions committed and rolledback, the
 * average duration of a transaction, etc.
 *
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version $Revision: 1.1 $
 */
public final class DomainMetrics
{


    /**
     * The accumulated transaction time.
     */
    private long   _accumTime;

    
    /**
     * The accumulated count of committed transactions.
     */
    private int    _accumCommitted;


    /**
     * The accumulated count of rollback transactions.
     */
    private int    _accumRolledback;


    /**
     * The number of active transactions.
     */
    private int    _active;


    /**
     * Returns the total number of committed transactions.
     *
     * @return The total number of committed transactions
     */
    public int getTotalCommitted()
    {
        return _accumCommitted;
    }


    /**
     * Returns the total number of rolled back transactions.
     *
     * @return The total number of rolled back transactions
     */
    public int getTotalRolledback()
    {
        return _accumRolledback;
    }


    /**
     * Returns the average duration for active transactions.
     * Returns the average number of seconds transactions have been
     * active, whether eventually committed or rolledback.
     * Ignores any transactions that are currently in progress.
     *
     * @return The average duration for active transactions
     */
    public synchronized float getAvgDuration()
    {
        return ( (float) ( _accumTime ) / (float) _accumCommitted + _accumRolledback ) / 10000;
    }


    /**
     * Record the duration for a committed transaction.
     *
     * @param ms The duration is milliseconds
     */
    public synchronized void recordCommitted( int ms )
    {
        ++_accumCommitted;
        _accumTime += ms;
    }


    /**
     * Record the duration for a rolled back transaction.
     *
     * @param ms The duration is milliseconds
     */
    public synchronized void recordRolledback( int ms )
    {
        ++_accumRolledback;
        _accumTime += ms;
    }


    /**
     * Returns the current number of active transactions.
     *
     * @return The current number of active transactions
     */
    public int getActive()
    {
        return _active;
    }


    /**
     * Called to change the number of active transactions. This method
     * reflects an increase or decrease in the number of active transactions.
     * If the change results in a negative total count, this method corrects
     * the count and throws an <tt>IllegalStateException</tt>.
     *
     * @param change The change in active transactions (positive or
     * negative integer)
     * @throws IllegalStateException Change resulted in an negative count
     */
    public synchronized void changeActive( int change )
    {
        change += _active;
        if ( change < 0 ) {
            _active = 0;
            throw new IllegalStateException( "Count of active transactions is negative" );
        }
        _active = change;
    }


    /**
     * Called to reset this metrics object.
     */
    public synchronized void reset()
    {
        _accumTime = 0;
        _accumCommitted = 0;
        _accumRolledback = 0;
        _active = 0;
    }


}
