package tyrex.jdbc.xa;


import java.io.*;
import java.sql.*;
import javax.sql.*;


public class Test
{


    public static void main( String args[] )
    {
	try {

	    EnabledDataSource ds;
	    Connection        conn;
	    ResultSet         rs;

	    Class.forName( "postgresql.Driver" );

	    ds = new EnabledDataSource();
	    ds.setDriverName( "jdbc:postgresql" );
	    ds.setUser( "arkin" );
	    ds.setPassword( "" );
	    ds.setDatabaseName( "test2" );
	    ds.setLogWriter( new PrintWriter( System.out ) );

	    conn = ds.getConnection();
	    rs = conn.createStatement().executeQuery( "select * from test" );
	    rs.next();
	    System.out.println( rs.getString( 1 ) + " - " + rs.getString( 2 ) );
	    conn.close();

	} catch ( Exception except ) {
	    System.out.println( except );
	    except.printStackTrace();
	}
    }


}
