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
 */

package tyrex.corba;

/**
 * This is the current interface implementation. This code is extracted from the OpenORB OTS source code.
 * 
 * @author <a href="mailto:jdaniel@intalio.com">Jerome Daniel &lt;daniel@intalio.com&gt;</a>
 * @version $Revision: 1.9 $ $Date: 2004/04/21 03:52:27 $ 
 */
public class Current extends org.omg.CORBA.LocalObject implements org.omg.CosTransactions.Current				     
{
	/**
	 * Reference to the transaction factory
	 */
	private org.omg.CosTransactions.TransactionFactory _tfactory;
	
	/**
	 * Transaction time out
	 */
	private int _time_out;
	
	/**
	 * OpenORB Initializer Info 
	 */
	private org.omg.PortableInterceptor.ORBInitInfo _info;
	
	/**
	 * Transactional slot ID
	 */
	private int t_slot;
	
        /**
         * Propagation context stack
         */
	private static java.util.Hashtable _pctx_stacks = new java.util.Hashtable();
        
	/**
	 * Constructor
	 */
	public Current(org.omg.CosTransactions.TransactionFactory factory, org.omg.PortableInterceptor.ORBInitInfo info, int t_slot )
	{
		_time_out = 0;
		_tfactory = factory;
		_info = info;
		this.t_slot = t_slot;				
	}
	
	// -----------------------------------------------------------------------------------------
	//
	// Current interface implementation
	//
	// -----------------------------------------------------------------------------------------
	
	/**
	 * A new transaction is created. The transaction context of the client thread is modified so
	 * that the thread is associated with the new transaction. If the client thread is currently
	 * associated with a transaction, the new transaction is a subtransaction of that
	 * transaction. Otherwise, the new transaction is a top-level transaction.
	 * 
	 * @exception  SubtransactionsUnavailable The SubtransactionsUnavailable exception is raised 
	 * if the client thread already has an associated transaction and the Transaction 
	 * Service implementation does not support nested transactions.
	 */
	public void begin()
		throws org.omg.CosTransactions.SubtransactionsUnavailable
	{
		print("Current", "begin");
                org.omg.CosTransactions.PropagationContext pctx = null;
		try
		{
			pctx = getPropagationContext();
			
                        // As a previous propagation context has been found, it means that we are
                        // going to create a sub transaction. Before, we need to save the current
                        // propagation context.
                        
                        push_txcontext( pctx );
		}
		catch (org.omg.CORBA.MARSHAL m)
		{
			try 
			{
				org.omg.CosTransactions.Control control = factory().create(_time_out);
				
				pctx = control.get_coordinator().get_txcontext();
				
				org.omg.CORBA.Any pctx_any = org.omg.CORBA.ORB.init().create_any();
				org.omg.CosTransactions.PropagationContextHelper.insert(pctx_any, pctx);
				org.openorb.PI.CurrentImpl piCurrent = (org.openorb.PI.CurrentImpl)_info.resolve_initial_references("PICurrent");
				piCurrent.set_slot(t_slot, pctx_any);
			}
			catch (org.omg.CosTransactions.Unavailable un) 
			{
				fatal("Current", "Transaction unavailable");
			}
			catch (org.omg.PortableInterceptor.InvalidSlot ins) 
			{
				fatal("Current", "Invalid slot");
			}
			catch (org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName in ) 
			{ 
				fatal("Current", "Unable to resolve PICurrent");
			}
                        finally
                        {
                                return;
                        }
		}
                
                // here is the case where we have to create the sub transaction                                
                
                try
                {
                    org.omg.CosTransactions.Control control = pctx.current.coord.create_subtransaction();
                    
                    pctx = control.get_coordinator().get_txcontext();
				
                    org.omg.CORBA.Any pctx_any = org.omg.CORBA.ORB.init().create_any();
                    org.omg.CosTransactions.PropagationContextHelper.insert(pctx_any, pctx);
                    org.openorb.PI.CurrentImpl piCurrent = (org.openorb.PI.CurrentImpl)_info.resolve_initial_references("PICurrent");
                    piCurrent.set_slot(t_slot, pctx_any);
                }
                catch (org.omg.PortableInterceptor.InvalidSlot ins) 
                {
                    fatal("Current", "Invalid slot");
                }
                catch (org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName in ) 
                { 
                    fatal("Current", "Unable to resolve PICurrent");
                }
                catch ( java.lang.Exception ex )
                {
                    ex.printStackTrace();
                    fatal("Current", "Unexpected exception");
                }
                
	}

