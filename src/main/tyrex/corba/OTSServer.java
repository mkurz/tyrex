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
 * Copyright 2000 (C) Intalio Inc. All Rights Reserved.
 */

package tyrex.corba;

import org.omg.CosTransactions.TransactionFactory;

/**
 * This class is the OTS server class. It provides the ability to use
 * Tyrex as an OTS server. Most of this code is extracted from the
 * OpenORB OTS developed by Jerome Daniel & Marina Daniel ( changed to be
 * used with a BOA approach ).
 *
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version 1.0
 */
public final class OTSServer
{
        /**
         * Flag that indicates if an IOR file must be generated
         */
    private static boolean ior_file = false;
     
        /**
         * Flag that indicates if the transaction factory must be bind to the ns
         */
    private static boolean naming = false;            
    
    	/**
    	 * Configuration file name
    	 */
    private static String config_file = "domain.xml";
    
    	/**
    	 * Reference to the transaction domain
    	 */
    private static tyrex.tm.TransactionDomain txDomain = null;
    
        /**
         * This function prints help.
         */
    private static void print_help()
    {
        System.out.println("Command line : \n");
        System.out.println("\tjava tyrex.corba.OTSServer [ orb options ] options\n");
        System.out.println("Options : ");
        
        System.out.println("\t-ior");
        System.out.println("\t\tgenerates a file named 'ots.ior' that contains the TransactionFactory ior.");
        
        System.out.println("\t-naming");
        System.out.println("\t\tbinds the transaction factory reference to the naming service using the");
        System.out.println("\t\tfollowing name : Tyrex\\TransactionFactory");
        
        System.out.println("\t-config");
        System.out.println("\t\tspecifies the configuration file name used by Tyrex to create a domain.");
        System.out.println("\t\tBy default, the current directory is used and the file name is 'domain.xml'");
        
        System.exit(0);
    }
    
        /**
         * This function is used to parse command line arguments.
         */
    private static void parse_args( String[] args )
    {
        for ( int i=0; i<args.length; i++ )
        {
            if ( args[i].equalsIgnoreCase("-help") )
                print_help();
            else
                if ( args[i].equalsIgnoreCase("-ior") )
                    ior_file = true;
                else
                        if ( args[i].equalsIgnoreCase("-naming") )
                            naming = true;
                        else
                            if ( args[i].equalsIgnoreCase("-config") )
                            {
                                if ( i+1 >= args.length )
                                {
                                    System.out.println("The '-config' flag must be followed by a file name...");
                                    System.exit(0);
                                }
                                config_file = args[++i];
                            }
        }
        
        if ( ( ior_file == false ) && ( naming == false ) )
        {
            fatal("OTS Server", "The transaction factory is not exported ( select IOR or Naming Service )");
        }
    }
    
    public static TransactionFactory createTransactionFactory(org.omg.CORBA.ORB orb ) 
    {
        org.omg.CORBA.BOA boa = org.omg.CORBA.BOA.init( orb, new String[0] );
                
        //
        // Gets the default transaction domain
        //
        
        try
        {
        	
        	//txDomain = tyrex.tm.TransactionDomain.getDomain( config_file );
        	//System.out.println(txDomain);
        	//if( txDomain == null ) {
        	  txDomain = tyrex.tm.TransactionDomain.createDomain( config_file );
          
         //} 
        }
        catch ( tyrex.tm.DomainConfigurationException ex )
        {
        	fatal("OTSServer", "Unable to create a transaction domain : " + ex.toString() );
        }
        
        //
        // Identifies the ORB
        //
        
        ((tyrex.tm.impl.TransactionDomainImpl)txDomain).identifyORB( orb, null, null );
        
        //
        // Check for recovery
        //
                
   	try
	{
		txDomain.recover();
	}
	catch ( tyrex.tm.RecoveryException ex )
	{
		fatal("OTSServer", "Unable to complete the recovery : " + ex.toString() );    	
	}
            
        
        //
        // Gets the transaction factory
        //
        
        TransactionFactory ots = txDomain.getTransactionFactory();
        
        //
        // Connect the transaction factory object
        //
        
        orb.connect( ots );
        
        return ots;
    }
    
   /**
     * Shutdown the transaction manager
     */
    public static void shutdownTransactionManager()
    {
    	txDomain.terminate(); 
    }
    
