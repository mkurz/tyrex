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
 * $Id: LockOwner.java,v 1.2 2001/03/23 03:57:48 arkin Exp $
 */


package tyrex.lock;


import java.util.HashSet;
import tyrex.tm.impl.ThreadContext;


/**
 * Represents a lock owner. A lock owner is an identity that owns
 * one or more locks in multiple lock sets. A lock owner object
 * is used to identify the transaction or thread that owns these
 * locks.
 * <p>
 * The lock owner implements strict two-phase locking. In the
 * growing phase, the owner is allowed to acquire and release
 * individual locks, and to manually drop all locks. In the
 * shrinking phase, the owner is not allowed to acquire or
 * release any individual locks.
 * <p>
 * A thread implements lock ownership with a single phase.
 * Locks must be dropped manually using {@link LockSet.dropLocks()
 * dropLocks}. A lock owner can be associated with a thread context
 * for life.
 * <p>
 * A transaction implements lock ownership using strict two-phase
 * locking. During the commit/rollback phase, the transaction
 * shifts to the shrinking phase by calling {@link #shrinking
 * shrinking}. Once the transaction has been committed or
 * rolledback, all locks are drops by calling {@link #discard
 * discard}.
 * <p>
 * A transaction owner supports persistent locks by implementing
 * the {@link #getIdentifier getIdentifier} method and returning
 * the transaction identifier. Threads do not support persistent
 * locks.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2001/03/23 03:57:48 $
 */
public abstract class LockOwner
{


    // IMPLEMENTATION DETAILS:
    //
    //   The actual lock owner transitions the lock through the
    //   two phases and discards the owner object by calling
    //   shrinking() and discard().
    //
    //   When a new LockOwner object is created, it is automatically
    //   added to a global list of lock owners and the lock owners
    //   count is incremeneted. When a LockOwner is discarded (by
    //   calling discard()), the lock owner is removed from the
    //   list and the lock owners count is decremented.
    //
    //   The lock owner keeps track of all locks acquired or
    //   released by this owner. Keeping track of all locks (rather
    //   than lock sets) allows for efficient lock dropping.
    //   The methods add()/remove() must be called each time a lock
    //   is acquired/released on behalf of this owner, except during
    //   invocation of dropLocks().
    //
    //   The lock owner keeps a reference count of blocks on that
    //   owner. When it is discarded, it will ask BlockedOwner to
    //   release all blocks held on behalf of this owner.


    /**
     * The owner is in the growing phase. The owner is allowed to
     * acquire and release individual locks.
     */
    protected static final boolean GROWING = true;


    /**
     * The owner is in the shrinking phase. The owner is not
     * allowed to acquire or release locks. This is a terminal
     * state for an owner.
     */
    protected static final boolean SHRINKING = false;


    /**
     * Reference to the first in a single-linked list of locks
     * managed by this owner.
     */
    private Lock             _firstLock;


    /**
     * The current lock phase of this lock owner.
     */
    private boolean          _phase = GROWING;


    /**
     * Reference count recording how many blocks are placed
     * on this owner.
     */
    private int              _blocked;


    /**
     * The next owner in a double-linked list of lock owners.
     */
    private LockOwner        _nextOwner;


    /**
     * The previous owner in a double-linked list of lock owners.
     */
    private LockOwner        _prevOwner;


    /**
     * The first owner in a double-linked list of lock owners.
     */
    private static LockOwner _firstOwner;


    /**
     * The last owner in a double-linked list of lock owners.
     */
    private static LockOwner _lastOwner;


    /**
     * The number of owners available.
     */
    private static int       _ownerCount;


    /**
     * Default constructor.
     */
    protected LockOwner()
    {
        synchronized ( LockOwner.class ) {
            if ( _firstOwner == null ) {
                _firstOwner = this;
                _lastOwner = this;
            } else {
                _prevOwner = _lastOwner;
                _lastOwner._nextOwner = this;
            }
            ++_ownerCount;
        }
    }


