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


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;


/////////////////////////////////////////////////////////////////////
// TyrexDatabaseMetaDataImpl
/////////////////////////////////////////////////////////////////////


/**
 * This is a implementation of java.sql.DatabaseMetaData that
 * returns Tyrex implementations of java.sql.Connection 
 * ({@link #getConnection}) and java.sql.ResultSet (various
 * get methods). This is done so that the actual connections
 * can never be accessed directly so that they can be pooled
 * ({@link tyrex.jdbc.ServerDataSource} and
 * {@link tyrex.jdbc.xa.XADataSourceImpl}).
 * <p>
 * This class is thread safe.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
final class TyrexDatabaseMetaDataImpl 
    implements DatabaseMetaData
{
    /**
     * The underlying meta data
     */
    private DatabaseMetaData _databaseMetaData;

    /**
     * The tyrex connection associated with meta data
     */
    private TyrexConnection _connection;

    /**
     * The driver major version
     */
    private final int _driverMajorVersion;


    /**
     * The driver minor version
     */
    private final int _driverMinorVersion;


    /**
     * Create the TyrexDatabaseMetaDataImpl
     *
     * @param databaseMetaData the underlying meta data
     * @param connection the connection
     * @throws SQLException if the connection is closed
     */
    TyrexDatabaseMetaDataImpl(DatabaseMetaData databaseMetaData,
                              TyrexConnection connection)
        throws SQLException
    {
        if (null == databaseMetaData) {
            throw new IllegalArgumentException("The argument 'databaseMetaData' is null.");
        }
        if (null == connection) {
            throw new IllegalArgumentException("The argument 'connection' is null.");
        }

        _databaseMetaData = databaseMetaData;
        _connection = connection;
        _driverMajorVersion = databaseMetaData.getDriverMajorVersion();
        _driverMinorVersion = databaseMetaData.getDriverMinorVersion();
    }

    /**
     * Can all the procedures returned by getProcedures be called by the
     * current user?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean allProceduresAreCallable() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.allProceduresAreCallable();
    }

    /**
     * Can all the tables returned by getTable be SELECTed by the
     * current user?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean allTablesAreSelectable() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.allTablesAreSelectable();
    }

    /**
     * What's the url for this database?
     *
     * @return the url or null if it cannot be generated
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getURL() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getURL();
    }

    /**
     * What's our user name as known to the database?
     *
     * @return our database user name
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getUserName() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getUserName();
    }

    /**
     * Is the database in read-only mode?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean isReadOnly() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.isReadOnly();
    }

    /**
     * Are NULL values sorted high?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean nullsAreSortedHigh() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.nullsAreSortedHigh();
    }

    /**
     * Are NULL values sorted low?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean nullsAreSortedLow() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.nullsAreSortedLow();
    }

    /**
     * Are NULL values sorted at the start regardless of sort order?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean nullsAreSortedAtStart() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.nullsAreSortedAtStart();
    }

    /**
     * Are NULL values sorted at the end regardless of sort order?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean nullsAreSortedAtEnd() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.nullsAreSortedAtEnd();
    }

    /**
     * What's the name of this database product?
     *
     * @return database product name
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getDatabaseProductName() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getDatabaseProductName();
    }

    /**
     * What's the version of this database product?
     *
     * @return database version
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getDatabaseProductVersion() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getDatabaseProductVersion();
    }

    /**
     * What's the name of this JDBC driver?
     *
     * @return JDBC driver name
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getDriverName() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getDriverName();
    }

    /**
     * What's the version of this JDBC driver?
     *
     * @return JDBC driver version
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getDriverVersion() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getDriverVersion();
    }

    /**
     * What's this JDBC driver's major version number?
     *
     * @return JDBC driver major version
     */
    public int getDriverMajorVersion()
    {
        return _driverMajorVersion;
    }

    /**
     * What's this JDBC driver's minor version number?
     *
     * @return JDBC driver minor version number
     */
    public int getDriverMinorVersion()
    {
        return _driverMinorVersion;
    }

    /**
     * Does the database store tables in a local file?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean usesLocalFiles() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.usesLocalFiles();
    }

    /**
     * Does the database use a file for each table?
     *
     * @return true if the database uses a local file for each table
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean usesLocalFilePerTable() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.usesLocalFilePerTable();
    }

    /**
     * Does the database treat mixed case unquoted SQL identifiers as
     * case sensitive and as a result store them in mixed case?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver will always return false.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsMixedCaseIdentifiers() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsMixedCaseIdentifiers();
    }

    /**
     * Does the database treat mixed case unquoted SQL identifiers as
     * case insensitive and store them in upper case?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean storesUpperCaseIdentifiers() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.storesUpperCaseIdentifiers();
    }

    /**
     * Does the database treat mixed case unquoted SQL identifiers as
     * case insensitive and store them in lower case?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean storesLowerCaseIdentifiers() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.storesLowerCaseIdentifiers();
    }

    /**
     * Does the database treat mixed case unquoted SQL identifiers as
     * case insensitive and store them in mixed case?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean storesMixedCaseIdentifiers() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.storesMixedCaseIdentifiers();
    }

    /**
     * Does the database treat mixed case quoted SQL identifiers as
     * case sensitive and as a result store them in mixed case?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver will always return true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsMixedCaseQuotedIdentifiers() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsMixedCaseQuotedIdentifiers();
    }

    /**
     * Does the database treat mixed case quoted SQL identifiers as
     * case insensitive and store them in upper case?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean storesUpperCaseQuotedIdentifiers() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.storesUpperCaseQuotedIdentifiers();
    }

    /**
     * Does the database treat mixed case quoted SQL identifiers as
     * case insensitive and store them in lower case?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean storesLowerCaseQuotedIdentifiers() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.storesLowerCaseQuotedIdentifiers();
    }

    /**
     * Does the database treat mixed case quoted SQL identifiers as
     * case insensitive and store them in mixed case?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean storesMixedCaseQuotedIdentifiers() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.storesMixedCaseQuotedIdentifiers();
    }

    /**
     * What's the string used to quote SQL identifiers?
     * This returns a space " " if identifier quoting isn't supported.
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> 
     * driver always uses a double quote character.
     *
     * @return the quoting string
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getIdentifierQuoteString() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getIdentifierQuoteString();
    }

    /**
     * Gets a comma-separated list of all a database's SQL keywords
     * that are NOT also SQL92 keywords.
     *
     * @return the list 
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getSQLKeywords() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getSQLKeywords();
    }

    /**
     * Gets a comma-separated list of math functions.  These are the 
     * X/Open CLI math function names used in the JDBC function escape 
     * clause.
     *
     * @return the list
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getNumericFunctions() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getNumericFunctions();
    }

    /**
     * Gets a comma-separated list of string functions.  These are the 
     * X/Open CLI string function names used in the JDBC function escape 
     * clause.
     *
     * @return the list
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getStringFunctions() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getStringFunctions();
    }

    /**
     * Gets a comma-separated list of system functions.  These are the 
     * X/Open CLI system function names used in the JDBC function escape 
     * clause.
     *
     * @return the list
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getSystemFunctions() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getSystemFunctions();
    }

    /**
     * Gets a comma-separated list of time and date functions.
     *
     * @return the list
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getTimeDateFunctions() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getTimeDateFunctions();
    }

    /**
     * Gets the string that can be used to escape wildcard characters.
     * This is the string that can be used to escape '_' or '%' in
     * the string pattern style catalog search parameters.
     *
     * <P>The '_' character represents any single character.
     * <P>The '%' character represents any sequence of zero or 
     * more characters.
     *
     * @return the string used to escape wildcard characters
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getSearchStringEscape() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getSearchStringEscape();
    }

    /**
     * Gets all the "extra" characters that can be used in unquoted
     * identifier names (those beyond a-z, A-Z, 0-9 and _).
     *
     * @return the string containing the extra characters 
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getExtraNameCharacters() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getExtraNameCharacters();
    }

    //--------------------------------------------------------------------
    // Functions describing which features are supported.

    /**
     * Is "ALTER TABLE" with add column supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsAlterTableWithAddColumn() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsAlterTableWithAddColumn();
    }

    /**
     * Is "ALTER TABLE" with drop column supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsAlterTableWithDropColumn() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsAlterTableWithDropColumn();
    }

    /**
     * Is column aliasing supported? 
     *
     * <P>If so, the SQL AS clause can be used to provide names for
     * computed columns or to provide alias names for columns as
     * required.
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsColumnAliasing() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsColumnAliasing();
    }

    /**
     * Are concatenations between NULL and non-NULL values NULL?
     * For SQL-92 compliance, a JDBC technology-enabled driver will
     * return <code>true</code>.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean nullPlusNonNullIsNull() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.nullPlusNonNullIsNull();
    }

    /**
     * Is the CONVERT function between SQL types supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsConvert() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsConvert();
    }

    /**
     * Is CONVERT between the given SQL types supported?
     *
     * @param fromType the type to convert from
     * @param toType the type to convert to     
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @see Types
     */
    public synchronized boolean supportsConvert(int fromType, int toType) 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsConvert(fromType, toType);
    }

    /**
     * Are table correlation names supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsTableCorrelationNames() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsTableCorrelationNames();
    }

    /**
     * If table correlation names are supported, are they restricted
     * to be different from the names of the tables?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsDifferentTableCorrelationNames() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsDifferentTableCorrelationNames();
    }

    /**
     * Are expressions in "ORDER BY" lists supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsExpressionsInOrderBy() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsExpressionsInOrderBy();
    }

    /**
     * Can an "ORDER BY" clause use columns not in the SELECT statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsOrderByUnrelated() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsOrderByUnrelated();
    }

    /**
     * Is some form of "GROUP BY" clause supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsGroupBy() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsGroupBy();
    }

    /**
     * Can a "GROUP BY" clause use columns not in the SELECT?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsGroupByUnrelated() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsGroupByUnrelated();
    }

    /**
     * Can a "GROUP BY" clause add columns not in the SELECT
     * provided it specifies all the columns in the SELECT?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsGroupByBeyondSelect() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsGroupByBeyondSelect();
    }

    /**
     * Is the escape character in "LIKE" clauses supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsLikeEscapeClause() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsLikeEscapeClause();
    }

    /**
     * Are multiple <code>ResultSet</code> from a single execute supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsMultipleResultSets() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsMultipleResultSets();
    }

    /**
     * Can we have multiple transactions open at once (on different
     * connections)?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsMultipleTransactions() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsMultipleTransactions();
    }

    /**
     * Can columns be defined as non-nullable?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsNonNullableColumns() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsNonNullableColumns();
    }

    /**
     * Is the ODBC Minimum SQL grammar supported?
     *
     * All JDBC Compliant<sup><font size=-2>TM</font></sup> drivers must return true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsMinimumSQLGrammar() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsMinimumSQLGrammar();
    }

    /**
     * Is the ODBC Core SQL grammar supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsCoreSQLGrammar() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsCoreSQLGrammar();
    }

    /**
     * Is the ODBC Extended SQL grammar supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsExtendedSQLGrammar() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsExtendedSQLGrammar();
    }

    /**
     * Is the ANSI92 entry level SQL grammar supported?
     *
     * All JDBC Compliant<sup><font size=-2>TM</font></sup> drivers must return true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsANSI92EntryLevelSQL() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsANSI92EntryLevelSQL();
    }

    /**
     * Is the ANSI92 intermediate SQL grammar supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsANSI92IntermediateSQL() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsANSI92IntermediateSQL();
    }

    /**
     * Is the ANSI92 full SQL grammar supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsANSI92FullSQL() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsANSI92FullSQL();
    }

    /**
     * Is the SQL Integrity Enhancement Facility supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsIntegrityEnhancementFacility() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsIntegrityEnhancementFacility();
    }

    /**
     * Is some form of outer join supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsOuterJoins() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsOuterJoins();
    }

    /**
     * Are full nested outer joins supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsFullOuterJoins() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsFullOuterJoins();
    }

    /**
     * Is there limited support for outer joins?  (This will be true
     * if supportFullOuterJoins is true.)
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsLimitedOuterJoins() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsLimitedOuterJoins();
    }

    /**
     * What's the database vendor's preferred term for "schema"?
     *
     * @return the vendor term
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getSchemaTerm() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getSchemaTerm();
    }

    /**
     * What's the database vendor's preferred term for "procedure"?
     *
     * @return the vendor term
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getProcedureTerm() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getProcedureTerm();
    }

    /**
     * What's the database vendor's preferred term for "catalog"?
     *
     * @return the vendor term
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getCatalogTerm() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getCatalogTerm();
    }

    /**
     * Does a catalog appear at the start of a qualified table name?
     * (Otherwise it appears at the end)
     *
     * @return true if it appears at the start 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean isCatalogAtStart() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.isCatalogAtStart();
    }

    /**
     * What's the separator between catalog and table name?
     *
     * @return the separator string
     * @exception SQLException if a database access error occurs
     */
    public synchronized String getCatalogSeparator() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getCatalogSeparator();
    }

    /**
     * Can a schema name be used in a data manipulation statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsSchemasInDataManipulation() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsSchemasInDataManipulation();
    }

    /**
     * Can a schema name be used in a procedure call statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsSchemasInProcedureCalls() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsSchemasInProcedureCalls();
    }

    /**
     * Can a schema name be used in a table definition statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsSchemasInTableDefinitions() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsSchemasInTableDefinitions();
    }

    /**
     * Can a schema name be used in an index definition statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsSchemasInIndexDefinitions() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsSchemasInIndexDefinitions();
    }

    /**
     * Can a schema name be used in a privilege definition statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsSchemasInPrivilegeDefinitions() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsSchemasInPrivilegeDefinitions();
    }

    /**
     * Can a catalog name be used in a data manipulation statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsCatalogsInDataManipulation() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsCatalogsInDataManipulation();
    }

    /**
     * Can a catalog name be used in a procedure call statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsCatalogsInProcedureCalls() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsCatalogsInProcedureCalls();
    }

    /**
     * Can a catalog name be used in a table definition statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsCatalogsInTableDefinitions() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsCatalogsInTableDefinitions();
    }

    /**
     * Can a catalog name be used in an index definition statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsCatalogsInIndexDefinitions() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsCatalogsInIndexDefinitions();
    }

    /**
     * Can a catalog name be used in a privilege definition statement?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsCatalogsInPrivilegeDefinitions() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsCatalogsInPrivilegeDefinitions();
    }


    /**
     * Is positioned DELETE supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsPositionedDelete() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsPositionedDelete();
    }

    /**
     * Is positioned UPDATE supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsPositionedUpdate() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsPositionedUpdate();
    }

    /**
     * Is SELECT for UPDATE supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsSelectForUpdate() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsSelectForUpdate();
    }

    /**
     * Are stored procedure calls using the stored procedure escape
     * syntax supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsStoredProcedures() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsStoredProcedures();
    }

    /**
     * Are subqueries in comparison expressions supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsSubqueriesInComparisons() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsSubqueriesInComparisons();
    }

    /**
     * Are subqueries in 'exists' expressions supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsSubqueriesInExists() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsSubqueriesInExists();
    }

    /**
     * Are subqueries in 'in' statements supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsSubqueriesInIns() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsSubqueriesInIns();
    }

    /**
     * Are subqueries in quantified expressions supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsSubqueriesInQuantifieds() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsSubqueriesInQuantifieds();
    }

    /**
     * Are correlated subqueries supported?
     *
     * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsCorrelatedSubqueries() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsCorrelatedSubqueries();
    }

    /**
     * Is SQL UNION supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsUnion() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsUnion();
    }

    /**
     * Is SQL UNION ALL supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsUnionAll() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsUnionAll();
    }

    /**
     * Can cursors remain open across commits? 
     * 
     * @return <code>true</code> if cursors always remain open;
     *       <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsOpenCursorsAcrossCommit() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsOpenCursorsAcrossCommit();
    }

    /**
     * Can cursors remain open across rollbacks?
     * 
     * @return <code>true</code> if cursors always remain open;
     *       <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsOpenCursorsAcrossRollback() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsOpenCursorsAcrossRollback();
    }

    /**
     * Can statements remain open across commits?
     * 
     * @return <code>true</code> if statements always remain open;
     *       <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsOpenStatementsAcrossCommit() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsOpenStatementsAcrossCommit();
    }

    /**
     * Can statements remain open across rollbacks?
     * 
     * @return <code>true</code> if statements always remain open;
     *       <code>false</code> if they might not remain open
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsOpenStatementsAcrossRollback() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsOpenStatementsAcrossRollback();
    }


    //----------------------------------------------------------------------
    // The following group of methods exposes various limitations 
    // based on the target database with the current driver.
    // Unless otherwise specified, a result of zero means there is no
    // limit, or the limit is not known.

    /**
     * How many hex characters can you have in an inline binary literal?
     *
     * @return max binary literal length in hex characters;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxBinaryLiteralLength() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxBinaryLiteralLength();
    }

    /**
     * What's the max length for a character literal?
     *
     * @return max literal length;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxCharLiteralLength() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxCharLiteralLength();
    }

    /**
     * What's the limit on column name length?
     *
     * @return max column name length;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxColumnNameLength() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxColumnNameLength();
    }

    /**
     * What's the maximum number of columns in a "GROUP BY" clause?
     *
     * @return max number of columns;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxColumnsInGroupBy() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxColumnsInGroupBy();
    }

    /**
     * What's the maximum number of columns allowed in an index?
     *
     * @return max number of columns;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxColumnsInIndex() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxColumnsInIndex();
    }

    /**
     * What's the maximum number of columns in an "ORDER BY" clause?
     *
     * @return max number of columns;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxColumnsInOrderBy() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxColumnsInOrderBy();
    }

    /**
     * What's the maximum number of columns in a "SELECT" list?
     *
     * @return max number of columns;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxColumnsInSelect() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxColumnsInSelect();
    }

    /**
     * What's the maximum number of columns in a table?
     *
     * @return max number of columns;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxColumnsInTable() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxColumnsInTable();
    }

    /**
     * How many active connections can we have at a time to this database?
     *
     * @return max number of active connections;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxConnections() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxConnections();
    }

    /**
     * What's the maximum cursor name length?
     *
     * @return max cursor name length in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxCursorNameLength() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxCursorNameLength();
    }

    /**
     * Retrieves the maximum number of bytes for an index, including all
     * of the parts of the index.
     *
     * @return max index length in bytes, which includes the composite of all
     *      the constituent parts of the index;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxIndexLength() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxIndexLength();
    }

    /**
     * What's the maximum length allowed for a schema name?
     *
     * @return max name length in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxSchemaNameLength() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxSchemaNameLength();
    }

    /**
     * What's the maximum length of a procedure name?
     *
     * @return max name length in bytes; 
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxProcedureNameLength() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxProcedureNameLength();
    }

    /**
     * What's the maximum length of a catalog name?
     *
     * @return max name length in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxCatalogNameLength() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxCatalogNameLength();
    }

    /**
     * What's the maximum length of a single row?
     *
     * @return max row size in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxRowSize() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxRowSize();
    }

    /**
     * Did getMaxRowSize() include LONGVARCHAR and LONGVARBINARY
     * blobs?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean doesMaxRowSizeIncludeBlobs() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.doesMaxRowSizeIncludeBlobs();
    }

    /**
     * What's the maximum length of an SQL statement?
     *
     * @return max length in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxStatementLength() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxStatementLength();
    }

    /**
     * How many active statements can we have open at one time to this
     * database?
     *
     * @return the maximum number of statements that can be open at one time;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxStatements() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxStatements();
    }

    /**
     * What's the maximum length of a table name?
     *
     * @return max name length in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxTableNameLength() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxTableNameLength();
    }

    /**
     * What's the maximum number of tables in a SELECT statement?
     *
     * @return the maximum number of tables allowed in a SELECT statement;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxTablesInSelect() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxTablesInSelect();
    }

    /**
     * What's the maximum length of a user name?
     *
     * @return max user name length  in bytes;
     *      a result of zero means that there is no limit or the limit is not known
     * @exception SQLException if a database access error occurs
     */
    public synchronized int getMaxUserNameLength() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getMaxUserNameLength();
    }

    //----------------------------------------------------------------------

    /**
     * What's the database's default transaction isolation level?  The
     * values are defined in <code>java.sql.Connection</code>.
     *
     * @return the default isolation level 
     * @exception SQLException if a database access error occurs
     * @see Connection
     */
    public synchronized int getDefaultTransactionIsolation() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.getDefaultTransactionIsolation();
    }

    /**
     * Are transactions supported? If not, invoking the method
     * <code>commit</code> is a noop and the
     * isolation level is TRANSACTION_NONE.
     *
     * @return <code>true</code> if transactions are supported; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsTransactions() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsTransactions();
    }

    /**
     * Does this database support the given transaction isolation level?
     *
     * @param level the values are defined in <code>java.sql.Connection</code>
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     * @see Connection
     */
    public synchronized boolean supportsTransactionIsolationLevel(int level)
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsTransactionIsolationLevel(level);
    }

    /**
     * Are both data definition and data manipulation statements
     * within a transaction supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsDataDefinitionAndDataManipulationTransactions()
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsDataDefinitionAndDataManipulationTransactions();
    }

    /**
     * Are only data manipulation statements within a transaction
     * supported?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean supportsDataManipulationTransactionsOnly()
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsDataManipulationTransactionsOnly();
    }

    /**
     * Does a data definition statement within a transaction force the
     * transaction to commit?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean dataDefinitionCausesTransactionCommit()
        throws SQLException
    {
        validate();

        return _databaseMetaData.dataDefinitionCausesTransactionCommit();
    }

    /**
     * Is a data definition statement within a transaction ignored?
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public synchronized boolean dataDefinitionIgnoredInTransactions()
        throws SQLException
    {
        validate();

        return _databaseMetaData.dataDefinitionIgnoredInTransactions();
    }


    /**
     * Gets a description of the stored procedures available in a
     * catalog.
     *
     * <P>Only procedure descriptions matching the schema and
     * procedure name criteria are returned.  They are ordered by
     * PROCEDURE_SCHEM, and PROCEDURE_NAME.
     *
     * <P>Each procedure description has the the following columns:
     *  <OL>
     *  <LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be null)
     *  <LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be null)
     *  <LI><B>PROCEDURE_NAME</B> String => procedure name
     *  <LI> reserved for future use
     *  <LI> reserved for future use
     *  <LI> reserved for future use
     *  <LI><B>REMARKS</B> String => explanatory comment on the procedure
     *  <LI><B>PROCEDURE_TYPE</B> short => kind of procedure:
     *      <UL>
     *      <LI> procedureResultUnknown - May return a result
     *      <LI> procedureNoResult - Does not return a result
     *      <LI> procedureReturnsResult - Returns a result
     *      </UL>
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param procedureNamePattern a procedure name pattern 
     * @return <code>ResultSet</code> - each row is a procedure description 
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     */
    public synchronized ResultSet getProcedures(String catalog, String schemaPattern,
                                                String procedureNamePattern) 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getProcedures(catalog, schemaPattern, procedureNamePattern), 
                                      _connection);
    }

    /**
     * Gets a description of a catalog's stored procedure parameters
     * and result columns.
     *
     * <P>Only descriptions matching the schema, procedure and
     * parameter name criteria are returned.  They are ordered by
     * PROCEDURE_SCHEM and PROCEDURE_NAME. Within this, the return value,
     * if any, is first. Next are the parameter descriptions in call
     * order. The column descriptions follow in column number order.
     *
     * <P>Each row in the <code>ResultSet</code> is a parameter description or
     * column description with the following fields:
     *  <OL>
     *  <LI><B>PROCEDURE_CAT</B> String => procedure catalog (may be null)
     *  <LI><B>PROCEDURE_SCHEM</B> String => procedure schema (may be null)
     *  <LI><B>PROCEDURE_NAME</B> String => procedure name
     *  <LI><B>COLUMN_NAME</B> String => column/parameter name 
     *  <LI><B>COLUMN_TYPE</B> Short => kind of column/parameter:
     *      <UL>
     *      <LI> procedureColumnUnknown - nobody knows
     *      <LI> procedureColumnIn - IN parameter
     *      <LI> procedureColumnInOut - INOUT parameter
     *      <LI> procedureColumnOut - OUT parameter
     *      <LI> procedureColumnReturn - procedure return value
     *      <LI> procedureColumnResult - result column in <code>ResultSet</code>
     *      </UL>
     *  <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => SQL type name, for a UDT type the
     *  type name is fully qualified
     *  <LI><B>PRECISION</B> int => precision
     *  <LI><B>LENGTH</B> int => length in bytes of data
     *  <LI><B>SCALE</B> short => scale
     *  <LI><B>RADIX</B> short => radix
     *  <LI><B>NULLABLE</B> short => can it contain NULL?
     *      <UL>
     *      <LI> procedureNoNulls - does not allow NULL values
     *      <LI> procedureNullable - allows NULL values
     *      <LI> procedureNullableUnknown - nullability unknown
     *      </UL>
     *  <LI><B>REMARKS</B> String => comment describing parameter/column
     * </OL>
     *
     * <P><B>Note:</B> Some databases may not return the column
     * descriptions for a procedure. Additional columns beyond
     * REMARKS can be defined by the database.
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema 
     * @param procedureNamePattern a procedure name pattern 
     * @param columnNamePattern a column name pattern 
     * @return <code>ResultSet</code> - each row describes a stored procedure parameter or 
     *      column
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     */
    public synchronized ResultSet getProcedureColumns(String catalog,
                                                      String schemaPattern,
                                                      String procedureNamePattern, 
                                                      String columnNamePattern) 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern), 
                                      _connection);
    }

    /**
     * Gets a description of tables available in a catalog.
     *
     * <P>Only table descriptions matching the catalog, schema, table
     * name and type criteria are returned.  They are ordered by
     * TABLE_TYPE, TABLE_SCHEM and TABLE_NAME.
     *
     * <P>Each table description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
     *                  "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", 
     *                  "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     *  <LI><B>REMARKS</B> String => explanatory comment on the table
     *  </OL>
     *
     * <P><B>Note:</B> Some databases may not return information for
     * all tables.
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param tableNamePattern a table name pattern 
     * @param types a list of table types to include; null returns all types 
     * @return <code>ResultSet</code> - each row is a table description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     */
    public synchronized ResultSet getTables(String catalog, String schemaPattern,
                                            String tableNamePattern, String types[]) 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getTables(catalog, schemaPattern, tableNamePattern, types),
                                      _connection);
    }

    /**
     * Gets the schema names available in this database.  The results
     * are ordered by schema name.
     *
     * <P>The schema column is:
     *  <OL>
     *  <LI><B>TABLE_SCHEM</B> String => schema name
     *  </OL>
     *
     * @return <code>ResultSet</code> - each row has a single String column that is a
     * schema name 
     * @exception SQLException if a database access error occurs
     */
    public synchronized ResultSet getSchemas() 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getSchemas(),
                                      _connection);
    }

    /**
     * Gets the catalog names available in this database.  The results
     * are ordered by catalog name.
     *
     * <P>The catalog column is:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => catalog name
     *  </OL>
     *
     * @return <code>ResultSet</code> - each row has a single String column that is a
     * catalog name 
     * @exception SQLException if a database access error occurs
     */
    public synchronized ResultSet getCatalogs() 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getCatalogs(),
                                      _connection);
    }

    /**
     * Gets the table types available in this database.  The results
     * are ordered by table type.
     *
     * <P>The table type is:
     *  <OL>
     *  <LI><B>TABLE_TYPE</B> String => table type.  Typical types are "TABLE",
     *                  "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", 
     *                  "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     *  </OL>
     *
     * @return <code>ResultSet</code> - each row has a single String column that is a
     * table type 
     * @exception SQLException if a database access error occurs
     */
    public synchronized ResultSet getTableTypes() 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getTableTypes(),
                                      _connection);
    }

    /**
     * Gets a description of table columns available in 
     * the specified catalog.
     *
     * <P>Only column descriptions matching the catalog, schema, table
     * and column name criteria are returned.  They are ordered by
     * TABLE_SCHEM, TABLE_NAME and ORDINAL_POSITION.
     *
     * <P>Each column description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => Data source dependent type name,
     *  for a UDT the type name is fully qualified
     *  <LI><B>COLUMN_SIZE</B> int => column size.  For char or date
     *      types this is the maximum number of characters, for numeric or
     *       decimal types this is precision.
     *  <LI><B>BUFFER_LENGTH</B> is not used.
     *  <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
     *  <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
     *  <LI><B>NULLABLE</B> int => is NULL allowed?
     *      <UL>
     *      <LI> columnNoNulls - might not allow NULL values
     *      <LI> columnNullable - definitely allows NULL values
     *      <LI> columnNullableUnknown - nullability unknown
     *      </UL>
     *  <LI><B>REMARKS</B> String => comment describing column (may be null)
     *  <LI><B>COLUMN_DEF</B> String => default value (may be null)
     *  <LI><B>SQL_DATA_TYPE</B> int => unused
     *  <LI><B>SQL_DATETIME_SUB</B> int => unused
     *  <LI><B>CHAR_OCTET_LENGTH</B> int => for char types the 
     *       maximum number of bytes in the column
     *  <LI><B>ORDINAL_POSITION</B> int=> index of column in table 
     *      (starting at 1)
     *  <LI><B>IS_NULLABLE</B> String => "NO" means column definitely 
     *      does not allow NULL values; "YES" means the column might 
     *      allow NULL values.  An empty string means nobody knows.
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param tableNamePattern a table name pattern 
     * @param columnNamePattern a column name pattern 
     * @return <code>ResultSet</code> - each row is a column description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     */
    public synchronized ResultSet getColumns(String catalog, String schemaPattern,
                                             String tableNamePattern, String columnNamePattern)
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern),
                                      _connection);
    }


    /**
     * Gets a description of the access rights for a table's columns.
     *
     * <P>Only privileges matching the column name criteria are
     * returned.  They are ordered by COLUMN_NAME and PRIVILEGE.
     *
     * <P>Each privilige description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>GRANTOR</B> => grantor of access (may be null)
     *  <LI><B>GRANTEE</B> String => grantee of access
     *  <LI><B>PRIVILEGE</B> String => name of access (SELECT, 
     *      INSERT, UPDATE, REFRENCES, ...)
     *  <LI><B>IS_GRANTABLE</B> String => "YES" if grantee is permitted 
     *      to grant to others; "NO" if not; null if unknown 
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those without a schema
     * @param table a table name
     * @param columnNamePattern a column name pattern 
     * @return <code>ResultSet</code> - each row is a column privilege description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     */
    public synchronized ResultSet getColumnPrivileges(String catalog, String schema,
                                                      String table, String columnNamePattern) 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getColumnPrivileges(catalog, schema, table, columnNamePattern),
                                      _connection);
    }

    /**
     * Gets a description of the access rights for each table available
     * in a catalog. Note that a table privilege applies to one or
     * more columns in the table. It would be wrong to assume that
     * this priviledge applies to all columns (this may be true for
     * some systems but is not true for all.)
     *
     * <P>Only privileges matching the schema and table name
     * criteria are returned.  They are ordered by TABLE_SCHEM,
     * TABLE_NAME, and PRIVILEGE.
     *
     * <P>Each privilige description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>GRANTOR</B> => grantor of access (may be null)
     *  <LI><B>GRANTEE</B> String => grantee of access
     *  <LI><B>PRIVILEGE</B> String => name of access (SELECT, 
     *      INSERT, UPDATE, REFRENCES, ...)
     *  <LI><B>IS_GRANTABLE</B> String => "YES" if grantee is permitted 
     *      to grant to others; "NO" if not; null if unknown 
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param tableNamePattern a table name pattern 
     * @return <code>ResultSet</code> - each row is a table privilege description
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     */
    public synchronized ResultSet getTablePrivileges(String catalog, String schemaPattern,
                                                     String tableNamePattern) 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getTablePrivileges(catalog, schemaPattern, tableNamePattern),
                                      _connection);
    }

    /**
     * Gets a description of a table's optimal set of columns that
     * uniquely identifies a row. They are ordered by SCOPE.
     *
     * <P>Each column description has the following columns:
     *  <OL>
     *  <LI><B>SCOPE</B> short => actual scope of result
     *      <UL>
     *      <LI> bestRowTemporary - very temporary, while using row
     *      <LI> bestRowTransaction - valid for remainder of current transaction
     *      <LI> bestRowSession - valid for remainder of current session
     *      </UL>
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => Data source dependent type name,
     *  for a UDT the type name is fully qualified
     *  <LI><B>COLUMN_SIZE</B> int => precision
     *  <LI><B>BUFFER_LENGTH</B> int => not used
     *  <LI><B>DECIMAL_DIGITS</B> short => scale
     *  <LI><B>PSEUDO_COLUMN</B> short => is this a pseudo column 
     *      like an Oracle ROWID
     *      <UL>
     *      <LI> bestRowUnknown - may or may not be pseudo column
     *      <LI> bestRowNotPseudo - is NOT a pseudo column
     *      <LI> bestRowPseudo - is a pseudo column
     *      </UL>
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those without a schema
     * @param table a table name
     * @param scope the scope of interest; use same values as SCOPE
     * @param nullable include columns that are nullable?
     * @return <code>ResultSet</code> - each row is a column description 
     * @exception SQLException if a database access error occurs
     */
    public synchronized ResultSet getBestRowIdentifier(String catalog, String schema,
                                                       String table, int scope, boolean nullable) 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getBestRowIdentifier(catalog, schema, table, scope, nullable),
                                      _connection);
    }

    /**
     * Gets a description of a table's columns that are automatically
     * updated when any value in a row is updated.  They are
     * unordered.
     *
     * <P>Each column description has the following columns:
     *  <OL>
     *  <LI><B>SCOPE</B> short => is not used
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => Data source dependent type name
     *  <LI><B>COLUMN_SIZE</B> int => precision
     *  <LI><B>BUFFER_LENGTH</B> int => length of column value in bytes
     *  <LI><B>DECIMAL_DIGITS</B> short => scale
     *  <LI><B>PSEUDO_COLUMN</B> short => is this a pseudo column 
     *      like an Oracle ROWID
     *      <UL>
     *      <LI> versionColumnUnknown - may or may not be pseudo column
     *      <LI> versionColumnNotPseudo - is NOT a pseudo column
     *      <LI> versionColumnPseudo - is a pseudo column
     *      </UL>
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those without a schema
     * @param table a table name
     * @return <code>ResultSet</code> - each row is a column description 
     * @exception SQLException if a database access error occurs
     */
    public synchronized ResultSet getVersionColumns(String catalog, String schema,
                                                    String table) 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getVersionColumns(catalog, schema, table),
                                      _connection);
    }


    /**
     * Gets a description of a table's primary key columns.  They
     * are ordered by COLUMN_NAME.
     *
     * <P>Each primary key column description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>COLUMN_NAME</B> String => column name
     *  <LI><B>KEY_SEQ</B> short => sequence number within primary key
     *  <LI><B>PK_NAME</B> String => primary key name (may be null)
     *   </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those
     * without a schema
     * @param table a table name
     * @return <code>ResultSet</code> - each row is a primary key column description 
     * @exception SQLException if a database access error occurs
     */
    public synchronized ResultSet getPrimaryKeys(String catalog, String schema,
                                                 String table) 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getPrimaryKeys(catalog, schema, table),
                                      _connection);
    }

    /**
     * Gets a description of the primary key columns that are
     * referenced by a table's foreign key columns (the primary keys
     * imported by a table).  They are ordered by PKTABLE_CAT,
     * PKTABLE_SCHEM, PKTABLE_NAME, and KEY_SEQ.
     *
     * <P>Each primary key column description has the following columns:
     *  <OL>
     *  <LI><B>PKTABLE_CAT</B> String => primary key table catalog 
     *      being imported (may be null)
     *  <LI><B>PKTABLE_SCHEM</B> String => primary key table schema
     *      being imported (may be null)
     *  <LI><B>PKTABLE_NAME</B> String => primary key table name
     *      being imported
     *  <LI><B>PKCOLUMN_NAME</B> String => primary key column name
     *      being imported
     *  <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be null)
     *  <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be null)
     *  <LI><B>FKTABLE_NAME</B> String => foreign key table name
     *  <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
     *  <LI><B>KEY_SEQ</B> short => sequence number within foreign key
     *  <LI><B>UPDATE_RULE</B> short => What happens to 
     *       foreign key when primary is updated:
     *      <UL>
     *      <LI> importedNoAction - do not allow update of primary 
     *               key if it has been imported
     *      <LI> importedKeyCascade - change imported key to agree 
     *               with primary key update
     *      <LI> importedKeySetNull - change imported key to NULL if 
     *               its primary key has been updated
     *      <LI> importedKeySetDefault - change imported key to default values 
     *               if its primary key has been updated
     *      <LI> importedKeyRestrict - same as importedKeyNoAction 
     *                                 (for ODBC 2.x compatibility)
     *      </UL>
     *  <LI><B>DELETE_RULE</B> short => What happens to 
     *      the foreign key when primary is deleted.
     *      <UL>
     *      <LI> importedKeyNoAction - do not allow delete of primary 
     *               key if it has been imported
     *      <LI> importedKeyCascade - delete rows that import a deleted key
     *      <LI> importedKeySetNull - change imported key to NULL if 
     *               its primary key has been deleted
     *      <LI> importedKeyRestrict - same as importedKeyNoAction 
     *                                 (for ODBC 2.x compatibility)
     *      <LI> importedKeySetDefault - change imported key to default if 
     *               its primary key has been deleted
     *      </UL>
     *  <LI><B>FK_NAME</B> String => foreign key name (may be null)
     *  <LI><B>PK_NAME</B> String => primary key name (may be null)
     *  <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key 
     *      constraints be deferred until commit
     *      <UL>
     *      <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     *      <LI> importedKeyInitiallyImmediate - see SQL92 for definition 
     *      <LI> importedKeyNotDeferrable - see SQL92 for definition 
     *      </UL>
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those
     * without a schema
     * @param table a table name
     * @return <code>ResultSet</code> - each row is a primary key column description 
     * @exception SQLException if a database access error occurs
     * @see #getExportedKeys 
     */
    public synchronized ResultSet getImportedKeys(String catalog, String schema,
                                                  String table) 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getImportedKeys(catalog, schema, table),
                                      _connection);
    }

    /**
     * Gets a description of the foreign key columns that reference a
     * table's primary key columns (the foreign keys exported by a
     * table).  They are ordered by FKTABLE_CAT, FKTABLE_SCHEM,
     * FKTABLE_NAME, and KEY_SEQ.
     *
     * <P>Each foreign key column description has the following columns:
     *  <OL>
     *  <LI><B>PKTABLE_CAT</B> String => primary key table catalog (may be null)
     *  <LI><B>PKTABLE_SCHEM</B> String => primary key table schema (may be null)
     *  <LI><B>PKTABLE_NAME</B> String => primary key table name
     *  <LI><B>PKCOLUMN_NAME</B> String => primary key column name
     *  <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be null)
     *      being exported (may be null)
     *  <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be null)
     *      being exported (may be null)
     *  <LI><B>FKTABLE_NAME</B> String => foreign key table name
     *      being exported
     *  <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
     *      being exported
     *  <LI><B>KEY_SEQ</B> short => sequence number within foreign key
     *  <LI><B>UPDATE_RULE</B> short => What happens to 
     *       foreign key when primary is updated:
     *      <UL>
     *      <LI> importedNoAction - do not allow update of primary 
     *               key if it has been imported
     *      <LI> importedKeyCascade - change imported key to agree 
     *               with primary key update
     *      <LI> importedKeySetNull - change imported key to NULL if 
     *               its primary key has been updated
     *      <LI> importedKeySetDefault - change imported key to default values 
     *               if its primary key has been updated
     *      <LI> importedKeyRestrict - same as importedKeyNoAction 
     *                                 (for ODBC 2.x compatibility)
     *      </UL>
     *  <LI><B>DELETE_RULE</B> short => What happens to 
     *      the foreign key when primary is deleted.
     *      <UL>
     *      <LI> importedKeyNoAction - do not allow delete of primary 
     *               key if it has been imported
     *      <LI> importedKeyCascade - delete rows that import a deleted key
     *      <LI> importedKeySetNull - change imported key to NULL if 
     *               its primary key has been deleted
     *      <LI> importedKeyRestrict - same as importedKeyNoAction 
     *                                 (for ODBC 2.x compatibility)
     *      <LI> importedKeySetDefault - change imported key to default if 
     *               its primary key has been deleted
     *      </UL>
     *  <LI><B>FK_NAME</B> String => foreign key name (may be null)
     *  <LI><B>PK_NAME</B> String => primary key name (may be null)
     *  <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key 
     *      constraints be deferred until commit
     *      <UL>
     *      <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     *      <LI> importedKeyInitiallyImmediate - see SQL92 for definition 
     *      <LI> importedKeyNotDeferrable - see SQL92 for definition 
     *      </UL>
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those
     * without a schema
     * @param table a table name
     * @return <code>ResultSet</code> - each row is a foreign key column description 
     * @exception SQLException if a database access error occurs
     * @see #getImportedKeys 
     */
    public synchronized ResultSet getExportedKeys(String catalog, String schema,
                                     String table) 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getExportedKeys(catalog, schema, table),
                                      _connection);
    }

    /**
     * Gets a description of the foreign key columns in the foreign key
     * table that reference the primary key columns of the primary key
     * table (describe how one table imports another's key.) This
     * should normally return a single foreign key/primary key pair
     * (most tables only import a foreign key from a table once.)  They
     * are ordered by FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and
     * KEY_SEQ.
     *
     * <P>Each foreign key column description has the following columns:
     *  <OL>
     *  <LI><B>PKTABLE_CAT</B> String => primary key table catalog (may be null)
     *  <LI><B>PKTABLE_SCHEM</B> String => primary key table schema (may be null)
     *  <LI><B>PKTABLE_NAME</B> String => primary key table name
     *  <LI><B>PKCOLUMN_NAME</B> String => primary key column name
     *  <LI><B>FKTABLE_CAT</B> String => foreign key table catalog (may be null)
     *      being exported (may be null)
     *  <LI><B>FKTABLE_SCHEM</B> String => foreign key table schema (may be null)
     *      being exported (may be null)
     *  <LI><B>FKTABLE_NAME</B> String => foreign key table name
     *      being exported
     *  <LI><B>FKCOLUMN_NAME</B> String => foreign key column name
     *      being exported
     *  <LI><B>KEY_SEQ</B> short => sequence number within foreign key
     *  <LI><B>UPDATE_RULE</B> short => What happens to 
     *       foreign key when primary is updated:
     *      <UL>
     *      <LI> importedNoAction - do not allow update of primary 
     *               key if it has been imported
     *      <LI> importedKeyCascade - change imported key to agree 
     *               with primary key update
     *      <LI> importedKeySetNull - change imported key to NULL if 
     *               its primary key has been updated
     *      <LI> importedKeySetDefault - change imported key to default values 
     *               if its primary key has been updated
     *      <LI> importedKeyRestrict - same as importedKeyNoAction 
     *                                 (for ODBC 2.x compatibility)
     *      </UL>
     *  <LI><B>DELETE_RULE</B> short => What happens to 
     *      the foreign key when primary is deleted.
     *      <UL>
     *      <LI> importedKeyNoAction - do not allow delete of primary 
     *               key if it has been imported
     *      <LI> importedKeyCascade - delete rows that import a deleted key
     *      <LI> importedKeySetNull - change imported key to NULL if 
     *               its primary key has been deleted
     *      <LI> importedKeyRestrict - same as importedKeyNoAction 
     *                                 (for ODBC 2.x compatibility)
     *      <LI> importedKeySetDefault - change imported key to default if 
     *               its primary key has been deleted
     *      </UL>
     *  <LI><B>FK_NAME</B> String => foreign key name (may be null)
     *  <LI><B>PK_NAME</B> String => primary key name (may be null)
     *  <LI><B>DEFERRABILITY</B> short => can the evaluation of foreign key 
     *      constraints be deferred until commit
     *      <UL>
     *      <LI> importedKeyInitiallyDeferred - see SQL92 for definition
     *      <LI> importedKeyInitiallyImmediate - see SQL92 for definition 
     *      <LI> importedKeyNotDeferrable - see SQL92 for definition 
     *      </UL>
     *  </OL>
     *
     * @param primaryCatalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param primarySchema a schema name; "" retrieves those
     * without a schema
     * @param primaryTable the table name that exports the key
     * @param foreignCatalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param foreignSchema a schema name; "" retrieves those
     * without a schema
     * @param foreignTable the table name that imports the key
     * @return <code>ResultSet</code> - each row is a foreign key column description 
     * @exception SQLException if a database access error occurs
     * @see #getImportedKeys 
     */
    public synchronized ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable,
                                                    String foreignCatalog, String foreignSchema, String foreignTable) 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getCrossReference(primaryCatalog, primarySchema, primaryTable,
                                                                          foreignCatalog, foreignSchema, foreignTable),
                                      _connection);
    }

    /**
     * Gets a description of all the standard SQL types supported by
     * this database. They are ordered by DATA_TYPE and then by how
     * closely the data type maps to the corresponding JDBC SQL type.
     *
     * <P>Each type description has the following columns:
     *  <OL>
     *  <LI><B>TYPE_NAME</B> String => Type name
     *  <LI><B>DATA_TYPE</B> short => SQL data type from java.sql.Types
     *  <LI><B>PRECISION</B> int => maximum precision
     *  <LI><B>LITERAL_PREFIX</B> String => prefix used to quote a literal 
     *      (may be null)
     *  <LI><B>LITERAL_SUFFIX</B> String => suffix used to quote a literal 
            (may be null)
     *  <LI><B>CREATE_PARAMS</B> String => parameters used in creating 
     *      the type (may be null)
     *  <LI><B>NULLABLE</B> short => can you use NULL for this type?
     *      <UL>
     *      <LI> typeNoNulls - does not allow NULL values
     *      <LI> typeNullable - allows NULL values
     *      <LI> typeNullableUnknown - nullability unknown
     *      </UL>
     *  <LI><B>CASE_SENSITIVE</B> boolean=> is it case sensitive?
     *  <LI><B>SEARCHABLE</B> short => can you use "WHERE" based on this type:
     *      <UL>
     *      <LI> typePredNone - No support
     *      <LI> typePredChar - Only supported with WHERE .. LIKE
     *      <LI> typePredBasic - Supported except for WHERE .. LIKE
     *      <LI> typeSearchable - Supported for all WHERE ..
     *      </UL>
     *  <LI><B>UNSIGNED_ATTRIBUTE</B> boolean => is it unsigned?
     *  <LI><B>FIXED_PREC_SCALE</B> boolean => can it be a money value?
     *  <LI><B>AUTO_INCREMENT</B> boolean => can it be used for an 
     *      auto-increment value?
     *  <LI><B>LOCAL_TYPE_NAME</B> String => localized version of type name 
     *      (may be null)
     *  <LI><B>MINIMUM_SCALE</B> short => minimum scale supported
     *  <LI><B>MAXIMUM_SCALE</B> short => maximum scale supported
     *  <LI><B>SQL_DATA_TYPE</B> int => unused
     *  <LI><B>SQL_DATETIME_SUB</B> int => unused
     *  <LI><B>NUM_PREC_RADIX</B> int => usually 2 or 10
     *  </OL>
     *
     * @return <code>ResultSet</code> - each row is an SQL type description 
     * @exception SQLException if a database access error occurs
     */
    public synchronized ResultSet getTypeInfo() 
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getTypeInfo(),
                                      _connection);
    }

    /**
     * Gets a description of a table's indices and statistics. They are
     * ordered by NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION.
     *
     * <P>Each index column description has the following columns:
     *  <OL>
     *  <LI><B>TABLE_CAT</B> String => table catalog (may be null)
     *  <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
     *  <LI><B>TABLE_NAME</B> String => table name
     *  <LI><B>NON_UNIQUE</B> boolean => Can index values be non-unique? 
     *      false when TYPE is tableIndexStatistic
     *  <LI><B>INDEX_QUALIFIER</B> String => index catalog (may be null); 
     *      null when TYPE is tableIndexStatistic
     *  <LI><B>INDEX_NAME</B> String => index name; null when TYPE is 
     *      tableIndexStatistic
     *  <LI><B>TYPE</B> short => index type:
     *      <UL>
     *      <LI> tableIndexStatistic - this identifies table statistics that are
     *           returned in conjuction with a table's index descriptions
     *      <LI> tableIndexClustered - this is a clustered index
     *      <LI> tableIndexHashed - this is a hashed index
     *      <LI> tableIndexOther - this is some other style of index
     *      </UL>
     *  <LI><B>ORDINAL_POSITION</B> short => column sequence number 
     *      within index; zero when TYPE is tableIndexStatistic
     *  <LI><B>COLUMN_NAME</B> String => column name; null when TYPE is 
     *      tableIndexStatistic
     *  <LI><B>ASC_OR_DESC</B> String => column sort sequence, "A" => ascending, 
     *      "D" => descending, may be null if sort sequence is not supported; 
     *      null when TYPE is tableIndexStatistic
     *  <LI><B>CARDINALITY</B> int => When TYPE is tableIndexStatistic, then 
     *      this is the number of rows in the table; otherwise, it is the 
     *      number of unique values in the index.
     *  <LI><B>PAGES</B> int => When TYPE is  tableIndexStatisic then 
     *      this is the number of pages used for the table, otherwise it 
     *      is the number of pages used for the current index.
     *  <LI><B>FILTER_CONDITION</B> String => Filter condition, if any.  
     *      (may be null)
     *  </OL>
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schema a schema name; "" retrieves those without a schema
     * @param table a table name  
     * @param unique when true, return only indices for unique values; 
     *     when false, return indices regardless of whether unique or not 
     * @param approximate when true, result is allowed to reflect approximate 
     *     or out of data values; when false, results are requested to be 
     *     accurate
     * @return <code>ResultSet</code> - each row is an index column description 
     * @exception SQLException if a database access error occurs
     */
    public synchronized ResultSet getIndexInfo(String catalog, String schema, String table,
                                               boolean unique, boolean approximate)
        throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getIndexInfo(catalog, schema, table,
                                                                     unique, approximate),
                                      _connection);
    }

    //--------------------------JDBC 2.0-----------------------------

    /**
     * Does the database support the given result set type?
     *
     * @param type defined in <code>java.sql.ResultSet</code>
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     * @see Connection
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized boolean supportsResultSetType(int type) 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsResultSetType(type);
    }

    /**
     * Does the database support the concurrency type in combination
     * with the given result set type?
     *
     * @param type defined in <code>java.sql.ResultSet</code>
     * @param concurrency type defined in <code>java.sql.ResultSet</code>
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     * @see Connection
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized boolean supportsResultSetConcurrency(int type, int concurrency)
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsResultSetConcurrency(type, concurrency);
    }

    /**
     *
     * Indicates whether a result set's own updates are visible.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if updates are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized boolean ownUpdatesAreVisible(int type) 
        throws SQLException
    {
        validate();

        return _databaseMetaData.ownUpdatesAreVisible(type);
    }

    /**
     *
     * Indicates whether a result set's own deletes are visible.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if deletes are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized boolean ownDeletesAreVisible(int type) 
        throws SQLException
    {
        validate();

        return _databaseMetaData.ownDeletesAreVisible(type);
    }

    /**
     *
     * Indicates whether a result set's own inserts are visible.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if inserts are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized boolean ownInsertsAreVisible(int type) 
        throws SQLException
    {
        validate();

        return _databaseMetaData.ownInsertsAreVisible(type);
    }

    /**
     *
     * Indicates whether updates made by others are visible.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if updates made by others
     * are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized boolean othersUpdatesAreVisible(int type) 
        throws SQLException
    {
        validate();

        return _databaseMetaData.othersUpdatesAreVisible(type);
    }

    /**
     *
     * Indicates whether deletes made by others are visible.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if deletes made by others
     * are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized boolean othersDeletesAreVisible(int type) 
        throws SQLException
    {
        validate();

        return _databaseMetaData.othersDeletesAreVisible(type);
    }

    /**
     *
     * Indicates whether inserts made by others are visible.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return true if updates are visible for the result set type
     * @return <code>true</code> if inserts made by others
     * are visible for the result set type;
     *        <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized boolean othersInsertsAreVisible(int type) 
        throws SQLException
    {
        validate();

        return _databaseMetaData.othersInsertsAreVisible(type);
    }

    /**
     *
     * Indicates whether or not a visible row update can be detected by 
     * calling the method <code>ResultSet.rowUpdated</code>.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return <code>true</code> if changes are detected by the result set type;
     *         <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized boolean updatesAreDetected(int type) 
        throws SQLException
    {
        validate();

        return _databaseMetaData.updatesAreDetected(type);
    }

    /**
     *
     * Indicates whether or not a visible row delete can be detected by 
     * calling ResultSet.rowDeleted().  If deletesAreDetected()
     * returns false, then deleted rows are removed from the result set.
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return true if changes are detected by the resultset type
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized boolean deletesAreDetected(int type) 
        throws SQLException
    {
        validate();

        return _databaseMetaData.deletesAreDetected(type);
    }

    /**
     *
     * Indicates whether or not a visible row insert can be detected
     * by calling ResultSet.rowInserted().
     *
     * @param result set type, i.e. ResultSet.TYPE_XXX
     * @return true if changes are detected by the resultset type
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized boolean insertsAreDetected(int type) 
        throws SQLException
    {
        validate();

        return _databaseMetaData.insertsAreDetected(type);
    }

    /**
     *
     * Indicates whether the driver supports batch updates.
     * @return true if the driver supports batch updates; false otherwise
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized boolean supportsBatchUpdates() 
        throws SQLException
    {
        validate();

        return _databaseMetaData.supportsBatchUpdates();
    }

    /**
     *
     * Gets a description of the user-defined types defined in a particular
     * schema.  Schema-specific UDTs may have type JAVA_OBJECT, STRUCT, 
     * or DISTINCT.
     *
     * <P>Only types matching the catalog, schema, type name and type  
     * criteria are returned.  They are ordered by DATA_TYPE, TYPE_SCHEM 
     * and TYPE_NAME.  The type name parameter may be a fully-qualified 
     * name.  In this case, the catalog and schemaPattern parameters are
     * ignored.
     *
     * <P>Each type description has the following columns:
     *  <OL>
     *  <LI><B>TYPE_CAT</B> String => the type's catalog (may be null)
     *  <LI><B>TYPE_SCHEM</B> String => type's schema (may be null)
     *  <LI><B>TYPE_NAME</B> String => type name
     *  <LI><B>CLASS_NAME</B> String => Java class name
     *  <LI><B>DATA_TYPE</B> String => type value defined in java.sql.Types.  
     *  One of JAVA_OBJECT, STRUCT, or DISTINCT
     *  <LI><B>REMARKS</B> String => explanatory comment on the type
     *  </OL>
     *
     * <P><B>Note:</B> If the driver does not support UDTs, an empty
     * result set is returned.
     *
     * @param catalog a catalog name; "" retrieves those without a
     * catalog; null means drop catalog name from the selection criteria
     * @param schemaPattern a schema name pattern; "" retrieves those
     * without a schema
     * @param typeNamePattern a type name pattern; may be a fully-qualified
     * name
     * @param types a list of user-named types to include (JAVA_OBJECT, 
     * STRUCT, or DISTINCT); null returns all types 
     * @return <code>ResultSet</code> - each row is a type description
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized ResultSet getUDTs(String catalog, String schemaPattern, 
                                          String typeNamePattern, int[] types) 
      throws SQLException
    {
        validate();

        return new TyrexResultSetImpl(_databaseMetaData.getUDTs(catalog, schemaPattern,
                                                                typeNamePattern, types),
                                      _connection);
    }

    /**
     * Retrieves the connection that produced this metadata object.
     *
     * @return the connection that produced this metadata object
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized Connection getConnection() 
        throws SQLException
    {
        return _connection;
    }


    /**
     * Ensure that the underlying meta data is still valid.
     *
     * @throws SQLException if the underlying meta data is not valid.
     */
    private void validate()
        throws SQLException
    {
        if (_connection.isClosed()) {
            throw new SQLException("The connection has been closed.");        
        }
    }
}
