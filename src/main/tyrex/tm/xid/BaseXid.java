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
 * $Id: BaseXid.java,v 1.2 2001/03/12 19:20:20 arkin Exp $
 */


package tyrex.tm.xid;


import javax.transaction.xa.Xid;


/**
 * Base implementation for all xids used by Tyrex (local, global, branch
 * and external). All xids have a string representation in the form
 * <tt>xid:<format>-[<global>][-<branch>]</tt>.
 * 
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $
 */
public abstract class BaseXid
    implements Xid
{


    /**
     * Prefix for textual identifier.
     */
    public static final String   XID_PREFIX = "xid:";


    /**
     * Format identifier for all internal Xids or newly created Xids.
     */
    public static final int      FORMAT_ID = 0xE0FFCE;


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
     * The textual representation of the transaction identifier.
     */
    protected String             _string;


    public String toString()
    {
        return _string;
    }


    public int hashCode()
    {
        return _string.hashCode();
    }


    /**
     * Creates a new transaction branch. A transaction branch has the
     * same format and global transaction identifier as this transaction
     * identifier, but a new unique branch qualifier (never empty).
     *
     * @return A new transaction branch
     */
    public abstract Xid newBranch();


    protected static final char[] createPrefix( int formatId )
    {
        int    offset;
        char[] prefix;

        offset = XID_PREFIX.length();
        prefix = new char[ offset + 9 ];
        for ( int i = 0 ; i < offset ; ++i )
            prefix[ i ] = XID_PREFIX.charAt( i );
        prefix[ offset     ] = HEX_DIGITS[ (int) ( ( formatId >> 28 ) & 0x0F ) ];
        prefix[ offset + 1 ] = HEX_DIGITS[ (int) ( ( formatId >> 24 ) & 0x0F ) ];
        prefix[ offset + 2 ] = HEX_DIGITS[ (int) ( ( formatId >> 20 ) & 0x0F ) ];
        prefix[ offset + 3 ] = HEX_DIGITS[ (int) ( ( formatId >> 16 ) & 0x0F ) ];
        prefix[ offset + 4 ] = HEX_DIGITS[ (int) ( ( formatId >> 12 ) & 0x0F ) ];
        prefix[ offset + 5 ] = HEX_DIGITS[ (int) ( ( formatId >> 8 ) & 0x0F ) ];
        prefix[ offset + 6 ] = HEX_DIGITS[ (int) ( ( formatId >> 4 ) & 0x0F ) ];
        prefix[ offset + 7 ] = HEX_DIGITS[ (int) ( formatId & 0x0F ) ];
        prefix[ offset + 8 ] = '-';
        return prefix;
    } 


}

