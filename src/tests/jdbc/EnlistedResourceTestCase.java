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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import org.exolab.exceptions.CWClassConstructorException;
import org.exolab.jtf.CWTestCase;
import org.exolab.jtf.CWVerboseStream;
import org.exolab.testing.Timing;
import tyrex.jdbc.ServerDataSource;
import tyrex.tm.EnlistedResource;
import tyrex.tm.Tyrex;
import tyrex.tm.TyrexTransaction;
import tyrex.tm.TyrexTransactionManager;


///////////////////////////////////////////////////////////////////////////////
// EnlistedResourceTestCase
///////////////////////////////////////////////////////////////////////////////

/**
 * Performs various tests with transactions using 
 * {@link tyrex.jdbc.EnlistedResource} and 
 * {@link tyrex.jdbc.SeverDataSource}
 * <UL>
 * </UL>
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
class EnlistedResourceTestCase
    extends AbstractTestCase
{
    /**
     * The default table index
     */
    private static final int DEFAULT_TABLE_INDEX = 0;
    

    /**
     * Create the EnlistedResourceTestCase with
     * the specified helper.
     *
     * @param name the name of the test case
     * @param helper the helper
     * @see JDBCHelper
     */
    EnlistedResourceTestCase( String name, JDBCHelper helper )
        throws CWClassConstructorException
    {
        super( name, "EnlistedResourceTestCase", helper );
    }

    public boolean run( CWVerboseStream stream )
    {
        TransactionManager transactionManager;
        Entry[] entries = null;
        boolean multipleEntries;
        
        try {
            
            stream.writeVerbose( "Test enlisted resource transaction actions " + 
                                 helper.toString() );
            
            try {
                stream.writeVerbose( "Creating data source entries " );
                entries = getEntries();

                if ( null == entries ) {
                    stream.writeVerbose( "Error: Failed to create entries" );
                    return false;        
                }

            } catch (Exception e) {
                stream.writeVerbose( "Error: Failed to create entries" );
                return false;
            }

            try {
                stream.writeVerbose( "Getting transaction manager " );
                // get the transaction manager
                transactionManager = Tyrex.getTransactionManager();
            } catch (Exception e) {
                stream.writeVerbose( "Error: Failed to get transaction manager " );
                return false;
            }

            try {
                if ( !testTransactionBoundary( transactionManager, entries, stream, true, true, true, 1, true ) ) {
                    stream.writeVerbose( "Error: Failed Enlisted Resource commit" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed Enlisted Resource commit" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testTransactionBoundary( transactionManager, entries, stream, false, false, true, 1, true ) ) {
                    stream.writeVerbose( "Error: Failed Enlisted Resource rollback" );
                    return false;
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed Enlisted Resource rollback" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testTransactionBoundary( transactionManager, entries, stream, false, true, false, 4, true ) ) {
                    stream.writeVerbose( "Error: Failed Enlisted Resource rollback" );
                    return false;
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed Enlisted Resource rollback" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testTransactionBoundary( transactionManager, entries, stream, false, false, false, 4, true ) ) {
                    stream.writeVerbose( "Error: Failed Enlisted Resource rollback" );
                    return false;
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed Enlisted Resource rollback" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testTransactionBoundary( transactionManager, entries, stream, false, true, false, 4, false ) ) {
                    stream.writeVerbose( "Error: Failed Enlisted Resource rollback" );
                    return false;
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed Enlisted Resource rollback" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testTransactionBoundary( transactionManager, entries, stream, false, false, false, 4, false ) ) {
                    stream.writeVerbose( "Error: Failed Enlisted Resource rollback" );
                    return false;
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed Enlisted Resource rollback" );
                stream.writeVerbose( e.toString() );
                return false;
            }
            
            return true;
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( null != entries) {
                for ( int i = 0; i < entries.length; ++i ) {
                    try {
                        closeConnection( entries[ i ].connection );
                    } catch ( Exception e ) {
                    }
                    try {
                        entries[i].serverDataSource.close();
                    } catch ( Exception e ) {
                    }
                }
            }
        }

        return false;
    }

    /**
     *  Commit or rollback the current transaction.
     *
     * @param transactionManager the transaction manager
     * @param stream logging stream
     * @param commit True if the current transaction is to
     *      be committed. False if the current transaction
     *      is to be rolled back
     * @return True if the operation was successful. Return false
     *      if the current thread is still associated
     */
    private boolean transactionBoundary( TransactionManager transactionManager,
                                         Entry[] entries,
                                         CWVerboseStream stream,
                                         boolean commit )
        throws Exception
    {
        Entry entry;
        int i;

        // make sure all the connections are enlisted
        for ( i = 0; i < entries.length; i++ ) {
            entry = entries[ i ];        

            if ( null == entry.connection ) {
                stream.writeVerbose( "The data source at index " + i + " does not have a connection." );
                return false;
            }

            if ( !( entry.connection instanceof EnlistedResource ) ) {
                stream.writeVerbose( "The data source at index " + i + " did not produce an enlisted resource." );
                return false;
            }

            if ( !( ( EnlistedResource ) entry.connection ).isEnlisted() ) {
                stream.writeVerbose( "The enlisted resource for data source at index " + i + " is not enlisted before transaction boundary." );    
            }
        }

        if ( commit ) {
            transactionManager.commit();    
        } else {
            transactionManager.rollback();
        }
        
        // make sure all the connections are enlisted
        for ( i = 0; i < entries.length; i++ ) {
            entry = entries[ i ];        

            if ( ( ( EnlistedResource ) entry.connection ).isEnlisted() ) {
                stream.writeVerbose( "The enlisted resource for data source at index " + i + " is still enlisted after transaction boundary." );    
            }

            entry.connection = null;
        }
        
        if ( null != transactionManager.getTransaction() ) {
            stream.writeVerbose( "Thread still associated with transaction after transaction boundary ");
            return false;
        }

        return true;
    }
    

    /**
     * Return false if the value in the database does not match 
     * the value in the specified entries. Return true otherwise.
     *
     * @param entries the entries
     * @param stream the logging stream used to log information
     * @return true if the value in the database matches
     *      the value in the specified entries. Return false 
     *      otherwise.
     * @throws IOException if there is a problem writing to
     *      the logging stream
     * @throws SQLException if there is a problem checking the 
     *      value in the data source
     * @see Entry
     */
    private boolean checkValues(Entry[] entries, CWVerboseStream stream)
        throws SQLException, IOException
    {
        Entry entry;
        boolean matched = true;

        for ( int i = 0; i < entries.length; ++i ) {
            entry = entries[ i ];
            
            if ( !checkValue( i, entry, DEFAULT_TABLE_INDEX, entry.key, entry.value, stream ) ) {
                matched = false;                
            }
        }

        return matched;
    }

    /**
     * Return false if the value in the database does not match 
     * the specified value. Return true otherwise.
     *
     * @param dataSourceIndex the dataSourceIndex of the data source
     * @param entry the entry
     * @param tableIndex the index of the table
     *      to read data from.
     * @param key the key
     * @param value the value
     * @param stream the logging stream used to log information
     * @return true if the value in the database matches
     *      the specified value. Return false 
     *      otherwise.
     * @throws IOException if there is a problem writing to
     *      the logging stream
     * @throws SQLException if there is a problem checking the 
     *      value in the data source
     * @see Entry
     */
    private boolean checkValue(int dataSourceIndex, 
                               Entry entry, 
                               int tableIndex,
                               String key, 
                               String value, 
                               CWVerboseStream stream)
        throws SQLException, IOException
    {
        Connection connection = getConnection( entry );

        try {
            if ( !helper.checkValue( dataSourceIndex, connection, tableIndex, key, value ) ) {
                stream.writeVerbose( "Values don't match for data source " + dataSourceIndex );
                return false;    
            }
        } finally {
            if ( connection != entry.connection) {
                closeConnection( connection );    
            }
        }
    
        return true;
    }

    
    /**
     * Update the datasource using
     * the specified entry and value
     *
     * @param transactionManager the transaction manager used
     *      to enlist the xa resource from the entry
     * @param dataSourceIndex the dataSourceIndex of the data source in the helper
     * @param entry the entry
     * @param value the new value.
     * @param stream the logging stream
     * @param closeConnection true if the connection is closed 
     *      upon exit of the method.
     * @return true if the update happened. Return false otherwise.
     * @throws Exception if there is a problem updating
     * @see Entry
     */
    private boolean update( TransactionManager transactionManager,
                             int dataSourceIndex,
                             Entry entry, 
                             String value, 
                             CWVerboseStream stream,
                            boolean closeConnection )
        throws Exception
    {
        return update( transactionManager, dataSourceIndex, 
                       entry, 
                       DEFAULT_TABLE_INDEX, entry.key, value, stream,
                       closeConnection );
    }


    /**
     * Set the connection for the specified entry.
     *
     * @param entry the entry
     * @see Entry
     */
    private void setEntryConnection( Entry entry )
        throws Exception
    {
        if ( ( null == entry.connection ) ||
                 entry.connection.isClosed() ) {
                // set the connection
                entry.connection = getConnection( entry );
            }
    }

    /**
     * Update the data source using
     * the specified key and value.
     * The XA resource is enlisted in the current
     * transaction before the data source is changed.
     *
     * @param transactionManager the transaction manager
     * @param dataSourceIndex the dataSourceIndex of the data source in the helper
     * @param entry the entry
     * @param tableIndex the index of the table
     *      to read data from.
     * @param key the key. Assumed to be already existing
     *      in data source.
     * @param value the new value.
     * @param stream the logging stream
     * @param closeConnection true if the connection is closed 
     *      upon exit of the method.
     * @return true if the update happened. Return false otherwise.
     * @throws Exception if there is a problem updating
     * @see Entry
     */
    private boolean update( TransactionManager transactionManager,
                             int dataSourceIndex,
                             Entry entry,
                             int tableIndex,
                             String key, 
                             String value, 
                             CWVerboseStream stream,
                            boolean closeConnection )
        throws Exception
    {
        try {
            setEntryConnection( entry );
            // update value
            helper.updateSQL( dataSourceIndex, entry.connection, tableIndex, key, value );
            
            // make sure the value changed
            if ( !helper.checkValue( dataSourceIndex, entry.connection, tableIndex, key, value ) ) {
                stream.writeVerbose( "Update failed for table index " + 
                                     tableIndex + 
                                     " in data source index " + 
                                     dataSourceIndex );
                return false;    
            }
            
            return true;
        } finally {
            if ( closeConnection) {
                closeConnection( entry.connection );        
            }
        }
    }

    
    /**
     * Close the specified connection.
     *
     * @param connection the connection
     */
    private void closeConnection( Connection connection )
    {
        try {
            if ( ( null != connection ) &&
                !connection.isClosed() ) {
                connection.close();
            }
        } catch ( SQLException e) {
        }
    }


    /**
     * Get the connection from the specified XA connection.
     *
     * @param entry the entry.
     * @return the connection from the specified XA connection.
     * @throws SQLException if there is a problem geting the connection
     * @see Entry
     */
    private Connection getConnection( Entry entry )
        throws SQLException
    {
        return ( ( null == entry.connection ) ||
                 entry.connection.isClosed() )
                ? entry.serverDataSource.getConnection( entry.userName, entry.password )
                : entry.connection;
    }
    

    /**
     * Insert the specified key and value in the
     * data source using the specified xa connection.
     * The XA resource is enlisted in the current
     * transaction before the data source is changed.
     *
     * @param transactionManager the transaction manager
     * @param dataSourceIndex the dataSourceIndex of the data source in the helper
     * @param entry the Entry
     * @param value the new value.
     * @param stream the logging stream
     * @param closeConnection true if the connection is closed 
     *      upon exit of the method.
     * @return true if the update happened. Return false otherwise.
     * @throws Exception if there is a problem updating
     * @see Entry
     * @see #DEFAULT_TABLE_INDEX
     */
    private boolean insert( TransactionManager transactionManager,
                            int dataSourceIndex,
                            Entry entry,
                            String value,
                            CWVerboseStream stream,
                            boolean closeConnection )
        throws Exception
    {
        setEntryConnection( entry );

        try {
            // insert key and value
            helper.insertSQL( dataSourceIndex, entry.connection, DEFAULT_TABLE_INDEX, entry.key, value );

            // make sure the value changed
            if ( !helper.checkValue( dataSourceIndex, entry.connection, DEFAULT_TABLE_INDEX, entry.key, value ) ) {
                stream.writeVerbose( "Insert failed for table index " + 
                                     DEFAULT_TABLE_INDEX + 
                                     " in data source index " + 
                                     dataSourceIndex );
                return false;    
            }

            return true;
        } finally {
            if ( closeConnection) {
                closeConnection( entry.connection );        
            }
        }
    }

    
    /**
     * Test transaction boundaries.
     *
     * @param transactionManager the transaction manager
     * @param entries the array of test entries
     * @param stream the logging stream
     * @param commitMode the commit mode.
     * @param insert True if data is to be inserted. False is data
     *      is to be updated.
     * @param commit True if the transaction is committed. False if the
     *      transaction is rolled back.
     * @param closeConnection true if the connection is closed 
     *      upon exit of the method.
     * @param numberOfTransactions the number of tranasactions that 
     *      take place in the same thread.
     * @param sameThread true if the transactions take place in the same thread
     * @return True if the test was successful. Return false otherwise.
     * @throws Exception if there is an error with the test.
     * @see Entry
     */
    private boolean testTransactionBoundary( final TransactionManager transactionManager, 
                                             final Entry[] entries,
                                             final CWVerboseStream stream,
                                             final boolean insert,
                                             final boolean commit,
                                             final boolean closeConnection,
                                             int numberOfTransactions,
                                             final boolean sameThread )
        throws Exception
    {
        Runnable runnable;
        int j = 0;
        int k;
        Entry entry;
        Connection connection;
        int actualNumberOfTransactions = Math.max( 1, numberOfTransactions );
        final Object[] result = new Object[]{ Boolean.TRUE };
        final Object lock = new Object();
        
        stream.writeVerbose( "Test Enlisted Resource " + 
                             ( commit ? "commit " : "rollback " ) +
                             ( ( actualNumberOfTransactions > 1 )
                               ?    actualNumberOfTransactions + 
                                    " times " + 
                                    ( sameThread 
                                      ? "in a single thread" 
                                      : "with each transaction in a different thread" )
                               : "" ) );

        while ( j < actualNumberOfTransactions ) {

            if ( insert ) {
                // make sure the keys dont not exist
                for ( k = 0; k < entries.length; ++k ) {
                    entry = entries[ k ];
                    connection = getConnection( entry );
                    try {
                        if ( !helper.checkValue( k, connection, DEFAULT_TABLE_INDEX, entry.key, null ) ) {
                            stream.writeVerbose( "Key already exists for data source " + k );
                            return false;    
                        }    
                    } finally {
                        closeConnection(connection);
                    }
                }
            }
            
            runnable = new Runnable() 
                {
                    public void run() 
                    {
                        synchronized (lock) {
                            try {

                                Entry runnableEntry;
                                String value;
        
                                transactionManager.begin();
            
                                for ( int i = 0; i < entries.length; ++i ) {
                                    runnableEntry = entries[ i ];
                        
                                    value = runnableEntry.value + "new";
                        
                                    if ( commit ) {
                                        runnableEntry.value = value;    
                                    }
                        
                                    if ( ( insert && !insert( transactionManager, i,
                                                              runnableEntry, value, stream, closeConnection) ) ||
                                         ( !insert && !update( transactionManager, i, runnableEntry, value, stream, closeConnection ) ) ) {
                                        result[ 0 ] = Boolean.FALSE;
                                        return;
                                    }
                                }
                        
                                if ( !transactionBoundary( transactionManager, entries, stream, commit ) ) {
                                    result[ 0 ] = Boolean.FALSE;
                                    return;
                                }
                                
                                /*
                                if ( !checkValues(entries, stream) ) {
                                    result[ 0 ] = Boolean.FALSE;
                                    return;
                                }
                                */
                                
                                if ( !sameThread ) {
                                    ( ( TyrexTransactionManager ) transactionManager ).recycleThread();    
                                }

                            } catch ( Exception e ) {
                                result[ 0 ] = e;
                            } finally {
                                lock.notify();
                            }
                        }
                    }
                };
            
            if ( sameThread || ( 0 == j ) ) {
                runnable.run();   

                if ( result[ 0 ] == Boolean.FALSE ) {
                    return false;    
                }
                if ( result[ 0 ] != Boolean.TRUE ) {
                    throw ( Exception )result[ 0 ];
                }
            } else {
                synchronized (lock) {
                    new Thread( runnable ).start();
                    lock.wait();
                }
                
            }

            ++j;

            if ( insert && 
                 commit && 
                 ( j < actualNumberOfTransactions ) ) {
                // make new keys
                for ( k = 0; k < entries.length; ++k ) {
                    entries[ k ].key += "new";
                }
            }
        }

        if ( !closeConnection ) {
            for ( j = 0; j < entries.length; ++j ) {
                closeConnection( entries[ j ].connection );
            }
        }

        return true;
    }


    /**
     * Return the array of entries to be used
     * in testing.
     *
     * @return the array of entries to be used
     *      in testing.
     * @throws SQLException if there is a problem getting the 
     *      XA data sources, connections and/or resources.
     * @see Entry
     */
    private Entry[] getEntries()
        throws SQLException
    {
        // get the number of data sources available
        int numberOfDataSources = helper.getNumberOfXADataSources();
        // make the array
        Entry[] entries = new Entry[numberOfDataSources];
        String key;
        int entryIndex = 0;
        

        //java.sql.DriverManager.setLogWriter(new java.io.PrintWriter(System.out, true));
        // populate it
        for ( int xaDataSourceIndex = 0; xaDataSourceIndex < numberOfDataSources; ++xaDataSourceIndex ) {
            if ( helper.getNumberOfTables( xaDataSourceIndex ) <= 0 ) {
                continue;    
            }

            key = helper.generateKey( xaDataSourceIndex, DEFAULT_TABLE_INDEX );
            entries[ entryIndex++ ] = new Entry( new ServerDataSource( helper.getXADataSource( xaDataSourceIndex ) ), 
                                                 helper.getUserName( xaDataSourceIndex ),
                                                 helper.getPassword( xaDataSourceIndex ),
                                                 key,
                                                 helper.generateValue( xaDataSourceIndex, DEFAULT_TABLE_INDEX, key ));
        }

        if ( entryIndex != numberOfDataSources ) {
            if ( 0 == entryIndex ) {
                return null;    
            }

            Entry[] temp = new Entry[ entryIndex ];

            System.arraycopy( entries, 0, temp, 0, entryIndex );

            entries = temp;

            temp = null;
        }

        return entries;
    }

    /**
     * Object that collects all the data
     * necessary to test a particular data source.
     */
    private static final class Entry
    {
        /**
         * The server data source
         */
        private final ServerDataSource serverDataSource;


        /**
         * The user name
         */
        private final String userName;


        /**
         * The password
         */
        private final String password;

        /**
         * The key
         */
        private String key;


        /**
         * The value stored with the key
         */
        private String value;


        /**
         * The current connection being used
         */
        private Connection connection;

        /**
         * Create the Entry with the specified
         * arguments.
         *
         * @param serverDataSource the server data source
         * @param userName the user name
         * @param password the password
         * @param key the primary key
         * @param value the value
         */
        private Entry( ServerDataSource serverDataSource,
                       String userName,
                       String password,
                       String key,
                       String value )
        {
            this.serverDataSource = serverDataSource;
            this.userName = userName;
            this.password = password;
            this.key = key;
            this.value = value;
        }
    }
}

