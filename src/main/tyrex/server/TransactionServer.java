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
 * $Id: TransactionServer.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.server;


import java.io.PrintWriter;
import java.io.IOException;
import java.security.AccessController;
import java.util.Enumeration;
import java.rmi.RemoteException;
import java.rmi.MarshalledObject;
import java.rmi.activation.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.*;
import javax.transaction.xa.*;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.otid_t;
import org.omg.CosTransactions.Inactive;
import tyrex.conf.Resources;
import tyrex.util.PoolManager;
import tyrex.util.PooledResources;
import tyrex.util.TimeoutException;
import tyrex.util.Messages;
import tyrex.util.Logger;


/**
 * The core of the transaction server. This object represents the
 * transaction server as a configurable server, performs lookup on
 * local and remote transactions, serves as the transaction factory
 * and performs other duties.
 * <p>
 * This object is not accessed directly by any application code.
 * Instead application code deals with {@link TransactionManagerImpl},
 * {@link UserTransactionImpl} and {@link Configure}.
 * <p>
 * The server implements the {@link RemoteTransactionServer} interface
 * for transaction sharing with similar kind servers. Client-server
 * propogation occurs through {@link RemoteUserTransaction}.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see Configure
 * @see Tyrex
 * @see TransactionManagerImpl
 */
