package ots;

import tests.*;

import junit.framework.*;

public class OTS extends TestSuite
{   
    public static VerboseStream stream = new VerboseStream();
    
    /**
     * The TransactionCurrent (OTS Server)
     */
    public static org.omg.CosTransactions.Current current;
    
    public OTS( String name )
    {
        super( name );
        
        TestCase tc;
        
        tc = new BasicTransactionTest();
        addTest( tc ); 
        tc = new IsTransactionTest();
        addTest( tc ); 
        tc = new CommitTransactionTest();
        addTest( tc ); 
        tc = new RollbackTransactionTest();
        addTest( tc );
        tc = new RollbackOnlyTransactionTest();
        addTest( tc );        
        tc = new SynchronizationTransactionTest();
        addTest( tc );
        tc = new CommitOnePhaseTransactionTest();
        addTest( tc );
        tc = new CommitResourcesTransactionTest();
        addTest( tc );
        tc = new RollbackResourcesTransactionTest();
        addTest( tc );
        tc = new ReadOnlyTransactionTest();
        addTest( tc );
        tc = new RollbackVoteTransactionTest();
        addTest( tc );
        tc = new ComplexRollbackVoteTransactionTest();
        addTest( tc );
        tc = new SuspendTransactionTest();
        addTest( tc );
        tc = new ComplexSuspendTransactionTest();
        addTest( tc );
        tc = new TimeOutTransactionTest();
        addTest( tc );
        tc = new ExplicitTransactionTest();
        addTest( tc );
        tc = new PropagationContextTransactionTest();
        addTest( tc );
        //tc = new SubordinateTransactionTest();
        //addTest( tc );
        //tc = new SubordinateRollbackTransactionTest();
        //addTest( tc );
        tc = new BasicMultipleTransactionTest();
        addTest( tc);
        tc = new MultipleTransactionTest();
        addTest( tc );
        tc = new BasicSubTransactionTest();
        addTest( tc );
        //tc = new SubtransactionTest();
        //addTest( tc );
        tc = new SubtransactionResourceTest();
        addTest( tc );
    }
    
    /**
     * TC 01 : Basic transaction test
     */
    public static class BasicTransactionTest extends OTSTestCase
    {        
        public BasicTransactionTest()
        {
            super( "[TC01] Basic transaction test" );
        }
        
