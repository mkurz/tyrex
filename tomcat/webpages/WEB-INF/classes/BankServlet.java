import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.exolab.castor.jdo.ODMG;
import org.exolab.castor.xml.Marshaller;
import org.apache.xalan.xslt.*;
import org.apache.xml.serialize.*;
import org.apache.xerces.dom.DocumentImpl;
import org.odmg.Database;
import org.odmg.Transaction;
import org.odmg.OQLQuery;
import bank.*;


public class BankServlet
    extends HttpServlet
{


    public void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();

        response.setContentType("text/html");

	try {
	    ODMG            odmg;
	    Database        db;
	    Transaction     tx;
	    OQLQuery        oql;

	    Account           from;
	    Account           to;
	    AccountHolder     holder;

	    // Open the Bank database
	    odmg = new ODMG();
	    db = odmg.newDatabase();
	    db.open( "bank", db.OPEN_EXCLUSIVE );

	    // Begin a new transaction -- all database operations performed within
	    // the context of a transaction
	    tx = odmg.newTransaction();
	    tx.begin();

	    // Create a new query to retrieve account information
	    oql = odmg.newOQLQuery();
	    oql.create( "SELECT a FROM bank.Account a WHERE id = $1" );
	    oql.bind( Integer.valueOf( request.getParameter( "fromId" ) ) );
	    from = (Account) oql.execute();

	    // Determine if we're doing a transfer and report the transaction status
	    if ( request.getParameter( "targetId" ) == null ||
		 request.getParameter( "targetId" ).length() == 0 ) {
		from.tx.status = bank.Transaction.NothingToDo;
	    } else {

		// Get the target account and the amount to transfer
		from.tx.targetId = Integer.parseInt( request.getParameter( "targetId" ) );
		from.tx.amount = Integer.parseInt( request.getParameter( "amount" ) );

		// Retrieve the traget account
		oql.bind( new Integer( from.tx.targetId ) );
		to = (Account) oql.execute();
		if ( to == null ) {
		    // Report that there is no such account
		    from.tx.status = bank.Transaction.NoSuchAccount;
		} else {

		    // Move money from one account to the other
		    from.balance -= from.tx.amount;
		    to.balance += from.tx.amount;

		    if ( from.tx.account.balance < 0 ) {
			// Overdraft -- cancel the transfer!
			tx.abort();

			// XXX This is only required until CacheEngine is fixed
			tx.begin();
			oql.create( "SELECT a FROM bank.Account a WHERE id = $1" );
			oql.bind( new Integer( 1 ) );
			from = (Account) oql.execute();
			// XXX

			// Report why we failed
			from.tx.status = bank.Transaction.OverDraft;
		    } else {
			// Transaction succeeded!
			from.tx.status = bank.Transaction.Completed;
		    }
		}
	    }	

	    // Close the transaction and database, we're done
	    tx.commit();
	    db.close();

	    // Time to report the transaction
	    XSLTProcessor  xslt;
	    StylesheetRoot stlr;
	    StringWriter   pipe;

	    pipe = new StringWriter();
	    xslt = XSLTProcessorFactory.getProcessor();
	    // Print account information as XML
	    Marshaller.marshal( from, pipe );
	    // Transform into HTML using stylesheet
	    xslt.process( new XSLTInputSource( new StringReader( pipe.getBuffer().toString() ) ),
			  new XSLTInputSource( "file:/opt/tomcat/webpages/WEB-INF/classes/account.xsl" ),
			  new XSLTResultTarget( out ) );
	    
	} catch ( Throwable except ) {
	    out.println( "<pre>except" );
	    except.printStackTrace( out );
	    out.println( "</pre>" );
	}
    }


    public void init( ServletConfig config )
    {
	try {
	    ODMG            odmg;

	    odmg = new ODMG();
	    odmg.loadMapping( "file:/opt/tomcat/webpages/WEB-INF/classes/mapping.xml" );
	} catch ( Exception except ) {
	    System.out.println( except.toString() );
	    except.printStackTrace();
	}
    }


}

