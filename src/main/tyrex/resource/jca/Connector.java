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
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import javax.resource.spi.ManagedConnectionFactory;
import org.xml.sax.InputSource;
import org.apache.log4j.Category;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.MarshalException;
import tyrex.tm.TransactionDomain;
import tyrex.tm.TyrexTransactionManager;
import tyrex.resource.ResourceConfig;
import tyrex.resource.Resource;
import tyrex.resource.ResourceException;
import tyrex.resource.PoolMetrics;
import tyrex.resource.jca.dd.DDConfigProperty;
import tyrex.resource.jca.dd.DDConnector;
import tyrex.resource.jca.dd.DDResourceAdapter;
import tyrex.util.Logger;


/**
 * 
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.9 $
 */
public class Connector
    extends ResourceConfig
{
    /**
     * The path of the deployment descriptor in the conenctor jar.
     */
    private static final String DEPLOYMENT_DESCRIPTOR_PATH = "META-INF/ra.xml";

    /**
     * Empty class array used in setting configuration properties
     */
    private static final Class[] EMPTY_CLASSES = new Class[0];

    /**
     * The resource, if created.
     */
    private Resource                _resource;


    /**
     * The connector loader used for loading the connector.
     */
    private ConnectorLoader         _loader;

    /**
     * The linked list of missing configuration properties that 
     * must be set.
     */
    private MissingConfigurationProperty _missing;

    /**
     * Called to set the factory object after it has been configured.
     *
     * @param factory The factory object
     */
    public void setFactory( Object factory )
    {
        MissingConfigurationProperty missing;
        Object value;

        if ( null != factory ) {
            // make sure the factory has all missing properties set
            missing = _missing;
    
            while( null != missing ) {
                try {
                    value = missing._configPropertyGetMethod.invoke( factory, EMPTY_CLASSES );
                }
                catch( Exception e ) {
                    Logger.resource.error( "Error in connector configuration '" + 
                                           getName() + 
                                           "'. Could not check that configuration property: '" + 
                                           missing._configProperty.getConfigPropertyName() + "' was set.", e);

                    throw new IllegalStateException( "Could not check that configuration property: '" + 
                                                     missing._configProperty.getConfigPropertyName() + 
                                                     "' was set." );
                }

                if ( ( ( null == missing._defaultValue ) && ( null == value ) ) ||
                     ( ( null != missing._defaultValue ) && ( missing._defaultValue.equals( value ) ) ) ) {
                    Logger.resource.error( "Error in connector configuration '" + 
                                           getName() + 
                                           "'. The configuration property: '" + 
                                           missing._configProperty.getConfigPropertyName() + "' was not set." );

                    throw new IllegalStateException( "Configuration property '" + 
                                                     missing._configProperty.getConfigPropertyName() + 
                                                     "' was not set." );
                }

                missing = missing._next;
            
            }
        }

        super.setFactory( factory );
    }


    public Object createFactory()
        throws ResourceException
    {
        try {
            return createFactory_();
        } catch ( ResourceException except ) {
            Logger.resource.error( "Error in connector configuration '" + getName() + "'", except );
            throw except;
        }
    }


    private Object createFactory_()
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
        DDConnector             ddConnector;
        DDResourceAdapter       ddAdapter;
        String                  txSupport;
        Mapping                 mapping;
        
        name = _name;
        if ( name == null || name.trim().length() == 0 )
            throw new ResourceException( "The configuration element is missing the resource manager name" );
        jarName = trim(_jar);
        if ( jarName == null || jarName.length() == 0 )
            throw new ResourceException( "The configuration element is missing the JAR name" );

        // Obtain the JAR file and use the paths to create
        // a list of URLs for the class loader.
        file = null;
        try {
            file = createFile( jarName );

            if ( file.exists() && file.canRead() ) {
                url = file.toURL();
            }
            else {
                url = new URL( jarName );
            }
            paths = _paths;
            if ( paths != null && paths.length() > 0 ) {
                tokenizer = new StringTokenizer( paths, ",; " );
                urls = new URL[ tokenizer.countTokens() + 1 ];
                urls[ 0 ] = url;
                for ( int i = 1 ; i < urls.length ; ++i ) {
                    jarName = tokenizer.nextToken();
                    file = createFile( jarName );
                    if ( file.exists() && file.canRead() )
                        urls[ i ] = file.toURL();
                    else
                        urls[ i ] = new URL( jarName );
                }
            } else
                urls = new URL[] { url };
        } catch ( MalformedURLException except ) {
            if ( null != file ) {
                Logger.resource.error("Could not create url for connector file: '" + file + "'. File may not exist.");
            }
            throw new ResourceException( except );
        }

        // Read the connector JAR file and it's deployment descriptor.
        try {
            info = new StringBuffer( "Loading connector " + name + " from " + _jar );
            
            mapping = new Mapping();
            mapping.loadMapping( new InputSource( Connector.class.getResourceAsStream( "mapping.xml" ) ) );
            ddConnector = (DDConnector) new Unmarshaller(mapping).unmarshal( new InputSource( getRAInputStream( urls[ 0 ] ) ) );
        } catch ( IOException except ) {
            throw new ResourceException( except );
        } catch ( ValidationException except ) {
            throw new ResourceException( except );
        } catch ( MarshalException except ) {
            throw new ResourceException( except );
        } catch ( MappingException except ) {
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
        txSupport = trim(ddAdapter.getTransactionSupport());
        
        // Create a new URL class loader for the data source.
        // Create a new connector loader using the class loader and
        // deployment descriptor.
        try {
            _loader = new ConnectorLoader( new URLClassLoader( urls, getClass().getClassLoader() ),
                                           trim(ddAdapter.getManagedconnectionfactoryClass()),
                                           trim(ddAdapter.getConnectionfactoryInterface()),
                                           trim(ddAdapter.getConnectionInterface()),
                                           DDResourceAdapter.XA_TRANSACTION.equals( txSupport ),
                                           DDResourceAdapter.LOCAL_TRANSACTION.equals( txSupport ) );
            configureManagedConnectionFactory( _loader.getConfigFactory(), ddAdapter.getConfigProperty() );
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


    /**
     * Return the input stream containing the ra.xml contents
     * in the specified url.
     *
     * @param url the url
     * @return the input stream containing the ra.xml contents
     *      in the specified url.
     * @throws IOException if there is a problem reading the
     *      data from the url.
     */
    private InputStream getRAInputStream(URL url) 
        throws IOException, ResourceException {
        JarFile     jarFile;
        JarEntry    jarEntry;
        URL         jarURL;
        String      file;

        try {
            if ( url.getProtocol().equals( "file" ) ) {
                jarFile = new JarFile( url.getFile(), true);
                jarEntry = jarFile.getJarEntry( DEPLOYMENT_DESCRIPTOR_PATH );
                
                if ( jarEntry == null )
                    throw new ResourceException( "The connector deployment descriptor is not found for " + url );
        
                return jarFile.getInputStream( jarEntry );
            }
    
            if ( url.getProtocol().equals( "jar" ) ) {
                file = url.getFile();
    
                if ( file.endsWith( "!/" ) ) {
                    jarURL = new URL( "jar:" + url.getFile() + DEPLOYMENT_DESCRIPTOR_PATH );        
                }
                else {
                    jarURL = new URL( "jar:" + file.substring( 0, file.lastIndexOf( '!' ) ) + 
                                      "!/" + DEPLOYMENT_DESCRIPTOR_PATH );
                }
            }
            else {
                jarURL = new URL( "jar:" + url + "!/" + DEPLOYMENT_DESCRIPTOR_PATH );
            }
            return jarURL.openStream();
        }
        catch(IOException e) {
            Logger.resource.error("Failed to extract connector ra.xml file for url " + url);

            throw e;
        }
    }


    /**
     * Return the result of trimming the specified string. If the 
     * specified string is null null is returned.
     *
     * @param string the string (optional)
     * @return the result of trimming the specified string. If the
     *      specified string is null null is returned.
     */
    private String trim(String string) {
        return (null == string) ? null : string.trim();
    }

    /**
     * Configure the managed connection factory instance with the 
     * configuration properties
     *
     * @param managedConnectionFactory the managed connection factory
     * @param configProperties Vector of 
     * {@link tyrex.resource.jca.dd.DDConfigProperty} objects. The 
     * vector may be null or empty.
     * @throws Exception if an error occured while trying to 
     * configure the managed connection factory.
     */
    private void configureManagedConnectionFactory( ManagedConnectionFactory managedFactory, 
                                                    Vector configProperties ) 
        throws Exception {
        DDConfigProperty configProperty;
        Class[] argClasses;
        Class[] argPrimitiveClasses;
        Object[] args;
        Class managedFactoryClass;
        String configPropertyValue;
        String methodName;
        Method method;
        MissingConfigurationProperty missing;

        if ( ( null != configProperties ) &&
             ( ! configProperties.isEmpty() ) ) {
            argClasses = new Class[ 1 ];
            argPrimitiveClasses = new Class[ 1 ];
            args = new Object[ 1 ];
            managedFactoryClass = managedFactory.getClass();

            configProperty = null;

            try {
                for ( int i = configProperties.size(); --i >= 0; ) {
                    configProperty = ( DDConfigProperty )configProperties.get( i );
                    configPropertyValue = trim( configProperty.getConfigPropertyValue() );
                    // can only add non-empty values
                    if ( ( null != configPropertyValue ) &&
                         ( 0 != configPropertyValue.length() ) ) {
                        initConfigPropertyValue( trim( configProperty.getConfigPropertyType() ), configPropertyValue, argClasses, 
                                                 argPrimitiveClasses, args );
                        methodName = getConfigPropertySetMethodName( trim( configProperty.getConfigPropertyName() ) );
                        method = null;

                        // find the method using the wrapper classes
                        try {
                            method = managedFactoryClass.getMethod( methodName, argClasses );
                        }
                        catch(NoSuchMethodException e) {
                            // could not find method with class args            
                            // try primitive class args
                            if ( null != argPrimitiveClasses[0] ) {
                                method = managedFactoryClass.getMethod( methodName, argPrimitiveClasses );
                            }

                            throw e;
                        }

                        method.invoke( managedFactory, args );
                    }
                    else {
                        try {
                            methodName = getConfigPropertyGetMethodName( trim( configProperty.getConfigPropertyName() ) );
                            method = managedFactoryClass.getMethod( methodName, EMPTY_CLASSES );
                            missing = new MissingConfigurationProperty( configProperty, 
                                                                        method, 
                                                                        method.invoke( managedFactory, EMPTY_CLASSES ) );
                            missing._next = _missing;
                            _missing = missing;
                        }
                        catch( Exception e ) {
                            // ignore
                            Logger.resource.debug( "Error in connector configuration '" + 
                                                    getName() + 
                                                    "'  - ignoring error with missing configuration property - name: '" + 
                                                   configProperty.getConfigPropertyName() + "' type: '" +
                                                   configProperty.getConfigPropertyType() + "' value: '" +
                                                   configProperty.getConfigPropertyValue() + "'", e );
                        }
                    }
                }
            }
            catch(Exception e) {
                if ( null != configProperty ) {
                    Logger.resource.error( "Error in connector configuration '" + 
                                           getName() + 
                                           "'  - error with configuration property - name: '" + 
                                           configProperty.getConfigPropertyName() + "' type: '" +
                                           configProperty.getConfigPropertyType() + "' value: '" +
                                           configProperty.getConfigPropertyValue() + "'" );
                }

                throw e;
            }
        }
    }

    /**
     * Return the method name for setting the resource adapter 
     * configuration property.
     * <P>
     * "set" is prepended to the configuration property name and 
     * returned.
     *
     * @param configPropertyName the name of the config property
     * name (required)
     * @return the method name for setting the resource adapter
     * configuration property.
     */
    private String getConfigPropertySetMethodName( String configPropertyName ) {
        if ( ( null == configPropertyName ) ||
             ( 0 == configPropertyName.length() ) ) {
            throw new IllegalArgumentException( "The argument 'configPropertyName' is null or empty." );
        }

        return "set" + configPropertyName;
    }

    /**
     * Return the method name for getting the resource adapter 
     * configuration property.
     * <P>
     * "get" is prepended to the configuration property name and 
     * returned.
     *
     * @param configPropertyName the name of the config property
     * name (required)
     * @return the method name for getting the resource adapter
     * configuration property.
     */
    private String getConfigPropertyGetMethodName( String configPropertyName ) {
        if ( ( null == configPropertyName ) ||
             ( 0 == configPropertyName.length() ) ) {
            throw new IllegalArgumentException( "The argument 'configPropertyName' is null or empty." );
        }

        return "get" + configPropertyName;
    }

    /**
     * Initialize the argument arrays with the
     * appropriate values for the configuration proprty type
     * and configuration proprty value.
     * <P>
     * The configPropertyType must be one of the following: 
     * java.lang.String, java.lang.Integer,
     * java.lang.Double, java.lang.Byte, java.lang.Short,
     * java.lang.Long, java.lang.Float, java.lang.Character
     *
     * @param configPropertyType the resource adapter configuration 
     * property type as a string (required)
     * @param configPropertyValue the resource adapter configuration
     * property value as a string. This argument is 
     * assumed to be valid.
     * @param argClasses the single-element Class array for the 
     * configuration property type. This argument is 
     * assumed to be valid.
     * @param argPrimitiveClasses the single-element Class array for the 
     * primitive class of configuration property type. This argument is 
     * assumed to be valid. If the property type does not have a 
     * primitive type the array element is set to null.
     * @param args the single-element Object array for the 
     * configuration property value. This argument is 
     * assumed to be valid.
     * @throws ResourceException if the configuration property type
     * is not recognized or if the value is a string of length greater than
     * one for character type.
     */
    private void initConfigPropertyValue( String configPropertyType, String configPropertyValue, 
                                          Class[] argClasses, Class[] argPrimitiveClasses, Object[] args )
        throws ResourceException {
        Class configPropertyClass;
        
        if ( ( null == configPropertyType ) ||
             ( 0 == configPropertyType.length() ) ) {
            throw new IllegalArgumentException( "The argument 'configPropertyType' is null or empty." );
        }

        try {
            configPropertyClass = Class.forName( configPropertyType );

            argClasses[ 0 ] = configPropertyClass;
            
            if ( configPropertyClass == Boolean.class ) {
                args[ 0 ] = Boolean.valueOf( configPropertyValue );
                argPrimitiveClasses[ 0 ] = Boolean.TYPE;
                return;
            }
            if ( configPropertyClass == String.class ) {
                args[ 0 ] = configPropertyValue;
                argPrimitiveClasses[ 0 ] = null;
                return;
            }
            if ( configPropertyClass == Integer.class ) {
                args[ 0 ] = Integer.valueOf( configPropertyValue );
                argPrimitiveClasses[ 0 ] = Integer.TYPE;
                return;
            }
            if ( configPropertyClass == Double.class ) {
                args[ 0 ] = Double.valueOf( configPropertyValue );
                argPrimitiveClasses[ 0 ] = Double.TYPE;
                return;
            }
            if ( configPropertyClass == Byte.class ) {
                args[ 0 ] = Byte.valueOf( configPropertyValue );
                argPrimitiveClasses[ 0 ] = Byte.TYPE;
                return;
            }
            if ( configPropertyClass == Short.class ) {
                args[ 0 ] = Short.valueOf( configPropertyValue );
                argPrimitiveClasses[ 0 ] = Short.TYPE;
                return;
            }
            if ( configPropertyClass == Long.class ) {
                args[ 0 ] = Long.valueOf( configPropertyValue );
                argPrimitiveClasses[ 0 ] = Long.TYPE;
                return;
            }
            if ( configPropertyClass == Float.class ) {
                args[ 0 ] = Float.valueOf( configPropertyValue );
                argPrimitiveClasses[ 0 ] = Float.TYPE;
                return;
            }
            if ( configPropertyClass == Character.class ) {
                if ( 1 != configPropertyValue.length() ) {
                    throw new ResourceException( "The argument 'configPropertyValue' is not valid for a character - '" + 
                                                 configPropertyValue + "'");
                }
                args[ 0 ] = new Character( configPropertyValue.charAt( 0 ) );
                argPrimitiveClasses[ 0 ] = Character.TYPE;
                return;
            }
        }
        catch( ClassNotFoundException e ) {
        }

        throw new ResourceException( "The argument 'configPropertyType' type is not valid - " + 
                                     configPropertyType ); 
    }


    /**
     * The entry representing the configuration properties that 
     * must be set.
     */
    private static class MissingConfigurationProperty {
        /**
         * The config property
         */
        private final DDConfigProperty _configProperty;

        /**
         * The get method for the config property
         */
        private final Method _configPropertyGetMethod;

        /**
         * The default value
         */
        private final Object _defaultValue;

        /**
         * The next missing configuration property
         */
        private MissingConfigurationProperty _next;

        /**
         * Create the MissingConfigurationProperty
         *
         * @param configProperty the configuration property 
         * (required)
         * @param configPropertyGetMethod the configuration property 
         * get method (required)
         * @param defaultValue the default value (optional)
         */
        private MissingConfigurationProperty(DDConfigProperty configProperty,
                                             Method configPropertyGetMethod, 
                                             Object defaultValue) {
            _configProperty = configProperty;
            _configPropertyGetMethod = configPropertyGetMethod;
            _defaultValue = defaultValue;
            _next = null;
        }
    }
}
