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
 * $Id: LocalXid.java,v 1.2 2001/03/12 19:20:20 arkin Exp $
 */


package tyrex.tm.xid;


import javax.transaction.xa.Xid;
import tyrex.services.UUID;
import tyrex.util.Messages;


/**
 * Local transaction identifier. Used by resource managers for local
 * transactions. A local transaction identifier has an empty global
 * transaction identifier and unique branch qualifier.
 * 
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $
 */
public final class LocalXid
    extends BaseXid
{

    
    /**
     * The branch qualifier for this transaction.
     */
    private final byte[] _branch;


    /**
     * The XID_PREFIX and XID_FORMAT as a character array.
     */
    private final static char[]  XID_PREFIX_ARRAY;


    /**
     * The format identifier used by all local transactions.
     */
    public final static int      LOCAL_FORMAT_ID = 0x80000000 + FORMAT_ID;


    /**
     * Construct a new unique transaction identifier.
     */
    public LocalXid()
    {
        StringBuffer buffer;
        byte[]       branch;

        branch = UUID.createBinary();
        buffer = new StringBuffer( 46 );
        buffer.append( XID_PREFIX_ARRAY ).append( '-' );
        for ( int i = branch.length ; i-- > 0 ; ) {
            buffer.append( HEX_DIGITS[ ( branch[ i ] & 0xF0 ) >> 4 ] );
            buffer.append( HEX_DIGITS[ ( branch[ i ] & 0x0F ) ] );
        }
        _branch = branch;
        _string = buffer.toString();
    }


    /**
     * Used by {@link XidUtils}.
     */
    LocalXid( String identifier, byte[] branch )
    {
        _string = identifier;
        _branch = branch;
    }


    static {
        XID_PREFIX_ARRAY = createPrefix( LOCAL_FORMAT_ID );
    }


    public int getFormatId()
    {
        return LOCAL_FORMAT_ID;
    }


    public byte[] getGlobalTransactionId()
    {
        return EMPTY_ARRAY;
    }


    public byte[] getBranchQualifier()
    {
        return _branch;
    }


    public boolean equals( Object other )
    {
        Xid     xid;
        byte[]  bytes;

        if ( other == this )
            return true;
        if ( other instanceof LocalXid )
            return _string.equals( ( (LocalXid) other )._string );
        if ( other instanceof Xid ) {
            xid = (Xid) other;
            if ( xid.getFormatId() != LOCAL_FORMAT_ID )
                return false;
            // Local Xid has empty global transaction identifier.
            bytes = xid.getGlobalTransactionId();
            if ( bytes != null && bytes.length != 0 )
                return false;
            // Compare length and all bytes of branch qualifier.
            bytes = xid.getBranchQualifier();
            if ( bytes == null || bytes.length != _branch.length )
                return false;
            for ( int i = bytes.length ; i-- > 0 ; )
                if ( bytes[ i ] != _branch[ i ] )
                    return false;
            return true;
        }
        return false;
    }


    public Xid newBranch()
    {
        return new LocalXid();
    }


}

