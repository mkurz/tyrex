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
 * Copyright 1999-2001 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: Configuration.java,v 1.8 2001/09/24 18:28:53 mohammed Exp $
 */


package tyrex.util;


import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Random;
import java.security.SecureRandom;


/**
 * Provides basic configuration for Tyrex components based on the
 * <tt>tyrex.config</tt> configuration file. Several Tyrex services
 * rely on this configuration file.
 *
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.8 $ $Date: 2001/09/24 18:28:53 $
 */
public final class Configuration
{


    /**
     * Property specifying whether to run in verbose mode.
     * <tt>tyrex.log.verbose</tt>
     */
    public static final String PROPERTY_LOG_VERBOSE = "tyrex.log.verbose";


    /**
     * Property specifying whether to enable console logging.
     * <tt>tyrex.log.console</tt>
     */
    public static final String PROPERTY_LOG_CONSOLE = "tyrex.log.console";


    /**
     * Property that determines the number of clock ticks for each
     * unsynchronized cycle. The value is an integer, the percision is
     * milliseconds. The name of this property is <tt>tyrex.clock.unsynchicks</tt>.
     */
    public static final String PROPERTY_UNSYNCH_TICKS = "tyrex.clock.unsynchTicks";


    /**
     * Property that determines the number of unsynchronized cycles
     * before the clock is synchronized. The value is an integer.
     * The name of this property is <tt>tyrex.clock.synchEvery</tt>.
     */
    public static final String PROPERTY_SYNCH_EVERY = "tyrex.clock.synchEvery";


    /**
     * Property that determines whether to use secure or standard random
     * number generator. This value is true or false. The name of this
     * property is <tt>tyrex.random.secure</tt>.
     */
    public static final String PROPERTY_SECURE_RANDOM = "tyrex.random.secure";


    /**
     * Property that specifies the name of the UUID state file.
     * The UUID state file is used to store the node identifier
     * and clock sequence. The name of this property is
     * <tt>tyrex.uuid.stateFile</tt>.
     */
    public static final String PROPERTY_UUID_STATE_FILE = "tyrex.uuid.stateFile";


    /**
     * Property that specified the name of domain configuration file(s)
     * to load at startup. This value is a comma separated list of zero
     * or more file names. The name of this property is <tt>tyrex.domain.files</tt>.
     */
    public static final String PROPERTY_DOMAIN_FILES = "tyrex.domain.files";


    /**
     * The vendor name. This variable is read from the configuration file.
     */
    public static final String  VENDOR_NAME;


    /**
     * The vendor URL. This variable is read from the configuration file.
     */
    public static final String  VENDOR_URL;


    /**
     * The version number. This variable is read from the configuration file.
     */
    public static final String  VERSION;


    /**
     * The product title. This variable is read from the configuration file.
     */
    public static final String  TITLE;


    /**
     * The copyright message. This variable is read from the configuration file.
     */
    public static final String  COPYRIGHT;


    /**
     * The name of the server configuration file (<tt>tyrex.config</tt>).
     */
    public static final String FILE_NAME = "tyrex.config";


    /**
     * The system property that specifies the configuration file name. If this
     * system property is specified, it will be used to load the configuration
     * file. Otherwise, the configuration file {@link #FILE_NAME} will be
     * looked for in the classpath.
     */
    public static final String CONFIG_SYSTEM_PROPERTY = "tyrex.config.file";


    /**
     * The name of the default configuration file as a resource.
     */
    private static final String RESOURCE_NAME = "/tyrex/tyrex.config";


    /**
     * The default properties loaded from the configuration file.
     */
    private static final Properties _default;


    /**
     * Public member variable that determines whether Tyrex should emit
     * verbose information messages, which can be used for troubleshooting
     * purposes. This variable is set from the configuration file.
     */
    public static final boolean     verbose;


    /**
     * Public member variable that determines whether Tyrex should emit
     * information messages to the console. This variable is set from the
     * configuration file.
     */
    public static final boolean     console;


    /**
     * The random number generator. This variable is set on-demand.
     */
    private static Random        _random;


    /**
     * Returns a property from the default configuration file.
     *
     * @param name The property name
     * @param default The property's default value
     * @return The property's value
     */
    public static String getProperty( String name, String defValue )
    {
        return _default.getProperty( name, defValue );
    }


    public static String getProperty( String name )
    {
        return _default.getProperty( name );
    }


    public static Properties getProperties()
    {
        return _default;
    }


    public static boolean getBoolean( String name )
    {
        String value;

        value = _default.getProperty( name );
        if ( value == null )
            return false;
        return value.equals( "true" ) || value.equals( "on" );
    }


    public static int getInteger( String name )
    {
        String value;

        value = _default.getProperty( name );
        if ( value != null ) {
            try {
                return Integer.parseInt( value );
            } catch ( NumberFormatException except ) { }
        }
        return -1;
    }


    /**
     * Returns a random number generator. Depending on the configuration this is
     * either a secure random number generator, or a standard random number generator
     * seeded with the system clock.
     *
     * @return A random number generator
     */
    public static synchronized Random getRandom()
    {
        if ( _random == null ) {
            if ( getBoolean( PROPERTY_SECURE_RANDOM ) ) {
                if ( Configuration.verbose )
                    Logger.tyrex.info( Messages.message( "tyrex.util.randomSecure" ) );
                _random = new SecureRandom();
            } else {
                if ( Configuration.verbose )
                    Logger.tyrex.info( Messages.message( "tyrex.util.randomStandard" ) );
                _random = new Random( System.currentTimeMillis() + Runtime.getRuntime().freeMemory() );
            }
        }
        return _random;
    }


    static {
        InputStream     is;
        Properties      properties;
        String          fileName;

        properties = new Properties( System.getProperties() );
        // Get detault configuration from the Tyrex JAR. Complain if not found.
        try {
            properties.load( Configuration.class.getResourceAsStream( RESOURCE_NAME ) );
        } catch ( Exception except ) {
            // This should never happen
            System.err.println( Messages.format( "tyrex.util.noDefaultConfigurationFile", RESOURCE_NAME ) );
        }

        // Make sure the version information is read from the Tyrex JAR.
        VENDOR_NAME = properties.getProperty( "version.vendorName" );
        VENDOR_URL = properties.getProperty( "version.vendorUrl" );
        VERSION = properties.getProperty( "version.number" );
        TITLE = properties.getProperty( "version.title" );
        COPYRIGHT = properties.getProperty( "version.copyright" );
        System.err.println( Messages.format( "tyrex.util.startingTyrex", TITLE, VERSION ) );
        System.err.println( COPYRIGHT );
        System.err.println();

        // Get overriding configuration. Use the system property if set,
        // the classpath otherwise.
        fileName = System.getProperty( CONFIG_SYSTEM_PROPERTY );
        try {
            if ( fileName  != null )
                is = new FileInputStream( fileName );
            else {
                fileName = FILE_NAME;
                is = Configuration.class.getResourceAsStream( "/" + fileName );
            }
            if ( is != null ) {
                properties = new Properties( properties );
                properties.load( is );
            } else
                fileName = null;
        } catch ( Exception except ) {
            System.err.println( Messages.format( "tyrex.util.noConfigurationFile", fileName ) );
            fileName = null;
        }
        _default = properties;

        // Make sure logging is set up properly before proceeding.
        verbose = getBoolean( PROPERTY_LOG_VERBOSE );
        console = getBoolean( PROPERTY_LOG_CONSOLE );

        if ( fileName != null ) {
            System.err.println( Messages.format( "tyrex.util.loadedConfigurationFile", fileName ) );
            System.err.println();
        }
    }


}
