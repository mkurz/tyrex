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
 * $Id: ThreadContext.java,v 1.3 2001/03/13 03:14:59 arkin Exp $
 */


package tyrex.tm.impl;


import javax.transaction.xa.XAResource;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.transaction.Transaction;
import tyrex.tm.RuntimeContext;
import tyrex.naming.MemoryContext;
import tyrex.naming.MemoryContextFactory;
import tyrex.naming.MemoryBinding;
import tyrex.util.FastThreadLocal;


/**
 * Implementation of {@link RuntimeContext}.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2001/03/13 03:14:59 $
 */
public class ThreadContext
    extends RuntimeContext
{


    /**
     * The transaction associated with this thread, if the thread
     * is in a transaction, or null if the thread is not in a
     * transaction.
     */
    protected TransactionImpl       _tx;


    /**
     * The XA resources that have been opened before or during the
     * transaction and must be enlisted with the transaction when
     * the transaction starts. Allows null entries, but no duplicates.
     * May be null.
     */
    protected XAResource[]          _resources;


    private final Subject           _subject;


    private final MemoryBinding     _bindings;


    private static ThreadEntry[]    _table;


    /**
     * Determines the size of the hash table. This must be a prime
     * value and within range of the average number of threads we
     * want to deal with. Potential values are:
     * <pre>
     * Threads  Prime
     * -------  -----
     *   256     239
     *   512     521
     *   1024    1103
     *   2048    2333
     *   4096    4049
     * </pre>
     */
    private static final int TABLE_SIZE = 1103;


    public ThreadContext( Subject subject )
    {
        _bindings = new MemoryBinding();
        _subject = subject;
    }


    public ThreadContext( Context context, Subject subject )
        throws NamingException
    {
        if ( context == null )
            _bindings = new MemoryBinding();
        else {
            if ( ! ( context instanceof MemoryContext ) )
                throw new NamingException( "The context was not created from " +
                                           MemoryContextFactory.class.getName() );
            _bindings = ( (MemoryContext) context ).getBindings();
            if ( ! _bindings.isRoot() )
                throw new NamingException( "The context is not a root context" );
        }
        _subject = subject;
    }


    static {
        _table = new ThreadEntry[ TABLE_SIZE ];
    }


    public static ThreadContext getThreadContext()
    {
        ThreadEntry   entry;
        ThreadContext context;
        Thread        thread;
        int           index;
        
        thread = Thread.currentThread();
        index = ( thread.hashCode() & 0x7FFFFFFF ) % _table.length;
        // Lookup the first entry that maps to the has code and
        // continue iterating to the last entry until a matching entry
        // is found. Even if the current entry is removed, we expect
        // entry._next to point to the next entry.
        entry = _table[ index ];
        while ( entry != null ) {
            if ( entry._thread != thread )
                return entry._context;
            entry = entry._nextEntry;
        }
        synchronized ( _table ) {
            context = new ThreadContext( null );
            entry = new ThreadEntry( context, thread, null );
            entry._nextEntry = _table[ index ];
            _table[ index ] = entry;
        }
        return context;
    }


    public static void setThreadContext( ThreadContext context )
    {
        Thread      thread;
        ThreadEntry entry;
        ThreadEntry next;
        int         index;

        if ( context == null )
            throw new IllegalArgumentException( "Argument context is null" );
        synchronized ( _table ) {
            thread = Thread.currentThread();
            index = ( thread.hashCode() & 0x7FFFFFFF ) % _table.length;
            
            entry = _table[ index ];
            if ( entry != null && entry._thread == thread ) {
                entry = new ThreadEntry( context, thread, entry );
                _table[ index ] = entry;
                return;
            }
            next = entry._nextEntry;
            while ( next != null ) {
                if ( next._thread == thread ) {
                    next = new ThreadEntry( context, thread, next );
                    entry._nextEntry = next;
                    return;
                }
                entry = next;
                next = next._nextEntry;
            }
            
            entry = new ThreadEntry( context, thread, null );
            entry._nextEntry = _table[ index ];
            _table[ index ] = entry;
        }
    }


    public static ThreadContext unsetThreadContext()
    {
        Thread      thread;
        ThreadEntry entry;
        ThreadEntry next;
        ThreadEntry previous;
        int         index;

        synchronized ( _table ) {
            thread = Thread.currentThread();
            index = ( thread.hashCode() & 0x7FFFFFFF ) % _table.length;

            entry = _table[ index ];
            if ( entry != null && entry._thread == thread ) {
                previous = entry._previous;
                if ( previous == null )
                    _table[ index ] = entry._nextEntry;
                else {
                    previous._nextEntry = entry._nextEntry;
                    _table[ index ] = previous;
                }
                return entry._context;
            }
            next = entry._nextEntry;
            while ( next != null ) {
                if ( next._thread == thread ) {
                    previous = next._previous;
                    if ( previous == null )
                        entry._nextEntry = next._nextEntry;
                    else {
                        previous._nextEntry = next._nextEntry;
                        entry._nextEntry = previous;
                    }
                    return next._context;
                }
                entry = next;
                next = next._nextEntry;
            }
        }
        return null;
    }


    public static void cleanup( Thread thread )
    {
        ThreadEntry entry;
        ThreadEntry next;
        int         index;

        if ( thread == null )
            throw new IllegalArgumentException( "Argument thread is null" );

        synchronized ( _table ) {
            index = ( thread.hashCode() & 0x7FFFFFFF ) % _table.length;
            entry = _table[ index ];
            if ( entry == null )
                return;
            if ( entry._thread == thread ) {
                _table[ index ] = entry._nextEntry;
                return;
            }
            next = entry._nextEntry;
            while ( next != null ) {
                if ( next._thread == thread ) {
                    entry._nextEntry = next._nextEntry;
                    return;
                }
                entry = next;
                next = next._nextEntry;
            }
        }
    }


    public Context getEnvContext()
    {
        return _bindings.getContext();
    }


    public Transaction getTransaction()
    {
        return _tx;
    }


    public Subject getSubject()
    {
        return _subject;
    }


    public void cleanup()
    {
        _tx = null;
        _resources = null;
    }


    public MemoryBinding getMemoryBinding()
    {
        return _bindings;
    }


    /**
     * Adds an XA resource to the association list.
     */
    protected void add( XAResource xaResource )
    {
        XAResource[] newResources;
        int          next = -1;
        
        if ( xaResource == null )
            throw new IllegalArgumentException( "Argument xaResource is null" );
        if ( _resources == null )
            _resources = new XAResource[] { xaResource };
        else {
            // Prevent duplicity.
            for ( int i = _resources.length ; i-- > 0 ; ) {
                if ( _resources[ i ] == xaResource )
                    return;
                else if ( _resources[ i ] == null )
                    next = i;
            }
            if ( next >= 0 )
                _resources[ next ] = xaResource;
            else {
                newResources = new XAResource[ _resources.length * 2 ];
                for ( int i = _resources.length ; i-- > 0 ; )
                    newResources[ i ] = _resources[ i ];
                newResources[ _resources.length ] = xaResource;
                _resources = newResources;
            }
        }
    }
    
    
    /**
     * Removes an XA resource from the associated list.
     *
     * @param xaResource the XA resource
     * @return True if removed
     */
    protected boolean remove( XAResource xaResource )
    {
        if ( _resources != null ) {
            for ( int i = _resources.length ; i-- > 0 ; )
                if ( _resources[ i ] == xaResource ) {
                    _resources[ i ] = null;
                    return true;
                }
        }
        return false;
    }


    /**
     * Returns all the XA resources, or null if no resources
     * are enlisted.
     *
     * @return All XA resources, or null
     */
    protected XAResource[] getResources()
    {
        int           count = 0;
        XAResource[]  resources;

        if ( _resources == null )
            return null;
        if ( _resources != null ) {
            for ( int i = _resources.length ; i-- > 0 ; )
                if ( _resources[ i ] != null )
                    ++count;
        }
        if ( count == 0 )
            return null;

        resources = new XAResource[ count ];
        count = 0;
        for ( int i = _resources.length ; i-- > 0 ; )
            if ( _resources[ i ] != null )
                resources[ count++ ] = _resources[ i ];
        return resources;
    }


    /**
     * Each entry in the table has a key (thread), a value or null
     * (we don't remove on null) and a reference to the next entry in
     * the same table position.
     */
    static private final class ThreadEntry
    {


        /**
         * The thread with which this entry is associated.
         */
        final Thread  _thread;
        

        /**
         * The current thread context.
         */
        ThreadContext _context;
        

        /**
         * The previous thread entry (single-linked stack)
         * associated with this thead.
         */
        ThreadEntry   _previous;

        
        /**
         * The next thread entry in this bucket.
         */
        ThreadEntry   _nextEntry;


        ThreadEntry( ThreadContext context, Thread thread, ThreadEntry previous )
        {
            _context = context;
            _thread = thread;
            if ( previous != null ) {
                _previous = previous;
                _nextEntry = previous._nextEntry;
            }
        }


    }


}