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
 *    permission of Intalio Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Technologies. Exolab is a registered
 *    trademark of Intalio Technologies.
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
 * Copyright 1999-2001 (C) Intalio Technologies Inc. All Rights Reserved.
 *
 */


package tyrex.resource.jdbc.xa;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Map;


/////////////////////////////////////////////////////////////////////
// AbstractTyrexConnectionImpl
/////////////////////////////////////////////////////////////////////


/**
 * This class defines base methods for implementing java.sql.Connection
 * so that an underlying java.sql.Connection may be pooled.
 * <P>
 * Subclasses are to implement {@link #isClosed}, {@link #close}, 
 * {@ #getUnderlyingConnection}. 
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public abstract class TyrexConnection
    implements Connection
{


    public TyrexConnection()
    {
    }

    
    public synchronized Statement createStatement()
        throws SQLException
    {
        try {
            return new TyrexStatementImpl(getUnderlyingConnection().createStatement(), this);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized Statement createStatement(int resultSetType, int resultSetConcurrency)
        throws SQLException
    {
        try {
            return new TyrexStatementImpl(getUnderlyingConnection().createStatement(resultSetType, resultSetConcurrency),
                                      this);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized PreparedStatement prepareStatement(String sql)
        throws SQLException
    {
        try {
            return new TyrexPreparedStatementImpl(getUnderlyingConnection().prepareStatement(sql), this);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
        throws SQLException
    {
        try {
            return new TyrexPreparedStatementImpl(getUnderlyingConnection().prepareStatement(sql, resultSetType, resultSetConcurrency),
                                              this);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized CallableStatement prepareCall(String sql)
        throws SQLException
    {
        try {
            return new TyrexCallableStatementImpl(getUnderlyingConnection().prepareCall(sql),
                                              this);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
        throws SQLException
    {
        try {
            return new TyrexCallableStatementImpl(getUnderlyingConnection().prepareCall(sql, resultSetType, resultSetConcurrency),
                                              this);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized String nativeSQL(String sql)
        throws SQLException
    {
        try {
            return getUnderlyingConnection().nativeSQL(sql);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized DatabaseMetaData getMetaData()
        throws SQLException
    {
        try {
            return getUnderlyingConnection().getMetaData();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized void setCatalog( String catalog )
        throws SQLException
    {
        try {
            getUnderlyingConnection().setCatalog( catalog );
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized String getCatalog()
        throws SQLException
    {
        try {
            return getUnderlyingConnection().getCatalog();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized SQLWarning getWarnings()
        throws SQLException
    {
        try {
            return getUnderlyingConnection().getWarnings();
            }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized void clearWarnings()
        throws SQLException
    {
        try {
            getUnderlyingConnection().clearWarnings();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized Map getTypeMap()
        throws SQLException
    {
        try {
            return getUnderlyingConnection().getTypeMap();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized void setTypeMap(Map map)
        throws SQLException
    {
        try {
            getUnderlyingConnection().setTypeMap(map);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized void setAutoCommit(boolean autoCommit)
        throws SQLException
    {
        try {
            getUnderlyingConnection().setAutoCommit(autoCommit);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized boolean getAutoCommit()
        throws SQLException
    {
        try {
            return getUnderlyingConnection().getAutoCommit();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized void commit()
        throws SQLException
    {
        try {
            getUnderlyingConnection().commit();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }



    public synchronized void rollback()
        throws SQLException
    {
        try {
            getUnderlyingConnection().rollback();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized void setReadOnly(boolean readOnly)
        throws SQLException
    {
        try {
            getUnderlyingConnection().setReadOnly(readOnly);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized boolean isReadOnly()
        throws SQLException
    {
        try {
            return getUnderlyingConnection().isReadOnly();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }
    

    public synchronized void setTransactionIsolation(int level)
        throws SQLException
    {
        try {
            getUnderlyingConnection().setTransactionIsolation(level);
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    public synchronized int getTransactionIsolation()
        throws SQLException
    {
        try {
            return getUnderlyingConnection().getTransactionIsolation();
        }
        catch(SQLException e) {
            notifyError(e);

            throw e;
        }
    }


    /**
     * Called when an exception is thrown by the underlying connection.
     * <P>
     * The default implementation is to do nothing
     *
     * @param except The exception thrown by the underlying
     *   connection
     */
    protected void notifyError(SQLException exception)
    {

    }


    /**
     * Close this connection which may or may not close the
     * underlying connection. And notify any listeners that
     * the connection has been closed.
     *
     * @throws SQLException if there is a problem closing the connection 
     * @see #internalClose
     */
    public synchronized void close()
        throws SQLException
    {
        try {
            internalClose();
        }
        catch(SQLException e) {
            notifyError(e);
            throw e;
        }
    }


    /**
     * Method that actually closes the connection.
     *
     * @throws SQLException if there is a problem closing the connection
     * @see #close
     */
    protected abstract void internalClose()
        throws SQLException;

    
    /**
     * Return true if the connection is closed.
     *
     * @return true if the connection is closed.
     */
    public abstract boolean isClosed();
    

    /**
     * Close the connection when it is being garbage collected.
     */
    protected void finalize()
        throws Throwable
    {
        if (!isClosed())
            close();
    }
    

    /**
     * Return the underlying connection.
     *
     * @return the underlying connection
     * @throws SQLException if the connection is closed 
     *      or cannot be retrieved.
     * @see #internalGetUnderlyingConnection
     */
    private Connection getUnderlyingConnection()
        throws SQLException
    {
        if (isClosed()) {
            throw new SQLException("The connection is closed.");    
        }

        return internalGetUnderlyingConnection();
    }


    /**
     * Return the underlying connection.
     * <P>
     * The connection is not closed ie {@link #isClosed} returns
     * false.
     *
     * @return the underlying connection
     * @throws SQLException if the connection cannot be retrieved.
     * @see #getUnderlyingConnection
     */
    protected abstract Connection internalGetUnderlyingConnection()
        throws SQLException;
    

}
