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
 * Copyright 2000, 2001 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: Version.java,v 1.1 2001/03/05 18:25:40 arkin Exp $
 */


package tyrex.util;


import java.util.StringTokenizer;


/**
 *
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2001/03/05 18:25:40 $
 */
public class Version
{


    /**
     * Returns true if the available version is compatible with the
     * required version. Compatibility is defined as the available
     * version being equal to or larger than the required version.
     * <p>
     * A version string is made up of several numerical components
     * with the most significant one coming first (e.g. 2.1.0 or
     * 99/12/25). The version components are compared one by one
     * starting with the most significant. All version components
     * are compared, if they are missing in their version, zero is
     * assumed (2.1 is equivalent to 2.1.0.0).
     * <p>
     * Available version 2.x will be compatible with required version
     * 1.x, 1.0 and 1 (the last two are the same) but not with
     * required version 3.x. 2.x will be compatible with 2.y if x
     * and y are independently compatible according to the same rules.
     * <p>
     * The supported version separators are <tt>. , / -</tt>.
     * <p>
     * True: available >= required
     *
     * @param available The available version number
     * @param required The ninimum required version number
     * @return True if available equals to or larger than the required
     */
    public static boolean isCompatibleWith( String available, String required )
    {
	StringTokenizer avlTok;
	StringTokenizer reqTok;
	int             avlVer;
	int             reqVer;

	// Return true if both are same version or same text string
	if ( available.equals( required ) )
	    return true;
	
	// The version string consists of any number of version components,
	// as in, 2.1.0 or 99/8/20. The first version component is the most
	// significant and must be matched first (2.1 is higher than 1.2).
	avlTok = new StringTokenizer( available, ".,/-" );
	reqTok = new StringTokenizer( required, ".,/-" );
	try {
	    while ( avlTok.hasMoreTokens() || reqTok.hasMoreTokens() ) {
		// Get the next significant version component.
		// If a less significant component is not available,
		// just assume zero (thus, 2.1 equals 2.1.0.0).
		if ( avlTok.hasMoreTokens() )
		    avlVer = Integer.parseInt( avlTok.nextToken() );
		else
		    avlVer = 0;
		if ( reqTok.hasMoreTokens() )
		    reqVer = Integer.parseInt( reqTok.nextToken() );
		else
		    reqVer = 0;

		// If available is 2 and required is 1, return true.
		// If available is 1 and required is 2, return false.
		// If available is 2 and required is 2, continue to
		// next version number, and if all match return
		// true at the end.
		if ( avlVer < reqVer )
		    return false;
		if ( avlVer > reqVer )
		    return true;
	    }
	} catch ( NumberFormatException except ) {
	    // Cannot process any non-numerical portion in the
	    // version string, so just return false.
	    return false;
	}
	// We reached this point is all version components were
	// equal in both version strings all along (including
	// missing zeros in either one).
	return true;
    }


}
