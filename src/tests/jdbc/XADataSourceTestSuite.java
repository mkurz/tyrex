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
 * $Id: XADataSourceTestSuite.java,v 1.1 2001/02/23 17:17:41 omodica Exp $
 */


package jdbc;

import tests.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import jdbc.db.TestConnectionImpl;
import jdbc.db.TestDriverImpl;

import junit.framework.*;

import tyrex.jdbc.ServerDataSource;
import tyrex.jdbc.xa.EnabledDataSource;
import tyrex.tm.Tyrex;


/**
 * Tests for tyrex.jdbc xa data source implementations
 */
public class XADataSourceTestSuite
    extends TestSuite
{
    /**
     * The test driver
     */
    private TestDriverImpl _driver = null;


    public XADataSourceTestSuite( String name)
    {
        super( name );
        
        TestCase tc;
        
        tc = new EnabledDataSourceTest();
        addTest( tc );
        
        tc = new ServerDataSourceTest();
        addTest( tc );

        tc = new XAConnectionTest();
        addTest( tc );

        tc = new PruneTest();
        addTest( tc );
        
        tc = new TransactionTimeoutTest();
        addTest( tc );
        
    }


    /**
     * Return the Tyrex driver registered with the
     * java.sql.DriverManager.
     *
     * @param stream the stream
     * @return the Tyrex driver registered with the
     * java.sql.DriverManager.
     * @throws IOException if there is an error writing to the stream
     * @throws ClassNotFoundException if the test driver class is not found
     * @throws SQLException if there is a problem geting the test driver
     */
    private TestDriverImpl getTestDriver(VerboseStream stream)
        throws IOException, ClassNotFoundException, SQLException
    {
        if (null == _driver) {
            try {
                Class.forName("jdbc.db.TestDriverImpl");
            }
            catch (ClassNotFoundException e) {
                stream.writeVerbose("Test driver class not found");
                throw e;
            }
    
            try {
                _driver = (TestDriverImpl)DriverManager.getDriver("jdbc:test");
            }
            catch (SQLException e)
            {
                stream.writeVerbose("Failed to get test driver");
                throw e;
            }
        }

        return _driver;
    }

    /**
     * Return the configured enabled data source.
     *
     * @return the configured enabled data source.
     */
    private EnabledDataSource getEnabledDataSource()
    {
        EnabledDataSource ds = new EnabledDataSource();
        ds.setDriverClassName("jdbc.db.TestDriverImpl");
        ds.setDriverName("jdbc:test");
        ds.setPruneFactor(0);

        return ds;
    }

    private class EnabledDataSourceTest
        extends TestCase
    {
        EnabledDataSourceTest()
        {
            super( "[TC01] Enabled Data Source" );
        }
    
        public void runTest()
        {
            TransactionManager transactionManager;
            TestDriverImpl testDriver;
            Connection connection;
            Statement stmt;
            XAConnection xaConnection;
            EnabledDataSource ds;
            int i;
            
            VerboseStream stream = new VerboseStream();
            
            try {
                testDriver = getTestDriver(stream);

                transactionManager = Tyrex.getTransactionManager();
                testDriver.clearNumberOfCreatedConnections();
                ds = getEnabledDataSource();
                i = 0;
                //System.out.println("ds connection " + ds.getConnection());
    
                try {
                    xaConnection = ds.getXAConnection();
                
                    for (; i < 10000; i++) {
                        transactionManager.begin();
        
                        connection = xaConnection.getConnection();
    
                        if (!transactionManager.getTransaction().enlistResource(xaConnection.getXAResource())) {
                            fail("Error: failed to enlist");
                        }
        
                        stmt = connection.createStatement();
        
                        stmt.executeUpdate("update enabled_test set text = '55' where id = 1");
        
                        stmt.close();
        
                        transactionManager.commit();
        
                        connection.close();
                    }
                }
                catch (Exception e)
                {
                    stream.writeVerbose("failed at iteration " + i);
                    stream.writeVerbose(e.toString());
                    e.printStackTrace();
                    fail( "Error: failed at iteration " + i );
                }
    
                if (testDriver.getNumberOfCreatedConnections() != 1) {
                    fail("Driver created " + 
                                        testDriver.getNumberOfCreatedConnections() +
                                        " drivers. Expected 1.");
                }
    
                
            }
            catch (Exception e) {
                e.printStackTrace();
                 fail( e.getMessage() );
            }
        }
    }


    private class PruneTest
        extends TestCase
    {
        PruneTest()
        {
            super( "[TC04] Prune" );
        }
    
        public void runTest()
        {
            TransactionManager transactionManager;
            TestDriverImpl testDriver;
            Connection connection;
            Statement stmt;
            XAConnection xaConnection;
            EnabledDataSource ds;
            
            VerboseStream stream = new VerboseStream();
            
            try {
                testDriver = getTestDriver(stream);

                transactionManager = Tyrex.getTransactionManager();
                testDriver.clearNumberOfCreatedConnections();
                ds = getEnabledDataSource();
                ds.setTransactionTimeout(1);
                ds.setPruneFactor((float)0.75);
                
                try {
                    xaConnection = ds.getXAConnection();
                
                    connection = xaConnection.getConnection();
                    stmt = connection.createStatement();
                    stmt.executeUpdate("update enabled_test set text = '55' where id = 1");
                    stmt.close();
                    connection.close();

                    if (testDriver.getNumberOfCreatedConnections() != 1) {
                        fail( "Driver created " + 
                                            testDriver.getNumberOfCreatedConnections() +
                                            " drivers. Expected 1.");
                    }

                    Thread.currentThread().sleep(3000);

                    connection = xaConnection.getConnection();
                    connection = xaConnection.getConnection();
                    stmt = connection.createStatement();
                    stmt.executeUpdate("update enabled_test set text = '55' where id = 1");
                    stmt.close();
                    connection.close();

                    if (testDriver.getNumberOfCreatedConnections() != 2) {
                        fail("Driver created " + 
                                            testDriver.getNumberOfCreatedConnections() +
                                            " drivers. Expected 2.");
                    }
                
                }
                catch (Exception e)
                {
                    stream.writeVerbose(e.toString());
                    e.printStackTrace();
                    fail( e.getMessage() ); 
                }
                
            }
            catch (Exception e) {
                e.printStackTrace();
                 fail( e.getMessage() );
            }
        }
    }


    private class ServerDataSourceTest
        extends TestCase
    {
        private ServerDataSourceTest()
        {
            super( "[TC02] Server Data Source" );
        }
    
        public void runTest()
        {
            TransactionManager transactionManager;
            TestDriverImpl testDriver;
            Connection connection;
            Statement stmt;
            EnabledDataSource ds;
            ServerDataSource pool;
            int i;
            
            VerboseStream stream = new VerboseStream();
            
            try {
                testDriver = getTestDriver(stream);

                transactionManager = Tyrex.getTransactionManager();

                testDriver.clearNumberOfCreatedConnections();
                
                ds = getEnabledDataSource();
                pool = new ServerDataSource();
    
                pool.setDataSource((XADataSource)ds);
                i = 0;
    
                try {
                    for (; i < 10000; i++) {
                        transactionManager.begin();
        
                        connection = pool.getConnection();
    
                        stmt = connection.createStatement();
        
                        stmt.executeUpdate("update enabled_test set text = '55' where id = 1");
        
                        stmt.close();
        
                        transactionManager.commit();
        
                        connection.close();
                    }
                }
                catch (Exception e)
                {
                    stream.writeVerbose("failed at iteration " + i);
                    stream.writeVerbose(e.toString());
                    e.printStackTrace();
                    fail("Error: failed at iteration " + i );
                }
    
                if (testDriver.getNumberOfCreatedConnections() != 1) {
                    fail("Driver created " + 
                                        testDriver.getNumberOfCreatedConnections() +
                                        " drivers. Expected 1.");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                fail( e.getMessage() );
            }
        }
    }

    private class TransactionTimeoutTest
        extends TestCase
    {
        private TransactionTimeoutTest()
        {
            super( "[TC05] Transaction Timeout" );
        }
    
        public void runTest()
        {
            //int i;
            try {
                 VerboseStream stream = new VerboseStream();
             
                final TestDriverImpl testDriver = getTestDriver(stream);
                Thread thread;
                final boolean[] result = new boolean[]{false};
                final TransactionManager transactionManager = Tyrex.getTransactionManager();
                EnabledDataSource ds = getEnabledDataSource();
                ds.setTransactionTimeout(3);
                
                //i = 0;

                try {
                    final XAConnection xaConnection = ds.getXAConnection();

                    //for (; i < 5; i++) {
                        thread = new Thread(new Runnable()
                            {
                                public void run() 
                                {
                                    Connection connection = null;
                                    
                                    try {
                                        Statement stmt;

                                        transactionManager.begin();

                                        transactionManager.getTransaction().enlistResource(xaConnection.getXAResource());
        
                                        connection = xaConnection.getConnection();

                                        stmt = connection.createStatement();
                        
                                        stmt.executeUpdate("update enabled_test set text = '55' where id = 1");
                        
                                        stmt.close();
                                        
                                        Thread.currentThread().sleep(7000);
                                        
                                        transactionManager.commit();
                        
                                    }
                                    catch (Exception e) {
                                        result[0] = true;
                                    }
                                    finally {
                                        if (null != connection) {
                                            try{connection.close();}catch(SQLException e){}
                                        }
                                    }
                                }
                            });


                            thread.start();

                            thread.join();
                            
                            if (!result[0]) {
                                //stream.writeVerbose("failed at iteration " + i);
                                fail( "Error: failed to execute update" );   
                            }
                    //}
                }
                catch (Exception e)
                {
                    //stream.writeVerbose("failed at iteration " + i);
                    stream.writeVerbose(e.toString());
                    e.printStackTrace();
                    fail( e.getMessage() );
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                fail( e.getMessage() );
            }

        }
    }



    private class XAConnectionTest
        extends TestCase
    {
        private XAConnectionTest()
        {
            super( "[TC03] XA Connections with different user/passwords" );
        }
    
        public void runTest()
        {
            TransactionManager transactionManager;
            TestDriverImpl testDriver;
            Connection connection;
            Statement stmt;
            EnabledDataSource ds;
            ServerDataSource pool;
            int i;
            
            VerboseStream stream = new VerboseStream();
            
            try {
                testDriver = getTestDriver(stream);

                transactionManager = Tyrex.getTransactionManager();

                testDriver.clearNumberOfCreatedConnections();
                
                ds = getEnabledDataSource();
                pool = new ServerDataSource();
    
                pool.setDataSource((XADataSource)ds);
                i = 0;
    
                try {
                    for (; i < 10000; i++) {
                        transactionManager.begin();
        
                        connection = pool.getConnection("user1", "pass1");
                        stmt = connection.createStatement();
                        stmt.executeUpdate("update enabled_test set text = '55' where id = 1");
                        stmt.close();
                        connection.close();

                        connection = pool.getConnection("user2", "pass2");
                        stmt = connection.createStatement();
                        stmt.executeUpdate("update enabled_test set text = '55' where id = 1");
                        stmt.close();
                        connection.close();


                        connection = pool.getConnection("user3", "pass3");
                        stmt = connection.createStatement();
                        stmt.executeUpdate("update enabled_test set text = '55' where id = 1");
                        stmt.close();
                        connection.close();
        
                        transactionManager.commit();
        
                        
                    }
                }
                catch (Exception e)
                {
                    stream.writeVerbose("failed at iteration " + i);
                    stream.writeVerbose(e.toString());
                    //e.printStackTrace();
                    fail( e.getMessage() );
                }
    
                if (testDriver.getNumberOfCreatedConnections() != 3) {
                    fail("Driver created " + 
                                        testDriver.getNumberOfCreatedConnections() +
                                        " drivers. Expected 3.");
                }
    
             }
            catch (Exception e) {
                e.printStackTrace();
                fail( e.getMessage() );
            }
        }
    }

   /*
    public static void main (String args[]) {

        class Test extends org.exolab.jtf.CWBaseApplication
        {
            private final java.util.Vector categories;

            public Test(String s)
                throws CWClassConstructorException
            {
                super(s);

                categories = new java.util.Vector();
                categories.addElement("jdbc.XADataSourceTestCategory");
            }
        
            protected String getApplicationName()
            {
                return "Test";
            }
        
            protected java.util.Enumeration getCategoryClassNames()
            {
                return categories.elements();
            }
        }

        try
        {
            Test test = new Test("enabled test");
            test.run(args);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }*/
    
}




