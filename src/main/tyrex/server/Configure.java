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
 * $Id: Configure.java,v 1.8 2001/02/23 19:19:51 jdaniel Exp $
 */


package tyrex.server;


import java.io.Serializable;
import java.io.PrintWriter;
import java.io.IOException;
import java.rmi.server.RemoteServer;
import java.rmi.MarshalledObject;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.rmi.activation.Activatable;
import java.rmi.activation.ActivationGroup;
import java.rmi.activation.ActivationGroupDesc;
import java.rmi.activation.ActivationGroupID;
import java.rmi.activation.ActivationDesc;
import java.rmi.activation.ActivationID;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import tyrex.resource.ResourceLimits;
import tyrex.resource.ResourcePoolManager;
import tyrex.resource.ResourcePoolManagerImpl;
import tyrex.util.Messages;
import tyrex.conf.Server;


/**
 * Used to configure and control the transaction server. The
 * transaction server properties are configured through this object,
 * which is also used to start and shutdown the server.
 * <p>
 * To construct a new transaction server, create a configuration
 * object with the suitable properties and call the start/shutdown
 * methods. To change the server's configuration, apply the changes
 * to this object and call the {@link #startServer} method.
 * To shutdown the server, call {@link #shutdownServer} on this object.
 * <p>
 * For security reasons, the server can only be shutdown/restarted
 * from the same configuration object that was used to start it.
 * Failure to do so will cause a {@link SecurityException}.
 * The caller must also have the suitable permission, see {@link
 * TyrexPermission}.
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.8 $ $Date: 2001/02/23 19:19:51 $
 */
