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
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.3 $
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
     * <p>Create an instance of FastThreadLocal.  Then create an
     * instance of TestThread using the FastThreadLocal as argument.
     * The TestThread sets an Integer as its local variable and sleeps
     * for 4 seconds.  Start the run.</p>
     *
     * @result The TestThread should set its variable and sleep 4
     * seconds.  During this time its local variable should be visible
     * both internally and externally.  The main thread should sleep a
     * longer time and then run the garbage collectort.  After this
     * the local variable should not be retrievable.
     */

    public void testBasicFunctionality()
        throws Exception
    {
        FastThreadLocal ftl = new FastThreadLocal();
        TestThread testThread = new TestThread(ftl);
        testThread.start();
        Thread.sleep(100);
        Integer i = (Integer)ftl.get(testThread);
        assertNotNull(ftl.get(testThread));
        assertEquals("Val", 1, i.intValue());
        Thread.sleep(400);
        ThreadGroup thrGroup = testThread.getThreadGroup();
        Thread[] threads = new Thread[10];
        thrGroup.enumerate(threads);
        Thread ftlThread = null;
        String thrName = Messages.message("tyrex.util.threadLocalDaemonName");
        for (int j = 0; j < threads.length; j++)
        {
            if (threads[j] != null)
            {
                if (threads[j].getName().compareTo(thrName) == 0)
                {
                    ftlThread = threads[j];
                    break;
                }
            }
        }
        testThread = null;
        ftlThread.interrupt();
        threads = null;
        ftlThread = null;
        Runtime.getRuntime().gc();
        Thread.sleep(10000);
        Runtime.getRuntime().gc();
        threads = new Thread[10];
        thrGroup.enumerate(threads);
        for (int j = 0; j < threads.length; j++)
        {
            if (threads[j] != null)
            {
                if (threads[j].getName().compareTo("Test Thread") == 0)
                {
                    testThread = (TestThread)threads[j];
                    break;
                }
            }
        }
        if (testThread != null)
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
            super("Test Thread");
            _ftl = ftl;
        }

        public void run()
        {
            Integer i1 = new Integer(1);
            _ftl.set(i1);
            Integer i2 = (Integer)_ftl.get();
            assertEquals(i1, i2);

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