        /**
         * Application entry point
         */
    public static void main( String [] args )
    {
        //
        // Print copyright message
        //
        System.out.println();
        System.out.println("Tyrex Transaction Monitor - OTS version -");
        System.out.println("(c) 1999-2001, Exolab.org");
        System.out.println();
        
        
        //
        // Parse command line arguments
        //
        
        parse_args( args );
        
        //
        // Initialize ORB
        //
        
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);
        
        //
        // Create transaction factory
        //
        TransactionFactory ots = createTransactionFactory(orb);
        
        //
        // Write TransactionService IOR if needed
        //
        if ( ior_file )
        {
            String ots_ior = orb.object_to_string( ots );
            
            try
            {
                java.io.FileOutputStream file = new java.io.FileOutputStream("ots.ior");
                java.io.PrintWriter pfile=new java.io.PrintWriter(file);
                pfile.println(ots_ior);
                pfile.close();
            }
            catch ( java.io.IOException ex )
            {
                fatal("OTS Server", "Unable to generate 'ots.ior'");
            }
            
            System.out.println("IOR file generated ( ots.ior ).\n");
        }
        
        //
        // Bind the TransactionService into the ns
        //
        
        if ( naming )
        {
            bind_to_naming_service( ots, orb );
        }
        
        //
        // Starts the transaction server
        //
        //cfg.startServer();
        
        //
        // Run the application
        //
        try
        {
            System.out.println();
            System.out.println("OTS server is now ready...");
            orb.run();
        }
        catch ( java.lang.Exception ex )
        {
            System.out.println("OTS server stopped...");
            ex.printStackTrace();
        }
    }
    
        /**
         * This operation binds the following object to naming service
         */
    private static void bind_to_naming_service( org.omg.CORBA.Object tf, org.omg.CORBA.ORB orb )
    {
        org.omg.CosNaming.NamingContext naming = null;
        try
        {
            org.omg.CORBA.Object obj = orb.resolve_initial_references("NameService");
            
            naming = org.omg.CosNaming.NamingContextHelper.narrow( obj );
        }
        catch ( org.omg.CORBA.ORBPackage.InvalidName ex )
        {
            fatal("OTS Server", "Unable to resolve NameService");
        }
        
        org.omg.CosNaming.NamingContext tyrex_ots = getNamingContext( naming, "Tyrex" );
        
        org.omg.CosNaming.NameComponent [] tf_name = new org.omg.CosNaming.NameComponent[ 1 ];
        
        tf_name[0] = new org.omg.CosNaming.NameComponent();
        tf_name[0].id = "TransactionFactory";
        tf_name[0].kind = "";
        
        try
        {
            tyrex_ots.resolve( tf_name );
        }
        catch ( java.lang.Exception ex )
        {
            try
            {
                tyrex_ots.bind( tf_name, tf );
            }
            catch ( java.lang.Exception exi )
            {
                fatal("OTS Server", "Unable to bind the transaction factory to naming service.");
            }
            
            return;
        }
        
        try
        {
            tyrex_ots.rebind( tf_name, tf );
        }
        catch ( java.lang.Exception ex )
        {
            fatal("OTS Server", "Unable to rebind the transaction factory to naming service.");
        }
    }
    
        /**
         * Returns a naming context. If this naming context does not exist, we create it.
         */
    private static org.omg.CosNaming.NamingContext getNamingContext( org.omg.CosNaming.NamingContext parent, String id )
    {
        org.omg.CosNaming.NameComponent [] name = new org.omg.CosNaming.NameComponent[ 1 ];
        
        name[0] = new org.omg.CosNaming.NameComponent();
        name[0].id = id;
        name[0].kind = "";
        
        org.omg.CORBA.Object obj = null;
        try
        {
            obj = parent.resolve( name );
            
            return org.omg.CosNaming.NamingContextHelper.narrow( obj );
        }
        catch ( java.lang.Exception ex )
        {
            try
            {
                return parent.bind_new_context( name );
            }
            catch ( java.lang.Exception exi )
            {
                fatal("OTS Server", "Unable to create a naming context.");
            }
        }
        
        return null;
    }
    
        /**
         * Display a fatal message then stops the transaction server and the application
         */
    private static void fatal( String from, String msg )
    {
        tyrex.util.Logger.ots.warn(from + ": " + msg );        
        System.exit(0);
    }
}

