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
 * $Id: RemoteUserTransaction.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.client;


import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.transaction.*;
import javax.transaction.xa.Xid;


/**
 * Remote interface for {@link UserTransaction} allows a client to
 * demarcate server transactions given only the transaction's global
 * identifier. See {@link ClientUserTransaction} for typical usage
 * scenario.
 * <p>
 * This interface is modeled after {@link UserTransaction} except
 * that a global identifier is returned from {@link #begin} and is
 * required on all subsequent method calls. The global identifier is
 * required as the remote server cannot associate the transaction with
 * a local thread of execution.
 * <p>
 * For efficiency in transport, only the global identifier is passed
 * on method calls. The global identifier is a sequence of bytes
 * containing sufficient information to locate the transaction.
 * Currently it is 12 bytes long, but the actual size may vary in
 * future versions.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see UserTransaction
 */
public interface RemoteUserTransaction
    extends Remote
{

    
    public byte[] begin()
	throws SystemException, RemoteException;


    public void commit( byte[] gxid )
	throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	       IllegalStateException, SystemException, RemoteException;

    public void rollback( byte[] gxid )
	throws IllegalStateException, SystemException, RemoteException;


    public void setRollbackOnly( byte[] gxid )
	throws IllegalStateException, SystemException, RemoteException;


    public int getStatus( byte[] gxid )
	throws SystemException, RemoteException;


    public void setTransactionTimeout( byte[] gxid, int seconds )
	throws SystemException, RemoteException;


    /**
     * This is the name by which the remote user transaction should
     * be bound to an RMI, JNDI or COS lookup within their respective
     * contexts.
     */
    public static String  LOOKUP_NAME = "/comp/RemoteUserTransaction";


}

