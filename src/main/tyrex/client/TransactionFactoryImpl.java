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
 * $Id: TransactionFactoryImpl.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.client;


import java.util.Properties;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TSIdentification;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions._TransactionFactoryImplBase;
import org.omg.CosTransactions.TransactionFactoryHelper;
import org.omg.CosTSPortability.Sender;
import org.omg.CosTSPortability.Receiver;
import javax.jts.TransactionService;


/**
 * Implements the client side OTS transaction factory and identification
 * interfaces. Allows the creation of new OTS transactions as well as the
 * importing of remote transactions. The identification interface allows the
 * transaction server to be registered as a service with any ORB.
 * <p>
 * The client implementation creates transactions on the remote server,
 * see {@link CurrentImpl} for more details.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see CurrentImpl
 */
public final class TransactionFactoryImpl
    extends _TransactionFactoryImplBase
    implements TransactionService
{


    private static TransactionFactory  _remoteFactory;


    public static String               _remoteObjectId;


    TransactionFactoryImpl()
    {
	synchronized ( getClass() ) {
	    if ( _remoteFactory == null ) {
		ORB                  orb;
		org.omg.CORBA.Object obj;
		
		try {
		    orb = ORB.init();
		    if ( _remoteObjectId == null ) {
			throw new RuntimeException( "Remote transaction server not identified" );
		    }
		    obj = orb.string_to_object( _remoteObjectId );
		    _remoteFactory = TransactionFactoryHelper.narrow( obj );
		} catch ( Exception except ) {
		    System.out.println( except );
		    except.printStackTrace();
		}
	    }
	}
    }


    public Control create( int timeout )
    {
	return _remoteFactory.create( timeout );
    }


    public Control recreate( PropagationContext pgContext )
    {
	return _remoteFactory.recreate( pgContext );
    }


    public void identifyORB( ORB orb, TSIdentification tsi, Properties prop )
    {
	CurrentImpl current;

	try {
	    current = new CurrentImpl();
	    tsi.identify_sender( (Sender) current );
	    tsi.identify_receiver( (Receiver) current );
	} catch ( Exception except ) {
	    // The ORB might tell us it's already using some sender/reciever,
	    // or any other error we are not interested in reporting back
	    // to the caller (i.e. the ORB).
	    //TransactionServer.logMessage( except.toString() );
	}
    }


}
