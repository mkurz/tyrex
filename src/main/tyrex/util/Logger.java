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
 * $Id: Logger.java,v 1.8 2001/02/23 19:23:13 jdaniel Exp $
 */


package tyrex.util;

import java.io.OutputStream;
import java.util.Properties;
import java.util.Enumeration;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Category;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;

/**
 * Simple logging facility. This logger extends <tt>PrintWriter</tt>
 * which is used to trace SQL statements, Castor operations and
 * mapping resolutions.
 * <p>
 * This logger augments <tt>PrintWriter</tt> by adding a prefix to
 * each printed line and optionally a time stamp, enabling easy
 * post-mortem analysis.
 *
 * @author <a href="jdaniel@intalio.com">Jerome DANIEL</a>
 * @version $Revision: 1.8 $ $Date: 2001/02/23 19:23:13 $
 */
public class Logger
{
    public static final Category conf;
    
    public static final Category ots;
    
    public static final Category jdbc;
    
    public static final Category recovery;
    
    public static final Category server;

    public static final Category tm;
    
    public static final Category tools;
    
    public static final Category util;
    
    public static final Appender appender;

    static {
        PatternLayout layout;
        Category      category;
        Properties    props;
        String        name;
        Enumeration   enum;
        FileAppender  nullAppender;        
        
        layout = new PatternLayout( "%d{dd MMM yyyy HH:mm:ss}:%c:%p %m%n" );
        
        nullAppender = new FileAppender( new PatternLayout(""), new DevNull() );

        ots = Category.getInstance( "tyrex.ots" );        
        conf = Category.getInstance( "tyrex.conf" );
        jdbc = Category.getInstance( "tyrex.jdbc" );
        recovery = Category.getInstance( "tyrex.recovery" );
        server = Category.getInstance( "tyrex.server" );
        tm = Category.getInstance( "tyrex.tm" );
        tools = Category.getInstance( "tyrex.tools" );
        util = Category.getInstance( "tyrex.util" );
        
         if ( !tyrex.tm.Tyrex.log() )
         {        
            appender = nullAppender;
            
            ots.addAppender( nullAppender );
            conf.addAppender( nullAppender );
            jdbc.addAppender( nullAppender );
            recovery.addAppender( nullAppender );
            server.addAppender( nullAppender );
            tm.addAppender( nullAppender );
            tools.addAppender( nullAppender );
            util.addAppender( nullAppender );
         }
        else
        {
            appender = new FileAppender( layout, System.out );
            
            ots.addAppender( appender );
            conf.addAppender( appender );
            jdbc.addAppender( appender );
            recovery.addAppender( appender );
            server.addAppender( appender );
            tm.addAppender( appender );
            tools.addAppender( appender );
            util.addAppender( appender );
        }
    }


    static private class DevNull
        extends OutputStream
    {

        public void close()
        {
        }

        public void flush()
        {
        }

        public void write( int value )
        {
        }

        public void write( byte[] bytes )
        {
        }

        public void write( byte[] bytes, int start, int length )
        {
        }

    }

}

