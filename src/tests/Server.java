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
 * $Id: Server.java,v 1.5 2001/02/23 19:23:57 jdaniel Exp $
 */


package tests;


import java.io.PrintWriter;
import java.rmi.*;
import javax.transaction.*;
import tyrex.util.Logger;
import org.exolab.testing.Memory;
import org.exolab.testing.Timing;
import tyrex.server.*;
import tyrex.tm.*;


/**
 * Performs a simple test of starting and demarcating a transaction on
 * a remote server. This test runs the client and remote user transaction
 * in the same JVM, but employes RMI for remote communications between
 * the two.
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $ $Date: 2001/02/23 19:23:57 $
 */
public class Server
{


    public static void main( String args[] )
    {
	PrintWriter             writer;
	Memory                  memory;
	int                     test;
	boolean                 debug;
	TransactionManager      tmManager;

	test = -1;
	if ( args.length > 0 ) {
	    if ( "perf".startsWith( args[ 0 ] ) )
		test = TEST_PERFORMANCE;
	    else if ( "tm".startsWith( args[ 0 ] ) )
		test = TEST_TM_TIMEOUT;
	    else if ( "kt".startsWith( args[ 0 ] ) )
		test = TEST_KT_TIMEOUT;
	}
	if ( test == -1 ) {
	    System.out.println( "Usage: <test> [debug]\nWhere <test> is one of:\n" );
	    System.out.println( "  p[erf]      Performance test" );
	    System.out.println( "  tm          Transaction manager timeout" );
	    System.out.println( "  kt          Timeout - kill thread" );
	    System.exit ( 1 );
	}
	writer = new java.io.PrintWriter( System.out, true );
	memory = new Memory( "Memory consumption" );
	debug = ( args.length >= 2 && "debug".startsWith( args[ 1 ] ) );

	Configure config;

	config = new Configure();
	config.getResourceLimits().setCheckEvery( 1 );
	config.startServer();
	/*
	if ( debug && test == TEST_PERFORMANCE )
	    new Meter( new Logger( System.out ).setPrefix( "METER" ), 200 );
	*/

	tmManager = Tyrex.getTransactionManager();

	try {
	    Transaction                tx;

	    memory.initial();

	    System.out.println( "Creating transaction locally" );
	    tmManager.begin();
	    tx = tmManager.getTransaction();
	    System.out.println( "Transaction: " + tx.toString() );
	    System.out.println( "Commiting transaction" );
	    tmManager.commit();

	    System.out.println( "Creating transaction locally" );
	    tmManager.begin();
	    tx = tmManager.getTransaction();
	    System.out.println( "Transaction: " + tx.toString() );

	    if ( test == TEST_TM_TIMEOUT || test == TEST_KT_TIMEOUT ) {
		new SecondThread( tx, writer ).start();

		tmManager.setTransactionTimeout( 1 );
		if ( debug ) {
		    Tyrex.dumpTransactionList( writer );
		    Tyrex.dumpCurrentTransaction( writer );
		}
		if ( test == TEST_TM_TIMEOUT ) {

		    System.out.println( "Suspending from transaction" );
		    tmManager.suspend();
		    try {
			Thread.sleep( 2000 );
		    } catch ( Exception except ) { }
		    System.out.println( "Resuming transaction" );
		    tmManager.resume( tx );

		} else {

		    config.setThreadTerminate( true );
		    try {
			Thread.sleep( 2000 );
		    } catch ( InterruptedException except ) { }
		    
		}
	    }

	    System.out.println( "Aborting transaction" );
	    tmManager.rollback();

	    if ( test == TEST_PERFORMANCE ) {
		Timing timing;

		timing = new Timing( "Empty transaction performance" );
		timing.start();
		for ( int i = 0 ; i < 100000 ; ++i ) {
		    tmManager.begin();
		    tmManager.commit();
		}
		timing.stop();
		timing.count( 100000 );
		System.out.println( timing.report() );
	    }


	    memory.peak();
	    if ( debug ) {
		Tyrex.dumpTransactionList( writer );
		Tyrex.dumpCurrentTransaction( writer );
	    }
	} catch ( Exception except ) {
	    System.out.println( except );
	    except.printStackTrace();
	}
	System.out.println( "[Test end]" );
	if ( debug ) {
	    Thread.currentThread().getThreadGroup().list();
	    Tyrex.dumpTransactionList( writer );
	    Tyrex.dumpCurrentTransaction( writer );
	}
	memory.claimed();
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
     * Perform test for transaction manager timeout. Will kill the thread
     * associated with the transaction by throwing a TransactionTimeout
     * exception.
     */
    public static final int TEST_KT_TIMEOUT = 2;



    static class SecondThread
	extends Thread
    {


	Transaction tx;


	PrintWriter writer;


	SecondThread( Transaction tx, PrintWriter writer )
	{
	    this.tx = tx;
	    this.writer = writer;
	}


	public void run()
	{
	    try {
		Tyrex.getTransactionManager().resume( tx );
		while ( Tyrex.getTransactionManager().getTransaction() != null ) {
		    sleep( 1000 );
		}
	    } catch ( Exception except ) {
		System.out.println( "Second thread reports: " + except.toString() );
	    }
	}


    }


}
