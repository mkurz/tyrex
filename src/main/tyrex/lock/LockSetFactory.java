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
 * $Id: LockSetFactory.java,v 1.2 2001/03/13 03:14:57 arkin Exp $
 */


package tyrex.lock;


import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import tyrex.tm.TransactionDomain;


/**
 * A factory for creating new lock sets.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2001/03/13 03:14:57 $
 */
public final  class LockSetFactory
{


    public LockSetFactory()
    {
    }


    /**
     * Create a new lock set and lock set.
     *
     * @return A new lock set
     */
    public LockSet create()
    {
        return new LockSet( null, null );
    }


    /**
     * Create a new lock set and lock set.
     *
     * @param identifier The lock set identifier (may be null)
     * @return A new lock set
     */
    public LockSet create( String identifier )
    {
        return new LockSet( identifier, null );
    }


    /**
     * Creates a new lock set that is related to an existing lock set.
     * Related lock sets drop their locks together.
     *
     * @param identifier The lock set identifier (may be null)
     * @param related The related lock set
     * @return A new lock set
     */
    public LockSet createRelated( String identifier, LockSet related )
    {
        return new LockSet( identifier, related );
    }


}
