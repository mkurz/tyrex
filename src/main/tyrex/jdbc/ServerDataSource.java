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
 * $Id: ServerDataSource.java,v 1.11 2000/09/26 18:27:17 mohammed Exp $
 */


package tyrex.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.XAConnection;
import javax.sql.PooledConnection;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.xa.XAResource;
import javax.naming.BinaryRefAddr;
import javax.naming.Referenceable;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import tyrex.naming.ReferenceRefAddr;
import tyrex.tm.ResourceManager;
import tyrex.resource.ResourceLimits;
import tyrex.resource.ResourcePool;
import tyrex.resource.ResourcePoolManager;
import tyrex.resource.ResourcePoolManagerImpl;
import tyrex.resource.ResourceTimeoutException;
import tyrex.util.Logger;
import tyrex.util.Messages;


/**
 * This class allows the connections from a JDBC data source
 * (javax.sql.ConnectionPoolDataSource and
 * javax.sql.XADataSource) to be used as 
 * {@link EnlistedResource} and to be pooled in a
 * {@link tyrex.resource.ResourcePool}.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.11 $ $Date: 2000/09/26 18:27:17 $
 *
 * Date         Author          Changes
 * ?            Assaf Arkin     Created  
 * Aug 8  2000  Riad Mohammed   Added retry functionality
 * Aug 22 2000  Riad Mohammed   Added convenience constructors
 */
