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
 */


package tyrex.resource.javamail;

import java.util.Properties;
import javax.mail.Session;
import javax.transaction.xa.XAResource;

import tyrex.resource.Resource;
import tyrex.resource.PoolLimits;
import tyrex.resource.PoolMetrics;
import tyrex.resource.ResourceException;


/**
 * Java Mail resource allows creation of a Session from the Resource
 * Configuration and accessed using the name provided.
 *
 *
 * @author <a href="ashish@intalio.com">Ashish Agrawal</a>
 * @version $Revision: 1.1 $
 */
public class JavaMailResource implements Resource
{

    private Object _factory;
    private Properties _properties;
    private Object _session;


    public JavaMailResource(Object factory, Properties properties) {

      _factory = factory;
      _properties = properties;

    }


    public PoolMetrics getPoolMetrics() {
		return null;
    }


    /**
     * Instantiates an instance of the mail Session based on the resource
     * configuration provided. 
     *
     */
    public Object getClientFactory() {
        _session = ((Session) _factory).getInstance(_properties);
        return _session;
    }


    public Class getClientFactoryClass() {

      return _session.getClass();

    }

    public XAResource getXAResource() {
        return null;
    }


    public PoolLimits getPoolLimits() {
        return null;
    }


    public void destroy() {

    }


}
