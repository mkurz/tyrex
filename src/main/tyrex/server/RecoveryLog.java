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
 * $Id: RecoveryLog.java,v 1.4 2000/09/08 23:06:04 mohammed Exp $
 */


package tyrex.server;


import java.io.IOException;
import javax.sql.XADataSource;
import javax.transaction.xa.Xid;


/**
 * Defines an interface to an external recovery log. The recovery
 * log is called to record each transaction that is started,
 * commited or rolled back. During recover the recovery log would
 * list all the active transactions that must be recovered.
 * The recovery log also records all the unique data sources used
 * in these transactions. The data sources are necessary to obtain
 * a working resource in order to rollback prepared transactions
 * (e.g. a JDBC connection and it's XA Resource from a {@link
 * XADAtaSource}).
 * <p>
 * A recovery log object is used either to log or recover, but
 * not both functions at once. Prior to any usage, the log is
 * notified with a call to either {@link #startLogging} or {@link
 * #startRecovery}, allowing it to set up all the necessary
 * resources.
 * <p>
 * The recovery log is only intended to record global transactions
 * running inside this transaction monitor and using local
 * resources. Only the global transaction identifier is required.
 * 
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $ $Date: 2000/09/08 23:06:04 $
 */
public interface RecoveryLog
{


    /**
     * Called once to inform the recovery log we are about
     * to start logging transactions. Gives it a chance to
     * create the relevant resources.
     *
     * @throws IOException The recovery log encountered an
     *   I/O exception with one of its resources
     */
    public void startLogging()
	throws IOException;


    /**
     * Called to log the beginning of a transaction.
     *
     * @param gxid The transaction global identifier
     */
    public void beginTransaction( byte[] gxid );


    /**
     * Called to log the successful completion of a transaction.
     *
     * @param gxid The transaction global identifier
     */
    public void commitTransaction( byte[] gxid );


    /**
     * Called to log the failure of a transaction.
     *
     * @param gxid The transaction global identifier
     */
    public void rollbackTransaction( byte[] gxid );


    /**
     * Called to log the use of an XA data source. Must be
     * called for each unique {@link XADataSource} that is
     * managed by the transaction manager.
     *
     * @param dx The XA data source
     */
    public void addResource( XADataSource ds );


    /**
     * Called once to inform the recovery log we are about
     * to start recovery. Gives it a chance to locate the
     * relevant resources.
     *
     * @throws IOException The recovery log encountered an
     *   I/O exception with one of its resources
     */
    public void startRecovery()
	throws IOException;


    /**
     * Returns a list of resources previously logged with a call
     * to one of the {@link #addResource} methods. They are not
     * returned in any particular order.
     *
     * @return List of resources
     */
    public Object[] listResources()
	throws IOException;


    /**
     * Returns a list of open transactions that should be
     * recovered. These are transactions previously logged
     * at beginning but never logged to commit or rollback.
     * The result is an array of global transaction identifiers.
     * There is no guarantee that any of these transactions
     * is still open and can be recovered.
     *
     * @return List of open transactions
     */
    public byte[][] listTransactions()
	throws IOException;


    /**
     * Return an I/O exception that occured while working
     * with the recovery log.
     */
    public IOException getLastException();


}
