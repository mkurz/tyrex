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
 * $Id: ConnectionContext.java,v 1.2 2000/09/08 23:04:44 mohammed Exp $
 */


package tyrex.connector.manager;


import java.util.Enumeration;
import java.util.NoSuchElementException;
import tyrex.util.FastThreadLocal;
import tyrex.connector.ManagedConnection;
import tyrex.connector.ConnectionManager;
import tyrex.connector.SynchronizationResource;
import javax.transaction.xa.XAResource;


/**
 * Identifies resources associated held by a thread. Each thread must
 * have exactly one of these objects associated with it, listing the
 * resources created by this thread and their factories.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/09/08 23:04:44 $
 */
public class ConnectionContext
{


    /**
     * Holds association between resource holders and threads.
     */
    private static final FastThreadLocal  _local = new FastThreadLocal();


    private ContextEntry[]  _entries;


    public Enumeration listConnections( ConnectionManager manager )
    {
        return new ConnectionEnumeration( manager, _entries );
    }


    public static ConnectionContext getCurrent()
    {
        ConnectionContext ctx;

        ctx = (ConnectionContext) _local.get();
        return ctx;
    }


    public static ConnectionContext createCurrent()
    {
        ConnectionContext ctx;

        ctx = new ConnectionContext();
        _local.set( ctx );
        return ctx;
    }

    /**
     * Add a new context entry containing the specified connection manager
     * and managed connection.
     *
     * @param manager the connection manager
     * @param managed the managed connection
     */
    public void add( ConnectionManager manager, ManagedConnection managed )
    {
        // check for duplicates
        if (null != _entries) {
            // the current entry
            ContextEntry entry = null;
            for (int i = _entries.length; --i >= 0;) {
                // get the current entry
                entry = _entries[i];
                if ((null != entry) && (manager == entry.manager) && (managed == entry.managed)) {
                    // duplicate found
                    return;    
                }
            }
        }
    }

    /**
     * Remove the context entry containing the specified connection manager
     * and managed connection. If the conect entry does not exist then nothing
     * is done.
     *
     * @param manager the connection manager
     * @param managed the managed connection
     */
    public void removeContextEntry( ConnectionManager manager, ManagedConnection managed )
    {

    }


    static class ContextEntry
    {

        final ConnectionManager  manager;

        final ManagedConnection  managed;


        ContextEntry( ConnectionManager manager, ManagedConnection managed )
        {
            this.manager = manager;
            this.managed = managed;
        }

    }


    static class ConnectionEnumeration
        implements Enumeration
    {

        private final ConnectionManager  _manager;

        private final ContextEntry[]     _entries;

        private int                      _index;

        ConnectionEnumeration( ConnectionManager manager, ContextEntry[] entries )
        {
            _manager = manager;
            if ( entries == null ) {
                _entries = new ContextEntry[ 0 ];
                _index = 0;
            } else {
                _entries = (ContextEntry[]) entries.clone();
                while ( _index < _entries.length ) {
                    if ( _entries[ _index ].manager == _manager )
                        break;
                }
            }
        }
       
        public boolean hasMoreElements()
        {
            return ( _index < _entries.length );
        }

        public Object nextElement()
        {
            ManagedConnection managed;

            if ( _index == _entries.length )
                throw new NoSuchElementException();
            managed = _entries[ _index ].managed;
            ++ _index;
            while ( _index < _entries.length ) {
                if ( _entries[ _index ].manager == _manager )
                    break;
            }
            return managed;
        }

    }


}




