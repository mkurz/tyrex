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
 * $Id: LocalXidTest.java,v 1.3 2001/09/12 11:17:52 mills Exp $
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

public class LocalXidTest extends TestCase
{
    private PrintWriter _logger = null;

    public LocalXidTest(String name)
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
     * getBranchQualifier() values create a second instance.  Ensure
     * that these are equal.  Create a third instance.  Ensure it is
     * not equal.  Ensure that the value returned by getFormatId() is
     * equal to LocalXid.LOCAL_FORMAT_ID and that the value returned
     * by getGlobalTransactionId() is equal to an empty byte array.
     *
     * <p>Create an ExternalXid using the attributes from the first
     * LocalXid.  Because an ExternalXid cannot be created with a null
     * or zero length global value the ExternalXid won't be equal to
     * the first LocalXid but using it in an equals call will further
     * extend the test coverage of the method equals().</p>
     */

    public void testNonBaseXidFunctions()
        throws Exception
    {
        LocalXid localId1 = new LocalXid();
        LocalXid localId2 = new LocalXid(localId1.toString(),
                                         localId1.getBranchQualifier());
        assert("Copy1", localId1.equals(localId2));
        LocalXid localId3 = (LocalXid)localId1.newBranch();
        assert("Copy2", !localId1.equals(localId3));
        assert("Copy3", !localId2.equals(localId3));
        assertEquals("Format id", LocalXid.LOCAL_FORMAT_ID,
                     localId1.getFormatId());
        assertEquals(BaseXid.EMPTY_ARRAY, localId1.getGlobalTransactionId());
        Xid xid = XidUtils.parse(localId3.toString());
        byte[] global = new byte[1];
        global[0] = 66;
        ExternalXid exId = new ExternalXid(LocalXid.LOCAL_FORMAT_ID,
                                           global,
                                           localId1.getBranchQualifier());
        assert("Copy4", !localId1.equals(exId));
        assert("Copy5", !localId1.equals(xid));
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
        TestSuite suite = new TestSuite(LocalXidTest.class);
        suite.addTest(new TestSuite(LocalXid_BaseXidImpl.class));
        return suite;
    }


    // Allow this test to be run on its own.
    public static void main(String args[])
    {
        tyrex.Unit.runTests(args, suite());
    }
}
