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


package tyrex.resource.jdbc;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.sql.ConnectionPoolDataSource;
import org.apache.log4j.Category;
import tyrex.tm.TransactionDomain;
import tyrex.tm.TyrexTransactionManager;
import tyrex.resource.BaseConfiguration;
import tyrex.resource.Resource;


/**
 * 
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version $Revision: 1.1 $
 */
public class DataSourceConfig
    extends BaseConfiguration
{


    /**
     * The data source class.
     */
    private String                  _className;


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
        throws Exception
    {
        try {
            return createFactory_();
        } catch ( Exception except ) {
            Category.getInstance( DataSourceManager.LOG4J_CATEGORY ).error( "Error", except );
            throw new Exception( except.toString() );
        }
    }


    public Object createFactory_()
        throws SQLException
    {
        String                  name;
        String                  jarName;
        File                    file;
        String                  className;
        URL[]                   urls;
        URL                     url;
        StringTokenizer         tokenizer;
        Class                   cls;
        Object                  object;
        String                  paths;
        ClassLoader             classLoader;
        TransactionDomain       txDomain;
        TyrexTransactionManager txManager;

        name = super.getName();
        if ( name == null || name.trim().length() == 0 )
            throw new SQLException( "The configuration element is missing the resource manager name" );
        jarName = super.getJAR();
        if ( jarName == null || jarName.trim().length() == 0 )
            throw new SQLException( "The configuration element is missing the JAR name" );
        className = _className;
        if ( className == null || className.trim().length() == 0 )
            throw new SQLException( "The configuration element is missing the data source class name" );
        txDomain = super.getTransactionDomain();
        if ( txDomain == null )
            throw new SQLException( "The configuration was not loaded from a transaction domain" );
        txManager = (TyrexTransactionManager) txDomain.getTransactionManager();

        // Obtain the JAR file and use the paths to create
        // a list of URLs for the class loader.
        try {
            file = new File( jarName );
            if ( file.exists() && file.canRead() )
                url = file.toURL();
            else
                url = new URL( jarName );
            paths = super.getPaths();
            if ( paths != null && paths.length() > 0 ) {
                tokenizer = new StringTokenizer( paths, ":; " );
                urls = new URL[ tokenizer.countTokens() + 1 ];
                urls[ 0 ] = url;
                for ( int i = 1 ; i < urls.length ; ++i ) {
                    jarName = tokenizer.nextToken();
                    file = new File( jarName );
                    if ( file.exists() && file.canRead() )
                        urls[ i ] = file.toURL();
                    else
                        urls[ i ] = new URL( jarName );
                }
            } else
                urls = new URL[] { url };
        } catch ( IOException except ) {
            throw new SQLException( except.toString() );
        }
            
        // Create a new URL class loader for the data source.
        // Create a new data source using the class names
        // specified in the configuration file.
        classLoader = new URLClassLoader( urls, getClass().getClassLoader() );
        try {
            cls = classLoader.loadClass( className );
            object = cls.newInstance();
        } catch ( Exception except ) {
            throw new SQLException( except.toString() );
        }

        if ( object instanceof DataSource )
            return object;
        else
            throw new SQLException( "Data source is not of type DataSource, XADataSource or ConnectionPoolDataSource" );

    }


    public DataSource getDataSource()
        throws SQLException
    {
        String                  name;
        Object                  factory;
        TransactionDomain       txDomain;
        TyrexTransactionManager txManager;

        name = super.getName();
        if ( name == null || name.trim().length() == 0 )
            throw new SQLException( "The configuration element is missing the resource manager name" );
        txDomain = super.getTransactionDomain();
        if ( txDomain == null )
            throw new SQLException( "The configuration was not loaded from a transaction domain" );
        txManager = (TyrexTransactionManager) txDomain.getTransactionManager();

        factory = getFactory();
        if ( factory == null )
            throw new SQLException( "No data source configured" );
        if ( factory instanceof ConnectionPool )
            return (DataSource) factory;
        if ( factory instanceof XADataSource ) {
            factory = new ConnectionPool( name, super.getLimits(), (XADataSource) factory, null,
                                          txManager, Category.getInstance( DataSourceManager.LOG4J_CATEGORY + "." + name ) );
            setFactory( factory );
            return (DataSource) factory;
        } else if ( factory instanceof ConnectionPoolDataSource ) {
            factory = new ConnectionPool( name, super.getLimits(), null, (ConnectionPoolDataSource) factory,
                                          txManager, Category.getInstance( DataSourceManager.LOG4J_CATEGORY + "." + name ) );
            setFactory( factory );
            return (DataSource) factory;
        } else if ( factory instanceof DataSource )
            return (DataSource) factory;
        else
            throw new SQLException( "Data source is not of type DataSource, XADataSource or ConnectionPoolDataSource" );
    }


    public Resource getResource()
        throws SQLException
    {
        DataSource dataSource;

        dataSource = getDataSource();
        if ( dataSource instanceof Resource )
            return (Resource) dataSource;
        else
            return null;
    }


}
