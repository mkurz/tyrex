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
 * $Id: Lock.java,v 1.2 2001/03/22 20:27:29 arkin Exp $
 */


package lock;

import tests.*;

import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;

import junit.framework.*;

import tyrex.lock.LockSet;
import tyrex.lock.LockMode;
import tyrex.lock.LockManager;
import tyrex.lock.LockNotHeldException;
import tyrex.lock.LockTimeoutException;



public class Lock extends TestSuite
{


    public Lock( String name )
    {
        super( name);
        
        TestCase tc;
        
        tc = new LockSetTest();
        addTest( tc );
        tc = new DeadlockTest();
        addTest( tc );
    }


    /**
     * LockSet test
     * 
     */
    public static class LockSetTest
        extends TestCase
    {

        public LockSetTest()
        {
            super( "[TC01] LockSet Test" );
        }

        /**
         * Main test method - TC01
         *  - lockset test
         *
         */
        public void runTest()
        {
            LockSet        lockSet;
            AcquireThread  acquire;
            
            VerboseStream stream = new VerboseStream();

            try {
                lockSet = LockManager.create();

                // Test acquiring read lock and two write locks
                try {
                    lockSet.lock( LockMode.READ );
                    stream.writeVerbose( "Acquired read lock" );
                } catch ( LockTimeoutException except ) {
                    fail( "Error: Failed to acquire read lock" );
                }
                try {
                    lockSet.lock( LockMode.WRITE );
                    stream.writeVerbose( "Acquired 1st write lock, same thread" );
                } catch ( LockTimeoutException except ) {
                    fail( "Error: Failed to acquire write lock" );
                }
                try {
                    lockSet.lock( LockMode.WRITE );
                    stream.writeVerbose( "Acquired 2nd write lock, same thread" );
                } catch ( LockTimeoutException except ) {
                    fail( "Error: Failed to acquire write lock" );
                }

                // Test acquiring write lock, different thread
                stream.writeVerbose( "Attempt to acquire write lock, different thread" );
                acquire = new AcquireThread( lockSet, LockMode.WRITE );
                acquire.start();
                acquire.join( 100 );
                if ( acquire.result() ) {
                    fail( "Error: Other thread managed to acquire write lock (1)" );
            
                } else
                    stream.writeVerbose( "OK: Other thread failed to acquire write lock" );

                // Releasing both write locks, second thread can't obtain lock yet
                try {
                    lockSet.unlock( LockMode.WRITE );
                } catch ( LockNotHeldException except ) {
                    fail( "Error: Write lock not held" );
         
                }
                stream.writeVerbose( "Released 2nd write lock" );
                acquire.join( 100 );
                if ( acquire.result() ) {
                    fail( "Error: Other thread managed to acquire write lock (2)" );
            
                } else
                    stream.writeVerbose( "OK: Other thread couldn't acquire lock" );
                try {
                    lockSet.unlock( LockMode.WRITE );
                } catch ( LockNotHeldException except ) {
                    fail( "Error: Write lock not held" );
               
                }
                stream.writeVerbose( "Released 1st write lock" );
                acquire.join( 100 );
                if ( acquire.result() ) {
                    fail( "Error: Second thread managed to acquire write lock (3)" );
          
                } else
                    stream.writeVerbose( "OK: Other thread couldn't acquire lock" );

                // Release read lock, other thread can acquire write lock
                try {
                    lockSet.unlock( LockMode.READ );
                } catch ( LockNotHeldException except ) {
                    fail( "Error: Read lock not held" );
          
                }
                stream.writeVerbose( "Released read lock" );
                acquire.join( 100 );
                if ( ! acquire.result() ) {
                    fail( "Error: Second thread could not manage to acquire write lock" );
         
                }
                stream.writeVerbose( "Other thread acquired write lock" );

                // Make sure this thread cannot acquire a read lock
                try {
                    lockSet.lock( LockMode.READ );
                    fail( "Error: This thread managed to acquire read lock" );
                } catch ( LockTimeoutException except ) {
                    stream.writeVerbose( "OK: This thread failed to acquire write lock" );
                }
        
            } catch ( InterruptedException except ) {
                System.out.println( except );
 
            }
        }


    }


    static class AcquireThread
        extends Thread
    {
        
        LockSet  lockSet;
        
        int       mode;
        
        boolean  result;
        
        AcquireThread( LockSet lockSet, int mode )
        {
            this.lockSet = lockSet;
            this.mode = mode;
            result = false;
        }

        public void run()
        {
            try {
                lockSet.lock( mode, Integer.MAX_VALUE );
                result = true;
            } catch ( LockTimeoutException except ) { }
        }

        boolean result()
        {
            return result;
        }

    }
 