    /**
     * Returns the phase of this lock owner. The return value
     * is either {@link #GROWING} or {@link #SHRINKING}.
     *
     * @return The phase of this lock owner
     */
    protected final boolean getPhase()
    {
        return _phase;
    }


    /**
     * Called prior to transaction completion to change the
     * owner phase to {@link #SHRINKING}, preventing any
     * individual locks from being acquired or released.
     */
    protected synchronized final void shrinking()
    {
        _phase = SHRINKING;
        // If lock owner is blocked, we need to terminate
        // all blocks.
        if ( _blocked > 0 ) {
            BlockedOwner.terminate( this, false );
        }
    }


    /**
     * Called after transaction completion to discard the
     * lock owner. All locks held by this owner are
     * automatically droped and the lock manager loses all
     * recollection of this lock owner. This method will
     * automatically call {@link #dropLocks dropLocks}.
     */
    protected synchronized final void discard()
    {
        Lock lock;

        _phase = SHRINKING;
        // Faster here than calling dropLocks.
        lock = _firstLock;
        while ( lock != null ) {
            lock._lockSet.drop( this );
            lock = lock._nextInOwner;
        }
        _firstLock = null;
        synchronized ( LockOwner.class ) {
            if ( _firstOwner == this ) {
                _firstOwner = _nextOwner;
                if ( _nextOwner != null )
                    _nextOwner._prevOwner = null;
            } else {
                _prevOwner._nextOwner = _nextOwner;
                if ( _nextOwner != null )
                    _nextOwner._prevOwner = _prevOwner;
            }
            if ( _lastOwner == this ) {
                _lastOwner = _prevOwner;
                if ( _prevOwner != null )
                    _prevOwner._nextOwner = null;
            } else {
                _nextOwner._prevOwner = _prevOwner;
                if ( _prevOwner != null )
                    _prevOwner._nextOwner = _nextOwner;
            }
            --_ownerCount;
        }
    }


    /**
     * Drops all locks associated with this owner.
     * <p>
     * This method cannot be called once in the shrinking
     * phase.
     */
    protected final synchronized void dropLocks()
    {
        Lock lock;

        // Don't do anything during the shrinking phase.
        // All locks will be dropped eventually.
        if ( _phase == SHRINKING )
            return;
        lock = _firstLock;
        while ( lock != null ) {
            lock._lockSet.drop( this );
            lock = lock._nextInOwner;
        }
        _firstLock = null;
    }


    /**
     * Returns an array of all locks sets in which this owner holds
     * one or more locks. The array contains no duplicate lock sets.
     * If the owner has no lock, the result is an empty array.
     *
     * @return All lock sets helds by this owner
     */
    protected final synchronized LockSet[] getLockSets()
    {
        Lock     lock;
        int      count;
        String[] locks;

        HashSet set;

        set = new HashSet();
        lock = _firstLock;
        while ( lock != null ) {
            set.add( lock._lockSet );
            lock = lock._nextInOwner;
        }
        return (LockSet[]) set.toArray( new LockSet[ set.size() ] );
    }


    /**
     * Determines if this owner is related to another owner
     * in a parent child relationship. Lock owners implement
     * this method.
     * <p>
     * This method returns true if the lock owner <tt>child</tt>
     * is a child of this lock owner. For a transaction, the
     * child relationship is identical to a nested transaction
     * relationship, returning true if the child owner commits
     * relative to this owner.
     *
     * @param child The child lock owner
     * @return True if a child of this owner
     */
    protected abstract boolean isParentOf( LockOwner child );


    /**
     * Returns the lock owner identifier for the purpose of
     * lock persistence. Lock owners implement this method.
     * <p>
     * If the lock owner can be resolved during recovery by
     * calling {@link #getOwner(String) getOwner}, this method
     * will return a suitable identifier. Otherwise, this method
     * returns null.
     *
     * @return The lock owner identifier, or null
     */
    protected abstract String getIdentifier();


