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
 *
 * Date         Author  Changes
 */

package tyrex.recovery;

/**
 * This class is used to read logs.
 * 
 * @author <a href="mailto:jdaniel@intalio.com">Jerome Daniel &lt;daniel@intalio.com&gt;</a>
 * @version $Revision: 1.1 $ $Date: 2001/01/11 23:26:33 $ 
 */
public class LogReader
{
	/**
	 * Log kind
	 */
	private final int TR_BEGIN = 0;
	private final int TR_COMMIT_END = 1;
	private final int TR_ROLLBACK_BEGIN = 2;
	private final int TR_ROLLBACK_END = 3;
	private final int TR_COMMIT_BEGIN = 4;	
        private final int TR_OLD_CONNECTION = 5;
	private final int TR_ROLLBACK_RESOURCE = 6;
	private final int TR_PREPARE_RESOURCE = 7;
	private final int TR_PREPARE_BEGIN = 8;
	private final int TR_PREPARE_END = 9;
	private final int TR_REGISTER = 10;
	private final int TR_COMMIT_RESOURCE = 11;
	private final int TR_COMPLETED = 12;
	private final int TR_CONNECTION = 13;
	private final int TR_RECOVERY = 14;
	private final int TR_PREVIOUS_LOG = 15;
	private final int TR_UNCOMPLETED = 16;
	
	/**
	 * Reference to the log access
	 */
	private java.io.RandomAccessFile _log;
	
	/**
	 * Is log open
	 */
	private boolean _open;
        
        /**
         * Reference to the ORB
         */
        private org.omg.CORBA.ORB _orb;
	
	/**
	 * Constructor
	 */
	public LogReader( String log_name, org.omg.CORBA.ORB orb )
	{
		_open = false;				
		_orb = orb;
               
		try		
		{
			_log = new java.io.RandomAccessFile( log_name, "r" );
			_open = true;
		}
		catch ( java.io.IOException ex )
		{
			System.out.println("OpenORB OTS error : Unable to read into a log file : " + log_name );
		}
	}
	
	/**
	 * This operation is used to close this log reader
	 */
	public void close()
	{
		if ( !_open )
			return;
		
		try
		{
			_log.close();
		}
		catch ( java.io.IOException ex )
		{
			System.out.println("OpenORB OTS errror : Unable to close a log file !");
			return;
		}
		
		_open = false;
	}
	
	/**
	 * Return all uncompleted transactions found into this log 
	 */
	public tyrex.tm.XidImpl [] uncompleted_transactions()
	{
		java.util.Vector begin = new java.util.Vector();
		java.util.Vector uncompleted = new java.util.Vector();
		
		int kind;
	
		if ( _open )
		{
			try
			{
				while (  !eof() )
				{
					kind = _log.readInt();
					
					switch ( kind )
					{
					case TR_BEGIN :						
						begin.addElement( _log.readUTF() );
						break;
					case TR_COMMIT_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_ROLLBACK_BEGIN :
						_log.readUTF();
						break;
					case TR_ROLLBACK_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_COMMIT_BEGIN :
						_log.readUTF();
                                                _log.readBoolean();
						break;
					case TR_ROLLBACK_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_PREPARE_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						_log.readInt();
						break;
					case TR_PREPARE_BEGIN :
						_log.readUTF();
						break;
					case TR_PREPARE_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_REGISTER :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMMIT_RESOURCE:
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMPLETED :
						String xid = _log.readUTF();                                                
						for ( int i=0; i<begin.size(); i++ )
						{
							if ( xid.equals( ( String ) begin.elementAt( i ) ) )
							{
								begin.removeElementAt( i );
								break;
							}
						}
						break;
					case TR_CONNECTION :
						_log.readUTF();
                                                _log.readUTF();
                                                _log.readUTF();
						break;
					case TR_RECOVERY :
						begin.removeAllElements();
						uncompleted.removeAllElements();
						break;
					case TR_PREVIOUS_LOG :
						_log.readUTF();
						break;
					case TR_UNCOMPLETED :
						uncompleted.addElement( _log.readUTF() );
						break;
					}
				}
			}
			catch ( java.io.IOException ex )
			{
			}
		}		                
                
		for ( int i=0; i<begin.size(); i++ )
                {
                        if ( !( ( String ) begin.elementAt(i) ).equals("") )
			    uncompleted.addElement( begin.elementAt( i ) );
                }
		
		tyrex.tm.XidImpl [] xids = new tyrex.tm.XidImpl[ uncompleted.size() ];
		
		for ( int i=0; i<uncompleted.size(); i++ )
			xids[i] = new tyrex.tm.XidImpl( ( String ) uncompleted.elementAt( i ) );
		
		return xids;		
	}
	
