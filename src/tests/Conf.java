package tests;


import java.io.OutputStreamWriter;
import tyrex.conf.Server;
import tyrex.conf.Resources;


public class Conf
{


    public static void main( String args[] )
    {
	try {

	    Server server;

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
