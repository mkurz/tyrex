package tests.ots;

import org.exolab.jtf.CWTestCategory;
import org.exolab.jtf.CWTestCase;
import org.exolab.jtf.CWVerboseStream;
import org.exolab.exceptions.CWClassConstructorException;

public class OTS extends CWTestCategory
{
    public OTS()
        throws CWClassConstructorException
    {
        super( "ots", "OTS tests");
        
        CWTestCase tc;
        
        tc = new BasicTransactionTest();
        add( tc.name(), tc, true ); 
        tc = new IsTransactionTest();
        add( tc.name(), tc, true ); 
        tc = new CommitTransactionTest();
        add( tc.name(), tc, true ); 
        tc = new RollbackTransactionTest();
        add( tc.name(), tc, true );
        tc = new RollbackOnlyTransactionTest();
        add( tc.name(), tc, true );        
        tc = new SynchronizationTransactionTest();
        add( tc.name(), tc, true );
        tc = new CommitOnePhaseTransactionTest();
        add( tc.name(), tc, true );
        tc = new CommitResourcesTransactionTest();
        add( tc.name(), tc, true );
        tc = new RollbackResourcesTransactionTest();
        add( tc.name(), tc, true );
        tc = new ReadOnlyTransactionTest();
        add( tc.name(), tc, true );
        tc = new RollbackVoteTransactionTest();
        add( tc.name(), tc, true );
        tc = new ComplexRollbackVoteTransactionTest();
        add( tc.name(), tc, true );
        tc = new SuspendTransactionTest();
        add( tc.name(), tc, true );
        tc = new ComplexSuspendTransactionTest();
        add( tc.name(), tc, true );
        tc = new TimeOutTransactionTest();
        add( tc.name(), tc, true );
        tc = new ExplicitTransactionTest();
        add( tc.name(), tc, true );
        tc = new PropagationContextTransactionTest();
        add( tc.name(), tc, true );
        //tc = new SubordinateTransactionTest();
        //add( tc.name(), tc, true );
        //tc = new SubordinateRollbackTransactionTest();
        //add( tc.name(), tc, true );
        tc = new BasicMultipleTransactionTest();
        add( tc.name(), tc, true );
        tc = new MultipleTransactionTest();
        add( tc.name(), tc, true );
        tc = new BasicSubTransactionTest();
        add( tc.name(), tc, true );
        //tc = new SubtransactionTest();
        //add( tc.name(), tc, true );
        tc = new SubtransactionResourceTest();
        add( tc.name(), tc, true );
    }
    
    /**
     * TC 01 : Basic transaction test
     */
    public static class BasicTransactionTest extends OTSTestCase
    {        
        public BasicTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC01", "Basic transaction test" );
        }
                
