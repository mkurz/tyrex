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
 * $Id: TestHarness.java,v 1.14 2001/04/24 20:14:07 psq Exp $
 */

package tests;

import java.util.Vector;
import java.util.Enumeration;

import junit.framework.*;

//import lock.Lock;
import naming.Naming;

import jdbc.XADataSourceTestSuite;
import transaction.Transaction;


/**
 * Test harness.
 */
public class TestHarness
{

    static public void main( String args[] )
    {
        try {
            // define all the test suites
            TestSuite main = new TestSuite("Tyrex Test Harness");            
            TestSuite naming = new Naming( "JNDI service provider" );
            if ( args.length != 1 )
            {
               System.out.println("Specify the tyrex configuration file name and path as argument to start the tests...");
               System.exit(0);
            }
            TestSuite jdbc = new XADataSourceTestSuite( "XADataSource test", args[0] );
            TestSuite transaction = new Transaction( "Transaction tests", args[0] );
           
            // set up the lock test suite
            //for( java.util.Enumeration e = lock.tests(); e.hasMoreElements(); )
            // main.addTest( (Test)e.nextElement());
            
            // set up the naming test suite
            for( java.util.Enumeration e = naming.tests(); e.hasMoreElements(); )
             main.addTest( (Test)e.nextElement());
            
           
            // set up the jdbc test suite
            for( java.util.Enumeration e = jdbc.tests(); e.hasMoreElements(); )
            main.addTest( (Test)e.nextElement());
             
            // set up the transaction test suite
            for( java.util.Enumeration e = transaction.tests(); e.hasMoreElements(); )
            main.addTest( (Test)e.nextElement());
           
            
            // Set up the verbose mode
            for(int i=0;i<args.length;i++) if(args[i].equals("-verbose")) VerboseStream.verbose=true;
          
            junit.textui.TestRunner.run(main);
                                                   

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    System.exit(0);
    }


}
