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
 * $Id: TransactionalLockSetImpl.java,v 1.2 2000/08/28 19:01:47 mohammed Exp $
 */


package tyrex.concurrency.engine;


import org.omg.CosTransactions.Coordinator;
import tyrex.concurrency.TransactionalLockSet;
import tyrex.concurrency.LockMode;
import tyrex.concurrency.LockCoordinator;
import tyrex.concurrency.LockNotHeldException;


/**
 * Implementation of a transactional lock set. The owner of each
 * lock is either the currently running transaction.
 * <p>
 * <b>Note:</b> NOT FULLY IMPLEMENTED
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/08/28 19:01:47 $
 */
public final class TransactionalLockSetImpl
    extends InternalLockSet
    implements TransactionalLockSet
{


    TransactionalLockSetImpl( TransactionalLockSetImpl lockSet )
    {
        super( lockSet );
    }

    
    public void lock( Coordinator which, LockMode mode )
    {
        if ( which == null )
            throw new IllegalArgumentException( "Argument 'which' is null" );
        internalLock( getLock( mode ), which );
    }


    public boolean tryLock( Coordinator which, LockMode mode )
    {
        if ( which == null )
            throw new IllegalArgumentException( "Argument 'which' is null" );
        return internalTryLock( getLock( mode ), which );
    }


    public void unlock( Coordinator which, LockMode mode )
        throws LockNotHeldException
    {
        if ( which == null )
            throw new IllegalArgumentException( "Argument 'which' is null" );
        internalUnlock( getLock( mode ), which );
    }


    public void changeMode( Coordinator which, LockMode heldMode, LockMode newMode )
        throws LockNotHeldException
    {
        if ( which == null )
            throw new IllegalArgumentException( "Argument 'which' is null" );
        internalChange( getLock( heldMode ), getLock( newMode), which );
    }


    public LockCoordinator getCoordinator( Coordinator which )
    {
        if ( which == null )
            throw new IllegalArgumentException( "Argument 'which' is null" );
        return new LockCoordinatorImpl( this, which );
    }


    protected boolean isRelated( Object existing, Object requesting )
    {
        return ( ( (Coordinator) existing ).is_related_transaction( (Coordinator) requesting ) );
    }


}