        public boolean run( CWVerboseStream stream )
        {                       
            try
            {                                
                stream.writeVerbose("Get the current object'");
                org.omg.CosTransactions.Current current = org.omg.CosTransactions.CurrentHelper.narrow( _client_orb.resolve_initial_references("TransactionCurrent") );
                                
                org.omg.CosTransactions.Status status = current.get_status();
         
                if ( status.value() != org.omg.CosTransactions.Status._StatusNoTransaction )
                    return false;
                
                stream.writeVerbose("Begin the transaction");
                current.begin();
                
                status = current.get_status();

                if ( status.value() != org.omg.CosTransactions.Status._StatusActive )
                    return false;
                
                stream.writeVerbose("Transaction name " + current.get_transaction_name() );
                
                stream.writeVerbose("Commit the transaction");
                current.commit( true );    
                
                status = current.get_status();
                
                if ( status.value() != org.omg.CosTransactions.Status._StatusNoTransaction )
                    return false;
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }

    /**
     * TC 02 : Commit transaction test
     */
    public static class IsTransactionTest extends OTSTestCase
    {        
        public IsTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC02", "Is transaction test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return true;
                }
                
                return false;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 03 : Commit transaction test
     */
    public static class CommitTransactionTest extends OTSTestCase
    {        
        public CommitTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC03", "Simple commit test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                
                if ( bobj.balance() != 500 ) 
                    return false;
                
                stream.writeVerbose("Commit the transaction");
                current.commit( true );    
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }

    /**
     * TC 04 : Rollback transaction test
     */
    public static class RollbackTransactionTest extends OTSTestCase
    {        
        public RollbackTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC04", "Simple rollback test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                
                if ( bobj.balance() != 500 ) 
                    return false;
                
                stream.writeVerbose("rollback the transaction");
                current.rollback();    
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 05 : Rollback only transaction test
     */
    public static class RollbackOnlyTransactionTest extends OTSTestCase
    {        
        public RollbackOnlyTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC05", "Simple rollback only test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return true;
                }
                
                return false;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }

    /**
     * TC 06 : Synchronization transaction test
     */    
    public static class SynchronizationTransactionTest extends OTSTestCase
    {        
        public SynchronizationTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC06", "Simple synchronization test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return false;
                }                                
                
                bobj.credit( 1000 );
                
                bobj.debit( 500 );
                
                if ( bobj.balance() != 500 ) 
                    return false;
                
                stream.writeVerbose("commit the transaction");
                current.commit( true );    
                
                stream.writeVerbose("check if the synchronization was used");
                
                if ( !synchro.isInvoked() )
                    return false;
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
 
    /**
     * TC 07 : Commit one phase transaction test
     */    
    public static class CommitOnePhaseTransactionTest extends OTSTestCase
    {        
        public CommitOnePhaseTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC07", "Commit one phase test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return false;
                }                                
                
                bobj.credit( 1000 );
                
                bobj.debit( 500 );
                
                if ( bobj.balance() != 500 ) 
                    return false;
                
                stream.writeVerbose("commit the transaction");
                current.commit( true );    
                
                stream.writeVerbose("check if the one phase commit was used");
                
                if ( !r.one_phase )
                    return false;
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 08 : Commit resources transaction test
     */    
    public static class CommitResourcesTransactionTest extends OTSTestCase
    {        
        public CommitResourcesTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC08", "Commit resources test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return false;
                }                                
                
                bobj.credit( 1000 );
                
                bobj.debit( 500 );
                
                if ( bobj.balance() != 500 ) 
                    return false;
                
                stream.writeVerbose("commit the transaction");
                current.commit( true );    
                
                stream.writeVerbose("check if the resources have been commited");
                
                if ( !( r1.commit && r2.commit ) )
                    return false;
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 09 : Commit one phase transaction test
     */    
    public static class RollbackResourcesTransactionTest extends OTSTestCase
    {        
        public RollbackResourcesTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC09", "Rollback resources test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return false;
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
                
                    if ( ( r1.rollback && r2.rollback) )
                        return true;
                }
                                                
                return false;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 10 : Read only transaction test
     */    
    public static class ReadOnlyTransactionTest extends OTSTestCase
    {        
        public ReadOnlyTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC10", "Readonly test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return false;
                }                                
                
                bobj.credit( 500 );
                
                stream.writeVerbose("commit the transaction");
                
                current.commit( true );    
                
                if ( r1.commit )
                   return false;               
                                                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 11 : Rollack vote test
     */    
    public static class RollbackVoteTransactionTest extends OTSTestCase
    {        
        public RollbackVoteTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC11", "Rollback vote test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return false;
                }                                
                
                bobj.credit( 500 );
                
                stream.writeVerbose("commit the transaction");
                
                try                
                {
                    current.commit( true );    
                }
                catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK e )
                {
                    if ( !r2.rollback )
                        return false;               
                    
                    return true;
                }                            
                
                return false;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 12 : Complex rollack vote test
     */    
    public static class ComplexRollbackVoteTransactionTest extends OTSTestCase
    {        
        public ComplexRollbackVoteTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC12", "Complex rollback vote test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return false;
                }                                
                
                bobj.credit( 500 );
                
                stream.writeVerbose("commit the transaction");
                
