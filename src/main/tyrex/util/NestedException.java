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
 * Copyright 2000, 2001 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: NestedException.java,v 1.1 2001/03/02 23:07:49 arkin Exp $
 */


package tyrex.util;


import java.io.PrintStream;
import java.io.PrintWriter;
import org.xml.sax.SAXException;
import java.sql.SQLException;



/**
 * Base type for all Type exceptions.
 * <p>
 * This exception can optionally wrap another exception. The printed stack trace
 * will be that of the wrapped exception, if one is provided in the constructor.
 * The underlying exception can be obtained from {@link #getException getException}.
 * <p>
 * Several exceptions support wrapping of an underlying exception by extending
 * from this class. When another nested exception is provided in the constructor,
 * the underlying exception will be used, so it's safe to construct a nested
 * exception from another nested exception.
 * <p>
 * Support for unwrapping the underlying exceptions include {@link NestedException},
 * {@link org.xml.sax.SAXException}.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $
 */
public abstract class NestedException
    extends Exception
{


    /**
     * The underlying exception.
     */
    private final Exception   _except;


    /**
     * Construct a new nested exception wrapping an underlying exception
     * and providing a message.
     *
     * @param message The exception message
     * @param except The underlying exception
     */
    public NestedException( String message, Exception except )
    {
        super( message == null ? "No message available" : message );
        if ( except instanceof NestedException &&
             ( (NestedException) except )._except != null )
            _except = ( (NestedException) except )._except;
        else if ( except instanceof SAXException &&
             ( (SAXException) except ).getException() != null )
            _except = ( (SAXException) except ).getException();
        else
            _except = except;
    }


    /**
     * Construct a new nested with a message.
     *
     * @param message The exception message
     */
    public NestedException( String message )
    {
        super( message == null ? "No message available" : message );
        _except = null;
    }


    /**
     * Construct a new nested exception wrapping an underlying exception.
     *
     * @param except The underlying exception
     */
    public NestedException( Exception except )
    {
        this( except == null ? null : except.getMessage(), except );
    }


    /**
     * Returns the underlying exception, if this exception wraps another exception.
     *
     * @return The underlying exception, or null
     */
    public Exception getException()
    {
        return _except;
    }


    public void printStackTrace()
    {
        if ( _except == null )
            super.printStackTrace();
        else
            _except.printStackTrace();
    }


    public void printStackTrace( PrintStream stream )
    {
        if ( _except == null )
            super.printStackTrace( stream );
        else
            _except.printStackTrace( stream );
    }


    public void printStackTrace( PrintWriter writer )
    {
        if ( _except == null )
            super.printStackTrace( writer );
        else
            _except.printStackTrace( writer );
    }


}


