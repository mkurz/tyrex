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
 * $Id: AppPath.java,v 1.4 2000/09/08 23:18:51 mohammed Exp $
 */


package tyrex.conf;


import java.io.Serializable;


/**
 * Defines an application path for the environment entry and
 * resource visibility.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $ $Date: 2000/09/08 23:18:51 $
 * @see Resource#getVisible
 * @see Visible
 */
public class AppPath
    implements Serializable
{


    /**
     * The wild card character. If this character is used at the end
     * of the path, application names are matched partially against
     * the path.
     */
    public static final String WildCard = "*";


    /**
     * The path to match against.
     */
    private String  _path;


    /**
     * True if a wild card has been used.
     */
    private boolean _wildCard;


    public AppPath( String appPath )
    {
	setContent( appPath );
    }


    public AppPath()
    {
    }


    public void setContent( String appPath )
    {
	if ( appPath.endsWith( WildCard ) ) {
	    _wildCard = true;
	    _path = appPath.substring( 0, appPath.length() - 1 );
	} else {
	    _wildCard = false;
	    _path = appPath;
	}
    }


    public String getContent()
    {
	if ( _wildCard ) {
	    return _path + WildCard;
	} else {
	    return _path;
	}
    }


    /**
     * Returns true if this resource is visible to the specified
     * application. If the application name is matched against the
     * full name (no wild card) or the partial name (with wild card),
     * this method returns true.
     *
     * @param appName The application's name
     * @return True if resource is visible to this application
     */
    public boolean isVisible( String appName )
    {
	return ( _wildCard && appName.startsWith( _path ) ) ||
	       ( ! _wildCard && appName.equals( _path ) );
    }


}
