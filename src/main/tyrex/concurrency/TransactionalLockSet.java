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
 * $Id: TransactionalLockSet.java,v 1.3 2000/09/08 23:18:34 mohammed Exp $
 */


package tyrex.concurrency;


import org.omg.CosTransactions.Coordinator;


/**
 * The <tt>TransactionLockSet</tt> interface provides operations to acquire
 * and release locks on behalf of a specified transaction.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/08 23:18:34 $
 */
public interface TransactionalLockSet
{


    /**
     * Acquires a lock on the specified lock set in the specified mode.
     * If a lock is held on the same lock set in an incompatible mode by
     * another client then the operation will block until the lock is
     * acquired.
     *
     * @param which The requesting transaction
     * @param mode The requested lock mode
     */
    public void lock( Coordinator which, LockMode mode );


    /**
     * Attempts to acquire a lock on the specified lock set. If the lock
     * is already held in an incompatible mode by another client then
     * the operation returns false to indicate the lock could not be
     * acquired.
     *
     * @param which The requesting transaction
     * @param mode The requested lock mode
     * @return True if acquired, false if could not be acquired
     */
    public boolean tryLock( Coordinator which, LockMode mode );


    /**
     * Drops a single lock on the specified lock set in the specified
     * mode. A lock can be held multiple times in the same mode, in
     * which case the lock will be released only once.
     *
     * @param which The requesting transaction
     * @param mode The lock mode
     * @throws LockNotHeldException Called to drop a lock that was
     *  not held
     */
    public void unlock( Coordinator which, LockMode mode )
        throws LockNotHeldException;


    /**
     * Changes the mode of a single lock. If the new mode conflicts
     * with an existing lock held by an unrelated client, the this
     * method blocks until the mode can be granted.
     *
     * @param which The requesting transaction
     * @param heldMode The held lock mode
     * @param newMode The new lock mode
     * @throws LockNotHeldException Called to drop a lock that was
     *  not held
     */
    public void changeMode( Coordinator which, LockMode heldMode, LockMode newMode )
        throws LockNotHeldException;


    /**
     * Returns the lock coordinator associated with the specified
     * transaction.
     *
     * @param which The specified transaction
     */
    public LockCoordinator getCoordinator( Coordinator which );


}
