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
 * $Id: EnvEntry.java,v 1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.conf;


import java.io.Serializable;
import java.util.Enumeration;


/**
 * An environment entry read from the resources configuration file.
 * Similar to the deployment descriptor environment entry, but adds
 * support for visibility based on application path.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see Visible
 */
public class EnvEntry
    implements Serializable
{


    public static class Types
    {

	public static final String String  = String.class.getName();
	public static final String Integer = Integer.class.getName();
	public static final String Long    = Long.class.getName();
	public static final String Byte    = Byte.class.getName();
	public static final String Float   = Float.class.getName();
	public static final String Double  = Double.class.getName();
	public static final String Boolean = Boolean.class.getName();

    }

    
    /**
     * The name of this entry.
     */
    private String  _name;


    /**
     * The type of this entry. Defaults to a string.
     */
    private String  _type = Types.String;


    /**
     * The value of this entry (could be null).
     */
    private String  _value;


    /**
     * The visibility list.
     */
    private Visible  _visible;


    public void setEnvEntryName( String name )
    {
	_name = name;
    }


    public String getEnvEntryName()
    {
	return _name;
    }


    public void setEnvEntryType( String type )
    {
	_type = type;
    }


    public String getEnvEntryType()
    {
	return _type;
    }


    public void setEnvEntryValue( String value )
    {
	_value = value;
    }


    public String getEnvEntryValue()
    {
	return _value;
    }


    /**
     * Returns the visibility list of this entry.
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
     * Sets the visibility list of this entry. If this entry
     * already has some visibility, the two lists are merged.
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
     * Returns true if this environment entry is visible to the specified
     * application. For a Servlet the application name would be the
     * Servlet's document base. If the visibility list specifies this
     * entry is visible to the application, this method will return
     * true.
     *
     * @param appName The application's name
     * @return True if entry is visible to this application
     */
    public boolean isVisible( String appName )
    {
	if ( _visible == null )
	    return false;
	else
	    return _visible.isVisible( appName );
    }


}
