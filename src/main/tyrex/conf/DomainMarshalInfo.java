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
 * $Id: DomainMarshalInfo.java,v 1.1 2000/02/23 21:20:21 arkin Exp $
 */


package tyrex.conf;


import java.lang.reflect.Method;
import java.io.IOException;
import org.exolab.castor.xml.MarshalInfo;
import org.exolab.castor.xml.MarshalDescriptor;
import org.exolab.castor.xml.MarshalHelper;
import org.exolab.castor.xml.SimpleMarshalInfo;
import org.exolab.castor.xml.SimpleMarshalDescriptor;
import org.exolab.castor.xml.MarshalException;
import tyrex.resource.ResourceLimits;


/**
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/02/23 21:20:21 $
 */
public class DomainMarshalInfo
    extends SimpleMarshalInfo
    implements MarshalInfo
{


    public DomainMarshalInfo()
	throws MarshalException
    {
	super( Domain.class );

	MarshalInfo         info;
	SimpleMarshalDescriptor smd;
	MarshalDescriptor[] desc;
        int                 i;

	info = MarshalHelper.generateMarshalInfo( Domain.class );

	smd = new SimpleMarshalDescriptor( Policy.class, "policy", "policy" );
	try {
	    smd.setWriteMethod( Domain.class.getMethod( "setPolicy",
							new Class[] { Policy.class } ) );
	    smd.setReadMethod( Domain.class.getMethod( "getPolicy",
						       new Class[ 0 ] ) );
	} catch ( Exception except ) {
	    // This should never happen
	    throw new RuntimeException( "Internal error: " + except.toString() );
	}
	addElementDescriptor( smd );

	smd = new SimpleMarshalDescriptor( ResourceLimits.class, "limits", "limits" );
	try {
	    smd.setWriteMethod( Domain.class.getMethod( "setLimits",
							new Class[] { ResourceLimits.class } ) );
	    smd.setReadMethod( Domain.class.getMethod( "getLimits",
						       new Class[ 0 ] ) );
	} catch ( Exception except ) {
	    // This should never happen
	    throw new RuntimeException( "Internal error: " + except.toString() );
	}
	smd.setMarshalInfo( new LimitsMarshalInfo() );
	addElementDescriptor( smd );

	smd = new SimpleMarshalDescriptor( InterceptorHolder.class, "interceptor", "interceptor" );
	try {
	    smd.setWriteMethod( Domain.class.getMethod( "addInterceptor",
							new Class[] { InterceptorHolder.class } ) );
	    smd.setReadMethod( Domain.class.getMethod( "listInterceptors",
						       new Class[ 0 ] ) );
	} catch ( Exception except ) {
	    // This should never happen
	    throw new RuntimeException( "Internal error: " + except.toString() );
	}
	addElementDescriptor( smd );

	smd = new SimpleMarshalDescriptor( Resources.class, "resources", "resources" );
	try {
	    smd.setWriteMethod( Domain.class.getMethod( "addResources",
							new Class[] { Resources.class } ) );
	    smd.setReadMethod( Domain.class.getMethod( "listResources",
						       new Class[ 0 ] ) );
	} catch ( Exception except ) {
	    // This should never happen
	    throw new RuntimeException( "Internal error: " + except.toString() );
	}
	addElementDescriptor( smd );

	smd = new SimpleMarshalDescriptor( String.class, "resourcesHref", "resources-href" );
	try {
	    smd.setWriteMethod( Domain.class.getMethod( "addResourcesHref",
							new Class[] { String.class } ) );
	    smd.setReadMethod( Domain.class.getMethod( "listResourcesHrefs",
						       new Class[ 0 ] ) );
	} catch ( Exception except ) {
	    // This should never happen
	    throw new RuntimeException( "Internal error: " + except.toString() );
	}
	addElementDescriptor( smd );

	desc = info.getAttributeDescriptors();
	for ( i = 0 ; i < desc.length ; ++i ) {
	    addAttributeDescriptor( desc[ i ] );
	}
    }


}