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
 */


package tyrex.tm;

import javax.transaction.Status;
import javax.transaction.xa.XAException;

///////////////////////////////////////////////////////////////////////////////
// Debug
///////////////////////////////////////////////////////////////////////////////

/**
 * Debug utilities
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public final class Debug 
{
    /**
     * No instances
     */
    private Debug()
    {
    }

    /**
     * Print the readable name for the specified
     * status.
     *
     * @param status the status
     */
    static void printStatus(int status)
    {
        switch (status) {
            case Status.STATUS_ACTIVE:
                System.out.println("STATUS_ACTIVE");
                System.out.println("A transaction is associated with the target object and it is in the active state.");
                break;
            case Status.STATUS_COMMITTED:
                System.out.println("STATUS_COMMITTED");
                System.out.println("A transaction is associated with the target object and it has been committed.");
                break;
            case Status.STATUS_COMMITTING:
                System.out.println("STATUS_COMMITTING");
                System.out.println("A transaction is associated with the target object and it is in the process of committing.");
                break;
            case Status.STATUS_MARKED_ROLLBACK:
                System.out.println("STATUS_MARKED_ROLLBACK");
                System.out.println("A transaction is associated with the target object and it has been marked for rollback, perhaps as a result of a setRollbackOnly operation.");
                break;
            case Status.STATUS_NO_TRANSACTION:
                System.out.println("STATUS_NO_TRANSACTION");
                System.out.println("No transaction is currently associated with the target object.");
                break;
            case Status.STATUS_PREPARED:
                System.out.println("STATUS_PREPARED");
                System.out.println("A transaction is associated with the target object and it has been prepared, i.e.");
                break;
            case Status.STATUS_PREPARING:
                System.out.println("STATUS_PREPARING");
                System.out.println("A transaction is associated with the target object and it is in the process of preparing.");
                break;           
            case Status.STATUS_ROLLEDBACK:
                System.out.println("STATUS_ROLLEDBACK");
                System.out.println("A transaction is associated with the target object and the outcome has been determined as rollback.");
                break;
            case Status.STATUS_ROLLING_BACK:
                System.out.println("STATUS_ROLLING_BACK");
                System.out.println("A transaction is associated with the target object and it is in the process of rolling back.");
                break;
            default:
                System.out.println("Unknown status " + status);
                break;
        }
    }

    /**
     * Print the readable name for the specified
     * heuristic
     *
     * @param heuristic the heuristic
     */
    static void printHeuristic(int heuristic)
    {
        switch (heuristic) {
            case Heuristic.Rollback:
                System.out.println("heuristic rollback");
                break;
            case Heuristic.Commit:
                System.out.println("heuristic commit");
                break;
            case Heuristic.Mixed:
                System.out.println("heuristic mixed");
                break;
            case Heuristic.Hazard:
                System.out.println("heuristic hazard");
                break;
            case Heuristic.Other:
                System.out.println("heuristic other");
                break;
            case Heuristic.Unknown:
                System.out.println("heuristic unknown");
                break;
            case Heuristic.ReadOnly:
                System.out.println("heuristic read only");
                break;
            default:
                if ((heuristic & Heuristic.Rollback) != 0)
                    System.out.print("heuristic rollback");
                if ((heuristic & Heuristic.Commit) != 0)
                    System.out.print("heuristic rollback");
                if ((heuristic & Heuristic.Mixed) != 0)
                    System.out.print("heuristic rollback");
                if ((heuristic & Heuristic.Hazard) != 0)
                    System.out.print("heuristic rollback");
                if ((heuristic & Heuristic.Other) != 0)
                    System.out.print("heuristic rollback");
                if ((heuristic & Heuristic.Unknown) != 0)
                    System.out.print("heuristic rollback");
                if ((heuristic & Heuristic.ReadOnly) != 0)
                    System.out.print("heuristic rollback");
                System.out.println("");
                break;
        }
    }

    /**
     * Print the details of the xa exception
     *
     * @param e the XA exception
     */
    static void printXAException(XAException e)
    {
        System.out.println("XA Exception occurred:");
        System.out.println(e.toString());
        switch (e.errorCode) {
            case XAException.XA_HEURCOM:
                System.out.println("XA_HEURCOM");
                System.out.println("The transaction branch has been heuristically committed.");
                break;

            case XAException.XA_HEURHAZ:
                System.out.println("XA_HEURHAZ");
                System.out.println("The transaction branch may have been heuristically completed.");
                break;
                  
            case XAException.XA_HEURMIX:
                System.out.println("XA_HEURMIX");
                System.out.println("The transaction branch has been heuristically committed and rolled back.");
                break;
                  
            case XAException.XA_HEURRB:
                System.out.println("XA_HEURRB");
                System.out.println("The transaction branch has been heuristically rolled back.");
                break;
                  
            case XAException.XA_NOMIGRATE:
                System.out.println("XA_NOMIGRATE");
                System.out.println("Resumption must occur where suspension occured.");
                break;
                  
            //XA_RBBASE:
            
            case XAException.XA_RBCOMMFAIL:
                System.out.println("XA_RBCOMMFAIL");
                System.out.println("");
                break;
                  
            case XAException.XA_RBDEADLOCK:
                System.out.println("XA_RBDEADLOCK");
                System.out.println("");
                break;
                  
            //XA_RBEND 
            
            case XAException.XA_RBINTEGRITY:
                System.out.println("XA_RBINTEGRITY");
                System.out.println("A condition that violates the integrity of the resource was detected.");
                break;
                  
            case XAException.XA_RBOTHER:
                System.out.println("XA_RBOTHER");
                System.out.println("The resource manager rolled back the transaction branch for a reason not on this list.");
                break;
                  
            case XAException.XA_RBPROTO:
                System.out.println("XA_RBPROTO");
                System.out.println("A protocol error occured in the resource manager. ");
                break;

            case XAException.XA_RBROLLBACK:
                System.out.println("XA_RBROLLBACK");
                System.out.println("Rollback was caused by unspecified reason.");
                break;
                  
            case XAException.XA_RBTIMEOUT:
                System.out.println("XA_RBTIMEOUT");
                System.out.println("A transaction branch took too long.");
                break;

            case XAException.XA_RBTRANSIENT:
                System.out.println("XA_RBTRANSIENT");
                System.out.println("May retry the transaction branch.");
                break;

            case XAException.XA_RDONLY:
                System.out.println("XA_RDONLY");
                System.out.println("The transaction branch has been read-only and has been committed.");
                break;

            case XAException.XA_RETRY:
                System.out.println("XA_RETRY");
                System.out.println("Routine returned with no effect and may be reissued.");
                break;

            case XAException.XAER_ASYNC:
                System.out.println("XAER_ASYNC");
                System.out.println("Asynchronous operation already outstanding.");
                break;

            case XAException.XAER_DUPID:
                System.out.println("XAER_DUPID");
                System.out.println("The XID already exists.");
                break;
                   
            case XAException.XAER_INVAL:
                System.out.println("XAER_INVAL");
                System.out.println("Invalid arguments were given.");
                break;

            case XAException.XAER_NOTA:
                System.out.println("XAER_NOTA");
                System.out.println("The XID is not valid.");
                break;

            case XAException.XAER_OUTSIDE:
                System.out.println("XAER_OUTSIDE");
                System.out.println("The resource manager is doing work outside global transaction.");
                break;

            case XAException.XAER_PROTO:
                System.out.println("XAER_PROTO");
                System.out.println("Routine was invoked in an inproper context.");
                break;

            case XAException.XAER_RMERR:
                System.out.println("XAER_RMERR");
                System.out.println("A resource manager error has occured in the transaction branch.");
                break;

            case XAException.XAER_RMFAIL:
                System.out.println("XAER_RMFAIL");
                System.out.println("Resource manager is unavailable.");
                break;

            default:
                System.out.println("Unknown error code: " + e.errorCode);
                break;
        }

        e.printStackTrace();    
    }

}
