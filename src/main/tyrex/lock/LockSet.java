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
 * $Id: LockSet.java,v 1.6 2001/03/23 03:57:48 arkin Exp $
 */


package tyrex.lock;


import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import javax.transaction.Transaction;
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
 * @version $Revision: 1.6 $ $Date: 2001/03/23 03:57:48 $
 * @see LockMode
 * @see LockCoordinator
 */
public final class LockSet
    implements Serializable
{


    // TODO:
    // - Prevent lock from being acquired if transaction aborted.
    // - Should tryLock just check if the lock can be acquired, but
    //   not acquire it?


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
     * A linked list of blocked owners. Used to track them
     * in FIFO order and notify the first when a lock can
     * be acquired.
     */
    private transient BlockedOwner _blocked = BlockedOwner.NULL_BLOCKED;


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
     * then this method will throw {@link LockNotGrantedException}.
     * <p>
     * If no lock is held in an incompatible model, or a lock is held by
     * the same or related owner, the lock will be acquired and this method
     * will return successfully.
     *
     * @param mode The requested lock mode
     * @throws LockNotGrantedException The lock could not be acquired
     */
    public final void lock( int mode )
        throws LockNotGrantedException
    {
        internalLock( mode, LockOwner.getOwner(), 0 );
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
     * @throws LockNotGrantedException The lock could not be acquired in the
     * specified time
     */
    public final void lock( int mode, int ms )
        throws LockNotGrantedException
    {
        internalLock( mode, LockOwner.getOwner(), ms );
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
    public final boolean tryLock( int mode )
    {
        try {
            return internalTryLock( mode, LockOwner.getOwner(), false );
        } catch ( LockNotGrantedException except ) {
            return false;
        }
    }


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
        internalUnlock( mode, LockOwner.getOwner() );
    }


    /**
     * Changes from one lock to another. This method attempts to acquire
     * a new lock before releasing an already held lock. This method
     * does not block.
     * <p>
     * If a lock is held in an incompatible mode by a different owner,
     * then this method will throw {@link LockNotGrantedException}.
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
     * @throws LockNotGrantedException The lock could not be acquired
     */
    public final void changeMode( int heldMode, int newMode )
        throws LockNotHeldException, LockNotGrantedException
    {
        internalChange( heldMode, newMode, LockOwner.getOwner(), 0 );
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
     * @throws LockNotGrantedException The lock could not be acquired in the
     * specified time
     */
    public final void changeMode( int heldMode, int newMode, int ms )
        throws LockNotHeldException, LockNotGrantedException
    {
        internalChange( heldMode, newMode, LockOwner.getOwner(), ms );
    }


    /**
     * Returns the lock coordinator associated with the current owner.
     */
    public final void dropLocks()
    {
        LockOwner.getOwner().dropLocks();
    }


    /**
     * Returns an arrays with all the locks held by this owner.
     *
     * @return All locks helds by this owner
     */
    public static final LockSet[] listLockSets()
    {
        return LockOwner.getOwner().getLockSets();
    }


    //----------------------------------------------------------------------------
    // Internal implementation
    //----------------------------------------------------------------------------


    /**
     * Called to acquire a lock of the specified mode on behalf
     * of an owner. This method will block until the lock can be
     * acquired, or a timeout occurs.
     *
     * @param mode The lock to acquire
     * @param owner The lock owner
     * @param ms The number of milliseconds to block
     * @throws LockNotGrantedException Timeout occured before lock
     * can be acquired, or the owner is in the shrinking phase
     */
    private void internalLock( int mode, LockOwner owner, int ms )
        throws LockNotGrantedException
    {
        BlockedOwner blocked;
        BlockedOwner last = null;
        BlockedOwner next;
        long         clock;
        long         timeout;
        String       reason;

        // If the owner is not allowed to acquire any more locks,
        // we throw an exception.
        if ( owner.getPhase() == LockOwner.SHRINKING )
            throw new LockNotGrantedException( "Owner cannot acquire any new locks" );

        // Synchronize on the lockSet to be able to acquire
        // a lock and change the _blocked reference.
        synchronized ( this ) {
            // Try to acquire lock, return if successful.
            // This method throws LockNotGrantedException
            // if the owner is in the shrinking phase.
            if ( internalTryLock( mode, owner, true ) )
                return;
            if ( ms <= 0 )
                throw new LockNotGrantedException( "Cannot acquire lock due to conflict" );
            
            // This thread must block until a lock becomes available.
            // Put a record in the FIFO linked list.
            blocked = new BlockedOwner( owner );
            if ( _blocked == BlockedOwner.NULL_BLOCKED )
                _blocked = blocked;
            else {
                last = _blocked;
                next = last._nextInSet;
                while ( next != null ) {
                    last = next;
                    next = next._nextInSet;
                }
                last._nextInSet = blocked;
            }
        }

        // Repeat until lock can be acquired, or timeout occurs.
        clock = Clock.clock();
        timeout = clock + ms;
        reason = "Cannot acquire lock within specified time due to conflict";
        while ( timeout > clock ) {
            // Synchronize on block to be notified when a lock
            // is released.
            synchronized ( blocked ) {
                try {
                    blocked.block( timeout - clock );
                } catch ( InterruptedException except ) {
                    // If interrupted we don't wait any longer and
                    // give up immediately. We exit the loop.
                    reason = "Cannot acquire lock - thread interrupted";
                    break;
                }
            }

            // It's possible that we were notified of lock being
            // release due to transaction completing, or deadlock
            // being detected.
            switch ( blocked._state ) {
            case BlockedOwner.ABORTED:
                reason = "Cannot acquire lock - transaction aborted";
                break;
            case BlockedOwner.DEADLOCK:
                reason = "Cannot acquire lock - deadlock detected, transaction aborted";
                break;
            case BlockedOwner.ALERT:
                // See if we can acquire the lock. Need to synchronize
                // so we can change the _block reference.
                synchronized ( this ) {
                    if ( internalTryLock( mode, owner, true ) ) {
                        _blocked = blocked.remove();
                        return;
                    }
                }
            }
            
            blocked.reset();
            // Repeat until timeout.
            clock = Clock.clock();
        }

        // Remove this thread from the blocked list.
        _blocked = blocked.remove();
        throw new LockNotGrantedException( reason );
    }


    /**
     * Attempt to acquire a lock of the specified mode. Returns true
     * if the lock can be acquired, false if the lock cannot be
     * acquired. The lock is acquired only if <tt>acquire</tt> is true.
     * This method never blocks.
     *
     * @param mode The lock to acquire
     * @param owner The lock owner
     * @param acquire True to acquire lock
     * @throws LockNotGrantedException Attempt to add lock
     * while in the shrinking phase
     */
    private synchronized boolean internalTryLock( int mode, LockOwner owner,
                                                  boolean acquire )
        throws LockNotGrantedException
    {
        Lock lock;
        Lock next;
        Lock other;

        // Determine which lock needs to be acquired.
        switch ( mode ) {
        case LockMode.READ_INTENT:
            lock = _readIntent;
            break;
        case LockMode.READ:
            lock = _readLock;
            break;
        case LockMode.UPGRADE:
            lock = _upgradeLock;
            break;
        case LockMode.WRITE:
            lock = _writeLock;
            break;
        case LockMode.WRITE_INTENT:
            lock = _writeIntent;
            break;
        default:
            throw new IllegalArgumentException( "Lock mode " + mode + " is invalid" );
        }

        // Detect existing lock by same owner. If found,
        // increase lock count and return immediately.
        //
        // If not found, the variable lock points to the
        // last lock in the linked list or null if the
        // linked list is empty.
        if ( lock != null ) {
            if ( lock._owner == owner ) {
                ++lock._count;
                return true;
            }
            next = lock._nextInMode;
            while ( next != null ) {
                if ( next._owner == owner ) {
                    ++next._count;
                    return true;
                }
                lock = next;
                next = next._nextInMode;
            }
        }

        // Check for conflictng locks. If conflicting lock
        // found, return false. Otherwise, return true.
        // Acquire the lock only if acquire flag is true.
        switch ( mode ) {
        case LockMode.READ_INTENT:
            // Detect conflicting locks: write
            //
            // We identify write locks as conflicting with
            // read lock. We look at all owners of the write
            // lock. If there are no owners, we can acquire
            // a read lock. If there is at least one owner
            // that is the requesting owner or a parent of
            // the requesting oner, we can acquire a read lock.
            other = _writeLock;
            if ( other != null ) {
                while ( other != null ) {
                    if ( other._owner == owner || other._owner.isParentOf( owner ) )
                        break;
                    other = other._nextInMode;
                }
                if ( other == null )
                    return false;
            }
            // Add a new lock only if acquire flag is true.
            //
            // If the lock linked list is not empty, the lock
            // variable points to the last lock in the linked
            // list and we add the new lock immediately after.
            // The lock constructor throws LockNotGrantedException
            // if the owner is in the shrinking phase.
            if ( acquire ) {
                if ( lock == null )
                    _readIntent = new Lock( this, owner );
                else
                    lock._nextInMode = new Lock( this, owner );
            }
            break;
        case LockMode.READ:
            // Detect conflicting locks: write, write intent
            other = _writeLock;
            if ( other != null ) {
                while ( other != null ) {
                    if ( other._owner == owner || other._owner.isParentOf( owner ) )
                        break;
                    other = other._nextInMode;
                }
                if ( other == null )
                    return false;
            }
            other = _writeIntent;
            if ( other != null ) {
                while ( other != null ) {
                    if ( other._owner == owner || other._owner.isParentOf( owner ) )
                        break;
                    other = other._nextInMode;
                }
                if ( other == null )
                    return false;
            }
            // Add a new lock only if acquire flag is true.
            if ( acquire ) {
                if ( lock == null )
                    _readLock = new Lock( this, owner );
                else
                    lock._nextInMode = new Lock( this, owner );
            }
            break;
        case LockMode.UPGRADE:
            // Detect conflicting locks: write intent, upgrade
            other = _writeIntent;
            if ( other != null ) {
                while ( other != null ) {
                    if ( other._owner == owner || other._owner.isParentOf( owner ) )
                        break;
                    other = other._nextInMode;
                }
                if ( other == null )
                    return false;
            }
            other = _upgradeLock;
            if ( other != null ) {
                while ( other != null ) {
                    if ( other._owner == owner || other._owner.isParentOf( owner ) )
                        break;
                    other = other._nextInMode;
                }
                if ( other == null )
                    return false;
            }
            // Add a new lock only if acquire flag is true.
            if ( acquire ) {
                if ( lock == null )
                    _upgradeLock = new Lock( this, owner );
                else
                    lock._nextInMode = new Lock( this, owner );
            }
            break;
        case LockMode.WRITE:
            // Detect conflicting locks: write intent, read, read intent
            other = _writeIntent;
            if ( other != null ) {
                while ( other != null ) {
                    if ( other._owner == owner || other._owner.isParentOf( owner ) )
                        break;
                    other = other._nextInMode;
                }
                if ( other == null )
                    return false;
            }
            other = _readLock;
            if ( other != null ) {
                while ( other != null ) {
                    if ( other._owner == owner || other._owner.isParentOf( owner ) )
                        break;
                    other = other._nextInMode;
                }
                if ( other == null )
                    return false;
            }
            other = _readIntent;
            if ( other != null ) {
                while ( other != null ) {
                    if ( other._owner == owner || other._owner.isParentOf( owner ) )
                        break;
                    other = other._nextInMode;
                }
                if ( other == null )
                    return false;
            }
            // Add a new lock only if acquire flag is true.
            if ( acquire ) {
                if ( lock == null )
                    _writeLock = new Lock( this, owner );
                else
                    lock._nextInMode = new Lock( this, owner );
            }
            break;
        case LockMode.WRITE_INTENT:
            // Detect conflicting locks: write, upgrade, read
            other = _writeLock;
            if ( other != null ) {
                while ( other != null ) {
                    if ( other._owner == owner || other._owner.isParentOf( owner ) )
                        break;
                    other = other._nextInMode;
                }
                if ( other == null )
                    return false;
            }
            other = _upgradeLock;
            if ( other != null ) {
                while ( other != null ) {
                    if ( other._owner == owner || other._owner.isParentOf( owner ) )
                        break;
                    other = other._nextInMode;
                }
                if ( other == null )
                    return false;
            }
            other = _readLock;
            if ( other != null ) {
                while ( other != null ) {
                    if ( other._owner == owner || other._owner.isParentOf( owner ) )
                        break;
                    other = other._nextInMode;
                }
                if ( other == null )
                    return false;
            }
            // Add a new lock only if acquire flag is true.
            if ( acquire ) {
                if ( lock == null )
                    _writeIntent = new Lock( this, owner );
                else
                    lock._nextInMode = new Lock( this, owner );
            }
            break;
        default:
            throw new IllegalArgumentException( "Lock mode " + mode + " is invalid" );
        }
        return true;
    }


    /**
     * Releases the lock in the specified mode on behalf of the owner.
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
        case LockMode.READ_INTENT:
            lock = _readIntent;
            if ( lock == null )
                throw new LockNotHeldException( "Lock not held by owner" );
            // Look only at the first lock in the linked list
            // and determine if held by this owner.
            // If it does, decrement the reference count. When the
            // reference count reaches zero, the lock is removed
            // from the lock list and the owner, and the next blocked
            // owner is notified.
            if ( lock._owner == owner ) {
                if ( --lock._count <= 0 ) {
                    lock._owner.remove( lock );
                    _readIntent = lock._nextInMode;
                    _blocked.alert();
                }
                return;
            }
            break;
        case LockMode.READ:
            lock = _readLock;
            if ( lock == null )
                throw new LockNotHeldException( "Lock not held by owner" );
            if ( lock._owner == owner ) {
                if ( --lock._count <= 0 ) {
                    lock._owner.remove( lock );
                    _readLock = lock._nextInMode;
                    _blocked.alert();
                }
                return;
            }
            break;
        case LockMode.UPGRADE:
            lock = _upgradeLock;
            if ( lock == null )
                throw new LockNotHeldException( "Lock not held by owner" );
            if ( lock._owner == owner ) {
                if ( --lock._count <= 0 ) {
                    lock._owner.remove( lock );
                    _upgradeLock = lock._nextInMode;
                    _blocked.alert();
                }
                return;
            }
            break;
        case LockMode.WRITE:
            lock = _writeLock;
            if ( lock == null )
                throw new LockNotHeldException( "Lock not held by owner" );
            if ( lock._owner == owner ) {
                if ( --lock._count <= 0 ) {
                    lock._owner.remove( lock );
                    _writeLock = lock._nextInMode;
                    _blocked.alert();
                }
                return;
            }
            break;
        case LockMode.WRITE_INTENT:
            lock = _writeIntent;
            if ( lock == null )
                throw new LockNotHeldException( "Lock not held by owner" );
            if ( lock._owner == owner ) {
                if ( --lock._count <= 0 ) {
                    lock._owner.remove( lock );
                    _writeIntent = lock._nextInMode;
                    _blocked.alert();
                }
                return;
            }
            break;
        default:
            throw new IllegalArgumentException( "Lock mode is invalid" );
        }

        // Iterate over all locks in the linked list except the
        // first one and find the one that is held by this owner.
        // If found, decrement the reference count. When the
        // reference count reaches zero, the lock is removed
        // from the lock list and the owner, and the next blocked
        // owner is notified.
        next = lock._nextInMode;
        while ( next != null ) {
            if ( next._owner == owner ) {
                if ( --next._count <= 0 ) {
                    lock._nextInMode = next._nextInMode;
                    next._owner.remove( next );
                    _blocked.alert();
                }
                return;
            }
            lock = next;
            next = next._nextInMode;
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
    private synchronized void internalChange( int heldMode, int newMode,
                                              LockOwner owner, int ms )
        throws LockNotHeldException, LockNotGrantedException
    {
        Lock lock;

        // Make sure we hold the lock we are trying to upgrade. This step
        // is essential, since we acquire the new lock before giving up
        // on the old lock.
        switch ( heldMode ) {
        case LockMode.READ_INTENT:
            lock = _readIntent;
            break;
        case LockMode.READ:
            lock = _readLock;
            break;
        case LockMode.UPGRADE:
            lock = _upgradeLock;
            break;
        case LockMode.WRITE:
            lock = _writeLock;
            break;
        case LockMode.WRITE_INTENT:
            lock = _writeIntent;
            break;
        default:
            throw new IllegalArgumentException( "Lock mode " + heldMode + " is invalid" );
        }
        while ( lock != null ) {
            if ( lock._owner == owner )
                break;
            lock = lock._nextInMode;
        }
        if ( lock == null )
            throw new LockNotHeldException( "Lock not held by owner" );
        internalLock( newMode, owner, ms );
        internalUnlock( heldMode, owner );
    }


    /**
     * Drop all locks belonging to a specific owner.
     * Includes locks acquired by the owner and locks
     * acquired by childern of the owner. This method
     * drops lock on all subordinate locks sets as well.
     * <p>
     * This method is called by {@link LinkOwner#discard
     * discard} and {@link LinkOwner#dropLocks dropLocks}.
     * It should not call {@link LinkOwner#remove remove}
     * on the owner.
     *
     * @param owner The owner
     */
    protected synchronized void drop( LockOwner owner )
    {
        Lock        lock;
        Lock        next;
        Subordinate sub;

        // Drop all locks held by this owner.
        //
        // Start with the head of the list and look for a lock
        // held by the owner or a child of the owner. If lock
        // held by owner, remove it from the lock list.
        // Repeat with the rest of the lock list.
        lock = _readIntent;
        while ( lock != null && ( owner == lock._owner ||
                                  owner.isParentOf( lock._owner ) ) ) {
            lock = lock._nextInMode;
            _readIntent = lock;
        }
        if ( lock != null ) {
            next = lock._nextInMode;
            while ( next != null ) {
                if ( owner == next._owner || owner.isParentOf( next._owner ) ) {
                    next = next._nextInMode;
                    lock._nextInMode = next;
                } else {
                    lock = next;
                    next = next._nextInMode;
                }
            }
        }

        lock = _readLock;
        while ( lock != null && ( owner == lock._owner ||
                                  owner.isParentOf( lock._owner ) ) ) {
            lock = lock._nextInMode;
            _readLock = lock;
        }
        if ( lock != null ) {
            next = lock._nextInMode;
            while ( next != null ) {
                if ( owner == next._owner || owner.isParentOf( next._owner ) ) {
                    next = next._nextInMode;
                    lock._nextInMode = next;
                } else {
                    lock = next;
                    next = next._nextInMode;
                }
            }
        }

        lock = _upgradeLock;
        while ( lock != null && ( owner == lock._owner ||
                                  owner.isParentOf( lock._owner ) ) ) {
            lock = lock._nextInMode;
            _upgradeLock = lock;
        }
        if ( lock != null ) {
            next = lock._nextInMode;
            while ( next != null ) {
                if ( owner == next._owner || owner.isParentOf( next._owner ) ) {
                    next = next._nextInMode;
                    lock._nextInMode = next;
                } else {
                    lock = next;
                    next = next._nextInMode;
                }
            }
        }

        lock = _writeLock;
        while ( lock != null && ( owner == lock._owner ||
                                  owner.isParentOf( lock._owner ) ) ) {
            lock = lock._nextInMode;
            _writeLock = lock;
        }
        if ( lock != null ) {
            next = lock._nextInMode;
            while ( next != null ) {
                if ( owner == next._owner || owner.isParentOf( next._owner ) ) {
                    next = next._nextInMode;
                    lock._nextInMode = next;
                } else {
                    lock = next;
                    next = next._nextInMode;
                }
            }
        }

        lock = _writeIntent;
        while ( lock != null && ( owner == lock._owner ||
                                  owner.isParentOf( lock._owner ) ) ) {
            lock = lock._nextInMode;
            _writeIntent = lock;
        }
        if ( lock != null ) {
            next = lock._nextInMode;
            while ( next != null ) {
                if ( owner == next._owner || owner.isParentOf( next._owner ) ) {
                    next = next._nextInMode;
                    lock._nextInMode = next;
                } else {
                    lock = next;
                    next = next._nextInMode;
                }
            }
        }
                
        // Drop locks on all the subordinates.
        sub = _subordinate;
        while ( sub != null ) {
            sub._lockSet.drop( owner );
            sub = sub._next;
        }
        // Notify next blocked owner.
        _blocked.alert();
    }


    //----------------------------------------------------------------------------
    // Serialization
    //----------------------------------------------------------------------------


    /*
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
    */


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
