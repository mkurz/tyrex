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
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: Concurrency.java,v 1.1 2000/04/10 20:53:52 arkin Exp $
 */


package concurrency;


import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import org.exolab.jtf.CWTestCategory;
import org.exolab.jtf.CWTestCase;
import org.exolab.jtf.CWVerboseStream;
import org.exolab.exceptions.CWClassConstructorException;
import tyrex.concurrency.LockSet;
import tyrex.concurrency.LockSetFactory;
import tyrex.concurrency.LockMode;
import tyrex.concurrency.LockNotHeldException;
import tyrex.concurrency.engine.TyrexLockSetFactory;



public class Concurrency
    extends CWTestCategory
{


    public Concurrency()
        throws CWClassConstructorException
    {
        super( "conc", "Concurrency service tests");
        
        CWTestCase tc;
        
        tc = new LockSetTest();
        add( tc.name(), tc, true );
        tc = new DeadlockTest();
        add( tc.name(), tc, true );
    }


    public static class LockSetTest
        extends CWTestCase
    {

        public LockSetTest()
            throws CWClassConstructorException
        {
            super( "TC01", "LockSet Test" );
        }

        public void preExecute()
        {
            super.preExecute();
        }
        
        public void postExecute()
        {
            super.postExecute();
        }
        
        public boolean run( CWVerboseStream stream )
        {
            LockSetFactory factory;
            LockSet        lockSet;
            AcquireThread  acquire;

            try {
                factory = new TyrexLockSetFactory( null );
                lockSet = factory.create();

                // Test acquiring read lock and two write locks
                if ( ! lockSet.tryLock( LockMode.Read ) ) {
                    stream.writeVerbose( "Error: Failed to acquire read lock" );
                    return false;
                }
                stream.writeVerbose( "Acquired read lock" );
                if ( ! lockSet.tryLock( LockMode.Write ) ) {
                    stream.writeVerbose( "Error: Failed to acquire write lock" );
                    return false;
                }
                stream.writeVerbose( "Acquired 1st write lock, same thread" );
                if ( ! lockSet.tryLock( LockMode.Write ) ) {
                    stream.writeVerbose( "Error: Failed to acquire write lock" );
                    return false;
                }
                stream.writeVerbose( "Acquired 1st write lock, same thread" );

                // Test acquiring write lock, different thread
                stream.writeVerbose( "Attempt to acquire write lock, different thread" );
                acquire = new AcquireThread( lockSet, LockMode.Write );
                acquire.start();
                acquire.join( 100 );
                if ( acquire.result() ) {
                    stream.writeVerbose( "Error: Other thread managed to acquire write lock" );
                    return false;
                } else
                    stream.writeVerbose( "OK: Other thread failed to acquire write lock" );

                // Releasing both write locks, second thread can't obtain lock yet
                try {
                    lockSet.unlock( LockMode.Write );
                } catch ( LockNotHeldException except ) {
                    stream.writeVerbose( "Error: Write lock not held" );
                    return false;
                }
                stream.writeVerbose( "Released 2nd write lock" );
                acquire.join( 100 );
                if ( acquire.result() ) {
                    stream.writeVerbose( "Error: Other thread managed to acquire write lock" );
                    return false;
                } else
                    stream.writeVerbose( "OK: Other thread couldn't acquire lock" );
                try {
                    lockSet.unlock( LockMode.Write );
                } catch ( LockNotHeldException except ) {
                    stream.writeVerbose( "Error: Write lock not held" );
                    return false;
                }
                stream.writeVerbose( "Released 1st write lock" );
                acquire.join( 100 );
                if ( acquire.result() ) {
                    stream.writeVerbose( "Error: Second thread managed to acquire write lock" );
                    return false;
                } else
                    stream.writeVerbose( "OK: Other thread couldn't acquire lock" );

                // Release read lock, other thread can acquire write lock
                try {
                    lockSet.unlock( LockMode.Read );
                } catch ( LockNotHeldException except ) {
                    stream.writeVerbose( "Error: Read lock not held" );
                    return false;
                }
                stream.writeVerbose( "Released read lock" );
                acquire.join( 100 );
                if ( ! acquire.result() ) {
                    stream.writeVerbose( "Error: Second thread could not manage to acquire write lock" );
                    return false;
                }
                stream.writeVerbose( "Other thread acquired write lock" );

                // Make sure this thread cannot acquire a read lock
                if ( lockSet.tryLock( LockMode.Read ) ) {
                    stream.writeVerbose( "Error: This thread managed to acquire write lock" );
                    return false;
                } else
                    stream.writeVerbose( "OK: This thread failed to acquire write lock" );

                return true;
            } catch ( IOException except ) {
                System.out.println( except );
                return false;
            } catch ( InterruptedException except ) {
                System.out.println( except );
                return false;
            }
        }


    }


    static class AcquireThread
        extends Thread
    {
        
        LockSet  lockSet;
        
        LockMode mode;
        
        boolean  result;
        
        AcquireThread( LockSet lockSet, LockMode mode )
        {
            this.lockSet = lockSet;
            this.mode = mode;
            result = false;
        }

        public void run()
        {
            lockSet.lock( mode );
            result = true;
        }

        boolean result()
        {
            return result;
        }

    }


    public static class DeadlockTest
        extends CWTestCase
    {

        public DeadlockTest()
            throws CWClassConstructorException
        {
            super( "TC02", "Deadlock Detection Test" );
        }

        public void preExecute()
        {
            super.preExecute();
        }
        
        public void postExecute()
        {
            super.postExecute();
        }
        
        public boolean run( CWVerboseStream stream )
        {
            LockSetFactory factory;
            LockSet        lockSet1;
            LockSet        lockSet2;
            LockSet        parent;
            DeadlockThread deadlock;

            try {
                factory = new TyrexLockSetFactory( null );
                parent = factory.create();
                lockSet1 = factory.createRelated( parent );
                lockSet2 = factory.createRelated( parent );

                deadlock = new DeadlockThread( parent, lockSet1, lockSet2, stream );
                deadlock.start();
                
                lockSet1.lock( LockMode.Read );
                lockSet2.lock( LockMode.Read );
                stream.writeVerbose( "Main: Acquired read locks" );

                Thread.currentThread().sleep( 500 );

                if ( lockSet1.tryLock( LockMode.Upgrade ) )
                    stream.writeVerbose( "Main: Acquired upgrade lock (1)" );
                else {
                    stream.writeVerbose( "Error: Main: Could not acquired upgrade lock (1)" );
                    parent.getCoordinator( null ).dropLocks();
                    deadlock.join();
                    return false;
                }
                Thread.currentThread().sleep( 500 );

                if ( lockSet2.tryLock( LockMode.Upgrade ) ) {
                    stream.writeVerbose( "Error: Maing: Acquired upgrade lock (2)" );
                    parent.getCoordinator( null ).dropLocks();
                    deadlock.join();
                    return false;
                } else {
                    stream.writeVerbose( "OK: Main: Could not acquired upgrade lock (2) -- deadlock detected" );
                    parent.getCoordinator( null ).dropLocks();
                    deadlock.join();
                    return true;
                }

            } catch ( IOException except ) {
                System.out.println( except );
                return false;
            } catch ( InterruptedException except ) {
                System.out.println( except );
                return false;
            }
        }


    }


    static class DeadlockThread
        extends Thread
    {
        
        LockSet         parent;

        LockSet         lockSet1;

        LockSet         lockSet2;

        CWVerboseStream stream;
        
        boolean         result;
        
        DeadlockThread( LockSet parent, LockSet lockSet1, LockSet lockSet2, CWVerboseStream stream )
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

                lockSet1.lock( LockMode.Read );
                lockSet2.lock( LockMode.Read );
                stream.writeVerbose( "Second: Acquired read locks" );

                sleep( 500 );

                if ( lockSet2.tryLock( LockMode.Upgrade ) )
                    stream.writeVerbose( "Second: Acquired upgrade lock (2)" );
                else {
                    stream.writeVerbose( "Error: Second: Could not acquired upgrade lock (2)" );
                    parent.getCoordinator( null ).dropLocks();
                    return;
                }
                sleep( 500 );

                if ( lockSet1.tryLock( LockMode.Upgrade ) )
                    stream.writeVerbose( "Second: Acquired upgrade lock (1)" );
                else {
                    stream.writeVerbose( "Error: Second: Could not acquired upgrade lock (1)" );
                    parent.getCoordinator( null ).dropLocks();
                    return;
                }

                sleep( 500 );

                try {
                    lockSet2.changeMode( LockMode.Read, LockMode.Write );
                    lockSet2.unlock( LockMode.Upgrade );
                } catch ( LockNotHeldException except ) {
                    stream.writeVerbose( "Error: Second: Read lock not held (2)" );
                    parent.getCoordinator( null ).dropLocks();
                    return;
                }
                stream.writeVerbose( "Second: Upgraded to write lock (2)" );

                try {
                    lockSet1.changeMode( LockMode.Read, LockMode.Write );
                    lockSet1.unlock( LockMode.Upgrade );
                } catch ( LockNotHeldException except ) {
                    stream.writeVerbose( "Error: Second: Read lock not held (1)" );
                    parent.getCoordinator( null ).dropLocks();
                    return;
                }
                stream.writeVerbose( "Second: Upgraded to write lock (1)" );

                result = true;
            } catch ( IOException except ) {
                System.out.println( except );
            } catch ( InterruptedException except ) {
                System.out.println( except );
            }
        }

        boolean result()
        {
            return result;
        }

    }

    
}
