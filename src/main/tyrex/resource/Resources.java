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


package tyrex.resource;


import java.util.HashMap;
import java.util.Iterator;
import tyrex.tm.TransactionDomain;

/**
 * Represents a collection of installed resources. Resources are
 * obtained from this collection by the name with which they were
 * installed.
 * <p>
 * The method {@link #addConfiguration addConfiguration} is called
 * to install a new resource configuration. The method {@link
 * #setTransactionDomain setTransactionDomain} is called to set the
 * transaction domain. The transaction domain is required to create
 * a {@link Resource} object from a {@link ResourceConfig} object.
 * <p>
 * The deployment process uses the methods {@link #addConfiguration
 * addConfiguration} and {@link #listConfigurations listConfigurations}
 * to add and list resource configurations.
 * <p>
 * The application server uses the methods {@link #listResources} and
 * {@link #getResource getResource} to obtain resources and make the
 * client connection factory available to the application.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.9 $
 */
public final class Resources
{


    /**
     * A collection of resource configurations using the name as key.
     */
    private final HashMap      _config;


    /**
     * A collection of resources using the name as key.
     */
    private final HashMap      _resources;


    /**
     * The transaction domain, may be null.
     */
    private TransactionDomain  _txDomain;


    /**
     * Default constructor.
     */
    public Resources()
    {
        _resources = new HashMap();
        _config = new HashMap();
    }


    /**
     * Sets the transaction domain for this resource list. This method must
     * be called before calling {@link #getResource}.
     *
     * @param txDomain The transaction domain
     */
    public void setTransactionDomain( TransactionDomain txDomain )
    {
        _txDomain = txDomain;
    }


    /**
     * Adds a resource configuration. Once added, the resource can be
     * obtained with a subsequent call to {@link #getResource}.
     *
     * @param config The resource configuration
     * @throws ResourceException A resource with this name already installed
     */
    public synchronized void addConfiguration( ResourceConfig config )
        throws ResourceException
    {
        if ( config == null )
            throw new IllegalArgumentException( "Argument config is null" );
        if ( _config.containsKey( config.getName() ) )
            throw new ResourceException( "A resource with the name " + config.getName() + " already installed" );
        _config.put( config.getName(), config );
    }


    /**
     * Returns all the resource configurations. Returns an iterator
     * of {@link ResourceConfig} objects that specify the configuration
     * of each resource.
     *
     * @return All the resource configurations
     */
    public Iterator listConfigurations()
    {
        return _config.values().iterator();
    }


    /**
     * Returns true if a resource by this name is installed.
     *
     * @param name The resource name
     * @return True if the resource is installed
     */
    public boolean hasResource( String name )
    {
        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        return _resources.containsKey( name );
    }


    /**
     * Returns an iterator of all the installed resources. Each element
     * is a string providing the resource name. The name can be used to
     * obtain the {@link Resource} object.
     *
     * @return An iterator of all installed resource names
     */
    public Iterator listResources()
    {
        return _config.keySet().iterator();
    }


    /**
     * Returns the named resource. The resource must have been installed
     * with a previous call to {@link #addConfiguration addConfiguration}
     * and the transaction domain must have been set up for this method
     * to succeed. It is possible that this method will not be able to
     * create the specified resource.
     *
     * @param name The resource name
     * @return The resource, null if no such resource installed
     * @throws ResourceException An error occured while attempting
     * to create this resource
     */
    public synchronized Resource getResource( String name )
        throws ResourceException
    {
        ResourceConfig    config;
        Resource          resource;
        TransactionDomain txDomain;

        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        resource = (Resource) _resources.get( name );
        if ( resource == null ) {
            config = (ResourceConfig) _config.get( name );
            if ( config == null )
                return null;
            txDomain = _txDomain;
            if ( txDomain == null )
                throw new ResourceException( "Must call setTransactionDomain() before calling getResource()" );
            resource = config.createResource( txDomain );
            _resources.put( name, resource );
        }
        return resource;
    }


    /**
     * Removes a resource. After return from this method, the
     * resource is no longer available and its client connection
     * factory is no longer useable.
     * <p>
     * This method automatically calls {@link Resource#destroy}.
     *
     * @param name The resource name
     */
    public synchronized void removeResource( String name )
    {
        Resource resource;

        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        _config.remove( name );
        resource = (Resource) _resources.remove( name );
        if ( resource != null )
            resource.destroy();
    }

    
}
