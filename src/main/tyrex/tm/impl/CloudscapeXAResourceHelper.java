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
 * manipulate XA resources from Oracle. This class has been
 * tested with Oracle 8.1.6, 8.1.7, 9.0.1. For Oracle 8.1.6 the
 * oracle classes (classes12.zip) must be in the Tyrex classpath because
 * Oracle 8.1.6 requires the xid to be oracle.jdbc.xa.OracleXid.
 * For Oracle 8.1.7 and above the oracle classes (classes12.zip) may optionally
 * be in the Tyrex classpath.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
final class CloudscapeXAResourceHelper
    extends XAResourceHelper
{
    
    /**
     * Default constructor
     */
    public CloudscapeXAResourceHelper()
    {
        
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
        return true; // true does not work in all cases
    }
}
