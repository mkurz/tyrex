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
 * $Id: DDConfigProperty.java,v 1.2 2001/03/12 19:20:17 arkin Exp $
 */


package tyrex.resource.jca.dd;


/**
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $
 */
public class DDConfigProperty
{


    public static final String SERVER_NAME    = "ServerName";


    public static final String PORT_NUMBER    = "PortNumber";


    public static final String USER_NAME      = "UserName";


    public static final String PASSWORD       = "Password";


    public static final String CONNECTION_URL = "ConnectionURL";


    private String _description;


    private String _name;


    private String _type;


    private String _value;


    public String getDescription()
    {
        return _description;
    }


    public void setDescription( String description )
    {
        _description = description;
    }


    /**
     * The name of the configuration property.
     *
     * @return The name of the configuration property
     */
    public String getConfigPropertyName()
    {
        return _name;
    }


    public void setConfigPropertyName( String name )
    {
        _name = name;
    }


    /**
     * The fully qualified Java type of a configuration property. The following are
     * the legal values: <tt>java.lang.Boolean</tt>, <tt>java.lang.Boolean</tt>,
     * <tt>java.lang.String</tt>, <tt>java.lang.Integer</tt>, <tt>java.lang.Double</tt>,
     * <tt>java.lang.Byte</tt>,<tt>java.lang.Short</tt>,<tt>java.lang.Long</tt> and
     * <tt>java.lang.Float</tt>.
     *
     * @return The type of the configuration property
     */
    public String getConfigPropertyType()
    {
        return _type;
    }


    public void setConfigPropertyType( String type )
    {
        _type = type;
    }


    /**
     * The value of a configuration entry.
     *
     * @return The value of a configuration entry
     */
    public String getConfigPropertyValue()
    {
        return _value;
    }


    public void setConfigPropertyValue( String value )
    {
        _value = value;
    }


}
