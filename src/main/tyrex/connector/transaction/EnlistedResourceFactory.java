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
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 */


package tyrex.connector.transaction;

import tyrex.connector.ManagedConnection;
import tyrex.tm.EnlistedResource;

///////////////////////////////////////////////////////////////////////////////
// EnlistedResourceBuilder
///////////////////////////////////////////////////////////////////////////////

/**
 * This is a factory class that allows objects 
 * to be turned into an automatically enlisted resources.
 * <P>
 * An automatically enlisted resource is a resource that
 * enlists automatically in the transaction of the thread
 * of control when it is used.
 *
 * @author <a href="mohammed@exoffice.com">Riad Mohammed</a>
 */
final class EnlistedResourceFactory
{
    /**
     * No instances
     */
    private EnlistedResourceFactory()
    {
    }
    
    /**
     * Return true if the specified connection handle from
     * the specified managed connection may be able
     * to be turned into a {@link EnlistedResource}.
     * <P>
     * Even if this method returns true the {#build}
     * method may still return null. If this method returns
     * false the {#build} method MUST return null.
     *
     * @return true if the specified connection handle from
     * the specified managed connection may be able to
     * be turned into a {@link EnlistedResource}
     */
    public static boolean isEnlistedResource(Object connectionHandle, 
                                             ManagedConnection managedConnection)
    {
        return true;
    }


    /**
     * Return the {@link EnlistedResource} object created
     * from the specified arguments. If one cannot be created
     * return null.
     *
     * @param connectionHandle the connection handle from the 
     *      managed connection.
     * @param managedConnection the managed connection that created
     *      connection handle.
     * @param listener the enlisted resource listener that listens
     *      for transaction changes in the returned enlisted resource.
     * @return the {@link EnlistedResource} object created from the 
     * specified arguments. If one cannot be created return null.
     */
    public static EnlistedResource build(Object connectionHandle, 
                                         ManagedConnection managedConnection,
                                         EnlistedResourceListener listener)
    {
        return null;
    }
}
