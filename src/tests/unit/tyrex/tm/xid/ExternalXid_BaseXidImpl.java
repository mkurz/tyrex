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
 * $Id: ExternalXid_BaseXidImpl.java,v 1.3 2001/09/12 13:36:25 mills Exp $
 */

package tyrex.tm.xid;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.PrintWriter;


/**
 *
 * @author <a href="mailto:mills@intalio.com">David Mills</a>
 * @version $Revision: 1.3 $
 */

public class ExternalXid_BaseXidImpl extends BaseXidTest
{
    private byte[] _global = new byte[] {(byte)0xA8, (byte)0xB7, (byte)0xC6,
                                        (byte)0xD5, (byte)0xE4, (byte)0xF3};
    private byte[] _branch = new byte[] {(byte)0x9F, (byte)0x8E, (byte)0x7D,
                                        (byte)0x6C, (byte)0x5B, (byte)0x4A};

    public ExternalXid_BaseXidImpl(String name)
    {
        super(name);
    }

    /**
     * The method for creating an instance of BaseXid.
     */

    public BaseXid newBaseXid()
        throws Exception
    {
        return (BaseXid) new ExternalXid(BaseXid.FORMAT_ID, _global, _branch);
    }


    /**
     * <p>The method for returning a String representation of
     * BaseXid.</p>
     */

    public String getStringXid(BaseXid xid)
        throws Exception
    {
        char[] pref = xid.createPrefix(BaseXid.FORMAT_ID);
        return new String(pref) + tyrex.Unit.byteArrayToString(_global) + "-"
            + tyrex.Unit.byteArrayToString(_branch);
    }


    /**
     * <p>Whether the class (the test's newBaseXid() method) is
     * expected to produce unique ids.</p> */

    public boolean uniqueIds()
        throws Exception
    {
        return false;
    }
}