        /**
         * Main test method - TC01
         *  - check status is StatusNoTransaction
         *  - begin the transaction
         *  - check its status is StatusActive
         *  - commit the transaction
         *  - check its status is StatusNoTransaction
         */        
        public void runTest()
        {                       
            try
            {                                
                stream.writeVerbose("Get the current object'");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                                
                org.omg.CosTransactions.Status status = current.get_status();
         
                assert( "Transaction should have been active.", status.value() == org.omg.CosTransactions.Status._StatusNoTransaction );
                 
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                status = current.get_status();

                assert( status.value() == org.omg.CosTransactions.Status._StatusActive );
                
                stream.writeVerbose("Transaction name " + current.get_transaction_name() );
                
                stream.writeVerbose("Commit the transaction");
                current.commit( true );    
                
                status = current.get_status();
                
                assert ( status.value() == org.omg.CosTransactions.Status._StatusNoTransaction );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }

    /**
     * TC 02 : Commit transaction test
     */
    public static class IsTransactionTest extends OTSTestCase
    {        
        public IsTransactionTest()
        {
            super( "[TC02] Is transaction test" );
        }
        
        /**
         * Main test method - TC02
         *  - try to commit a transaction 
         *  - catch the expected exception NoTransaction
         */               
        public void runTest()
        {                       
            try
            {                                
                stream.writeVerbose("Get the current object'");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Commit the transaction");
                try
                {
                    current.commit( true );    
                }
                catch ( org.omg.CosTransactions.NoTransaction e )
                {
                    return;;
                }
                
                fail("Transactioon not commited");
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 03 : Commit transaction test
     */
    public static class CommitTransactionTest extends OTSTestCase
    {        
        public CommitTransactionTest()
        {
            super( "[TC03] Simple commit test" );
        }
        
        /**
         * Main test method - TC03
         *  - begin the transaction
         *  - do some operations (credit/debt)
         *  - check the results
         *  - commit the transaction
         */                
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the basic test'");
                bankImpl bk = new bankImpl( _server_orb );
            
                otstests.bank bobj = otstests.bankHelper.narrow( getObject( bk ) );
                
                stream.writeVerbose("Get the current object'");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                bobj.credit( 1000 );
                
                bobj.debit( 500 );
                
                assert ( "Balance is not equal to 500", bobj.balance() == 500 ); 
                
                stream.writeVerbose("Commit the transaction");
                current.commit( true );    
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }

    /**
     * TC 04 : Rollback transaction test
     */
    public static class RollbackTransactionTest extends OTSTestCase
    {        
        public RollbackTransactionTest()
        {
            super( "[TC04] Simple rollback test" );
        }
        
        
        /**
         * Main test method - TC04
         *  - begin the transaction
         *  - do some operations (credit/debt)
         *  - check the results
         *  - rollback the transaction
         */     
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the basic test'");
                bankImpl bk = new bankImpl( _server_orb );
            
                otstests.bank bobj = otstests.bankHelper.narrow( getObject( bk ) );
                
                stream.writeVerbose("Get the current object'");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                bobj.credit( 1000 );
                
                bobj.debit( 500 );
                
                assert ( "Balance is not equal to 500", bobj.balance() == 500 ); 
                
                stream.writeVerbose("rollback the transaction");
                current.rollback();    
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 05 : Rollback only transaction test
     */
    public static class RollbackOnlyTransactionTest extends OTSTestCase
    {        
        public RollbackOnlyTransactionTest()
        {
            super( "[TC05] Simple rollback only test" );
        }
         
        /**
         * Main test method - TC05
         *  - begin the transaction
         *  - do some operations (credit/debt)
         *  - try to commit
         *  - transaction should have been rollbacked
         */             
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the basic test'");
                bankImpl bk = new bankImpl( _server_orb );
            
                otstests.bank bobj = otstests.bankHelper.narrow( getObject( bk ) );
                
                stream.writeVerbose("Get the current object'");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                bobj.debit( 500 );
                
                stream.writeVerbose("Commit the transaction");
                try
                {
                    current.commit( true );    
                }
                catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK e )
                {                
                    return;
                }
                
                fail( "Transaction was not rollbacked" ); 
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }

    /**
     * TC 06 : Synchronization transaction test
     */    
    public static class SynchronizationTransactionTest extends OTSTestCase
    {        
        public SynchronizationTransactionTest()
        {
            super( "[TC06] Simple synchronization test" );
        }
        
        /**
         * Main test method - TC06
         *  - begin the transaction
         *  - register a synchronization object
         *  - do some operations (credit/debt)
         *  - check the results
         *  - try to commit
         *  - synchronization should have been invoked
         */               
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the basic test'");
                bankImpl bk = new bankImpl( _server_orb );
                
                synchroImpl synchro = new synchroImpl();
            
                otstests.bank bobj = otstests.bankHelper.narrow( getObject( bk ) );
                
                stream.writeVerbose("Get the current object");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                stream.writeVerbose("Register the synchronization object");
                
                try
                {
                    org.omg.CosTransactions.Synchronization sync = org.omg.CosTransactions.SynchronizationHelper.narrow( getObject( synchro ) );
                    
                    current.get_control().get_coordinator().register_synchronization( sync );
                }
                catch ( java.lang.Exception e )
                {
                    e.printStackTrace();
                    fail( e.getMessage() );
                }                                
                
                bobj.credit( 1000 );
                
                bobj.debit( 500 );
                
                assert ( "Balance is not equal to 500", bobj.balance() == 500 ); 
                
                stream.writeVerbose("commit the transaction");
                current.commit( true );    
                
                stream.writeVerbose("check if the synchronization was used");
                
                assert( "Synchronization not invoked", synchro.isInvoked() );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
 
    /**
     * TC 07 : Commit one phase transaction test
     */    
    public static class CommitOnePhaseTransactionTest extends OTSTestCase
    {        
        public CommitOnePhaseTransactionTest()
        {
            super( "[TC07] Commit one phase test" );
        }
        
        /**
         * Main test method - TC07
         *  - begin the transaction
         *  - register a resource object
         *  - do some operations (credit/debt)
         *  - check the results
         *  - try to commit using 1PC
         *  - check 1PC was used
         */                       
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                bankImpl bk = new bankImpl( _server_orb );
                
                resourceImpl r = new resourceImpl();
            
                otstests.bank bobj = otstests.bankHelper.narrow( getObject( bk ) );
                
                stream.writeVerbose("Get the current object");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                stream.writeVerbose("Register the resource object");
                
                try
                {
                    org.omg.CosTransactions.Resource res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r ) );
                    
                    current.get_control().get_coordinator().register_resource( res );
                }
                catch ( java.lang.Exception e )
                {
                    e.printStackTrace();
                    fail( e.getMessage() );
                }                                
                
                bobj.credit( 1000 );
                
                bobj.debit( 500 );
                
                assert ( "Balance is not equal to 500", bobj.balance() == 500 ); 
                
                stream.writeVerbose("commit the transaction");
                current.commit( true );    
                
                stream.writeVerbose("check if the one phase commit was used");
                                
                assert ( "One phase commit not used", r.one_phase ); 
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 08 : Commit resources transaction test
     */    
    public static class CommitResourcesTransactionTest extends OTSTestCase
    {        
        public CommitResourcesTransactionTest()
        {
            super( "[TC08] Commit resources test" );
        }
        
        /**
         * Main test method - TC08
         *  - begin the transaction
         *  - register 2 resource objects
         *  - do some operations (credit/debt)
         *  - check the results
         *  - check the resources were commited
         */                
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                bankImpl bk = new bankImpl( _server_orb );
                
                resourceImpl r1 = new resourceImpl();
            
                resourceImpl r2 = new resourceImpl();
                
                otstests.bank bobj = otstests.bankHelper.narrow( getObject( bk ) );
                
                stream.writeVerbose("Get the current object");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                stream.writeVerbose("Register the resource objects");
                
                try
                {
                    org.omg.CosTransactions.Resource res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r1 ) );
                    
                    current.get_control().get_coordinator().register_resource( res );
                    
                    res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r2 ) );
                    
                    current.get_control().get_coordinator().register_resource( res );
                }                
                catch ( java.lang.Exception e )
                {
                    e.printStackTrace();
                    fail( e.getMessage() );
                }                                
                
                bobj.credit( 1000 );
                
                bobj.debit( 500 );
                
                assert ( "Balance is not equal to 500", bobj.balance() == 500 ); 
                
                stream.writeVerbose("commit the transaction");
                current.commit( true );    
                
                stream.writeVerbose("check if the resources have been commited");
                
                assert ( "Resources haven't been commited", ( r1.commit && r2.commit ) );
              
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 09 : Commit one phase transaction test
     */    
    public static class RollbackResourcesTransactionTest extends OTSTestCase
    {        
        public RollbackResourcesTransactionTest()
        {
            super( "[TC09] Rollback resources test" );
        }
        
        /**
         * Main test method - TC09
         *  - begin the transaction
         *  - register 2 resource objects
         *  - do some operations (credit/debt)
         *  - check the results
         *  - check the resources were rollbacked
         */               
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                bankImpl bk = new bankImpl( _server_orb );
                
                resourceImpl r1 = new resourceImpl();
            
                resourceImpl r2 = new resourceImpl();
                
                otstests.bank bobj = otstests.bankHelper.narrow( getObject( bk ) );
                
                stream.writeVerbose("Get the current object");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                stream.writeVerbose("Register the resource objects");
                
                try
                {
                    org.omg.CosTransactions.Resource res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r1 ) );
                    
                    current.get_control().get_coordinator().register_resource( res );
                    
                    res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r2 ) );
                    
