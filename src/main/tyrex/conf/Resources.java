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
 * $Id: Resources.java,v 1.5 2000/09/08 23:18:51 mohammed Exp $
 */


package tyrex.conf;


import java.util.Vector;
import java.util.Enumeration;
import java.io.Serializable;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.MarshalException;
import tyrex.util.Messages;
import tyrex.util.Logger;


/**
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $ $Date: 2000/09/08 23:18:51 $
 */
public class Resources
    implements Serializable
{


    public static class DTD
    {

	/**
	 * The public identifier of the DTD.
	 * <pre>
	 * -//EXOLAB/Tyrex Resources DTD Version 1.0//EN
	 * </pre>
	 */
	public static final String PublicId = 
	    "-//EXOLAB/Tyrex Resources DTD Version 1.0//EN";

	/**
	 * The system identifier of DTD.
	 * <pre>
	 * http://tyrex.exolab.org/resources.dtd
	 * </pre>
	 */
	public static final String SystemId =
	    "http://tyrex.exolab.org/resources.dtd";

	/**
	 * The resource named of the DTD:
	 * <tt>/tyrex/conf/resources.dtd</tt>.
	 */
	private static final String Resource =
	    "/tyrex/conf/resources.dtd";

    }


    public static class Namespace
    {

	/**
	 * The public identifier of the XML schema.
	 * <pre>
	 * -//EXOLAB/Tyrex Resources Schema Version 1.0//EN
	 * </pre>
	 */
	public static final String PublicId =
	    "-//EXOLAB/Tyrex Resources Schema Version 1.0//EN";

	/**
	 * The system identifier of the XML schema.
	 * <pre>
	 * http://tyrex.exolab.org/resources.xsd
	 * </pre>
	 */
	public static final String SystemId =
	    "http://tyrex.exolab.org/resources.xsd";

	/**
	 * The namespace prefix: <tt>resources</tt>
	 */
	public static final String prefix =
	    "resources";

	/**
	 * The namespace URI:
	 * <pre>
	 * http://tyrex.exolab.org/resources
	 * </pre>
	 */
	public static final String URI =
	    "http://tyrex.exolab.org/resources";
	
	/**
	 * The resource named of the XML schema:
	 * <tt>/tyrex/conf/resources.xsd</tt>.
	 */
	private static final String Resource =
	    "/tyrex/conf/resources.xsd";
	
    }


    /**
     * The name of the default resource file:
     * <tt>resources.xml</tt>.
     */
    public static String FileName = "resources.xml";


    /**
     * List of all the resource definitions. Each element is of type
     * {@link Resource}. To get the actual resource factory for an
     * element use {@link Resource#getFactory}.
     */
    private Vector  _resList = new Vector();


    /**
     * List of environment entries that override those specified by
     * the application. Each element is of type {@link EnvEntry}.
     */
    private Vector  _envEntries = new Vector();


    /**
     * The configuration file from which this resources were loaded.
     * If the file is known, changes to the object model can be
     * saved back to the file by calling {@link #save}.
     */
    private transient File    _source;


    /**
     * If true will emit more information about problems reading/writing
     * the XML configuration file.
     */
    public static boolean debug;


    /**
     * The default resources returned from {@link #getResources}.
     * These are loaded on demand, so at least one call is required
     * to get them loaded.
     */
    private static Resources  _resources;


    /**
     * Returns the default resources configuration. These resources
     * are loaded from the default configuration file. If an error
     * occured reading the configuration file is inaccessible, an empty
     * resources object will be returned. Multiple calls will return
     * the same resources object.
     */
    public static synchronized Resources getResources()
    {
	if ( _resources == null ) {
	    reloadResources();
	}
	return _resources;
    }


    /**
     * Called to reload the default resources. These resources are
     * loaded from the default configuration file. If an error occured
     * reading the configuration file, the previous resources object
     * will be returned.
     */
    public static synchronized void reloadResources()
    {
	try {
	    _resources = load();
	} catch ( IOException except ) {
	    // Error is reported by load method, but a resource
	    // must always exist. A new one is created, an existing
	    // one is reused.
	    if ( _resources == null ) {
		_resources = new Resources();
	    }
	}
    }


    /**
     * Loades the resources list from the default resources file. The
     * file <tt>resources.xml</tt> is loaded from the current
     * directory and if no such file is found in the current directory
     * from the Java home library directory
     * (<tt>JAVA_HOME/lib/resources.xml</tt>). The file can be saved
     * back to reflect changes to the resources list by calling {@link
     * #save}.
     *
     * @return The resources list
     * @throws IOException The default file could not be found
     */
    public static Resources load()
	throws IOException
    {
	File file;
	URL  url;

	try {
	    url = Server.class.getResource( "/" + FileName );
	    if ( url != null ) {
		file = new File( url.getFile() );
		if ( file.exists() ) {
		    Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingResources", file ) );
		    return load( file );
		}
	    }
	    file = new File( System.getProperty( "java.home" ), "lib" );
	    file = new File( file, FileName );
	    if ( file.exists() ) {
		Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingResources", file ) );
		return load( file );
	    }
	} catch ( IOException except ) {
	    Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingResourcesError", except ) );
	    throw except;
	}
	Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingResourcesMissing", FileName ) );
	throw new IOException( Messages.format( "tyrex.conf.loadingResourcesMissing", FileName ) );
    }


    /**
     * Loades the resources list from specified resources file. The
     * file can be saved back to reflect changes to the resources list
     * by calling {@link #save}.
     *
     * @param file The resources configuration file
     * @return The resources list
     * @throws IOException The file could not be found
     */
    public static Resources load( File file )
	throws IOException
    {
	Resources resources;

	resources = load( new FileReader( file ) );
	resources._source = file;
	return resources;
    }


    /**
     * Loades the resources list from specified resources file. The
     * file can be saved back to reflect changes to the resources list
     * by calling {@link #save}.
     *
     * @param file The resources configuration file
     * @return The resources list
     * @throws IOException The file could not be found
     */
    public static Resources load( Reader reader )
	throws IOException
    {
	Unmarshaller unmarshaller;

	try {
	    unmarshaller = new Unmarshaller( Resources.class );
	    if ( debug )
		unmarshaller.setLogWriter( Logger.getSystemLogger() );
	    unmarshaller.setEntityResolver( new SchemaEntityResolver() );
	    return (Resources) unmarshaller.unmarshal( reader );
	} catch ( MarshalException except ) {
	    Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingResourcesError", except ) );
	    throw new IOException( except.toString() );
	} catch ( Exception except ) {
	    Logger.getSystemLogger().println( Messages.format( "tyrex.conf.loadingResourcesError", except ) );
	    throw new IOException( "Nested exception: " + except.toString() );
	}
    }


    /**
     * Saved this resource list to the specified writer as a resource
     * configuration file.
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
            if ( debug )
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
     * Saves this resources list to the resource configuration  file
     * from which it was loaded. This resources list must have been
     * loaded with a call to either {@link #load()} or {@link
     * #load(File)}. Throws an {@link IOException} if this
     * configuration was not loaded from a specified or default file.
     *
     * @throws IOException This resources list was not loaded from a
     *   specified or default file, or an I/O error occured reading it
     */
    public void save()
	throws IOException
    {
	if ( _source == null )
	    throw new IOException( Messages.message( "tyrex.conf.savingResourcesNotLoaded" ) );
	save( new FileWriter( _source ) );
    }


    /**
     * Adds a resource definition to this list.
     *
     * @param res The resource definition
     */
    public void addResource( Resource res )
    {
	if ( ! _resList.contains( res ) )
	    _resList.addElement( res );
    }


    /**
     * Returns an enumeration of all the resource definitions in this
     * list. Each element is of type {@link Resource}.
     */
    public Enumeration listResources()
    {
	return _resList.elements();
    }


    /**
     * Adds an environment entry to this list.
     *
     * @param entry The environment entry
     */
    public void addEnvEntry( EnvEntry entry )
    {
	if ( ! _envEntries.contains( entry ) )
	    _envEntries.addElement( entry );
    }


    /**
     * Returns an enumeration of all the environment entries in this
     * list. Each element is of type {@link EnvEntry}.
     */
    public Enumeration listEnvEntries()
    {
	return _envEntries.elements();
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
