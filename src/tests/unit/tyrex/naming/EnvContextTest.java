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
 * $Id: EnvContextTest.java,v 1.6 2001/08/23 09:47:27 mills Exp $
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
 * @version $Revision: 1.6 $
 */

public class EnvContextTest extends TestCase
{
    private PrintWriter _logger = null;

    public EnvContextTest(String name)
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


    /**
     * Tests to improve test coverage of the class.
     *
     * @result Create an instance of MemoryBinding.  Add several
     * objects to the binding with different names (i.e. an Integer, a
     * LinkRef, another MemoryBinding and a Reference.  All these
     * should be retrievable using lookup.  Create an EnvContext
     * instance with a null argument, with the binding as argument and
     * with the binding and a HashTable as argument.  The context
     * should return an enumerated list of the bindings set earlier
     * when list() is called with an empty String argument.
     *
     * <p>Calling listBindings() should return the same enumerated
     * list.  Calling listBindings(), createSubcontext() or
     * destroySubcontext() should throw a NotContextException,
     * OperationNotSupportedException and
     * OperationNotSupportedException respectively.</p>
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
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                tyrex.naming.MemoryContextFactory.class.getName());
        env.put(Context.URL_PKG_PREFIXES, "tyrex.naming");
        env.put("key", "value");
        EnvContext context = new EnvContext(null);
        context = new EnvContext(binding, null);
        context = new EnvContext(binding, env);
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
        try
        {
            context.createSubcontext("subcontext");
        }
        catch (OperationNotSupportedException e)
        {
            // Expected.
        }
        try
        {
            context.destroySubcontext("subcontext");
        }
        catch (OperationNotSupportedException e)
        {
            // Expected.
        }
        assertEquals("", context.toString());
    }


    /**
     * Repeat the coverage tests using Name arguments rather than
     * String where possible.
     *
     * @result The results should be the same as for testCoverage().
     */

    public void testCoverageUsingName()
        throws Exception
    {
        MemoryBinding binding = new MemoryBinding();
        Integer i1 = new Integer(1);
        binding.put("binding", i1);
        CompositeName linkName = new CompositeName("link");
        binding.put("linkRef", new LinkRef(linkName));
        binding.put("memBind", new MemoryBinding());
        Reference ref = new Reference("MemoryBinding");
        binding.put("ref", ref);
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                tyrex.naming.MemoryContextFactory.class.getName());
        env.put(Context.URL_PKG_PREFIXES, "tyrex.naming");
        env.put("key", "value");
        EnvContext context = new EnvContext(null);
        context = new EnvContext(binding, null);
        context = new EnvContext(binding, env);
        CompositeName bindingName = new CompositeName("binding");
        assertEquals(i1, context.lookup(bindingName));
//        context.lookup("linkRef");
        CompositeName memBindName = new CompositeName("memBind");
        assertEquals("memBind", context.lookup(memBindName).toString());
        CompositeName refName = new CompositeName("ref");
        assertEquals(ref, context.lookup(refName));
        assertEquals(i1, context.lookupLink(bindingName));
        CompositeName emptyName = new CompositeName("");
        Enumeration enum = context.list(emptyName);
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
            CompositeName keyName = new CompositeName("key");
            context.list(keyName);
        }
        catch (NotContextException e)
        {
            // Expected.
        }
        enum = context.listBindings(emptyName);
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
            context.listBindings(bindingName);
        }
        catch (NotContextException e)
        {
            // Expected.
        }
        CompositeName subContName = new CompositeName("subcontext");
        try
        {
            context.createSubcontext(subContName);
        }
        catch (OperationNotSupportedException e)
        {
            // Expected.
        }
        try
        {
            context.destroySubcontext(subContName);
        }
        catch (OperationNotSupportedException e)
        {
            // Expected.
        }
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
        TestSuite suite = new TestSuite(EnvContextTest.class);
        suite.addTest(new TestSuite(EnvContext_ContextImpl.class));
        return suite;
    }


    // Allow this test to be run on its own.
    public static void main(String args[])
    {
        for( int i = 0 ; i < args.length ; i++ )
            if ( args[ i ].equals( "-verbose" ) )
                tests.VerboseStream.verbose = true;
        junit.textui.TestRunner.run(new TestSuite(EnvContextTest.class));
    }
}