	/**
	 * This operation returns the transaction status found in this log for the asked XID
	 */
	public org.omg.CosTransactions.Status transaction_status( tyrex.tm.XidImpl xid )
	{
		org.omg.CosTransactions.Status status = org.omg.CosTransactions.Status.StatusNoTransaction;
		String str = null;
		
		int kind;

		if ( _open )
		{
			try
			{
				while (  !eof() )
				{
					kind = _log.readInt();
					
					switch ( kind )
					{
					case TR_BEGIN :						
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
							status = org.omg.CosTransactions.Status.StatusActive;
						break;
					case TR_COMMIT_END :
						str = _log.readUTF();	
                                                _log.readInt();
						if ( str.equals( xid.toString() ) )
							status = org.omg.CosTransactions.Status.StatusCommitted;
						break;
					case TR_ROLLBACK_BEGIN :
						str = _log.readUTF();	
						if ( str.equals( xid.toString() ) )
							status = org.omg.CosTransactions.Status.StatusRollingBack;
						break;
					case TR_ROLLBACK_END :
						str = _log.readUTF();	
                                                _log.readInt();
						if ( str.equals( xid.toString() ) )
							status = org.omg.CosTransactions.Status.StatusRolledBack;
						break;
					case TR_COMMIT_BEGIN :
						str = _log.readUTF();	
                                                _log.readBoolean();
						if ( str.equals( xid.toString() ) )
							status = org.omg.CosTransactions.Status.StatusCommitting;
						break;
					case TR_ROLLBACK_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_PREPARE_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						_log.readInt();
						break;
					case TR_PREPARE_BEGIN :
						str = _log.readUTF();	
                                                _log.readInt();
						if ( str.equals( xid.toString() ) )
							status = org.omg.CosTransactions.Status.StatusPreparing;
						break;
					case TR_PREPARE_END :
						str = _log.readUTF();
                                                _log.readInt();
						if ( str.equals( xid.toString() ) )
							status = org.omg.CosTransactions.Status.StatusPrepared;
						break;
					case TR_REGISTER :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMMIT_RESOURCE:
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMPLETED :
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
							return status;						
					case TR_CONNECTION :
						_log.readUTF();
                                                _log.readUTF();
                                                _log.readUTF();
						break;
					case TR_RECOVERY :
						break;
					case TR_PREVIOUS_LOG :
						_log.readUTF();
						break;
					case TR_UNCOMPLETED :
						_log.readUTF();
						break;
					}
				}
			}
			catch ( java.io.IOException ex )
			{
			}		
		}
		
		return status;
	}

