/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.8.10</a>, using an
 * XML Schema.
 * $Id: Datasource.java,v 1.2 2001/02/23 17:17:43 omodica Exp $
 */

package transaction.configuration;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;
import org.exolab.castor.xml.*;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.DocumentHandler;

/**
 * 
 * @version $Revision: 1.2 $ $Date: 2001/02/23 17:17:43 $
**/
public class Datasource implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _name;

    private java.lang.String _type;

    private java.util.Vector _attributeList;

    private java.lang.String _uri;

    private java.util.Vector _groupList;

    private java.lang.String _userName;

    private java.lang.String _password;

    private java.lang.String _tableName;

    private int _failSleepTime;

    /**
     * keeps track of state for field: _failSleepTime
    **/
    private boolean _has_failSleepTime;

    private boolean _performanceTest;

    /**
     * keeps track of state for field: _performanceTest
    **/
    private boolean _has_performanceTest;

    private boolean _reuseDelistedXaresources;

    /**
     * keeps track of state for field: _reuseDelistedXaresources
    **/
    private boolean _has_reuseDelistedXaresources;


      //----------------/
     //- Constructors -/
    //----------------/

    public Datasource() {
        super();
        _attributeList = new Vector();
        _groupList = new Vector();
    } //-- transaction.configuration.Datasource()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vAttribute
    **/
    public void addAttribute(Attribute vAttribute)
        throws java.lang.IndexOutOfBoundsException
    {
        _attributeList.addElement(vAttribute);
    } //-- void addAttribute(Attribute) 

    /**
     * 
     * @param vGroup
    **/
    public void addGroup(java.lang.String vGroup)
        throws java.lang.IndexOutOfBoundsException
    {
        _groupList.addElement(vGroup);
    } //-- void addGroup(java.lang.String) 

    /**
    **/
    public void deleteFailSleepTime()
    {
        this._has_failSleepTime= false;
    } //-- void deleteFailSleepTime() 

    /**
    **/
    public void deletePerformanceTest()
    {
        this._has_performanceTest= false;
    } //-- void deletePerformanceTest() 

    /**
    **/
    public void deleteReuseDelistedXaresources()
    {
        this._has_reuseDelistedXaresources= false;
    } //-- void deleteReuseDelistedXaresources() 

    /**
    **/
    public java.util.Enumeration enumerateAttribute()
    {
        return _attributeList.elements();
    } //-- java.util.Enumeration enumerateAttribute() 

    /**
    **/
    public java.util.Enumeration enumerateGroup()
    {
        return _groupList.elements();
    } //-- java.util.Enumeration enumerateGroup() 

    /**
     * 
     * @param index
    **/
    public Attribute getAttribute(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _attributeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Attribute) _attributeList.elementAt(index);
    } //-- Attribute getAttribute(int) 

    /**
    **/
    public Attribute[] getAttribute()
    {
        int size = _attributeList.size();
        Attribute[] mArray = new Attribute[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Attribute) _attributeList.elementAt(index);
        }
        return mArray;
    } //-- Attribute[] getAttribute() 

    /**
    **/
    public int getAttributeCount()
    {
        return _attributeList.size();
    } //-- int getAttributeCount() 

    /**
    **/
    public int getFailSleepTime()
    {
        return this._failSleepTime;
    } //-- int getFailSleepTime() 

    /**
     * 
     * @param index
    **/
    public java.lang.String getGroup(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _groupList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (String)_groupList.elementAt(index);
    } //-- java.lang.String getGroup(int) 

    /**
    **/
    public java.lang.String[] getGroup()
    {
        int size = _groupList.size();
        java.lang.String[] mArray = new String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String)_groupList.elementAt(index);
        }
        return mArray;
    } //-- java.lang.String[] getGroup() 

    /**
    **/
    public int getGroupCount()
    {
        return _groupList.size();
    } //-- int getGroupCount() 

    /**
    **/
    public java.lang.String getName()
    {
        return this._name;
    } //-- java.lang.String getName() 

    /**
    **/
    public java.lang.String getPassword()
    {
        return this._password;
    } //-- java.lang.String getPassword() 

    /**
    **/
    public boolean getPerformanceTest()
    {
        return this._performanceTest;
    } //-- boolean getPerformanceTest() 

    /**
    **/
    public boolean getReuseDelistedXaresources()
    {
        return this._reuseDelistedXaresources;
    } //-- boolean getReuseDelistedXaresources() 

    /**
    **/
    public java.lang.String getTableName()
    {
        return this._tableName;
    } //-- java.lang.String getTableName() 

    /**
    **/
    public java.lang.String getType()
    {
        return this._type;
    } //-- java.lang.String getType() 

    /**
    **/
    public java.lang.String getUri()
    {
        return this._uri;
    } //-- java.lang.String getUri() 

    /**
    **/
    public java.lang.String getUserName()
    {
        return this._userName;
    } //-- java.lang.String getUserName() 

    /**
    **/
    public boolean hasFailSleepTime()
    {
        return this._has_failSleepTime;
    } //-- boolean hasFailSleepTime() 

    /**
    **/
    public boolean hasPerformanceTest()
    {
        return this._has_performanceTest;
    } //-- boolean hasPerformanceTest() 

    /**
    **/
    public boolean hasReuseDelistedXaresources()
    {
        return this._has_reuseDelistedXaresources;
    } //-- boolean hasReuseDelistedXaresources() 

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
    **/
    public void removeAllAttribute()
    {
        _attributeList.removeAllElements();
    } //-- void removeAllAttribute() 

    /**
    **/
    public void removeAllGroup()
    {
        _groupList.removeAllElements();
    } //-- void removeAllGroup() 

    /**
     * 
     * @param index
    **/
    public Attribute removeAttribute(int index)
    {
        Object obj = _attributeList.elementAt(index);
        _attributeList.removeElementAt(index);
        return (Attribute) obj;
    } //-- Attribute removeAttribute(int) 

    /**
     * 
     * @param index
    **/
    public java.lang.String removeGroup(int index)
    {
        Object obj = _groupList.elementAt(index);
        _groupList.removeElementAt(index);
        return (String)obj;
    } //-- java.lang.String removeGroup(int) 

    /**
     * 
     * @param vAttribute
     * @param index
    **/
    public void setAttribute(Attribute vAttribute, int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _attributeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _attributeList.setElementAt(vAttribute, index);
    } //-- void setAttribute(Attribute, int) 

    /**
     * 
     * @param attributeArray
    **/
    public void setAttribute(Attribute[] attributeArray)
    {
        //-- copy array
        _attributeList.removeAllElements();
        for (int i = 0; i < attributeArray.length; i++) {
            _attributeList.addElement(attributeArray[i]);
        }
    } //-- void setAttribute(Attribute) 

    /**
     * 
     * @param _failSleepTime
    **/
    public void setFailSleepTime(int _failSleepTime)
    {
        this._failSleepTime = _failSleepTime;
        this._has_failSleepTime = true;
    } //-- void setFailSleepTime(int) 

    /**
     * 
     * @param vGroup
     * @param index
    **/
    public void setGroup(java.lang.String vGroup, int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _groupList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _groupList.setElementAt(vGroup, index);
    } //-- void setGroup(java.lang.String, int) 

    /**
     * 
     * @param groupArray
    **/
    public void setGroup(java.lang.String[] groupArray)
    {
        //-- copy array
        _groupList.removeAllElements();
        for (int i = 0; i < groupArray.length; i++) {
            _groupList.addElement(groupArray[i]);
        }
    } //-- void setGroup(java.lang.String) 

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
     * @param _password
    **/
    public void setPassword(java.lang.String _password)
    {
        this._password = _password;
    } //-- void setPassword(java.lang.String) 

    /**
     * 
     * @param _performanceTest
    **/
    public void setPerformanceTest(boolean _performanceTest)
    {
        this._performanceTest = _performanceTest;
        this._has_performanceTest = true;
    } //-- void setPerformanceTest(boolean) 

    /**
     * 
     * @param _reuseDelistedXaresources
    **/
    public void setReuseDelistedXaresources(boolean _reuseDelistedXaresources)
    {
        this._reuseDelistedXaresources = _reuseDelistedXaresources;
        this._has_reuseDelistedXaresources = true;
    } //-- void setReuseDelistedXaresources(boolean) 

    /**
     * 
     * @param _tableName
    **/
    public void setTableName(java.lang.String _tableName)
    {
        this._tableName = _tableName;
    } //-- void setTableName(java.lang.String) 

    /**
     * 
     * @param _type
    **/
    public void setType(java.lang.String _type)
    {
        this._type = _type;
    } //-- void setType(java.lang.String) 

    /**
     * 
     * @param _uri
    **/
    public void setUri(java.lang.String _uri)
    {
        this._uri = _uri;
    } //-- void setUri(java.lang.String) 

    /**
     * 
     * @param _userName
    **/
    public void setUserName(java.lang.String _userName)
    {
        this._userName = _userName;
    } //-- void setUserName(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static transaction.configuration.Datasource unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (transaction.configuration.Datasource) Unmarshaller.unmarshal(transaction.configuration.Datasource.class, reader);
    } //-- transaction.configuration.Datasource unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator v = new org.exolab.castor.xml.Validator();
        v.validate(this, null);
    } //-- void validate() 

}
