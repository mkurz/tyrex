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
 * $Id: LockCoordinator.java,v 1.1 2001/03/12 19:39:15 arkin Exp $
 */


package tyrex.lock;


/**
 * The lock coordinator is used to drop all locks held by a
 * particular owner. Dropping all locks at once assures the lock
 * set and all related lock sets remain in a consistent state.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2001/03/12 19:39:15 $
 */
public final class LockCoordinator
{


    /**
     * The lock set.
     */
    private final LockSet  _lockSet;


    /**
     * The owner.
     */
    private final Object   _owner;


    /**
     * Constructs a new coordinator to drop all locks on the specified
     * locks set on behalf of the specified owner.
     */
    LockCoordinator( LockSet lockSet, Object owner )
    {
        if ( lockSet == null )
            throw new IllegalArgumentException( "Argument lockSet is null" );
        if ( owner == null )
            throw new IllegalArgumentException( "Argument owner is null" );
        _lockSet = lockSet;
        _owner = owner;
    }


    /**
     * Releases all locks helds by the owner. Includes both locks in
     * this lock set and related lock sets.
     * <p>
     * For nested transactions this operation must be called when
     * the nested transaction aborts, or when the parent transaction
     * commits.
     */
    public void dropLocks()
    {
        _lockSet.internalDropLocks( _owner );
    }


}
