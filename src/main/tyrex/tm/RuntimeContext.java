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
 * $Id: RuntimeContext.java,v 1.1 2001/03/12 19:23:40 arkin Exp $
 */


package tyrex.tm;


import javax.transaction.xa.XAResource;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.transaction.Transaction;
import tyrex.naming.MemoryBinding;
import tyrex.tm.impl.ThreadContext;
import tyrex.util.FastThreadLocal;


/**
 * The runtime context provides an association between a component
 * or client, the current thread, and resources used by the application.
 * <p>
 * The runtime context keeps track of active transaction, JNDI ENC,
 * security subject, and open connections. It is associated with a
 * component or client, and can be preserved across method invocations
 * by associating it with the current thread.
 * <p>
 * Each thread is associated with a runtime context, whether explicitly
 * or implicitly. A thread can be associated with a particular runtime
 * context by calling {@link #setRuntimeContext setRuntimeContext}.
 * If this method was not called, a default runtime context is created
 * on demand.
 * <p>
 * A new runtime context can be created with one of the {@link
 * #newRuntimeContext newRuntimeContext} methods and populated with
 * JNDI bindings and security context. The runtime context is then
 * associated with the current thread across method invocations
 * belonging to the same component or client.
 * <p>
 * The current thread association behaves like a FIFO stack.
 * The current runtime context is the last context associated with
 * a call to {@link #setRuntimeContext setRuntimeContext}. Calling
 * this method multiple times pushes runtime contexts down the
 * stack. The previous runtime context can be restored by calling
 * {@link #unsetRuntimeContext unsetRuntimeContext}.
 * <p>
 * The runtime context keeps track of the current active transaction.
 * When the runtime context is associated with the current thread,
 * this is the same transaction available from <tt>TransactionManager</tt>.
 * <p>
 * The runtime context keeps track of the JNDI environment naming
 * context. The JNDI bindings are accessible from the <tt>java:</tt>
 * URL when the runtime context is associated with the current thread.
 * The bindings can be changed by obtaining a read-write JNDI context
 * from the runtime context.
 * <p>
 * The runtime context keeps track of the security subject used for
 * authentication and authorization when the runtime context is
 * associated with the current thread.
 * <p>
 * The runtime context keeps track of all connections obtained from
 * the environment naming context. Open connections can be retained
 * across method invocations.
 * <p>
 * This runtime context is thread-safe.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2001/03/12 19:23:40 $
 */
public abstract class RuntimeContext
{


    /**
     * Returns the JNDI environment context associated with this
     * runtime context.
     * <p>
     * The returned context is read-write and can be populated with
     * objects available from the JNDI environment context when this
     * runtime context is associated with the current thread.
     *
     * @return The JNDI environment context
     */
    public abstract Context getEnvContext();


    /**
     * Returns the transaction associated with this runtime context.
     * <p>
     * If the runtime context is associated with any open transaction,
     * the transaction will be returned. When the runtime context is
     * associated with the current thread, this method will return the
     * same transaction as {@link TransactionManager#getTransaction}.
     *
     * @return The transaction, or null
     */
    public abstract Transaction getTransaction();


    /**
     * Returns the security subject associated with this runtime context.
     * <p>
     * This security subject is used for authentication and authorization.
     *
     * @return The security subject, or null
     */
    public abstract Subject getSubject();


    /**
     * Cleanup the runtime context and discard all resources associated
     * with it.
     * <p>
     * This method is called when it has been determined that this runtime
     * context will no longer be used.
     */
    public abstract void cleanup();


    /**
     * Creates and returns a new runtime context.
     * <p>
     * If a JNDI context is provided, it's bindings will be used for the
     * JNDI environment naming context available when this context is
     * associated with the current thread.
     * <p>
     * If not JNDI context is provided, a new context will be created.
     * The context can be populated with objects by calling {@link
     * #getEnvContext} on the new runtime context.
     * <p>
     * If a security subject is provided, it will be used for authentication
     * and authorization when this context is associated with the current
     * thread.
     * <p>
     * The runtime context is not associated with any thread. To associate
     * the runtime context with a thread, use {@link #setRuntimeContext
     * setRuntimeContext}.
     *
     * @param envContext The JNDI environment context to use, or null
     * @param subject The security subject, or null
     * @throws NamingException The context is not a valid context
     */
    public static RuntimeContext newRuntimeContext( Context envContext, Subject subject )
        throws NamingException
    {
        return new ThreadContext( envContext, subject );
    }


    /**
     * Creates and returns a new runtime context.
     * <p>
     * A new context will be created. The context can be populated with objects
     * by calling {@link #getEnvContext} on the new runtime context.
     * <p>
     * The runtime context is not associated with any thread. To associate
     * the runtime context with a thread, use {@link #setRuntimeContext
     * setRuntimeContext}.
     */
    public static RuntimeContext newRuntimeContext()
    {
        return new ThreadContext( null );
    }


    /**
     * Returns the runtime context associated with the current thread.
     * <p>
     * This method returns the last runtime context associated with the
     * current thread by calling {@link #setRuntimeContext setRuntimeContext}.
     * <p>
     * If the thread was not previously associated with any runtime context,
     * a new runtime context is created and associated with the thread.
     * This method never returns null.
     *
     * @return The runtime context
     */
    public static RuntimeContext getRuntimeContext()
    {
        return ThreadContext.getThreadContext();
    }


    /**
     * Associates a runtime context with the current thread.
     * <p>
     * While in effect, this runtime context will provide the transaction,
     * JNDI environment context, security subject and resources for all
     * operations performed in the current thread.
     * <p>
     * If the current thread is assocaited with any other runtime context,
     * the association will be suspended until a subsequent call to
     * {@link #unsetRuntimeContext}.
     * <p>
     * The runtime context must have previoulsy been created with one of
     * the {@link #newRuntimeContext newRuntimeContext} methods listed here.
     *
     * @param context The runtime context to be associated with the
     * current thread
     */
    public static void setRuntimeContext( RuntimeContext context )
    {
        ThreadContext current;

        if ( context == null )
            throw new IllegalArgumentException( "Argument context is null" );
        if ( ! ( context instanceof ThreadContext ) )
            throw new IllegalArgumentException( "Argument context is not a valid runtime context" );
        ThreadContext.setThreadContext( (ThreadContext) context );
    }


    /**
     * Dissociates the runtime context from the current thread,
     * <p>
     * This method dissociates the last current runtime associated
     * using {@link #setRuntimeContext setRuntimeContext}, and restores
     * a previous runtime context.
     * <p>
     * The dissociated runtime context is returned. Call {@link #cleanup},
     * if the dissociated runtime context will not be used in the future.
     *
     * @return The previous runtime context, or null
     */
    public static RuntimeContext unsetRuntimeContext()
    {
        return ThreadContext.unsetThreadContext();
    }


    /**
     * Called to destroy all association with a thread. This method
     * is called when the thread is no longer used.
     *
     * @param thread The thread
     */
    public static void cleanup( Thread thread )
    {
        if ( thread == null )
            throw new IllegalArgumentException( "Argument thread is null" );
        ThreadContext.cleanup( thread );
    }


    /**
     * Creates and returns an new JNDI context.
     * <p>
     * The JNDI context can be populated with objects and passed to a newly
     * created runtime context, to be used for the JNDI environment naming
     * context.
     *
     * @return A new JNDI context
     */
    public static Context newEnvContext()
    {
        return new MemoryBinding().getContext();
    }


}
