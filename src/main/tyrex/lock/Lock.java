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
 * $Id: Lock.java,v 1.2 2001/03/23 03:57:48 arkin Exp $
 */


package tyrex.lock;


/**
 * Represents a lock in a specific mode on behalf of an owner.
 * <p>
 * Each lock set contains a linked list of locks, one linked list
 * per lock mode. Each linked list contains zero or more locks,
 * one for each owner holding a lock in this particular mode.
 * Each such lock set, lock mode, lock owner association is
 * represented by this object.
 * <p>
 * Each lock owner maintains a linked list of all locks held
 * by that owner. The linked list allows the owner to drop
 * all locks across all lock sets.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2001/03/23 03:57:48 $
 */
final class Lock
{


    /**
     * The owner of the lock.
     */
    final LockOwner          _owner;


    /**
     * The lock set to which this lock belongs.
     */
    final LockSet            _lockSet;


    /**
     * A reference count. The initial value for a new lock is 1.
     * This reference count is incremented each time the lock
     * is acquired on behalf of the same owner, and decremented
     * each time the lock is released by its owner. Managed by
     * {@link LockSet}.
     */
    int                      _count = 1;


    /**
     * Reference to the next lock in a single linked list of
     * locks maintained by the lock set for a particular lock
     * mode. Managed by {@link LockSet}.
     */
    Lock                     _nextInMode;


    /**
     * Reference to the next lock in a single linked list of
     * locks maintained by the lock owner across all lock
     * sets. Managed by {@link LockOwner}.
     */
    Lock                     _nextInOwner;


    /**
     * Creates a new lock on behalf of an owner. The lock is
     * created with a reference count of 1.
     *
     * @param lockSet The lock set
     * @param owner The lock owner
     * @throws LockNotGrantedException Attempt to add lock
     * while in the shrinking phase
     */
    Lock( LockSet lockSet, LockOwner owner )
        throws LockNotGrantedException
    {
        if ( owner == null )
            throw new IllegalArgumentException( "Argument owner is null" );
        if ( lockSet == null )
            throw new IllegalArgumentException( "Argument lockSet is null" );
        _owner = owner;
        _lockSet = lockSet;
        owner.add( this );
    }


}
