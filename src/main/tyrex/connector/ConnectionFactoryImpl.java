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


package tyrex.connector;

import java.io.PrintWriter;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

///////////////////////////////////////////////////////////////////////////////
// AbstractConnectionFactory
///////////////////////////////////////////////////////////////////////////////

/**
 * This classes defines default behaviour for {@link ConnectionFactory}
 * 
 *@author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public class ConnectionFactoryImpl 
    implements ConnectionFactory
{
    /**
     * The login timeout type. 
     * Used to indicate when the login timeout has
     * changed and as the address type in the
     * Reference of the ConnectionFactoryImpl
     */
    protected static final String loginTimeoutType  = "loginTimeout".intern();


    /**
     * The description type
     * Used to indicate when the description has
     * changed and as the address type in the
     * Reference of the ConnectionFactoryImpl
     */
    protected static final String descriptionType   = "description".intern();


    /**
     * The log writer type
     * Used to indicate when the description has
     * changed and as the address type in the
     * Reference of the ConnectionFactoryImpl
     */
    protected static final String logWriterType     = "logWriter".intern();


    /**
     * The connection manager associated with connection factory
     */
    private final ConnectionManager connectionManager;


    /**
     * The managed connection factory associated with the connection factory
     */
    private final ManagedConnectionFactory managedConnectionFactory;


    /**
     * The description of the factory. Can be null.
     */
    private String description = null;


    /**
     * The print writer responsible for writing to the log.
     * can be null.
     */
    private PrintWriter logWriter = null;


    /**
     * The amount of time in milliseconds to wait for login
     */
    private int timeout = 0;


    /**
     * The reference for the ConnectionFactory
     */
    private Reference reference = null;

    /**
     * Create the AbstractConnectionFactory with the specified connection
     * manager and managed connection factory.
     * 
     * @param connectionManager the object that manages managed connections
     *      Cannot be null.
     * @param managedConnectionFactory the factory for creating managed connections
     *      Cannot be null.
     */
    public ConnectionFactoryImpl(ManagedConnectionFactory managedConnectionFactory,
                                 ConnectionManager connectionManager)
    {
        // validate
        if (null == connectionManager) {
            throw new IllegalArgumentException("The argument 'connectionManager' is null.");
        }
        if (null == managedConnectionFactory) {
            throw new IllegalArgumentException("The argument 'managedConnectionFactory' is null.");
        }
        // set the fields
        this.connectionManager = connectionManager;
        this.managedConnectionFactory = managedConnectionFactory;
    }

    /**
     * Returns a suitable connection given the
     * connection creation properties.
     *
     * @param info Optional connection creation information
     * @return connection
     * @throws ConnectionException The connection cannot be created
     */
    public Object getConnection(Object info)
        throws ConnectionException
    {
        return connectionManager.getConnection(managedConnectionFactory, info);
    }


    /**
     * Returns the description of this factory.
     *
     * @return The description of this factory
     */
    public final String getDescription()
    {
        return description;
    }


    /**
     * Sets the description of this factory.
     *
     * @param description The description of this factory
     */
    public final void setDescription(String description)
    {
        this.description = description;
        propertyHasChanged(descriptionType);
    }
    

    /**
     * Return the log writer associated with the connection
     * factory. Can return null.
     *
     * @return the log writer associated with the connection
     * factory.
     */
    public final PrintWriter getLogWriter()
    {
        return logWriter;
    }
    

    /**
     * Set the log writer for the connection factory.
     *
     * @param logWriter the new log writer. Can be null.
     */
    public final void setLogWriter(PrintWriter logWriter)
    {
        this.logWriter = logWriter;
        propertyHasChanged(logWriterType);
    }


    /**
     * Return the login timeout
     *
     * @return the login timeout
     */
    public final int getLoginTimeout()
    {
        return timeout;
    }


    /**
     * Set the login timeout.
     *
     * @param timeout The new login timeout. Must not
     *      be less than zero.
     */
    public final void setLoginTimeout(int timeout)
    {
        if (timeout < 0) {
            throw new IllegalArgumentException("The argument 'timeout' is less than 0.");    
        }
        this.timeout = timeout;
        propertyHasChanged(loginTimeoutType);
    }
    

    /**
     * Return the connection manager.
     * 
     * @return the connection manager
     */
    protected final ConnectionManager getConnectionManager()
    {
        return connectionManager;
    }

    /**
     * Return the managed connection factory
     * 
     * @return the managed connection factory
     */
    protected final ManagedConnectionFactory getManagedConnectionFactory()
    {
        return managedConnectionFactory;
    }

    /**
     * Notify the subclasses that a property has changed.
     *
     * @param change the change that has occurred
     * @see loginTimeoutType
     * @see descriptionType
     * @see logWriterType
     */
    protected void propertyHasChanged(String change)
    {
        // do nothing
    }


    /**
     * Return the fully qualified name of the
     * object factory class used to construct
     * the connection factory.
     *
     * @return the fully qualified name of the
     *      object factory class used to construct
     *      the connection factory.
     */
    protected String getObjectFactoryClassName()
    {
        return "tyrex.connector.ConnectionFactoryImplObjectFactory";
    }


    /**
     * Retrieves the Reference of this object.
     *
     * @return The non-null Reference of this object.
     * @throws If a naming exception was encountered 
     *      while retrieving the reference.
     */
    public final Reference getReference()
        throws NamingException
    {
        return reference;
    }


    /**
     * Set the reference for this object to the specified reference.
     *
     * @param reference the reference
     */
    public final void setReference(Reference reference)
    {
        this.reference = reference;
    }

}
