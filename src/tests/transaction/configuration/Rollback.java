/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.8.10</a>, using an
 * XML Schema.
 * $Id: Rollback.java,v 1.2 2001/02/23 17:17:43 omodica Exp $
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

/**
 * 
 * @version $Revision: 1.2 $ $Date: 2001/02/23 17:17:43 $
**/
public class Rollback implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private int _iterations;

    /**
     * keeps track of state for field: _iterations
    **/
    private boolean _has_iterations;

    private int _value;

    /**
     * keeps track of state for field: _value
    **/
    private boolean _has_value;


      //----------------/
     //- Constructors -/
    //----------------/

    public Rollback() {
        super();
    } //-- transaction.configuration.Rollback()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public void deleteIterations()
    {
        this._has_iterations= false;
    } //-- void deleteIterations() 

    /**
    **/
    public void deleteValue()
    {
        this._has_value= false;
    } //-- void deleteValue() 

    /**
    **/
    public int getIterations()
    {
        return this._iterations;
    } //-- int getIterations() 

    /**
    **/
    public int getValue()
    {
        return this._value;
    } //-- int getValue() 

    /**
    **/
    public boolean hasIterations()
    {
        return this._has_iterations;
    } //-- boolean hasIterations() 

    /**
    **/
    public boolean hasValue()
    {
        return this._has_value;
    } //-- boolean hasValue() 

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
     * @param _iterations
    **/
    public void setIterations(int _iterations)
    {
        this._iterations = _iterations;
        this._has_iterations = true;
    } //-- void setIterations(int) 

    /**
     * 
     * @param _value
    **/
    public void setValue(int _value)
    {
        this._value = _value;
        this._has_value = true;
    } //-- void setValue(int) 

    /**
     * 
     * @param reader
    **/
    public static transaction.configuration.Rollback unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (transaction.configuration.Rollback) Unmarshaller.unmarshal(transaction.configuration.Rollback.class, reader);
    } //-- transaction.configuration.Rollback unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator v = new org.exolab.castor.xml.Validator();
        v.validate(this, null);
    } //-- void validate() 

}
