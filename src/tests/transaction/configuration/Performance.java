/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.8.10</a>, using an
 * XML Schema.
 * $Id: Performance.java,v 1.1 2001/02/16 23:47:55 mohammed Exp $
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
 * @version $Revision: 1.1 $ $Date: 2001/02/16 23:47:55 $
**/
public class Performance implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.util.Vector _groupList;

    private OnePhaseCommit _onePhaseCommit;

    private TwoPhaseCommit _twoPhaseCommit;

    private Rollback _rollback;


      //----------------/
     //- Constructors -/
    //----------------/

    public Performance() {
        super();
        _groupList = new Vector();
    } //-- transaction.configuration.Performance()


      //-----------/
     //- Methods -/
    //-----------/

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
    public java.util.Enumeration enumerateGroup()
    {
        return _groupList.elements();
    } //-- java.util.Enumeration enumerateGroup() 

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
    public OnePhaseCommit getOnePhaseCommit()
    {
        return this._onePhaseCommit;
    } //-- OnePhaseCommit getOnePhaseCommit() 

    /**
    **/
    public Rollback getRollback()
    {
        return this._rollback;
    } //-- Rollback getRollback() 

    /**
    **/
    public TwoPhaseCommit getTwoPhaseCommit()
    {
        return this._twoPhaseCommit;
    } //-- TwoPhaseCommit getTwoPhaseCommit() 

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
    public void removeAllGroup()
    {
        _groupList.removeAllElements();
    } //-- void removeAllGroup() 

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
     * @param _onePhaseCommit
    **/
    public void setOnePhaseCommit(OnePhaseCommit _onePhaseCommit)
    {
        this._onePhaseCommit = _onePhaseCommit;
    } //-- void setOnePhaseCommit(OnePhaseCommit) 

    /**
     * 
     * @param _rollback
    **/
    public void setRollback(Rollback _rollback)
    {
        this._rollback = _rollback;
    } //-- void setRollback(Rollback) 

    /**
     * 
     * @param _twoPhaseCommit
    **/
    public void setTwoPhaseCommit(TwoPhaseCommit _twoPhaseCommit)
    {
        this._twoPhaseCommit = _twoPhaseCommit;
    } //-- void setTwoPhaseCommit(TwoPhaseCommit) 

    /**
     * 
     * @param reader
    **/
    public static transaction.configuration.Performance unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (transaction.configuration.Performance) Unmarshaller.unmarshal(transaction.configuration.Performance.class, reader);
    } //-- transaction.configuration.Performance unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator.validate(this, null);
    } //-- void validate() 

}
