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
 * $Id: BranchXidTest.java,v 1.3 2001/09/13 23:51:14 mills Exp $
 */

package tyrex.tm.xid;

import javax.transaction.xa.Xid;

import java.io.PrintWriter;

import junit.framework.*;
import junit.extensions.*;


/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.3 $
 */

public class BranchXidTest extends TestCase
{
    private PrintWriter _logger = null;

    public BranchXidTest(String name)
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
     * @result Create an instance.  Using its toString(),
     * getGlobalTransactionId() and getBranchQualifier() values create
     * a second instance.  Ensure that these are equal.  Create a
     * third instance.  Ensure it is not equal.  Ensure that the value
     * returned by getFormatId() is equal to BranchXid.FORMAT_ID and
     * that the value returned by getGlobalTransactionId() is equal to
     * the global value used in the constructor.
     *
     * <p>Create an Xid with XidUtils.parse() using the third
     * BranchXid as argument.  Ensure that the Xid returned is equal
     * to the BrancXid used.  Create an ExternalXid using the
     * attributes from the first BranchXid.  Ensure that the resulting
     * Xid is equal.</p>
     *
     * <p>Use TestXid to complete the coverage of equals().  Create
     * TestXids with a format id not equal to
     * GlobalXid.GLOBAL_FORMAT_ID, with a null global transaction id,
     * with a branch qualifyer that is different from the original,
     * with a null branch id and with a global transaction id that is
     * different from the original.  None of these should be equal to
     * the BranchXid.  Use a non-Xid Object as argument to equals.  It
     * should return false as well.</p> */

    public void testNonBaseXidFunctions()
        throws Exception
    {
        byte[] global = new byte[] {(byte)0xA8, (byte)0xB7, (byte)0xC6,
                                    (byte)0xD5, (byte)0xE4, (byte)0xF3};
        byte[] branch = new byte[] {(byte)0x9F, (byte)0x8E, (byte)0x7D,
                                    (byte)0x6C, (byte)0x5B, (byte)0x4A};
        BranchXid branchId1 = new BranchXid(global, branch);
        BranchXid branchId2 = new BranchXid(branchId1.toString(),
                                            branchId1.getGlobalTransactionId(),
                                            branchId1.getBranchQualifier());
        assert("Copy1", branchId1.equals(branchId2));
        BranchXid branchId3 = (BranchXid)branchId1.newBranch();
        assert("Copy2", !branchId1.equals(branchId3));
        assert("Copy3", !branchId2.equals(branchId3));
        assertEquals("Format id", GlobalXid.GLOBAL_FORMAT_ID,
                     branchId1.getFormatId());
        assertEquals("Global", global, branchId1.getGlobalTransactionId());
        Xid xid = XidUtils.parse(branchId3.toString());
        assert("Copy4", branchId3.equals(xid));
        ExternalXid exId = new ExternalXid(GlobalXid.GLOBAL_FORMAT_ID,
                                           branchId1.getGlobalTransactionId(),
                                           branchId1.getBranchQualifier());
        assert("Copy5", branchId1.equals(exId));
        TestXid tId = new TestXid(GlobalXid.GLOBAL_FORMAT_ID + 1,
                                  branchId1.getGlobalTransactionId(),
                                  branchId1.getBranchQualifier());
        assert("Copy6", !branchId1.equals(tId));
        tId = new TestXid(GlobalXid.GLOBAL_FORMAT_ID, null,
                          branchId1.getBranchQualifier());
        assert("Copy6", !branchId1.equals(tId));
        byte[] global1 = new byte[] {(byte)0xA8, (byte)0xB7, (byte)0xC6,
                                     (byte)0xD5, (byte)0xE4, (byte)0x45};
        tId = new TestXid(GlobalXid.GLOBAL_FORMAT_ID, global1,
                          branchId1.getBranchQualifier());
        assert("Copy6", !branchId1.equals(tId));
        tId = new TestXid(GlobalXid.GLOBAL_FORMAT_ID,
                          branchId1.getGlobalTransactionId(), null);
        assert("Copy6", !branchId1.equals(tId));
        byte[] branch1 = new byte[] {(byte)0x9F, (byte)0x8E, (byte)0x7D,
                                     (byte)0x6C, (byte)0x5B, (byte)0x49};
        tId = new TestXid(GlobalXid.GLOBAL_FORMAT_ID,
                          branchId1.getGlobalTransactionId(), branch1);
        assert("Copy6", !branchId1.equals(tId));
        assert("Copy6", !branchId1.equals(new Integer(1)));
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
        TestSuite suite = new TestSuite(BranchXidTest.class);
        suite.addTest(new TestSuite(BranchXid_BaseXidImpl.class));
        return suite;
    }


    // Allow this test to be run on its own.
    public static void main(String args[])
    {
        tyrex.Unit.runTests(args, suite());
    }
}
