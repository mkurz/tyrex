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
 * $Id: Naming.java,v 1.6 2001/03/19 17:39:03 arkin Exp $
 */


package naming;

import tests.*;

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import junit.framework.*;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import tyrex.tm.RuntimeContext;


/**
 * Naming test suite
 */
public class Naming extends TestSuite
{
    public Naming( String name )
    {
        super( name );
        
        TestCase tc;
        
        tc = new MemoryContextTest();
        addTest( tc );
        tc = new EnvContextTest();
        addTest( tc );
        tc = new ReferenceableTest();
        addTest( tc );
    }


    public static InitialContext getInitialContext( String url )
        throws NamingException
    {
        Hashtable env;

        env = new Hashtable();
        env.put( Context.INITIAL_CONTEXT_FACTORY, tyrex.naming.MemoryContextFactory.class.getName() );
        env.put( Context.URL_PKG_PREFIXES, "tyrex.naming" );
        if ( url != null )
            env.put( Context.PROVIDER_URL, url );
        return new InitialContext( env );
    }


    /**
     * Tests the in-memory service provider (MemoryContext).
     */
    public static class MemoryContextTest
     extends TestCase
    {

        public MemoryContextTest()
        {
            super( "[TC01] In-Memory Service Provider" );
        }

        /**
         * Main test method - TC01
         *  - in-memory service provider test
         *
         */
        public void runTest()
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
                initCtx = Naming.getInitialContext( name );
                ctx1 = initCtx;
                initCtx = Naming.getInitialContext( "" );
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
                ctx2 = Naming.getInitialContext( null );
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
                ctx2 = Naming.getInitialContext( null );
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
            }
        }


    }


    /**
     * Tests the environment naming context (EnvContext).
     */
    public static class EnvContextTest
        extends TestCase
    {

        public EnvContextTest()
        {
            super( "[TC02] Environment Naming Context" );
        }
        
        
        /**
         * Main test method - TC02
         *  - environment naming context test
         *
         */
        public void runTest()
        {
            String               value = "testValue";
            String               path = "comp/env";
            String               name = "test";
            RuntimeContext       runCtx;
            Context              rootCtx;
            Context              ctx;
            Context              enc = null;
            InitialContext       initCtx;
            
            VerboseStream stream = new VerboseStream();

            try {
                runCtx = RuntimeContext.newRuntimeContext();
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

                stream.writeVerbose( "Test the stack nature of the JNDI ENC" );
                initCtx = new InitialContext();
                ctx.bind( path + "/" + name, value );
                RuntimeContext.setRuntimeContext( runCtx );
                RuntimeContext.setRuntimeContext( RuntimeContext.newRuntimeContext() );
                try {
                    initCtx.lookup( "java:" + path + "/" + name );
                    fail( "Error: NotContextException not reported" );
                } catch ( NotContextException except ) { }
                RuntimeContext.unsetRuntimeContext();
                try {
                    if ( initCtx.lookup( "java:" + path + "/" + name ) != value ) {
                        fail( "Error: Failed to lookup name (5)" );
                    }
                } catch ( NamingException except ) {
                    fail( "Error: NamingException reported" );
                }
                RuntimeContext.unsetRuntimeContext();
                try {
                    initCtx.lookup( "java:" + path + "/" + name );
                    fail( "Error: NamingException not reported" );
                } catch ( NamingException except ) { }


                stream.writeVerbose( "Test the serialization nature of JNDI ENC" );
                RuntimeContext.setRuntimeContext( runCtx );

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
                    System.out.println( except );
                }
                RuntimeContext.unsetRuntimeContext();
                try {
                    enc.lookup( name );
                    stream.writeVerbose( "Error: Managed to lookup name but java:comp not bound to thread" );
                } catch ( NamingException except ) { }{}
                RuntimeContext.setRuntimeContext( runCtx );
                try {
                    if ( enc.lookup( name ) != value ) {
                        fail( "Error: Failed to lookup name (6)" );
                    }
                } catch ( NameNotFoundException except ) {
                    fail( "Error: NameNotFoundException reported" );
                }
             } catch ( NamingException except ) {
                System.out.println( except );
                except.printStackTrace();
             }
        }


    }


    /**
     * Test the handling of referencable objects.
     */
    public static class ReferenceableTest
        extends TestCase
    {

        public ReferenceableTest()
        {
            super( "[TC03] Referenceable Object Handling" );
        }
        
        /**
         * Main test method - TC03
         *  - handling of referencable objects test
         *
         */
        public void runTest()
        {
            Context        ctx;
            String         name = "test";
            String         value = "Just A Test";
            Object         object;
            RuntimeContext runCtx;
            VerboseStream  stream = new VerboseStream();

            try {
                object = new TestObject( value );
                // Construct the same context from two perspsective
                // and compare bound values
                stream.writeVerbose( "Test binding of Referenceable object in MemoryContext" );
                runCtx = RuntimeContext.newRuntimeContext();
                ctx = runCtx.getEnvContext();
                ctx.bind( name, object );
                if ( ctx.lookup( name ) == object ) {
                    fail( "Error: Same object instance returned in both cases" );
                }
                if ( ! ctx.lookup( name ).equals( object ) ) {
                    fail( "Error: The two objects are not identical" );
                }
                stream.writeVerbose( "Bound one object, reconstructed another, both pass equality test" );

                stream.writeVerbose( "Test looking up Referenceable object from EnvContext" );
                RuntimeContext.setRuntimeContext( runCtx );
                ctx = new InitialContext();
                if ( ctx.lookup( "java:" + name ) == object ) {
                    fail( "Error: Same object instance returned in both cases" );
                }
                if ( ! ctx.lookup( "java:" + name ).equals( object ) ) {
                    fail( "Error: The two objects are not identical" );
                }
                RuntimeContext.unsetRuntimeContext();
                stream.writeVerbose( "Bound one object, reconstructed another, both pass equality test" );

            } catch ( NamingException except ) {
                System.out.println( except );
                except.printStackTrace();
            }
        }


        public static class TestObject
            implements Referenceable
        {

            private String _value;

            TestObject( String value )
            {
                _value = value;
            }

            public boolean equals( Object other )
            {
                if ( this == other )
                    return true;
                if ( other instanceof TestObject &&
                     ( (TestObject) other )._value.equals( _value ) )
                    return true;
                return false;
            }

            public Reference getReference()
                throws NamingException
            {
                Reference ref;

                // We use same object as factory.
                ref = new Reference( getClass().getName(), TestObjectFactory.class.getName(), null );
                ref.add( new StringRefAddr( "Value", _value ) );
                return ref;
            }

        }

        public static class TestObjectFactory
            implements ObjectFactory
        {

            public Object getObjectInstance( Object refObj, Name name, Context nameCtx, Hashtable env )
                throws NamingException
            {
                Reference ref;
                
                // Can only reconstruct from a reference.
                if ( refObj instanceof Reference ) {
                    ref = (Reference) refObj;
                    // Make sure reference is of datasource class.
                    if ( ref.getClassName().equals( TestObject.class.getName() ) ) {
                        TestObject object;

                        object = new TestObject( (String) ref.get( "Value" ).getContent() );
                        return object;
                        
                    } else
                        throw new NamingException( "Not a reference" );
                }
                return null;
            }

        }


    }

}




