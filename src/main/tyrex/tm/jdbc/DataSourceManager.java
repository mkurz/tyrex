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
 * Copyright 2000, 2001 (C) Intalio Inc. All Rights Reserved.
 *
 */


package tyrex.tm.jdbc;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.xml.sax.InputSource;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.sql.ConnectionPoolDataSource;
import org.apache.log4j.Category;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.MarshalException;
import tyrex.tm.TyrexTransactionManager;
import tyrex.tm.TransactionDomain;
import tyrex.util.Configuration;


/**
 */
public final class DataSourceManager
{


    public static final String LOG4J_CATEGORY = "jdbc";


    private final TransactionDomain  _txDomain;


    private final HashMap            _pools;


    public DataSourceManager( TransactionDomain txDomain )
    {
        if ( txDomain == null )
            throw new IllegalArgumentException( "Argument txDomain is null" );
        _txDomain = txDomain;
        _pools = new HashMap();
    }


    public synchronized void install( DataSourceConfig dataSource )
        throws SQLException, IOException
    {
        String              name;
        String              jarName;
        String              paths;
        File                jarFile;
        StringTokenizer     tokenizer;
        URL[]               urls;
        ConnectionPool      pool;
        ClassLoader         classLoader;
        Class               cls;
        Object              object;
        String              className;

        if ( dataSource == null )
            throw new IllegalArgumentException( "Argument dataSource is null" );
        name = dataSource.getName();
        if ( name == null || name.trim().length() == 0 )
            throw new SQLException( "The data source config is missing the data source name" );
        jarName = dataSource.getJAR();
        if ( jarName == null || jarName.trim().length() == 0 )
            throw new SQLException( "The data source config is missing the JAR name" );
        className = dataSource.getClassName();
        if ( className == null || className.trim().length() == 0 )
            throw new SQLException( "The data source config is missing the data source class name" );
        if ( _pools.containsKey( name ) )
            throw new SQLException( "Data source " + name + " already installed" );

        // Obtain the JAR file and use the paths to create
        // a list of URLs for the class loader.
        jarFile = new File( jarName );
        paths = dataSource.getPaths();
        if ( paths != null ) {
            tokenizer = new StringTokenizer( paths, ":; " );
            urls = new URL[ tokenizer.countTokens() + 1 ];
            urls[ 0 ] = jarFile.toURL();
            for ( int i = 1 ; i < urls.length ; ++i )
                urls[ i ] = new URL( tokenizer.nextToken() );
        } else
            urls = new URL[] { jarFile.toURL() };

        // Create a new URL class loader for the data source.
        // Create a new data source using the class names
        // specified in the configuration file.
        classLoader = new URLClassLoader( urls, DataSourceManager.class.getClassLoader() );
        try {
            cls = classLoader.loadClass( className );
            object = cls.newInstance();
        } catch ( Exception except ) {
            throw new SQLException( except.getMessage() );
        }
        if ( object instanceof DataSource )
            _pools.put( name, object );
        else if ( object instanceof XADataSource ) {
            pool = new ConnectionPool( name, dataSource.getLimits(), (XADataSource) object, null,
                                       (TyrexTransactionManager) _txDomain.getTransactionManager(),
                                   Category.getInstance( LOG4J_CATEGORY + "." + name ) );
            _pools.put( name, pool );
        } else if ( object instanceof ConnectionPoolDataSource ) {
            pool = new ConnectionPool( name, dataSource.getLimits(), null, (ConnectionPoolDataSource) object,
                                       (TyrexTransactionManager) _txDomain.getTransactionManager(),
                                   Category.getInstance( LOG4J_CATEGORY + "." + name ) );
            _pools.put( name, pool ); 
        } else
            throw new SQLException( "Data source is not of type DataSource, XADataSource or ConnectionPoolDataSource" );
    }


    /**
     * Returns true if a data source by this name is installed.
     *
     * @param name The data source name
     * @return True if the daa source is installed
     */
    public boolean hasDataSource( String name )
    {
        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        return _pools.containsKey( name );
    }


    /**
     * Returns an iterator of all the installed data sources. Each element
     * is a string providing the data source name. The name can be used to
     * obtain the <tt>DataSource</tt> client object and connection pool metrics.
     *
     * @return An iterator of all installed data source names
     */
    public Iterator listDataSources()
    {
        return _pools.keySet().iterator();
    }


    /**
     * Returns the <tt>DataSource</tt> client object for the specified
     * data source.  Returns the <tt>DataSource</tt> client object that
     * is placed in the JNDI environment naming context for access by the application.
     *
     * @param name The data source name
     * @return The client connection factory
     * @throws SQLException No data source with this name installed
     */
    public DataSource getDataSource( String name )
        throws SQLException
    {
        DataSource dataSource;

        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        dataSource = (DataSource) _pools.get( name );
        if ( dataSource == null )
            throw new SQLException( "No JDBC data source " + name + " installed" );
        return dataSource;
    }


    /**
     * Returns the connection pool metrics for the specified data source.
     * The connection pool metrics can be used to collect statistical
     * information about the connection pool.
     *
     * @param name The data source name
     * @return The connection pool metrics, or null if no pooling supported
     * by this data source
     * @throws SQLException No data source with this name installed
     */
    public PoolMetrics getPoolMetrics( String name )
        throws SQLException
    {
        DataSource dataSource;

        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        dataSource = (DataSource) _pools.get( name );
        if ( dataSource == null )
            throw new SQLException( "No JDBC data source " + name + " installed" );
        if ( dataSource instanceof ConnectionPool )
            return ( (ConnectionPool) dataSource ).getPoolMetrics();
        else
            return null;
    }


}
