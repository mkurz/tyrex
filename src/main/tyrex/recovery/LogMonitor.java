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
 * $Id: LogMonitor.java,v 1.1 2001/01/11 23:26:33 jdaniel Exp $
 *
 * Date         Author  Changes
 */

package tyrex.recovery;

/**
 * The log monitor is able to get information from logs a transaction. This monitor is usefull for transaction
 * managment and transaction recovery.
 * 
 * @author <a href="mailto:jdaniel@intalio.com">Jerome Daniel &lt;daniel@intalio.com&gt;</a>
 * @version $Revision: 1.1 $ $Date: 2001/01/11 23:26:33 $ 
 */
public class LogMonitor
{
	/**
	 * Reference to the current reader
	 */
        private tyrex.recovery.LogReader _reader;
	
	/**
	 * Last log name
	 */
	private String _last_log;
	
	/**
	 * Reference to the ORB
	 */
	private org.omg.CORBA.ORB _orb;
        
        /**
         * Log directory
         */
        private String _directory;
								
	/**
	 * Constructor
	 */
	public LogMonitor( String dir )
	{
            _directory = dir;
        }
        
        /**
         * Sets the ORB reference
         */
        public void setORB( org.omg.CORBA.ORB orb )
        {
            _orb = orb;
        }
	
	/**
	 * Return a transaction status
	 */	
	public org.omg.CosTransactions.Status transaction_status( tyrex.tm.XidImpl xid )
	{
		org.omg.CosTransactions.Status status = null;
		
		openLast();
		
		status = _reader.transaction_status( xid );
		
		if ( status.value() == org.omg.CosTransactions.Status._StatusNoTransaction )
		{
			while ( true )
			{
				if ( ! openPrevious() )
					break;
			
				status = _reader.transaction_status( xid );
				
				if ( status.value() != org.omg.CosTransactions.Status._StatusNoTransaction )
					break;
			}
		}
				
		_reader.close();
		
		return status;
	}
	
	/**
	 * Return all uncompleted transactions XIDs.
	 */
	public tyrex.tm.XidImpl [] uncompleted_transactions()
	{
		openLast();
		
		tyrex.tm.XidImpl [] xids = _reader.uncompleted_transactions();
			
		_reader.close();
		
		return xids;
	}
	
	/**
	 * Return all registered resources for a transaction.
	 */
	public org.omg.CosTransactions.Resource [] registered_ots_resources( tyrex.tm.XidImpl xid )
	{
		org.omg.CosTransactions.Resource [] registered = null;
		
		java.util.Vector resources = new java.util.Vector();
		
		openLast();
		
		boolean is_begin_here = _reader.registered_ots_resources( xid, resources );
		
		if ( is_begin_here )
		{
			while ( true )
			{
				if ( ! openPrevious() ) // Strange... some logs have been loosed :-(				
					break;
			
				is_begin_here = _reader.registered_ots_resources( xid, resources );
				
				if ( is_begin_here )
					break;
			}
		}
				
		_reader.close();
		
		registered = new org.omg.CosTransactions.Resource[ resources.size() ];
		
		for ( int i=0; i<resources.size(); i++ )
			registered[ i ] = ( org.omg.CosTransactions.Resource ) resources.elementAt( i );
		
		return registered;
	}
	
	/**
	 * Return all prepared resources ( not committed, not rolledback ) for a transaction.
	 */
	public org.omg.CosTransactions.Resource [] prepared_ots_resources( tyrex.tm.XidImpl xid )
	{
		org.omg.CosTransactions.Resource [] prepared = null;
		
		java.util.Vector resources = new java.util.Vector();
		
		openLast();
		
		boolean is_begin_here = _reader.prepared_ots_resources( xid, resources );
		
		if ( is_begin_here )
		{
			while ( true )
			{
				if ( ! openPrevious() ) // Strange... some logs have been loosed :-(				
					break;
			
				is_begin_here = _reader.prepared_ots_resources( xid, resources );
				
				if ( is_begin_here )
					break;
			}
		}
				
		_reader.close();
		
		int nb_prepared = 0;
		tyrex.recovery.ResourceLog rl = null;
		for ( int i=0; i<resources.size(); i++ )
		{
			rl = ( tyrex.recovery.ResourceLog ) resources.elementAt( i ) ;
			if ( !rl.reserved )
				nb_prepared++;
		}
		
		prepared = new org.omg.CosTransactions.Resource[ nb_prepared ];
		
		nb_prepared = 0;
		for ( int i=0; i<resources.size(); i++ )
		{
			rl = ( tyrex.recovery.ResourceLog ) resources.elementAt( i ) ;
			
			if ( !rl.reserved )
				prepared[ nb_prepared++ ] = rl.resource;
		}
		
		return prepared;
	}
	
