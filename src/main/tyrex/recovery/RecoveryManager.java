/**
 * Redistribution and use of this software and associated
 * documentation ("Software"), with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright statements
 *    and notices.  Redistributions must also contain a copy of this
 *    document.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio Inc.  For written permission, please
 *    contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Inc. Exolab is a registered trademark of
 *    Intalio Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL INTALIO OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * Copyright 2000 (C) Intalio Inc. All Rights Reserved.
 */

package tyrex.recovery;

import tyrex.util.Logger;

/**
 * The recovery manager is run when a failure occured. It reads all log in order
 * to complete all pending transactions.
 * 
 * @author <a href="mailto:jdaniel@intalio.com">Jerome Daniel &lt;daniel@intalio.com&gt;</a>
 * @version $Revision: 1.1 $ $Date: 2001/01/11 23:26:33 $ 
 */
public class RecoveryManager
{
	/**
	 * Reference to the log monitor
	 */
	private tyrex.recovery.LogMonitor _monitor;
        
        /**
         * Are there some XA resources used ?
         */
        private boolean _xa_used = false;
        
        /**
         * List of all XA resources used
         */
        private javax.transaction.xa.XAResource [] _xa;
	
        /**
         * Reference to the recovery manager
         */
        public static RecoveryManager manager;
        
	/**
	 * Constructor
	 */
	public RecoveryManager( String directory, org.omg.CORBA.ORB orb )
	{
		_monitor = new tyrex.recovery.LogMonitor( directory );
                _monitor.setORB( orb );
	}
                
	/**
	 * This operation is used to recover uncompleted transactions.
	 */
	public void recover_transactions()
	{
		print("RecoveryManager","Begin transaction recovery");
		
		tyrex.tm.XidImpl [] xids = _monitor.uncompleted_transactions();
		
		for ( int i=0; i<xids.length; i++ )
		{
			recover_this_transaction( xids[ i ] );
		}
		
		tyrex.recovery.LogWriter.out.recovery();
                
                print("RecoveryManager", "End of recovery : " + xids.length + " transaction(s) recovered.");
	}
	
