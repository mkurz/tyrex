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


package tyrex.jdbc.xa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/////////////////////////////////////////////////////////////////////
// SybaseEnabledDataSource
/////////////////////////////////////////////////////////////////////

/**
 * This class defines a daemon thread to truncate the transaction log
 * of a Sybase database. If the database name is not given then the
 * daemon thread is not run.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public final class SybaseEnabledDataSource 
    extends EnabledDataSource
{
    /**
     * The time to wait before attempting to truncate the sybase log,
     * in milliseconds.
     */
    public static long  DEFAULT_WAIT_TIME = 5000;

    /**
     * The user name used to get the connection, 
     * to execute the truncation sql
     */
    private /*final*/ String _userName; // compiler error

    /**
     * The password used to get the connection,
     * to execute the truncation sql
     */
    private /*final*/ String _password; // compiler error

    /**
     * The sql to execute
     */
    private /*final*/ String _sql; // compiler error

    /**
     * The time to wait before attempting to truncate the sybase log,
     * in milliseconds.
     */
    private long _waitTime = DEFAULT_WAIT_TIME;


    /**
     * Create the SybaseEnabledDataSource with the specified database.
     * <p>
     * The user name and password specified are only used to truncate
     * the Sybase transaction log.
     *
     * @param databaseName the database name. Can be null.
     * @param userName the user name. Can be null.
     * @param password the passowrd. Can be null.
     */
    public SybaseEnabledDataSource(String databaseName, String userName, String password)
    {
        String trimmedDatabaseName = null == databaseName ? "" : databaseName.trim();

        _userName = userName;
        _password = password;

        if (0 == trimmedDatabaseName.length()) {
            _sql = null;    
        }
        else {
            Thread truncThread;

            _sql = "dump tran " + trimmedDatabaseName + " with truncate_only";

            // Create a background thread that will track transactions
        	// that timeout, abort them and release the underlying
        	// connections to the pool.
        	truncThread = new Thread( new TruncRunnable(), "Sybase Transaction Log Truncation Daemon"  );
        	truncThread.setPriority( Thread.MIN_PRIORITY );
        	truncThread.setDaemon( true );
        	truncThread.start();
        }
    }


    /**
     * Get the truncation wait time in milliseconds.
     *
     * @return the truncation wait time
     *      in milliseconds.
     */
    public long getWaitTime()
    {
        return _waitTime;
    }


    /**
     * Set the truncation wait time.
     *
     * @param waitTime the new wait time in
     *      milliseconds.
     */
    public void setWaitTime(long waitTime)
    {
        if (0 >= waitTime) {
            throw new IllegalArgumentException("The argument 'waitTime' must be greater than 0.");
        }

        _waitTime = waitTime;
    }

    /**
     * The runnable for truncating the Sybase transaction log
     */
    private class TruncRunnable 
        implements Runnable
    {
        public void run() 
        {
            Connection connection = null;
            PreparedStatement stmt = null;

            while (true) {
                try {
                    Thread.currentThread().sleep(_waitTime);
                }
                catch (InterruptedException e) {
                }

                synchronized (SybaseEnabledDataSource.this) {
                    try {
                        //System.out.println("dumping tran log");
                        connection = SybaseEnabledDataSource.this.getXAConnection(_userName, _password).getConnection();
                        stmt = connection.prepareStatement(_sql);
                        stmt.execute();
                        connection.commit();
                        //System.out.println("dumped tran log");
                    }
                    catch(Exception e) {
                        System.out.println("failed to dump tran log");
                        e.printStackTrace();
                    }
                    if (null != connection) {
                        try {    
                            connection.close();
                        }
                        catch (SQLException e){
                        }
                        connection = null;
                    }

                    if (null != stmt) {
                        try {    
                            stmt.close();
                        }
                        catch (SQLException e){
                        }
                        stmt = null;
                    }
                }
            }
        }
    }

    /**
     * Construct the JDBC URL used to connect to the database.
     *
     * @return the JDBC URL used to connect to the database.
     */
    //TODO make this method smarted about Sybase urls
    /*protected String createJDBCURL()
    {
        return super.createJDBCURL();
    }*/
}
