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
import javax.transaction.xa.Xid;


/**
 * This class describes various methods to help the transaction manipulate
 * XA resources from database vendors that don't comply fully with the XA
 * specification.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public class XAResourceHelper 
{


    /**
     * Default constructor
     */
    public XAResourceHelper()
    {
    }


    /**
     * Create the xid for use with the XA resource from the specified xid.
     * <P>
     * The default implementation is to return the xid.
     *
     * @param xid The xid
     * @throws XAException An error occured obtaining the xid
     */
    public Xid getXid( Xid xid )
        throws XAException
    {
        return xid;
    }


    /**
     * End work performed by the specified xa resource that has previously
     * been suspended.
     * <P>
     * The default implementation is to call {@link javax.transaction.xa.XAResource#end
     * end} with the flag {@link javax.transaction.xa.XAResource#TMSUCCESS}
     *
     * @param xaResource The XA resource
     * @throws XAException An error occured while ending the work performed
     * by the XA resource
     */
    public void endSuspended( XAResource xaResource, Xid xid )
        throws XAException
    {
        xaResource.end( xid, XAResource.TMSUCCESS );
    }


}
