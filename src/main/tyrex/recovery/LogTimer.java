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
 * $Id: LogTimer.java,v 1.1 2001/01/11 23:26:33 jdaniel Exp $
 *
 * Date         Author  Changes
 */

package tyrex.recovery;

/**
 * This class is a log timer. At 0:0 it changes the log.
 * 
 * @author <a href="mailto:jdaniel@intalio.com">Jerome Daniel &lt;daniel@intalio.com&gt;</a>
 * @version $Revision: 1.1 $ $Date: 2001/01/11 23:26:33 $ 
 */
public class LogTimer extends java.lang.Thread
{
	/**
	 * Reference to the launch day
	 */
	private int _day;
	
	/**
	 * Constructor
	 */	
	public LogTimer( int day )
	{
		_day = day;
	}
	
	/**
	 * Thread entry point
	 */
	public void run()
	{
		boolean time_ok = false;
		
		while ( ! time_ok )
		{
			long wait_in_seconds = getWaitUntilMidnight();
			
			try
			{
				sleep( wait_in_seconds * 1000 );
			}
			catch ( java.lang.InterruptedException ex )
			{ }
			
			//
			// Check if day has changes
			//
			if ( getCurrentDay() != _day )
				time_ok = true;
			
			//
			// If day is ok, change log
			//
			if ( time_ok )
				tyrex.recovery.LogWriter.out.changeLog();
		}
	}
	
	/**
	 * This operation returns in seconds the delay until midnight.
	 */
	private long getWaitUntilMidnight()
	{
		java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
		
		if ( _day != getCurrentDay() )
			return 0;
		
		int _second = calendar.get( java.util.Calendar.SECOND );
		int _minute = calendar.get( java.util.Calendar.MINUTE );
		int _hour = calendar.get( java.util.Calendar.HOUR_OF_DAY );
		
		long delay_second = 60 - _second;
		
		long delay_minute = ( 60 - _minute ) * 60 ;
		
		long delay_hour = ( 24 - _hour ) * 3600;
		
		return delay_hour + delay_minute + delay_second;
	}
	
	/**
	 * Return the current day
	 */
	public int getCurrentDay()
	{
		java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
		
		return calendar.get( java.util.Calendar.DAY_OF_MONTH );
	}
}