	/**
	 * This operation returns all registered resources for this transaction XID. It also returns TRUE if
	 * the begin transaction was found into this log.
	 */
	public boolean registered_ots_resources( tyrex.tm.XidImpl xid, java.util.Vector resources )
	{
		int kind;
		boolean begin = false;
		String str = null;
		
		if ( _open )
		{
			try
			{
				while (  !eof() )
				{
					kind = _log.readInt();
					
					switch ( kind )
					{
					case TR_BEGIN :						
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
							begin = true;
						break;
					case TR_COMMIT_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_ROLLBACK_BEGIN :
						_log.readUTF();
						break;
					case TR_ROLLBACK_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_COMMIT_BEGIN :
						_log.readUTF();
                                                _log.readBoolean();
						break;
					case TR_ROLLBACK_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_PREPARE_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						_log.readInt();
						break;
					case TR_PREPARE_BEGIN :
						_log.readUTF();
						break;
					case TR_PREPARE_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_REGISTER :
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
						{
							str = _log.readUTF();
							
							try
							{
								org.omg.CosTransactions.Resource r = org.omg.CosTransactions.ResourceHelper.narrow( 
																		_orb.string_to_object( str ) );
							
								resources.addElement( r );
							}
							catch ( java.lang.Exception ex )
							{ }							
						}
						else
							_log.readUTF();
						break;
					case TR_COMMIT_RESOURCE:
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMPLETED :
						_log.readUTF();
						break;
					case TR_CONNECTION :
						_log.readUTF();
                                                _log.readUTF();
                                                _log.readUTF();			
						break;
					case TR_RECOVERY :
						break;
					case TR_PREVIOUS_LOG :
						_log.readUTF();
						break;
					case TR_UNCOMPLETED :
						_log.readUTF();
						break;
					}
				}
			}
			catch ( java.io.IOException ex )
			{
			}		
		}
		
		return begin;
	}
	
	/**
	 * This operation returns all prepared resources ( not committed, not rolledback ) for this transaction XID. 
	 * It also returns TRUE if the begin transaction was found into this log.
	 */
	public boolean prepared_ots_resources( tyrex.tm.XidImpl xid, java.util.Vector resources )
	{
		int kind;
		boolean begin = false;
		String str = null;								
		
		if ( _open )
		{
			try
			{
				while (  !eof() )
				{
					kind = _log.readInt();
					
					switch ( kind )
					{
					case TR_BEGIN :						
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
							begin = true;
						break;
					case TR_COMMIT_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_ROLLBACK_BEGIN :
						_log.readUTF();
						break;
					case TR_ROLLBACK_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_COMMIT_BEGIN :
						_log.readUTF();
                                                _log.readBoolean();
						break;					
					case TR_ROLLBACK_RESOURCE :
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
						{
							str = _log.readUTF();
							
							if ( ! reserved( resources, str ) )
							{
								tyrex.recovery.ResourceLog rl = new tyrex.recovery.ResourceLog();
								
								rl.ior = str;
								rl.reserved = true;
								rl.resource = null;											
							}
						}
						else
							_log.readUTF();
						break;
					case TR_PREPARE_RESOURCE :
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
						{
							str = _log.readUTF();
							
							if ( ! alreadyReserved( resources, str ) )
							{
								tyrex.recovery.ResourceLog rl = new tyrex.recovery.ResourceLog();
								
								rl.ior = str;
								rl.reserved = false;
								
								try
								{
									rl.resource = org.omg.CosTransactions.ResourceHelper.narrow( 
														_orb.string_to_object( str ) );
							
									resources.addElement( rl );
								}
								catch ( java.lang.Exception ex )
								{ }			
							}
						}
						else
							_log.readUTF();
						_log.readInt();
						break;
					case TR_PREPARE_BEGIN :
						_log.readUTF();
						break;
					case TR_PREPARE_END :
						_log.readUTF();
						_log.readInt();						
						break;
					case TR_REGISTER :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMMIT_RESOURCE:
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
						{
							str = _log.readUTF();
							
							if ( ! reserved( resources, str ) )
							{
								tyrex.recovery.ResourceLog rl = new tyrex.recovery.ResourceLog();
								
								rl.ior = str;
								rl.reserved = true;
								rl.resource = null;											
							}
						}
						else
							_log.readUTF();
						break;
					case TR_COMPLETED :
						_log.readUTF();
						break;
					case TR_CONNECTION :
						_log.readUTF();
                                                _log.readUTF();
                                                _log.readUTF();			
						break;
					case TR_RECOVERY :
						break;
					case TR_PREVIOUS_LOG :
						_log.readUTF();
						break;
					case TR_UNCOMPLETED :
						_log.readUTF();
						break;
					}
				}
			}
			catch ( java.io.IOException ex )
			{
			}		
		}
		
		return begin;
	}
	
