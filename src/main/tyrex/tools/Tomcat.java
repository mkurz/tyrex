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
 * $Id: Tomcat.java,v 1.1 2000/08/28 19:01:52 mohammed Exp $
 */


package tyrex.interceptor;

import java.util.Enumeration;
import java.net.URL;
import java.io.InputStream;
import java.util.Hashtable;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.NamingException;
import javax.transaction.RollbackException;
import org.apache.tomcat.core.ServiceInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.Constants;
import org.apache.tomcat.core.InterceptorException;
import org.apache.tomcat.deployment.WebApplicationDescriptor;
import org.apache.tomcat.deployment.WebApplicationReader;
import org.apache.tomcat.deployment.WebDescriptorFactoryImpl;
import org.apache.tomcat.deployment.ResourceReference;
import org.apache.tomcat.deployment.EnvironmentEntry;
import tyrex.tm.Tyrex;
import tyrex.naming.ENCHelper;


/**
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/08/28 19:01:52 $
 */
public class Tomcat
    implements ServiceInterceptor
{


    private Hashtable  _encs = new Hashtable();


    private boolean   _started;


    public Tomcat()
    {
    }


    public void preInvoke( Context context, Servlet servlet,
			   HttpServletRequest req, HttpServletResponse res )
	throws InterceptorException
    {
	ENCHelper enc;

	// Make sure the Tyrex is started at this point.
	// We don't do it in the constructor since in this
	// version of Tomcat the constructor will be called
	// both at startup and shutdown
	if ( ! _started ) {
	    Tyrex.getTransactionManager();
	    _started = true;
	}

	try {
	    enc = getENCHelper( context );
	    enc.setThreadContext();
	} catch ( NamingException except ) {
	} catch ( Exception except ) {
	    throw new InterceptorException( except );
	}
    }
    

    public void postInvoke( Context context, Servlet servlet,
			    HttpServletRequest req, HttpServletResponse res )
	throws InterceptorException
    {
	ENCHelper enc;

	try {
	    enc = getENCHelper( context );
	    enc.suspendThreadContext();
	    Tyrex.recycleThread();
	} catch ( RollbackException except ) {
	} catch ( Exception except ) {
	    throw new InterceptorException( except );
	}
    }


    protected ENCHelper getENCHelper( Context context )
    {
	ENCHelper   enc;

	enc = (ENCHelper) _encs.get( context );
	if ( enc == null ) {
	    enc = new ENCHelper();
	    _encs.put( context, enc );
	    try {
		enc.addUserTransaction( Tyrex.getUserTransaction() );
	    } catch ( Exception except ) {
	    }

	    WebApplicationDescriptor appDesc;
	    URL         url;
	    String      base;
	    InputStream is;

	    base = context.getDocumentBase().toString();
	    if ( context.getDocumentBase().getProtocol().equalsIgnoreCase(
		     Constants.Request.WAR ) ) {
		if ( base.endsWith( "/" ) ) {
		    base = base.substring( 0, base.length() - 1 );
		}
		base += "!/";
	    }
	    try {
		url = new URL( base + Constants.Context.ConfigFile );
		is = url.openConnection().getInputStream();
		appDesc = new WebApplicationReader().
		    getDescriptor( is,  new WebDescriptorFactoryImpl(),
				   context.isWARValidated() );

		Enumeration       enum;
		EnvironmentEntry  envEntry;
		ResourceReference resRef;

		enum = appDesc.getEnvironmentEntries();
		while ( enum.hasMoreElements() ) {
		    envEntry = (EnvironmentEntry) enum.nextElement();
		    try {
			enc.addEnvEntry( envEntry.getName(), envEntry.getType(), envEntry.getValue() );
		    } catch ( NamingException except ) { }
		}

		enum = appDesc.getResourceReferences();
		while ( enum.hasMoreElements() ) {
		    resRef = (ResourceReference) enum.nextElement();
		    enc.addResource( context.getDocumentBase().toString(), resRef.getName(),
				     resRef.getType(), 
				     ResourceReference.APPLICATION_AUTHORIZATION.equals( resRef.getAuthorization() ) );
		}

	    } catch ( Exception except ) {
		System.out.println( except );
		except.printStackTrace();
	    }
	    enc.addEnvEntries( context.getDocumentBase().toString() );
	    
      	}
	return enc;
    }


}
