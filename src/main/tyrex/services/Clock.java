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
 * $Id: Clock.java,v 1.2 2001/03/12 19:20:19 arkin Exp $
 */


package tyrex.services;


import tyrex.util.Configuration;
import tyrex.util.Messages;
import tyrex.util.Logger;


/**
 * Provides an efficient mechanism for obtaining the current time and
 * date. Uses a background thread to automatically increment an internal
 * clock and periodically synchronize with the system clock. The method
 * {@link #clock clock} method is more efficient than {@link java.lang.System#currentTimeMillis
 * currentTimeMillis}, and also allows the clock to be artificially advanced
 * for testing purposes.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $
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
     * synchronizing with the system clock. Default value is 10 (or 2000
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
    private long                _ticks;


    /**
     * The number of clock ticks to skip before incrementing the internal clock.
     */
    private int                 _sleepTicks;


    /**
     * The number of sleep ticks to skip before synchronizing with the system clock.
     */
    private int                 _synchEvery;


    /**
     * The amount of time in milliseconds by which to advance the clock compared
     * to the system clock.
     */
    private int                _advance;


    /**
     * Used to adjust the clock when it gets out of synch. Based on the different
     * between the last clock and the system clock at the point of synchronization,
     * divided by synchEvery.
     */
    private int                 _adjust;


    /**
     * Returns the current clock.
     *
     * @return The current clock
     */
    public static synchronized long clock()
    {
        // Synchronization is required since clock is a long.
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
     * Artficially advances the clock compared to the system clock.
     * This method is used when conducting testing.
     *
     * @param byMillis The number of milliseconds by which to
     * advance the clock (must be positive)
     */
    public synchronized static void advance( int byMillis )
    {
        // Synchronization is required since clock is a long.
        _clock._advance += byMillis;
        _clock._ticks += byMillis;
    }


    /**
     * Returns the number of milliseconds by which the clock is
     * advanced compared to the system clock.
     *
     * @return The number of milliseconds by which the clock is
     * advanced
     */
    public static int getAdvance()
    {
        return _clock._advance;
    }


    public void run()
    {
        while ( true ) {
            try {
                for ( int i = 0 ; i < _synchEvery ; ++i ) {
                    sleep( _sleepTicks );
                    synchronized ( Clock.class ) {
                        _ticks += _sleepTicks + _adjust;
                    }
                }
                synchronize();
            } catch ( Throwable thrw ) {
                Logger.tyrex.error( "Internal error in clock daemon", thrw );
            }
        }
    }


    public static synchronized long synchronize()
    {
        long current;
        long retarded;
        long ticks;
        int  adjust;

        current = System.currentTimeMillis();
        ticks = _clock._ticks;
        retarded = ticks - _clock._advance;
        // Adjust clock to new difference
        if ( current != retarded ) {
            adjust = (int) ( current - retarded ) / _clock._synchEvery;
            if ( adjust != 0 ) {
                _clock._adjust += adjust;
                /*
                if ( Configuration.verbose )
                    Logger.tyrex.debug( "Clock late by " + ( current - retarded ) +
                                        "ms -> synchronized, adjusting by " + _clock._adjust );
                */
            }
        }
        // Make sure clock is progressive
        if ( current > retarded ) {
            ticks = current + _clock._advance;
            _clock._ticks = ticks;
        }
        return ticks;
    }


    protected Clock()
    {
        super( Messages.message( "tyrex.util.clockDaemon" ) );
        _ticks = System.currentTimeMillis();
        setPriority( Thread.MAX_PRIORITY );
        setDaemon( true );
    }


    static {
        int value;

        _clock = new Clock();
        value = Configuration.getInteger( Configuration.PROPERTY_SLEEP_TICKS );
        _clock._sleepTicks = value > 0 ? value : SLEEP_TICKS;
        value = Configuration.getInteger( Configuration.PROPERTY_SYNCH_EVERY );
        _clock._synchEvery = value > 0 ? value : SYNCH_EVERY;
        _clock.start();
        if ( Configuration.verbose )
            Logger.tyrex.info( Messages.format( "tyrex.util.clockDaemonStart",
                                                new Long( _clock._sleepTicks ),
                                                new Long( _clock._sleepTicks * _clock._synchEvery ) ) );
    }


    public static void main( String[] args )
    {
        long clock;
        int  count;

        try {
            count = 1000000;
            System.out.println( "Using Clock.clock()" );
            clock = System.currentTimeMillis();
            for ( int i = 0 ; i < count ; ++i ) {
                if ( ( i % 100 ) == 0 )
                    synchronize();
                else
                    clock();
            }
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
