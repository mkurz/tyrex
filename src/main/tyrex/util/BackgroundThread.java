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
 * Copyright 1999-2001 (C) Intalio Technologies Inc. All Rights Reserved.
 *
 */


package tyrex.util;

import java.lang.ref.WeakReference;

/////////////////////////////////////////////////////////////////////
// BackgroundThread
/////////////////////////////////////////////////////////////////////

/**
 * This thread allows a runnable to run continously until the runnable
 * is garbage collected.
 * <P>
 * The thread waits for n number of milliseconds before running
 * the runnable. The wait-run execution occurs in an infinite loop.
 * If the runnable is garbage collected then the thread exits its loop
 * and stops running. 
 * <P>
 * For best results the runnable should not have an embedded loop. Obviously
 * if the runnable has an infinite loop then this thread never ends by itself.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public final class BackgroundThread extends Thread
{
    /**
     * The default boolean value that controls whether
     * the background thread exits when it is interrupted.
     */
    private static final boolean EXIT_ON_INTERRUPT = false; 


    /**
     * The name part of the default name of the
     * BackgroundThread object.
     * <P>
     * The default name is constructed by concatenating
     * DEFAULT_NAME and {@link #_counter}.
     */
    private static final String DEFAULT_NAME = "BackgroundThread-";


    /**
     * The number to place at the end of the default name 
     * of BackgroundThread object
     * <P>
     * The default name is constructed by concatenating
     * {@link #DEFAULT_NAME} and _counter.
     */
    private static int _counter;


    /**
     * The wait time in milliseconds
     */
    private volatile long _wait;


    /**
     * True if the background thread exits the wait-run
     */
    private volatile boolean _exitOnInterrupt;

    /**
     * The reference to the runnable
     */
    private final WeakReference _runnableReference;

    /**
     * Create the BackgroundThread.
     *
     * @param runnable the runnable to run
     * @param wait the time to wait before the runnable
     *      is run.
     */
    public BackgroundThread(Runnable runnable, long wait)
    {
        this(runnable, wait, getDefaultName());
    }


    /**
     * Create the BackgroundThread.
     *
     * @param runnable the runnable to run
     * @param wait the time to wait before the runnable
     *      is run.
     * @param name the name of the Thread. Cannot be null.
     */
    public BackgroundThread(Runnable runnable, long wait, String name)
    {
        this(runnable, wait, null, name);
    }


    /**
     * Create the BackgroundThread.
     *
     * @param runnable the runnable to run
     * @param wait the time to wait before the runnable
     *      is run.
     * @param threadGroup the thread group of the new thread. Can br null.
     */
    public BackgroundThread(Runnable runnable, long wait, ThreadGroup threadGroup)
    {
        this(runnable, wait, threadGroup, getDefaultName());
    }

    /**
     * Create the BackgroundThread.
     *
     * @param runnable the runnable to run
     * @param wait the time to wait before the runnable
     *      is run.
     * @param threadGroup the thread group of the new thread. Can br null.
     * @param name the name of the Thread. Cannot be null.
     */
    public BackgroundThread(Runnable runnable, long wait, ThreadGroup threadGroup, String name)
    {
        super(threadGroup, name);

        if (null == runnable) {
            throw new IllegalArgumentException("The argument 'runnable' is null.");
        }

        _runnableReference = new WeakReference(runnable);
        _exitOnInterrupt = EXIT_ON_INTERRUPT;
        setWait(wait);
    }


    /**
     * Return the default name for a new BackgroundThread.
     * <P>
     * The default name is constructed by concatenating
     * {@link #DEFAULT_NAME} and {@link #_counter}.
     *
     * @return the default name for a new BackgroundThread.
     */
    private static synchronized String getDefaultName()
    {
        return DEFAULT_NAME + _counter++;
    }

    /**
     * Return true if the background thread exits when
     * it is interrupted.
     *
     * @return true if the background thread exits when
     * it is interrupted.
     */
    public boolean getExitOnInterrupt()
    {
        return _exitOnInterrupt;
    }


    /**
     * Tell the background thread to exit or not, when
     * it is interrupted.
     *
     * @param exitOnInterrupt True if the background thread
     *      exits when it is interrupted
     */
    public void setExitOnInterrupt(boolean exitOnInterrupt)
    {
        _exitOnInterrupt = exitOnInterrupt;
    }

    /**
     * Return the time in milliseconds to wait before
     * the runnable is run.
     *
     * @return the wait time in milliseconds
     */
    public long getWait()
    {
        return _wait;
    }

    /**
     * Set the time in milliseconds to wait before the
     * the runnable is run.
     *
     * @param wait the wait time in milliseconds. 
     *      Must be greater than 0.
     */
    public void setWait(long wait)
    {
        if (0 >= wait) {
            throw new IllegalArgumentException("The argument 'wait' " + wait + " is invalid.");
        }

        _wait = wait;
    }

    /**
     * Run the runnable.
     * <P>
     * If the runnable has been garbage collected the thread ends.
     * <P>
     * The thread sleeps the prescribed number of seconds before running
     * the runnable. The wait-run execution occurs in an infinite loop
     */
    public void run()
    {
        Runnable run;

        while(true) {
            try {
                sleep(_wait);
            }
            catch (InterruptedException e){
                if (_exitOnInterrupt) {
                    break;    
                }
            }

            run = (Runnable)_runnableReference.get();

            if (null == run) {
                break;    
            }

            run.run();

            run = null;

            if (_exitOnInterrupt && isInterrupted()) {
                break;    
            }
        }
    }
}
