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
 * $Id: ExternalXidTest.java,v 1.4 2001/11/12 02:50:45 mills Exp $
 */

package tyrex.tm.xid;

import javax.transaction.xa.Xid;

import java.io.PrintWriter;

import junit.framework.*;
import junit.extensions.*;


/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.4 $
 */

public class ExternalXidTest extends TestCase
{
    private PrintWriter _logger = null;

    public ExternalXidTest(String name)
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
     * <p>Test the aspects of the class that do not relate to
     * BaseXid.</p>
     *
     * @result Create an instance.  Using its getFormatId(),
     * getGlobalTransactionId() and getBranchQualifier() values create
     * a second instance.  Ensure that these are equal.  Create a
     * third instance using the first as argument.  Ensure that this
     * is also equal.  Create a fourth using toString() from the first
     * and otherwise the same arguments as the first.  Ensure that
     * this is also equal.
     *
     * <p>Call newBranch() on the first.  The xid returned should not
     * be equal.  Ensure that the value returned by getFormatId() is
     * equal to BaseXid.FORMAT_ID and that the values returned by
     * getGlobalTransactionId() and getBranchQualifier() are equal to
     * the values used in the constructor.</p>
     *
     * <p>Create an Xid using XidUtils.parse().  Ensure that the id
     * returned is equal to the one used as argument in the call.</p>
     *
     * <p>Use TestXid to complete the coverage of equals().  Create
     * TestXids with a format id not equal to BaseXid.FORMAT_ID, with
     * a null global transaction id, with a branch qualifyer that is
     * neither null nor empty, with a global transaction id that is
     * different from the original.  None of these should be equal to
     * the GloablXid.  Use a non-Xid Object as argument to equals.  It
     * should return false as well.</p>
     */

    public void testNonBaseXidFunctions()
        throws Exception
    {
        byte[] global = new byte[] {(byte)0xA8, (byte)0xB7, (byte)0xC6,
                                    (byte)0xD5, (byte)0xE4, (byte)0xF3};
        byte[] branch = new byte[] {(byte)0x9F, (byte)0x8E, (byte)0x7D,
                                    (byte)0x6C, (byte)0x5B, (byte)0x4A};
        ExternalXid extId1 = new ExternalXid(BaseXid.FORMAT_ID, global,
                                             branch);
        ExternalXid extId2 = new ExternalXid(extId1.getFormatId(),
                                             extId1.getGlobalTransactionId(),
                                             extId1.getBranchQualifier());
        ExternalXid extId3 = new ExternalXid(extId1);
        ExternalXid extId4 = new ExternalXid(extId1.toString(),
                                             BaseXid.FORMAT_ID, global,
                                             branch);
        assert("Copy1", extId1.equals(extId2));
        assert("Copy2", extId1.equals(extId3));
        assert("Copy3", extId1.equals(extId4));
        ExternalXid extId5 = (ExternalXid)extId1.newBranch();
        assert("Copy4", !extId1.equals(extId5));
        assertEquals("Format id", BaseXid.FORMAT_ID, extId1.getFormatId());
        assertEquals("Transaction id", global,
                     extId1.getGlobalTransactionId());
        assertEquals("Branch id", branch, extId1.getBranchQualifier());
        Xid xid = XidUtils.parse(extId1.toString());
        assert("Equals", extId1.equals(xid));
        TestXid tId = new TestXid(ExternalXid.FORMAT_ID,
                                  extId1.getGlobalTransactionId(),
                                  extId1.getBranchQualifier());
        assert("Copy7", extId1.equals(tId));
        tId = new TestXid(ExternalXid.FORMAT_ID + 1,
                          extId1.getGlobalTransactionId(),
                          extId1.getBranchQualifier());
        assert("Copy8", !extId1.equals(tId));
        tId = new TestXid(ExternalXid.FORMAT_ID, null,
                          extId1.getBranchQualifier());
        assert("Copy9", !extId1.equals(tId));
        byte[] branch1 = new byte[] {(byte)0xA8, (byte)0xB7, (byte)0xC6,
                                     (byte)0xD5, (byte)0xE4, (byte)0x43};
        tId = new TestXid(ExternalXid.FORMAT_ID, null, branch1);
        assert("Copy9", !extId1.equals(tId));
        byte[] global1 = new byte[] {(byte)0xA8, (byte)0xB7, (byte)0xC6,
                                     (byte)0xD5, (byte)0xE4, (byte)0x43};
        tId = new TestXid(ExternalXid.FORMAT_ID, global1,
                          extId1.getBranchQualifier());
        assert("Copy10", !extId1.equals(tId));
        tId = new TestXid(ExternalXid.FORMAT_ID,
                          extId1.getGlobalTransactionId(), null);
        assert("Copy11", !extId1.equals(tId));
        assert("Copy12", !extId1.equals(new Integer(1)));
    }


    /**
     * <p>Bounds tests.  Ensure that illegal values result in the
     * correct exceptions being thrown.</p>
     *
     * @result Call the constructor with a -1 format id or a null or
     * empty byte array global value.  It should throw an
     * IllegalArgumentException.
     */

    public void testBounds()
        throws Exception
    {
        byte[] global = new byte[] {(byte)0xA8, (byte)0xB7, (byte)0xC6,
                                    (byte)0xD5, (byte)0xE4, (byte)0xF3};
        byte[] branch = new byte[] {(byte)0x9F, (byte)0x8E, (byte)0x7D,
                                    (byte)0x6C, (byte)0x5B, (byte)0x4A};
        try
        {
            ExternalXid extId1 = new ExternalXid(-1, global, branch);
            fail("Expected an exception to have been raised.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected.
        }
        try
        {
            ExternalXid extId1 = new ExternalXid(BaseXid.FORMAT_ID, null,
                                                 branch);
            fail("Expected an exception to have been raised.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected.
        }
        try
        {
            ExternalXid extId1 = new ExternalXid(BaseXid.FORMAT_ID,
                                                 BaseXid.EMPTY_ARRAY,
                                                 branch);
            fail("Expected an exception to have been raised.");
        }
        catch (IllegalArgumentException e)
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


    // Compile the test suite.
    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite(ExternalXidTest.class);
        suite.addTest(new TestSuite(ExternalXid_BaseXidImpl.class));
        return suite;
    }


    // Allow this test to be run on its own.
    public static void main(String args[])
    {
        tyrex.Unit.runTests(args, suite());
    }
}
