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
 * $Id: EnumerationTest.java,v 1.1 2001/08/23 10:11:12 mills Exp $
 */

package tyrex.util;

import java.util.Enumeration;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.PrintWriter;


/**
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.1 $
 */

public abstract class EnumerationTest extends TestCase
{
    private PrintWriter _logger = null;

    public EnumerationTest(String name)
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
     * <p>The abstract method for creating an instance of Enumeration.
     * The method must populate the enumeration with three
     * elements.</p>
     */

    public abstract Enumeration newEnumeration()
        throws Exception;

    public abstract Enumeration newEmptyEnumeration()
        throws Exception;

    public abstract Object getValue(int idx);


    /**
     * <p>Create an instance.</p>
     *
     * @result Ensure that hasMoreElements() returns true.  Call
     * next() three times ensuring that the correct values are
     * returned each time.  hasMoreElements() should now return false.
     */

    public void testBasicFunctionality()
        throws Exception
    {
        Enumeration enum = newEnumeration();
        assert(enum.hasMoreElements());
        assertEquals(enum.nextElement(), getValue(0));
        assertEquals(enum.nextElement(), getValue(1));
        assertEquals(enum.nextElement(), getValue(2));
        assert(!enum.hasMoreElements());
    }


    /**
     * <p>Attempt to test the bounds of argument values etc.</p>
     *
     * @result Create an Enumeration with no elements.
     * hasMoreElements() should return false and nextElement() should
     * throw a NoSuchElementException.
     *
     * <p>Now create an instance as in testBasicFunctionality.  Call
     * nextElement() 4 times instead of three.  On the fourth call a
     * NoSuchElementException should be thrown.</p>
     */

    public void testBoundsTest()
        throws Exception
    {
        try
        {
            Enumeration enum = newEnumeration();
            assert(enum.hasMoreElements());
            enum.nextElement();
            enum.nextElement();
            enum.nextElement();
            assertEquals(enum.nextElement(), getValue(3));
            fail("Expected an exception to have been raised.");
        }
        catch (java.util.NoSuchElementException e)
        {
            // Expected.
        }
    }


    /** Adds a message in the log (except if the log is null)*/
    private void logMessage(String message)
    {
        if (_logger != null)
        {
            _logger.println(message);
        }
    }
}
