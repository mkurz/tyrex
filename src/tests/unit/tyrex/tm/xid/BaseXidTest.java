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
 * $Id: BaseXidTest.java,v 1.4 2001/09/12 11:17:52 mills Exp $
 */

package tyrex.tm.xid;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.PrintWriter;


/**
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.4 $
 */

public abstract class BaseXidTest extends TestCase
{
    private PrintWriter _logger = null;

    public BaseXidTest(String name)
    {
        super(name);
    }


    /**
     * <p>The abstract method for creating an instance of BaseXid.</p>
     */

    public abstract BaseXid newBaseXid()
        throws Exception;


    /**
     * <p>The abstract method for returning a String representation of
     * BaseXid.</p>
     */

    public abstract String getStringXid(BaseXid xid)
        throws Exception;

    /**
     * <p>Create an instance.</p>
     *
     * @result Create a prefix using createPrefix().  It should return
     * a prefix of the correct form.  Create a String from this array
     * and ensure that the String is of the correct form.  Call
     * toString().  Compare the value returned to that returned by the
     * test classes getStringXid() (which should return the correct
     * string for the type of Xid).  Ensure they are the same.
     *
     * <p>Ensure that two calls to hashCode() return the same
     * value.</p>
     */

    public void testBasicFunctionality()
        throws Exception
    {
        BaseXid xid = newBaseXid();
        char[] pref = xid.createPrefix(762817453);
        String strPref = new String(pref);
        assertEquals("Prefeix", "xid:2d77abad-", strPref);
        assertEquals("toString()", getStringXid(xid), xid.toString());

        // The hashCode() function uses String's version.  To test
        // it's value String's version would have to be used again.
        // Therefore there is no gain.
        assertEquals("HashCode", xid.hashCode(), xid.hashCode());
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
