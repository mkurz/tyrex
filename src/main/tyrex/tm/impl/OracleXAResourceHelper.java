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


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * This class describes various methods to help the transaction
 * manipulate XA resources from Oracle. This class has been
 * tested with Oracle 8.1.6, 8.1.7, 9.0.1. 
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public final class OracleXAResourceHelper
    extends XAResourceHelper
{


    /**
     * The name of the XID implementation class required by Oracle 8.1.6.
     * The value is "oracle.jdbc.xa.OracleXid".
     */
    public static final String XID_CLASS_NAME = "oracle.jdbc.xa.OracleXid";


    /**
     * The Oracle Xid constructor
     */
    private Constructor _xidConstructor;

    /**
     * True if the Oracle Xid constructor could not be loaded.
     */
    private boolean _failedXidConstructor;

    /**
     * The method for getErrorCode() from the OracleXAException
     */
    private Method _errorCodeMethod;

    /**
     * True if the getErrorCode() from the OracleXAException
     * could not be loaded
     */
    private boolean _failedErrorCodeMethod;


    /**
     * Default constructor
     */
    public OracleXAResourceHelper()
    {
        
    }


    /**
     * Create the xid for use with the XA resource from the specified xid.
     * <P>
     * The default implementation is to return the xid.
     *
     * @param xaResource The XAResource
     * @param xid The xid
     * @throws XAException An error occured obtaining the xid
     */
    public Xid getXid( XAResource xaResource, Xid xid )
        throws XAException
    {
        Object[] contructorArgs;

        if ( !loadOracleXidClass( xaResource ) ) {
            if ( ( null == xid.getBranchQualifier() ) || 
                 ( 0 == xid.getBranchQualifier().length ) ) {
                return new OracleXidWrapper( xid );
            }

            return xid;    
        }

        try {
            // populate the constructor args
            contructorArgs = new Object[3];
            contructorArgs[ 0 ] = new Integer( xid.getFormatId() );
            contructorArgs[ 1 ] = xid.getGlobalTransactionId();
            if ( ( null == xid.getBranchQualifier() ) || ( 0 == xid.getBranchQualifier().length ) ) {
                contructorArgs[ 2 ] = xid.getGlobalTransactionId();    
            }
            else {
                contructorArgs[ 2 ] = xid.getBranchQualifier();
            }
            return (Xid) _xidConstructor.newInstance( contructorArgs );
        } catch ( Throwable thrw ) {
            if ( thrw instanceof XAException )
                throw (XAException) thrw;
            // Unable to access constructor, assume default behavior.
            return xid;
        }
    }

    /**
     * Return true if shared xa resources must use 
     * different branches when enlisted in the transaction.The 
     * resource may still be treated as shared in that prepare/commit
     * is only called once on a single xa resource 
     * (@see #treatDifferentBranchesForSharedResourcesAsShared}).
     * The default implementation returns false.
     *
     * @return true if shared xa resources must use 
     * different branches when enlisted in the transaction. 
     * @see #treatDifferentBranchesForSharedResourcesAsShared
     */
    public boolean useDifferentBranchesForSharedResources() {
        return true;
    }

    /**
     * Return true if shared xa resources can be treated as shared 
     * even if they use different branches so that these xa resources
     * are not prepared/committed separately even if they don't have the same
     * xid. This method is only used if 
     * {@link #useDifferentBranchesForSharedResources} returns true.
     * The default implementation returns false.
     *
     * @return true if shared xa resources can be treated as shared 
     * even if they use different branches so that these xa resources
     * are not prepared separately even if they don't have the same
     * xid.
     * @see #useDifferentBranchesForSharedResources
     */
    public boolean treatDifferentBranchesForSharedResourcesAsShared() {
        return false; // true does not work in all cases
    }

    /**
     * Return the oracle error code as a string if the exception is 
     * {@link XAResourceHelperManager#_oracleXAExceptionClassName} 
     * otherwise return null.
     *
     * @param xaException the XAException
     * @return an 
     */
    public String getXAErrorString( XAException xaException ) {
        Class xaExceptionClass;

        xaExceptionClass = xaException.getClass();
        if ( xaExceptionClass.getName().equals( XAResourceHelperManager._oracleXAExceptionClassName ) ) {
            try {
                if (loadOracleXAExceptionErrorMethod(xaExceptionClass)) {
                    return " oracle error code: " + _errorCodeMethod.invoke( xaException, null );
                }
            }
            catch( Throwable thrw ) {
                
            }
        }

        return "";
    }


    /**
     * Return true if the oracle error method for the Oracle 
     * XAException is loaded. If this method returns true then
     * the variable {@link #_errorCodeMethod} will not be null and
     * the variable {@link #_failedErrorCodeMethod} will be false. 
     * If this method returns false then  
     * the variable {@link #_errorCodeMethod} will be null and
     * the variable {@link #_failedErrorCodeMethod} will be true. 
     *
     * @return true if the oracle error method for the Oracle
     *      XAException is loaded.
     */
    private synchronized boolean loadOracleXAExceptionErrorMethod(Class xaExceptionClass) {
        Method method = null;
        
        if ( ( null == _errorCodeMethod ) &&
             !_failedErrorCodeMethod ) {
            try {
                _errorCodeMethod = xaExceptionClass.getDeclaredMethod( "getOracleError", null );
                return true;
            } catch ( Throwable thrw ) {
                _failedErrorCodeMethod = true;
                return false;
            }
        }

        return null != _errorCodeMethod;
    }

    /**
     * Return true if the class {@link XID_CLASS_NAME} is
     * loaded. The variable {@link _xidConstructor} will be
     * set if the class {@link XID_CLASS_NAME} can be loaded.
     *
     * @param xaResource The XAResource
     * @return true if the class {@link XID_CLASS_NAME} can be
     *      loaded.
     */
    private synchronized boolean loadOracleXidClass( XAResource xaResource )
    {
        Constructor xidConstructor = null;
        Class       xidClass;
        Class       byteArrayClass;

        if ( ( null == _xidConstructor ) &&
             !_failedXidConstructor ) {
            try {
                xidClass = xaResource.getClass().getClassLoader().loadClass( XID_CLASS_NAME );
                byteArrayClass = Class.forName( "[B" );
                _xidConstructor = xidClass.getDeclaredConstructor( new Class[]{ Integer.TYPE, byteArrayClass, byteArrayClass } );
                return true;
            } catch ( Throwable thrw ) {
                // Oracle classes not found, this helper will assume the
                // default behavior for creating the xid.
                _failedXidConstructor = true;
                return false;
            }
        }

        return null != _xidConstructor;
    }

    /**
     * Helper Xid class to handle bug where Oracle 8.1.7 and 
     * Oracle 9.0.1 expects a valid branch qualifier. This class 
     * wont work for Oracle 8.1.6 which expects {@link #XID_CLASS_NAME}.
     */
    private static class OracleXidWrapper 
        implements Xid 
    {
        
        /**
         * The uderlying xid
         */
        private final Xid _xid;

        /**
         * Create the OracleXidWrapper
         *
         * @param xid the uderlying xid (required)
         */
        private OracleXidWrapper( Xid xid ) 
        {
            _xid = xid;
        }

        public byte[] getBranchQualifier() 
        {
            return getGlobalTransactionId();
        }
        
        public int getFormatId() 
        {
            return _xid.getFormatId();
        }
        
        public byte[] getGlobalTransactionId() 
        {
            return _xid.getGlobalTransactionId();
        }
    }
}