public final class TransactionServer
    extends Activatable
    implements RemoteTransactionServer, Heuristic, Runnable, PooledResources
{


    // IMPLEMENTATION NOTES:
    //
    //   All access to transactions must be synchronzied against
    //   the transaction object itself to prevent changes to the
    //   transaction as it is being committed/rolledback.
    //
    //   All transactions have unique, non-repeatable global
    //   identifiers. If synchronizing against the transaction
    //   object itself, there is no need to synchronized access
    //   to _txTable.


    /**
     * The only instance of the transaction server in this JVM.
     */
    private static TransactionServer _instance;


    /**
     * The recovery log is used to record all transactions
     * so prepared transactions can be recovered in the
     * event of a server crash. This one is obtained from the
     * configuration object and kept here for direct access.
     * It is set upon initialization, never changes and is
     * never null.
     */
    private static RecoveryLog     _recoveryLog;


    /**
     * The table of all transactions registered in the server.
     * We use the global identifier as the unique key, and no two
     * transactions will share the same global identifier.
     * There are special requirements for using this table, see
     * {@link XidHashtable} for more info. Specifically:
     * <ul>
     * <li>We should not add and remove the same transaction
     * at once, so all access should be synchronized on the
     * transaction object, not the hashtable
     * <li>The enumerators are thread-safe and allow us to
     * remove as we enumerate
     * </ul>
     */
    private static XidHashtable    _txTable = new XidHashtable();


    /**
     * The background thread that terminates timed out transactions.
     */
    private static Thread          _background;


    /**
     * The number of transactions currently active (i.e. associated
     * with some thread).
     */
    private static int             _activeCount;


    /**
     * The configuration object for this transaction server.
     */
    private static Configure       _config;


    /**
     * The pool manager to control number of active and registered
     * transaction in this server. Obtained from the configuration
     * on initialization.
     */
    private static PoolManager     _poolManager;


    /**
     * Counts how many transactions have been created in the life
     * time of the server.
     */
    private static long            _createdCounter;


    /**
     * Counts how many transactions have been forcefully terminated
     * in the life time of the server.
     */
    private static long            _terminatedCounter;


    /**
     * The state of the server as a server.
     */
    private static int             _status = Configure.Status.Inactive;




    /**
     * Activation constructor. Used during activation.
     */
    public TransactionServer( ActivationID id, MarshalledObject data )
	throws RemoteException
    {
	super( id, 0 );

	// This constructor gets called when we are activated,
	// but also when we are already there. Need to make sure
	// we don't try to start twice.
	if ( _instance == null ) {
	    Configure config;

	    _instance = this;
	    // Try to start the server with the specified configuation,
	    // if we fail, start it with the default configuration.
	    config = null;
	    try {
		if ( data != null )
		    config = (Configure) data.get();
	    } catch ( Exception except ) { }
	    if ( config == null )
		config = Configure.createDefault();
	    start( config );
	}
    }


    /**
     * Private constructor. Use {@link #getInstance} instead.
     */
    private TransactionServer()
	throws Exception
    {
	super( null,0 );
    }


    /**
     * Returns an instance of the transaction server. If the server
     * was not started through {@link Configure} it will be started
     * with a default configuration. Failure to restart the server
     * will throw a runtime exception. The server is not restarted if
     * if has been shut down.
     *
     * @return An instance of the transaction server
     */
    public static synchronized TransactionServer getInstance()
    {
	if ( _instance == null ) {
	    // If instance is null, the server was never started,
	    // so we start it now. The restart process will recurse
	    // into this method call again, but with _instance set.
	    try {
		start( Configure.createDefault() );
	    } catch ( Exception except ) {
		Logger.getLogger().println(
		    Messages.format( "tyrex.server.failedInitialize", except ) );
		throw new RuntimeException( Messages.format( "tyrex.server.failedInitialize", except ) );
	    }
	}
	return _instance;
    }


    public int getPooledCount()
    {
	return _txTable.size() - _activeCount;
    }


    public int getActiveCount()
    {
	return _activeCount;
    }


    public void releasePooled( int count )
    {
    }


    /**
     * Returns a count of how many transactions have been created
     * in the life time of the server.
     */
    static long getCreatedCounter()
    {
	return _createdCounter;
    }


    /**
     * Returns a counts of how many transactions have been
     * forcefully terminated in the life time of the server.
     */
    static long getTerminatedCounter()
    {
	return _terminatedCounter;
    }


    /**
     * Called by {@link Configure} to start the server with a given
     * configuration. Subsequent calls to this method will reset the
     * server with changes done to the configuration object. Once this
     * method has been called with a configuration, any attempt to call
     * it with a different configuration will throw a security
     * exception. If the server was started with {@link #getInstance},
     * {@link #getConfigure} must be used to restart it. The caller
     * must have the {@link TransactionServerPermission.Server#Start}
     * permission to perform this operation.
     *
     * @param config The configuration object
     */
    static synchronized void start( Configure config )
    {
 	// Make sure we have sufficient privileges
	AccessController.checkPermission( TransactionServerPermission.Server.Start );
	if ( config == null )
	    throw new NullPointerException( "Argument 'config' is null" );

	// Need to create a new instance of the server first.
	// Other method calls in this operation will require
	// access to the instance through getInstance().
	if ( _instance == null ) {
	    try {
		_instance = new TransactionServer();
	    } catch ( Exception except ) {
		Logger.getLogger().println(
		    Messages.format( "tyrex.server.failedInitialize", except ) );
		except.printStackTrace( Logger.getLogger() );
		throw new RuntimeException( Messages.format( "tyrex.server.failedInitialize", except ) );
	    }
	}

	// Synchronize on the single instance to prevent
	// concurrent shutdown
	synchronized ( _instance ) {
	    
	    if ( _status == Configure.Status.Inactive ) {
		Package pkg;
		
		// This is the first time the server is started. We need
		// to start using the configuration and bring up the
		// server components. If we are starting after shutdown,
		// make sure we are using the same configuration object.
		
		if ( _config != null && _config != config ) {
		    Logger.getLogger().println(
		        Messages.message( "tyrex.server.serverNotSameConfig" ) );
		    throw new SecurityException( Messages.message( "tyrex.server.serverNotSameConfig" ) );
		}
		
		_status = Configure.Status.Starting;
		_config = config;
		
		Logger.getLogger().println(
		    Messages.message( "tyrex.server.serverStart" ) );
		pkg = _instance.getClass().getPackage();
		if ( pkg != null ) {
		    Logger.getLogger().println( pkg.getImplementationTitle() +
						"  Version " +
						pkg.getImplementationVersion() );
		    Logger.getLogger().println( pkg.getImplementationVendor() );
		}

		// Use the pool manager and recovery log specified in
		// the configuration object.
		_poolManager = _config.getPoolManager();
		_poolManager.manage( _instance, false );
		_recoveryLog = _config.getRecoveryLog();
		if ( _recoveryLog == null ) {
		    _recoveryLog = new NullRecoveryLog();
		    _config.setRecoveryLog( _recoveryLog );
		}
		// Start the background thread that will terminate
		// transactions upon timeout.
		_background = new Thread( _instance, Messages.message( "tyrex.server.deamonName" ) );
		_background.setPriority( Thread.MIN_PRIORITY );
		_background.setDaemon( true );
		_background.start();

	    } else  if ( _status == Configure.Status.Active ) {

		RecoveryLog recoveryLog;

		// The server has been started before, we are merely
		// reseting the configuration. Make sure we are using
		// the same configuration object to do that.

		if ( _config != config ) {
		    Logger.getLogger().println( 
		        Messages.message( "tyrex.server.serverNotSameConfig" ) );
		    throw new SecurityException( Messages.message( "tyrex.server.serverNotSameConfig" ) );
		}

		_status = Configure.Status.Starting;
		Logger.getLogger().println(
		    Messages.message( "tyrex.server.serverRestart" ) );

		if (  _poolManager != null )
		    _poolManager.unmanage();
		// Load new configuration information from disk
		try {
		    config.refresh();
		} catch ( IOException except ) { }

		_poolManager = _config.getPoolManager();
		_poolManager.manage( _instance, false );
		// We cannot at any point set _recoveryLog to null,
		// so we need to use an intermediate.
		recoveryLog = _config.getRecoveryLog();
		if ( recoveryLog == null ) {
		    _recoveryLog = new NullRecoveryLog();
		    _config.setRecoveryLog( _recoveryLog );
		} else
		    _recoveryLog = _recoveryLog;

	    }

	    // Reload the resources configuration file. This also
	    // works for the first time.
	    Resources.reloadResources();
		
	    // Notify all the threads waiting to use this server
	    // (createTransaction, getTransaction, etc) that it's
	    // available once more.
	    _status = Configure.Status.Active;
	    _instance.notifyAll();
	}

	Logger.getLogger().println(
	    Messages.message( "tyrex.server.serverStarted" ) );
    }


    /**
     * Called by {@link Configure} to shutdown the server. Must be
     * called with the same configuration object that was used to
     * start the server. If the server was started with {@link
     * #getInstance}, {@link #getConfigure} must be used to restart it.
     * The caller must have the {@link
     * TransactionServerPermission.Server#Shutdown} permission to
     * perform this operation.
     *
     * @param config The configuration object
     */
    static void shutdown( Configure config )
    {
	Enumeration       enum;
	TransactionHolder txh;

	// Make sure we have sufficient privileges
	AccessController.checkPermission( TransactionServerPermission.Server.Shutdown );
	if ( config == null )
	    throw new NullPointerException( "Argument 'config' is null" );

	// No point in shutdown before start ;-)
	if ( _instance == null )
	    return;

	synchronized ( _instance ) {
	    
	    if ( config != _config ) {
		Logger.getLogger().println( 
		    Messages.message( "tyrex.server.serverNotSameConfig" ) );
		throw new SecurityException( Messages.message( "tyrex.server.serverNotSameConfig" ) );
	    }
	    if ( _status != Configure.Status.Active )
		return;
	    
	    _status = Configure.Status.Shutdown;
	    Logger.getLogger().println( 
	        Messages.message( "tyrex.server.serverShutdown" ) );
	    
	    // Unregister the local UserTransaction with JNDI.
	    try {
		InitialContext ctx;
		
		ctx = new InitialContext();
		if ( ctx.lookup( Configure.Names.UserTransaction ) == UserTransactionImpl.getInstance() )
		    ctx.unbind( Configure.Names.UserTransaction );
	    } catch ( NamingException except ) {
		Logger.getLogger().println( except );
	    }
	    
	    // Stop the background thread by interrupting it.
	    // If the background thread is busy terminating timedout
	    // transaction, it will proceed in its efforts.
	    _background.interrupt();
	    _background = null;
	    try {
		_background.join();
	    } catch ( InterruptedException except ) { }

	    // Manually terminate all the transactions that have
	    // not timed out yet.
	    enum = _txTable.elements();
	    while ( enum.hasMoreElements() ) {
		txh = (TransactionHolder) enum.nextElement();
		try {
		    terminateTransaction( txh );
		} catch ( Exception except ) { }
	    }

	    _status = Configure.Status.Active;
	}

	Logger.getLogger().println( 
	  Messages.message( "tyrex.server.serverStopped" ) );
    }


    /**
     * Called by {@link Configure} to determine the server status of
     * the transaction server.
     */
    static int status()
    {
	return _status;
    }


    /**
     * Obtains the configuration object use with this server.
     */
    static Configure getConfigure()
    {
	return _config;
    }


    /**
     * Called by several methods to determine if the server is active
     * and can service them. If the server is in the process of
     * starting or shutting down, this method will block for a few
     * seconds until the server comes back up. If the server does not
     * come up, this method will throw a {@link SystemException}.
     */
    private static void assureServerActive()
	throws SystemException
    {
	synchronized ( _instance ) {
	    if ( _status != Configure.Status.Active ) {
		try {
		    _instance.wait( _config.getPoolManager().getWaitTimeout() * 1000 );
		} catch ( InterruptedException except ) { }
		if ( _status != Configure.Status.Active )
		    throw new SystemException( Messages.message( "tyrex.server.serversInactive" ) );
	    }
	}
    }


    /**
     * Creates a new transaction. If <tt>parent</tt> is not null,
     * the transaction is nested within its parent. If <tt>activate</tt>
     * is true (and not nested) the transaction will be activated
     * for the current thread. Throws a {@link SystemException} if
     * we have reached the quota for new transactions or active
     * transactions, or the server has not been started.
     *
     * @param parent The parent transaction
     * @param activate True to activate the transaction with the
     *   current thread
     * @return The newly created transaction
     * @throws SystemException Reached the quota for new transactions
     */
    static TransactionImpl createTransaction( TransactionImpl parent,
					      boolean activate )
	throws SystemException
    {
	TransactionHolder txh;
	XidImpl           xid;

	// Create a new transaction with a new Xid, or a nested
	// transaction which is a branch in the parent transaction.
	if ( parent == null ) 
	    xid = new XidImpl();
	else
	    xid = parent.getXid().newBranch();
	txh = new TransactionHolder();
	txh.tx = new TransactionImpl( xid, parent );
	txh.started = System.currentTimeMillis();
	txh.timeout = _config.getTransactionTimeout() * 1000;

	// Nested transactions are not registered directly
	// with the transaction server. They are not considered
	// new creation/activation and are not subject to timeout.
	if ( parent != null )
	    return txh.tx;

	synchronized ( _poolManager ) {
	    assureServerActive();
	    // Ask the pool manager whether we can create this new
	    // transaction before we register it. (A non-registered
	    // transaction does not consume resources)
	    // At this point we might get a SystemException.
	    try {
		_poolManager.canCreateNew();
	    } catch ( TimeoutException except ) {
		throw new SystemException( Messages.message( "tyrex.server.txCreateExceedsQuota" ) );
	    }
	    // If we were requested to activate the transaction,
	    // ask the pool manager whether we can activate it,
	    // then associate it with the current thread.
	    if ( activate ) {
		try {
		    _poolManager.canActivate();
		    ++_activeCount;
		    txh.threads = new Thread[ 1 ];
		    txh.threads[ 0 ] = Thread.currentThread();
		} catch ( TimeoutException except ) {
		    throw new SystemException( Messages.message( "tyrex.server.txActiveExceedsQuota" ) );
		}
	    }

	    // The table has not yet seen this gxid, so we trust the
	    // put will be properly synchronized and do not need to
	    // synchronize the table.
	    // Nested transactions are not registered directly
	    // with the transaction server.
	    _txTable.put( xid.getGlobalTransactionId(), txh );
	}
	++ _createdCounter;
	return txh.tx;
    }


    /**
     * Creates a new transaction but does not activate it yet.
     * Throws a {@link SystemException} if we have reached the quota
     * for new transactions or the server has not been started.
     * Returns the transaction's global identifier that the client
     * will recieve.
     *
     * @return The newly created transaction's global identifier
     * @throws SystemException Reached the quota for new transactions
     * @see RemoteTransactionServer
     */
    public byte[] createRemoteTransaction()
	throws SystemException
    {
	return createTransaction( null, false ).getXid().getGlobalTransactionId();
    }


    /**
     * Creates a new transaction to represent a remote OTS
     * transaction, but does not activate it yet. Throws a {@link
     * SystemException} if we have reached the quota for new
     * transactions or the server has not been started.
     * <p>
     * The newly created transaction will have a non-native Xid,
     * therefore it cannot be distributed across two machines using
     * the RMI interface but only through OTS propagation context.
     *
     * @param pgContext The OTS propagation context
     * @return The newly created transaction
     * @throws SystemException Reached the quota for new transactions
     * @see TransactionFactoryImpl
     * @see PropagationContext
     */
    static TransactionImpl recreateTransaction( PropagationContext pgContext )
	throws SystemException
    {
	TransactionHolder txh;
	XidImpl           xid;

	// Create a new transaction based on the propagation context.
	xid = new XidImpl();
	txh = new TransactionHolder();
	try {
	    txh.tx = new TransactionImpl( xid, pgContext );
	} catch ( Inactive except ) {
	    throw new SystemException( Messages.message( "tyrex.tx.inactive" ) );
	}
	txh.started = System.currentTimeMillis();
	txh.timeout = pgContext.timeout;

	synchronized ( _poolManager ) {
	    assureServerActive();
	    // Ask the pool manager whether we can create this new
	    // transaction before we register it. (A non-registered
	    // transaction does not consume resources)
	    // At this point we might get a SystemException.
	    try {
		_poolManager.canCreateNew();
	    } catch ( TimeoutException except ) {
		throw new SystemException( Messages.message( "tyrex.server.txCreateExceedsQuota" ) );
	    }
	    // The table has not yet seen this gxid, so we trust the
	    // put will be properly synchronized and do not need to
	    // synchronize the table.
	    _txTable.put( xid.getGlobalTransactionId(), txh );
	}
	++ _createdCounter;
	return txh.tx;
    }


    /**
     * Called by {@link RemoteUserTransaction} to obtain a transaction
     * created on this server remotely through it's global identifier.
     * Returns the transaction if it was found, throws an {@link
     * InvalidTransactionException} if the transaction has timed out
     * or cannot be located.
     * <p>
     * In a future version this method will be able to locate the
     * transaction on a remote server and provide a local interface
     * to the remote transaction.
     *
     * @param gxid The transaction global identifier
     * @return The transaction
     * @throws InvalidTransactionException The transaction cannot be
     *   located, the identifier is invalid, or the transaction has
     *   timed out
     */
    static TransactionImpl getTransaction( byte[] gxid )
	throws InvalidTransactionException
    {
	TransactionHolder txh;
	int               i;

	try {
	    assureServerActive();
	} catch ( SystemException except ) {
	    throw new InvalidTransactionException( except.getMessage() );
	}

	if ( gxid.length == XidImpl.GLOBAL_XID_LENGTH ) {
	    if ( ! XidImpl.isLocal( gxid ) )
		    throw new InvalidTransactionException( Messages.message( "tyrex.server.originateElsewhere" ) );
	    txh = (TransactionHolder) _txTable.get( gxid );
	    if ( txh == null || txh.tx == null )
		throw new InvalidTransactionException( Messages.message( "tyrex.server.txRemoteMissing" ) );
	    return txh.tx;
	} else
	    throw new InvalidTransactionException( Messages.message( "tyrex.server.xidIllegalFormat" ) );
    }


    /**
     * Called by {@link TransactionImpl#forget} to forget about the
     * transaction once it has been commited/rolledback. The
     * transaction will no longer be available to {@link
     * #getTransaction}. The transaction's association and global
     * identifier are forgotten as well as all thread associated with
     * it. Subsequent calls to {@link #getTransaction} and {@link
     * #getControl} will not be able to locate the transaction.
     *
     * @param tx The transaction to forget about
     */
    static void forgetTransaction( TransactionImpl tx )
    {
	TransactionHolder txh;
	int               i;

	// This method is synchronized so we know that the same
	// transaction identifier will not be removed twice at the
	// same time and do not need to synchronize the table.
	// We take into account that the transaction might have been
	// removed before.
	synchronized ( tx ) {
	    txh = (TransactionHolder) _txTable.remove( tx.getXid().getGlobalTransactionId() );
	    // If there were any threads associated with the transaction,
	    // we forget about them.
	    if ( txh != null ) {
		if ( txh.threads != null && txh.threads.length > 0 ) {
		    --_activeCount;
		    for ( i = 0 ; i < txh.threads.length ; ++i )
			txh.threads[ i ] = null;
		    txh.threads = null;
		}
		txh.tx = null;
	    }
	}
	// We notify the pool managed the a resource has been
	// released so threads waiting to create a new transaction
	// might proceed.
	_poolManager.released();
    }


    /**
     * Called to terminate a transaction in progress. If the
     * transaction already completed, it will simply be forgotten
     * and removed from the transaction table. If the transaction is
     * active, it will be rolled back with a timed-out flag and all
     * threads associated with it will be terminated. Unlike normal
     * transaction termination through {@link #forgetTransaction} this
     * method assumes something went wrong and the transaction (or
     * it's resources) are no longer useable.
     *
     * @param txh The holder the thread to terminate
     */
    private static void terminateTransaction( TransactionHolder txh )
    {
	int i;

	// Synchronize against the transaction to prevent any chance
	// of timeout during a commit/rollback.
	synchronized ( txh.tx ) {

	    // It is possible that we are looking at a transaction
	    // that just completed, or one that has been left in
	    // the transaction table on purpose (see run() ).
	    if ( txh.tx.getStatus() != Status.STATUS_ACTIVE &&
		 txh.tx.getStatus() != Status.STATUS_MARKED_ROLLBACK ) {
		_txTable.remove( txh.tx.getXid().getGlobalTransactionId() );
		if ( txh.threads != null && txh.threads.length > 0 ) {
		    --_activeCount;
		    for ( i = 0 ; i < txh.threads.length ; ++i )
			txh.threads[ i ] = null;
		    txh.threads = null;
		}
		txh.tx = null;
		_poolManager.released();
	    }
	    else {
		Xid      xid;
		
		// We need the Xid upfront since timedOut() will cause
		// us to forget all about this transaction.
		xid = txh.tx.getXid();
		try {
		    Thread[] threads = null;
		    
		    logMessage( Messages.format( "tyrex.server.timeoutTerminate", xid.toString() ) );
		    if ( txh.threads != null ) {
			threads = (Thread[]) txh.threads.clone();
			// No need to decrease active count, this is done
			// in forgetTransaction().
		    }
		    try {
			// This call will cause all the XA resources to
			// die, will forget the transaction and all its
			// association by calling TransactionImpl.forget()
			// and forgetTransaction().
			txh.tx.timedOut();
		    } catch ( Exception except ) {
			logMessage( Messages.format( "tyrex.server.timeoutTerminateError", xid.toString(), except ) );
		    }
		    
		    // Sadly we have no way of checking out whether we
		    // are in the middle of some important code. This will
		    // terminate the thread correctly, since the thread is
		    // not supposed to do any synchronization, and hopefully
		    // will not damage the resources.
		    if ( threads != null && _config.getThreadTerminate() )
			for ( i = 0 ; i < threads.length ; ++i ) {
			    try {
				threads[ i ].stop( new TransactionTimeoutException() );
			    } catch ( Exception except ) {
				// This will throw a security exception if we
				// have no permission to stop the thread.
			    }
			}
		    ++ _terminatedCounter;
		} catch ( Exception except ) {
		    logMessage( Messages.format( "tyrex.server.timeoutTerminateError", txh.tx.toString(), except ) );
		}
	    }
	}
    }


    /**
     * Called by {@link Tyrex} to terminate a transaction in progress.
     * If the transaction already completed, it will simply be forgotten
     * and removed from the transaction table. If the transaction is
     * active, it will be rolled back with a timed-out flag and all
     * threads associated with it will be terminated.
     *
     * @param tx The transaction to terminate
     * @throws InvalidTransactionException The transaction did not originate
     *   on this server, or has already been terminated
     */
    static void terminateTransaction( Transaction tx )
	throws InvalidTransactionException
    {
	TransactionHolder txh;

	if ( ! ( tx instanceof TransactionImpl ) )
	    throw new InvalidTransactionException( Messages.message( "tyrex.server.originateElsewhere" ) );
	txh = (TransactionHolder) _txTable.get( ( (TransactionImpl) tx ).getXid().getGlobalTransactionId() );
	if ( txh == null )
	    throw new InvalidTransactionException( Messages.message( "tyrex.tx.inactive" ) );
	terminateTransaction( txh );
    }


    /**
     * Called by {@link TransactionManager#setTransactionTimeout} to
     * change the timeout of the transaction and all the resources
     * enlisted with that transaction.
     *
     * @param tx The transaction
     * @param seconds The new timeout in seconds, zero to use the
     *   default timeout for all new transactions.
     * @see TransactionManager#setTransactionTimeout
     */
    static void setTransactionTimeout( TransactionImpl tx, int seconds )
    {
	TransactionHolder txh;

	// For zero we use the default timeout for all new transactions.
	if ( seconds == 0 )
	    seconds = _config.getTransactionTimeout();
	// Change the timeout for the transaction so the background
	// thread will not attempt to kill it (if it didn't already).
	// We then ask the transaction to change the timeout on all
	// the resources enlisted with it.
	synchronized ( tx ) {
	    txh = (TransactionHolder) _txTable.get( tx.getXid().getGlobalTransactionId() );
	    if ( txh != null ) {
		txh.timeout = seconds * 1000;
		if ( txh.tx != null )
		    txh.tx.setTransactionTimeout( seconds );
	    }
	}
    }


    /**
     * Called by {@link ControlImpl} to obtain the timeout on a
     * transaction for the purpose of the propagation context.
     *
     * @param tx The transaction
     * @return The transaction's timeout in seconds
     */
    static int getTransactionTimeout( TransactionImpl tx )
    {
	TransactionHolder txh;

	txh = (TransactionHolder) _txTable.get( tx.getXid().getGlobalTransactionId() );
	if ( txh == null )
	    return _config.getTransactionTimeout();
	else
	    return (int) ( txh.timeout ) / 1000;
    }


    /**
     * Called by {@link TransactionImpl#resume} to associate the
     * transaction with the thread. This will allow us to terminate
     * the thread when the transaction times out. If the transaction
     * has not been associated with any thread before, it now becomes
     * active. We ask the pool manager whether we can activate the
     * transaction and if a timeout occurs, we throw a system exception.
     *
     * @param tx The transaction
     * @param thread The thread to associate with the transaction
     * @throws SystemException The pool manager does not allow us to
     *   active the transaction
     */
    static void enlistThread( TransactionImpl tx, Thread thread )
        throws SystemException
    {
	TransactionHolder txh;

	synchronized ( tx ) {
	    txh = (TransactionHolder) _txTable.get( tx.getXid().getGlobalTransactionId() );
	    if ( txh != null ) {
		if ( txh.threads == null ) {
		    // We ask the pool manager whether we can activate
		    // another transaction. If we cannot, a timeout
		    // exception will cause us to throw a system exception.
		    try {
			synchronized ( _poolManager ) {
			    _poolManager.canActivate();
			    ++_activeCount;
			}
			txh.threads = new Thread[ 1 ];
			txh.threads[ 0 ] = thread;
		    } catch ( TimeoutException except ) {
			throw new SystemException( Messages.message( "tyrex.server.txActiveExceedsQuota" ) );
		    }
		} else {
		    // We use arrays because supposedly they are faster
		    // than vectors. We don't expect to reach this point
		    // too much, only when two threads share the same
		    // transaction.
		    Thread[] newList;
		    
		    newList = new Thread[ txh.threads.length + 1 ];
		    System.arraycopy( txh.threads, 0, newList, 0, txh.threads.length );
		    newList[ txh.threads.length ] = thread;
		    txh.threads = newList;
		}
	    }
	}
    }
	
	
    /**
     * Called by {@link TransactionImpl#resume} to dissociate the
     * transaction from the thread. If the transaction has only been
     * associated with this one thread, it becomes inactive.
     *
     * @param tx The transaction
     * @param thread The thread to dissociate from the transaction
     * @see enlistThread
     */
    static void delistThread( TransactionImpl tx, Thread thread )
    {
	TransactionHolder txh;

	synchronized ( tx ) {
	    txh = (TransactionHolder) _txTable.get( tx.getXid().getGlobalTransactionId() );
	    if ( txh != null && txh.threads != null ) {
		// If only one thread has been associated with the
		// transaction and this is the thread we are
		// dissociating, then the thread becomes inactive.
		// Notify the pool manager that another transaction
		// can become active.
		if ( txh.threads.length == 1 ) {
		    if ( txh.threads[ 0 ] == thread ) {
			txh.threads = null;
			--_activeCount;
			_poolManager.released();
		    }
		} else {
		    // We use arrays because supposedly they are faster
		    // than vectors. We don't expect to reach this point
		    // too much, only when two threads share the same
		    // transaction.
		    Thread[] newList;
		    int      i;
		    
		    for ( i = 0 ; i < txh.threads.length ; ++i )
			if ( txh.threads[ i ] == thread ) {
			    txh.threads[ i ] = txh.threads[ txh.threads.length - 1 ];
			    newList = new Thread[ txh.threads.length - 1 ];
			    System.arraycopy( txh.threads, 0, newList, 0, txh.threads.length - 1 );
			    txh.threads = newList;
			    return;
			}
		}
	    }
	}
    }


    /**
     * Called by {@link TransactionImpl} to check whether the thread
     * is an owner of the transaction. Only owners are allowed to
     * commit/rollback a transaction. Owners are all the threads
     * previously associated with the transaction and the background
     * thread.
     *
     * @param tx The transaction
     * @param thread The thread asking to commit/rollback
     * @return True if the thread is an owner of the transaction
     */
    static boolean isOwner( TransactionImpl tx, Thread thread )
    {
	TransactionHolder txh;
	int               i;

	// The background thread can terminate any transaction.
	if ( thread == _background )
	    return true;
	txh = (TransactionHolder) _txTable.get( tx.getXid().getGlobalTransactionId() );
	if ( txh != null && txh.threads != null ) {
	    for ( i = 0 ; i < txh.threads.length ; ++i )
		if ( txh.threads[ i ] == thread )
		    return true;
	}
	return false;
    }


    /**
     * Returns an enumeration of all the transactions currently
     * registered with the server. Each entry is described using the
     * {@link TransactionStatus} object providing information about
     * the transaction, its resources, timeout and active state.
     * Some of that information is only current to the time the list
     * was produced.
     *
     * @return List of all transactions currently registered
     */
    static TransactionStatus[] listTransactions()
    {
	Enumeration          enum;
	TransactionStatus[]  txsList;
	TransactionHolder    txh;
	int                  index;

	txsList = new TransactionStatus[ _txTable.size() ];
	index = 0;
	enum = _txTable.elements();
	while ( enum.hasMoreElements() && index < txsList.length ) {
	    txh = (TransactionHolder) enum.nextElement();
	    if ( txh.tx != null ) {
		txsList[ index ] = new TransactionStatus( txh.tx, txh.started + txh.timeout, txh.threads != null );
		++index;
	    }
	}
	if ( index < txsList.length ) {
	    TransactionStatus[] newList;

	    newList = new TransactionStatus[ index ];
	    System.arraycopy( txsList, 0, newList, 0, index );
	    return newList;
	} else
	    return txsList;
    }


    /**
     * Returns information about the specified transactions using a
     * {@link TransactionStatus} object. Provides information about
     * the transaction, its resources, timeout and active state.
     * Some of that information is only current to the time the
     * request was made.
     *
     * @param tx The transaction
     * @return Information about that transaction, null if the
     *   transaction is no longer available
     */
    static TransactionStatus getTransactionStatus( Transaction tx )
    {
	TransactionStatus txs;
	TransactionHolder txh;

	txh = (TransactionHolder) _txTable.get( ( (TransactionImpl) tx ).getXid().getGlobalTransactionId() );
	if ( txh != null && txh.tx != null ) {
	    txs = new TransactionStatus( txh.tx, txh.started + txh.timeout, txh.threads != null );
	    return txs;
	}
	return null;
    }


    /**
     * Called to print the textual messages into the log writer
     * specified for this server.
     */
    static void logMessage( String message )
    {
	if ( _config.getLogWriter() != null )
	    _config.getLogWriter().println( message );
    }


    static void logTransaction( XidImpl xid, int heuristic )
    {
	if ( _recoveryLog == null )
	    return;

	switch ( heuristic ) {
	case BEGIN_TX:
	    _recoveryLog.beginTransaction( xid.getGlobalTransactionId() );
	    TransactionServer.logMessage( "Begin " + xid.toString() );
	    break;
	case HEURISTIC_COMMIT:
	    _recoveryLog.commitTransaction( xid.getGlobalTransactionId() );
	    TransactionServer.logMessage( "Commit " + xid.toString() );
	    break;
	case HEURISTIC_READONLY:
	    _recoveryLog.commitTransaction( xid.getGlobalTransactionId() );
	    TransactionServer.logMessage( "Read-only " + xid.toString() );
	    break;
	case HEURISTIC_ROLLBACK:
	    _recoveryLog.rollbackTransaction( xid.getGlobalTransactionId() );
	    TransactionServer.logMessage( "Rollback " +  xid.toString() );
	    break;
	case HEURISTIC_MIXED:
	    _recoveryLog.rollbackTransaction( xid.getGlobalTransactionId() );
	    TransactionServer.logMessage( "Mixed " + xid.toString() );
	    break;
	}
    }


    /**
     * Background thread that looks for transactions that have timed
     * out and terminates them. Will be running in a low priority for
     * as long as the server is active, monitoring the transaction
     * table and terminating threads in progress.
     * <p>
     * A transaction might time out while a client is not accessing
     * it. When the client tries to lookup the transaction, no
     * transaction is there to indicate the possibility of a timeout.
     * (This could happen, e.g. immediately after the transaction
     * thread has been terminated.) To ease the pain, the transaction
     * is actually removed in two phases: first terminated, then
     * after a wait period, forgotten about. All client accesses
     * between termination and forgetting will yield a transaction
     * that has rolled back.
     * <p>
     * This thread is terminated by interrupting it. The caller
     * must have {@link TransactionServerPermission.Server#Shutdown}
     * permission to interrupt it.
     */
    public void run()
    {
	TransactionHolder txh;
	Enumeration       enum;
	long              timeout;
	TransactionImpl   tx;

	while ( true ) {
	    try {
		Thread.sleep( _config.getPoolManager().getCheckEvery() * 1000 );
		
		// Our enumerator is guaranteed to remain valid even if
		// we just removed the entry we were looking at or some
		// other thread removed the next entry (that entry will
		// be marked so we will not time it out.
		timeout = System.currentTimeMillis();
		enum = _txTable.elements();
		while ( enum.hasMoreElements() ) {
		    txh = (TransactionHolder) enum.nextElement();
		    if ( ( txh.started + txh.timeout ) < timeout ) {
			// If the transaction is active at this point,
			// we remember it and reinstantiate it after
			// terminateTransaction will eradicate it.
			tx = txh.tx;
			if ( tx.getStatus() != Status.STATUS_ACTIVE &&
			     tx.getStatus() != Status.STATUS_MARKED_ROLLBACK )
			    tx = null;
			terminateTransaction( txh );
			if ( tx != null ) {
			    txh.tx = tx;
			    txh.timeout = _config.getPoolManager().getCheckEvery() * 1000;
			    _txTable.put( tx.getXid().getGlobalTransactionId(), txh );
			}
		    }
		}
	    } catch ( InterruptedException except ) {
		// Thread interruption will cause the thread to die
		// if the caller has sufficient privileges.
		try {
		    AccessController.checkPermission( TransactionServerPermission.Server.Shutdown );
		    return;
		} catch ( SecurityException except2 ) { }
	    }
	}
    }


}



/**
 * Holds information about a transaction, its projected timeout and
 * the threads associated with it. All transactions managed by this
 * server are held inside a class like this.
 */
class TransactionHolder
{
    
    /**
     * The transaction associated with this thread, or null if
     * there is no transaction in progress. Note, a transaction
     * might be associated with the thread but inactive.
     */
    TransactionImpl tx;
    
    
    /**
     * If the transaction is a local transaction it will be
     * associated with any number of currently running threads.
     * This association is required in order to stop the running
     * threads on timeout.
     */
    Thread[]      threads;
    
    
    /**
     * Indicates when the transaction will timeout as milliseconds
     * from its beginning.
     */
    long       timeout;
    
    
    /**
     * Indicates when the transaction started as current sysem time.
     */
    long       started;


}




