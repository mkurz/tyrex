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
 * $Id: Domain.java,v 1.4 2001/02/27 00:34:05 arkin Exp $
 */


package tyrex.conf;


import java.io.Serializable;
import java.util.Vector;
import java.util.Enumeration;
import tyrex.tm.TransactionDomain;
import tyrex.resource.ResourceLimits;


/**
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $ $Date: 2001/02/27 00:34:05 $
 */
public class Domain
    implements Serializable
{


    private String         _name;


    private Policy         _policy;


    private ResourceLimits _limits;


    private Vector         _interceptors = new Vector();


    private Vector         _resources = new Vector();


    private Vector         _resourcesHref = new Vector();


    public String getName()
    {
	if ( _name == null || _name.length() == 0 )
	    _name = TransactionDomain.DEFAULT_DOMAIN;
	return _name;
    }


    public void setName( String name )
    {
	_name = name.trim();
    }


    public Policy getPolicy()
    {
	if ( _policy == null )
	    _policy = new Policy();
	return _policy;
    }


    public void setPolicy( Policy policy )
    {
	_policy = policy;
    }


    public ResourceLimits getLimits()
    {
	if ( _limits == null )
	    _limits = new ResourceLimits();
	return _limits;
    }
    

    public void setLimits( ResourceLimits limits )
    {
	_limits = limits;
    }
    

    public Enumeration listInterceptors()
    {
	return _interceptors.elements();
    }


    public void addInterceptor( InterceptorHolder interceptor )
    {
	_interceptors.addElement( interceptor );
    }


    public Enumeration listResources()
    {
	return _resources.elements();
    }


    public void addResources( Resources resources )
    {
	_resources.addElement( resources );
    }


    public Enumeration listResourcesHrefs()
    {
	return _resourcesHref.elements();
    }


    public void addResourcesHref( String href )
    {
	_resourcesHref.addElement( href );
    }


}