	/**
	 * This operation is used to recover an uncompleted transaction.
	 */
	private void recover_this_transaction( tyrex.tm.XidImpl xid )
	{
		print("RecoveryManager", "Recover transaction : " + xid.toString() );
		
		org.omg.CosTransactions.Status status = _monitor.transaction_status( xid );
		
		switch ( status.value() )
		{
		case org.omg.CosTransactions.Status._StatusActive :
			{
			print("RecoveryManager", "transaction active");
			// Here, the transaction is begun. We have to rollback all registered resources.
			org.omg.CosTransactions.Resource [] resources = _monitor.registered_ots_resources( xid );			
			rollback_ots_resources( xid, resources );
                        // We also have to rollback all XA resources
                        rollback_xa_resources( xid );
			// Now, we log the transaction completion
			tyrex.recovery.LogWriter.out.rollback_end( xid, 0 );
			tyrex.recovery.LogWriter.out.completed( xid );
			}
			break;
		case org.omg.CosTransactions.Status._StatusPreparing :
			{
			print("RecoveryManager", "transaction is preparing");
			// Here, the transaction is preparing. We have to rollback all prepared resources
			// and all rolledback others ressources.
			org.omg.CosTransactions.Resource [] prepared_resources = _monitor.prepared_ots_resources( xid );
			rollback_ots_resources( xid, prepared_resources );
			org.omg.CosTransactions.Resource [] free_resources = _monitor.free_ots_resources( xid );
			rollback_ots_resources( xid, free_resources );
                        // We also have to rollback all XA resources
                        rollback_xa_resources( xid );
			// Now, we log the transaction completion
			tyrex.recovery.LogWriter.out.rollback_end( xid, 0 );
			tyrex.recovery.LogWriter.out.completed( xid );
			}
			break;
		case org.omg.CosTransactions.Status._StatusPrepared :
			{
				print("RecoveryManager", "transaction is prepared");
				
				org.omg.CosTransactions.Vote vote = _monitor.prepare_vote( xid );
				
				switch ( vote.value() )
				{
				case org.omg.CosTransactions.Vote._VoteCommit :
					{
						print("RecoveryManager", "Commit is voted");
						// Two possibilities !
						// 1. No rollback has been sent to resources. In this case, we continue to commit
						// 2. A rollback has been sent, and we have to continue rollback
						if ( _monitor.is_any_rollback( xid ) )
						{
							print("RecoveryManager", "Must be rolledback");
							// Case 2
							// Here, we have to rollback all prepared resources ( which are not already rolledback
							// and not committed but in this case the prepared_resources does not return them ).
							org.omg.CosTransactions.Resource [] prepared_resources = _monitor.prepared_ots_resources( xid );
							rollback_ots_resources( xid, prepared_resources );
                                                        // then see XA resources
                                                        rollback_xa_resources( xid );
							// Now, we log the transaction completion
							tyrex.recovery.LogWriter.out.rollback_end( xid, 0 );
							tyrex.recovery.LogWriter.out.completed( xid );
						}
						else
						{
							print("RecoveryManager","Continue commit");
							// Case 1
							// Here, we have to rollback all prepared resources ( which are not already committed
							// but in this case the prepared_resources does not return them ).
							org.omg.CosTransactions.Resource [] prepared_resources = _monitor.prepared_ots_resources( xid );
							org.omg.CosTransactions.Resource [] registered_resources = _monitor.registered_ots_resources( xid );
							if ( ( prepared_resources.length == 1 ) && ( registered_resources.length == 1 ) )
								continue_ots_commit_one_phase( xid, prepared_resources[0] );
							else
							continue_ots_commit( xid, prepared_resources );				
                                                        // continue commit on XA resources
                                                        commit_xa_resources( xid );
						}
					}
					break;
				case org.omg.CosTransactions.Vote._VoteReadOnly :
					{
						print("RecoveryManager","ReadOnly is voted");
						// Now, we log the transaction completion
						tyrex.recovery.LogWriter.out.commit_end( xid, 0 );
						tyrex.recovery.LogWriter.out.completed( xid );
					}
					break;
				case org.omg.CosTransactions.Vote._VoteRollback :
					{
						print("RecoveryManager","Rollback is voted");
						// Here, we have to rollback all prepared resources ( which are not already rolledback
						// but in this case the prepared_resources does not return them ).
						org.omg.CosTransactions.Resource [] prepared_resources = _monitor.prepared_ots_resources( xid );
						rollback_ots_resources( xid, prepared_resources );
                                                // then, rollback xa resources
                                                rollback_xa_resources( xid );
						// Now, we log the transaction completion
						tyrex.recovery.LogWriter.out.rollback_end( xid, 0 );
						tyrex.recovery.LogWriter.out.completed( xid );
					}
					break;
				}
			}
			break;
		case org.omg.CosTransactions.Status._StatusCommitting :
			{
			print("RecoveryManager","transaction is committing");
			// Two possibilities !
			// 1. No rollback has been sent to resources. In this case, we continue to commit
			// 2. A rollback has been sent, and we have to continue rollback
			if ( _monitor.is_any_rollback( xid ) )
			{
				print("RecoveryManager","Must be rolledback");
				// Case 2
				// Here, we have to rollback all prepared resources ( which are not already rolledback
				// and not committed but in this case the prepared_resources does not return them ).
				org.omg.CosTransactions.Resource [] prepared_resources = _monitor.prepared_ots_resources( xid );
				rollback_ots_resources( xid, prepared_resources );
                                // Rollback XA resources
                                rollback_xa_resources( xid );
				// Now, we log the transaction completion
				tyrex.recovery.LogWriter.out.rollback_end( xid, 0 );
				tyrex.recovery.LogWriter.out.completed( xid );
			}
			else
			{
				print("RecoveryManager","Continue commit");
				// Case 1
				// Here, we have to rollback all prepared resources ( which are not already committed
				// but in this case the prepared_resources does not return them ).
				org.omg.CosTransactions.Resource [] prepared_resources = _monitor.prepared_ots_resources( xid );
				org.omg.CosTransactions.Resource [] registered_resources = _monitor.registered_ots_resources( xid );
				if ( ( prepared_resources.length == 1 ) && ( registered_resources.length == 1 ) )
					continue_ots_commit_one_phase( xid, prepared_resources[0] );
				else
					continue_ots_commit( xid, prepared_resources );				
                                // Commit XA resources
                                commit_xa_resources( xid );
			}
			}
			break;
		case org.omg.CosTransactions.Status._StatusRollingBack :
			{
			print("RecoveryManager","transaction is rolling back");
			// Here, the transaction rollingback. We have to rollback all resources which are not already
			// rolledback.
			org.omg.CosTransactions.Resource [] free_resources = _monitor.free_ots_resources( xid );
			rollback_ots_resources( xid, free_resources );
                        // Rollback XA resources
                        rollback_xa_resources( xid );
			// Now, we log the transaction completion
			tyrex.recovery.LogWriter.out.rollback_end( xid, 0 );
			tyrex.recovery.LogWriter.out.completed( xid );
			}
			break;			
		default :
			// Nothing to do... really ! 
			// Just log the transaction completion
			print("RecoveryManager","transaction state not expected, just to be completed : " + status.value() );
			tyrex.recovery.LogWriter.out.completed( xid );
			break;
		}				
	}
	
