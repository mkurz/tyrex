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


public class ClockTest extends TestCase
{
    private PrintWriter _logger = null;

    public ClockTest(String name)
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
     * <p>Get the system time and the clock value.  Advance the time
     * and ensure that the advanced time is correct.</p>
     *
     * @result The clock and the system time should be equal to within
     * a very small margin.  Allow 10 milliseconds.  Ensure this.
     * Advance the clock by 200 milliseconds.  Ensure that the time is
     * now equal to or just over 200 milliseconds past the old time.
     * Ensure that the advance is 200.
     *
     * <p>Set the unsynch ticks and ensure that getUnsyncTicks()
     * returns the value set.  Other aspects of the class cannot be
     * tested since setting different values with setUnsyncTicks() and
     * setSynchEvery() will not change the behavior of the class in a
     * predictable way.  Different hardware and loads on the machine
     * will have a greater effect.</p>
     */

    public void testBasicFunctionality()
        throws Exception
    {
        long currTime = Clock.clock();
        long systemTime = System.currentTimeMillis();

        // Allow 10 milliseconds - but probably equal or closer.
        assert("Clock", systemTime >= currTime && systemTime < currTime + 10);
        Clock.advance(200);
        long newTime = Clock.clock();
        assert(newTime >= currTime + 200 && newTime <= currTime + 210);
        assertEquals("Advance", 200, Clock.getAdvance());
        Clock.setUnsynchTicks(100);
        assertEquals("Unsynch ticks", 100, Clock.getUnsynchTicks());
        Clock.setUnsynchTicks(200);
        assertEquals("Unsynch ticks", 200, Clock.getUnsynchTicks());
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
        tyrex.Unit.runTests(args, new TestSuite(ClockTest.class));
    }
}