	/**
	 * Return all free resources ( not prepared, not committed, not rolledback ) for a transaction.
	 */
	public org.omg.CosTransactions.Resource [] free_ots_resources( tyrex.tm.XidImpl xid )
	{
		org.omg.CosTransactions.Resource [] free = null;
		
		java.util.Vector resources = new java.util.Vector();
		
		openLast();
		
		boolean is_begin_here = _reader.free_ots_resources( xid, resources );
		
		if ( is_begin_here )
		{
			while ( true )
			{
				if ( ! openPrevious() ) // Strange... some logs have been loosed :-(				
					break;
			
				is_begin_here = _reader.free_ots_resources( xid, resources );
				
				if ( is_begin_here )
					break;
			}
		}
				
		_reader.close();
		
		int nb_free = 0;
		tyrex.recovery.ResourceLog rl = null;
		for ( int i=0; i<resources.size(); i++ )
		{
			rl = ( tyrex.recovery.ResourceLog ) resources.elementAt( i ) ;
			if ( !rl.reserved )
				nb_free++;
		}
		
		free = new org.omg.CosTransactions.Resource[ nb_free ];
		
		nb_free = 0;
		for ( int i=0; i<resources.size(); i++ )
		{
			rl = ( tyrex.recovery.ResourceLog ) resources.elementAt( i ) ;
			
			if ( !rl.reserved )
				free[ nb_free++ ] = rl.resource;
		}
		
		return free;
	}
	
	/**
	 * Return a vote result after a prepare.
	 */
	public org.omg.CosTransactions.Vote prepare_vote( tyrex.tm.XidImpl xid )
	{
		int vote;
		
		openLast();
		
		vote = _reader.prepare_vote( xid );
		
		if ( vote == -1 )
		{
			while ( true )
			{
				if ( ! openPrevious() )
					break;
			
				vote = _reader.prepare_vote( xid );
				
				if ( vote != -1 )
					break;
			}
		}
				
		_reader.close();
		
		return org.omg.CosTransactions.Vote.from_int( vote );
	}
	
        /**
         * Returns all open connections log
         */
        public tyrex.recovery.ConnectionLog [] open_connections()
        {
            return _reader.open_connections();
        }
        
	/**
	 * Return true if one resource has rolledback
	 */
	public boolean is_any_rollback( tyrex.tm.XidImpl xid )
	{
		boolean rollback = false;
		
		openLast();
		
		// Result
		// 0 . no rollback
		// 1 . rollback
		// 2 . begin of transaction
		int result = _reader.is_any_rollback( xid );
		
		if ( result == 0 )
		{
			while ( true )
			{
				if ( ! openPrevious() )
					break;
			
				result = _reader.is_any_rollback( xid );
				
				if ( result != 0 )
					break;
			}
		}
				
		_reader.close();
		
		if ( result == 1 )
			rollback = true;
		
		return rollback;
	}
	
	/**
	 * Open the last log
	 */
	private void openLast()
	{
		if ( _reader != null )
			_reader.close();
			
		if ( _last_log == null )
		{			
			if ( !_directory.endsWith( java.io.File.separator ) )
				_directory = _directory + java.io.File.separator;
		
			String lock_last = _directory + "ots.log";
				
			try
			{
				java.io.FileInputStream lock = new java.io.FileInputStream( lock_last );
				java.io.DataInputStream reader = new java.io.DataInputStream( lock );
				_last_log = reader.readUTF();
				reader.close();
				lock.close();
			}
			catch ( java.io.IOException ex )
			{                                
				exception("LogMonitor", "Unable to read lock file for log !", ex);
			}
		}
		
		openReader( _last_log );
	}
	
	/**
	 * Open the previous log of the current log
	 */
	private boolean openPrevious()
	{
		String previous_name = _reader.previous_log();
		
		if ( previous_name == null )
			return false;
		
		_reader.close();
		
		openReader( previous_name );	
		
		return true;
	}
	
	/**
	 * Open a log reader
	 */
	private void openReader( String log_name )
	{
		_reader = new tyrex.recovery.LogReader( log_name, _orb );
	}
        
        /**
         * Displays a trace message
         */
        private void exception( String from, String msg, java.lang.Exception ex )
        {
        }
}

