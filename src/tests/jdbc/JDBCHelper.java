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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

///////////////////////////////////////////////////////////////////////////////
// JDBCHelper
///////////////////////////////////////////////////////////////////////////////

/**
 * This class is used to decouple database details
 * from test cases.
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
public abstract class JDBCHelper
{
    /**
     * The default value stored in the database
     */
    protected final static String DEFAULT_VALUE = "Riad was here";

    /**
     * The array of table names
     */
    private String[] tables = null;

    /**
     * Counter to ensure that keys are unique
     */
    private int counter = 0;


    /**
     * The array of data sources
     */
    private XADataSource[] xaDataSources = null;


    /**
     * The two-phase commit transaction type
     *
     * @see #getMinimumNumberOfTransactionsPerMinute
     */
    public static final int TWO_PHASE_COMMIT = 1;


    /**
     * The one-phase commit transaction type
     *
     * @see #getMinimumNumberOfTransactionsPerMinute
     */
    public static final int ONE_PHASE_COMMIT = 2;


    /**
     * The rollback transaction type
     *
     * @see #getMinimumNumberOfTransactionsPerMinute
     */
    public static final int ROLLBACK = 2;
    
    /**
     * Default constructor
     */
    public JDBCHelper()
    {
    }

    /**
     * Called before a test case is executed.
     * <P>
     * The default implementation is to drop the
     * existing tables and recreate them.
     */
    public void preExecute()
    {
        dropAndCreateTables( true );
    }

    /**
     * Called after a test case is executed.
     * <P>
     * The default implementation is to drop the
     * created tables.
     */
    public void postExecute()
    {
        try {
            dropAndCreateTables( false );
        } catch ( RuntimeException e ) {
            System.out.println("Cleanup failed");
            e.printStackTrace();
        }
    }

    /**
     * Return the printed representation of the helper.
     *
     * @return the printed representation of the helper.
     */
    public abstract String toString();


    /**
     * Return true if the helper can test performance.
     * <P>
     * The default is true.
     *
     * @return true if the helper can test performance.
     */
    public boolean canTestPerformance()
    {
        return true;
    }


    /**
     * Drop and create the tables for the data sources.
     *
     * @param create True if the tables are to be created
     */
    private void dropAndCreateTables( boolean create )
    {
        XAConnection xaConnection = null;
        Connection  connection = null;
        Statement statement = null;
        int tableIndex;
        String tableName;

        try {
            for ( int xaDataSourceIndex = getNumberOfXADataSources(); --xaDataSourceIndex >= 0; ) {
                xaConnection = getXAConnection( xaDataSourceIndex );
                connection = xaConnection.getConnection();
                /*if ( !connection.getAutoCommit() ) {
                    try {
                        connection.setAutoCommit(true);
                    } catch ( SQLException e ) {
                    }
                }*/
                statement = connection.createStatement();

                for ( tableIndex = getNumberOfTables( xaDataSourceIndex ); --tableIndex >= 0; ) {
                    // get the table name
                    tableName = getTableName( xaDataSourceIndex, tableIndex );

                    // drop the table
                    try {
                        statement.execute( "drop table " + tableName);
                    }
                    catch( SQLException e ) {
                        //System.out.println( "Failed to drop table " + 
                        //                    tableName + " - " +
                        //                    ((null != e.getNextException()) ? e.getNextException().toString() : e.toString()) );
                    }

                    if ( create ) {
                        statement.execute( "create table " + tableName + 
                                           " (" + getPrimaryKeyColumnName( xaDataSourceIndex , tableIndex ) + 
                                           " varchar (255) primary key, " + 
                                           getValueColumnName( xaDataSourceIndex, tableIndex ) + 
                                           " varchar (255))" );        
                    }
                }

                if ( null != statement ) {
                    try { statement.close(); } catch ( Exception e ){}
                }
    
                if ( null != connection ) {
                    try { connection.close(); } catch ( Exception e ){}
                }
    
                if ( null != xaConnection ) {
                    try { xaConnection.close(); } catch ( Exception e ){}
                }
            }
        } catch ( Exception e ) {
            if ( e instanceof RuntimeException ) {
                throw (RuntimeException)e;    
            }

            e.printStackTrace();

            throw new RuntimeException ( e.toString() );
        } finally {
            if ( null != statement ) {
                try { statement.close(); } catch ( Exception e ){}
            }

            if ( null != connection ) {
                try { connection.close(); } catch ( Exception e ){}
            }

            if ( null != xaConnection ) {
                try { xaConnection.close(); } catch ( Exception e ){}
            }
        }        
    }


    /**
     * Return the minimum number of transactions 
     * per minute 
     *
     * @param type the transaction type
     * @return the minimum number of transactions
     * per minute
     * @see #TWO_PHASE_COMMIT
     * @see #ONE_PHASE_COMMIT
     * @see #ROLLBACK
     */
    public int getMinimumNumberOfTransactionsPerMinute( int transactionType)
    {
        return ( int )( ( ( TWO_PHASE_COMMIT == transactionType )
                          ? 800 
                          : ( ( ONE_PHASE_COMMIT == transactionType ) 
                              ? 1600
                              : 1600 ) ) / xaDataSources.length );
    }
    

    /**
     * Return the XA connection for the specified
     * data source.
     *
     * @param xaDataSourceIndex the xaDataSourceIndex of the 
     *      xa connection to return
     * @return the XA connection to the data source
     * @throws SQLException if there is a problem getting
     *      the XA connection.
     */
    public XAConnection getXAConnection( int xaDataSourceIndex )
        throws SQLException
    {
        return getXAConnection( getXADataSource( xaDataSourceIndex ), xaDataSourceIndex );
    }


    /**
     * Return the XA connection for the specified
     * data source.
     *
     * @param xaDataSource the xa data source
     * @param xaDataSourceIndex the xaDataSourceIndex of the 
     *      xa connection to return
     * @return the XA connection to the data source
     * @throws SQLException if there is a problem getting
     *      the XA connection.
     */
    public XAConnection getXAConnection( XADataSource xaDataSource, int xaDataSourceIndex )
        throws SQLException
    {
        String userName = getUserName( xaDataSourceIndex );
        
        return null == userName 
                ? xaDataSource.getXAConnection()
                : xaDataSource.getXAConnection( userName, getPassword( xaDataSourceIndex ) );
    }


    /**
     * Return the XA data source used to get connections
     * to the database. IF the XA data source does not 
     * exist it is created and cached.
     *
     * @param xaDataSourceIndex the xaDataSourceIndex of the data source to return
     * @return the XA data source used to get connections
     *      to the database.
     * @throws SQLException if there is a problem getting
     *      the XA data source
     */
    public synchronized final XADataSource getXADataSource( int xaDataSourceIndex )
        throws SQLException
    {
        XADataSource xaDataSource;
        
        if ( null == xaDataSources ) {
            int numberOfXADataSources = getNumberOfXADataSources();

            if ( 0 >= numberOfXADataSources ) {
                throw new NoSuchElementException( "Invalid number of XA data sources [" + 
                                                  numberOfXADataSources +
                                                  "]" );
            }

            xaDataSources = new XADataSource[ numberOfXADataSources ];
        }

        // get the table name
        xaDataSource = xaDataSources[ xaDataSourceIndex ];

        if ( null == xaDataSource ) {
            xaDataSource = createXADataSource( xaDataSourceIndex );
            xaDataSources[ xaDataSourceIndex ] = xaDataSource;
        }

        return xaDataSource;
    }

    /**
     * Return true if one phase commit optimization
     * can be tested on the specifed data source.
     *
     * @param xaDataSourceIndex the index of the xa data source
     * @return true if one phase commit optimization
     * can be tested.
     */
    //public abstract boolean canTestOnePhaseCommitOptimization( int xaDataSourceIndex );


    /**
     * Create the XA data source used to get connections
     * to the database. The XA data source is not cached.
     *
     * @param xaDataSourceIndex the xaDataSourceIndex of the data source to return
     * @return the XA data source used to get connections
     *      to the database.
     * @throws SQLException if there is a problem getting
     *      the XA data source
     */
    public abstract XADataSource createXADataSource( int xaDataSourceIndex )
        throws SQLException;


    /**
     * Return the number of data sources that the
     * JDBCHelper can return
     */
    public abstract int getNumberOfXADataSources();


    /**
     * Insert the specified primary key and value
     * into the database.
     *
     * @param xaDataSourceIndex the xaDataSourceIndex of the data source
     * @param connection the database connection.
     * @param tableIndex the xaDataSourceIndex of the table
     *      to read data from.
     * @param key the unique primary key. The primary
     *      does not exist in the table.
     * @param value the value to be stored
     * @throws SQLException if there is a problem inserting
     *      the data.
     */
    public void insertSQL( int xaDataSourceIndex,
                              Connection connection, 
                              int tableIndex,
                              String key, 
                              String value )
        throws SQLException
    {
        Statement stmt = null;
        
        try {
            stmt = connection.createStatement();
    
            stmt.executeUpdate("insert into " + 
                               getTableName( xaDataSourceIndex, tableIndex ) + 
                               " values ('" + key + "', '" + value + "')");
        } finally {
            if ( null != stmt ) {
                try {    
                    stmt.close();
                } catch ( SQLException e ) {
                }
            }
        }
    }


    /**
     * Return the name of the primary key column for the
     * specified table in the specified data source.
     * <P>
     * The default implementation returns "id".
     *
     * @param xaDataSourceIndex the index of the data source
     * @param tableIndex the index of the table
     * @return the name of the primary key column for the
     *      specified table
     */
    protected String getPrimaryKeyColumnName( int xaDataSourceIndex, int tableIndex )
    {
        return "id";
    }


    /**
     * Return the name of the primary key column for the
     * specified table in the specified data source.
     * <P>
     * The default implementation returns "value".
     *
     * @param xaDataSourceIndex the index of the data source
     * @param tableIndex the index of the table
     * @return the name of the primary key column for the
     *      specified table
     */
    protected String getValueColumnName( int xaDataSourceIndex, int tableIndex )
    {
        return "value";
    }


    /**
     * Update the specified primary key and value
     * into the database.
     *
     * @param xaDataSourceIndex the xaDataSourceIndex of the data source
     * @param connection the database connection.
     * @param tableIndex the index of the table
     *      to read data from.
     * @param key the primary key. The primary
     *      exists in the table.
     * @param value the value to be stored
     * @throws SQLException if there is a problem inserting
     *      the data.
     */
    public void updateSQL( int xaDataSourceIndex,
                              Connection connection,
                              int tableIndex,
                              String key, 
                              String value )
        throws SQLException
    {
        Statement stmt = null;
        
        try {
            stmt = connection.createStatement();
            if ( tableIndex > 0) {
                System.out.println("table " + getTableName( xaDataSourceIndex, tableIndex ));
            }
            stmt.executeUpdate("update " + getTableName( xaDataSourceIndex, tableIndex ) + 
                               " set " + getValueColumnName( xaDataSourceIndex, tableIndex ) + 
                               " = '" + value + "' where " + 
                               getPrimaryKeyColumnName( xaDataSourceIndex, tableIndex ) + 
                               " = '" + key + "'");
        } finally {
            if ( null != stmt ) {
                try {    
                    stmt.close();
                } catch ( SQLException e ) {
                }
            }
        }
    }
    

    /**
     * Return true if the value stored in the database
     * using the specified primary key is the same as
     * the specified value. Return false otherwise
     *
     * @param xaDataSourceIndex the xaDataSourceIndex of the data source
     * @param connection the database connection.
     * @param tableIndex the index of the table
     *      to read data from.
     * @param key the primary key
     * @param value the value. Can be null.
     * @return true if  the value stored in the database
     *      using the specified primary key is the same
     *      as the specified value. Return false otherwise..
     * @throws SQLException if there is a problem
     *      reading the data.
     */
    public boolean checkValue( int xaDataSourceIndex,
                               Connection connection,
                               int tableIndex,
                               String key,
                               String value )
        throws SQLException
    {
        String readValue = readSQL( xaDataSourceIndex, connection,
                                    tableIndex, key);
        return null == value ? null == readValue : value.equals( readValue );
    }

    /**
     * Return the value stored in the database
     * using the specified primary key. Return 
     * null if the key does not exist.
     *
     * @param xaDataSourceIndex the xaDataSourceIndex of the data source
     * @param connection the database connection.
     * @param tableIndex the index of the table
     *      to read data from.
     * @param key the primary key
     * @return the value stored in the database
     *      using the specified primary key.
     * @throws SQLException if there is a problem
     *      reading the data.
     */
    protected String readSQL( int xaDataSourceIndex,
                              Connection connection,
                              int tableIndex,
                              String key )
        throws SQLException
    {
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            stmt = connection.createStatement();
            resultSet = stmt.executeQuery("select " + getValueColumnName( xaDataSourceIndex, tableIndex ) +
                                          " from " + 
                                          getTableName( xaDataSourceIndex, tableIndex ) + 
                                          " where " + 
                                          getPrimaryKeyColumnName( xaDataSourceIndex, tableIndex ) + 
                                          " = '" + key + "'");
            if (resultSet.next()) {
                return resultSet.getString(1);    
                //String string = resultSet.getString(1);
                //System.out.println("found " + string);
                //return string;
            } else {
                //System.out.println("nothin");
                return null;
            }
        } finally {
            if ( null != resultSet ) {
                try {    
                    resultSet.close();
                } catch ( SQLException e ) {
                } 
            }
            if ( null != stmt ) {
                try {    
                    stmt.close();
                } catch ( SQLException e ) {
                }
            }
        }
    }
    
    /**
     * Return true if the XA resources can be
     * used in a new transaction when they've
     * been delisted using XAResource.TMSUCCESS in
     * an existed transaction that has not been 
     * committed or rolled back.
     * Return false otherwise.
     *
     * @return true if the XA resources can be
     *      used in a new transaction when they've
     *      been delisted using XAResource.TMSUCCESS.
     *      Return false otherwise.
     */
    public boolean canReuseDelistedXAResources()
    {
        return true;
    }


    /**
     * Return the number of tables that are available
     * from the specified data source.
     * <P>
     * The default implementation returns 1
     *
     * @param xaDataSourceIndex the xaDataSourceIndex of the data source
     * @return the number of tables that are available
     * from the specified data source.
     */
    public int getNumberOfTables( int xaDataSourceIndex )
    {
        return 1;
    }


    /**
     * Return the table name that corresponds to
     * the specified table index.
     * The default implementation returns "test"
     * appended with the table index if the number of 
     * tables is greater or equal to 1. Otherwise
     * a java.util.NoSuchElementException is thrown.
     *
     * @param xaDataSourceIndex the index of the data source
     * @param tableIndex the index of the table
     */
    public synchronized String getTableName( int xaDataSourceIndex, int tableIndex )
    {
        if ( null == tables ) {
            int i;
            int numberOfTables = Integer.MIN_VALUE;

            for ( i = getNumberOfXADataSources(); --i >= 0; ) {
                numberOfTables = Math.max( numberOfTables, getNumberOfTables( i ) );    
            }

            if ( 0 >= numberOfTables ) {
                throw new NoSuchElementException( "Invalid number of tables [" + 
                                                  numberOfTables +
                                                  "] for data source index " + 
                                                  xaDataSourceIndex );
            }

            tables = new String[ numberOfTables ];

            for ( i = 0; i < numberOfTables; ++i ) {
                tables[ i ] = "test" + i;    
            }
        }

        // return the table name
        return tables[ tableIndex ];
    }
    

    /**
     * Return a string that can be used a primary key in a 
     * database.
     *
     * @param xaDataSourceIndex the xaDataSourceIndex of the data source
     * @param tableIndex the xaDataSourceIndex of the table
     *      to read data from.
     * @return a string that can be used a primary key in a 
     *      database.
     */
    public String generateKey( int xaDataSourceIndex,
                                  int tableIndex )
    {
        return  Long.toString( System.currentTimeMillis() ) + 
                "-" + 
                xaDataSourceIndex +
                "-" + 
                tableIndex +
                "-" +
                ++counter;
    }


    /**
     * Return a string that is stored in a database with
     * the specified primary key.
     *
     * @param xaDataSourceIndex the xaDataSourceIndex of the data source
     * @param tableIndex the xaDataSourceIndex of the table
     *      to read data from.
     * @param key the primary key used to store the 
     *      generated value.
     * @return a string that stored in a database with
     *      the specified primary key.
     */
    public String generateValue( int xaDataSourceIndex,
                                    int tableIndex,
                                    String key )
    {
        return  DEFAULT_VALUE + 
                "-" + 
                xaDataSourceIndex +
                "-" +
                tableIndex;
    }


    /**
     * Return the user name used to get a connection to
     * the database. Return null if the user name is to
     * be ignored.
     *
     * @param xaDataSourceIndex the xaDataSourceIndex of the data source
     * @return the user name used to get a connection to
     *      the database.
     */
    public String getUserName( int xaDataSourceIndex )
    {
        return null;
    }


    /**
     * Return the password used to get a connection to
     * the database. Return null if the user name is to
     * be ignored.
     *
     * @param xaDataSourceIndex the xaDataSourceIndex of the data source
     * @return the password used to get a connection to
     *      the database.
     */
    public String getPassword( int xaDataSourceIndex )
    {
        return null;
    }
}
    
