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
 */

package tyrex.corba;

/**
 * This object is used to provide a pseudo control object. On the client and server sides, the control object cannot be
 * found ( since its reference is not set in the propagation context ). However, this object only provides two accessors to
 * get the coordinator and the terminator. These objects reference are set in the propagation context, so it is possible
 * to simulate the original control object.
 *
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version 1.0
 */
public class PseudoControl extends org.omg.CORBA.LocalObject implements org.omg.CosTransactions.Control
{
    /**
     * Reference to the coordinator
     */
    private org.omg.CosTransactions.Coordinator _coord;
    
    /**
     * Reference to the terminator
     */
    private org.omg.CosTransactions.Terminator _term;
    
    /**
     * Constructor. The parameters are the coordinator and terminator references.
     */
    public PseudoControl( org.omg.CosTransactions.Coordinator coord, org.omg.CosTransactions.Terminator term )
    {
       _coord = coord;
       _term = term;
    }
    
    /**
     * Return the terminator
     */
    public org.omg.CosTransactions.Terminator get_terminator()
        throws org.omg.CosTransactions.Unavailable
    {
        return _term;
    }
    
    /**
     * Return the coordinator
     */
    public org.omg.CosTransactions.Coordinator get_coordinator()
        throws org.omg.CosTransactions.Unavailable
    {
        return _coord;
    }
}