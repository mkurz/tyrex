/**
* Copyright (C) 2001, Intalio Inc.
*
* The program(s) herein may be used and/or copied only with
* the written permission of Intalio Inc. or in accordance with
* the terms and conditions stipulated in the agreement/contract
* under which the program(s) have been supplied.
*
* $Id: Unit.java,v 1.1 2001/07/31 02:08:01 mills Exp $
* Date        Author    Changes
*
* 2001/07/31  Mills     Created
*
*/

package tyrex;

import tyrex.naming.NamingSuite;

import junit.framework.*;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.FileInputStream;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;


/**
 * Main entry class for test cases execution.
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.1 $
 */

public class Unit
{
    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite("Tyrex Unit Test Harness");
        suite.addTest(NamingSuite.suite());
        return suite;
    }


    public static void main(String args[])
    {
        junit.textui.TestRunner.run(Unit.suite());
    }
}
