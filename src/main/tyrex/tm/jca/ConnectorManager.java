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


package tyrex.tm.jca;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import org.xml.sax.InputSource;
import javax.resource.ResourceException;
import org.apache.log4j.Category;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.MarshalException;
import tyrex.tm.TyrexTransactionManager;
import tyrex.tm.TransactionDomain;
import tyrex.tm.jca.dd.DDConnector;
import tyrex.tm.jca.dd.DDResourceAdapter;
import tyrex.util.Configuration;


/**
 */
public final class ConnectorManager
{


    public static final String LOG4J_CATEGORY = "jca";


    private final TransactionDomain  _txDomain;


    private final HashMap            _pools;


    public ConnectorManager( TransactionDomain txDomain )
    {
        if ( txDomain == null )
            throw new IllegalArgumentException( "Argument txDomain is null" );
        _txDomain = txDomain;
        _pools = new HashMap();
    }


    public void install( Connector connector )
        throws ResourceException, IOException
    {
        String              name;
        String              rarName;
        String              paths;
        File                rarFile;
        StringTokenizer     tokenizer;
        URL[]               urls;
        DDConnector         ddConnector;
        DDResourceAdapter   ddAdapter;
        JarFile             jarFile;
        JarEntry            jarEntry;
        StringBuffer        info;
        ConnectionPool      pool;
        ClassLoader         classLoader;
        ConnectorLoader     loader;

        if ( connector == null )
            throw new IllegalArgumentException( "Argument connector is null" );
        name = connector.getName();
        if ( name == null || name.trim().length() == 0 )
            throw new ResourceException( "The connector descriptor is missing the connector name" );
        rarName = connector.getRAR();
        if ( rarName == null || rarName.trim().length() == 0 )
            throw new ResourceException( "The connector descriptor is missing the connector name" );

        // Obtain the RAR file and use the paths to create
        // a list of URLs for the class loader.
        rarFile = new File( rarName );
        paths = connector.getPaths();
        if ( paths != null ) {
            tokenizer = new StringTokenizer( paths, ":; " );
            urls = new URL[ tokenizer.countTokens() + 1 ];
            urls[ 0 ] = rarFile.toURL();
            for ( int i = 1 ; i < urls.length ; ++i )
                urls[ i ] = new URL( tokenizer.nextToken() );
        } else
            urls = new URL[] { rarFile.toURL() };

        // Read the connector JAR file and it's deployment descriptor.
        jarFile = new JarFile( rarFile, true );
        info = new StringBuffer( "Loading connector " + name + " from " + rarFile.getName() );
        jarEntry = jarFile.getJarEntry( "META-INF/ra.xml" );
        if ( jarEntry == null )
            throw new ResourceException( "Connector " + name + 
                                         " missing deployment descriptor" );
        try {
            ddConnector = (DDConnector) Unmarshaller.unmarshal( DDConnector.class,
                                                              new InputSource( jarFile.getInputStream( jarEntry ) ) );
        } catch ( ValidationException except ) {
            throw new ResourceException( except.getMessage() );
        } catch ( MarshalException except ) {
            throw new ResourceException( except.getMessage() );
        }
        if ( ddConnector.getDisplayName() != null )
            info.append( "  " ).append( ddConnector.getDisplayName() );
        if ( ddConnector.getVendorName() != null )
            info.append( "  " ).append( ddConnector.getVendorName() );
        if ( ddConnector.getVersion() != null )
            info.append( "  " ).append( ddConnector.getVersion() );
        ddAdapter = ddConnector.getResourceadapter();
        if ( ddAdapter == null )
            throw new ResourceException( "Connector " + name + 
                                         " missing resource adapter deployment descriptor" );
        // Create a new URL class loader for the connector.
        // Create a new connector loader using the class names
        // specified in the deployment descriptor.
        classLoader = new URLClassLoader( urls, ConnectorManager.class.getClassLoader() );
        loader = new ConnectorLoader( classLoader, ddAdapter.getManagedconnectionfactoryClass(),
                                      ddAdapter.getConnectionfactoryInterface(),
                                      ddAdapter.getConnectionInterface() );
        if ( Configuration.verbose )
            Category.getInstance( LOG4J_CATEGORY ).info( info.toString() );

        pool = new ConnectionPool( name, connector.getLimits(), loader,
                                   (TyrexTransactionManager) _txDomain.getTransactionManager(),
                                   Category.getInstance( LOG4J_CATEGORY + "." + name ) );
        // Finally, able to add the connector's connection pool.
        _pools.put( name, pool );
    }


    /**
     * Returns true if a connector by this name is installed.
     *
     * @param name The connector name
     * @return True if the connector is installed
     */
    public boolean hasConnector( String name )
    {
        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        return _pools.containsKey( name );
    }


    /**
     * Returns an iterator of all the installed connector. Each element
     * is a string providing the connector name. The name can be used to
     * obtain the client connection factory and connection pool metrics.
     *
     * @return An iterator of all installed connector names
     */
    public Iterator listConnectors()
    {
        return _pools.keySet().iterator();
    }


    /**
     * Returns the connection factory for the specified connector.
     * Returns the client connection factory object that is placed in
     * the JNDI environment naming context for access by the application.
     *
     * @param name The connector name
     * @return The client connection factory
     * @throws ResourceException No connector with this name installed
     */
    public Object getConnectionFactory( String name )
        throws ResourceException
    {
        ConnectionPool  pool;

        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        pool = (ConnectionPool) _pools.get( name );
        if ( pool == null )
            throw new ResourceException( "No connector " + name + " installed" );
        return pool.getConnectionFactory();
    }


    /**
     * Returns the connection pool metrics for the specified connector.
     * The connection pool metrics can be used to collect statistical
     * information about the connection pool.
     *
     * @param name The connector name
     * @return The connection pool metrics
     * @throws ResourceException No connector with this name installed
     */
    public PoolMetrics getPoolMetrics( String name )
        throws ResourceException
    {
        ConnectionPool  pool;

        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        pool = (ConnectionPool) _pools.get( name );
        if ( pool == null )
            throw new ResourceException( "No connector " + name + " installed" );
        return pool.getPoolMetrics();
    }


}
