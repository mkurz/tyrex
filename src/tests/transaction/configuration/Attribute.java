/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id: Attribute.java,v 1.3 2001/07/10 19:16:20 mohammed Exp $
 */

package transaction.configuration;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import org.exolab.castor.xml.*;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.DocumentHandler;
import transaction.configuration.types.Type;

/**
 * 
 * @version $Revision: 1.3 $ $Date: 2001/07/10 19:16:20 $
**/
public class Attribute implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _name;

    private java.lang.String _value;

    private transaction.configuration.types.Type _type;


      //----------------/
     //- Constructors -/
    //----------------/

    public Attribute() {
        super();
    } //-- transaction.configuration.Attribute()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public java.lang.String getName()
    {
        return this._name;
    } //-- java.lang.String getName() 

    /**
    **/
    public transaction.configuration.types.Type getType()
    {
        return this._type;
    } //-- transaction.configuration.types.Type getType() 

    /**
    **/
    public java.lang.String getValue()
    {
        return this._value;
    } //-- java.lang.String getValue() 

    /**
    **/
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * 
     * @param out
    **/
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * 
     * @param handler
    **/
    public void marshal(org.xml.sax.DocumentHandler handler)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.DocumentHandler) 

    /**
     * 
     * @param _name
    **/
    public void setName(java.lang.String _name)
    {
        this._name = _name;
    } //-- void setName(java.lang.String) 

    /**
     * 
     * @param _type
    **/
    public void setType(transaction.configuration.types.Type _type)
    {
        this._type = _type;
    } //-- void setType(transaction.configuration.types.Type) 

    /**
     * 
     * @param _value
    **/
    public void setValue(java.lang.String _value)
    {
        this._value = _value;
    } //-- void setValue(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static transaction.configuration.Attribute unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (transaction.configuration.Attribute) Unmarshaller.unmarshal(transaction.configuration.Attribute.class, reader);
    } //-- transaction.configuration.Attribute unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
