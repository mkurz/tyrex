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
 * Copyright 2000 (C) Intalio Technologies Inc. All Rights Reserved.
 *
 */


package tyrex.resource.jdbc;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;


/////////////////////////////////////////////////////////////////////
// TyrexStatementImpl
/////////////////////////////////////////////////////////////////////


/**
 * This class implements java.sql.Statement so that it is returned
 * when createStatement is called on a {@link TyrexConnection} object.
 * <p>
 * The reason for this class is for the method java.sql.Statement#getConnection
 * to return the Tyrex connection and not the actual underlying connection.
 * <p>
 * This class is thread safe.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
class TyrexStatementImpl 
    implements Statement
{
    /**
     * The connection that created the statement.
     */
    private TyrexConnection      _connection;


    /**
     * The underlying statement
     */
    private Statement           _statement;


    /**
     * The current result set
     */
    private TyrexResultSetImpl _resultSet;


    /**
     * Create the TyrexStatementImpl with the specified arguments.
     *
     * @param statement the underlying statement
     * @param connection the connection that created
     *      the statement.
     * @throws SQLException if there is a problem creating the statement
     */
    TyrexStatementImpl(Statement statement, TyrexConnection connection)
        throws SQLException
    {
        if (null == statement) {
            throw new IllegalArgumentException("The argument 'statement' is null.");
        }
        
        if (null == connection) {
            throw new IllegalArgumentException("The argument 'connection' is null.");
        }

        _connection = connection;
        _statement = statement;
    }

    /**
     * Executes an SQL statement that returns a single <code>ResultSet</code> object.
     *
     * @param sql typically this is a static SQL <code>SELECT</code> statement
     * @return a <code>ResultSet</code> object that contains the data produced by the
     * given query; never <code>null</code> 
     * @exception SQLException if a database access error occurs
     */
    public final ResultSet executeQuery(String sql) 
        throws SQLException
    {
        // this method is not synchronized so that
        // cancel can work

        Statement statement;

        synchronized (this) {
            statement = getStatement();
            // close the existing result set before getting
            // the new one just in case the underlying
            // statement does not close its underlying
            // result set. There is no way to query a result
            // set whether it is closed or not.
            closeResultSet();
        }
        
        return setResultSet(statement.executeQuery(sql));
    }


    /**
     * Set the result set as a result of executing a query
     * on this statement. 
     * <P>
     * This method assumes that the existing result is closed.
     * <P>
     * If the specified result set is of type {@link TyrexResultSetImpl}
     * then the result set of the statement is set to the specified result
     * set. Else the specified result set is wrapped in a {@link TyrexResultSetImpl}
     * which is then set as the result set of the statement.
     *
     * @param resultSet the underlying result set of the underlying statement.
     *      Can be null.
     * @return the  result set to be returned as the result of a query. 
     * @see #closeResultSet
     */
    protected synchronized ResultSet setResultSet(ResultSet resultSet)
    {
        _resultSet = null == resultSet 
                        ? null 
                        : ((resultSet instanceof TyrexResultSetImpl) 
                            ? (TyrexResultSetImpl)resultSet 
                            : new TyrexResultSetImpl(resultSet, this));

        return _resultSet;
    }


    /**
     * Close the existing result set associated with the statement.
     * If there is no existing result set nothing is done. 
     * <p>
     * This method assumes that the calling method
     * synchronizes on this instance.
     * <P>
     * Any exceptions caused by closing the result set are ignored.
     */
    protected void closeResultSet()
    {
        ResultSet resultSet;

        if (null != _resultSet) {
            try {    
                // set the instance variable _resultSet
                // to null before calling close
                resultSet = _resultSet;
                _resultSet = null;

                resultSet.close();
            }
            catch (SQLException e) {
                // ignore sql exception on old result set
            }
        }
    }

    /**
     * Executes an SQL <code>INSERT</code>, <code>UPDATE</code> or 
     * <code>DELETE</code> statement. In addition,
     * SQL statements that return nothing, such as SQL DDL statements,
     * can be executed.
     *
     * @param sql an SQL <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code> statement or an SQL statement that returns nothing
     * @return either the row count for <code>INSERT</code>, <code>UPDATE</code>
     * or <code>DELETE</code> statements, or 0 for SQL statements that return nothing
     * @exception SQLException if a database access error occurs
     */
    public final int executeUpdate(String sql) 
        throws SQLException
    {
        // this method is not synchronized so that
        // cancel can work

        Statement statement;

        synchronized (this) {
            statement = getStatement();
        }
        
        return statement.executeUpdate(sql);
    }

    /**
     * Releases this <code>Statement</code> object's database 
     * and JDBC resources immediately instead of waiting for
     * this to happen when it is automatically closed.
     * It is generally good practice to release resources as soon as
     * you are finished with them to avoid tying up database
     * resources.
     * <P><B>Note:</B> A <code>Statement</code> object is automatically closed when it is
     * garbage collected. When a <code>Statement</code> object is closed, its current
     * <code>ResultSet</code> object, if one exists, is also closed.  
     *
     * @exception SQLException if a database access error occurs
     */
    public final synchronized void close() 
        throws SQLException
    {
        internalClose();
    }


    /**
     * The method that actually closes the underlying statement.
     * Any existing result sets are closed as well.
     * <P>
     * This method assumes that the calling methods are synchronized
     * on the statement
     *
     * @throws SQLException if the statement is already closed or if there
     *      is a problem closing the underlying statement.
     */
    private void internalClose() 
        throws SQLException
    {
        if (null == _statement) {
            throw new SQLException("The statement is already closed");    
        }

        // close any existing result set
        closeResultSet();

        try {
            _statement.close();    
        }
        finally {
            _statement = null;
            _connection = null;

        }
    }

    //----------------------------------------------------------------------

    /**
     * Returns the maximum number of bytes allowed
     * for any column value. 
     * This limit is the maximum number of bytes that can be
     * returned for any column value.
     * The limit applies only to <code>BINARY</code>,
     * <code>VARBINARY</code>, <code>LONGVARBINARY</code>, <code>CHAR</code>, <code>VARCHAR</code>, and <code>LONGVARCHAR</code>
     * columns.  If the limit is exceeded, the excess data is silently
     * discarded.
     *
     * @return the current max column size limit; zero means unlimited 
     * @exception SQLException if a database access error occurs
     */
    public final synchronized int getMaxFieldSize() 
        throws SQLException
    {
        return getStatement().getMaxFieldSize();
    }
    
    /**
     * Sets the limit for the maximum number of bytes in a column to
     * the given number of bytes.  This is the maximum number of bytes 
     * that can be returned for any column value.  This limit applies
     * only to <code>BINARY</code>, <code>VARBINARY</code>,
     * <code>LONGVARBINARY</code>, <code>CHAR</code>, <code>VARCHAR</code>, and
     * <code>LONGVARCHAR</code> fields.  If the limit is exceeded, the excess data
     * is silently discarded. For maximum portability, use values
     * greater than 256.
     *
     * @param max the new max column size limit; zero means unlimited 
     * @exception SQLException if a database access error occurs
     */
    public final synchronized void setMaxFieldSize(int max) 
        throws SQLException
    {
        getStatement().setMaxFieldSize(max);
    }

    /**
     * Retrieves the maximum number of rows that a
     * <code>ResultSet</code> object can contain.  If the limit is exceeded, the excess
     * rows are silently dropped.
     *
     * @return the current max row limit; zero means unlimited
     * @exception SQLException if a database access error occurs
     */
    public final synchronized int getMaxRows() 
        throws SQLException
    {
        return getStatement().getMaxRows();
    }

    /**
     * Sets the limit for the maximum number of rows that any
     * <code>ResultSet</code> object can contain to the given number.
     * If the limit is exceeded, the excess
     * rows are silently dropped.
     *
     * @param max the new max rows limit; zero means unlimited 
     * @exception SQLException if a database access error occurs
     */
    public final synchronized void setMaxRows(int max) 
        throws SQLException
    {
        getStatement().setMaxRows(max);
    }

    /**
     * Sets escape processing on or off.
     * If escape scanning is on (the default), the driver will do
     * escape substitution before sending the SQL to the database.
     *
     * Note: Since prepared statements have usually been parsed prior
     * to making this call, disabling escape processing for prepared
     * statements will have no effect.
     *
     * @param enable <code>true</code> to enable; <code>false</code> to disable
     * @exception SQLException if a database access error occurs
     */
    public final synchronized void setEscapeProcessing(boolean enable) 
        throws SQLException
    {
        getStatement().setEscapeProcessing(enable);
    }

    /**
     * Retrieves the number of seconds the driver will
     * wait for a <code>Statement</code> object to execute. If the limit is exceeded, a
     * <code>SQLException</code> is thrown.
     *
     * @return the current query timeout limit in seconds; zero means unlimited 
     * @exception SQLException if a database access error occurs
     */
    public final synchronized int getQueryTimeout() 
        throws SQLException
    {
        return getStatement().getQueryTimeout();
    }

    /**
     * Sets the number of seconds the driver will
     * wait for a <code>Statement</code> object to execute to the given number of seconds.
     * If the limit is exceeded, an <code>SQLException</code> is thrown.
     *
     * @param seconds the new query timeout limit in seconds; zero means 
     * unlimited 
     * @exception SQLException if a database access error occurs
     */
    public final synchronized void setQueryTimeout(int seconds) 
        throws SQLException
    {
        getStatement().setQueryTimeout(seconds);
    }

    /**
     * Cancels this <code>Statement</code> object if both the DBMS and
     * driver support aborting an SQL statement.
     * This method can be used by one thread to cancel a statement that
     * is being executed by another thread.
     *
     * @exception SQLException if a database access error occurs
     */
    public final synchronized void cancel() 
        throws SQLException
    {
        getStatement().cancel();
    }

    /**
     * Retrieves the first warning reported by calls on this <code>Statement</code> object.
     * Subsequent <code>Statement</code> object warnings will be chained to this
     * <code>SQLWarning</code> object.
     *
     * <p>The warning chain is automatically cleared each time
     * a statement is (re)executed.
     *
     * <P><B>Note:</B> If you are processing a <code>ResultSet</code> object, any
     * warnings associated with reads on that <code>ResultSet</code> object 
     * will be chained on it.
     *
     * @return the first <code>SQLWarning</code> object or <code>null</code> 
     * @exception SQLException if a database access error occurs
     */
    public final synchronized SQLWarning getWarnings() 
        throws SQLException
    {
        return getStatement().getWarnings();
    }

    /**
     * Clears all the warnings reported on this <code>Statement</code>
     * object. After a call to this method,
     * the method <code>getWarnings</code> will return 
     * <code>null</code> until a new warning is reported for this
     * <code>Statement</code> object.  
     *
     * @exception SQLException if a database access error occurs
     */
    public final synchronized void clearWarnings() 
        throws SQLException
    {
        getStatement().clearWarnings();
    }

    /**
     * Defines the SQL cursor name that will be used by
     * subsequent <code>Statement</code> object <code>execute</code> methods.
     * This name can then be
     * used in SQL positioned update/delete statements to identify the
     * current row in the <code>ResultSet</code> object generated by this statement.  If
     * the database doesn't support positioned update/delete, this
     * method is a noop.  To insure that a cursor has the proper isolation
     * level to support updates, the cursor's <code>SELECT</code> statement should be
     * of the form 'select for update ...'. If the 'for update' phrase is 
     * omitted, positioned updates may fail.
     *
     * <P><B>Note:</B> By definition, positioned update/delete
     * execution must be done by a different <code>Statement</code> object than the one
     * which generated the <code>ResultSet</code> object being used for positioning. Also,
     * cursor names must be unique within a connection.
     *
     * @param name the new cursor name, which must be unique within
     *             a connection
     * @exception SQLException if a database access error occurs
     */
    public final synchronized void setCursorName(String name) 
        throws SQLException
    {
        getStatement().setCursorName(name);
    }

    //----------------------- Multiple Results --------------------------

    /**
     * Executes an SQL statement that may return multiple results.
     * Under some (uncommon) situations a single SQL statement may return
     * multiple result sets and/or update counts.  Normally you can ignore
     * this unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an
     * unknown SQL string.  The  methods <code>execute</code>,
     * <code>getMoreResults</code>, <code>getResultSet</code>,
     * and <code>getUpdateCount</code> let you navigate through multiple results.
     *
     * The <code>execute</code> method executes an SQL statement and indicates the
     * form of the first result.  You can then use the methods 
     * <code>getResultSet</code> or <code>getUpdateCount</code>
     * to retrieve the result, and <code>getMoreResults</code> to
     * move to any subsequent result(s).
     *
     * @param sql any SQL statement
     * @return <code>true</code> if the next result is a <code>ResultSet</code> object;
     * <code>false</code> if it is an update count or there are no more results
     * @exception SQLException if a database access error occurs
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults 
     */
    public final boolean execute(String sql) 
        throws SQLException
    {
        // this method is not synchronized so that
        // cancel can work

        Statement statement;

        synchronized (this) {
            statement = getStatement();
        }
        
        return statement.execute(sql);
    }

    /**
     *  Returns the current result as a <code>ResultSet</code> object. 
     *  This method should be called only once per result.
     *
     * @return the current result as a <code>ResultSet</code> object;
     * <code>null</code> if the result is an update count or there are no more results
     * @exception SQLException if a database access error occurs
     * @see #execute 
     */
    public final ResultSet getResultSet() 
        throws SQLException
    {
        Statement statement;

        synchronized (this){
            statement = getStatement();

            // close the existing result set before getting
            // the new one just in case the underlying
            // statement does not close its underlying
            // result set. There is no way to query a result
            // set whether it is closed or not.
            closeResultSet();
        }
        
        // the call to statement.getResultSet is not synchronized so that
        // cancel can be called.
        
        return setResultSet(statement.getResultSet());
    }

    /**
     *  Returns the current result as an update count;
     *  if the result is a <code>ResultSet</code> object or there are no more results, -1
     *  is returned. This method should be called only once per result.
     * 
     * @return the current result as an update count; -1 if the current result is a
     * <code>ResultSet</code> object or there are no more results
     * @exception SQLException if a database access error occurs
     * @see #execute 
     */
    public final synchronized int getUpdateCount() 
        throws SQLException
    {
        return getStatement().getUpdateCount();
    }

    /**
     * Moves to a <code>Statement</code> object's next result.  It returns 
     * <code>true</code> if this result is a <code>ResultSet</code> object.
     * This method also implicitly closes any current <code>ResultSet</code>
     * object obtained with the method <code>getResultSet</code>.
     *
     * <P>There are no more results when the following is true:
     * <PRE>
     *      <code>(!getMoreResults() && (getUpdateCount() == -1)</code>
     * </PRE>
     *
     * @return <code>true</code> if the next result is a <code>ResultSet</code> object;
     * <code>false</code> if it is an update count or there are no more results
     * @exception SQLException if a database access error occurs
     * @see #execute 
     */
    public final synchronized boolean getMoreResults() 
        throws SQLException
    {
        return getStatement().getMoreResults();
    }


    //--------------------------JDBC 2.0-----------------------------


    /**
     * Gives the driver a hint as to the direction in which
     * the rows in a result set
     * will be processed. The hint applies only to result sets created 
     * using this <code>Statement</code> object.  The default value is 
     * <code>ResultSet.FETCH_FORWARD</code>.
     * <p>Note that this method sets the default fetch direction for 
     * result sets generated by this <code>Statement</code> object.
     * Each result set has its own methods for getting and setting
     * its own fetch direction.
     * @param direction the initial direction for processing rows
     * @exception SQLException if a database access error occurs
     * or the given direction
     * is not one of <code>ResultSet.FETCH_FORWARD</code>,
     * <code>ResultSet.FETCH_REVERSE</code>, or <code>ResultSet.FETCH_UNKNOWN</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public final synchronized void setFetchDirection(int direction) 
        throws SQLException
    {
        getStatement().setFetchDirection(direction);
    }

    /**
     * Retrieves the direction for fetching rows from
     * database tables that is the default for result sets
     * generated from this <code>Statement</code> object.
     * If this <code>Statement</code> object has not set
     * a fetch direction by calling the method <code>setFetchDirection</code>,
     * the return value is implementation-specific.
     *
     * @return the default fetch direction for result sets generated
     *          from this <code>Statement</code> object
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public final synchronized int getFetchDirection() 
        throws SQLException
    {
        return getStatement().getFetchDirection();
    }

    /**
     * Gives the JDBC driver a hint as to the number of rows that should 
     * be fetched from the database when more rows are needed.  The number 
     * of rows specified affects only result sets created using this 
     * statement. If the value specified is zero, then the hint is ignored.
     * The default value is zero.
     *
     * @param rows the number of rows to fetch
     * @exception SQLException if a database access error occurs, or the
     * condition 0 <= rows <= this.getMaxRows() is not satisfied.
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public final synchronized void setFetchSize(int rows) 
        throws SQLException
    {
        getStatement().setFetchSize(rows);
    }
  
    /**
     * Retrieves the number of result set rows that is the default 
     * fetch size for result sets
     * generated from this <code>Statement</code> object.
     * If this <code>Statement</code> object has not set
     * a fetch size by calling the method <code>setFetchSize</code>,
     * the return value is implementation-specific.
     * @return the default fetch size for result sets generated
     *          from this <code>Statement</code> object
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public final synchronized int getFetchSize() 
        throws SQLException
    {
        return getStatement().getFetchSize();
    }

    /**
     * Retrieves the result set concurrency for <code>ResultSet</code> objects
     * generated by this <code>Statement</code> object.
     *
     * @return either <code>ResultSet.CONCUR_READ_ONLY</code> or
     * <code>ResultSet.CONCUR_UPDATABLE</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public final synchronized int getResultSetConcurrency() 
        throws SQLException
    {
        return getStatement().getResultSetConcurrency();
    }

    /**
     * Retrieves the result set type for <code>ResultSet</code> objects
     * generated by this <code>Statement</code> object.
     *
     * @return one of <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     * <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     * <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public final synchronized int getResultSetType()  
        throws SQLException
    {
        return getStatement().getResultSetType();
    }

    /**
     * Adds an SQL command to the current batch of commmands for this
     * <code>Statement</code> object. This method is optional.
     *
     * @param sql typically this is a static SQL <code>INSERT</code> or 
     * <code>UPDATE</code> statement
     * @exception SQLException if a database access error occurs, or the
     * driver does not support batch statements
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public final synchronized void addBatch( String sql ) 
        throws SQLException
    {
        getStatement().addBatch(sql);
    }

    /**
     * Makes the set of commands in the current batch empty.
     * This method is optional.
     *
     * @exception SQLException if a database access error occurs or the
     * driver does not support batch statements
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public final synchronized void clearBatch() 
        throws SQLException
    {
        getStatement().clearBatch();
    }

    /**
     * Submits a batch of commands to the database for execution and
     * if all commands execute successfully, returns an array of update counts.
     * The <code>int</code> elements of the array that is returned are ordered
     * to correspond to the commands in the batch, which are ordered 
     * according to the order in which they were added to the batch.
     * The elements in the array returned by the method <code>executeBatch</code>
     * may be one of the following:
     * <OL>
     * <LI>A number greater than or equal to zero -- indicates that the
     * command was processed successfully and is an update count giving the
     * number of rows in the database that were affected by the command's
     * execution
     * <LI>A value of <code>-2</code> -- indicates that the command was
     * processed successfully but that the number of rows affected is
     * unknown
     * <P> 
     * If one of the commands in a batch update fails to execute properly,
     * this method throws a <code>BatchUpdateException</code>, and a JDBC
     * driver may or may not continue to process the remaining commands in
     * the batch.  However, the driver's behavior must be consistent with a
     * particular DBMS, either always continuing to process commands or never
     * continuing to process commands.  If the driver continues processing
     * after a failure, the array returned by the method
     * <code>BatchUpdateException.getUpdateCounts</code>
     * will contain as many elements as there are commands in the batch, and
     * at least one of the elements will be the following:
     * <P> 
     * <LI>A value of <code>-3</code> -- indicates that the command failed
     * to execute successfully and occurs only if a driver continues to
     * process commands after a command fails
     * </OL>
     * <P>
     * A driver is not required to implement this method.
     * The possible implementations and return values have been modified in
     * the Java 2 SDK, Standard Edition, version 1.3 to
     * accommodate the option of continuing to proccess commands in a batch
     * update after a <code>BatchUpdateException</code> obejct has been thrown.
     *
     * @return an array of update counts containing one element for each
     * command in the batch.  The elements of the array are ordered according 
     * to the order in which commands were added to the batch.
     * @exception SQLException if a database access error occurs or the
     * driver does not support batch statements. Throws {@link BatchUpdateException}
     * (a subclass of <code>SQLException</code>) if one of the commands sent to the
     * database fails to execute properly or attempts to return a result set.
     * @since 1.3
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public final int[] executeBatch() 
        throws SQLException
    {
        // this method is not synchronized so that
        // cancel can work

        Statement statement;

        synchronized (this) {
            statement = getStatement();
        }
        
        return statement.executeBatch();
    }

    /**
     * Returns the <code>Connection</code> object
     * that produced this <code>Statement</code> object.
     * @return the connection that produced this statement
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public final Connection getConnection()  
        throws SQLException
    {
        return _connection;
    }


    /**
     * Finalize the statement
     */
    protected void finalize()
        throws Throwable
    {
        close();
    }


    /**
     * Return the statement.
     * <p>
     * This method assumes that the calling method
     * synchronizes on this instance.
     *
     * @return the statement.
     * @throws SQLException if either the statement or
     *      connection has been closed.
     */
    protected final Statement getStatement()
        throws SQLException
    {
        if ( _connection.isClosed())
            throw new SQLException("The statement has been closed.");
        return _statement;
    }


    /**
     * The specified result set from this statement has been closed
     *
     * @param resultSet the result set
     */
    synchronized final void resultSetIsClosed(TyrexResultSetImpl resultSet)
    {
        if (resultSet == _resultSet) {
            _resultSet = null;    
        }
    }
}
