/**
* Copyright (C) 2001, Intalio Inc.
*
* The program(s) herein may be used and/or copied only with
* the written permission of Intalio Inc. or in accordance with
* the terms and conditions stipulated in the agreement/contract
* under which the program(s) have been supplied.
*
* $Id: UnitNames.java,v 1.1 2001/07/31 02:08:01 mills Exp $
* Date        Author    Changes
*
* 2001/07/31  Mills     Created
*
*/

package tyrex;

import junit.framework.*;
import junit.extensions.*;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;


/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.1 $
 */

public class UnitNames
{
    public UnitNames()
    {
        // Empty.
    }


    public static void main(String args[])
    {
        TestSuite suite = Unit.suite();
        dumpTests(suite);
    }

    public static void dumpTests(TestSuite suite)
    {
        boolean printed = false;
        Enumeration tests = suite.tests();
        while (tests.hasMoreElements())
        {
            Test test = (Test)tests.nextElement();
            if (test instanceof TestSuite)
            {
                dumpTests((TestSuite)test);
            }
            else if (!printed)
            {
                System.out.println(test.getClass().getName());
                printed = true;
            }
        }
    }
}
