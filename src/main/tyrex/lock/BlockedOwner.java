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
 * $Id: BlockedOwner.java,v 1.1 2001/03/22 20:28:07 arkin Exp $
 */


package tyrex.lock;


/**
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2001/03/22 20:28:07 $
 * @see LockMode
 * @see LockCoordinator
 */
final class BlockedOwner
{


    static final int            BLOCKED = 0;


    static final int            DONE    = 1;


    static final int            TIMEOUT = 2;


    BlockedOwner                _nextInSet;


    private BlockedOwner        _nextBlocked;


    private BlockedOwner        _prevBlocked;


    private static BlockedOwner _firstBlocked;


    private static BlockedOwner _lastBlocked;


    static int                  _blockedCount;


    final LockOwner             _owner;


    int                         _state = BLOCKED;


    BlockedOwner( LockOwner owner )
    {
        _owner = owner;
        synchronized ( BlockedOwner.class ) {
            if ( _firstBlocked == null ) {
                _firstBlocked = this;
                _lastBlocked = this;
            } else {
                _prevBlocked = _lastBlocked;
                _lastBlocked._nextBlocked = this;
            }
            ++_blockedCount;
        }
    }


    synchronized void remove()
    {
        synchronized ( BlockedOwner.class ) {
            if ( _firstBlocked == this ) {
                _firstBlocked = _nextBlocked;
                if ( _nextBlocked != null )
                    _nextBlocked._prevBlocked = null;
            } else {
                _prevBlocked._nextBlocked = _nextBlocked;
                if ( _nextBlocked != null )
                    _nextBlocked._prevBlocked = _prevBlocked;
            }
            if ( _lastBlocked == this ) {
                _lastBlocked = _prevBlocked;
                if ( _prevBlocked != null )
                    _prevBlocked._nextBlocked = null;
            } else {
                _nextBlocked._prevBlocked = _prevBlocked;
                if ( _prevBlocked != null )
                    _prevBlocked._nextBlocked = _nextBlocked;
            }
            --_blockedCount;
        }
    }


}
