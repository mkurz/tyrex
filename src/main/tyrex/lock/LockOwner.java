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
 * $Id: LockOwner.java,v 1.1 2001/03/22 20:28:07 arkin Exp $
 */


package tyrex.lock;


import java.util.HashSet;
import tyrex.tm.impl.ThreadContext;


/**
 * Represents a lock owner. A lock owner is an identity that owns
 * one or more locks in different lock sets. There is one lock
 * owner object per individual owner (transaction, thread, etc).
 * <p>
 * The lock owner transitions through three steps that represent
 * the two-phase lock model. The owner starts in the growing phase,
 * where the owner is allowed to acquire and release individual
 * locks. During commit or rollback, the owner transitions to the
 * shrinking phase, which prevents it from acquiring or releasing
 * any locks. The current phase is returned from {@link #getLockPhase
 * getLockPhase}.
 * <p>
 * Once commit or rollback has completed, the owner drops all its
 * locks by calling {@link #dropLocks dropLocks}. The shrinking
 * phase is a terminal state.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2001/03/22 20:28:07 $
 */
public abstract class LockOwner
{


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
    protected static final boolean SHRINKING = true;


    /**
     * Reference to the first in a single-linked list of locks
     * managed by this owner.
     */
    Lock   _firstLock;


    private LockOwner        _nextOwner;


    private LockOwner        _prevOwner;


    private boolean          _phase = GROWING;


    private static LockOwner _firstOwner;


    private static LockOwner _lastOwner;


    static int               _ownerCount;


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


    protected final boolean getPhase()
    {
        return _phase;
    }


    protected final void beforeCompletion()
    {
        _phase = SHRINKING;
    }


    protected synchronized final void afterCompletion()
    {
        Lock lock;

        _phase = SHRINKING;
        lock = _firstLock;
        while ( lock != null ) {
            lock._lockSet.drop( this, false );
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
     * Determines if this lock owner is related to a second owner
     * requesting a new lock. Returns true if both owners are
     * identical.
     * <p>
     * Two owners are related if the requesting owner can acquire
     * a lock held by this owner without conflict. From a transaction
     * perspective, the requesting owner must commit relative to the
     * existing owner (i.e. a nested transaction).
     *
     * @param requesting The requesting lock owner
     * @return True if requesting lock owner is related to this owner
     */
    protected abstract boolean isRelated( LockOwner requesting );


    /**
     * Returns the lock owner identifier, if the lock owner can be
     * resolved by calling {@link #getOwner(String) getOwner}.
     *
     * @return The lock owner identifier, or null
     */
    protected abstract String getIdentifier();


    /**
     * Returns an arrays with all the locks held by this owner.
     *
     * @return All locks helds by this owner
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
     * Called to remove the lock. This method is called when
     * a lock is droped or released to inform the owner.
     * This method is not called during {@link #dropLocks}.
     *
     * @param lock The lock being removed
     */
    synchronized boolean remove( Lock lock )
    {
        Lock next;
        Lock last;

        if ( lock == null )
            throw new IllegalArgumentException( "Argument lock is null" );
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
    static LockOwner getOwner()
    {
        return ThreadContext.getThreadContext().getLockOwner();
    }


    static LockOwner getOwner( String identifier )
    {
        return null;
    }


}
