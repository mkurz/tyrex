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
 */


package tyrex.connector.naming;

import java.util.Hashtable;
import java.util.HashMap;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import tyrex.connector.ConnectionFactory;
import tyrex.connector.ConnectionManager;
import tyrex.connector.ManagedConnectionFactory;
import tyrex.connector.ManagedConnectionFactoryBuilder;
import tyrex.util.WrappedException;

///////////////////////////////////////////////////////////////////////////////
// ConnectionFactoryObjectFactoryImpl
///////////////////////////////////////////////////////////////////////////////

/**
 * This implementation of javax.naming.spi.ObjectFactory provides a
 * way to create connection factories
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public class ConnectionFactoryObjectFactoryImpl 
    implements ObjectFactory
{
    /**
     * Map to track managed connection factories
     * //TODO Replace with weak value hash map
     */
    private final static HashMap managedConnectionFactoryMap = new HashMap();

    /**
     * Public no-args constructor.
     */
    public ConnectionFactoryObjectFactoryImpl()
    {
    }


    /**
     * Return the connection factory that is constructed
     * using the specified parameters.
     *
     * @param obj The possibly null object containing 
     *      location or reference information that can 
     *      be used in creating the managed connection
     *      factory that will be used to create the
     *      actual connection factory.
     * @param name The name of this object relative to 
     *      nameCtx, or null if no name is specified.
     * @param nameCtx The context relative to which 
     *      the name parameter is specified, or null 
     *      if name is relative to the default initial context.
     * @param environment The possibly null environment 
     *      that is used in creating the object.
     * @return The object created; null if an object 
     *      cannot be created.
     * @throws if this object factory encountered an 
     *      exception while attempting to create an object, 
     *      and no other object factories are to be tried.
     */
    public final Object getObjectInstance(Object obj,
                                          Name name,
                                          Context nameCtx,
                                          Hashtable environment)
        throws Exception
    {
        // get the key for looking up managed connection factories
        Object managedConnectionFactoryKey = getManagedConnectionFactoryKey(obj);
            
        if (null != managedConnectionFactoryKey) {
            synchronized(managedConnectionFactoryMap) {
                // look up the an existing managed connection factory
                ManagedConnectionFactory managedConnectionFactory = (ManagedConnectionFactory)managedConnectionFactoryMap.get(managedConnectionFactoryKey);
                
                // if one exists return it
                if (null != managedConnectionFactory) {
                    return managedConnectionFactory;
                }

                // give the subclass first crack at creating the managed connection factory
                managedConnectionFactory = createManagedConnectionFactory(obj, name, 
                                                                          nameCtx, environment);

                    

                if ((null == managedConnectionFactory) &&
                    (obj instanceof Reference)) {
                    Reference reference = (Reference)obj;

                    // if the subclass fail to create
                    if (null == managedConnectionFactory) {
                        String managedConnectionFactoryBuilderName = ConnectionFactoryDeployer.getManagedConnectionFactoryBuilderClassName(reference);
        
                        if (null != managedConnectionFactoryBuilderName) {
                            managedConnectionFactory = createManagedConnectionFactory(managedConnectionFactoryBuilderName,
                                                                                      getManagedConnectionFactoryParameters(reference,
                                                                                                                            name,
                                                                                                                            nameCtx,
                                                                                                                            environment));
                        }
                    }
                }

                // store the managed connection factory
                if (null != managedConnectionFactory) {
                    managedConnectionFactoryMap.put(managedConnectionFactoryKey, 
                                                    managedConnectionFactory);    
                    // get the connection manager
                    ConnectionManager connectionManager = getConnectionManager();
                    // call the correct createConnectionFactory method depending
                    // of whether the connectionManager is null
                    return null == connectionManager 
                            ? managedConnectionFactory.createConnectionFactory()
                            : managedConnectionFactory.createConnectionFactory(connectionManager);    
                }
            }
        }
        // allow the subclasses one last crack
        return createConnectionFactory(obj, name, nameCtx, environment);
    }

    
    /**
     * Return the key used to retrieve already created managed
     * connection factories from a static map.
     * <p>
     * The default method returns the specified object as the key.
     *
     * @param obj The possibly null object containing 
     *      location or reference information that can 
     *      be used in creating the managed connection
     *      factory that will be used to create the
     *      actual connection factory.
     * @see #getObjectInstance
     */
    protected Object getManagedConnectionFactoryKey(Object obj)
    {
        return obj;
    }

    /**
     * Create the managed connection factory using the specified 
     * fully qualified managed connection factory builder class 
     * name and the specified parameters
     *
     * @param builderClassName the fully qualified managed 
     *      connection factory builder class name. This class
     *      is assumed to have a no-arguments public 
     *      constructor and implements 
     *      {@link tyrex.connector.ManagedConnectionFactoryBuilder}.
     * @param parameters hashtable containing the parameters to be
     *      used in initializing the managed connection factory.
     * @return the created managed connection factory
     * @throws ManagedConnectionFactoryBuilder.BuildException if the
     *      managed connection factory cannot be created
     */
    protected ManagedConnectionFactory createManagedConnectionFactory(String builderClassName,
                                                                      Hashtable parameters)
        throws ManagedConnectionFactoryBuilder.BuildException
    {
        //return ((ManagedConnectionFactoryBuilder)Class.forName(builderClassName).newInstance()).build(parameters);
        try {
            // get the class
            Class builderClass = Class.forName(builderClassName);
            // check that the class implements ManagedConnectionFactoryBuilder
            if (!ManagedConnectionFactoryBuilder.class.isAssignableFrom(builderClass)) {
                throw new ManagedConnectionFactoryBuilder.BuildException("The class '" + 
                                                                         builderClassName + 
                                                                         "' does not implement tyrex.connection.ManagedConnectionFactoryBuilder.");
            }
            // ignoring other checks like "is builder class an interface, 
            // abstract, has a public no-arg constructor
            // let the newInstance method throw the exception
            return ((ManagedConnectionFactoryBuilder)builderClass.newInstance()).build(parameters);
        }
        catch (ClassNotFoundException e) {
            throw new ManagedConnectionFactoryBuilder.BuildException("The class '" + builderClassName + "' does not exist.");
        }
        catch(IllegalAccessException e) {
            throw new ManagedConnectionFactoryBuilder.BuildException("The class '" + builderClassName + "' is not accessible.",
                                                                     e);
        }
        catch(InstantiationException e) {
            throw new ManagedConnectionFactoryBuilder.BuildException("The class '" + builderClassName + "' could not be instantiated.",
                                                                     e);
        }
    }
                                                   

    /**
     * Return the connection manager that is to be used
     * with the connection factory and managed connection
     * factory.
     * <p>
     * The defualt implementation returns null.
     *
     * @return the connection manager
     */
    protected ConnectionManager getConnectionManager()
    {
        return null;
    }


    /**
     * Return the connection factory that is constructed
     * using the specified parameters.
     * <p>
     * This method can be overridden by subclasses.
     * The default implementation returns null.
     *
     * @param reference The reference to the connection factory.
     * @param name The name of this object relative to 
     *      nameCtx, or null if no name is specified.
     * @param nameCtx The context relative to which 
     *      the name parameter is specified, or null 
     *      if name is relative to the default initial context.
     * @param environment The possibly null environment 
     *      that is used in creating the object.
     * @return The object created; null if an object 
     *      cannot be created.
     * @throws if this object factory encountered an 
     *      exception while attempting to create an object, 
     *      and no other object factories are to be tried.
     */
    protected ConnectionFactory createConnectionFactory(Object obj,
                                                        Name name,
                                                        Context nameCtx,
                                                        Hashtable environment)
        throws Exception
    {
        return null;
    }
    

    /**
     * Return the connection factory that is constructed
     * using the specified parameters.
     * <p>
     * This method can be overridden by subclasses.
     * The default implementation returns null.
     *
     * @param obj The possibly null object containing 
     *      location or reference information that can 
     *      be used in creating an object.
     * @param name The name of this object relative to 
     *      nameCtx, or null if no name is specified.
     * @param nameCtx The context relative to which 
     *      the name parameter is specified, or null 
     *      if name is relative to the default initial context.
     * @param environment The possibly null environment 
     *      that is used in creating the object.
     * @return The object created; null if an object 
     *      cannot be created.
     * @throws if this object factory encountered an 
     *      exception while attempting to create an object, 
     *      and no other object factories are to be tried.
     */
    protected ManagedConnectionFactory createManagedConnectionFactory(Object obj,
                                                                      Name name,
                                                                      Context nameCtx,
                                                                      Hashtable environment)
        throws Exception
    {
        return null;
    }

    /**
     * Return the hashtable used to initialize the managed connection
     * factory.
     * <p>
     * The default implementation returns null.
     *
     * @return the hashtable used to initialize the managed connection
     * factory.
     */
    protected Hashtable getManagedConnectionFactoryParameters(Reference reference,
                                                              Name name,
                                                              Context nameCtx,
                                                              Hashtable environment)
    {
        return null;
    }
}
