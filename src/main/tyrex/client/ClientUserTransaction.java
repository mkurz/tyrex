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
 * $Id: ClientUserTransaction.java,v 1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.client;


import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import javax.transaction.*;
import javax.transaction.xa.Xid;


/**
 * Implements the {@link UserTransaction} interface for the client
 * side. Can be used by the client to demarcate transactions in the
 * application server. The client interacts with the server through
 * the {@link RemoteUserTransaction} interface passing along the
 * transaction's global identifier. Client calls to the application
 * server should carry the transaction identifier, retrieving it
 * from {@link #getGlobalXid}.
 * <p>
 * There is a single list of transaction and thread association
 * per client JVM, regardless of how many client user transactions
 * have been created on that JVM. This object is thread safe.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see UserTransaction
 * @see RemoteUserTransaction
 */
public class ClientUserTransaction
    implements UserTransaction, Serializable
{


    /**
     * Keeps an association between the local thread and the
     * transaction associated with that thread. Holds objects of
     * type byte array representing the transaction's global
     * identifier.
     */
    private static transient ThreadLocal  _globalXid = new ThreadLocal();


    /**
     * Holds a reference to the remote user transaction through
     * which transactions are created and managed.
     */
    private static transient RemoteUserTransaction _remoteTx;


    /**
     * Resource bundle for exception messages.
     */
    private static ResourceBundle       _messages;


    /**
     * Names the resource bundle containing client messages.
     * The actual resource file will bear the extension
     * <tt>.properties</tt>.
     */
    private static final String MESSAGES_RESOURCE = "tyrex.client.messages";




    /**
     * Constructs a new client user transaction. Any number of client
     * user transactions may be created and used, they all share the
     * same transaction association list. This constructor will throw
     * an exception if it cannot contact the remote transaction server.
     * A connection attempt is perform only the first time this object
     * is created and is shared by all subsequently created objects.
     *
     * @throws RemoteException An error occured trying to access the
     *   remote transaction server
     */
    public ClientUserTransaction()
    	throws RemoteException
    {
	super();

	// This code happens only the first time we initiate a client
	// user transaction in this JVM. This code does not happen
	// in the static constructor, since we can't reliabily deal
	// with exceptions in the static constructor.
	synchronized ( getClass() ) {
	    if ( _messages == null ) {
		try {
		    _messages = ResourceBundle.getBundle( MESSAGES_RESOURCE );
		} catch ( MissingResourceException except ) { }
	    }

	    if ( _remoteTx == null ) {
		// Lookup the remote user transaction object and
		// throw an exception if it's inaccessible.
		try {
		    _remoteTx = (RemoteUserTransaction) Naming.lookup( RemoteUserTransaction.LOOKUP_NAME );
		} catch ( MalformedURLException except ) {
		    throw new RemoteException( getMessage( "tyrex.client.malformedURL" ) );
		} catch ( NotBoundException except ) {
		    throw new RemoteException( getMessage( "tyrex.client.notBound" ) );
		} catch ( RemoteException except ) {
		    throw new RemoteException( getMessage( "tyrex.client.error" ) + except.getMessage() );
		}
	    }
	}
    }



    public void begin()
	throws NotSupportedException, SystemException
    {
	byte[] gxid;

	gxid = (byte[]) _globalXid.get();
	// Nested transactions are not supported on the client side.
	if ( gxid != null )
	    throw new NotSupportedException( getMessage( "tyrex.client.nestedNotSupported" ) );
	try {
	    // Begin a remote transaction. This call will return the
	    // transaction's global identifier which we associate it
	    // with the thread and use it subsequently.
	    gxid = _remoteTx.begin();
	    _globalXid.set( gxid );
	} catch ( RemoteException except ) {
 	    // A SystemException indicates something is wrong in the
	    // transaction monitor or its communication with other
	    // resources.
	    if ( except.detail != null && ( except.detail instanceof SystemException ) )
		throw (SystemException) except.detail;
	    else
		throw new SystemException( except.getMessage() );
	}
    }


    public void commit()
	throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	       SecurityException, IllegalStateException, SystemException
    {
	byte[] gxid;

	gxid = (byte[]) _globalXid.get();
	if ( gxid == null )
	    throw new IllegalStateException( getMessage( "tyrex.client.txInactive" ) );
	try {
	    _remoteTx.commit( gxid );
	} catch ( RemoteException except ) {
 	    // A SystemException indicates something is wrong in the
	    // transaction monitor or its communication with other
	    // resources.
	    if ( except.detail != null && ( except.detail instanceof SystemException ) )
		throw (SystemException) except.detail;
	    else
		throw new SystemException( except.getMessage() );
	} finally {
	    // Transaction is no longer associated with this thread.
	    _globalXid.set( null );
	}
    }


    public void rollback()
	throws IllegalStateException, SecurityException, SystemException
    {
	byte[] gxid;

	gxid = (byte[]) _globalXid.get();
	if ( gxid == null )
	    throw new IllegalStateException( getMessage( "tyrex.client.txInactive" ) );
	try {
	    _remoteTx.rollback( gxid );
	} catch ( RemoteException except ) {
 	    // A SystemException indicates something is wrong in the
	    // transaction monitor or its communication with other
	    // resources.
	    if ( except.detail != null && ( except.detail instanceof SystemException ) )
		throw (SystemException) except.detail;
	    else
		throw new SystemException( except.getMessage() );
	} finally {
	    // Transaction is no longer associated with this thread.
	    _globalXid.set( null );
	}
    }


    public void setRollbackOnly()
	throws IllegalStateException, SystemException
    {
	byte[] gxid;

	gxid = (byte[]) _globalXid.get();
	if ( gxid == null )
	    throw new IllegalStateException( getMessage( "tyrex.client.txInactive" ) );
	try {
	    _remoteTx.setRollbackOnly( gxid );
	} catch ( RemoteException except ) {
 	    // A SystemException indicates something is wrong in the
	    // transaction monitor or its communication with other
	    // resources.
	    if ( except.detail != null && ( except.detail instanceof SystemException ) )
		throw (SystemException) except.detail;
	    else
		throw new SystemException( except.getMessage() );
	}
    }


    public int getStatus()
	throws SystemException
    {
	byte[] gxid;

	gxid = (byte[]) _globalXid.get();
        // If thread is not associated with any transaction, we return
	// the no transaction status. This stauts will be returned at
	// the end of a transaction if we initiated begin/commit.
	if ( gxid == null )
	    return Status.STATUS_NO_TRANSACTION;
	else {
	    try {
		return _remoteTx.getStatus( gxid );
	    } catch ( RemoteException except ) {
		// A SystemException indicates something is wrong in the
		// transaction monitor or its communication with other
		// resources.
		if ( except.detail != null && ( except.detail instanceof SystemException ) )
		    throw (SystemException) except.detail;
		else
		    throw new SystemException( except.getMessage() );
	    }
	}
    }


    public void setTransactionTimeout( int seconds )
	throws SystemException
    {
	byte[] gxid;

	gxid = (byte[]) _globalXid.get();
	if ( gxid != null ) {
	    try {
		_remoteTx.setTransactionTimeout( gxid, seconds );
	    } catch ( RemoteException except ) {
		// A SystemException indicates something is wrong in the
		// transaction monitor or its communication with other
		// resources.
		if ( except.detail != null && ( except.detail instanceof SystemException ) )
		    throw (SystemException) except.detail;
		else
		    throw new SystemException( except.getMessage() );
	    }
	}
    }


    /**
     * Returns the global identifier of the transaction associated with
     * the current thread, or null if the thread is not associated with
     * any transaction. Use this identifier when invoking the server
     * in the context of this transaction.
     *
     * @return The transaction's global identifier or null
     */
    public static byte[] getGlobalXid()
    {
	byte[] gxid;

	gxid = (byte[]) _globalXid.get();
	return gxid;
    }


    /**
     * Obtains a message from the resource bundle.
     */
    private static synchronized String getMessage( String name )
    {
	if ( _messages == null )
	    return name;
	try {
	    return _messages.getString( name );
	} catch ( MissingResourceException except ) {
	    return name;
	}
    }


}


