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
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: Meter.java,v 1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.server;


import java.io.PrintWriter;
import java.security.AccessController;
import tyrex.util.PoolManager;


/**
 * Provides a mechanism to obtain activity information from the
 * transaction server. A meter object is constructed and will always
 * talk to the transaction server running in the same JVM. It can be
 * used to obtain information such as:
 * <ul>
 * <li>The number of active transactions and ratio relative to upper
 *   limit
 * <li>The number of available transactions (active and pooled) and
 *   ration relative to upper limit
 * <li>The number of transactions created so far
 * <li>The number of transactions terminated so far
 * </ul>
 * This information relates only to top level and imported transactions.
 * Subtransactions in nested transactions are not counted.
 * <p>
 * For security reasons, creation of a new meter requires the {@link
 * TransactionServerPermission.Server#Meter} permission.
 * <p>
 * For convenience in debugging, a constructor is provided that will
 * run a background thread dumping activity information to a specified
 * writer.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/11 00:33:46 $
 */
public final class Meter
    extends Thread
{


    /**
     * We need an instance of the transaction server to access some of
     * its information (the pool counters) that are only available from
     * the instance, not statically.
     */
    private TransactionServer  _txServer;


    /**
     * The pool manager used with the transaction server.
     */
    private PoolManager        _poolManager;


    /**
     * A writer to write activity information to, if requested to run
     * with a dumping background thread.
     */
    private PrintWriter        _writer;


    /**
     * The delay between writes to the writer specified in milliseconds.
     */
    private int                _delay;



    /**
     * Construct a new meter object that can retrieve information from
     * the transaction server running in this JVM. Requires the
     * {@link TransactionServerPermission.Server#Meter} permission.
     */
    public Meter()
    {
	// Requires permission to create a new meter.
	AccessController.checkPermission( TransactionServerPermission.Server.Meter );
	_txServer = TransactionServer.getInstance();
	_poolManager = _txServer.getConfigure().getPoolManager();
    }


    /**
     * Construct a new meter object that can retrieve information from
     * the transaction server running in this JVM. Requires the
     * {@link TransactionServerPermission.Server#Meter} permission.
     * A background thread will dump information to the specified
     * <tt>writer</tt> every <tt>delay</tt> milliseconds.
     * <p>
     * To stop the background running thread simply call
     * <tt>interrupt</tt> on this meter.
     *
     * @param writer Writer to dump activity report to
     * @param delay Delay between dumps in milliseconds
     */
    public Meter( PrintWriter writer, int delay )
    {
	this();
	_writer = writer;
	_delay = delay;
	setPriority( Thread.MIN_PRIORITY );
	setDaemon( true );
	setName( "Transaction Server Meter" );
	start();
    }


    /**
     * Returns a count of how many transaction are presently active.
     */
    public int getActiveCount()
    {
	return _txServer.getActiveCount();
    }


    /**
     * Returns a percentage of how many transaction are presently active,
     * out of the upper limit on active transactions. Return value is
     * between 0.0 and 1.0.
     */
    public float getActiveCountPct()
    {
	return (float) _txServer.getActiveCount() / (float) _poolManager.getActiveLimit();
    }


    /**
     * Returns a count of how many transactions are listed with the
     * transaction server (active and pooled).
     */
    public int getTotalCount()
    {
	return ( _txServer.getActiveCount() + _txServer.getPooledCount() );
    }


    /**
     * Returns a percentage of how many transactions are presently
     * listed with the transaction server (active and pooled) out of
     * the upper limit. Return value is between 0.0 and 1.0.
     */
    public float getTotalCountPct()
    {
	return (float) ( _txServer.getActiveCount() + _txServer.getPooledCount() ) /
	       (float) _poolManager.getUpperLimit();
    }


    /**
     * Returns a count of how many transactions have been created in
     * the life time of the server.
     */
    public long getCreatedCounter()
    {
	return TransactionServer.getCreatedCounter();
    }


    /**
     * Returns a counts of how many transactions have been forcefully
     * terminated in the life time of the server.
     */
    public long getTerminatedCounter()
    {
	return TransactionServer.getTerminatedCounter();
    }


    /**
     * Returns the value as a percentage readout with exactly one
     * digit after the decimal point (#.#%).
     */
    private String toPercent( float value )
    {
	int round;
	round = (int) ( value * 1000 );
	return ( round / 10 ) + "." + (round % 10 );
    }


    public void run()
    {
	long createdLast = 0;
	long terminatedLast = 0;

	try {
	    while ( true ) {
		sleep( _delay );
		_writer.println( "ACTIVE " + getActiveCount() + " / " +
				 toPercent( getActiveCountPct() ) + "%  " +
				 "POOL " + getTotalCount() + " / " + toPercent( getTotalCountPct() ) + "%  " +
				 "CREATED " + ( getCreatedCounter() - createdLast ) + "  " +
				 "KILLED " + ( getTerminatedCounter() - terminatedLast ) );
		createdLast = getCreatedCounter();
		terminatedLast = getTerminatedCounter();
	    }
	} catch ( InterruptedException except ) { }
    }



}





