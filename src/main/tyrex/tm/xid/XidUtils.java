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
 * $Id: XidUtils.java,v 1.1 2001/02/27 00:37:53 arkin Exp $
 */


package tyrex.tm.xid;


import javax.transaction.xa.Xid;
import tyrex.services.UUID;
import tyrex.util.Messages;


/**
 * Utility class for creating transaction identifiers, importing <tt>Xid</tt>
 * objects, and converting a transaction identifier to/from a string
 * representation.
 * 
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $
 */
public final class XidUtils
{


    /**
     * Prefix for textual identifier.
     */
    public static final String   XID_PREFIX = BaseXid.XID_PREFIX;


    /**
     * Format identifier for all internal Xids or newly created Xids.
     */
    public static final int      FORMAT_ID = BaseXid.FORMAT_ID;


    /**
     * The default branch is always an empty byte array.
     */
    protected static final byte[]  EMPTY_ARRAY = new byte[ 0 ];


    /**
     * Efficient mapping from 4 bit value to lower case hexadecimal digit.
     */
    protected final static char[]  HEX_DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7',
                                                           '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };


    /**
     * Create a new local transaction identifier.
     *
     * @return A new local transaction identifier
     */
    public static Xid newLocal()
    {
        return new LocalXid();
    }


    /**
     * Create a new global transaction identifier.
     *
     * @return A new global transaction identifier
     */
    public static Xid newGlobal()
    {
        return new GlobalXid();
    }


    /**
     * Imports a transaction identifier. Returns a equivalent transaction
     * identifier that can be converted to/from a string representation and
     * supports the equality test.
     *
     * @param xid An existing transaction identifier
     * @return An equivalent transaction identifier
     */
    public static Xid importXid( Xid xid )
    {
        if ( xid == null )
            throw new IllegalArgumentException( "Argument xid is null" );
        if ( xid instanceof BaseXid )
            return xid;
        return new ExternalXid( xid );
    }


    /**
     * Imports a transaction identifier. Returns a equivalent transaction
     * identifier that can be converted to/from a string representation and
     * supports the equality test.
     *
     * @param formatId The format identifier
     * @param global The global transaction identifier
     * @param branch The branch qualifier
     * @return An equivalent transaction identifier
     */
    public static Xid importXid( int formatId, byte[] global, byte[] branch )
    {
        return new ExternalXid( formatId, global, branch );
    }


    /**
     * Creates a new transaction branch. Returns a transaction identifier that
     * uses the same format identiier and global transaction identifier but
     * includes a new branch qualifier.
     *
     * @param xid An existing transaction identifier
     * @return A new branch for that transaction
     */
    public static Xid newBranch( Xid xid )
    {
        if ( xid == null )
            throw new IllegalArgumentException( "Argument xid is null" );
        if ( xid instanceof BaseXid )
            return ( (BaseXid) xid ).newBranch();
        return new ExternalXid( xid.getFormatId(), xid.getGlobalTransactionId(), UUID.createBinary() );
    }


    /**
     * Returns true if the transaction identifier represents a local
     * transaction. A local transaction has no global transaction identifier.
     *
     * @param xid A transaction identifier
     * @return True if a local transaction
     */
    public static boolean isLocal( Xid xid )
    {
        byte[] global;

        if ( xid == null )
            throw new IllegalArgumentException( "Argument xid is null" );
        if ( xid instanceof LocalXid )
            return true;
        global = xid.getGlobalTransactionId();
        return ( global == null || global.length == 0 );
    }


    /**
     * Returns true if the transaction identifier represents a global
     * transaction. A global transaction must have a global transaction
     * identifier.
     *
     * @param xid A transaction identifier
     * @return True if a global transaction
     */
    public static boolean isGlobal( Xid xid )
    {
        byte[] global;

        if ( xid == null )
            throw new IllegalArgumentException( "Argument xid is null" );
        if ( xid instanceof GlobalXid || xid instanceof BranchXid )
            return true;
        global = xid.getGlobalTransactionId();
        return ( global != null && global.length != 0 );
    }


