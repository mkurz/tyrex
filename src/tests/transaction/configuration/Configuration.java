/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.8.10</a>, using an
 * XML Schema.
 * $Id: Configuration.java,v 1.2 2001/02/23 17:17:43 omodica Exp $
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
public class Configuration implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.util.Vector _performanceList;

    private java.util.Vector _datasourceList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Configuration() {
        super();
        _performanceList = new Vector();
        _datasourceList = new Vector();
    } //-- transaction.configuration.Configuration()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vDatasource
    **/
    public void addDatasource(Datasource vDatasource)
        throws java.lang.IndexOutOfBoundsException
    {
        _datasourceList.addElement(vDatasource);
    } //-- void addDatasource(Datasource) 

    /**
     * 
     * @param vPerformance
    **/
    public void addPerformance(Performance vPerformance)
        throws java.lang.IndexOutOfBoundsException
    {
        _performanceList.addElement(vPerformance);
    } //-- void addPerformance(Performance) 

    /**
    **/
    public java.util.Enumeration enumerateDatasource()
    {
        return _datasourceList.elements();
    } //-- java.util.Enumeration enumerateDatasource() 

    /**
    **/
    public java.util.Enumeration enumeratePerformance()
    {
        return _performanceList.elements();
    } //-- java.util.Enumeration enumeratePerformance() 

    /**
     * 
     * @param index
    **/
    public Datasource getDatasource(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _datasourceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Datasource) _datasourceList.elementAt(index);
    } //-- Datasource getDatasource(int) 

    /**
    **/
    public Datasource[] getDatasource()
    {
        int size = _datasourceList.size();
        Datasource[] mArray = new Datasource[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Datasource) _datasourceList.elementAt(index);
        }
        return mArray;
    } //-- Datasource[] getDatasource() 

    /**
    **/
    public int getDatasourceCount()
    {
        return _datasourceList.size();
    } //-- int getDatasourceCount() 

    /**
     * 
     * @param index
    **/
    public Performance getPerformance(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _performanceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Performance) _performanceList.elementAt(index);
    } //-- Performance getPerformance(int) 

    /**
    **/
    public Performance[] getPerformance()
    {
        int size = _performanceList.size();
        Performance[] mArray = new Performance[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Performance) _performanceList.elementAt(index);
        }
        return mArray;
    } //-- Performance[] getPerformance() 

    /**
    **/
    public int getPerformanceCount()
    {
        return _performanceList.size();
    } //-- int getPerformanceCount() 

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
    public void removeAllDatasource()
    {
        _datasourceList.removeAllElements();
    } //-- void removeAllDatasource() 

    /**
    **/
    public void removeAllPerformance()
    {
        _performanceList.removeAllElements();
    } //-- void removeAllPerformance() 

    /**
     * 
     * @param index
    **/
    public Datasource removeDatasource(int index)
    {
        Object obj = _datasourceList.elementAt(index);
        _datasourceList.removeElementAt(index);
        return (Datasource) obj;
    } //-- Datasource removeDatasource(int) 

    /**
     * 
     * @param index
    **/
    public Performance removePerformance(int index)
    {
        Object obj = _performanceList.elementAt(index);
        _performanceList.removeElementAt(index);
        return (Performance) obj;
    } //-- Performance removePerformance(int) 

    /**
     * 
     * @param vDatasource
     * @param index
    **/
    public void setDatasource(Datasource vDatasource, int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _datasourceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _datasourceList.setElementAt(vDatasource, index);
    } //-- void setDatasource(Datasource, int) 

    /**
     * 
     * @param datasourceArray
    **/
    public void setDatasource(Datasource[] datasourceArray)
    {
        //-- copy array
        _datasourceList.removeAllElements();
        for (int i = 0; i < datasourceArray.length; i++) {
            _datasourceList.addElement(datasourceArray[i]);
        }
    } //-- void setDatasource(Datasource) 

    /**
     * 
     * @param vPerformance
     * @param index
    **/
    public void setPerformance(Performance vPerformance, int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _performanceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _performanceList.setElementAt(vPerformance, index);
    } //-- void setPerformance(Performance, int) 

    /**
     * 
     * @param performanceArray
    **/
    public void setPerformance(Performance[] performanceArray)
    {
        //-- copy array
        _performanceList.removeAllElements();
        for (int i = 0; i < performanceArray.length; i++) {
            _performanceList.addElement(performanceArray[i]);
        }
    } //-- void setPerformance(Performance) 

    /**
     * 
     * @param reader
    **/
    public static Configuration unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (Configuration) Unmarshaller.unmarshal(Configuration.class, reader);
    } //-- transaction.configuration.Configuration unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator v = new org.exolab.castor.xml.Validator();
        v.validate(this, null);
    } //-- void validate() 

}
