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
 * $Id: JDBCManagedConnectionFactory.java,v 1.3 2000/09/08 23:04:44 mohammed Exp $
 */


package tyrex.connector.jdbc;


import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.sql.SQLException;
import javax.security.auth.Subject;
import javax.sql.XADataSource;
import tyrex.connector.AbstractManagedConnectionFactory;
import tyrex.connector.ManagedConnectionFactory;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ConnectionManager;
import tyrex.connector.ConnectionException;


/**
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/08 23:04:44 $
 */
public abstract class JDBCManagedConnectionFactory 
    extends AbstractManagedConnectionFactory
    implements ManagedConnectionFactory, Serializable
{

    /**
     * The XA data source 
     */
    private XADataSource xaDataSource = null;
    

    /**
     * Default constructor
     */
    protected JDBCManagedConnectionFactory()
    {

    }


    /**
     * Return the XA data source associated with the factory. 
     * If one does not exist return null.
     *
     * @return the XA data source associated with the factory. 
     *      If one does not exist return null.
     */
    protected final XADataSource getDataSource()
    {
        return xaDataSource;
    }

    
    /**
     * Return the XA data source associated with the factory. If one does not
     * exist then create it using the specified arguments.
     *
     * @param subject the security information for getting the data source.
     * @param info the optional info used to create the data source
     * @return the XA data source associated with the factory.
     */
    protected synchronized final XADataSource getDataSource(Subject subject,
                                                            Set credentials,
                                                            Object info)
        throws ConnectionException
    {
        // The double-check idiom is not used because it may not work because
        // out-of-order execution of instructions ie the setting of the reference
        // can occur before the referenced object has been fully constructed. This
        // happens especially in a multi-processor environment
        
        if ( xaDataSource == null ) {

            xaDataSource = createDataSource(subject, credentials, info);

            if (null == xaDataSource) {
                throw new ConnectionException("Failed to create data source.");    
            }

            try {
                xaDataSource.setLogWriter(getLogWriter());
                xaDataSource.setLoginTimeout(getLoginTimeout());    
            }
            catch (SQLException e) {
                throw new ConnectionException(e);
            }
        }
        else if (!canAccess(xaDataSource, subject, credentials, info)){
            throw new ConnectionException("Subject <" + subject + "> cannot access required data source.");
        }

        return xaDataSource;
    }

    /**
     * Return true if the specified subject can access the specified
     * data source.
     *
     * @param subject the subject containing the security information
     * @param info optional info for accessing the data source.
     * @param xaDataSource the data source
     * @return true if the specified subject can access the specified
     * data source.
     */
    protected abstract boolean canAccess(XADataSource xaDataSource,
                                         Subject subject, 
                                         Set credentials,
                                         Object info);

    /**
     * Create the XA data source to be associated with this factory.
     * This method is only called once for a particular connection info object
     * and should not be called directly.
     *
     * @param subject the security information for creating the data source.
     * @param info optional info for creating the data source. Can be null.
     * @return the created data source.
     */
    protected abstract XADataSource createDataSource(Subject subject,
                                                     Set credentials,
                                                     Object info)
        throws ConnectionException;


    /**
     * Return true if the credential is valid for this factory
     * <p>
     * This method is used by the default implementation of 
     * {@link #getCredentials}.
     * <p>
     * The default implementation returns true if the specified
     * credential is an instance of {@link JDBCConnectionCredential}.
     *
     * @param credential the credential
     * @param info optional info
     * @param isPrivate true if the specified credential came from
     *      the subject's private credentials.
     * @return true if the credential is valid for this factory 
     */
    protected boolean isValidCredential(Object credential,
                                        Object info,
                                        boolean isPrivate)
    {
        return credential instanceof JDBCConnectionCredential;
    }


    /**
     * Return the JDBCConnectionInfo object from the specified object.
     * <p>
     * The default implementation tries to cast the specified
     * object to JDBCConnectionInfo. If the cast fails null is returned.
     *
     * @param object the object
     * @return the JDBCConnectionInfo object from the specified object.
     */
    protected JDBCConnectionInfo getJDBCConnectionInfo(Object object)
    {
        return ((null == object) || (!(object instanceof JDBCConnectionInfo)))
                ? null
                : (JDBCConnectionInfo)object;
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
     */
    public final ManagedConnection createManagedConnection(Subject subject, Set credentials, Object info)
        throws ConnectionException
    {
        // get the connection credential
        JDBCConnectionCredential credential = getConnectionCredential(subject, credentials, info);

        if (null == credential) {
            throw new ConnectionException("Cannot find the connection credential.");    
        }

        try {
            return new JDBCManagedConnection(getDataSource(subject, credentials, info).getXAConnection(credential.getUserName(),
                                                                                                       credential.getPassword()),
                                             getJDBCConnectionInfo(info));
        } 
        catch (SQLException except) {
            throw new ConnectionException(except);
        }
    }


    /**
     * Return the credential from the specified set of
     * credentials used to get a connection from the
     * data source.
     * <p>
     * The default implementation is to return the
     * the only credential if the set is of size 1.
     * Otherwise null is returned.
     *
     * @param credentials the set of credentials
     * @return the credential from the specified set of
     * credentials used to get a connection from the
     * data source.
     */
    protected JDBCConnectionCredential getConnectionCredential(Subject subject, 
                                                               Set credentials, 
                                                               Object jdbcInfo)
    {
        return (null == credentials) || (1 != credentials.size())
                ? null
                : (JDBCConnectionCredential) credentials.iterator().next();
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
    protected boolean canAccess(ManagedConnection managedConnection, 
                                Subject subject, 
                                Set credentials, 
                                Object info)
        throws ConnectionException
    {
        return (managedConnection instanceof JDBCManagedConnection)
                ? canAccess((JDBCManagedConnection) managedConnection,
                            subject,
                            credentials,
                            getJDBCConnectionInfo(info))
                : false;
    }

    /**
     * Return true if the specified subject can access the 
     * specified managed connection.
     * <p>
     * The default implementation just tests whether the
     * the specified info object is the same as the one used
     * to create the specified managed connection.
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
    protected boolean canAccess(JDBCManagedConnection managedConnection, 
                                Subject subject, 
                                Set credentials, 
                                JDBCConnectionInfo info)
        throws ConnectionException
    {
        return managedConnection.isSameInfo(info);
    }
}




