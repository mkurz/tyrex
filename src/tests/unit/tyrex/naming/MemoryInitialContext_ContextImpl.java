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
 * $Id: MemoryInitialContext_ContextImpl.java,v 1.2 2001/08/10 07:18:01 mills Exp $
 */

package tyrex.naming;

import tyrex.naming.MemoryContext;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

import java.util.Hashtable;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.File;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.2 $
 */

public class MemoryInitialContext_ContextImpl extends ContextTest
{
    String namespace_ = "root/test";
    int initialEnvSize_ = 0;

    public MemoryInitialContext_ContextImpl(String name)
    {
        super(name);
    }

    public Context newContext()
        throws Exception
    {
        return getInitialContext(namespace_);
    }

    public String getNamespace()
    {
        return namespace_;
    }

    public int getInitialEnvSize()
    {
        return initialEnvSize_;
    }

    public InitialContext getInitialContext( String url )
        throws NamingException
    {
        Hashtable env = new Hashtable();

        // Ensures that MemoryContext is used.
        env.put( Context.INITIAL_CONTEXT_FACTORY, tyrex.naming.MemoryContextFactory.class.getName() );
        initialEnvSize_++;
        env.put( Context.URL_PKG_PREFIXES, "tyrex.naming" );
        initialEnvSize_++;
        if ( url != null )
        {
            env.put( Context.PROVIDER_URL, url );
            initialEnvSize_++;
        }
        return new InitialContext(env);
    }
}
