/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio. Exolab is a registered
 *    trademark of Intalio.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999-2001 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: DDConnector.java,v 1.2 2001/03/12 19:20:17 arkin Exp $
 */


package tyrex.resource.jca.dd;


/**
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $
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
