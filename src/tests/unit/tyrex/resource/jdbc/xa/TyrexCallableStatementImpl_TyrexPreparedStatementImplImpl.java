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
 *    permission of Intalio.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio. Exolab is a registered
 *    trademark of Intalio.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999-2001 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: TyrexCallableStatementImpl_TyrexPreparedStatementImplImpl.java,v 1.1 2001/10/31 03:06:26 mills Exp $
 * Date        Author    Changes
 *
 * 2001/10/30  Mills     Created
 *
 */


package tyrex.resource.jdbc.xa;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.sql.Statement;
import java.sql.ResultSet;

import java.io.PrintWriter;


/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.1 $
 */

public class TyrexCallableStatementImpl_TyrexPreparedStatementImplImpl
    extends TyrexPreparedStatementImplTest
{
    public TyrexCallableStatementImpl_TyrexPreparedStatementImplImpl(String name)
    {
        super(name);
    }

    /**
     * The abstract method for creating an instance of TyrexConnectionTest.
     */

    public TyrexPreparedStatementImpl newTyrexPreparedStatementImpl()
        throws Exception
    {
        EnabledDataSource dataSource = new EnabledDataSource();
        XAConnectionImpl conn
            = (XAConnectionImpl) dataSource.getPooledConnection();
        ClientConnection connection = new ClientConnection(conn, dataSource,
                                                           0);
        TyrexResultSetImpl resultSet = new TyrexResultSetImpl(null,
                                                              connection);
        Statement statement = resultSet.getStatement();
        return new TyrexPreparedStatementImpl((java.sql.PreparedStatement)statement,
                                              (TyrexConnection) connection);
    }
}
