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
 * $Id: ResourceHolder.java,v 1.1 2000/04/10 20:52:34 arkin Exp $
 */


package tyrex.resource.manager;


import tyrex.util.FastThreadLocal;
import tyrex.resource.Connection;
import tyrex.resource.ConnectionManager;
import tyrex.resource.SynchronizationResource;
import javax.transaction.xa.XAResource;


/**
 * Identifies resources associated held by a thread. Each thread must
 * have exactly one of these objects associated with it, listing the
 * resources created by this thread and their factories.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/04/10 20:52:34 $
 */
public final class ResourceHolder
{


    /**
     * Holds association between resource holders and threads.
     */
    private static final FastThreadLocal  _local = new FastThreadLocal();


    private HolderEntry[]  _entries;


    /**
     * Returns the current resource holder for this thread.
     *
     * @return The current resource holder
     */
    public static ResourceHolder getCurrentHolder()
    {
        ResourceHolder holder;

        holder = (ResourceHolder) _local.get();
        if ( holder == null ) {
            holder = new ResourceHolder();
            _local.set( holder );
        }
        return holder;
    }


    /**
     * Private constructor.
     */
    private ResourceHolder()
    {
    }


    public Connection getExisting( ConnectionManager manager )
    {
        if ( _entries != null ) {
            for ( int i = 0 ; i < _entries.length ; ++i )
                if ( manager.equals( _entries[ i ].manager ) )
                    return manager.connection;
        }
        return null;
    }


    public void addConnection( ConnectionManager manager, Connection connection,
                               XAResource xa, SynchronizationResource sync )
        throws ConnectionException
    {
        HolderEntry entry;

        entry = new HodlerEntry( manager, connection, xa, sync );
    }


    static class HolderEntry
    {

        final ConnectionManager       manager;

        final Connection              connection;

        final XAResource              xa;

        final SynchronizationResource sync;

        HolderEntry( ConnectionManager manager, Connection connection,
                     XAResource xa, SynchronizationResource sync )
        {
            this.manager = manager;
            this.connection = connection;
            this.xa = xa;
            this.sync = sync;
        }

    }


}