	/**
	 * If there is no transaction associated with the client thread, the NoTransaction exception
	 * is raised. If the client thread does not have permission to commit the transaction, the
	 * standard exception NO_PERMISSION is raised. (The commit operation may be restricted
	 * to the transaction originator in some implementations.)
	 * 
	 * Otherwise, the transaction associated with the client thread is completed. The effect of
	 * this request is equivalent to performing the commit operation on the corresponding
	 * Terminator object.
	 * 
	 * The client thread transaction context is modified as follows: If the transaction was
	 * begun by a thread (invoking begin) in the same execution environment, then the
	 * threads transaction context is restored to its state prior to the begin request. Otherwise,
	 * the threads transaction context is set to null.
	 */
	public void commit(boolean report_heuristics)
		throws org.omg.CosTransactions.NoTransaction, org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
	{
		print("Current", "commit");
		try
		{
			org.omg.CosTransactions.PropagationContext pctx = getPropagationContext();
			pctx.current.term.commit( report_heuristics );						
		}
		catch (org.omg.CORBA.MARSHAL m) 
		{
			throw new org.omg.CosTransactions.NoTransaction();
		}
		finally 
		{
                    // Gets the previous propagation context if the current transaction was
                    // a sub transaction

                    pop_txcontext();    		
		}
	}

	/**
	 * If there is no transaction associated with the client thread, the NoTransaction exception
	 * is raised. If the client thread does not have permission to rollback the transaction, the
	 * standard exception NO_PERMISSION is raised. (The rollback operation may be restricted
	 * to the transaction originator in some implementations; however, the rollback_only
	 * operation, described below, is available to all transaction participants.)
	 * 
	 * Otherwise, the transaction associated with the client thread is rolled back. The effect of
	 * this request is equivalent to performing the rollback operation on the corresponding
	 * Terminator object.
	 * 
	 * The client thread transaction context is modified as follows: If the transaction was
	 * begun by a thread (invoking begin) in the same execution environment, then the
	 * thread?s transaction context is restored to its state prior to the begin request. Otherwise,
	 * the thread?s transaction context is set to null.		 
	 */
	public void rollback()
		throws org.omg.CosTransactions.NoTransaction
	{
		print("Current", "rollback");
		try
		{
			org.omg.CosTransactions.PropagationContext pctx = getPropagationContext();
			pctx.current.term.rollback();
			
			//forgetXABag();
		}
		catch (org.omg.CORBA.MARSHAL m) 
		{
			throw new org.omg.CosTransactions.NoTransaction();
		}
		finally 
		{
                    // Gets the previous propagation context if the current transaction was
                    // a sub transaction

                    pop_txcontext();    
		}
	}

	/**
	 * If there is no transaction associated with the client thread, the NoTransaction exception
	 * is raised. Otherwise, the transaction associated with the client thread is modified so
	 * that the only possible outcome is to rollback the transaction. The effect of this request
	 * is equivalent to performing the rollback_only operation on the corresponding
	 * Coordinator object.
	 */
	public void rollback_only()
		throws org.omg.CosTransactions.NoTransaction
	{
		print("Current", "rollback_only");
		try
		{
			org.omg.CosTransactions.PropagationContext pctx = getPropagationContext();
			pctx.current.coord.rollback_only();
		}
		catch (org.omg.CORBA.MARSHAL m) 
		{
			throw new org.omg.CosTransactions.NoTransaction();
		}
		catch ( org.omg.CosTransactions.Inactive ex ){ }		
	}

	/**
	 * If there is no transaction associated with the client thread, the StatusNoTransaction value
	 * is returned. Otherwise, this operation returns the status of the transaction associated
	 * with the client thread. The effect of this request is equivalent to performing the
	 * get_status operation on the corresponding Coordinator object.
	 */
	public org.omg.CosTransactions.Status get_status()
	{
		try
		{
			org.omg.CosTransactions.PropagationContext pctx = getPropagationContext();
			return pctx.current.coord.get_status();
		}
		catch (org.omg.CORBA.MARSHAL m) 
		{
			return org.omg.CosTransactions.Status.StatusNoTransaction;
		}		
	}

	/**
	 * If there is no transaction associated with the client thread, an empty string is returned.
	 * Otherwise, this operation returns a printable string describing the transaction. The
	 * returned string is intended to support debugging. The effect of this request is
	 * equivalent to performing the get_transaction_name operation on the corresponding
	 * Coordinator object.
	 */
	public java.lang.String get_transaction_name()
	{
		try
		{
			org.omg.CosTransactions.PropagationContext pctx = getPropagationContext();
			return pctx.current.coord.get_transaction_name();
		}
		catch (org.omg.CORBA.MARSHAL m) 
		{
			return "";
		}
	}

