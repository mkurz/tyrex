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
 * $Id: TransactionDomainImpl.java,v 1.31 2002/04/17 00:53:22 mohammed Exp $
 */


package tyrex.tm.impl;


import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Iterator;
import java.util.ArrayList;
import java.lang.reflect.Field;
import java.security.AccessController;
import org.apache.log4j.Category;
import org.omg.CosTransactions.otid_t;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TSIdentification;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;
import tyrex.tm.DomainMetrics;
import tyrex.tm.Heuristic;
import tyrex.tm.TransactionDomain;
import tyrex.tm.TransactionInterceptor;
import tyrex.tm.TransactionTimeoutException;
import tyrex.tm.DomainConfigurationException;
import tyrex.tm.Journal;
import tyrex.tm.JournalFactory;
import tyrex.tm.RecoveryException;
import tyrex.tm.xid.BaseXid;
import tyrex.tm.xid.XidUtils;
import tyrex.resource.Resource;
import tyrex.resource.Resources;
import tyrex.resource.ResourceException;
import tyrex.services.Clock;
import tyrex.services.DaemonMaster;
import tyrex.services.UUID;
import tyrex.util.Messages;
import tyrex.util.Configuration;
import tyrex.util.LoggerPrintWriter;
import tyrex.util.Logger;


/**
 * Implementation of a transaction domain.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.31 $ $Date: 2002/04/17 00:53:22 $
 */
