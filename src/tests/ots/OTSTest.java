package ots;

import tests.*;
import org.omg.CosTransactions.*;
import junit.framework.*;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyManager;
import org.omg.CORBA.SetOverrideType;
import org.omg.PortableServer.POA;
import java.util.Properties;
import org.omg.CosNaming.NamingContextExt;
import org.openorb.util.MapNamingContext;
import tyrex.corba.OTSServer;

public class OTSTest extends TestCase
{
    public static VerboseStream stream = new VerboseStream();  
    
    private static long count = 0;
    
    public OTSTest(String name)
    {
        super( name );
        stream.writeVerbose("");
        stream.writeVerbose("Starting test: " + name);        
    }
    
  /**
   * This method is called prior to calling run and basically starts
   * up a server and client orb, and spawns a thread for the server orb
   * to run with.
   */
    protected void setUp()
    {
        if ( count == 0 )
        {
        String [] args = new String[0];
        String nsStringRef = null;
        
        {
            Properties props = new Properties();
            props.put("ImportModule.ForwardAdapter", "${openorb.home}config/default.xml#ForwardAdapter");
            _svcORB = org.omg.CORBA.ORB.init(args, props);
            
            MapNamingContext map = new MapNamingContext(_svcORB, null);
            NamingContextExt root_ns = map.getRootCtxt();
            nsStringRef = map.bindCorbaloc();
                        
            TransactionFactory ots = OTSServer.createTransactionFactory(_svcORB);
            map.put("Tyrex/TransactionFactory", ots);
            
            Thread.currentThread().interrupt();
            _svcORB.run();
            Thread.interrupted();
        }
        
        Properties props = new Properties();
        props.setProperty("ImportModule.tyrex", "tyrex");
        props.setProperty("InitRef.NameService", nsStringRef );
        
        _orb = org.omg.CORBA.ORB.init(args, props);
        
        try {
            POA rootPOA = (POA)_orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();
            
            Policy force = _orb.create_policy(org.openorb.policy.FORCE_MARSHAL_POLICY_ID.value, _orb.create_any());
            
            PolicyManager opm = (PolicyManager)_orb.resolve_initial_references("ORBPolicyManager");
            opm.set_policy_overrides(new Policy [] {force}, SetOverrideType.ADD_OVERRIDE);
        }
        catch(org.omg.CORBA.UserException ex) {
            fail(ex.toString());
        }
        
        Thread.currentThread().interrupt();
        _orb.run();
        Thread.interrupted();
        }
        count++;
    }
    
    
    /**
     * This method is called after calling run. It shuts down the service and
     * real orbs.
     */
    protected void tearDown()
    {
        if ( count == 24 )
        {
    	   OTSServer.shutdownTransactionManager();	
        _orb.shutdown(true);
        _svcORB.shutdown(true);
        }
    }
    
    private static org.omg.CORBA.ORB _orb;
    private static org.omg.CORBA.ORB _svcORB;
    
    /**
     * TC 01 : Basic transaction test
     *    - check status is StatusNoTransaction
     *    - begin the transaction
     *    - check its status is StatusActive
     *    - commit the transaction
     *    - check its status is StatusNoTransaction
     */
    public void testBasicTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Get the current object'");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        Status status = current.get_status();

        assert( status.value() == Status._StatusNoTransaction );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        status = current.get_status();

        //assert("Transaction not active!", status.value() == Status._StatusActive );

        stream.writeVerbose("Transaction name " + current.get_transaction_name() );

        stream.writeVerbose("Commit the transaction");
        current.commit( false );

        status = current.get_status();

