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
 * $Id: Test.java,v 1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.client;


import java.io.PrintWriter;
import java.rmi.*;
import javax.transaction.*;
import tyrex.server.*;
import tyrex.util.Logger;
import org.exolab.testing.Timing;


/**
 * Performs a simple test of starting and demarcating a transaction on
 * a remote server. This test runs the client and remote user transaction
 * in the same JVM, but employes RMI for remote communications between
 * the two.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/11 00:33:46 $
 */
public class Test
{


    public static void main( String args[] )
    {
	PrintWriter                writer;

	writer = Logger.getLogger();

	//System.setProperty( "java.rmi.server.logCalls", "true" );

	try {
	    RemoteUserTransactionImpl  remoteTxImpl;
	    ClientUserTransaction      clientTx;
	    byte[]                     gxid;


	    // Create the RemoteUserTranaction implementation
	    // (in fact, the transaction server) and register
	    // it in the RMI registry. (For simplicity, I don't
	    // use JNDI in this test)
	    System.out.println( "Creating RemoteUserTransaction" );
	    remoteTxImpl = new RemoteUserTransactionImpl();
	    System.out.println( "Registering RemoteUserTransaction" );
	    Naming.rebind( RemoteUserTransaction.LOOKUP_NAME, remoteTxImpl );
	    
	    // From the client side create a new ClientUserTransaction
	    // that will automatically attempt to obtain the remote
	    // user transaction registered before.
	    System.out.println( "Obtaining new ClientUserTransaction" );
	    clientTx = new ClientUserTransaction();

	    // Begin a new transaction, display the transaction
	    // identifier, commit the transaction.
	    System.out.println( "Creating transaction on remote server" );
	    clientTx.begin();
	    gxid = clientTx.getGlobalXid();
	    System.out.println( "Commiting transaction" );
	    clientTx.commit();

	    // Begin a new transaction, display the transaction
	    // identifier, commit the transaction.
	    System.out.println( "Creating transaction on remote server" );
	    clientTx.begin();
	    gxid = clientTx.getGlobalXid();

	    /*
	    Tyrex.dumpTransactionList( writer );
	    clientTx.setTransactionTimeout( 1 );
	    try {
		Thread.sleep( 2000 );
	    } catch ( InterruptedException except ) {}
	    */

	    System.out.println( "Aborting transaction" );
	    clientTx.rollback();


	    Timing timing;

	    timing = new Timing( "Client empty transaction performance" );
	    timing.start();
	    for ( int i = 0 ; i < 10000 ; ++i ) {
		clientTx.begin();
		clientTx.commit();
	    }
	    timing.stop();
	    timing.count( 10000 );
	    System.out.println( timing.report() );

	    Tyrex.dumpTransactionList( writer );


	} catch ( Exception except ) {
	    System.out.println( except );
	    except.printStackTrace();
	    Tyrex.dumpTransactionList( writer );
	}
	// All well, we need to terminate explicitly otherwise the
	// transaction server is running in a non-daemon thread and
	// we'll be hanging forever.
	System.exit( 0 );
    }


}