                try                
                {
                    current.commit( true );    
                }
                catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK e )
                {
                    if ( !r1.rollback )
                        return false;               
                    
                    return true;
                }                            
                
                return false;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 13 : Suspend test
     */    
    public static class SuspendTransactionTest extends OTSTestCase
    {        
        public SuspendTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC13", "Simple suspend test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 14 : Complex suspend test
     */    
    public static class ComplexSuspendTransactionTest extends OTSTestCase
    {        
        public ComplexSuspendTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC14", "Complex suspend test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return true;
                }
                
                return false;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 15 : TimeOut test
     */    
    public static class TimeOutTransactionTest extends OTSTestCase
    {        
        public TimeOutTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC15", "TimeOut test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return true;
                }
                
                return false;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }

    /**
     * TC 16 : Explicit transaction management
     */    
    public static class ExplicitTransactionTest extends OTSTestCase
    {        
        public ExplicitTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC16", "Explicit transaction test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return false;
                }                                
                
                bobj.credit( 500 );
                
                stream.writeVerbose("Commit the transaction");
                ctrl.get_terminator().commit( true );
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 17 : Propagation context test
     */    
    public static class PropagationContextTransactionTest extends OTSTestCase
    {        
        public PropagationContextTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC17", "Propagation context test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                
                if ( !pctx.current.coord.equals( ctrl.get_coordinator() ) )
                    return false;
                
                if ( !pctx.current.term.equals( ctrl.get_terminator() ) )
                    return false;
                
                if ( pctx.timeout != 10 )
                    return false;
                
                if ( pctx.parents.length != 0 )
                    return false;
                
                stream.writeVerbose("Commit the transaction");
                ctrl.get_terminator().commit( true );
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 18 : Subordinate test
     */    
    public static class SubordinateTransactionTest extends OTSTestCase
    {        
        public SubordinateTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC18", "Subordinate test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return false;
                }          
                                
                stream.writeVerbose("Commit the transaction");
                ctrl.get_terminator().commit( true );
                
                if ( !synchro.isInvoked() )
                    return false;
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 19 : Subordinate Rollback test
     */    
    public static class SubordinateRollbackTransactionTest extends OTSTestCase
    {        
        public SubordinateRollbackTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC19", "Subordinate rollback test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return true;
                }
                
                return false;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }

    /**
     * TC 20 : Basic multiple transactions test
     */    
    public static class BasicMultipleTransactionTest extends OTSTestCase
    {        
        public BasicMultipleTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC20", "Basic multiple transactions test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                
                if ( !coord1.is_same_transaction( coord1 ) )
                    return false;
                
                if ( coord1.is_same_transaction( coord2 ) )
                    return false;
                
                if ( coord1.is_related_transaction( coord2 ) )
                    return false;
                
                ctrl.get_terminator().commit( true );
                
                ctrl2.get_terminator().commit( true );
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 21 : Multiple transactions test
     */    
    public static class MultipleTransactionTest extends OTSTestCase
    {        
        public MultipleTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC21", "Multiple transactions test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return true;
                }
                
                return false;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 22 : Basic subtransaction test
     */    
    public static class BasicSubTransactionTest extends OTSTestCase
    {        
        public BasicSubTransactionTest()
            throws CWClassConstructorException
        {
            super( "TC22", "Basic subtransaction test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 23 : Subtransaction test
     */    
    public static class SubtransactionTest extends OTSTestCase
    {        
        public SubtransactionTest()
            throws CWClassConstructorException
        {
            super( "TC23", "Subtransaction test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                
                if ( coord1.is_same_transaction( coord2 ) )
                    return false;
                
                if ( coord1.is_related_transaction( coord2 ) )
                    return false;
                
                if ( !coord2.is_descendant_transaction( coord1 ) )
                    return false;
                
                if ( !coord1.is_ancestor_transaction( coord2 ) )
                    return false;

                sub.get_terminator().commit( true );
                
                ctrl.get_terminator().commit( true );
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
    /**
     * TC 24 : Subtransaction Resource test
     */    
    public static class SubtransactionResourceTest extends OTSTestCase
    {        
        public SubtransactionResourceTest()
            throws CWClassConstructorException
        {
            super( "TC24", "Subtransaction resource test" );
        }
                
        public boolean run( CWVerboseStream stream )
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
                    return false;
                }                  
                                
                stream.writeVerbose("Commit the sub transaction");
                current.commit( true );
                
                stream.writeVerbose("Commit the top level transaction");
                current.commit( true );
                
                if ( !r.commit_sub )
                      return false;
                
                return true;
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                
                return false;
            }
        }
    }
    
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