    /**
     * Returns true if the transaction identifier represents a branch
     * transaction. A branch transaction must have a branch qualifier.
     *
     * @param xid A transaction identifier
     * @return True if a branch transaction
     */
    public static boolean isBranch( Xid xid )
    {
        byte[] global;

        if ( xid == null )
            throw new IllegalArgumentException( "Argument xid is null" );
        if ( xid instanceof LocalXid || xid instanceof BranchXid )
            return true;
        global = xid.getGlobalTransactionId();
        return ( global == null || global.length == 0 );
    }


    /**
     * Converts an <tt>Xid</tt> into a string representation.
     *
     * @param xid A transaction identifier
     * @return The string representation
     */
    public static String toString( Xid xid )
    {
        StringBuffer buffer;
        int          formatId;
        byte[]       global;
        byte[]       branch;

        if ( xid == null )
            throw new IllegalArgumentException( "Argument xid is null" );
        if ( xid instanceof BaseXid )
            return xid.toString();

        formatId = xid.getFormatId();
        global = xid.getGlobalTransactionId();
        branch = xid.getBranchQualifier();
        buffer = new StringBuffer();
        buffer.append( XID_PREFIX );
        buffer.append( HEX_DIGITS[ (int) ( ( formatId >> 28 ) & 0x0F ) ] );
        buffer.append( HEX_DIGITS[ (int) ( ( formatId >> 24 ) & 0x0F ) ] );
        buffer.append( HEX_DIGITS[ (int) ( ( formatId >> 20 ) & 0x0F ) ] );
        buffer.append( HEX_DIGITS[ (int) ( ( formatId >> 16 ) & 0x0F ) ] );
        buffer.append( HEX_DIGITS[ (int) ( ( formatId >> 12 ) & 0x0F ) ] );
        buffer.append( HEX_DIGITS[ (int) ( ( formatId >> 8 ) & 0x0F ) ] );
        buffer.append( HEX_DIGITS[ (int) ( ( formatId >> 4 ) & 0x0F ) ] );
        buffer.append( HEX_DIGITS[ (int) ( formatId & 0x0F ) ] );
        buffer.append( '-' );
        if ( global != null && global.length > 0 ) {
            for ( int i = global.length ; i-- > 0 ; ) {
                buffer.append( HEX_DIGITS[ ( global[ i ] & 0xF0 ) >> 4 ] );
                buffer.append( HEX_DIGITS[ ( global[ i ] & 0x0F ) ] );
            }
        }
        if ( branch != null && branch.length > 0 ) {
            buffer.append( '-' );
            for ( int i = branch.length ; i-- > 0 ; ) {
                buffer.append( HEX_DIGITS[ ( branch[ i ] & 0xF0 ) >> 4 ] );
                buffer.append( HEX_DIGITS[ ( branch[ i ] & 0x0F ) ] );
            }
        }
        return buffer.toString();
    }


