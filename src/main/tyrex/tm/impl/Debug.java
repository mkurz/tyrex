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
 */


package tyrex.tm.impl;


import java.util.Date;
import java.text.SimpleDateFormat;
import javax.transaction.Status;
import javax.transaction.xa.XAException;
import tyrex.tm.Heuristic;


/**
 * Debug utilities
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
final class Debug 
{


    /**
     * The date format for represnting time instant based on
     * the ISO 8601 lexical representation.
     */
    private final static SimpleDateFormat _dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" );


    /**
     * No instances
     */
    private Debug()
    {
    }


    /**
     * Returns the readable name for the specified status.
     *
     * @param status The status
     * @return The status
     */
    static String getStatus( int status )
    {
        StringBuffer buffer;

        buffer = new StringBuffer();
        switch ( status ) {
        case Status.STATUS_ACTIVE:
            buffer.append( "STATUS_ACTIVE: " );
            buffer.append( "A transaction is associated with the target object and it is in the active state." );
            break;
        case Status.STATUS_COMMITTED:
            buffer.append( "STATUS_COMMITTED: " );
            buffer.append( "A transaction is associated with the target object and it has been committed." );
            break;
        case Status.STATUS_COMMITTING:
            buffer.append( "STATUS_COMMITTING: " );
            buffer.append( "A transaction is associated with the target object and it is in the process of committing." );
            break;
        case Status.STATUS_MARKED_ROLLBACK:
            buffer.append( "STATUS_MARKED_ROLLBACK: " );
            buffer.append( "A transaction is associated with the target object and it has been marked for rollback, perhaps as a result of a setRollbackOnly operation." );
            break;
        case Status.STATUS_NO_TRANSACTION:
            buffer.append( "STATUS_NO_TRANSACTION: " );
            buffer.append( "No transaction is currently associated with the target object." );
            break;
        case Status.STATUS_PREPARED:
            buffer.append( "STATUS_PREPARED: " );
            buffer.append( "A transaction is associated with the target object and it has been prepared, i.e." );
            break;
        case Status.STATUS_PREPARING:
            buffer.append( "STATUS_PREPARING: " );
            buffer.append( "A transaction is associated with the target object and it is in the process of preparing." );
            break;           
        case Status.STATUS_ROLLEDBACK:
            buffer.append( "STATUS_ROLLEDBACK: " );
            buffer.append( "A transaction is associated with the target object and the outcome has been determined as rollback." );
            break;
        case Status.STATUS_ROLLING_BACK:
            buffer.append( "STATUS_ROLLING_BACK: " );
            buffer.append( "A transaction is associated with the target object and it is in the process of rolling back." );
            break;
        default:
            buffer.append( "Unknown status " + status );
            break;
        }
        return buffer.toString();
    }


    /**
     * Returns the readable name for the specified heuristic
     *
     * @param heuristic The heuristic
     * @return The heuristic
     */
    static String getHeuristic( int heuristic )
    {
        StringBuffer buffer;

        buffer = new StringBuffer();
        switch ( heuristic ) {
        case Heuristic.ROLLBACK:
            buffer.append( "rollback" );
            break;
        case Heuristic.COMMIT:
            buffer.append( "commit" );
            break;
        case Heuristic.MIXED:
            buffer.append( "mixed" );
            break;
        case Heuristic.HAZARD:
            buffer.append( "hazard" );
            break;
        case Heuristic.OTHER:
            buffer.append( "other" );
            break;
        case Heuristic.READONLY:
            buffer.append( "read only" );
            break;
        default:
            if ( ( heuristic & Heuristic.ROLLBACK ) != 0 )
                buffer.append( "rollback" );
            if ( ( heuristic & Heuristic.COMMIT ) != 0 ) {
                if ( buffer.length() > 0 )
                    buffer.append( " and " );
                buffer.append( "commit" );
            }
            if ( ( heuristic & Heuristic.MIXED ) != 0 ) {
                if ( buffer.length() > 0 )
                    buffer.append( " and " );
                buffer.append( "mixed" );
            }
            if ( ( heuristic & Heuristic.HAZARD ) != 0 ) {
                if ( buffer.length() > 0 )
                    buffer.append( " and " );
                buffer.append( "hazard" );
            }
            if ( ( heuristic & Heuristic.OTHER ) != 0 ) {
                if ( buffer.length() > 0 )
                    buffer.append( " and " );
                buffer.append( "other" );
            }
            if ( ( heuristic & Heuristic.READONLY ) != 0 ) {
                if ( buffer.length() > 0 )
                    buffer.append( " and " );
                buffer.append( "read only" );
            }
            break;
        }
        return buffer.toString();
    }


    /**
     * Retrusn the details of the xa exception
     *
     * @param except The XA exception
     * @return The XA exception
     */
    static String getXAException( XAException except )
    {
        StringBuffer buffer;

        buffer = new StringBuffer();
        buffer.append( "XA Exception occurred: " );
        switch ( except.errorCode ) {
        case XAException.XA_HEURCOM:
            buffer.append( "XA_HEURCOM: " );
            buffer.append( "The transaction branch has been heuristically committed." );
            break;
            
        case XAException.XA_HEURHAZ:
            buffer.append( "XA_HEURHAZ: ");
            buffer.append( "The transaction branch may have been heuristically completed." );
            break;
            
        case XAException.XA_HEURMIX:
            buffer.append( "XA_HEURMIX: " );
            buffer.append( "The transaction branch has been heuristically committed and rolled back." );
            break;
            
        case XAException.XA_HEURRB:
            buffer.append( "XA_HEURRB: " );
            buffer.append( "The transaction branch has been heuristically rolled back." );
            break;
            
        case XAException.XA_NOMIGRATE:
            buffer.append( "XA_NOMIGRATE: " );
            buffer.append( "Resumption must occur where suspension occured." );
            break;
            
            //XA_RBBASE:
            
        case XAException.XA_RBCOMMFAIL:
            buffer.append( "XA_RBCOMMFAIL: " );
            buffer.append( "Communication error occured." );
            break;
            
        case XAException.XA_RBDEADLOCK:
            buffer.append( "XA_RBDEADLOCK: " );
            buffer.append( "A deadlock was detected." );
            break;
            
            //XA_RBEND 
            
        case XAException.XA_RBINTEGRITY:
            buffer.append( "XA_RBINTEGRITY: " );
            buffer.append( "A condition that violates the integrity of the resource was detected." );
            break;
            
        case XAException.XA_RBOTHER:
            buffer.append( "XA_RBOTHER: " );
            buffer.append( "The resource manager rolled back the transaction branch for a reason not on this list." );
            break;
            
        case XAException.XA_RBPROTO:
            buffer.append( "XA_RBPROTO: " );
            buffer.append( "A protocol error occured in the resource manager." );
            break;
            
        case XAException.XA_RBROLLBACK:
            buffer.append( "XA_RBROLLBACK: " );
            buffer.append( "Rollback was caused by unspecified reason." );
            break;
            
        case XAException.XA_RBTIMEOUT:
            buffer.append( "XA_RBTIMEOUT: " );
            buffer.append( "A transaction branch took too long." );
            break;
            
        case XAException.XA_RBTRANSIENT:
            buffer.append( "XA_RBTRANSIENT: " );
            buffer.append( "May retry the transaction branch." );
            break;
            
        case XAException.XA_RDONLY:
            buffer.append( "XA_RDONLY: " );
            buffer.append( "The transaction branch has been read-only and has been committed." );
            break;
            
        case XAException.XA_RETRY:
            buffer.append( "XA_RETRY: " );
            buffer.append( "Routine returned with no effect and may be reissued." );
            break;
            
        case XAException.XAER_ASYNC:
            buffer.append( "XAER_ASYNC: " );
            buffer.append( "Asynchronous operation already outstanding." );
            break;
            
        case XAException.XAER_DUPID:
            buffer.append( "XAER_DUPID: " );
            buffer.append( "The XID already exists.");
            break;
            
        case XAException.XAER_INVAL:
            buffer.append( "XAER_INVAL: " );
            buffer.append( "Invalid arguments were given." );
            break;
            
        case XAException.XAER_NOTA:
            buffer.append( "XAER_NOTA: " );
            buffer.append( "The XID is not valid." );
            break;
            
        case XAException.XAER_OUTSIDE:
            buffer.append( "XAER_OUTSIDE: " );
            buffer.append( "The resource manager is doing work outside global transaction." );
            break;
            
        case XAException.XAER_PROTO:
            buffer.append( "XAER_PROTO: " );
            buffer.append( "Routine was invoked in an inproper context." );
            break;
            
        case XAException.XAER_RMERR:
            buffer.append( "XAER_RMERR: " );
            buffer.append( "A resource manager error has occured in the transaction branch." );
            break;
            
        case XAException.XAER_RMFAIL:
            buffer.append( "XAER_RMFAIL: " );
            buffer.append( "Resource manager is unavailable." );
            break;
            
        default:
            buffer.append( "Unknown error code: " + except.errorCode );
            break;
        }
        if ( except.getMessage() != null )
            buffer.append( ": " ).append( except.getMessage() );
        return buffer.toString();
    }


    /**
     * Converts a computer time into a string representation of time instant.
     * The string representation of time duration is the ISO 8601 extended
     * format <tt>CCYY-MM-DDThh:mm:ss.sss</tt>.
     *
     * @param clock The computer time
     * @return The string repreentation of time instant
     */
    public static String fromClock( long clock )
    {
        return _dateFormat.format( new Date( clock ) );
    }
 
   
}
