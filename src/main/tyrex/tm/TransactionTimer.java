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
 * $Id: TransactionTimer.java,v 1.1 2001/02/09 20:46:49 jdaniel Exp $
 */

package tyrex.tm;


/**
 * Each transaction domain (see {@link TransactionDomain}) is associated
 * with a transaction timer. A transaction timer manages transaction
 * time out.
 *
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @see TransactionDomain
 */
public class TransactionTimer extends java.lang.Thread
{
    private final int WAIT_TIME = 5;
    
    private java.util.Hashtable _txList;
    
    public TransactionTimer()
    {
        _txList = new java.util.Hashtable();
    }
    
    /**
     * Thread entry point. This method simply checks ( after a pause of
     * WAIT_TIME seconds ) if some transaction manages by the associated
     * transaction domain must be rolledback.
     */
    public void run()
    {
        while ( true )
        {
            try
            {
                // wait WAIT_TIME seconds                
                java.lang.Thread.currentThread().sleep( WAIT_TIME * 1000 );
                
                // check the transaction to cancel
                checkTimeOut();
            }
            catch ( java.lang.Exception ex )
            { }
        }        
    }
    
    /**
     * This operation is used to register a new transaction
     */
    public void register( TransactionImpl tx, long timeout )
    {
        TimeHolder th = new TimeHolder();
        th.tx = tx;
        th.deadline = System.currentTimeMillis() + timeout;
        _txList.put( tx, th );
    }
    
    /**
     * This operation unregisters a transaction. It means that the
     * transaction will be no more used for the time out checking
     */
    public void unregister( TransactionImpl tx )
    {
        _txList.remove( tx );
    }
    
    /**
     * This operation is used to check if some transaction timeout
     * is reached. In this case, the transaction is marked as
     * rollback only.
     */
    private void checkTimeOut()
    {
        long time = System.currentTimeMillis();
        java.util.Enumeration enum = _txList.elements();
        
        while ( enum.hasMoreElements() )
        {
            try
            {
                TimeHolder th = ( TimeHolder ) enum.nextElement();
            
                if ( th.deadline >= time )
                {
                    th.tx.setRollbackOnly();
                    _txList.remove( th.tx );
                }
            }
            catch ( java.lang.Exception ex )
            { }
        }
    }
    
    /**
     * Time holder are stored by the timer to maintain information
     * about a transaction.
     */
    public static class TimeHolder
    {
        public TransactionImpl tx;
        
        public long deadline;
    }
}