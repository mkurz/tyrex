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

///////////////////////////////////////////////////////////////////////////////
// ConnectionEventListenerAdapter
///////////////////////////////////////////////////////////////////////////////

/**
 * This class has empty stubs for the methods in
 * {@link ConnectionEventListener}
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
public abstract class ConnectionEventListenerAdapter 
    implements ConnectionEventListener
{
    /**
     * Default constructor
     */
    public ConnectionEventListenerAdapter()
    {

    }


    /**
     * Called by a managed connection to inform the listener
     * that the application closed the connection. After this call the
     * listener, like the connection manager may recycle the connection 
     * and hand it to a different caller.
     *
     * @param event The event
     */
    public void connectionClosed(ConnectionEvent event)
    {

    }


    /**
     * Called by a managed connection to inform the listeners
     * that a critical error occured with the connection. After this
     * call a listeners like a connection manager will not attempt to use the
     * connection and will properly discard it.
     *
     * @param event The event that may contain the exception that occurred.
     */
    public void connectionErrorOccurred(ConnectionEvent event)
    {

    }


    /**
     * Called by the managed connection to inform a listener
     * that a local transaction has begun for the managed connection
     *
     * @param event The event
     */
    public void localTransactionBegun(ConnectionEvent event)
    {

    }


    /**
     * Called by the managed connection to inform a listener
     * that a local transaction has been committed for the managed connection
     *
     * @param event The event
     */
    public void localTransactionCommitted(ConnectionEvent event)
    {

    }
    
    
    /**
     * Called by the managed connection to inform a listener
     * that a local transaction has been rolled back for the managed connection
     *
     * @param event The event
     */
    public void localTransactionRolledback(ConnectionEvent event)
    {

    }

}
