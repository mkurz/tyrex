/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.8.10</a>, using an
 * XML Schema.
 * $Id: Type.java,v 1.1 2001/02/16 23:47:56 mohammed Exp $
 */

package transaction.configuration.types;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
import java.util.Hashtable;
import org.exolab.castor.xml.*;

/**
 * 
 * @version $Revision: 1.1 $ $Date: 2001/02/16 23:47:56 $
**/
public class Type implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The int type
    **/
    public static final int INT_TYPE = 0;

    /**
     * The instance of the int type
    **/
    public static final Type INT = new Type(INT_TYPE, "int");

    /**
     * The integer type
    **/
    public static final int INTEGER_TYPE = 1;

    /**
     * The instance of the integer type
    **/
    public static final Type INTEGER = new Type(INTEGER_TYPE, "integer");

    /**
     * The string type
    **/
    public static final int STRING_TYPE = 2;

    /**
     * The instance of the string type
    **/
    public static final Type STRING = new Type(STRING_TYPE, "string");

    private static java.util.Hashtable _memberTable = init();

    private int type = -1;

    private java.lang.String stringValue = null;


      //----------------/
     //- Constructors -/
    //----------------/

    private Type(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- transaction.configuration.types.Type(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the type of this Type
    **/
    public int getType()
    {
        return this.type;
    } //-- int getType() 

    /**
    **/
    private static java.util.Hashtable init()
    {
        Hashtable members = new Hashtable();
        members.put("int", INT);
        members.put("integer", INTEGER);
        members.put("string", STRING);
        return members;
    } //-- java.util.Hashtable init() 

    /**
     * Returns the String representation of this Type
    **/
    public java.lang.String toString()
    {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Returns a new Type based on the given String value.
     * @param string
    **/
    public static transaction.configuration.types.Type valueOf(java.lang.String string)
    {
        Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid Type";
            throw new IllegalArgumentException(err);
        }
        return (Type) obj;
    } //-- transaction.configuration.types.Type valueOf(java.lang.String) 

}