        assert ( status.value() == Status._StatusNoTransaction );
    }

    /**
     * TC 02 : Commit transaction test
     *    - try to commit a transaction
     *    - catch the expected exception NoTransaction
     */
    public void testIsTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Get the current object'");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Commit the transaction");
        try
        {
            current.commit( false );
        }
        catch ( NoTransaction e )
        {
            return;
        }

        fail("Transaction not commited");
    }

    /**
     * TC 03 : Commit transaction test
     *    - begin the transaction
     *    - do some operations (credit/debt)
     *    - check the results
     *    - commit the transaction
     */
    public void testCommitTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the basic test'");

        otstests.Bank bobj = (new BankImpl())._this(_orb);;

        stream.writeVerbose("Get the current object'");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        bobj.credit( 1000 );

        bobj.debit( 500 );

        assert ( "Balance is not equal to 500", bobj.balance() == 500 );

        stream.writeVerbose("Commit the transaction");
        current.commit( false );
    }

    /**
     * TC 04 : Rollback transaction test
     *    - begin the transaction
     *    - do some operations (credit/debt)
     *    - check the results
     *    - rollback the transaction
     */
    public void testRollbackTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the basic test'");

        otstests.Bank bobj = (new BankImpl())._this(_orb);;

        stream.writeVerbose("Get the current object'");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        bobj.credit( 1000 );

        bobj.debit( 500 );

        assert ( "Balance is not equal to 500", bobj.balance() == 500 );

        stream.writeVerbose("rollback the transaction");
        current.rollback();
    }

    /**
     * TC 05 : Rollback only transaction test
     *    - begin the transaction
     *    - do some operations (credit/debt)
     *    - try to commit
     *    - transaction should have been rollbacked
     */
    public void testRollbackOnlyTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the basic test'");

        otstests.Bank bobj = (new BankImpl())._this(_orb);;

        stream.writeVerbose("Get the current object'");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

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

    /**
     * TC 06 : Synchronization transaction test
     *    - begin the transaction
     *    - register a synchronization object
     *    - do some operations (credit/debt)
     *    - check the results
     *    - try to commit
     *    - synchronization should have been invoked
     */
    public void testSynchronizationTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the basic test'");

        otstests.Bank bobj = (new BankImpl())._this(_orb);;

        stream.writeVerbose("Get the current object");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        stream.writeVerbose("Register the synchronization object");

        SynchroImpl synchro = new SynchroImpl();
        Synchronization sync = synchro._this(_orb);

        current.get_control().get_coordinator().register_synchronization( sync );

        bobj.credit( 1000 );

        bobj.debit( 500 );

        assert ( "Balance is not equal to 500", bobj.balance() == 500 );

        stream.writeVerbose("commit the transaction");
        current.commit(false );

        stream.writeVerbose("check if the synchronization was used");

        assert( "Synchronization not invoked", synchro.isInvoked() );
    }

    /**
     * TC 07 : Commit one phase transaction test
     *    - begin the transaction
     *    - register a resource object
     *    - do some operations (credit/debt)
     *    - check the results
     *    - try to commit using 1PC
     *    - check 1PC was used
     */
    public void testCommitOnePhaseTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        ResourceImpl r = new ResourceImpl();

        otstests.Bank bobj = (new BankImpl())._this(_orb);;

        stream.writeVerbose("Get the current object");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        stream.writeVerbose("Register the resource object");

        Resource res = r._this(_orb);

        current.get_control().get_coordinator().register_resource( res );

        bobj.credit( 1000 );

        bobj.debit( 500 );

        assert ( "Balance is not equal to 500", bobj.balance() == 500 );

        stream.writeVerbose("commit the transaction");
        current.commit( false );

        stream.writeVerbose("check if the one phase commit was used");

        assert ( "One phase commit not used", r.one_phase );
    }

    /**
     * TC 08 : Commit resources transaction test
     *    - begin the transaction
     *    - register 2 resource objects
     *    - do some operations (credit/debt)
     *    - check the results
     *    - check the resources were commited
     */
    public void testCommitResourcesTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        ResourceImpl r1 = new ResourceImpl();

        ResourceImpl r2 = new ResourceImpl();

        otstests.Bank bobj = (new BankImpl())._this(_orb);;

        stream.writeVerbose("Get the current object");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        stream.writeVerbose("Register the resource objects");

        Resource res = r1._this(_orb);

        current.get_control().get_coordinator().register_resource( res );

        res = r2._this(_orb);

        current.get_control().get_coordinator().register_resource( res );

        bobj.credit( 1000 );

        bobj.debit( 500 );

        assert ( "Balance is not equal to 500", bobj.balance() == 500 );

        stream.writeVerbose("commit the transaction");
        current.commit( false );

        stream.writeVerbose("check if the resources have been commited");

        assert ( "Resources haven't been commited", ( r1.commit && r2.commit ) );

    }

    /**
     * TC 09 : Commit one phase transaction test
     *    - begin the transaction
     *    - register 2 resource objects
     *    - do some operations (credit/debt)
     *    - check the results
     *    - check the resources were rollbacked
     */
    public void testRollbackResourcesTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        ResourceImpl r1 = new ResourceImpl();

        ResourceImpl r2 = new ResourceImpl();

        otstests.Bank bobj = (new BankImpl())._this(_orb);;

        stream.writeVerbose("Get the current object");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        stream.writeVerbose("Register the resource objects");

        Resource res = r1._this(_orb);

        current.get_control().get_coordinator().register_resource( res );

        res = r2._this(_orb);

        current.get_control().get_coordinator().register_resource( res );

        bobj.debit( 500 );

        stream.writeVerbose("commit the transaction");
        try
        {
            current.commit( true );
        }
        catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK e )
        {
            stream.writeVerbose("check if the resources have been rolledback");

            assert ( "Resources haven't been rollbacked", ( r1.rollback && r2.rollback)    );

            return;
        }

        fail( "Error: resources weren't rollbacked" );
    }

    /**
     * TC 10 : Read only transaction test
     *    - begin the transaction
     *    - register a resource object
     *    - do some operations (credit/debt)
     *    - check the results
     *    - commit the transaction
     *    - check the resource was commited
     */
    public void testReadOnlyTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        ResourceImpl r1 = new ResourceImpl();
        r1.readonly_vote = true;

        ResourceImpl r2 = new ResourceImpl();

        otstests.Bank bobj = (new BankImpl())._this(_orb);;

        stream.writeVerbose("Get the current object");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        stream.writeVerbose("Register the resource objects");

        Resource res = r1._this(_orb);

        current.get_control().get_coordinator().register_resource( res );

        res = r2._this(_orb);

        current.get_control().get_coordinator().register_resource( res );

        bobj.credit( 500 );

        stream.writeVerbose("commit the transaction");

        current.commit( false );

        assert( "Resource wasn't commited",    !r1.commit );

        return;
    }


    /**
     * TC 11 : Rollack vote test
     *    - begin the transaction
     *    - register a resource object
     *    - do some operations (credit/debt)
     *    - check the results
     *    - vote rollback the transaction
     *    - check the resource was rollbacked
     */
    public void testRollbackVoteTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        ResourceImpl r1 = new ResourceImpl();
        r1.rollback_vote = true;

        ResourceImpl r2 = new ResourceImpl();

        otstests.Bank bobj = (new BankImpl())._this(_orb);;

        stream.writeVerbose("Get the current object");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        stream.writeVerbose("Register the resource objects");

        Resource res = r1._this(_orb);

        current.get_control().get_coordinator().register_resource( res );

        res = r2._this(_orb);

        current.get_control().get_coordinator().register_resource( res );

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

    /**
     * TC 12 : Complex rollack vote test
     *    - begin the transaction
     *    - register 2 resource objects
     *    - do some operations (credit/debt)
     *    - check the results
     *    - vote rollback the transaction
     *    - check the resources were rollbacked
     */
    public void testComplexRollbackVoteTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        ResourceImpl r1 = new ResourceImpl();

        ResourceImpl r2 = new ResourceImpl();
        r2.rollback_vote = true;

        otstests.Bank bobj = (new BankImpl())._this(_orb);;

        stream.writeVerbose("Get the current object");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        stream.writeVerbose("Register the resource objects");

        Resource res = r1._this(_orb);

        current.get_control().get_coordinator().register_resource( res );

        res = r2._this(_orb);

        current.get_control().get_coordinator().register_resource( res );

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

    /**
     * TC 13 : Suspend test
     *    - begin the transaction
     *    - suspend the transaction
     *    - begin a new transaction
     *    - resume the transaction
     *    - check the results
     *    - vote rollback the transaction
     *    - check the resources were rollbacked
     */
    public void testSuspendTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        stream.writeVerbose("Get the current object");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        stream.writeVerbose("Suspend the first transaction...");

        Control ctrl = current.suspend();

        stream.writeVerbose("Begin a new transaction");

        current.begin();

        stream.writeVerbose("commit the transaction");

        current.commit( false );

        stream.writeVerbose("Resume the first transaction");

        current.resume( ctrl );

        stream.writeVerbose("commit the first transaction");

        current.commit( false );

    }

    /**
     * TC 14 : Complex suspend test
     *    - begin the transaction
     *    - suspend the transaction
     *    - begin a new transaction
     *    - resume the transaction
     *    - check the results
     *    - vote rollback the transaction
     *    - check the resources were rollbacked
     */
    public void testComplexSuspendTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        stream.writeVerbose("Get the current object");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        stream.writeVerbose("Suspend the first transaction...");

        Control ctrl1 = current.suspend();

        stream.writeVerbose("Begin a new transaction");

        current.begin();

        stream.writeVerbose("Mark the second transaction as rollback only...");

        current.rollback_only();

        stream.writeVerbose("Suspend the second transaction...");

        Control ctrl2 = current.suspend();

        stream.writeVerbose("Resume the first transaction");

        current.resume( ctrl1 );

        stream.writeVerbose("commit the first transaction");

        current.commit( false );

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

    /**
     * TC 15 : TimeOut test
     *    - begin the transaction
     *    - set timeout to 5 secs
     *    - wait 15 secs
     *    - check the transaction was rollbacked
     */
    public void testTimeOutTransaction() throws org.omg.CORBA.UserException, InterruptedException {
        stream.writeVerbose("Begin the test");

        stream.writeVerbose("Get the current object");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Set the time out to 5 seconds...");
        current.set_timeout( 5 );

        stream.writeVerbose("Begin the transaction");
        current.begin();

        stream.writeVerbose("Wait for 15 seconds...");

        Thread.currentThread().sleep( 15000 );

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
    
    /**
     * TC 16 : Explicit transaction management
     *    - begin the transaction
     *    - set timeout to 5 secs
     *    - wait 15 secs
     *    - check the transaction was rollbacked
     */
    public void testExplicitTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");
        
        ResourceImpl r1 = new ResourceImpl();
        
        ResourceImpl r2 = new ResourceImpl();
        
        otstests.Bank bobj = (new BankImpl())._this(_orb);;
        
        stream.writeVerbose("Get the transaction factory");
        
        org.omg.CORBA.Object obj = _orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");
        
        TransactionFactory factory = TransactionFactoryHelper.narrow( obj );
        
        stream.writeVerbose("Create a new transaction");
        
        Control ctrl = factory.create( 0 );
        
        stream.writeVerbose("Register the resource objects");
        
        Resource res = r1._this(_orb);
        
        ctrl.get_coordinator().register_resource( res );
        
        res = r2._this(_orb);
        
        ctrl.get_coordinator().register_resource( res );
        
        bobj.credit( 500 );
        
        stream.writeVerbose("Commit the transaction");
        ctrl.get_terminator().commit( false );
        
        return;
    }
    
    /**
     * TC 17 : Propagation context test
     *    - begin the transaction
     *    - get propagation context
     *    - check the propagation context
     */
    public void testPropagationContextTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        stream.writeVerbose("Get the transaction factory");

        org.omg.CORBA.Object obj = _orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");

        TransactionFactory factory = TransactionFactoryHelper.narrow( obj );

        stream.writeVerbose("Create a new transaction");

        Control ctrl = factory.create( 10 );

        stream.writeVerbose("Get the propagation context");

        PropagationContext pctx = ctrl.get_coordinator().get_txcontext();

        assert ( pctx.current.coord.equals( ctrl.get_coordinator() ) );

        assert ( pctx.current.term.equals( ctrl.get_terminator() ) );

        assert ( pctx.timeout == 10 );

        assert ( pctx.parents.length == 0 );

        stream.writeVerbose("Commit the transaction");
        ctrl.get_terminator().commit( false );

        return;
    }
    
    /**
     * TC 18 : Subordinate test
     *    - create a transaction
     *    - get propagation context
     *    - recreate a transaction
     *    - register a synchronization object
     *    - commit the transaction
     *    - check the synchronization object was invoked
     */
    public void testSubordinateTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");
        
        stream.writeVerbose("Get the transaction factory");
        
        org.omg.CORBA.Object obj = _orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");
        
        TransactionFactory factory = TransactionFactoryHelper.narrow( obj );
        
        stream.writeVerbose("Create a new transaction");
        
        Control ctrl = factory.create( 10 );
        
        stream.writeVerbose("Get the propagation context");
        
        PropagationContext pctx = ctrl.get_coordinator().get_txcontext();
        
        stream.writeVerbose("Re-create a transaction");
        
        Control ctrl2 = factory.recreate( pctx );
        
        stream.writeVerbose("Register the synchronization object");
        
        SynchroImpl synchro = new SynchroImpl();
        Synchronization sync = synchro._this(_orb);
        
        ctrl2.get_coordinator().register_synchronization( sync );
        
        stream.writeVerbose("Commit the transaction");
        ctrl.get_terminator().commit( false );
        
        assert ( "Synchronization not invoked", synchro.isInvoked() );
        
        return;
    }
    
    /**
     * TC 19 : Subordinate Rollback test
     *    - create a transaction
     *    - get propagation context
     *    - recreate a transaction
     *    - register a synchronization object
     *    - rollback the transaction
     *    - check the transaction was rollbacked
     */
    public void testSubordinateRollbackTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");
        
        stream.writeVerbose("Get the transaction factory");
        
        org.omg.CORBA.Object obj = _orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");
        
        TransactionFactory factory = TransactionFactoryHelper.narrow( obj );
        
        stream.writeVerbose("Create a new transaction");
        
        Control ctrl = factory.create( 10 );
        
        stream.writeVerbose("Get the propagation context");
        
        PropagationContext pctx = ctrl.get_coordinator().get_txcontext();
        
        stream.writeVerbose("Re-create a transaction");
        
        Control ctrl2 = factory.recreate( pctx );
        
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
        catch ( org.omg.CosTransactions.HeuristicMixed e )
        {
            return;
        }
        
        fail( "Error" );
    }

    /**
     * TC 20 : Basic multiple transactions test
     *    - create a transaction
     *    - create a transaction
     *    - compares the 2 transactions
     */
    public void testBasicMultipleTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        stream.writeVerbose("Get the transaction factory");

        org.omg.CORBA.Object obj = _orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");

        TransactionFactory factory = TransactionFactoryHelper.narrow( obj );

        stream.writeVerbose("Create a new transaction");

        Control ctrl = factory.create( 0 );

        stream.writeVerbose("Create a new transaction");

        Control ctrl2 = factory.create( 0 );

        stream.writeVerbose("Compare transactions");

        Coordinator coord1 = ctrl.get_coordinator();

        Coordinator coord2 = ctrl2.get_coordinator();

        assert ( coord1.is_same_transaction( coord1 ) );

        assert ( !coord1.is_same_transaction( coord2 ) );

        assert ( !coord1.is_related_transaction( coord2 ) );

        ctrl.get_terminator().commit( false );

        ctrl2.get_terminator().commit( false );

        return;
    }

    /**
     * TC 21 : Multiple transactions test
     *    - create a transaction
     *    - create a transaction
     *    - compares the 2 transactions
     */
    public void testMultipleTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        stream.writeVerbose("Get the transaction factory");

        org.omg.CORBA.Object obj = _orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");

        TransactionFactory factory = TransactionFactoryHelper.narrow( obj );

        stream.writeVerbose("Create a new transaction");

        Control ctrl = factory.create( 0 );

        stream.writeVerbose("Create a new transaction");

        Control ctrl2 = factory.create( 0 );

        stream.writeVerbose("Compare transactions");

        ctrl.get_coordinator().rollback_only();

        ctrl2.get_terminator().commit( false );

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

    /**
     * TC 22 : Basic subtransaction test
     *    - begin a transaction
     *    - begin a sub transaction
     *    - commit the sub transaction
     *    - commit the top level transaction
     */
    public void testBasicSubTransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        stream.writeVerbose("Get the current object");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin a transaction");
        current.begin();

        stream.writeVerbose("Begin a sub transaction");
        current.begin();

        stream.writeVerbose("Commit the sub transaction");
        current.commit( false );

        stream.writeVerbose("Commit the top level transaction");
        current.commit( false );

        return;
    }
    
    /**
     * TC 23 : Subtransaction test
     *    - create a transaction
     *    - create a sub transaction
     *    - compare the transactions
     *    - commit the sub transaction
     *    - commit the top level transaction
     */
    public void testSubtransaction() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");
        
        stream.writeVerbose("Get the transaction factory");
        
        org.omg.CORBA.Object obj = _orb.string_to_object("corbaname:rir:#Tyrex/TransactionFactory");
        
        TransactionFactory factory = TransactionFactoryHelper.narrow( obj );
        
        stream.writeVerbose("Create a new transaction");
        
        Control ctrl = factory.create( 0 );
        
        stream.writeVerbose("Create a sub transaction");
        
        Control sub = ctrl.get_coordinator().create_subtransaction();
        
        stream.writeVerbose("Compare the transaction");
        
        Coordinator coord1 = ctrl.get_coordinator();
        
        Coordinator coord2 = sub.get_coordinator();
        
        assert ( !coord1.is_same_transaction( coord2 ) );
        
        assert ( !coord1.is_related_transaction( coord2 ) );
        
        assert ( coord2.is_descendant_transaction( coord1 ) );
        
        assert ( coord1.is_ancestor_transaction( coord2 ) );
        
        sub.get_terminator().commit( false );
        
        ctrl.get_terminator().commit( false );
        
        return;
    }
    
    /**
     * TC 24 : Subtransaction Resource test
     *    - create a transaction
     *    - create a sub transaction
     *    - register a resource as a subtransaction aware
     *    - commit the sub transaction
     *    - commit the top level transaction
     */
    public void testSubtransactionResource() throws org.omg.CORBA.UserException {
        stream.writeVerbose("Begin the test");

        stream.writeVerbose("Get the current object");
        Current current = CurrentHelper.narrow( _orb.resolve_initial_references("TransactionCurrent") );

        stream.writeVerbose("Begin a transaction");
        current.begin();

        stream.writeVerbose("Begin a sub transaction");
        current.begin();

        stream.writeVerbose("Register a resource as a subtransaction aware resource");

        SubResourceImpl r = new SubResourceImpl();

        SubtransactionAwareResource res = r._this(_orb);

        current.get_control().get_coordinator().register_subtran_aware( res );

        stream.writeVerbose("Commit the sub transaction");
        current.commit( false );

        stream.writeVerbose("Commit the top level transaction");
        current.commit( false );

        assert ( r.commit_sub );
    }
    
    /**
     * Resource implementation class used for testing
     */
    public class ResourceImpl extends ResourcePOA
    {
        public boolean one_phase;
        public boolean prepare;
        public boolean commit;
        public boolean rollback;
        public boolean forget;
        public boolean readonly_vote;
        public boolean rollback_vote;
        
        public ResourceImpl()
        {
            one_phase = false;
            prepare = false;
            commit = false;
            rollback = false;
            forget = false;
            readonly_vote = false;
            rollback_vote = false;
        }
        
        public Vote prepare()
        throws HeuristicMixed, HeuristicHazard
        {
            one_phase = false;
            prepare = true;
            
            if ( readonly_vote )
                return Vote.VoteReadOnly;
            
            if ( rollback_vote )
                return Vote.VoteRollback;
            
            return Vote.VoteCommit;
        }
        
        public void rollback()
        throws HeuristicCommit, HeuristicMixed, HeuristicHazard
        {
            rollback = true;
        }
        
        public void commit()
        throws NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
        {
            commit = true;
        }
        
        public void commit_one_phase()
        throws HeuristicHazard
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
    public class SubResourceImpl extends SubtransactionAwareResourcePOA
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
        
        public SubResourceImpl()
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
        
        public Vote prepare()
        throws HeuristicMixed, HeuristicHazard
        {
            one_phase = false;
            prepare = true;
            
            if ( readonly_vote )
                return Vote.VoteReadOnly;
            
            if ( rollback_vote )
                return Vote.VoteRollback;
            
            return Vote.VoteCommit;
        }
        
        public void rollback()
        throws HeuristicCommit, HeuristicMixed, HeuristicHazard
        {
            rollback = true;
        }
        
        public void commit()
        throws NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
        {
            commit = true;
        }
        
        public void commit_one_phase()
        throws HeuristicHazard
        {
            if ( !prepare )
                one_phase = true;
        }
        
        public void forget() {
            forget = true;
        }
        
        public void commit_subtransaction( Coordinator parent ) {
            commit_sub = true;
        }
        
        public void rollback_subtransaction() {
            rollback_sub = true;
        }
    }
        /**
         * Synchonization implementation class used for testing
         */
    public class SynchroImpl extends SynchronizationPOA
    {
        private int count;
        
        public SynchroImpl() {
            count = 0;
        }
        
        public boolean isInvoked() {
            if ( count == 2 )
                return true;
            
            return false;
        }
        
        public void before_completion() {
            count++;
        }
        
        public void after_completion(Status status) {
            count++;
        }
    }
    
        /**
         * Implementation class used for testing
         */
    public  class BankImpl extends otstests.BankPOA
    {
        private float _balance;
        
        private org.omg.CORBA.ORB _orb;
        
        public BankImpl() {
            _balance = 0;
        }
        
        public float balance() {
            return _balance;
        }
        
        public void credit( float amount ) {
            _balance += amount;
        }
        
        public void debit( float amount )	{
            if ( _balance - amount < 0 )
            {
                try
                {
                    Current current = CurrentHelper.narrow( _orb().resolve_initial_references("TransactionCurrent") );
                    
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
    
    static public void main(String args[])
    {
        junit.textui.TestRunner.run(new TestSuite(OTSTest.class));
    }
}
