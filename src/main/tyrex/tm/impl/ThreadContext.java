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
 * Copyright 2000,2001 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: ThreadContext.java,v 1.1 2001/02/27 00:37:52 arkin Exp $
 */


package tyrex.tm.impl;


import javax.transaction.xa.XAResource;
import tyrex.util.FastThreadLocal;


/**
 * A thread context is used to associate the thread with a transaction
 * and various resources across method invocations.
 * <p>
 * The thread context is used across method invocations to remember
 * the resources opened within that particular context. During its
 * life time, the context may be associated with multiple threads,
 * but may only be associated with a single thread at any one time.
 * <p>
 * The transaction manager automatically creates a thread context
 * for the current thread to remember which transaction is used
 * by that thread.
 * <p>
 * An application server may create thread contexts and associate
 * them with the current thread directly. The thread context follows
 * a stack pattern that allows contexts to be pushed/popped across
 * method invocations occuring in the same thread.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2001/02/27 00:37:52 $
 */
public class ThreadContext
{
	

    /**
     * Holds associations between threads and resources.
     * Each entry is of type {@link ThreadResources}.
     */
    private static FastThreadLocal  _local = new FastThreadLocal();


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


    /**
     * Reference to the previous context in the stack.
     */
    private ThreadContext           _previous;


    /**
     * True if this context is associated with any thread.
     */
    private boolean                 _inThread;


    /**
     * Returns the thread context associated with the current thread.
     *
     * @return The thread context associated with the current thread
     */
    public static ThreadContext getThreadContext()
    {
	ThreadContext context;
        
	context = (ThreadContext) _local.get();
	if ( context == null ) {
	    context = new ThreadContext();
            context._inThread = true;
	    _local.set( context );
	}
        return context;
    }


    /**
     * Pushes a thread context to the current thread. This context
     * will be returned from a subsequent call to {@link #getThreadContext}
     * until the context is popped.
     *
     * @param context The thread context to be associated with the
     * current thread
     */
    protected static void pushThreadContext( ThreadContext context )
    {
        ThreadContext current;

        if ( context == null )
            throw new IllegalArgumentException( "Argument context is null" );
        synchronized ( context ) {
            if ( ! context._inThread ) {
                current = (ThreadContext) _local.get();
                if ( current != null )
                    current._previous = context;
                _local.set( context );
            }
        }
    }


    /**
     * Pops a thread context previously pushed with {@link #pushThreadContext}.
     *
     * @return The previous thread context, or null
     */
    protected static ThreadContext popThreadContext()
    {
        ThreadContext current;
        ThreadContext previous;

        current = (ThreadContext) _local.get();
        if ( current != null ) {
            previous = current._previous;
            current._previous = null;
            current._inThread = false;
            _local.set( previous );
            return previous;
        } else
            return null;
    }
	

    /**
     * Discards all resources associated with the thread context.
     *
     * @param context The thread context to discard
     */
    protected static void cleanup( ThreadContext context )
    {
        if ( context == null )
            throw new IllegalArgumentException( "Argument context is null" );
        if ( ! context._inThread ) {
            context._tx = null;
            context._resources = null;
        }
    }


    /**
     * Called to destroy all association with a thread. This method
     * is called when the thread is no longer used.
     *
     * @param thread The thread
     */
    protected static void cleanup( Thread thread )
    {
        if ( thread == null )
            throw new IllegalArgumentException( "Argument thread is null" );
        _local.remove( thread );
    }


    /**
     * Adds an XA resource to the association list.
     */
    public void add( XAResource xaResource )
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
    public boolean remove( XAResource xaResource )
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
     * are enlisted. This method returns XA resources for both
     * the current thread and all previous threads.
     *
     * @return All XA resources, or null
     */
    protected XAResource[] getResources()
    {
        int           count = 0;
        XAResource[]  resources;
        ThreadContext previous;

        if ( _resources == null && _previous == null )
            return null;
        if ( _resources != null ) {
            for ( int i = _resources.length ; i-- > 0 ; )
                if ( _resources[ i ] != null )
                    ++count;
        }
        previous = _previous;
        if ( previous == null ) {
            if ( count == 0 )
                return null;
            if ( count == _resources.length )
                return _resources;
        } else {
            while ( previous != null ) {
                if ( previous._resources != null ) {
                    for ( int i = previous._resources.length ; i-- > 0 ; )
                        if ( previous._resources[ i ] != null )
                            ++count;
                }
                previous = previous._previous;
            }
        }
        if ( count == 0 )
            return null;

        resources = new XAResource[ count ];
        count = 0;
        for ( int i = _resources.length ; i-- > 0 ; )
            if ( _resources[ i ] != null )
                resources[ count++ ] = _resources[ i ];
        previous = _previous;
        while ( previous != null ) {
            if ( previous._resources != null ) {
                for ( int i = previous._resources.length ; i-- > 0 ; )
                    if ( previous._resources[ i ] != null )
                        resources[ count++ ] = previous._resources[ i ];
            }
            previous = previous._previous;
        }
        return resources;
    }


}