	/**
	 * This operation returns all free resources ( not prepared, not committed, not rolledback ) for this transaction XID. 
	 * It also returns TRUE if the begin transaction was found into this log.
	 */
	public boolean free_ots_resources( tyrex.tm.XidImpl xid, java.util.Vector resources )
	{
		int kind;
		boolean begin = false;
		String str = null;								
		
		if ( _open )
		{
			try
			{
				while (  !eof() )
				{
					kind = _log.readInt();
					
					switch ( kind )
					{
					case TR_BEGIN :						
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
							begin = true;
						break;
					case TR_COMMIT_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_ROLLBACK_BEGIN :
						_log.readUTF();
						break;
					case TR_ROLLBACK_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_COMMIT_BEGIN :
						_log.readUTF();
                                                _log.readBoolean();
						break;					
					case TR_ROLLBACK_RESOURCE :
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
						{
							str = _log.readUTF();
							
							if ( ! reserved( resources, str ) )
							{
								tyrex.recovery.ResourceLog rl = new tyrex.recovery.ResourceLog();
								
								rl.ior = str;
								rl.reserved = true;
								rl.resource = null;											
							}
						}
						else
							_log.readUTF();
						break;
					case TR_PREPARE_RESOURCE :
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
						{
							str = _log.readUTF();
							
							if ( ! reserved( resources, str ) )
							{
								tyrex.recovery.ResourceLog rl = new tyrex.recovery.ResourceLog();
								
								rl.ior = str;
								rl.reserved = true;
								rl.resource = null;											
							}
						}
						else
							_log.readUTF();
						_log.readInt();
						break;
					case TR_PREPARE_BEGIN :
						_log.readUTF();
						break;
					case TR_PREPARE_END :
						_log.readUTF();						
						_log.readInt();
						break;
					case TR_REGISTER :
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
						{
							str = _log.readUTF();
							
							if ( ! alreadyReserved( resources, str ) )
							{
								tyrex.recovery.ResourceLog rl = new tyrex.recovery.ResourceLog();
								
								rl.ior = str;
								rl.reserved = false;
								
								try
								{
									rl.resource = org.omg.CosTransactions.ResourceHelper.narrow( 
														_orb.string_to_object( str ) );
							
									resources.addElement( rl );
								}
								catch ( java.lang.Exception ex )
								{ }			
							}
						}
						else
							_log.readUTF();
						break;
					case TR_COMMIT_RESOURCE:
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
						{
							str = _log.readUTF();
							
							if ( ! reserved( resources, str ) )
							{
								tyrex.recovery.ResourceLog rl = new tyrex.recovery.ResourceLog();
								
								rl.ior = str;
								rl.reserved = true;
								rl.resource = null;											
							}
						}
						else
							_log.readUTF();
						break;
					case TR_COMPLETED :
						_log.readUTF();
						break;
					case TR_CONNECTION :
						_log.readUTF();
                                                _log.readUTF();
                                                _log.readUTF();			
						break;
					case TR_RECOVERY :
						break;
					case TR_PREVIOUS_LOG :
						_log.readUTF();
						break;
					case TR_UNCOMPLETED :
						_log.readUTF();
						break;
					}
				}
			}
			catch ( java.io.IOException ex )
			{
			}		
		}
		
		return begin;
	}
	
