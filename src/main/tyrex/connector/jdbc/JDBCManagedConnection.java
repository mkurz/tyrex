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
 * $Id: JDBCManagedConnection.java,v 1.1 2000/04/10 20:52:34 arkin Exp $
 */


package tyrex.connector.jdbc;


import java.io.PrintWriter;
import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.transaction.xa.XAResource;
import tyrex.connector.ConnectionManager;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ConnectionException;
import tyrex.connector.SynchronizationResource;


/**
 * An adapter for JDBC XAConnection interface to expose
 * it as a managed connection.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/04/10 20:52:34 $
 */
public class JDBCManagedConnection
    implements ManagedConnection, ConnectionEventListener
{


    private final XAConnection _xaConnection;


    private ConnectionManager  _manager;


    public JDBCManagedConnection( XAConnection xaConnection )
    {
        _xaConnection = xaConnection;
        _xaConnection.addConnectionEventListener( this );
    }


    public void setConnectionManager( ConnectionManager manager )
    {
        if ( _manager != null )
            throw new IllegalStateException( "Internal error: ConnectionManager already set for this adapter" );
        _manager = manager;
    }


    public void unsetConnectionManager()
    {
        if ( _manager == null )
            throw new IllegalStateException( "Internal error: No ConnectionManager set for this adapter" );
        _manager = null;
    }


    public short getTransactionType()
    {
        return TRANSACTION_XA;
    }


    public void close()
        throws ConnectionException
    {
        _xaConnection.removeConnectionEventListener( this );
        try {
            _xaConnection.close();
        } catch ( SQLException except ) {
            throw new ConnectionException( except );
        }
    }


    public XAResource getXAResource()
        throws ConnectionException
    {
        try {
            return _xaConnection.getXAResource();
        } catch ( SQLException except ) {
            throw new ConnectionException( except );
        }
    }


    public SynchronizationResource getSynchronizationResource()
        throws ConnectionException
    {
        return null;
    }


    public Object getConnection()
        throws ConnectionException
    {
        try {
            return _xaConnection.getConnection();
        } catch ( SQLException except ) {
            throw new ConnectionException( except );
        }
    }


    public void connectionClosed( ConnectionEvent event )
    {
        if ( _manager != null )
            _manager.connectionClosed( this );
    }


    public void connectionErrorOccurred( ConnectionEvent event )
    {
        if ( _manager != null )
            _manager.connectionErrorOccurred( this, event.getSQLException() );
    }


}

