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


import java.io.PrintWriter;
import java.util.Set;
import javax.security.auth.Subject;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;


/**
 * Provides a means to create managed connections and connection factories
 * from a connector loaded in a separate class loader.
 * <p>
 * In order to enable deployment of multiple connector versions and their
 * dependent JARs, each connector can be loaded in a separate class loader.
 * <p>
 * The connector deployment descriptor specifies the interface and
 * implementation classes used by the connector.
 * <p>
 * This class provides a mechanism to obtain a new managed connection and
 * a new connection factory from a connector loaded in a separate class
 * loader. In addition, it validates that connection and factory objects
 * match the classes specified in the deployment descriptor.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $
 */
final class ConnectorLoader
{


    /**
     * An instance of the managed connection factory. This is used to
     * create new managed connections and connection factories.
     */
    protected final ManagedConnectionFactory  _managedFactory;


    /**
     * The class for the client connection.
     */
    private final Class                       _connectionClass;


    /**
     * The class for the client connection factory.
     */
    private final Class                       _factoryClass;


    /**
     * True if XA transactions supported by this connector.
     */
    protected final boolean                   _xaSupported;


    /**
     * True if local transactions supported by this connector.
     */
    protected final boolean                   _localSupported;


    /**
     * Constructs a new connection loader.
     *
     * @param loader The class loader to use
     * @param managedFactoryCN The class name of the managed connection factory
     * @param factoryCN The class name of the client connection factory
     * @param connCN The class name of the client connection
     * @param xaSupported True if XA transactions supported
     * @param localSupported True if local transactions supported
     * @throws Exception An error occured attempting to resolve any
     * of the specified class names
     */
    ConnectorLoader( ClassLoader loader, String managedFactoryCN,
                     String factoryCN, String connCN, boolean xaSupported, boolean localSupported )
        throws Exception
    {
        if ( loader == null )
            throw new IllegalArgumentException( "Argument loader is null" );
        _managedFactory = (ManagedConnectionFactory) loader.loadClass( managedFactoryCN ).newInstance();
        _factoryClass = loader.loadClass( factoryCN );
        _connectionClass = loader.loadClass( connCN );
        _xaSupported = xaSupported;
        _localSupported = localSupported;
    }


    protected void setLogWriter( PrintWriter logWriter )
        throws ResourceException
    {
        if ( logWriter != null )
            _managedFactory.setLogWriter( logWriter );
    }


    /**
     * Matches a managed connection. This method is similar to <tt>matchManagedConnections</tt>
     * in <tt>ConnectionManagedFactory</tt>, but validates that the resulting managed
     * connection matches the specified class.
     *
     * @return A managed connection, or null
     */
    protected ManagedConnection matchManagedConnections( Set set, Subject subject,
                                                         ConnectionRequestInfo requestInfo )
        throws ResourceException
    {
        ManagedConnection managed;

        managed = _managedFactory.matchManagedConnections( set, subject, requestInfo );
        if ( managed == null )
            return null;
        return managed;
    }


    /**
     * Creates a new managed connection. This method is similar to <tt>createManagedConnection</tt>
     * in <tt>ConnectionManagedFactory</tt>, but validates that the resulting managed
     * connection matches the specified class.
     *
     * @return A managed connection
     */
    protected ManagedConnection createManagedConnection( Subject subject,
                                                         ConnectionRequestInfo requestInfo )
        throws ResourceException
    {
        ManagedConnection managed;

        managed = _managedFactory.createManagedConnection( subject, requestInfo );
        if ( managed == null )
            throw new ResourceException( "Connector error: returned null from createManagedConnetion" );
        return managed;
    }


    /**
     * Creates a new client connection. This method is similar to <tt>getConection</tt>
     * in <tt>ManagedConnection</tt>, but validates that the resulting client
     * connection matches the specified class.
     *
     * @return A client connection
     */
    protected Object getConnection( ManagedConnection managed, Subject subject,
                                    ConnectionRequestInfo requestInfo )
        throws ResourceException
    {
        Object connection;

        if ( managed == null )
            throw new IllegalArgumentException( "Argument managed is null" );
        connection = managed.getConnection( subject, requestInfo );
        if ( connection == null )
            throw new ResourceException( "Connector error: returned null from getConnection" );
        if ( _connectionClass.isAssignableFrom( connection.getClass() ) )
            return connection;
        throw new ResourceException( "Connector error: unexpected connection class " + managed.getClass().getName() );
    }


    /**
     * Creates a new client connection factory. This method is similar to
     * <tt>createConnectionFactory</tt> in <tt>ManagedConnectionFactory</tt>,
     * but validates that the resulting client connection factory matches
     * the specified class.
     *
     * @return A client connection factory
     */
    protected Object createConnectionFactory( ConnectionManager manager )
        throws ResourceException
    {
        Object factory;

        if ( manager == null )
            throw new IllegalArgumentException( "Argument manager is null" );
        factory = _managedFactory.createConnectionFactory( manager );
        if ( factory == null )
            throw new ResourceException( "Connector error: returned null from createConnectionFactory" );
        if ( _factoryClass.isAssignableFrom( factory.getClass() ) )
            return factory;
        throw new ResourceException( "Connector error: unexpected connection facotry class " + factory.getClass().getName() );
    }


    /**
     * Returns the managed connection factory. The managed connection
     * factory is used to configure the connector.
     *
     * @return The managed connection factory
     */
    protected ManagedConnectionFactory getConfigFactory()
    {
        return _managedFactory;
    }

    
}