	/**
	 * This operation returns the prepare vote found in this log for the asked XID
	 */
	public int prepare_vote( tyrex.tm.XidImpl xid )
	{
		int vote = -1;
		String str = null;
		
		int kind;

		if ( _open )
		{
			try
			{
				while (  !eof() )
				{
					kind = _log.readInt();
					
					switch ( kind )
					{
					case TR_BEGIN :						
						_log.readUTF();
						break;
					case TR_COMMIT_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_ROLLBACK_BEGIN :
						_log.readUTF();
						break;
					case TR_ROLLBACK_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_COMMIT_BEGIN :
						_log.readUTF();
                                                _log.readBoolean();
						break;
					case TR_ROLLBACK_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_PREPARE_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						_log.readInt();
						break;
					case TR_PREPARE_BEGIN :
						_log.readUTF();
						break;
					case TR_PREPARE_END :
						str = _log.readUTF();						
						if ( str.equals( xid.toString() ) )	
                                                {                                                    
                                                    int heuristic = _log.readInt();
                                                    switch ( heuristic )
                                                    {
                                                        case tyrex.tm.Heuristic.ReadOnly :
                                                            vote = org.omg.CosTransactions.Vote._VoteReadOnly;
                                                            break;
                                                        case tyrex.tm.Heuristic.Commit :
                                                            vote = org.omg.CosTransactions.Vote._VoteCommit;
                                                            break;
                                                        case tyrex.tm.Heuristic.Rollback :
                                                        case tyrex.tm.Heuristic.Hazard :
                                                        case tyrex.tm.Heuristic.Mixed :
                                                            vote = org.omg.CosTransactions.Vote._VoteRollback;
                                                            break;
                                                    }
                                                }
						else
							_log.readInt();
						return vote;
					case TR_REGISTER :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMMIT_RESOURCE:
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMPLETED :
						_log.readUTF();
						break;
					case TR_CONNECTION :
						_log.readUTF();
                                                _log.readUTF();
                                                _log.readUTF();			
						break;
					case TR_RECOVERY :
						break;
					case TR_PREVIOUS_LOG :
						_log.readUTF();
						break;
					case TR_UNCOMPLETED :
						_log.readUTF();
						break;
					}
				}
			}
			catch ( java.io.IOException ex )
			{
				System.out.println("OpenORB OTS Error : Unable to read into a log !");
			}		
		}
		
		return vote;
	}
	
	/**
	 * Return the previous log name or NULL if none.
	 */
	public String previous_log()
	{
		int kind;

		if ( _open )
		{
			try
			{
				while (  !eof() )
				{
					kind = _log.readInt();
					
					switch ( kind )
					{
                                        case TR_BEGIN :						
						_log.readUTF();
						break;
					case TR_COMMIT_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_ROLLBACK_BEGIN :
						_log.readUTF();
						break;
					case TR_ROLLBACK_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_COMMIT_BEGIN :
						_log.readUTF();
                                                _log.readBoolean();
						break;
					case TR_ROLLBACK_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_PREPARE_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						_log.readInt();
						break;
					case TR_PREPARE_BEGIN :
						_log.readUTF();
						break;
					case TR_PREPARE_END :
						_log.readUTF();
						_log.readInt();						
						break;
					case TR_REGISTER :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMMIT_RESOURCE:
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMPLETED :
						_log.readUTF();
						break;
					case TR_CONNECTION :
						_log.readUTF();
                                                _log.readUTF();
                                                _log.readUTF();			
						break;
					case TR_RECOVERY :
						break;
					case TR_UNCOMPLETED :
						_log.readUTF();
						break;					
					case TR_PREVIOUS_LOG :
						return _log.readUTF();									
					}
				}
			}
			catch ( java.io.IOException ex )
			{
			}		
		}
		
		return null;
	}
	
        /**
         * This operation returns a description of all open connections
         */
        public tyrex.recovery.ConnectionLog [] open_connections()
        {
                java.util.Vector list = new java.util.Vector();
                int kind;

		if ( _open )
		{
			try
			{
				while (  !eof() )
				{
					kind = _log.readInt();
					
					switch ( kind )
					{
                                        case TR_BEGIN :						
						_log.readUTF();
						break;
					case TR_COMMIT_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_ROLLBACK_BEGIN :
						_log.readUTF();
						break;
					case TR_ROLLBACK_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_COMMIT_BEGIN :
						_log.readUTF();
                                                _log.readBoolean();
						break;
					case TR_ROLLBACK_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_PREPARE_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						_log.readInt();
						break;
					case TR_PREPARE_BEGIN :
						_log.readUTF();
						break;
					case TR_PREPARE_END :
						_log.readUTF();
						_log.readInt();						
						break;
					case TR_REGISTER :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMMIT_RESOURCE:
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMPLETED :
						_log.readUTF();
						break;
					case TR_CONNECTION :
                                                tyrex.recovery.ConnectionLog ct = new tyrex.recovery.ConnectionLog();
						ct.name = _log.readUTF();
                                                ct.password = _log.readUTF();
                                                ct.datasource = _log.readUTF();			
                                                list.addElement( ct );
						break;
					case TR_RECOVERY :
						break;
					case TR_UNCOMPLETED :
						_log.readUTF();
						break;					
					case TR_PREVIOUS_LOG :
						_log.readUTF();									
                                                break;
					}
				}
			}
			catch ( java.io.IOException ex )
			{
			}		
		}
		
                tyrex.recovery.ConnectionLog [] cts = new tyrex.recovery.ConnectionLog[ list.size() ];
                for ( int i=0; i<list.size(); i++ )
                {
                    cts[i] = ( tyrex.recovery.ConnectionLog ) list.elementAt(i);
                }
                
		return cts;
        }
        
