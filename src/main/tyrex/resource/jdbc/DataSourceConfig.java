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
 * Original code is Copyright (c) 1999-2001, Intalio, Inc. All Rights Reserved.
 *
 * Contributions by MetaBoss team are Copyright (c) 2003-2004, Softaris Pty. Ltd. All Rights Reserved.
 *
 */


package tyrex.resource.jdbc;


import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

import org.apache.log4j.Category;

import tyrex.resource.PoolLimits;
import tyrex.resource.PoolMetrics;
import tyrex.resource.Resource;
import tyrex.resource.ResourceConfig;
import tyrex.resource.ResourceException;
import tyrex.tm.TransactionDomain;
import tyrex.tm.TyrexTransactionManager;
import tyrex.util.Logger;


/**
 * 
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.14 $
 */
public class DataSourceConfig
    extends ResourceConfig
{


    /**
     * The data source class.
     */
    private String                  _className;


    /**
     * The resource, if created.
     */
    private Resource                _resource;


    /**
     * The class loader used to load the data source.
     */
    private ClassLoader             _classLoader;


    /**
     * Sets the name for the data source class. The data source will be
     * constructed from this class. It can implement <tt>DataSource</tt>,
     * <tt>XADataSource</tt> or <tt>PooledConnectionDataSource</tt>.
     *
     * @param name The data source class name
     */
    public void setClassName( String className )
    {
        _className = className;
    }


    /**
     * Returns the name for the data source class.
     *
     * @return The data source class
     */
    public String getClassName()
    {
        return _className;
    }


    public Object createFactory()
        throws ResourceException
    {
        try {
            return createFactory_();
        } catch ( ResourceException except ) {
            Logger.resource.error( "Error in datasource configuration '" + getName() + "'", except );
            throw except;
        }
    }


    private Object createFactory_()
        throws ResourceException
    {
        String                  name = _name;
        String                  jarName = _jar;
        String                  className = _className;
		String                  paths = _paths;
        Class                   cls;
        Object                  object;

        if ( name == null || name.trim().length() == 0 )
            throw new ResourceException( "The configuration element is missing the resource manager name" );
        if ( className == null || className.trim().length() == 0 )
            throw new ResourceException( "The configuration element is missing the data source class name" );

		// See if we have to use dedicated classloader - this is only the case
		// if we have <jar> and / or <paths> elements populated
		ArrayList lURLs = new ArrayList();
		try
		{
			if (jarName != null && jarName.trim().length() > 0)
				lURLs.add(getURL( jarName));
			if ( paths != null && paths.length() > 0 )
			{
				StringTokenizer tokenizer = new StringTokenizer( paths, ",; " );
				while(tokenizer.hasMoreTokens())
				{
					jarName = tokenizer.nextToken();
					lURLs.add(getURL(jarName));
				}
			}
		}
		catch ( IOException except )
		{
			Logger.resource.error("Could not create url for datasource file: '" + jarName + "'. File may not exist.");
			throw new ResourceException( except );
		}

		// Create a new URL class loader for the data source if necessary.
		if (lURLs.size() > 0)
			_classLoader = new URLClassLoader( (URL[])lURLs.toArray(new URL[lURLs.size()]) , getClass().getClassLoader() );
		else
			_classLoader = getClass().getClassLoader();
        // Create a new data source using the class names
        // specified in the configuration file.
        try {
            cls = _classLoader.loadClass( className );
            object = cls.newInstance();
        } catch ( Exception except ) {
            throw new ResourceException( except );
        }

        if ( ( object instanceof DataSource ) ||
             ( object instanceof XADataSource ) ||
             ( object instanceof ConnectionPoolDataSource ) )
            return object;
        else
            throw new ResourceException( "Data source is not of type DataSource, XADataSource or ConnectionPoolDataSource" );
    }


    public synchronized Resource createResource( TransactionDomain txDomain )
        throws ResourceException
    {
        String                  name;
        Object                  factory;
        TyrexTransactionManager txManager;

        name = _name;
        if ( name == null || name.trim().length() == 0 )
            throw new ResourceException( "The configuration element is missing the resource manager name" );
        if ( txDomain == null )
            throw new ResourceException( "The configuration was not loaded from a transaction domain" );
        txManager = (TyrexTransactionManager) txDomain.getTransactionManager();

        if ( _resource != null )
            return _resource;
        factory = _factory;
        if ( factory == null )
            throw new ResourceException( "No data source configured" );
        if ( factory instanceof XADataSource ) {
            _resource = new ConnectionPool( name, super.getLimits(), _classLoader,
                                            (XADataSource) factory, null,
                                            txManager, Category.getInstance( Logger.resource.getName() + "." + name ) );
            return _resource;
        } else if ( factory instanceof ConnectionPoolDataSource ) {
            _resource = new ConnectionPool( name, super.getLimits(), _classLoader,
                                            null, (ConnectionPoolDataSource) factory,
                                            txManager, Category.getInstance( Logger.resource.getName() + "." + name ) );
            return _resource;
        } else if ( factory instanceof DataSource ) {
            _resource = new DataSourceResource( (DataSource) factory );
            return _resource;
        } else
            throw new ResourceException( "Data source is not of type DataSource, XADataSource or ConnectionPoolDataSource" );
    }


    
    private static final class DataSourceResource
        implements Resource
    {


        private final PoolMetrics   _metrics;


        private final DataSource    _dataSource;


        private final PoolLimits    _limits;


        DataSourceResource( DataSource dataSource )
        {
            _metrics = new PoolMetrics();
            _dataSource = dataSource;
            _limits = new PoolLimits();
        }


        public PoolMetrics getPoolMetrics()
        {
            return _metrics;
        }


        public Object getClientFactory()
        {
            return _dataSource;
        }


        public Class getClientFactoryClass()
        {
            return DataSource.class;
        }


        public XAResource getXAResource()
        {
            return null;
        }


        public PoolLimits getPoolLimits()
        {
            return _limits;
        }


        public void destroy()
        {
        }


    }


}
