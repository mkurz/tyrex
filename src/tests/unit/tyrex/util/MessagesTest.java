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


public class MessagesTest extends TestCase
{
    private PrintWriter _logger = null;

    public MessagesTest(String name)
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
     * <p>Since the methods are all static an instance of
     * Messages is not required.  Ensure that all methods return
     * the expected values.</p>
     *
     * @result Call various instances of Message.format() with no
     * extra object arguments, 1 argument, 2 args and 3 args.  Ensure
     * that all the messages are formatted correctly.
     *
     * <p>Call it with a message name that does not exist.  The
     * message name itself should be returned.
     */

    public void testBasicFunctionality()
        throws Exception
    {
        Integer i1 = new Integer(1);
        Integer i2 = new Integer(2);
        Integer i3 = new Integer(3);
        assertEquals("The transaction is not yet or no longer active",
                     Messages.message("tyrex.tx.inactive"));
        assertEquals("Transcation has been partially commited, partially rolledback",
                     Messages.message("tyrex.tx.heuristicMixed"));
        assertEquals("Cannot initialize the Transaction Server: 1",
                     Messages.format("tyrex.server.failedInitialize", i1));
        assertEquals("Cannot resume parent transaction: 1",
                     Messages.format("tyrex.tx.resumeParent", i1));
        assertEquals("Error delisting enlisted resource 1: 2",
                     Messages.format("tyrex.tx.delistEnlistedResource", i1,
                                     i2));
        assertEquals("Error terminating transaction 1: 2",
                     Messages.format("tyrex.server.timeoutTerminateError", i1,
                                     i2));
        assertEquals("Initializing UUID generator: node identifier 1, clock sequence 2, UUIDs pre tick 3",
                     Messages.format("tyrex.uuid.initializing", i1,
                                     i2, i3));
        assertEquals("tyrex.tx.nonexistent",
                     Messages.message("tyrex.tx.nonexistent"));
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
        tyrex.Unit.runTests(args, new TestSuite(MessagesTest.class));
    }
}
