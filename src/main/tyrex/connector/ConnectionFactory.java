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
 */


package tyrex.connector;


import java.io.PrintWriter;
import tyrex.connector.naming.Referenceable;


/**
 *
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 * @version $Revision: 1.2 $ $Date: 2000/09/08 23:04:43 $
 */
public interface ConnectionFactory
    extends Referenceable
{

    /**
     * Returns the description of this factory.
     *
     * @return The description of this factory
     */
    public String getDescription();


    /**
     * Sets the description of this factory.
     *
     * @param description The description of this factory
     */
    public void setDescription( String description );


    /**
     * Returns the log writer for this managed connection.
     *
     * @return The log writer
     */
    public PrintWriter getLogWriter();


    /**
     * Sets the log writer for this managed connection.
     *
     * @param logWriter The log writer
     */
    public void setLogWriter( PrintWriter logWriter );

 
    /**
     * Returns a suitable connection given the
     * connection creation properties.
     *
     * @param info Optional connection creation information
     * @return connection
     * @throws ConnectionException The connection cannot be created
     */
    public Object getConnection( Object info)
        throws ConnectionException;


}




