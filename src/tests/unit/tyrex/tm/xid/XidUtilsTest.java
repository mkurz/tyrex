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
 * $Id: XidUtilsTest.java,v 1.2 2001/09/08 05:05:31 mills Exp $
 */


package tyrex.tm.xid;

import javax.transaction.xa.Xid;

import java.io.PrintWriter;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.2 $
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
     * global Xid-s.  Use each in the is<Type>() calls.  Only the
     * relevent ones should return true.
     *
     * <p>Call each of the new<Type>() methods.  Ensure that the Xid-s
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
        assert("Uniqueness", localId.toString().compareTo(branchId.toString())
               != 0);
        assert("Uniqueness", localId.toString().compareTo(globalId.toString())
               != 0);
        assert("Uniqueness", globalId.toString().compareTo(branchId.toString())
               != 0);
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
