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
 * $Id: Logger.java,v 1.17 2001/10/18 00:45:28 mohammed Exp $
 */


package tyrex.util;

import java.io.OutputStream;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Category;
import org.apache.log4j.FileAppender;

/**
 *
 * @author <a href="jdaniel@intalio.com">Jerome DANIEL</a>
 * @version $Revision: 1.17 $ $Date: 2001/10/18 00:45:28 $
 */
public class Logger
{


    public static final Category tyrex;
    
    
    public static final Category castor;


    public static final Category resource;
    
    
    public static final Category ots;


    public static final Category security;   


    static {
        tyrex = Category.getInstance( "tyrex" );

        // add an appender to tyrex if necessary
        /*if ( ! tyrex.getDefaultHierarchy().getRoot().getAllAppenders().hasMoreElements() ) {
            tyrex.addAppender( new FileAppender( new PatternLayout( "%d{dd MMM yyyy HH:mm:ss}:%c:%p %m%n" ), 
                                                   System.out ) );
        }*/

        resource = Category.getInstance( "tyrex.resource" );
        ots = Category.getInstance( "tyrex.ots" );
        security = Category.getInstance( "tyrex.security" );
        castor = Category.getInstance( "tyrex.resource.castor" );

        tyrex.getDefaultHierarchy().getRoot().removeAllAppenders();
        tyrex.addAppender( new FileAppender( new PatternLayout( "" ), new DevNull() ) );
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

