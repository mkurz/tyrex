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
 * $Id: JDBCManagedConnection.java,v 1.2 2000/04/13 22:06:00 arkin Exp $
 */


package tyrex.connector.jdbc;


import java.io.PrintWriter;
import java.util.Properties;
import java.sql.SQLException;
import java.sql.Connection;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ConnectionException;
import tyrex.connector.ConnectionEventListener;
import tyrex.connector.SynchronizationResource;


/**
 * An adapter for JDBC XAConnection interface to expose
 * it as a managed connection.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/04/13 22:06:00 $
 */
public class JDBCManagedConnection
    implements ManagedConnection, javax.sql.ConnectionEventListener
{


    private final XAConnection            _xaConnection;


    private Connection                    _connection;


    private final SynchronizationResource _syncResource;


    private final XAResource              _xaResource;


    private final JDBCConnectionInfo      _info;


    private ConnectionEventListener[]     _listeners;



    public JDBCManagedConnection( XAConnection xaConnection, JDBCConnectionInfo info )
        throws ConnectionException
    {
        _xaConnection = xaConnection;
        _xaConnection.addConnectionEventListener( this );
        _info = info;
        try {
            _connection = _xaConnection.getConnection();
            _xaResource = _xaConnection.getXAResource();
            _syncResource = null;
        } catch ( SQLException except ) {
            throw new ConnectionException( except );
        }
    }


    boolean isSameInfo( JDBCConnectionInfo info )
    {
        return ( ( info == null && _info == null ) ||
                 ( info != null && info.equals( _info ) ) );
    }


    //-------------------//
    // ManagedConnection //
    //-------------------//


    public synchronized void addConnectionEventListener( ConnectionEventListener listener )
    {
        if ( listener == null )
            throw new IllegalArgumentException( "Argument 'listener' is null" );
        if ( _listeners == null ) {
            _listeners = new ConnectionEventListener[ 1 ];
            _listeners[ 0 ] = listener;
        } else {
            ConnectionEventListener[] newListeners;

            // Make sure same listener is not registered twice
            for ( int i = 0 ; i < _listeners.length ; ++i )
                if ( _listeners[ i ] == listener )
                    return;
            newListeners = new ConnectionEventListener[ _listeners.length + 1 ];
            for ( int i = 0 ; i < _listeners.length ; ++i )
                newListeners[ i ] = _listeners[ i ];
            newListeners[ _listeners.length ] = listener;
            _listeners = newListeners;
        }
    }


    public synchronized void removeConnectionEventListener( ConnectionEventListener listener )
    {
        if ( listener == null )
            throw new IllegalArgumentException( "Argument 'listener' is null" );
        // Do nothing if listener not registered
        if ( _listeners == null )
            return;
        if ( _listeners.length == 1 ) {
            if ( _listeners[ 0 ] == listener )
                _listeners = null;
            return;
        }
        for ( int i = 0 ; i < _listeners.length ; ++i )
            if ( _listeners[ i ] == listener ) {
                ConnectionEventListener[] newListeners;

                _listeners[ i ] = _listeners[ _listeners.length - 1 ];
                newListeners = new ConnectionEventListener[ _listeners.length - 1 ];
                for ( int j = 0 ; j < _listeners.length - 1 ; ++j )
                    newListeners[ j ] = _listeners[ j ];
                _listeners = newListeners;
                return;
            }
    }


    public void pool()
        throws ConnectionException
    {
        if ( _connection == null )
            throw new ConnectionException( "Connection closed" );
        
    }


    public void close()
        throws ConnectionException
    {
        if ( _connection == null )
            throw new ConnectionException( "Connection closed" );

        _xaConnection.removeConnectionEventListener( this );
        try {
            _xaConnection.close();
        } catch ( SQLException except ) {
            throw new ConnectionException( except );
        } finally {
            _connection = null;
        }
    }


    public Object getConnection( Object info )
        throws ConnectionException
    {
        if ( _connection == null )
            throw new ConnectionException( "Connection closed" );
        // Ignore info, only properties affecting connection creation
        // from XAConnection were passed.
        return new JDBCConnectionHandle( this, _connection );
    }


    public XAResource getXAResource()
        throws ConnectionException
    {
        if ( _connection == null )
            throw new ConnectionException( "Connection closed" );
        return _xaResource;
    }


    public SynchronizationResource getSynchronizationResource()
        throws ConnectionException
    {
        if ( _connection == null )
            throw new ConnectionException( "Connection closed" );
        return _syncResource;
    }


    public void connect( Object connection )
        throws ConnectionException
    {
        if ( _connection == null )
            throw new ConnectionException( "Connection closed" );
        if ( connection instanceof JDBCConnectionHandle ) {
            try {
                ( (JDBCConnectionHandle) connection ).connect( _connection );
            } catch ( Exception except ) {
                throw new ConnectionException( except.getMessage() );
            }
        } else
            throw new ConnectionException( "Internal error: Not a ProxyConnection" );
    }


    public PrintWriter getLogWriter()
    {
        // Not supported on XAConnection
        return null;
    }


    public void setLogWriter( PrintWriter logWriter )
    {
        // Not supported on XAConnection
    }


    //-----------------------------------//
    // javax.sql.ConnectionEventListener //
    //-----------------------------------//

    public synchronized void connectionClosed( javax.sql.ConnectionEvent event )
    {
        if ( _listeners != null )
            for ( int i = 0 ; i < _listeners.length ; ++i )
                _listeners[ i ].connectionClosed( this );
    }


    public synchronized void connectionErrorOccurred( javax.sql.ConnectionEvent event )
    {
        if ( _listeners != null )
            for ( int i = 0 ; i < _listeners.length ; ++i )
                _listeners[ i ].connectionErrorOccurred( this, event.getSQLException() );
    }


    public synchronized void notifyClosed()
    {
        if ( _listeners != null )
            for ( int i = 0 ; i < _listeners.length ; ++i )
                _listeners[ i ].connectionClosed( this );
    }


}

