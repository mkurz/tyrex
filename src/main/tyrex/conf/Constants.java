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
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: Constants.java,v 1.1 2000/02/23 21:20:21 arkin Exp $
 */


package tyrex.conf;



/**
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/02/23 21:20:21 $
 */
public abstract class Constants
{


    public static class DTD
    {

	/**
	 * The public identifier of the DTD.
	 * <pre>
	 * -//EXOLAB/Tyrex Configuration DTD Version 1.0//EN
	 * </pre>
	 */
	public static final String PublicId = 
	    "-//EXOLAB/Tyrex Configuration DTD Version 1.0//EN";

	/**
	 * The system identifier of the DTD.
	 * <pre>
	 * http://tyrex.exolab.org/tyrex.dtd
	 * </pre>
	 */
	public static final String SystemId =
	    "http://tyrex.exolab.org/tyrex.dtd";

	/**
	 * The resource named of the DTD:
	 * <tt>/tyrex/conf/tyrex.dtd</tt>.
	 */
	public static final String Resource =
	    "/tyrex/conf/tyrex.dtd";

    }


    public static class Schema
    {

	/**
	 * The public identifier of the XML schema.
	 * <pre>
	 * -//EXOLAB/Tyrex Configuration Schema Version 1.0//EN
	 * </pre>
	 */
	public static final String PublicId =
	    "-//EXOLAB/Tyrex Configuration Schema Version 1.0//EN";

	/**
	 * The system identifier of the XML schema.
	 * <pre>
	 * http://tyrex.exolab.org/tyrex.xsd
	 * </pre>
	 */
	public static final String SystemId =
	    "http://tyrex.exolab.org/tyrex.xsd";
	
	/**
	 * The resource named of the server configuration XML schema:
	 * <tt>/tyrex/conf/tyrex.xsd</tt>.
	 */
	public static final String Resource =
	    "/tyrex/conf/tyrex.xsd";
	
    }


    public static class Namespace
    {

	/**
	 * The namespace prefix: <tt>tyrex</tt>
	 */
	public static final String prefix =
	    "tyrex";

	/**
	 * The namespace URI:
	 * <pre>
	 * http://tyrex.exolab.org/tyrex
	 * </pre>
	 */
	public static final String URI =
	    "http://tyrex.exolab.org/tyrex";

    }	


    public static class Implementation
    {

	/**
	 * The implementation vendor name, obtained from the manifest file.
	 */
	public static final String  Vendor;

	/**
	 * The implementation version number, obtained from the manifest file.
	 */
	public static final String  Version;

	static
	{
	    Package pkg;
	    
	    // Determine the implementation vendor name and version number
	    // from the package information in the manifest file
	    pkg = Server.class.getPackage();
	    if ( pkg != null ) {
		Vendor = pkg.getImplementationVendor();
		Version = pkg.getImplementationVersion();
	    } else {
		Vendor = "Exoffice Technologies. Inc";
		Version = "";
	    }
	}

    }


    public static final String FileName = "tyrex.xml";


    public static final String ResourceName = "/tyrex.xml";


}