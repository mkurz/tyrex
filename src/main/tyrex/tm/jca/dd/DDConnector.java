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
public class DDConnector
{


    private String            _displayName;


    private String            _description;


    private String            _vendorName;


    private String            _specVersion;


    private String            _eisType;


    private String            _version;


    private DDIcon            _icon;


    private DDLicense         _license;


    private DDResourceAdapter _adapter;


    /**
     * A short name of the resource adatper that is intended to be displayed
     * by tools.
     *
     * @return A short name of the resource adatper
     */
    public String getDisplayName()
    {
        return _displayName;
    }


    public void setDisplayName( String displayName )
    {
        _displayName = displayName;
    }


    public String getDescription()
    {
        return _description;
    }


    public void setDescription( String description )
    {
        _description = description;
    }


    public DDIcon getIcon()
    {
        return _icon;
    }


    public void setIcon( DDIcon icon )
    {
        _icon = icon;
    }


    /**
     * Specifies the name of the resource adapter provider.
     *
     * @return The name of the resource adapter provider
     */
    public String getVendorName()
    {
        return _vendorName;
    }


    public void setVendorName( String vendorName )
    {
        _vendorName = vendorName;
    }


    /**
     * The version of the connector architecture specification that is
     * supported by this resource adapter.
     *
     * @return Version of the connector architecture specification supported
     */
    public String getSpecVersion()
    {
        return _specVersion;
    }


    public void setSpecVersion( String specVersion )
    {
        _specVersion = specVersion;
    }


    /**
     * Information about the type of the EIS. For example, the product name
     * of EIS independent of any version information.
     *
     * @return The type of the EIS
     */
    public String getEisType()
    {
        return _eisType;
    }


    public void setEisType( String eisType )
    {
        _eisType = eisType;
    }


    /**
     * Specifies a string-based version of the resource adapter from the
     * resource adapter provider.
     *
     * @return A string-based version of the resource adapter
     */
    public String getVersion()
    {
        return _version;
    }


    public void setVersion( String version )
    {
        _version = version;
    }


    /**
     * Specifies licensing requirements for the resource adapter module.
     * Specifies wether a license is required to deply and use this resource
     * adapter, and an optional description of the licensing terms.
     *
     * @return Licensing requirements for the resource adapte
     */
    public DDLicense getLicense()
    {
        return _license;
    }


    public void setLicense( DDLicense license )
    {
        _license = license;
    }


    public DDResourceAdapter getResourceadapter()
    {
        return _adapter;
    }


    public void addResourceadapter( DDResourceAdapter adapter )
    {
        _adapter = adapter;
    }


}