                    current.get_control().get_coordinator().register_resource( res );
                }                
                catch ( java.lang.Exception e )
                {
                    e.printStackTrace();
                    fail( e.getMessage() );
                }                                
                
                bobj.debit( 500 );
                
                stream.writeVerbose("commit the transaction");
                try
                {
                    current.commit( true );    
                }
                catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK e )
                {                
                    stream.writeVerbose("check if the resources have been rolledback");
                
                    assert ( "Resources haven't been rollbacked", ( r1.rollback && r2.rollback)  );
                    
                    return; 
                }
                                                
                fail( "Error: resources weren't rollbacked" );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 10 : Read only transaction test
     */    
    public static class ReadOnlyTransactionTest extends OTSTestCase
    {        
        public ReadOnlyTransactionTest()
        {
            super( "[TC10] Readonly test" );
        }
        
        /**
         * Main test method - TC10
         *  - begin the transaction
         *  - register a resource object
         *  - do some operations (credit/debt)
         *  - check the results
         *  - commit the transaction
         *  - check the resource was commited
         */                 
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                bankImpl bk = new bankImpl( _server_orb );
                
                resourceImpl r1 = new resourceImpl();
                r1.readonly_vote = true;
            
                resourceImpl r2 = new resourceImpl();
                
                otstests.bank bobj = otstests.bankHelper.narrow( getObject( bk ) );
                
                stream.writeVerbose("Get the current object");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                stream.writeVerbose("Register the resource objects");
                
                try
                {
                    org.omg.CosTransactions.Resource res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r1 ) );
                    
                    current.get_control().get_coordinator().register_resource( res );
                    
                    res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r2 ) );
                    
                    current.get_control().get_coordinator().register_resource( res );
                }                
                catch ( java.lang.Exception e )
                {
                    e.printStackTrace();
                    fail( e.getMessage() );
                }                                
                
                bobj.credit( 500 );
                
                stream.writeVerbose("commit the transaction");
                
                current.commit( true );    
                
                assert( "Resource wasn't commited",  !r1.commit );
                                               
                return;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 11 : Rollack vote test
     */    
    public static class RollbackVoteTransactionTest extends OTSTestCase
    {        
        public RollbackVoteTransactionTest()
        {
            super( "[TC11] Rollback vote test" );
        }
               
        /**
         * Main test method - TC11
         *  - begin the transaction
         *  - register a resource object
         *  - do some operations (credit/debt)
         *  - check the results
         *  - vote rollback the transaction
         *  - check the resource was rollbacked
         */                
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                bankImpl bk = new bankImpl( _server_orb );
                
                resourceImpl r1 = new resourceImpl();
                r1.rollback_vote = true;
            
                resourceImpl r2 = new resourceImpl();
                
                otstests.bank bobj = otstests.bankHelper.narrow( getObject( bk ) );
                
                stream.writeVerbose("Get the current object");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                stream.writeVerbose("Register the resource objects");
                
                try
                {
                    org.omg.CosTransactions.Resource res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r1 ) );
                    
                    current.get_control().get_coordinator().register_resource( res );
                    
                    res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r2 ) );
                    
                    current.get_control().get_coordinator().register_resource( res );
                }                
                catch ( java.lang.Exception e )
                {
                    e.printStackTrace();
                    fail( e.getMessage() );
                }                                
                
                bobj.credit( 500 );
                
                stream.writeVerbose("commit the transaction");
                
                try                
                {
                    current.commit( true );    
                }
                catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK e )
                {
                    assert ( "Resource not rollbacked", r2.rollback );
                   
                    return;
                }                            
                
                fail("Error");
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 12 : Complex rollack vote test
     */    
    public static class ComplexRollbackVoteTransactionTest extends OTSTestCase
    {        
        public ComplexRollbackVoteTransactionTest()
        {
            super( "[TC12] Complex rollback vote test" );
        }
        
        /**
         * Main test method - TC12
         *  - begin the transaction
         *  - register 2 resource objects
         *  - do some operations (credit/debt)
         *  - check the results
         *  - vote rollback the transaction
         *  - check the resources were rollbacked
         */            
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                bankImpl bk = new bankImpl( _server_orb );
                
                resourceImpl r1 = new resourceImpl();                
            
                resourceImpl r2 = new resourceImpl();
                r2.rollback_vote = true;
                
                otstests.bank bobj = otstests.bankHelper.narrow( getObject( bk ) );
                
                stream.writeVerbose("Get the current object");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                stream.writeVerbose("Register the resource objects");
                
                try
                {
                    org.omg.CosTransactions.Resource res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r1 ) );
                    
                    current.get_control().get_coordinator().register_resource( res );
                    
                    res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r2 ) );
                    
                    current.get_control().get_coordinator().register_resource( res );
                }                
                catch ( java.lang.Exception e )
                {
                    e.printStackTrace();
                    fail( e.getMessage() );
                }                                
                
                bobj.credit( 500 );
                
                stream.writeVerbose("commit the transaction");
                
                try                
                {
                    current.commit( true );    
                }
                catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK e )
                {
                    assert ( "Resource not rollbacked", r1.rollback );
                                 
                    return;
                }                            
                
                fail( "Error" );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 13 : Suspend test
     */    
    public static class SuspendTransactionTest extends OTSTestCase
    {        
        public SuspendTransactionTest()
        {
            super( "[TC13] Simple suspend test" );
        }
        
        /**
         * Main test method - TC13
         *  - begin the transaction
         *  - suspend the transaction
         *  - begin a new transaction
         *  - resume the transaction
         *  - check the results
         *  - vote rollback the transaction
         *  - check the resources were rollbacked
         */                    
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                
                stream.writeVerbose("Get the current object");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                stream.writeVerbose("Suspend the first transaction...");
                
                org.omg.CosTransactions.Control ctrl = current.suspend();
                                                
                stream.writeVerbose("Begin a new transaction");
                
                current.begin();                                
                
                stream.writeVerbose("commit the transaction");
                
                current.commit( true );    
                
                stream.writeVerbose("Resume the first transaction");
                
                current.resume( ctrl );
                
                stream.writeVerbose("commit the first transaction");
                
                current.commit( true );    
                
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 14 : Complex suspend test
     */    
    public static class ComplexSuspendTransactionTest extends OTSTestCase
    {        
        public ComplexSuspendTransactionTest()
        {
            super( "[TC14] Complex suspend test" );
        }
        
        /**
         * Main test method - TC14
         *  - begin the transaction
         *  - suspend the transaction
         *  - begin a new transaction
         *  - resume the transaction
         *  - check the results
         *  - vote rollback the transaction
         *  - check the resources were rollbacked
         */                    
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                
                stream.writeVerbose("Get the current object");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                stream.writeVerbose("Suspend the first transaction...");
                
                org.omg.CosTransactions.Control ctrl1 = current.suspend();
                                                
                stream.writeVerbose("Begin a new transaction");
                
                current.begin();                          
                
                stream.writeVerbose("Mark the second transaction as rollback only...");
                
                current.rollback_only();
                
                stream.writeVerbose("Suspend the second transaction...");
                
                org.omg.CosTransactions.Control ctrl2 = current.suspend();
                
                stream.writeVerbose("Resume the first transaction");
                
                current.resume( ctrl1 );
                                
                stream.writeVerbose("commit the first transaction");
                                
                current.commit( true );    
                
                stream.writeVerbose("Resume the second transaction");
                
                current.resume( ctrl2 );
                
                stream.writeVerbose("commit the second transaction");
                
                try
                {
                    current.commit( true );    
                }
                catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK e )
                {
                    return;
                }
                
                fail( "Error" );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 15 : TimeOut test
     */    
    public static class TimeOutTransactionTest extends OTSTestCase
    {        
        public TimeOutTransactionTest()
        {
            super( "[TC15] TimeOut test" );
        }
        
        /**
         * Main test method - TC15
         *  - begin the transaction
         *  - set timeout to 5 secs
         *  - wait 15 secs
         *  - check the transaction was rollbacked
         */                    
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                
                stream.writeVerbose("Get the current object");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );                               
                
                stream.writeVerbose("Set the time out to 5 seconds...");
                current.set_timeout( 5 );
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                stream.writeVerbose("Wait for 15 seconds...");
                
                java.lang.Thread.currentThread().sleep( 15000 );
                
                try
                {
                    stream.writeVerbose("Commit the transaction...");
                    current.commit( true );    
                }
                catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK e )
                {
                    return;
                }
                
                fail("Error");
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }

    /**
     * TC 16 : Explicit transaction management
     */    
    public static class ExplicitTransactionTest extends OTSTestCase
    {        
        public ExplicitTransactionTest()
        {
            super( "[TC16] Explicit transaction test" );
        }
        
        /**
         * Main test method - TC16
         *  - begin the transaction
         *  - set timeout to 5 secs
         *  - wait 15 secs
         *  - check the transaction was rollbacked
         */                 
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                
                bankImpl bk = new bankImpl( _server_orb );
                
                resourceImpl r1 = new resourceImpl();                
            
                resourceImpl r2 = new resourceImpl();
                
                otstests.bank bobj = otstests.bankHelper.narrow( getObject( bk ) );
                                
                stream.writeVerbose("Get the transaction factory");
                
                org.omg.CORBA.Object obj = _client_orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");
                
                org.omg.CosTransactions.TransactionFactory factory = org.omg.CosTransactions.TransactionFactoryHelper.narrow( obj );
                                
                stream.writeVerbose("Create a new transaction");
                
                org.omg.CosTransactions.Control ctrl = factory.create( 0 );
                
                stream.writeVerbose("Register the resource objects");
                
                try
                {
                    org.omg.CosTransactions.Resource res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r1 ) );
                    
                    ctrl.get_coordinator().register_resource( res );
                    
                    res = org.omg.CosTransactions.ResourceHelper.narrow( getObject( r2 ) );
                    
                    ctrl.get_coordinator().register_resource( res );
                }                
                catch ( java.lang.Exception e )
                {
                    e.printStackTrace();
                    fail( e.getMessage() );
                }                                
                
                bobj.credit( 500 );
                
                stream.writeVerbose("Commit the transaction");
                ctrl.get_terminator().commit( true );
                
                return;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 17 : Propagation context test
     */    
    public static class PropagationContextTransactionTest extends OTSTestCase
    {        
        public PropagationContextTransactionTest()
        {
            super( "[TC17] Propagation context test" );
        }
                
        /**
         * Main test method - TC17
         *  - begin the transaction
         *  - get propagation context
         *  - check the propagation context
         */                
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                                            
                stream.writeVerbose("Get the transaction factory");
                
                org.omg.CORBA.Object obj = _client_orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");
                
                org.omg.CosTransactions.TransactionFactory factory = org.omg.CosTransactions.TransactionFactoryHelper.narrow( obj );
                                
                stream.writeVerbose("Create a new transaction");
                
                org.omg.CosTransactions.Control ctrl = factory.create( 10 );
                
                stream.writeVerbose("Get the propagation context");
                
                org.omg.CosTransactions.PropagationContext pctx = ctrl.get_coordinator().get_txcontext();
                
                assert ( pctx.current.coord.equals( ctrl.get_coordinator() ) );
                
                assert ( pctx.current.term.equals( ctrl.get_terminator() ) );
                
                assert ( pctx.timeout == 10 );
                 
                assert ( pctx.parents.length == 0 );
                
                stream.writeVerbose("Commit the transaction");
                ctrl.get_terminator().commit( true );
                
                return;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 18 : Subordinate test
     */    
    public static class SubordinateTransactionTest extends OTSTestCase
    {        
        public SubordinateTransactionTest()
        {
            super( "[TC18] Subordinate test" );
        }
        
        /**
         * Main test method - TC18
         *  - create a transaction
         *  - get propagation context
         *  - recreate a transaction
         *  - register a synchronization object
         *  - commit the transaction
         *  - check the synchronization object was invoked
         */            
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                                            
                stream.writeVerbose("Get the transaction factory");
                
                org.omg.CORBA.Object obj = _client_orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");
                
                org.omg.CosTransactions.TransactionFactory factory = org.omg.CosTransactions.TransactionFactoryHelper.narrow( obj );
                                
                stream.writeVerbose("Create a new transaction");
                
                org.omg.CosTransactions.Control ctrl = factory.create( 10 );

                stream.writeVerbose("Get the propagation context");
                
                org.omg.CosTransactions.PropagationContext pctx = ctrl.get_coordinator().get_txcontext();
                
                stream.writeVerbose("Re-create a transaction");
                
                org.omg.CosTransactions.Control ctrl2 = factory.recreate( pctx );
                
                synchroImpl synchro = new synchroImpl();
                            
                stream.writeVerbose("Register the synchronization object");
                
                try
                {
                    org.omg.CosTransactions.Synchronization sync = org.omg.CosTransactions.SynchronizationHelper.narrow( getObject( synchro ) );
                    
                    ctrl2.get_coordinator().register_synchronization( sync );
                }
                catch ( java.lang.Exception e )
                {
                    e.printStackTrace();
                    fail( e.getMessage() );
                }          
                                
                stream.writeVerbose("Commit the transaction");
                ctrl.get_terminator().commit( true );
                
                assert ( "Synchronization not invoked", synchro.isInvoked() );
                
                return;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 19 : Subordinate Rollback test
     */    
    public static class SubordinateRollbackTransactionTest extends OTSTestCase
    {        
        public SubordinateRollbackTransactionTest()
        {
            super( "[TC19] Subordinate rollback test" );
        }
         
        /**
         * Main test method - TC19
         *  - create a transaction
         *  - get propagation context
         *  - recreate a transaction
         *  - register a synchronization object
         *  - rollback the transaction
         *  - check the transaction was rollbacked
         */          
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                                            
                stream.writeVerbose("Get the transaction factory");
                
                org.omg.CORBA.Object obj = _client_orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");
                
                org.omg.CosTransactions.TransactionFactory factory = org.omg.CosTransactions.TransactionFactoryHelper.narrow( obj );
                                
                stream.writeVerbose("Create a new transaction");
                
                org.omg.CosTransactions.Control ctrl = factory.create( 10 );

                stream.writeVerbose("Get the propagation context");
                
                org.omg.CosTransactions.PropagationContext pctx = ctrl.get_coordinator().get_txcontext();
                
                stream.writeVerbose("Re-create a transaction");
                
                org.omg.CosTransactions.Control ctrl2 = factory.recreate( pctx );                                
                   
                ctrl2.get_coordinator().rollback_only();
                 
                try                
                {
                    stream.writeVerbose("Commit the transaction");
                    ctrl.get_terminator().commit( true );
                }
                catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK e )
                {
                    return;
                }
                
                fail( "Error" );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }

    /**
     * TC 20 : Basic multiple transactions test
     */    
    public static class BasicMultipleTransactionTest extends OTSTestCase
    {        
        public BasicMultipleTransactionTest()
        {
            super( "[TC20] Basic multiple transactions test" );
        }
        
        /**
         * Main test method - TC20
         *  - create a transaction
         *  - create a transaction
         *  - compares the 2 transactions 
         */          
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                                            
                stream.writeVerbose("Get the transaction factory");
                
                org.omg.CORBA.Object obj = _client_orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");
                
                org.omg.CosTransactions.TransactionFactory factory = org.omg.CosTransactions.TransactionFactoryHelper.narrow( obj );
                                
                stream.writeVerbose("Create a new transaction");
                
                org.omg.CosTransactions.Control ctrl = factory.create( 0 );
                
                stream.writeVerbose("Create a new transaction");
                
                org.omg.CosTransactions.Control ctrl2 = factory.create( 0 );                                
                
                stream.writeVerbose("Compare transactions");   
                
                org.omg.CosTransactions.Coordinator coord1 = ctrl.get_coordinator();
                
                org.omg.CosTransactions.Coordinator coord2 = ctrl2.get_coordinator();
                
                assert ( coord1.is_same_transaction( coord1 ) );
                
                assert ( !coord1.is_same_transaction( coord2 ) );
                
                assert ( !coord1.is_related_transaction( coord2 ) );
                   
                ctrl.get_terminator().commit( true );
                
                ctrl2.get_terminator().commit( true );
                
                return;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 21 : Multiple transactions test
     */    
    public static class MultipleTransactionTest extends OTSTestCase
    {        
        public MultipleTransactionTest()
        {
            super( "[TC21] Multiple transactions test" );
        }
         
        /**
         * Main test method - TC21
         *  - create a transaction
         *  - create a transaction
         *  - compares the 2 transactions  
         */          
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                                            
                stream.writeVerbose("Get the transaction factory");
                
                org.omg.CORBA.Object obj = _client_orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");
                
                org.omg.CosTransactions.TransactionFactory factory = org.omg.CosTransactions.TransactionFactoryHelper.narrow( obj );
                                
                stream.writeVerbose("Create a new transaction");
                
                org.omg.CosTransactions.Control ctrl = factory.create( 0 );
                
                stream.writeVerbose("Create a new transaction");
                
                org.omg.CosTransactions.Control ctrl2 = factory.create( 0 );                                
                
                stream.writeVerbose("Compare transactions");   
                
                ctrl.get_coordinator().rollback_only();
                
                ctrl2.get_terminator().commit( true );
                
                try
                {
                    ctrl.get_terminator().commit( true );
                }
                catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK e )
                {
                    return;
                }
                
                fail( "Error" );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 22 : Basic subtransaction test
     */    
    public static class BasicSubTransactionTest extends OTSTestCase
    {        
        public BasicSubTransactionTest()
        {
            super( "[TC22] Basic subtransaction test" );
        }
         
        /**
         * Main test method - TC22
         *  - begin a transaction
         *  - begin a sub transaction
         *  - commit the sub transaction
         *  - commit the top level transaction 
         */          
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                                            
                stream.writeVerbose("Get the current object");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );                               
                
                stream.writeVerbose("Begin a transaction");
                current.begin();
                
                stream.writeVerbose("Begin a sub transaction");
                current.begin();
                
                stream.writeVerbose("Commit the sub transaction");
                current.commit( true );
                
                stream.writeVerbose("Commit the top level transaction");
                current.commit( true );
                                
                return;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 23 : Subtransaction test
     */    
    public static class SubtransactionTest extends OTSTestCase
    {        
        public SubtransactionTest()
        {
            super( "[TC23) Subtransaction test" );
        }
        
        /**
         * Main test method - TC23
         *  - create a transaction
         *  - create a sub transaction
         *  - compare the transactions
         *  - commit the sub transaction
         *  - commit the top level transaction  
         */          
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                                            
                stream.writeVerbose("Get the transaction factory");
                
                org.omg.CORBA.Object obj = _client_orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");
                
                org.omg.CosTransactions.TransactionFactory factory = org.omg.CosTransactions.TransactionFactoryHelper.narrow( obj );
                                
                stream.writeVerbose("Create a new transaction");
                
                org.omg.CosTransactions.Control ctrl = factory.create( 0 );

                stream.writeVerbose("Create a sub transaction");
                
                org.omg.CosTransactions.Control sub = ctrl.get_coordinator().create_subtransaction();
                
                stream.writeVerbose("Compare the transaction");
                
                org.omg.CosTransactions.Coordinator coord1 = ctrl.get_coordinator();
                
                org.omg.CosTransactions.Coordinator coord2 = sub.get_coordinator();
                
                assert ( !coord1.is_same_transaction( coord2 ) );
                         
                assert ( !coord1.is_related_transaction( coord2 ) );
                
                assert ( coord2.is_descendant_transaction( coord1 ) );
                
                assert ( coord1.is_ancestor_transaction( coord2 ) );

                sub.get_terminator().commit( true );
                
                ctrl.get_terminator().commit( true );
                
                return;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * TC 24 : Subtransaction Resource test
     */    
    public static class SubtransactionResourceTest extends OTSTestCase
    {        
        public SubtransactionResourceTest()
        {
            super( "[TC24] Subtransaction resource test" );
        }
        
        /**
         * Main test method - TC24
         *  - create a transaction
         *  - create a sub transaction
         *  - register a resource as a subtransaction aware
         *  - commit the sub transaction
         *  - commit the top level transaction  
         */           
        public void runTest()
        {                       
            try
            {
                stream.writeVerbose("Begin the test");
                                            
                stream.writeVerbose("Get the current object");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );                               
                
                stream.writeVerbose("Begin a transaction");
                current.begin();
                
                stream.writeVerbose("Begin a sub transaction");
                current.begin();
                
                stream.writeVerbose("Register a resource as a subtransaction aware resource");
                
                subResourceImpl r = new subResourceImpl();                
            
                try
                {
                    org.omg.CosTransactions.SubtransactionAwareResource res = org.omg.CosTransactions.SubtransactionAwareResourceHelper.narrow( getObject( r ) );
                    
                    current.get_control().get_coordinator().register_subtran_aware( res );                    
                }                
                catch ( java.lang.Exception e )
                {
                    e.printStackTrace();
                    fail( e.getMessage() );
                }                  
                                
                stream.writeVerbose("Commit the sub transaction");
                current.commit( true );
                
                stream.writeVerbose("Commit the top level transaction");
                current.commit( true );
                
                assert ( r.commit_sub );
                   
                return;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                fail( ex.getMessage() );
            }
        }
    }
    
    /**
     * Resource implementation class used for testing
     */ 
    public static class resourceImpl extends org.omg.CosTransactions.ResourcePOA
    {
        public boolean one_phase;
        public boolean prepare;
        public boolean commit;
        public boolean rollback;
        public boolean forget;
        public boolean readonly_vote;
        public boolean rollback_vote;
        
        public resourceImpl()
        {
            one_phase = false;
            prepare = false;
            commit = false;
            rollback = false;
            forget = false;
            readonly_vote = false;
            rollback_vote = false;
        }
        
        public org.omg.CosTransactions.Vote prepare()
		throws org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
        {
            one_phase = false;
            prepare = true;
            
            if ( readonly_vote )
                return org.omg.CosTransactions.Vote.VoteReadOnly;
            
            if ( rollback_vote )
                return org.omg.CosTransactions.Vote.VoteRollback;
            
            return org.omg.CosTransactions.Vote.VoteCommit;
        }
	
	public void rollback()
		throws org.omg.CosTransactions.HeuristicCommit, org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
        {
            rollback = true;
        }       

	public void commit()
		throws org.omg.CosTransactions.NotPrepared, org.omg.CosTransactions.HeuristicRollback, org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
        {
            commit = true;
        }

	public void commit_one_phase()
		throws org.omg.CosTransactions.HeuristicHazard
        {
            if ( !prepare )
                one_phase = true;
        }

	public void forget()
        {
            forget = true;
        }
    }
    
    /**
     * Sub-resource implementation class used for testing
     */ 
    public static class subResourceImpl extends org.omg.CosTransactions.SubtransactionAwareResourcePOA
    {
        public boolean one_phase;
        public boolean prepare;
        public boolean commit;
        public boolean rollback;
        public boolean forget;
        public boolean readonly_vote;
        public boolean rollback_vote;
        public boolean commit_sub;
        public boolean rollback_sub;
        
        public subResourceImpl()
        {
            one_phase = false;
            prepare = false;
            commit = false;
            rollback = false;
            forget = false;
            readonly_vote = false;
            rollback_vote = false;
            commit_sub = false;
            rollback_sub = false;
        }
        
        public org.omg.CosTransactions.Vote prepare()
		throws org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
        {
            one_phase = false;
            prepare = true;
            
            if ( readonly_vote )
                return org.omg.CosTransactions.Vote.VoteReadOnly;
            
            if ( rollback_vote )
                return org.omg.CosTransactions.Vote.VoteRollback;
            
            return org.omg.CosTransactions.Vote.VoteCommit;
        }
	
	public void rollback()
		throws org.omg.CosTransactions.HeuristicCommit, org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
        {
            rollback = true;
        }       

	public void commit()
		throws org.omg.CosTransactions.NotPrepared, org.omg.CosTransactions.HeuristicRollback, org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
        {
            commit = true;
        }

	public void commit_one_phase()
		throws org.omg.CosTransactions.HeuristicHazard
        {
            if ( !prepare )
                one_phase = true;
        }

	public void forget()
        {
            forget = true;
        }
        
        public void commit_subtransaction( org.omg.CosTransactions.Coordinator parent )
        {
            commit_sub = true;
        }
        
        public void rollback_subtransaction()
        {
            rollback_sub = true;
        }
    }
    
    /**
     * Synchonization implementation class used for testing
     */ 
    public static class synchroImpl extends org.omg.CosTransactions.SynchronizationPOA
    {
        private int count;
        
        public synchroImpl()
        {
            count = 0;
        }
        
        public boolean isInvoked()
        {
            if ( count == 2 )
                return true;
            
            return false;
        }
        
        public void before_completion()
        {
            count++;
        }
	
	public void after_completion(org.omg.CosTransactions.Status status)
        {
            count++;
        }
    }
    
    /**
     * Implementation class used for testing
     */ 
    public static class bankImpl extends otstests.bankPOA
    {
        private float _balance;
        
        private org.omg.CORBA.ORB _orb;
        
        public bankImpl( org.omg.CORBA.ORB orb )
        {
            _balance = 0;
            _orb = orb;
        }
        
        public float balance()
        {
            return _balance;
        }
        
        public void credit( float amount )
        {
            _balance += amount;
        }
        
        public void debit( float amount )
        {
            if ( _balance - amount < 0 ) 
            {
                try
                {
                    org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );
                    
                    current.rollback_only();
                }
                catch ( Exception ex )
                {
                    ex.printStackTrace();
                    throw new org.omg.CORBA.INTERNAL();
                }
            }
            else
                _balance -= amount;
        }
    }
}