    /**
     * Deadlock detection test
     */
    public static class DeadlockTest
        extends TestCase
    {

        public DeadlockTest()
        {
            super( "[TC02] Deadlock Detection Test" );
        }
        
        
        /**
         * Main test method - TC02
         *  - deadlock detection test
         *
         */
        public void runTest()
        {
            LockSet        lockSet1;
            LockSet        lockSet2;
            LockSet        parent;
            DeadlockThread deadlock;
            
            VerboseStream stream = new VerboseStream();

            try {
                parent = LockManager.create();
                lockSet1 = LockManager.createRelated( null, parent );
                lockSet2 = LockManager.createRelated( null, parent );

                deadlock = new DeadlockThread( parent, lockSet1, lockSet2, stream );
                deadlock.start();
                
                try {
                    lockSet1.lock( LockMode.READ );
                    lockSet2.lock( LockMode.READ );
                    stream.writeVerbose( "Main: Acquired read locks" );
                } catch ( LockTimeoutException except ) {
                    fail( "Error: Main: Could not acquired read locks" );
                }
                    
                Thread.currentThread().sleep( 500 );

                try {
                    lockSet1.lock( LockMode.UPGRADE );
                    stream.writeVerbose( "Main: Acquired upgrade lock (1)" );
                } catch ( LockTimeoutException except ) {
                    fail( "Error: Main: Could not acquired upgrade lock (1)" );
                    parent.getCoordinator().dropLocks();
                    deadlock.join();
                }
                Thread.currentThread().sleep( 500 );

                try {
                    lockSet2.lock( LockMode.UPGRADE );
                    fail( "Error: Maing: Acquired upgrade lock (2)" );
                    parent.getCoordinator().dropLocks();
                    deadlock.join();
                } catch ( LockTimeoutException except ) {
                    stream.writeVerbose( "OK: Main: Could not acquired upgrade lock (2) -- deadlock detected" );
                    parent.getCoordinator().dropLocks();
                    deadlock.join();
                }

            } catch ( InterruptedException except ) {
                System.out.println( except );
            }
        }


    }


    static class DeadlockThread
        extends Thread
    {
        
        LockSet         parent;

        LockSet         lockSet1;

        LockSet         lockSet2;

        VerboseStream stream;
        
        boolean         result;
        
        DeadlockThread( LockSet parent, LockSet lockSet1, LockSet lockSet2, VerboseStream stream )
        {
            this.parent = parent;
            this.lockSet1 = lockSet1;
            this.lockSet2 = lockSet2;
            this.stream = stream;
            this.result = false;
        }

        public void run()
        {
            try {
                sleep( 100 );

                try {
                    lockSet1.lock( LockMode.READ );
                    lockSet2.lock( LockMode.READ );
                    stream.writeVerbose( "Second: Acquired read locks" );
                } catch ( LockTimeoutException except ) {
                    stream.writeVerbose( "Error: Main: Could not acquired read locks" );
                }

                sleep( 500 );

                try {
                    lockSet2.lock( LockMode.UPGRADE );
                    stream.writeVerbose( "Second: Acquired upgrade lock (2)" );
                } catch ( LockTimeoutException except ) {
                    stream.writeVerbose( "Error: Second: Could not acquired upgrade lock (2)" );
                    parent.getCoordinator().dropLocks();
                    return;
                }
                sleep( 500 );

                try {
                    lockSet1.lock( LockMode.UPGRADE );
                    stream.writeVerbose( "Second: Acquired upgrade lock (1)" );
                } catch ( LockTimeoutException except ) {
                    stream.writeVerbose( "Error: Second: Could not acquired upgrade lock (1)" );
                    parent.getCoordinator().dropLocks();
                    return;
                }

                sleep( 500 );

                try {
                    lockSet2.changeMode( LockMode.READ, LockMode.WRITE );
                    lockSet2.unlock( LockMode.UPGRADE );
                } catch ( LockTimeoutException except ) {
                    stream.writeVerbose( "Error: Could not acquire write lock (2)" );
                    parent.getCoordinator().dropLocks();
                    return;
                } catch ( LockNotHeldException except ) {
                    stream.writeVerbose( "Error: Second: Read lock not held (2)" );
                    parent.getCoordinator().dropLocks();
                    return;
                }
                stream.writeVerbose( "Second: Upgraded to write lock (2)" );

                try {
                    lockSet1.changeMode( LockMode.READ, LockMode.WRITE );
                    lockSet1.unlock( LockMode.UPGRADE );
                } catch ( LockTimeoutException except ) {
                    stream.writeVerbose( "Error: Could not acquire write lock (1)" );
                    parent.getCoordinator().dropLocks();
                    return;
                } catch ( LockNotHeldException except ) {
                    stream.writeVerbose( "Error: Second: Read lock not held (1)" );
                    parent.getCoordinator().dropLocks();
                    return;
                }
                stream.writeVerbose( "Second: Upgraded to write lock (1)" );

                result = true;
            }  catch ( InterruptedException except ) {
                System.out.println( except );
            }
        }

        boolean result()
        {
            return result;
        }

    }

    
}
