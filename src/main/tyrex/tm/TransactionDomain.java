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
 * $Id: TransactionDomain.java,v 1.17 2001/03/16 02:00:12 arkin Exp $
 */


package tyrex.tm;


import java.io.PrintWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import org.omg.CosTransactions.TransactionFactory;
import org.xml.sax.InputSource;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.mapping.Mapping;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.Xid;
import tyrex.tm.impl.TransactionDomainImpl;
import tyrex.tm.impl.DomainConfig;
import tyrex.resource.Resources;


/**
 * A transaction domain provides centralized management for transactions.
 * <p>
 * A transaction domain defines the policy for all transactions created
 * from that domain, such as default timeout, maximum number of open
 * transactions, IIOP support, and journaling. In addition, the domain
 * maintains resource managers such as JDBC data sources and JCA connectors.
 * <p>
 * The application server obtains a transaction manager or user transaction
 * object, and managed resources from the transaction domain.
 * <p>
 * Transaction domains are created from a domain configuration file.
 * For more information about domain configuration files, refer to the
 * relevant documentation and <tt>domain.xsd</tt>.
 * <p>
 * A newly created transaction domain is in the state {@link #READY}.
 * The {@link #recover recover} method must be called in order to make it
 * active ({@link #ACTIVE}). The domain can be deactivated by calling
 * {@link #terminate terminate}.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.17 $ $Date: 2001/03/16 02:00:12 $
 */
public abstract class TransactionDomain
{


    public static final int  READY      = 0;


    public static final int  RECOVERING = 1;


    public static final int  ACTIVE     = 2;


    public static final int  TERMINATED = 3;



    /**
     * A hash map of all transaction domains.
     */
    private static final HashMap            _domains = new HashMap();

    
    /**
     * Returns a transaction domain with the specified name. Returns null
     * if no transaction domain with that name was created.
     *
     * @param name The name of the transaction domain
     * @return The transaction domain, or if no such domain
     */
    public static synchronized TransactionDomain getDomain( String name )
    {
        if ( name == null || name.trim().length() == 0 )
            throw new IllegalArgumentException( "Argument name is null or an empty string" );
        return (TransactionDomain) _domains.get( name );
    }


    /**
     * Creates a new transaction domain from the specified domain
     * configuration file.
     * <p>
     * This method throws an exception if a transaction domain
     * with the same name already exists, or the transaction domain
     * could not be created.
     *
     * @param url URL for the transaction domain configuration file
     * @return A new transaction domain
     * @throw DomainConfigurationException An error occured while
     * attempting to create the domain
     */
    public static TransactionDomain createDomain( String url )
        throws DomainConfigurationException
    {
        if ( url == null )
            throw new IllegalArgumentException( "Argument url is null" );
        return createDomain( new InputSource( url ) );
    }


    /**
     * Creates a new transaction domain from the specified domain
     * configuration file.
     * <p>
     * This method throws an exception if a transaction domain
     * with the same name already exists, or the transaction domain
     * could not be created.
     *
     * @param steam Input stream for the transaction domain configuration file
     * @return A new transaction domain
     * @throw DomainConfigurationException An error occured while
     * attempting to create the domain
     */
    public static TransactionDomain createDomain( InputStream stream )
        throws DomainConfigurationException
    {
        if ( stream == null )
            throw new IllegalArgumentException( "Argument stream is null" );
        return createDomain( new InputSource( stream ) );
    }


    /**
     * Creates a new transaction domain from the specified domain
     * configuration file.
     * <p>
     * This method throws an exception if a transaction domain
     * with the same name already exists, or the transaction domain
     * could not be created.
     *
     * @param source SAX input source for the transaction domain
     * configuration file
     * @return A new transaction domain
     * @throw DomainConfigurationException An error occured while
     * attempting to create the domain
     */
    public synchronized static TransactionDomain createDomain( InputSource source )
        throws DomainConfigurationException
    {
        TransactionDomain domain;
        DomainConfig      config;
        Mapping           mapping;
        Unmarshaller      unmarshaller;

        if ( source == null )
            throw new IllegalArgumentException( "Argument source is null" );
        try {
            mapping = new Mapping();
            mapping.loadMapping( new InputSource( DomainConfig.class.getResourceAsStream( "mapping.xml" ) ) );
            unmarshaller = new Unmarshaller( (Class) null );
            unmarshaller.setMapping( mapping );
            config = (DomainConfig) unmarshaller.unmarshal( source );
        } catch ( Exception except ) {
            throw new DomainConfigurationException( except );
        }
        if ( _domains.containsKey( config.getName() ) )
            throw new DomainConfigurationException( "Transaction domain " + config.getName() + " already exists" );
        domain = config.getDomain();
        _domains.put( domain.getDomainName(), domain );
        return domain;
    }


