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
 * @version $Revision: 1.4 $
 */


public class ConfigurationTest extends TestCase
{
    private PrintWriter _logger = null;

    public ConfigurationTest(String name)
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
     * Configuration is not required.  Ensure that all methods return
     * the expected values based on the tyrex.config file.</p>
     *
     * @result Call getBoolean(Configuration.PROPERTY_LOG_VERBOSE).  It
     * should return true.  Calling it
     * with an unknown configuration name should also return false
     * (the default).  Call
     * getInteger(Configuration.PROPERTY_SLEEP_TICKS) and it should
     * return 10.  Calling it with an unknown configuration name
     * should return -1.
     *
     * <p>Call getProperty("version.vendorName").  It should return
     * the name of the vendor or null if one does not exist in the
     * configuration.</p>
     */

    public void testBasicFunctionality()
        throws Exception
    {
        assert(!Configuration.getBoolean(Configuration.PROPERTY_LOG_VERBOSE));
        assert(!Configuration.getBoolean(Configuration.PROPERTY_LOG_CONSOLE));
        assertEquals("Unsync", 100, Configuration
                     .getInteger(Configuration.PROPERTY_UNSYNCH_TICKS));
        assertEquals("Unsync", "100", Configuration
                     .getProperty(Configuration.PROPERTY_UNSYNCH_TICKS));
        assertEquals("Sync", 10, Configuration
                     .getInteger(Configuration.PROPERTY_SYNCH_EVERY));
        assertEquals("Sync", "10", Configuration
                     .getProperty(Configuration.PROPERTY_SYNCH_EVERY));
        assertEquals("uuid", "uuid.state", Configuration
                     .getProperty(Configuration.PROPERTY_UUID_STATE_FILE));
        assertEquals("URL", "http://www.intalio.com",
                     Configuration.VENDOR_URL);
        assertEquals("Title", "Tyrex", Configuration.TITLE);
        assert(!Configuration.getBoolean("Unknown.property"));
        assertNull(Configuration.getProperty("Unknown.property"));
        assertEquals("Unknown", "defaultVal", Configuration
                     .getProperty("Unknown.property", "defaultVal"));
        Properties props = Configuration.getProperties();
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
        tyrex.Unit.runTests(args, new TestSuite(ConfigurationTest.class));
    }
}
