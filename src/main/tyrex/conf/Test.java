package tyrex.conf;


import java.io.*;
import javax.sql.DataSource;
import org.exolab.castor.xml.*;
import tyrex.util.*;
import tyrex.server.*;


public class Test
{


    public static void main( String args[] )
    {
	try {

	    Server server;

	    Server.debug = true;
	    server = Server.load();
	    server.save( new OutputStreamWriter( System.out ) );

	    Resources resources;

	    Resources.debug = true;
	    resources = Resources.load();
	    resources.save( new OutputStreamWriter( System.out ) );

	} catch ( Exception except ) {
	    System.out.println( except );
	    except.printStackTrace();
	}
    }


}
