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
 * $Id: LockSet.java,v 1.3 2001/03/19 17:39:00 arkin Exp $
 */


package tyrex.lock;


import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import javax.transaction.Transaction;
import javax.transaction.Synchronization;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import tyrex.tm.TransactionDomain;
import tyrex.tm.TyrexTransaction;
import tyrex.tm.impl.ThreadContext;
import tyrex.services.Clock;
import tyrex.services.UUID;


/**
 * A lock set provides lock management for a resource.
 * <p>
 * A lock set is associated with a resource in an application specific way.
 * The lock set can be used to acquire multiple locks on behalf of multiple
 * owners.
 * <p>
 * A lock set supports five lock modes:
 * <ul>
 * <li>{@link #READ_INTENT} Read intention lock, acquired to signal an intention
 * to acquire a read lock.</li>
 * <li>{@link #READ} Read locks conflict with any write locks, but multiple read
 * locks by different owners are allowed.</li>
 * <li>{@link #UPGRADE} Upgrade locks are read locks that conflict with each other.
 * Read and upgrade locks are allowed at the same time, but multiple upgrade locks
 * are not.</li>
 * <li>{@link #WRITE_INTENT} Write intention lock, acquired to signal an intention
 * to acquire a write lock.</li>
 * <li>{@link #WRITE} Write locks conflicts with read and write locks. They are
 * exclusive locks.</li>
 * </ul>
 * <p>
 * Compatible lock modes can be acquired by multiple owners, while incompatible
 * lock modes provide exclusive locking for a single owner. The same owner is
 * allowed to acquire incompatible lock modes, if they do not conflict with locks
 * held by other owners (e.g. both read and write locks).
 * <p>
 * The owner of a lock is a transaction or a thread. If a lock is acquired within
 * the context of a transaction, or explicitly by providing a transaction, than
 * that transaction owns the lock. The lock can only be released by that transaction.
 * Otherwise, the lock is ownerd by the calling thread context.
 * <p>
 * The same lock can be acquired and released multiple times by the same owner.
 * In addition, a nested transaction is allowed to reacquire the same locks held
 * by a parent transaction.
 * <p>
 * When a nested transaction aborts, all locks held by that transaction are released
 * immediately. When a parent transaction commits, all locks held by that transaction
 * and all its child transactions are released. Locks held by transactions are
 * released automatically, or can be released using {@link LockCoordinator}.
 * <p>
 * Each lock set has an identifier that can be used for correlation. The identifier
 * can be obtained by calling {@link #toString}. If no identifier is provided when
 * creating a new lock set, a unique identifier is assigned to the lock set.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2001/03/19 17:39:00 $
 */
