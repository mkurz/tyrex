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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.util.Properties;
import java.util.Enumeration;

import javax.mail.Session;

import tyrex.tm.TransactionDomain;

import tyrex.resource.ResourceConfig;
import tyrex.resource.Resource;
import tyrex.resource.PoolLimits;
import tyrex.resource.PoolMetrics;
import tyrex.resource.ResourceException;


/**
 * Extends ResourceConfig to load the JavaMail Resource
 * configuration.
 *
 * See the Castor Mapping file (mapping.xml) found in 
 * package tyrex.tm.impl.
 *
 * An example configuration is given below:	
 *
 *       <javamail>
 *		<name>MyMail</name>
 *		<property>
 *			<key>mail.smtp.host</key>
 *			<value>mail.exolab.org</value>
 *		</property>
 *       </javamail>
 * 
 *
 * @author <a href="ashish@intalio.com">Ashish Agrawal</a>
 * @version $Revision: 1.1 $
 */
public class JavaMailConfig extends ResourceConfig
{

    public Properties _properties = new Properties();


    /**
     * Called by Castor and defined in the mapping file to 
     * add each specific property associated with the Session.
     * See the JavaMail documentation for the specific keys
     * and their values.
     *
     */
    public void addProperty(Property aProperty) {

        _properties.setProperty(aProperty.getKey(), aProperty.getValue());
    }


    public Enumeration getProperties() {

	return _properties.propertyNames();

    }



    /**
     * Called to create a new factory object for the purpose of
     * configuring it. This method will return a factory object that
     * will be configured from the resource configuration file, before
     * being added to this object with a subsequent call to {@link
     * #setFactory setFactory}.
     *
     * @return The factory object (never null)
     * @throws ResourceException An error occured while attempting
     * to create a new factory
     */
    public Object createFactory()
        throws ResourceException {

          _factory = Session.getDefaultInstance(_properties);
          return _factory;

    }


    /**
     * Called to create a new resource from this resource configuration.
     *
     * @param txDomain The transaction domain in which the resource will
     * be used
     * @return The resource
     * @throws ResourceException An error occured while attempting to
     * create the resource
     */
    public Resource createResource( TransactionDomain txDomain )
        throws ResourceException {

        return new JavaMailResource(_factory,_properties);

    }



}
