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
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.6 $ $Date: 2001/03/19 17:39:02 $
 */
public final class DomainConfig
{


    public static final int  NO_LIMIT = 0;


    /**
     * The default timeout for all transactions, specified in seconds.
     * This value is used unless the transaction domain, or transaction manager
     * are requested to use a different value. The default value is 120 seconds.
     */
    public static final int    DEFAULT_TIMEOUT = 120;


    private String              _name;


    private int                 _maximum = NO_LIMIT;


    private int                 _timeout = DEFAULT_TIMEOUT;


    private String              _journalFactory;


    private boolean             _autoRecover;


    private TransactionDomain   _txDomain;


    private Resources           _resources;


    public String getName()
    {
        return _name;
    }


    public void setName( String name )
    {
        _name = name;
    }


    public int getMaximum()
    {
        return _maximum;
    }


    public void setMaximum( int maximum )
    {
        if ( maximum < 0 )
            maximum = 0;
        _maximum = maximum;
    }


    public int getTimeout()
    {
        return _timeout;
    }


    public void setTimeout( int timeout )
    {
        if ( timeout <= 0 )
            timeout = DEFAULT_TIMEOUT;
        _timeout = timeout;
    }


    public String getJournalFactory()
    {
        return _journalFactory;
    }


    public void setJournalFactory( String factory )
    {
        _journalFactory = factory;
    }


    public boolean getAutoRecover()
    {
        return _autoRecover;
    }


    public void setAutoRecover( boolean autoRecover )
    {
        _autoRecover = autoRecover;
    }


    public synchronized TransactionDomain getDomain()
        throws DomainConfigurationException
    {
        if ( _txDomain == null )
            _txDomain = new TransactionDomainImpl( this );
        return _txDomain;
    }


    public Resources getResources()
    {
        return _resources;
    }


    public void setResources( Resources resources )
    {
        _resources = resources;
    }


}