	/**
	 * This operation modifies a state variable associated with the target object that affects
	 * the time-out period associated with top-level transactions created by subsequent
	 * invocations of the begin operation. If the parameter has a nonzero value n, then top-level
	 * transactions created by subsequent invocations of begin will be subject to being
	 * rolled back if they do not complete before n seconds after their creation. If the
	 * parameter is zero, then no application specified time-out is established.
	 */
	public void set_timeout(int seconds)
	{
		_time_out = seconds;
	}

	/**
	 * If the client thread is not associated with a transaction, a null object reference is
	 * returned. Otherwise, a Control object is returned that represents the transaction context
	 * currently associated with the client thread. This object can be given to the resume
	 * operation to reestablish this context in the same thread or a different thread. The scope
	 * within which this object is valid is implementation dependent; at a minimum, it must
	 * be usable by the client thread. This operation is not dependent on the state of the
	 * transaction; in particular, it does not raise the TRANSACTION_ROLLEDBACK
	 * exception.
	 */
	public org.omg.CosTransactions.Control get_control()
	{
		try
		{
			org.omg.CosTransactions.PropagationContext pctx = getPropagationContext();
			
			return new tyrex.corba.PseudoControl( pctx.current.coord, pctx.current.term );
		}
		catch ( org.omg.CORBA.MARSHAL ex )
		{
			return null;
		}
	}

	/**
	 * If the client thread is not associated with a transaction, a null object reference is
	 * returned. Otherwise, an object is returned that represents the transaction context
	 * currently associated with the client thread. This object can be given to the resume
	 * operation to reestablish this context in the same thread or a different thread. The scope
	 * within which this object is valid is implementation dependent; at a minimum, it must
	 * be usable by the client thread. In addition, the client thread becomes associated with no
	 * transaction. This operation is not dependent on the state of the transaction; in
	 * particular, it does not raise the TRANSACTION_ROLLEDBACK exception.
	 */
	public org.omg.CosTransactions.Control suspend()
	{
		org.omg.CosTransactions.Control ctrl = get_control();
                
                try
                {
                        org.openorb.PI.CurrentImpl piCurrent = (org.openorb.PI.CurrentImpl)_info.resolve_initial_references("PICurrent");
                        piCurrent.set_slot(t_slot, org.omg.CORBA.ORB.init().create_any());
                }
                catch (org.omg.PortableInterceptor.InvalidSlot ins) 
                {
                        fatal("Current", "Invalid slot");
                }
                catch (org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName in ) 
                { 
                        fatal("Current", "Unable to resolve PICurrent");
                }
                
                return ctrl;
	}

	/**
	 * If the parameter is a null object reference, the client thread becomes associated with no
	 * transaction. Otherwise, if the parameter is valid in the current execution environment,
	 * the client thread becomes associated with that transaction (in place of any previous
	 * transaction). Otherwise, the InvalidControl exception is raised.
	 * 
	 * This operation is not dependent on the state of the transaction; in particular, 
	 * it does not raise the TRANSACTION_ROLLEDBACK exception.
	 */
	public void resume(org.omg.CosTransactions.Control which)
		throws org.omg.CosTransactions.InvalidControl
	{
		print("Current", "resume");
		if ( which == null )
		{
			try
			{
				org.openorb.PI.CurrentImpl piCurrent = (org.openorb.PI.CurrentImpl)_info.resolve_initial_references("PICurrent");
				piCurrent.set_slot(t_slot, org.omg.CORBA.ORB.init().create_any());
			}
			catch (org.omg.PortableInterceptor.InvalidSlot ins) 
			{
				fatal("Current", "Invalid slot");
			}
			catch (org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName in ) 
			{ 
				fatal("Current", "Unable to resolve PICurrent");
			}
		}
		else
		{		
			try
			{
				org.omg.CosTransactions.PropagationContext pctx = which.get_coordinator().get_txcontext();
				
				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
				
				org.omg.CosTransactions.PropagationContextHelper.insert( any, pctx );
				
				org.openorb.PI.CurrentImpl piCurrent = (org.openorb.PI.CurrentImpl)_info.resolve_initial_references("PICurrent");
				piCurrent.set_slot(t_slot, any);
			}
			catch ( java.lang.Exception ex )
			{				
				throw new org.omg.CosTransactions.InvalidControl();
			}
		}
	}
	
	// -----------------------------------------------------------------------------------------
	//
	// Implementation specific operations
	//
	// -----------------------------------------------------------------------------------------

	/**
	 * This operation is used to return the Coordinator ior
	 */
	public String get_coordinator_ior()
		throws org.omg.CORBA.MARSHAL
	{
		org.omg.CosTransactions.Coordinator coord = getPropagationContext().current.coord;

		return ( ( org.openorb.PI.OpenORBInitInfo ) _info ).orb().object_to_string( coord );
	}

