/**
* Copyright (C) 2001, Intalio Inc.
*
* The program(s) herein may be used and/or copied only with
* the written permission of Intalio Inc. or in accordance with
* the terms and conditions stipulated in the agreement/contract
* under which the program(s) have been supplied.
*
* $Id: ContextTest.java,v 1.1 2001/07/31 01:06:50 mills Exp $
* Date        Author    Changes
*
* 2001/07/26  Mills     Created
*
*/


package tyrex.naming;

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import tyrex.runtime.RuntimeContext;
import tyrex.naming.MemoryBinding;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.PrintWriter;


/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.1 $
 */

public abstract class ContextTest extends TestCase
{
    private PrintWriter _logger = null;

    public ContextTest(String name)
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

    public abstract Context newContext()
        throws Exception;

    public abstract InitialContext getInitialContext(String url)
        throws NamingException;

    public abstract String getNamespace();

    public abstract int getInitialEnvSize();


    /**
     * Test the methods relating to environments.
     *
     * @result Add 2 values.  Ensure that the values are now contained
     * in the environment.  Remove one of the values.  Ensure that the
     * environment now only contains the other.
     */

    public void testEnvironment()
        throws Exception
    {
        Context ctx = newContext();
        Integer i1 = new Integer(1);
        ctx.addToEnvironment("name1", i1);
        Integer i2 = new Integer(2);
        ctx.addToEnvironment("name2", i2);
        Hashtable env = ctx.getEnvironment();
        assertEquals("Size", 2 + getInitialEnvSize(), env.size());
        assertEquals("Val", i1, env.get("name1"));
        assertEquals("Val", i2, env.get("name2"));
        ctx.removeFromEnvironment("name1");
        env = ctx.getEnvironment();
        assertEquals("Size", 1 + getInitialEnvSize(), env.size());
        assertNull("Null val", env.get("name1"));
        assertEquals("Val", i2, env.get("name2"));
        ctx.close();
    }


    /**
     * Create a sub-context and ensure that the 2 contexts are different.
     *
     * @result Add a name/value pair to the main context.  Ensure that
     * it can be retrieved.  Create a sub-context.  Add a couple of
     * values and ensure that they can be retrieved.  Ensure that they
     * cannot be retrieved from the main context nor that the main
     * contexts value can be retrieved from the sub context.
     *
     * <p>Except where the main context is an InitialContext ensure
     * that the sub-context can be destroyed.  Ensure that the
     * destruction of the sub-context does not prevent the value kept
     * in the main context from being accessed.</p>
     */

    public void testSubContexts()
        throws Exception
    {
        try
        {
            String subCtxName = "sub";
            Context ctx = newContext();
            Integer i4 = new Integer(4);
            ctx.bind("name4", i4);
            Context subCtx = ctx.createSubcontext(subCtxName);
            Integer i1 = new Integer(1);
            subCtx.bind("name1", i1);
            Integer i2 = new Integer(2);
            subCtx.bind("name2", i2);
            assertEquals("Val", i1, subCtx.lookup("name1"));
            assertEquals("Val", i2, subCtx.lookup("name2"));
            assertEquals("Val", i4, ctx.lookup("name4"));
            try
            {
                subCtx.lookup("name4");
                fail("Sub-context can access contexts values.");
            }
            catch (NameNotFoundException e)
            {
                // Expected.
            }
            try
            {
                ctx.lookup("name1");
                fail("Context can access sub-contexts values.");
            }
            catch (NameNotFoundException e)
            {
                // Expected.
            }
            try
            {
                ctx.lookup("name2");
                fail("Context can access sub-contexts values.");
            }
            catch (NameNotFoundException e)
            {
                // Expected.
            }
            Integer i3 = new Integer(3);
            subCtx.rebind("name2", i3);
            assertEquals("Val", i3, subCtx.lookup("name2"));
            subCtx.rename("name2", "name3");
            try
            {
                subCtx.lookup("name2");
                fail("Can still access old name.");
            }
            catch (NameNotFoundException e)
            {
                // Expected.
            }
            assertEquals("Val", i3, subCtx.lookup("name3"));
            subCtx.unbind("name3");
            try
            {
                subCtx.lookup("name3");
                fail("Can still access unbound value.");
            }
            catch (NameNotFoundException e)
            {
                // Expected.
            }
            Context subCtx1 = (Context)ctx.lookup(subCtxName);
            assertEquals("Val", i1, subCtx.lookup("name1"));
            subCtx.unbind("name1");
            if (!(ctx instanceof InitialContext))
            {
                ctx.destroySubcontext("sub");
            }
            assertEquals("Val", i4, ctx.lookup("name4"));
        }
        catch (OperationNotSupportedException e)
        {
            // Expected in some cases.
        }
    }


