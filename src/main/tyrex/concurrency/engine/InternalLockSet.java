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
 * $Id: InternalLockSet.java,v 1.1 2000/04/10 20:51:51 arkin Exp $
 */


package tyrex.concurrency.engine;


import tyrex.concurrency.LockMode;
import tyrex.concurrency.LockNotHeldException;


/**
 * Implementation of a lock set.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/04/10 20:51:51 $
 * @see LockSetImpl
 * @see TransactionalLockSetImpl
 */
abstract class InternalLockSet
{


    /**
     * Number of locks in the array (5).
     */
    private static final int LockSetSize     = 5;

    /**
     * The index for an intent read lock.
     */
    private static final int LockIntentRead  = 0;

    /**
     * The index for a read lock.
     */
    private static final int LockRead        = 1;

    /**
     * The index for an upgrade lock.
     */
    private static final int LockUpgrade     = 2;

    /**
     * The index for an intent write lock.
     */
    private static final int LockIntentWrite = 3;

    /**
     * The index for a write lock.
     */
    private static final int LockWrite       = 4;



    /**
     * An array of locks, one for each lock mode.
     * Each entry is initial empty and can be filled up
     * with one or more locks.
     */
    private Lock[]       _locks = new Lock[ LockSetSize ];


    /**
     * A linked list of waiting threads. Used to track them
     * in FIFO order and notify the first-in of the next
     * release of a lock.
     */
    private Waiting      _waiting;


    /**
     * A linked list of all subordinate lock sets, which will
     * drop locks when this set drop locks.
     */
    private Subordinate  _subordinate;


    /**
     * A lock on behalf of an owner. The lock type is determined by
     * the index of the lock inside the lock array. Locks are linked
     * lists, since multiple owners may acquire non-conflicting locks
     * and the same owner may acquire multiple locks.
     */
    static class Lock
    {

        final Object   owner;

        Lock           next;

        Lock( Object owner, Lock next )
        {
            this.owner = owner;
            this.next = next;
        }

        Lock( Object owner )
        {
            this.owner = owner;
            this.next = null;
        }

    }


    /**
     * A linked list of threads blocked and waiting for the release
     * of a lock. Each thread adds itself to the end of the list, and
     * when notified checks to see if it's at the top.
     */
    static class Waiting
    {

        Waiting next;

    }


    /**
     * A record of a subordinate lock set. When this set drop locks,
     * so will all subordinate lock sets.
     */
    static class Subordinate
    {

        final InternalLockSet internal;

        final Subordinate     next;

        Subordinate( InternalLockSet internal, Subordinate next )
        {
            this.internal = internal;
            this.next = next;
        }
    }


    /**
     * Records conflicts between lock types. The lock mode is used as
     * an index to obtain an list of conflicts with that lock mode,
     * in the form of an array of lock modes.
     */
    private static int[][] _conflicts = new int[][] {
        // LockIntentRead
        { LockWrite },
        // LockRead
        { LockWrite, LockIntentWrite },
        // LockUpgrade
        { LockWrite, LockIntentWrite, LockUpgrade },
        // LockIntentWrite
        { LockWrite, LockUpgrade, LockRead },
        // LockWrite
        { LockWrite, LockIntentWrite, LockUpgrade, LockRead, LockIntentRead }
    };


    /**
     * Construct a new lock set. The related lock set is
     * specified, if known.
     */
    protected InternalLockSet( InternalLockSet related )
    {
        if ( related != null )
            related.addSubordinate( this );
    }


    /**
     * Obtains the lock type (integer) from the lock mode (enumerator).
     */
    int getLock( LockMode lockMode )
    {
        if ( lockMode == LockMode.Write )
            return LockWrite;
        if ( lockMode == LockMode.IntentionWrite )
            return LockIntentWrite;
        if ( lockMode == LockMode.Read )
            return LockRead;
        if ( lockMode == LockMode.IntentionRead )
            return LockIntentRead;
        if ( lockMode == LockMode.Upgrade )
            return LockUpgrade;
        throw new IllegalArgumentException( "Lock mode argument is invalid" );
    }


