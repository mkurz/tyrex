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
 * $Id: TestConnectionImpl.java,v 1.2 2004/12/15 06:18:47 metaboss Exp $
 */

package jdbc.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

/**
 * The test implementation of java.sql.Connection
 */
public final class TestConnectionImpl 
    implements Connection 
{
    /**
     * True if auto-commit is on.
     */
    private boolean _autoCommit = true;


    /**
     * True if the connection is closed
     */
    private boolean _closed = false;


    /**
     * The transaction isolation level
     */
    private int _level = Connection.TRANSACTION_READ_COMMITTED;


    /**
     * True if the connection is read only
     */
    private boolean _readOnly = false;


    /**
     * Transaction lock 1
     * <p>
     * Commit will get _lock1 and then _lock2
     * Rollback wil get _lock2 and then _lock1
     */
    private final Object _lock1 = new Object();


    /**
     * Transaction lock 2
     * <p>
     * Commit will get _lock1 and then _lock2
     * Rollback wil get _lock2 and then _lock1
     */
    private final Object _lock2 = new Object();


    /**
     * Commit wait time in milliseconds.
     * <p>
     * This is the time that commit will wait
     * before trying to get lock2.
     */
    private int _commitWaitTime = 0;


    /**
     * Counter to ensure synchronized blocks in commit
     * and rollback do not get optimized.
     */
    private int _counter = 0;
    

    /**
     * Create the TestConnectionImpl.
     * <p>
     * The specified arguments are ignored.
     *
     * @param url the jdbc url.
     * @param info the properties
     */
    TestConnectionImpl(String url, Properties info)
    {

    }
    
    /**
	 * Return {@link TyrexStatement} object.
     *
     * @return a new {@link TyrexStatement} object 
     * @exception SQLException if a database access error occurs
     */
    public Statement createStatement() 
        throws SQLException
    {
        return new TestStatementImpl(this);
    }

    /**
	 * Not supported. Throws SQLException.
     *
     * @param sql a SQL statement that may contain one or more '?' IN
     * parameter placeholders
     * @return a new PreparedStatement object containing the
     * pre-compiled statement 
     * @exception SQLException if a database access error occurs
     */
    public PreparedStatement prepareStatement(String sql)
	    throws SQLException
    {
        throw new SQLException("prepareStatement method is not supported.");
    }


    /**
	 * Not supported. Throws SQLException.
     *
     * @param sql a SQL statement that may contain one or more '?'
     * parameter placeholders. Typically this  statement is a JDBC
     * function call escape string.
     * @return a new CallableStatement object containing the
     * pre-compiled SQL statement 
     * @exception SQLException if a database access error occurs
     */
    public CallableStatement prepareCall(String sql) 
        throws SQLException
    {
        throw new SQLException("prepareCall method is not supported.");
    }

						
    /**
	 * Not supported. Throws SQLException.
     *
     * @param sql a SQL statement that may contain one or more '?'
     * parameter placeholders
     * @return the native form of this statement
     * @exception SQLException if a database access error occurs
     */
    public String nativeSQL(String sql) 
        throws SQLException
    {
        throw new SQLException("nativeSQL method is not supported.");
    }


    /**
	 * Sets this connection's auto-commit mode.
     *
     * @param autoCommit true enables auto-commit; false disables
     * auto-commit.  
     * @exception SQLException if a database access error occurs
     */
    public void setAutoCommit(boolean autoCommit) 
        throws SQLException
    {
        _autoCommit = autoCommit;
    }

    /**
     * Gets the current auto-commit state.
     *
     * @return the current state of auto-commit mode
     * @exception SQLException if a database access error occurs
     * @see #setAutoCommit 
     */
    public boolean getAutoCommit() 
        throws SQLException
    {
        return _autoCommit;
    }

    /**
     * Set the commit wait time.
     * <p>
     * This is the time that commit will wait
     * before trying to get a second transaction lock.
     * <p>
     * A commit wait time of zero or less
     * means don't wait.
     *
     * @param commitWaitTime
     */
    public void setCommitWaitTime(int commitWaitTime)
    {
        _commitWaitTime = commitWaitTime;
    }


    /**
     * Get the commit wait time
     * <p>
     * A commit wait time of zero or less
     * means don't wait.
     *
     * @return the commit wait time
     */
    public int getCommitWaitTime()
    {
        return _commitWaitTime;
    }


    /**
     * Does nothing.
     *
     * @exception SQLException if a database access error occurs
     * @see #setAutoCommit 
     */
    public void commit() 
        throws SQLException
    {
        /*if (_commitWaitTime > 0) {
            //System.out.println(this + " commit: getting lock1");
        }*/
        synchronized (_lock1)
        {
            if (_commitWaitTime > 0) {
                //System.out.println(this + " commit: got lock1");

                try {
                    Thread.sleep(_commitWaitTime);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                //System.out.println(this + " commit: getting lock2");
            }

            synchronized (_lock2)
            {
                /*if (_commitWaitTime > 0) {
                    //System.out.println(this + " commit: got lock2");
                }*/

                ++_counter;
            }
        }
    }


    /**
     * Does nothing.
     *
     * @exception SQLException if a database access error occurs
     * @see #setAutoCommit 
     */
    public void rollback() 
        throws SQLException
    {
        /*if (_commitWaitTime > 0) {
            //System.out.println(this + " rollback: getting lock2");
        }*/
        synchronized (_lock2)
        {
            /*if (_commitWaitTime > 0) {
                //System.out.println(this + " rollback: got lock2");

                //System.out.println(this + " rollback: getting lock1");
            }*/
            synchronized (_lock1)
            {
                /*if (_commitWaitTime > 0) {
                    //System.out.println(this + " rollback: got lock1");
                }*/

                ++_counter;
            }
        }
    }


    /**
     * Return the value of the counter. [To ensure the synchronized blocks
     * in commit and rollback do not get optimized]
     *
     * @return the value of the counter.
     */
    public int getCounter()
    {
        return _counter;
    }

    /**
     * Sets {@link #_closed} flag.
     *
     * @exception SQLException if a database access error occurs
     */
    public void close() 
        throws SQLException
    {
        _closed = true;
    }

    /**
     * Tests to see if a Connection is closed.
     *
     * @return true if the connection is closed; false if it's still open
     * @exception SQLException if a database access error occurs
     */
    public boolean isClosed() 
        throws SQLException
    {
        return _closed;
    }

    //======================================================================
    // Advanced features:

    /**
	 * Not supported. Throws SQLException.
     *
     * @return a DatabaseMetaData object for this Connection 
     * @exception SQLException if a database access error occurs
     */
    public DatabaseMetaData getMetaData() 
        throws SQLException
    {
        throw new SQLException("getMetaData method is not supported.");
    }

    /**
     * Puts this connection in read-only mode as a hint to enable 
     * database optimizations.
     *
     * @param readOnly true enables read-only mode; false disables
     * read-only mode.  
     * @exception SQLException if a database access error occurs
     */
    public void setReadOnly(boolean readOnly) 
        throws SQLException
    {
        _readOnly = true;
    }


    /**
     * Tests to see if the connection is in read-only mode.
     *
     * @return true if connection is read-only and false otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isReadOnly() 
        throws SQLException
    {
        return _readOnly;
    }


    /**
     * Ignored
     *
     * @exception SQLException if a database access error occurs
     */
    public void setCatalog(String catalog) 
        throws SQLException
    {

    }

    /**
     * Returns null.
     *
     * @return null
     * @exception SQLException if a database access error occurs
     */
    public String getCatalog() 
        throws SQLException
    {
        return null;
    }

    /**
     * Set the isolation level
     *
     * @param level one of the TRANSACTION_* isolation values with the
     * exception of TRANSACTION_NONE; some databases may not support
     * other values
     * @exception SQLException if a database access error occurs
     * @see DatabaseMetaData#supportsTransactionIsolationLevel 
     */
    public void setTransactionIsolation(int level) 
        throws SQLException
    {
        if ((level != Connection.TRANSACTION_NONE) &&
            (level != Connection.TRANSACTION_READ_COMMITTED) &&
            (level != Connection.TRANSACTION_READ_UNCOMMITTED) &&
            (level != Connection.TRANSACTION_REPEATABLE_READ) &&
            (level != Connection.TRANSACTION_SERIALIZABLE)) {
            throw new IllegalArgumentException("The argument 'level' " + level + " is invalid.");
        }

        _level = level;
    }

    /**
     * Gets this Connection's current transaction isolation level.
     *
     * @return the current TRANSACTION_* mode value
     * @exception SQLException if a database access error occurs
     */
    public int getTransactionIsolation() 
        throws SQLException
    {
        return _level;
    }


    /**
     * Returns null.
     *
     * @return null 
     * @exception SQLException if a database access error occurs
     */
    public SQLWarning getWarnings() 
        throws SQLException
    {
        return null;
    }

    /**
     * Does nothing.
     *
     * @exception SQLException if a database access error occurs
     */
    public void clearWarnings() 
        throws SQLException
    {

    }


    //--------------------------JDBC 2.0-----------------------------

    /**
     * Not supported. Throws SQLException.
     *
     * @param resultSetType a result set type; see ResultSet.TYPE_XXX
     * @param resultSetConcurrency a concurrency type; see ResultSet.CONCUR_XXX
     * @return a new Statement object 
     * @exception SQLException if a database access error occurs
	 */
    public Statement createStatement(int resultSetType, int resultSetConcurrency) 
        throws SQLException
    {
        throw new SQLException("createStatement method is not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param resultSetType a result set type; see ResultSet.TYPE_XXX
     * @param resultSetConcurrency a concurrency type; see ResultSet.CONCUR_XXX
     * @return a new PreparedStatement object containing the
     * pre-compiled SQL statement 
     * @exception SQLException if a database access error occurs
	 */
     public PreparedStatement prepareStatement(String sql, int resultSetType, 
					                            int resultSetConcurrency)
        throws SQLException
     {
         throw new SQLException("prepareStatement method is not supported.");
     }

    /**
     * Not supported. Throws SQLException.
     *
     * @param resultSetType a result set type; see ResultSet.TYPE_XXX
     * @param resultSetConcurrency a concurrency type; see ResultSet.CONCUR_XXX
     * @return a new CallableStatement object containing the
     * pre-compiled SQL statement 
     * @exception SQLException if a database access error occurs
	 */
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) 
        throws SQLException
    {
        throw new SQLException("prepareCall method is not supported.");
    }

    /**
     * Not supported. Throws SQLException.
	 *
	 * @return the <code>java.util.Map</code> object associated 
	 *         with this <code>Connection</code> object
	 */
    public Map getTypeMap() 
        throws SQLException
    {
        throw new SQLException("getTypeMap method is not supported.");
    }

    /**
     * Not supported. Throws SQLException.
	 *
	 * @param the <code>java.util.Map</code> object to install
	 *        as the replacement for this <code>Connection</code>
	 *        object's default type map
	 */
    public void setTypeMap(Map map) 
        throws SQLException
    {
        throw new SQLException("setTypeMap method is not supported.");
    }
    
    
	/* (non-Javadoc)
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLException("createStatement method is not supported.");
	} 

	/* (non-Javadoc)
	 * @see java.sql.Connection#getHoldability()
	 */
	public int getHoldability() throws SQLException {
        throw new SQLException("getHoldability method is not supported.");
	} 

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLException("prepareCall method is not supported.");
	} 

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLException("prepareStatement method is not supported.");
	} 

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("prepareStatement method is not supported.");
	} 

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("prepareStatement method is not supported.");
	} 

		/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
	 */
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        throw new SQLException("prepareStatement method is not supported.");
	} 

	/* (non-Javadoc)
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new SQLException("releaseSavepoint method is not supported.");
	} 

	/* (non-Javadoc)
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	public void rollback(Savepoint savepoint) throws SQLException {
        throw new SQLException("rollback method is not supported.");
	} 

	/* (non-Javadoc)
	 * @see java.sql.Connection#setHoldability(int)
	 */
	public void setHoldability(int holdability) throws SQLException {
        throw new SQLException("setHoldability method is not supported.");
	} 

	/* (non-Javadoc)
	 * @see java.sql.Connection#setSavepoint()
	 */
	public Savepoint setSavepoint() throws SQLException {
        throw new SQLException("setSavepoint method is not supported.");
	} 

	/* (non-Javadoc)
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLException("setSavepoint method is not supported.");
	} 
}







