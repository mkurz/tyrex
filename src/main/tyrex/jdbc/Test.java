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
 * $Id: Test.java,v 1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.jdbc;


import java.io.PrintWriter;
import java.sql.*;
import javax.sql.*;
import javax.transaction.*;
import javax.transaction.xa.*;
import tyrex.server.*;
import postgresql.PostgresqlDataSource;
import tyrex.util.Logger;
//import tyrex.jdbc.xa.EnabledDataSource;
import org.exolab.testing.Memory;
import org.exolab.testing.Timing;


/**
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/11 00:33:46 $
 */
public class Test
{

    public static void main( String args[] )
    {
	PrintWriter            writer;
	Memory                 memory;
	int                    test;
	boolean                debug;
	PostgresqlDataSource      xaDs;
	TransactionManager     tmManager;


	test = -1;
	if ( args.length > 0 ) {
	    if ( "perf".startsWith( args[ 0 ] ) )
		test = TEST_PERFORMANCE;
	    else if ( "integrity".startsWith( args[ 0 ] ) )
		test = TEST_INTEGRITY;
	    else if ( "tm".startsWith( args[ 0 ] ) )
		test = TEST_TM_TIMEOUT;
	    else if ( "db".startsWith( args[ 0 ] ) )
		test = TEST_DB_TIMEOUT;
	}
	if ( test == -1 ) {
	    System.out.println( "Usage: <test> [debug]\nWhere <test> is one of:\n" );
	    System.out.println( "  p[erf]      Performance test" );
	    System.out.println( "  i[ntegrity] Integrity test" );
	    System.out.println( "  tm          Transaction manager timeout" );
	    System.out.println( "  db          Database (resource manager) timeout" );
	    System.exit ( 1 );
	}
	writer = Logger.getLogger();
	memory = new Memory( "Memory consumption" );
	debug = ( args.length >= 2 && "debug".startsWith( args[ 1 ] ) );

	// Requires PostgreSQL JDBC driver!
	try {
	    Class.forName( "postgresql.Driver" );
	} catch ( ClassNotFoundException except ) {
	    System.out.println( "You must install the PostgreSQL JDBC 2 driver" );
	    System.exit( 1 );
	}

	xaDs = new PostgresqlDataSource();
	//xaDs.setDriverName( "jdbc:postgresql" );
	xaDs.setDatabaseName( "test2" );
	xaDs.setUser( "test" );
	xaDs.setPassword( "" );

	Configure config;

	config = new Configure();
	if ( debug )
	    config.setLogWriter( writer );
	config.getPoolManager().setCheckEvery( 1 );
	config.startServer();

	tmManager = Tyrex.getTransactionManager();

	try
	{
	    Timing                     timing;
	    Transaction                tx;
	    Connection                 conn;
	    Statement                  stmt;
	    ResultSet                  rs;
	    int                        counter;
	    ServerDataSource           ds;


	    if ( test == TEST_PERFORMANCE )
		counter = 1000;
	    else
		counter = 2;

	    if ( test == TEST_DB_TIMEOUT )
		xaDs.setTransactionTimeout( 1 );
	    ds = new ServerDataSource();
	    ds.setDataSource( (XADataSource) xaDs );
	    if ( debug ) {
		xaDs.setLogWriter( new Logger( System.out ).setPrefix( "JDBC" ) );
		ds.setLogWriter( new Logger( System.out ).setPrefix( "JDBC Pool" ) );
	    }
	    memory.initial();


	    System.out.println( "[Transaction manager: commit]" );
	    timing = new Timing( "Transaction manager performance: commit" );
	    timing.start();
	    for ( int i = 0 ; i < counter ; ++i ) {
		conn = ds.getConnection();
		tmManager.begin();

		if ( test == TEST_TM_TIMEOUT || test == TEST_DB_TIMEOUT ) {
		    // Make sure the thread does not die, we want to try
		    // using the closed connection.
		    config.setThreadTerminate( false );

		    if ( test == TEST_TM_TIMEOUT )
			tmManager.setTransactionTimeout( 1 );
		    if ( debug )
			Tyrex.dumpTransactionList( writer );
		    try {
			Thread.sleep( 5000 );
		    } catch ( InterruptedException except ) { }
		}
		stmt = conn.createStatement();
		stmt.executeUpdate( "update test set text='original' where id=1" );
		tmManager.commit();
		conn.close();
	    }
	    timing.stop();
	    timing.count( counter );
	    System.out.println( timing.report() );


	    if ( test == TEST_INTEGRITY ) {
		System.out.println( "[Transaction manager: rollback]" );
		for ( int i = 0 ; i < counter ; ++i ) {
		    tmManager.begin();
		    conn = ds.getConnection();
		    stmt = conn.createStatement();
		    stmt.executeUpdate( "update test set text='never-seen' where id=1" );
		    tmManager.rollback();
		    conn.close();
		}

		conn = ds.getConnection();
		stmt = conn.createStatement();
		rs = stmt.executeQuery( "select text from test where id=1" );
		rs.next();
		System.out.println( "Value is: " + rs.getString( 1 ) );
		conn.close();
	    }


	    if ( test == TEST_PERFORMANCE ) {
		System.out.println( "[JDBC performance: commit]" );
		timing = new Timing( "JDBC performance: commit" );
		timing.start();
		for ( int i = 0 ; i < counter ; ++i ) {
		    conn = ds.getConnection();
		    conn.setAutoCommit( false );
		    stmt = conn.createStatement();
		    stmt.executeUpdate( "update test set text='modified' where id=1" );
		    conn.commit();
		    conn.close();
		}
		timing.stop();
		timing.count( counter );
		System.out.println( timing.report() );
	    }


	    memory.peak();
	}
	catch ( Exception except )
	{
	    System.out.println( except );
	    except.printStackTrace();
	}
	System.out.println( "[Test end]" );
	memory.claimed();

	if ( debug ) {
	    Thread.currentThread().getThreadGroup().list();
	    Tyrex.dumpTransactionList( writer );
	    Tyrex.dumpCurrentTransaction( writer );
	    xaDs.debug( xaDs.getLogWriter() );
	}
	System.out.println( memory.report() );
	System.exit( 0 );
    }


    /**
     * Perform performance test comparing transaction per minute with and
     * without the transaction monitor.
     */
    public static final int TEST_PERFORMANCE = 0;


    /**
     * Perform test for transaction manager timeout. The first attempt to
     * execute a statement will kill the thread with a transaction
     * timeout exception and rollback the transaction.
     */
    public static final int TEST_TM_TIMEOUT = 1;


    /**
     * Perform test for resource manager (JDBC) timeout. The first
     * attempt to execute a statement will kill the connection and fail
     * at the commit stage.
     */
    public static final int TEST_DB_TIMEOUT = 2;


    /**
     * Perform a transactional integrity check. The first run updates
     * the table value to 'original', the second run updates it back to
     * 'never-seen'. The query result should show 'original'.
     */
    public static final int TEST_INTEGRITY = 3;


}