    /**
     * Constructs a transaction identifier from a string representation.
     * The identifier syntax is <tt>xid:<format>-[<global>][-<branch>]</tt>.
     * An exception is thrown if the identifier does not match this format.
     *
     * @param identifier The transaction identifier
     * @throws InvalidXidException The indentifier is invalid
     */
    public static Xid parse( String identifier )
        throws InvalidXidException
    {
        int    formatId;
        byte[] global;
        byte[] branch;
        int    offset;
        int    length;
        char   digit1;
        char   digit2;
        byte[] bytes;
        int    count;

        if ( identifier == null )
            throw new IllegalArgumentException( "Argument identifier is null" );
        if ( ! identifier.startsWith( XID_PREFIX ) )
            throw new InvalidXidException( Messages.format( "tyrex.util.idInvalidPrefix",
                                                            XID_PREFIX, identifier ) );
        offset = XID_PREFIX.length();
        length = identifier.length();
        formatId = 0;
        while ( offset < length ) {
            digit1 = identifier.charAt( offset );
            ++offset;
            if ( digit1 == '-' )
                break;
            if ( offset == length )
                throw new InvalidXidException( Messages.format( "tyrex.util.idInvalidOddDigits",
                                                                identifier ) );
            digit2 = identifier.charAt( offset );
            ++offset;
            formatId = ( formatId << 8 ) + getByte( digit1, digit2, identifier );
        }
        if ( formatId == -1 )
            throw new InvalidXidException( "Null transaction identifier is invalid" );
        if ( offset == length )
            throw new InvalidXidException( "Transaction identifier missing global transaction identifier" );
        bytes = new byte[ Xid.MAXGTRIDSIZE ];
        for ( count = 0 ; offset < length ; ++count ) {
            if ( count == Xid.MAXGTRIDSIZE )
                throw new InvalidXidException( "Transaction identifier exceeding maximum length allowed" );
            digit1 = identifier.charAt( offset );
            ++offset;
            if ( digit1 == '-' )
                break;
            if ( offset == length )
                throw new InvalidXidException( Messages.format( "tyrex.util.idInvalidOddDigits",
                                                                identifier ) );
            digit2 = identifier.charAt( offset );
            ++offset;
            bytes[ count ] = getByte( digit1, digit2, identifier );
        }
        if ( count == Xid.MAXGTRIDSIZE )
            global = bytes;
        else if ( count == 0 )
            global = EMPTY_ARRAY;
        else {
            global = new byte[ count ];
            for ( int i = count ; i-- > 0 ; )
                global[ i ] = bytes[ i ];
        }
        if ( offset == length )
            branch = EMPTY_ARRAY;
        else {
            bytes = new byte[ Xid.MAXBQUALSIZE ];
            for ( count = 0 ; offset < length ; ++count ) {
                if ( count == Xid.MAXBQUALSIZE )
                    throw new InvalidXidException( "Branch qualifier exceeding maximum length allowed" );
                digit1 = identifier.charAt( offset );
                ++offset;
                if ( digit1 == '-' )
                    break;
                if ( offset == length )
                    throw new InvalidXidException( Messages.format( "tyrex.util.idInvalidOddDigits",
                                                                    identifier ) );
                digit2 = identifier.charAt( offset );
                ++offset;
                bytes[ count ] = getByte( digit1, digit2, identifier );
            }
            if ( count == Xid.MAXBQUALSIZE )
                branch = bytes;
            else if ( count == 0 )
                branch = EMPTY_ARRAY;
            else {
                branch = new byte[ count ];
                for ( int i = count ; i-- > 0 ; )
                    branch[ i ] = bytes[ i ];
            }
        }
        
        // Local format id and no global transaction identifier - local Xid.
        if ( formatId == LocalXid.LOCAL_FORMAT_ID &&
             global == EMPTY_ARRAY && branch != EMPTY_ARRAY )
            return new LocalXid( identifier, branch );
        // Global format id and no branch qualifier - global Xid.
        // Global format id and branch qualifier - branch Xid.
        if ( formatId == GlobalXid.GLOBAL_FORMAT_ID &&
             global != EMPTY_ARRAY ) {
            if ( branch == EMPTY_ARRAY )
                return new GlobalXid( identifier, global );
            else
                return new BranchXid( identifier, global, branch );
        }
        // Anything else is an external Xid.
        return new ExternalXid( identifier, formatId, global, branch );
    }


    private static byte getByte( char digit1, char digit2, String identifier )
        throws InvalidXidException
    {
        byte nibble;

        if ( digit1 >= '0' && digit1 <= '9' )
            nibble = (byte) ( ( digit1 - '0' ) << 4 );
        else if ( digit1 >= 'A' && digit1 <= 'F' )
            nibble = (byte) ( ( digit1 - ( 'A' - 0x0A ) ) << 4 );
        else if ( digit1 >= 'a' && digit1 <= 'f' )
            nibble = (byte) ( ( digit1 - ( 'a' - 0x0A ) ) << 4 );
        else 
            throw new InvalidXidException( Messages.format( "tyrex.util.idInvalidCharacter",
                                                            String.valueOf( digit1 ), identifier ) );
        if ( digit2 >= '0' && digit2 <= '9' )
                nibble = (byte) ( nibble | ( digit2 - '0' ) );
        else if ( digit2 >= 'A' && digit2 <= 'F' )
            nibble = (byte) ( nibble | ( digit2 - ( 'A' - 0x0A ) ) );
        else if ( digit2 >= 'a' && digit2 <= 'f' )
            nibble = (byte) ( nibble | ( digit2 - ( 'a' - 0x0A ) ) );
        else 
            throw new InvalidXidException( Messages.format( "tyrex.util.idInvalidCharacter",
                                                            String.valueOf( digit2 ), identifier ) );
        return nibble;
    }


}

