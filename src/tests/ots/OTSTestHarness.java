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
 * $Id: OTSTestHarness.java,v 1.1 2001/02/09 20:46:10 jdaniel Exp $
 */

package tests.ots;

import org.exolab.jtf.CWTestCategory;
import org.exolab.jtf.CWTestCase;
import org.exolab.jtf.CWBaseApplication;
import org.exolab.exceptions.CWClassConstructorException;
import java.util.Vector;
import java.util.Enumeration;

public class OTSTestHarness
    extends CWBaseApplication
{


    static Vector _categories = new Vector();


    static
    {
        _categories.addElement( tests.ots.OTS.class.getName() );
    }



    static public void main( String args[] )
    {
        try {
            OTSTestHarness harness;
            harness = new OTSTestHarness();
            harness.run( args );
        } catch ( Exception except ) {
            except.printStackTrace();
        }
    }


    public OTSTestHarness()
        throws CWClassConstructorException
    {
        super( "Tyrex-OTS" );
    }


    protected java.util.Enumeration getCategoryClassNames()
    {
        return _categories.elements();
    }


    protected String getApplicationName()
    {
        return getClass().getName();
    }


}

