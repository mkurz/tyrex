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
 * $Id: GlobalXidTest.java,v 1.3 2001/09/13 23:51:14 mills Exp $
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

public class GlobalXidTest extends TestCase
{
    private PrintWriter _logger = null;

    public GlobalXidTest(String name)
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
     * @result Create an instance.  Using its toString() and
     * getGlobalTransactionId() values create a second instance.
     * Ensure that these are equal.  Create a third instance.  Ensure
     * it is not equal.  Ensure that the value returned by
     * getFormatId() is equal to GlobalXid.FORMAT_ID and that the
     * value returned by getGlobalTransactionId() is equal to the
     * global value used in the constructor.  Ensure that the value
     * returned by getBranchQualifier() is equal to an empty byte
     * array.
     *
     * <p>Create an Xid using XidUtils.parse() and the fourth
     * GlobalXid.  Ensure that the id returned is equal to the id used
     * as argument in parse().  Create an ExternalXid using the
     * attributes from the first GlobalXid.  Ensure that this
     * ExternalXid is equal to the GlobalXid.</p>
     *
     * <p>Use TestXid to complete the coverage of equals().  Create
     * TestXids with a format id not equal to
     * GlobalXid.GLOBAL_FORMAT_ID, with a null global transaction id,
     * with a branch qualifyer that is neither null nor empty, with a
     * global transaction id that is different from the original.
     * None of these should be equal to the GloablXid.  Use a non-Xid
     * Object as argument to equals.  It should return false as
     * well.</p>
     */

    public void testNonBaseXidFunctions()
        throws Exception
    {
        byte[] global = new byte[] {(byte)0xA8, (byte)0xB7, (byte)0xC6,
                                    (byte)0xD5, (byte)0xE4, (byte)0xF3};
        GlobalXid globalId1 = new GlobalXid();
        GlobalXid globalId2
            = new GlobalXid(globalId1.toString(),
                            globalId1.getGlobalTransactionId());
        assert("Copy1", globalId1.equals(globalId2));
        GlobalXid globalId3 = new GlobalXid(globalId1.toString(), global);
        BranchXid globalId4 = (BranchXid)globalId1.newBranch();
        assert("Copy2", !globalId1.equals(globalId4));
        assert("Copy3", !globalId2.equals(globalId4));
        assertEquals("Format id", GlobalXid.GLOBAL_FORMAT_ID,
                     globalId3.getFormatId());
        assertEquals("Global", global, globalId3.getGlobalTransactionId());
        byte[] glb = globalId3.getBranchQualifier();
        assertEquals("Branch", BaseXid.EMPTY_ARRAY, glb);
        Xid xid = XidUtils.parse(globalId4.toString());
        assert("Copy4", !globalId1.equals(xid));
        assert("Copy5", globalId4.equals(xid));
        ExternalXid exId = new ExternalXid(GlobalXid.GLOBAL_FORMAT_ID,
                                           globalId1.getGlobalTransactionId(),
                                           globalId1.getBranchQualifier());
        assert("Copy6", globalId1.equals(exId));
        TestXid tId = new TestXid(GlobalXid.GLOBAL_FORMAT_ID,
                                  globalId1.getGlobalTransactionId(),
                                  globalId1.getBranchQualifier());
        assert("Copy7", globalId1.equals(tId));
        tId = new TestXid(GlobalXid.GLOBAL_FORMAT_ID + 1,
                          globalId1.getGlobalTransactionId(),
                          globalId1.getBranchQualifier());
        assert("Copy8", !globalId1.equals(tId));
        tId = new TestXid(GlobalXid.GLOBAL_FORMAT_ID, null,
                          globalId1.getBranchQualifier());
        assert("Copy9", !globalId1.equals(tId));
        byte[] branch1 = new byte[] {(byte)0xA8, (byte)0xB7, (byte)0xC6,
                                     (byte)0xD5, (byte)0xE4, (byte)0x43};
        tId = new TestXid(GlobalXid.GLOBAL_FORMAT_ID, null, branch1);
        assert("Copy9", !globalId1.equals(tId));
        byte[] global1 = new byte[] {(byte)0xA8, (byte)0xB7, (byte)0xC6,
                                     (byte)0xD5, (byte)0xE4, (byte)0x43};
        tId = new TestXid(GlobalXid.GLOBAL_FORMAT_ID, global1,
                          globalId1.getBranchQualifier());
        assert("Copy10", !globalId1.equals(tId));
        tId = new TestXid(GlobalXid.GLOBAL_FORMAT_ID,
                          globalId1.getGlobalTransactionId(), null);
        assert("Copy11", globalId1.equals(tId));
        assert("Copy12", !globalId1.equals(new Integer(1)));
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
        TestSuite suite = new TestSuite(GlobalXidTest.class);
        suite.addTest(new TestSuite(GlobalXid_BaseXidImpl.class));
        return suite;
    }


    // Allow this test to be run on its own.
    public static void main(String args[])
    {
        tyrex.Unit.runTests(args, suite());
    }
}
