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
 * Copyright 2000 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: EnlistedResource.java,v 1.2 2000/09/08 23:06:13 mohammed Exp $
 */


package tyrex.tm;

import javax.transaction.Transaction;

/**
 * Callback interface allowing a resource that automatically enlists
 * itself to learn when it has been delisted. Certain resources (e.g.
 * JDBC connections) can be used that have been opened in a previous
 * invocation under a different thread. Not having created them, we
 * have no way of enlisting them unless they automatically enlist as
 * they are being used. This call back interface will notify them when
 * they are delisted, to they may re-enlist as necessary.
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/09/08 23:06:13 $
 */
public interface EnlistedResource
{


    /**
     * Called by the transaction manager to notify the resource
     * that it had been delisted from the transaction after the
     * transaction has either committed, rolledback or been suspended.
     *
     */
    public void delisted();


    /**
     * Called by the transaction manager to notify the resource
     * that it had been enlisted in the specified transaction.
     *
     * @param tx the transaction that the resource has been 
     *      enlsited in.
     */
    public void enlisted( Transaction tx );


    /**
     * Return true if the resource is enlisted in a transaction.
     * Return false otherwise.
     *
     * @return true if the resource is enlisted in a transaction.
     */
    public boolean isEnlisted();
}
