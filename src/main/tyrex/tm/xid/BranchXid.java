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
 * $Id: BranchXid.java,v 1.1 2001/02/27 00:37:53 arkin Exp $
 */


package tyrex.tm.xid;


import javax.transaction.xa.Xid;
import tyrex.services.UUID;
import tyrex.util.Messages;


/**
 * Global transaction identifier with a branch qualifier. Used for all
 * distributed transaction branches created locally. The format identifier
 * is always the same.
 * 
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $
 */
public final class BranchXid
    extends BaseXid
{


    /**
     * The global transaction identifier for this transaction.
     */
    private final byte[] _global;


    /**
     * The branch qualifier for this transaction.
     */
    private final byte[] _branch;


    /**
     * Construct a new transaction branch identifier.
     *
     * @param global The global transaction identifier
     * @param branch The branch qualifier
     */
    BranchXid( byte[] global, byte[] branch )
    {
        StringBuffer buffer;

        buffer = new StringBuffer( 14 + ( global.length + branch.length ) * 2 );
        buffer.append( GlobalXid.XID_PREFIX_ARRAY );
        for ( int i = global.length ; i-- > 0 ; ) {
            buffer.append( HEX_DIGITS[ ( global[ i ] & 0xF0 ) >> 4 ] );
            buffer.append( HEX_DIGITS[ ( global[ i ] & 0x0F ) ] );
        }
        buffer.append( '-' );
        for ( int i = branch.length ; i-- > 0 ; ) {
            buffer.append( HEX_DIGITS[ ( branch[ i ] & 0xF0 ) >> 4 ] );
            buffer.append( HEX_DIGITS[ ( branch[ i ] & 0x0F ) ] );
        }
        _global = global;
        _branch = branch;
        _string = buffer.toString();
    }


    /**
     * Used by {@link XidUtils}.
     */
    BranchXid( String identifier, byte[] global, byte[] branch )
    {
        _string = identifier;
        _global = global;
        _branch = branch;
    }


    public int getFormatId()
    {
	return GlobalXid.GLOBAL_FORMAT_ID;
    }


    public byte[] getGlobalTransactionId()
    {
	return _global;
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
        if ( other instanceof BaseXid )
            return _string.equals( ( (BaseXid) other )._string );
        if ( other instanceof Xid ) {
            xid = (Xid) other;
            if ( xid.getFormatId() != GlobalXid.GLOBAL_FORMAT_ID )
                return false;
            // Branch Xid has both global transaction and branch.
            bytes = xid.getGlobalTransactionId();
            if ( bytes == null || bytes.length != _global.length )
                return false;
            for ( int i = bytes.length ; i-- > 0 ; )
                if ( bytes[ i ] != _global[ i ] )
                    return false;
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
        return new BranchXid( _global, UUID.createBinary() );
    }


}

