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
 * $Id: LDAPPrincipal.java,v 1.4 2001/03/12 19:20:19 arkin Exp $
 */


package tyrex.security.ldap;


import java.io.Serializable;
import java.security.Principal;


/**
 * The LDAP DN prinicipal. Provides the LDAP distinguished name by which
 * an account is known (e.g. <tt>'uid=Joe.Smith,ou=People,dc=acme,dc=com'</tt>).
 * Immutable and serializable.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $ $Date: 2001/03/12 19:20:19 $
 */
public final class LDAPPrincipal
    implements Principal
{


    private String  _dn;
    
    
    public LDAPPrincipal( String dn )
    {
        if ( dn == null )
            throw new IllegalArgumentException( "Argument 'dn' is null" );
        _dn = dn;
    }
    
    
    public String getName()
    {
        return _dn;
    }
    
    
    public String toString()
    {
        return "DN: " + _dn;
    }
    
    
    public int hashCode()
    {
        return _dn.hashCode();
    }
    
    
    public boolean equals( Object object )
    {
        if ( object == this )
            return true;
        if ( object == null )
            return false;
        if ( ! ( object instanceof LDAPPrincipal ) )
            return false;
        return _dn.equals( ( (LDAPPrincipal) object )._dn );
    }
    
    
}
