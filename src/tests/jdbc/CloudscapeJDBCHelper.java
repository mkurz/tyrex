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


package jdbc;

import COM.cloudscape.core.DataSourceFactory;
import COM.cloudscape.core.XaDataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.XADataSource;

///////////////////////////////////////////////////////////////////////////////
// CloudscapeJDBCHelper
///////////////////////////////////////////////////////////////////////////////

/**
 * 
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
final class CloudscapeJDBCHelper
    extends JDBCHelper
{

    private static final String[] dataSources = {};

    static {
        try {
            DriverManager.registerDriver((Driver)Class.forName("COM.cloudscape.core.JDBCDriver").newInstance());
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Called before a test case is executed.
     * <P>
     * The default implementation is to drop the
     * existing tables and recreate them.
     */
    public void preExecute()
    {

        Connection connection;

        // create the databases if necessary
        try {
            for ( int i = dataSources.length; --i >= 0; ) {
                connection = DriverManager.getConnection("jdbc:cloudscape:" + dataSources[i] + ";create=true", "system", "manager");
    
                try {
                    connection.close();
                } catch(SQLException e) {
                }        
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException( e.toString() );
        }

        super.preExecute();
    }


    /**
     * Create the XA data source used to get connections
     * to the database. The XA data source is not cached.
     *
     * @param dataSourceIndex the dataSourceIndex of the data source to return
     * @return the XA data source used to get connections
     *      to the database.
     * @throws SQLException if there is a problem getting
     *      the XA data source
     */
    public XADataSource createXADataSource( int dataSourceIndex )
        throws SQLException
    {
        try {
            // get the xa data source
            XADataSource dataSource = DataSourceFactory.getXADataSource();
            // set the database name
            ((XaDataSource)dataSource).setDatabaseName(dataSources[ dataSourceIndex ] );
            return dataSource;
        } catch( Exception e ) {
            if ( e instanceof SQLException ) {
                throw ( SQLException )e;
            }
            throw new SQLException( e.toString() );
        }
    }


    /**
     * Return false. Cloudscape cannot test one-phase optimization.
     *
     * @param xaDataSourceIndex the index of the xa data source
     * @return false. Cloudscape cannot test one-phase optimization.
     */
    /*public boolean canTestOnePhaseCommitOptimization( int xaDataSourceIndex )
    {
        return false;
    }*/


    /**
     * Return the printed representation of the helper.
     *
     * @return the printed representation of the helper.
     */
    public String toString()
    {
        return "Cloudscape";
    }

    /**
     * Return the number of data sources that the
     * JDBCHelper can return
     */
    public int getNumberOfXADataSources()
    {
        return dataSources.length;
    }

    /**
     * Cloudscape delisted xa resources cannot be reused
     * before the transaction
     * has been committed or rolledback. This includes both
     * new transactions and the existing transaction (lame).
     *
     * @return false
     */
    public boolean canReuseDelistedXAResources()
    {
        return false;
    }

    /**
     * Return the number of tables that are available
     * from the specified data source
     *
     * @param dataSourceIndex the dataSourceIndex of the data source
     * @return the number of tables that are available
     * from the specified data source.
     */
    public int getNumberOfTables( int dataSourceIndex )
    {
        return 1;
    }



    public static void main (String args[]) 
        throws Exception
    {
        XADataSource dataSource;
        String userName;
        String password;
        javax.sql.XAConnection xaConnection;
        javax.transaction.xa.XAResource xaResource;
        CloudscapeJDBCHelper helper;
        java.sql.Connection connection;
        String key;

        helper = new CloudscapeJDBCHelper();

        helper.preExecute();
        

        //java.sql.DriverManager.setLogWriter(new java.io.PrintWriter(System.out, true));
        // get the user name and password
        userName = helper.getUserName( 0 );
        password = helper.getPassword( 0 );
        // get the xa data source
        dataSource = helper.getXADataSource( 0 );
        System.out.println("dataSource " + dataSource);
        // get the xa connection
        //xaConnection = dataSource.getXAConnection();
        xaConnection = ( ( null == userName ) && ( null == password ) )
                        ? dataSource.getXAConnection()
                        : dataSource.getXAConnection( userName, password );
        System.out.println("xaConnection " + xaConnection);
        xaResource = xaConnection.getXAResource();
        System.out.println("xaResource " + xaResource);
                        
        connection = xaConnection.getConnection();
        // turn off auto commit
        connection.setAutoCommit( false );

        key = helper.generateKey( 0, 0 );

        try {
            // insert key and value
            helper.insertSQL( 0, connection, 0, key, helper.generateValue( 0, 0, key ) );
            System.out.println("result " + helper.readSQL( 0, connection, 0, key ) );
        } finally {
            try {connection.close();}catch(Exception e){}
        }
    }
}

