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
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;


/**
 * This class describes various methods to help the transaction
 * manipulate XA resources from Oracle.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public final class OracleXAResourceHelper
    extends XAResourceHelper
{


    /**
     * The oracle Xid constructor
     */
    private final Constructor _xidConstructor;


    /**
     * The array used in the new instance method
     * call for the Xid constructor
     *
     * @see #xidConstructor
     */
    private final Object[]    _contructorArgs;


    /**
     * Default constructor
     */
    public OracleXAResourceHelper()
    {
        Constructor xidConstructor = null;
        Class       xidClass;
        Class       byteArrayClass;

        try {
            xidClass = Class.forName( "oracle.jdbc.xa.OracleXid" );
            byteArrayClass = Class.forName( "[B" );
            xidConstructor = xidClass.getDeclaredConstructor( new Class[]{ Integer.TYPE, byteArrayClass, byteArrayClass } );
        } catch ( Exception except ) {
            // Oracle classes not found, this helper will assume the
            // default behavior.
        }
        
        _xidConstructor = xidConstructor;
        if ( null == xidConstructor )
            _contructorArgs = null;        
        else
            _contructorArgs = new Object[ 3 ];
    }


    public Xid getXid( Xid xid )
        throws XAException
    {
        if ( null == _xidConstructor )
            return xid;    

        try {
            // populate the constructor args
            _contructorArgs[ 0 ] = new Integer( xid.getFormatId() );
            _contructorArgs[ 1 ] = xid.getGlobalTransactionId();
            _contructorArgs[ 2 ] = xid.getBranchQualifier();
            return (Xid) _xidConstructor.newInstance( _contructorArgs );
        } catch ( Throwable thrw ) {
            if ( thrw instanceof XAException )
                throw (XAException) thrw;
            // Unable to access constructor, assume default behavior.
            return xid;
        }
    }


    public void endSuspended( XAResource xaResource, Xid xid )
        throws XAException
    {
        xaResource.start( xid, XAResource.TMRESUME );
        xaResource.end( xid, XAResource.TMSUCCESS );
    }


}
