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
 * $Id: NamingPermission.java,v 1.1 2000/04/11 20:25:13 arkin Exp $
 */


package tyrex.naming;


import java.security.BasicPermission;


/**
 * Permissions required to read/write the in-memory namespace
 * and set/unset the environment naming context for a thread.
 * <p>
 * The following permissions are defined:
 * <dl>
 *  <dd>tyrex.naming.NamingPermission "shared"</dd>
 *  <dt>Access (read/write) to the shared in-memory namespace</dt>
 *  <dd>tyrex.naming.NamingPermission "enc"</dd>
 *  <dt>Access (read/write) to the environment naming context
 *     and ability to set/unset it for the current thread</dt>
 * </dl>
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/04/11 20:25:13 $
 */
public final class NamingPermission
    extends BasicPermission
{


    /**
     * Permission to set the JNDI environment naming context.
     */
    public static final NamingPermission ENC =
        new NamingPermission( "enc" );


    /**
     * Permission to access the shared in-memory namespace.
     */
    public static final NamingPermission Shared =
        new NamingPermission( "shared" );



    public NamingPermission( String permission, String actions )
    {
	super( permission, actions );
    }


    public NamingPermission( String permission )
    {
	super( permission );
    }


}
