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
 * Copyright 1999-2001 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: Performance.java,v 1.8 2001/03/17 01:27:19 arkin Exp $
 */


package tyrex.tm.impl;


import java.io.PrintWriter;
import java.io.InputStream;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import tyrex.util.Logger;
import tyrex.tm.TransactionDomain;
import tyrex.tm.TyrexTransactionManager;


/**
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.8 $ $Date: 2001/03/17 01:27:19 $
 */
public class Performance
{


    private static TransactionDomain   _txDomain;


    private static TyrexTransactionManager  _txManager;


    public static InputStream          _configFile;


    public static void main( String args[] )
    {
        PrintWriter  writer;
        int          test;
        int          count;
        Transaction  tx;
        long         clock;

        test = -1;
        if ( args.length > 0 ) {
            if ( "perf".startsWith( args[ 0 ] ) )
                test = TEST_PERFORMANCE;
            else if ( "tm".startsWith( args[ 0 ] ) )
                test = TEST_TM_TIMEOUT;
        }
        if ( test == -1 ) {
            System.out.println( "Usage: <test> [debug]\nWhere <test> is one of:\n" );
            System.out.println( "  p[erf]      Performance test" );
            System.out.println( "  tm          Transaction manager timeout" );
            System.exit ( 1 );
        }
        writer = new PrintWriter( System.out, true );
        
        try {
            if ( _configFile == null )
                _configFile = Performance.class.getResourceAsStream( "test.xml" );
            _txDomain = TransactionDomain.createDomain( _configFile );
            _txDomain.recover();
            _txManager = (TyrexTransactionManager) _txDomain.getTransactionManager();
            
            System.out.println( "Creating transaction locally" );
            _txManager.begin();
            tx = _txManager.getTransaction();
            System.out.println( "Transaction: " + tx.toString() );
            System.out.println( "Commiting transaction" );
            _txManager.commit();
            
            System.out.println( "Creating transaction locally" );
            _txManager.setTransactionTimeout( 1 );
            _txManager.begin();
            tx = _txManager.getTransaction();
            System.out.println( "Transaction: " + tx.toString() );
            
            if ( test == TEST_TM_TIMEOUT ) {
                new SecondThread( tx, writer ).start();
                
                _txManager.dumpCurrentTransaction( writer );
                try {
                    Thread.sleep( 3000 );
                } catch ( Exception except ) { }
                _txManager.dumpCurrentTransaction( writer );
            }
            
            System.out.println( "Aborting transaction" );
            _txManager.rollback();
            
            if ( test == TEST_PERFORMANCE ) {
                clock = System.currentTimeMillis();
                count = 100000;
                for ( int i = 0 ; i < count ; ++i ) {
                    _txManager.begin();
                    _txManager.commit();
                }
                clock = System.currentTimeMillis() - clock;
                System.out.println( "Rate " + (double) count / ( (double) clock / 1000 ) + "/sec" );
            }
        } catch ( Exception except ) {
            System.out.println( except );
            except.printStackTrace();
        }
        System.out.println( "[Test end]" );
        
        _txManager.dumpTransactionList( writer );
        _txManager.dumpCurrentTransaction( writer );
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
                _txManager.resume( tx );
                while ( _txManager.getTransaction() != null ) {
                    sleep( 2000 );
                }
            } catch ( Exception except ) {
                System.out.println( "Second thread reports: " + except.toString() );
            }
        }


    }


}
