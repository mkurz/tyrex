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
 * <p>Bounds testing the interface can be done as part of the basic
 * functionality since when inputs are invalid, default values are
 * returned.  Bounds testing the configuration file itself is useful
 * to ensure sensible behavior when the file is corrupted or
 * incorrectly written.</p>
 *
 * <p>This class is expected to change and so tests will not be
 * implemented until after the changes have been made and these
 * documented tests updated accordingly.</p>
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.1 $
 */


public class FastThreadLocalTest extends TestCase
{
    private PrintWriter _logger = null;

    public FastThreadLocalTest(String name)
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
     * an instance of FastThreadLocal using this runnable class and
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
        FastThreadLocal ftl = new FastThreadLocal();
        TestThread testThread = new TestThread(ftl);
        new Thread(testThread).start();
        Thread.sleep(100);
        Integer i = (Integer)ftl.get(testThread);
        assertNotNull(ftl.get(testThread));
        assertEquals("Val", 1, i.intValue());
        Thread.sleep(400);
        Thread thr = new Thread(ftl);
        thr.interrupt();
        Thread.sleep(100);
        Runtime.getRuntime().gc();
        Thread.sleep(10000);
        assertNull(ftl.get(testThread));
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
        tyrex.Unit.runTests(args, new TestSuite(FastThreadLocalTest.class));
    }

    private class TestThread extends Thread
    {
        FastThreadLocal _ftl;

        public TestThread(FastThreadLocal ftl)
        {
            _ftl = ftl;
        }

        public void run()
        {
            Integer i1 = new Integer(1);
            _ftl.set(i1);
            Integer i2 = (Integer)_ftl.get();
            assertEquals(i1, i2);
            System.out.println(this.getClass().getName());
            System.out.println(Thread.currentThread().getClass().getName());
            System.out.println(this.hashCode());
            System.out.println(Thread.currentThread().hashCode());

            // Don't do much just live a short while then die.
            try
            {
                Thread.sleep(4000);
            }
            catch (Exception e)
            {
                // Ignore any sleep exceptions.
            }
        }
    }
}
