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
 * $Id: EnlistedConnection.java,v 1.10 2001/02/27 00:34:07 arkin Exp $
 */


package tyrex.jdbc;


import java.util.*;
import java.sql.*;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.xa.XAResource;


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
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version 1.0
 * @see ServerDataSource
 *
 * Date         Author          Changes
 * ?            Assaf Arkin     Created  
 * Aug 8 2000   Riad Mohammed   Added changes that prevents
 *                              underlying connection from
 *                              being transactional.
 */
public class EnlistedConnection
    extends AbstractTyrexConnectionImpl
    implements Connection
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
     * Constructs a new connection with the underlying JDBC
     * connection and the associated XA resource.
     * <p>
     * Assumes that the underlying connection does not auto-commit
     *
     * @param underlying the underlying connection
     * @param xaRes the XA resource associated with the
     *      underlying connection.
     * @throws SQLException if the auto-commit cannot be
     *      turned off in the underlying connection.
     */
    public EnlistedConnection( Connection underlying, XAResource xaRes )
        throws SQLException
    {
	_underlying = underlying;
	_xaRes = xaRes;
    }


    public void setAutoCommit( boolean autoCommit )
        throws SQLException
    {
        throw new SQLException("SetAutoCommit not supported in enlisted connections.");
	//getUnderlying().setAutoCommit( autoCommit );
    }


    public boolean getAutoCommit()
        throws SQLException
    {
    return false;
    
    //return getUnderlying().getAutoCommit();
    }


    public void commit()
        throws SQLException
    {
    throw new SQLException("Commit not supported in enlisted connections.");
    
    //getUnderlying().commit();
    }



    public void rollback()
        throws SQLException
    {
    throw new SQLException("Rollback not supported in enlisted connections.");
    
    //getUnderlying().rollback();
    }


    protected void internalClose()
	throws SQLException
    {
        if (!isClosed()) {
            _underlying.close();
            _underlying = null;
            _xaRes = null;        
        }
    }


    public synchronized boolean isClosed()
    {
	return ( _underlying == null );
    }


    public String toString()
    {
	if ( _underlying == null )
	    return "Connection closed";
	else
	    return _underlying.toString();
    }


    /**
     * Called to retrieve the underlying JDBC connection. Actual JDBC
     * operations are performed against it. Throws an SQLException if
     * this connection has been closed or there is a problem enlisting the
     * the underlying connection with the resource manager.
     */
    protected Connection internalGetUnderlyingConnection()
        throws SQLException
    {
	return _underlying;
    }


}


