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
 *
 * $Id: Server.java,v 1.6 2000/09/08 23:18:52 mohammed Exp $
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
import java.net.URL;
import java.util.Hashtable;
import java.util.Enumeration;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import tyrex.tm.TransactionDomain;
import tyrex.util.Messages;
import tyrex.util.Logger;


/**
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.6 $ $Date: 2000/09/08 23:18:52 $
 * @see Configure
 * @see LogOption
 * @see PoolManager
 */
public class Server
    extends Constants
    implements Serializable
{



    /**
     * The source XML file that was used to load the configuration and
     * can also be used to save it back. Null if the configuration was
     * loaded directly from a stream.
     */
    private transient File  _source;


    private Hashtable       _domains = new Hashtable();


    private String          _default;


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
	File file;
	URL  url;      

	try {
	    url = Server.class.getResource( ResourceName );
        if ( url != null ) {     
		file = new File( url.getFile() );
		if ( file.exists() ) {
		    Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingServer", file ) );
		    return load( file );
		}
	    }

        
        file = new File( System.getProperty( "user.dir" ), FileName );
        if ( file.exists() ) {
		Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingServer", file ) );
		return load( file );
	    }
        
	    file = new File( System.getProperty( "java.home" ), "lib" );
	    file = new File( file, FileName );
        if ( file.exists() ) {
		Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingServer", file ) );
		return load( file );
	    }
	} catch ( IOException except ) {
	    Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingServerError", except ) );
	    throw except;
	}
	Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingServerMissing", FileName ) );
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
	    unmarshaller.setLogWriter( Logger.getSystemLogger() );
	    unmarshaller.setEntityResolver( new SchemaEntityResolver() );
	    return (Server) unmarshaller.unmarshal( reader );
	} catch ( MarshalException except ) {
	    Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingResourcesError", except ) );
	    throw new IOException( except.toString() );
	} catch ( Exception except ) {
	    Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingResourcesError", except ) );
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
	        // make the marshaller
            Marshaller marshaller = new Marshaller(writer);
            // set the log
            marshaller.setLogWriter(Logger.getSystemLogger());
            // make the call
            marshaller.marshal(this);
        } catch ( MarshalException except ) {
	        throw new IOException( except.toString() );
	    } catch ( Exception except ) {
	        throw new IOException( "Nested exception: " + except );
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


    public void addDomain( Domain domain )
    {
	if ( _domains.put( domain.getName(), domain ) != null )
	    throw new IllegalArgumentException( "Multiple domains found with the same name " +
						domain.getName() );
    }


    public Enumeration listDomains()
    {
	return _domains.elements();
    }


    public String getDefault()
    {
	if ( _default == null || _default.length() == 0 )
	    _default = TransactionDomain.DefaultDomain;
	return _default;
    }


    public void setDefault( String defName )
    {
	_default = null == defName ? null : defName.trim();
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

	    if ( publicId.equals( Schema.PublicId ) ||
		 systemId.equals( Schema.SystemId ) ) {
		source = new InputSource();
		source.setByteStream( Server.class.getResourceAsStream( Schema.Resource ) );
		return source;
	    }

	    return null; 
	}

    }


}