    /**
     * Returns the actual lock owner object. Lock owners
     * implement this method.
     * <p>
     * For a transaction this method returns the transaction
     * object. For a thread this method returns the thread
     * object.
     *
     * @return The actual lock owner object
     */
    protected abstract Object getActualOwner();


    /**
     * Called to add a lock. This method is called when
     * a lock is acquired, informing the owner so it can
     * drop the lock during {@link #dropLocks}.
     *
     * @param lock The lock to add
     * @throws LockNotGrantedException Attempt to add lock
     * while in the shrinking phase
     */
    synchronized void add( Lock lock )
        throws LockNotGrantedException
    {
        if ( _phase == SHRINKING )
            throw new LockNotGrantedException( "Cannot acquire lock while in the shrinking phase" );
        if ( _firstLock == null )
            _firstLock = lock;
        else {
            lock._nextInOwner = _firstLock;
            _firstLock = lock;
        }
    }


    /**
     * Called to remove the lock. This method is called when
     * a lock is released or dropped to inform the owner that
     * the lock no longer exists. This method is not called
     * during {@link #dropLocks dropLocks} or {@link #discard
     * discard}.
     *
     * @param lock The lock to remove
     * @return True if the lock has been removed
     */
    synchronized boolean remove( Lock lock )
    {
        Lock next;
        Lock last;

        if ( lock == null )
            throw new IllegalArgumentException( "Argument lock is null" );
        // Don't do anything during the shrinking phase.
        // All locks will be dropped eventually.
        if ( _phase == SHRINKING )
            return false;
        // !!! Need to change to double-linked list if we
        // expect many locks held by a single transaction.
        next = _firstLock;
        if ( next == lock ) {
            _firstLock = next._nextInOwner;
            return true;
        }
        last = next;
        next = next._nextInOwner;
        while ( next != null ) {
            if ( next == lock ) {
                last._nextInOwner = next._nextInOwner;
                return true;
            }
            last = next;
            next = next._nextInOwner;
        }
        return false;
    }


    /**
     * Records that the owner is being blocked. This method
     * is called once each time an owner is being blocked
     * while attempting to acquire a lock.
     */
    final void addBlock()
    {
        ++_blocked;
    }


    /**
     * Records that the owner is no longer blocked. This method
     * is called once each time a block is released while
     * attempting to acquire a lock.
     */
    final void removeBlock()
    {
        --_blocked;
    }


    /**
     * Returns the lock owner associated with the current thread.
     * If the current thread is associated with a transaction, the
     * transaction's lock owner is returned. Otherwise, the thread
     * context's lock owner is returned.
     *
     * @return The lock owner associated with the current thread
     */
    static LockOwner getOwner()
    {
        return ThreadContext.getThreadContext().getLockOwner();
    }


    /**
     * Returns an owner from an identifier. This method is used
     * when locks are read from persistent storage. The identifier
     * is one returned by a call to {@link #getIdentifier getIdentifier}.
     * This method will return null if the lock owner cannot be
     * identified.
     *
     * @param identifier The lock owner identifier
     * @return The lock owner, or null
     */
    protected static LockOwner getOwner( String identifier )
    {
        return null;
    }


    /**
     * Returns a list of all lock owners. All owners of one or more
     * locks are returned without duplicates. If no locks are
     * owned, and empty array is returned.
     *
     * @return A list of all lock owners
     */
    static synchronized  Object[] getOwners()
    {
        LockOwner owner;
        Object[]  owners;

        owners = new Object[ _ownerCount ];
        owner = _firstOwner;
        for ( int i = 0 ; i < _ownerCount ; ++i ) {
            owners[ i ] = owner.getActualOwner();
            owner = owner._nextOwner;
        }
        return owners;
    }


}
