package tyrex.tools;


import java.awt.*;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.util.Random;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import tyrex.tm.Tyrex;
import tyrex.server.Configure;
//import tyrex.util.PoolManager;
//import org.exolab.fx.*;


/**
 *
 * Notes about the log: In choke mode there is a limit on the number of
 * transactions that can be activated and pooled. At some point threads
 * will be denied the ability to create new transactions (waiting for
 * more than 5 seconds), but most of the time they will keep hanging.
 * Since the transaction timeout is very short, some threads will be
 * terminated before they get to complete their transaction, reporting
 * a {@link tyrex.server.TransactionTimeoutException}. For some threads
 * the timeout will occur in the middle of suspension and will be thrown
 * when the thread attempts to resume the transaction as a {@link
 * tyrex.server.InvalidTransactionException}. These exceptions will be
 * rare and far apart, however, every so often the active transaction
 * count will drop to 10 and at once 20 transactions will be terminated.
 * This is an indication of the transaction processing capability of Tyrex.
 * In a real life application with larger pools and longer wait times,
 * this scenario will only be duplicated when the server reaches 100%
 * capacity.
 */
public class Demo
    extends Thread
{


    private static TransactionManager _tmManager;


    private static Random             _random = new Random();


    private static PrintWriter        _writer;


    public static void main( String[] args )
    {
	if ( hasArgument( args, "help" ) ) {
	    showUsage();
	    System.exit( 0 );
	} else if ( args.length == 0 ) {
	    System.out.println( "Run with --help to see list of options" );
	}

	try {

	    Configure config;
	    int       count;

	    if ( hasArgument( args, "log" ) )
		_writer = new PrintWriter( new FileOutputStream( getArgumentValue( args, "log", "log" ) ) );
	    else
		_writer = new PrintWriter( System.out, true );

	    // Create a default configuration for this server.
	    // --activity will log all the transaction server
	    // activities to the specified writer.
	    config = new Configure();
	    if ( hasArgument( args, "activity" ) )
		config.setLogWriter( _writer );
	    config.startServer();

	    if ( hasArgument( args, "choke" ) ) {
		//config.getPoolManager().setActiveLimit( 30 );
		//config.getPoolManager().setUpperLimit( 50 );
		//config.getPoolManager().setWaitTimeout( 5 );
		config.setTransactionTimeout( 10 );
		config.setThreadTerminate( true );
	    }
	    
	    // --meter will start a meter running.
	    /*
	    if ( hasArgument( args, "meter" ) )
		new Meter( _writer, 200 );
	    */

	    // --noawt Unless requested we are running with GUI
	    /*if ( ! hasArgument( args, "noawt" ) ) {
		DemoFrame demo;

		demo = new DemoFrame( config );
		demo.show();
	    }*/

	    // Obtain a transaction manager.
	    _tmManager = Tyrex.getTransactionManager();

	    // Determine the number of threads to start.
	    count = getArgumentValue( args, "count", 100 );
	    _writer.println( "Running demonstration with " + count + " concurrent threads" );
	    while ( count-- > 0 ) {
		new Demo().start();
	    }

	    // --dump Every two seconds run a dump of all the
	    // transactions in the server.
	    if ( hasArgument( args, "dump" ) ) {
		while ( true ) {
		    Thread.sleep( 2000 );
		    Tyrex.dumpTransactionList( _writer );
		}
	    }

	} catch ( Exception except ) {
	    _writer.println( except );
	    except.printStackTrace( _writer );
	}
    }


    static boolean hasArgument( String[] args, String name )
    {
	int i;

	name = "--" + name;
	for ( i = 0 ; i < args.length ; ++i )
	    if ( args[ i ].equals( name ) )
		return true;
	return false;
    }


    static int getArgumentValue( String[] args, String name, int def )
    {
	int i;

	name = "--" + name;
	for ( i = 0 ; i < args.length ; ++i )
	    if ( args[ i ].equals( name ) && i + 1 < args.length ) {
		try {
		    return Integer.parseInt( args[ i + 1 ] );
		} catch ( NumberFormatException except ) { }
	    }
	return def;
    }


    static String getArgumentValue( String[] args, String name, String def )
    {
	int i;

	name = "--" + name;
	for ( i = 0 ; i < args.length ; ++i )
	    if ( args[ i ].equals( name ) && i + 1 < args.length ) {
		return args[ i + 1 ];
	    }
	return def;
    }


    static void showUsage()
    {
	System.out.println( "java tyrex.server.Demo [options]" );
	System.out.println( "  --help      Show this message" );
	//System.out.println( "  --noawt     Do not start GUI" );
	System.out.println( "  --count n   How many threads to run (default 100)" );
	System.out.println( "  --log file  Dump all logs to the named file" );
	System.out.println( "  --meter     Log: meter counters" );
	System.out.println( "  --activity  Log: transaction server activity log" );
	System.out.println( "  --dump      Log: transaction list every 2 seconds" );
	System.out.println( "  --choke     Unrealistic limit on active transactions" );
	System.out.println( "" );
    }


    public void run()
    {
	Transaction tx;

	while ( true ) {
	    try {
                //System.out.println(Thread.currentThread().getName() + "begin");
		_tmManager.begin();
		Thread.sleep( _random.nextInt( 5000 ) );
		/*if ( _random.nextBoolean() ) {
		    _tmManager.begin();
		    Thread.sleep( _random.nextInt( 1000 ) );
		    if ( _random.nextBoolean() )
			_tmManager.commit();
		    else
			_tmManager.rollback();
		    Thread.sleep( _random.nextInt( 1000 ) );
		} else*/ if ( _random.nextBoolean() ) {
                    //System.out.println(Thread.currentThread().getName() + "suspend");
		    tx = _tmManager.suspend();
		    Thread.sleep( _random.nextInt( 5000 ) );
                    //System.out.println(Thread.currentThread().getName() + "resume");
		    _tmManager.resume( tx );
		}
		if ( _random.nextBoolean() ) {
                    //System.out.println(Thread.currentThread().getName() + "commit");
                    _tmManager.commit();
                }
		    
                else {
                    //System.out.println(Thread.currentThread().getName() + "rollback");
                    _tmManager.rollback();
                }
		Thread.sleep( _random.nextInt( 5000 ) );
	    } catch ( Exception except ) {
		_writer.println( except );
                except.printStackTrace();
                try {
		    Tyrex.dumpCurrentTransaction( _writer );
		    Tyrex.recycleThread();
		} catch ( Exception except2 ) { }
	    }
	}
    }


    /*static class DemoFrame
	extends Frame
    {


	DemoFrame( Configure config )
	{
	    setTitle( "Tyrex - Performance Monitor" );
	    setLayout( new FlowLayout( FlowLayout.CENTER ) );
	    StatusPanel sp = new StatusPanel( "Tyrex - Performance Monitor" );
	    sp.addDataFeed( new MeterDataFeeder( "TX/A", "Active transaction count",
						 MeterDataFeeder.TYPE_ACTIVE, 0 ) );
	    sp.addDataFeed( new MeterDataFeeder( "TX/T", "Total transaction count",
						 MeterDataFeeder.TYPE_TOTAL, 0 ) );
	    sp.addDataFeed( new MeterDataFeeder( "TX/C", "Created  transaction counter",
		 MeterDataFeeder.TYPE_CREATED, config.getPoolManager().getUpperLimit() ) );
	    sp.addDataFeed( new MeterDataFeeder( "TX/K", "Terminated  transaction counter",
		 MeterDataFeeder.TYPE_KILLED, config.getPoolManager().getUpperLimit() ) );

	    add( sp );
	    pack();
	    new Thread( sp ).start();
	}


    }*/


    /*static class MeterDataFeeder
	extends StatusDataFeed
    {


	public static final int TYPE_ACTIVE = 0;
	public static final int TYPE_TOTAL = 1;
	public static final int TYPE_CREATED = 2;
	public static final int TYPE_KILLED = 3;


	private String _desc;


	private String _title;


	private Meter   _meter;


	private int     _last;


	private int     _maximum;


	private int     _type;


	MeterDataFeeder( String title, String desc, int type, int maximum )
	{
	    _title = title;
	    _desc = desc;
	    _type = type;
	    _maximum = maximum;
	    _meter = new Meter();
	}


	public String getDescription()
	{
	    return _desc;
	}


	public String getTitle()
	{
	    return _title;
	}


	public float getPercent()
	{
	    int result;

	    switch ( _type ) {
	    case TYPE_ACTIVE:
		return  _meter.getActiveCountPct();
	    case TYPE_TOTAL:
		return  _meter.getTotalCountPct();
	    case TYPE_CREATED:
		result = ( (int) _meter.getCreatedCounter() - _last );
		_last = (int) _meter.getCreatedCounter();
		if ( result > _maximum )
		    _maximum = result * 10;
		return (float) result / (float) _maximum;
	    case TYPE_KILLED:
		result = ( (int) _meter.getTerminatedCounter() - _last );
		_last = (int) _meter.getTerminatedCounter();
		if ( result > _maximum )
		    _maximum = result * 10;
		return (float) result / (float) _maximum;
	    default:
		return 0F;
	    }

	}


	public Number getActual()
	{
	    int result;
		
	    switch ( _type ) {
	    case TYPE_ACTIVE:
		return new Integer( _meter.getActiveCount() );
	    case TYPE_TOTAL:
		return new Integer( _meter.getTotalCount() );
	    case TYPE_CREATED:
		result = ( (int) _meter.getCreatedCounter() - _last );
		return new Integer( result );
	    case TYPE_KILLED:
		result = ( (int) _meter.getTerminatedCounter() - _last );
		return new Integer( result );
	    default:
		return new Integer( 0 );
	    }
	}



    }*/


}

