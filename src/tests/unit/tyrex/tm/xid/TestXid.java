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
 * $Id: TestXid.java,v 1.1 2001/09/14 07:30:33 mills Exp $
 */


package tyrex.tm.xid;


import javax.transaction.xa.Xid;
import tyrex.services.UUID;
import tyrex.util.Messages;


/**
 * This is a copy of ExternalXid but not based on BaseXid.  The reason
 * for this is to extend the coverage of the equals() methods of
 * ...Xid.
 * 
 * @author <a href="mills@intalio.com">David Mills</a>
 * @version $Revision: 1.1 $
 */

public final class TestXid
    implements Xid
{
    /**
     * Prefix for textual identifier.
     */
    public static final String XID_PREFIX = "xid:";


    /**
     * Format identifier for all internal Xids or newly created Xids.
     */
    public static final int FORMAT_ID = BaseXid.FORMAT_ID;


    /**
     * The default branch is always an empty byte array.
     */
    protected static final byte[]  EMPTY_ARRAY = new byte[0];


    /**
     * Efficient mapping from 4 bit value to lower case hexadecimal digit.
     */
    protected final static char[]  HEX_DIGITS
    = new char[] {'0', '1', '2', '3', '4', '5', '6', '7',
                  '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    /**
     * The textual representation of the transaction identifier.
     */
    protected String _string;


    /**
     * The format identifier.
     */
    private final int _formatId;

    
    /**
     * The global identifier for this transaction.
     */
    private final byte[] _global;


    /**
     * The branch qualifier for this transaction.
     */
    private final byte[] _branch;


    /**
     * Construct a new transaction identifier. The format identifier must
     * not be -1, this value is reserved for the null transaction.
     *
     * @param format The format identifier
     * @param global The global transaction identifier
     * @param branch The branch qualifier
     */
    public TestXid(int formatId, byte[] global, byte[] branch)
    {
        StringBuffer buffer;

        if (formatId == -1)
        {
            throw new IllegalArgumentException("Argument format is -1");
        }
        _formatId = formatId;
        buffer = new StringBuffer(14);
        buffer.append(XID_PREFIX);
        buffer.append(HEX_DIGITS[(int) ((formatId >> 28) & 0x0F)]);
        buffer.append(HEX_DIGITS[(int) ((formatId >> 24) & 0x0F)]);
        buffer.append(HEX_DIGITS[(int) ((formatId >> 20) & 0x0F)]);
        buffer.append(HEX_DIGITS[(int) ((formatId >> 16) & 0x0F)]);
        buffer.append(HEX_DIGITS[(int) ((formatId >> 12) & 0x0F)]);
        buffer.append(HEX_DIGITS[(int) ((formatId >> 8) & 0x0F)]);
        buffer.append(HEX_DIGITS[(int) ((formatId >> 4) & 0x0F)]);
        buffer.append(HEX_DIGITS[(int) (formatId & 0x0F)]);
        buffer.append('-');
        if (global == null || global.length == 0)
        {
            _global = EMPTY_ARRAY;
        }
        else
        {
            _global = global;
            for (int i = global.length ; i-- > 0 ;)
            {
                buffer.append(HEX_DIGITS[(global[i] & 0xF0) >> 4]);
                buffer.append(HEX_DIGITS[(global[i] & 0x0F)]);
            }
        }
        if (branch == null || branch.length == 0)
        {
            _branch = EMPTY_ARRAY;
        }
        else
        {
            buffer.append('-');
            _branch = branch;
            for (int i = branch.length ; i-- > 0 ;)
            {
                buffer.append(HEX_DIGITS[(branch[i] & 0xF0) >> 4]);
                buffer.append(HEX_DIGITS[(branch[i] & 0x0F)]);
            }
        }
        _string = buffer.toString();
    }


    /**
     * Constructs a new transaction identifier from an existing transaction
     * identifier. This constructor is used when importing external transaction
     * identiiers.
     *
     * @param xid The existing transaction identifier
     */
    public TestXid(Xid xid)
    {
        this(xid.getFormatId(), xid.getGlobalTransactionId(),
             xid.getBranchQualifier());
    }


    /**
     * Used by {@link XidUtils}.
     */
    TestXid(String identifier, int formatId, byte[] global, byte[] branch)
    {
        _formatId = formatId;
        _string = identifier;
        _global = global;
        _branch = branch;
    }


    public String toString()
    {
        return _string;
    }

    public int hashCode()
    {
        return _string.hashCode();
    }

    public int getFormatId()
    {
        return _formatId;
    }


    public byte[] getGlobalTransactionId()
    {
        return _global;
    }


    public byte[] getBranchQualifier()
    {
        return _branch;
    }


    protected static final char[] createPrefix(int formatId)
    {
        int    offset;
        char[] prefix;

        offset = XID_PREFIX.length();
        prefix = new char[offset + 9];
        for (int i = 0 ; i < offset ; ++i)
        {
            prefix[i] = XID_PREFIX.charAt(i);
        }
        prefix[offset    ] = HEX_DIGITS[(int) ((formatId >> 28) & 0x0F)];
        prefix[offset + 1] = HEX_DIGITS[(int) ((formatId >> 24) & 0x0F)];
        prefix[offset + 2] = HEX_DIGITS[(int) ((formatId >> 20) & 0x0F)];
        prefix[offset + 3] = HEX_DIGITS[(int) ((formatId >> 16) & 0x0F)];
        prefix[offset + 4] = HEX_DIGITS[(int) ((formatId >> 12) & 0x0F)];
        prefix[offset + 5] = HEX_DIGITS[(int) ((formatId >> 8) & 0x0F)];
        prefix[offset + 6] = HEX_DIGITS[(int) ((formatId >> 4) & 0x0F)];
        prefix[offset + 7] = HEX_DIGITS[(int) (formatId & 0x0F)];
        prefix[offset + 8] = '-';
        return prefix;
    } 


    public boolean equals(Object other)
    {
        Xid     xid;
        byte[]  bytes;

        if (other == this)
        {
            return true;
        }
        if (other instanceof BaseXid)
        {
            return _string.equals(((BaseXid) other)._string);
        }
        if (other instanceof Xid)
        {
            xid = (Xid) other;
            if (xid.getFormatId() != _formatId)
            {
                return false;
            }
            // Global transaction identifier might be empty.
            bytes = xid.getGlobalTransactionId();
            if (bytes == null)
            {
                if (_global.length != 0)
                {
                    return false;
                }
            }
            else
            {
                if (bytes.length != _global.length)
                {
                    return false;
                }
                for (int i = bytes.length ; i-- > 0 ;)
                {
                    if (bytes[i] != _global[i])
                    {
                        return false;
                    }
                }
            }
            // Branch qualifier might be empty.
            bytes = xid.getBranchQualifier();
            if (bytes == null)
            {
                if (_branch.length != 0)
                {
                    return false;
                }
            }
            else
            {
                if (bytes.length != _branch.length)
                {
                    return false;
                }
                for (int i = bytes.length ; i-- > 0 ;)
                {
                    if (bytes[i] != _branch[i])
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }


    public Xid newBranch()
    {
        return new TestXid(_formatId, _global, UUID.createBinary());
    }
}

