/**
* Copyright (C) 2001, Intalio Inc.
*
* The program(s) herein may be used and/or copied only with
* the written permission of Intalio Inc. or in accordance with
* the terms and conditions stipulated in the agreement/contract
* under which the program(s) have been supplied.
*
* $Id: EnvContextTest.java,v 1.1 2001/07/31 01:06:50 mills Exp $
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
import tyrex.naming.EnvContext;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.PrintWriter;


/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.1 $
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
     * Comment.
     *
     * @result Expected results.
     */

    public void testPurpose()
        throws Exception
    {
        String               value = "testValue";
        String               path = "comp/env";
        String               name = "test";
        RuntimeContext       runCtx;
        RuntimeContext       tempCtx;
        Context              rootCtx;
        Context              ctx;
        Context              enc = null;
        InitialContext       initCtx;
            
        VerboseStream stream = new VerboseStream();

        try {
            runCtx = new RuntimeContext( "test" );
            stream.writeVerbose( "Constructing a context with comp/env/test as a test value" );
            rootCtx = runCtx.getEnvContext();
            rootCtx.createSubcontext( "comp" );
            rootCtx.createSubcontext( "comp/env" );
            rootCtx.bind( path + "/" + name, value );
            ctx = (Context) rootCtx.lookup( "" );

            stream.writeVerbose( "Test ability to read from the ENC in variety of ways" );
            try {
                if ( rootCtx.lookup( path + "/" + name ) != value ) {
                    fail( "Error: Failed to lookup name (1)" );
                }
            } catch ( NameNotFoundException except ) {
                fail( "Error: Failed to lookup name (2)" );
            }
            try {
                enc = (Context) rootCtx.lookup( "comp" );
                enc = (Context) enc.lookup( "env" );
                if ( enc.lookup( name ) != value ) {
                    fail( "Error: Failed to lookup name (3)" );
                }
            } catch ( NameNotFoundException except ) {
                fail( "Error: Failed to lookup name (4)" );
            }

            stream.writeVerbose( "Test updates on memory context reflecting in ENC" );
            ctx.unbind( path + "/" + name );
            try {
                enc.lookup( name );
                fail( "Error: NameNotFoundException not reported" );
            } catch ( NameNotFoundException except ) { }

            stream.writeVerbose( "Test the suspend/resume nature of the JNDI ENC" );
            initCtx = new InitialContext();
            ctx.bind( path + "/" + name, value );
            tempCtx = new RuntimeContext( "empty" );
            tempCtx.enter();
            try {
                initCtx.lookup( "java:" + path + "/" + name );
                fail( "Error: NotContextException not reported" );
            } catch ( NotContextException except ) {
                // This occurs if the ENC is entirely empty.
            } catch ( NameNotFoundException except ) {
                // This occurs if the ENC has some content in comp/env.
            }
            tempCtx.leave();
            try {
                initCtx.lookup( "java:" + path + "/" + name );
                fail( "Error: NotContextException not reported" );
            } catch ( NotContextException except ) {
                // This occurs if the ENC is entirely empty.
            } catch ( NameNotFoundException except ) {
                // This occurs if the ENC has some content in comp/env.
            }
            runCtx.enter();
            try {
                if ( initCtx.lookup( "java:" + path + "/" + name ) != value ) {
                    fail( "Error: Failed to lookup name (5)" );
                }
            } catch ( NamingException except ) {
                fail( "Error: NamingException reported" );
            }
            runCtx.leave();
            try {
                initCtx.lookup( "java:" + path + "/" + name );
                fail( "Error: NamingException not reported" );
            } catch ( NamingException except ) { }


            stream.writeVerbose( "Test the serialization nature of JNDI ENC" );
            runCtx.enter();

            stream.writeVerbose( "Test that the JNDI ENC is read only" );
            ctx.unbind( path + "/" + name );
            try {
                initCtx.bind( "java:" + name, value );
                fail( "Error: JNDI ENC not read-only (1)" );
            } catch ( OperationNotSupportedException except ) { }

            ctx.bind( path + "/" + name, value );
            try {
                initCtx.unbind( "java:" + name );
                fail( "Error: JNDI ENC not read-only (2)" );
            } catch ( OperationNotSupportedException except ) { }

            try {
                ObjectOutputStream    oos;
                ObjectInputStream     ois;
                ByteArrayOutputStream aos;
                    
                enc = (Context) initCtx.lookup( "java:" + path );
                aos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream( aos );
                oos.writeObject( enc );
                ois = new ObjectInputStream( new ByteArrayInputStream( aos.toByteArray() ) );
                enc = (Context) ois.readObject();
            } catch ( Exception except ) {
                fail( "Error: Failed to (de)serialize: " + except );
            }
            runCtx.leave();
            try {
                enc.lookup( name );
                fail( "Error: Managed to lookup name but java:comp not bound to thread" );
            } catch ( NamingException except ) { }{}
            runCtx.enter();
            try {
                if ( enc.lookup( name ) != value ) {
                    fail( "Error: Failed to lookup name (6)" );
                }
            } catch ( NameNotFoundException except ) {
                fail( "Error: NameNotFoundException reported" );
            }
            runCtx.leave();
        } catch ( NamingException except ) {
            System.out.println( except );
            except.printStackTrace();
        }
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
                VerboseStream.verbose = true;
        junit.textui.TestRunner.run(new TestSuite(EnvContextTest.class));
    }
}
