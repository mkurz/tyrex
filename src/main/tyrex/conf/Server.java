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
 * $Id: Server.java,v 1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.conf;


import java.io.Serializable;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.Vector;
import java.util.Enumeration;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import tyrex.server.Configure;
import tyrex.util.PoolManager;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import tyrex.util.Messages;
import tyrex.util.Logger;


/**
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see Configure
 * @see LogOption
 * @see PoolManager
 */
public class Server
    implements Serializable
{


    public static class DTD
    {

	/**
	 * The public identifier of the DTD.
	 * <pre>
	 * -//EXOLAB/Tyrex Configuration DTD Version 1.0//EN
	 * </pre>
	 */
	public static final String PublicId = 
	    "-//EXOLAB/Tyrex Configuration DTD Version 1.0//EN";

	/**
	 * The system identifier of the DTD.
	 * <pre>
	 * http://tyrex.exolab.org/tyrex.dtd
	 * </pre>
	 */
	public static final String SystemId =
	    "http://tyrex.exolab.org/tyrex.dtd";

	/**
	 * The resource named of the DTD:
	 * <tt>/tyrex/conf/tyrex.dtd</tt>.
	 */
	private static final String Resource =
	    "/tyrex/conf/tyrex.dtd";

    }


    public static class Namespace
    {

	/**
	 * The public identifier of the XML schema.
	 * <pre>
	 * -//EXOLAB/Tyrex Configuration Schema Version 1.0//EN
	 * </pre>
	 */
	public static final String PublicId =
	    "-//EXOLAB/Tyrex Configuration Schema Version 1.0//EN";

	/**
	 * The system identifier of the XML schema.
	 * <pre>
	 * http://tyrex.exolab.org/tyrex.xsd
	 * </pre>
	 */
	public static final String SystemId =
	    "http://tyrex.exolab.org/tyrex.xsd";
	
	/**
	 * The namespace prefix: <tt>tyrex</tt>
	 */
	public static final String prefix =
	    "tyrex";

	/**
	 * The namespace URI:
	 * <pre>
	 * http://tyrex.exolab.org/tyrex
	 * </pre>
	 */
	public static final String URI =
	    "http://tyrex.exolab.org/tyrex";
	
	/**
	 * The resource named of the server configuration XML schema:
	 * <tt>/tyrex/conf/tyrex.xsd</tt>.
	 */
	private static final String Resource =
	    "/tyrex/conf/tyrex.xsd";
	
    }


    public static class Implementation
    {


	/**
	 * The implementation vendor name, obtained from the manifest file.
	 */
	private static final String  Vendor;


	/**
	 * The implementation version number, obtained from the manifest file.
	 */
	private static final String  Version;


	static
	{
	    Package pkg;
	    
	    // Determine the implementation vendor name and version number
	    // from the package information in the manifest file
	    pkg = Server.class.getPackage();
	    if ( pkg != null ) {
		Vendor = pkg.getImplementationVendor();
		Version = pkg.getImplementationVersion();
	    } else {
		Vendor = "Exoffice Technologies. Inc";
		Version = "";
	    }
	}
	

    }


    /**
     * The name of the default configuration file to be loaded when no
     * file is specified.
     */
    public static final String FileName = "tyrex.xml";


    /**
     * The Tyrex configuration object.
     */
    private Configure       _configure;


    /**
     * The pool manager if the pool manager has been specified before
     * the configure object. Otherwise the pool manager is immediately
     * associated with the configure object.
     */
    private PoolManager     _pool;


    /**
     * The source XML file that was used to load the configuration and
     * can also be used to save it back. Null if the configuration was
     * loaded directly from a stream.
     */
    private transient File  _source;


    /**
     * List of log options used to construct log writers.
     */
    private Vector          _logOptions = new Vector();


    /**
     * If true will emit more information about problems reading/writing
     * the XML configuration file.
     */
    public static boolean debug;


    /**
     * Loads the default configuration file. The configuration file
     * <tt>tyrex.xml</tt> is loaded from either the current
     * directory or the <tt>$JAVA_HOME/lib</tt> in that order. If no
     * such file is found in either directory, an {@link IOException}
     * is thrown. The configuration file can be updated with a call
     * to {@link #save()}.
     *
     * @return A server configuration object
     * @throws IOException The configuration file was not found or
     *   an I/O error occured while reading it
     */
    public static Server load()
	throws IOException
    {
	File        file;

	file = new File( System.getProperty( "user.dir" ), FileName );
	try {
	    if ( file.exists() ) {
		Logger.getLogger().println( Messages.format( "tyrex.conf.loadingServer", file ) );
		return load( file );
	    }
	    file = new File( System.getProperty( "java.home" ), "lib" );
	    file = new File( file, FileName );
	    if ( file.exists() ) {
		Logger.getLogger().println( Messages.format( "tyrex.conf.loadingServer", file ) );
		return load( file );
	    }
	} catch ( IOException except ) {
	    Logger.getLogger().println( Messages.format( "tyrex.conf.loadingServerError", except ) );
	    throw except;
	}
	Logger.getLogger().println( Messages.format( "tyrex.conf.loadingServerMissing", FileName ) );
	throw new IOException( Messages.format( "tyrex.conf.loadingServerMissing", FileName ) );
    }


    /**
     * Loads the specified configuration file. The configuration file
     * can be updated with a call to {@link #save()}.
     *
     * @return A server configuration object
     * @throws IOException The configuration file was not found or
     *   an I/O error occured while reading it
     */
    public static Server load( File file )
	throws IOException
    {
	Server      server;

	server = load( new FileReader( file ) );
	server._source = file;
	return server;
    }


    /**
     * Loads the configuration from the specified reader.
     *
     * @param reader A reader for the configuration file
     * @return A server configuration object
     * @throws IOException An I/O error occured while reading
     */
    public static Server load( Reader reader )
	throws IOException
    {
	Unmarshaller unmarshaller;

	try {
	    unmarshaller = new Unmarshaller( Server.class );
	    if ( debug )
		unmarshaller.setLogWriter( Logger.getLogger() );
	    unmarshaller.setEntityResolver( new SchemaEntityResolver() );
	    return (Server) unmarshaller.unmarshal( reader );
	} catch ( IOException except ) {
	    Logger.getLogger().println( Messages.format( "tyrex.conf.loadingResourcesError", except ) );
	    throw except;
	} catch ( Exception except ) {
	    Logger.getLogger().println( Messages.format( "tyrex.conf.loadingResourcesError", except ) );
	    throw new IOException( "Nested exception: " + except );
	}
    }


    /**
     * Saved this configuration to the specified writer.
     *
     * @param writer A writer for the configuration file
     * @throws IOException An I/O error occured while writing
     */
    public void save( Writer writer )
	throws IOException
    {
	try {
	    if ( debug )
		Marshaller.marshal( this, writer, Logger.getLogger() );
	    else
		Marshaller.marshal( this, writer );
	} catch ( IOException except ) {
	    throw except;
	} catch ( Exception except ) {
	    throw new IOException( "Nested exception: " + except.toString() );
	}
    }


    /**
     * Saves this configuration to the file from which it was loaded.
     * This configuration must have been loaded with a call to either
     * {@link #load()} or {@link #load(File)}. Throws an {@link
     * IOException} if this configuration was not loaded from a
     * specified or default file.
     *
     * @throws IOException The configuration was not loaded from a
     *   specified or default file, or an I/O error occured reading it
     */
    public void save()
	throws IOException
    {
	if ( _source == null )
	    throw new IOException( Messages.message( "tyrex.conf.savingServerNotLoaded" ) );
	save( new FileWriter( _source ) );
    }


    /**
     * Returns the implementation vendor name (could be null).
     */
    public String getVendor()
    {
	return Implementation.Vendor;
    }


    /**
     * Returns the implementation version number (could be null).
     */
    public String getVersion()
    {
	return Implementation.Version;
    }


    /**
     * Sets the transaction server configuration object. If limits
     * were already set, they will be associated with this configuration
     * object.
     */
    public void setConfig( Configure configure )
    {
	_configure = configure;
	if ( _pool != null ) {
	    _configure.setPoolManager( _pool );
	    _pool = null;
	}
    }


    /**
     * Returns the transaction server configuration object.
     * Will be null if not specified.
     */
    public Configure getConfig()
    {
	return _configure;
    }


    /**
     * Sets the transaction server limits. If a configuration
     * object has been specified, these limits will apply to it,
     * if not, they will apply to the next configuration object
     * to be specified.
     */
    public void setLimits( PoolManager pool )
    {
	if ( _configure != null )
	    _configure.setPoolManager( pool );
	else
	    _pool = pool;
    }


    /**
     * Returnss the transaction server limits.
     */
    public PoolManager getLimits()
    {
	if ( _configure != null )
	    return _configure.getPoolManager();
	else
	    return _pool;
    }


    public void setLogOption( LogOption logOption )
    {
	_logOptions.addElement( logOption );
    }


    public Enumeration listLogOption()
    {
	return _logOptions.elements();
    }


    static class SchemaEntityResolver
	implements EntityResolver
    {

	public InputSource resolveEntity( String publicId, String systemId )
	    throws IOException
	{
	    InputSource source;

	    if ( publicId.equals( DTD.PublicId ) ||
		 systemId.equals( DTD.SystemId ) ) {
		source = new InputSource();
		source.setByteStream( Server.class.getResourceAsStream( DTD.Resource ) );
		return source;
	    }

	    if ( publicId.equals( Namespace.PublicId ) ||
		 systemId.equals( Namespace.SystemId ) ) {
		source = new InputSource();
		source.setByteStream( Server.class.getResourceAsStream( Namespace.Resource ) );
		return source;
	    }

	    return null; 
	}

    }


}










