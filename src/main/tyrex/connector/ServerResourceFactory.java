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
 * $Id: ServerResourceFactory.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.connector;


import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.xa.XAResource;
import javax.naming.Referenceable;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import tyrex.server.ResourceManager;
import tyrex.util.PoolManager;
import tyrex.util.PooledResources;
import tyrex.util.TimeoutException;
import tyrex.util.Messages;


/**
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
 */
public class ServerResourceFactory
    implements ResourceFactory, ResourceEventListener,
	       Referenceable, Serializable, PooledResources
{


    /**
     * The default timeout for waiting to obtain a new resource,
     * specified in seconds.
     */
    public static final int DEFAULT_TIMEOUT = 120;


    /**
     * Holds the timeout for waiting to obtain a new resource,
     * specified in seconds. The default is {@link #DEFAULT_TIMEOUT}.
     */
    private int _timeout = DEFAULT_TIMEOUT;


    /**
     * Holds the name of the resource.
     */
    private String _factoryName;


    /**
     * Description of this resource.
     */
    private String _description = "Resource";


    /**
     * Holds the log writer to which all messages should be
     * printed. The default writer is obtained from the driver
     * manager, but it can be specified at the factory level
     * and will be passed to the driver. May be null.
     */    
    private transient PrintWriter _logWriter;


    private transient PooledResourceFactory _resFactory;


    /**
     * Holds a list of all the pooled resources that are available
     * for reuse by the application. Each resource is held inside
     * an {@link ResourcePoolEntry} that records the resource's
     * account. This cannot be implemented as a stack if we want
     * to pool resources for different accounts.
     */
    private transient Vector     _pool = new Vector();


    /**
     * Holds a list of all the active resources that are being
     * used by the application. Each resource is held inside
     * an {@link ResourcePoolEntry} that records the resource's
     * account, using {@link PooledResource} as the key.
     */
    private transient Hashtable _active = new Hashtable();


    private PoolManager         _poolManager;



    public ServerResourceFactory()
    {
    }


    public synchronized Resource getResource()
        throws ResourceException
    {
	return getResource( null, null );
    }


    public Resource getResource( String user, String password )
        throws ResourceException
    {
	ResourcePoolEntry entry;
	Resource          res;

	// If we have a problem creating a new resource,
	// getPoolEntry will throw a ResourceException.
	try {
	    entry = getPoolEntry( user, password );
	} catch ( TimeoutException except ) {
	    // Time out occured waiting for an available resource.
	    throw new ResourceException( except.getMessage() );
	}

	// If the resource is unuseable we might detect it
	// at this point and attempt to return a different
	// resource to the application.
	try {
	    // Check to see if we can produce a resource for
	    // the application.
	    res = entry.res.getResource();

	    /*
	    // This will delist an XA resource with the transaction
	    // manager whether or not we have a transaction.
	    if ( entry.xaRes != null )
		res = new EnlistedResource( res, entry.xaRes );
	    */
	    
	    // Obtain a new pool entry, add it to the active list and
	    // register as a listener on it.
	    _active.put( entry.res, entry );
	    entry.res.addResourceEventListener( this );
	    return res;
	} catch ( ResourceException except ) {
	    // This is not a problem of creating a new entry,
	    // so just try to create a new one.
	    return getResource( user, password );
	}
    }


    /**
     * Obtains a pool entry with a useable resource. If a resource
     * available in the pool for the specified account, will return that
     * resource. If not resource is in the pool, will attempt to create
     * a new one, as long as we do not reach the maximum capacity.
     * If we reached maximum capacity and no resource was released during
     * the login timeout, will throw an {@link ResourceException}.
     *
     * @param user The user name for creating the resource
     * @param password The password for creating the resource
     * @return A resource entry that we can use
     * @throws ResourceException An error occured opening a new resource,
     *    of timeout occured while waiting to open a new resource
     * @throws TimeoutException A timeout occured waiting to obtain a
     *   resource after the pool limit has been reached
     */
    private synchronized ResourcePoolEntry getPoolEntry( String user, String password )
	throws ResourceException, TimeoutException
    {
	ResourcePoolEntry entry;
	String            account;
        long              timeout;

	timeout = 0;
	account = getAccount( user, password );

	// If there is no pool manager, we create a default one
	// that has no limitations and is slow on releasing
	// pooled resources.
	getPoolManager();

	synchronized ( _poolManager ) {
	    // See if we can activate another resource.
	    _poolManager.canActivate();

	    // If there are any entries in the pool, try to retrieve
	    // one suitable for out account. Work from the end of the
	    // pool since removal a the end of a vector should be faster.
	    for ( int i = _pool.size() ; i-- > 0 ; ) {
		entry = (ResourcePoolEntry) _pool.elementAt( i );
		if ( ( entry.account == null && account == null ) ||
		     ( entry.account != null && entry.account.equals( account ) ) ) {
		    // Found an entry in the pool. Remove it from
		    // the pool and return it's pool entry.
		    _pool.removeElementAt( i );
		    if ( getLogWriter() != null )
			getLogWriter().println( Messages.format( "tyrex.jdbc.pool.reusing", entry.res ) );
		    return entry;
		}
	    }
	
	    // No resource found in the pool, see if we can create
	    // a new entry (have not reached maximum capacity).
	    _poolManager.canCreateNew();

	    // We got so far either because we can create a new resource,
	    // or there are resources in the pool, but not for the account
	    // we're looking for. We don't expect this to happen often,
	    // so we create a new one anyhow.
	    entry = new ResourcePoolEntry();
	    entry.res = createResource( user, password );
	    entry.xaRes = entry.res.getXAResource();
	    entry.account = account;
	    if ( getLogWriter() != null )
		getLogWriter().println( Messages.format( "tyrex.jdbc.pool.creating", entry.res ) );
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
	    _pool.removeElementAt( count );
    }



    public synchronized void resourceClosed( ResourceEvent event )
    {
	PooledResource    pres;
	ResourcePoolEntry entry;

	// The resource has been released by the holder, we place it
	// back in the pool.
	try {
	    pres = (PooledResource) event.getSource();
	    pres.removeResourceEventListener( this );
	    entry = (ResourcePoolEntry) _active.remove( pres );

	    if ( entry != null ) {
		_pool.addElement( entry );
		// Notify all waiting threads that a new resource
		// is available to the pool and they might use it.
		_poolManager.released();
		if ( getLogWriter() != null )
		    getLogWriter().println( Messages.format( "tyrex.jdbc.pool.returned", entry.res ) );
	    }
	} catch ( Exception except ) { 
	    // We handle null pointer and class cast exceptions gracefully.
	}
    }


    public synchronized void resourceErrorOccurred( ResourceEvent event )
    {
	PooledResource    pres;
	ResourcePoolEntry entry;

	// The resource is no longer useable, we remove it from the pool.
	try {
	    pres = (PooledResource) event.getSource();
	    pres.removeResourceEventListener( this );
	    entry = (ResourcePoolEntry) _active.remove( pres );

	    // If this is an X/A resource dessociate it from any
	    // active transaction. If there is a problem, the TM
	    // will throw an exception at this point.
	    if ( entry.xaRes != null )
		ResourceManager.discardResource( entry.xaRes );

	    // Notify all waiting threads that a new resource can be created.
	    _poolManager.released();
	    if ( entry != null && getLogWriter() != null )
		getLogWriter().println( Messages.format( "tyrex.jdbc.pool.faulty", entry.res ) );
	} catch ( Exception except ) { 
	    // We handle null pointer and class cast exceptions gracefully.
	}
    }


    /**
     * In order to deal with resource opened for a specific
     * account, we record the account name in the pool.
     * Given the user and password used to open the account,
     * we get a unique account identifier that combines the two.
     * The returned account can be encrypted for added security.
     * 
     * @param user The user name for creating the resource
     * @param password The password for creating the resource
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


    private PooledResource createResource( String user, String password )
	throws ResourceException
    {
	if ( _resFactory == null ) {
	    try {
		Object         obj;
	    
		if ( _factoryName == null )
		    throw new ResourceException( Messages.message( "tyrex.jdbc.pool.noDataSource" ) );
		obj = new InitialContext().lookup( _factoryName );
		if ( obj == null )
		    throw new ResourceException( Messages.format( "tyrex.jdbc.pool.missingDataSource",
								  _factoryName ) );
		if ( obj instanceof PooledResourceFactory )
		    _resFactory = (PooledResourceFactory) obj;
		else
		    throw new ResourceException( Messages.format( "tyrex.jdbc.pool.incorrectDataSource",
								  _factoryName ) );

	    } catch ( NamingException except ) {
		throw new ResourceException( except.toString() );
	    }
	}

	if ( user == null )
	    return _resFactory.getPooledResource();
	else
	    return _resFactory.getPooledResource( user, password );
    }


    public synchronized void setResourceFactory( PooledResourceFactory resFactory )
    {
	if ( resFactory == null )
	    throw new NullPointerException( "Argument 'resFactory' is null" );
	_factoryName = null;
	_resFactory = resFactory;
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
     * Sets the name of the factory used for creating new resources.
     * The standard name for this property is <tt>factoryName</tt>.
     *
     * @param factoryName The name of the resource factory
     */
    public synchronized void setFactoryName( String factoryName )
    {
	if ( factoryName == null )
	    throw new NullPointerException( "Argument 'factoryName' is null" );
	_factoryName = factoryName;
	_resFactory = null;
    }


    /**
     * Returns the name of the factory used for creating new resources.
     * The standard name for this property is <tt>factoryName</tt>.
     *
     * @return The name of the resource factory
     */
    public String getFactoryName()
    {
	return _factoryName;
    }


    /**
     * Sets the description of this resource.
     * The standard name for this property is <tt>description</tt>.
     *
     * @param description The description of this resourc
     */
    public void setDescription( String description )
    {
	if ( description == null )
	    throw new NullPointerException( "Argument 'description' is null" );
	_description = description;
    }


    /**
     * Returns the description of this resource.
     * The standard name for this property is <tt>description</tt>.
     *
     * @return The description of this resource
     */
    public String getDescription()
    {
	return _description;
    }


    public synchronized void setPoolManager( PoolManager poolManager )
    {
	if ( poolManager == null )
	    throw new IllegalArgumentException( "Argument 'poolManager' is null" );
	if ( _poolManager != null )
	    _poolManager.unmanage();
	_poolManager = poolManager;
	_poolManager.manage( this, true );
    }


    public PoolManager getPoolManager()
    {
	if ( _poolManager == null )
	    _poolManager = new PoolManager( this, true );
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
	if ( _factoryName != null )
	    ref.add( new StringRefAddr( "dataSourceName", _factoryName ) );
 	return ref;
    }


    public Object getObjectInstance( Object refObj, Name name, Context nameCtx, Hashtable env )
        throws NamingException
    {
	Reference ref;

	// Can only reconstruct from a reference.
	if ( refObj instanceof Reference ) {
	    ref = (Reference) refObj;
	    // Make sure reference is of resource class.
	    if ( ref.getClassName().equals( getClass().getName() ) ) {

		ServerResourceFactory self;
		RefAddr               addr;

		self = new ServerResourceFactory();
		self._timeout = Integer.parseInt( (String) ref.get( "loginTimeout" ).getContent() );
		addr = ref.get( "description" );
		if ( addr != null )
		    self._description = (String) addr.getContent();
		addr = ref.get( "factoryName" );
		if ( addr != null )
		    self._factoryName = (String) addr.getContent();
		return self;

	    } else
		throw new NamingException( Messages.format( "tyrex.jdbc.pool.badReference", getClass().getName() ) );
	} else if ( refObj instanceof Remote )
	    return refObj;
	else
	    return null;
    }


}


/**
 * In order to pool resources opened for a particular user
 * account, we need to remember the account associated with
 * that resource - we cannot mix resources from different
 * accounts.
 */
class ResourcePoolEntry
{
    

    /**
     * Identifies the account for which the resource was
     * opened, see {@link ServerResourceFactory#getAccount}.
     */
    String           account;
    
    
    /**
     * The XA resource returned by the actual factory.
     * We use this one to create the underlying resource.
     */
    PooledResource  res;
    
    
    /**
     * The XA resource associated with the resource.
     */
    XAResource      xaRes;
    
    
}
