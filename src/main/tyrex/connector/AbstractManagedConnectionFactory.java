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


package tyrex.connector;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import javax.naming.Reference;
import javax.security.auth.Subject;
import tyrex.connector.manager.ConnectionManagerFactory;
import tyrex.util.ArraySet;

///////////////////////////////////////////////////////////////////////////////
// AbstractManagedConnectionFactory
///////////////////////////////////////////////////////////////////////////////

/**
 * This class defines base behaviour for concrete implementations of
 * {@link ManagedConnectionFactory}.
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
public abstract class AbstractManagedConnectionFactory 
    implements ManagedConnectionFactory, Serializable
{
    /**
     * Flag indicating the login timeout has changed
     */
    protected static final int loginTimeoutChanged      = 1;

    /**
     * Flag indicating the maximum number of connections
     * has changed
     */
    protected static final int maxConnectionChanged     = 2;

    /**
     * Flag indicating the minimum number of connections
     * has changed
     */
    protected static final int minConnectionChanged     = 3;

    /**
     * Flag indicating the description has changed
     */
    protected static final int descriptionChanged       = 4;

    /**
     * Flag indicating the log writer has changed
     */
    protected static final int logWriterChanged         = 5;

    /**
     * The amount of time to wait for login
     */
    private int         loginTimeout;

    /**
     * The maximum number of connections 
     * supported by the factory.
     */
    private int         maxConn;

    /**
     * The minimum number of connections
     * supported by the factory.
     */
    private int         minConn;

    /**
     * The description of the factory.
     */
    private String      description;

    /**
     * The print writer used to log actions
     */
    private PrintWriter logWriter;

    /**
     * The default connection manager used
     * with connection factories that did
     * not have a connection manager specified.
     */
    private transient ConnectionManager defaultConnectionManager;

    /**
     * Default constructor
     */
    protected AbstractManagedConnectionFactory()
    {
        // do nothing
    }

    /**
     * Creates and returns a new managed connection. A managed connection
     * includes an underlying connection and is managed by the connection
     * manager for pooling and transaction enlistment.
     *
     * @param subject the security information to create the managed 
     *      connection with.
     * @param info Optional connection creation information
     * @return Open managed connection
     * @throws ConnectionException The connection cannot be created
     * @see #allowEmptyCredentials
     */
    public final ManagedConnection createManagedConnection(Subject subject, Object info)
        throws ConnectionException
    {
        Set credentials = getCredentials(subject, info, true);

        if (!allowEmptyCredentials(true) &&
                ((null == credentials) || 
                 (0 == credentials.size()))) {
            throw new ConnectionException("Cannot find the proper credentials to create the managed connection.");    
        }

        try {
            return createManagedConnection(subject, credentials, info);
        } 
        catch (Exception except) {
            if (except instanceof ConnectionException) {
                throw (ConnectionException)except;    
            }
            throw new ConnectionException(except);
        }
    }


    /**
     * Return true if null or empty credentials are allowed 
     * to create a managed connection.
     * <p>
     * This method is used by 
     * {@link #createManagedConnection(Subject, Object)} and
     * {@link #getManagedConnection}.
     * <p>
     * Thedefault implemtnation is to return false;
     *
     * @param forCreate True if the empty credentials to
     *      used in creating a managed connection
     * @return true if null or empty credentials are allowed
     *      to create a managed connection.
     */
    protected boolean allowEmptyCredentials(boolean forCreate)
    {
        return false;
    }

    /**
     * Creates and returns a new managed connection. A managed connection
     * includes an underlying connection and is managed by the connection
     * manager for pooling and transaction enlistment.
     *
     * @param subject the security information to create the managed 
     *      connection with.
     * @param credentials the set of credentials to be used to create
     *      the managed connection. Can be null or empty depending on
     *      the method {@link #allowEmptyCredentials}
     * @param info Optional connection creation information
     * @return Open managed connection
     * @throws ConnectionException The connection cannot be created
     */
    protected abstract ManagedConnection createManagedConnection(Subject subject, Set credentials, Object info)
        throws ConnectionException;
    

    /**
     * Returns a suitable managed connection from the pool given the
     * connection creation properties. If a match is found it is returned,
     * otherwise, null is returned and a new connection will be created.
     *
     * @param subject the security information 
     * @param enum An enumeration of existing connections
     * @param info Optional connection creation information
     * @return Open managed connection
     * @throws ConnectionException The connection cannot be created
     */
    public final ManagedConnection getManagedConnection( Subject subject, Enumeration enum, Object info )
        throws ConnectionException
    {
        if (enum.hasMoreElements()) {
            ManagedConnection managed;
            
            Set credentials = getCredentials(subject, info, false);

            if (!allowEmptyCredentials(false) &&
                ((null == credentials) || 
                 (0 == credentials.size()))) {
                throw new ConnectionException("Cannot find the proper credentials to access the managed connection.");    
            }
            
            do {
                managed = (ManagedConnection) enum.nextElement();
                if (canAccess(managed, subject, credentials, info)) {
                    return managed;
                }
            } while (enum.hasMoreElements());
        }
        return null;
    }


    /**
     * Return true if the specified subject can access the 
     * specified managed connection.
     *
     * @param managedConnection the managed connection
     * @param subject the subject
     * @param credentials the set of credentials to be used
     *      by the subject to access the managed connection.
     *      Can be null or empty
     * @param info optional info
     * @return True if the specified subject can access the
     *      specified managed connection.
     * @throws ConnectionException if there is a connection problem
     */
    protected abstract boolean canAccess(ManagedConnection managedConnection, 
                                         Subject subject, 
                                         Set credentials, 
                                         Object info)
        throws ConnectionException;
    

    /**
     * Create the connection factory that uses the specified connection
     * manager.
     *
     * @param manager the connection manager. Cannot be null.
     * @return the created connection factory
     * @throws ConnectionException if the connection factory could not be created.
     */
    public ConnectionFactory createConnectionFactory(ConnectionManager manager)
        throws ConnectionException
    {
        return new ConnectionFactoryImpl(this, manager);
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
        propertyHasChanged(descriptionChanged);
    }


    /**
     * Returns the log writer for this managed connection.
     *
     * @return The log writer
     */
    public final PrintWriter getLogWriter()
    {
        return logWriter;
    }


    /**
     * Sets the log writer for this managed connection.
     *
     * @param logWriter The log writer
     */
    public final void setLogWriter(PrintWriter logWriter)
    {
        this.logWriter = logWriter;
        propertyHasChanged(logWriterChanged);
    }

 
    /**
     * Returns the login timeout.
     *
     * @return Login timeout
     */
    public final int getLoginTimeout()
    {
        return loginTimeout;
    }


    /**
     * Sets the login timeout.
     *
     * @param timeout Login timeout
     */
    public final void setLoginTimeout(int timeout)
    {
        this.loginTimeout = loginTimeout;
        propertyHasChanged(loginTimeoutChanged);
    }


    /**
     * Returns the maximum number of connections.
     *
     * @return Maximum number of connections
     */
    public final int getMaxConnection()
    {
        return maxConn;
    }


    /**
     * Sets the maximum number of connections.
     *
     * @param max Maximum number of connections
     */
    public final void setMaxConnection(int max)
    {
        this.maxConn = max;
        propertyHasChanged(maxConnectionChanged);
    }


    /**
     * Returns the minimum number of connections.
     *
     * @return Minimum number of connections
     */
    public final int getMinConnection()
    {
        return minConn;
    }


    /**
     * Sets the minimum number of connections.
     *
     * @param min Minimum number of connections
     */
    public final void setMinConnection(int min)
    {
        this.minConn = min;
        propertyHasChanged(minConnectionChanged);
    }


    /**
     * Creates a new connection factory to be enlisted in JNDI and
     * used by the application. The connection factory is associated
     * with a default connection manager on creation.
     *
     * @throws ConnectionException The connection factory cannot be created
     */
    public final ConnectionFactory createConnectionFactory()
        throws ConnectionException
    {
        return createConnectionFactory(getDefaultConnectionManager());
    }
    

    /**
     * Return the printed representation of the factory
     * that consists of the default toString string
     * with the description.
     *
     * @return the printed representation of the factory.
     */
    public String toString()
    {
        return super.toString() + 
                "[" + 
                (null == description ? "" : description) + 
                "]";
    }


    /**
     * Return the credential, used for making a connection, from the 
     * specified subject. If one does not exist return null.
     * <p>
     * The default implementation returns the union of the public
     * and private credentials that satisfy {@link #isValidCredential}.
     * 
     * @param subject the subject containing the security information
     * @param info optional info
     * @param forCreate True if the credentials to be returned are to
     *      used in creating 
     * @return the credential, used for making a connection, from the
     *      specified subject. If one does not exist return null.
     * @throws ConnectionException if the specified subject cannot 
     *      access the managed connection factory
     * @see #isValidCredential
     * @see #searchPrivateCredentials
     */
    protected Set getCredentials(Subject subject,
                                 Object info,
                                 boolean forCreate)
        throws ConnectionException
    {
        // the result set of credentials
        Set credentials = new ArraySet();
        // get the credential by first looking at the public credentials
        findCredentials(subject.getPublicCredentials(),
                        info,
                        false,
                        credentials);
        // if that failed try the private credentials
        if (searchPrivateCredentials(subject)) {
            findCredentials(subject.getPrivateCredentials(),
                            info,
                            true,
                            credentials);
        }
        return credentials;
    }


    /**
     * Return true if the private credentials are to be searched for the
     * specified subject.
     * <p>
     * The default implementation returns true for all subjects. This
     * method is used by the default implementation of 
     * {@link #getCredentials}.
     *
     * @param subject the subject
     * @return true if the private credentials are to be searched for the
     * specified subject.
     */
    protected boolean searchPrivateCredentials(Subject subject)
    {
        return true;
    }

    /**
     * Return the credential from the set of credentials that matches
     * the specified data source name. If none is found return null.
     * <BR>
     * The default implementation searches for the first JDBCConnectionCredential
     * whose data source name matches the data source name of the specified
     * jdbcInfo. It treats null and empty string data source names as the same.
     *
     * @param jdbcInfo the optional information. Can be null.
     * @param credentials the set of credentials.
     * @param isPrivate True if the credentials are private
     */
    private void findCredentials(Set credentials,
                                 Object info, 
                                 boolean isPrivate,
                                 Set foundCredentials)
    {
        // the current credential
        Object credential;

        for (Iterator i = credentials.iterator(); i.hasNext();) {
            credential = i.next();
            
            if (isValidCredential(credential, info, isPrivate)) {
                foundCredentials.add(credential);
            }
        }
    }


    /**
     * Return true if the credential is valid for this factory
     * <p>
     * This method is used by the default implementation of 
     * {@link #getCredentials}.
     *
     * @param credential the credential
     * @param info optional info
     * @param isPrivate true if the specified credential came from
     *      the subject's private credentials.
     * @return true if the credential is valid for this factory 
     */
    protected abstract boolean isValidCredential(Object credential,
                                                 Object info,
                                                 boolean isPrivate);
    

    /**
     * Notify the subclasses that a property has changed.
     *
     * @param change the change that has occurred
     * @see loginTimeoutChanged
     * @see maxConnectionChanged
     * @see minConnectionChanged
     * @see descriptionChanged
     * @see logWriterChanged
     */
    protected void propertyHasChanged(int change)
    {
        // do nothing
    }

    /**
     * Return true if a new connection manager is created everytime
     * for use with connection factories that did not
     * have a connection manager specified.
     * The default is false ie a new default connection manager is not
     * created.
     *
     * @return True if a new connection manager is created everytime
     * for use in creating connection factories that did not
     * have a connection manager specified.
     */
    protected boolean createNewDefaultConnectionManager()
    {
        return false;
    }

    /**
     * Create a default connection manager for use with
     * connection factories that did not have a connection
     * manager specified.
     *
     * @return a default connection manager
     */
    protected ConnectionManager createDefaultConnectionManager()
    {
        return ConnectionManagerFactory.build(null);
    }

    /**
     * Get a default connection manager for use with 
     * connection factories that do not have
     * a connection manager specified.
     *
     * @return a default connection manager.
     */
    private ConnectionManager getDefaultConnectionManager()
    {
        if (createNewDefaultConnectionManager()) {
            return createDefaultConnectionManager();    
        }

        synchronized (this)
        {
            if (null == defaultConnectionManager) {
                defaultConnectionManager = createDefaultConnectionManager();    
            }
        }
        
        return defaultConnectionManager;
    }
}
