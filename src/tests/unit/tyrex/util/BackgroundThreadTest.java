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


package tyrex.util;

import java.util.Properties;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.File;

import junit.framework.*;
import junit.extensions.*;

/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.2 $
 */


public class BackgroundThreadTest extends TestCase
{
    private PrintWriter _logger = null;

    public BackgroundThreadTest(String name)
    {
        super(name);
    }

    public void setUp()
    {
        _logger= new PrintWriter(System.out);
    }

    public void tearDown()
    {
        _logger.flush();
    }


    /**
     * <p>Using a counter class initialise a runnable class.  Create
     * an instance of BackgroundThread using this runnable class and
     * 200 as arguments.  Start the run.  Each run will delay 200
     * milliseconds between runs.  The class itself increments the
     * counter class instance and sleeps 400 milliseconds.  So the
     * full cycle takes 600 milliseconds.  Get the main class to sleep
     * 6 seconds.</p>
     *
     * @result Set the local variable for the runnable class to null.
     * Delay slightly and then run the garbage collector.  The
     * background thread should die.  During the 6 seconds the
     * background thread should have been able to run 10 times.
     * Ensure that the counter has this value.  Delay a further 10
     * seconds and ensure that the counter is still 10 (i.e. the
     * background thread has infact stopped).
     */

    public void testBasicFunctionality()
        throws Exception
    {
        RunCount runCount = new RunCount();
        TestThread testThread = new TestThread(runCount);
        BackgroundThread bgThread = new BackgroundThread(testThread, 200);
        new Thread(bgThread).start();
        Thread.sleep(6000);
        testThread = null;
        Thread.sleep(200);
        Runtime.getRuntime().gc();
        assertEquals("Number of runs", 10, runCount.getRuns());
        Thread.sleep(10000);
        assertEquals("The background thread must still be running", 10,
                     runCount.getRuns());
    }


    /** Adds a message in the log (except if the log is null)*/
    private void logMessage(String message)
    {
        if (_logger != null)
        {
            _logger.println(message);
        }
    }


    // Allow this test to be run on its own.
    public static void main(String args[])
    {
        tyrex.Unit.runTests(args, new TestSuite(BackgroundThreadTest.class));
    }

    private class RunCount
    {
        private int numRuns = 0;

        public void inc()
        {
            numRuns++;
        }

        public int getRuns()
        {
            return numRuns;
        }
    }

    private class TestThread implements Runnable
    {
        private RunCount runs;

        public TestThread(RunCount runCount)
        {
            runs = runCount;
        }

        public void run()
        {
            runs.inc();
            try
            {
                Thread.sleep(400);
            }
            catch (Exception e)
            {
                // Ignore any sleep exceptions.
            }
        }
    }
}
