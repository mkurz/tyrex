/**
 * Copyright (C) 2000, Intalio Inc.
 *
 * The program(s) herein may be used and/or copied only with the
 * written permission of Intalio Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */


package tyrex.tm.jca.dd;


/**
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version $Revision: 1.1 $
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
