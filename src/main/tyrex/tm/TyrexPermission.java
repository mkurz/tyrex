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
 * $Id: TyrexPermission.java,v 1.1 2000/08/28 19:01:52 mohammed Exp $
 */


package tyrex.tm;


import java.security.Permission;
import java.security.BasicPermission;


/**
 * Permissions required to control certain aspects of the transaction
 * server.
 * <p>
 * The following permissions must be used to perform certain
 * configuration, management and reporting operations:
 * <dl>
 * <dd>server.start<dt>Start the transaction server
 * <dd>server.shutdown<dt>Shutdown the transaction server
 * <dd>server.meter<dt>Run a meter against the transaction server
 * <dd>transaction.terminate<dt>Terminate an arbitrary transaction
 * <dd>transaction.list<dt>Obtain information about an arbitrary
 *   transaction or list all transactions in the server
 * <dd>transaction.manager<dt>Use {@link TransactionManager}
 * </dl>
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/08/28 19:01:52 $
 */
public final class TyrexPermission
    extends BasicPermission
{

    public TyrexPermission( String permission, String actions )
    {
	super( permission, actions );
    }


    public TyrexPermission( String permission )
    {
	super( permission );
    }


    public static class Server
    {

	/**
	 * Permission to start the transaction server.
	 */
	public static final TyrexPermission Start =
	    new TyrexPermission( "start" );

	/**
	 * Permission to shutdown the transaction server.
	 */
	public static final TyrexPermission Shutdown =
	    new TyrexPermission( "shutdown" );

	/**
	 * Permission to meter resource utilization from the server.
	 */
	public static final TyrexPermission Meter =
	    new TyrexPermission( "meter" );

    }


    public static class Transaction
    {

	/**
	 * Permission to terminate a transaction in progress.
	 */
	public static final TyrexPermission Terminate =
	    new TyrexPermission( "terminate" );

	/**
	 * Permission to retrieve a transaction or all transactions
	 * in use.
	 */
	public static final TyrexPermission List =
	    new TyrexPermission( "list" );

	/**
	 * Permission to use the transaction manager interface.
	 */
	public static final TyrexPermission Manager =
	    new TyrexPermission( "manager" );

    }


    public static class Naming
    {

	/**
	 * Permission to set the JNDI environment naming context.
	 */
	public static final TyrexPermission ENC =
	    new TyrexPermission( "enc" );

    }


}
