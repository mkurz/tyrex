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
 * $Id: MemoryContextTest.java,v 1.4 2001/08/10 11:39:10 mills Exp $
 */


package tyrex.naming;

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import tyrex.tm.RuntimeContext;
import tyrex.naming.EnvContext;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.PrintWriter;


/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.4 $
 */

public class MemoryContextTest extends TestCase
{
    private PrintWriter _logger = null;

    public MemoryContextTest(String name)
    {
        super(name);
    }

    public void setUp()
    {
        _logger= new PrintWriter(System.out);
    }

    public void tearDown()
    {
        _logger.flush();
    }


    public static InitialContext getInitialContext( String url )
        throws NamingException
    {
        Hashtable env;

        env = new Hashtable();

        // Ensures that MemoryContext is used.
        env.put( Context.INITIAL_CONTEXT_FACTORY, tyrex.naming.MemoryContextFactory.class.getName() );
        env.put( Context.URL_PKG_PREFIXES, "tyrex.naming" );
        if ( url != null )
            env.put( Context.PROVIDER_URL, url );
        return new InitialContext( env );
    }


    /**
     * Base test as taken from tyrex/src/tests/naming/Naming.java.
     * Test that 2 views of the same context retrieve the same bound
     * values.  Test that a shared and a non-shared context do not
     * retrieve the same bound values.  Test that 2 non-shared
     * contexts are different.
     */

    public void testSharedAndNonShared()
        throws Exception
    {
        String               value = "Just A Test";
        String               name = "test";
        String               sub = "sub";
        InitialContext       initCtx;
        Context              ctx1;
        Context              ctx2;
            
        tests.VerboseStream stream = new tests.VerboseStream();

        try {
            // Construct the same context from two perspsective
            // and compare bound values
            stream.writeVerbose( "Constructing same context in two different ways and comparing bound values" );
            initCtx = getInitialContext( "root/" + name );
            ctx1 = initCtx;
            initCtx = getInitialContext( "root" );
            ctx2 = (Context) initCtx.lookup( name );
            ctx1.bind( name, value );
            if ( ctx2.lookup( name ) != value ) {
                fail( "Error: Same testValue not bound in both contexts (1)" );
            }
            ctx2 = ctx2.createSubcontext( sub );
            ctx1 = (Context) ctx1.lookup( sub );
            ctx1.bind( sub + name, value );
            if ( ctx2.lookup( sub + name ) != value ) {
                fail( "Error: Same testValue not bound in both contexts (2)" );
            }

            // Test that shared and non-shared spaces not the same.
            stream.writeVerbose( "Testing that shared and non-shared namespaces are different" );
            ctx2 = getInitialContext( null );
            try {
                ctx2 = (Context) ctx2.lookup( name );
                if ( ctx2.lookup( name ) == value ) {
                    fail( "Error: Same testValue bound to not-shared contexts (1)" );
                }
                fail( "Error: NameNotFoundException not reported" );
            } catch ( NameNotFoundException except ) {
                ctx2.bind( name, value );
            }
            // Test that two non-shared spaces not the same.
            stream.writeVerbose( "Testing that two non-shared namespaces are different" );
            ctx2 = getInitialContext( null );
            try {
                ctx2 = (Context) ctx2.lookup( name );
                if ( ctx2.lookup( name ) == value ) {
                    fail( "Error: Same testValue bound to not-shared contexts (2)" );
                }
                fail( "Error: NameNotFoundException not reported" );
            } catch ( NameNotFoundException except ) {
                ctx2.bind( name, value );
            }
            
        } catch ( NamingException except ) {
            System.out.println( except );
            except.printStackTrace();
        }
    }


    /**
     * 
     *
     * @result 
     */

    public void testCoverage()
        throws Exception
    {
        MemoryBinding binding = new MemoryBinding();
        Integer i1 = new Integer(1);
        binding.put("binding", i1);
        binding.put("linkRef", new LinkRef("link"));
        binding.put("memBind", new MemoryBinding());
        Reference ref = new Reference("MemoryBinding");
        binding.put("ref", ref);
        Hashtable env = new Hashtable();

        // Ensures that EnvContext is used.
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                tyrex.naming.MemoryContextFactory.class.getName());
        env.put(Context.URL_PKG_PREFIXES, "tyrex.naming");
        env.put(Context.PROVIDER_URL, "root/name");
        MemoryContext context = new MemoryContext(null);
        context = new MemoryContext(binding, null);
        context = new MemoryContext(binding, env);
        assertEquals(i1, context.lookup("binding"));
//        context.lookup("linkRef");
        assertEquals("memBind", context.lookup("memBind").toString());
        assertEquals(ref, context.lookup("ref"));
        assertEquals(i1, context.lookupLink("binding"));
        Enumeration enum = context.list("");
        assert(enum.hasMoreElements());
        NameClassPair pair = (NameClassPair)enum.nextElement();
        assertEquals("memBind", pair.getName());
        assertEquals(context.getClass().getName(), pair.getClassName());
        assert(enum.hasMoreElements());
        pair = (NameClassPair)enum.nextElement();
        assertEquals("ref", pair.getName());
        assertEquals("MemoryBinding", pair.getClassName());
        assert(enum.hasMoreElements());
        pair = (NameClassPair)enum.nextElement();
        assertEquals("binding", pair.getName());
        assertEquals(i1.getClass().getName(), pair.getClassName());
        try
        {
            context.list("key");
        }
        catch (NotContextException e)
        {
            // Expected.
        }
        enum = context.listBindings("");
        pair = (NameClassPair)enum.nextElement();
        assertEquals("memBind", pair.getName());
        assertEquals(context.getClass().getName(), pair.getClassName());
        assert(enum.hasMoreElements());
        pair = (NameClassPair)enum.nextElement();
        assertEquals("ref", pair.getName());
        assertEquals("MemoryBinding", pair.getClassName());
        assert(enum.hasMoreElements());
        pair = (NameClassPair)enum.nextElement();
        assertEquals("binding", pair.getName());
        assertEquals(i1.getClass().getName(), pair.getClassName());
        try
        {
            context.listBindings("binding");
        }
        catch (NotContextException e)
        {
            // Expected.
        }
        context.createSubcontext("subcontext");
        context.destroySubcontext("subcontext");
        assertEquals("", context.toString());
    }


    /** Adds a message in the log (except if the log is null)*/
    private void logMessage(String message)
    {
        if (_logger != null)
        {
            _logger.println(message);
        }
    }


    // Compile the test suite.
    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite(MemoryContextTest.class);
        suite.addTest(new TestSuite(MemoryContext_ContextImpl.class));
        suite.addTest(new TestSuite(MemoryInitialContext_ContextImpl.class));
        return suite;
    }


    // Allow this test to be run on its own.
    public static void main(String args[])
    {
        for( int i = 0 ; i < args.length ; i++ )
            if ( args[ i ].equals( "-verbose" ) )
                tests.VerboseStream.verbose = true;
        junit.textui.TestRunner.run(MemoryContextTest.suite());
    }
}
