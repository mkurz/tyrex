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
 * $Id: LockMode.java,v 1.2 2000/08/28 19:01:46 mohammed Exp $
 */


package tyrex.concurrency;


/**
 * Enumerator for the lock modes. There are five lock modes:
 * <ul>
 * <li>Read lock - multiple allows, conflict with write locks
 * <li>Write lock - only one write lock allowed, conflict with
 *  any other lock
 * <li>Upgrade lock - allows read, conflicts with writes and
 *  upgrade, used to prevent deadlocks when upgrading
 * <li>Read intention - signals intention to acquire read lock
 * <li>Write intention - signals intention to acquire write lock
 * </ul>
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/08/28 19:01:46 $
 */
public final class LockMode
{


    /**
     * Read lock: Read locks conflicts with any write lock,
     * but allow multiple read locks.
     * <p>
     * Conflicts with: {@link #Write}, {@link #IntentionWrite}
     */
    public static final LockMode Read = new LockMode( "read" );
    

    /**
     * Write lock: Write locks conflicts with read and write
     * locks. They may serve as exclusive locks.
     * <p>
     * Conflicts with: all locks.
     */
    public static final LockMode Write = new LockMode( "write" );


    /**
     * Upgrade lock: Upgrade locks are read locks that conflict
     * with each other. Read and upgrade locks are allowed at the
     * same time, but multiple upgrade locks are not. Upgrade locks
     * are used to minimize deadlocks.
     * <p>
     * Conflicts with: {@link #Write}, {@link #IntentionWrite},
     * {@link #Upgrade}
     */
    public static final LockMode Upgrade = new LockMode( "upgrade" );


    /**
     * Read intention lock: Acquired to signal an intention to
     * acquire a read lock.
     * <p>
     * Conflicts with: {@link #Write}.
     */
    public static final LockMode IntentionRead = new LockMode( "intent-read" );


    /**
     * Write intention lock: Acquired to signal an intention to
     * acquire a write lock.
     * <p>
     * Conflicts with: {@link #Write}, {@link #Upgrade}, {@link Read}
     */
    public static final LockMode IntentionWrite = new LockMode( "intent-write" );


    private String _name;


    /**
     * Hidden constructor.
     */
    private LockMode( String name )
    {
        _name = name;
    }


    public String toString()
    {
        return _name;
    }

}
