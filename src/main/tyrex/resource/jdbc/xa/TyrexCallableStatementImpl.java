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


import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;


/////////////////////////////////////////////////////////////////////
// TyrexCallableStatementImpl
/////////////////////////////////////////////////////////////////////

/**
 * This class implements java.sql.CallableStatement so that it returned
 * when prepareCall is called on {@link TyrexConnection} object
 * <p>
 * The reason for this class is for the method java.sql.Statement#getConnection
 * to return the correct connection.
 * <p>
 * This class is thread safe.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
final class TyrexCallableStatementImpl 
    extends TyrexPreparedStatementImpl 
    implements CallableStatement
{

    /**
     * Create the TyrexCallableStatementImpl with the
     * specified arguments.
     *
     * @param statement the underlying statement
     * @param connection the connection that created
     *      the statement.
     * @throws SQLException if there is a problem creating the statement
     */
    TyrexCallableStatementImpl(CallableStatement statement,
                               TyrexConnection connection)
        throws SQLException
    {
        super(statement, connection);
    }

     /**
     * Registers the OUT parameter in ordinal position 
     * <code>parameterIndex</code> to the JDBC type 
     * <code>sqlType</code>.  All OUT parameters must be registered
     * before a stored procedure is executed.
     * <p>
     * The JDBC type specified by <code>sqlType</code> for an OUT
     * parameter determines the Java type that must be used
     * in the <code>get</code> method to read the value of that parameter.
     * <p>
     * If the JDBC type expected to be returned to this output parameter
     * is specific to this particular database, <code>sqlType</code>
     * should be <code>java.sql.Types.OTHER</code>.  The method 
     * {@link #getObject} retrieves the value.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @param sqlType the JDBC type code defined by <code>java.sql.Types</code>.
     * If the parameter is of JDBC type <code>NUMERIC</code>
     * or <code>DECIMAL</code>, the version of
     * <code>registerOutParameter</code> that accepts a scale value 
     * should be used.
     * @exception SQLException if a database access error occurs
     * @see Types 
     */
    public final synchronized void registerOutParameter(int parameterIndex, int sqlType)
        throws SQLException
    {
        getCallableStatement().registerOutParameter(parameterIndex, sqlType);
    }

    /**
     * Registers the parameter in ordinal position
     * <code>parameterIndex</code> to be of JDBC type
     * <code>sqlType</code>.  This method must be called
     * before a stored procedure is executed.
     * <p>
     * The JDBC type specified by <code>sqlType</code> for an OUT
     * parameter determines the Java type that must be used
     * in the <code>get</code> method to read the value of that parameter.
     * <p>
     * This version of <code>registerOutParameter</code> should be
     * used when the parameter is of JDBC type <code>NUMERIC</code>
     * or <code>DECIMAL</code>.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param sqlType SQL type code defined by <code>java.sql.Types</code>.
     * @param scale the desired number of digits to the right of the
     * decimal point.  It must be greater than or equal to zero.
     * @exception SQLException if a database access error occurs
     * @see Types 
     */
    public final synchronized void registerOutParameter(int parameterIndex, int sqlType, int scale)
        throws SQLException
    {
        getCallableStatement().registerOutParameter(parameterIndex, sqlType, scale);
    }

    /**
     * Indicates whether or not the last OUT parameter read had the value of
     * SQL <code>NULL</code>.  Note that this method should be called only after
     * calling a <code>getXXX</code> method; otherwise, there is no value to use in 
     * determining whether it is <code>null</code> or not.
     * @return <code>true</code> if the last parameter read was SQL
     * <code>NULL</code>; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public final synchronized boolean wasNull() 
        throws SQLException
    {
        return getCallableStatement().wasNull();
    }

    /**
     * Retrieves the value of a JDBC <code>CHAR</code>, <code>VARCHAR</code>, 
     * or <code>LONGVARCHAR</code> parameter as a <code>String</code> in 
     * the Java programming language.
     * <p>
     * For the fixed-length type JDBC <code>CHAR</code>,
     * the <code>String</code> object
     * returned has exactly the same value the JDBC
     * <code>CHAR</code> value had in the
     * database, including any padding added by the database.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @return the parameter value. If the value is SQL <code>NULL</code>, the result 
     * is <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized String getString(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getString(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>BIT</code> parameter as a <code>boolean</code> 
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result 
     * is <code>false</code>.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized boolean getBoolean(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getBoolean(parameterIndex);
    }
    
    /**
     * Gets the value of a JDBC <code>TINYINT</code> parameter as a <code>byte</code> 
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result 
     * is 0.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized byte getByte(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getByte(parameterIndex);
    }
    
    /**
     * Gets the value of a JDBC <code>SMALLINT</code> parameter as a <code>short</code>
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result 
     * is 0.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized short getShort(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getShort(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>INTEGER</code> parameter as an <code>int</code>
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result 
     * is 0.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized int getInt(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getInt(parameterIndex);
    }
    
    /**
     * Gets the value of a JDBC <code>BIGINT</code> parameter as a <code>long</code>
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result 
     * is 0.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized long getLong(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getLong(parameterIndex);
    }
    
    /**
     * Gets the value of a JDBC <code>FLOAT</code> parameter as a <code>float</code>
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result 
     * is 0.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized float getFloat(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getFloat(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>DOUBLE</code> parameter as a <code>double</code>
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result 
     * is 0.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized double getDouble(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getDouble(parameterIndex);
    }

    /** 
     * Gets the value of a JDBC <code>NUMERIC</code> parameter as a 
     * <code>java.math.BigDecimal</code> object with scale digits to
     * the right of the decimal point.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @param scale the number of digits to the right of the decimal point 
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result is
     * <code>null</code>. 
     * @exception SQLException if a database access error occurs
     * @deprecated
     */
    public final synchronized BigDecimal getBigDecimal(int parameterIndex, int scale)
        throws SQLException
    {
        return getCallableStatement().getBigDecimal(parameterIndex, scale);
    }

    /**
     * Gets the value of a JDBC <code>BINARY</code> or <code>VARBINARY</code> 
     * parameter as an array of <code>byte</code> values in the Java
     * programming language.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result is 
     *  <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized byte[] getBytes(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getBytes(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>DATE</code> parameter as a 
     * <code>java.sql.Date</code> object.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result 
     * is <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized Date getDate(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getDate(parameterIndex);
    }

    /**
     * Get the value of a JDBC <code>TIME</code> parameter as a 
     * <code>java.sql.Time</code> object.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result 
     * is <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized Time getTime(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getTime(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>TIMESTAMP</code> parameter as a 
     * <code>java.sql.Timestamp</code> object.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result 
     * is <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized Timestamp getTimestamp(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getTimestamp(parameterIndex);
    }

    //----------------------------------------------------------------------
    // Advanced features:


    /**
     * Gets the value of a parameter as an <code>Object</code> in the Java 
     * programming language.
     * <p>
     * This method returns a Java object whose type corresponds to the JDBC
     * type that was registered for this parameter using the method
     * <code>registerOutParameter</code>.  By registering the target JDBC
     * type as <code>java.sql.Types.OTHER</code>, this method can be used
     * to read database-specific abstract data types.
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @return A <code>java.lang.Object</code> holding the OUT parameter value.
     * @exception SQLException if a database access error occurs
     * @see Types 
     */
    public final synchronized Object getObject(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getObject(parameterIndex);
    }


    //--------------------------JDBC 2.0-----------------------------

    /**
     *
     * Gets the value of a JDBC <code>NUMERIC</code> parameter as a 
     * <code>java.math.BigDecimal</code> object with as many digits to the
     * right of the decimal point as the value contains.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value in full precision.  If the value is 
     * SQL <code>NULL</code>, the result is <code>null</code>. 
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public final synchronized BigDecimal getBigDecimal(int parameterIndex) 
        throws SQLException
    {
        return getCallableStatement().getBigDecimal(parameterIndex);
    }

    /**
     *
     * Returns an object representing the value of OUT parameter 
     * <code>i</code> and uses <code>map</code> for the custom
     * mapping of the parameter value.
     * <p>
     * This method returns a Java object whose type corresponds to the
     * JDBC type that was registered for this parameter using the method
     * <code>registerOutParameter</code>.  By registering the target
     * JDBC type as <code>java.sql.Types.OTHER</code>, this method can
     * be used to read database-specific abstract data types.  
     * @param i the first parameter is 1, the second is 2, and so on
     * @param map the mapping from SQL type names to Java classes
     * @return a <code>java.lang.Object</code> holding the OUT parameter value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
     public final synchronized Object  getObject(int i, Map map) 
        throws SQLException
     {
         return getCallableStatement().getObject(i, map);
     }

    /**
     *
     * Gets the value of a JDBC <code>REF(&lt;structured-type&gt;)</code>
     * parameter as a {@link Ref} object in the Java programming language.
     * @param i the first parameter is 1, the second is 2, 
     * and so on
     * @return the parameter value as a <code>Ref</code> object in the
     * Java programming language.  If the value was SQL <code>NULL</code>, the value
     * <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
     public final synchronized Ref getRef(int i) 
        throws SQLException
     {
         return getCallableStatement().getRef(i);
     }

    /**
     *
     * Gets the value of a JDBC <code>BLOB</code> parameter as a
     * {@link Blob} object in the Java programming language.
     * @param i the first parameter is 1, the second is 2, and so on
     * @return the parameter value as a <code>Blob</code> object in the
     * Java programming language.  If the value was SQL <code>NULL</code>, the value
     * <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
     public final synchronized Blob getBlob(int i) 
        throws SQLException
     {
         return getCallableStatement().getBlob(i);
     }

    /**
     *
     * Gets the value of a JDBC <code>CLOB</code> parameter as a
     * <code>Clob</code> object in the Java programming language.
     * @param i the first parameter is 1, the second is 2, and
     * so on
     * @return the parameter value as a <code>Clob</code> object in the
     * Java programming language.  If the value was SQL <code>NULL</code>, the
     * value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
     public final synchronized Clob getClob (int i) 
        throws SQLException
     {
         return getCallableStatement().getClob (i);
     }

    /**
     *
     * Gets the value of a JDBC <code>ARRAY</code> parameter as an
     * {@link Array} object in the Java programming language.
     * @param i the first parameter is 1, the second is 2, and 
     * so on
     * @return the parameter value as an <code>Array</code> object in
     * the Java programming language.  If the value was SQL <code>NULL</code>, the
     * value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
     public final synchronized Array getArray(int i) 
        throws SQLException
     {
         return getCallableStatement().getArray(i);
     }

    /**
     * Gets the value of a JDBC <code>DATE</code> parameter as a 
     * <code>java.sql.Date</code> object, using
     * the given <code>Calendar</code> object
     * to construct the date.
     * With a <code>Calendar</code> object, the driver
     * can calculate the date taking into account a custom timezone and locale.
     * If no <code>Calendar</code> object is specified, the driver uses the
     * default timezone and locale.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @param cal the <code>Calendar</code> object the driver will use
     *            to construct the date
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result is 
     * <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized Date getDate(int parameterIndex, Calendar cal) 
        throws SQLException
    {
        return getCallableStatement().getDate(parameterIndex, cal);
    }

    /**
     * Gets the value of a JDBC <code>TIME</code> parameter as a 
     * <code>java.sql.Time</code> object, using
     * the given <code>Calendar</code> object
     * to construct the time.
     * With a <code>Calendar</code> object, the driver
     * can calculate the time taking into account a custom timezone and locale.
     * If no <code>Calendar</code> object is specified, the driver uses the
     * default timezone and locale.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param cal the <code>Calendar</code> object the driver will use
     *            to construct the time
     * @return the parameter value; if the value is SQL <code>NULL</code>, the result is 
     * <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized Time getTime(int parameterIndex, Calendar cal) 
        throws SQLException
    {
        return getCallableStatement().getTime(parameterIndex, cal);
    }

    /**
     * Gets the value of a JDBC <code>TIMESTAMP</code> parameter as a
     * <code>java.sql.Timestamp</code> object, using
     * the given <code>Calendar</code> object to construct
     * the <code>Timestamp</code> object.
     * With a <code>Calendar</code> object, the driver
     * can calculate the timestamp taking into account a custom timezone and locale.
     * If no <code>Calendar</code> object is specified, the driver uses the
     * default timezone and locale.
     *
     *
     * @param parameterIndex the first parameter is 1, the second is 2, 
     * and so on
     * @param cal the <code>Calendar</code> object the driver will use
     *            to construct the timestamp
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result is 
     * <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public final synchronized Timestamp getTimestamp(int parameterIndex, Calendar cal) 
        throws SQLException
    {
        return getCallableStatement().getTimestamp(parameterIndex, cal);
    }


    /**
     *
     * Registers the designated output parameter.  This version of 
     * the method <code>registerOutParameter</code>
     * should be used for a user-named or REF output parameter.  Examples
     * of user-named types include: STRUCT, DISTINCT, JAVA_OBJECT, and
     * named array types.
     *
     * Before executing a stored procedure call, you must explicitly
     * call <code>registerOutParameter</code> to register the type from
     * <code>java.sql.Types</code> for each
     * OUT parameter.  For a user-named parameter the fully-qualified SQL
     * type name of the parameter should also be given, while a REF
     * parameter requires that the fully-qualified type name of the
     * referenced type be given.  A JDBC driver that does not need the
     * type code and type name information may ignore it.   To be portable,
     * however, applications should always provide these values for
     * user-named and REF parameters.
     *
     * Although it is intended for user-named and REF parameters,
     * this method may be used to register a parameter of any JDBC type.
     * If the parameter does not have a user-named or REF type, the
     * typeName parameter is ignored.
     *
     * <P><B>Note:</B> When reading the value of an out parameter, you
     * must use the <code>getXXX</code> method whose Java type XXX corresponds to the
     * parameter's registered SQL type.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,...
     * @param sqlType a value from {@link java.sql.Types}
     * @param typeName the fully-qualified name of an SQL structured type
     * @exception SQLException if a database access error occurs
     * @see Types
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public final synchronized void registerOutParameter (int paramIndex, int sqlType, String typeName)
        throws SQLException
    {
        getCallableStatement().registerOutParameter (paramIndex, sqlType, typeName);
    }

    /**
     * Return the callable statement.
     *
     * @return the callable statement.
     * @throws SQLException if the statement is closed.
     */
    protected final CallableStatement getCallableStatement()
        throws SQLException
    {
        return (CallableStatement)getStatement();
    }
}
