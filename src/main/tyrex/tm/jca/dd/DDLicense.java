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
public class DDLicense
{


    public static final String TRUE  = "true";


    public static final String FALSE = "false";


    private String _description;


    private String _licenseRequired;


    public String getDescription()
    {
        return _description;
    }


    public void setDescription( String description )
    {
        _description = description;
    }


    /**
     * Specifies whether a license is required to deploy and use the
     * resource adapter. Valid values are {@link #TRUE} or {@link #FALSE}.
     *
     * @return Specifies whether a license is required
     */
    public String getLicenseRequired()
    {
        return _licenseRequired;
    }


    public void setLicenseRequired( String required )
    {
        _licenseRequired = required;
    }


}
