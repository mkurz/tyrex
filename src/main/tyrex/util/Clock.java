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
 */

package tyrex.util;

/**
 * Provides an efficient mechanism for obtaining the current time and
 * date. Uses a background thread to automatically increment an internal
 * clock and periodically synchronize with the system clock. The method
 * {@link #clock clock} method is more efficient than {@link java.lang.System#currentTimeMillis
 * currentTimeMillis}, and also allows the clock to be artificially advanced
 * for testing purposes.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $
 */
public final class Clock
    extends Thread
{


    /**
     * The number of clock ticks to skip before incrementing the internal
     * clock. Default size is 200 milliseconds.
     */
    public static final int SLEEP_TICKS = 200;


    /**
     * The number of {@link #SLEEP_TICKS} clock ticks to skip before
     * synchronizing with the system clock. Default size is 10 (or 2000
     * milliseconds).
     */
    public static final int SYNCH_EVERY = 10;


    /**
     * The singleton clock.
     */
    private static final Clock  _clock;


    /**
     * The current clock.
     */
    private long  _ticks;


    /**
     * The number of clock ticks to skip before incrementing the internal clock.
     */
    private int   _sleepTicks;


    /**
     * The number of sleep ticks to skip before synchronizing with the system clock.
     */
    private int   _synchEvery;


    /**
     * The amount of time in milliseconds by which to advance the clock compared
     * to the system clock.
     */
    private long  _advance;


    /**
     * Used to adjust the clock when it gets out of synch. Based on the different
     * between the last clock and the system clock at the point of synchronization,
     * divided by synchEvery.
     */
    private int   _adjust;


    /**
     * Returns the current clock.
     *
     * @return The current clock
     */
    public static long clock()
    {
        return _clock._ticks;
    }


    /**
     * Sets the number of clock ticks to skip before incrementing
     * the internal clock. Too large a number might cause the clock
     * to get out of synch. Use zero to restore the default value.
     *
     * @param ticks The number of clock ticks in milliseconds
     */
    public static void setSleepTicks( int ticks )
    {
        if ( ticks == 0 )
            ticks = SLEEP_TICKS;
        else if ( ticks < 100 )
            ticks = 100;
        _clock._sleepTicks = ticks;
    }


    /**
     * Returns the number of clock ticks to skip before incrementing
     * the internal clock.
     *
     * @return The number of clock ticks in milliseconds
     */
    public static int getSleepTicks()
    {
        return _clock._sleepTicks;
    }


    /**
     * Sets the number of sleep ticks to skip before synchronizing
     * with the system clock. Synchronization will occur every
     * <tt>sleepTicks * synchEvery</tt> milliseconds. Too large
     * a number might cause the clock to get out of synch.
     * Use zero to restore the default value.
     *
     * @param every The number of sleep ticks
     */
    public static void setSynchEvery( int every )
    {
        if ( every == 0 )
            every = SYNCH_EVERY;
        else if ( every < 1 )
            every = 1;
        _clock._synchEvery = every;
    }


    /**
     * Returns the number of milliseconds by which the clock is
     * advanced compared to the system clock.
     *
     * @return The number of milliseconds by which the clock is
     * advanced
     */
    public static long getAdvance()
    {
        return _clock._advance;
    }


    public void run()
    {
        while ( true ) {
            try {
                for ( int i = 0 ; i < _synchEvery ; ++i ) {
                    sleep( _sleepTicks );
                    _ticks += _sleepTicks + _adjust;
                }
                synchronize();
            } catch ( Throwable thrw ) { 
            }
        }
    }


    private void synchronize()
    {
        long current;
        long retarded;

        current = System.currentTimeMillis();
        // Adjust clock to new difference
        if ( current != _ticks )
            _adjust += (int) ( current - _ticks ) / _synchEvery;
        // Make sure clock is progressive
        if ( current > _ticks )
            _ticks = current;        
    }


    private Clock()
    {
        super( "Clock Daemon Name" );
        _ticks = System.currentTimeMillis();
        setPriority( Thread.MAX_PRIORITY );
        setDaemon( true );
    }


    static {
        int value;

        _clock = new Clock();
        _clock._sleepTicks = SLEEP_TICKS;
        _clock._synchEvery = SYNCH_EVERY;
        _clock.start();
   }


    public static void main( String[] args )
    {
        long clock;
        int  count;

        try {
            count = 1000000;
            System.out.println( "Using Clock.clock()" );
            clock = System.currentTimeMillis();
            for ( int i = 0 ; i < count ; ++i )
                clock();
            clock = System.currentTimeMillis() - clock;
            System.out.println( "Performed " + count + " in " + clock + "ms" );
            System.out.println( "Using System.currentTimeMillis()" );
            clock = System.currentTimeMillis();
            for ( int i = 0 ; i < count ; ++i )
                System.currentTimeMillis();
            clock = System.currentTimeMillis() - clock;
            System.out.println( "Performed " + count + " in " + clock + "ms" );
        } catch ( Exception except ) { }
    }


}