public final class Configure
    implements Serializable
{


    public static class Default
    {


	/**
	 * The default timeout for all newly created transactions,
	 * specified in seconds. Defaults to one minute.
	 */
	public static final int TxTimeout = 60;


	/**
	 * The default upper limit for concurrent transactions
	 * and active transactions. Provides the default values
	 * for the pool manager. Defaults to 200.
	 */
	public static final int UpperLimit = 200;


	/**
	 * The default wait duration for starting a new transaction,
	 * checking timeout on transactions, etc. Specified in seconds.
	 * Defaults to 10 seconds.
	 */
	public static final int Duration = 10;


    }


    public static class Status
    {


	/**
	 * Indicates that the server is inactive and must be started
	 * before it can be used.
	 */
	public static final int Inactive = 0;


	/**
	 * Indicates that the server is active.
	 */
	public static final int Active = 1;


	/**
	 * Indicates that the server is in the process of starting.
	 */
	public static final int Starting = 2;


	/**
	 * Indicates that the server is in the process of shutting down.
	 */
	public static final int Shutdown = 3;


    }


    public static class Names
    {


	/**
	 * The name used to register and lookup the transaction server
	 * as a remote server or for activation.
	 */
	public static final String TransactionServer = "/tyrex/TransactionServer";
	
	
	/**
	 * The name used to register and lookup the {@link UserTransaction}
	 * interface in RMI and JNDI.
	 */
	public static final String UserTransaction = "/comp/UserTransaction";


    }

    /**
     * The default transaction timeout in milliseconds.
     */
    private int             _txTimeout = Default.TxTimeout * 1000;


    /**
     * True if transactions should terminate the running
     * threads associated with this transaction.
     */
    private boolean         _txTerminate;


    /**
     * True if nested transactions should be supported.
     */
    private boolean         _txNested = true;


    /**
     * The pool manager associated with this server.
     */
    //private ResourcePoolManager     _poolManager;

    /**
     * Flag to specify if the logs for recovery are used
     */
    private boolean                 _use_log;
    
    /**
     * The resource limits for the pool manager
     */
    private ResourceLimits          _limits;
     
     /**
      * Log directory
      */
     private String _log_directory;
     
    /**
     * If loaded from a configuration file this will reference the
     * server configuration object allowing us to save back to the
     * configuration file.
     */
    private transient Server         _serverConf;

    /**
     * Reference to the ORB reference
     */
    private org.omg.CORBA.ORB _orb;

    /**
     * This flag is used to set if recovery mechanism must be used
     */
    private boolean _recovery = false;
    
    public Configure()
    {
	super();
    }

    /**
     * Sets the timeout for transactions. Transactions taking longer
     * than this duration will timeout, rollback and potentially
     * timeout the associated threads.
     *
     * @param second The default timeout specified in seconds,
     *   zero to use the default value
     */
    public void setTransactionTimeout( int seconds )
    {
	if ( seconds < 0 )
	    throw new IllegalArgumentException( Messages.message( "tyrex.tx.timeNegative" ) );
	if ( seconds == 0 )
	    _txTimeout = ( Default.TxTimeout * 1000 );
	else
	    _txTimeout = ( seconds * 1000 );
    }


    /**
     * Returns the timeout for transactions. See {@link
     * #setTransactionTimeout}.
     *
     * @return The default timeout specified in seconds
     */
    public int getTransactionTimeout()
    {
	return ( _txTimeout / 1000 );
    }


    /**
     * Sets the transaction thread termination policy..
     * If true, when transactions timeout, the associated
     * threads will be stopped immediately by throwing a
     * {@link TimeoutException} in the thread.
     *
     * @param terminate True if threads should be terminated
     *   on transaction timeout
     */
    public void setThreadTerminate( boolean terminate )
    {
	_txTerminate = terminate;
    }


    /**
     * Returns the transaction thread termination policy.
     *
     * @return True if threads should be terminated
     *   on transaction timeout
     */
    public boolean getThreadTerminate()
    {
	return _txTerminate;
    }


    /**
     * Turns nested transaction support on or off. This applies
     * to all transactions created inside the local transaction
     * manager. The default is true.
     *
     * @param nested True if nested transactions should be
     *   supported
     */
    public void setNestedTransaction( boolean nested )
    {
	_txNested = nested;
    }


    /**
     * Returns true if nested transactions should be supported.
     *
     * @return True if nested transactions should be supported
     */
    public boolean getNestedTransaction()
    {
	return _txNested;
    }


    /**
     * Sets the pool manager for use with this server.
     *
     * @param poolManager The pool manager to use
     * @see PoolManager
     */
    /*public synchronized void setResourcePoolManager( ResourcePoolManager poolManager )
    {
	if ( poolManager == null )
	    throw new IllegalArgumentException( "Argument 'poolManager' is null" );
	_poolManager = poolManager;
    }
    */

    public synchronized ResourceLimits getResourceLimits()
    {
    if ( _limits == null ) {
        _limits = new ResourceLimits();    
    }
    return _limits;
    }


    public synchronized void setResourceLimits( ResourceLimits limits )
    {
    _limits = limits;
    }
    
    public void setLogProperty( String directory )
    {
        _log_directory = directory;
        if ( directory != null )
            _use_log = true;
    }    

    public boolean isLogActivated()
    {
        return tyrex.tm.Tyrex.log();
    }
    
    public String getLogDirectory()
    {
        return _log_directory;
    }
    
    public void setORB( org.omg.CORBA.ORB orb )
    {
        _orb = orb;
    }
    
    public org.omg.CORBA.ORB getORB()
    {
        return _orb;
    }
    
    public void activateRecovery()
    {
        _recovery = true;
    }
    
    public boolean isRecoveryActivated()
    {
        return _recovery;
    }
    
    /**
     * Returns the pool manager for use with this server.
     * If not pool manager was associated, a default one is
     * created and returned.
     *
     * @return The pool manager to use, never null
     * @see PoolManager
     */
    /*public synchronized ResourcePoolManager getResourcePoolManager()
    {
	if ( _poolManager == null ) {
	    
        // create a resource limits
        ResourceLimits limits = getResourceLimits();
	    limits.setUpperLimit( Default.UpperLimit );
	    limits.setActiveLimit( Default.UpperLimit );
	    limits.setDesiredSize( Default.UpperLimit );
	    limits.setWaitTimeout( Default.Duration );
	    limits.setCheckEvery( Default.Duration );
	    //limits.setPruneFactor( 0.1F  10%  );
        _poolManager = new ResourcePoolManagerImpl(limits);
	}
	return _poolManager;
    }
    */

    /**
     * Sets the log to which the server will write messages during
     * execution.
     *
     * @param write A suitable print writer
     */
    /*
    public void setLogWriter( PrintWriter writer )
    {
	// Make sure we do not set the log writer to null,
	// some methods might be asking if it's not null
	// and then using it.
	if ( writer == null )
	    throw new NullPointerException( "Argument 'writer' is null" );
	_logWriter = writer;
    }*/


    /**
     * Returns the log to which the server writes messages during
     * execution.
     *
     * @return The log writer, null if none was set
     *//*
    public PrintWriter getLogWriter()
    {
	return _logWriter;
    }*/


    /**
     * Called to start or restart the server with this configuration.
     * If the server is already started, it will reset to changes done
     * to this configuration object. This method may only be called
     * with the same configuration object for the life of the server.
     * The caller must have the {@link
     * TyrexPermission.SERVER_START} permission to perform
     * this operation. If problems occur during start up, use {@link
     * TransactionServer#setLogWriter} to view them.
     */
    public void startServer()
    {
	// Ask the server to start with this configuration.
	// Don't use getInstance(), the server might not have
	// been instantiated yet.
	TransactionServer.start( this );
    }


    /**
     * Called to shutdown the server. Must be called with the same
     * configuration object that was used to start the server.
     * The caller must have the {@link
     * TyrexPermission.SERVER_SHUTDOWN} permission to
     * perform this operation.
     */
    public void shutdownServer()
    {
	TransactionServer.shutdown( this );
    }


    /**
     * Returns the status of the server.
     */
    public int serverStatus()
    {
	return TransactionServer.status();
    }


    /**
     * Registers the server for automatic activation. If an activation
     * server (<tt>rmid</tt>) is running, the transaction server will
     * be registered under the name {@link NAME_TRANSACTION_SERVER}.
     * Any lookup in the RMI registry or JNDI of this name will
     * re-activate the server with the current configuration.
     *
     * @throws Exception An RMI, activation or JNDI error occured
     */
    public void activation()
	throws Exception
    {
	ActivationGroupDesc group;
	ActivationGroupID   groupID;
	ActivationDesc      act;
	ActivationID        actID;
	InitialContext      initCtx;
	Remote              remote;
	
	try {
	    // XXX Need to supply information about security policy here.
	    //     (Perhaps not if we're always Java extension)
	    group = new ActivationGroupDesc( new java.util.Properties(), null );
	    groupID = ActivationGroup.getSystem().registerGroup( group );
	    ActivationGroup.createGroup( groupID, group, 0 );
	    // Describe the transaction server for activation.
	    // Use this configuration as the marshalled data to
	    // pass on construction.
	    // XXX Need to supply location of classes, but we leave it
	    //     null assuming we're always running as Java extension.
	    act = new ActivationDesc( groupID, TransactionServer.class.getName(),
				      null, new MarshalledObject( this ) );
	    actID = ActivationGroup.getSystem().registerObject( act );
	    remote = Activatable.register( act );
	    Naming.rebind( Names.TransactionServer, remote );

	    initCtx = new InitialContext();
	    initCtx.rebind( Names.TransactionServer, remote );
	} catch ( Exception except ) {
	   // if ( getLogWriter() != null )
		//getLogWriter().println( Messages.format( "tyrex.server.failedActivate", except ) );
            tyrex.util.Logger.server.warn( Messages.format( "tyrex.server.failedActivate", except )  );
	    throw except;
	}
    }


    /**
     * Flushes the configuration, updating the file from which it was
     * read. If the configuration was not read from a configuration
     * file, this method will throw an {@link IOException}.
     *
     * @throws IOException Configuration not loaded from a
     *    configuration file, or error encountered updating the file
     */
    public void flush()
	throws IOException
    {
	if ( _serverConf == null )
	    throw new IOException( Messages.message( "tyrex.server.noConfigFile" ) );
	_serverConf.save();
    }


    /**
     * Refreshes the configuration from a default configuration file.
     *
     * @throws IOException The default configuration file could not
     *   be located, or an error encountered reading the file
     */
    /*
    public void refresh()
	throws IOException
    {
	Server    server;
	Configure config;

	server = Server.load();
	config = server.getConfig();
	_serverConf = server;
	if ( config != null ) {
	    if ( config.getRecoveryLog() != null )
		setRecoveryLog( config.getRecoveryLog() );
	    setTransactionTimeout( config.getTransactionTimeout() );
	    setThreadTerminate( config.getThreadTerminate() );
	    setNestedTransaction( config.getNestedTransaction() );
	    setPoolManager( config.getPoolManager() );
	    if ( config.getLogWriter() != null )
		setLogWriter( config.getLogWriter() );
	}
    }
    */


    /**
     * Creates a default configuration object. If a configuration file
     * has been specified, it will be read and used. Otherwise, some
     * default configuration will be used. This method does not throw
     * an exception if the configuration file could not be found or
     * read; use {@link #refresh} to learn of such problems.
     *
     * @return A new configuration object
     */
    /*
    public static Configure createDefault()
    {
	Server    server;
	Configure config;

	try {
	    server = Server.load();
	    config = server.getConfig();
	    if ( config == null ) {
		config = new Configure();
		server.setConfig( config );
	    }
	    config._serverConf = server;
	    return config;
	} catch ( IOException except ) {
	}
	return new Configure();
    }
    */


}