	/**
	 * This operation is used to return the propagation context
	 */
	public org.omg.CosTransactions.PropagationContext getPropagationContext()
		throws org.omg.CORBA.MARSHAL
	{
		org.omg.CORBA.Any any = null;
		org.openorb.PI.CurrentImpl piCurrent = null;
		try
		{
			piCurrent = (org.openorb.PI.CurrentImpl)_info.resolve_initial_references("PICurrent");
			any = piCurrent.get_slot(t_slot);
		}
		catch (org.omg.PortableInterceptor.InvalidSlot ins) 
		{
			fatal("Current", "Invalid slot");
		}
		catch (org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName in ) 
		{ 
			fatal("Current", "Unable to resolve PICurrent");
		}				
		
		org.omg.CosTransactions.PropagationContext mpctx = org.omg.CosTransactions.PropagationContextHelper.extract(any);
		return mpctx;
	}
        
        /**
         * This operation is used to push a propagation context in a stack. When a sub transaction
         * begins, the parent propagation context must be stored in a stack.
         */
        public void push_txcontext( org.omg.CosTransactions.PropagationContext pctx )
        {
            java.util.Stack stack = ( java.util.Stack ) _pctx_stacks.get( java.lang.Thread.currentThread() );
            
            if ( stack == null )
            {
                stack = new java.util.Stack();
                _pctx_stacks.put( java.lang.Thread.currentThread(), stack );
            }
            
            stack.push( pctx );
        }
        
        /**
         * This operation restores a previously saved propagation context. For example, when a sub transaction
         * is completed, the parent propagation context is restored.
         */
        public void pop_txcontext()
        {
            boolean clean = false;
            
            java.util.Stack stack = ( java.util.Stack ) _pctx_stacks.get( java.lang.Thread.currentThread() );
            
            if ( stack == null )
                clean = true;
            else
            {
                if ( stack.empty() )
                    clean = true;
                else
                {
                    org.omg.CosTransactions.PropagationContext pctx = ( org.omg.CosTransactions.PropagationContext ) stack.pop();

                    try
                    {
                        org.omg.CORBA.Any pctx_any = org.omg.CORBA.ORB.init().create_any();
                        org.omg.CosTransactions.PropagationContextHelper.insert(pctx_any, pctx);
                        org.openorb.PI.CurrentImpl piCurrent = (org.openorb.PI.CurrentImpl)_info.resolve_initial_references("PICurrent");
                        piCurrent.set_slot(t_slot, pctx_any);
                    }
                    catch (org.omg.PortableInterceptor.InvalidSlot ins) 
                    {
                           fatal("Current", "Invalid slot");
                    }
                    catch (org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName in ) 
                    { 
                           fatal("Current", "Unable to resolve PICurrent");
                    }
                }
            }
            
            if ( clean )
            {
                try
                {
                    org.openorb.PI.CurrentImpl piCurrent = (org.openorb.PI.CurrentImpl)_info.resolve_initial_references("PICurrent");
	            piCurrent.set_slot(t_slot, org.omg.CORBA.ORB.init().create_any());
                }
                catch (org.omg.PortableInterceptor.InvalidSlot ins) 
                {
                      fatal("Current", "Invalid slot");
                }
                catch (org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName in ) 
                { 
                      fatal("Current", "Unable to resolve PICurrent");
                }
            }
        }
        
        /**
         * Displays a trace
         */
        public void print( String from, String msg )
        { 
        	if ( tyrex.util.Configuration.verbose )
                	tyrex.util.logging.Logger.ots.info(from + ": " + msg );            
        }
        
        /**
         * Displays a trace and throw a INTERNAL exception...
         */
        public void fatal( String from, String msg )
        {
			tyrex.util.logging.Logger.ots.warn(from + ": " + msg );
            throw new org.omg.CORBA.INTERNAL(msg);
        }
        
        /**
         * Returns the transaction factory
         */
        public org.omg.CosTransactions.TransactionFactory factory()
        {
            try
            {
               if ( _tfactory == null )                  
               {
                  org.omg.CORBA.Object obj = _info.resolve_initial_references("TransactionService");
   						
   		  _tfactory = org.omg.CosTransactions.TransactionFactoryHelper.narrow( obj );				
               }
               
               return _tfactory;  
            }
            catch ( java.lang.Exception ex )
            {
               ex.printStackTrace();
               fatal("Initializer", "Unable to resolve TransactionService");
            }
            
            return null;
        }
        
}
