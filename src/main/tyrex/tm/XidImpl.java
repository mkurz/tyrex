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
 * $Id: XidImpl.java,v 1.5 2001/02/23 19:22:50 jdaniel Exp $
 */


package tyrex.tm;


import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.transaction.xa.Xid;
import tyrex.util.Messages;
import tyrex.util.Logger;


/**
 * Implementation of {@link Xid}. A transaction identifier consists of
 * three parts: the format is unique to the implementation; the global
 * identifier uniquely identifies the transaction across a collection
 * of server; the branch identifier identifies one of several branches
 * inside the transaction.
 * <p>
 * This implementation has the following properties:
 * <ul>
 * <li>The global identifier is made up of 96 bits including a
 *   unique local transaction identifier and the machine's identifier
 * <li>For a newly created transaction the global and branch use the
 *   same unique local identifier
 * <li>For a new branch Xid, the branch identifier contains a unique
 *   identifier, the global identifier remains the same
 * <li>The local identifier part is made up of the system clock when
 *   the transaction was created (52 bits) and a counter to assure
 *   uniqueness (12 bits)
 * <li>The machine identifier defaults to the machine's IP address
 *   (32 bits)
 * <li>The total Xid size (including the static format identifier)
 *   is 224 bits but only the 96 bits of the global or branch
 *   identifiers are needed for most operations
 * </ul>
 * This object is immutable and can be used as a key in a hashtable.
 * 
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $ $Date: 2001/02/23 19:22:50 $
 *
 * Date     Author      Change
 * 1/8/1    J.Daniel    Added a new constructor to be able to restore an
 *                      XID from its stringified representation.
 */