    /**
     * Returns the transaction domain state.
     * <p>
     * The initial state for a transaction domain is {@link #READY}. The domain
     * transitions to {@link #ACTIVE} after recovery has completed by calling
     * {@link #recover recover}.
     * <p>
     * The domain transitions to {@link #TERMINATED} after it has been terminated
     * by calling {@link #terminate terminate}.
     *
     * @return The transaction domain state
     */
    public abstract int getState();


    /**
     * Called to initiate recovery. This method must be called before the
     * transaction domain is active and can be used to create new transactions.
     *
     * @throws RecoveryException A chain of errors reported during recovery
     */
    public abstract void recover()
        throws RecoveryException;


    /**
     * Returns a transaction based on the transaction identifier.
     * <p>
     * Returns the transaction object is the transaction is known to
     * any transaction domain. The transaction may be in the prepared
     * or complete state.
     *
     * @param xid The transaction identifier
     * @return The transaction, or null if no such transaction exists
     */
    public static Transaction getTransaction( Xid xid )
    {
        Iterator              iterator;
        TransactionDomainImpl domain;
        Transaction           tx;

        iterator = _domains.values().iterator();
        while ( iterator.hasNext() ) {
            domain = (TransactionDomainImpl) iterator.next();
            tx = domain.findTransaction( xid );
            if ( tx != null )
                return tx;
        }
        return null;
    }


    /**
     * Returns a transaction based on the transaction identifier.
     * <p>
     * Returns the transaction object is the transaction is known to
     * any transaction domain. The transaction may be in the prepared
     * or complete state.
     * <p>
     * The transaction identifier is a string obtained by calling
     * <tt>toString()</tt> on the transaction or <tt>Xid</tt> object.
     *
     * @param xid The transaction identifier
     * @return The transaction, or null if no such transaction exists
     */
    public static Transaction getTransaction( String xid )
    {
        Iterator              iterator;
        TransactionDomainImpl domain;
        Transaction           tx;

        iterator = _domains.values().iterator();
        while ( iterator.hasNext() ) {
            domain = (TransactionDomainImpl) iterator.next();
            tx = domain.findTransaction( xid );
            if ( tx != null )
                return tx;
        }
        return null;
    }


    /**
     * Returns a transaction manager for this transaction domain.
     * <p>
     * The transaction managed can be used to begin, commit and rollback
     * transactions in this domain only.
     * <p>
     * Calling this method multiple times will return the same instance
     * of the transaction manager.
     *
     * @return The transaction manager for this domain
     */
    public abstract TransactionManager getTransactionManager();


    /**
     * Returns a user transaction for this transaction domain.
     * <p>
     * The user transaction can be used to begin, commit and rollback
     * transactions in this domain only.
     * <p>
     * Calling this method multiple times will return the same instance
     * of the user transaction.
     *
     * @return The user transaction for this domain
     */
    public abstract UserTransaction getUserTransaction();


    /**
     * Returns an OTS transaction factory for this transaction domain.
     * <p>
     * The transaction factory can be used to create and re-create
     * OTS transactions in this domain only. It is also used to identify
     * the ORB by implementing <tt>TransactionService</tt>.
     * <p>
     * Calling this method multiple times will return the same instance
     * of the transaction factory.
     *
     * @return The transaction factory for this domain
     */
    public abstract TransactionFactory getTransactionFactory();


    /**
     * Adds a transaction interceptor to this transaction domain.
     * The interceptor will be notified of all transactional activities
     * within this domain.
     *
     * @param interceptor The transaction interceptor
     */
    public abstract void addInterceptor( TransactionInterceptor interceptor );


    /**
     * Removes a transaction interceptor to this transaction domain.
     *
     * @param interceptor The transaction interceptor
     */
    public abstract void removeInterceptor( TransactionInterceptor interceptor );


    /**
     * Terminates the transaction domain. After this method returns, the
     * transaction manager is no longer able to begin new transactions in
     * this domain.
     */
    public abstract void terminate();


    /**
     * Returns the transaction domain metrics.
     *
     * @return The transaction domain metrics
     */
    public abstract DomainMetrics getDomainMetrics();


    /**
     * Returns the transaction domain name.
     *
     * @return The transaction domain name
     */
    public abstract String getDomainName();


    /**
     * Returns resources installed for this transaction domain.
     * <p>
     * Initially the resource list is based on resources defined in
     * the domain configuration file. This method can be used to
     * add new resources or disable existing resources.
     *
     * @return Resources installed for this transaction domain
     */
    public abstract Resources getResources();


}
