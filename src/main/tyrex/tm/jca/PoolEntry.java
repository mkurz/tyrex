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
 */


package tyrex.tm.jca;


import javax.resource.spi.ManagedConnection;
import javax.resource.spi.LocalTransaction;
import javax.transaction.xa.XAResource;
import tyrex.services.Clock;


/**
 * Represents an entry in the connection pool.
 *
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version $Revision: 1.1 $
 */
final class PoolEntry
{
    
    
    /**
     * The managed connection associated with this entry.
     */
    protected final ManagedConnection  _managed;
    
    
    /**
     * The hash code for this entry.
     */
    protected final int                _hashCode;
    
    
    /**
     * Reference to the next connection entry in hash table.
     */
    protected PoolEntry                _nextEntry;
    
    
    /**
     * True if this connection is available, false if currently in use.
     */
    protected boolean                  _available;
    
    
    /**
     * The XA resource associated with this connection. May be null.
     */
    protected final XAResource         _xaResource;
    
    
    /**
     * The local transaction associated with this connection. May be null.
     */
    protected final LocalTransaction   _localTx;
    
    
    /**
     * The timestamp for a used connection returns the clock time at which
     * the connection was made available to the application. The timestamp
     * for an unused connection returns the clock time at which the
     * connection was placed in the pool.
     */
    protected long                     _timeStamp;
    
    
    /**
     * Constructs a new pool entry. A new pool entry is not available by
     * default. The <tt>available</tt> variable must be set to false to
     * make it available.
     *
     * @param managed The managed connection
     * @param hashCode The managed connection hash code
     * @param xaResource The XA resource interface, or null
     * @param localTx The local transaction interface, or null
     */
    protected PoolEntry( ManagedConnection managed, int hashCode,
                         XAResource xaResource, LocalTransaction localTx )
    {
        if ( managed == null )
            throw new IllegalArgumentException( "Argument managed is null" );
        _managed = managed;
        _hashCode = hashCode;
        _xaResource = xaResource;
        _localTx = localTx;
        _available = false;
        _timeStamp = Clock.clock();
    }
    
    
}
