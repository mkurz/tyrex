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
 * Original code is Copyright (c) 1999-2001, Intalio, Inc. All Rights Reserved.
 *
 * Contributions by MetaBoss team are Copyright (c) 2003-2004, Softaris Pty. Ltd. All Rights Reserved.
 *
 * $Id: TestStatementImpl.java,v 1.2 2004/12/15 06:18:47 metaboss Exp $
 */

package jdbc.db;

import java.sql.*;

/**
 * Test implementation of java.sql.Statement
 */
final class TestStatementImpl 
    implements Statement
{
    /**
     * The tyrex connection that created this statement
     */
    private final TestConnectionImpl _connection;


    /**
     * Create the statement with the specified connection.
     *
     * @param connection the connection that created the statement
     */
    TestStatementImpl(TestConnectionImpl connection)
    {
        if (null == connection) {
            throw new IllegalArgumentException("The argument 'connection' is null.");
        }

        _connection = connection;
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param sql typically this is a static SQL <code>SELECT</code> statement
     * @return a <code>ResultSet</code> object that contains the data produced by the
     * given query; never <code>null</code> 
     * @exception SQLException if a database access error occurs
     */
    public ResultSet executeQuery(String sql) 
        throws SQLException
    {
        throw new SQLException("executeQuery method not supported.");
    }

    /**
     * Return 0;
     *
     * @param sql an SQL <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code> statement or an SQL statement that returns nothing
     * @return 0
     * @exception SQLException if a database access error occurs
     */
    public int executeUpdate(String sql) 
        throws SQLException
    {
        //throw new SQLException("executeUpdate method not supported.");
        // do nothing
        return 0;
    }

    /**
     * Does nothing
     *
     * @exception SQLException if a database access error occurs
     */
    public void close() 
        throws SQLException
    {

    }

    //----------------------------------------------------------------------

    /**
     * Not supported. Throws SQLException.
     *
     * @exception SQLException if a database access error occurs
     */
    public int getMaxFieldSize() 
        throws SQLException
    {
        throw new SQLException("getMaxFieldSize method not supported.");
    }
    
    /**
     * Not supported. Throws SQLException.
     *
     * @param max the new max column size limit; zero means unlimited 
     * @exception SQLException if a database access error occurs
     */
    public void setMaxFieldSize(int max) 
        throws SQLException
    {
        throw new SQLException("setMaxFieldSize method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @exception SQLException if a database access error occurs
     */
    public int getMaxRows() 
        throws SQLException
    {
        throw new SQLException("getMaxRows method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param max the new max rows limit; zero means unlimited 
     * @exception SQLException if a database access error occurs
     */
    public void setMaxRows(int max) 
        throws SQLException
    {
        throw new SQLException("setMaxRows method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param enable <code>true</code> to enable; <code>false</code> to disable
     * @exception SQLException if a database access error occurs
     */
    public void setEscapeProcessing(boolean enable) 
        throws SQLException
    {
        throw new SQLException("setEscapeProcessing method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @exception SQLException if a database access error occurs
     */
    public int getQueryTimeout() 
        throws SQLException
    {
        throw new SQLException("getQueryTimeout method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param seconds the new query timeout limit in seconds; zero means 
     * unlimited 
     * @exception SQLException 
     */
    public void setQueryTimeout(int seconds) 
        throws SQLException
    {
        throw new SQLException("setQueryTimeout method not supported.");
    }

    /**
     * Does nothing.
     *
     * @exception SQLException if a database access error occurs
     */
    public void cancel() 
        throws SQLException
    {

    }

    /**
     * Not supported. Throws SQLException.
     *
     * @exception SQLException if a database access error occurs
     */
    public SQLWarning getWarnings() 
        throws SQLException
    {
        throw new SQLException("getWarnings method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @exception SQLException if a database access error occurs
     */
    public void clearWarnings() 
        throws SQLException
    {
        throw new SQLException("clearWarnings method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param name the new cursor name, which must be unique within
	 *             a connection
     * @exception SQLException if a database access error occurs
     */
    public void setCursorName(String name) 
        throws SQLException
    {
        throw new SQLException("setCursorName method not supported.");
    }
	
    //----------------------- Multiple Results --------------------------

    /**
     * Not supported. Throws SQLException.
     *
     * @param sql any SQL statement
     */
    public boolean execute(String sql) 
        throws SQLException
    {
        throw new SQLException("execute method not supported.");
    }
	
    /**
     *  Not supported. Throws SQLException.
     *
     * @return the current result as a <code>ResultSet</code> object;
	 * <code>null</code> if the result is an update count or there are no more results
     * @exception SQLException if a database access error occurs
     * @see #execute 
     */
    public ResultSet getResultSet() 
        throws SQLException
    {
        throw new SQLException("getResultSet method not supported.");
    }

    /**
     *  Not supported. Throws SQLException.
     * 
     * @return -1
     * @exception SQLException if a database access error occurs
     * @see #execute 
     */
    public int getUpdateCount() 
        throws SQLException
    {
        throw new SQLException("getUpdateCount method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @return <code>true</code> if the next result is a <code>ResultSet</code> object;
	 * <code>false</code> if it is an update count or there are no more results
     * @exception SQLException if a database access error occurs
     */
    public boolean getMoreResults() 
        throws SQLException
    {
        throw new SQLException("getMoreResults method not supported.");
    }


    //--------------------------JDBC 2.0-----------------------------


    /**
     * Not supported. Throws SQLException.
     */
    public void setFetchDirection(int direction) 
        throws SQLException
    {
        throw new SQLException("setFetchDirection method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @exception SQLException if a database access error occurs
     */
    public int getFetchDirection() 
        throws SQLException
    {
        throw new SQLException("setFetchDirection method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param rows the number of rows to fetch
     * @exception SQLException if a database access error occurs, or the
     * condition 0 <= rows <= this.getMaxRows() is not satisfied.
     */
    public void setFetchSize(int rows) 
        throws SQLException
    {
        throw new SQLException("setFetchSize method not supported.");   
    }
  
    /**
     * Not supported. Throws SQLException.
     *
     * @exception SQLException if a database access error occurs
     */
    public int getFetchSize() 
        throws SQLException
    {
        throw new SQLException("getFetchSize method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     */
    public int getResultSetConcurrency() 
        throws SQLException
    {
        throw new SQLException("getResultSetConcurrency method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
	 *
	 * @return one of <code>ResultSet.TYPE_FORWARD_ONLY</code>,
	 * <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or	
	 * <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     */
    public int getResultSetType()  
        throws SQLException
    {
        throw new SQLException("getResultSetType method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param sql typically this is a static SQL <code>INSERT</code> or 
	 * <code>UPDATE</code> statement
     * @exception SQLException if a database access error occurs, or the
     * driver does not support batch statements
     * @since 1.2
	 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
    public void addBatch( String sql ) 
        throws SQLException
    {
        throw new SQLException("addBatch method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @exception SQLException if a database access error occurs or the
     * driver does not support batch statements
     */
    public void clearBatch() 
        throws SQLException
    {
        throw new SQLException("clearBatch method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @return an array of update counts containing one element for each
     * command in the batch.  The elements of the array are ordered according 
     * to the order in which commands were added to the batch.
     * @exception SQLException if a database access error occurs or the
     * driver does not support batch statements. Throws {@link BatchUpdateException}
	 * (a subclass of <code>SQLException</code>) if one of the commands sent to the
	 * database fails to execute properly or attempts to return a result set.
     */
    public int[] executeBatch() 
        throws SQLException
    {
        throw new SQLException("executeBatch method not supported.");
    }

    /**
     * Returns the <code>Connection</code> object
	 * that produced this <code>TestStatementImpl</code> object.
     *
	 * @return the connection that produced this statement
     * @exception SQLException if a database access error occurs
     * @since 1.2
	 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
    public Connection getConnection()  
        throws SQLException
    {
        return _connection;
    }


    /**
     * Calls close when object is finalized
     */
    protected void finalize()
	    throws Throwable
    {
	    close();
    }
    
	/* (non-Javadoc)
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
	 */
	public final synchronized int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("executeUpdate method not supported.");
	}

	/* (non-Javadoc)
	 * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
	 */
	public final synchronized int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLException("executeUpdate method not supported.");
	}

	/* (non-Javadoc)
	 * @see java.sql.Statement#execute(java.lang.String, int)
	 */
	public final synchronized boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("execute method not supported.");
	}

	/* (non-Javadoc)
	 * @see java.sql.Statement#execute(java.lang.String, int[])
	 */
	public final synchronized boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("execute method not supported.");
	}

	/* (non-Javadoc)
	 * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
	 */
	public final synchronized boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLException("execute method not supported.");
	}

	/* (non-Javadoc)
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int)
	 */
	public final synchronized int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("executeUpdate method not supported.");
	}

	/* (non-Javadoc)
	 * @see java.sql.Statement#getGeneratedKeys()
	 */
	public final synchronized ResultSet getGeneratedKeys() throws SQLException {
        throw new SQLException("getGeneratedKeys method not supported.");
	}

	/* (non-Javadoc)
	 * @see java.sql.Statement#getMoreResults(int)
	 */
	public final synchronized boolean getMoreResults(int current) throws SQLException {
        throw new SQLException("getMoreResults method not supported.");
	}

	/* (non-Javadoc)
	 * @see java.sql.Statement#getResultSetHoldability()
	 */
	public final synchronized int getResultSetHoldability() throws SQLException {
        throw new SQLException("getResultSetHoldability method not supported.");
	}

}	
