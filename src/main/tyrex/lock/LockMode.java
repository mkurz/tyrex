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
 * $Id: LockMode.java,v 1.1 2001/03/22 20:28:07 arkin Exp $
 */


package tyrex.lock;


/**
 * Values representing the five lock modes supported by a lock set:
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
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2001/03/22 20:28:07 $
 * @see LockSet
 */
public interface LockMode
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


}
