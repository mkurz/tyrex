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
 * Original code is Copyright (c) 1999-2001, Intalio, Inc. All Rights Reserved.
 *
 * Contributions by MetaBoss team are Copyright (c) 2003-2004, Softaris Pty. Ltd. All Rights Reserved.
 *
 * $Id: TransactionTestSuite.java,v 1.10 2004/12/15 06:25:56 metaboss Exp $
 */

package transaction;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.sql.XAConnection;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import transaction.configuration.Performance;
import tyrex.tm.TyrexTransaction;
import util.VerboseStream;


/**
 * Performs various tests with transactions using databases.
 * It tests both that the transactions perform the correct
 * behaviour and that the databases have the correct data
 * at all times. There is overlap between the some
 * of the tests.
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
 * <LI> Test performance for two phase commits.
 * <LI> Test performance for one phase commits.
 * <LI> Test performance for rollbacks.            
 * </UL>
 * <P>
 * The two-phase commit tests can only apply to multiple data sources so if a single
 * data source is defined then the two-phase commit tests are not run.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
class TransactionTestSuite
  extends TestSuite
    
{
    /**
     * Use two phase commit when testing commit.
     *
     * @see #testCommit
     */
    private static final int USE_2PC_COMMIT = 1;


    /**
     * Use one phase commit when testing commit.
     *
     * @see #testCommit
     */
    private static final int USE_1PC_COMMIT = 2;


    /**
     * Use two phase commit with one phase commit 
     * optimization when testing commit.
     *
     * @see #testCommit
     */
    private static final int USE_1PC_COMMIT_OPTIMIZATION = 3;


    /**
     * The default table index
     */
    private static final int DEFAULT_TABLE_INDEX = 0;

    /**
     * Synchronization before completion method failure
     *
     * @see #testSynchronizationWithFailure
     */
    private static final byte BEFORE_COMPLETION_FAILURE = 1;


    /**
     * Synchronization after completion method failure
     *
     * @see #testSynchronizationWithFailure
     */
    private static final byte AFTER_COMPLETION_FAILURE = 2;


    /**
     * One phase commit optimization could not be tested
     *
     * @see #testOnePhaseCommitOptimizationUsingMultipleDatasources
     */
    private static final int COULD_NOT_TEST_ONE_PHASE_COMMIT = 0;


    /**
     * One phase commit optimization test failed.
     *
     * @see #testOnePhaseCommitOptimizationUsingMultipleDatasources
     */
    private static final int ONE_PHASE_COMMIT_TEST_FAILED = 1;


    /**
     * One phase commit optimization test succeeded
     *
     * @see #testOnePhaseCommitOptimizationUsingMultipleDatasources
     */
    private static final int ONE_PHASE_COMMIT_TEST_SUCCEEDED = 2;


    /**
     * Test two-phase commit performace
     *
     *@see #performance
     */
    private static final int TWO_PHASE_COMMIT_PERFORMANCE_TEST = 1;


    /**
     * Test one-phase commit performace
     *
     *@see #performance
     */
    private static final int ONE_PHASE_COMMIT_PERFORMANCE_TEST = 2;


    /**
     * Test rolback performace
     *
     *@see #performance
     */
    private static final int ROLLBACK_PERFORMANCE_TEST = 3;

    /**
     * The transaction test case category
     */
    private static final String CATEGORY = "TransactionTestCase";

    /**
     * The parameter types for test class constructor
     */
    private static final Class[] TEST_CLASS_PARAMETER_TYPES = new Class[]{DataSourceGroupEntry.class};
    
    /**
     * The transaction domain
     */
    private static tyrex.tm.TransactionDomain _txDomain = null;
    
    /**
     * Verbose stream replacing the one from JTF
     */
    public static VerboseStream stream;   

    public TransactionTestSuite(String name, ArrayList groups, VerboseStream theStream, tyrex.tm.TransactionDomain txDomain )  //The database entry groups
    {
        super( name );
        
        stream = theStream;
        
        _txDomain = txDomain;
        
        Constructor[] constructors;
    
        int j;
        DataSourceGroupEntry group;
        Object[] arguments;
        TestCase tc;
        
        arguments = new Object[1];
        
        try {
            
            constructors = getTestClassConstructors();
            
            for (int i = 0; i < groups.size(); i++) {
                group = (DataSourceGroupEntry)groups.get(i);    
                
              
                for (j = constructors.length; --j>=0 ;) {
                    arguments[0] = group;
                    tc = (TestCase)constructors[j].newInstance(arguments);
                    //System.out.println("adding " + tc.name() + " " + tc);
                    addTest(tc);  
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
    }

    /**
     * No instances
     */
    private TransactionTestSuite() {
    }

    /**
     * Return the constructor for the test class. It is assumed that the constructor
     * will take one argument of type {@link DataSourceGroupEntry}.
     *
     * @param testClass the test class (required)
     * @return the constructor for the test class.
     */
    static Constructor getTestClassConstructor(Class testClass) 
        throws Exception {
        if (null == testClass) {
            throw new IllegalArgumentException("The argument 'testClass' is null.");
        }

        return testClass.getDeclaredConstructor(TEST_CLASS_PARAMETER_TYPES);
    }

    /**
     * Return the constructors for the test classes defined in this class.
     *
     * @return the constructors for the test classes defined in this class.
     * @see #getTestClassConstructor
     */
    static Constructor[] getTestClassConstructors() 
        throws Exception {
        Class[] testClasses;
        Constructor[] constructors;

        testClasses = getTestClasses();

        constructors = new Constructor[testClasses.length];

        int j = 0;
        for (int i = constructors.length; --i >= 0;) {
            constructors[i] = getTestClassConstructor(testClasses[i]);    
        }
        //for (int i = 0; i<constructors.length;i++) {
        //    constructors[j++] = getTestClassConstructor(testClasses[i]);    
        //}

       return constructors;
       //return new Constructor[] {getTestClassConstructor(testClasses[testClasses.length-2]),
         //getTestClassConstructor(testClasses[testClasses.length-1])};
    }

    /**
     * Return the array of test case classes defined in this class
     *
     * @return the array of test case classes defined in this class
     */
    static Class[] getTestClasses() {
        Class[] classes;
        Class[] temp;
        int removed;
        int index;
        int i;

        classes = TransactionTestSuite.class.getDeclaredClasses();

        removed = 0;

        for (i = classes.length; --i >= 0;) {
            if (!TestCase.class.isAssignableFrom(classes[i])) {
                classes[i] = null;    
                ++removed;
            }
        }

        if (0 != removed) {
            temp = new Class[classes.length - removed];
            index = 0;

            for (i = classes.length; --i >= 0;) {
                if (null != classes[i]) {
                    temp[index++] = classes[i]; 
                }
            }

            return temp;
        }

        temp = new Class[classes.length];

        index = 0;

        for (i = classes.length; --i >= 0;) {
            temp[index++] = classes[i]; 
        }

        return temp;
    }


    /**
     * Test two phase commit
     */
    static class Test2PC
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Two-phase commit";
        
        /**
         * Create the Test2PC
         *
         *
         * @param group the group (required)
         */
        Test2PC(DataSourceGroupEntry group) {
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
         
            TransactionManager transactionManager = null;
              
            try {
                if (!_group.hasMultiple()) {
                    stream.writeVerbose("Cannot test two-phase commit");
                    return;
                }

                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    fail("Error: Failed to get transaction manager");
                }
    
                try {
                    
                    if (!testCommit(transactionManager, _group, stream, USE_2PC_COMMIT)) {
                        fail("Error: Failed two-phase commit");
                    } 
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail( "Error: Failed two-phase commit" );
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }
    
   
    /**
     * Test one phase commit
     */
    static class Test1PC
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "One-phase commit";
       
        /**
         * Create the Test1PC
         *
         *
         *
         * @param group the group (required)
         */
         Test1PC(DataSourceGroupEntry group) {
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
              
            TransactionManager transactionManager = null;

            try {
                try {
                    
                    // get the transaction manager
                    
                    transactionManager = _txDomain.getTransactionManager();
                    
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager ");
                }
    
                try {
                    
                    if (!testCommit(transactionManager, _group, stream, USE_1PC_COMMIT)) {
                       fail("Error: Failed one-phase commit");    
                    }

                    return;
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed one-phase commit");
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
            System.out.println("End1PC");
        }
    }

    /**
     * Test one phase commit optimization
     */
    static class Test1PCOptimization
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "One-phase commit optimization";
        
        /**
         * Create the Test1PCOptimization
         *
         *
         *
         * @param group the group (required)
         */
        Test1PCOptimization(DataSourceGroupEntry group) {
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;

            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    fail( "Error: Failed to get transaction manager " );
                }
    
                try {
                    if (!testCommit(transactionManager, _group, stream, USE_1PC_COMMIT_OPTIMIZATION)) {
                        fail("Error: Failed two-phase commit, with one-phase commit optimization" );
                    }
                    return;
                } catch ( Exception e ) {
                    e.printStackTrace();
                    fail( "Error: Failed two-phase commit, with one-phase commit optimization" );
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test rollback
     */
    static class TestRollback
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Rollback";

        /**
         * Create the TestRollback
         *
         *
         *
         * @param group the group (required)
         */
        TestRollback(DataSourceGroupEntry group) {
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            ArrayList entries;

            entries = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail( "Error: Failed to get transaction manager " );
                }
                
                try {
                    entries = getEntries(_group, false, stream);

                    insert(entries, stream);

                    if (!testRollback(transactionManager, entries, stream)) {
                        fail("Error: Failed rollback");
                    }
                    return;
                } 
                catch ( Exception e ) {
                    e.printStackTrace();
                    fail( "Error: Failed rollback" );
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
            finally {
                close(entries);
            }
        }
    }


    /**
     * Test mark for rollback on commits
     */
    static class TestMarkForRollbackOnCommit
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Mark for rollback on commit";

        /**
         * Create the TestMarkForRollbackOnCommit
         *
         *
         *
         * @param group the group (required)
         */
        TestMarkForRollbackOnCommit(DataSourceGroupEntry group) {
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testMarkForRollback(transactionManager, _group, stream, true)) {
                        fail("Error: Failed mark for rollback on commit");
                    }
                    return;
                } 
                catch ( Exception e ) {
                    e.printStackTrace();
                    fail("Error: Failed mark for rollback on commit");
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());;
            }
        }
    }

    /**
     * Test mark for rollback on rollbacks
     */
    static class TestMarkForRollbackOnRollback
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Mark for rollback on rollback";

        /**
         * Create the TestMarkForRollbackOnRollback
         *
         *
         *
         * @param group the group (required)
         */
        TestMarkForRollbackOnRollback(DataSourceGroupEntry group) {
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testMarkForRollback(transactionManager, _group, stream, false)) {
                        fail("Error: Failed mark for rollback on rollback");
                    }
                    return;
                } 
                catch ( Exception e ) {
                    e.printStackTrace();
                    fail("Error: Failed mark for rollback on rollback");
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test resource delist on commits
     */
    static class TestResourceDelistOnCommit
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Resource delisting on commits";

        /**
         * Create the TestResourceDelistOnCommit
         *
         *
         *
         * @param group the group (required)
         */
        TestResourceDelistOnCommit(DataSourceGroupEntry group) {
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;

            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }

                try {
                    if (!testDelist(transactionManager, _group, stream, false, true, false)) {
                        fail("Error: Failed to commit with resource delist");
                    }
                } catch ( Exception e ) {
                    e.printStackTrace();
                    fail("Error: Failed to commit with resource delist" );
                }
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test resource delist on rollback
     */
    static class TestResourceDelistOnRollback
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Resource delisting on rollback";

        /**
         * Create the TestResourceDelistOnRollback
         *
         *
         *
         * @param group the group (required)
         */
        TestResourceDelistOnRollback(DataSourceGroupEntry group) {
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testDelist(transactionManager, _group, stream, false, false, false)) {
                        fail("Error: Failed to rollback with resource delist");
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to rollback with resource delist");
                  
                }
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test resource delist and reuse on commit
     */
    static class TestResourceDelistAndReuseOnCommit
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Resource delist and reuse on commit";

        /**
         * Create the TestResourceDelistAndReuseOnCommit
         *
         *
         *
         * @param group the group (required)
         */
        TestResourceDelistAndReuseOnCommit(DataSourceGroupEntry group) {
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testDelist(transactionManager, _group, stream, false, true, true)) {
                        fail("Error: Failed to commit with resource delist and resource reuse");
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to commit with resource delist and resource reuse");
                    
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test resource delist and reuse on rollback
     */
    static class TestResourceDelistAndReuseOnRollback
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Resource delist and reuse on rollback";

        /**
         * Create the TestResourceDelistAndReuseOnRollback
         *
         *
         *
         * @param group the group (required)
         */
        TestResourceDelistAndReuseOnRollback(DataSourceGroupEntry group) {
            
            super("[" + group.getGroupName() + "] " + NAME);
         
            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testDelist( transactionManager, _group, stream, false, false, true)) {
                        fail("Error: Failed to rollback with resource delist and resource reuse");

                    }
                } 
                catch ( Exception e ) {
                    e.printStackTrace();
                    fail("Error: Failed to rollback with resource delist and resource reuse");
                   
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }

    /**
     * Test resource delist on one phase commit optimization
     */
    static class TestResourceDelistOn1PCommit
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "One-phase commit optimization, with resource delist";

        /**
         * Create the TestResourceDelistAndReuseOn1PCommit
         *
         *
         *
         * @param group the group (required)
         */
        TestResourceDelistOn1PCommit(DataSourceGroupEntry group) {
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;

            try {
                if (!_group.hasMultiple()) {
                    stream.writeVerbose("Unable to test one-phase commit optimization, with resource delist.");
                    return;
                }
                
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testDelist( transactionManager, _group, stream, true, true, false)) {
                        fail("Error: Failed to commit, using one-phase commit optimization, with resource delist");
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to commit, using one-phase commit optimization, with resource delist");
                  
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test resource delist and reuse on one phase commit optimization
     */
    static class TestResourceDelistAndReuseOn1PCommit
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "One-phase commit optimization, with resource delist and resource reuse";

        /**
         * Create the TestResourceDelistAndReuseOn1PCommit
         *
         *
         *
         * @param group the group (required)
         */
        TestResourceDelistAndReuseOn1PCommit(DataSourceGroupEntry group) {
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;

            try {
                if (!_group.hasMultiple()) {
                    stream.writeVerbose("Unable to test one-phase commit optimization, with resource delist and resource reuse.");
                    return;
                }
                
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testDelist(transactionManager, _group, stream, true, true, true)) {
                        fail("Error: Failed to commit, using one-phase commit optimization, with resource delist and resource reuse");
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to commit, using one-phase commit optimization, with resource delist and resource reuse");
                   
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }

    /**
     * Test rollback with resource delist
     */
    static class TestRollbackWithResourceDelist
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Rollback with resource delist";

        /**
         * Create the TestRollbackWithResourceDelist
         *
         *
         *
         * @param group the group (required)
         */
        TestRollbackWithResourceDelist(DataSourceGroupEntry group){
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                if (!_group.canTestFailedDelist()) {
                    stream.writeVerbose("Unable to test rollback with resource failure.");
                    return;    
                }

                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                    
                }
                
                try {
                    if (!testRollbackWithFailedDelist( transactionManager, _group, stream)) {
                        fail("Error: Failed to rollback with resource delist");
                            
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to rollback with resource delist");
                  
                }
                
                return;
            }
            catch(java.lang.Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }
    


    /**
     * Test synchronization behaviour during commit
     */
    static class TestCommitSynchronization
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Synchronization during commit";

        /**
         * Create the TestCommitSynchronization
         *
         *
         *
         * @param group the group (required)
         */
        TestCommitSynchronization(DataSourceGroupEntry group){
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testSynchronization(transactionManager, _group, stream, true)) {
                        fail("Error: Failed to test synchronization during commit");
                     }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to Failed to test synchronization during commit");
   
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test synchronization behaviour during rollback
     */
    static class TestRollbackSynchronization
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Synchronization during rollback";

        /**
         * Create the TestRollbackSynchronization
         *
         *
         *
         * @param group the group (required)
         */
        TestRollbackSynchronization(DataSourceGroupEntry group) { 
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testSynchronization(transactionManager, _group, stream, false)) {
                        fail("Error: Failed to test synchronization during rollback");
 
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to Failed to test synchronization during rollback");
                   
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test synchronization behaviour with before completion failure during commit
     */
    static class TestSynchronizationWithBeforeFailureOnCommit
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Synchronization (beforeCompletion failure) during commit";

        /**
         * Create the testSynchronizationWithFailure
         *
         *
         *
         * @param group the group (required)
         */
        TestSynchronizationWithBeforeFailureOnCommit(DataSourceGroupEntry group){
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testSynchronizationWithFailure( transactionManager, _group, stream, true, 
                                                          BEFORE_COMPLETION_FAILURE)) {
                        fail( "Error: Failed to test synchronization (beforeCompletion failure) during commit" );
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to test synchronization (beforeCompletion failure) during commit");
                    stream.writeVerbose(e.toString()); 
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test synchronization behaviour with after completion failure during commit
     */
    static class TestSynchronizationWithAfterFailureOnCommit
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Synchronization (afterCompletion failure) during commit";

        /**
         * Create the TestSynchronizationWithAfterFailureOnCommit
         *
         *
         *
         * @param group the group (required)
         */
        TestSynchronizationWithAfterFailureOnCommit(DataSourceGroupEntry group){
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testSynchronizationWithFailure( transactionManager, _group, stream, true, 
                                                          AFTER_COMPLETION_FAILURE)) {
                        fail( "Error: Failed to test synchronization (afterCompletion failure) during commit" );
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to test synchronization (afterCompletion failure) during commit");
                   
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test synchronization behaviour with before and after completion failure during commit
     */
    static class TestSynchronizationWithFailureOnCommit
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Synchronization (beforeCompletion and afterCompletion failure) during commit";

        /**
         * Create the TestSynchronizationWithFailureOnCommit
         *
         *
         *
         * @param group the group (required)
         */
        TestSynchronizationWithFailureOnCommit(DataSourceGroupEntry group){
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testSynchronizationWithFailure( transactionManager, _group, stream, true, 
                                                          (byte) (BEFORE_COMPLETION_FAILURE | AFTER_COMPLETION_FAILURE))) {
                        fail( "Error: Failed to test synchronization (beforeCompletion and afterCompletion failure) during commit" );
          
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to test synchronization (beforeCompletion and afterCompletion failure) during commit");
                   
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test synchronization behaviour with before completion failure during rollback
     */
    static class TestSynchronizationWithBeforeFailureOnRollback
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Synchronization (beforeCompletion failure) during rollback";

        /**
         * Create the TestSynchronizationWithBeforeFailureOnRollback
         *
         *
         *
         * @param group the group (required)
         */
        TestSynchronizationWithBeforeFailureOnRollback(DataSourceGroupEntry group){
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    if (!testSynchronizationWithFailure( transactionManager, _group, stream, false, 
                                                          BEFORE_COMPLETION_FAILURE)) {
                        fail( "Error: Failed to test synchronization (beforeCompletion failure) during rollback" );
  
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to test synchronization (beforeCompletion failure) during rollback");
                  
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test synchronization behaviour with after completion failure during rollback
     */
    static class TestSynchronizationWithAfterFailureOnRollback
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Synchronization (afterCompletion failure) during rollback";

        /**
         * Create the TestSynchronizationWithAfterFailureOnRollback
         *
         *
         *
         * @param group the group (required)
         */
        TestSynchronizationWithAfterFailureOnRollback(DataSourceGroupEntry group){
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
    
                }
                
                try {
                    if (!testSynchronizationWithFailure( transactionManager, _group, stream, false, 
                                                          AFTER_COMPLETION_FAILURE)) {
                        fail( "Error: Failed to test synchronization (afterCompletion failure) during rollback" );

                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to test synchronization (afterCompletion failure) during rollback");
                   
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test synchronization behaviour with before and after completion failure during rollback
     */
    static class TestSynchronizationWithFailureOnRollback
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Synchronization (beforeCompletion and afterCompletion failure) during rollback";

        /**
         * Create the TestSynchronizationWithFailureOnRollback
         *
         *
         *
         * @param group the group (required)
         */
        TestSynchronizationWithFailureOnRollback(DataSourceGroupEntry group){
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            
            try {
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                  }
                
                try {
                    if (!testSynchronizationWithFailure( transactionManager, _group, stream, false, 
                                                          (byte) (BEFORE_COMPLETION_FAILURE | AFTER_COMPLETION_FAILURE))) {
                        fail( "Error: Failed to test synchronization (beforeCompletion and afterCompletion failure) during rollback" );
   
                    }
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to test synchronization (beforeCompletion and afterCompletion failure) during rollback");
                    
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test one phase commit optimization with multiple data sources
     */
    static class Test1PCOptimizationWithMultipleDataSources
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "One-phase commit optimization using multiple data sources";

        /**
         * Create the Test1PCOptimizationWithMultipleDataSources
         *
         *
         *
         * @param group the group (required)
         */
        Test1PCOptimizationWithMultipleDataSources(DataSourceGroupEntry group){
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            int result;
            
            try {
                if (!_group.canTestMultiplePerformance()) {
          
                    stream.writeVerbose("Unable to test one-phase commit optimization using multiple data sources.");
                    return;
                }
                
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    fail("Error: Failed to get transaction manager");
                }
                
                try {
                    result  = testOnePhaseCommitOptimizationUsingMultipleDatasources(transactionManager, _group, stream);
    
                    switch (result) {
                        case COULD_NOT_TEST_ONE_PHASE_COMMIT:
                            //stream.writeVerbose( "Could not test one phase commit optimization." );
                            break;
                        case ONE_PHASE_COMMIT_TEST_FAILED:
                            fail( "Error: Failed to test one phase commit optimization using multiple data sources." );
                              
                        default:
                            if (ONE_PHASE_COMMIT_TEST_SUCCEEDED != result) {
                                fail( "Error: Unknown result from one phase commit optimization test using multiple data sources." );
                 
                            }
                    }
                } 
                catch ( Exception e ) {
                    fail("Error: Failed to test one phase commit optimization using multiple data sources");
                    
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test two phase commit performance
     */
    static class Test2PCPerformance
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Two-phase commit performance";

        /**
         * Create the Test2PCPerformance
         *
         *
         *
         * @param group the group (required)
         */
        Test2PCPerformance(DataSourceGroupEntry group){
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            int result;
            
            try {
                if (!_group.canTestMultiplePerformance()) {
                    stream.writeVerbose("Unable to test two-phase commit performance.");
                    return;
                }
                
                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    fail("Error: Failed to get transaction manager");

                }
                
                try {
                    performance(transactionManager, _group, stream, TWO_PHASE_COMMIT_PERFORMANCE_TEST);                        
                } 
                catch (Exception e) {
                    fail("Error: Performance two-phase commit failed");
                 
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test one phase commit performance
     */
    static class Test1PCPerformance
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "One-phase commit performance";

        /**
         * Create the Test1PCPerformance
         *
         *
         *
         * @param group the group (required)
         */
        Test1PCPerformance(DataSourceGroupEntry group){
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            TransactionManager transactionManager = null;
            int result;
            
            try {
                if (!_group.canTestPerformance()) {
                    stream.writeVerbose("Unable to test two-phase commit performance.");
                    return;
                }

                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    fail("Error: Failed to get transaction manager");

                }
                
                try {
                    performance(transactionManager, _group, stream, ONE_PHASE_COMMIT_PERFORMANCE_TEST);
                } 
                catch (Exception e) {
                    fail("Error: Performance one-phase commit failed");
                    
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
    }


    /**
     * Test rollback performance
     */
    static class TestRollbackPerformance
        extends TestCase {
        
        /**
         * The group
         */
        private final DataSourceGroupEntry _group;

        /**
         * The name
         */
        static final String NAME = "Rollback performance";

        /**
         * Create the TestRollbackPerformance
         *
         *
         *
         * @param group the group (required)
         */
        TestRollbackPerformance(DataSourceGroupEntry group){
            
            super("[" + group.getGroupName() + "] " + NAME);

            if (null == group) {
                throw new IllegalArgumentException("The argument 'group' is null.");
            }

            _group = group;
        }

        public void runTest()
        {
            
            TransactionManager transactionManager = null;
            int result;
            
            try {
                if (!_group.canTestPerformance()) {
                    stream.writeVerbose("Unable to test rollback performance.");
                    return;
                }

                try {
                    
                    // get the transaction manager
                    transactionManager = _txDomain.getTransactionManager();
                } 
                catch (Exception e) {
                    fail("Error: Failed to get transaction manager");
        
                }
                
                try {
                    performance(transactionManager, _group, stream, ROLLBACK_PERFORMANCE_TEST);
                } 
                catch (Exception e) {
                    fail("Error: Performance rollback failed");
                   
                }
                
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }
        }
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
    static private boolean transactionBoundary( TransactionManager transactionManager,
                                         VerboseStream stream,
                                         boolean commit )
        throws Exception
    {
        if ( commit ) {
            transactionManager.commit();    
        } else {
            transactionManager.rollback();
        }
        
        if ( null != transactionManager.getTransaction() ) {
            stream.writeVerbose("Thread still associated with transaction after transaction boundary");
            return false;
        }

        return true;
    }
    

    /**
     * Test rollback with the xa resources being delisted with the
     * XAResource.TMFAIL flag.
     *
     * @param transactionManager the transaction manager
     * @param group the group
     * @param stream the logging stream
     * @return True if the test was successful. Return false otherwise.
     * @throws Exception if there is an error with the test.
     * @see Entry
     */
    static private boolean testRollbackWithFailedDelist(TransactionManager transactionManager,
                                                        DataSourceGroupEntry group,
                                                        VerboseStream stream )
        throws Exception
    {
        Entry entry;
        ArrayList entries;
        int i;
        long time;
        
        stream.writeVerbose("Test rollback with resource delist (TMFAIL). This test may cause the database to hang so it may be preferable to set the timeout for database locks to a small number");
                
        entries = getEntries(group, false, stream);

        insert(entries, stream);
        time = 0;

        try {
            transactionManager.begin();
    
            for (i = 0; i < entries.size(); ++i) {
                entry = (Entry)entries.get(i);
    
                if (!update(transactionManager, entry, entry._value + "new", stream)) {
                    return false;    
                }
            }
    
            for (i = 0; i < entries.size(); ++i) {
                entry = (Entry)entries.get(i);

                if (entry._dataSourceEntry.getFailSleepTime() < 0) {
                    continue;    
                }

                transactionManager.getTransaction().delistResource(entry._xaResource, XAResource.TMFAIL);

                //System.out.println("old xa connection " + entry.xaConnection);
                //System.out.println("old xa resource " + entry.xaResource);
                try {
                    entry._xaConnection.close();
                }
                catch(SQLException e) {
                    // ignore
                    // the xa connection may be closed automatically on fail
                }
                entry._xaConnection = entry._dataSourceEntry.getXAConnection(); 
                entry._xaResource = entry._xaConnection.getXAResource();
                ///*System.out.println("timeout " + entry.xaResource.getTransactionTimeout());
                entry._xaResource.setTransactionTimeout(200);
                //System.out.println("new xa connection " + entry.xaConnection);
                //System.out.println("new xa resource " + entry.xaResource);

                time += entry._dataSourceEntry.getFailSleepTime();
                
            }
    
            // the status should be rollback
            if (transactionManager.getStatus() != Status.STATUS_MARKED_ROLLBACK) {
                stream.writeVerbose("Transaction not marked for rollback");    
                return false;
            }

            if (time > 0) {
                Thread.sleep(time);
            }

            if (!transactionBoundary(transactionManager, stream, false)) {
                return false;
            }
    
            return checkValues(entries, stream);
        }
        finally {
            close(entries);
        }

    }
    

    /**
     * Test set rollback only on a transaction that
     * is to be committed or rolled back depending
     * on the commit flag.
     *
     * @param transactionManager the transaction manager
     * @param group the group
     * @param stream the logging stream
     * @param commit True if the transaction is to be commited. False if
     *      the transaction is to be rollbed back.
     * @return True if the test was successful. Return false otherwise.
     * @throws Exception if there is an error with the test.
     * @see Entry
     */
    static private boolean testMarkForRollback(TransactionManager transactionManager,
                                        DataSourceGroupEntry group,
                                        VerboseStream stream,
                                        boolean commit)
        throws Exception
    {
        Entry entry;
        ArrayList entries;
        String transactionType;
        boolean rollbackExceptionOccurred;
         
        transactionType = commit ? "commit" : "rollback";

        entries = getEntries(group, false, stream);

        stream.writeVerbose("Test mark for rollback during " + transactionType);

        insert(entries, stream);
                
        try {
            transactionManager.begin();
    
            for (int i = 0; i < entries.size(); ++i) {
                entry = (Entry)entries.get(i);
    
                if (!update(transactionManager, entry, entry._value + "new", stream)) {
                    return false;    
                }
            }
    
            transactionManager.getTransaction().setRollbackOnly();
            
            // the status should be rollback
            if (transactionManager.getStatus() != Status.STATUS_MARKED_ROLLBACK) {
                stream.writeVerbose("Transaction not marked for rollback during " + transactionType);    
                return false;
            }
    
            if (commit) {
                rollbackExceptionOccurred = false;
    
                try {
                    if (!transactionBoundary(transactionManager, stream, true)) {
                        return false;
                    }
                }
                catch (RollbackException e) {
                    rollbackExceptionOccurred = true;    
                }
    
                if (!rollbackExceptionOccurred) {
                    stream.writeVerbose( "Rollback exception not thrown during " + transactionType );
                }
    
            } 
            else {
                if (!transactionBoundary(transactionManager, stream, false)) {
                        return false;
                }
            }
            
            return checkValues(entries, stream);
        }
        finally {
            close(entries);
        }
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
     * @throws Exception if there is a problem writing to
     *      the logging stream
     * @throws SQLException if there is a problem checking the 
     *      value in the data source
     * @see Entry
     */
    static private boolean checkValues(ArrayList entries, VerboseStream stream)
        throws SQLException, Exception
    {
        Entry entry;

        for (int i = entries.size(); --i >= 0;) {
            entry = (Entry)entries.get(i);
            if (!checkValue(entry, entry._value, stream)) {
                return false;                
            }
        }

        return true;
    }

    /**
     * Return false if the value in the database does not match 
     * the specified value. Return true otherwise.
     *
     * @param entry the entry
     * @param value the value
     * @param stream the logging stream used to log information
     * @return true if the value in the database matches
     *      the specified value. Return false 
     *      otherwise.
     * @throws Exception if there is a problem writing to
     *      the logging stream
     * @throws SQLException if there is a problem checking the 
     *      value in the data source
     * @see Entry
     */
    static private boolean checkValue(Entry entry, 
                               String value, 
                               VerboseStream stream)
        throws SQLException, Exception
    {
        Connection connection = getConnection(entry);

        try {
            if (!checkValue(connection, entry._dataSourceEntry.getTableName(), entry._key, value)) {
                stream.writeVerbose("Values don't match for data source " + entry._dataSourceEntry.getName());
                return false;    
            }
        } 
        finally {
            closeConnection(connection);
        }
    
        return true;
    }


    /**
     * Return false if the value in the database does not match 
     * the specified value. Return true otherwise.
     *
     * @param connection the connection
     * @param tableName the name of the table
     * @param key the key
     * @param value the value
     * @param stream the logging stream used to log information
     * @return true if the value in the database matches
     *      the specified value. Return false 
     *      otherwise.
     * @throws SQLException if there is a problem checking the 
     *      value in the data source
     * @see Entry
     */
    static private boolean checkValue(Connection connection, 
                               String tableName, 
                               String key,
                               String value)
        throws SQLException {
        ResultSet resultSet;
        Statement statement;

        resultSet = null;
        statement = null;

        try {
            statement = connection.createStatement();

            resultSet = statement.executeQuery("select " + 
                                               Transaction.VALUE_COLUMN_NAME + 
                                               " from " + 
                                               tableName + 
                                               " where " + 
                                               Transaction.PRIMARY_KEY_COLUMN_NAME + 
                                               " = '" + 
                                               key + "'");

            if (!resultSet.next()) {
                return false;    
            }

            return value.equals(resultSet.getString(1));
        }
        finally {
            if (null != resultSet) {
                try{    
                    resultSet.close();
                }
                catch(SQLException e) {
                }
            }

            if (null != statement) {
                try{    
                    statement.close();
                }
                catch(SQLException e) {
                }
            }
        }

    }

    
    /**
     * Update the value in the table
     *
     * @param connection the connection
     * @param tableName the name of the table
     * @param key the key
     * @param value the new value.
     * @param stream the logging stream
     * @throws Exception if there is a problem updating
     * @see Entry
     */
    static private void update(Connection connection, 
                                  String tableName,
                                  String key,
                                  String value)
        throws SQLException {
        Statement statement;

        statement = null;

        try {
            statement = connection.createStatement();

            statement.executeUpdate("update " + tableName + 
                               " set " + Transaction.VALUE_COLUMN_NAME + 
                               " = '" + value + "' where " + 
                               Transaction.PRIMARY_KEY_COLUMN_NAME + 
                               " = '" + key + "'"); 
        }
        finally {
            if (null != statement) {
                try {
                    statement.close();
                }
                catch(SQLException e) {
                }
            }
        }
    }


    /**
     * Update the data source using
     * the specified key and value.
     * The XA resource is enlisted in the current
     * transaction before the data source is changed.
     *
     * @param transactionManager the transaction manager
     * @param entry the entry
     * @param value the new value.
     * @param stream the logging stream
     * @return true if the update happened. Return false otherwise.
     * @throws SQLException if there is a problem updating
     * @throws Exception if there is a problem writing to the stream
     * @throws SystemException if there is a problem enlisting the resouce
     * @throws RollbackException if the current transaction is marked for rollback
     * @see Entry
     */
    static private boolean update( TransactionManager transactionManager,
                            Entry entry, 
                            String value, 
                            VerboseStream stream)
        throws SQLException, Exception, SystemException, RollbackException
    {
        Connection connection;

        connection = null;

        try {
            // enlist the xa resource
            if (!transactionManager.getTransaction().enlistResource(entry._xaResource)) {
                stream.writeVerbose( "Failed to enlist resource" );
                return false;
            }
            
            connection = getConnection(entry);
            // update value
            update(connection, entry._dataSourceEntry.getTableName(), 
                   entry._key, value);
            
            // make sure the value changed
            if (!checkValue(connection, entry._dataSourceEntry.getTableName(), entry._key, value)) {
                stream.writeVerbose( "Update failed for table in data source " + 
                                     entry._dataSourceEntry.getName() + 
                                     " for key " + 
                                     entry._key + 
                                     " and value " + value );
                return false;    
            }
            
            return true;
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Test that the synchronization methods are called
     * during a transaction boundary that fails.
     *
     * @param transactionManager the transaction manager
     * @param group the group
     * @param stream the logging stream
     * @param commit True if commit is tested. False if
     *     rollback is tested.
     * @param beforeCompletion True if the Synchronization fails
     *     on the beforeCompletion method
     * @return True if the test was successful. Return false otherwise.
     * @throws Exception if there is an error with the test.
     * @see Entry
     * @see #BEFORE_COMPLETION_FAILURE
     * @see #AFTER_COMPLETION_FAILURE
     */
    static private boolean testSynchronizationWithFailure(final TransactionManager transactionManager,
                                                          DataSourceGroupEntry group,
                                                          VerboseStream stream,
                                                          boolean commit,
                                                          final byte synchronizationFailure)
        throws Exception
    {
        Entry entry;
        ArrayList entries;
        String value;
        String transactionType;
        final int[] completionCalled = { -1, -1 };
        final Exception[] completionException = new Exception[ 1 ];
        final String forceRollbackString = "Force rollback";
        final boolean isBeforeCompletionFailure = ( synchronizationFailure & BEFORE_COMPLETION_FAILURE ) != 0;
        final boolean isAfterCompletionFailure = ( synchronizationFailure & AFTER_COMPLETION_FAILURE ) != 0;
    
        transactionType = (commit ? "commit" : "rollback");

        class FailureSynchronization implements Synchronization
        {
            public void afterCompletion(int mode) {
                if (isAfterCompletionFailure) {
                    throw new RuntimeException(forceRollbackString);        
                }
            }
        
            public void beforeCompletion() {
                if (isBeforeCompletionFailure) {
                    throw new RuntimeException(forceRollbackString);        
                }
            }
        };
        
        stream.writeVerbose( "Test Synchronization failure [" + 
                             (isBeforeCompletionFailure
                               ? " before completion"
                               : "" ) +
                             (isAfterCompletionFailure
                               ? " after completion"
                               : "" ) +
                             " ] during " + 
                             transactionType +
                             ".");

        entries = getEntries(group, false, stream);

        insert(entries, stream);
        
        try {
            transactionManager.begin();
            
             // add the synchronization to force the rollback
            transactionManager.getTransaction().registerSynchronization( new FailureSynchronization() );
            
             // add the synchronization
            transactionManager.getTransaction().registerSynchronization( new Synchronization() {
                    public void afterCompletion(int mode) {
                        completionCalled[1] = mode;
                    }
            
                    public void beforeCompletion() {
    
                        try {
                            // the status should be rollback
                            if (isBeforeCompletionFailure && 
                                (transactionManager.getStatus() != Status.STATUS_MARKED_ROLLBACK)) {
                                completionCalled[0] = 1;
                            } 
                            else {
                                completionCalled[0] = 0;
                            }
                        } 
                        catch (Exception e) {
                            completionException[0] = e;
                        }
                    }
                });
            
            // add the synchronization to force the rollback
            transactionManager.getTransaction().registerSynchronization(new FailureSynchronization());
            
            for (int i = 0; i < entries.size(); ++i) {
                 entry = (Entry)entries.get(i);
            
                 value = entry._value + "new";
            
                 if (commit && !isBeforeCompletionFailure) {
                    entry._value = value;    
                 }
                 
                 if (!update(transactionManager, entry, value, stream)) {
                     return false;
                 }
             }
            
            if (commit) {
                boolean rollbackExceptionOccurred = false;
             
                try {
                    if (!transactionBoundary(transactionManager, stream, true)) {
                        return false;
                    }
                } 
                catch (HeuristicRollbackException e) {
                    if (isBeforeCompletionFailure) {
                        rollbackExceptionOccurred = true;    
                    } 
                    else {
                        throw e;
                    }
                } 
                catch (SystemException e) {
					if (-1 == e.getMessage().indexOf(forceRollbackString)) {
                        throw e;
                    }
                }
            
                if (isBeforeCompletionFailure && 
                    !rollbackExceptionOccurred) {
                    stream.writeVerbose("Rollback exception not thrown");
                    return false;
                }

				if (null != completionException[0]) {
					throw completionException[0];    
				}
		
				if (commit && (completionCalled[0] == -1)) {
					stream.writeVerbose("Synchronization beforeCompletion method not called");    
					return false;
				}

				if (!commit && (completionCalled[0] != -1)) {
					stream.writeVerbose("Synchronization beforeCompletion method called");    
					return false;
				}
		
				if (isBeforeCompletionFailure &&
					(completionCalled[0] == 1)) {
					stream.writeVerbose("Transaction not marked for rollback during beforeCompletion");    
					return false;
				}
            } 
            else {
                try {
                    if (!transactionBoundary(transactionManager, stream, false)) {
                        return false;
                    }
                } 
                catch (SystemException e) {
					if (-1 == e.getMessage().indexOf(forceRollbackString)) {
                        throw e;
                    }
                }
            }
            
            if (completionCalled[1] == -1) {
                stream.writeVerbose("Synchronization afterCompletion method not called");    
                return false;
            }
            
            if (completionCalled[1] != (isBeforeCompletionFailure || !commit ? Status.STATUS_ROLLEDBACK : Status.STATUS_COMMITTED)) {
                stream.writeVerbose("Synchronization afterCompletion method called with incorrect status - " + 
                                    completionCalled[1]);    
                return false;
            }
            
            if (!checkValues(entries, stream)) {
                stream.writeVerbose("Failed to " + transactionType);
                return false;
            }
            
            return true;
        }
        finally {
            close(entries);
        }
    }
    

    /**
     * Test that the one phase commit optimization using the
     * multiple data sources. 
     * <P>
     * If the resource managers from multiple data sources can
     * be shared then test one phase commit optimization.
     *
     * @param transactionManager the transaction manager
     * @param group the group
     * @param stream the logging stream
     * @return {@link #COULD_NOT_TEST_ONE_PHASE_COMMIT}
     *     if one phase commit optimization could not be tested;
     *     {@link #ONE_PHASE_COMMIT_TEST_FAILED} if the test failed; 
     *     {@link #ONE_PHASE_COMMIT_TEST_SUCCEEDED} if it succeeded.
     * @throws Exception if there is an error with the test.
     * @see Entry
     * @see #COULD_NOT_TEST_ONE_PHASE_COMMIT
     * @see #ONE_PHASE_COMMIT_TEST_FAILED
     * @see #ONE_PHASE_COMMIT_TEST_SUCCEEDED
     */
    static private int testOnePhaseCommitOptimizationUsingMultipleDatasources( final TransactionManager transactionManager,
                                                                     DataSourceGroupEntry group,
                                                                     VerboseStream stream)
        throws Exception
    {
    
        Entry entry;
        Entry temp;
        ArrayList entries;
        int j;
        int i;
        ArrayList sharedEntries;
        int entriesLength;
        boolean found;
        final boolean[] onePhaseFailed = new boolean[]{false};

        sharedEntries = new ArrayList();
        
        stream.writeVerbose("Test one phase commit phase optimization using multiple data sources");

        entries = getEntries(group, false, stream);

        insert(entries, stream);

        entriesLength = entries.size();

        try {
            for (j = 0; j < entriesLength; ++j ) {
                
                entry = (Entry)entries.get(j);

                if (!sharedEntries.contains(entry)) {
                    found = false;
                    for (i = j + 1; i < entriesLength; ++i) {
                        temp = (Entry)entries.get(i);

                        if (temp._xaResource.isSameRM(entry._xaResource)) {
                            sharedEntries.add(temp);
                            found = true;
                        }

                        if (found) {
                            sharedEntries.add(entry);    
                        }
                    }
                }
            }
            
            if (sharedEntries.isEmpty()) {
                return COULD_NOT_TEST_ONE_PHASE_COMMIT;    
            }
            
            transactionManager.begin();
    
            transactionManager.getTransaction().registerSynchronization( new Synchronization() {
                            public void afterCompletion(int mode) {
    
                            }
    
                            public void beforeCompletion() {
                                try {
                                    if (transactionManager.getStatus() != Status.STATUS_COMMITTING) {
                                    onePhaseFailed[0] = true;    
                                    }
                                } 
                                catch (SystemException e) {
                                    throw new RuntimeException(e.toString());
                                }
                            }
                        } );
            
            for (j = 0; j < sharedEntries.size() ; ++j) {
                entry = (Entry)sharedEntries.get(j);
    
                entry._value += "new";
                
                if (!update(transactionManager,
                            entry, 
                            entry._value, 
                            stream ) ) {
                    return ONE_PHASE_COMMIT_TEST_FAILED;
                }
            }
            
            if (!((TyrexTransaction)transactionManager.getTransaction()).canUseOnePhaseCommit()) {
                stream.writeVerbose("Cannot use one phase commit optimization");    
                return ONE_PHASE_COMMIT_TEST_FAILED;
            }
    
            if (!transactionBoundary(transactionManager, stream, true)) {
                return ONE_PHASE_COMMIT_TEST_FAILED;
            }
            
            if (!onePhaseFailed[0] ||
                !checkValues(sharedEntries, stream)) {
                return ONE_PHASE_COMMIT_TEST_FAILED;        
            }
    
            return ONE_PHASE_COMMIT_TEST_SUCCEEDED;
        }
        finally {
            close(entries);
        }
    }
     
    
     /**
      * Test that the synchronization methods are called
      * during a transaction boundary
      *
      * @param transactionManager the transaction manager
      * @param group the group
      * @param stream the logging stream
      * @param commit True if commit is tested. False if
      *     rollback is tested.
      * @return True if the test was successful. Return false otherwise.
      * @throws Exception if there is an error with the test.
      * @see Entry
      */
    static private boolean testSynchronization( TransactionManager transactionManager,
                                          DataSourceGroupEntry group,
                                          VerboseStream stream,
                                          boolean commit )
        throws Exception
    {
        Entry entry;
        ArrayList entries;
        String testType;
        String value;
        final int[] completionCalled = {-1, -1};
        
        testType = (commit ? "commit" : "rollback");
        
        stream.writeVerbose("Test Synchronization during " + 
                            testType );

        entries = getEntries(group, false, stream);

        insert(entries, stream);

        try {
            transactionManager.begin();

             // add the synchronization to force the rollback
            transactionManager.getTransaction().registerSynchronization( new Synchronization()
                {
                    public void afterCompletion(int mode) {
                        completionCalled[1] = mode;
                    }
    
                    public void beforeCompletion() {
                        completionCalled[0] = 0;
                    }
                });
    
            for (int i = 0; i < entries.size(); ++i) {
                entry = (Entry)entries.get(i);
    
                if (commit) {
                    entry._value += "new";
                    value = entry._value;
                }
                else {
                    value = entry._value + "new";
                }
    
                if (!update(transactionManager, entry, value, stream)) {
                    return false;
                }
            }
    
    
            if (!transactionBoundary(transactionManager, stream, commit)) {
                return false;
            }
             
            if (!checkValues(entries, stream)) {
                stream.writeVerbose("Failed to " + testType);
                return false;
            }
    
            if (commit && (completionCalled[0] == -1)) {
                stream.writeVerbose("Synchronization beforeCompletion method not called");    
                return false;
            }

			if (!commit && (completionCalled[0] != -1)) {
                stream.writeVerbose("Synchronization beforeCompletion method called");    
                return false;
            }
    
            if (completionCalled[1] == -1) {
                stream.writeVerbose( "Synchronization afterCompletion method not called" );    
                return false;
            }
    
            if (commit 
                    ? (completionCalled[1] != Status.STATUS_COMMITTED) 
                    : (completionCalled[1] != Status.STATUS_ROLLEDBACK)) {
                stream.writeVerbose("Synchronization afterCompletion method called with incorrect status - " + 
                                    completionCalled[1]);    
            }
    
            return true;
        }
        finally {
            close(entries);
        }
    }
     

    /**
     * Insert the specified key and value in the
     * data source using the specified xa connection.
     * The XA resource is enlisted in the current
     * transaction before the data source is changed.
     *
     * @param transactionManager the transaction manager
     * @param entry the entry
     * @param value the new value.
     * @param stream the logging stream
     * @return true if the update happened. Return false otherwise.
     * @throws Exception if there is a problem updating
     */
    static private boolean insert(TransactionManager transactionManager,
                                  Entry entry,
                                  String value,
                                  VerboseStream stream)
        throws Exception {
        return insert(transactionManager, entry, entry._key, value, stream);
    }

    /**
     * Insert the specified key and value in the
     * data source using the specified xa connection.
     * The XA resource is enlisted in the current
     * transaction before the data source is changed.
     *
     * @param transactionManager the transaction manager
     * @param entry the entry
     * @param key the key. Assumed to be already existing
     *      in data source.
     * @param value the new value.
     * @param stream the logging stream
     * @return true if the update happened. Return false otherwise.
     * @throws Exception if there is a problem updating
     */
    static private boolean insert(TransactionManager transactionManager,
                                  Entry entry,
                                  String key,
                                  String value,
                                  VerboseStream stream)
        throws Exception
    {
        Connection connection;
        Statement stmt;

        // enlist the xa resource
        if (!transactionManager.getTransaction().enlistResource(entry._xaResource)) {
            stream.writeVerbose( "Failed to enlist resource" );
            return false;
        }
        connection = getConnection(entry);
        stmt = null;

        try {
            // insert key and value
            try {
            stmt = connection.createStatement();
    
            stmt.executeUpdate("insert into " + 
                               entry._dataSourceEntry.getTableName() + 
                               " values ('" + key + "', '" + value + "')");
            } 
            finally {
                if (null != stmt) {
                    try {    
                        stmt.close();
                    } 
                    catch (SQLException e) {
                    }
                }
            }

            // make sure the value changed
            if (!checkValue(connection, entry._dataSourceEntry.getTableName(), key, value)) {
                stream.writeVerbose( "Insert failed for table " + 
                                     entry._dataSourceEntry.getTableName() + 
                                     " in data source index " + 
                                     entry._dataSourceEntry.getName());
                return false;    
            }

            return true;
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Insert the data defined by the entries.
     *
     * @param entries the list of entry
     * @param stream the logging stream (optional)
     * @return true if the update happened. Return false otherwise.
     * @throws Exception if there is a problem inserting the data
     */
    static private boolean insert(ArrayList entries,
                                  VerboseStream stream)
        throws Exception {
        for (int i = entries.size(); --i >= 0;) {
            if (!insert((Entry)entries.get(i), stream)) {
                return false;
            }
        }

        return true;
    }


    /**
     * Insert the data defined by the specified entry.
     *
     * @param entry the entry
     * @param stream the logging stream (optional)
     * @return true if the update happened. Return false otherwise.
     * @throws Exception if there is a problem inserting the data
     */
    static private boolean insert(Entry entry,
                                  VerboseStream stream)
        throws Exception
    {
        TransactionManager transactionManager = null;
        Connection connection;
        Statement stmt;
        
        // get the transaction manager
        transactionManager = _txDomain.getTransactionManager();
        
        transactionManager.begin();

        // enlist the xa resource
        if (!transactionManager.getTransaction().enlistResource(entry._xaResource)) {
            if (null != stream) {
                stream.writeVerbose( "Failed to enlist resource" );
            }
            return false;
        }
        connection = getConnection(entry);
        stmt = null;

        try {
            // insert key and value
            try {
            stmt = connection.createStatement();
    
            stmt.executeUpdate("insert into " + 
                               entry._dataSourceEntry.getTableName() + 
                               " values ('" + entry._key + "', '" + entry._value + "')");
            } 
            finally {
                if (null != stmt) {
                    try {    
                        stmt.close();
                    } 
                    catch (SQLException e) {
                    }
                }
            }

            // make sure the value changed
            if (!checkValue(connection, entry._dataSourceEntry.getTableName(), entry._key, entry._value)) {
                if (null != stream) {
                    stream.writeVerbose( "Insert failed for table " + 
                                     entry._dataSourceEntry.getTableName() + 
                                     " in data source index " + 
                                     entry._dataSourceEntry.getName());
                }
                return false;    
            }

            return transactionBoundary(transactionManager, stream, true);
        } 
        finally {
            closeConnection(connection);
        }
    }



    /**
     * Test commit or rollback with the xa resources being delisted with the
     * XAResource.TMSUCCESS flag.
     *
     * @param transactionManager the transaction manager
     * @param entries the array of test entries
     * @param stream the logging stream
     * @param onePhaseCommit true if one phase commit 
     *      optimization is tested.
     * @param commit True if commit is performed. False if rollback
     *      is performed
     * @param reuse True is the delisted resources are reused in the
     *      same transaction.
     * @return True if the test was successful. Return false otherwise.
     * @throws Exception if there is an error with the test.
     * @see Entry
     */
    static private boolean testDelist(TransactionManager transactionManager,
                                      DataSourceGroupEntry group,
                                      VerboseStream stream,
                                      boolean onePhaseCommit,
                                      boolean commit,
                                      boolean reuse)
        throws Exception
    {
        Entry entry;
        ArrayList entries;
        ArrayList resuableEntries;
        int numberOfEntries;
        String value;
        int i;
        String[] newKeys;
        String[] newValues;
        Connection connection;

        entries = getEntries(group, onePhaseCommit, stream);

        if (reuse) {
            entries = getReusableEntries(entries);    

            if (0 == entries.size()) {
                stream.writeVerbose("Could not test " + 
                                    (commit ? "commit" : "rollback") +
                                    (onePhaseCommit 
                                        ? ", using one-phase commit optimization,"
                                        : "") +
                                    " with resource delist (TMSUCCESS)" +
                                    (reuse 
                                        ? " and resource re-enlistment in same transaction"
                                        : "" ));
                return true;    
            }
        }

        numberOfEntries = entries.size();
        newKeys = null;
        newValues = null;
        connection = null;

        stream.writeVerbose("Test " + 
                             (commit ? "commit" : "rollback") +
                             (onePhaseCommit 
                                ? ", using one-phase commit optimization,"
                                : "") +
                             " with resource delist (TMSUCCESS)" +
                             (reuse 
                                ? " and resource re-enlistment in same transaction"
                                : "" ));
                
        insert(entries, stream);

        transactionManager.begin();

        for (i = 0; i < numberOfEntries; ++i) {
            entry = (Entry)entries.get(i);

            value = entry._value + "new";

            if (commit) {
                entry._value = value;    
            }

            if (!update(transactionManager, entry, value, stream)) {
                return false;
            }
        }

        for (i = 0; i < numberOfEntries; ++i) {
            transactionManager.getTransaction().delistResource(((Entry)entries.get(i))._xaResource, XAResource.TMSUCCESS);
        }

        if (reuse) {
            newKeys = new String[numberOfEntries];
            newValues = new String[numberOfEntries];

            for (i = 0; i < numberOfEntries; ++i) {
                entry = (Entry)entries.get(i);

                newKeys[i] = generateKey();
                newValues[i] = generateValue();
    
                if (!insert(transactionManager,
                              entry,
                              newKeys[i],
                              newValues[i],
                              stream)) {
                    return false;
                }
            }
        } 
        else {
            resuableEntries = getReusableEntries(entries);

            if (!resuableEntries.isEmpty() &&
                !useDelistedXAResourcesInNewTransaction(transactionManager,
                                                         resuableEntries,
                                                         stream)) {
                return false;
            }
        }

        if (onePhaseCommit && !((TyrexTransaction)transactionManager.getTransaction()).canUseOnePhaseCommit()) {
            stream.writeVerbose("One phase commit optimization failed");
            return false;
        }

        if (!transactionBoundary(transactionManager, stream, commit)) {
            return false;
        }
        
        if (!checkValues(entries, stream)) {
            return false;
        }

        if (reuse) {
            for (i = 0; i < numberOfEntries; ++i ) {
                entry = (Entry)entries.get(i);

                connection = getConnection(entry);

                try {
                    if (!checkValue(connection,
                                entry._dataSourceEntry.getTableName(),
                                newKeys[i], newValues[i])) {
                    return false;    
                    }
                }
                finally {
                    closeConnection(connection);
                }

                connection = null;
            }
        }

        return true;
    }


    /**
     * Return the list of entries whose xa resources can be reused in the same transaction.
     *
     * @param entries the list of entries
     * @return the list of entries whose xa resources can be reused in the same transaction.
     */
    static private ArrayList getReusableEntries(ArrayList entries) {
        ArrayList  resuableEntries;
        Entry entry;

        resuableEntries = new ArrayList();

        for (int i = 0; i < entries.size(); ++i) {
            entry = (Entry)entries.get(i);

            if (entry._dataSourceEntry.getReuseDelistedXAResources()) {
                System.out.println("resuable " + entry._dataSourceEntry.getName());
                resuableEntries.add(entry);
            }
        }
        
        return resuableEntries;
    }

    /**
     * Use the XA resources in the specified entries that have been
     * delisted successfully (using XAResource.TMSUCCESS) in a transaction
     * that has not been committed, in a new transaction.
     *
     * @param transactionManager the transaction manager
     * @param entries the list of entries
     * @param stream the logging stream
     * @return True if the new transaction was successful. 
     *      Return false otherwise.
     * @throws Exception if there is an error with using the delisted
     *      XA resources.
     * @see Entry
     * @see #testDelist
     */
    static private boolean useDelistedXAResourcesInNewTransaction( final TransactionManager transactionManager,
                                                            final ArrayList entries,
                                                            final VerboseStream stream )
        throws Exception
    {
        final Object lock = new Object();

        final Object[] threadResults = new Object[1];

        synchronized (lock) {
            new Thread(new Runnable()
                        {
                            public void run() 
                            {
                                synchronized ( lock ) {
                                    try {
                                        threadResults[ 0 ] = testRollback(transactionManager, entries, stream) 
                                                            ? Boolean.TRUE 
                                                            : Boolean.FALSE;
                                    } catch ( Exception e ) {
                                        threadResults[0] = e;
                                    }
    
                                    lock.notify();
                                }
                            }
                        } ).start();
            lock.wait();
        }

        if (threadResults[ 0 ] != Boolean.TRUE) {
            stream.writeVerbose("Rollback in other transaction using delisted XA resources failed");
            
            if (threadResults[0] == Boolean.FALSE) {
                return false;
            }

            throw (Exception)threadResults[0];    
        }

        return false;
    }


    /**
     * Test the number of transactions that can be performed
     * per minute.
     *
     * @param transactionManager the transaction manager
     * @param entries the array of test entries
     * @param stream the logging stream
     * @param performanceMode mode for the performace test.
     * @return True if the test was successful. Return false otherwise.
     * @throws Exception if there is an error with the test.
     * @see #ONE_PHASE_COMMIT_PERFORMANCE_TEST
     * @see #TWO_PHASE_COMMIT_PERFORMANCE_TEST
     * @see #ROLLBACK_PERFORMANCE_TEST
     */
    static private boolean performance( TransactionManager transactionManager,
                                 DataSourceGroupEntry group,
                                 VerboseStream stream,
                                 int performanceMode )
        throws Exception
    {
        int numberOfEntries;
        ArrayList entries;
        Entry entry;
        Connection connection;
        int i;
        //Timing timing;
        String transactionType;
        int rate;
        int counter;

        rate = getPerformanceRate(group.getPerformance(), performanceMode);
        counter = getPerformanceCounter(group.getPerformance(), performanceMode);
        
        transactionType = ((ONE_PHASE_COMMIT_PERFORMANCE_TEST == performanceMode)
                            ? "one phase commit" 
                            : ((TWO_PHASE_COMMIT_PERFORMANCE_TEST == performanceMode)
                                ? "two phase commit"
                                : "rollback"));

        stream.writeVerbose("Performance: " + transactionType + " begin:");
        //timing = new Timing("Performance: " + transactionType);
	//timing.start();

        entries = getEntries(group, false, stream);
        numberOfEntries = entries.size();
        connection = null;

        insert(entries, stream);
        
        for (int j = counter; --j >= 0;) {
            transactionManager.begin();
    
            for (i = 0; i < numberOfEntries; ++i) {
                entry = (Entry)entries.get(i);

                if (!entry._dataSourceEntry.getPerformanceTest()) {
                    continue;    
                }
    
                // enlist the xa resource
                if (!transactionManager.getTransaction().enlistResource(entry._xaResource)) {
                    stream.writeVerbose( "Failed to enlist resource for iteration: " + j );
                    return false;
                }
    
                try {
                    connection = getConnection(entry);
                    // update value
                    update(connection, entry._dataSourceEntry.getTableName(), entry._key, "new 5555");
                } finally {
                    closeConnection( connection );
                }
            }
    
            if (TWO_PHASE_COMMIT_PERFORMANCE_TEST == performanceMode) {
                transactionManager.commit();    
            } else if (ONE_PHASE_COMMIT_PERFORMANCE_TEST == performanceMode) {
                ((TyrexTransaction)transactionManager.getTransaction()).onePhaseCommit();
            } else {
                transactionManager.rollback();
            }
        }

        //timing.stop();
        //timing.count(counter);
        //stream.writeVerbose(timing.report());
        stream.writeVerbose("Minimum number of transactions expected: " + rate );
        stream.writeVerbose("Performance: " +  transactionType + " end");
        //return rate <= timing.perMinute();
        return true;
    }
    
    
    /**
     * Return the counter used to test performance
     *
     * @param performance the performance (required)
     * @param performanceMode the type of performance
     * @see #ONE_PHASE_COMMIT_PERFORMANCE_TEST
     * @see #TWO_PHASE_COMMIT_PERFORMANCE_TEST
     * @see #ROLLBACK_PERFORMANCE_TEST
     */
    static private int getPerformanceCounter(Performance performance, int performanceMode) {
        switch (performanceMode) {
            case ONE_PHASE_COMMIT_PERFORMANCE_TEST:
                return performance.getOnePhaseCommit().getIterations();
            case TWO_PHASE_COMMIT_PERFORMANCE_TEST:
                return performance.getTwoPhaseCommit().getIterations();
            case ROLLBACK_PERFORMANCE_TEST:
                return performance.getRollback().getIterations();
            default:
                throw new IllegalArgumentException("Unknown performance mode " + performanceMode);
        }
    }


    /**
     * Return the counter used to test performance
     *
     * @param performance the performance (required)
     * @param performanceMode the type of performance
     * @see #ONE_PHASE_COMMIT_PERFORMANCE_TEST
     * @see #TWO_PHASE_COMMIT_PERFORMANCE_TEST
     * @see #ROLLBACK_PERFORMANCE_TEST
     */
    static private int getPerformanceRate(Performance performance, int performanceMode) {
        switch (performanceMode) {
            case ONE_PHASE_COMMIT_PERFORMANCE_TEST:
                return performance.getOnePhaseCommit().getValue();
            case TWO_PHASE_COMMIT_PERFORMANCE_TEST:
                return performance.getTwoPhaseCommit().getValue();
            case ROLLBACK_PERFORMANCE_TEST:
                return performance.getRollback().getValue();
            default:
                throw new IllegalArgumentException("Unknown performance mode " + performanceMode);
        }
    }

    /**
     * Test rollback.
     *
     * @param transactionManager the transaction manager
     * @param entries the list of test entries
     * @param stream the logging stream
     * @return True if the test was successful. Return false otherwise.
     * @throws Exception if there is an error with the test.
     * @see Entry
     */
    static private boolean testRollback( final TransactionManager transactionManager, 
                                  ArrayList entries,
                                  VerboseStream stream )
            throws Exception
    {
        Entry entry;

        final int[] synchronizationStatus = new int[]{ -1, -1 };
        final Exception[] synchronizationException = new Exception[ 1 ];
        
        stream.writeVerbose( "Test rollback" );
                
        transactionManager.begin();

        for (int i = 0; i < entries.size(); ++i) {
            entry = (Entry)entries.get(i);

            if (!update(transactionManager, entry, entry._value + "new", stream)) {
                return false;    
            }
        }

        transactionManager.getTransaction().registerSynchronization(new Synchronization() {
                    public void afterCompletion(int mode) {
                        synchronizationStatus[ 1 ] = mode;
                    }

                    public void beforeCompletion() {
                        synchronizationStatus[ 0 ] = 0;
                    }
                } );
        
        if (!transactionBoundary(transactionManager, stream, false)) {
            return false;
        }

        if (null != synchronizationException[0]) {
            throw synchronizationException[0];    
        }

        if ( -1 != synchronizationStatus[0]) {
            stream.writeVerbose( "Before completion method called" );
            return false;
        }
        
        if ( -1 == synchronizationStatus[ 1 ] ) {
            stream.writeVerbose( "After completion method not called" );
            return false;
        }

        if ( Status.STATUS_ROLLEDBACK != synchronizationStatus[ 1 ] ) {
            stream.writeVerbose( "After completion method called with incorrect status - " +
                                 synchronizationStatus[ 1 ] );
            return false;
        }

        return checkValues(entries, stream);
    }


    /**
     * Close the specified connection.
     *
     * @param connection the connection
     */
    static private void closeConnection( Connection connection )
    {
        if ( null != connection ) {
            try {
                connection.close();
            } catch ( SQLException e) {
    
            }
        }
    }


    /**
     * Get the connection from the specified entry.
     *
     * @param entry the entry
     * @return the connection from the specified entry.
     * @throws SQLException if there is a problem geting the connection
     * @see Entry
     */
    static private Connection getConnection(Entry entry)
        throws SQLException {
        return getConnection(entry._xaConnection);
    }


    /**
     * Get the connection from the specified XA connection.
     *
     * @param xaConnection the XA connection.
     * @return the connection from the specified XA connection.
     * @throws SQLException if there is a problem geting the connection
     * @see Entry
     */
    static private Connection getConnection(XAConnection xaConnection)
        throws SQLException
    {
        /*Connection connection = xaConnection.getConnection();

        if (connection.getAutoCommit()) {
            // turn off auto commit
            connection.setAutoCommit( false );
        }
        // set the isolation level
        //connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        return connection;*/
        return xaConnection.getConnection();
    }
    

    /**
     * Test updating items into the data sources and 
     * committing the changes.
     *
     * @param transactionManager the transaction manager
     * @param group the group
     * @param stream the logging stream
     * @param commitMode the commit mode.
     * @param insert True if data is to be inserted. False is data
     *      is to be updated.
     * @return True if the test was successful. Return false otherwise.
     * @throws Exception if there is an error with the test.
     * @see Entry
     * @see #use1PCCommit
     * @see #use1PCCommitOptimization
     * @see #use2PCCommit
     */
    private static boolean testCommit( final TransactionManager transactionManager, 
                                        DataSourceGroupEntry group,
                                        VerboseStream stream,
                                        int commitMode)
        throws Exception
    {
        boolean used1PC = ((USE_1PC_COMMIT == commitMode) ||
                           (USE_1PC_COMMIT_OPTIMIZATION == commitMode));
        ArrayList entries;
        int numberOfEntries;
        Entry entry;
        Connection connection;
        final int[] synchronizationStatus = new int[]{-1, -1};
        final Exception[] synchronizationException = new Exception[1];

        entries = null;

        entries = getEntries(group, used1PC, stream);
        numberOfEntries = entries.size();
        
        stream.writeVerbose( "Test commit using " +
                             ((USE_1PC_COMMIT == commitMode) 
                                ? "one-phase commit" 
                                :   "two-phase commit" + 
                                    ((USE_1PC_COMMIT_OPTIMIZATION == commitMode) 
                                      ? " with one-phase-commit optimization" 
                                      : "" )));

        insert(entries, stream);

        try {

            transactionManager.begin();
    
            for (int i = 0; i < numberOfEntries; ++i) {
                entry = (Entry)entries.get(i);
                entry._value += "new";
    
                if (!update(transactionManager, entry, entry._value, stream)) {
                    return false;    
                }
            }
    
            transactionManager.getTransaction().registerSynchronization(new Synchronization() {
                        public void afterCompletion(int mode) {
                            synchronizationStatus[1] = mode;
                        }
    
                        public void beforeCompletion() {
                            try {
                                synchronizationStatus[0] = transactionManager.getStatus();
                            } 
                            catch (Exception e) {
                                synchronizationException[0] = e;
                            }
                        }
                    } );
            
            if ((USE_1PC_COMMIT_OPTIMIZATION == commitMode) &&
                 !((TyrexTransaction)transactionManager.getTransaction()).canUseOnePhaseCommit()) {
                stream.writeVerbose("One phase commit optimization not working");
                return false;
            }
    
    
            if (USE_1PC_COMMIT == commitMode) {
                ((TyrexTransaction) transactionManager.getTransaction()).onePhaseCommit();    
            } 
            else {
                transactionManager.commit();
            }
            
            if (null != transactionManager.getTransaction()) {
                stream.writeVerbose("Thread still associated with transaction after commit");
                return false;
            }
            
            if (null != synchronizationException[0]) {
                throw synchronizationException[0];    
            }
    
            if (-1 == synchronizationStatus[0]) {
                stream.writeVerbose("Before completion method not called");
                return false;
            }
            
            if (!used1PC && 
                 (synchronizationStatus[ 0 ] != Status.STATUS_PREPARING)) {
                stream.writeVerbose("BeforeCompletion - Prepare phase called with incorrect status - " + 
                                     synchronizationStatus[0]);
                return false;
            }
    
            if (used1PC && 
                 (synchronizationStatus[0] != Status.STATUS_ACTIVE)) {
                stream.writeVerbose("BeforeCompletion - One phase commit called with incorrect status - " + 
                                     synchronizationStatus[0]);
                return false; 
            }
    
            if (-1 == synchronizationStatus[1]) {
                stream.writeVerbose("After completion method not called");
                return false;
            }
    
            if (Status.STATUS_COMMITTED != synchronizationStatus[1]) {
                stream.writeVerbose("After completion method called with incorrect status - " +
                                     synchronizationStatus[1]);
                return false;
            }
    
            return checkValues(entries, stream);
        }
        finally {
            close(entries);
        }
    }


    /**
     * Close the connections associated with the data sources
     *
     * @param entries the list of {@link Entry}
     */
    static private void close(ArrayList entries) {
        if (null != entries) {
            for (int i = entries.size(); --i >= 0;) {
                try {
                    ((Entry)entries.get(i))._xaConnection.close();    
                }
                catch(SQLException e) {
                }
            }
        }
    }

    /**
     * Return the list of entries to be used
     * in testing.
     *
     * @param group the group
     * @param useOne true if only a list of one netry is to be returned.
     * @return the list of entries to be used
     *      in testing.
     * @throws Exception if there is a problem getting the 
     *      XA data sources, connections and/or resources.
     * @see Entry
     */
    static private ArrayList getEntries(DataSourceGroupEntry group, boolean useOne, VerboseStream stream)
        throws Exception
    {
        ArrayList entries;
        XAConnection xaConnection;
        XAResource xaResource;
        Entry entry;
        int groupSize;
        DataSourceEntry dataSourceEntry;
        
        groupSize = useOne ? 1 : group.getNumberOfDataSourceEntries();
        entries = new ArrayList();

        //java.sql.DriverManager.setLogWriter(new java.io.PrintWriter(System.out, true));
        // populate it
        for (int i = 0; i < groupSize; ++i ) {
            dataSourceEntry = group.getDataSourceEntry(i);
            // get the xa connection
            xaConnection = dataSourceEntry.getXAConnection();
            //System.out.println("xaConnection " + xaConnection);
            xaResource = xaConnection.getXAResource();
            //System.out.println("xaResource " + xaResource);
                        
            entry = new Entry(xaConnection, 
                                  xaResource,
                                  generateKey(),
                                  generateValue(),
                                  dataSourceEntry);
            entries.add(entry);
        }

        return entries;
    }

    /**
     * Generate a random key
     *
     * @return a random key
     */
    static private String generateKey(){
        synchronized (TransactionTestSuite.class) {
            try {
                TransactionTestSuite.class.wait(50);
            }
            catch(InterruptedException e){
            }

            return "key" + System.currentTimeMillis();
        }
    }

    /**
     * Generate a random value
     *
     * @return a random value
     */
    static private String generateValue(){
        synchronized (TransactionTestSuite.class) {
            try {
                TransactionTestSuite.class.wait(50);
            }
            catch(InterruptedException e){
            }

            return "value" + System.currentTimeMillis();
        }
    }

    /**
     * Object that collects all the data
     * necessary to test a particular data source.
     */
     private static final class Entry
     {
         /**
          * The xa connection
          */
         private XAConnection _xaConnection;


         /**
          * The xa resource
          */
         private XAResource _xaResource;


         /**
          * The key
          */
         private final String _key;


         /**
          * The value stored with the key
          */
         private String _value;

         /**
          * The data source entry
          */
         private final DataSourceEntry _dataSourceEntry;


         /**
          * Create the Entry with the specified
          * arguments.
          *
          * @param xaConnection the physical connection
          * @param xaResource the xa resource
          * @param key the primary key
          * @param value the value
          */
         private Entry(XAConnection xaConnection,
                       XAResource xaResource,
                       String key,
                       String value,
                       DataSourceEntry dataSourceEntry)
         {
             _xaConnection = xaConnection;
             _xaResource = xaResource;
             _key = key;
             _value = value;
             _dataSourceEntry = dataSourceEntry;
         }
     }

     /*
     public static void main (String args[]) 
        throws Exception {
        Class[] classes;

        classes = getTestClasses();

        for (int i = 0; i < classes.length; ++i) {
            System.out.println(classes[i].getName());
            System.out.println(getTestClassConstructor(classes[i]));
            System.out.println("");
        }
     }*/
}