public class TransactionDomainImpl
    extends TransactionDomain
    implements Runnable, DomainMetrics
{


    // IMPLEMENTATION NOTES:
    //
    //   All access to transactions must be synchronzied against
    //   the transaction object itself to prevent changes to the
    //   transaction as it is being committed/rolledback.
    //
    //   All transactions have unique, non-repeatable global
    //   identifiers. If synchronizing against the transaction
    //   object itself, there is no need to synchronized access
    //   to _hashTable.


    /**
     * The size of the hash table. This must be a prime value.
     */
    public static final int  TABLE_SIZE = 1103;


    /**
     * A hash table of transactions.
     */
    private final TransactionImpl[]        _hashTable;


    /**
     * The number of transactions in the hash table.
     */
    private int                            _txCount;


    /**
     * The name of this transaction domain.
     */
    private final String                   _domainName;


    /**
     * A singleton transaction manager implementation.
     */
    protected final TransactionManagerImpl _txManager;


    /**
     * A singleton user transaction implementation.
     */
    private final UserTransaction          _userTx;


    /**
     * The transaction factory.
     */
    private final TransactionFactoryImpl   _txFactory;
    
    
    /**
     * The CORBA ORB used by this transaction domain, or null
     * if no CORBA ORB is used.
     */
    protected ORB                          _orb;


    /**
     * Field to access the branch qualifier length member variable of <tt>otid_t</tt>,
     * which is named differently in the Sun JTS and OMG OTS IDLs.
     */
    private Field                          _bqualField;



    /**
     * The default timeout for all transactions, in seconds.
     */
    private int                            _txTimeout;


    /**
     * The time to wait for a new transaction when limit exceeded.
     * This value is specified in milliseconds, while it is specified
     * in seconds for {@link DomainConfig}.
     */
    private int                            _waitNew;


    /**
     * The log4J category for this transaction domain.
     */
    protected final Category               _category;


    /**
     * True if nested transactions are supported.
     */
    private boolean                        _nestedTx = false;


    /**
     * A list of registered transaction interceptors. This array is
     * never null, if there are no interceptors it is empty.
     */
    private TransactionInterceptor[]       _interceptors;


    /**
     * The next clock time we terminate a transaction, or null if
     * no transaction has a pending timeout.
     */
    private long                           _nextTimeout = 0;


    /**
     * The transaction journal used by this domain.
     */
    protected final Journal                _journal;


    /**
     * The maximum number of concurrent transactions supported.
     */
    private final int                      _maximum;


    /**
     * Resources loaded for this transaction domain.
     */
    private final Resources                _resources;


    /**
     * The state of the transaction domain.
     */
    private int                            _state;


    /**
     * Records errors reported during recovery.
     */
    private RecoveryException              _recoveryErrors;


    /**
     * The next domain in a single linked list of transaction domains.
     */
    private TransactionDomainImpl          _nextDomain;


    /**
     * The accumulated transaction time.
     */
    private int                            _accumTime;

    
    /**
     * The accumulated count of committed transactions.
     */
    private int                            _accumCommitted;


    /**
     * The accumulated count of rollback transactions.
     */
    private int                            _accumRolledback;


    /**
     * The number of active transactions.
     */
    private int                            _active;


    /**
     * Constructs a new transaction domain.
     *
     * @param config The domain configuration object
     * @throws DomainConfigurationException Failed to create the transaction domain
     */
    public TransactionDomainImpl( DomainConfig config )
        throws DomainConfigurationException
    {
        String         domainName;
        JournalFactory factory;
        String         factoryName;

        if ( config == null )
            throw new IllegalArgumentException( "Argument config is null" );
        domainName = config.getName();
        if ( domainName == null || domainName.trim().length() == 0 )
            throw new DomainConfigurationException( "The domain name is missing" );
        _domainName = domainName.trim();
        _maximum = config.getMaximum();
        setTransactionTimeout( config.getTimeout() );
        _waitNew = config.getWaitNew() * 1000;

        /*
        factoryName = config.getJournalFactory();
        if ( factoryName != null && factoryName.trim().length() != 0 ) {
            factoryName = factoryName.trim();
            try {
                factory = (JournalFactory) getClass().getClassLoader().loadClass( factoryName ).newInstance();
            } catch ( Exception except ) {
                throw new DomainConfigurationException( "Error obtaining transaction journal factory " + factoryName, except );
            }
            try {
                _journal = factory.openJournal( _domainName );
            } catch ( SystemException except ) {
                throw new DomainConfigurationException( except );
            }
        } else*/
            _journal = null;

        _interceptors = new TransactionInterceptor[ 0 ];
        _txManager = new TransactionManagerImpl( this );
        _userTx = new UserTransactionImpl( _txManager );
        _txFactory = new TransactionFactoryImpl( this );
        _category = Category.getInstance( "tyrex." + _domainName );
        //_category.addAppender(Logger.appender);
        _hashTable = new TransactionImpl[ TABLE_SIZE ];

        // Obtain all the resources. We need to have the transaction manager
        // set up first for the purpose of creating connection pools.
        if ( config.getResources() != null ) {
            try {
                _resources = config.getResources();
                _resources.setTransactionDomain( this );
            } catch ( Exception except ) {
                throw new DomainConfigurationException( except );
            }
        } else
            _resources = new Resources();
        
        // starts the background thread
        DaemonMaster.addDaemon( this, "Transaction Domain " + _domainName );
        _state = READY;
        if ( Configuration.verbose )
            _category.info( "Created transaction domain " + _domainName );
    }


    public String toString()
    {
        return _domainName;
    }


    //----------------------------------------------------------------
    // Methods defined for TransactionDomain
    //----------------------------------------------------------------


    public TransactionManager getTransactionManager()
    {
        return _txManager;
    }


    public UserTransaction getUserTransaction()
    {
        return _userTx;
    }

    
    public TransactionFactory getTransactionFactory()
    {
        return _txFactory;
    }


    public int getTransactionTimeout()
    {
        return _txTimeout;
    }


    public boolean getNestedTransactions()
    {
        return _nestedTx;
    }


    public synchronized void addInterceptor( TransactionInterceptor interceptor )
    {
        TransactionInterceptor[] newInterceptors;

        for ( int i = 0 ; i < _interceptors.length ; ++i )
            if ( _interceptors[ i ] == interceptor )
                return;
        newInterceptors = new TransactionInterceptor[ _interceptors.length + 1 ];
        System.arraycopy( _interceptors, 0, newInterceptors, 0, _interceptors.length );
        newInterceptors[ _interceptors.length ] = interceptor;
        _interceptors = newInterceptors;
    }


    public synchronized void removeInterceptor( TransactionInterceptor interceptor )
    {
        TransactionInterceptor[] newInterceptors;
        
        for ( int i = 0 ; i < _interceptors.length ; ++i ) {
            if ( _interceptors[ i ] == interceptor ) {
                _interceptors[ i ] = _interceptors[ _interceptors.length - 1 ];
                newInterceptors = new TransactionInterceptor[ _interceptors.length - 1 ];
                System.arraycopy( _interceptors, 0, newInterceptors, 0, _interceptors.length - 1 );
                _interceptors = newInterceptors;
                break;
            }
        }
    }


    public synchronized void terminate()
    {
        if ( _state != TERMINATED ) {
            _state = TERMINATED;
            // Notify the background thread that we are terminating.
            // This will cause all transactions to time out.
            notifyAll();
        }
    }


    public DomainMetrics getDomainMetrics()
    {
        return this;
    }


    public String getDomainName()
    {
        return _domainName;
    }


    public Resources getResources()
    {
        return _resources;
    }


    public int getState()
    {
        return _state;
    }

    public synchronized void recover()
        throws RecoveryException
    {
        if ( _state == READY ) {
            _state = ACTIVE;
        }
    }

    /*public synchronized void recover()
        throws RecoveryException
    {
        ArrayList         array;
        Resource          resource;
        XAResource        xaResource;
        XAResource[]      xaResources;
        Iterator          iterator;
        RecoveryException errors;
        RecoveryException next;
        PrintWriter       writer;

        if ( _state == READY ) {
            _state = RECOVERING;
            array = new ArrayList();
            iterator = _resources.listResources();
            while ( iterator.hasNext() ) {
                try {
                    resource = _resources.getResource( (String) iterator.next() );
                    if ( resource != null ) {
                        xaResource = resource.getXAResource();
                        if ( xaResource != null )
                            array.add( xaResource );
                    }
                } catch ( ResourceException except ) {
                    recoveryError( new RecoveryException( except ) );
                }
            }
            xaResources = (XAResource[]) array.toArray( new XAResource[ array.size() ] );
            recover( _journal, xaResources );
            _state = ACTIVE;
            errors = _recoveryErrors;
            _recoveryErrors = null;
            if ( errors != null ) {
                _category.info( "Transaction recovery for domain " + _domainName +
                                " reported errors:" );
                writer = new LoggerPrintWriter( _category, null, true );
                next = errors;
                while ( next != null ) {
                    writer.println( next.toString() );
                    next = next.getNextException();
                }
                throw errors;
            }
        }
    } */


    //----------------------------------------------------------------
    // Methods defined for DomainMetrics
    //----------------------------------------------------------------


    public int getTotalCommitted()
    {
        return _accumCommitted;
    }


    public int getTotalRolledback()
    {
        return _accumRolledback;
    }


    public synchronized float getAvgDuration()
    {
        return ( (float) ( _accumTime ) / (float) _accumCommitted + _accumRolledback ) / 10000;
    }


    public int getActive()
    {
        return _active;
    }


    public synchronized void reset()
    {
        _accumTime = 0;
        _accumCommitted = 0;
        _accumRolledback = 0;
    }


    //-------------------------------------------------------------------------
    // Methods used by other classes
    //-------------------------------------------------------------------------


    public TransactionImpl findTransaction( Xid xid )
    {
        TransactionImpl entry;
        int             hashCode;
        int             index;

        if ( xid == null )
            throw new IllegalArgumentException( "Argument xid is null" );
        hashCode = xid.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _hashTable.length;
        entry = _hashTable[ index ];
        if ( entry != null ) {
            if ( entry._hashCode == hashCode && entry._xid.equals( xid ) )
                return entry;
            entry = entry._nextEntry;
            while ( entry != null ) {
                if ( entry._hashCode == hashCode && entry._xid.equals( xid ) )
                    return entry;
                entry = entry._nextEntry;
            }
        }
        return null;
    }


    public TransactionImpl findTransaction( String xid )
    {
        TransactionImpl entry;
        int             hashCode;
        int             index;

        if ( xid == null )
            throw new IllegalArgumentException( "Argument xid is null" );
        hashCode = xid.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _hashTable.length;
        entry = _hashTable[ index ];
        if ( entry != null ) {
            if ( entry._hashCode == hashCode && entry._xid.toString().equals( xid ) )
                return entry;
            entry = entry._nextEntry;
            while ( entry != null ) {
                if ( entry._hashCode == hashCode && entry._xid.toString().equals( xid ) )
                    return entry;
                entry = entry._nextEntry;
            }
        }
        return null;
    }


    public TransactionDomainImpl getNextDomain()
    {
        return _nextDomain;
    }


    public void setNextDomain( TransactionDomainImpl nextDomain )
    {
        _nextDomain = nextDomain;
    }


    /**
     * Creates a new transaction. If <tt>parent</tt> is not null,
     * the transaction is nested within its parent. The transaction
     * timeout is specified in seconds, or zero to use the default
     * transaction timeout. Throws a {@link SystemException} if
     * we have reached the quota for new transactions or active
     * transactions, or the server has not been started.
     *
     * @param parent The parent transaction
     * @param timeout The default timeout for the new transaction,
     * specified in seconds
     * @return The newly created transaction
     * @throws SystemException Reached the quota for new transactions
     */
    protected TransactionImpl createTransaction( TransactionImpl parent,
                                                 long timeout )
        throws SystemException
    {
        TransactionImpl newTx;
        TransactionImpl entry;
        TransactionImpl next;
        BaseXid         xid;
        int             hashCode;
        int             index;

        if ( _state != ACTIVE )
            throw new SystemException( "Transaction domain not active" );

        // Create a new transaction with a new Xid. At the moment,
        // this also works for nested transactions.
        xid = (BaseXid) XidUtils.newGlobal();
        if ( timeout <= 0 )
            timeout = _txTimeout;
        else if ( timeout > DomainConfig.MAXIMUM_TIMEOUT )
            timeout = DomainConfig.MAXIMUM_TIMEOUT;
        hashCode = xid.hashCode();
        newTx = new TransactionImpl( xid, parent, this, timeout * 1000 );
        timeout = newTx._timeout;

        synchronized ( this ) {
            // Block if exceeded maximum number of transactions allowed.
            // At this point we might get a SystemException. This only
            // applies for top-level transactions.
            if ( parent == null )
                canCreateNew();
        
            for ( int i = 0 ; i < _interceptors.length ; ++i ) {
                try {
                    _interceptors[ i ].begin( xid );
                } catch ( Throwable thrw ) {
                    _category.error( "Interceptor " + _interceptors[ i ] + " reported error", thrw );
                }
            }
            
            // Nested transactions are not registered directly
            // with the transaction domain. They are not considered
            // new creation/activation and are not subject to timeout.
            if ( parent != null )
                return newTx;
            
            index = ( hashCode & 0x7FFFFFFF ) % _hashTable.length;
            entry = _hashTable[ index ];
            if ( entry == null )
                _hashTable[ index ] = newTx;
            else {
                next = entry._nextEntry;
                while ( next != null ) {
                    if ( next._hashCode == hashCode && next._xid.equals( xid ) )
                        throw new SystemException( "A transaction with the identifier " + xid.toString() + " already exists" );
                    entry = next;
                    next = next._nextEntry;
                }
                entry._nextEntry = newTx;
            }
            ++_txCount;
            ++_active;
            
            // If this transaction times out before any other transaction,
            // need to wakeup the background thread so it can update its
            // transaction timeout.
            if ( _nextTimeout == 0 || _nextTimeout > timeout ) {
                _nextTimeout = timeout;
                notifyAll();
            }
        }
        return newTx;
    }


    /**
     * Creates a new transaction to represent a remote OTS
     * transaction, but does not activate it yet. Throws a {@link
     * SystemException} if we have reached the quota for new
     * transactions or the server has not been started.
     * <p>
     * The newly created transaction will have a non-native Xid,
     * therefore it cannot be distributed across two machines using
     * the RMI interface but only through OTS propagation context.
     *
     * @param pgContext The OTS propagation context
     * @return The newly created transaction
     * @throws SystemException Reached the quota for new transactions,
     * or a transaction with the same identifier already exists
     * @see TransactionFactoryImpl
     * @see PropagationContext
     */
    protected TransactionImpl recreateTransaction( PropagationContext pgContext )
        throws SystemException
    {
        TransactionImpl newTx;
        TransactionImpl entry;
        TransactionImpl next;
        byte[]          global;
        BaseXid         xid;
        int             hashCode;
        int             index;
        long            timeout;
        otid_t          otid;
        Class           otidClass;
        int             bqualLength;

        if ( pgContext == null )
            throw new IllegalArgumentException( "Argument pgContext is null" );
        if ( pgContext.current == null || pgContext.current.otid == null )
            throw new SystemException( "Propagation context missing otid in current transaction identifier" );

        if ( _state != ACTIVE )
            throw new SystemException( "Transaction domain not active" );

        otid = pgContext.current.otid;
        otidClass = otid.getClass();
        if ( _bqualField == null ) {
            try {
                // Get the otid_t field for OTS
                _bqualField = otidClass.getField( "bqual_length" );
            } catch ( NoSuchFieldException except ) {
                try {
                    // Get the otid_t field for JTS
                    _bqualField = otidClass.getField( "bequal_length" );
                } catch ( NoSuchFieldException except2 ) {
                    throw new NestedSystemException( except2 );
                } catch ( SecurityException except2 ) { 
                    throw new NestedSystemException( except2 );
                } 
            } catch ( SecurityException except ) { 
                throw new NestedSystemException( except );
            }
        }

        // Get the bqual field length using introspection.
        try {
            bqualLength = _bqualField.getInt( otid );
        } catch ( IllegalAccessException except ) {
            throw new NestedSystemException( except );
        } catch ( IllegalArgumentException except ) {
            throw new NestedSystemException( except );
        }
        global = new byte[ bqualLength ];
        for ( int i = bqualLength ; i-- > 0 ; )
            global[ i ] = otid.tid[ i ];
        xid = (BaseXid) XidUtils.importXid( otid.formatID, global, null );

        synchronized ( this ) {
            // Check if we already have this transaction, if so return the existing
            // transaction, otherwise proceed to create a new one.
            newTx = findTransaction( xid );
            if ( newTx != null )
                return newTx;
        
            timeout = pgContext.timeout;
            if ( timeout <= 0 )
                timeout = _txTimeout;
            else if ( timeout > DomainConfig.MAXIMUM_TIMEOUT )
                timeout = DomainConfig.MAXIMUM_TIMEOUT;
            // !!! Is pgContext timeout in seconds or milliseconds
            try {
                newTx = new TransactionImpl( xid, pgContext, this, timeout * 1000 );
            } catch ( Inactive except ) {
                throw new SystemException( Messages.message( "tyrex.tx.inactive" ) );
            }
            hashCode = xid.hashCode();
            timeout = newTx._timeout;
            
            // Block if exceeded maximum number of transactions allowed.
            // At this point we might get a SystemException.
            canCreateNew();
            
            for ( int i = 0 ; i < _interceptors.length ; ++i ) {
                try {
                    _interceptors[ i ].begin( xid );
                } catch ( Throwable thrw ) {
                    _category.error( "Interceptor " + _interceptors[ i ] + " reported error", thrw );
                }
            }
            
            index = ( hashCode & 0x7FFFFFFF ) % _hashTable.length;
            entry = _hashTable[ index ];
            if ( entry == null )
                _hashTable[ index ] = newTx;
            else {
                next = entry._nextEntry;
                while ( next != null ) {
                    if ( next._hashCode == hashCode && next._xid.equals( xid ) )
                        throw new SystemException( "A transaction with the identifier " + xid.toString() + " already exists" );
                    entry = next;
                    next = next._nextEntry;
                }
                entry._nextEntry = newTx;
            }
            ++_txCount;
            ++_active;
            
            // If this transaction times out before any other transaction,
            // need to wakeup the background thread so it can update its
            // transaction timeout.
            if ( _nextTimeout == 0 || _nextTimeout > timeout ) {
                _nextTimeout = timeout;
                notifyAll();
            }
        }
        return newTx;
    }


    /**
     * Called by {@link TransactionImpl#forget forget} to forget about
     * the transaction once it has been commited/rolledback.
     * <p>
     * The transaction will no longer be available to {@link #findTransaction
     * findTransaction}. The transaction's association and global identifier
     * are forgotten as well as all thread associated with it.
     * Subsequent calls to {@link #findTransaction findTransaction}
     * and {@link #getControl getControl} will not be able to locate the
     * transaction.
     * <p>
     * If this is the last transaction in a terminating domain, the
     * domain will close some of its resources (e.g. journal) after this
     * method returns.
     *
     * @param tx The transaction to forget about
     */
    protected synchronized void forgetTransaction( TransactionImpl tx )
    {
        TransactionImpl entry;
        TransactionImpl next;
        Xid             xid;
        int             hashCode;
        int             index;
        Thread[]        threads;
        Thread          thread;
        ThreadContext   context;

        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        xid = tx._xid;
        hashCode = tx._hashCode;
        index = ( hashCode & 0x7FFFFFFF ) % _hashTable.length;
        entry = _hashTable[ index ];
        if ( entry == null )
            return;
        if ( entry._hashCode == hashCode && entry._xid.equals( xid ) )
            _hashTable[ index ] = entry._nextEntry;
        else {
            next = entry._nextEntry;
            while ( next != null ) {
                if ( next._hashCode == hashCode && next._xid.equals( xid ) ) {
                    entry._nextEntry = next._nextEntry;
                    entry = next;
                    break;
                }
                entry = next;
                next = next._nextEntry;
            }
            if ( next == null )
                return;
        }
        --_txCount;
        --_active;
        
        // If the domain is terminated, we close the transaction
        // journal at this point. Otherwise, we notify any blocking
        // thread that it's able to create a new transaction.
        if ( _state == TERMINATED ) {
            if ( _journal != null ) {
                try {
                    _journal.close();
                } catch ( SystemException except ) {
                    _category.error( "Error closing journal for transaction domain " + _domainName, except );
                }
            }
        } else 
            notifyAll();
    }


    /**
     * Called to set the timeout of all transactions created from this domain.
     *
     * @param timeout The new timeout in seconds, zero to restore the
     * default timeout
     */
    protected void setTransactionTimeout( int timeout )
    {
        if ( timeout <= 0 )
            timeout = DomainConfig.DEFAULT_TIMEOUT;
        else if ( timeout > DomainConfig.MAXIMUM_TIMEOUT )
            timeout = DomainConfig.MAXIMUM_TIMEOUT;
        _txTimeout = timeout;
    }


    /**
     * Called to change the timeout of the transaction and all the resources
     * enlisted with that transaction.
     *
     * @param tx The transaction
     * @param timeout The new timeout in seconds, zero to use the
     * default timeout for all new transactions.
     * @see TransactionManager#setTransactionTimeout setTransactionTimeout
     */
    protected synchronized void setTransactionTimeout( TransactionImpl tx, int timeout )
    {
        long  newTimeout;

        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );

        // For zero we use the default timeout for all new transactions.
        if ( timeout <= 0 )
            timeout = _txTimeout;
        else if ( timeout > DomainConfig.MAXIMUM_TIMEOUT )
            timeout = DomainConfig.MAXIMUM_TIMEOUT;

        // Synchronization is required to block background thread from
        // attempting to time out the transaction while we process it,
        // and so we can notify it of the next timeout.
        // Timeout is never set back.
        newTimeout = tx._started + timeout * 1000;
        if ( newTimeout > tx._timeout ) {
            tx._timeout = newTimeout;
            tx.internalSetTransactionTimeout( timeout );
            // If this transaction times out before any other transaction,
            // need to wakeup the background thread so it can update its
            // transaction timeout. Background thread is synchronizing on
            // hash table.
            if ( _nextTimeout == 0 || _nextTimeout > newTimeout ) {
                _nextTimeout = newTimeout;
                notifyAll();
            }
        }
    }


    /**
     * Called by {@link ControlImpl} to obtain the timeout on a
     * transaction for the purpose of the propagation context.
     *
     * @param tx The transaction
     * @return The transaction's timeout in seconds
     */
    protected int getTransactionTimeout( TransactionImpl tx )
    {
        return (int) ( tx._timeout - tx._started ) / 1000;
    }


    protected void notifyCompletion( TransactionImpl tx, int heuristic )
    {
        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        for ( int i = _interceptors.length ; i-- > 0 ; ) {
            try {
                _interceptors[ i ].completed( tx._xid, heuristic );
            } catch ( Throwable thrw ) {
                _category.error( "Interceptor " + _interceptors[ i ] + " reported error", thrw );
            }
        }
    }


    protected void notifyCommit( TransactionImpl tx )
        throws RollbackException
    {
        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        for ( int i = _interceptors.length ; i-- > 0 ; ) {
            try {
                _interceptors[ i ].commit( tx._xid );
            } catch ( RollbackException except ) {
                throw except;
            } catch ( Throwable thrw ) {
                _category.error( "Interceptor " + _interceptors[ i ] + " reported error", thrw );
            }
        }
        ++_accumCommitted;
        _accumTime += (int) ( Clock.clock() - tx._started );
    }


    protected void notifyRollback( TransactionImpl tx )
    {
        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        for ( int i = _interceptors.length ; i-- > 0 ; ) {
            try {
                _interceptors[ i ].rollback( tx._xid );
            } catch ( Throwable thrw ) {
                _category.error( "Interceptor " + _interceptors[ i ] + " reported error", thrw );
            }
        }
        ++_accumRolledback;
        _accumTime += ( Clock.clock() - tx._started );
    }


    protected synchronized Transaction[] listTransactions()
    {
        Transaction[]   txList;
        TransactionImpl entry;
        int             index = 0;
        
        txList = new Transaction[ _txCount ];
        for ( int i = _hashTable.length ; i-- > 0 ; ) {
            entry = _hashTable[ i ];
            while ( entry != null ) {
                txList[ index++ ] = entry;
                entry = entry._nextEntry;
            }
        }
        return txList;
    }


    protected synchronized void dumpTransactionList( PrintWriter writer )
    {
        TransactionImpl entry;
        
        if ( writer == null )
            throw new IllegalArgumentException( "Argument writer is null" );
        writer.println( "Transaction domain " + _domainName + " has " + _txCount + " transactions" );
        for ( int i = _hashTable.length ; i-- > 0 ; ) {
            entry = _hashTable[ i ];
            while ( entry != null ) {
                writer.println( "  Transaction " + entry._xid + " " + Util.getStatus( entry._status ) );
                writer.println( "  Started " + Util.fromClock( entry._started ) +
                                " time-out " + Util.fromClock( entry._timeout ) );
                entry = entry._nextEntry;
            }
        }
    }




    /**
     * Called to associate the transaction with the thread.
     *
     * @param tx The transaction
     * @param context The thread context
     * @param thread The thread
     * @return True if transaction enlisted in thread, false if failed
     */
    protected boolean enlistThread( TransactionImpl tx, ThreadContext context, Thread thread )
    {
        Xid       xid;

        if ( tx == null )
            throw new IllegalArgumentException( "Argument tx is null" );
        if ( context == null )
            throw new IllegalArgumentException( "Argument context is null" );
        if ( context._tx != null )
            delistThread( context, thread );
        xid = tx._xid;
        for ( int i = _interceptors.length ; i-- > 0 ; ) {
            try {
                _interceptors[ i ].resume( xid, thread );
            } catch ( InvalidTransactionException except ) {
                for ( ++i ; i < _interceptors.length ; ++i ) {
                    try {
                        _interceptors[ i ].suspend( xid, thread );
                    } catch ( Throwable thrw ) {
                        _category.error( "Interceptor " + _interceptors[ i ] + " reported error", thrw );
                    }
                }
                // Transaction will not be associated with this thread.
                return false;
            } catch ( Throwable thrw ) {
                _category.error( "Interceptor " + _interceptors[ i ] + " reported error", thrw );
            }
        }
        context._tx = tx;
        return true;
    }
    

    /**
     * Called to dissociatethe transaction from the thread.
     *
     * @param tx The transaction
     * @param context The thread context
     * @param thread The thread
     * @see enlistThread
     */
    protected void delistThread( ThreadContext context, Thread thread )
    {
        Xid             xid;
        TransactionImpl tx;

        if ( context == null )
            throw new IllegalArgumentException( "Argument context is null" );
        tx = context._tx;
        if ( tx == null )
            return;
        xid = tx._xid;
        for ( int i = _interceptors.length ; i-- > 0 ; ) {
            try {
                _interceptors[ i ].suspend( xid, thread );
            } catch ( Throwable thrw ) {
                _category.error( "Interceptor " + _interceptors[ i ] + " reported error", thrw );
            }
        }
        context._tx = null;
    }


    //-------------------------------------------------------------------------
    // Implementation details
    //-------------------------------------------------------------------------


    /**
     * Called to determine whether a new transaction can be created.
     * If we reached the maximum number of transactions allowed, this
     * method will block until we are able to create a new transaction,
     * or a timeout occured. This method must be called from a
     * synchronization block.
     *
     * @throws SystemException Timeout occured waiting to create a new
     * transaction
     */
    private void canCreateNew()
        throws SystemException
    {
        long clock;
        long timeout;

        if ( _maximum == 0 || _txCount < _maximum )
            return;
        clock = Clock.clock();
        timeout = clock + _waitNew;
        try {
            while ( clock < timeout ) {
                wait( timeout - clock );
                if ( _maximum == 0 || _txCount < _maximum )
                    return;
                clock = Clock.clock();
            }
        } catch ( InterruptedException except ) { }
        throw new SystemException( Messages.message( "tyrex.server.txCreateExceedsQuota" ) );
    }


    /**
     * Background thread that looks for transactions that have timed
     * out and terminates them. Will be running in a low priority for
     * as long as the server is active, monitoring the transaction
     * table and terminating threads in progress.
     * <p>
     * This thread is terminated by interrupting it. This thread
     * synchronizes on itself (thread instance) to be notified of
     * a changed in the next timeout.
     */
    public synchronized void run()
    {
        TransactionImpl entry;
        TransactionImpl next;
        long            nextTimeout;
        long            clock;

        while ( true ) {
            try {
                // We synchronize to be notified when the timeout of any
                // transaction changes on the hashtable object, and we will
                // need to remove records from the hashtable.
                // No transaction to time out, wait forever. Otherwise,
                // wait until the next transaction times out.
                clock = Clock.clock();
                while ( _nextTimeout == 0 || _nextTimeout > clock ) {
                    if ( _nextTimeout > clock )
                        wait( _nextTimeout - clock );
                    else
                        wait();
                    clock = Clock.clock();
                }
                    
                // If we have been notified that the domain is
                // terminating, we timeout all the transactions
                // in this domain.
                if ( _state == TERMINATED ) {
                    for ( int i = _hashTable.length ; i-- > 0 ; ) {
                        entry = _hashTable[ i ];
                        while ( entry != null ) {
                            next = entry._nextEntry;;
                            entry.timedOut();
                            entry = next;
                        }
                    }
                    DaemonMaster.removeDaemon( this );
                    return;
                }
                
                if ( _nextTimeout != 0 && _nextTimeout <= clock ) {
                    nextTimeout = 0;
                    for ( int i = _hashTable.length ; i-- > 0 ; ) {
                        entry = _hashTable[ i ];
                        while ( entry != null ) {
                            if ( entry._timeout <= clock ) {
                                entry.timedOut();
                            } else if ( nextTimeout == 0 || nextTimeout > entry._timeout )
                                nextTimeout = entry._timeout;
                            entry = entry._nextEntry;
                        }
                    }
                    _nextTimeout = nextTimeout;
                }
            } catch ( InterruptedException except ) {
                return;
            }
        }
    }


    //----------------------------------------------------------------------
    // Recovery
    //----------------------------------------------------------------------


    /**
     * Called to perform recovery. Runs through all heuristically complete
     * transactions loaded during the previous stages, and attempts to complete
     * them. When this method returns, all transactions have been resolved.
     * <p>
     * If a transaction has been prepared prior to failure, it will be prepared
     * a second time, allowing the resource managers to return a different vote.
     * If a transaction has been committed or rolledback prior to failure, they
     * will be committed or rolled back again.
     *
     * @param journal The transaction journal
     * @param resources An array of XA resources
     */
    private  void recover( Journal journal, XAResource[] resources )
    {
        TransactionImpl entry;
        TransactionImpl next;
        long            clock;
        int             commit = 0;
        int             rollback = 0;
        int             count;

        if ( Configuration.verbose )
            _category.info( "Initiating transaction recovery for domain " + _domainName );
        clock = Clock.clock();

        if ( journal != null ) {
            count = recoverJournal( journal );
            if ( count > 0 && Configuration.verbose )
                _category.info( "Loaded " + count + " records from transaction journal." );
        }
        if ( resources != null )
            recoverResources( resources );

        for ( int i = _hashTable.length ; i-- > 0 ; ) {
            entry = _hashTable[ i ];
            while ( entry != null ) {
                next = entry._nextEntry;
                if ( entry._status == Status.STATUS_PREPARED ) {
                    // The transaction was prepared. Need to markes it as active,
                    // so we can attempt to prepare it again, and potentially
                    // rollback or end with a heuristic decision.
                    entry._status = Status.STATUS_ACTIVE;
                    try {
                        entry.commit();
                        ++commit;
                    } catch ( Exception except ) {
                        recoveryError( new RecoveryException( except ) );
                        ++rollback;
                    }
                } else if ( entry._status == Status.STATUS_COMMITTED ) {
                    // The transaction has been marked as committed.
                    // We assume that all RM are capable of committing.
                    entry._status = Status.STATUS_PREPARED;
                    entry.internalCommit( entry.canUseOnePhaseCommit() );
                    try {
                        entry.forget( Heuristic.COMMIT );
                    } catch ( IllegalStateException except ) { }
                    ++commit;
                } else if ( entry._status == Status.STATUS_ROLLEDBACK ) {
                    // The transaction has been marked as rolledback.
                    // We assume that all RM are capable of committing.
                    entry._status = Status.STATUS_MARKED_ROLLBACK;
                    entry.internalRollback();
                    try {
                        entry.forget( Heuristic.ROLLBACK );
                    } catch ( IllegalStateException except ) { }
                    ++rollback;
                }
                entry = next;
            }
        }
        clock = Clock.clock() - clock;
        if ( Configuration.verbose ) {
            _category.info( "Transaction recovery for domain " + _domainName +
                            " completed in " + clock + " ms" );
            _category.info( "Transaction recovery for domain " + _domainName + ": " +
                            commit + " committed, " + rollback + " rolled back" );
        }
        reset();
        _active = _txCount;
    }


    /**
     * Called at the first stage of recovery to initiate recovery from
     * the transaction journal. This method must be called before any
     * resource managers are added.
     * <p>
     * All recoverable transaction records will be loaded and re-created
     * in memory, for completion during a subsequent call to {@link #recover
     * recover}.
     *
     * @param journal The transaction journal
     * @return The number of records read from the journal
     */
    private int recoverJournal( Journal journal )
    {
        Journal.RecoveredTransaction[] recovered;

        try {
            recovered = journal.recover();
        } catch ( SystemException except ) {
            recoveryError( new RecoveryException( except ) );
            return 0;
        }
        if ( recovered != null && recovered.length > 0 ) {
            for ( int i = recovered.length ; i-- > 0 ; ) {
                if ( recovered[ i ] != null ) {
                    try {
                        recoverRecord( recovered[ i ] );
                    } catch ( RecoveryException except ) {
                        recoveryError( except );
                    }
                }
            }
        }
        return recovered.length;
    }


    /**
     * Called by {@link #recoverJournal recoverJournal} for each transaction
     * recovery record. It will reconstruct a transcation in memory that has
     * the same heuristic decision, and is either prepared, committed or
     * rolledback. The transaction must be recovered by attempting to commit
     * or rollback.
     *
     * @param recovered The recovered transaction record
     * @throw RecoveryException The recovered transaction record is invalid
     */
    private void recoverRecord( Journal.RecoveredTransaction recovered )
        throws RecoveryException
    {
        TransactionImpl newTx;
        TransactionImpl entry;
        TransactionImpl next;
        Xid             xid;
        int             index;
        int             hashCode;

        xid = recovered.getXid();
        if ( xid == null )
            throw new RecoveryException( "Transaction recovery record missing Xid" );
        // Create a transaction with the specified properties and
        // add it to the transaction list.
        newTx = new TransactionImpl( (BaseXid) XidUtils.importXid( xid ), recovered.getHeuristic(), this );
        hashCode = newTx._hashCode;
        index = ( hashCode & 0x7FFFFFFF ) % _hashTable.length;
        entry = _hashTable[ index ];
        if ( entry == null )
            _hashTable[ index ] = newTx;
        else {
            next = entry._nextEntry;
            while ( next != null ) {
                if ( next._hashCode == hashCode && next._xid.equals( xid ) )
                    throw new RecoveryException( "A transaction with the identifier " + xid.toString() + " already exists" );
                entry = next;
                next = next._nextEntry;
            }
            entry._nextEntry = newTx;
        }
        ++_txCount;
        ++_active;
    }


    /**
     * Called at the second stage of recovery to register the resource managers.
     * The resource managers are registered using the XA resource interface.
     * The resource manager will be asked to return all recoverable transaction
     * branches. If a recoverable transaction branch was recorded in the journal,
     * it will be recovered during the last stage of recovery. Otherwise, the
     * resource manager will be asked to commit it.
     *
     * @param resources An array of XA resources
     */
    private void recoverResources( XAResource[] resources )
    {
        TransactionImpl tx;
        Xid             xid;
        Xid[]           xids = null;
        byte[]         uuid;

        for ( int i = resources.length ; i-- > 0 ; ) {
            try {
                xids = resources[ i ].recover( XAResource.TMSTARTRSCAN );
            } catch ( XAException except ) {
                recoveryError( new RecoveryException( "Resource manager " + resources[ i ] +
                                                      " failed to recover: " + Util.getXAException( except ) ) );
            }
            if ( xids != null )
                for ( int j = xids.length ; j-- > 0 ; ) {
                    xid = xids[ j ];
                    if ( xid != null ) {
                        // Obtain the transaction from the Xid (ignoring the
                        // branch qualifier).
                        xid = XidUtils.importXid( xid.getFormatId(), xid.getGlobalTransactionId(), null );
                        tx = (TransactionImpl) findTransaction( xid );
                        if ( tx == null ) {
                            // We don't recall this transaction from the journal,
                            // perhaps because we didn't manage it. To determine
                            // if we initiated this transaction:
                            // - The branch qualifier must contain a UUID for this
                            //   server (nested and OTS re-created transaction)
                            // - The branch qualifier is empty and the global
                            //   transaction identifier contains a UUID for this
                            //   server (top-level transaction)
                            uuid = xid.getBranchQualifier();
                            if ( ( uuid == null ) ||
                                 ( 0 == uuid.length ) ) {
                                uuid = xid.getGlobalTransactionId();
                                if ( uuid == null || ! UUID.isLocal( uuid ) ) {
                                    continue;
                                }
                            } else if ( ! UUID.isLocal( uuid ) ) {
                                continue;
                            }
                            // No recollection of the transaction, ask the resource
                            // manager to roll it back.
    
                            try {
                                resources[ i ].rollback( xid );
                            } catch ( XAException except ) {
                                recoveryError( new RecoveryException( Util.getXAException( except ) ) );
                                // Assuming a heuristic decision occured.
                                try {
                                    resources[ i ].forget( xid );
                                } catch ( Exception except2 ) {
                                }
                            }
                        } else {
                            // We have a transaction, we enlist the resource
                            // in that transaction so we can communicate the
                            // transaction completion.
                            tx.addRecovery( resources[ i ], xid );
                        }
                    }
                }
        }
    }


    /**
     * Records a recovery exception.
     *
     * @param except The recovery exception
     */
    private void recoveryError( RecoveryException except )
    {
        RecoveryException last;

        if ( except != null ) {
            last = _recoveryErrors;
            if ( last == null )
                _recoveryErrors = except;
            else {
                while ( last.getNextException() != null )
                    last = last.getNextException();
                last.setNextException( except );
            }
        }
    }


    //----------------------------------------------------
    // JTS support
    //----------------------------------------------------


    public synchronized void identifyORB( ORB orb, TSIdentification tsi, Properties prop )
    {
        try {
            if ( _orb != null )
                throw new org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified();
            //IllegalStateException( "Transaction domain already identified to an ORB" );
            _orb = orb;
            if ( tsi != null )
            {
            	tsi.identify_sender( _txFactory );
            	tsi.identify_receiver( _txFactory );
            }
        } catch ( Exception except ) {
            // The ORB might tell us it's already using some sender/reciever,
            // or any other error we are not interested in reporting back
            // to the caller (i.e. the ORB).
            _category.error( "Error occured while identifying ORB", except );
            _orb = null;
        }
    }


}