	/**
	 * This operation is used to rollback resources
	 */
	private void rollback_ots_resources( tyrex.tm.XidImpl xid, org.omg.CosTransactions.Resource [] resources )
	{
		for ( int i=0; i<resources.length; i++ )
		{
			try
			{
				resources[i].rollback();
				
				tyrex.recovery.LogWriter.out.rollback_ots_resource( xid, resources[i] );
			}
			catch ( org.omg.CosTransactions.HeuristicCommit ex )
			{
			}
			catch ( org.omg.CosTransactions.HeuristicMixed ex )
			{
			}
			catch ( org.omg.CosTransactions.HeuristicHazard ex )
			{
			}
			catch ( org.omg.CORBA.COMM_FAILURE ex )
			{ 
				// Nothing to do... Normally we have to retry later but we consider that the transaction
				// is rolledback and the resource will have to use the recovery coordinator to known
				// this outcome.
			}
			catch ( org.omg.CORBA.OBJECT_NOT_EXIST ex )
			{ 
				// Nothing to do...
			}
		}		
	}
        
        /**
         * This operation is used to rollback XA resources.
         */
        private void rollback_xa_resources( tyrex.tm.XidImpl xid )
        {
            if ( is_xa_used() )
            {
                javax.transaction.xa.XAResource [] xa = list_pending_xa_resources( xid );
                
                for ( int i=0; i<xa.length; i++ )
                {
                    try
                    {
                        xa[i].rollback( xid );
                    }
                    catch ( javax.transaction.xa.XAException ex )
                    { }
                }
            }
        }
        
        /**
         * This operation is used to commit XA resources
         */
        private void commit_xa_resources( tyrex.tm.XidImpl xid )
        {
            if ( is_xa_used() )
            {
                javax.transaction.xa.XAResource [] xa = list_pending_xa_resources( xid );
                
                for ( int i=0; i<xa.length; i++ )
                {
                    try
                    {
                        xa[i].commit( xid, false );
                    }
                    catch ( javax.transaction.xa.XAException ex )
                    { }
                }
            }
        }
        
        /**
         * This operation returns true if some XA resources have been used
         */
        private boolean is_xa_used()
        {
            if ( _xa_used == false )
            {
                if ( _xa == null )
                {
                    open_xa_resources();
                    
                    if ( ( _xa != null ) && ( _xa.length != 0 ) )
                        _xa_used = true;
                }
            }
            
            return _xa_used;
        }
        
        /**
         * This operation re-opens all connections in order to get all
         * XA resources.
         */
        private void open_xa_resources()
        {
            tyrex.recovery.ConnectionLog [] connections = _monitor.open_connections();
                        
            _xa = new javax.transaction.xa.XAResource[ connections.length ];
            
            for ( int i=0; i<connections.length; i++ )
            {
                try
                {
                    if ( connections[i].datasource.startsWith("tyrex-driver:") )
                    {
                        tyrex.jdbc.xa.EnabledDataSource xasource = new tyrex.jdbc.xa.EnabledDataSource();
                        
                        xasource.setDriverName( connections[i].datasource.substring( 13 ) );
                        
                        _xa[i] = xasource.getXAConnection( connections[i].name, connections[i].password ).getXAResource();
                    }
                    else
                    {
                        tyrex.jdbc.ServerDataSource sds = new tyrex.jdbc.ServerDataSource( connections[i].datasource );
                        
                        java.sql.Connection conn = sds.getConnection( connections[i].name, connections[i].password );
                        
                        _xa[i] = ( ( javax.sql.XAConnection ) conn ).getXAResource();
                    }
                }
                catch ( java.lang.Exception ex )
                { }
            }                        
        }
        
        /**
         * This operation returns all XA resources which needs to be recovered for the XID passed
         * as argument.
         */
        private javax.transaction.xa.XAResource [] list_pending_xa_resources( tyrex.tm.XidImpl xid )
        {
            java.util.Vector list = new java.util.Vector();
            
            for ( int i=0; i<_xa.length; i++ )
            {
                try
                {
                    javax.transaction.xa.Xid [] xids = _xa[i].recover( javax.transaction.xa.XAResource.TMNOFLAGS );
                
                    for ( int j=0; j<xids.length; j++ )
                        if ( xid.equals( xids[j] ) )
                        {
                            list.addElement( _xa[i] );
                            j = xids.length;
                        }
                }
                catch ( javax.transaction.xa.XAException ex )
                { }
            }
            
            javax.transaction.xa.XAResource [] resources = new javax.transaction.xa.XAResource[ list.size() ];
            
            for ( int i=0; i<list.size(); i++ )
                resources[i] = ( javax.transaction.xa.XAResource ) list.elementAt( i );
            
            return resources;
        }
	