    /**
     * Test name, name parser and namespace related methods.
     *
     * @result Ensure that getNameInNamespace() returns that correct
     * namespace name.  Use getNameParser() to return a name parser
     * and then use this name parser to parse a URL (the URL need not
     * exist on the host since it is never access simply parsed).
     * Ensure that the Name returned is correct.
     *
     * <p>Use composeName() and ensure that the name composed is
     * correct.</p>
     */

    public void testNamesAndNamespaces()
        throws Exception
    {
        Context ctx = newContext();
        assertEquals("Namespace", getNamespace(), ctx.getNameInNamespace());
        NameParser np = ctx.getNameParser("name1");
        Name name = np.parse("file:/usr/local/src/java/jdk1.3/docs/api/java/util/Hashtable.html#get(java.lang.Object)");
        assertEquals("Name size", 11, name.size());
        assertEquals("Name part", "file:", name.get(0));
        assertEquals("Name part", "usr", name.get(1));
        assertEquals("Name part", "local", name.get(2));
        assertEquals("Name part", "src", name.get(3));
        assertEquals("Name part", "java", name.get(4));
        assertEquals("Name part", "jdk1.3", name.get(5));
        assertEquals("Name part", "docs", name.get(6));
        assertEquals("Name part", "api", name.get(7));
        assertEquals("Name part", "java", name.get(8));
        assertEquals("Name part", "util", name.get(9));
        assertEquals("Name part", "Hashtable.html#get(java.lang.Object)",
                     name.get(10));
        if (ctx instanceof InitialContext)
        {
            assertEquals("Composed name", "name3",
                         ctx.composeName("name3", "pref1"));
        }
        else
        {
            assertEquals("Composed name", "pref1/name3",
                         ctx.composeName("name3", "pref1"));
        }
    }


    /**
     * Test bindings and renaming.
     *
     * @result Add a couple of name/value pairs to the context.
     * Ensure that they can be retrieved.  Rebind a name and ensure
     * that the new value is retrieved and not the old.  Rename the
     * value and ensure that it is retrieved by the new name and not
     * the old.
     */

    public void testBindings()
        throws Exception
    {
        Context ctx = newContext();
        try
        {
            Integer i1 = new Integer(1);
            ctx.bind("name1", i1);
            Integer i2 = new Integer(2);
            ctx.bind("name2", i2);
            assertEquals("Val", i1, ctx.lookup("name1"));
            assertEquals("Val", i2, ctx.lookup("name2"));
            Integer i3 = new Integer(3);
            ctx.rebind("name2", i3);
            assertEquals("Val", i3, ctx.lookup("name2"));
            ctx.rename("name2", "name3");
            try
            {
                ctx.lookup("name2");
                fail("Can still access old name.");
            }
            catch (NameNotFoundException e)
            {
                // Expected.
            }
            assertEquals("Val", i3, ctx.lookup("name3"));
            ctx.unbind("name3");
            try
            {
                ctx.lookup("name3");
                fail("Can still access unbound value.");
            }
            catch (NameNotFoundException e)
            {
                // Expected.
            }
        }
        catch (OperationNotSupportedException e)
        {
            // Expected in some cases.
        }
        ctx.close();
    }


    /** Adds a message in the log (except if the log is null)*/
    private void logMessage(String message)
    {
        if (_logger != null)
        {
            _logger.println(message);
        }
    }
}
