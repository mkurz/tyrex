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
 * Contributions by MetaBoss team are Copyright (c) 2003-2004, Softaris Pty. Ltd. All Rights Reserved.
 *
 * $Id: DaemonMaster.java,v 1.6 2004/04/30 06:34:14 metaboss Exp $
 */


package tyrex.services;


import java.io.PrintWriter;

import tyrex.util.Configuration;
import tyrex.util.Messages;
import tyrex.util.logging.Logger;


/**
 * The daemon master is responsible for starting, terminating and restarting
 * daemon thread.
 * <p>
 * A daemon thread is a thread that is kept live for the duration of the server's
 * life and is only terminated when the server is stopped.
 * <p>
 * A sudden termination of a daemon thread is an unwelcome occurance in the
 * life time of the system. The daemon master protects the system from the sudden
 * and unexpected termination of daemons by automatically restarting them.
 * <p>
 * A daemon implements the <tt>Runnable</tt> interface which allows it to be
 * executed on any given thread. The daemon master assigns a thread within the
 * daemon master's thread group. If the daemon is suddently terminated, the
 * daemon master will be informed and attempt to restart the daemon with a new
 * thread.
 * <p>
 * The daemon master is thread-safe and consumes a single thread.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.6 $
 */
public class DaemonMaster
    extends ThreadGroup
    implements Runnable
{


    /**
     * The interval for checking when daemon threads have terminated. This interval
     * is set to a reasonable long time, however, if an error is detected in one
     * of the threads, the daemon master will attend to it immediately.
     */
    private static final long  CHECK_EVERY = 60000;


    /**
     * The first daemon record in a single linked-list of records.
     */
    private DaemonRecord  _first;


    /**
     * The number of daemons currently active.
     */
    private int           _count;


    /**
     * Reference to the singleton instance.
     */
    private static final DaemonMaster  _instance;


    /**
     * Private constructor.
     */
    private DaemonMaster()
    {
        super( Messages.message( "tyrex.util.daemonMaster" ) );

        Thread thread;

        thread = new Thread( this, this, Messages.message( "tyrex.util.daemonMaster" ) );
        thread.setPriority( Thread.MAX_PRIORITY );
        thread.setDaemon( true );
        thread.start();
    }


    static {
        _instance = new DaemonMaster();
    }


    /**
     * Returns the number of daemons currently in the system.
     *
     * @return The number of daemons currently in the system
     */
    public static int getCount()
    {
        return _instance._count;
    }


    /**
     * Adds a daemon. Once added, a daemon will be started asynchronously
     * and managed by the daemon master. If the daemon thread accidentally
     * terminates, the daemon master will attempt to restart the daemon
     * in a different thread.
     *
     * @param runnable The runnable object
     * @param name The daemon name
     */
    public static void addDaemon( Runnable runnable, String name )
    {
        addDaemon( runnable, name, Thread.NORM_PRIORITY );
    }


    /**
     * Adds a daemon. Once added, a daemon will be started asynchronously
     * and managed by the daemon master. If the daemon thread accidentally
     * terminates, the daemon master will attempt to restart the daemon
     * in a different thread.
     *
     * @param runnable The runnable object
     * @param name The daemon name
     * @param priority The thread priority
     */
    public static void addDaemon( Runnable runnable, String name, int priority )
    {
        DaemonRecord record;
        Thread       thread;

        synchronized( _instance ) {
            if ( runnable == null )
                throw new IllegalArgumentException( "Argument runnable is null" );
            if ( name == null )
                throw new IllegalArgumentException( "Argument name is null" );
            record = _instance._first;
            while( record != null ) {
                if ( record._runnable == runnable )
                    return;
                record = record._next;
            }
            thread = new Thread( _instance, runnable, name );
            _instance._first = new DaemonRecord( runnable, name, priority, thread, _instance._first );
            ++_instance._count;
            if ( Configuration.verbose )
                Logger.tyrex.info( "Starting daemon: " + name );
            thread.setPriority( priority );
            thread.setDaemon(true);
            thread.start();
        }
    }


    /**
     * Removes a daemon. Once removed, the daemon master will no longer
     * attempt to restart the daemon. This method must be called before
     * the thread has completed to prevent accidental restart. It will
     * automatically interrupt the background thread.
     *
     * @param runnable The runnable object
     */
    public static boolean removeDaemon( Runnable runnable )
    {
        DaemonRecord record;
        DaemonRecord last;

        synchronized( _instance ) {
            record = _instance._first;
            last = null;
            while( record != null ) {
                if ( record._runnable == runnable ) {
                    if ( last == null )
                        _instance._first = record._next;
                    else
                        last._next = record._next;
                    --_instance._count;
                    record._thread.interrupt();
                    return true;
                } else
                    last = record;
                record = record._next;
            }
        }
        return false;
    }


    public static synchronized void dump( PrintWriter writer )
    {
        DaemonRecord record;

        writer.println( "Daemon master managing " + _instance._count + " daemons" );
        record = _instance._first;
        while( record != null ) {
            writer.println( "  " + record._name );
            writer.println( "    Thread:   " + record._thread );
            writer.println( "    Runnable: " + record._runnable );
            writer.println( "    Priority: " + record._priority );
            record = record._next;
        }
    }


    public void uncaughtException( Thread thread , Throwable thrw )
    {
        Logger.tyrex.error( "Uncaught exception in daemon " + thread.getName(), thrw );
        synchronized ( _instance ) {
            _instance.notify();
        }
    }


    public void run()
    {
        DaemonRecord record;

        try {
            while ( true ) {
                synchronized ( this ) {
                    record = _first;
                    while( record != null ) {
                        if ( ! record._thread.isAlive() ) {
                            Logger.tyrex.error( "Detected daemon " + record._name + " stopped: restarting" );
                            record._thread = new Thread( this, record._runnable, record._name );
                            record._thread.setPriority( record._priority );
                            record._thread.start();
                        }
                        record = record._next;
                    }
                    wait( CHECK_EVERY );
                }
            }
        } catch ( Throwable thrw ) {
            Logger.tyrex.error( "Error reported by daemon master", thrw );
        }
    }


    /**
     * Record for a managed daemon.
     */
    private static class DaemonRecord
    {


        /**
         * Reference to the runnable object.
         */
        final Runnable  _runnable;


        /**
         * The daemon name.
         */
        final String    _name;


        /**
         * The daemon thread priority.
         */
        final int      _priority;


        /**
         * The current thread assigned to the daemon.
         */
        Thread        _thread;


        /**
         * Reference to the next record.
         */
        DaemonRecord  _next;


        /**
         * Constructs a new daemon record.
         */
        DaemonRecord( Runnable runnable, String name, int priority, Thread thread, DaemonRecord next )
        {
            _name = name;
            _runnable = runnable;
            _priority = priority;
            _thread = thread;
            _next = next;
        }


    }


}


