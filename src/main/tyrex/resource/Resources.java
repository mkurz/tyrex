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


package tyrex.resource;


import java.util.HashMap;
import java.util.Iterator;
import javax.transaction.SystemException;
import tyrex.tm.TransactionDomain;
import tyrex.resource.jdbc.DataSourceConfig;


/**
 *
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version $Revision: 1.1 $
 */
public class Resources
{


    private final HashMap      _config;


    private final HashMap      _resources;


    private TransactionDomain  _txDomain;


    public Resources()
    {
        _resources = new HashMap();
        _config = new HashMap();
    }


    public synchronized void addConfiguration( BaseConfiguration config )
        throws SystemException
    {
        if ( config == null )
            throw new IllegalArgumentException( "Argument config is null" );
        if ( _config.containsKey( config.getName() ) )
            throw new SystemException( "A resource with the name " + config.getName() + " already installed" );
        _config.put( config.getName(), config );
    }


    public synchronized Iterator getConfiguration()
    {
        return _config.entrySet().iterator();
    }


    public synchronized Resource[] createResources( TransactionDomain txDomain )
        throws SystemException
    {
        Iterator          iterator;
        String            name;
        BaseConfiguration config;
        Resource[]        resources;
        Object            object;

        if ( txDomain == null )
            throw new IllegalArgumentException( "Argument txDomain is null" );
        iterator = _config.keySet().iterator();
        while ( iterator.hasNext() ) {
            name = (String) iterator.next();
            if ( ! _resources.containsKey( name ) ) {
                config = (BaseConfiguration) _config.get( name );
                _resources.put( name, config.createResource( txDomain ) );
            }
        }
        resources = new Resource[ _resources.size() ];
        iterator = _resources.values().iterator();
        for ( int i = 0 ; i < resources.length ; ++i )
            resources[ i ] = (Resource) iterator.next();
        return resources;
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
        return _resources.keySet().iterator();
    }


    /**
     * Returns the named resource.
     *
     * @param name The resource name
     * @return The resource, null if no such resource installed
     */
    public Resource getResource( String name )
    {
        Resource resource;

        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        resource = (Resource) _resources.get( name );
        return resource;
    }

    
}
