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
 * $Id: XidUtilsTest.java,v 1.5 2001/11/12 02:50:45 mills Exp $
 */


package tyrex.tm.xid;

import javax.transaction.xa.Xid;

import java.io.PrintWriter;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.5 $
 */


public class XidUtilsTest extends TestCase
{
    private PrintWriter _logger = null;

    public XidUtilsTest(String name)
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
     * XidUtils is not required.</p>
     *
     * @result Create instances of each of branch, external, local and
     * global Xid-s.  Use each in the is&gt;Type&gt;() calls.  Only the
     * relevent ones should return true.
     *
     * <p>Call each of the new&lt;Type&gt;() methods.  Ensure that the Xid-s
     * returned are unique.</p>
     */

    public void testBasicFunctionality()
        throws Exception
    {
        BranchXid_BaseXidImpl branch = new BranchXid_BaseXidImpl("");
        Xid branchId = XidUtils.importXid(branch.newBaseXid());
        assert("Branch", XidUtils.isBranch(branchId));
        assert("Branch/Global", XidUtils.isGlobal(branchId));
        assert("Branch/Local", !XidUtils.isLocal(branchId));

        ExternalXid_BaseXidImpl external = new ExternalXid_BaseXidImpl("");
        ExternalXid eId = (ExternalXid)external.newBaseXid();
        Xid externalId = XidUtils.importXid(eId);
        assert("External", !XidUtils.isBranch(externalId));
        assert("External", XidUtils.isGlobal(externalId));
        assert("External", !XidUtils.isLocal(externalId));

        GlobalXid_BaseXidImpl global = new GlobalXid_BaseXidImpl("");
        Xid globalId = XidUtils.importXid(global.newBaseXid());
        assert("Global", !XidUtils.isBranch(globalId));
        assert("Global", XidUtils.isGlobal(globalId));
        assert("Global", !XidUtils.isLocal(globalId));

        LocalXid_BaseXidImpl local = new LocalXid_BaseXidImpl("");
        Xid localId = XidUtils.importXid(local.newBaseXid());
        assert("Local/Branch", XidUtils.isBranch(localId));
        assert("Local/Global", !XidUtils.isGlobal(localId));
        assert("Local", XidUtils.isLocal(localId));

        localId = XidUtils.newLocal();
        branchId = XidUtils.newBranch(localId);
        globalId = XidUtils.newGlobal();
        assert("Uniqueness1", localId.toString().compareTo(branchId.toString())
               != 0);
        assert("Uniqueness2", localId.toString().compareTo(globalId.toString())
               != 0);
        assert("Uniqueness3",
               globalId.toString().compareTo(branchId.toString()) != 0);
    }


    /**
     * <p>Bounds tests.  Ensure that illegal values result in the
     * correct exceptions being thrown.</p>
     *
     * @result All of the following calls with a null argument should
     * result in an IllegalArgumentException being thrown:
     * importXid(), newBranch(), isLocal(), isGlobal(), isBranch(),
     * toString() and parse().
     *
     * <p>These calls should result in an InvalidXidException being
     * thrown: call parse() with an argument that does not begin with
     * the XID_PREFIX, call parse() with an id whose transaction part
     * has an odd number of hexidecimal digits, calling it with an id
     * where the first part after the prefix equates to -1, calling it
     * with an id that is just a prefix, calling it with an id whose
     * global value too long, calling it with an id whose global value
     * has an odd number of hexidecimal digits, calling it with an id
     * whose branch value too long, calling with an id that contains
     * characters that are not dash ("-") or hexidecimal digits.</p>
     */

    public void testBounds()
        throws Exception
    {
        try
        {
            XidUtils.importXid(null);
            fail("Expected an exception to have been raised.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected - java.lang.IllegalArgumentException.
        }
        try
        {
            XidUtils.newBranch(null);
            fail("Expected an exception to have been raised.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected - java.lang.IllegalArgumentException.
        }
        try
        {
            XidUtils.isLocal(null);
            fail("Expected an exception to have been raised.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected - java.lang.IllegalArgumentException.
        }
        try
        {
            XidUtils.isGlobal(null);
            fail("Expected an exception to have been raised.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected - java.lang.IllegalArgumentException.
        }
        try
        {
            XidUtils.isBranch(null);
            fail("Expected an exception to have been raised.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected - java.lang.IllegalArgumentException.
        }
        try
        {
            XidUtils.toString(null);
            fail("Expected an exception to have been raised.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected - java.lang.IllegalArgumentException.
        }
        try
        {
            XidUtils.parse(null);
            fail("Expected an exception to have been raised.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected - java.lang.IllegalArgumentException.
        }
        try
        {
            XidUtils.parse("sjfhjdhf");
            fail("Expected an exception to have been raised.");
        }
        catch (InvalidXidException e)
        {
            // Expected - tyrex.tm.xid.InvalidXidException
            // tyrex.util.idInvalidPrefix.
        }
        try
        {
            XidUtils.parse(XidUtils.XID_PREFIX + "b");
            fail("Expected an exception to have been raised.");
        }
        catch (InvalidXidException e)
        {
            // Expected - tyrex.tm.xid.InvalidXidException
            // tyrex.util.idInvalidOddDigits.
        }
        try
        {
            XidUtils.parse(XidUtils.XID_PREFIX + "ff-63");
            fail("Expected an exception to have been raised.");
        }
        catch (InvalidXidException e)
        {
            // Expected - tyrex.tm.xid.InvalidXidException "Null
            // transaction".
        }
        try
        {
            XidUtils.parse(XidUtils.XID_PREFIX);
            fail("Expected an exception to have been raised.");
        }
        catch (InvalidXidException e)
        {
            // Expected - tyrex.tm.xid.InvalidXidException "Missing global".
        }
        try
        {
            XidUtils.parse(XidUtils.XID_PREFIX + "41-1a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc5-45");
            fail("Expected an exception to have been raised.");
        }
        catch (InvalidXidException e)
        {
            // Expected - tyrex.tm.xid.InvalidXidException "exceeded
            // maximum".
        }
        try
        {
            XidUtils.parse(XidUtils.XID_PREFIX + "41-1abcbc5");
            fail("Expected an exception to have been raised.");
        }
        catch (InvalidXidException e)
        {
            // Expected - tyrex.tm.xid.InvalidXidException
            // tyrex.util.idInvalidOddDigits.
        }
        try
        {
            XidUtils.parse(XidUtils.XID_PREFIX + "41-1a-3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc51a3bcbc5");
            fail("Expected an exception to have been raised.");
        }
        catch (InvalidXidException e)
        {
            // Expected - tyrex.tm.xid.InvalidXidException "Branch
            // qualifier exceeding maximum".
        }
        try
        {
            XidUtils.parse(XidUtils.XID_PREFIX + "41-1a-3bc");
            fail("Expected an exception to have been raised.");
        }
        catch (InvalidXidException e)
        {
            // Expected - tyrex.tm.xid.InvalidXidException
            // tyrex.util.idInvalidOddDigits.
        }
        try
        {
            XidUtils.parse(XidUtils.XID_PREFIX + "gh");
            fail("Expected an exception to have been raised.");
        }
        catch (InvalidXidException e)
        {
            // Expected - tyrex.tm.xid.InvalidXidException
            // tyrex.util.idInvalidCharacter.
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


    // Allow this test to be run on its own.
    public static void main(String args[])
    {
        tyrex.Unit.runTests(args, new TestSuite(XidUtilsTest.class));
    }
}
