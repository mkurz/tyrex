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

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;


/**
 * Class for creating and managing {@link XAResourceHelper} object.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
final class XAResourceHelperManager 
{


    /**
     * The default instance of XAResourceHelper
     */
    private static final XAResourceHelper _defaultHelper = new XAResourceHelper();


    /**
     * The Oracle XA resource helper
     */
    private static XAResourceHelper       _oracleHelper;

    /**
     * The Cloudscape XA resource helper
     */
    private static XAResourceHelper       _cloudscapeHelper;

    /**
     * The Informix XA resource helper
     */
    private static XAResourceHelper       _informixHelper;

    /**
     * The oracle resource class name
     */
    private static final String           _oracleXAResourceClassName = "oracle.jdbc.xa.client.OracleXAResource";

    /**
     * The name of the Oracle XAException class name.
     * The value is "oracle.jdbc.xa.OracleXAException".
     */
    static final String _oracleXAExceptionClassName = "oracle.jdbc.xa.OracleXAException";


    /**
     * The cloudscape resource class name
     */
    private static final String           _cloudscapeXAResourceClassName = "c8e.bm.a";

    /**
     * The informix resource class name
     */
    private static final String           _informixXAResourceClassName = "com.informix.jdbcx.IfxXAResource";

    /**
     * Private constructor
     */
    private XAResourceHelperManager()
    {
    }


    /**
     * Get the XAResourceHelperManager for the specified xa resource.
     *
     * @param xaResource the xa resource.
     * @return the XAResourceHelper
     */
    static XAResourceHelper getHelper( XAResource xaResource )
    {
        String className;

        className = xaResource.getClass().getName();

        if ( className.equals( _oracleXAResourceClassName ) ) {
            synchronized ( _oracleXAResourceClassName ) {
                if ( null == _oracleHelper )
                    _oracleHelper = new OracleXAResourceHelper();
                return _oracleHelper;
            }
        } 
        if ( className.equals( _cloudscapeXAResourceClassName ) ) {
            synchronized ( _cloudscapeXAResourceClassName ) {
                if ( null == _cloudscapeHelper )
                    _cloudscapeHelper = new CloudscapeXAResourceHelper();
                return _cloudscapeHelper;
            }
        }

        if ( className.equals( _informixXAResourceClassName ) ) {
            synchronized ( _informixXAResourceClassName ) {
                if ( null == _informixHelper )
                    _informixHelper = new InformixXAResourceHelper();
                return _informixHelper;
            }
        }
        
        return _defaultHelper;
    }

    /**
     * Get the XAResourceHelperManager for the specified XA exception.
     * It is assumed the only the method 
     * {@link XAResourceHelper.getXAErrorString()} will be called on the
     * returned object.
     *
     * @param xaException the XA exception.
     * @return the XAResourceHelper
     */
    static XAResourceHelper getHelper( XAException xaException )
    {
        if ( xaException.getClass().getName().equals( _oracleXAExceptionClassName ) ) {
            synchronized ( _oracleXAResourceClassName ) {
                if ( null == _oracleHelper )
                    _oracleHelper = new OracleXAResourceHelper();
                return _oracleHelper;
            }
        } 
        
        return _defaultHelper;
    }


}
