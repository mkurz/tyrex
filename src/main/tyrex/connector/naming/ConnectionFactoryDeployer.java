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
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 */


package tyrex.connector.naming;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import tyrex.connector.ManagedConnectionFactoryBuilder;

///////////////////////////////////////////////////////////////////////////////
// ConnectionFactoryDeployer
///////////////////////////////////////////////////////////////////////////////

/**
 * This class is responsible for binding connection factories in
 * an jndi context.
 * <P>
 * The deployment of connection factory stores a reference
 * containing managed connection factory builder class 
 * and an object factory class, that makes use of that 
 * class, in a context. The managed connection factory 
 * builder has a public no-arguments
 * constructor and "knows" how to create the required
 * managed connection factory. The object factory class
 * instantiates the managed connection factory builder class
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 * @see ConnectionFactoryObjectFactoryImpl
 */
public final class ConnectionFactoryDeployer 
{
   /**
    * The reference address type used to store the 
    * fully qualified class name for the managed
    * connection factory builder.
    */
    private static final String managedConnectionFactoryBuilderName = "managedConnectionFactoryBuilder";


    /**
     * The default object factory
     * @see ConnectionFactoryObjectFactoryImpl
     */
    public static final String defaultObjectFactoryClassName = "tyrex.connector.naming.ConnectionFactoryObjectFactoryImpl";


    /**
     * No instances
     */
    private ConnectionFactoryDeployer()
    {
        
    }

    /**
     * Return the fully qualified class name of the
     * object used to build managed connection factories from
     * the specified reference. If one does not exist return
     * null.
     *
     * @return the fully qualified class name of the
     *      object used to build managed connection factories from
     *      the specified reference. If one does not exist return
     *      null.
     */
    public static String getManagedConnectionFactoryBuilderClassName(Reference ref)
    {
        RefAddr addr = ref.get(ConnectionFactoryDeployer.managedConnectionFactoryBuilderName);
        
        return (null != addr) ? (String)addr.getContent() : null;
    }

    /**
     * Deploy the specified connection factory by bind a reference,
     * that created using the specified arguments, in the specified
     * context.
     * <P>
     * The object factory class name defaults to 
     * {@link #defaultObjectFactoryClassName}.
     *
     * @param name the name in the context
     * @param context the naming context
     * @param connectionFactoryClassName the fully qualified 
     *      class name of the connection factory. Cannot be null.
     * @param managedConnectionFactoryBuilderClassName the
     *      fully qualified class name of the class used
     *      build managed connection factories. Can be null.
     * @throw NamingException if there is a problem creating the 
     *      reference and binding it in the context.
     */
    public static void deploy(String name,
                              Context context,
                              String connectionFactoryClassName,
                              String managedConnectionFactoryBuilderClassName)
        throws NamingException
    {
        deploy(name, context, connectionFactoryClassName, 
               managedConnectionFactoryBuilderClassName,
               defaultObjectFactoryClassName);
    }

    /**
     * Deploy the specified connection factory by bind a reference,
     * that created using the specified arguments, in the specified
     * context.
     * <P>
     * The object factory location defaults to null.
     *
     * @param name the name in the context
     * @param context the naming context
     * @param connectionFactoryClassName the fully qualified 
     *      class name of the connection factory. Cannot be null.
     * @param managedConnectionFactoryBuilderClassName the
     *      fully qualified class name of the class used
     *      build managed connection factories. Can be null.
     * @param objectFactoryClassName the fully qualified
     *      class name of the object factory used to create
     *      connection factory using the 
     *      managedConnectionFactoryBuilderClassName. Cannot
     *      be null.
     * @throw NamingException if there is a problem creating the 
     *      reference and binding it in the context.
     */
    public static void deploy(String name,
                              Context context,
                              String connectionFactoryClassName,
                              String managedConnectionFactoryBuilderClassName,
                              String objectFactoryClassName)
        throws NamingException
    {
        deploy(name, context, connectionFactoryClassName, 
               managedConnectionFactoryBuilderClassName, 
               objectFactoryClassName, null);
    }

    /**
     * Deploy the specified connection factory by bind a reference,
     * that created using the specified arguments, in the specified
     * context.
     *
     * @param name the name in the context
     * @param context the naming context
     * @param connectionFactoryClassName the fully qualified 
     *      class name of the connection factory. Cannot be null.
     * @param managedConnectionFactoryBuilderClassName the
     *      fully qualified class name of the class used
     *      build managed connection factories. Can be null.
     * @param objectFactoryClassName the fully qualified
     *      class name of the object factory used to create
     *      connection factory using the 
     *      managedConnectionFactoryBuilderClassName. Cannot
     *      be null.
     * @param objectFactoryLocation the location of the
     *      object factory class. Can be null.
     * @throw NamingException if there is a problem creating the 
     *      reference and binding it in the context.
     */
    public static void deploy(String name,
                              Context context,
                              String connectionFactoryClassName,
                              String managedConnectionFactoryBuilderClassName,
                              String objectFactoryClassName,
                              String objectFactoryLocation)
        throws NamingException
    {
        if (null == connectionFactoryClassName) {
            throw new IllegalArgumentException("The argument 'connectionFactoryClassName' is null.");
        }
        else {
            validateClass(connectionFactoryClassName);
        }
        
        if (null != managedConnectionFactoryBuilderClassName) {
            validateClass(managedConnectionFactoryBuilderClassName);
        }

        if (defaultObjectFactoryClassName.equals(objectFactoryClassName)) {
            try {
                Class clazz = Class.forName(managedConnectionFactoryBuilderClassName);
                if (!ManagedConnectionFactoryBuilder.class.isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException("The class <" + 
                                                        managedConnectionFactoryBuilderClassName + 
                                                        "> does not implement tyrex.connection.ManagedConnectionFactoryBuilder.");
                }
            }
            catch (ClassNotFoundException e) {
                // this should not happen
                throw new IllegalArgumentException("The class <" + 
                                                   managedConnectionFactoryBuilderClassName + 
                                                   "> does not exist.");
            }    
        }
        else if (null == objectFactoryClassName) {
            throw new IllegalArgumentException("The argument 'objectFactoryClassName' is null.");
        }
        else {
            validateClass(objectFactoryClassName);
        }

        // make the reference
        Reference ref = new Reference(connectionFactoryClassName,
                                      new StringRefAddr(managedConnectionFactoryBuilderName,
                                                        managedConnectionFactoryBuilderClassName),
                                      objectFactoryClassName,
                                      objectFactoryLocation);

        context.bind(name, ref);

    }

    /**
     * Validate that the class with the specified fully qualified 
     * class name exists.
     */
    private static void validateClass(String className)
    {
        try {
            Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("The class <" + className + "> does not exist.");
        }
    }

}