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
 * Copyright 1999-2001 (C) Intalio Inc. All Rights Reserved.
 *
 */


package tyrex.resource.jca;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import org.xml.sax.InputSource;
import org.apache.log4j.Category;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.MarshalException;
import tyrex.tm.TransactionDomain;
import tyrex.tm.TyrexTransactionManager;
import tyrex.resource.ResourceConfig;
import tyrex.resource.Resource;
import tyrex.resource.ResourceException;
import tyrex.resource.PoolMetrics;
import tyrex.resource.jca.dd.DDConnector;
import tyrex.resource.jca.dd.DDResourceAdapter;
import tyrex.util.Logger;


/**
 * 
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $
 */
public class Connector
    extends ResourceConfig
{


    /**
     * The resource, if created.
     */
    private Resource                _resource;


    /**
     * The connector loader used for loading the connector.
     */
    private ConnectorLoader         _loader;


    public Object createFactory()
        throws ResourceException
    {
        try {
            return createFactory_();
        } catch ( ResourceException except ) {
            Logger.resource.error( "Error", except );
            throw except;
        }
    }


    public Object createFactory_()
        throws ResourceException
    {
        String                  name;
        String                  jarName;
        File                    file;
        URL[]                   urls;
        URL                     url;
        StringTokenizer         tokenizer;
        String                  paths;
        StringBuffer            info;
        JarFile                 jarFile;
        JarEntry                jarEntry;
        DDConnector             ddConnector;
        DDResourceAdapter       ddAdapter;
        String                  txSupport;

        name = _name;
        if ( name == null || name.trim().length() == 0 )
            throw new ResourceException( "The configuration element is missing the resource manager name" );
        jarName = _jar;
        if ( jarName == null || jarName.trim().length() == 0 )
            throw new ResourceException( "The configuration element is missing the JAR name" );

        // Obtain the JAR file and use the paths to create
        // a list of URLs for the class loader.
        try {
            file = new File( jarName );
            if ( file.exists() && file.canRead() )
                url = file.toURL();
            else
                url = new URL( jarName );
            paths = _paths;
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
            throw new ResourceException( except );
        }


        // Read the connector JAR file and it's deployment descriptor.
        try {
            jarFile = new JarFile( urls[ 0 ].toString(), true );
            info = new StringBuffer( "Loading connector " + name + " from " + jarFile.getName() );
            jarEntry = jarFile.getJarEntry( "META-INF/ra.xml" );
            if ( jarEntry == null )
                throw new ResourceException( "Connector " + name + 
                                             " missing deployment descriptor" );
            ddConnector = (DDConnector) Unmarshaller.unmarshal( DDConnector.class,
                                                                new InputSource( jarFile.getInputStream( jarEntry ) ) );
        } catch ( IOException except ) {
            throw new ResourceException( except );
        } catch ( ValidationException except ) {
            throw new ResourceException( except );
        } catch ( MarshalException except ) {
            throw new ResourceException( except );
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
        txSupport = ddAdapter.getTransactionSupport();
            
        // Create a new URL class loader for the data source.
        // Create a new connector loader using the class loader and
        // deployment descriptor.
        try {
            _loader = new ConnectorLoader( new URLClassLoader( urls, getClass().getClassLoader() ),
                                           ddAdapter.getManagedconnectionfactoryClass(),
                                           ddAdapter.getConnectionfactoryInterface(),
                                           ddAdapter.getConnectionInterface(),
                                           DDResourceAdapter.XA_TRANSACTION.equals( txSupport ),
                                           DDResourceAdapter.LOCAL_TRANSACTION.equals( txSupport ) );
        } catch ( Exception except ) {
            throw new ResourceException( except );
        }
        return _loader.getConfigFactory();
    }


    public synchronized Resource createResource( TransactionDomain txDomain )
        throws ResourceException
    {
        String                  name;
        ConnectorLoader         loader;
        TyrexTransactionManager txManager;

        name = _name;
        if ( name == null || name.trim().length() == 0 )
            throw new ResourceException( "The configuration element is missing the resource manager name" );
        if ( txDomain == null )
            throw new ResourceException( "The configuration was not loaded from a transaction domain" );
        txManager = (TyrexTransactionManager) txDomain.getTransactionManager();

        if ( _resource != null )
            return _resource;
        loader = _loader;
        if ( loader == null )
            throw new ResourceException( "No connector configured" );
        try {
            _resource = new ConnectionPool( name, super.getLimits(), loader, 
                                            txManager, Category.getInstance( Logger.resource.getName() + "." + name ) );
        } catch ( Exception except ) {
            throw new ResourceException( except );
        }
        return _resource;
    }


}
