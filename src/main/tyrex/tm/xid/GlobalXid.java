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
 * Copyright 1999-2001 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: GlobalXid.java,v 1.2 2001/03/12 19:20:20 arkin Exp $
 */


package tyrex.tm.xid;


import javax.transaction.xa.Xid;
import tyrex.services.UUID;
import tyrex.util.Messages;


/**
 * Global transaction identifier. Used for all distributed transactions
 * created locally. The format identifier is always the same, the branch
 * qualifier is always empty.
 * 
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $
 */
public final class GlobalXid
    extends BaseXid
{


    /**
     * The global transaction identifier this transaction.
     */
    private final byte[] _global;


    /**
     * The XID_PREFIX and XID_FORMAT as a character array.
     */
    protected final static char[]  XID_PREFIX_ARRAY;


    /**
     * The format identifier used by all local transactions.
     */
    protected final static int     GLOBAL_FORMAT_ID = FORMAT_ID;



    /**
     * Construct a new global transaction identifier.
     */
    public GlobalXid()
    {
        StringBuffer buffer;
        byte[]       global;

        global = UUID.createBinary();
        buffer = new StringBuffer( 45 );
        buffer.append( XID_PREFIX_ARRAY );
        for ( int i = global.length ; i-- > 0 ; ) {
            buffer.append( HEX_DIGITS[ ( global[ i ] & 0xF0 ) >> 4 ] );
            buffer.append( HEX_DIGITS[ ( global[ i ] & 0x0F ) ] );
        }
        _global = global;
        _string = buffer.toString();
    }


    /**
     * Used by {@link XidUtils}.
     */
    GlobalXid( String identifier, byte[] global )
    {
        _string = identifier;
        _global = global;
    }


    static {
        XID_PREFIX_ARRAY = createPrefix( GLOBAL_FORMAT_ID );
    }


    public int getFormatId()
    {
        return GLOBAL_FORMAT_ID;
    }


    public byte[] getGlobalTransactionId()
    {
        return _global;
    }


    public byte[] getBranchQualifier()
    {
        return EMPTY_ARRAY;
    }


    public boolean equals( Object other )
    {
        Xid     xid;
        byte[]  bytes;

        if ( other == this )
            return true;
        if ( other instanceof GlobalXid )
            return _string.equals( ( (GlobalXid) other )._string );
        if ( other instanceof Xid ) {
            xid = (Xid) other;
            if ( xid.getFormatId() != GLOBAL_FORMAT_ID )
                return false;
            // Global Xid has empty branch qualifier.
            bytes = xid.getBranchQualifier();
            if ( bytes != null && bytes.length != 0 )
                return false;
            // Compare length and all bytes of transaction identifier.
            bytes = xid.getGlobalTransactionId();
            if ( bytes == null || bytes.length != _global.length )
                return false;
            for ( int i = bytes.length ; i-- > 0 ; )
                if ( bytes[ i ] != _global[ i ] )
                    return false;
            return true;
        }
        return false;
    }


    public Xid newBranch()
    {
        return new BranchXid( _global, UUID.createBinary() );
    }


}

