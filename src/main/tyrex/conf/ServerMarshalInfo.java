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
 * $Id: ServerMarshalInfo.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.conf;


import java.lang.reflect.Method;
import java.io.IOException;
import org.exolab.castor.xml.MarshalInfo;
import org.exolab.castor.xml.MarshalDescriptor;
import org.exolab.castor.xml.MarshalHelper;
import org.exolab.castor.xml.SimpleMarshalInfo;
import org.exolab.castor.xml.SimpleMarshalDescriptor;


/**
 * Marshalling information for {@link Server}.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
 */
public class ServerMarshalInfo
    extends SimpleMarshalInfo
    implements MarshalInfo
{


    public ServerMarshalInfo()
	throws IOException
    {
	super( Server.class );

	MarshalInfo         info;
	MarshalDescriptor[] desc;
        int                 i;

	info = MarshalHelper.generateMarshalInfo( Server.class );
	desc = info.getElementDescriptors();
	// We need a special descriptor for limits since we use
	// LimitsMarshalInfo and not PoolMarshalInfo.
	for ( i = 0 ; i < desc.length ; ++i ) {
	    if ( "limits".equals( desc[ i ].getXMLName() ) ) {
		addElementDescriptor( new LimitsDescriptor( desc[ i ] ) );
	    } else {
		addElementDescriptor( desc[ i ] );
	    }
	}
	desc = info.getAttributeDescriptors();
	for ( i = 0 ; i < desc.length ; ++i ) {
	    addAttributeDescriptor( desc[ i ] );
	}
    }


    static class LimitsDescriptor
	extends SimpleMarshalDescriptor
    {
	
	public LimitsDescriptor( MarshalDescriptor desc )
	{
	    super( desc.getName(), desc.getXMLName() );
	    setWriteMethod( desc.getWriteMethod() );
	    setReadMethod( desc.getReadMethod() );
	}

	public MarshalInfo getMarshalInfo()
	{
	    try {
		return new LimitsMarshalInfo();
	    } catch ( IOException except ) {
		// This should never happen
		return null;
	    }
	}
	
    }


}
