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
public class DDIcon
{


    private String _smallIcon;


    private String _largeIcon;


    /**
     * The name of the file containing an icon for the resource adapter module.
     * The file name is a relative path within the resource adapter module.
     * The file is either GIF or JPG format.
     *
     * @return Name of the file containing an icon
     */
    public String getSmallIcon()
    {
        return _smallIcon;
    }


    public void setSmallIcon( String icon )
    {
        _smallIcon = icon;
    }


    /**
     * The name of the file containing an icon for the resource adapter module.
     * The file name is a relative path within the resource adapter module.
     * The file is either GIF or JPG format.
     *
     * @return Name of the file containing an icon
     */
    public String getLargeIcon()
    {
        return _largeIcon;
    }


    public void setLargeIcon( String icon )
    {
        _largeIcon = icon;
    }


}
