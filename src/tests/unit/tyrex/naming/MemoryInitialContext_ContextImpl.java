/**
* Copyright (C) 2001, Intalio Inc.
*
* The program(s) herein may be used and/or copied only with
* the written permission of Intalio Inc. or in accordance with
* the terms and conditions stipulated in the agreement/contract
* under which the program(s) have been supplied.
*
* $Id: MemoryInitialContext_ContextImpl.java,v 1.1 2001/07/31 01:06:50 mills Exp $
* Date        Author    Changes
*
* 2001/07/26  Mills     Created
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
 * @version $Revision: 1.1 $
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
