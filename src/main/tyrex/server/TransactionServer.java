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
 * $Id: TransactionServer.java,v 1.5 2000/09/08 23:06:04 mohammed Exp $
 */


package tyrex.server;


import java.io.PrintWriter;
import java.io.IOException;
import java.security.AccessController;
import java.util.Enumeration;
import java.util.Hashtable;
import java.rmi.RemoteException;
import java.rmi.MarshalledObject;
import java.rmi.activation.*;
import javax.transaction.*;
import javax.transaction.xa.*;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.otid_t;
import org.omg.CosTransactions.Inactive;
import tyrex.conf.Resources;
import tyrex.resource.ResourceLimits;
import tyrex.resource.ResourceTimeoutException;
import tyrex.tm.Heuristic;
import tyrex.tm.TransactionDomain;
import tyrex.tm.TyrexPermission;
import tyrex.tm.XidImpl;
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
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $ $Date: 2000/09/08 23:06:04 $
 * @see Configure
 * @see Tyrex
 * @see TransactionManagerImpl
 */
public final class TransactionServer
    extends Activatable
    implements RemoteTransactionServer
{


    /**
     * The name of the default domain ('Default'). A transaction domain with
     * this name always exists.
     */
    public static final String DefaultDomain = "Default";


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
     * The configuration object for this transaction server.
     */
    private static Configure       _config;


    /**
     * The state of the server as a server.
     */
    private static int             _status = Configure.Status.Inactive;


    /**
     * All the transaction domains listed with this server.
     */
    private static Hashtable       _txDomains = new Hashtable();



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
	    // XXX
	    /*
	    if ( config == null )
		config = Configure.createDefault();
	    */
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


    public synchronized static TransactionDomain getTransactionDomain( String name, boolean createNew )
    {
	TransactionDomain txDomain;

	txDomain = (TransactionDomain) _txDomains.get( name );
	if ( txDomain == null ) {
	    if ( createNew ) {
		txDomain = createTransactionDomain( name );
	    } else {
		txDomain = getTransactionDomain( DefaultDomain, true );
        }
	}
	return txDomain;
    }


    /**
     * Create the transaction domain with the specified name.
     *
     * @param name the name
     * @return the new transaction domain
     */
    private static TransactionDomain createTransactionDomain( String name )
    {
        TransactionDomain txDomain = new TransactionDomain( name, new ResourceLimits() );
        _txDomains.put( name, txDomain );
        return txDomain;
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
		// XXX
		///**
		start( new Configure() );
		//*/
	    } catch ( Exception except ) {
		    Logger.getSystemLogger().println(
		    Messages.format( "tyrex.server.failedInitialize", except ) );
		    throw new RuntimeException( Messages.format( "tyrex.server.failedInitialize", except ) );
	    }
	}
	return _instance;
    }


    /**
     * Called by {@link Configure} to start the server with a given
     * configuration. Subsequent calls to this method will reset the
     * server with changes done to the configuration object. Once this
     * method has been called with a configuration, any attempt to call
     * it with a different configuration will throw a security
     * exception. If the server was started with {@link #getInstance},
     * {@link #getConfigure} must be used to restart it. The caller
     * must have the {@link TyrexPermission.Server#Start}
     * permission to perform this operation.
     *
     * @param config The configuration object
     */
    static synchronized void start( Configure config )
    {
 	// Make sure we have sufficient privileges
	AccessController.checkPermission( TyrexPermission.Server.Start );
	if ( config == null )
	    throw new NullPointerException( "Argument 'config' is null" );

	// Need to create a new instance of the server first.
	// Other method calls in this operation will require
	// access to the instance through getInstance().
	if ( _instance == null ) {
	    try {
		_instance = new TransactionServer();
	    } catch ( Exception except ) {
		Logger.getSystemLogger().println(
		    Messages.format( "tyrex.server.failedInitialize", except ) );
		except.printStackTrace( Logger.getSystemLogger() );
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
		    Logger.getSystemLogger().println(
		        Messages.message( "tyrex.server.serverNotSameConfig" ) );
		    throw new SecurityException( Messages.message( "tyrex.server.serverNotSameConfig" ) );
		}
		
		_status = Configure.Status.Starting;
		_config = config;
		
		Logger.getSystemLogger().println(
		    Messages.message( "tyrex.server.serverStart" ) );
		pkg = _instance.getClass().getPackage();
		if ( pkg != null ) {
		    Logger.getSystemLogger().println( pkg.getImplementationTitle() +
						      "  Version " +
						      pkg.getImplementationVersion() );
		    Logger.getSystemLogger().println( pkg.getImplementationVendor() );
		}

		// Use the pool manager and recovery log specified in
		// the configuration object.
		_recoveryLog = _config.getRecoveryLog();
		if ( _recoveryLog == null ) {
		    _recoveryLog = new NullRecoveryLog();
		    _config.setRecoveryLog( _recoveryLog );
		}
		getTransactionDomain( DefaultDomain, true );

	    } else  if ( _status == Configure.Status.Active ) {

		RecoveryLog recoveryLog;

		// The server has been started before, we are merely
		// reseting the configuration. Make sure we are using
		// the same configuration object to do that.

		if ( _config != config ) {
		    Logger.getSystemLogger().println( 
		        Messages.message( "tyrex.server.serverNotSameConfig" ) );
		    throw new SecurityException( Messages.message( "tyrex.server.serverNotSameConfig" ) );
		}

		_status = Configure.Status.Starting;
		Logger.getSystemLogger().println(
		    Messages.message( "tyrex.server.serverRestart" ) );

		// Load new configuration information from disk
		// XXX
		/*
		try {
		    config.refresh();
		} catch ( IOException except ) { }
		*/

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

	Logger.getSystemLogger().println(
	    Messages.message( "tyrex.server.serverStarted" ) );
    }


    /**
     * Called by {@link Configure} to shutdown the server. Must be
     * called with the same configuration object that was used to
     * start the server. If the server was started with {@link
     * #getInstance}, {@link #getConfigure} must be used to restart it.
     * The caller must have the {@link
     * TyrexPermission.Server#Shutdown} permission to
     * perform this operation.
     *
     * @param config The configuration object
     */
    static void shutdown( Configure config )
    {
	Enumeration       enum;

	// Make sure we have sufficient privileges
	AccessController.checkPermission( TyrexPermission.Server.Shutdown );
	if ( config == null )
	    throw new NullPointerException( "Argument 'config' is null" );

	// No point in shutdown before start ;-)
	if ( _instance == null )
	    return;

	synchronized ( _instance ) {
	    
	    if ( config != _config ) {
		Logger.getSystemLogger().println( 
		    Messages.message( "tyrex.server.serverNotSameConfig" ) );
		throw new SecurityException( Messages.message( "tyrex.server.serverNotSameConfig" ) );
	    }
	    if ( _status != Configure.Status.Active )
		return;
	    
	    _status = Configure.Status.Shutdown;
	    Logger.getSystemLogger().println( 
	        Messages.message( "tyrex.server.serverShutdown" ) );
	    
	    enum = _txDomains.elements();
	    while ( enum.hasMoreElements() ) {
		( (TransactionDomain) enum.nextElement() ).shutdown();
	    }

	    _status = Configure.Status.Active;
	}

	Logger.getSystemLogger().println( 
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
	case Heuristic.BEGIN_TX:
	    _recoveryLog.beginTransaction( xid.getGlobalTransactionId() );
	    TransactionServer.logMessage( "Begin " + xid.toString() );
	    break;
	case Heuristic.Commit:
	    _recoveryLog.commitTransaction( xid.getGlobalTransactionId() );
	    TransactionServer.logMessage( "Commit " + xid.toString() );
	    break;
	case Heuristic.ReadOnly:
	    _recoveryLog.commitTransaction( xid.getGlobalTransactionId() );
	    TransactionServer.logMessage( "Read-only " + xid.toString() );
	    break;
	case Heuristic.Rollback:
	    _recoveryLog.rollbackTransaction( xid.getGlobalTransactionId() );
	    TransactionServer.logMessage( "Rollback " +  xid.toString() );
	    break;
	case Heuristic.Mixed:
	case Heuristic.Hazard:
	    _recoveryLog.rollbackTransaction( xid.getGlobalTransactionId() );
	    TransactionServer.logMessage( "Mixed " + xid.toString() );
	    break;
	}
    }


    public byte[] createRemoteTransaction()
	throws SystemException, RemoteException
    {
	TransactionDomain txDomain;

	txDomain = getTransactionDomain( DefaultDomain, true );
	return txDomain.createRemoteTransaction();
    }


}


