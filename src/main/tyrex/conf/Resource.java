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
 * Copyright 2000 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: Resource.java,v 1.5 2001/02/23 18:58:03 jdaniel Exp $
 */


package tyrex.conf;


import java.io.Serializable;
import java.util.Vector;
import java.util.Enumeration;
import tyrex.resource.ResourcePoolManager;
import tyrex.resource.ResourceFactoryBuilder;
import tyrex.util.Messages;
import tyrex.util.Logger;


/**
 * Defines a resource. A mapping between a resource reference and
 * the actual resource factory. The visibility property allows this
 * resource to be available only to applications in a particular
 * path. Multiple resources with the same name but different
 * visibility lists are supported.
 * <p>
 * The following resource factories are supported:
 * <ul>
 * <li>javax.sql.DataSource
 * </ul>
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $ $Date: 2001/02/23 18:58:03 $
 * @see ResourceFactoryBuilder
 * @see PoolManager
 */
public class Resource
    implements Serializable
{


    public static class Authentication
    {

	/**
	 * Value for <tt>res-auth</tt> attribute indicating resource
	 * authentication is provided by the container.
	 */
	public static final String Container = "container";

	/**
	 * Value for <tt>res-auth</tt> attribute indicating resource
	 * authentication is provided by the application.
	 */
	public static final String Application = "application";

    }


    /**
     * The name of the resource.
     */
    private String  _name;


    /**
     * The type of the resource (the actual class name).
     */
    private String  _type;


    /**
     */
    private Object  _param;


    /**
     * The pool manager associated with this resource.
     */
    private ResourcePoolManager _pool;


    /**
     * The authentication type specified by this resource.
     */
    private String  _auth;


    /**
     * The visibility list.
     */
    private Visible  _visible;


    /**
     * Sets the resource name.
     *
     * @param name The resource name
     */
    public void setResName( String name )
    {
	_name = name;
    }


    /**
     * Returns the resource name.
     *
     * @return The resource name
     */
    public String getResName()
    {
	return _name;
    }


    /**
     * Sets the resource type. This is the name of the class
     * used to construct a new resource (aka resource factory).
     *
     * @param type The resource type (class name)
     */
    public void setResType( String type )
    {
	_type = type;
    }


    /**
     * Returns the resource type.
     *
     * @return The resource type
     */
    public String getResType()
    {
	return _type;
    }


    /**
     * Sets the resource authentication to either {@link
     * Authentication#Container} or {@link Authentication#Application}.
     *
     * @param auth The authentication type
     */
    public void setResAuth( String auth )
    {
	_auth = auth;
    }


    /**
     * Returns the resource authentication type.
     *
     * @return The authentication type
     */
    public String getResAuth()
    {
	return _auth;
    }


    /**
     * Returns true if the resource authentication is provided by the
     * application, false if provided by the container.
     */
    public boolean isApplicationAuth()
    {
	return ( _auth != null && _auth.equals( Authentication.Application ) );
    }


    /**
     * Sets the parameters for the resource factory. The exact object
     * type will change depending on the type of the resource factory.
     * For example, {@link javax.sql.XADataSource} would be the object
     * type for JDBC resources.
     */
    public void setParam( Object obj )
    {
	_param = obj;
    }


    /**
     * @see #setParam
     */
    public Object getParam()
    {
	return _param;
    }


    /**
     * Called by the marshal descriptor to construct a new object
     * for the specified resource based on the resource's type.
     * Since <tt>param</tt> is an element this method will be
     * called after <tt>type</tt> has been set.
     */
    public Object createParam()
    {
	try {
	    return Class.forName( _type ).newInstance();
	} catch ( Exception except ) {
	    Logger.conf.warn( Messages.format( "tyrex.conf.cannotCreateFactory",
							       _type, except ) );
	    return null;
	}
    }


    /**
     * Sets the pool manager for use with this resource.
     *
     * @param pool The pool manager
     * @see PoolManager
     */
    public void setPool( ResourcePoolManager pool )
    {
	_pool = pool;
    }


    /**
     * Returns the pool manager for use with this resource.
     *
     * @return The pool manager
     * @see PoolManager
     */
    public ResourcePoolManager getPool()
    {
	return _pool;
    }


    /**
     * Returns the visibility list of this resource.
     *
     * @return The visibility list
     * @see Visible
     */
    public Visible getVisible()
    {
	if ( _visible == null )
	    _visible = new Visible();
	return _visible;
    }


    /**
     * Sets the visibility list of this resource. If this
     * resource already has some visibility, the two lists
     * are merged.
     *
     * @param visible The visibility list
     * @see Visible
     */
    public void setVisible( Visible visible )
    {
	Enumeration enum;

	if ( _visible != null ) {
	    enum = visible.listAppPaths();
	    while ( enum.hasMoreElements() ) {
		_visible.addAppPath( (AppPath) enum.nextElement() );
	    }
	} else {
	    _visible = visible;
	}
    }


    /**
     * Returns true if this resource is visible to the specified
     * application. For a Servlet the application name would be the
     * Servlet's document base. If the visibility list specifies this
     * resource is visible to the application, this method will return
     * true.
     *
     * @param appName The application's name
     * @return True if resource is visible to this application
     */
    public boolean isVisible( String appName )
    {
	if ( _visible == null )
	    return false;
	else
	    return _visible.isVisible( appName );
    }


    /**
     * Called to construct and return the actual resource factory.
     * The returned object will be a container manager resource
     * factory using the specified resource factory provided to
     * {@link #setParam}. For example, <tt>tyrex.jdbc.ServerDataSource</tt>
     * will be created if the parameters are of type
     * <tt>javax.sql.XADataSource</tt>.
     */
    public Object createResourceFactory()
    {
	Enumeration            enum;
	ResourceFactoryBuilder resBuilder;
	Object                 factory;

	if ( _param == null )
	    return null;
	enum = ResourceFactoryBuilder.listFactoryBuilders();
	while ( enum.hasMoreElements() ) {
	    // Loop through all the registered factory builders
	    // looking for the one that can create a factory for
	    // this resource.
	    resBuilder = (ResourceFactoryBuilder) enum.nextElement();
	    factory = resBuilder.buildResourceFactory( _param, _pool );
	    if ( factory != null )
		return factory;
	}
	return null;
    }




}
