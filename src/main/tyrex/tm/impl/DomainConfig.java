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
 */


package tyrex.tm.impl;


import tyrex.tm.TransactionDomain;
import tyrex.tm.DomainConfigurationException;
import tyrex.tm.impl.TransactionDomainImpl;
import tyrex.resource.Resources;


/**
 * Domain configuration object read from the domain configuration
 * file and used to construct a new transaction domain.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.8 $ $Date: 2001/03/21 04:53:08 $
 */
public final class DomainConfig
{


    /**
     * Value indicating no limit on the maximum number of concurrent
     * top-level transactions allowed. This value is zero.
     */
    public static final int  NO_LIMIT = 0;


    /**
     * The default timeout for all transactions. This value is used unless
     * the transaction transaction manager has been requested to use a
     * different value. The default value is 120 seconds.
     */
    public static final int    DEFAULT_TIMEOUT = 120;


    /**
     * The default timeout waiting to begin a new transaction when maximum
     * limit exceeded. The default value is 5 seconds.
     */
    public static final int    DEFAULT_WAIT_NEW = 5;


    /**
     * The maximum possible timeout for a transaction. This is ten minutes,
     * specified as seconds.
     */
    public static final int  MAXIMUM_TIMEOUT = 10 * 60;


    /**
     * The name of this transaction domain.
     */
    private String              _name;


    /**
     * Maximum number of concurrent top-level transactions supported.
     * The default is zero (no limit).
     */
    private int                 _maximum = NO_LIMIT;


    /**
     * The default transaction timeout in seconds.
     */
    private int                 _timeout = DEFAULT_TIMEOUT;


    /**
     * The time to wait for a new transaction when limit exceeded.
     */
    private int                 _waitNew = DEFAULT_WAIT_NEW;


    /**
     * Name of the transaction journal factory.
     */
    private String              _journalFactory;


    /**
     * The transaction domain created from this configuration after
     * a successful return from {@link #getDomain}.
     */
    private TransactionDomain   _txDomain;


    /**
     * Sets the resources list associated with this transaction domain.
     */
    private Resources           _resources;


    /**
     * Return the name of the transaction domain.
     *
     * @return Name of transaction domain
     */
    public String getName()
    {
        return _name;
    }


    /**
     * Sets the name of the transaction domain.
     *
     * @param name Name of transaction domain
     */
    public void setName( String name )
    {
        _name = name;
    }


    /**
     * Returns the maximum number of concurrent top-level transactions
     * supported. The value {@link #NO_LIMIT} indicates unlimited number
     * of transactions.
     *
     * @return The maximum number of concurrent top-level transactions
     */
    public int getMaximum()
    {
        return _maximum;
    }


    /**
     * Sets the maximum number of concurrent top-level transactions
     * supported. The value {@link #NO_LIMIT} indicates unlimited number
     * of transactions.
     *
     * @param maximum The maximum number of concurrent top-level transactions
     */
    public void setMaximum( int maximum )
    {
        if ( maximum < 0 )
            maximum = 0;
        _maximum = maximum;
    }


    /**
     * Returns the default transaction timeout in seconds. This value applies
     * to all new transactions created in this domain, unless overridden by
     * the transaction manager.
     * <p> 
     * <p>
     * The actual value is kept in the range one to {@link #MAXIMUM_TIMEOUT}.
     * The value zero is understood to be the default value, or {@link
     * #DEFAULT_TIMEOUT}.
     *
     * @return The default transaction timeout in seconds
     */
    public int getTimeout()
    {
        return _timeout;
    }


    /**
     * Returns the default transaction timeout in seconds. This value applies
     * to all new transactions created in this domain, unless overridden by
     * the transaction manager.
     * <p>
     * The actual value is kept in the range one to {@link #MAXIMUM_TIMEOUT}.
     * The value zero is understood to be the default value, or {@link
     * #DEFAULT_TIMEOUT}.
     *
     * @param timeout The default transaction timeout in seconds
     */
    public void setTimeout( int timeout )
    {
        if ( timeout <= 0 )
            timeout = DEFAULT_TIMEOUT;
        else if ( timeout > MAXIMUM_TIMEOUT )
            timeout = MAXIMUM_TIMEOUT;
        _timeout = timeout;
    }


    /**
     * Returns the time to wait for a new transaction when limit exceeded,
     * specified in seconds.
     * <p>
     * When the maximum number of concurrent transactions have exceeded,
     * any attempt to create a new transaction will block until a new
     * transaction can be created or this timeout has been reached.
     *
     * @return The time to wait to begin a new transaction when limit
     * exceeded, specified in seconds
     */
    public int getWaitNew()
    {
        return _waitNew;
    }


    /**
     * Sets the time to wait for a new transaction when limit exceeded,
     * specified in seconds.
     * <p>
     * When the maximum number of concurrent transactions have exceeded,
     * any attempt to create a new transaction will block until a new
     * transaction can be created or this timeout has been reached.
     *
     * @param timeout The time to wait to begin a new transaction when limit
     * exceeded, specified in seconds
     */
    public void setWaitNew( int timeout )
    {
        if ( timeout < 0 )
            timeout = 0;
        _waitNew = timeout;
    }


    /**
     * Returns the class name of the transaction journal factory.
     *
     * @return The class name of the transaction journal factory
     */
    public String getJournalFactory()
    {
        return _journalFactory;
    }


    /**
     * Sets the class name of the transaction journal factory.
     * If this value is not empty, it must specify a class that
     * implements {@link JournalFactory}, and can be used to open
     * a new transaction journal for use by a transaction domain.
     *
     * @param factory The class name of the transaction journal factory
     */
    public void setJournalFactory( String factory )
    {
        _journalFactory = factory;
    }


    /**
     * Returns a transaction domain based on this configuration.
     * This method attempts to create a new transaction domain using
     * the configuration. The same transaction domain will be returned
     * if this method is called multiple times.
     *
     * @return A transaction domain based on this configuration
     * @throws DomainConfigurationException The transaction domain
     * configuration is invalid
     */
    public synchronized TransactionDomain getDomain()
        throws DomainConfigurationException
    {
        if ( _txDomain == null )
            _txDomain = new TransactionDomainImpl( this );
        return _txDomain;
    }


    /**
     * Returns the resources list associated with this transaction domain.
     *
     * @return The resources list
     */
    public Resources getResources()
    {
        return _resources;
    }


    /**
     * Sets the resources list associated with this transaction domain.
     *
     * @param resources The resources list
     */
    public void setResources( Resources resources )
    {
        _resources = resources;
    }


}
