/**
* Copyright (C) 2001, Intalio Inc.
*
* The program(s) herein may be used and/or copied only with
* the written permission of Intalio Inc. or in accordance with
* the terms and conditions stipulated in the agreement/contract
* under which the program(s) have been supplied.
*
* $Id: NamingSuite.java,v 1.1 2001/07/31 01:06:50 mills Exp $
* Date        Author    Changes
*
* 2001/07/26  Mills     Created
*
*/


package tyrex.naming;

import junit.framework.TestSuite;


/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.1 $
 */

public class NamingSuite
{
    public NamingSuite()
    {
        // Empty.
    }

    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite("NamingSuite test harness");
        suite.addTest(EnvContextTest.suite());
        suite.addTest(MemoryContextTest.suite());
        suite.addTest(new Naming("Naming tests"));
        return suite;
    }


    public static void main(String args[])
    {
        junit.textui.TestRunner.run(NamingSuite.suite());
    }
}
