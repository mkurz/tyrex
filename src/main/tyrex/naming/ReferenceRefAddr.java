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
 * $Id: ReferenceRefAddr.java,v 1.2 2001/03/12 19:20:16 arkin Exp $
 */


package tyrex.naming;

import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.NamingManager;


/////////////////////////////////////////////////////////////////////
// ReferenceRefAddr
/////////////////////////////////////////////////////////////////////

/**
 * This class allows referenceable objects to be stored
 * inside of other references.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
final class ReferenceRefAddr 
    extends RefAddr
{


    /**
     * The reference
     */
    private final Reference _reference;


    /**
     * The environment used to recreate the referenceable
     * object from the underlying {@link #_reference}
     */
    private final Hashtable _environment;


    /**
     * The object
     */
    private transient Object _object;


    /**
     * The hash code
     */
    private transient int _hashCode;


    /**
     * Create the ReferenceRefAddr with the specified arguments.
     * <p>
     * The reference from the specified referenceable object is 
     * retrieved and stored.
     * <p>
     * A null environment hashtable is used in the recreation of the 
     * referenceable object from the underlying reference.
     *
     * @param addressType the address type of the ref addr
     * @param referenceable the referenceable object
     * @throw NamingException if there is a problem get the reference
     *      from the referenceable object.
     */
    ReferenceRefAddr (String addressType, Referenceable referenceable)
        throws NamingException
    {
        this(addressType, referenceable, null);
    }


    /**
     * Create the ReferenceRefAddr with the specified arguments.
     * <p>
     * The reference from the specified referenceable object is 
     * retrieved and stored.
     *
     * @param addressType the address type of the ref addr
     * @param referenceable the referenceable object
     * @param environment used in the recreation of the
     * referenceable object from the underlying reference. Can be null.
     * @throw NamingException if there is a problem get the reference
     *      from the referenceable object.
     */
    ReferenceRefAddr (String addressType, 
                      Referenceable referenceable,
                      Hashtable environment)
        throws NamingException
    {
        super(addressType);

        if (null == referenceable) {
            throw new IllegalArgumentException("The argument 'referenceable' is null.");
        }

        _reference = referenceable.getReference();
        _environment = environment;

    }


    /**
     * Create the ReferenceRefAddr with the specified arguments.
     * <p>
     * A null environment hashtable is used in the recreation of the 
     * referenceable object from the underlying reference.
     *
     * @param addressType the address type of the ref addr
     * @param reference the reference
     */
    ReferenceRefAddr( String addressType, Reference reference )
    {
        this(addressType, reference, null);
    }

    /**
     * Create the ReferenceRefAddr with the specified arguments.
     *
     * @param addressType the address type of the ref addr
     * @param reference the reference
     * @param environment used in the recreation of the
     * referenceable object from the underlying reference. Can be null.
     */
    ReferenceRefAddr( String addressType, 
                      Reference reference,
                      Hashtable environment )
    {
        super( addressType );

        if (null == reference)
            throw new IllegalArgumentException("The argument 'reference' is null.");
        _reference = reference;
        _environment = environment;

    }

    /**
     * Return true if the specified object is of type
     * ReferenceRefAddr and has the same underlying reference.
     *
     * @param object the object
     * @return true if the specified object is of type
     * ReferenceRefAddr and has the same underlying reference.
     */
    public boolean equals(Object object)
    {
        if ((null == object) ||
            !(object instanceof ReferenceRefAddr)) {
            return false;
        }

        return _reference.equals(((ReferenceRefAddr)object)._reference);
    }

    /**
     * Return the hashcode of the underlying reference as the hashcode
     * of the ReferenceRefAddr.
     *
     * @return the hashcode of the underlying reference as the hashcode
     *      of the ReferenceRefAddr.
     */
    public int hashCode()
    {
        if (0 == _hashCode) 
            _hashCode = _reference.hashCode();

        return _hashCode;
    }

    /**
     * Returns the object referred to by the underlying
     * reference.
     *
     * @return the object referred to by the underlying
     * reference.
     */
    public Object getContent()
    {
        if (null == _object) {
            try {
                _object = NamingManager.getObjectInstance(_reference, null, null, _environment);        
            } catch (Exception e) { }
        }

        return _object;
    }

/*
    public static class Test 
        implements Referenceable, javax.naming.spi.ObjectFactory
    {
        private final String _string;

        public Test()
        {
            this("");
        }

        private Test(String string)
        {
            _string = string;
        }

        public javax.naming.Reference getReference() 
        {
            Reference ref = new Reference("tyrex.naming.ReferenceRefAddr$Test", 
                                          "tyrex.naming.ReferenceRefAddr$Test", 
                                          null);
            ref.add(new javax.naming.StringRefAddr("test", _string));
            return ref;
        }

        public Object getObjectInstance(Object object, 
                                                  javax.naming.Name name, 
                                                  javax.naming.Context context, 
                                                  Hashtable env)
            throws Exception
        {
            Reference ref = (Reference)object;
            return new Test((String)ref.get("test").getContent());
        }

        public String toString()
        {
            return super.toString() + "[" + _string + "]";
        }
    }


    public static class TestRef
        implements Referenceable, javax.naming.spi.ObjectFactory
    {
        private final Test _test;

        public TestRef()
        {
            this(null);
        }

        private TestRef(Test test)
        {
            _test = test;
        }

        public javax.naming.Reference getReference() 
        {
            Reference ref = new Reference("tyrex.naming.ReferenceRefAddr$TestRef", 
                                          "tyrex.naming.ReferenceRefAddr$TestRef", 
                                          null);
            try {
                ref.add(new ReferenceRefAddr("testref", _test));
            }
            catch(NamingException e) {
                e.printStackTrace();
            }
            return ref;
        }

        public Object getObjectInstance(Object object, 
                                                  javax.naming.Name name, 
                                                  javax.naming.Context context, 
                                                  Hashtable env)
            throws Exception
        {
            Reference ref = (Reference)object;
            return new TestRef((Test)ref.get("testref").getContent());
        }

        public String toString()
        {
            return super.toString() + "[" + _test + "]";
        }
    }

    public static void main (String args[]) 
        throws Exception
    {
        javax.naming.InitialContext ctx = new javax.naming.InitialContext();
        TestRef testref = new TestRef(new Test("Riad was here"));
        System.out.println("before " + testref);

        ctx.bind("testref", testref);

        TestRef resolved = (TestRef)ctx.lookup("testref");

        System.out.println("after " + resolved);
        
    }
*/
}