public final class XidImpl
    implements Xid, Serializable
{


    // IMPLEMENTATION NOTES:
    //    Xid is made of three parts: format, global identifier,
    //    branch identifier. The format is the same for all XidImpl.
    //
    //    The global identifier is created when a new XidImpl is
    //    constructed, although an XidImpl might be constructed to
    //    represent a global transaction with that transaction's global
    //    identifier. When created locally, the global identifier
    //    comprises a unique transaction identifier and unique machine
    //    identifier.
    //
    //    The branch identifier is created to identify a unique branch
    //    (subordinate transaction, XAResource, etc) within the global
    //    transaction. For a new transaction it will be equivalent to
    //    the global identifier (for efficiency, the same array).
    //    When a new branch is created, a new unique identifier is
    //    generated for that branch. It also needs to be globally
    //    unique, in case two servers try to communciate with the
    //    same resource manager.
    //
    //    For the machine identifier we use the IP address which is
    //    sufficient to identify any accessible transaction server.
    //    Currently we only support one transaction server per machine.
    //
    //    For the unique identifier we use the timestamp when the
    //    transaction was created, which eases bug tracking. We append
    //    a unique counter, since two transactions created within the
    //    same millisecond (actually, 10 ms) will have the same
    //    timestamp.


    /**
     * The global identifier for this transaction.
     */
    private byte[] _global;


    /**
     * The branch identifier for this transaction.
     */
    private byte[] _branch;


    /**
     * Static counter used to assure all identifiers are unique.
     * An identifier consists of the current system clock and this
     * counter, since two transactions created within the same
     * millisecond will have the same system clock.
     */
    private static long         _xidCounter;


    /**
     * The machine identifier used for all transactions created
     * on this server to identify the originator of the transaction.
     * Initialized in the static constructor.
     */
    private static byte[]       _machineId;


    /**
     * All {@link XidImpl} bear the exact same format identifier.
     */
    public static final int    XID_FORMAT = 0xE0FFCE;


    /**
     * Defines the size of the global identifier in bytes.
     */
    public static final int    GLOBAL_XID_LENGTH = 36;


    /**
     * Defines the size of the machine identifier in bytes.
     */
    public static final int    MACHINE_ID_LENGTH = 4;





    /**
     * Construct a new Xid with the default format and specified
     * machine identifier.
     */
    public XidImpl()
    {
	int   i;
	byte[] bytes;

	_branch = tyrex.util.UUID.createBinary();
	_global = _branch;
    }

    /**
     * Construct a nex XID from its stringified format
     */
    public XidImpl( String xid_str )
    {
        unmarshalXID( xid_str );
    }
    
    /**
     * Returns a new Xid that represents a unique branch
     * of this Xid. The new Xid will bear the same format and
     * global identifier, but a different branch idenfitier.
     * The machine identifier on the new branch will be that
     * of this machine, not necessarily the same of the global
     * identifier part.
     *
     * @return New branch Xid
     */
    public XidImpl newBranch()
    {
	XidImpl branch;
	int     i;
	byte[]  bytes;

	branch = new XidImpl();
	branch._global = _global;
	branch._branch = tyrex.util.UUID.createBinary();
	return branch;
    }


    public int getFormatId()
    {
	return XID_FORMAT;
    }


    public byte[] getGlobalTransactionId()
    {
	return _global;
    }


    public byte[] getBranchQualifier()
    {
	return _branch;
    }


    /**
     * Produces a textual presentation of the transaction identifier.
     * The returned string contains the hexadecimal presentation of
     * the format (8 characters), global identifier (36 characters)
     * and branch identifier (36 characters), separated with a colon.
     * Foe example:
     * <pre>
     * E0FC:000DB98D87A5B001CC9C9235:000DB98D87A5B001CC9C9235
     * </pre>
     */
    public String toString()
    {
	StringBuffer buffer;
	int          i;

	buffer = new StringBuffer( 80 );
	for ( i = 8 ; i-- > 0 ; )
	    buffer.append( toHex( XID_FORMAT >> ( 4 * i ) ) );
	buffer.append( ':' );
	for ( i = GLOBAL_XID_LENGTH ; i-- > 0 ; ) {
	    buffer.append( toHex( _global[ i ] >> 4 ) );
	    buffer.append( toHex( _global[ i ] ) );
	}
	buffer.append( ':' );
	for ( i = GLOBAL_XID_LENGTH ; i-- > 0 ; ) {
	    buffer.append( toHex( _branch[ i ] >> 4 ) ); 
	    buffer.append( toHex( _branch[ i ] ) );

	}
	return buffer.toString();
    }

    /**
     * This operation unmarshals an XID from its stringified representation.
     */
    private void unmarshalXID( String xid )
    {
        long format = 0;
        
        for ( int i=0; i<8; i++ )
        {
            format = ( format << 4 ) + fromHex( xid.charAt(i) );
        }
        if ( format == XID_FORMAT )
        {
            _global = new byte[ GLOBAL_XID_LENGTH ];
            for ( int i=0; i<GLOBAL_XID_LENGTH; i++ )
                _global[i] = ( byte ) fromHex( xid.charAt( i + 9 ) );
            
            _branch = new byte[ GLOBAL_XID_LENGTH ];
            for ( int i=0; i<GLOBAL_XID_LENGTH; i++ )
                _branch[i] = ( byte ) fromHex( xid.charAt( i + 10 + GLOBAL_XID_LENGTH ) );
        }
    }

    public boolean equals( Object other )
    {
	XidImpl xid;
	int     i;

	if ( other == this )
	    return true;
	if ( ! ( other instanceof XidImpl ) )
	    return false;

	// We know that both Xids will bear the same format, we only
	// have to check global and branch identifiers. We compare
	// the branch identifier first because it defines uniqueness
	// amongst different global transactions as well as same
	// global transactions with different branches.
	xid = (XidImpl) other;
	for ( i = 0 ; i < GLOBAL_XID_LENGTH ; ++i )
	    if ( _branch[ i ] != xid._branch[ i ] )
		return false;
	if ( _global != xid._global ) {
	    for ( i = 0 ; i < GLOBAL_XID_LENGTH ; ++i )
		if ( _global[ i ] != xid._global[ i ] )
		return false;
	}
	return true;
    }


    public int hashCode()
    {
	int hash;
	int i;
	
	// We typically hash global transactions generated by this
	// server, so there's no purpose in hashing either format or
	// the branch identifier. The global identifier is 64 bits
	// long, but the hash code is only 32 bits, so we try to
	// hash appropriately. The greatest variant is in the first
	// bytes (the counter) compared to the last bytes (the year).
	hash = 0;
	for ( i = ( GLOBAL_XID_LENGTH - MACHINE_ID_LENGTH ) ; i-- > 0 ;  )
	    hash = ( hash << 4 ) + _branch[ i ];
	return hash;
    }


    public Object clone()
    {
	XidImpl clone;

	clone = new XidImpl();
	clone._branch = _branch;
	clone._global = _global;
	return clone;
    }

    /**
     * Used internally to convert the binary value into a viewable
     * string. <tt>value</tt> must range between 0x00 and 0x0f.
     */
    static char toHex( long value )
    {
	value = value & 0x0F;
	if ( value < 0x0A )
	    return (char) ( '0' + value );
	else
	    return (char) ( 'A' - 0x0A + value );
    }
    
    /**
     * Return a decimal value from an hexadecimal value
     */
    long fromHex( char c )
    {
        if(c >= '0' && c <= '9')
            return (c-'0');
        else if(c >= 'a' && c <= 'f')
            return (c-'a'+0xA);
        else if(c >= 'A' && c <= 'F')
            return (c-'A'+0xA);
        
        return 0;
    }

    /**
     * Used internally to textually present global and branch
     * identifiers alone, primarily for debugging purposes.
     */
    static String toString( byte[] gxid )
    {
	StringBuffer buffer;
	int          i;

	buffer = new StringBuffer();
	for ( i = GLOBAL_XID_LENGTH ; i-- > 0 ; ) {
	    buffer.append( toHex( gxid[ i ] >> 4 ) );
	    buffer.append( toHex( gxid[ i ] ) );
	}
	return buffer.toString();
    }


    /**
     * Returns true if the global identifier eminated from this
     * machine based on the machine identifier part of it.
     *
     * @param gxid The global transaction identifier
     * @return True if eminated on this machine
     */
    public static boolean isLocal( byte[] gxid )
    {
	int i;

	if ( gxid.length != GLOBAL_XID_LENGTH  )
	    return false;
	for ( i = 0 ; i < MACHINE_ID_LENGTH ; ++i )
	    if ( _machineId[ i ] != gxid[ GLOBAL_XID_LENGTH - MACHINE_ID_LENGTH + i ] )
		return false;
	return true;
    }


    static
    {
	InetAddress inet;
	byte[]      bytes;
	long        addr;
	int         i;
	
	// Determine part of the global transaction id based
	// on the server's TCP/IP address.
	try {
	    bytes = InetAddress.getLocalHost().getAddress();
	    addr = 0;
	    for ( i = 0 ; i < bytes.length ; ++i ) {
		addr = ( addr << 8 ) + bytes[ i ];
		if ( bytes[ i ] < 0 )
		    addr += 0x100;
	    }
	} catch ( UnknownHostException except ) {
	    addr = 0xFFFFFFFF;
	    Logger.tm.warn( Messages.message( "tyrex.server.failedFindHostIP" ) );
	}
	_machineId = new byte[ MACHINE_ID_LENGTH ];
	for ( i = 0 ; i < MACHINE_ID_LENGTH ; ++i ) {
	    _machineId[ i ] = (byte) addr;
	    addr = addr >> 8;
	}
    }


}
