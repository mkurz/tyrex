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
 * $Id: Test.java,v 1.1.1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.naming;


import java.io.*;
import java.util.Hashtable;
import javax.naming.*;
import javax.sql.DataSource;
import tyrex.jdbc.ServerDataSource;
import tyrex.naming.java.*;


/**
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2000/01/11 00:33:46 $
 */
public class Test
{


    public static void main( String args[] )
    {
	try {

	    Context         ctx;
	    DataSource      ds;
	    InitialContext  fsCtx;
	    Hashtable       env;

	    ds = new ServerDataSource();
	    ( (ServerDataSource) ds ).setDescription( "test data source" );

	    // Stage one: create a context that lists all the JDBC data sources.
	    TyrexContext jdbc;

	    jdbc = new TyrexContext( new Hashtable() );
	    jdbc.rebind( "postgres", ds );
	    System.out.println( "JDBC context: bound postgres data source" );


	    // Stage two: create a context that lists specific JDBC data sources
	    // for the current thread.
	    ENCHelper enc;
	    
	    enc = new ENCHelper();
	    enc.addEnvEntry( "test", Integer.class.getName(), "5" );
	    enc.addDataSource( "pool/mydb", (DataSource) jdbc.lookup( "postgres" ) );

	    // Stage three: use this context from the current thread.
	    enc.setThreadContext();
	    System.out.println( "Threaded context: bound comp/env/jdbc/pool/mydbc data source" );

	    env = new Hashtable();
	    env.put( "java.naming.factory.initial", "tyrex.naming.TyrexContextFactory" );

	    ctx = new InitialContext( env );
	    System.out.println( ctx.lookup( "java:/comp/env/jdbc/pool/mydb" ) );
	    System.out.println( "Test type " + ctx.lookup( "java:/comp/env/test" ).getClass() +
				" value " + ctx.lookup( "java:/comp/env/test" ) );
	    // ! Read only
	    try { 
		ctx.unbind( "java:/comp/env/jdbc/pool/mydb" );
		System.out.println( "Error: java: is not read-only" );
	    } catch ( NamingException except ) {
		System.out.println( except.getMessage() );
	    }

	    // Stage four: put some different objects in the read-only context
	    enc = new ENCHelper();
	    enc.addDataSource( "pool/mydb", new ServerDataSource() );
	    enc.setThreadContext();
	    System.out.println( "Threaded context: updated by server" );
	    System.out.println( ctx.lookup( "java:/comp/env/jdbc/pool/mydb" ) );

	} catch ( Throwable except ) {
	    System.out.println( except );
	    except.printStackTrace();
	}
    }


}
