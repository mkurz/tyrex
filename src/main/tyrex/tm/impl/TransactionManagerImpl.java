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
 * $Id: TransactionManagerImpl.java,v 1.6 2001/03/16 03:38:28 arkin Exp $
 */


package tyrex.tm.impl;


import java.io.PrintWriter;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;
import tyrex.tm.TyrexTransactionManager;
import tyrex.tm.TransactionStatus;
import tyrex.util.Messages;


/**
 * Implements a local transaction manager. The transaction manager
 * allows the application server to manage transactions on the local
 * thread through the {@link TransactionManager} interface.
 * <p>
 * Nested transactions are supported if the server configuration
 * indicates so, but all nested transactions appear as flat
 * transactions to the resources and are not registered with the
 * transaction server.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.6 $ $Date: 2001/03/16 03:38:28 $
 * @see Tyrex#recycleThread
 * @see TransactionDomain
 * @see TransactionImpl
 */
final class TransactionManagerImpl
    implements TransactionManager, Status, TyrexTransactionManager
{


    /**
     * The transaction domain to which this manager belongs.
     */
    private TransactionDomainImpl       _txDomain;


    TransactionManagerImpl( TransactionDomainImpl txDomain )
    {
        if ( txDomain == null )
            throw new IllegalArgumentException( "Argument 'txDomain' is null" );
        _txDomain = txDomain;
    }


    //-------------------------------------------------------------------------
    // Methods defined in JTA Transaction
    //-------------------------------------------------------------------------


    public void begin()
        throws NotSupportedException, SystemException
    {
        ThreadContext   context;
        XAResource[]    resources;

        context = ThreadContext.getThreadContext();
        if ( context._tx != null && context._tx.getStatus() != STATUS_COMMITTED &&
             context._tx.getStatus() != STATUS_ROLLEDBACK ) {
            if ( ! _txDomain.getNestedTransactions() )
                throw new NotSupportedException( Messages.message( "tyrex.tx.noNested" ) );
            else {
                // A nested transaction is not registered with
                // the transaction server and the thread is not
                // enlisted. Resources are not enlisted with
                // this transaction, only the top-level one.
                context._tx = _txDomain.createTransaction( context._tx, null, 0 );
                return; // !!! Is this the correct behavior
            }
        } else
            context._tx = _txDomain.createTransaction( null, Thread.currentThread(), 0 );

        // If there are any resources associated with the thread,
        // we need to enlist them with the transaction.
        resources = context.getResources();
        if ( resources != null )
            enlistResources( context._tx, resources );
        
        try {
            // register a synchronization to perform cleanup
            context._tx.registerSynchronization( new TransactionManagerSynchronization() );
        } catch ( RollbackException except ) {
            // this should not happen
            throw new NestedSystemException( except );
        }
    }


    public void commit()
        throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
               SecurityException, IllegalStateException, SystemException
    {
        ThreadContext   context;
        TransactionImpl parent;

        context = ThreadContext.getThreadContext();
        if ( context._tx == null )
            throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
        parent = (TransactionImpl) context._tx.getParent();
        try {
            context._tx.commit();
        } finally {
            // This forgets about the transaction for the thread.
            // We cannot forget about the resources, since a new
            // transaction might be started on this thread.
            // If this is a sub-transaction, we resume the parent
            // transaction.
            context._tx = parent;
        }
    }


    public void rollback()
        throws IllegalStateException, SecurityException, SystemException
    {
        ThreadContext   context;
        TransactionImpl parent;
        
        context = ThreadContext.getThreadContext();
        if ( context._tx == null )
            throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
        parent = (TransactionImpl) context._tx.getParent();
        try {
            context._tx.rollback();
        } finally {
            // This forgets about the transaction for the thread.
            // We cannot forget about the resources, since a new
            // transaction might be started on this thread.
            // If this is a sub-transaction, we resume the parent
            // transaction.
            context._tx = parent;
        }
    }


    public int getStatus()
    {
        ThreadContext   context;

        context = ThreadContext.getThreadContext();
        if ( context._tx == null )
            return Status.STATUS_NO_TRANSACTION;
        else
            return context._tx._status;
    }


    public Transaction getTransaction()
    {
        ThreadContext   context;

        context = ThreadContext.getThreadContext();
        if ( context._tx == null )
            return null;    
        return  context._tx;
    }


    public void resume( Transaction tx )
        throws InvalidTransactionException, IllegalStateException, SystemException
    {
        ThreadContext   context;
        TransactionImpl txImpl;

        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        if ( ! ( tx instanceof TransactionImpl ) )
            throw new InvalidTransactionException( Messages.message( "tyrex.tx.resumeForeign" ) );
        txImpl = (TransactionImpl) tx;
        context = ThreadContext.getThreadContext();
        if ( context._tx != null )
            throw new IllegalStateException( Messages.message( "tyrex.tx.resumeOverload" ) );
        synchronized ( tx ) {
            if ( txImpl.getTimedOut() )
                throw new InvalidTransactionException( Messages.message( "tyrex.tx.timedOut" ) );
            if ( txImpl._status != Status.STATUS_ACTIVE && txImpl._status != Status.STATUS_MARKED_ROLLBACK )
                throw new InvalidTransactionException( Messages.message( "tyrex.tx.inactive" ) );
            context._tx = txImpl;
            txImpl = (TransactionImpl) txImpl.getTopLevel();
            try {
                txImpl.resumeAndEnlistResources( context.getResources() );
            } catch ( RollbackException except ) { }
            // Enlist the current thread with the transaction so it
            // may timeout the thread. We always enlist the top
            // level transaction.
            _txDomain.enlistThread( txImpl, Thread.currentThread() );
        }
    }


    public Transaction suspend()
    {
        ThreadContext   context;
        TransactionImpl tx;

        context = ThreadContext.getThreadContext();
        if ( context._tx == null )
            return null;
        
        // get the toplevel transaction
        tx = (TransactionImpl) context._tx.getTopLevel();
        context._tx = null;
        
        synchronized ( tx ) {
            // Delist the current thread form the transaction so it
            // does not attempt to timeout. We always enlist the top
            // level transaction.
            _txDomain.delistThread( tx, Thread.currentThread() );
            
            // We do not return an inactive transaction.
            if ( tx._status == STATUS_ACTIVE || tx._status == STATUS_MARKED_ROLLBACK ) {
                // It is our responsibility at this point to suspend
                // all the resources we associated with the transaction.
                try {
                    // the resources associated with the thread is
                    // a subset of the resources associated with the 
                    // transaction.
                    tx.suspendResources();
                } catch ( SystemException except ) { }
                return tx;
            } else
                return null;
        }
    }
    

    public void setRollbackOnly()
        throws IllegalStateException, SystemException
    {
        ThreadContext   context;
        
        context = ThreadContext.getThreadContext();
        if ( context._tx == null )
            throw new IllegalStateException( Messages.message( "tyrex.tx.inactive" ) );
        context._tx.setRollbackOnly();
    }


    public void setTransactionTimeout( int seconds )
    {
        ThreadContext   context;
        
        context = ThreadContext.getThreadContext();
        if ( context._tx != null )
            _txDomain.setTransactionTimeout( (TransactionImpl) context._tx.getTopLevel(), seconds );
    }


    //-------------------------------------------------------------------------
    // Methods defined by TyrexTransactionManager
    //-------------------------------------------------------------------------


    public Transaction getTransaction( Xid xid )
    {
        return _txDomain.findTransaction( xid );
    }


    public TransactionStatus getTransactionStatus( Thread thread )
    {
        TransactionImpl tx;

        if ( thread == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        tx = (TransactionImpl) getTransaction( thread );
        if ( tx == null )
            return null;
        return new TransactionStatusImpl( tx );
    }


    public TransactionStatus[] listTransactions()
    {
        return _txDomain.listTransactions();
    }


    public void dumpTransactionList( PrintWriter writer )
    {
        _txDomain.dumpTransactionList( writer );
    }


    public void dumpCurrentTransaction( PrintWriter writer )
    {
        TransactionImpl  tx;
        Thread[]         threads;
        int              count = 0;

        if ( writer == null )
            throw new IllegalArgumentException( "Argument writer is null" );
        tx = (TransactionImpl) getTransaction();
        if ( tx == null )
            writer.println( "No transaction associated with current thread" );
        else {
            threads = tx._threads;
            if ( threads != null ) {
                for ( int i = threads.length ; i-- > 0 ; )
                    if ( threads[ i ] != null )
                        ++count;
            }
            writer.println( "  Transaction " + tx._xid + " " + Util.getStatus( tx._status ) +
                            ( count != 0 ? ( " " + count + " threads" ) : "" ) );
            writer.println( "  Started " + Util.fromClock( tx._started ) +
                            " time-out " + Util.fromClock( tx._timeout ) );
        }
    }


    /**
     * Returns the transaction currently associated with the given
     * thread, or null if the thread is not associated with any
     * transaction. This method is equivalent to calling {@link
     * TransactionManager#getTransaction} from within the thread.
     *
     * @param thread The thread to lookup
     * @return The transaction currently associated with that thread
     */
    public Transaction getTransaction( Thread thread )
    {
        return ThreadContext.getThreadContext()._tx;
    }


    public void enlistResource( XAResource xaResource )
        throws SystemException
    {
        ThreadContext   context;
        
        if ( xaResource == null )
            throw new IllegalArgumentException( "Argument xaResource is null" );
        context = ThreadContext.getThreadContext();
        // If there is a transaction in progress, we enlist the
        // resource immediately and throw an exception if we
        // cannot do that. Otherwise, we just record the resource
        // for the current thread.
        // For the resources all transaction are flat: we always
        // enlist with the top level transaction.
        context.add( xaResource );
        if ( context._tx != null ) {
            try {
                context._tx.getTopLevel().enlistResource( xaResource );
            } catch ( IllegalStateException except ) {
                // The transaction is preparing/committing.
                // We still allow future enlistment.
            } catch ( RollbackException except ) {
                // The transaction has rolled back.
                // We still allow future enlistment.
            }
        }
    }


    public void delistResource( XAResource xaResource, int flag )
    {
        ThreadContext   context;
        
        if ( xaResource == null )
            throw new IllegalArgumentException( "Argument xaResource is null" );
        if ( flag != XAResource.TMSUCCESS && flag != XAResource.TMFAIL )
            throw new IllegalArgumentException( "Invalid value for flag" );
        context = ThreadContext.getThreadContext();
        // If there is a transaction in progress, we delist the
        // resource immediately and throw an exception if we
        // cannot do that. Otherwise, we just remove the resource
        // for the current thread.
        // For the resources all transaction are flat: we always
        // delist with the top level transaction.
        context.remove( xaResource );
        if ( context._tx != null ) {
            try {
                context._tx.getTopLevel().delistResource( xaResource, flag );
            } catch ( SystemException except ) {
                // We ignore failure to delist.
            } catch ( IllegalStateException except ) {
                // The transaction is preparing/committing.
                // We still need to prevent future enlistment.
            }
        }
    }


    //-------------------------------------------------------------------------
    // Implementation details
    //-------------------------------------------------------------------------


    /**
     * Called to resume the current transaction, but does not attempt to
     * associate the resources with this transaction. This method is used
     * during the synchronization.
     */
    protected void internalResume( TransactionImpl tx )
        throws IllegalStateException, SystemException
    {
        ThreadContext   context;

        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        context = ThreadContext.getThreadContext();
        if ( context._tx != null )
            throw new IllegalStateException( Messages.message( "tyrex.tx.resumeOverload" ) );
        synchronized ( tx ) {
            context._tx = tx;
            tx = (TransactionImpl) tx.getTopLevel();
            // Enlist the current thread with the transaction so it
            // may timeout the thread. We always enlist the top
            // level transaction.
            _txDomain.enlistThread( tx, Thread.currentThread() );
        }
    }


    /**
     * Enlist the XA resources in the specified transaction. The
     * transaction is assumed to be a top-level transaction.
     *
     * @param tx The top-level transaction
     * @param xaResources The array of XA resources to be enlisted
     * in the transaction. Can be null.
     * @throws SystemException if there is a problem enlisting the 
     * resources.
     */
    private void enlistResources( Transaction tx, XAResource[] xaResources )
        throws SystemException
    {
        if ( xaResources != null ) {
            try {
                for ( int i = xaResources.length ; i-- > 0 ; )
                    tx.enlistResource( xaResources[ i ] );
            } catch ( Exception except ) {
                // Any error that occurs in the enlisting and we
                // cannot go on with the transaction. Must rollback
                // the enlisted resources, though.
                try {
                    rollback();
                } catch ( Exception except2 ) { }
                if ( except instanceof SystemException )
                    throw ( SystemException) except;
                else
                    throw new NestedSystemException( except );
            }
        }
    }


    /**
     * The transaction that has been executing in this
     * thread is complete.
     */
    private void transactionComplete()
    {
        ThreadContext   context;
        TransactionImpl tx;
        
        context = ThreadContext.getThreadContext();
        tx = context._tx;
        if ( tx != null )
            context._tx = (TransactionImpl) tx.getParent();
    }


    /**
     * Synchronization used to inform the transaction manager
     * when a transaction is complete.
     */
    private class TransactionManagerSynchronization 
        implements Synchronization
    {


        /**
         * Create the EnlistedResourceSynchronization with the
         * the specified thread resources
         */
        private TransactionManagerSynchronization()
        {
        }
        

        public void afterCompletion( int status ) 
        {
            TransactionManagerImpl.this.transactionComplete();
        }
       

        public void beforeCompletion() 
        {
            //do nothing
        }


    }


}
