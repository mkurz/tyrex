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
 * $Id: EnlistedConnection.java,v 1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.jdbc;


import java.util.*;
import java.sql.*;
import javax.transaction.TransactionManager;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.xa.XAResource;
import tyrex.server.ResourceManager;
import tyrex.server.EnlistedResource;


/**
 * Encapsulates an application's view of a connection. The connection
 * is obtained from {@link ServerDataSource} and automatically
 * enlisted with the transaction manager whenever it is used. This
 * connection implementation is suitable for EJB beans which will
 * reuse it across transactions and in different method invocations
 * (i.e. dissociated threads). It implements a mechanism for
 * automatically registering the connection with the current
 * transaction when the connection is used. This connection is only
 * usable inside the transaction manager.
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version 1.0
 * @see ServerDataSource
 */
public class EnlistedConnection
    implements Connection, EnlistedResource
{


    /**
     * The underlying connection against which all JDBC operations
     * are performed.
     */
    private Connection        _underlying;


    /**
     * The XA resource associated with the underlying connection
     * that is registered with the current transaction.
     */
    private XAResource        _xaRes;


    /**
     * True if the connection has been enlisted with the
     * transaction. Reset with call to {@link #delisted}.
     */
    private boolean           _enlisted;



    /**
     * Constructs a new connection with the underlying JDBC
     * connection and the associated XA resource.
     */
    EnlistedConnection( Connection underlying, XAResource xaRes )
    {
	_underlying = underlying;
	_xaRes = xaRes;
    }


    public Statement createStatement()
        throws SQLException
    {
	return getUnderlying().createStatement();
    }


    public Statement createStatement( int resultSetType, int resultSetConcurrency )
        throws SQLException
    {
	return getUnderlying().createStatement( resultSetType, resultSetConcurrency );
    }


    public PreparedStatement prepareStatement( String sql )
        throws SQLException
    {
	return getUnderlying().prepareStatement( sql );
    }


    public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency )
        throws SQLException
    {
	return getUnderlying().prepareStatement( sql, resultSetType, resultSetConcurrency );
    }


    public CallableStatement prepareCall( String sql )
        throws SQLException
    {
	return getUnderlying().prepareCall( sql );
    }


    public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency )
        throws SQLException
    {
	return getUnderlying().prepareCall( sql, resultSetType, resultSetConcurrency );
    }


    public String nativeSQL( String sql )
        throws SQLException
    {
	return getUnderlying().nativeSQL( sql );
    }


    public DatabaseMetaData getMetaData()
        throws SQLException
    {
	return getUnderlying().getMetaData();
    }


    public void setCatalog( String catalog )
        throws SQLException
    {
	getUnderlying().setCatalog( catalog );
    }


    public String getCatalog()
        throws SQLException
    {
	return getUnderlying().getCatalog();
    }


    public SQLWarning getWarnings()
        throws SQLException
    {
	return getUnderlying().getWarnings();
    }


    public void clearWarnings()
        throws SQLException
    {
	getUnderlying().clearWarnings();
    }


    public Map getTypeMap()
        throws SQLException
    {
	return getUnderlying().getTypeMap();
    }


    public void setTypeMap( Map map )
        throws SQLException
    {
	getUnderlying().setTypeMap( map );
    }


    public void setAutoCommit( boolean autoCommit )
        throws SQLException
    {
	getUnderlying().setAutoCommit( autoCommit );
    }


    public boolean getAutoCommit()
        throws SQLException
    {
	return getUnderlying().getAutoCommit();
    }


    public void commit()
        throws SQLException
    {
	getUnderlying().commit();
    }



    public void rollback()
        throws SQLException
    {
	getUnderlying().rollback();
    }


    public void setReadOnly( boolean readOnly )
        throws SQLException
    {
	getUnderlying().setReadOnly( readOnly );
    }


    public boolean isReadOnly()
        throws SQLException
    {
	return getUnderlying().isReadOnly();
    }
    

    public void setTransactionIsolation( int level )
        throws SQLException
    {
	getUnderlying().setTransactionIsolation( level );
    }


    public int getTransactionIsolation()
        throws SQLException
    {
	return getUnderlying().getTransactionIsolation();
    }


    public synchronized void close()
	throws SQLException
    {
	_underlying.close();
	_underlying = null;
	_xaRes = null;
    }


    public synchronized boolean isClosed()
    {
	return ( _underlying == null );
    }


    protected void finalize()
	throws Throwable
    {
	close();
    }


    public String toString()
    {
	if ( _underlying == null )
	    return "Connection closed";
	else
	    return _underlying.toString();
    }


    public void delisted()
    {
	_enlisted = false;
    }


    /**
     * Called to retrieve the underlying JDBC connection. Actual JDBC
     * operations are performed against it. Throws an SQLException if
     * this connection has been closed.
     */
    Connection getUnderlying()
        throws SQLException
    {
	if ( ! _enlisted  ) {
	    try {
		ResourceManager.enlistResource( _xaRes, this );
		_enlisted = true;
	    } catch ( RollbackException except ) {
		throw new SQLException( except.getMessage() );
	    } catch ( SystemException except ) {
		throw new SQLException( except.toString() );
	    }
	}
	return _underlying;
    }


}


