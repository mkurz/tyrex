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
 * $Id: Transaction.java,v 1.10 2001/07/10 20:26:59 mohammed Exp $
 */


package transaction;

import transaction.configuration.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import junit.framework.*;

import transaction.configuration.Attribute;
import transaction.configuration.Configuration;
import transaction.configuration.Datasource;
import transaction.configuration.Performance;
import transaction.configuration.types.Type;
import tyrex.resource.jdbc.xa.EnabledDataSource;
//import tyrex.tm.Tyrex;
import tyrex.tm.TyrexTransaction;

/**
 * Performs various tests with transactions using databases.
 * It tests both that the transactions perform the correct
 * behaviour and that the databases have the correct data
 * at all times. There is overlap between the some
 * of the tests.
 * <P>
 * The configuration for the transaction test is specified via
 * an xml file.
 * Look for the configuration file according to the following criteria:
 * <UL>
 * <LI>First look at the {@link #CONFIGURATION_FILE_PROPERTY java property}
 * <LI>Then look for the {@link #CONFIGURATION_FILE configuration file} as a resource
 * <LI>Then look for the {@link #CONFIGURATION_FILE configuration file} in working directory
 * <LI>Then look for the {@link #CONFIGURATION_FILE configuration file} in the home directory
 * <LI>Then look for the {@link #CONFIGURATION_FILE configuration file} in the java directory
 * <LI>Then throw exception that the configuration cannot be found
 * </UL>
 * <P>
 * The tests are:
 * <UL>
 * <LI> Test two-phase commit and that the status is correct
 *      throughout the commit lifecycle.
 * <LI> Test one-phase commit  and that the status is correct
 *      throughout the commit lifecycle.
 * <LI> Test one-phase commit optimization and that the status is correct
 *      throughout the commit lifecycle.
 * <LI> Test rollback  and that the status is correct
 *      throughout the rollback lifecycle
 * <LI> Test that transaction is heuristically rolled back
 *      if the transaction has been marked for rollback and
 *      has been committed.
 * <LI> Test that a transaction can be rolled back if the
 *      transaction has been marked for rollback.
 * <LI> Test commit with XA resources being 
 *      delisted with XAResource.TMSUCCESS before
 *      commit occurs.
 * <LI> Test rollback with XA resources being 
 *      delisted with XAResource.TMSUCCESS before
 *      rollback occurs.
 * <LI> Test commit with XA resources being 
 *      delisted with XAResource.TMSUCCESS and re-enlisted
 *      in same transaction before commit occurs.
 * <LI> Test rollback with XA resources being 
 *      delisted with XAResource.TMSUCCESS and re-enlisted
 *      in same transaction before rollback occurs.
 * <LI> Test one phase commit optimization with a single 
 *      XA resource being 
 *      delisted with XAResource.TMSUCCESS before
 *      commit occurs.
 * <LI> Test one phase commit optimization with a single 
 *      XA resource being 
 *      delisted with XAResource.TMSUCCESS and re-enlisted
 *      in same transaction before commit occurs.
 * <LI> Test that transaction is marked for rollback
 *      when the XA resources are delisted with
 *      XAResource.TMFAIL.
 * <LI> Test that a transaction can be committed asynchronously.
 * <LI> Test that a transaction can be rolled back asynchronously.
 * <LI> Test that all the Synchronization methods are called during 
 *      commit
 * <LI> Test that all the Synchronization methods are called during 
 *      rollback
 * <LI> Test that all the Synchronization methods are called when an 
 *      error occurs in the beforeCompletion method of a 
 *      Synchronization during commit. The transaction must be marked
 *      for rollback and a heuristic rollback exception is thrown.
 * <LI> Test that all the Synchronization methods are called when an 
 *      error occurs in the afterCompletion method of a 
 *      Synchronization during commit.
 * <LI> Test that all the Synchronization methods are called when an 
 *      error occurs in both the beforeCompletion and afterCompletion 
 *      methods of a Synchronization during commit. The transaction 
 *      must be marked for rollback and a heuristic rollback exception 
 *      is thrown.
 * <LI> Test that all the Synchronization methods are called when an 
 *      error occurs in the beforeCompletion method of a 
 *      Synchronization during rollback. The transaction 
 *      must be marked for rollback
 * <LI> Test that all the Synchronization methods are called when an 
 *      error occurs in the afterCompletion method of a 
 *      Synchronization during rollback.
 * <LI> Test that all the Synchronization methods are called when an 
 *      error occurs in both the beforeCompletion and afterCompletion 
 *      methods of a Synchronization during rollback. The transaction 
 *      must be marked for rollback
 * <LI> Test that one phase commit optimization applies to XA 
 *      resources with the same resource manager.
 * <LI> Test performance for two-phase commit..
 * <LI> Test performance for one-phase commit.
 * <LI> Test performance for rollbacks.            
 * </UL>
 * <P>
 * The two-phase commit tests can only apply to multiple data sources so if a single
 * data source is defined then the two-phase commit tests are not run.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public class Transaction
  extends TestSuite
{
    /**
     * The property defining the configuration file.
     * <P>
     * The configuration file can be specified in as a java property 
     * java -tyrex.test.transaction.configuration=conf.xml
     */
    public static final String CONFIGURATION_FILE_PROPERTY = "transaction.configuration";

    /**
     * The name of the configuration file
     */
    private static final String CONFIGURATION_FILE = "configuration.xml";

    /**
     * The name of the primary key column in the test tables
     */
    static final String PRIMARY_KEY_COLUMN_NAME = "id";

    /**
     * The name of the value column in the test tables
     */
    static final String VALUE_COLUMN_NAME = "value";
    
    /**
     * Transaction domain
     */
    private static tyrex.tm.TransactionDomain _txDomain = null;

    /**
     * The database entry groups
     */
    private ArrayList _groups;

    public Transaction( String name, String config_file )
    {
        super();
                
        try {
         
         _txDomain = createTransactionDomain( config_file );
         
         _groups = getDataSourceGroups();
         
         TransactionTestSuite trans = new TransactionTestSuite("Transaction Test Suite", _groups, new tests.VerboseStream(), _txDomain ); 
         
         // add the initializer test
         addTest( new TestInitializer(this) );
         
         // set up the transaction test suite
         
         for( java.util.Enumeration e = trans.tests(); e.hasMoreElements(); )
           addTest( (Test)e.nextElement());
 
         
         // add the cleaner test
         addTest( new TestCleaner(this) );
        
        }
        catch(Exception e) {
            e.printStackTrace();

        }  
    }
    
    /**
     * Creates a transaction domain
     */
    private tyrex.tm.TransactionDomain createTransactionDomain( String config_file )    
    {
      try
        {        	        
           tyrex.tm.TransactionDomain txDomain = tyrex.tm.TransactionDomain.getDomain( "default" );
        	  if( txDomain == null ) 
        	  {
        	   txDomain = tyrex.tm.TransactionDomain.createDomain( config_file );   
        	   
       	   try
	         {
		         txDomain.recover();
	         }
	         catch ( tyrex.tm.RecoveryException ex )
	         {
		         ex.printStackTrace();
	         }     	          	          	          	            
           } 
        	  return txDomain;
        }
        catch ( tyrex.tm.DomainConfigurationException ex )
        {
        	   ex.printStackTrace();
        	   System.exit(0);
        }  
        
      return null;      
    }
    
    /**
     * The test is used as an initializer 
     */
    static class TestInitializer
        extends TestCase {
        
        /**
         * The transaction test
         */
        private final Transaction _trans;

        /**
         * The name
         */
        static final String NAME = "Initializer";

        /**
         * Create the TestInitializer
         *
         *
         *
         * @param trans the transaction test(required)
         */
        TestInitializer(Transaction trans) {
            
            super(NAME);

            _trans = trans;
        }
        
        public void setUp() {
         
          _trans.dropAndCreateTables(true);
        }
        
        public void runTest() {}
     };
     
    /**
     * The test is used as a cleaner
     */
    static class TestCleaner
        extends TestCase {
        
        /**
         * The transaction test
         */
        private final Transaction _trans;

        /**
         * The name
         */
        static final String NAME = "Clean";

        /**
         * Create the TestCleaner
         *
         *
         *
         * @param trans the transaction test(required)
         */
        TestCleaner(Transaction trans) {
            
            super(NAME);

            _trans = trans;
        }
        
        public void tearDown() {
         
          _trans.dropAndCreateTables(false);
        }
        
        public void runTest() {}
     };          

    

    public void postExecute() {
        dropAndCreateTables(false);
    }

    public void preExecute() {
        dropAndCreateTables(true);
    }

    /**
     * Drop and create the tables listed in the list of {@link DataSourceGroupEntry}.
     * <P>
     * <UL>
     * <LI>First the table list in the {@link DataSourceEntry} is dropped
     * <LI>Then if the create flag is true the a 2 column table is created 
     *      with a {@link #PRIMARY_KEY_COLUMN_NAME primary key column} and a 
     *      {@link VALUE_COLUMN_NAME value column}. Both columns are varchar(255).
     * </UL>
     * This process is only done once for each {@link DataSourceGroupEntry}
     * even if multiple groups share the same {@link DataSourceGroupEntry}
     *
     * @param create True if the tables are to be created
     * @see #_groups
     */
    private void dropAndCreateTables(boolean create) {
     
     
        DataSourceGroupEntry group;
        DataSourceEntry entry;
        int j;
        XAConnection xaConnection;
        Connection connection;
        Statement statement;
        ArrayList visited;
        TransactionManager transactionManager;
        
        try {
            visited = new ArrayList();
            xaConnection = null;
            connection = null;
            statement = null;
    
            transactionManager = _txDomain.getTransactionManager();
                
            // make sure we're not in a stray trnasaction
            try {
           		transactionManager.rollback();
            }
            catch(Exception e) {
            }
            
            // set up the database tables
            for (int i = _groups.size(); --i >= 0;) {
                group = (DataSourceGroupEntry)_groups.get(i);
                
                for (j = group.getNumberOfDataSourceEntries(); --j >= 0;) {
                    
                    
                    entry = group.getDataSourceEntry(j);
                    
                    if (!visited.contains(entry)) {
						visited.add(entry);

						try {
							if (!entry.getCreateDropTables()) {
								xaConnection = entry.getXAConnection();
								
								connection = xaConnection.getConnection();

								transactionManager.begin();

								transactionManager.getTransaction().enlistResource(xaConnection.getXAResource());
								
								statement = connection.createStatement();
								
								// ignore if the table exists
								try {
									statement.executeUpdate("delete from " + 
														entry.getTableName());
								}
								catch(SQLException e) {
									// ignore if this is not create
									if (create) {
										throw e;	
									}
								}
							}
							else {
								xaConnection = entry.getXAConnectionForCreation();
									
								connection = xaConnection.getConnection();
								
								statement = connection.createStatement();
															
								transactionManager.begin();
	
								transactionManager.getTransaction().enlistResource(xaConnection.getXAResource());
		
								try {
									statement.execute("drop table " + entry.getTableName());
								}
								catch(SQLException e) {
									//e.printStackTrace();
								}
								
								if (create) {
									try {
										statement.execute( "create table " + entry.getTableName() + 
													   " (" + PRIMARY_KEY_COLUMN_NAME + 
													   " varchar(255) primary key, " + 
													   VALUE_COLUMN_NAME + 
													   " varchar(255))");
									}
									catch(SQLException e){
										// hack for oracle 8.1.7
										transactionManager.rollback();
	
										if (null != statement) {
											try{statement.close();}catch(SQLException e1){}
											statement = null;
										}
										if (null != connection) {
											try{connection.close();}catch(SQLException e1){}
											connection = null;
										}
										if (null != xaConnection) {
											try{xaConnection.close();}catch(SQLException e1){}
											xaConnection = null;
										}
	
										xaConnection = entry.getXAConnection();
								
										connection = xaConnection.getConnection();
	
										transactionManager.begin();
	
										transactionManager.getTransaction().enlistResource(xaConnection.getXAResource());
										
										statement = connection.createStatement();
										
										statement.executeUpdate("delete from " + 
																entry.getTableName());
									}
								}
								
							}

							transactionManager.commit();
						}
						finally {
                            if (null != statement) {
								try{statement.close();}catch(SQLException e){}
								statement = null;
							}
							if (null != connection) {
								try{connection.close();}catch(SQLException e){}
								connection = null;
							}
							if (null != xaConnection) {
								try{xaConnection.close();}catch(SQLException e){}
								xaConnection = null;
							}
						}
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
        
    }
    

    /**
     * Return the reader containing configuration information.
     * <P>
     * Look for the configuration file according to the following criteria:
     * <UL>
     * <LI>First look at the {@link #CONFIGURATION_FILE_PROPERTY java property}
     * <LI>Then look for the {@link #CONFIGURATION_FILE configuration file} as a resource
     * <LI>Then look for the {@link #CONFIGURATION_FILE configuration file} in working directory
     * <LI>Then look for the {@link #CONFIGURATION_FILE configuration file} in the home directory
     * <LI>Then look for the {@link #CONFIGURATION_FILE configuration file} in the java directory
     * <LI>Then throw exception
     * </UL>
     *
     * @return the reader containing configuration information.
     * @throws FileNotFoundException if the file is not found.
     */
    private static Reader getConfiguration() 
        throws FileNotFoundException {
        File file;
        InputStream inputStream;
        String property;

        // first look at the property
        property = System.getProperty(CONFIGURATION_FILE_PROPERTY);
		if (null != property) {
            file = new File(property);

            if (file.exists()) {
                return new FileReader(file);    
            }
        }

        inputStream = Transaction.class.getResourceAsStream(CONFIGURATION_FILE);

        if (null != inputStream) {
            return new InputStreamReader(inputStream);    
        }

        // look in the working directory
        file = new File(System.getProperty("user.dir"), CONFIGURATION_FILE);

        if (file.exists()) {
            return new FileReader(file);    
        }

        // look in the home directory
        file = new File(System.getProperty("user.home"), CONFIGURATION_FILE);

        if (file.exists()) {
            return new FileReader(file);    
        }

        // look in the java directory
        file = new File(System.getProperty("java.home"), CONFIGURATION_FILE);

        if (file.exists()) {
            return new FileReader(file);    
        }

        throw new FileNotFoundException("Configuration file not found.");
    }
    

    /**
     * Return the list containing {@link DataSourceGroupEntry} from the
     * configuration file.
     *
     * @return the list containing {@link DataSourceGroupEntry} from the
     *      configuration file.
     * @throws ClassNotFoundException if the XA data source class is not found
     * @throws InstantiationException if there is a problem creating the XA data source
     * @throws IllegalAccessException if there is a problem accessing the constructor of
     *      XA data source class.
     * @throws FileNotFoundException if the configuration file is not found.
     * @throws ValidationException if there is a problem unmarshalling the xml file
     * @throws MarshalException if there is a problem unmarshalling the xml file
     * @throws NoSuchMethodException if the attribute method is not found
     * @throws InvocationTargetException if there is a problem invoking the method
     */
    private static ArrayList getDataSourceGroups()
        throws  ClassNotFoundException, MarshalException, 
                ValidationException, FileNotFoundException,
                InstantiationException, IllegalAccessException,
                NoSuchMethodException, InvocationTargetException {
        Reader reader;
        Configuration configuration;
        Datasource datasource;
        int datasourceCount;
        int performanceCount;
        ArrayList result;
        int resultCount;
        String group;
        DataSourceGroupEntry entry;
        int i;
        int k;

        reader = getConfiguration();
        
        try {
            configuration = Configuration.unmarshal(reader);
            datasourceCount = configuration.getDatasourceCount();
            performanceCount = configuration.getPerformanceCount();
            resultCount = 0;
            
            result = new ArrayList();

            for (int j = 0; j < datasourceCount; ++j) {
                datasource = configuration.getDatasource(j);

                group:
                for (k = datasource.getGroupCount(); --k >= 0;) {
                    group = datasource.getGroup(k);

                    for (i = 0; i < resultCount; ++i) {
                        entry = (DataSourceGroupEntry)result.get(i);

                        if (null == group 
                                ? null == entry.getGroupName() 
                                : group.equals(entry.getGroupName())) {
                            entry.addDataSourceEntry(createDataSourceEntry(datasource));                            
                            continue group;
                        }
                    }

                    ++resultCount;
                    entry = new DataSourceGroupEntry(group, getPerformance(configuration, group));
                    entry.addDataSourceEntry(createDataSourceEntry(datasource));
                    result.add(entry);
                }
            }
        }
        finally {
            try {reader.close();} catch(IOException e){}
        }

        return result;
    }

    /**
     * Create the XA data source from the data source configuration
     *
     * @param datasource the data source configuration;
     * @return the XA data source created from the data source configuration
     * @throws ClassNotFoundException if the XA data source class is not found
     * @throws InstantiationException if there is a problem creating the XA data source
     * @throws IllegalAccessException if there is a problem accessing the constructor of
     *      XA data source class or a problem accessing the set attribute method.
     * @throws NoSuchMethodException if the attribute method is not found
     * @throws InvocationTargetException if there is a problem invoking the method
     */
    private static DataSourceEntry createDataSourceEntry(Datasource datasource) 
        throws  ClassNotFoundException, InstantiationException, IllegalAccessException,
                NoSuchMethodException, InvocationTargetException {
		XADataSource creationDataSource;
        XADataSource xaDataSource;
        EnabledDataSource enabledDataSource;
        
        if (null == datasource.getUri()) {
            xaDataSource = (XADataSource)Class.forName(datasource.getType()).newInstance();

            for (int i = datasource.getAttributeCount(); --i >= 0;) {
                setAttribute(xaDataSource, datasource.getAttribute(i));    
            }
        }
        else {
            enabledDataSource = new EnabledDataSource();
            enabledDataSource.setDriverName(datasource.getUri());
            enabledDataSource.setDriverClassName(datasource.getType());
            enabledDataSource.setUser(datasource.getUserName());
            enabledDataSource.setPassword(datasource.getPassword());
            xaDataSource = enabledDataSource;
        }

		if ((null == datasource.getCreateClass()) ||
			(null == datasource.getCreateUri())) {
			creationDataSource = xaDataSource;
		}
		else {
			enabledDataSource = new EnabledDataSource();
            enabledDataSource.setDriverName(datasource.getCreateUri());
            enabledDataSource.setDriverClassName(datasource.getCreateClass());
            enabledDataSource.setUser(datasource.getUserName());
            enabledDataSource.setPassword(datasource.getPassword());
            creationDataSource = enabledDataSource;
		}

        return new DataSourceEntry(xaDataSource, 
								   creationDataSource,
                                   datasource);
    }

    /**
     * Set the attribute for the XA data source
     *
     * @param xaDataSource the XA data source (required)
     * @param attribute the attribute (required)
     * @throws NoSuchMethodException if the attribute method is not found
     * @throws IllegalAccessException if access to the method is prohibited
     * @throws InvocationTargetException if there is a problem invoking the method
     */
    private static void setAttribute(XADataSource xaDataSource, Attribute attribute) 
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (null == attribute.getType()) {
            throw new IllegalArgumentException("The 'attribute type' is null.");
        }
        xaDataSource.getClass().getMethod(getSetMethodName(attribute.getName()), 
                                          new Class[]{getSetMethodClass(attribute.getType())}).invoke(xaDataSource,
                                                                                                        new Object[]{getSetMethodObject(attribute.getValue(), attribute.getType())});
    }


    /**
     * Return the object required by the set method
     * <P>
     * It is assumed that the type is valid.
     *
     * @param value the value
     * @param type the type
     */
    private static Object getSetMethodObject(String value, Type type) {
        if ((type == Type.INT) ||
            (type == Type.INTEGER)) {
            if ((null == value) ||
                (0 == value.length())) {
                throw new IllegalArgumentException("The argument 'value' is null or empty.");
            }
            
            return new Integer(Integer.parseInt(value.trim()));    
        }
        if (type == Type.STRING) {
            return value;    
        }

        throw new IllegalArgumentException("Unknown type '" + type + "'.");    
    }

    /**
     * Return the class of the set method
     *
     * @param type the type
     */
    private static Class getSetMethodClass(Type type) {
        if (type == Type.INT) {
            return Integer.TYPE;    
        }
        if (type == Type.INTEGER) {
            return Integer.class;
        }
        if (type == Type.STRING) {
            return String.class;    
        }

        throw new IllegalArgumentException("Unknown type '" + type + "'.");
    }

    /**
     * Return the name of the set method
     * <P>
     * Prepend "set" to the capitalized name and
     * return the result
     *
     * @param name the name
     * @return the name of the set method
     */
    private static String getSetMethodName(String name) {
        StringBuffer buffer;

        if ((null == name) ||
            (0 == name.length())) {
            throw new IllegalArgumentException("The argument 'name' is null or empty.");
        }

        buffer = new StringBuffer();

        buffer.append("set").append(name);

        buffer.setCharAt(3, Character.toUpperCase(buffer.charAt(3)));
        
        return buffer.toString();
    }

    /**
     * Return the performance that corresponds to the group (optional)
     *
     * @param group the group
     * @return the performance that corresponds to the group (optional)
     */
    private static Performance getPerformance(Configuration configuration, String group) {
        int j;
        Performance performance;

        // dont care about speed
        for (int i = configuration.getPerformanceCount(); --i >= 0;) {
            performance = configuration.getPerformance(i);

            for (j = performance.getGroupCount(); --j >= 0;) {
                if (null == group 
                        ? null == performance.getGroup(j) 
                        : group.equals(performance.getGroup(j))) {
                    return performance;
                }
            }
        }

        return null;
    }

    public static void main (String args[]) {

        /*class TransactionTest 
            extends org.exolab.jtf.CWBaseApplication {
            private final Vector _categories;

            public TransactionTest(String s)
                throws CWClassConstructorException {
                super(s);

                _categories = new Vector();
                _categories.addElement("transaction.Transaction");
            }
        
            protected String getApplicationName() {
                return "TransactionTest";
            }
        
            protected Enumeration getCategoryClassNames() {
                return _categories.elements();
            }
        }

        try {
            TransactionTest test = new TransactionTest("Transaction Test");
            //System.out.println("test " + test);
            test.run(args);
        }
        catch(Exception exception) {
            exception.printStackTrace();
        }*/
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.BasicConfigurator.disableAll();
		tests.VerboseStream.verbose = true;
        TestSuite main = new Transaction( "Transaction Test", args[0]);
        
        junit.textui.TestRunner.run(main);
    }
    
}




