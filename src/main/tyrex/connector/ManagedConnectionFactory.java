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
 * $Id: ManagedConnectionFactory.java,v 1.3 2000/08/28 19:01:48 mohammed Exp $
 */


package tyrex.connector;


import java.io.PrintWriter;
import java.util.Enumeration;
import javax.security.auth.Subject;

/**
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/08/28 19:01:48 $
 */
public interface ManagedConnectionFactory
{

    /**
     * Returns the description of this factory.
     *
     * @return The description of this factory
     */
    public String getDescription();


    /**
     * Sets the description of this factory.
     *
     * @param description The description of this factory
     */
    public void setDescription( String description );


    /**
     * Returns the log writer for this managed connection.
     *
     * @return The log writer
     */
    public PrintWriter getLogWriter();


    /**
     * Sets the log writer for this managed connection.
     *
     * @param logWriter The log writer
     */
    public void setLogWriter( PrintWriter logWriter );

 
    /**
     * Returns the login timeout.
     *
     * @return Login timeout
     */
    public int getLoginTimeout();


    /**
     * Sets the login timeout.
     *
     * @param timeout Login timeout
     */
    public void setLoginTimeout( int timeout );


    /**
     * Returns the maximum number of connections.
     *
     * @return Maximum number of connections
     */
    public int getMaxConnection();


    /**
     * Sets the maximum number of connections.
     *
     * @param max Maximum number of connections
     */
    public void setMaxConnection( int max );


    /**
     * Returns the minimum number of connections.
     *
     * @return Minimum number of connections
     */
    public int getMinConnection();


    /**
     * Sets the minimum number of connections.
     *
     * @param min Minimum number of connections
     */
    public void setMinConnection( int min );


    /**
     * Creates a new connection factory to be enlisted in JNDI and
     * used by the application. The connection factory is associated
     * with a given connection manager on creation.
     *
     * @param manager The connection manager
     * @throws ConnectionException The connection factory cannot be created
     */
    public ConnectionFactory createConnectionFactory( ConnectionManager manager )
        throws ConnectionException;

    /**
     * Creates a new connection factory to be enlisted in JNDI and
     * used by the application. The connection factory is associated
     * with a default connection manager on creation.
     *
     * @throws ConnectionException The connection factory cannot be created
     */
    public ConnectionFactory createConnectionFactory( )
        throws ConnectionException;



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
     */
    public ManagedConnection createManagedConnection( Subject subject, Object info )
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
    public ManagedConnection getManagedConnection( Subject subject, Enumeration enum, Object info )
        throws ConnectionException;


}




