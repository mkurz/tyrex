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
 */


package jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
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
import tyrex.tm.AsyncCompletionCallback;
import tyrex.tm.Tyrex;
import tyrex.tm.TyrexTransaction;


///////////////////////////////////////////////////////////////////////////////
// SimpleTestCase
///////////////////////////////////////////////////////////////////////////////

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
 * <LI> Test performance for commits.
 * <LI> Test performance for rollbacks.            
 * </UL>
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
class SimpleTestCase
    extends AbstractTestCase
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
     * Counter used in performance testing
     *
     * @see #performance
     */
    private static final int COUNTER = 100;


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
     * @see #testOnePhaseCommitOptimization
     */
    private static final int COULD_NOT_TEST_ONE_PHASE_COMMIT = 0;


    /**
     * One phase commit optimization test failed.
     *
     * @see #testOnePhaseCommitOptimization
     */
    private static final int ONE_PHASE_COMMIT_TEST_FAILED = 1;


    /**
     * One phase commit optimization test succeeded
     *
     * @see #testOnePhaseCommitOptimization
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
     * Create the SimpleTestCase with
     * the specified helper.
     *
     * @param name the name of the test case
     * @param helper the helper
     * @see JDBCHelper
     */
    SimpleTestCase( String name, JDBCHelper helper )
        throws CWClassConstructorException
    {
        super( name, "SimpleTestCase", helper );
    }

    public boolean run( CWVerboseStream stream )
    {
        TransactionManager transactionManager;
        Entry[] entries = null;
        boolean multipleEntries;
        
        try {
            
            stream.writeVerbose( "Test simple transaction actions " + helper.toString() );

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

            multipleEntries = entries.length > 1;

            try {
                stream.writeVerbose( "Getting transaction manager " );
                // get the transaction manager
                transactionManager = Tyrex.getTransactionManager();
            } catch (Exception e) {
                stream.writeVerbose( "Error: Failed to get transaction manager " );
                return false;
            }

            if ( multipleEntries ) {
                try {
                    if ( !testCommit( transactionManager, entries, stream, USE_2PC_COMMIT, true ) ) {
                        stream.writeVerbose( "Error: Failed two-phase commit" );
                        return false;    
                    } 
                } catch ( Exception e ) {
                    stream.writeVerbose( "Error: Failed two-phase commit" );
                    stream.writeVerbose( e.toString() );
                    return false;
                }
            } else {
                stream.writeVerbose( "Cannot test two-phase commit" );
            }

            try {
                if ( !testCommit( transactionManager, entries, stream, USE_1PC_COMMIT, !multipleEntries ) ) {
                    stream.writeVerbose( "Error: Failed one-phase commit" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed one-phase commit" );
                stream.writeVerbose( e.toString() );
                if ( ( e instanceof SQLException ) && ( ( ( SQLException ) e ).getNextException() != null ) ) {
                    ( ( SQLException ) e ).getNextException().printStackTrace();            
                }
                return false;
            }
            
            try {
                if ( !testCommit( transactionManager, entries, stream, USE_1PC_COMMIT_OPTIMIZATION, false ) ) {
                    stream.writeVerbose( "Error: Failed two-phase commit, with one-phase commit optimization" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed two-phase commit, with one-phase commit optimization" );
                stream.writeVerbose( e.toString() );
                return false;
            }    
            
            try {
                if ( !testRollback( transactionManager, entries, stream ) ) {
                    stream.writeVerbose( "Error: Failed rollback" );
                    return false;
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed rollback" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testMarkForRollback( transactionManager, entries, stream, true ) ) {
                    stream.writeVerbose( "Error: Failed marked for rollback during commit" );
                    return false;
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed marked for rollback during commit" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testMarkForRollback( transactionManager, entries, stream, false ) ) {
                    stream.writeVerbose( "Error: Failed marked for rollback during rollback" );
                    return false;
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed marked for rollback during rollback" );
                stream.writeVerbose( e.toString() );
                return false;
            }
            
            try {
                if ( !testDelist( transactionManager, entries, stream, false, true, false ) ) {
                    stream.writeVerbose( "Error: Failed to commit with resource delist" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed to commit with resource delist" );
                stream.writeVerbose( e.toString() );
                return false;
            }
            
            try {
                if ( !testDelist( transactionManager, entries, stream, false, false, false ) ) {
                    stream.writeVerbose( "Error: Failed to rollback with resource delist" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed to rollback with resource delist" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            if ( helper.canReuseDelistedXAResources() ) {
                try {
                    if ( !testDelist( transactionManager, entries, stream, false, true, true ) ) {
                        stream.writeVerbose( "Error: Failed to commit with resource delist and resource reuse" );
                        return false;    
                    }
                } catch ( Exception e ) {
                    stream.writeVerbose( "Error: Failed to commit with resource delist and resource reuse" );
                    stream.writeVerbose( e.toString() );
                    return false;
                }
                
                try {
                    if ( !testDelist( transactionManager, entries, stream, false, false, true ) ) {
                        stream.writeVerbose( "Error: Failed to rollback with resource delist and resource reuse" );
                        return false;    
                    }
                } catch ( Exception e ) {
                    stream.writeVerbose( "Error: Failed to rollback with resource delist and resource reuse" );
                    stream.writeVerbose( e.toString() );
                    return false;
                }
            } else {
                stream.writeVerbose( "Cannot test resource delisting and re-enlisting in same transaction" );
            }
            
            if ( multipleEntries ) {
                try {
                    if ( !testDelist( transactionManager, entries, stream, true, true, false ) ) {
                        stream.writeVerbose( "Error: Failed to commit, using one-phase commit optimization, with resource delist" );
                        return false;    
                    }
                } catch ( Exception e ) {
                    stream.writeVerbose( "Error: Failed to commit, using one-phase commit optimization, with resource delist" );
                    stream.writeVerbose( e.toString() );
                    return false;
                }

                // no need to test rollback with one-phase commit optimization

                if ( helper.canReuseDelistedXAResources() ) {
                    try {
                        if ( !testDelist( transactionManager, entries, stream, true, true, true ) ) {
                            stream.writeVerbose( "Error: Failed to commit, using one-phase commit optimization, with resource delist and resource reuse" );
                            return false;    
                        }
                    } catch ( Exception e ) {
                        stream.writeVerbose( "Error: Failed to commit, using one-phase commit optimization, with resource delist and resource reuse" );
                        stream.writeVerbose( e.toString() );
                        return false;
                    }
    
                    // no need to test rollback with one-phase commit optimization and resource reuse
                } else {
                    stream.writeVerbose( "Cannot test resource delisting and re-enlisting in same transaction using one-phase optimization " );
                }
            }
            
            
            try {
                if ( !testRollbackWithFailedDelist( transactionManager, entries, stream ) ) {
                    stream.writeVerbose( "Error: Failed to rollback with resource delist" );
                    return false;    
                }
            } catch ( Exception e ) {
                e.printStackTrace();
                stream.writeVerbose( "Error: Failed to rollback with resource delist" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            if ( helper.getFailSleepTime() > 0) {
                stream.writeVerbose( "Sleeping for " + 
                                     helper.getFailSleepTime() + 
                                     " milliseconds after delisting resource with failure ");
                try {Thread.sleep(helper.getFailSleepTime());}catch (Exception e){}    
            }
            
            try {
                if ( !testAsynchronousTransaction( transactionManager, entries, stream, true ) ) {
                    stream.writeVerbose( "Error: Failed to commit asynchronously" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed to commit asynchronously" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testAsynchronousTransaction( transactionManager, entries, stream, false ) ) {
                    stream.writeVerbose( "Error: Failed to rollback asynchronously" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed to rollback asynchronously" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testSynchronization( transactionManager, entries, stream, true ) ) {
                    stream.writeVerbose( "Error: Failed to test synchronization during commit" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed to Failed to test synchronization during commit" );
                stream.writeVerbose( e.toString() );
                return false;
            }


            try {
                if ( !testSynchronization( transactionManager, entries, stream, false ) ) {
                    stream.writeVerbose( "Error: Failed to test synchronization during rollback" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed to test synchronization during rollback" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testSynchronizationWithFailure( transactionManager, entries, stream, true, 
                                                      BEFORE_COMPLETION_FAILURE ) ) {
                    stream.writeVerbose( "Error: Failed to test synchronization (beforeCompletion failure) during commit" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed to test synchronization (beforeCompletion failure) during commit" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testSynchronizationWithFailure( transactionManager, entries, stream, true, 
                                                      AFTER_COMPLETION_FAILURE ) ) {
                    stream.writeVerbose( "Error: Failed to test synchronization (afterCompletion failure) during commit" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed to test synchronization (afterCompletion failure) during commit" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testSynchronizationWithFailure( transactionManager, entries, stream, true, 
                                                      ( byte ) ( BEFORE_COMPLETION_FAILURE | AFTER_COMPLETION_FAILURE ) ) ) {
                    stream.writeVerbose( "Error: Failed to test synchronization (beforeCompletion and afterCompletion failure) during commit" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed to test synchronization (beforeCompletion and afterCompletion failure) during commit" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testSynchronizationWithFailure( transactionManager, entries, stream, false,
                                                      BEFORE_COMPLETION_FAILURE ) ) {
                    stream.writeVerbose( "Error: Failed to test synchronization (beforeCompletion failure) during rollback" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed to test synchronization (beforeCompletion failure) during rollback" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            try {
                if ( !testSynchronizationWithFailure( transactionManager, entries, stream, false,
                                                      AFTER_COMPLETION_FAILURE ) ) {
                    stream.writeVerbose( "Error: Failed to test synchronization (afterCompletion failure) during rollback" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed to test synchronization (afterCompletion failure) during rollback" );
                stream.writeVerbose( e.toString() );
                return false;
            }


            try {
                if ( !testSynchronizationWithFailure( transactionManager, entries, stream, false,
                                                      ( byte ) ( BEFORE_COMPLETION_FAILURE | AFTER_COMPLETION_FAILURE ) ) ) {
                    stream.writeVerbose( "Error: Failed to test synchronization (beforeCompletion and afterCompletion failure) during rollback" );
                    return false;    
                }
            } catch ( Exception e ) {
                stream.writeVerbose( "Error: Failed to test synchronization (beforeCompletion and afterCompletion failure) during rollback" );
                stream.writeVerbose( e.toString() );
                return false;
            }

            if ( multipleEntries ) {
                try {
                    int result  = testOnePhaseCommitOptimization( transactionManager,
                                                                  entries,
                                                                  stream);
    
                    switch (result) {
                        case COULD_NOT_TEST_ONE_PHASE_COMMIT:
                            //stream.writeVerbose( "Could not test one phase commit optimization." );
                            break;
                        case ONE_PHASE_COMMIT_TEST_FAILED:
                            stream.writeVerbose( "Error: Failed to test one phase commit optimization." );
                            return false;    
                        default:
                            if ( ONE_PHASE_COMMIT_TEST_SUCCEEDED != result ) {
                                stream.writeVerbose( "Error: Unknown result from one phase commit optimization test." );
                                return false;        
                            }
                    }
                } catch ( Exception e ) {
                    stream.writeVerbose( "Error: Failed to test one phase commit optimization" );
                    stream.writeVerbose( e.toString() );
                    return false;
                }
            }

            if ( helper.canTestPerformance() ) {
                if ( multipleEntries) {
                    try {
                        if ( !performance( transactionManager, entries, stream, TWO_PHASE_COMMIT_PERFORMANCE_TEST ) ) {
                            stream.writeVerbose( "Error: Performance two-phase commit failed" );
                            return false;    
                        }
                    } catch ( Exception e ) {
                        stream.writeVerbose( "Error: Performance two-phase commit failed" );
                        stream.writeVerbose( e.toString() );
                        return false;
                    }
                } else {
                    stream.writeVerbose( "Could not test two-phase commit performance" );
                }
                
    
                try {
                    if ( !performance( transactionManager, entries, stream, ONE_PHASE_COMMIT_PERFORMANCE_TEST ) ) {
                        stream.writeVerbose( "Error: Performance one-phase commit failed" );
                        return false;    
                    }
                } catch ( Exception e ) {
                    stream.writeVerbose( "Error: Performance one-phase commit failed" );
                    stream.writeVerbose( e.toString() );
                    return false;
                }
    
    
                try {
                    if ( !performance( transactionManager, entries, stream, ROLLBACK_PERFORMANCE_TEST ) ) {
                        stream.writeVerbose( "Error: Performance rollback failed" );
                        return false;    
                    }
                } catch ( Exception e ) {
                    stream.writeVerbose( "Error: Performance rollback failed" );
                    stream.writeVerbose( e.toString() );
                    return false;
                }
            } else {
                stream.writeVerbose( "Cannot test performance" );
            }
            
            return true;
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( null != entries) {
                for ( int i = 0; i < entries.length; ++i ) {
                    try {
                        entries[i].xaConnection.close();
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
                                         CWVerboseStream stream,
                                         boolean commit )
        throws Exception
    {
        if ( commit ) {
            transactionManager.commit();    
        } else {
            transactionManager.rollback();
        }
        
        if ( null != transactionManager.getTransaction() ) {
            stream.writeVerbose( "Thread still associated with transaction after transaction boundary ");
            return false;
        }

        return true;
    }

    /**
     * Test rollback with the xa resources being delisted with the
     * XAResource.TMFAIL flag.
     *
     * @param transactionManager the transaction manager
     * @param entries the array of test entries
     * @param stream the logging stream
     * @return True if the test was successful. Return false otherwise.
     * @throws Exception if there is an error with the test.
     * @see Entry
     */
    private boolean testRollbackWithFailedDelist( TransactionManager transactionManager,
                                                   Entry[] entries,
                                                   CWVerboseStream stream )
        throws Exception
    {
        Entry entry;
        
        stream.writeVerbose( "Test rollback with resource delist (TMFAIL)" );
                
        transactionManager.begin();

        for ( int i = 0; i < entries.length; ++i ) {
            entry = entries[ i ];

            if ( !update( transactionManager, i, entry, entry.value + "new", stream ) ) {
                return false;    
            }
        }

        for ( int i = 0; i < entries.length; ++i ) {
            entry = entries[ i ];
            transactionManager.getTransaction().delistResource( entry.xaResource, XAResource.TMFAIL );
            //System.out.println("old xa connection " + entry.xaConnection);
            //System.out.println("old xa resource " + entry.xaResource);
            entry.xaConnection.close();
            entry.xaConnection = /*helper.createXADataSource( i ).getXAConnection(); */helper.getXAConnection( i );
            entry.xaResource = entry.xaConnection.getXAResource();
            /*System.out.println("timeout " + entry.xaResource.getTransactionTimeout());
            if ( entry.xaResource.setTransactionTimeout(200) ) {
                System.out.println(" set timeout");
            }*/
            //System.out.println("new xa connection " + entry.xaConnection);
            //System.out.println("new xa resource " + entry.xaResource);

        }

        // the status should be rollback
        if ( transactionManager.getStatus() != Status.STATUS_MARKED_ROLLBACK ) {
            stream.writeVerbose( "Transaction not marked for rollback" );    
            return false;
        }

        if ( !transactionBoundary( transactionManager, stream, false ) ) {
                    return false;
        }

        return checkValues(entries, stream);
    }


    /**
     * Test set rollback only on a transaction that
     * is to be committed or rolled back depending
     * on the commit flag.
     *
     * @param transactionManager the transaction manager
     * @param entries the array of test entries
     * @param stream the logging stream
     * @param commit True if the transaction is to be commited. False if
     *      the transaction is to be rollbed back.
     * @return True if the test was successful. Return false otherwise.
     * @throws Exception if there is an error with the test.
     * @see Entry
     */
    private boolean testMarkForRollback( TransactionManager transactionManager,
                                         Entry[] entries,
                                         CWVerboseStream stream,
                                         boolean commit )
        throws Exception
    {
        Entry entry;
        
        String transactionType = commit ? "commit" : "rollback";

        stream.writeVerbose( "Test mark for rollback during " + transactionType );
                
        transactionManager.begin();

        for ( int i = 0; i < entries.length; ++i ) {
            entry = entries[ i ];

            if ( !update( transactionManager, i, entry, entry.value + "new", stream ) ) {
                return false;    
            }
        }

        transactionManager.getTransaction().setRollbackOnly();
        
        // the status should be rollback
        if ( transactionManager.getStatus() != Status.STATUS_MARKED_ROLLBACK ) {
            stream.writeVerbose( "Transaction not marked for rollback during " + transactionType );    
            return false;
        }

        if ( commit ) {
            boolean rollbackExceptionOccurred = false;

            try {
                if ( !transactionBoundary( transactionManager, stream, true ) ) {
                    return false;
                }
            }
            catch ( HeuristicRollbackException e ) {
                rollbackExceptionOccurred = true;    
            }

            if ( !rollbackExceptionOccurred ) {
                stream.writeVerbose( "Rollback exception not thrown during " + transactionType );
            }

        } else {
            if ( !transactionBoundary( transactionManager, stream, false ) ) {
                    return false;
            }
        }
        
        return checkValues(entries, stream);
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
        Connection connection;
        boolean matched = true;

        for ( int i = 0; i < entries.length; ++i ) {
            entry = entries[ i ];
            connection = getConnection( entry );

            if ( !checkValue( i, entry.xaConnection, DEFAULT_TABLE_INDEX, entry.key, entry.value, stream ) ) {
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
     * @param xaConnection the xa connection
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
                               XAConnection xaConnection, 
                               int tableIndex,
                               String key, 
                               String value, 
                               CWVerboseStream stream)
        throws SQLException, IOException
    {
        Connection connection = getConnection( xaConnection );

        try {
            if ( !helper.checkValue( dataSourceIndex, connection, tableIndex, key, value ) ) {
                stream.writeVerbose( "Values don't match for data source " + dataSourceIndex );
                return false;    
            }
        } finally {
            closeConnection( connection );
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
     * @return true if the update happened. Return false otherwise.
     * @throws Exception if there is a problem updating
     * @see Entry
     */
    private boolean update( TransactionManager transactionManager,
                             int dataSourceIndex,
                             Entry entry, 
                             String value, 
                             CWVerboseStream stream )
        throws Exception
    {
        return update( transactionManager, dataSourceIndex, 
                       entry.xaConnection, entry.xaResource, 
                       DEFAULT_TABLE_INDEX, entry.key, value, stream );
    }


    /**
     * Update the data source using
     * the specified key and value.
     * The XA resource is enlisted in the current
     * transaction before the data source is changed.
     *
     * @param transactionManager the transaction manager
     * @param dataSourceIndex the dataSourceIndex of the data source in the helper
     * @param xaConnection the XA connection to the data source
     * @param xaResource the XA resource.
     * @param tableIndex the index of the table
     *      to read data from.
     * @param key the key. Assumed to be already existing
     *      in data source.
     * @param value the new value.
     * @param stream the logging stream
     * @return true if the update happened. Return false otherwise.
     * @throws Exception if there is a problem updating
     * @see Entry
     */
    private boolean update( TransactionManager transactionManager,
                             int dataSourceIndex,
                             XAConnection xaConnection, 
                             XAResource xaResource,
                             int tableIndex,
                             String key, 
                             String value, 
                             CWVerboseStream stream )
        throws Exception
    {
        Connection connection = null;;

        try {
            // enlist the xa resource
            if (!transactionManager.getTransaction().enlistResource(xaResource)) {
                stream.writeVerbose( "Failed to enlist resource" );
                return false;
            }
            
            connection = getConnection( xaConnection );
            // update value
            helper.updateSQL( dataSourceIndex, connection, tableIndex, key, value );
            
            // make sure the value changed
            if ( !helper.checkValue( dataSourceIndex, connection, tableIndex, key, value ) ) {
                stream.writeVerbose( "Update failed for table index " + 
                                     tableIndex + 
                                     " in data source index " + 
                                     dataSourceIndex + 
                                     " for key " + 
                                     key + 
                                     " and value " + value );
                return false;    
            }
            
            return true;
        } finally {
            closeConnection( connection );
        }
    }

    /**
     * Test that the synchronization methods are called
     * during a transaction boundary that fails.
     *
     * @param transactionManager the transaction manager
     * @param entries the array of test entries
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
    private boolean testSynchronizationWithFailure( final TransactionManager transactionManager,
                                                     Entry[] entries,
                                                     CWVerboseStream stream,
                                                     boolean commit,
                                                     final byte synchronizationFailure )
        throws Exception
    {
        Entry entry;

        String value;
        
        final int[] completionCalled = { -1, -1 };

        final Exception[] completionException = new Exception[ 1 ];
        
        final String forceRollbackString = "Force rollback";

        final boolean isBeforeCompletionFailure = ( synchronizationFailure & BEFORE_COMPLETION_FAILURE ) != 0;
        
        final boolean isAfterCompletionFailure = ( synchronizationFailure & AFTER_COMPLETION_FAILURE ) != 0;
    
        String transactionType = (commit ? "commit" : "rollback");

        class FailureSynchronization implements Synchronization
        {
            public void afterCompletion(int mode) 
            {
                if ( isAfterCompletionFailure ) {
                    throw new RuntimeException( forceRollbackString );        
                }
            }
        
            public void beforeCompletion() 
            {
                if ( isBeforeCompletionFailure ) {
                    throw new RuntimeException( forceRollbackString );        
                }
            }
        };
        
        stream.writeVerbose( "Test Synchronization failure [" + 
                             ( isBeforeCompletionFailure
                               ? " before completion"
                               : "" ) +
                             ( isAfterCompletionFailure
                               ? " after completion"
                               : "" ) +
                             " ] during " + 
                             transactionType +
                             "." );
        
        transactionManager.begin();
        
         // add the synchronization to force the rollback
        transactionManager.getTransaction().registerSynchronization( new FailureSynchronization() );
        
         // add the synchronization
        transactionManager.getTransaction().registerSynchronization( new Synchronization()
            {
                public void afterCompletion(int mode) 
                {
                    completionCalled[1] = mode;
                }
        
                public void beforeCompletion() 
                {

                    try {
                        // the status should be rollback
                        if ( isBeforeCompletionFailure && transactionManager.getStatus() != Status.STATUS_MARKED_ROLLBACK ) {
                            completionCalled[0] = 1;
                        } else {
                            completionCalled[0] = 0;
                        }
                    } catch ( Exception e ) {
                        completionException[ 0 ] = e;
                    }
                }
            });
        
        // add the synchronization to force the rollback
        transactionManager.getTransaction().registerSynchronization( new FailureSynchronization() );
        
        for ( int i = 0; i < entries.length; ++i ) {
             entry = entries[ i ];
        
             value = entry.value + "new";
        
             if ( commit && !isBeforeCompletionFailure ) {
                entry.value = value;    
             }
             
             if ( !update( transactionManager, i, entry, value, stream ) ) {
                 return false;
             }
         }
        
        if (commit) {
            boolean rollbackExceptionOccurred = false;
         
            try {
                if ( !transactionBoundary( transactionManager, stream, true ) ) {
                    return false;
                }
            } catch ( HeuristicRollbackException e ) {
                if ( isBeforeCompletionFailure ) {
                    rollbackExceptionOccurred = true;    
                } else {
                    throw e;
                }
            } catch ( SystemException e ) {
                if ( !e.getMessage().endsWith( forceRollbackString ) ) {
                    throw e;
                }
            }
        
            if ( isBeforeCompletionFailure && 
                 !rollbackExceptionOccurred ) {
                stream.writeVerbose( "Rollback exception not thrown" );
                return false;
            }
        } else {
            try {
                if ( !transactionBoundary( transactionManager, stream, false ) ) {
                    return false;
                }
            } catch ( SystemException e ) {
                if ( !e.getMessage().endsWith( forceRollbackString ) ) {
                    throw e;
                }
            }
        }
        
        if ( null != completionException[ 0 ]) {
            throw completionException[ 0 ];    
        }

        if ( completionCalled[0] == -1) {
            stream.writeVerbose( "Synchronization beforeCompletion method not called" );    
            return false;
        }

        if ( isBeforeCompletionFailure &&
             completionCalled[0] == 1) {
            stream.writeVerbose( "Transaction not marked for rollback during beforeCompletion " );    
            return false;
        }
        
        if ( completionCalled[1] == -1) {
            stream.writeVerbose( "Synchronization afterCompletion method not called" );    
            return false;
        }
        
        if ( completionCalled[1] != ( isBeforeCompletionFailure || !commit ? Status.STATUS_ROLLEDBACK : Status.STATUS_COMMITTED ) ) {
            stream.writeVerbose( "Synchronization afterCompletion method called with incorrect status - " + 
                                 completionCalled[1]);    
            return false;
        }
        
        if (!checkValues(entries, stream)) {
            stream.writeVerbose( "Failed to " + transactionType );
            return false;
        }
        
        return true;
    }


     /**
      * Test that the one phase commit optimization works
      *
      * @param transactionManager the transaction manager
      * @param entries the array of test entries
      * @param stream the logging stream
      * @return 0 if one phase commit optimization could not be tested;
      *     1 if the test failed; 2 if it succeeded.
      * @throws Exception if there is an error with the test.
      * @see Entry
      */
    private int testOnePhaseCommitOptimization( TransactionManager transactionManager,
                                                Entry[] entries,
                                                CWVerboseStream stream )
        throws Exception
    {
        int result = testOnePhaseCommitOptimizationUsingMultipleDatasources( transactionManager,
                                                                             entries,
                                                                             stream );

        if ( COULD_NOT_TEST_ONE_PHASE_COMMIT == result ) {
            stream.writeVerbose( "Could not test one phase commit phase optimization using multiple data sources" );
            //result =  testOnePhaseCommitOptimizationUsingSingleDatasource( transactionManager,
            //                                                               entries,
            //                                                               stream );
        }

        return result;
    }


    /**
     * Test that the one phase commit optimization using the
     * multiple data sources. 
     * <P>
     * If the resource managers from multiple data sources can
     * be shared then test one phase commit optimization.
     *
     * @param transactionManager the transaction manager
     * @param entries the array of test entries
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
    private int testOnePhaseCommitOptimizationUsingMultipleDatasources( final TransactionManager transactionManager,
                                                                     Entry[] entries,
                                                                     CWVerboseStream stream)
        throws Exception
    {
    
        Entry entry;
        int dataSourceIndex;
        int i;

        int entriesLength = entries.length;

        final boolean[] onePhaseFailed = new boolean[]{false};

        Entry[] sharedEntries = null;
        Entry[] temp;
        Entry[] tempSharedEntries = new Entry[ entriesLength ];

        int sharedLength = 0;
        int tempSharedLength = 0;

        stream.writeVerbose( "Test one phase commit phase optimization using multiple data sources" );

        
        for ( dataSourceIndex = 0; dataSourceIndex < entriesLength; ++dataSourceIndex ) {
            
            entry = entries[ dataSourceIndex ];

            if (sharedLength < ( entriesLength - dataSourceIndex ) ) {
                if ( null == tempSharedEntries ) {
                    tempSharedEntries = new Entry[ entriesLength ];    
                } else {
                    for ( i = 0 ; i < entriesLength; ++i ) {
                        tempSharedEntries[ i ] = null;    
                    }
                }

                for ( i = dataSourceIndex + 1; i < entriesLength; ++i) {
                    if ( entries[ i ].xaResource.isSameRM( entry.xaResource ) ) {
                        tempSharedEntries[i] = entries[ i ];
                        ++tempSharedLength;
                    }
                }
                if ( 0 != tempSharedLength ) {
                    tempSharedEntries[ dataSourceIndex ] = entry;    
                }
            }

            if ( tempSharedLength > sharedLength) {
                temp = sharedEntries;

                sharedEntries = tempSharedEntries;

                tempSharedEntries = temp;
            }
        }
        
        if ( 0 == sharedLength ) {
            return COULD_NOT_TEST_ONE_PHASE_COMMIT;    
        }
        
        transactionManager.begin();

        transactionManager.getTransaction().registerSynchronization( new Synchronization()
                    {
                        public void afterCompletion(int mode) {

                        }

                        public void beforeCompletion() {
                            try {
                                if ( transactionManager.getStatus() != Status.STATUS_COMMITTING ) {
                                onePhaseFailed[0] = true;    
                                }
                            } catch ( SystemException e ) {
                                throw new RuntimeException( e.toString() );
                            }
                        }
                    } );
        
        for ( dataSourceIndex = 0; dataSourceIndex < entriesLength ; ++dataSourceIndex ) {
            entry = sharedEntries[ dataSourceIndex ];

            if ( null == entry ) {
                continue;    
            }

            entry.value += "new";
            
            if (!update( transactionManager,
                         dataSourceIndex,
                         entry, 
                         entry.value, 
                         stream ) ) {
                return ONE_PHASE_COMMIT_TEST_FAILED;
            }
        }
        
        if ( !( (TyrexTransaction) transactionManager.getTransaction() ).canUseOnePhaseCommit() ) {
            stream.writeVerbose( "Cannot use one phase commit optimization" );    
            return ONE_PHASE_COMMIT_TEST_FAILED;
        }

        if ( !transactionBoundary( transactionManager, stream, true ) ) {
                    return ONE_PHASE_COMMIT_TEST_FAILED;
        }
        
        for ( dataSourceIndex = 0; dataSourceIndex < entriesLength; ++dataSourceIndex ) {
            entry = sharedEntries[ dataSourceIndex ];

            if ( null == entry ) {
                continue;    
            }
            
            if ( !checkValue( dataSourceIndex, 
                              entry.xaConnection, 
                              DEFAULT_TABLE_INDEX,
                              entry.key, 
                              entry.value, 
                              stream ) ) {
                return ONE_PHASE_COMMIT_TEST_FAILED;    
            }
        }

        return onePhaseFailed[0] ? ONE_PHASE_COMMIT_TEST_FAILED : ONE_PHASE_COMMIT_TEST_SUCCEEDED;
    }
     
    /**
     * Test that the one phase commit optimization using the
     * tables from a single data source. In other words trying
     * creating multiple XA connections from a single XA data
     * source and using those conenctions to update different
     * tables in the data source.
     *
     * @param transactionManager the transaction manager
     * @param entries the array of test entries
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
    /*private int testOnePhaseCommitOptimizationUsingSingleDatasource( final TransactionManager transactionManager,
                                                                     Entry[] entries,
                                                                     CWVerboseStream stream)
        throws Exception
    {
    
        Entry entry;
        
        String value;

        int numberOfTables = 0;

        XAConnection[] xaConnections = null;

        XAResource[] xaResources = null;

        String[] keys = null;
        String[] values = null;

        XADataSource xaDataSource;

        int dataSourceIndex;
        int tableIndex;

        final boolean[] onePhaseFailed = new boolean[]{false};

        stream.writeVerbose( "Test one phase commit phase optimization using a single data source" );

        
        for ( dataSourceIndex = 0; dataSourceIndex < entries.length; ++dataSourceIndex ) {
            
            numberOfTables = helper.getNumberOfTables( dataSourceIndex );

            if ( !helper.canTestOnePhaseCommitOptimization( dataSourceIndex ) ||
                 numberOfTables <= 1 ) {
                continue;    
            }

            entry = entries[ dataSourceIndex ];
            
            xaConnections = new XAConnection[ numberOfTables ];
            xaResources = new XAResource[ numberOfTables ];
            keys = new String[ numberOfTables ];
            values = new String[ numberOfTables ];

            xaConnections[ 0 ] = entry.xaConnection;    
            xaResources[ 0 ] = entry.xaResource;    
            
            xaDataSource = helper.createXADataSource( dataSourceIndex );

            xaConnections[ 1 ] = helper.getXAConnection( xaDataSource, dataSourceIndex );
            xaResources[ 1 ] = xaConnections[ 1 ].getXAResource();    

            if ( xaResources[ 1 ].isSameRM( xaResources[ 0 ] ) ) {
                keys[ 0 ] = entry.key;
                entry.value += "new";
                values[ 0 ] = entry.value;
                keys[ 1 ] = keys[ 0 ] + "1";
                values[ 1 ] = values[ 0 ] + "1";
                break;
            }
            else {
                try {
                    xaConnections[ 1 ].close();
                } catch ( Exception e ) {

                }
                xaConnections = null;
                xaResources = null;
                keys = null;
                values = null;
            }
        }
        
        if ( null == xaConnections ) {
            dataSourceIndex = 0;
            entry = entries[ dataSourceIndex ];
            xaConnections = new XAConnection[]{entry.xaConnection};
            xaResources = new XAResource[]{entry.xaResource};
            keys = new String[]{entry.key};
            entry.value += "new";
            values = new String[]{entry.value};
            numberOfTables = 1;
        }
        
        try {
            // make the other xa connections
            for ( tableIndex = 2; tableIndex < numberOfTables; ++tableIndex ) {
                xaConnections[ tableIndex ] = helper.getXAConnection( dataSourceIndex );    
                xaResources[ tableIndex ] = xaConnections[ tableIndex ].getXAResource();
                keys[ tableIndex ] = keys[ 0 ] + tableIndex;
                values[ tableIndex ] = values[ 0 ] + tableIndex;
            }
            
            transactionManager.begin();
    
            transactionManager.getTransaction().registerSynchronization( new Synchronization()
                        {
                            public void afterCompletion(int mode) {
    
                            }
    
                            public void beforeCompletion() {
                                try {
                                    if ( transactionManager.getStatus() != Status.STATUS_COMMITTING ) {
                                    onePhaseFailed[0] = true;    
                                    }
                                } catch ( SystemException e ) {
                                    throw new RuntimeException( e.toString() );
                                }
                            }
                        } );
            
            for ( tableIndex = 0; tableIndex < numberOfTables ; ++tableIndex ) {
                if (!update( transactionManager,
                             dataSourceIndex, 
                             xaConnections[ tableIndex ], 
                             xaResources[ tableIndex ],
                             tableIndex, 
                             keys[ tableIndex ], 
                             values[ tableIndex ], 
                             stream ) ) {
                    return ONE_PHASE_COMMIT_TEST_FAILED;
                }
            }
            
            if ( !( (TyrexTransaction) transactionManager.getTransaction() ).canUseOnePhaseCommit() ) {
                stream.writeVerbose( "Cannot use one phase commit optimization" );    
                return ONE_PHASE_COMMIT_TEST_FAILED;
            }
    
            if ( !transactionBoundary( transactionManager, stream, true ) ) {
                    return false;
            }
            
            for ( tableIndex = 0; tableIndex < numberOfTables; ++tableIndex ) {
                if ( !checkValue( dataSourceIndex, 
                                  xaConnections[ tableIndex ], 
                                  tableIndex, 
                                  keys[ tableIndex ], 
                                  values [ tableIndex ], 
                                  stream ) ) {
                    stream.writeVerbose( "Failed to commit for table index " + 
                                         tableIndex + 
                                         " in data source index " + 
                                         dataSourceIndex);
                    return ONE_PHASE_COMMIT_TEST_FAILED;    
                }
            }
    
            return onePhaseFailed[0] ? ONE_PHASE_COMMIT_TEST_FAILED : ONE_PHASE_COMMIT_TEST_SUCCEEDED;
        } finally {
            for ( tableIndex = 1; tableIndex < numberOfTables; ++tableIndex) {
                try {   
                    xaConnections[ tableIndex ].close();
                } catch ( Exception e ) {

                }
            }
        }
    }
    */


     /**
      * Test that the synchronization methods are called
      * during a transaction boundary
      *
      * @param transactionManager the transaction manager
      * @param entries the array of test entries
      * @param stream the logging stream
      * @param commit True if commit is tested. False if
      *     rollback is tested.
      * @return True if the test was successful. Return false otherwise.
      * @throws Exception if there is an error with the test.
      * @see Entry
      */
    private boolean testSynchronization( TransactionManager transactionManager,
                                          Entry[] entries,
                                          CWVerboseStream stream,
                                          boolean commit )
        throws Exception
    {
        Entry entry;

        String value;
        
        final int[] completionCalled = {-1, -1};
        
        String testType = (commit ? "commit" : "rollback");
        
        stream.writeVerbose( "Test Synchronization during " + 
                              testType );

        transactionManager.begin();

         // add the synchronization to force the rollback
        transactionManager.getTransaction().registerSynchronization( new Synchronization()
            {
                public void afterCompletion(int mode) 
                {
                    completionCalled[1] = mode;
                }

                public void beforeCompletion() 
                {
                    completionCalled[0] = 0;
                }
            });

        for ( int i = 0; i < entries.length; ++i ) {
            entry = entries[ i ];

            if (commit) {
                entry.value += "new";
                value = entry.value;
            }
            else {
                value = entry.value + "new";
            }

            if ( !update( transactionManager, i, entry, value, stream )) {
                return false;
            }
        }


        if ( !transactionBoundary( transactionManager, stream, commit ) ) {
                    return false;
        }
         
        if (!checkValues(entries, stream)) {
            stream.writeVerbose( "Failed to " + testType );
            return false;
        }

        if ( completionCalled[0] == -1) {
            stream.writeVerbose( "Synchronization beforeCompletion method not called" );    
            return false;
        }

        if ( completionCalled[1] == -1) {
            stream.writeVerbose( "Synchronization afterCompletion method not called" );    
            return false;
        }

        if ( commit 
                ? ( completionCalled[1] != Status.STATUS_COMMITTED ) 
                : ( completionCalled[1] != Status.STATUS_ROLLEDBACK ) )
        {
            stream.writeVerbose( "Synchronization afterCompletion method called with incorrect status - " + 
                                 completionCalled[1]);    
        }

        return true;
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
    private boolean testDelist( TransactionManager transactionManager,
                                Entry[] entries,
                                CWVerboseStream stream,
                                boolean onePhaseCommit,
                                boolean commit,
                                boolean reuse )
        throws Exception
    {
        Entry entry;

        Entry[] testEntries = onePhaseCommit ? new Entry[]{entries[0]} : entries;

        int numberOfTestEntries = testEntries.length;

        String value;

        int i;

        String[] newKeys = null;
        String[] newValues = null;

        stream.writeVerbose( "Test " + 
                             ( commit ? "commit" : "rollback" ) +
                             ( onePhaseCommit 
                                ? ", using one-phase commit optimization,"
                                : "" ) +
                             " with resource delist (TMSUCCESS)" +
                             ( reuse 
                                ? " and resource re-enlistment in same transaction"
                                : "" ) );
                
        transactionManager.begin();

        for ( i = 0; i < numberOfTestEntries; ++i ) {
            entry = testEntries[ i ];

            value = entry.value + "new";

            if ( commit ) {
                entry.value = value;    
            }

            if ( !update( transactionManager, i, entry, value, stream )) {
                return false;
            }
        }

        for ( i = 0; i < numberOfTestEntries; ++i ) {
            transactionManager.getTransaction().delistResource( testEntries[ i ].xaResource, XAResource.TMSUCCESS );
        }

        if ( reuse ) {
            newKeys = new String[ numberOfTestEntries ];
            newValues = new String[ numberOfTestEntries ];

            for ( i = 0; i < numberOfTestEntries; ++i ) {
                entry = testEntries[ i ];

                newKeys[ i ] = helper.generateKey( i, DEFAULT_TABLE_INDEX );
                newValues[ i ] = helper.generateValue( i, DEFAULT_TABLE_INDEX, newKeys[ i ] );
    
                if ( !insert( transactionManager,
                              i,
                              entry.xaConnection,
                              entry.xaResource,
                              newKeys[ i ],
                              newValues[ i ],
                              stream ) ) {
                    return false;
                }
            }
        } else if ( helper.canReuseDelistedXAResources() &&
                    !useDelistedXAResourcesInNewTransaction( transactionManager,
                                                      testEntries,
                                                      stream ) ) {
            return false;
        }

        if ( onePhaseCommit && !( ( TyrexTransaction )transactionManager.getTransaction() ).canUseOnePhaseCommit( ) ) {
            stream.writeVerbose( "One phase commit optimization failed" );
            return false;
        }

        if ( !transactionBoundary( transactionManager, stream, commit ) ) {
                    return false;
        }
        
        if ( !checkValues(testEntries, stream) ) {
            return false;
        }

        if ( reuse) {
            for ( i = 0; i < numberOfTestEntries; ++i ) {
                entry = testEntries[ i ];

                if (!checkValue( i, entry.xaConnection, DEFAULT_TABLE_INDEX,
                                 newKeys[ i ], newValues[ i ], stream ) ) {
                    return false;    
                }
            }
        }

        return true;
    }


    /**
     * Use the XA resources in the specified entries that have been
     * delisted successfully (using XAResource.TMSUCCESS) in a transaction
     * that has not been committed, in a new transaction.
     *
     * @param transactionManager the transaction manager
     * @param entries the array of test entries
     * @param stream the logging stream
     * @return True if the new transaction was successful. 
     *      Return false otherwise.
     * @throws Exception if there is an error with using the delisted
     *      XA resources.
     * @see Entry
     * @see #testDelist
     */
    private boolean useDelistedXAResourcesInNewTransaction( final TransactionManager transactionManager,
                                                            final Entry[] entries,
                                                            final CWVerboseStream stream )
        throws Exception
    {
        final Object lock = new Object();

        final Object[] threadResults = new Object[ 1 ];

        synchronized ( lock ) {
            new Thread( new Runnable()
                        {
                            public void run() 
                            {
                                synchronized ( lock ) {
                                    try {
                                        threadResults[ 0 ] = testRollback( transactionManager, entries, stream ) 
                                                            ? Boolean.TRUE 
                                                            : Boolean.FALSE;
                                    } catch ( Exception e ) {
                                        threadResults[ 0 ] = e;
                                    }
    
                                    lock.notify();
                                }
                            }
                        } ).start();
            lock.wait();
        }

        if ( threadResults[ 0 ] != Boolean.TRUE ) {
            stream.writeVerbose( "Rollback in other transaction using delisted XA resources failed" );
            
            if ( threadResults[ 0 ] == Boolean.FALSE) {
                return false;
            }

            throw ( Exception )threadResults[ 0 ];    
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
     * @see Entry
     * @see #ONE_PHASE_COMMIT_PERFORMANCE_TEST
     * @see #TWO_PHASE_COMMIT_PERFORMANCE_TEST
     * @see #ROLLBACK_PERFORMANCE_TEST
     */
    private boolean performance( TransactionManager transactionManager,
                                 Entry[] entries,
                                 CWVerboseStream stream,
                                 int performanceMode )
        throws Exception
    {
        int numberOfEntries = entries.length;
        Entry entry;
        Connection connection = null;;
        int i;
        Timing timing;
        String transactionType = ( ( ONE_PHASE_COMMIT_PERFORMANCE_TEST == performanceMode )
                                    ? "one phase commit" 
                                    : ( ( TWO_PHASE_COMMIT_PERFORMANCE_TEST == performanceMode )
                                        ? "two phase commit"
                                        : "rollback" ) );

        stream.writeVerbose( "Performance: " + transactionType + " begin:" );
        timing = new Timing( "Performance: " + transactionType );
		timing.start();


        for ( int j = 0; j < COUNTER; ) {
            transactionManager.begin();
    
            for ( i = 0; i < numberOfEntries; ++i, ++j ) {
                entry = entries[ i ];
    
                // enlist the xa resource
                if (!transactionManager.getTransaction().enlistResource(entry.xaResource)) {
                    stream.writeVerbose( "Failed to enlist resource for iteration: " + j );
                    return false;
                }
    
                try {
                    connection = getConnection( entry );
                    // update value
                    helper.updateSQL( i, connection, 0, entry.key, "new 5555" );
                } finally {
                    closeConnection( connection );
                }
            }
    
            if ( TWO_PHASE_COMMIT_PERFORMANCE_TEST == performanceMode ) {
                transactionManager.commit();    
            } else if ( ONE_PHASE_COMMIT_PERFORMANCE_TEST == performanceMode ) {
                ( ( TyrexTransaction ) transactionManager.getTransaction() ).onePhaseCommit();
            } else {
                transactionManager.rollback();
            }
        }

        timing.stop();
		timing.count( COUNTER );
		stream.writeVerbose( timing.report() );
        stream.writeVerbose( "Minimum number of transactions expected: " + 
                             helper.getMinimumNumberOfTransactionsPerMinute( performanceMode ) );
        stream.writeVerbose( "Performance: " +  transactionType + " end" );

        return helper.getMinimumNumberOfTransactionsPerMinute( performanceMode ) <= timing.perMinute();
    }
    
    
    /**
     * Test rollback.
     *
     * @param transactionManager the transaction manager
     * @param entries the array of test entries
     * @param stream the logging stream
     * @return True if the test was successful. Return false otherwise.
     * @throws Exception if there is an error with the test.
     * @see Entry
     */
    private boolean testRollback( final TransactionManager transactionManager, 
                                  Entry[] entries,
                                  CWVerboseStream stream )
            throws Exception
    {
        Entry entry;

        final int[] synchronizationStatus = new int[]{ -1, -1 };
        final Exception[] synchronizationException = new Exception[ 1 ];
        
        stream.writeVerbose( "Test rollback" );
                
        transactionManager.begin();

        for ( int i = 0; i < entries.length; ++i ) {
            entry = entries[ i ];

            if ( !update( transactionManager, i, entry, entry.value + "new", stream ) ) {
                return false;    
            }
        }

        transactionManager.getTransaction().registerSynchronization(new Synchronization()
                {
                    public void afterCompletion(int mode) 
                    {
                        synchronizationStatus[ 1 ] = mode;
                    }

                    public void beforeCompletion() 
                    {
                        try {
                            synchronizationStatus[ 0 ] = transactionManager.getStatus();
                        } catch ( Exception e ) {
                            synchronizationException[ 0 ] = e;
                        }
                    }
                } );
        
        if ( !transactionBoundary( transactionManager, stream, false ) ) {
                    return false;
        }

        if ( null != synchronizationException[ 0 ] ) {
            throw synchronizationException[ 0 ];    
        }

        if ( -1 == synchronizationStatus[ 0 ] ) {
            stream.writeVerbose( "Before completion method not called" );
            return false;
        }
        
        if ( ( synchronizationStatus[ 0 ] != Status.STATUS_ROLLING_BACK ) ) {
            stream.writeVerbose( "BeforeCompletion called with incorrect status - " + 
                                 synchronizationStatus[ 0 ] );
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
    private void closeConnection( Connection connection )
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
    private Connection getConnection(Entry entry)
        throws SQLException
    {
        return getConnection(entry.xaConnection);
    }


    /**
     * Get the connection from the specified XA connection.
     *
     * @param xaConnection the XA connection.
     * @return the connection from the specified XA connection.
     * @throws SQLException if there is a problem geting the connection
     * @see Entry
     */
    private Connection getConnection(XAConnection xaConnection)
        throws SQLException
    {
        Connection connection = xaConnection.getConnection();

        if (connection.getAutoCommit()) {
            // turn off auto commit
            connection.setAutoCommit( false );
        }
        // set the isolation level
        //connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        return connection;
    }

    /**
     * Test asynchronous transactions
     *
     * @param transactionManager the transaction manager
     * @param entries the array of test entries
     * @param stream the logging stream
     * @param commit True if asynchronous commit is tested.
     *      False is asynchronous rollback is tested.
     * @return True if the test was successful. Return false otherwise.
     * @throws Exception if there is an error with the test.
     * @see Entry
     */
    private boolean testAsynchronousTransaction( TransactionManager transactionManager, 
                                                 Entry[] entries,
                                                 CWVerboseStream stream,
                                                 boolean commit )
        throws Exception
    {
        Entry entry;

        final Object lock = new Object();
        // a way to get any exceptions thrown by the async commit
        final Exception[] exception = new Exception[1];

        String testType = (commit ? "commit" : "rollback");
        
        stream.writeVerbose( "Test asynchronous " + testType );

        String value = null;
                
        transactionManager.begin();

        for ( int i = 0; i < entries.length; ++i ) {
            entry = entries[ i ];

            if ( commit ) {
                entry.value += "new";
                value = entry.value;
            } else {
                value = entry.value + "new";
            }

            if ( !update( transactionManager, i, entry, value, stream )) {
                return false;
            }
        }

        synchronized (lock) {
            AsyncCompletionCallback callback = new AsyncCompletionCallback()
                                                    {
                                                        public void exceptionOccurred(TyrexTransaction transaction, Exception e)
                                                        {
                                                            exception[0] = e;
                                                            synchronized (lock) {
                                                                lock.notify();
                                                            }
                                                        }
                                                        
                                                        public void beforeCompletion(TyrexTransaction transaction)
                                                        {

                                                        }
                                                        
                                                        public void afterCompletion(TyrexTransaction transaction)
                                                        {
                                                            synchronized (lock) {
                                                                lock.notifyAll();
                                                            }
                                                        }
                                                    };

            TyrexTransaction transaction = (TyrexTransaction)transactionManager.getTransaction();

            if ( commit ) {
                transaction.asyncCommit(callback);
            } else {
                transaction.asyncRollback(callback);
            }
            lock.wait();
        }

        if ( null != exception[0]) {
            throw exception[0];    
        }
        return checkValues(entries, stream);    
    }
    

    /**
     * Insert the specified key and value in the
     * data source using the specified xa connection.
     * The XA resource is enlisted in the current
     * transaction before the data source is changed.
     *
     * @param transactionManager the transaction manager
     * @param dataSourceIndex the dataSourceIndex of the data source in the helper
     * @param xaConnection the XA connection to the data source
     * @param xaResource the XA resource.
     * @param key the key. Assumed to be already existing
     *      in data source.
     * @param value the new value.
     * @param stream the logging stream
     * @return true if the update happened. Return false otherwise.
     * @throws Exception if there is a problem updating
     * @see Entry
     * @see #DEFAULT_TABLE_INDEX
     */
    private boolean insert( TransactionManager transactionManager,
                            int dataSourceIndex,
                            XAConnection xaConnection,
                            XAResource xaResource,
                            String key,
                            String value,
                            CWVerboseStream stream )
        throws Exception
    {
        Connection connection;

        // enlist the xa resource
        if (!transactionManager.getTransaction().enlistResource(xaResource)) {
            stream.writeVerbose( "Failed to enlist resource" );
            return false;
        }
        connection = getConnection( xaConnection );

        try {
            // insert key and value
            helper.insertSQL( dataSourceIndex, connection, DEFAULT_TABLE_INDEX, key, value );

            // make sure the value changed
            if ( !helper.checkValue( dataSourceIndex, connection, DEFAULT_TABLE_INDEX, key, value ) ) {
                stream.writeVerbose( "Insert failed for table index " + 
                                     DEFAULT_TABLE_INDEX + 
                                     " in data source index " + 
                                     dataSourceIndex );
                return false;    
            }

            return true;
        } finally {
            closeConnection(connection);
        }
    }

    
    /**
     * Test inserting items into the data sources and 
     * committing the changes.
     *
     * @param transactionManager the transaction manager
     * @param entries the array of test entries
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
    private boolean testCommit( final TransactionManager transactionManager, 
                                Entry[] entries,
                                CWVerboseStream stream,
                                int commitMode,
                                boolean insert )
        throws Exception
    {
        boolean used1PC = ( ( USE_1PC_COMMIT == commitMode ) ||
                            ( USE_1PC_COMMIT_OPTIMIZATION == commitMode ) );
        Entry[] testEntries = used1PC
                                ? ( 1 == entries.length ? entries :  new Entry[]{ entries[0] } )
                                : entries;
        int numberOfTestEntries = testEntries.length;
        int i;
        Entry entry;
        Connection connection;
        final int[] synchronizationStatus = new int[]{ -1, -1 };
        final Exception[] synchronizationException = new Exception[ 1 ];

        stream.writeVerbose( "Test commit using " +
                             ( ( USE_1PC_COMMIT == commitMode ) 
                                ? "one-phase commit" 
                                :   "two-phase commit" + 
                                    ( ( USE_1PC_COMMIT_OPTIMIZATION == commitMode ) 
                                      ? " with one-phase-commit optimization" 
                                      : "" ) ) );

        if ( insert ) {
            // make sure the keys dont not exist
            for ( i = 0; i < numberOfTestEntries; ++i ) {
                entry = testEntries[ i ];
                connection = getConnection( entry );
                try {
                    if ( !helper.checkValue( i, connection, DEFAULT_TABLE_INDEX, entry.key, null ) ) {
                        stream.writeVerbose( "Key already exists for data source " + i );
                        return false;    
                    }    
                } finally {
                    closeConnection(connection);
                }
            }
        }
        
        transactionManager.begin();

        for ( i = 0; i < numberOfTestEntries; ++i ) {
            entry = testEntries[ i ];

            if ( insert && !insert( transactionManager, i,
                          entry.xaConnection, entry.xaResource,
                          entry.key, entry.value,
                          stream) ) {
                stream.writeVerbose( "Insert failed" );
                return false;
            } else if ( !insert ) {
                entry.value += "new";

                if ( !update( transactionManager, i, entry, entry.value, stream ) ) {
                    return false;    
                }
            }
        }

        transactionManager.getTransaction().registerSynchronization(new Synchronization()
                {
                    public void afterCompletion(int mode) 
                    {
                        synchronizationStatus[ 1 ] = mode;
                    }

                    public void beforeCompletion() 
                    {
                        try {
                            synchronizationStatus[ 0 ] = transactionManager.getStatus();
                        } catch ( Exception e ) {
                            synchronizationException[ 0 ] = e;
                        }
                    }
                } );
        
        if ( ( USE_1PC_COMMIT_OPTIMIZATION == commitMode ) &&
             !( ( TyrexTransaction ) transactionManager.getTransaction() ).canUseOnePhaseCommit() ) {
            stream.writeVerbose( "One phase commit optimization not working ");
            return false;
        }


        if ( USE_1PC_COMMIT == commitMode ) {
            ( ( TyrexTransaction ) transactionManager.getTransaction() ).onePhaseCommit();    
        } else {
            transactionManager.commit();
        }
        
        if ( null != transactionManager.getTransaction() ) {
            stream.writeVerbose( "Thread still associated with transaction after commit ");
            return false;
        }
        
        if ( null != synchronizationException[ 0 ] ) {
            throw synchronizationException[ 0 ];    
        }

        if ( -1 == synchronizationStatus[ 0 ] ) {
            stream.writeVerbose( "Before completion method not called" );
            return false;
        }
        
        if ( !used1PC && 
             ( synchronizationStatus[ 0 ] != Status.STATUS_PREPARING ) ) {
            stream.writeVerbose( "BeforeCompletion - Prepare phase called with incorrect status - " + 
                                 synchronizationStatus[ 0 ] );
            return false;
        }

        if ( used1PC && 
             ( synchronizationStatus[ 0 ] != Status.STATUS_COMMITTING ) ) {
            stream.writeVerbose( "BeforeCompletion - One phase commit called with incorrect status - " + 
                                 synchronizationStatus[ 0 ] );
            return false; 
        }

        if ( -1 == synchronizationStatus[ 1 ] ) {
            stream.writeVerbose( "After completion method not called" );
            return false;
        }

        if ( Status.STATUS_COMMITTED != synchronizationStatus[ 1 ] ) {
            stream.writeVerbose( "After completion method called with incorrect status - " +
                                 synchronizationStatus[ 1 ] );
            return false;
        }

        return checkValues(testEntries, stream);
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
        XAConnection xaConnection;
        XAResource xaResource;
        String key;
        int entryIndex = 0;
        

        //java.sql.DriverManager.setLogWriter(new java.io.PrintWriter(System.out, true));
        // populate it
        for ( int xaDataSourceIndex = 0; xaDataSourceIndex < numberOfDataSources; ++xaDataSourceIndex ) {
            if ( helper.getNumberOfTables( xaDataSourceIndex ) <= 0 ) {
                continue;    
            }

            // get the xa connection
            xaConnection = helper.getXAConnection( xaDataSourceIndex );
            //System.out.println("xaConnection " + xaConnection);
            xaResource = xaConnection.getXAResource();
            //System.out.println("xaResource " + xaResource);
                        
            key = helper.generateKey( xaDataSourceIndex, DEFAULT_TABLE_INDEX );
            entries[ entryIndex++ ] = new Entry( xaConnection, 
                                          xaResource,
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
         * The xa connection
         */
        private XAConnection xaConnection;


        /**
         * The xa resource
         */
        private XAResource xaResource;


        /**
         * The key
         */
        private String key;


        /**
         * The value stored with the key
         */
        private String value;


        /**
         * Create the Entry with the specified
         * arguments.
         *
         * @param xaConnection the physical connection
         * @param xaResource the xa resource
         * @param key the primary key
         * @param value the value
         */
        private Entry( XAConnection xaConnection,
                           XAResource xaResource,
                           String key,
                           String value )
        {
            this.xaConnection = xaConnection;
            this.xaResource = xaResource;
            this.key = key;
            this.value = value;
        }
    }
}

