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
 * $Id: UserTransactionImpl.java,v 1.3 2000/09/23 00:10:50 mohammed Exp $
 */


package tyrex.tm;


import java.util.Hashtable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.transaction.*;
import javax.transaction.xa.*;


/**
 * Simple implementation of the {@link UserTransaction} interface.
 * This is a local implementation exposed to local users through JNDI
 * lookup or through the application server. This interface is
 * decoupled from {@link TransactionManagerImpl} to prevent unwanted
 * casting, since the later is public.
 * <p>
 * To obtain the user transaction use either {@link Tyrex} or look
 * it up through JNDI (<tt>java:/comp/UserTransaction</tt>).
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/23 00:10:50 $
 */
public final class UserTransactionImpl
    implements UserTransaction
{


    /**
     * The only instance of the transaction manager in this JVM.
     */
    private TransactionManager   _txManager;



    /**
     * Private constructor. Use {@link #getInstance} instead.
     */
    public UserTransactionImpl( TransactionManager txManager )
    {
	if ( txManager == null )
	    throw new IllegalArgumentException( "Argument 'txManager' is null" );
	_txManager = txManager;
    }


    public void begin()
	throws NotSupportedException, SystemException
    {
	_txManager.begin();
    }


    public void commit()
	throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	       SecurityException, IllegalStateException, SystemException
    {
	_txManager.commit();
    }


    public void rollback()
	throws IllegalStateException, SecurityException, SystemException
    {
	_txManager.rollback();
    }


    public int getStatus()
	throws SystemException
    {
	return _txManager.getStatus();
    }


    public void setRollbackOnly()
	throws IllegalStateException, SystemException
    {
	_txManager.setRollbackOnly();
    }


    public void setTransactionTimeout( int timeout )
	throws SystemException
    {
	_txManager.setTransactionTimeout( timeout );
    }


    /*
    public Reference getReference()
    {
	Reference ref;
	Package   pkg;

	// We use same object as factory.
	ref = new Reference( getClass().getName(), getClass().getName(), null );
	// No properties, the entire transaction manager is static.
	pkg = UserTransactionImpl.class.getPackage();
	if ( pkg != null ) {
	    ref.add( new StringRefAddr( "title", pkg.getImplementationTitle() ) );
	    ref.add( new StringRefAddr( "vendor", pkg.getImplementationVendor() ) );
	    ref.add( new StringRefAddr( "version", pkg.getImplementationVersion() ) );
	}
 	return ref;
    }


    public Object getObjectInstance( Object refObj, Name name, Context nameCtx, Hashtable env )
    {
	Reference ref;

	// Can only reconstruct from a reference.
	if ( refObj instanceof Reference ) {
	    return this;
	} else if ( refObj instanceof Remote )
	    return refObj;
	else
	    return null;
    }


    public static UserTransaction getInstance()
    {
	return new UserTransactionImpl();
    }

    */


}
