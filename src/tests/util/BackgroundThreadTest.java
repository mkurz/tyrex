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
 * $Id: BackgroundThreadTest.java,v 1.2 2001/02/23 17:17:43 omodica Exp $
 */

package util;

import tests.*;

import java.util.Enumeration;
import java.util.Vector;

import junit.framework.*;

import tyrex.util.BackgroundThread;

/**
 * Background thread test
 */ 
public class BackgroundThreadTest
    extends TestSuite
{
    public BackgroundThreadTest( String name )
    {
        super( name );
        
        TestCase tc;
        
        tc = new ATestCase();
        addTest( tc );
    }

    private class ATestCase
        extends TestCase
    {
        /**
         * Create TestCase.
         *
         */
        private ATestCase()
        {

            super( "[TC01] BackgroundThread Test" );
        }
    
         
        /**
         * Main test method - TC01
         *  - background thread test
         *
         */
        public void runTest()
        {
           VerboseStream stream = new VerboseStream();
         
            BackgroundThreadTestRunnable runnable = new BackgroundThreadTestRunnable();
            BackgroundThread thread = new BackgroundThread(runnable, 100);
            runnable.setThread(thread);
            thread.setDaemon(true);
            thread.start();

            try {
                Thread.currentThread().sleep(1000);
            }
            catch (InterruptedException e) {
            }

            if (!thread.isAlive()) {
                fail( "Error: Thread is not alive." );    
            }

            runnable = null;

            System.gc();

            try {
                Thread.currentThread().sleep(1000);
            }
            catch (InterruptedException e) {
            }

            if (thread.isAlive()) {
                thread.stop();

                fail( "Error: Thread was still alive." );
            }
        }
        
    }

    private static final class BackgroundThreadTestRunnable 
        implements Runnable
    {
        private Thread _thread;

        /**
         * Create the BackgroundThreadTestRunnable
         *
         */
        BackgroundThreadTestRunnable()
        {
        }
    
        private void setThread(Thread thread)
        {
            _thread = thread;
        }

        /**
         * 
         */
        public void run()
        {
            //System.out.println("running2");
        }
    }
    
}




