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
 * Copyright 2000, 2001 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: TerminatorImpl.java,v 1.1 2001/03/05 18:25:39 arkin Exp $
 */


package tyrex.tm.impl;


import org.omg.CosTransactions.Terminator;
import org.omg.CosTransactions._TerminatorImplBase;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;


/**
 * Implements a {@link Terminator} interface into a transaction.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2001/03/05 18:25:39 $
 */
final class TerminatorImpl
    extends _TerminatorImplBase
    implements Terminator
{


    /**
     * The control that returned this terminator.
     */
    private final ControlImpl    _control;


    TerminatorImpl( ControlImpl control )
    {
        if ( control == null )
            throw new IllegalArgumentException( "Argument control is null" );
        _control = control;
    }


    public void commit( boolean reportHeuristic )
        throws HeuristicMixed, HeuristicHazard
    {        
        // No heuristics are reported on subtransaction completion.
        if ( _control._parents != null )
            reportHeuristic = false;
        try {
            _control._tx.commit();
	} catch ( IllegalStateException except ) {
	    throw new INVALID_TRANSACTION( except.getMessage() );
	} catch ( SystemException except ) {
	    if ( reportHeuristic )
		throw new HeuristicHazard();
	} catch ( RollbackException except ) {
	    throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
	} catch ( HeuristicRollbackException except ) {
	    if ( reportHeuristic )
		throw new TRANSACTION_ROLLEDBACK( except.getMessage() );
	} catch ( HeuristicMixedException except ) {
	    if ( reportHeuristic )
		throw new HeuristicMixed();
	} catch ( SecurityException except ) {
	    if ( reportHeuristic )
		throw new HeuristicHazard();
	    throw new INVALID_TRANSACTION( except.toString() );
        } finally {
            _control.deactivate();
        }
    }


    public void rollback()
    {
	try {
	    _control._tx.rollback();
	} catch ( IllegalStateException except ) {
	    throw new INVALID_TRANSACTION( except.getMessage() );
	} catch ( SystemException except ) {
	    throw new INVALID_TRANSACTION( except.toString() );
	} finally {
            _control.deactivate();
        }
    }


}
