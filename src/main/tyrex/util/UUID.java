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

package tyrex.util;


import java.util.Properties;
import java.util.Random;
import java.util.Date;
import java.util.HashMap;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Universally Unique Identifier (UUID) generator.
 * <p>
 * A UUID is an identifier that is unique across both space and time, with
 * respect to the space of all UUIDs. A UUID can be used for objects with an
 * extremely short lifetime, and to reliably identifying very persistent
 * objects across a network. UUIDs are 128 bit values and encoded into 36
 * characters.
 * <p>
 * This generator produces time-based UUIDs based on the varient specified
 * in an IETF draft from February 4, 1998.
 * <p>
 * Unprefixed identifiers are generated by calling {@link #create() create}.
 * A method is also provided to create prefixed identifiers. Prefixed identifiers
 * are useful for logging and associating identifiers to application objects.
 * <p>
 * To assure that identifiers can be presisted, identifiers must be kept within
 * the limit of {@link #MAXIMUM_LENGTH} characters. This includes both prefixed
 * identifiers and identifiers created by the application. The {@link #trim trim}
 * method can be used to trim an identifier to the maximum allowed length. If an
 * identifier was generated with no prefix, or with the maximum supported prefix
 * legnth, the identifier is guaranteed to be short enough and this method is not
 * required.
 * <p>
 * Convenience methods are also provided for converting an identifier to and from
 * an array of bytes. The conversion methods assume that the identifier is a
 * prefixed or unprefixed encoding of the byte array as pairs of hexadecimal
 * digits.
 * <p>
 * The UUID specification prescribes the following format for representing
 * UUIDs. Four octets encode the low field of the time stamp, two octects encode
 * the middle field of the timestamp, and two octets encode the high field of
 * the timestamp with the version number. Two octets encode the clock sequence
 * number and six octets encode the unique node identifier.
 * <p>
 * The timestamp is a 60 bit value holding UTC time as a count of 100
 * nanosecond intervals since October 15, 1582. UUIDs generated in this manner
 * are guaranteed not to roll over until 3400 AD.
 * <p>
 * The clock sequence is used to help avoid duplicates that could arise when
 * the clock is set backward in time or if the node ID changes. Although the
 * system clock is guaranteed to be monotonic, the system clock is not guaranteed
 * to be monotonic across system failures. The UUID cannot be sure that no UUIDs
 * were generated with timestamps larger than the current timestamp.
 * <p>
 * If the clock sequence can be determined at initialization, it is incremented
 * by one, otherwise it is set to a random number. The clock sequence MUST be
 * originally (i.e. once in the lifetime of a system) initialized to a random
 * number to minimize the correlation across systems. The initial value must not
 * be correlated to the node identifier.
 * <p>
 * The node identifier must be unique for each UUID generator. This is
 * accomplished using the IEEE 802 network card address. For systems with multiple
 * IEEE 802 addresses, any available address can be used. For systems with no
 * IEEE address, a 48 bit random value is used and the multicast bit is set
 * so it will never conflict with addresses obtained from network cards.
 * <p>
 * The UUID state consists of the node identifier and clock sequence. The state
 * need only be read once when the generator is initialized, and the clock
 * sequence must be incremented and stored back. If the UUID state cannot be
 * read and updated, a random number is used.
 * <p>
 * The UUID state is maintained in a fail called {@link #UUID_STATE_FILE}.
 * The file name can be overriden from the configuration property {@link
 * Configuration#PROPERTY_UUID_STATE_FILE}. The UUID state file contains two properties.
 * The node identifier ({@link #PROPERTY_NODE_IDENTIFIER}) is a 48 bit hexadecimal
 * value. The clock sequence ({@link #PROPERTY_CLOCK_SEQUENCE} is a 12 bit
 * hexadecimal value.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version 1.0
 */
public final class UUID 
{


    /**
     * Default prefix that can be used by identifiers. This prefix is not added to
     * identifiers created using {@link #create() create}. Identifiers created using
     * {@link #create(String) create(String)} may use this prefix to denote an identifier.
     * The value of this variable is <code>ID:</code>.
     */
    public static final String    PREFIX   = "ID:";


    /**
     * The identifier resolution in bytes. Identifiers are 16-byte long, or 128 bits.
     * Without a prefix, an identifier can be represented as 36 hexadecimal digits
     * and hyphens. (4 hyphens are used in the UUID format)
     */
    public static final int       RESOLUTION_BYTES = 16;


    /**
     * The maximum length of an identifier in textual form. Prefixed identifiers and
     * application identifiers must be less or equal to the maximum length to allow
     * persistence. This maximum length is 64 characters.
     */
    public static final int       MAXIMUM_LENGTH = 64;


    /**
     * The maximum length of an identifier prefix. Identifiers created using {@link
     * #create(String) create(String)} with a prefix that is no longer than the maximum
     * prefix size are guaranteed to be within the maximum length allowed and need not
     * be trimmed.
     */
    public static final int       MAXIMUM_PREFIX = 28;


    /**
     * UUID state file property that determined the node identifier.
     * The value of this property is an hexadecimal 47-bit value.
     * The name of this property is <tt>uuid.nodeIdentifier</tt>.
     */
    public static final String    PROPERTY_NODE_IDENTIFIER = "uuid.nodeIdentifier";


    /**
     * UUID state file property that determined the clock sequence.
     * The value of this property is an hexadecimal 12-bit value.
     * The name of this property is <tt>uuid.clockSequence</tt>.
     */
    public static final String    PROPERTY_CLOCK_SEQUENCE = "uuid.clockSequence";


    /**
     * Name of the UUID state file. If no file was specified in the
     * configuration properties, this file name is used. The file name
     * is <tt>uuid.state</tt>.
     */
    public static final String    UUID_STATE_FILE = "uuid.state";


    /**
     * The variant value determines the layout of the UUID. This variant
     * is specified in the IETF February 4, 1998 draft. Used for
     * character octets.
     */
    private static final int     UUID_VARIANT_OCTET        = 0x08;


    /**
     * The variant value determines the layout of the UUID. This variant
     * is specified in the IETF February 4, 1998 draft. Used for
     * byte array.
     */
    private static final int     UUID_VARIANT_BYTE         = 0x80;


    /**
     * The version identifier for a time-based UUID. This version
     * is specified in the IETF February 4, 1998 draft. Used for
     * character octets.
     */
    private static final int     UUID_VERSION_CLOCK_OCTET  = 0x01;


    /**
     * The version identifier for a time-based UUID. This version
     * is specified in the IETF February 4, 1998 draft. Used for
     * byte array.
     */
    private static final int     UUID_VERSION_CLOCK_BYTE   = 0x10;


    /**
     * The version identifier for a name-based UUID. This version
     * is specified in the IETF February 4, 1998 draft. Used for
     * character octets.
     */
    private static final int     UUID_VERSION_NAME_OCTET   = 0x03;


    /**
     * The version identifier for a name-based UUID. This version
     * is specified in the IETF February 4, 1998 draft. Used for
     * byte array.
     */
    private static final int     UUID_VERSION_NAME_BYTE    = 0x30;


    /**
     * The version identifier for a random-based UUID. This version
     * is specified in the IETF February 4, 1998 draft. Used for
     * character octets.
     */
    private static final int     UUID_VERSION_RANDOM_CLOCK = 0x04;


    /**
     * The version identifier for a random-based UUID. This version
     * is specified in the IETF February 4, 1998 draft. Used for
     * byte array.
     */
    private static final int     UUID_VERSION_RANDOM_BYTE  = 0x40;


    /**
     * The difference between Java clock and UUID clock. Java clock is base
     * time is January 1, 1970. UUID clock base time is October 15, 1582.
     */
    private static final long    JAVA_UUID_CLOCK_DIFF = 0x0b1d069b5400L;


    /**
     * Efficient mapping from 4 bit value to lower case hexadecimal digit.
     */
    private final static char[] HEX_DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7',
                                                          '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };


    /**
     * The number of UUIDs that can be generated per clock tick. UUID assumes
     * a clock tick every 100 nanoseconds. The actual clock ticks are measured
     * in milliseconds and based on the sync-every property of the clock.
     * The product of these two values is used to set this variable.
     */
    private static int           _uuidsPerTick;


    /**
     * The number of UUIDs generated in this clock tick. This counter is reset
     * each time the clock is advanced a tick. When it reaches the maximum number
     * of UUIDs allowed per tick, we block until the clock advances.
     */
    private static int           _uuidsThisTick;


    /**
     * The last clock. Whenever the clock changes, we record the last clock to
     * identify when we get a new clock, or when we should increments the
     * UUIDs per tick counter.
     */
    private static long          _lastClock;


    /**
     * The clock sequence. The clock sequence is obtained from the UUID properties
     * and incremented by one each time we boot, or is generated randomaly if missing
     * in the UUID properties. The clock sequence is made of four hexadecimal digits.
     */
    private static char[]        _clockSeqOctet;


    /**
     * The clock sequence. The clock sequence is obtained from the UUID properties
     * and incremented by one each time we boot, or is generated randomaly if missing
     * in the UUID properties. The clock sequence is made of two bytes.
     */
    private static byte[]        _clockSeqByte;


    /**
     * The node identifier. The node identifier is obtained from the UUID properties,
     * or is generated if missing in the UUID properties. The node identifier is made
     * of twelve hexadecimal digits.
     */
    private static char[]        _nodeIdentifierOctet;


    /**
     * The node identifier. The node identifier is obtained from the UUID properties,
     * or is generated if missing in the UUID properties. The node identifier is made
     * of six bytes.
     */
    private static byte[]        _nodeIdentifierByte;


    /**
     * Creates and returns a new identifier.
     *
     * @return An identifier
     */
    public static String create()
    {
        return String.valueOf( createTimeUUIDChars() );
    }


    /**
     * Creates and returns a new prefixed identifier.
     * <p>
     * This method is equivalent to <code>prefix + create()</code>.
     *
     * @param prefix The prefix to use
     * @return A prefixed identifier
     */
    public static String create( String prefix )
    {
        StringBuffer buffer;

        if ( prefix == null )
            throw new IllegalArgumentException( "Argument prefix is null" );
        buffer = new StringBuffer( MAXIMUM_LENGTH - MAXIMUM_PREFIX + prefix.length() );
        buffer.append( prefix );
        buffer.append( createTimeUUIDChars() );
        return buffer.toString();
    }


    /**
     * Creates and returns a new identifier.
     *
     * @return An identifier
     */
    public static byte[] createBinary()
    {
        return createTimeUUIDBytes();
    }


    /**
     * Converts a prefixed identifier into a byte array. An exception is thrown
     * if the identifier does not match the excepted textual encoding.
     * <p>
     * The format for the identifier is <code>prefix{nn|-}*</code>: a prefix
     * followed by a sequence of bytes, optionally separated by hyphens.
     * Each byte is encoded as a pair of hexadecimal digits.
     *
     * @param prefix The identifier prefix
     * @param identifier The prefixed identifier
     * @return The identifier as an array of bytes
     * @throws InvalidIDException The identifier does not begin with the prefix,
     * or does not consist of a sequence of hexadecimal digit pairs, optionally
     * separated by hyphens
     */
    public static byte[] toBytes( String prefix, String identifier )
        throws InvalidIDException
    {
        int    index;
        char   digit;
        byte   nibble;
        byte[] bytes;
        byte[] newBytes;

        if ( identifier == null )
            throw new IllegalArgumentException( "Argument identifier is null" );
        if ( prefix == null )
            throw new IllegalArgumentException( "Argument prefix is null" );
        if ( ! identifier.startsWith( prefix ) )
            throw new InvalidIDException( "Invalid prefix" );
        index = 0;
        bytes = new byte[ ( identifier.length() - prefix.length() ) / 2 ];
        for ( int i = prefix.length() ; i < identifier.length() ; ++i ) {
            digit = identifier.charAt( i );
            if ( digit == '-' )
                continue;
            if ( digit >= '0' && digit <= '9' )
                nibble = (byte) ( ( digit - '0' ) << 4 );
            else if ( digit >= 'A' && digit <= 'F' )
                nibble = (byte) ( ( digit - ( 'A' - 0x0A ) ) << 4 );
            else if ( digit >= 'a' && digit <= 'f' )
                nibble = (byte) ( ( digit - ( 'a' - 0x0A ) ) << 4 );
            else 
                throw new InvalidIDException( "Invalid char set" );
            ++i;
            if ( i == identifier.length() )
                throw new InvalidIDException( "Invalid odd digit" );
            digit = identifier.charAt( i );
            if ( digit >= '0' && digit <= '9' )
                nibble = (byte) ( nibble | ( digit - '0' ) );
            else if ( digit >= 'A' && digit <= 'F' )
                nibble = (byte) ( nibble | ( digit - ( 'A' - 0x0A ) ) );
            else if ( digit >= 'a' && digit <= 'f' )
                nibble = (byte) ( nibble | ( digit - ( 'a' - 0x0A ) ) );
            else 
                throw new InvalidIDException( "Invalid Character" );
            bytes[ index ] = nibble;
            ++index;
        }
        if ( index == bytes.length )
            return bytes;
        newBytes = new byte[ index ];
        while ( index-- > 0 )
            newBytes[ index ] = bytes[ index ];
        return newBytes;
    }


    /**
     * Converts an identifier into a byte array. An exception is thrown if the
     * identifier does not match the excepted textual encoding.
     * <p>
     * The format for the identifier is <code>{nn|-}*</code>: a sequence of bytes,
     * optionally separated by hyphens. Each byte is encoded as a pair of hexadecimal
     * digits.
     *
     * @param identifier The identifier
     * @return The identifier as an array of bytes
     * @throws InvalidIDException The identifier does not consist of a sequence of
     * hexadecimal digit pairs, optionally separated by hyphens
     */
    public static byte[] toBytes( String identifier )
        throws InvalidIDException
    {
        int    index;
        char   digit;
        byte   nibble;
        byte[] bytes;
        byte[] newBytes;

        if ( identifier == null )
            throw new IllegalArgumentException( "Argument identifier is null" );
        index = 0;
        bytes = new byte[ identifier.length() / 2 ];
        for ( int i = 0 ; i < identifier.length() ; ++i ) {
            digit = identifier.charAt( i );
            if ( digit == '-' )
                continue;
            if ( digit >= '0' && digit <= '9' )
                nibble = (byte) ( ( digit - '0' ) << 4 );
            else if ( digit >= 'A' && digit <= 'F' )
                nibble = (byte) ( ( digit - ( 'A' - 0x0A ) ) << 4 );
            else if ( digit >= 'a' && digit <= 'f' )
                nibble = (byte) ( ( digit - ( 'a' - 0x0A ) ) << 4 );
            else 
                throw new InvalidIDException( "Invalid Character" );
            ++i;
            if ( i == identifier.length() )
                throw new InvalidIDException( "Invalid odd digits" );
            digit = identifier.charAt( i );
            if ( digit >= '0' && digit <= '9' )
                nibble = (byte) ( nibble | ( digit - '0' ) );
            else if ( digit >= 'A' && digit <= 'F' )
                nibble = (byte) ( nibble | ( digit - ( 'A' - 0x0A ) ) );
            else if ( digit >= 'a' && digit <= 'f' )
                nibble = (byte) ( nibble | ( digit - ( 'a' - 0x0A ) ) );
            else 
                throw new InvalidIDException( "Invalid Character" );
            bytes[ index ] = nibble;
            ++index;
        }
        if ( index == bytes.length )
            return bytes;
        newBytes = new byte[ index ];
        while ( index-- > 0 )
            newBytes[ index ] = bytes[ index ];
        return newBytes;
    }


    /**
     * Converts a byte array into a prefixed identifier. 
     * <p>
     * The format for the identifier is <code>prefix{nn|-}*</code>: a prefix
     * followed by a sequence of bytes, optionally separated by hyphens.
     * Each byte is encoded as a pair of hexadecimal digits.
     *
     * @param prefix The identifier prefix
     * @param byte An array of bytes
     * @return A string representation of the identifier
     */
    public static String fromBytes( String prefix, byte[] bytes )
    {
        StringBuffer buffer;

        if ( prefix == null )
            throw new IllegalArgumentException( "Argument prefix is null" );
        if ( bytes == null || bytes.length == 0 )
            throw new IllegalArgumentException( "Argument bytes is null or an empty array" );
        buffer = new StringBuffer( prefix );
        for ( int i = 0 ; i < bytes.length ; ++i ) {
            buffer.append( HEX_DIGITS[ ( bytes[ i ] & 0xF0 ) >> 4 ] );
            buffer.append( HEX_DIGITS[ ( bytes[ i ] & 0x0F ) ] );
        }
        return buffer.toString();
    }


    /**
     * Converts a byte array into an identifier. 
     * <p>
     * The format for the identifier is <code>{nn|-}*</code>: a sequence
     * of bytes, optionally separated by hyphens. Each byte is encoded as
     * a pair of hexadecimal digits.
     *
     * @param byte An array of bytes
     * @return A string representation of the identifier
     */
    public static String fromBytes( byte[] bytes )
    {
        StringBuffer buffer;

        if ( bytes == null || bytes.length == 0 )
            throw new IllegalArgumentException( "Argument bytes is null or an empty array" );
        buffer = new StringBuffer();
        for ( int i = bytes.length ; i-- > 0 ; ) {
            buffer.append( HEX_DIGITS[ ( bytes[ i ] & 0xF0 ) >> 4 ] );
            buffer.append( HEX_DIGITS[ ( bytes[ i ] & 0x0F ) ] );
        }
        return buffer.toString();
    }


    /**
     * Truncates an identifier so that it does not extend beyond {@link #MAXIMUM_LENGTH}
     * characters in length.
     *
     * @param identifier An identifier
     * @return An identifier trimmed to {@link #MAXIMUM_LENGTH} characters
     */
    public static String trim( String identifier )
    {
        if ( identifier == null )
            throw new IllegalArgumentException( "Argument identifier is null" );
        if ( identifier.length() > MAXIMUM_LENGTH )
            return identifier.substring( 0, MAXIMUM_LENGTH );
        return identifier;
    }


    /**
     * Returns a time-based UUID as a character array. The UUID identifier is
     * always 36 characters long.
     *
     * @return A time-based UUID
     */
    public static char[] createTimeUUIDChars()
    {
        long   clock;
        char[] chars;

        // Acquire lock to assure synchornized generation
        synchronized ( UUID.class ) {
            while ( true ) {
                clock = Clock.clock();
                if ( clock > _lastClock ) {
                    // Clock reading changed since last UUID generated,
                    // reset count of UUIDs generated with this clock.
                    _uuidsThisTick = 0;
                    _lastClock = clock;
                    // Adjust UUIDs per tick in case the clock sleep ticks
                    // have changed.
                    _uuidsPerTick = Clock.getSleepTicks() * 100;
                    break;
                }
                if ( _uuidsThisTick + 1 < _uuidsPerTick ) {
                    // Able to create mode UUIDs for this clock, proceed.
                    ++ _uuidsThisTick;
                    break;
                }
                // UUIDs generated too fast, suspend for a while.
                // Note, we release synchronization at this point,
                // but don't change any variable, so other thread might
                // be able to generate UUID before us.
                try {
                    UUID.class.wait( 100 );
                } catch ( InterruptedException except ) { }
            }                
            
            // Modify Java clock (milliseconds) to UUID clock (100 nanoseconds).
            // Add the count of uuids to low order bits of the clock reading,
            // assuring we get a unique clock.
            clock = ( clock + JAVA_UUID_CLOCK_DIFF ) * 100 + _uuidsThisTick;
            
            chars = new char[ 36 ];
            // Add the low field of the clock (4 octets )
            chars[ 0 ]  = HEX_DIGITS[ (int) ( ( clock >> 28 ) & 0x0F ) ];
            chars[ 1 ]  = HEX_DIGITS[ (int) ( ( clock >> 24 ) & 0x0F ) ];
            chars[ 2 ]  = HEX_DIGITS[ (int) ( ( clock >> 20 ) & 0x0F ) ];
            chars[ 3 ]  = HEX_DIGITS[ (int) ( ( clock >> 16 ) & 0x0F ) ];
            chars[ 4 ]  = HEX_DIGITS[ (int) ( ( clock >> 12 ) & 0x0F ) ];
            chars[ 5 ]  = HEX_DIGITS[ (int) ( ( clock >> 8 ) & 0x0F ) ];
            chars[ 6 ]  = HEX_DIGITS[ (int) ( ( clock >> 4 ) & 0x0F ) ];
            chars[ 7 ]  = HEX_DIGITS[ (int) ( clock & 0x0F ) ];
            chars[ 8 ]  = '-';
            // Add the medium field of the clock (2 octets)
            chars[ 9 ]  = HEX_DIGITS[ (int) ( ( clock >> 44 ) & 0x0F ) ];
            chars[ 10 ] = HEX_DIGITS[ (int) ( ( clock >> 40 ) & 0x0F ) ];
            chars[ 11 ] = HEX_DIGITS[ (int) ( ( clock >> 36 ) & 0x0F ) ];
            chars[ 12 ] = HEX_DIGITS[ (int) ( ( clock >> 32 ) & 0x0F ) ];
            chars[ 13 ] = '-';
            // Add the high field of the clock multiplexed with version number (2 octets)
            chars[ 14 ] = HEX_DIGITS[ (int) ( ( ( clock >> 60 ) & 0x0F ) | UUID_VERSION_CLOCK_OCTET ) ];
            chars[ 15 ] = HEX_DIGITS[ (int) ( ( clock >> 56 ) & 0x0F ) ];
            chars[ 16 ] = HEX_DIGITS[ (int) ( ( clock >> 52 ) & 0x0F ) ];
            chars[ 17 ] = HEX_DIGITS[ (int) ( ( clock >> 48 ) & 0x0F ) ];
            chars[ 18 ] = '-';
            // Add the clock sequence and version identifier (2 octets)
            chars[ 19 ] = _clockSeqOctet[ 0 ];
            chars[ 20 ] = _clockSeqOctet[ 1 ];
            chars[ 21 ] = _clockSeqOctet[ 2 ];
            chars[ 22 ] = _clockSeqOctet[ 3 ];
            chars[ 23 ] = '-';
            // Add the node identifier (6 octets)
            chars[ 24 ] = _nodeIdentifierOctet[ 0 ];
            chars[ 25 ] = _nodeIdentifierOctet[ 1 ];
            chars[ 26 ] = _nodeIdentifierOctet[ 2 ];
            chars[ 27 ] = _nodeIdentifierOctet[ 3 ];
            chars[ 28 ] = _nodeIdentifierOctet[ 4 ];
            chars[ 29 ] = _nodeIdentifierOctet[ 5 ];
            chars[ 30 ] = _nodeIdentifierOctet[ 6 ];
            chars[ 31 ] = _nodeIdentifierOctet[ 7 ];
            chars[ 32 ] = _nodeIdentifierOctet[ 8 ];
            chars[ 33 ] = _nodeIdentifierOctet[ 9 ];
            chars[ 34 ] = _nodeIdentifierOctet[ 10 ];
            chars[ 35 ] = _nodeIdentifierOctet[ 11 ];
        }
        return chars;
    }


    /**
     * Returns a time-based UUID as a character array. The UUID identifier is
     * always 16 bytes long.
     *
     * @return A time-based UUID
     */
    public static byte[] createTimeUUIDBytes()
    {
        long   clock;
        byte[] bytes;

        // Acquire lock to assure synchornized generation
        synchronized ( UUID.class ) {
            while ( true ) {
                clock = Clock.clock();
                if ( clock != _lastClock ) {
                    // Clock reading changed since last UUID generated,
                    // reset count of UUIDs generated with this clock.
                    _uuidsThisTick = 0;
                    _lastClock = clock;
                    // Adjust UUIDs per tick in case the clock sleep ticks
                    // have changed.
                    _uuidsPerTick = Clock.getSleepTicks() * 100;
                    break;
                }
                if ( _uuidsThisTick + 1 < _uuidsPerTick ) {
                    // Able to create mode UUIDs for this clock, proceed.
                    ++ _uuidsThisTick;
                    break;
                }
                // UUIDs generated too fast, suspend for a while.
                // Note, we release synchronization at this point,
                // but don't change any variable, so other thread might
                // be able to generate UUID before us.
                try {
                    UUID.class.wait( 100 );
                } catch ( InterruptedException except ) { }
            }                
            
            // Modify Java clock (milliseconds) to UUID clock (100 nanoseconds).
            // Add the count of uuids to low order bits of the clock reading,
            // assuring we get a unique clock.
            clock = ( clock + JAVA_UUID_CLOCK_DIFF ) * 100 + _uuidsThisTick;
            
            bytes = new byte[ 16 ];
            // Add the low field of the clock (4 octets )
            bytes[ 0 ]  = (byte) ( ( clock >> 24 ) & 0xFF );
            bytes[ 1 ]  = (byte) ( ( clock >> 16 ) & 0xFF );
            bytes[ 2 ]  = (byte) ( ( clock >> 8 ) & 0xFF );
            bytes[ 3 ]  = (byte) ( clock & 0xFF );
            // Add the medium field of the clock (2 octets)
            bytes[ 4 ]  = (byte) ( ( clock >> 40 ) & 0xFF );
            bytes[ 5 ]  = (byte) ( ( clock >> 32 ) & 0xFF );
            // Add the high field of the clock multiplexed with version number (2 octets)
            bytes[ 6 ]  = (byte) ( ( ( clock >> 60 ) & 0xFF ) | UUID_VERSION_CLOCK_BYTE );
            bytes[ 7 ]  = (byte) ( ( clock >> 48 ) & 0xFF );
            // Add the clock sequence and version identifier (2 octets)
            bytes[ 8 ] = _clockSeqByte[ 0 ];
            bytes[ 9 ] = _clockSeqByte[ 1 ];
            // Add the node identifier (6 octets)
            bytes[ 10 ] = _nodeIdentifierByte[ 0 ];
            bytes[ 11 ] = _nodeIdentifierByte[ 1 ];
            bytes[ 12 ] = _nodeIdentifierByte[ 2 ];
            bytes[ 13 ] = _nodeIdentifierByte[ 3 ];
            bytes[ 14 ] = _nodeIdentifierByte[ 4 ];
            bytes[ 15 ] = _nodeIdentifierByte[ 5 ];
        }
        return bytes;
    }

    /**
     * An exception indicating the identifier is invalid and cannot be
     * converted into an array of bytes.
     */
    public static class InvalidIDException
        extends Exception
    {


        public InvalidIDException( String message )
        {
            super( message );
        }


    }



}

