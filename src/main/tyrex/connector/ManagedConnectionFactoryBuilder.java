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
 */


package tyrex.connector;

import java.util.Hashtable;
import tyrex.util.WrappedException;

///////////////////////////////////////////////////////////////////////////////
// ManagedConnectionFactoryBuilder
///////////////////////////////////////////////////////////////////////////////

/**
 * This interface provides a way to build managed connection
 * factories so that managed connection factories can be deployed
 * and resolved without other components knowing how to build
 * and initialize a particular managed connection factory.
 * <p>
 * Objects that implement this interface should have a public
 * no-args constructor.
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
public interface ManagedConnectionFactoryBuilder 
{
    /**
     * Build a managed connection factory using the
     * parameters defined in the specified hashtable.
     *
     * @param env hashtable whose keys and values are strings.
     * @return the non-null managed connection factory.
     * @throws BuildException if there is a problem building the managed
     *      connection factory.
     */
    ManagedConnectionFactory build(Hashtable env)
        throws BuildException;


    /**
     * This exception is thrown if the managed connection 
     * factory cannot be built.
     */
    public final class BuildException 
        extends WrappedException
    {
        /**
         * Create a default BuildException.
         */
        public BuildException() 
        {
            super();
        }


        /**
         * Create the BuildException with the
         * specified message.
         *
         * @param msg the message
         */
        public BuildException(String msg) 
        {
            super(msg);
        }


        /**
         * Create the BuildException with the
         * specified throwable
         *
         * @param t the throwable that actually occurred.
         */
        public BuildException(Throwable t) 
        {
            super(t);
        }


        /**
         * Create the BuildException with the
         * specified message and throwable.
         *
         * @param msg the message
         * @param t the throwable that actually occurred.
         */
        public BuildException(String msg, Throwable t) 
        {
            super(msg, t);
        }
    }
}
