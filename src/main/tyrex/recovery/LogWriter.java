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
 * $Id: LogWriter.java,v 1.1 2001/01/11 23:26:33 jdaniel Exp $
 *
 * Date         Author  Changes
 * 1/5/2001     J.Daniel    First implementation.
 */

package tyrex.recovery;

import tyrex.util.Logger;

/**
 * This class is a log writer to save information about transaction processing.
 * 
 * @author <a href="mailto:jdaniel@intalio.com">Jerome Daniel &lt;daniel@intalio.com&gt;</a>
 */
public class LogWriter
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
	 * An external access to writer
	 */
	public static LogWriter out;
	
	/**
	 * Reference to the output stream
	 */
	private java.io.RandomAccessFile log;
		
	/**
	 * Previous log date
	 */
	private int _date_month, _date_day, _date_year;
	
	/**
	 * Previous log name
	 */
	private String _previous_log;
        
        /**
         * Is the log activated
         */
	private boolean _log_enable;
        
        /**
         * The ORB reference
         */
        private org.omg.CORBA.ORB _orb;
        
        /**
         * Reference to the log directory
         */
        private String _log_directory;
        
	/**
	 * Constructor
         *
         * Specifies the log directory as parameters
	 */
	protected LogWriter( String logs, org.omg.CORBA.ORB orb, boolean activated, boolean recovery )
	{		
                _orb = orb;
                _log_enable = activated;
		openLog( logs, recovery );		
                _log_directory = logs;	
        }
	        
	/**
	 * This operation is used to open a log. It first try to open an existing log and if no log exist, it creates
	 * a new one.
	 */
	private void openLog( String directory, boolean recovery )
	{
                if ( !_log_enable )
			return;
		
		String log_name = null;
		
		if ( !directory.endsWith( java.io.File.separator ) )
			directory = directory + java.io.File.separator;
		
		java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
		
		_date_month = calendar.get( calendar.MONTH ) + 1;
		_date_day = calendar.get( calendar.DAY_OF_MONTH );
		_date_year = calendar.get( calendar.YEAR );
		
		log_name = "tyrex_" +  ( calendar.get( calendar.MONTH ) + 1 )+ "_" + calendar.get( calendar.DAY_OF_MONTH )+ "_" + calendar.get( calendar.YEAR ) + ".log";			
			
		try
		{
			boolean exist = new java.io.File( directory + log_name ).exists();
			
			_previous_log = directory + log_name;
			
			log = new java.io.RandomAccessFile( directory + log_name, "rw" );
			
                        if ( !recovery )
			    write_last_log( _previous_log, directory );
			
			if ( exist )
				log.seek( log.length() );
		}
		catch ( java.io.IOException ex )
		{ 
			Logger.getSystemLogger().println("Unable to create a log file.");
			
			throw new org.omg.CORBA.INITIALIZE();
		}
	
                if ( !recovery )
                {
		    //
		    // Now, starts the log timer
		    //
		    tyrex.recovery.LogTimer timer = new tyrex.recovery.LogTimer( _date_day );
		
		    timer.setDaemon( true );
		
		    timer.start();
                }
	}
	
        /**
         * This operation is called when the recovery process is completed to start the next writer
         */
        public void recovery_completed()
        {
            write_last_log( _previous_log, _log_directory );
            
            //
            // Now, starts the log timer
	    //
	    tyrex.recovery.LogTimer timer = new tyrex.recovery.LogTimer( _date_day );
		
	    timer.setDaemon( true );
		
	    timer.start();
        }
        
        
	/**
	 * Add a log for transaction beginning
	 * 
	 * Log is :
	 * 
	 *		TR_BEGIN : int 
	 *		xid : string 
	 */
	public void begin_transaction( tyrex.tm.XidImpl xid )
	{
                if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{			
			try
			{
				log.writeInt( TR_BEGIN );
				                               
				log.writeUTF( xid.toString() );                                  
			}
			catch ( java.io.IOException ex )
			{
			}						
		}
	}		
	
	/**
	 * Add a log for transaction resource registration
	 * 
	 * Log is :
	 * 
	 *		TR_REGISTER : int 
	 *		xid : string
	 *		resource : ior
	 */
	public void register_ots_resource( tyrex.tm.XidImpl xid, org.omg.CosTransactions.Resource resource )
	{
                if ( !_log_enable )
			return;
		                
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_REGISTER );
				
				log.writeUTF( xid.toString() );
				
				log.writeUTF( _orb.object_to_string( resource ) );								
			}
			catch ( java.io.IOException ex )
			{
			}
		}
	}

	/**
	 * Add a log for transaction commit resource
	 * 
	 * Log is :
	 * 
	 *		TR_COMMIT_RESOURCE : int 
	 *		xid : string
	 *		resource : ior	 
	 */
	public void commit_ots_resource( tyrex.tm.XidImpl xid, org.omg.CosTransactions.Resource resource )
	{
                if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_COMMIT_RESOURCE );
				
				log.writeUTF( xid.toString() );
				
				log.writeUTF( _orb.object_to_string( resource ) );			
			}
			catch ( java.io.IOException ex )
			{
			}
		}
	}
	
	/**
	 * Add a log for transaction commit ending
	 * 
	 * Log is :
	 * 
	 *		TR_COMMIT_END : int 
	 *		xid : string
	 *		heuristic : int
	 */
	public void commit_end( tyrex.tm.XidImpl xid, int heuristic )
	{
                if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_COMMIT_END );
				
				log.writeUTF( xid.toString() );						
				
				log.writeInt( heuristic );
			}
			catch ( java.io.IOException ex )
			{
			}
		}
	}
	
	/**
	 * Add a log for transaction commit beginning
	 * 
	 * Log is :
	 * 
	 *		TR_COMMIT_BEGIN : int 
	 *		xid : string	 
         *              two_phase_commit : boolean
	 */
	public void commit_begin( tyrex.tm.XidImpl xid, boolean two_phase_commit )
	{
                if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_COMMIT_BEGIN );
				
				log.writeUTF( xid.toString() );			
                                
                                log.writeBoolean( two_phase_commit );
			}
			catch ( java.io.IOException ex )
			{
			}
		}
	}		
	
	/**
	 * Add a log for transaction rollback ending
	 * 
	 * Log is :
	 * 
	 *		TR_ROLLBACK_END : int 
	 *		xid : string
	 *		heuristic : int
	 */
	public void rollback_end( tyrex.tm.XidImpl xid, int heuristic )
	{
                if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_ROLLBACK_END );
				
				log.writeUTF( xid.toString() );						
				
				log.writeInt( heuristic );
			}
			catch ( java.io.IOException ex )
			{
			}
		}
	}
	
	/**
	 * Add a log for transaction rollback beginning
	 * 
	 * Log is :
	 * 
	 *		TR_ROLLBACK_BEGIN : int 
	 *		xid : string
	 */
	public void rollback_begin( tyrex.tm.XidImpl xid )
	{
                if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_ROLLBACK_BEGIN );
				
				log.writeUTF( xid.toString() );			
			}
			catch ( java.io.IOException ex )
			{
			}
		}
	}
	
	/**
	 * Add a log for transaction rollback
	 * 
	 * Log is :
	 * 
	 *		TR_ROLLBACK_RESOURCE : int 
	 *		xid : string
	 *		resource : ior
	 */
	public void rollback_ots_resource( tyrex.tm.XidImpl xid, org.omg.CosTransactions.Resource resource )
	{
                if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_ROLLBACK_RESOURCE );
				
				log.writeUTF( xid.toString() );						
				
				log.writeUTF( _orb.object_to_string( resource ) );
			}
			catch ( java.io.IOException ex )
			{
			}
		}
	}
	
	
	/**
	 * Add a log for transaction resource prepare
	 * 
	 * Log is :
	 * 
	 *		TR_PREPARE_OTS_RESOURCE : int 
	 *		xid : string
	 *		resource : ior	 
	 *		vote : int
	 */
	public void prepare_ots_resource( tyrex.tm.XidImpl xid, org.omg.CosTransactions.Resource resource, int vote )
	{
                if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_PREPARE_RESOURCE );
				
				log.writeUTF( xid.toString() );
				
				log.writeUTF( _orb.object_to_string( resource ) );						
				
				log.writeInt( vote );
			}
			catch ( java.io.IOException ex )
			{
			}
		}
	}
	
	/**
	 * Add a log for transaction prepare beginning
	 * 
	 * Log is :
	 * 
	 *		TR_PREPARE_BEGIN : int 
	 *		xid : string
	 */
	public void prepare_begin( tyrex.tm.XidImpl xid )
	{
                if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_PREPARE_BEGIN );
				
				log.writeUTF( xid.toString() );			
			}
			catch ( java.io.IOException ex )
			{
			}
		}
	}
	
	/**
	 * Add a log for transaction prepare ending
	 * 
	 * Log is :
	 * 
	 *		TR_PREPARE_END : int 
	 *		xid : string
	 *		heuristic : int	 	 
	 */
	public void prepare_end( tyrex.tm.XidImpl xid, int heuristic )
	{
                if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_PREPARE_END );
				
				log.writeUTF( xid.toString() );
				
				log.writeInt( heuristic );
			}		
			catch ( java.io.IOException ex )
			{
			}
		}
	}
		
	/**
	 * Add a log for transaction completion ( abort or commit )
	 * 
	 * Log is :
	 * 
	 *		TR_COMPLETED : int 
	 *		xid : string	 
	 */
	public void completed( tyrex.tm.XidImpl xid )
	{
                if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_COMPLETED );
				
				log.writeUTF( xid.toString() );											
			}
			catch ( java.io.IOException ex )
			{			
			}
		}
	}
	
	/**
	 * Add a log for transaction uncompletion ( when we change log )
	 * 
	 * Log is :
	 * 
	 *		TR_UNCOMPLETED : int 
	 *		xid : string	 
	 */
	public void uncompleted( tyrex.tm.XidImpl xid )
	{
		if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_UNCOMPLETED );
				
				log.writeUTF( xid.toString() );											
			}
			catch ( java.io.IOException ex )
			{			
			}
		}
	}
        
        /**
	 * Add a log for transaction prepare beginning
	 * 
	 * Log is :
	 * 
	 *		TR_CONNECTION : int 
	 *		User name : string
         *              User password : string
         *              XA data source name : string
	 */
	public void open_connection( String name, String pwd, String source_name )
	{
                if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_CONNECTION );
				
				log.writeUTF( name );			
                                log.writeUTF( pwd );
                                log.writeUTF( source_name );
			}
			catch ( java.io.IOException ex )
			{
			}
		}
	}
	
	/**
	 * Add a log for a recovery. It means that everything above is no more needed for a next recovery
	 * 
	 * Log is :
	 * 
	 *		TR_RECOVERY : int 	 
	 */
	public void recovery()
	{
		if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_RECOVERY );								
			}
			catch ( java.io.IOException ex )
			{			
			}
		}
	}
	
	/**
	 * Add a log for a previous log.
	 * 
	 * Log is :
	 * 
	 *		TR_PREVIOUS_LOG : int 	 
	 *		previous log name : string
	 */
	public void previous_log( String previous_log_name )
	{
		if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				log.writeInt( TR_PREVIOUS_LOG );								
				
				log.writeUTF( previous_log_name );
			}
			catch ( java.io.IOException ex )
			{			
			}
		}
	}
        
	/**
	 * At 0:0, the log must be changed. This operation closes the current log, creates a new one.
	 */
	public void changeLog()
	{
		if ( !_log_enable )
			return;
		
		synchronized ( LogWriter.class )
		{
			try
			{
				//
				// Close the current log
				//
				log.close();																			
				
				//
				// Check date to apply new log
				//				
				int _month, _day, _year;				
				boolean date_ok = false;
				
				while ( ! date_ok )
				{
					java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
				
					_month = calendar.get( calendar.MONTH ) + 1;
					_day = calendar.get( calendar.DAY_OF_MONTH );
					_year = calendar.get( calendar.YEAR );
					
					if ( _year > _date_year )
						date_ok = true;
					else
					if ( _month > _date_month )
						date_ok = true;
					else
					if ( _day > _date_day )
						date_ok = true;
					else
					{
						//
						// Make a wait of 30 seconds
						//
						try
						{
							Thread.currentThread().wait( 30000 );
						}
						catch ( java.lang.InterruptedException ex )
						{ }
					}					
				}
				
				//
				// Look for uncompleted transactions
				//
				tyrex.recovery.LogReader reader = new tyrex.recovery.LogReader( _previous_log, _orb );
				
				tyrex.tm.XidImpl [] xids = reader.uncompleted_transactions();			
				
				reader.close();
				
				// 
				// Create the new log
				//
				String _previous_log_name = _previous_log;
				
				openLog( _log_directory, false );
				
				//
				// Add the previous log name
				//
				previous_log( _previous_log_name );
				
				//
				// Add uncompleted transactions
				//
				for ( int i=0; i<xids.length; i++ )
				{
					uncompleted( xids[i] );					
				}
				
			}
			catch ( java.io.IOException ex )
			{ }
		}
	}
	
	/**
	 * Create a lock file that contains the last log name.
	 */
	private void write_last_log( String last_log, String directory )
	{
		if ( !directory.endsWith( java.io.File.separator ) )
			directory = directory + java.io.File.separator;
		
		String lock_last = directory + "ots.log";
				
		try
		{
			java.io.FileOutputStream lock = new java.io.FileOutputStream( lock_last );
			java.io.DataOutputStream writer = new java.io.DataOutputStream( lock );
			writer.writeUTF( last_log );
			writer.close();
			lock.close();
		}
		catch ( java.io.IOException ex )
		{
                        ex.printStackTrace();
			Logger.getSystemLogger().println("Unable to write lock file for log !");
		}
		
	}
	
	public static void newWriter( String directory, org.omg.CORBA.ORB orb, boolean activated, boolean recovery )
	{
		out = new LogWriter( directory, orb, activated, recovery );
	}
}

