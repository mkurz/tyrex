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
 * $Id: Heuristic.java,v 1.3 2000/09/08 23:05:11 mohammed Exp $
 */


package tyrex.interceptor;


/**
 * Values for heuristic decisions made regarding a transaction.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/08 23:05:11 $
 */
public interface Heuristic
{
    
    /**
     * Indicates that the transaction has no resources or has only
     * read-only resources. The transaction has committed but no
     * resources participated in the commit.
     */
    public static final int ReadOnly = 0x00;


    /**
     * Indicates that all resources in the transaction (at least one,
     * excluding any read-only) have agreed to commit and the
     * transaction has committed across all resources.
     */
    public static final int Commit = 0x01;
    

    /**
     * Indicates that one or more resources in the transaction
     * (excluding read-only) could not be prepared or that an error
     * marked this transaction as faulty. The transaction has been
     * rolled back across all resources.
     */
    public static final int Rollback = 0x02;
    

    /**
     * Indicates that some resources have commited and others have
     * rolledback. The transaction has not fully committed or
     * rolledback across all resources.
     */
    public static final int Mixed = 0x04;
    

    /**
     * Indicates that resources have commited, or resources have
     * rolled back, but we don't know what was the exact outcome.
     */
    public static final int Hazard = 0x08;
    

    /**
     * Transaction has been timed out and has been rolled back.
     */
    public static final int TimedOut = 0x10;


}
