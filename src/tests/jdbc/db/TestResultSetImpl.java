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


package jdbc.db;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/////////////////////////////////////////////////////////////////////
// TestResultSetImpl
/////////////////////////////////////////////////////////////////////

/**
 * Test implementation of java.sql.ResultSet. 
 * The result set is always empty.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public final class TestResultSetImpl 
    implements ResultSet
{
    /**
     * The statement
     */
    private TestStatementImpl _statement;


    /**
     * Create the TestResultSetImpl
     *
     * @param statement the statement
     */
    TestResultSetImpl(TestStatementImpl statement)
    {
        if (null == statement) {
            throw new IllegalArgumentException("The argument 'statement' is null.");
        }
        _statement = statement;
    }

    /**
     * Return false - result set is empty.	 
     *
     * @return false
     * @exception SQLException
     */
    public boolean next() 
        throws SQLException
    {
        return false;
    }


    /**
     * Does nothing
     *
     * @exception SQLException
     */
    public void close() 
        throws SQLException
    {

    }

    /**
     * Not supported. Throws SQLException.
     *
     * @exception SQLException
     */
    public boolean wasNull() 
        throws SQLException
    {
        throw new SQLException("wasNull method not supported.");
    }
    
    //======================================================================
    // Methods for accessing results by column index
    //======================================================================

    /**
     * Not supported. Throws SQLException.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException
     */
    public String getString(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getString method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException
     */
    public boolean getBoolean(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getBoolean method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException
     */
    public byte getByte(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getByte method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException
     */
    public short getShort(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getShort method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException
     */
    public int getInt(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getInt method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException
     */
    public long getLong(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getLong method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException
     */
    public float getFloat(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getFloat method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException
     */
    public double getDouble(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getDouble method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param scale the number of digits to the right of the decimal point
     * @exception SQLException
     * @deprecated
     */
    public BigDecimal getBigDecimal(int columnIndex, int scale) 
        throws SQLException
    {
        throw new SQLException("getBigDecimal method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException
     */
    public byte[] getBytes(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getBytes method not supported.");
    }

    /**
     * Not supported. Throws SQLException.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException
     */
    public java.sql.Date getDate(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getDate method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Time</code> object in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
     * @exception SQLException
     */
    public java.sql.Time getTime(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getTime method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Timestamp</code> object in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
     * @exception SQLException
     */
    public java.sql.Timestamp getTimestamp(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getTimestamp method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a stream of ASCII characters. The value can then be read in chunks from the
	 * stream. This method is particularly
     * suitable for retrieving large <char>LONGVARCHAR</char> values.
	 * The JDBC driver will
     * do any necessary conversion from the database format into ASCII.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a <code>getXXX</code> method implicitly closes the stream.  Also, a
     * stream may return <code>0</code> when the method
	 * <code>InputStream.available</code>
	 * is called whether there is data available or not.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     * as a stream of one-byte ASCII characters;
	 * if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
     * @exception SQLException
     */
    public java.io.InputStream getAsciiStream(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getAsciiStream method not supported.");
    }

    /**
	 * Gets the value of a column in the current row as a stream of
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * as a stream of Unicode characters.
	 * The value can then be read in chunks from the
	 * stream. This method is particularly
     * suitable for retrieving large<code>LONGVARCHAR</code>values.  The JDBC driver will
     * do any necessary conversion from the database format into Unicode.
	 * The byte format of the Unicode stream must be Java UTF-8,
	 * as specified in the Java virtual machine specification.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a <code>getXXX</code> method implicitly closes the stream.  Also, a
     * stream may return <code>0</code> when the method 
	 * <code>InputStream.available</code>
     * is called whether there is data available or not.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     * as a stream in Java UTF-8 byte format;
	 * if the value is SQL <code>NULL</code>, the value returned is <code>null</code>
     * @exception SQLException
     * @deprecated use <code>getCharacterStream</code> in place of 
	 *              <code>getUnicodeStream</code>
     */
    public java.io.InputStream getUnicodeStream(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getUnicodeStream method not supported.");
    }

    /**
	 * Gets the value of a column in the current row as a stream of
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a binary stream of
	 * uninterpreted bytes. The value can then be read in chunks from the
	 * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARBINARY</code> values.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a <code>getXXX</code> method implicitly closes the stream.  Also, a
     * stream may return <code>0</code> when the method 
	 * <code>InputStream.available</code>
     * is called whether there is data available or not.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     * as a stream of uninterpreted bytes;
	 * if the value is SQL <code>NULL</code>, the value returned is <code>null</code>
     * @exception SQLException
     */
    public java.io.InputStream getBinaryStream(int columnIndex)
        throws SQLException
    {
        throw new SQLException("getBinaryStream method not supported.");
    }


    //======================================================================
    // Methods for accessing results by column name
    //======================================================================

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>String</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
     * @exception SQLException
     */
    public String getString(String columnName) 
        throws SQLException
    {
        throw new SQLException("getString method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>boolean</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>false</code>
     * @exception SQLException
     */
    public boolean getBoolean(String columnName) 
        throws SQLException
    {
        throw new SQLException("getBoolean method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
     * @exception SQLException
     */
    public byte getByte(String columnName) 
        throws SQLException
    {
        throw new SQLException("getByte method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>short</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
     * @exception SQLException
     */
    public short getShort(String columnName) 
        throws SQLException
    {
        throw new SQLException("getShort method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * an <code>int</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
     * @exception SQLException
     */
    public int getInt(String columnName) 
        throws SQLException
    {
        throw new SQLException("getInt method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>long</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
     * @exception SQLException
     */
    public long getLong(String columnName) 
        throws SQLException
    {
        throw new SQLException("getLong method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>float</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
     * @exception SQLException
     */
    public float getFloat(String columnName) 
        throws SQLException
    {
        throw new SQLException("getFloat method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>double</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
     * @exception SQLException
     */
    public double getDouble(String columnName) 
        throws SQLException
    {
        throw new SQLException("getDouble method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.math.BigDecimal</code> in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @param scale the number of digits to the right of the decimal point
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
     * @exception SQLException
     * @deprecated
     */
    public BigDecimal getBigDecimal(String columnName, int scale) 
        throws SQLException
    {
        throw new SQLException("getBigDecimal method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> array in the Java programming language.
     * The bytes represent the raw values returned by the driver.
     *
     * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
     * @exception SQLException
     */
    public byte[] getBytes(String columnName) 
        throws SQLException
    {
        throw new SQLException("getBytes method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Date</code> object in the Java programming language.
     *
     * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
     * @exception SQLException
     */
    public java.sql.Date getDate(String columnName) 
        throws SQLException
    {
        throw new SQLException("getDate method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row  
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Time</code> object in the Java programming language.
     *
     * @param columnName the SQL name of the column
     * @return the column value; 
	 * if the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code>
     * @exception SQLException
     */
    public java.sql.Time getTime(String columnName) 
        throws SQLException
    {
        throw new SQLException("getTime method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Timestamp</code> object.
     *
     * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
     * @exception SQLException
     */
    public java.sql.Timestamp getTimestamp(String columnName) 
        throws SQLException
    {
        throw new SQLException("getTimestamp method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a stream of
	 * ASCII characters. The value can then be read in chunks from the
	 * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARCHAR</code> values.
	 * The JDBC driver will
     * do any necessary conversion from the database format into ASCII.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a <code>getXXX</code> method implicitly closes the stream. Also, a
     * stream may return <code>0</code> when the method <code>available</code>
     * is called whether there is data available or not.
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of one-byte ASCII characters.
	 * If the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code>.
     * @exception SQLException
     */
    public java.io.InputStream getAsciiStream(String columnName) 
        throws SQLException
    {
        throw new SQLException("getAsciiStream method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a stream of
	 * Unicode characters. The value can then be read in chunks from the
	 * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARCHAR</code> values.
	 * The JDBC driver will
     * do any necessary conversion from the database format into Unicode.
	 * The byte format of the Unicode stream must be Java UTF-8,
	 * as defined in the Java virtual machine specification.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a <code>getXXX</code> method implicitly closes the stream. Also, a
     * stream may return <code>0</code> when the method <code>available</code>
     * is called whether there is data available or not.
     *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of two-byte Unicode characters.  
	 * If the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code>.
     * @exception SQLException
     * @deprecated
     */
    public java.io.InputStream getUnicodeStream(String columnName) 
        throws SQLException
    {
        throw new SQLException("getUnicodeStream method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a stream of uninterpreted
	 * <code>byte</code>s.
	 * The value can then be read in chunks from the
	 * stream. This method is particularly
     * suitable for retrieving large <code>LONGVARBINARY</code>
	 * values. 
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a <code>getXXX</code> method implicitly closes the stream. Also, a
     * stream may return <code>0</code> when the method <code>available</code>
     * is called whether there is data available or not.
	 *
     * @param columnName the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of uninterpreted bytes; 
	 * if the value is SQL <code>NULL</code>, the result is <code>null</code>
     * @exception SQLException
     */
    public java.io.InputStream getBinaryStream(String columnName)
        throws SQLException
    {
        throw new SQLException("getBinaryStream method not supported.");
    }


    //=====================================================================
    // Advanced features:
    //=====================================================================

    /**
     * Returns the first warning reported by calls on this 
	 * <code>ResultSet</code> object.
     * Subsequent warnings on this <code>ResultSet</code> object
	 * will be chained to the <code>SQLWarning</code> object that 
	 * this method returns.
     *
     * <P>The warning chain is automatically cleared each time a new
     * row is read.
     *
     * <P><B>Note:</B> This warning chain only covers warnings caused
     * by <code>ResultSet</code> methods.  Any warning caused by
	 * <code>Statement</code> methods
     * (such as reading OUT parameters) will be chained on the
     * <code>Statement</code> object. 
     *
     * @return the first <code>SQLWarning</code> object reported or <code>null</code>
     * @exception SQLException
     */
    public SQLWarning getWarnings() 
        throws SQLException
    {
        throw new SQLException("getWarnings method not supported.");
    }

    /**
	 * Clears all warnings reported on this <code>ResultSet</code> object.
     * After this method is called, the method <code>getWarnings</code>
	 * returns <code>null</code> until a new warning is
     * reported for this <code>ResultSet</code> object.  
     *
     * @exception SQLException
     */
    public void clearWarnings() 
        throws SQLException
    {
        throw new SQLException("clearWarnings method not supported.");
    }

    /**
     * Gets the name of the SQL cursor used by this <code>ResultSet</code>
	 * object.
     *
     * <P>In SQL, a result table is retrieved through a cursor that is
     * named. The current row of a result set can be updated or deleted
     * using a positioned update/delete statement that references the
     * cursor name. To insure that the cursor has the proper isolation
     * level to support update, the cursor's <code>select</code> statement should be 
     * of the form 'select for update'. If the 'for update' clause is 
     * omitted, the positioned updates may fail.
     * 
     * <P>The JDBC API supports this SQL feature by providing the name of the
     * SQL cursor used by a <code>ResultSet</code> object.
	 * The current row of a <code>ResultSet</code> object
     * is also the current row of this SQL cursor.
     *
     * <P><B>Note:</B> If positioned update is not supported, a
     * <code>SQLException</code> is thrown.
     *
     * @return the SQL name for this <code>ResultSet</code> object's cursor
     * @exception SQLException
     */
    public String getCursorName() 
        throws SQLException
    {
        throw new SQLException("getCursorName method not supported.");
    }

    /**
     * Retrieves the  number, types and properties of
	 * this <code>ResultSet</code> object's columns.
     *
     * @return the description of this <code>ResultSet</code> object's columns
     * @exception SQLException
     */
    public ResultSetMetaData getMetaData() 
        throws SQLException
    {
        throw new SQLException("getMetaData method not supported.");
    }

    /**
     * <p>Gets the value of the designated column in the current row 
	 * of this <code>ResultSet</code> object as 
	 * an <code>Object</code> in the Java programming language.
     *
     * <p>This method will return the value of the given column as a
     * Java object.  The type of the Java object will be the default
     * Java object type corresponding to the column's SQL type,
     * following the mapping for built-in types specified in the JDBC 
     * specification.
     *
     * <p>This method may also be used to read datatabase-specific
     * abstract data types.
     *
     * In the JDBC 2.0 API, the behavior of method
	 * <code>getObject</code> is extended to materialize  
     * data of SQL user-defined types.  When a column contains
     * a structured or distinct value, the behavior of this method is as 
     * if it were a call to: <code>getObject(columnIndex, 
     * this.getStatement().getConnection().getTypeMap())</code>.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a <code>java.lang.Object</code> holding the column value  
     * @exception SQLException
     */
    public Object getObject(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getObject method not supported.");
    }

    /**
     * <p>Gets the value of the designated column in the current row 
	 * of this <code>ResultSet</code> object as 
	 * an <code>Object</code> in the Java programming language.
     *
     * <p>This method will return the value of the given column as a
     * Java object.  The type of the Java object will be the default
     * Java object type corresponding to the column's SQL type,
     * following the mapping for built-in types specified in the JDBC 
     * specification.
     *
     * <p>This method may also be used to read datatabase-specific
     * abstract data types.
     *
     * In the JDBC 2.0 API, the behavior of the method
	 * <code>getObject</code> is extended to materialize  
     * data of SQL user-defined types.  When a column contains
     * a structured or distinct value, the behavior of this method is as 
     * if it were a call to: <code>getObject(columnIndex, 
     * this.getStatement().getConnection().getTypeMap())</code>.
     *
     * @param columnName the SQL name of the column
     * @return a <code>java.lang.Object</code> holding the column value  
     * @exception SQLException
     */
    public Object getObject(String columnName) 
        throws SQLException
    {
        throw new SQLException("getObject method not supported.");
    }

    //----------------------------------------------------------------

    /**
     * Maps the given <code>ResultSet</code> column name to its
	 * <code>ResultSet</code> column index.
     *
     * @param columnName the name of the column
     * @return the column index of the given column name
     * @exception SQLException
     */
    public int findColumn(String columnName) 
        throws SQLException
    {
        throw new SQLException("findColumn method not supported.");
    }


    //--------------------------JDBC 2.0-----------------------------------

    //---------------------------------------------------------------------
    // Getters and Setters
    //---------------------------------------------------------------------

    /**
     * Gets the value of the designated column in the current row 
	 * of this <code>ResultSet</code> object as a
	 * <code>java.io.Reader</code> object.
	 * @return a <code>java.io.Reader</code> object that contains the column
	 * value; if the value is SQL <code>NULL</code>, the value returned is
	 * <code>null</code> in the Java programming language.
     * @param columnIndex the first column is 1, the second is 2, ...
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.io.Reader getCharacterStream(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getCharacterStream method not supported.");
    }

    /**
     * Gets the value of the designated column in the current row 
	 * of this <code>ResultSet</code> object as a
	 * <code>java.io.Reader</code> object.
	 *
	 * @return a <code>java.io.Reader</code> object that contains the column
	 * value; if the value is SQL <code>NULL</code>, the value returned is
	 * <code>null</code> in the Java programming language.
     * @param columnName the name of the column
	 * @return the value in the specified column as a <code>java.io.Reader</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.io.Reader getCharacterStream(String columnName) 
        throws SQLException
    {
        throw new SQLException("getCharacterStream method not supported.");
    }

    /**
	 * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a
	 * <code>java.math.BigDecimal</code> with full precision.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value (full precision);
	 * if the value is SQL <code>NULL</code>, the value returned is
	 * <code>null</code> in the Java programming language.
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public BigDecimal getBigDecimal(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("getBigDecimal method not supported.");
    }

    /**
	 * Gets the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a
	 * <code>java.math.BigDecimal</code> with full precision.
     *
     * @param columnName the column name
     * @return the column value (full precision);
	 * if the value is SQL <code>NULL</code>, the value returned is
	 * <code>null</code> in the Java programming language.
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     *
     */
    public BigDecimal getBigDecimal(String columnName) 
        throws SQLException
    {
        throw new SQLException("getBigDecimal method not supported.");
    }

    //---------------------------------------------------------------------
    // Traversal/Positioning
    //---------------------------------------------------------------------

    /**
     * Indicates whether the cursor is before the first row in 
	 * this <code>ResultSet</code> object.
     *
     * @return <code>true</code> if the cursor is before the first row;
	 * <code>false</code> if the cursor is at any other position or the
	 * result set contains no rows
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean isBeforeFirst() 
        throws SQLException
    {
        return false;
    }
      
    /**
     * Indicates whether the cursor is after the last row in 
	 * this <code>ResultSet</code> object.
     *
     * @return <code>true</code> if the cursor is after the last row;
	 * <code>false</code> if the cursor is at any other position or the
	 * result set contains no rows
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean isAfterLast() 
        throws SQLException
    {
        return false;
    }
 
    /**
     * Indicates whether the cursor is on the first row of
	 * this <code>ResultSet</code> object.
     *
     * @return <code>true</code> if the cursor is on the first row;
	 * <code>false</code> otherwise   
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean isFirst() 
        throws SQLException
    {
        return false;
    }
 
    /**
     * Indicates whether the cursor is on the last row of 
	 * this <code>ResultSet</code> object.
     * Note: Calling the method <code>isLast</code> may be expensive
	 * because the JDBC driver
     * might need to fetch ahead one row in order to determine 
     * whether the current row is the last row in the result set.
     *
     * @return <code>true</code> if the cursor is on the last row;
	 * <code>false</code> otherwise   
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean isLast() 
        throws SQLException
    {
        return false;
    }

    /**
     * Moves the cursor to the front of
	 * this <code>ResultSet</code> object, just before the
     * first row. This method has no effect if the result set contains no rows.
     *
     * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void beforeFirst() 
        throws SQLException
    {
        throw new SQLException("beforeFirst method not supported.");
    }

    /**
     * Moves the cursor to the end of
	 * this <code>ResultSet</code> object, just after the
     * last row. This method has no effect if the result set contains no rows.
     * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
	 */
    public void afterLast() 
        throws SQLException
    {
        throw new SQLException("afterLast method not supported.");
    }

    /**
     * Moves the cursor to the first row in
	 * this <code>ResultSet</code> object.
     *
     * @return <code>true</code> if the cursor is on a valid row;
	 * <code>false</code> if there are no rows in the result set
     * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean first() 
        throws SQLException
    {
        throw new SQLException("first method not supported.");
    }

    /**
     * Moves the cursor to the last row in
	 * this <code>ResultSet</code> object.
     *
     * @return <code>true</code> if the cursor is on a valid row;
	 * <code>false</code> if there are no rows in the result set
     * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean last() 
        throws SQLException
    {
        throw new SQLException("last method not supported.");
    }

    /**
     * Retrieves the current row number.  The first row is number 1, the
     * second number 2, and so on.  
     *
     * @return the current row number; <code>0</code> if there is no current row
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public int getRow() 
        throws SQLException
    {
        throw new SQLException("getRow method not supported.");
    }

    /**
     * Moves the cursor to the given row number in
	 * this <code>ResultSet</code> object.
     *
     * <p>If the row number is positive, the cursor moves to 
	 * the given row number with respect to the
     * beginning of the result set.  The first row is row 1, the second
     * is row 2, and so on. 
     *
     * <p>If the given row number is negative, the cursor moves to
	 * an absolute row position with respect to
     * the end of the result set.  For example, calling the method
	 * <code>absolute(-1)</code> positions the 
     * cursor on the last row; calling the method <code>absolute(-2)</code>
	 * moves the cursor to the next-to-last row, and so on.
     *
     * <p>An attempt to position the cursor beyond the first/last row in
     * the result set leaves the cursor before the first row or after 
	 * the last row.
     *
     * <p><B>Note:</B> Calling <code>absolute(1)</code> is the same
	 * as calling <code>first()</code>. Calling <code>absolute(-1)</code> 
     * is the same as calling <code>last()</code>.
     *
     * @return <code>true</code> if the cursor is on the result set;
	 * <code>false</code> otherwise
     * @exception SQLException if a database access error
	 * occurs, the row is <code>0</code>, or the result set type is 
	 * <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean absolute( int row ) 
        throws SQLException
    {
        throw new SQLException("absolute method not supported.");
    }

    /**
     * Moves the cursor a relative number of rows, either positive or negative.
     * Attempting to move beyond the first/last row in the
     * result set positions the cursor before/after the
     * the first/last row. Calling <code>relative(0)</code> is valid, but does
     * not change the cursor position.
     *
     * <p>Note: Calling the method <code>relative(1)</code>
	 * is different from calling the method <code>next()</code>
     * because is makes sense to call <code>next()</code> when there
	 * is no current row,
     * for example, when the cursor is positioned before the first row
     * or after the last row of the result set.
     *
     * @return <code>true</code> if the cursor is on a row;
	 * <code>false</code> otherwise
     * @exception SQLException, 
	 * there is no current row, or the result set type is 
	 * <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean relative( int rows ) 
        throws SQLException
    {
        throw new SQLException("relative method not supported.");
    }

    /**
     * Moves the cursor to the previous row in this
	 * <code>ResultSet</code> object.
     *
     * <p><B>Note:</B> Calling the method <code>previous()</code> is not the same as
	 * calling the method <code>relative(-1)</code> because it
     * makes sense to call</code>previous()</code> when there is no current row.
     *
     * @return <code>true</code> if the cursor is on a valid row; 
	 * <code>false</code> if it is off the result set
     * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean previous() 
        throws SQLException
    {
        throw new SQLException("previous method not supported.");
    }

    //---------------------------------------------------------------------
    // Properties
    //---------------------------------------------------------------------

    /**
     * The constant indicating that the rows in a result set will be 
	 * processed in a forward direction; first-to-last.
	 * This constant is used by the method <code>setFetchDirection</code>
	 * as a hint to the driver, which the driver may ignore.
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    int FETCH_FORWARD = 1000;

    /**
     * The constant indicating that the rows in a result set will be 
     * processed in a reverse direction; last-to-first.
	 * This constant is used by the method <code>setFetchDirection</code>
	 * as a hint to the driver, which the driver may ignore.
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    int FETCH_REVERSE = 1001;

    /**
     * The constant indicating that the order in which rows in a 
	 * result set will be processed is unknown.
	 * This constant is used by the method <code>setFetchDirection</code>
	 * as a hint to the driver, which the driver may ignore.
     */
    int FETCH_UNKNOWN = 1002;

    /**
     * Gives a hint as to the direction in which the rows in this
	 * <code>ResultSet</code> object will be processed. 
	 * The initial value is determined by the 
	 * <code>Statement</code> object
     * that produced this <code>ResultSet</code> object.
	 * The fetch direction may be changed at any time.
     *
     * @exception SQLException or
     * the result set type is <code>TYPE_FORWARD_ONLY</code> and the fetch
	 * direction is not <code>FETCH_FORWARD</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
	 * @see Statement#setFetchDirection
     */
    public void setFetchDirection(int direction) 
        throws SQLException
    {
        throw new SQLException("setFetchDirection method not supported.");
    }

    /**
     * Returns the fetch direction for this 
	 * <code>ResultSet</code> object.
     *
	 * @return the current fetch direction for this <code>ResultSet</code> object 
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public int getFetchDirection() 
        throws SQLException
    {
        throw new SQLException("getFetchDirection method not supported.");
    }

    /**
     * Gives the JDBC driver a hint as to the number of rows that should 
     * be fetched from the database when more rows are needed for this 
	 * <code>ResultSet</code> object.
     * If the fetch size specified is zero, the JDBC driver 
     * ignores the value and is free to make its own best guess as to what
     * the fetch size should be.  The default value is set by the 
	 * <code>Statement</code> object
     * that created the result set.  The fetch size may be changed at any time.
     *
     * @param rows the number of rows to fetch
     * @exception SQLException or the
     * condition <code>0 <= rows <= this.getMaxRows()</code> is not satisfied
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void setFetchSize(int rows) 
        throws SQLException
    {
        throw new SQLException("setFetchSize method not supported.");
    }

    /**
     *
     * Returns the fetch size for this 
	 * <code>ResultSet</code> object.
     *
	 * @return the current fetch size for this <code>ResultSet</code> object
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public int getFetchSize() 
        throws SQLException
    {
        throw new SQLException("getFetchSize method not supported.");
    }

    /**
     * Returns the type of this <code>ResultSet</code> object.  
	 * The type is determined by the <code>Statement</code> object
	 * that created the result set.
     *
     * @return <code>TYPE_FORWARD_ONLY</code>,
	 * <code>TYPE_SCROLL_INSENSITIVE</code>,
	 * or <code>TYPE_SCROLL_SENSITIVE</code>
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public int getType() 
        throws SQLException
    {
        throw new SQLException("getType method not supported.");
    }

    /**
     * Returns the concurrency mode of this <code>ResultSet</code> object.
	 * The concurrency used is determined by the 
	 * <code>Statement</code> object that created the result set.
     *
     * @return the concurrency type, either <code>CONCUR_READ_ONLY</code>
	 * or <code>CONCUR_UPDATABLE</code>
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public int getConcurrency() 
        throws SQLException
    {
        throw new SQLException("getConcurrency method not supported.");
    }

    //---------------------------------------------------------------------
    // Updates
    //---------------------------------------------------------------------

    /**
     * Indicates whether the current row has been updated.  The value returned 
     * depends on whether or not the result set can detect updates.
     *
     * @return <code>true</code> if the row has been visibly updated
	 * by the owner or another, and updates are detected
     * @exception SQLException
     * 
     * @see DatabaseMetaData#updatesAreDetected
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean rowUpdated() 
        throws SQLException
    {
        throw new SQLException("rowUpdated method not supported.");
    }

    /**
     * Indicates whether the current row has had an insertion.
	 * The value returned depends on whether or not this
	 * <code>ResultSet</code> object can detect visible inserts.
     *
     * @return <code>true</code> if a row has had an insertion
	 * and insertions are detected; <code>false</code> otherwise
     * @exception SQLException
     * 
     * @see DatabaseMetaData#insertsAreDetected
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean rowInserted() 
        throws SQLException
    {
        throw new SQLException("rowInserted method not supported.");
    }
   
    /**
     * Indicates whether a row has been deleted.  A deleted row may leave
     * a visible "hole" in a result set.  This method can be used to
     * detect holes in a result set.  The value returned depends on whether 
     * or not this <code>ResultSet</code> object can detect deletions.
     *
     * @return <code>true</code> if a row was deleted and deletions are detected;
	 * <code>false</code> otherwise
     * @exception SQLException
     * 
     * @see DatabaseMetaData#deletesAreDetected
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean rowDeleted() 
        throws SQLException
    {
        throw new SQLException("rowDeleted method not supported.");
    }

    /**
     * Gives a nullable column a null value.
     * 
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code>
	 * or <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateNull(int columnIndex) 
        throws SQLException
    {
        throw new SQLException("updateNull method not supported.");
    }

    /**
     * Updates the designated column with a <code>boolean</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBoolean(int columnIndex, boolean x) 
        throws SQLException
    {
        throw new SQLException("updateBoolean method not supported.");
    }

    /**
     * Updates the designated column with a <code>byte</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateByte(int columnIndex, byte x) 
        throws SQLException
    {
        throw new SQLException("updateByte method not supported.");
    }

    /**
     * Updates the designated column with a <code>short</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateShort(int columnIndex, short x) 
        throws SQLException
    {
        throw new SQLException("updateShort method not supported.");
    }

    /**
     * Updates the designated column with an <code>int</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateInt(int columnIndex, int x) 
        throws SQLException
    {
        throw new SQLException("updateInt method not supported.");
    }

    /**
     * Updates the designated column with a <code>long</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateLong(int columnIndex, long x) 
        throws SQLException
    {
        throw new SQLException("updateLong method not supported.");
    }

    /**
     * Updates the designated column with a <code>float</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateFloat(int columnIndex, float x) 
        throws SQLException
    {
        throw new SQLException("updateFloat method not supported.");
    }

    /**
     * Updates the designated column with a <code>double</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateDouble(int columnIndex, double x) 
        throws SQLException
    {
        throw new SQLException("updateDouble method not supported.");
    }

    /**
     * Updates the designated column with a <code>java.math.BigDecimal</code> 
	 * value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBigDecimal(int columnIndex, BigDecimal x) 
        throws SQLException
    {
        throw new SQLException("updateBigDecimal method not supported.");
    }

    /**
     * Updates the designated column with a <code>String</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateString(int columnIndex, String x) 
        throws SQLException
    {
        throw new SQLException("updateString method not supported.");
    }

    /**
     * Updates the designated column with a <code>byte</code> array value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBytes(int columnIndex, byte x[]) 
        throws SQLException
    {
        throw new SQLException("updateBytes method not supported.");
    }

    /**
     * Updates the designated column with a <code>java.sql.Date</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateDate(int columnIndex, java.sql.Date x) 
        throws SQLException
    {
        throw new SQLException("updateDate method not supported.");
    }

    /**
     * Updates the designated column with a <code>java.sql.Time</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateTime(int columnIndex, java.sql.Time x) 
        throws SQLException
    {
        throw new SQLException("updateTime method not supported.");
    }

    /**
     * Updates the designated column with a <code>java.sql.Timestamp</code>
	 * value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateTimestamp(int columnIndex, java.sql.Timestamp x)
      throws SQLException
    {
        throw new SQLException("updateTimestamp method not supported.");
    }

    /** 
     * Updates the designated column with an ascii stream value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateAsciiStream(int columnIndex, 
			   java.io.InputStream x, 
			   int length) 
        throws SQLException
    {
        throw new SQLException("updateAsciiStream method not supported.");
    }

    /** 
     * Updates the designated column with a binary stream value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value     
     * @param length the length of the stream
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBinaryStream(int columnIndex, 
			    java.io.InputStream x,
			    int length) 
        throws SQLException
    {
        throw new SQLException("updateBinaryStream method not supported.");
    }

    /**
     * Updates the designated column with a character stream value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateCharacterStream(int columnIndex,
			     java.io.Reader x,
			     int length) 
        throws SQLException
    {
        throw new SQLException("updateCharacterStream method not supported.");
    }

    /**
     * Updates the designated column with an <code>Object</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param scale for <code>java.sql.Types.DECIMA</code>
	 *  or <code>java.sql.Types.NUMERIC</code> types,
     *  this is the number of digits after the decimal point.  For all other
     *  types this value will be ignored.
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateObject(int columnIndex, Object x, int scale)
        throws SQLException
    {
        throw new SQLException("updateObject method not supported.");
    }

    /**
     * Updates the designated column with an <code>Object</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateObject(int columnIndex, Object x) 
        throws SQLException
    {
        throw new SQLException("updateObject method not supported.");
    }

    /**
     * Updates the designated column with a <code>null</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateNull(String columnName) 
        throws SQLException 
    {
        throw new SQLException("updateNull method not supported.");
    }

    /**
     * Updates the designated column with a <code>boolean</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBoolean(String columnName, boolean x) 
        throws SQLException
    {
        throw new SQLException("updateBoolean method not supported.");
    }

    /**
     * Updates the designated column with a <code>byte</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateByte(String columnName, byte x) 
        throws SQLException
    {
        throw new SQLException("updateByte method not supported.");
    }

    /**
     * Updates the designated column with a <code>short</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateShort(String columnName, short x) 
        throws SQLException
    {
        throw new SQLException("updateShort method not supported.");
    }

    /**
     * Updates the designated column with an <code>int</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateInt(String columnName, int x) 
        throws SQLException
    {
        throw new SQLException("updateInt method not supported.");
    }

    /**
     * Updates the designated column with a <code>long</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateLong(String columnName, long x) 
        throws SQLException
    {
        throw new SQLException("updateLong method not supported.");
    }

    /**
     * Updates the designated column with a <code>float	</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateFloat(String columnName, float x) 
        throws SQLException
    {
        throw new SQLException("updateFloat method not supported.");
    }

    /**
     * Updates the designated column with a <code>double</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateDouble(String columnName, double x) 
        throws SQLException
    {
        throw new SQLException("updateDouble method not supported.");
    }

    /**
     * Updates the designated column with a <code>java.sql.BigDecimal</code>
	 * value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBigDecimal(String columnName, BigDecimal x) 
        throws SQLException
    {
        throw new SQLException("updateBigDecimal method not supported.");
    }

    /**
     * Updates the designated column with a <code>String</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateString(String columnName, String x) 
        throws SQLException
    {
        throw new SQLException("updateString method not supported.");
    }

    /**
     * Updates the designated column with a <code>boolean</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * JDBC 2.0
     *  
     * Updates a column with a byte array value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBytes(String columnName, byte x[]) 
        throws SQLException
    {
        throw new SQLException("updateBytes method not supported.");
    }

    /**
     * Updates the designated column with a <code>java.sql.Date</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateDate(String columnName, java.sql.Date x) 
        throws SQLException
    {
        throw new SQLException("updateDate method not supported.");
    }

    /**
     * Updates the designated column with a <code>java.sql.Time</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateTime(String columnName, java.sql.Time x) 
        throws SQLException
    {
        throw new SQLException("updateTime method not supported.");
    }

    /**
     * Updates the designated column with a <code>java.sql.Timestamp</code>
	 * value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateTimestamp(String columnName, java.sql.Timestamp x)
        throws SQLException
    {
        throw new SQLException("updateTimestamp method not supported.");
    }

    /** 
     * Updates the designated column with an ascii stream value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateAsciiStream(String columnName, 
			   java.io.InputStream x, 
			   int length) 
        throws SQLException
    {
        throw new SQLException("updateAsciiStream method not supported.");
    }

    /** 
     * Updates the designated column with a binary stream value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBinaryStream(String columnName, 
			    java.io.InputStream x,
			    int length) 
        throws SQLException
    {
        throw new SQLException("updateBinaryStream method not supported.");
    }

    /**
     * Updates the designated column with a character stream value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateCharacterStream(String columnName,
			     java.io.Reader reader,
			     int length) 
        throws SQLException
    {
        throw new SQLException("updateCharacterStream method not supported.");
    }

    /**
     * Updates the designated column with an <code>Object</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param scale for <code>java.sql.Types.DECIMA</code>
	 *  or <code>java.sql.Types.NUMERIC</code> types,
     *  this is the number of digits after the decimal point.  For all other
     *  types this value will be ignored.
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateObject(String columnName, Object x, int scale)
        throws SQLException
    {
        throw new SQLException("updateObject method not supported.");
    }

    /**
     * Updates the designated column with an <code>Object</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not 
     * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateObject(String columnName, Object x) 
        throws SQLException
    {
        throw new SQLException("updateObject method not supported.");
    }

    /**
     * Inserts the contents of the insert row into this 
	 * <code>ResultSet</code> objaect and into the database.  
	 * The cursor must be on the insert row when this method is called.
     *
     * @exception SQLException,
     * if this method is called when the cursor is not on the insert row,
	 * or if not all of non-nullable columns in
	 * the insert row have been given a value
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void insertRow() 
        throws SQLException
    {
        throw new SQLException("insertRow method not supported.");
    }

    /**
     * Updates the underlying database with the new contents of the
     * current row of this <code>ResultSet</code> object.
	 * This method cannot be called when the cursor is on the insert row.
     *
     * @exception SQLException or
     * if this method is called when the cursor is on the insert row
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateRow() 
        throws SQLException
    {
        throw new SQLException("updateRow method not supported.");
    }

    /**
     * Deletes the current row from this <code>ResultSet</code> object 
	 * and from the underlying database.  This method cannot be called when
	 * the cursor is on the insert row.
     *
     * @exception SQLException
	 * or if this method is called when the cursor is on the insert row
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void deleteRow() 
        throws SQLException
    {
        throw new SQLException("deleteRow method not supported.");
    }

    /**
     * Refreshes the current row with its most recent value in 
     * the database.  This method cannot be called when
	 * the cursor is on the insert row.
     *
     * <P>The <code>refreshRow</code> method provides a way for an 
	 * application to 
     * explicitly tell the JDBC driver to refetch a row(s) from the
     * database.  An application may want to call <code>refreshRow</code> when 
     * caching or prefetching is being done by the JDBC driver to
     * fetch the latest value of a row from the database.  The JDBC driver 
     * may actually refresh multiple rows at once if the fetch size is 
     * greater than one.
     * 
     * <P> All values are refetched subject to the transaction isolation 
     * level and cursor sensitivity.  If <code>refreshRow</code> is called after
     * calling an <code>updateXXX</code> method, but before calling
	 * the method <code>updateRow</code>, then the
     * updates made to the row are lost.  Calling the method
	 * <code>refreshRow</code> frequently will likely slow performance.
     *
     * @exception SQLException if a database access error
	 * occurs or if this method is called when the cursor is on the insert row
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void refreshRow() 
        throws SQLException
    {
        throw new SQLException("refreshRow method not supported.");
    }

    /**
	 * Cancels the updates made to the current row in this
	 * <code>ResultSet</code> object.
     * This method may be called after calling an
     * <code>updateXXX</code> method(s) and before calling
	 * the method <code>updateRow</code> to roll back 
     * the updates made to a row.  If no updates have been made or 
     * <code>updateRow</code> has already been called, this method has no 
     * effect.
     *
     * @exception SQLException if a database access error
	 * occurs or if this method is called when the cursor is on the insert row
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void cancelRowUpdates() 
        throws SQLException
    {
        throw new SQLException("cancelRowUpdates method not supported.");
    }

    /**
     * Moves the cursor to the insert row.  The current cursor position is 
     * remembered while the cursor is positioned on the insert row.
     *
     * The insert row is a special row associated with an updatable
     * result set.  It is essentially a buffer where a new row may
     * be constructed by calling the <code>updateXXX</code> methods prior to 
     * inserting the row into the result set.  
     *
     * Only the <code>updateXXX</code>, <code>getXXX</code>,
	 * and <code>insertRow</code> methods may be 
     * called when the cursor is on the insert row.  All of the columns in 
     * a result set must be given a value each time this method is
     * called before calling <code>insertRow</code>.  
	 * An <code>updateXXX</code> method must be called before a
     * <code>getXXX</code> method can be called on a column value.
     *
     * @exception SQLException
     * or the result set is not updatable
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void moveToInsertRow() 
        throws SQLException
    {
        throw new SQLException("moveToInsertRow method not supported.");
    }

    /**
     * Moves the cursor to the remembered cursor position, usually the
     * current row.  This method has no effect if the cursor is not on 
	 * the insert row. 
     *
     * @exception SQLException
     * or the result set is not updatable
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void moveToCurrentRow() 
        throws SQLException
    {
        throw new SQLException("moveToCurrentRow method not supported.");
    }

    /**
     * Returns the <code>Statement</code> object that produced this 
	 * <code>ResultSet</code> object.
	 * If the result set was generated some other way, such as by a
	 * <code>DatabaseMetaData</code> method, this method returns 
	 * <code>null</code>.
     *
     * @return the <code>Statment</code> object that produced 
	 * this <code>ResultSet</code> object or <code>null</code>
     * if the result set was produced some other way
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Statement getStatement() 
        throws SQLException
    {
        return _statement;
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as an <code>Object</code>
	 * in the Java programming language.
	 * This method uses the given <code>Map</code> object
     * for the custom mapping of the
     * SQL structured or distinct type that is being retrieved.
     *
     * @param i the first column is 1, the second is 2, ...
     * @param map a <code>java.util.Map</code> object that contains the mapping 
	 * from SQL type names to classes in the Java programming language
     * @return an <code>Object</code> in the Java programming language
	 * representing the SQL value
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Object getObject(int i, java.util.Map map) 
        throws SQLException
    {
        throw new SQLException("getObject method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>Ref</code> object
	 * in the Java programming language.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Ref</code> object representing an SQL <code>REF</code> value
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Ref getRef(int i) 
        throws SQLException
    {
        throw new SQLException("getRef method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>Blob</code> object
	 * in the Java programming language.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Blob</code> object representing the SQL <code>BLOB</code> value in
	 *         the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Blob getBlob(int i) 
        throws SQLException
    {
        throw new SQLException("getBlob method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>Clob</code> object
	 * in the Java programming language.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Clob</code> object representing the SQL <code>CLOB</code> value in
	 *         the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Clob getClob(int i) 
        throws SQLException
    {
        throw new SQLException("getClob method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as an <code>Array</code> object
	 * in the Java programming language.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return an <code>Array</code> object representing the SQL <code>ARRAY</code> value in
	 *         the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Array getArray(int i) 
        throws SQLException
    {
        throw new SQLException("getArray method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as an <code>Object</code>
	 * in the Java programming language.
	 * This method uses the specified <code>Map</code> object for
	 * custom mapping if appropriate.
     *
     * @param colName the name of the column from which to retrieve the value
     * @param map a <code>java.util.Map</code> object that contains the mapping 
	 * from SQL type names to classes in the Java programming language
     * @return an <code>Object</code> representing the SQL value in the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Object getObject(String colName, java.util.Map map) 
        throws SQLException
    {
        throw new SQLException("getObject method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>Ref</code> object
	 * in the Java programming language.
     *
     * @param colName the column name
     * @return a <code>Ref</code> object representing the SQL <code>REF</code> value in
	 *         the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Ref getRef(String colName) 
        throws SQLException
    {
        throw new SQLException("getRef method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>Blob</code> object
	 * in the Java programming language.
     *
     * @param colName the name of the column from which to retrieve the value
     * @return a <code>Blob</code> object representing the SQL <code>BLOB</code> value in
	 *         the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Blob getBlob(String colName) 
        throws SQLException
    {
        throw new SQLException("getBlob method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>Clob</code> object
	 * in the Java programming language.
     *
     * @param colName the name of the column from which to retrieve the value
     * @return a <code>Clob</code> object representing the SQL <code>CLOB</code>
	 * value in the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Clob getClob(String colName) 
        throws SQLException
    {
        throw new SQLException("getClob method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as an <code>Array</code> object
	 * in the Java programming language.
     *
     * @param colName the name of the column from which to retrieve the value
     * @return an <code>Array</code> object representing the SQL <code>ARRAY</code> value in
	 *         the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Array getArray(String colName) 
        throws SQLException
    {
        throw new SQLException("getArray method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>java.sql.Date</code> object
	 * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the date if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the <code>java.util.Calendar</code> object
	 * to use in constructing the date
     * @return the column value as a <code>java.sql.Date</code> object;
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code> in the Java programming language
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.sql.Date getDate(int columnIndex, Calendar cal) 
        throws SQLException
    {
        throw new SQLException("getDate method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>java.sql.Date</code> object
	 * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the date if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column from which to retrieve the value
     * @param cal the <code>java.util.Calendar</code> object
	 * to use in constructing the date
     * @return the column value as a <code>java.sql.Date</code> object;
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code> in the Java programming language
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.sql.Date getDate(String columnName, Calendar cal) 
        throws SQLException
    {
        throw new SQLException("getDate method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>java.sql.Time</code> object
	 * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the time if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the <code>java.util.Calendar</code> object
	 * to use in constructing the time
     * @return the column value as a <code>java.sql.Time</code> object;
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code> in the Java programming language
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.sql.Time getTime(int columnIndex, Calendar cal) 
        throws SQLException
    {
        throw new SQLException("getTime method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>java.sql.Time</code> object
	 * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the time if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column
     * @param cal the <code>java.util.Calendar</code> object
	 * to use in constructing the time
     * @param cal the calendar to use in constructing the time
     * @return the column value as a <code>java.sql.Time</code> object;
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code> in the Java programming language
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.sql.Time getTime(String columnName, Calendar cal) 
        throws SQLException
    {
        throw new SQLException("getTime method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>java.sql.Timestamp</code> object
	 * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the timestamp if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the <code>java.util.Calendar</code> object
	 * to use in constructing the timestamp
     * @return the column value as a <code>java.sql.Timestamp</code> object;
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code> in the Java programming language
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) 
        throws SQLException
    {
        throw new SQLException("getTimestamp method not supported.");
    }

    /**
     * Returns the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>java.sql.Timestamp</code> object
	 * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the timestamp if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column
     * @param cal the <code>java.util.Calendar</code> object
	 * to use in constructing the date
     * @return the column value as a <code>java.sql.Timestamp</code> object;
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code> in the Java programming language
     * @exception SQLException
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.sql.Timestamp getTimestamp(String columnName, Calendar cal)	
        throws SQLException
    {
        throw new SQLException("getTimestamp method not supported.");
    }
}
