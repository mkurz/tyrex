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

import java.sql.SQLException;
import java.sql.CallableStatement;

/////////////////////////////////////////////////////////////////////
// ClientCallableStatementImpl
/////////////////////////////////////////////////////////////////////

/**
 * Implementation of {@link AbstractClientCallableStatementImpl}.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
final class ClientCallableStatementImpl 
    extends AbstractClientCallableStatementImpl
    implements CallableStatement
{
    /**
     * The underlying prepared statement
     */
    private CallableStatement _statement;


    /**
     * Create the ClientCallableStatementImpl with the specified arguments.
     *
     * @param clientConn the client connection that created
     *      the client statement.
     * @param statement the underlying callable statement
     * @param dataSource the data source from where the 
     *      {@link ClientConnection} came from.
     */
    ClientCallableStatementImpl(CallableStatement statement,
                                ClientConnection clientConn,
                                XADataSourceImpl dataSource)
    {
        super(clientConn, dataSource);

        if (null == statement) {
            throw new IllegalArgumentException("The argument 'statement' is null.");
        }

        _statement = statement;
    }

    /**
     * Return the callable statement.
     *
     * @return the callable statement.
     * @throws SQLException if the statement has been closed.
     */
    CallableStatement getCallableStatement()
        throws SQLException
    {
        if (null == _statement) {
            throw new SQLException("The statement has been closed.");    
        }

        return _statement;
    }


    /**
     * Reset the statement to null as it is not 
     * being used anymore
     */
    void resetStatement()
    {
        _statement = null;
    }
}