public class ServerDataSource
    implements DataSource, ConnectionEventListener,
	       ObjectFactory, Referenceable, Serializable, ResourcePool
{


    /**
     * The default timeout for waiting to obtain a new connection,
     * specified in seconds.
     */
    public static final int DEFAULT_TIMEOUT = 120;


    /**
     * The default number of times to try getting a
     * a connection
     */
    public static final int DEFAULT_RETRY_ATTEMPTS = 5;


    /**
     * Holds the timeout for waiting to obtain a new connection,
     * specified in seconds. The default is {@link #DEFAULT_TIMEOUT}.
     */
    private int _timeout = DEFAULT_TIMEOUT;


    /**
     * Holds the number of times to try to get a connection.
     * The default is {@link #DEFAULT_RETRY_ATTEMPTS}.
     */
    private int _retryAttempts = DEFAULT_RETRY_ATTEMPTS;


    /**
     * Holds the name of the data source used for creating new connections.
     */
    private String _dataSourceName;


    /**
     * Description of this datasource.
     */
    private String _description = "DataSource";


    /**
     * Holds the log writer to which all messages should be
     * printed. The default writer is obtained from the driver
     * manager, but it can be specified at the datasource level
     * and will be passed to the driver. May be null.
     */    
    private transient PrintWriter _logWriter;


    /**
     * The underlying data source used to create new connections
     * to the target database. Created on-demand when the first
     * connection is requested based on the data source specified
     * in {@link #_dataSourceName}. May be either a {@link
     * PooledConnectionDataSource} or {@link XADataSource}. Since
     * these interfaces do not extend each other, we use Object.
     */
    private transient Object       _dataSource;


    /**
     * Holds a list of all the pooled connections that are available
     * for reuse by the application. Each connection is held inside
     * an {@link ConnectionPoolEntry} that records the connection's
     * account. This cannot be implemented as a stack if we want
     * to pool connections for different accounts.
     */
    private transient Vector     _pool = new Vector();


    /**
     * Holds a list of all the active connections that are being
     * used by the application. Each connection is held inside
     * an {@link ConnectionPoolEntry} that records the connection's
     * account, using {@link PooledConnection} as the key.
     */
    private transient Hashtable _active = new Hashtable();


    private ResourcePoolManager         _poolManager;




    public ServerDataSource()
    {
    }


    public ServerDataSource( ConnectionPoolDataSource dataSource )
    {
	_dataSource = dataSource;
    }


    public ServerDataSource( XADataSource dataSource )
    {
    _dataSource = dataSource;
    }

    public ServerDataSource( String dataSourceName )
    {
    _dataSourceName = dataSourceName;
    }

    public synchronized Connection getConnection()
        throws SQLException
    {
	return getConnection( null, null );
    }


    public Connection getConnection( String user, String password )
        throws SQLException
    {
	ConnectionPoolEntry entry;
	Connection          conn;
    int                 retryAttempts = 0;
    SQLException exception = null;

    while ( ( -1 == _retryAttempts ) ||
            ( retryAttempts <= _retryAttempts ) ) {
        // If the connection is unuseable we might detect it
    	// at this point and attempt to return a different
    	// connection to the application.
    	try {
    	    entry = getPoolEntry( user, password );
    	} catch ( ResourceTimeoutException except ) {
    	    // Time out occured waiting for an available connection.
    	    throw new SQLException( except.getMessage() );
    	}
    
    	try {
    
    	    // Check to see if we can produce a connection for
    	    // the application.
    	    conn = entry.conn.getConnection();
    
    	    // This will delist an XA resource with the transaction
    	    // manager whether or not we have a transaction.
    	    // We must return a {@link EnlistedConnection} to the
    	    // application.
    	    if ( entry.xaRes != null ) {
                conn = new EnlistedConnection( conn, entry.xaRes );
            }
    	    
    	    // Obtain a new pool entry, add it to the active list and
    	    // register as a listener on it.
    	    _active.put( entry.conn, entry );
    	    entry.conn.addConnectionEventListener( this );
    	    return conn;
    	} catch ( SQLException except ) {
    	    // This is not a problem of creating a new entry,
    	    // so just try to create a new one.
            exception = except;
        }
        ++retryAttempts;
    }
    if ( null != exception ) {
        throw exception;    
    }
    throw new SQLException("Failed to get a connection.");
    }


    /**
     * Obtains a pool entry with a useable connection. If a connection
     * available in the pool for the specified account, will return that
     * connection. If not connection is in the pool, will attempt to create
     * a new one, as long as we do not reach the maximum capacity.
     * If we reached maximum capacity and no connection was released during
     * the login timeout, will throw an {@link SQLException}.
     *
     * @param user The user name for creating the connection
     * @param password The password for creating the connection
     * @return A connection entry that we can use
     * @throws SQLException An error occured opening a new connection,
     *    of timeout occured while waiting to open a new connection
     * @throws TimeoutException A timeout occured waiting to obtain a
     *   connection after the pool limit has been reached
     */
    private synchronized ConnectionPoolEntry getPoolEntry( String user, String password )
	throws SQLException, ResourceTimeoutException
    {
	ConnectionPoolEntry entry;
	String              account;
        long                timeout;

	timeout = 0;
	account = getAccount( user, password );

	// If there is no pool manager, we create a default one
	// that has no limitations and is slow on releasing
	// pooled connections.
	getPoolManager();

	synchronized ( _poolManager ) {
	    // See if we can activate another connection.
	    _poolManager.canActivate();

	    // If there are any entries in the pool, try to retrieve
	    // one suitable for out account. Work from the end of the
	    // pool since removal a the end of a vector should be faster.
	    for ( int i = _pool.size() ; i-- > 0 ; ) {
		entry = (ConnectionPoolEntry) _pool.elementAt( i );
		if ( ( entry.account == null && account == null ) ||
		     ( entry.account != null && entry.account.equals( account ) ) ) {
		    // Found an entry in the pool. Remove it from
		    // the pool and return it's pool entry.
		    _pool.removeElementAt( i );
		    if ( getLogWriter() != null )
			getLogWriter().println( Messages.format( "tyrex.jdbc.pool.reusing", entry.conn ) );
		    return entry;
		}
	    }
	
	    // No connection found in the pool, see if we can create
	    // a new entry (have not reached maximum capacity).
	    _poolManager.canCreateNew();

	    // We got so far either because we can create a new resource,
	    // or there are resources in the pool, but not for the account
	    // we're looking for. We don't expect this to happen often,
	    // so we create a new one anyhow.
	    entry = new ConnectionPoolEntry();
	    entry.conn = createConnection( user, password );
	    if ( entry.conn instanceof XAConnection )
		entry.xaRes = ( (XAConnection) entry.conn ).getXAResource();
	    entry.account = account;
	    if ( getLogWriter() != null )
		getLogWriter().println( Messages.format( "tyrex.jdbc.pool.creating", entry.conn ) );
	}
	return entry;
    }


    public int getPooledCount()
    {
	return _pool.size();
    }


    public int getActiveCount()
    {
	return _active.size();
    }


    public synchronized void releasePooled( int count )
    {
	int start;
    start = _pool.size() - count;
	if ( start < 0 )
	    start = 0;
	count = _pool.size();
	while ( count-- > start )
	    try {
            ( ( ConnectionPoolEntry ) _pool.remove( count ) ).conn.close();
        } catch ( Exception e ) {
        }
    }



    public synchronized void connectionClosed( ConnectionEvent event )
    {
	PooledConnection    conn;
	ConnectionPoolEntry entry;

	// The connection has been released by the holder, we place it
	// back in the pool.
	try {
	    conn = (PooledConnection) event.getSource();
	    conn.removeConnectionEventListener( this );
	    entry = (ConnectionPoolEntry) _active.remove( conn );

	    if ( entry != null ) {
		_pool.addElement( entry );
		// Notify all waiting threads that a new connection
		// is available to the pool and they might use it.
		_poolManager.released();
		if ( getLogWriter() != null )
		    getLogWriter().println( Messages.format( "tyrex.jdbc.pool.returned", entry.conn ) );
	    }
	} catch ( Exception except ) { 
	    // We handle null pointer and class cast exceptions gracefully.
	}
    }


    public synchronized void connectionErrorOccurred( ConnectionEvent event )
    {
	PooledConnection    conn;
	ConnectionPoolEntry entry;

	// The connection is no longer useable, we remove it from the pool.
	try {
	    conn = (PooledConnection) event.getSource();
	    conn.removeConnectionEventListener( this );
	    entry = (ConnectionPoolEntry) _active.remove( conn );

	    // If this is an X/A connection dessociate it from any
	    // active transaction. If there is a problem, the TM
	    // will throw an exception at this point.
	    if ( entry.xaRes != null )
		ResourceManager.discardResource( entry.xaRes );

	    // Notify all waiting threads that a new connection can be created.
	    _poolManager.released();
	    if ( entry != null && getLogWriter() != null )
		getLogWriter().println( Messages.format( "tyrex.jdbc.pool.faulty", entry.conn ) );
	} catch ( Exception except ) { 
	    // We handle null pointer and class cast exceptions gracefully.
	}
    }


    /**
     * Close the data source, freeing any pooled connections.
     *
     * @throws IllegalStateException if there are active connections
     */
    public synchronized void close()
        throws IllegalStateException
    {
        if ( 0 != getActiveCount() ) {
            throw new IllegalStateException( "Data source " + toString() +
                                             " has active connections" );
        }
        if ( 0 != getPooledCount() ) {
            // free the pooled connections
            for ( Iterator i = _pool.iterator(); i.hasNext(); ) {
                try {    
                    ( ( ConnectionPoolEntry) i.next() ).conn.close();
                } catch ( Exception except ) {
                }
            }
            _pool.clear();
        }
    }

    /**
     * In order to deal with connections opened for a specific
     * account, we record the account name in the pool.
     * Given the user and password used to open the account,
     * we get a unique account identifier that combines the two.
     * The returned account can be encrypted for added security.
     * 
     * @param user The user name for creating the connection
     * @param password The password for creating the connection
     * @return A unique account name matching the user name
     *   and password, null if <tt>user</tt> is null
     */
    private String getAccount( String user, String password )
    {
	if ( user == null )
	    return null;

	// XXX  We should encrypt this part so as not to hold the
	//      password in memory
	if ( password == null )
	    return user;
	else
	    return user + ":" + password;
    }


    private PooledConnection createConnection( String user, String password )
	throws SQLException
    {
	if ( _dataSource == null ) {
	    try {
		Object         obj;
	    
		if ( _dataSourceName == null )
		    throw new SQLException( Messages.message( "tyrex.jdbc.pool.noDataSource" ) );
		obj = new InitialContext().lookup( _dataSourceName );
		if ( obj == null )
		    throw new SQLException( Messages.format( "tyrex.jdbc.pool.missingDataSource",
							     _dataSourceName ) );
		if ( obj instanceof ConnectionPoolDataSource ||
		     obj instanceof XADataSource )
		    _dataSource = obj;
		else
		    throw new SQLException( Messages.format( "tyrex.jdbc.pool.incorrectDataSource",
							     _dataSourceName ) );

	    } catch ( NamingException except ) {
		throw new SQLException( except.toString() );
	    }
	}

	if ( _dataSource instanceof XADataSource ) {
	    if ( user == null )
		return ( (XADataSource) _dataSource ).getXAConnection();
	    else
		return ( (XADataSource) _dataSource ).getXAConnection( user, password );
	} else {
        if ( user == null )
		return ( (ConnectionPoolDataSource) _dataSource ).getPooledConnection();
	    else
		return ( (ConnectionPoolDataSource) _dataSource ).getPooledConnection( user, password );
	}
    }


    public synchronized void setDataSource( ConnectionPoolDataSource dataSource )
    {
	if ( dataSource == null )
	    throw new NullPointerException( "Argument 'dataSource' is null" );
	_dataSourceName = null;
	_dataSource = dataSource;
    }


    public synchronized void setDataSource( XADataSource dataSource )
    {
	if ( dataSource == null )
	    throw new NullPointerException( "Argument 'dataSource' is null" );
	_dataSourceName = null;
	_dataSource = dataSource;
    }


    public PrintWriter getLogWriter()
    {
	return _logWriter;
    }


    public synchronized void setLogWriter( PrintWriter writer )
    {
	// We might be using the writer at this exact time,
	// so we cannot allow the reference to be set to null.
	if ( writer == null )
	    throw new IllegalArgumentException( "Argument 'writer' is null" );
	_logWriter = writer;
    }


    public void setLoginTimeout( int seconds )
    {
	_timeout = seconds;
    }


    public int getLoginTimeout()
    {
	return _timeout;
    }


    /**
     * Set the number of attempts to try to get a connection.
     * A negative value means try indefinitely.
     *
     * @param retryAttempts the number of attempts to try to get 
     *      a connection
     */
    public void setRetryAttempts( int retryAttempts )
    {
    _retryAttempts = Math.max( -1, retryAttempts );
    }


    /**
     * Return the number of attempts to try to 
     * get a connection.
     *
     * @return the number of attempts to try to
     *      get a connection.
     */
    public int getRetryAttempts()
    {
    return _retryAttempts;
    }

    /**
     * Sets the name of the data source used for creating new connections.
     * The standard name for this property is <tt>dataSourceName</tt>.
     *
     * @param dataSourceName The name of the data source
     */
    public synchronized void setDataSourceName( String dataSourceName )
    {
	if ( dataSourceName == null )
	    throw new NullPointerException( "Argument 'dataSourceName' is null" );
	_dataSourceName = dataSourceName;
	_dataSource = null;
    }


    /**
     * Returns the name of the data source used for creating new connections.
     * The standard name for this property is <tt>dataSourceName</tt>.
     *
     * @return The name of the data source
     */
    public String getDataSourceName()
    {
	return _dataSourceName;
    }


    /**
     * Sets the description of this datasource.
     * The standard name for this property is <tt>description</tt>.
     *
     * @param description The description of this datasource
     */
    public void setDescription( String description )
    {
	if ( description == null )
	    throw new NullPointerException( "Argument 'description' is null" );
	_description = description;
    }


    /**
     * Returns the description of this datasource.
     * The standard name for this property is <tt>description</tt>.
     *
     * @return The description of this datasource
     */
    public String getDescription()
    {
	return _description;
    }


    public synchronized void setPoolManager( ResourcePoolManager poolManager )
    {
	if ( poolManager == null )
	    throw new IllegalArgumentException( "Argument 'poolManager' is null" );
	if ( _poolManager != null )
	    _poolManager.unmanage();
	_poolManager = poolManager;
	_poolManager.manage( this, true );
    }


    /**
     * Set the resource limits for the resource pool manager.
     * If the specified argument is null then the default resource
     * limits is used.
     * @param resourceLimits the resource
     * @see ResourceLimits
     */
    public void setResourceLimits( ResourceLimits resourceLimits )
    {
        getPoolManager().setResourceLimits( resourceLimits );
    }


    /**
     * Return the resource limits associated with the resource pool manager.
     * The same copy used by the resource pool manager is returned so changes
     * to the resource limit object will affect the resource pool manager.
     * @return the resource limits used by the resource pool manager.
     * @see ResourceLimits
     */
    public ResourceLimits getResourceLimits()
    {
        return getPoolManager().getResourceLimits();
    }


    public ResourcePoolManager getPoolManager()
    {
	if ( _poolManager == null )
	    _poolManager = new ResourcePoolManagerImpl( this, true );
	return _poolManager;
    }


    public Reference getReference()
    {
	Reference ref;

	// We use same object as factory.
	ref = new Reference( getClass().getName(), getClass().getName(), null );
	ref.add( new StringRefAddr( "loginTimeout", Integer.toString( _timeout ) ) );
	if ( _description != null )
	    ref.add( new StringRefAddr( "description", _description ) );
    
	if ( _dataSourceName != null )
	    ref.add( new StringRefAddr( "dataSourceName", _dataSourceName ) );
    else if ( _dataSource != null ) {
        try {
            if ( _dataSource instanceof Referenceable ) {
                ref.add( new ReferenceRefAddr( "dataSource", (Referenceable)_dataSource ) );
            }
            else if ( _dataSource instanceof Reference ) {
                ref.add( new ReferenceRefAddr( "dataSource", (Reference)_dataSource ) );
            }
            else if ( _dataSource instanceof Serializable ) {
                ref.add( new BinaryRefAddr( "dataSource", getDataSourceBytes() ) );
            }
        } catch (NamingException e) {
            Logger.getSystemLogger().print( "Failed to bind data source" );
            Logger.getSystemLogger().print( _dataSource );
            Logger.getSystemLogger().println(e);
        } catch (IOException e) {
        }
    }
 	return ref;
    }

    /**
     * Return the byte array containing the bytes of the
     * data source. 
     * <p>
     * It is assumed that the data source is not null.
     *
     * @return the byte array containing the bytes of the
     *      data source.
     * @throws IOException if there is a problem getting the
     *      data source bytes.
     */
    private byte[] getDataSourceBytes()
        throws IOException
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream output = null;
        
        try {
            output = new ObjectOutputStream(byteStream);

            output.writeObject(_dataSource);
            output.flush();
            
            return byteStream.toByteArray();
        }
        finally {
            try{output.close();}catch(IOException e){}
        }
    }


    /**
     * Return data source unmarshalled from the specified byte array. 
     *
     * @param bytes the array of bytes
     * @return data source unmarshalled from the specified byte array. 
     * @throws ClassNotFoundException if the class of the data source
     *      is not found.
     * @throws IOException if there is a problem getting the
     *      data source bytes.
     */
    private Object getDataSourceFromBytes(byte[] bytes)
        throws ClassNotFoundException, IOException
    {
        ObjectInputStream input = null; 
        
        try {
            input = new ObjectInputStream(new ByteArrayInputStream(bytes));

            return input.readObject();
        }
        finally {
            if (null != input) {
                try {input.close();}catch (IOException e){}
            }
        }
    }


    public Object getObjectInstance( Object refObj, Name name, Context nameCtx, Hashtable env )
        throws NamingException
    {
	Reference ref;

	// Can only reconstruct from a reference.
	if ( refObj instanceof Reference ) {
	    ref = (Reference) refObj;
	    // Make sure reference is of datasource class.
	    if ( ref.getClassName().equals( getClass().getName() ) ) {

		ServerDataSource ds;
		RefAddr          addr;

		ds = new ServerDataSource();
		ds._timeout = Integer.parseInt( (String) ref.get( "loginTimeout" ).getContent() );
		addr = ref.get( "description" );
		if ( addr != null )
		    ds._description = (String) addr.getContent();
		addr = ref.get( "dataSourceName" );
		if ( addr != null )
		    ds._dataSourceName = (String) addr.getContent();
        else {
            addr = ref.get( "dataSource" );
            if ( addr != null ) {
                try {
                    if ( addr instanceof BinaryRefAddr ) {
                        ds._dataSource = getDataSourceFromBytes( ( byte[] ) addr.getContent() );
                    }
                    else {
                        ds._dataSource = addr.getContent();
                    }
                } catch ( Exception e ) {
                    throw new NamingException( e.toString() );
                }
            }
        }
		return ds;

	    } else
		throw new NamingException( Messages.format( "tyrex.jdbc.pool.badReference", getClass().getName() ) );
	} else if ( refObj instanceof Remote )
	    return refObj;
	else
	    return null;
    }


}


/**
 * In order to pool connections opened for a particular user
 * account, we need to remember the account associated with
 * that connection - we cannot mix connections from different
 * accounts.
 */
class ConnectionPoolEntry
{
    

    /**
     * Identifies the account for which the connection was
     * opened, see {@link ServerDataSource#getAccount}.
     */
    String           account;
    
    
    /**
     * The XA/pooled connection returned by the actual data
     * source. We use this one to create the underlying
     * connections.
     */
    PooledConnection conn;
    
    
    /**
     * The XA resource associated with the XA connection,
     * or null if this is only a pooled connection.
     */
    XAResource      xaRes;
    
    
}
