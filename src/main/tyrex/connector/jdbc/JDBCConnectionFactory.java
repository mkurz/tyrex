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
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
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
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: JDBCConnectionFactory.java,v 1.1 2000/04/10 20:52:34 arkin Exp $
 */


package tyrex.connector.jdbc;


import java.io.PrintWriter;
import java.sql.SQLException;
import javax.sql.XADataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import tyrex.connector.ManagedConnectionFactory;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ConnectionException;


/**
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/04/10 20:52:34 $
 */
public class JDBCConnectionFactory
    implements ManagedConnectionFactory
{


    private XADataSource  _xaDataSource;


    private String        _name;


    private byte[]        _password;


    private String        _userName;


    private int           _waitTimeout;


    private int           _loginTimeout;


    private int           _minPool;


    private int           _maxPool;


    private String        _description;


    private PrintWriter   _logWriter;


    public void setDataSource( String name )
    {
        _name = name;
    }


    public String getDataSource()
    {
        return _name;
    }


    public String getDescription()
    {
        return _description;
    }


    public void setDescription( String description )
    {
        description = description;
    }


    public PrintWriter getLogWriter()
    {
        return _logWriter;
    }


    public void setLogWriter( PrintWriter logWriter )
    {
        _logWriter = logWriter;
    }


    public int getLoginTimeout()
    {
        return _loginTimeout;
    }


    public void setLoginTimeout( int ms )
    {
        _loginTimeout = ms;
    }


    public int getMaxPool()
    {
        return _maxPool;
    }


    public void setMaxPool( int max )
    {
        _maxPool = max;
    }


    public int getMinPool()
    {
        return _minPool;
    }


    public void setMinPool( int min )
    {
        _minPool = min;
    }


    public int getWaitTimeout()
    {
        return _waitTimeout;
    }

    
    public void setWaitTimeout( int ms )
    {
        _waitTimeout = ms;
    }

    
    public String getUserName()
    {
        return _userName;
    }


    public void setUserName( String userName )
    {
        _userName = userName;
    }


    public void setPassword( byte[] password )
    {
        _password = (byte[]) password.clone();
    }


    public synchronized ManagedConnection getManagedConnection()
        throws ConnectionException
    {
        try {
            if ( _xaDataSource == null ) {
                InitialContext ctx;
                
                if ( _name == null )
                    throw new ConnectionException( "DataSource name not configured" );
                try {
                    ctx = new InitialContext();
                    _xaDataSource = (XADataSource) ctx.lookup( "java:conf/datasource/" + _name );
                } catch ( NamingException except ) {
                    throw new ConnectionException( except );
                } catch ( ClassCastException except ) {
                    throw new ConnectionException( "DataSource " + _name + " is not an XADataSource" );
                }
                _xaDataSource.setLogWriter( _logWriter );
                _xaDataSource.setLoginTimeout( _loginTimeout );
            }
            if ( _userName == null )
                return new JDBCManagedConnection( _xaDataSource.getXAConnection() );
            else
                return new JDBCManagedConnection( _xaDataSource.getXAConnection( _userName, new String( _password ) ) );
        } catch ( SQLException except ) {
            throw new ConnectionException( except );
        }
    }


}