	/**
	 * This operation is used to complete the second phase of the 2.P.C protocol.
	 */
	private void continue_ots_commit( tyrex.tm.XidImpl xid, org.omg.CosTransactions.Resource [] resources )
	{
		boolean error = false;
		boolean committed = false;
		boolean rollbacked = false;
		
		java.util.Vector _heuristic = new java.util.Vector();
		
		for ( int i=0; i<resources.length; i++ )
		{						
			if ( ( error ) && ( !committed ) )
			{
				try
				{
					resources[i].rollback();
					rollbacked = true;					
					
					tyrex.recovery.LogWriter.out.rollback_ots_resource( xid, resources[i] );
				}
				catch ( org.omg.CosTransactions.HeuristicCommit ex )
				{
					if ( rollbacked == false )
						committed = true;		
					
					_heuristic.addElement( resources[i] );
				}
				catch ( org.omg.CosTransactions.HeuristicHazard ex )
				{
					_heuristic.addElement( resources[i] );
				}
				catch ( org.omg.CosTransactions.HeuristicMixed ex )
				{
					_heuristic.addElement( resources[i] );
				}
				catch ( org.omg.CORBA.COMM_FAILURE ex )
				{
					// Nothing to do...
				}
				catch ( org.omg.CORBA.OBJECT_NOT_EXIST ex )
				{
					// Nothing to do...
				}
			}
			else
			{
				try
				{
					resources[i].commit();
					committed = true;
					
					tyrex.recovery.LogWriter.out.commit_ots_resource( xid, resources[i] );
				}
				catch ( org.omg.CosTransactions.NotPrepared ex )
				{
					error = true;
				}
				catch ( org.omg.CosTransactions.HeuristicRollback ex )
				{
					error = true;
					
					_heuristic.addElement( resources[i] );
				}
				catch ( org.omg.CosTransactions.HeuristicMixed ex )
				{
					error = true;
					_heuristic.addElement( resources[i] );
				}
				catch ( org.omg.CosTransactions.HeuristicHazard ex )
				{
					error = true;
					_heuristic.addElement( resources[i] );
				}
				catch ( org.omg.CORBA.COMM_FAILURE ex )
				{
					error = true;
				}
				catch ( org.omg.CORBA.OBJECT_NOT_EXIST ex )
				{
					error = true;
				}
			}
		}
		
		if ( !error )
		{
			tyrex.recovery.LogWriter.out.commit_end( xid, 0 );
		}
		else
		{
			tyrex.recovery.LogWriter.out.rollback_end( xid, 0 );			
		}
		
		//
		// Send forget to the resources that reply heuristic
		//
		org.omg.CosTransactions.Resource res = null;
		for ( int i=0; i<_heuristic.size(); i++ )
		{
			res = ( org.omg.CosTransactions.Resource ) _heuristic.elementAt( i );
			
			try
			{
				res.forget();
			}
			catch ( org.omg.CORBA.COMM_FAILURE ex )
			{
				// Nothing to do...
			}
			catch ( org.omg.CORBA.OBJECT_NOT_EXIST ex )
			{
				// Nothing to do...
			}
		}
		
		tyrex.recovery.LogWriter.out.completed( xid );
	}
	
	/**
	 * This operation is used to complete a one phase commit
	 */
	private void continue_ots_commit_one_phase( tyrex.tm.XidImpl xid, org.omg.CosTransactions.Resource resource )
	{
		try
		{
			resource.commit_one_phase();
			
			tyrex.recovery.LogWriter.out.commit_ots_resource( xid, resource );
		}
		catch ( org.omg.CosTransactions.HeuristicHazard ex )
		{
			resource.forget();	
		}
		catch ( org.omg.CORBA.COMM_FAILURE ex )
		{
			// Nothing to do...
		}
		catch ( org.omg.CORBA.OBJECT_NOT_EXIST ex )
		{
			// Nothing to do...
		}
		
		tyrex.recovery.LogWriter.out.completed( xid );
	}
        
        /**
         * Displays a trace
         */
        private void print( String from, String msg )
        {
            tyrex.util.Logger.getSystemLogger().println(from + ": " + msg );
        }
        
        public static void newRecoveryManager( String directory, org.omg.CORBA.ORB orb )
        {
            manager = new RecoveryManager( directory, orb );
        }
}