public final class LockSet
    implements Synchronization, Serializable
{


    /**
     * Read intention lock, acquired to signal an intention to
     * acquire a read lock.
     * <p>
     * Conflicts with: {@link #WRITE}.
     */
    public static final int READ_INTENT  = 0;


    /**
     * Read locks conflict with any write locks, but multiple read
     * locks by different owners are allowed.
     * <p>
     * Conflicts with: {@link #WRITE}, {@link #WRITE_INTENT}
     */
    public static final int READ         = 1;
    

    /**
     * Upgrade locks are read locks that conflict with each other.
     * Read and upgrade locks are allowed at the same time, but multiple
     * upgrade locks are not. Upgrade locks are used to detect deadlocks
     * while upgrading from a read lock to a write lock.
     * <p>
     * Conflicts with: {@link #WRITE}, {@link #WRITE_INTENT},
     * {@link #UPGRADE}
     */
    public static final int UPGRADE      = 2;


    /**
     * Write intention lock, acquired to signal an intention to
     * acquire a write lock.
     * <p>
     * Conflicts with: {@link #WRITE}, {@link #UPGRADE}, {@link #READ}
     */
    public static final int WRITE_INTENT = 3;


    /**
     * Write locks conflicts with read and write locks. They are exclusive
     * locks.
     * <p>
     * Conflicts with: all locks.
     */
    public static final int WRITE        = 4;


    /**
     * A linked list of read intent locks.
     */
    private transient Lock         _readIntent;


    /**
     * A linked list of read locks.
     */
    private transient Lock         _readLock;


    /**
     * A linked list of upgrade intent locks.
     */
    private transient Lock         _upgradeLock;


    /**
     * A linked list of write intent locks.
     */
    private transient Lock         _writeIntent;


    /**
     * A linked list of write locks.
     */
    private transient Lock         _writeLock;


    /**
     * A linked list of waiting threads. Used to track them
     * in FIFO order and notify the first-in of the next
     * release of a lock.
     */
    private transient Waiting      _waiting;


    /**
     * A linked list of all subordinate lock sets, which will
     * drop locks when this set drop locks.
     */
    private transient Subordinate  _subordinate;


    /**
     * The lock set identifier.
     */
    private transient String       _identifier;


    /**
     * Construct a new lock set. The related lock set is
     * specified, if known.
     */
    protected LockSet( String identifier, LockSet related )
    {
        if ( related != null ) {
            synchronized ( related ) {
                related._subordinate = new Subordinate( this, related._subordinate );
            }
        }
        if ( identifier != null )
            _identifier = identifier;
        else
            _identifier = UUID.create( "lock:" );
    }


    /**
     * Default constructor used for serialization.
     */    
    private LockSet()
    {
    }


    public String toString()
    {
        return _identifier;
    }


    public int hashCode()
    {
        return _identifier.hashCode();
    }
    
    public boolean equals( Object other )
    {
        if ( other == this )
            return true;
        if ( other instanceof LockSet )
            return ( (LockSet) other )._identifier.equals( _identifier );
        return false;
    }


    //---------------------------------------------------------------------
    // Public LockSet methods
    //---------------------------------------------------------------------


    /**
     * Acquires a lock on this lock set in the specified mode. This method
     * does not block.
     * <p>
     * If a lock is held in an incompatible mode by a different owner,
     * then this method will throw {@link LockTimeoutException}.
     * <p>
     * If no lock is held in an incompatible model, or a lock is held by
     * the same or related owner, the lock will be acquired and this method
     * will return successfully.
     *
     * @param mode The requested lock mode
     * @throws LockTimeoutException The lock could not be acquired
     */
    public final void lock( int mode )
        throws LockTimeoutException
    {
        internalLock( mode, getOwner(), 0 );
    }


    /**
     * Acquires a lock on this lock set in the specified mode. This method
     * blocks for the specified timeout.
     * <p>
     * If a lock is held in an incompatible mode by a different owner,
     * then this method will block until the lock can be acquired, or the
     * specified time has elapsed (specified in milliseconds).
     * <p>
     * If no lock is held in an incompatible model, or a lock is held by
     * the same or related owner, the lock will be acquired and this method
     * will return successfully.
     *
     * @param mode The requested lock mode
     * @param ms The number of milliseconds to block
     * @throws LockTimeoutException The lock could not be acquired in the
     * specified time
     */
    public final void lock( int mode, int ms )
        throws LockTimeoutException
    {
        internalLock( mode, getOwner(), ms );
    }


    /**
     * Acquires a lock on this lock set in the specified mode on behalf
     * of a transaction. This method blocks for the specified timeout.
     * <p>
     * If a lock is held in an incompatible mode by a different owner,
     * then this method will block until the lock can be acquired, or the
     * specified time has elapsed (specified in milliseconds).
     * <p>
     * If no lock is held in an incompatible model, or a lock is held by
     * the same or related owner, the lock will be acquired and this method
     * will return successfully.
     *
     * @param tx The owner transaction
     * @param mode The requested lock mode
     * @param ms The number of milliseconds to block
     * @throws LockTimeoutException The lock could not be acquired in the
     * specified time
     */
    public final void lock( Transaction tx, int mode, int ms )
        throws LockTimeoutException
    {
        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        internalLock( mode, tx, ms );
    }


    /**
     * Attempt to acquires a lock on this lock set in the specified mode.
     * <p>
     * If a lock is held in an incompatible mode by a different owner,
     * then this method returns false.
     * <p>
     * If no lock is held in an incompatible model, or a lock is held by
     * the same or related owner, the lock will be acquired and this method
     * will return true.
     *
     * @param mode The requested lock mode
     * @return True if lock acquired, false if failed to acquire lock
     */
    /*
    public final boolean tryLock( int mode )
    {
        internalTryLock( mode, getOwner() );
    }
    */


    /**
     * Attempt to acquires a lock on this lock set in the specified mode.
     * <p>
     * If a lock is held in an incompatible mode by a different owner,
     * then this method returns false.
     * <p>
     * If no lock is held in an incompatible model, or a lock is held by
     * the same or related owner, the lock will be acquired and this method
     * will return true.
     *
     * @param tx The owner transaction
     * @param mode The requested lock mode
     * @return True if lock acquired, false if failed to acquire lock
     */
    /*
    public final boolean tryLock( Transcation tx, int mode )
    {
        internalTryLock( mode, tx );
    }
    */


    /**
     * Drops a single lock from this lock set.
     * <p>
     * If the lock was acquired multiple times, it will be released
     * exactly once. This method must be called once for each time
     * {@link #lock lock} has been called to acquire this lock.
     *
     * @param mode The lock mode
     * @throws LockNotHeldException The lock is not held
     */
    public final void unlock( int mode )
        throws LockNotHeldException
    {
        internalUnlock( mode, getOwner() );
    }


    /**
     * Drops a single lock from this lock set on behalf of a transaction.
     * <p>
     * If the lock was acquired multiple times, it will be released
     * exactly once. This method must be called once for each time
     * {@link #lock lock} has been called to acquire this lock.
     *
     * @param tx The owner transaction
     * @param mode The lock mode
     * @throws LockNotHeldException The lock is not held
     */
    public final void unlock( Transaction tx, int mode )
        throws LockNotHeldException
    {
        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        internalUnlock( mode, tx );
    }


    /**
     * Changes from one lock to another. This method attempts to acquire
     * a new lock before releasing an already held lock. This method
     * does not block.
     * <p>
     * If a lock is held in an incompatible mode by a different owner,
     * then this method will throw {@link LockTimeoutException}.
     * <p>
     * If no lock is held in an incompatible model, or a lock is held by
     * the same or related owner, the lock will be acquired and this method
     * will return successfully.
     * <p>
     * If the held lock was acquired multiple times, it will be released
     * exactly once.
     *
     * @param heldMode The held lock mode
     * @param newMode The new lock mode
     * @throws LockNotHeldException The lock is not held
     * @throws LockTimeoutException The lock could not be acquired
     */
    public final void changeMode( int heldMode, int newMode )
        throws LockNotHeldException, LockTimeoutException
    {
        internalChange( heldMode, newMode, getOwner(), 0 );
    }


    /**
     * Changes from one lock to another. This method attempts to acquire
     * a new lock before releasing an already held lock. This method
     * blocks for the specified timeout.
     * <p>
     * If a lock is held in an incompatible mode by a different owner,
     * then this method will block until the lock can be acquired, or the
     * specified time has elapsed (specified in milliseconds).
     * <p>
     * If no lock is held in an incompatible model, or a lock is held by
     * the same or related owner, the lock will be acquired and this method
     * will return successfully.
     * <p>
     * If the held lock was acquired multiple times, it will be released
     * exactly once.
     *
     * @param heldMode The held lock mode
     * @param newMode The new lock mode
     * @param ms The number of milliseconds to block
     * @throws LockNotHeldException The lock is not held
     * @throws LockTimeoutException The lock could not be acquired in the
     * specified time
     */
    public final void changeMode( int heldMode, int newMode, int ms )
        throws LockNotHeldException, LockTimeoutException
    {
        internalChange( heldMode, newMode, getOwner(), ms );
    }


    /**
     * Changes from one lock to another on hehalf of a transaction.
     * This method attempts to acquire a new lock before releasing
     * an already held lock. This method blocks for the specified timeout.
     * <p>
     * If a lock is held in an incompatible mode by a different owner,
     * then this method will block until the lock can be acquired, or the
     * specified time has elapsed (specified in milliseconds).
     * <p>
     * If no lock is held in an incompatible model, or a lock is held by
     * the same or related owner, the lock will be acquired and this method
     * will return successfully.
     * <p>
     * If the held lock was acquired multiple times, it will be released
     * exactly once.
     *
     * @param tx The owner transaction
     * @param heldMode The held lock mode
     * @param newMode The new lock mode
     * @param ms The number of milliseconds to block
     * @throws LockNotHeldException The lock is not held
     * @throws LockTimeoutException The lock could not be acquired in the
     * specified time
     */
    public final void changeMode( Transaction tx, int heldMode,
                                  int newMode, int ms )
        throws LockNotHeldException, LockTimeoutException
    {
        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        internalChange( heldMode, newMode, tx, ms );
    }


    /**
     * Returns the lock coordinator associated with the specified
     * transaction
     *
     * @param tx The transaction
     */
    public final LockCoordinator getCoordinator( Transaction tx )
    {
        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        return new LockCoordinator( this, tx );
    }


    /**
     * Returns the lock coordinator associated with the current owner.
     */
    public final LockCoordinator getCoordinator()
    {
        return new LockCoordinator( this, getOwner() );
    }


    //----------------------------------------------------------------------------
    // Internal implementation
    //----------------------------------------------------------------------------


    /**
     * Called to acquire a lock of the specified type on behalf
     * of the specified owner. This method will block until the
     * lock can be acquired, or a timeout occured.
     *
     * @param mode The lock to acquire
     * @param owner The lock owner
     * @param ms The number of milliseconds to block
     * @throws LockTimeoutException Timeout occured before lock
     * can be acquired
     */
    private synchronized void internalLock( int mode, Object owner, int ms )
        throws LockTimeoutException
    {
        Waiting waiting;
        Waiting last = null;
        long    clock;
        long    timeout;

        // Try to acquire lock, return if successful.
        if ( internalTryLock( mode, owner ) )
            return;
        if ( ms <= 0 )
            throw new LockTimeoutException( "Cannot acquire lock in specified time" );

        // This thread has to block until a lock becomes available.
        // Put a record in the FIFO linked list (_waiting) and
        // track your position (waiting).
        waiting = new Waiting();
        if ( _waiting == null )
            _waiting = waiting;
        else {
            last = _waiting;
            while ( last._next != null )
                last = last._next;
            last._next = waiting;
        }

        // This method will repeat until lock can be acquired,
        // or timeout occured.
        clock = Clock.clock();
        timeout = clock + ms;
        while ( timeout > clock ) {
            try {
                wait( timeout - clock );
            } catch ( InterruptedException except ) {
                // If interrupted we don't wait any longer and
                // give up immediately.
                break;
            }

            // If we are next in FIFO list, we can attempt to acquire
            // lock and remove ourselves from list.
            if ( _waiting == waiting && internalTryLock( mode, owner ) ) {
                _waiting = _waiting._next;
                return;
            }

            // Repeat until timeout.
            clock = Clock.clock();
        }

        // Remove this thread from the waiting list.
        if ( _waiting == waiting )
            _waiting = _waiting._next;
        else
            last._next = waiting._next;
        throw new LockTimeoutException( "Cannot acquire lock in specified time" );
    }
    

    /**
     * Attempt to acquire a lock of the specified type.
     * Return true if the lock has been acquired, false if failed.
     * This method never blocks.
     *
     * @param mode The lock to acquire
     * @param owner The lock owner
     */
    private synchronized boolean internalTryLock( int mode, Object owner )
    {
        Lock lock;
        Lock next;
        Lock other;

        // Determine which lock needs to be acquired.
        switch ( mode ) {
        case READ_INTENT:
            lock = _readIntent;
            break;
        case READ:
            lock = _readLock;
            break;
        case UPGRADE:
            lock = _upgradeLock;
            break;
        case WRITE:
            lock = _writeLock;
            break;
        case WRITE_INTENT:
            lock = _writeIntent;
            break;
        default:
            throw new IllegalArgumentException( "Lock mode is invalid" );
        }

        // Detect existing lock by same owner. If found, increase
        // lock count and return true. If not found, lock points to
        // last lock in the linked list (or null)
        if ( lock != null ) {
            if ( lock._owner == owner ) {
                ++lock._count;
                return true;
            }
            next = lock._next;
            while ( next != null ) {
                if ( next._owner == owner ) {
                    ++next._count;
                    return true;
                }
                lock = next;
                next = next._next;
            }
        }

        // Check for conflicting locks and if no conflicts found,
        // acquire new lock.
        switch ( mode ) {
        case READ_INTENT:
            // Detect conflicting locks: write
            other = _writeLock;
            while ( other != null ) {
                if ( ! isRelated( other._owner, owner ) )
                    return false;
                other = other._next;
            }
            // Add new lock.
            if ( lock == null )
                _readIntent = new Lock( owner );
            else
                lock._next = new Lock( owner );
            break;
        case READ:
            // Detect conflicting locks: write, write intent
            other = _writeLock;
            while ( other != null ) {
                if ( ! isRelated( other._owner, owner ) )
                    return false;
                other = other._next;
            }
            other = _writeIntent;
            while ( other != null ) {
                if ( ! isRelated( other._owner, owner ) )
                    return false;
                other = other._next;
            }
            // Add new lock.
            if ( lock == null )
                _readLock = new Lock( owner );
            else
                lock._next = new Lock( owner );
            break;
        case UPGRADE:
            // Detect conflicting locks: write intent, upgrade
            other = _writeIntent;
            while ( other != null ) {
                if ( ! isRelated( other._owner, owner ) )
                    return false;
                other = other._next;
            }
            other = _upgradeLock;
            while ( other != null ) {
                if ( ! isRelated( other._owner, owner ) )
                    return false;
                other = other._next;
            }
            // Add new lock.
            if ( lock == null )
                _upgradeLock = new Lock( owner );
            else
                lock._next = new Lock( owner );
            break;
        case WRITE:
            // Detect conflicting locks: write intent, read, read intent
            other = _writeIntent;
            while ( other != null ) {
                if ( ! isRelated( other._owner, owner ) )
                    return false;
                other = other._next;
            }
            other = _readLock;
            while ( other != null ) {
                if ( ! isRelated( other._owner, owner ) )
                    return false;
                other = other._next;
            }
            other = _readIntent;
            while ( other != null ) {
                if ( ! isRelated( other._owner, owner ) )
                    return false;
                other = other._next;
            }
            // Add new lock.
            if ( lock == null )
                _writeLock = new Lock( owner );
            else
                lock._next = new Lock( owner );
            break;
        case WRITE_INTENT:
            // Detect conflicting locks: write, upgrade, read
            other = _writeLock;
            while ( other != null ) {
                if ( ! isRelated( other._owner, owner ) )
                    return false;
                other = other._next;
            }
            other = _upgradeLock;
            while ( other != null ) {
                if ( ! isRelated( other._owner, owner ) )
                    return false;
                other = other._next;
            }
            other = _readLock;
            while ( other != null ) {
                if ( ! isRelated( other._owner, owner ) )
                    return false;
                other = other._next;
            }
            // Add new lock.
            if ( lock == null )
                _writeIntent = new Lock( owner );
            else
                lock._next = new Lock( owner );
            break;
        default:
            throw new IllegalArgumentException( "Lock mode is invalid" );
        }

        // If the owner is a transaction, must register for synchronization
        // to drop the locks upon completion of the transaction.
        if ( owner instanceof Transaction ) {
            try {
                ( (Transaction) owner ).registerSynchronization( this );
            } catch ( RollbackException except ) {
                // Transaction rollback, no need to register.
            } catch ( SystemException except ) {
                // System error, no need to register.
            }
        }
        return true;
    }


    /**
     * Releases the specified lock for the specified owner.
     *
     * @param mode The lock to acquire
     * @param owner The lock owner
     * @throw LockNotHeldException Lock not held by this owner
     */
    private synchronized void internalUnlock( int mode, Object owner )
        throws LockNotHeldException
    {
        Lock lock;
        Lock next;

        // Detech which lock need to be released and release
        // directly if the first lock in the linked lists is
        // held by same owner.
        switch ( mode ) {
        case READ_INTENT:
            lock = _readIntent;
            if ( lock != null && lock._owner == owner ) {
                if ( --lock._count <= 0 ) {
                    _readIntent = lock._next;
                    notifyAll();
                }
                return;
            }
            break;
        case READ:
            lock = _readLock;
            if ( lock != null && lock._owner == owner ) {
                if ( --lock._count <= 0 ) {
                    _readLock = lock._next;
                    notifyAll();
                }
                return;
            }
            break;
        case UPGRADE:
            lock = _upgradeLock;
            if ( lock != null && lock._owner == owner ) {
                if ( --lock._count <= 0 ) {
                    _upgradeLock = lock._next;
                    notifyAll();
                }
                return;
            }
            break;
        case WRITE:
            lock = _writeLock;
            if ( lock != null && lock._owner == owner ) {
                if ( --lock._count <= 0 ) {
                    _writeLock = lock._next;
                    notifyAll();
                }
                return;
            }
            break;
        case WRITE_INTENT:
            lock = _writeIntent;
            if ( lock != null && lock._owner == owner ) {
                if ( --lock._count <= 0 ) {
                    _writeIntent = lock._next;
                    notifyAll();
                }
                return;
            }
            break;
        default:
            throw new IllegalArgumentException( "Lock mode is invalid" );
        }

        // Iterate over all locks in the linked list and
        // release the suitable one.
        if ( lock != null ) {
            next = lock._next;
            while ( next != null ) {
                if ( next._owner == owner ) {
                    if ( --next._count <= 0 ) {
                        lock._next = next._next;
                        notifyAll();
                    }
                    return;
                }
                lock = next;
                next = next._next;
            }
        }
        // No lock found for this owner.
        throw new LockNotHeldException( "Lock not held by owner" );
    }


    /**
     * Change from one lock mode to another by acquiring the new
     * lock mode and releasing the held lock mode.
     *
     * @param heldMode The lock mode held and released
     * @param newMode The new lock mode to acquire
     * @param owner The lock owner
     * @param ms The number of milliseconds to block
     * @throws LockTimeoutException Timeout occured before lock
     * can be acquired
     * @throw LockNotHeldException Lock not held by this owner
     */    
    private synchronized void internalChange( int heldMode, int newMode, Object owner, int ms )
        throws LockNotHeldException, LockTimeoutException
    {
        Lock lock;

        // Make sure we hold the lock we are trying to upgrade. This step
        // is essential, since we acquire the new lock before giving up
        // on the old lock.
        switch ( heldMode ) {
        case READ_INTENT:
            lock = _readIntent;
            break;
        case READ:
            lock = _readLock;
            break;
        case UPGRADE:
            lock = _upgradeLock;
            break;
        case WRITE:
            lock = _writeLock;
            break;
        case WRITE_INTENT:
            lock = _writeIntent;
            break;
        default:
            throw new IllegalArgumentException( "Lock mode is invalid" );
        }
        while ( lock != null ) {
            if ( lock._owner == owner )
                break;
            lock = lock._next;
        }
        if ( lock == null )
            throw new LockNotHeldException( "Lock not held by owner" );
        internalLock( newMode, owner, ms );
        internalUnlock( heldMode, owner );
    }


    /**
     * Drop all the locks belonging to a specific owner. This includes
     * locks acquired by the owner and locks acquired by related owners
     * (e.g. nested transactions). This method also drops locks on all
     * subordinate lock sets.
     *
     * @param owner The owner
     */
    protected synchronized void internalDropLocks( Object owner )
    {
        Lock        lock;
        Lock        next;
        Subordinate sub;

        // Drop all locks held by this owner.
        lock = _readIntent;
        while ( lock != null && isRelated( owner, lock._owner ) ) {
            lock = lock._next;
            _readIntent = lock;
        }
        if ( lock != null ) {
            next = lock._next;
            while ( next != null ) {
                if ( isRelated( owner, next._owner ) ) {
                    next = next._next;
                    lock._next = next;
                } else {
                    lock = next;
                    next = next._next;
                }
            }
        }
        lock = _readLock;
        while ( lock != null && isRelated( owner, lock._owner ) ) {
            lock = lock._next;
            _readLock = lock;
        }
        if ( lock != null ) {
            next = lock._next;
            while ( next != null ) {
                if ( isRelated( owner, next._owner ) ) {
                    next = next._next;
                    lock._next = next;
                } else {
                    lock = next;
                    next = next._next;
                }
            }
        }
        lock = _upgradeLock;
        while ( lock != null && isRelated( owner, lock._owner ) ) {
            lock = lock._next;
            _upgradeLock = lock;
        }
        if ( lock != null ) {
            next = lock._next;
            while ( next != null ) {
                if ( isRelated( owner, next._owner ) ) {
                    next = next._next;
                    lock._next = next;
                } else {
                    lock = next;
                    next = next._next;
                }
            }
        }
        lock = _writeLock;
        while ( lock != null && isRelated( owner, lock._owner ) ) {
            lock = lock._next;
            _writeLock = lock;
        }
        if ( lock != null ) {
            next = lock._next;
            while ( next != null ) {
                if ( isRelated( owner, next._owner ) ) {
                    next = next._next;
                    lock._next = next;
                } else {
                    lock = next;
                    next = next._next;
                }
            }
        }
        lock = _writeIntent;
        while ( lock != null && isRelated( owner, lock._owner ) ) {
            lock = lock._next;
            _writeIntent = lock;
        }
        if ( lock != null ) {
            next = lock._next;
            while ( next != null ) {
                if ( isRelated( owner, next._owner ) ) {
                    next = next._next;
                    lock._next = next;
                } else {
                    lock = next;
                    next = next._next;
                }
            }
        }
                
        // Drop locks on all the subordinates.
        sub = _subordinate;
        while ( sub != null ) {
            sub._lockSet.internalDropLocks( owner );
            sub = sub._next;
        }
        // Notify next thread waiting for lock.
        if ( _waiting != null )
            notifyAll();
    }


    /**
     * Determine if existing owner of lock (<tt>existing</tt>) is related
     * to owner requesting new lock (<tt>requesting</tt>), or they are
     * the same owner.
     * <p>
     * Two owners are related if the requesting owner can acquire a lock
     * held by the existing owner without conflict. From a transaction
     * perspective, the existing owner must be a parent of the requesting
     * owner.
     * <p>
     * Since the two owners are only related, if this method returns true,
     * the requesting owner acquires a new lock.
     *
     * @param existing The existing lock owner
     * @param requesting The requesting lock owner
     * @return True if requesting lock owner can acquire lock held by
     * existing lock owner without conflict
     */
    private boolean isRelated( Object existing, Object requesting )
    {
        TyrexTransaction parent;

        if ( existing == requesting )
            return true;
        if ( existing instanceof TyrexTransaction &&
             requesting instanceof TyrexTransaction ) {
            parent = (TyrexTransaction) ( (TyrexTransaction) requesting ).getParent();
            while ( parent != null ) {
                if ( parent == existing )
                    return true;
                parent = (TyrexTransaction) parent.getParent();
            }
        }
        return false;
    }


    /**
     * Returns the owner for this request. If the current thread is
     * associated with a transaction, the transaction is returned and
     * will be used as the owner for the lock. Otherwise, the thread
     * context will be used.
     * <p>
     * Using the thread context instead of the thread allows locks to
     * be used across method invocations.
     *
     * @return The owner, either a transaction or thread context
     */
    private Object getOwner()
    {
        ThreadContext context;
        Transaction   tx;

        context = ThreadContext.getThreadContext();
        tx = context.getTransaction();
        if ( tx != null )
            return tx;
        else
            return context;
    }


    //----------------------------------------------------------------------------
    // Transaction synchronization
    //----------------------------------------------------------------------------


    public void beforeCompletion()
    {
    }


    public void afterCompletion( int heuristic )
    {
        internalDropLocks( getOwner() );
    }


    //----------------------------------------------------------------------------
    // Serialization
    //----------------------------------------------------------------------------


    private void writeObject( ObjectOutputStream stream )
        throws IOException
    {
        Subordinate sub;
        Lock        lock;
        int         count;

        stream.writeUTF( _identifier );
        // Write number of locks for which the owner is a transaction.
        // Order: read intent, read, upgrade, write intent, write
        for ( lock = _readIntent, count = 0; lock != null ; lock = lock._next )
            if ( lock._owner instanceof Transaction )
                ++count;
        stream.writeShort( (short) count );
        for ( lock = _readIntent ; lock != null ; lock = lock._next )
            if ( lock._owner instanceof Transaction ) {
                stream.writeUTF( lock._owner.toString() );
                stream.writeShort( lock._count );
            }
        for ( lock = _readLock, count = 0; lock != null ; lock = lock._next )
            if ( lock._owner instanceof Transaction )
                ++count;
        stream.writeShort( (short) count );
        for ( lock = _readLock ; lock != null ; lock = lock._next )
            if ( lock._owner instanceof Transaction ) {
                stream.writeUTF( lock._owner.toString() );
                stream.writeShort( lock._count );
            }
        for ( lock = _upgradeLock, count = 0; lock != null ; lock = lock._next )
            if ( lock._owner instanceof Transaction )
                ++count;
        stream.writeShort( (short) count );
        for ( lock = _upgradeLock ; lock != null ; lock = lock._next )
            if ( lock._owner instanceof Transaction ) {
                stream.writeUTF( lock._owner.toString() );
                stream.writeShort( lock._count );
            }
        for ( lock = _writeIntent, count = 0; lock != null ; lock = lock._next )
            if ( lock._owner instanceof Transaction )
                ++count;
        stream.writeShort( (short) count );
        for ( lock = _writeIntent ; lock != null ; lock = lock._next )
            if ( lock._owner instanceof Transaction ) {
                stream.writeUTF( lock._owner.toString() );
                stream.writeShort( lock._count );
            }
        for ( lock = _writeLock, count = 0; lock != null ; lock = lock._next )
            if ( lock._owner instanceof Transaction )
                ++count;
        stream.writeShort( (short) count );
        for ( lock = _writeLock ; lock != null ; lock = lock._next )
            if ( lock._owner instanceof Transaction ) {
                stream.writeUTF( lock._owner.toString() );
                stream.writeShort( lock._count );
            }
        // Write number of subordinates and lock-set of each subordinate.
        sub = _subordinate;
        for ( count = 0 ; sub != null ; ++count )
            sub = sub._next;
        stream.writeShort( (short) count );
        sub = _subordinate;
        while ( sub != null ) {
            stream.writeObject( sub._lockSet );
            sub = sub._next;
        }
        stream.flush();
    }


    private void readObject( ObjectInputStream stream )
        throws IOException, ClassNotFoundException
    {
        int         count;
        Transaction tx;
        Lock        lock;
        Lock        last;
        LockSet     lockSet;

        _identifier = stream.readUTF();
        // Read all locks for which a transaction can be resolved.
        // Order: read intent, read, upgrade, write intent, write
        for ( count = stream.readShort(), last = null ; count-- > 0 ; ) {
            tx = TransactionDomain.getTransaction( stream.readUTF() );
            if ( tx != null ) {
                lock = new Lock( tx );
                lock._count = stream.readShort();
                if ( last == null )
                    _readIntent = lock;
                else
                    last._next = lock;
                last = lock;
            }
        }
        for ( count = stream.readShort(), last = null ; count-- > 0 ; ) {
            tx = TransactionDomain.getTransaction( stream.readUTF() );
            if ( tx != null ) {
                lock = new Lock( tx );
                lock._count = stream.readShort();
                if ( last == null )
                    _readLock = lock;
                else
                    last._next = lock;
                last = lock;
            }
        }
        for ( count = stream.readShort(), last = null ; count-- > 0 ; ) {
            tx = TransactionDomain.getTransaction( stream.readUTF() );
            if ( tx != null ) {
                lock = new Lock( tx );
                lock._count = stream.readShort();
                if ( last == null )
                    _upgradeLock = lock;
                else
                    last._next = lock;
                last = lock;
            }
        }
        for ( count = stream.readShort(), last = null ; count-- > 0 ; ) {
            tx = TransactionDomain.getTransaction( stream.readUTF() );
            if ( tx != null ) {
                lock = new Lock( tx );
                lock._count = stream.readShort();
                if ( last == null )
                    _writeIntent = lock;
                else
                    last._next = lock;
                last = lock;
            }
        }
        for ( count = stream.readShort(), last = null ; count-- > 0 ; ) {
            tx = TransactionDomain.getTransaction( stream.readUTF() );
            if ( tx != null ) {
                lock = new Lock( tx );
                lock._count = stream.readShort();
                if ( last == null )
                    _writeLock = lock;
                else
                    last._next = lock;
                last = lock;
            }
        }
        // Read all subordinates.
        count = stream.readShort();
        while ( count-- > 0 ) {
            lockSet = (LockSet) stream.readObject();
            _subordinate = new Subordinate( lockSet, _subordinate );
        }
    }



    /**
     * A lock on behalf of an owner. Locks are linked lists to allow
     * multiple owners to acquire non-conflicting locks. If the same
     * owner acquires the same lock, a reference count is used.
     */
    private final static class Lock
    {

        /**
         * The owner of the lock. The owner could be a thread or
         * a transaction.
         */
        final Object   _owner;


        /**
         * The number of times the lock has been acquired by this
         * owner.
         */
        int            _count = 1;


        /**
         * Reference to the next lock for a different owner.
         */
        Lock           _next;


        Lock( Object owner )
        {
            _owner = owner;
        }


    }


    /**
     * A linked list of threads blocked and waiting for the release
     * of a lock. Each thread adds itself to the end of the list, and
     * when notified checks to see if it's at the top.
     */
    private static final class Waiting
    {

        Waiting _next;

    }


    /**
     * A record of a subordinate lock set. When this set drop locks,
     * so will all subordinate lock sets.
     */
    private static final class Subordinate
    {


        /**
         * The subordinate lock set.
         */
        final LockSet         _lockSet;


        /**
         * The next subordinate in a single-linked list of subordinates.
         */
        final Subordinate     _next;


        Subordinate( LockSet lockSet, Subordinate next )
        {
            _lockSet = lockSet;
            _next = next;
        }


    }


}
