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

import java.util.Enumeration;

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
 * @version $Revision: 1.2 $
 */


public class HashIntTableTest extends TestCase
{
    private PrintWriter _logger = null;

    public HashIntTableTest(String name)
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
     * <p>Create an instance.  Populate it, change some of the values
     * associated with various keys.  Remove instances.  Ensure it
     * behaves as expected.</p>
     *
     * @result 
     */

    public void testBasicFunctionality()
        throws Exception
    {
        HashIntTable table = new HashIntTable();
        assertEquals("Size", 0, table.size());
        Integer i1 = new Integer(1);
        Integer i2 = new Integer(2);
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        table.put(i1, 1);
        table.put(i2, 2);
        table.put(i3, 3);
        assertEquals("Size", 3, table.size());
        assertEquals("get()", 1, table.get(i1));
        assertEquals("get()", 2, table.get(i2));
        assertEquals("get()", 3, table.get(i3));
        assertEquals("get()", 0, table.get(i4));
        table.increment(i2, 5);
        assertEquals("get()", 7, table.get(i2));
        table.remove(i1);
        assertEquals("Size", 2, table.size());
        Enumeration keys = table.keys();
        assert(keys.hasMoreElements());
        assertEquals("Key", i2, keys.nextElement());
        assert(keys.hasMoreElements());
        assertEquals("Key", i3, keys.nextElement());
        assert(!keys.hasMoreElements());
        table = new HashIntTable(10, 999);
        table.put(i1, 1);
        table.put(i2, 2);
        table.put(i3, 3);
        assertEquals("Size", 3, table.size());
        assertEquals("get()", 1, table.get(i1));
        assertEquals("get()", 2, table.get(i2));
        assertEquals("get()", 3, table.get(i3));
        assertEquals("get()", 999, table.get(i4));
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
        junit.textui.TestRunner.run(new TestSuite(HashIntTableTest.class));
    }
}
