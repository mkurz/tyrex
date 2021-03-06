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
 * $Id: WrappedException.java,v 1.2 2000/09/08 23:06:38 mohammed Exp $
 */


package tyrex.util;


import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * Reports an exception with an underlying exception.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/09/08 23:06:38 $
 */
public class WrappedException
    extends Exception
{


    private Throwable  _except;


    /**
     * Constructs a new exception.
     * 
     */
    public WrappedException( )
    {
        super( );
    }

    
    /**
     * Constructs a new exception with the specified message.
     * 
     * @param message The exception message
     */
    public WrappedException( String message )
    {
        super( message );
    }


    /**
     * Constructs a new exception with the specified message and
     * trigger exception.
     * 
     * @param message The exception message
     * @param except The exception that triggered this exception
     */
    public WrappedException( String message, Throwable except )
    {
        super( message );
        _except = except;
    }


    /**
     * Constructs a new exception with the specified trigger exception.
     * 
     * @param except The exception that triggered this exception
     */
    public WrappedException( Throwable except )
    {
        super( except.toString() );
        _except = except;
    }


    /**
     * Return the exception that triggered this exception.
     * May be null.
     */
    public final Throwable getException()
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
    
    
    public void printStackTrace( PrintStream print )
    {
        if ( _except == null )
            super.printStackTrace( print );
        else
            _except.printStackTrace( print );
    }
    
    
    public void printStackTrace( PrintWriter print )
    {
        if ( _except == null )
            super.printStackTrace( print );
        else
            _except.printStackTrace( print );
    }
    

}





