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
 * $Id: Unit.java,v 1.8 2001/10/31 03:06:25 mills Exp $
 */

package tyrex;

import tyrex.naming.NamingSuite;
import tyrex.resource.ResourceUnit;
import tyrex.services.ServicesSuite;
import tyrex.tm.TmUnit;
import tyrex.util.UtilSuite;

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
 * @version $Revision: 1.8 $
 */

public class Unit
{
    protected final static char[]  HEX_DIGITS
    = new char[] {'0', '1', '2', '3', '4', '5', '6', '7',
                  '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static void runTests(String args[], TestSuite suite)
    {
        Class[] classes = new Class[1];
        classes[0] = Test.class;
        java.lang.reflect.Method method = null;
        try
        {
            if (args.length == 1)
            {
                try
                {
                    Class cls = Class.forName(args[0]);
                    method = cls.getMethod("run", classes);
                }
                catch (ClassNotFoundException e)
                {
                    // OK, runner not found.  The default will be used.
                    System.out.println("Couldn't find different runner.");
                }
            }
            if (method == null)
            {
                method = junit.textui.TestRunner.class
                    .getMethod("run", classes);
            }
            Object[] methodArgs = new Object[1];
            methodArgs[0] = suite;
            method.invoke(null, methodArgs);
        }
        catch (Exception e)
        {
            // OK, just don't run the tests.
            System.out.println("Failure to run tests.");
            e.printStackTrace();
            System.exit(1);
        }
    }


    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite("Tyrex Unit Test Harness");
        suite.addTest(NamingSuite.suite());
        suite.addTest(ResourceUnit.suite());
        suite.addTest(ServicesSuite.suite());
        suite.addTest(TmUnit.suite());
        suite.addTest(UtilSuite.suite());
        return suite;
    }


    public static void main(String args[])
    {
        runTests(args, Unit.suite());
    }

    public static String byteArrayToString(byte[] bytes)
    {
        StringBuffer buffer = new StringBuffer(bytes.length * 2);
        for ( int i = bytes.length ; i-- > 0 ; ) {
            buffer.append(HEX_DIGITS[(bytes[i] & 0xF0) >> 4]);
            buffer.append(HEX_DIGITS[(bytes[i] & 0x0F)]);
        }
        return buffer.toString();
    }
}
