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
 * $Id: TransactionFactoryImpl.java,v 1.3 2001/01/11 23:26:33 jdaniel Exp $
 */


package tyrex.tm;


import java.util.Properties;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.ORB;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions._TransactionFactoryImplBase;


/**
 * Implements an OTS transaction factory and identification interfaces.
 * Allows the creation of new OTS transactions as well as the importing
 * of remote transactions.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2001/01/11 23:26:33 $
 * @see TransactionImpl
 *
 * Changes 
 *
 * J. Daniel : Changed code to be compliant with CORBA developing rules.
 */
public final class TransactionFactoryImpl
    extends _TransactionFactoryImplBase    
{

    /**
     * To be used as a CORBA object, the control object must be
     * activated by  its object adapter. In this implementation, we are
     * using BOA. To provide more flexibility we only used the ORB interface
     * to connect and disconnect CORBA objects.
     */
    private org.omg.CORBA.ORB _orb;
    
    /**
     * The transaction domain to which this factory belongs.
     */
    private TransactionDomain  _txDomain;


    public TransactionFactoryImpl( TransactionDomain txDomain, org.omg.CORBA.ORB orb )
    {
	if ( txDomain == null )
	    throw new IllegalArgumentException( "Argument 'txDomain' is null" );
	_txDomain = txDomain;
        _orb = orb;
    }


    public Control create( int timeout )
    {
	TransactionImpl tx;

	// Create a new transaction and return the control
	// interface of that transaction.
	try {
	    tx = _txDomain.createTransaction( null, null );
            
            // <---------- CORBA Part ----------->
            if ( _orb != null )
                tx.setORB( _orb );            
            // </---------- CORBA Part ----------->
            
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
            
            // <---------- CORBA Part ----------->
            if ( _orb != null )
                tx.setORB( _orb );
            // </---------- CORBA Part ----------->
	    
            return tx.getControl();
	} catch ( Exception except ) {
	    throw new INVALID_TRANSACTION();
	}
    }
     
}
