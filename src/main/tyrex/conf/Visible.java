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
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: Visible.java,v 1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.conf;


import java.util.Vector;
import java.util.Enumeration;
import java.io.Serializable;


/**
 * Visibily list holds the name of all applications and paths to which
 * a resource is visible.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see Resource#getVisible
 * @see AppPath
 */
public class Visible
    implements Serializable
{


    /**
     * List of all application names and paths to which the resource
     * is visible. Each element is of type {@link AppPath}.
     */
    private Vector  _appPaths = new Vector();


    /**
     * Add a path to the visibility list. The resource/entry
     * will be visible to all applications specified by this
     * path.
     */
    public void addAppPath( AppPath appPath )
    {
	if ( ! _appPaths.contains( appPath ) )
	    _appPaths.addElement( appPath );
    }


    /**
     * Returns a list of all the application paths. Each element
     * is of type {@link AppPath}.
     */
    public Enumeration listAppPaths()
    {
	return _appPaths.elements();
    }


    /**
     * Sets the visibility to all applications. This has the
     * effect of removing all existing paths (if any) and adding
     * one path <tt>*</tt>.
     */
    public void setVisibilityAll()
    {
	_appPaths.clear();
	_appPaths.addElement( new AppPath( AppPath.WildCard ) );
    }


    /**
     * Sets the visibility to no applications. This has the
     * effect of removing all existing paths.
     */
    public void setVisibilityNone()
    {
	_appPaths.clear();
    }


    /**
     * Returns true if this resource is visible to the specified
     * application. If the application name is matched against any
     * of the paths, this method returns true. If the application
     * name does not match any of the paths, this method returns
     * false and the resource is not made visible to the application.
     *
     * @param appName The application's name
     * @return True if resource is visible to this application
     */
    public boolean isVisible( String appName )
    {
	Enumeration enum;
	AppPath     appPath;

	enum = _appPaths.elements();
	while ( enum.hasMoreElements() ) {
	    appPath = (AppPath) enum.nextElement();
	    if ( appPath.isVisible( appName ) )
		return true;
	}
	return false;
    }


}
