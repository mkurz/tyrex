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
 * $Id: Naming.java,v 1.4 2000/09/08 23:03:11 mohammed Exp $
 */


package naming;


import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import org.exolab.jtf.CWBaseApplication;
import org.exolab.jtf.CWTestCategory;
import org.exolab.jtf.CWTestCase;
import org.exolab.jtf.CWVerboseStream;
import org.exolab.exceptions.CWClassConstructorException;
import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import tyrex.naming.EnvContext;
import tyrex.naming.NamingPermission;



public class Naming
    extends CWTestCategory
{


    public Naming()
        throws CWClassConstructorException
    {
        super( "naming", "JNDI service provider");
        
        CWTestCase tc;
        
        tc = new MemoryContextTest();
        add( tc.name(), tc, true );
        tc = new EnvContextTest();
        add( tc.name(), tc, true );
        tc = new ReferenceableTest();
        add( tc.name(), tc, true );
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


    public static InitialContext getEnvInitialContext()
        throws NamingException
    {
        Hashtable env;

        env = new Hashtable();
        env.put( Context.URL_PKG_PREFIXES, "tyrex.naming" );
        return new InitialContext( env );
    }


    /**
     * Tests the in-memory service provider (MemoryContext).
     */
    public static class MemoryContextTest
        extends CWTestCase
    {

        public MemoryContextTest()
            throws CWClassConstructorException
        {
            super( "TC01", "In-Memory Service Provider" );
        }

        public void preExecute()
        {
            super.preExecute();
        }
        
        public void postExecute()
        {
            super.postExecute();
        }
        
        public boolean run( CWVerboseStream stream )
        {
            String               value = "Just A Test";
            String               name = "test";
            String               sub = "sub";
            InitialContext       initCtx;
            Context              ctx1;
            Context              ctx2;

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
                    stream.writeVerbose( "Error: Same testValue not bound in both contexts" );
                    return false;
                }
                ctx2 = ctx2.createSubcontext( sub );
                ctx1 = (Context) ctx1.lookup( sub );
                ctx1.bind( sub + name, value );
                if ( ctx2.lookup( sub + name ) != value ) {
                    stream.writeVerbose( "Error: Same testValue not bound in both contexts" );
                    return false;
                }

                // Test that shared and non-shared spaces not the same.
                stream.writeVerbose( "Testing that shared and non-shared namespaces are different" );
                ctx2 = Naming.getInitialContext( null );
                try {
                    ctx2 = (Context) ctx2.lookup( name );
                    if ( ctx2.lookup( name ) == value ) {
                        stream.writeVerbose( "Error: Same testValue bound to not-shared contexts" );
                        return false;
                    }
                    stream.writeVerbose( "Error: NameNotFoundException not reported" );
                    return false;
                } catch ( NameNotFoundException except ) {
                    ctx2.bind( name, value );
                }
                // Test that two non-shared spaces not the same.
                stream.writeVerbose( "Testing that two non-shared namespaces are different" );
                ctx2 = Naming.getInitialContext( null );
                try {
                    ctx2 = (Context) ctx2.lookup( name );
                    if ( ctx2.lookup( name ) == value ) {
                        stream.writeVerbose( "Error: Same testValue bound to not-shared contexts" );
                        return false;
                    }
                    stream.writeVerbose( "Error: NameNotFoundException not reported" );
                    return false;
                } catch ( NameNotFoundException except ) {
                    ctx2.bind( name, value );
                }

                return true;
            } catch ( IOException except ) {
                System.out.println( except );
                return false;
            } catch ( NamingException except ) {
                System.out.println( except );
                return false;
            }
        }


    }


    /**
     * Tests the environment naming context (EnvContext).
     */
    public static class EnvContextTest
        extends CWTestCase
    {

        public EnvContextTest()
            throws CWClassConstructorException
        {
            super( "TC02", "Environment Naming Context" );
        }

        public void preExecute()
        {
            super.preExecute();
        }
        
        public void postExecute()
        {
            super.postExecute();
        }
        
        public boolean run( CWVerboseStream stream )
        {
            String               value = "testValue";
            String               path = "comp/env";
            String               name = "test";
            InitialContext       initCtx;
            Context              ctx;
            Context              enc;

            try {
                stream.writeVerbose( "Constructing a context with comp/env/test as a test value" );
                initCtx = Naming.getInitialContext( null );
                initCtx.createSubcontext( "comp" );
                initCtx.createSubcontext( "comp/env" );
                initCtx.bind( path + "/" + name, value );
                ctx = (Context) initCtx.lookup( "" );
                EnvContext.setEnvContext( ctx );

                stream.writeVerbose( "Test ability to read from the ENC in variety of ways" );
                try {
                    if ( initCtx.lookup( "java:" + path + "/" + name ) != value ) {
                        stream.writeVerbose( "Error: Failed to lookup name" );
                        return false;
                    }
                } catch ( NameNotFoundException except ) {
                    stream.writeVerbose( "Error: Failed to lookup name" );
                    return false;
                }
                try {
                    enc = (Context) initCtx.lookup( "java:" );
                    enc = (Context) enc.lookup( "comp" );
                    enc = (Context) enc.lookup( "env" );
                    if ( enc.lookup( name ) != value ) {
                        stream.writeVerbose( "Error: Failed to lookup name" );
                        return false;
                    }
                } catch ( NameNotFoundException except ) {
                    stream.writeVerbose( "Error: Failed to lookup name" );
                    return false;
                }


                stream.writeVerbose( "Test updates on memory context reflecting in ENC" );
                ctx.unbind( path + "/" + name );
                try {
                    enc.lookup( name );
                    stream.writeVerbose( "Error: NameNotFoundException not reported" );
                    return false;
                } catch ( NameNotFoundException except ) { }

                stream.writeVerbose( "Test that the JNDI ENC is read only" );
                try {
                    enc.bind( name, value );
                    stream.writeVerbose( "Error: JNDI ENC not read-only" );
                    return false;
                } catch ( OperationNotSupportedException except ) { }

                ctx.bind( path + "/" + name, value );
                try {
                    enc.unbind( name );
                    stream.writeVerbose( "Error: JNDI ENC not read-only" );
                    return false;
                } catch ( OperationNotSupportedException except ) { }


                stream.writeVerbose( "Test the stack nature of the JNDI ENC" );
                EnvContext.setEnvContext( (Context) Naming.getInitialContext( null ).lookup( "" ) );
                try {
                    initCtx.lookup( "java:" + path + "/" + name );
                    stream.writeVerbose( "Error: NameNotFoundException not reported" );
                    return false;
                } catch ( NotContextException except ) { }
                EnvContext.unsetEnvContext();
                try {
                    if ( initCtx.lookup( "java:" + path + "/" + name ) != value ) {
                        stream.writeVerbose( "Error: Failed to lookup name" );
                        return false;
                    }
                } catch ( NameNotFoundException except ) {
                    stream.writeVerbose( "Error: NameNotFoundException reported" );
                    return false;
                }
                EnvContext.unsetEnvContext();
                try {
                    initCtx.lookup( "java:" + path + "/" + name );
                    stream.writeVerbose( "Error: NamingException not reported" );
                    return false;
                } catch ( NamingException except ) { }


                stream.writeVerbose( "Test the serialization nature of JNDI ENC" );
                ctx = (Context) initCtx.lookup( "" );
                EnvContext.setEnvContext( ctx );
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
                    stream.writeVerbose( "Error: Failed to (de)serialize: " + except );
                    System.out.println( except );
                    return false;
                }
                EnvContext.unsetEnvContext();
                try {
                    enc.lookup( name );
                    stream.writeVerbose( "Error: Managed to lookup name but java:comp not bound to thread" );
                    return false;
                } catch ( NamingException except ) { }{}
                ctx = (Context) initCtx.lookup( "" );
                EnvContext.setEnvContext( ctx );
                try {
                    if ( enc.lookup( name ) != value ) {
                        stream.writeVerbose( "Error: Failed to lookup name" );
                        return false;
                    }
                } catch ( NameNotFoundException except ) {
                    stream.writeVerbose( "Error: NameNotFoundException reported" );
                    return false;
                }
                
                return true;
            } catch ( IOException except ) {
                System.out.println( except );
                return false;
            } catch ( NamingException except ) {
                System.out.println( except );
                except.printStackTrace();
                return false;
            }
        }


    }


    /**
     * Test the handling of referencable objects.
     */
    public static class ReferenceableTest
        extends CWTestCase
    {

        public ReferenceableTest()
            throws CWClassConstructorException
        {
            super( "TC03", "Referenceable Object Handling" );
        }

        public void preExecute()
        {
            super.preExecute();
        }
        
        public void postExecute()
        {
            super.postExecute();
        }
        
        public boolean run( CWVerboseStream stream )
        {
            Context   ctx;
            String    name = "test";
            String    value = "Just A Test";
            Object    object;

            try {
                object = new TestObject( value );
                // Construct the same context from two perspsective
                // and compare bound values
                stream.writeVerbose( "Test binding of Referenceable object in MemoryContext" );
                ctx = (Context) Naming.getInitialContext( null ).lookup( "" );
                ctx.bind( name, object );
                if ( ctx.lookup( name ) == object ) {
                    stream.writeVerbose( "Error: Same object instance returned in both cases" );
                    return false;
                }
                if ( ! ctx.lookup( name ).equals( object ) ) {
                    stream.writeVerbose( "Error: The two objects are not identical" );
                    return false;
                }
                stream.writeVerbose( "Bound one object, reconstructed another, both pass equality test" );

                stream.writeVerbose( "Test looking up Referenceable object from EnvContext" );
                EnvContext.setEnvContext( ctx );
                ctx = getEnvInitialContext();
                if ( ctx.lookup( "java:" + name ) == object ) {
                    stream.writeVerbose( "Error: Same object instance returned in both cases" );
                    return false;
                }
                if ( ! ctx.lookup( "java:" + name ).equals( object ) ) {
                    stream.writeVerbose( "Error: The two objects are not identical" );
                    return false;
                }
                EnvContext.unsetEnvContext();
                stream.writeVerbose( "Bound one object, reconstructed another, both pass equality test" );

                return true;
            } catch ( IOException except ) {
                System.out.println( except );
                return false;
            } catch ( NamingException except ) {
                System.out.println( except );
                except.printStackTrace();
                return false;
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

    /*
    public static void main (String args[]) {

        class NamingTest extends CWBaseApplication
        {
            private final Vector categories;

            public NamingTest(String s)
                throws CWClassConstructorException
            {
                super(s);

                categories = new Vector();
                categories.addElement("naming.Naming");
            }
        
            protected String getApplicationName()
            {
                return "NamingTest";
            }
        
            protected Enumeration getCategoryClassNames()
            {
                return categories.elements();
            }
        }

        try
        {
            NamingTest test = new NamingTest("naming test");
            test.run(args);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }
    */
}




