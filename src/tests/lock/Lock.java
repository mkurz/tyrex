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
 * $Id: Lock.java,v 1.4 2001/03/23 23:50:02 arkin Exp $
 */


package lock;

import tests.*;

import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import javax.transaction.TransactionManager;

import junit.framework.*;

import tyrex.lock.LockSet;
import tyrex.lock.LockMode;
import tyrex.lock.LockManager;
import tyrex.lock.LockNotHeldException;
import tyrex.lock.LockNotGrantedException;
import tyrex.tm.TransactionDomain;



public class Lock extends TestSuite
{


    static public void main( String args[] )
    {
        try {
            TestSuite lock = new Lock( "Lock service tests" );
            for(int i=0;i<args.length;i++) if(args[i].equals("-verbose")) VerboseStream.verbose=true;
            junit.textui.TestRunner.run(lock);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    public Lock( String name )
    {
        super( name );
        /*
        addTest( new LockSetTest() );
        addTest( new UpgradeTest() );
        */
        addTest( new DeadlockTest() );
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
                } catch ( LockNotGrantedException except ) {
                    fail( "Error: Failed to acquire read lock" );
                }
                try {
                    lockSet.lock( LockMode.WRITE );
                    stream.writeVerbose( "Acquired 1st write lock, same thread" );
                } catch ( LockNotGrantedException except ) {
                    fail( "Error: Failed to acquire write lock" );
                }
                try {
                    lockSet.lock( LockMode.WRITE );
                    stream.writeVerbose( "Acquired 2nd write lock, same thread" );
                } catch ( LockNotGrantedException except ) {
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
                } catch ( LockNotGrantedException except ) {
                    stream.writeVerbose( "OK: This thread failed to acquire write lock" );
                }
        
            } catch ( InterruptedException except ) {
                System.out.println( except );
 
            }
        }


        
        class AcquireThread
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
                } catch ( LockNotGrantedException except ) {
                    fail( "Error: Other thread failed to acquire lock" );
                }
            }
            
            boolean result()
            {
                return result;
            }

        }

    }
 

    /**
     * Deadlock prevention test
     */
    public static class UpgradeTest
        extends TestCase
    {

        public UpgradeTest()
        {
            super( "[TC02] Deadlock Prevention Test" );
        }
        
        
        /**
         * Main test method - TC02
         *  - deadlock prevention test
         *
         */
        public void runTest()
        {
            LockSet        lockSet1;
            LockSet        lockSet2;
            LockSet        parent;
            UpgradeThread  upgrade;
            
            VerboseStream stream = new VerboseStream();

            try {
                parent = LockManager.create();
                lockSet1 = LockManager.createRelated( null, parent );
                lockSet2 = LockManager.createRelated( null, parent );

                upgrade = new UpgradeThread( parent, lockSet1, lockSet2, stream );
                upgrade.start();
                
                try {
                    lockSet1.lock( LockMode.READ );
                    lockSet2.lock( LockMode.READ );
                    stream.writeVerbose( "Main: Acquired read locks" );
                } catch ( LockNotGrantedException except ) {
                    fail( "Error: Main: Could not acquired read locks" );
                }
                    
                Thread.currentThread().sleep( 500 );

                try {
                    lockSet1.lock( LockMode.UPGRADE );
                    stream.writeVerbose( "Main: Acquired upgrade lock (1)" );
                } catch ( LockNotGrantedException except ) {
                    fail( "Error: Main: Could not acquired upgrade lock (1)" );
                    parent.dropLocks();
                    upgrade.join();
                }
                Thread.currentThread().sleep( 500 );

                try {
                    lockSet2.lock( LockMode.UPGRADE );
                    fail( "Error: Maing: Acquired upgrade lock (2)" );
                    parent.dropLocks();
                    upgrade.join();
                } catch ( LockNotGrantedException except ) {
                    stream.writeVerbose( "OK: Main: Could not acquired upgrade lock (2) -- deadlock detected" );
                    parent.dropLocks();
                    upgrade.join();
                }

            } catch ( InterruptedException except ) {
                System.out.println( except );
            }
        }


        class UpgradeThread
            extends Thread
        {
            
            LockSet         parent;
            
            LockSet         lockSet1;
            
            LockSet         lockSet2;
            
            VerboseStream stream;
            
            boolean         result;
            
            UpgradeThread( LockSet parent, LockSet lockSet1, LockSet lockSet2, VerboseStream stream )
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
                    } catch ( LockNotGrantedException except ) {
                        fail( "Error: Second: Could not acquired read locks" );
                    }
                    
                    sleep( 500 );
                    
                    try {
                        lockSet2.lock( LockMode.UPGRADE );
                        stream.writeVerbose( "Second: Acquired upgrade lock (2)" );
                    } catch ( LockNotGrantedException except ) {
                        fail( "Error: Second: Could not acquired upgrade lock (2)" );
                        parent.dropLocks();
                        return;
                    }
                    sleep( 500 );
                    
                    try {
                        lockSet1.lock( LockMode.UPGRADE );
                        stream.writeVerbose( "Second: Acquired upgrade lock (1)" );
                    } catch ( LockNotGrantedException except ) {
                        fail( "Error: Second: Could not acquired upgrade lock (1)" );
                        parent.dropLocks();
                        return;
                    }
                    
                    sleep( 500 );
                    
                    try {
                        lockSet2.changeMode( LockMode.READ, LockMode.WRITE );
                        lockSet2.unlock( LockMode.UPGRADE );
                    } catch ( LockNotGrantedException except ) {
                        fail( "Error: Could not acquire write lock (2)" );
                        parent.dropLocks();
                        return;
                    } catch ( LockNotHeldException except ) {
                        fail( "Error: Second: Read lock not held (2)" );
                        parent.dropLocks();
                        return;
                    }
                    stream.writeVerbose( "Second: Upgraded to write lock (2)" );
                    
                    try {
                        lockSet1.changeMode( LockMode.READ, LockMode.WRITE );
                        lockSet1.unlock( LockMode.UPGRADE );
                    } catch ( LockNotGrantedException except ) {
                        fail( "Error: Could not acquire write lock (1)" );
                        parent.dropLocks();
                        return;
                    } catch ( LockNotHeldException except ) {
                        fail( "Error: Second: Read lock not held (1)" );
                        parent.dropLocks();
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

    
    /**
     * Deadlock detection test
     */
    public static class DeadlockTest
        extends TestCase
    {

        TransactionManager  _txManager;

        public DeadlockTest()
        {
            super( "[TC03] Deadlock Detection Test" );
        }
        
        
        /**
         * Main test method - TC03
         *  - deadlock detection test
         *
         */
        public void runTest()
        {
            LockSet        lockSet1;
            LockSet        lockSet2;
            LockSet        parent;
            ConflictThread1 conflict1;
            ConflictThread2 conflict2;

            VerboseStream stream = new VerboseStream();

            try {
                _txManager = TransactionDomain.getDomain( "default" ).getTransactionManager();

                // This transaction will end in 10 seconds
                _txManager.setTransactionTimeout( 10 );
                _txManager.begin();
                stream.writeVerbose( "Main: " + _txManager.getTransaction() );
                
                parent = LockManager.create();
                lockSet1 = LockManager.createRelated( null, parent );
                lockSet2 = LockManager.createRelated( null, parent );

                // Acquire write lock on 1
                try {
                    lockSet1.lock( LockMode.WRITE, Integer.MAX_VALUE );
                    stream.writeVerbose( "Main: Acquired write lock (1)" );
                } catch ( LockNotGrantedException except ) {
                    fail( "Error: Main: Could not acquire write lock (1)" );
                    parent.dropLocks();
                }
                // Let conflict 1 acquire write lock on 2
                conflict1 = new ConflictThread1( parent, lockSet1, lockSet2, stream );
                conflict1.start();
                Thread.currentThread().sleep( 100 );
                synchronized ( conflict1 ) {
                    conflict1.notify();
                }
                Thread.currentThread().sleep( 100 );

                // Let conflict 2 block trying to acquire write lock on 1
                conflict2 = new ConflictThread2( parent, lockSet1, lockSet2, stream );
                conflict2.start();
                Thread.currentThread().sleep( 100 );
                synchronized ( conflict2 ) {
                    conflict2.notify();
                }
                Thread.currentThread().sleep( 100 );

                // Acquire write lock on 2 - blocked by conflict 2
                stream.writeVerbose( "Main: Waiting to acquire write lock (2)" );
                try {
                    lockSet2.lock( LockMode.WRITE, Integer.MAX_VALUE );
                    stream.writeVerbose( "Main: Acquired write lock (2)" );
                } catch ( LockNotGrantedException except ) {
                    fail( "Error: Main: Could not acquire write lock (2)" );
                    parent.dropLocks();
                }

                _txManager.commit();
                conflict1.join();
                conflict2.join();
            } catch ( Exception except ) {
                System.out.println( except );
                except.printStackTrace();
            }
        }


        class ConflictThread1
            extends Thread
        {
            
            LockSet         parent;
            
            LockSet         lockSet1;
            
            LockSet         lockSet2;
            
            VerboseStream stream;
            
            boolean         result;
            
            ConflictThread1( LockSet parent, LockSet lockSet1, LockSet lockSet2, VerboseStream stream )
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
                    // This transaction will end in 5 seconds
                    _txManager.setTransactionTimeout( 5 );
                    _txManager.begin();
                    stream.writeVerbose( "Conflict1: " + _txManager.getTransaction() );
                    synchronized  ( this ) {
                        wait();
                    }
                    try {
                        lockSet2.lock( LockMode.WRITE, Integer.MAX_VALUE );
                        stream.writeVerbose( "Conflict1: Acquired write lock (2)" );
                    } catch ( LockNotGrantedException except ) {
                        fail( "Error: Conflict1: Could not acquire write lock (2)" );
                    }
                    
                    Thread.currentThread().sleep( 100 );
                    stream.writeVerbose( "Conflict1: Waiting to acquire write lock (1)" );
                    try {
                        lockSet1.lock( LockMode.WRITE, Integer.MAX_VALUE );
                        stream.writeVerbose( "Conflict1: Acquired write lock (1)" );
                    } catch ( LockNotGrantedException except ) {
                        fail( "Error: Conflict1: Could not acquire write lock (1)" );
                    }
                    result = true;
                    _txManager.commit();
                }  catch ( Exception except ) {
                    System.out.println( except );
                    except.printStackTrace();
                }
            }
            
            boolean result()
            {
                return result;
            }
            
        }


        class ConflictThread2
            extends Thread
        {
            
            LockSet         parent;
            
            LockSet         lockSet1;
            
            LockSet         lockSet2;
            
            VerboseStream stream;
            
            boolean         result;
            
            ConflictThread2( LockSet parent, LockSet lockSet1, LockSet lockSet2, VerboseStream stream )
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
                    // This transaction will end in 20 seconds
                    _txManager.setTransactionTimeout( 20 );
                    _txManager.begin();
                    stream.writeVerbose( "Conflict2: " + _txManager.getTransaction() );
                    synchronized  ( this ) {
                        wait();
                    }
                    try {
                        stream.writeVerbose( "Conflict2: waiting to acquire write lock (1)" );
                        lockSet1.lock( LockMode.WRITE, Integer.MAX_VALUE );
                        stream.writeVerbose( "Conflict2: acquired write lock (1)" );
                        stream.writeVerbose( "Conflict2: waiting to acquire write lock (2)" );
                        lockSet2.lock( LockMode.WRITE, Integer.MAX_VALUE );
                        stream.writeVerbose( "Conflict2: acquired write lock (2)" );
                    } catch ( LockNotGrantedException except ) {
                        fail( "Error: Conflict2: Could not acquire write locks" );
                    }
                    result = true;
                    _txManager.commit();
                } catch ( Exception except ) {
                    System.out.println( except );
                    except.printStackTrace();
                }
            }
            
            boolean result()
            {
                return result;
            }
            
        }


    }

}