	/**
	 * Return 0 if no rollback found for a resource, 1 if a rollback is found and 2 if the rollback is not found with
	 * transaction begin
	 */
	public int is_any_rollback( tyrex.tm.XidImpl xid )
	{
		String str = null;
		int kind;
		
		int result = 0;

		if ( _open )
		{
			try
			{
				while (  !eof() )
				{
					kind = _log.readInt();
					
					switch ( kind )
					{
					case TR_BEGIN :						
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
							result = 2;
						break;
					case TR_COMMIT_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_ROLLBACK_BEGIN :
						_log.readUTF();
						break;
					case TR_ROLLBACK_END :
						_log.readUTF();
						_log.readInt();
						break;
					case TR_COMMIT_BEGIN :
						_log.readUTF();
                                                _log.readBoolean();
						break;
					case TR_ROLLBACK_RESOURCE :
						str = _log.readUTF();
						if ( str.equals( xid.toString() ) )
							return 1;
						_log.readUTF();
						break;
					case TR_PREPARE_RESOURCE :
						_log.readUTF();
						_log.readUTF();
						_log.readInt();
						break;
					case TR_PREPARE_BEGIN :
						_log.readUTF();
						break;
					case TR_PREPARE_END :
						_log.readUTF();
						_log.readInt();						
						break;
					case TR_REGISTER :
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMMIT_RESOURCE:
						_log.readUTF();
						_log.readUTF();
						break;
					case TR_COMPLETED :
						_log.readUTF();
						break;
                                        case TR_CONNECTION :
						_log.readUTF();
                                                _log.readUTF();
                                                _log.readUTF();			
						break;
					case TR_RECOVERY :
						break;
					case TR_PREVIOUS_LOG :
						_log.readUTF();
						break;
					case TR_UNCOMPLETED :
						_log.readUTF();
						break;
					}
				}
			}
			catch ( java.io.IOException ex )
			{
			}		
		}
		
		return result;
	}
	
	/**
	 * This operation is used to test is the file pointer is at the end of the log.
	 */
	private boolean eof()
	{
		try
		{
			if ( _log.getFilePointer() == _log.length() )
				return true;
		}
		catch ( java.io.IOException ex )
		{
			ex.printStackTrace();
		}
		
		return false;
	}	
	
	/**
	 * Return FALSE if the resource is not already in the list ( thus cannot be reserved )
	 */
	private boolean reserved( java.util.Vector resources, String ior )
	{
		tyrex.recovery.ResourceLog rl = null;
		
		for ( int i=0; i<resources.size(); i++ )
		{
			rl = ( tyrex.recovery.ResourceLog ) resources.elementAt( i );
			
			if ( rl.ior.equals( ior ) )
			{
				rl.reserved = true;
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Return FALSE if the resource is not already reserved in the list ( thus cannot be reserved )
	 */
	private boolean alreadyReserved( java.util.Vector resources, String ior )
	{
		tyrex.recovery.ResourceLog rl = null;
		
		for ( int i=0; i<resources.size(); i++ )
		{
			rl = ( tyrex.recovery.ResourceLog ) resources.elementAt( i );
			
			if ( rl.ior.equals( ior ) )
			{
				if ( rl.reserved )
					return true;
				else
					return false;
			}
		}
		
		return false;
	}
	
}


