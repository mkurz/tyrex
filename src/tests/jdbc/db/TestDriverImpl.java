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
 *    permission of Intalio Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Technologies. Exolab is a registered
 *    trademark of Intalio Technologies.
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
 * Copyright 2000 (C) Intalio Technologies Inc. All Rights Reserved.
 *
 */

package jdbc.db;

import java.sql.*;
import java.util.*;

/**
 * The test jdbc driver
 */
public final class TestDriverImpl 
    implements Driver
{
    /**
     * The start of a valid test url (header + subprotocol)
     */
    private static final String TEST_URL_START = "jdbc:test";

    
    /**
     * Empty driver property array
     */
    private static final DriverPropertyInfo[] emptyDriverPropertyInfo = new DriverPropertyInfo[0];


    /**
     * The Test driver major version
     */
    private static final int TEST_MAJOR_VERSION = 1;


    /**
     * The number of connections created
     */
    private int _numberOfCreatedConnections = 0;


    /**
     * The Test driver minor version
     */
    private static final int TEST_MINOR_VERSION = 0;

    /**
     * The last connection returned
     */
    private TestConnectionImpl _lastConnection;


    static {
        try {
            DriverManager.registerDriver(new TestDriverImpl());
        }
        catch(Exception e) {
            System.out.println("Failed to register TestDriverImpl.");
            e.printStackTrace();
        }
    }


    /**
     * The default constructor
     */
    public TestDriverImpl()
    {

    }

    /**
     * Return a <code>Connection</code> object that represents a
     *      connection to the URL.
     *
     * @param url the URL of the database to which to connect
     * @param info a list of arbitrary string tag/value pairs as
     * connection arguments. 
     * @return a <code>Connection</code> object that represents a
	 *         connection to the URL
     * @exception SQLException if a database access error occurs
     */
    public Connection connect(String url, Properties info)
        throws SQLException
    {
        TestConnectionImpl connection = new TestConnectionImpl(url, info);
        ++_numberOfCreatedConnections;
        _lastConnection = connection;
        return connection;
    }


    /**
     * Return the last connection that was created.
     * Can be null if no connections were created.
     *
     * @return the last connection that was created.
     */
    public TestConnectionImpl getLastConnection()
    {
        return _lastConnection;
    }


    /**
     * Return the number of connections created by 
     * this driver instance.
     *
     * @return the number of connections created by
     * this driver instance.
     */
    public int getNumberOfCreatedConnections()
    {
        return _numberOfCreatedConnections;
    }


    /**
     * Clear the number of connections created by
     * this driver instance.
     */
    public void clearNumberOfCreatedConnections()
    {
        _numberOfCreatedConnections = 0;
    }

    /**
     * Returns true if the driver thinks that it can open a connection
     * to the given URL.
     *
     * @param url the URL of the database
     * @return true if this driver can connect to the given URL  
     * @exception SQLException if a database access error occurs
     */
    public boolean acceptsURL(String url) 
        throws SQLException
    {
        return null == url ? false : url.startsWith(TEST_URL_START);
    }


    /**
	 * Return an empty DriverPropertyInfo array.
     *
     * @param url the URL of the database to which to connect
     * @param info a proposed list of tag/value pairs that will be sent on
     *          connect open
     * @return an empty DriverPropertyInfo array.
     * @exception SQLException if a database access error occurs
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			 throws SQLException
    {
        return emptyDriverPropertyInfo;
    }


    /**
     * Gets the driver's major version number. 
     *
	 * @return this driver's major version number
     */
    public int getMajorVersion()
    {
        return TEST_MAJOR_VERSION;
    }

    /**
     * Gets the driver's minor version number. 
     *
	 * @return this driver's minor version number
     */
    public int getMinorVersion()
    {
        return TEST_MINOR_VERSION;
    }


    /**
     * Test driver is not jdbc compliant (not yet at least :-)).
     *
     * @return false;
     */
    public boolean jdbcCompliant()
    {
        return false;
    }
} 

