/**
* Copyright (C) 2001, Intalio Inc.
*
* The program(s) herein may be used and/or copied only with
* the written permission of Intalio Inc. or in accordance with
* the terms and conditions stipulated in the agreement/contract
* under which the program(s) have been supplied.
*
* $Id: MemoryContextTest.java,v 1.2 2001/07/31 02:08:03 mills Exp $
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
import tyrex.tm.RuntimeContext;
import tyrex.naming.EnvContext;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.PrintWriter;


/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.2 $
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
            
        VerboseStream stream = new VerboseStream();

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

    public void testOther()
        throws Exception
    {
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
                VerboseStream.verbose = true;
        junit.textui.TestRunner.run(MemoryContextTest.suite());
    }
}
