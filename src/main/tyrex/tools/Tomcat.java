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
 * Copyright 2000 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: Tomcat.java,v 1.5 2000/09/25 06:41:56 mohammed Exp $
 */


package tyrex.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.NamingException;
import javax.transaction.RollbackException;
import org.apache.tomcat.core.ServiceInterceptor;
import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.RequestInterceptor;
import org.apache.tomcat.core.Context;
//import org.apache.tomcat.core.Constants;
import org.apache.tomcat.shell.Constants;
import org.apache.tomcat.core.InterceptorException;
import org.apache.tomcat.deployment.WebApplicationDescriptor;
import org.apache.tomcat.deployment.WebApplicationReader;
import org.apache.tomcat.deployment.WebDescriptorFactoryImpl;
import org.apache.tomcat.deployment.ResourceReference;
import org.apache.tomcat.deployment.EnvironmentEntry;
import tyrex.tm.Tyrex;
import tyrex.naming.MemoryContext;
import tyrex.naming.EnvContext;
//import tyrex.naming.ENCHelper;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.util.URLUtil;


/**
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $ $Date: 2000/09/25 06:41:56 $
 */
public final class Tomcat
    extends BaseInterceptor
    implements RequestInterceptor, ServiceInterceptor
{
    private Hashtable  _memoryContexts = new Hashtable();


    private boolean   _started;


    public Tomcat()
    {
    }

    public int preService(Request req, Response resp ) {
        try {
        preInvoke( req.getContext(), null, /*req.getWrapper().getServlet(),*/
				    null, null /*req.getFacade(),  resp.getFacade()*/);
	    return 0;
	} catch( InterceptorException ex ) {
	    return -1; // map exceptions to error codes
	}
    }

    // Warning servlet, req and res may be null
    public void preInvoke( Context context, Servlet servlet,
			   HttpServletRequest req, HttpServletResponse res )
	throws InterceptorException
    {
    
	MemoryContext memoryContext;

	// Make sure the Tyrex is started at this point.
	// We don't do it in the constructor since in this
	// version of Tomcat the constructor will be called
	// both at startup and shutdown
	if ( ! _started ) {
	    Tyrex.getTransactionManager();
	    _started = true;
	}

	try {
	    memoryContext = getMemoryContext( context );
	    EnvContext.setEnvContext(memoryContext);
	} catch ( NamingException except ) {
	} catch ( Exception except ) {
	    throw new InterceptorException( except );
	}
    }
    
    public int postService(Request req, Response resp ) {
	try {
	    postInvoke( req.getContext(), null, /*req.getWrapper().getServlet(),*/
				    null, null /*req.getFacade(),  resp.getFacade()*/);
	    return 0;
	} catch( InterceptorException ex ) {
	    return -1; // map exceptions to error codes
	}
    }

    // Warning servlet, req and res maybe null
    public void postInvoke( Context context, Servlet servlet,
			    HttpServletRequest req, HttpServletResponse res )
	throws InterceptorException
    {
    try {
	    EnvContext.unsetEnvContext();
	    Tyrex.recycleThread();
	} catch ( RollbackException except ) {
	} catch ( Exception except ) {
	    throw new InterceptorException( except );
	}
    }

    protected MemoryContext getMemoryContext( Context context )
        throws NamingException
    {
    MemoryContext   memoryContext;

	memoryContext = (MemoryContext) _memoryContexts.get( context );
	if ( memoryContext == null ) {
        javax.naming.Context ctx;
        String appName;

	    memoryContext = TomcatContextHelper.createMemoryContext();
        _memoryContexts.put( context, memoryContext );

        try {
		TomcatContextHelper.addUserTransaction( memoryContext, Tyrex.getUserTransaction() );
	    } catch ( Exception except ) {
	    }
        appName = context.getDocBase();
        loadWebApplication(memoryContext, 
                           getWebApplicationConfigPath(context.getContextManager().getTomcatHome(), appName), 
                           appName);
        TomcatContextHelper.addEnvEntries( memoryContext, appName );
        //TomcatContextHelper.addResources( memoryContext, appName );
    } 
	return memoryContext;
    }


    /**
     * Return the path of the web application configuration file
     * path.
     *
     * @param tomcatHome the path of the home of tomcat
     * @param appName the name of the application
     * @return the path of the web application configuration file
     *      path.
     */
    private String  getWebApplicationConfigPath(String tomcatHome, String appName)
    {
        return new File(new File(tomcatHome, appName), "WEB-INF/web.xml").getPath();
    }

    /**
     * Load the entries from the web.xml into the specified memory
     * context.
     *
     * @param memoryContext the memory context
     * @param path the path of the web.xml file
     * @param appName the name of the application
     */
    private void loadWebApplication(MemoryContext memoryContext, String path, String appName)
    {
        WebApplicationDescriptor appDesc;
	    FileInputStream fis = null;

	    try {
            Enumeration       enum;
    		EnvironmentEntry  envEntry;
    		ResourceReference resRef;
    
            fis = new FileInputStream(path);
    		appDesc = new WebApplicationReader().getDescriptor( fis,  
                                                                new WebDescriptorFactoryImpl(),
                                                                false ); // perform no validation
    
    		enum = appDesc.getEnvironmentEntries();
            while ( enum.hasMoreElements() ) {
    		    envEntry = (EnvironmentEntry) enum.nextElement();
    		    try {
                TomcatContextHelper.addEnvEntry( memoryContext, envEntry.getName(), envEntry.getType(), envEntry.getValue() );
    		    } catch ( NamingException except ) { }
    		}
    
    		enum = appDesc.getResourceReferences();
            while ( enum.hasMoreElements() ) {
    		    resRef = (ResourceReference) enum.nextElement();
                TomcatContextHelper.addResource( memoryContext,
                                                 appName, 
                                                 resRef.getName(),
                                                 resRef.getType(), 
                                                 ResourceReference.APPLICATION_AUTHORIZATION.equals( resRef.getAuthorization() ) );
		}

	    } catch ( Exception except ) {
    		System.out.println( except );
    		except.printStackTrace();
	    } finally {
            if (null != fis) {
                try {    
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
