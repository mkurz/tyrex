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
 * $Id: TransactionFactoryImpl.java,v 1.2 2000/09/08 23:06:13 mohammed Exp $
 */


package tyrex.tm;


import java.util.Properties;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TSIdentification;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions._TransactionFactoryImplBase;
import org.omg.CosTSPortability.Sender;
import org.omg.CosTSPortability.Receiver;
import javax.jts.TransactionService;


/**
 * Implements an OTS transaction factory and identification interfaces.
 * Allows the creation of new OTS transactions as well as the importing
 * of remote transactions. The identification interface allows the
 * transaction server to be registered as a service with any ORB.
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/09/08 23:06:13 $
 * @see TransactionImpl
 */
public final class TransactionFactoryImpl
    extends _TransactionFactoryImplBase
    implements TransactionService
{


    /**
     * The transaction domain to which this factory belongs.
     */
    private TransactionDomain  _txDomain;


    TransactionFactoryImpl( TransactionDomain txDomain )
    {
	if ( txDomain == null )
	    throw new IllegalArgumentException( "Argument 'txDomain' is null" );
	_txDomain = txDomain;
    }


    public Control create( int timeout )
    {
	TransactionImpl tx;

	// Create a new transaction and return the control
	// interface of that transaction.
	try {
	    tx = _txDomain.createTransaction( null, null );
	    return tx.getControl();
	} catch ( Exception except ) {
	    throw new INVALID_TRANSACTION();
	}
    }


    public Control recreate( PropagationContext pgContext )
    {
	TransactionImpl tx;

	try {
	    tx = _txDomain.recreateTransaction( pgContext );
	    return tx.getControl();
	} catch ( Exception except ) {
	    throw new INVALID_TRANSACTION();
	}
    }


    public void identifyORB( ORB orb, TSIdentification tsi, Properties prop )
    {
	CurrentImpl current;

	try {
	    current = new CurrentImpl( this, (TransactionManagerImpl) _txDomain.getTransactionManager() );
	    tsi.identify_sender( (Sender) current );
	    tsi.identify_receiver( (Receiver) current );
	} catch ( Exception except ) {
	    // The ORB might tell us it's already using some sender/reciever,
	    // or any other error we are not interested in reporting back
	    // to the caller (i.e. the ORB).
	    _txDomain.logMessage( except.toString() );
	}
    }


}
