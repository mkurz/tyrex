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


package tyrex.services;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.File;

import junit.framework.*;
import junit.extensions.*;

/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.1 $
 */


public class DaemonMasterTest extends TestCase
{
    private PrintWriter _logger = null;

    public DaemonMasterTest(String name)
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
     * <p>Create a test daemon and ensure that it gets restarted after
     * dying.</p>
     *
     * @result Ensure that there are no daemons running.  Add the
     * dummy daemon.  Ensure that it is running.  Wait sufficient time
     * for it to have stopped.  Ensure that it is not running (via a
     * direct call to the Runnable).  Wait sufficient time for the
     * Daemon manager to have re-started it.  Check whether it is
     * running again.  It should be.
     */

    public void testBasicFunctionality()
        throws Exception
    {
        assertEquals("Unexpected daemon running", 0, DaemonMaster.getCount());
        TestDaemon testDaemon = new TestDaemon();
        assert(!testDaemon.isRunning());
        DaemonMaster.addDaemon(testDaemon, "Dummy daemon");
        Thread.sleep(1000);
        assertEquals("Daemon not running", 1, DaemonMaster.getCount());
        assert(testDaemon.isRunning());
        Thread.sleep(30000);

        // At this point the daemon won't run again till the
        // DaemonMaster detects that it has stopped and starts it
        // again.  However the DaemonMaster will still mark it as
        // running.
        assert(!testDaemon.isRunning());
        assertEquals("Daemon not running", 1, DaemonMaster.getCount());

        // Sleep enough time for the DaemonMaster to have started it again.
        Thread.sleep(40000);
        assertEquals("Daemon not running", 1, DaemonMaster.getCount());
        assert(testDaemon.isRunning());
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
        tyrex.Unit.runTests(args, new TestSuite(DaemonMasterTest.class));
    }

    private class TestDaemon implements Runnable
    {
        private boolean _isRunning = false;

        public TestDaemon()
        {
            // Empty.
        }

        public boolean isRunning()
        {
            return _isRunning;
        }

        public void run()
        {
            _isRunning = true;
            try
            {
                // Doesn't do anything except exist for 400 milliseconds.
                Thread.sleep(30000);
            }
            catch (Exception e)
            {
                // Ignore any sleep exceptions.
            }
            _isRunning = false;
        }
    }
}
