/**
* Copyright (C) 2001, Intalio Inc.
*
* The program(s) herein may be used and/or copied only with
* the written permission of Intalio Inc. or in accordance with
* the terms and conditions stipulated in the agreement/contract
* under which the program(s) have been supplied.
*
* $Id: EnvContextTest.java,v 1.2 2001/07/31 02:08:03 mills Exp $
* Date        Author    Changes
*
* 2001/07/26  Mills     Created
*
*/


package tyrex.naming;

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import tyrex.tm.RuntimeContext;
import tyrex.naming.EnvContext;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.PrintWriter;


/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.2 $
 */

public class EnvContextTest extends TestCase
{
    private PrintWriter _logger = null;

    public EnvContextTest(String name)
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


    public void testNone()
    {
        // Empty.
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
        TestSuite suite = new TestSuite(EnvContextTest.class);
        suite.addTest(new TestSuite(EnvContext_ContextImpl.class));
        return suite;
    }


    // Allow this test to be run on its own.
    public static void main(String args[])
    {
        for( int i = 0 ; i < args.length ; i++ )
            if ( args[ i ].equals( "-verbose" ) )
                VerboseStream.verbose = true;
        junit.textui.TestRunner.run(new TestSuite(EnvContextTest.class));
    }
}