    /**
     * Called to acquire a lock of the specified type on behalf
     * of the specified owner.
     */
    synchronized void internalLock( int mode, Object owner )
    {
        // Try to acquire lock, return if successful.
        if ( internalTryLock( mode, owner ) )
            return;
        
        // This thread has to block until a lock becomes available.
        // Put a record in the FIFO linked list (_waiting) and
        // track your position (waiting).
        Waiting waiting;

        waiting = new Waiting();
        try {
            if ( _waiting == null )
                _waiting = waiting;
            else {
                Waiting last;

                last = _waiting;
                while ( last.next != null )
                    last = last.next;
                last.next = waiting;
            }
            // This loop will repeat indefinitly. It waits for a
            // notification that a lock has been released. If this
            // is the next waiting thread (FIFO), then it attempts
            // to acquire the lock, and if successful returns.
            while ( true ) {
                try {
                    wait();
                } catch ( InterruptedException except ) {
                }
                if ( _waiting == waiting && internalTryLock( mode, owner ) ) {
                    _waiting = _waiting.next;
                    return;
                }
            }
        } catch ( Throwable except ) {
            // An error has occured -- must remove this thread from
            // the waiting list, otherwise no other thread will be
            // able to acquire a lock.
            if ( _waiting == waiting )
                _waiting = _waiting.next;
            else {
                Waiting last;

                last = _waiting;
                while ( last.next != null ) {
                    if ( last.next == waiting ) {
                        last.next = last.next.next;
                        break;
                    }
                    last = last.next;
                }
            }
            notifyAll();
            // If this was an error or runtime exception, rethrow it.
            if ( except instanceof RuntimeException )
                throw (RuntimeException) except;
            if ( except instanceof Error )
                throw (Error) except;
        }
    }
    

    /**
     * Change from one lock mode to another by acquiring
     * one lock mode, and releasing the other.
     */    
    synchronized void internalChange( int heldMode, int newMode, Object owner )
        throws LockNotHeldException
    {
        internalLock( newMode, owner );
        internalUnlock( heldMode, owner );
    }


    /**
     * Attempt to acquire a lock of the specified type, return
     * true if the lock has been acquired, false if failed.
     * This method never blocks.
     */
    synchronized boolean internalTryLock( int mode, Object owner )
    {
        // If such a lock has been acquired, check the owner. If the
        // existing owner or a child, increase the lock count.
        if ( _locks[ mode ] != null ) {
            if ( isRelated( _locks[ mode ].owner, owner ) ) {
                _locks[ mode ] = new Lock( owner, _locks[ mode ] );
                return true;
            }
        }

        // Obtain a list of all conflicting mode. Check to see if can
        // acquire a lock on any of them by being related to the
        // existing owner.
        int[] conflicts;
        Lock  existing;

        conflicts = _conflicts[ mode ];
        for ( int i = 0 ; i < conflicts.length ; ++i ) {
            existing = _locks[ conflicts[ i ] ];
            while ( existing != null ) {
                if ( ! isRelated( existing.owner, owner ) )
                    return false;
                existing = existing.next;
            }
        }

        // No conflict detected, acquire the lock and return true;
        _locks[ mode ] = new Lock( owner, _locks[ mode ] );
        return true;
    }


    /**
     * Releases the specified lock for the specified owner.
     */
    synchronized void internalUnlock( int mode, Object owner )
        throws LockNotHeldException
    {
        Lock lock;

        lock = _locks[ mode ];
        if ( lock != null ) {
            // If owner of first lock, remove that lock.
            // If only lock, notify next thread waiting for lock.
            if ( lock.owner == owner ) {
                try {
                    _locks[ mode ] = lock.next;
                    notifyAll();
                } finally {
                    // Trick to assure we notify blocked threads,
                    // even if this thread has been terminated.
                    notifyAll();
                }
                return;
            } else
                // Iterate through lock, remove the one
                // that matches the owner.
                while ( lock.next != null ) {
                    if ( lock.next.owner == owner ) {
                        lock.next = lock.next.next;
                            return;
                    };
                    lock = lock.next;
                }
        }
        // No lock found for this owner.
        throw new LockNotHeldException();
    }


    /**
     * Drop all the locks belonging to a specific owner.
     */
    synchronized void internalDropLocks( Object owner )
    {
        // Iterate through all the locks held by this owner and
        // release them.
        for ( int i = 0 ; i < _locks.length ; ++i ) {
            Lock lock;

            while ( _locks[ i ] != null && isRelated( _locks[ i ].owner, owner ) )
                _locks[ i ] = _locks[ i ].next;
            lock = _locks[ i ];
            if ( lock != null ) {
                while ( lock.next != null ) {
                    if ( isRelated( lock.next.owner, owner ) )
                        lock.next = lock.next.next;
                    else
                        lock = lock.next;
                }
            }
        }
        // Drop locks on all the subordinates.
        Subordinate sub;

        sub = _subordinate;
        while ( sub != null ) {
            sub.internal.internalDropLocks( owner );
            sub = sub.next;
        }
        // Notify next thread waiting for lock.
        if ( _waiting != null )
            notifyAll();
    }


    /**
     * Determine if existing owner of lock (<tt>existing</tt>) is somehow
     * related to owner requesting new lock (<tt>requesting</tt>), and the
     * two are related such that acquiring a conflicting lock does not create
     * a conflict.
     */
    protected abstract boolean isRelated(  Object existing, Object requesting );


    /**
     * Called by the releated internal set to add itself as a subordinate.
     */
    private void addSubordinate( InternalLockSet internal )
    {
        _subordinate = new Subordinate( internal, _subordinate );
    }


}
