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
 * $Id: ConnectorInfo.java,v 1.2 2000/09/08 23:04:44 mohammed Exp $
 */


package tyrex.connector.manager;


import java.io.PrintWriter;
import java.util.Vector;
import java.util.Hashtable;
import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ManagedConnectionFactory;
import tyrex.connector.ConnectionManager;
import tyrex.connector.ConnectionException;
import tyrex.connector.SynchronizationResource;
import tyrex.util.Messages;


/**
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/09/08 23:04:44 $
 */
public class ConnectorInfo
{


    public static class TransactionType
    {

        public static final TransactionType None =
            new TransactionType( "none", 0 );

        
        public static final TransactionType XA =
            new TransactionType( "X/A", 1 );


        public static final TransactionType XA1PC =
            new TransactionType( "X/A one-phase", 2 );


        public static final TransactionType Local =
            new TransactionType( "Local", 3 );


        private final String _name;

        private int          _code;

        TransactionType( String name, int code )
        {
            _name = name;
            _code = code;
        }

        int getCode()
        {
            return _code;
        }

        public String toString()
        {
            return _name;
        }

    }


    private TransactionType  _txType;


    private boolean          _share;


    public void setTransactionType( TransactionType txType )
    {
        _txType = txType;
    }


    public TransactionType getTransactionType()
    {
        return ( _txType == null ? TransactionType.None : _txType );
    }


    public void setShareConnections( boolean share )
    {
        _share = share;
    }


    public boolean getShareConnections()
    {
        return _share;
    }



}




