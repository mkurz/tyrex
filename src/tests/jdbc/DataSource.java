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
 * $Id: DataSource.java,v 1.1 2000/09/29 01:25:20 mohammed Exp $
 */


package jdbc;


import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import jdbc.db.TyrexDriver;
import org.exolab.jtf.CWTestCategory;
import org.exolab.jtf.CWTestCase;
import org.exolab.jtf.CWVerboseStream;
import org.exolab.exceptions.CWClassConstructorException;
import tyrex.jdbc.ServerDataSource;
import tyrex.jdbc.xa.EnabledDataSource;
import tyrex.tm.Tyrex;



public class DataSource
    extends CWTestCategory
{

    public DataSource()
        throws CWClassConstructorException
    {
        super( "DataSource", "DataSource");
        
        CWTestCase tc;
        
        tc = new EnabledDataSourceTest();
        add( tc.name(), tc, true );

        tc = new ServerDataSourceTest();
        add( tc.name(), tc, true );

        tc = new XAConnectionTest();
        add( tc.name(), tc, true );


        tc = new PruneTest();
        add( tc.name(), tc, true );
    }


    /**
     * Return the Tyrex driver registered with the
     * java.sql.DriverManager.
     *
     * @param stream the stream
     * @return the Tyrex driver registered with the
     * java.sql.DriverManager. Return null if there is a problem
     */
    private TyrexDriver getTyrexDriver(CWVerboseStream stream)
        throws IOException
    {
        try {
            Class.forName("jdbc.db.TyrexDriver");
        }
        catch (ClassNotFoundException e) {
            stream.writeVerbose("Tyrex driver class not found");
            return null;
        }

        try {
            return (TyrexDriver)DriverManager.getDriver("jdbc:tyrex");
        }
        catch (SQLException e)
        {
            stream.writeVerbose("Failed to get tyrex driver");
            stream.writeVerbose(e.toString());
        }
        return null;
    }

    /**
     * Return the configured enabled data source.
     *
     * @return the configured enabled data source.
     */
    private EnabledDataSource getEnabledDataSource()
    {
        EnabledDataSource ds = new EnabledDataSource();
        ds.setDriverClassName("jdbc.db.TyrexDriver");
        ds.setDriverName("jdbc:tyrex:");
        ds.setPruneFactor(0);

        return ds;
    }

    private class EnabledDataSourceTest
        extends CWTestCase
    {
        EnabledDataSourceTest()
            throws CWClassConstructorException
        {
            super( "TC01", "Enabled Data Source" );
        }
    
        public boolean run( CWVerboseStream stream )
        {
            TransactionManager transactionManager;
            TyrexDriver tyrexDriver;
            Connection connection;
            Statement stmt;
            XAConnection xaConnection;
            EnabledDataSource ds;
            int i;
            
            try {
                tyrexDriver = getTyrexDriver(stream);

                if (null == tyrexDriver) {
                    return false;    
                }
                
                transactionManager = Tyrex.getTransactionManager();
                tyrexDriver.clearNumberOfCreatedConnections();
                ds = getEnabledDataSource();
                i = 0;
    
                try {
                    xaConnection = ds.getXAConnection();
                
                    for (; i < 10000; i++) {
                        transactionManager.begin();
        
                        connection = xaConnection.getConnection();
    
                        transactionManager.getTransaction().enlistResource(xaConnection.getXAResource());
        
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
                    return false;
                }
    
                if (tyrexDriver.getNumberOfCreatedConnections() != 1) {
                    stream.writeVerbose("Driver created " + 
                                        tyrexDriver.getNumberOfCreatedConnections() +
                                        " drivers. Expected 1.");
                    return false;
                }
    
                return true;
                
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }


    private class PruneTest
        extends CWTestCase
    {
        PruneTest()
            throws CWClassConstructorException
        {
            super( "TC04", "Prune" );
        }
    
        public boolean run( CWVerboseStream stream )
        {
            TransactionManager transactionManager;
            TyrexDriver tyrexDriver;
            Connection connection;
            Statement stmt;
            XAConnection xaConnection;
            EnabledDataSource ds;
            
            try {
                tyrexDriver = getTyrexDriver(stream);

                if (null == tyrexDriver) {
                    return false;    
                }
                
                transactionManager = Tyrex.getTransactionManager();
                tyrexDriver.clearNumberOfCreatedConnections();
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

                    if (tyrexDriver.getNumberOfCreatedConnections() != 1) {
                        stream.writeVerbose("Driver created " + 
                                            tyrexDriver.getNumberOfCreatedConnections() +
                                            " drivers. Expected 1.");
                        return false;
                    }

                    Thread.currentThread().sleep(3000);

                    connection = xaConnection.getConnection();
                    connection = xaConnection.getConnection();
                    stmt = connection.createStatement();
                    stmt.executeUpdate("update enabled_test set text = '55' where id = 1");
                    stmt.close();
                    connection.close();

                    if (tyrexDriver.getNumberOfCreatedConnections() != 2) {
                        stream.writeVerbose("Driver created " + 
                                            tyrexDriver.getNumberOfCreatedConnections() +
                                            " drivers. Expected 2.");
                        return false;
                    }
                
                }
                catch (Exception e)
                {
                    stream.writeVerbose(e.toString());
                    e.printStackTrace();
                    return false;
                }
    
                return true;
                
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }


    private class ServerDataSourceTest
        extends CWTestCase
    {
        private ServerDataSourceTest()
            throws CWClassConstructorException
        {
            super( "TC02", "Server Data Source" );
        }
    
        public boolean run( CWVerboseStream stream )
        {
            TransactionManager transactionManager;
            TyrexDriver tyrexDriver;
            Connection connection;
            Statement stmt;
            EnabledDataSource ds;
            ServerDataSource pool;
            int i;
            
            try {
                tyrexDriver = getTyrexDriver(stream);

                if (null == tyrexDriver) {
                    return false;    
                }

                transactionManager = Tyrex.getTransactionManager();

                tyrexDriver.clearNumberOfCreatedConnections();
                
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
                    return false;
                }
    
                if (tyrexDriver.getNumberOfCreatedConnections() != 1) {
                    stream.writeVerbose("Driver created " + 
                                        tyrexDriver.getNumberOfCreatedConnections() +
                                        " drivers. Expected 1.");
                    return false;
                }
    
                return true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }


    private class XAConnectionTest
        extends CWTestCase
    {
        private XAConnectionTest()
            throws CWClassConstructorException
        {
            super( "TC03", "XA Connections with different user/passwords" );
        }
    
        public boolean run( CWVerboseStream stream )
        {
            TransactionManager transactionManager;
            TyrexDriver tyrexDriver;
            Connection connection;
            Statement stmt;
            EnabledDataSource ds;
            ServerDataSource pool;
            int i;
            
            try {
                tyrexDriver = getTyrexDriver(stream);

                if (null == tyrexDriver) {
                    return false;    
                }

                transactionManager = Tyrex.getTransactionManager();

                tyrexDriver.clearNumberOfCreatedConnections();
                
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
                    e.printStackTrace();
                    return false;
                }
    
                if (tyrexDriver.getNumberOfCreatedConnections() != 3) {
                    stream.writeVerbose("Driver created " + 
                                        tyrexDriver.getNumberOfCreatedConnections() +
                                        " drivers. Expected 3.");
                    return false;
                }
    
                return true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static void main (String args[]) {

        class Test extends org.exolab.jtf.CWBaseApplication
        {
            private final java.util.Vector categories;

            public Test(String s)
                throws CWClassConstructorException
            {
                super(s);

                categories = new java.util.Vector();
                categories.addElement("jdbc.DataSource");
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
    }
    
}




