/**
 * Redistribution and use of this software and associated
 * documentation ("Software"), with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright statements
 *    and notices.  Redistributions must also contain a copy of this
 *    document.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio Inc.  For written permission, please
 *    contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Inc. Exolab is a registered trademark of
 *    Intalio Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL INTALIO OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * Copyright 2000 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: Initializer.java,v 1.6 2001/03/15 22:59:52 jdaniel Exp $
 *
 * Date         Author  Changes
 */

package tyrex.corba;

/**
 * Tyrex OTS Initializer. This code is reused from the OpenORB OTS developed by Jerome & Marina DANIEL.
 * 
 * @author <a href="mailto:mdaniel@intalio.com">Marina Daniel &lt;mdaniel@intalio.com&gt;</a>
 */
public class Initializer extends org.omg.CORBA.LocalObject implements org.omg.PortableInterceptor.ORBInitializer
{
	/**
	 * Transactional slot id
	 */
	private int t_slot;
	
	/**
	 * Pre initialization
	 */
	public void pre_init(org.omg.PortableInterceptor.ORBInitInfo info)
	{
		t_slot = info.allocate_slot_id();
		
		tyrex.corba.ClientInterceptor clientInterceptor = new tyrex.corba.ClientInterceptor(info, t_slot);
		tyrex.corba.ServerInterceptor serverInterceptor = new tyrex.corba.ServerInterceptor(info, t_slot);
		
		try
		{
			info.add_client_request_interceptor(clientInterceptor);
			info.add_server_request_interceptor(serverInterceptor);
		}
		catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName dn)
		{
			exception("Initializer", "Duplicate name when adding the interceptor", dn);
		}				
	}

	/**
	 * Post initialization
	 */
	public void post_init(org.omg.PortableInterceptor.ORBInitInfo info)
	{
		try
		{
			org.omg.CORBA.Object obj = info.resolve_initial_references("TransactionService");
						
			org.omg.CosTransactions.TransactionFactory _tfactory = org.omg.CosTransactions.TransactionFactoryHelper.narrow( obj );
				
			tyrex.corba.Current current = new tyrex.corba.Current(_tfactory, info, t_slot);
			
			info.register_initial_reference("TransactionCurrent", current);			
	        }
		catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName ex ) 
		{
			fatal("Initializer", "Unable to resolve TransactionService");
		}
					
	}
                
        /**
         * Displays a trace and throw a INTERNAL exception...
         */
        public void fatal( String from, String msg )
        {
            tyrex.util.Logger.ots.warn(from + ": " + msg );
            throw new org.omg.CORBA.INTERNAL(msg);
        }
        
        /**
         * Displays a trace and throw a INTERNAL exception...
         */
        public void exception( String from, String msg, java.lang.Exception ex )
        {
            tyrex.util.Logger.ots.warn("EXCEPTION => " + from + ": " + msg );            
        }
}

