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


package transaction;

import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import transaction.configuration.Datasource;

/////////////////////////////////////////////////////////////////////
// DataSourceEntry
/////////////////////////////////////////////////////////////////////

/**
 * Collects information about a XA data source used for testing
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
final class DataSourceEntry {

    /**
     * The XA Data source
     */
    private final XADataSource _dataSource;

    /**
     * The xa Data source used for creation
     */
    private final XADataSource _creationDataSource;

    /**
     * The number of milliseconds to sleep after
     * ending an xa resource with the TMFAIL flag.
     * <P>
     * A value of zero or less means don't wait.
     */
    //private final long _failSleepTime;

    /**
     * True if performance can be tested.
     */
    //private final boolean _performanceTest;

    /**
     * True if the XA resources can be
     * used in a new transaction when they've
     * been delisted using XAResource.TMSUCCESS in
     * an existed transaction that has not been 
     * committed or rolled back.
     */
    //private final boolean _reuseDelistedXAResources;

    /**
     * The name of the table that is 
     * to be created in the database.
     */
    //private final String _tableName;

    /**
     * The name
     */
    //private final String _name;

    /**
     * The user name
     */
    //private final String _userName;

    /**
     * The password
     */
    //private final String _password;

    /**
     * The config
     */
    private final Datasource _config;

    /**
     * Create the DataSourceEntry
     *
     * @param dataSource the data source (required)
     * @param creationDataSource the data source used to create the test
     *      tables (required)
     * @param config the configuration (required)
     */
    DataSourceEntry(XADataSource dataSource, XADataSource creationDataSource, 
                    Datasource config) {
        if (null == dataSource) {
            throw new IllegalArgumentException("The argument 'dataSource' is null.");
        }
        if (null == creationDataSource) {
            throw new IllegalArgumentException("The argument 'creationDataSource' is null.");
        }
        if (null == config) {
            throw new IllegalArgumentException("The argument 'config' is null.");    
        }

        if ((null == config.getTableName()) || (0 == config.getTableName().length())) {
            throw new IllegalArgumentException("The argument 'tableNamePrefix' is null or empty.");    
        }
        _dataSource = dataSource;
        _creationDataSource = creationDataSource;
        _config = config;
    }
    /**
     * Return the user name
     *
     * @return the user name
     */
    String getUserName() {
        return _config.getUserName();
    }

    /**
     * Return the password
     *
     * @return the password
     */
    String getPassword() {
        return _config.getPassword();
    }

    /**
     * Return true if the tables are to be created
     * and/or dropped as part of initialization and
     * finalization
     */
    boolean getCreateDropTables() {
        return _config.getCreateDropTables();
    }


    /**
     * Return the name
     *
     * @return the name
     */
    String getName() {
        return _config.getName();
    }

    /**
     * Return the XA Data source
     *
     * @return the XA Data source
     */
    XADataSource getDataSource() {
        return _dataSource;
    }

    /**
     * Return the XA Data source used to create the test tables.
     *
     * @return the XA Data source
     */
    XADataSource getCreateDataSource() {
        return _creationDataSource;
    }

    /**
     * Return the number of milliseconds to sleep after
     * ending an xa resource with the TMFAIL flag.
     * <P>
     * A value of zero or less means don't wait.
     *
     * @return the number of milliseconds to sleep after
     *      ending an xa resource with the TMFAIL flag.
     */
    long getFailSleepTime() {
        return _config.getFailSleepTime();
    }

    /**
     * Return true if performance can be tested.
     *
     * @return true if performance can be tested.
     */
    boolean getPerformanceTest() {
        return _config.getPerformanceTest();
    }

    /**
     * True if the XA resources can be
     * used in a new transaction when they've
     * been delisted using XAResource.TMSUCCESS in
     * an existed transaction that has not been 
     * committed or rolled back.
     */
    boolean getReuseDelistedXAResources() {
        return _config.getReuseDelistedXaresources();
    }

    /**
     * The name of the table that is 
     * to be created in the database.
     */
    String getTableName() {
        return _config.getTableName();
    }

    /**
     * Return the XA Connection from the data source entry.
     *
     * @param dataSourceEntry the data source entry
     * @return the XA Connection from the data source entry.
     * @throws SQLException if there is a problem getting the XA connection
     */
    XAConnection getXAConnection() 
        throws SQLException {
        return null == _config.getUserName() 
                ? _dataSource.getXAConnection()
                : _dataSource.getXAConnection(_config.getUserName(), _config.getPassword());
    }

    /**
     * Return the XA Connection from the data source entry used to
     * create the test tables
     *
     * @param dataSourceEntry the data source entry
     * @return the XA Connection from the data source entry.
     * @throws SQLException if there is a problem getting the XA connection
     */
    XAConnection getXAConnectionForCreation() 
        throws SQLException {
        return null == _config.getUserName() 
                ? _creationDataSource.getXAConnection()
                : _creationDataSource.getXAConnection(_config.getUserName(), _config.getPassword());
    }
}
