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
 * $Id: LDAPRealm.java,v 1.5 2001/03/12 19:20:19 arkin Exp $
 */


package tyrex.security.ldap;


import java.util.Vector;
import java.net.MalformedURLException;
import netscape.ldap.LDAPUrl;
import netscape.ldap.LDAPConnection;
import netscape.ldap.LDAPEntry;
import netscape.ldap.LDAPException;
import netscape.ldap.LDAPAttribute;
import netscape.ldap.LDAPModification;
import netscape.ldap.LDAPv2;
import netscape.ldap.LDAPSearchResults;
import netscape.ldap.util.DN;
import netscape.ldap.util.RDN;
import javax.security.auth.login.LoginException;
import tyrex.security.container.helper.RolesRealm;


/**
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $ $Date: 2001/03/12 19:20:19 $
 */
public class LDAPRealm
    extends RolesRealm
{


    // XXX members: should contain user DNs
   
 
    public static final String DefaultRealmName = "<default>";
    
    
    private LDAPUrl  _url;
    
    
    private String   _dnMaskStart;
    
    
    private String   _dnMaskEnd;
    
    
    private String   _rolesRDN;
    
    
    LDAPRealm( String realmName, String url, String dnMask, String rolesRDN )
        throws MalformedURLException, LDAPException
    {
        super( realmName, null );
        
        DN     dn;
        RDN    rdn;
        Vector rdns;
        
        _url = new LDAPUrl( url );
        //_url = new LDAPUrl( _url.getHost(), _url.getPort(), null );
        
        dn = new DN( dnMask );
        rdns = dn.getRDNs();
        if ( rdns.size() < 2 )
            throw new IllegalArgumentException( "DN mask " + dnMask + " not of the form x=*,y=z" );
        rdn = (RDN) rdns.elementAt( 0 );
        if ( ! rdn.getValues()[ 0 ].equals( "*" ) )
            throw new IllegalArgumentException( "DN mask " + dnMask + " not of the form x=*,y=z" );
        _dnMaskStart = rdn.getTypes()[ 0 ] + "=";
        _dnMaskEnd = "," + dn.getParent();
        
        _rolesRDN = rolesRDN;
        
        LDAPConnection    conn;
        LDAPSearchResults results;
        LDAPEntry         entry;
        
        conn = new LDAPConnection();
        try {
            conn.connect( _url.getHost(), _url.getPort() );
            results = conn.search( rolesRDN, LDAPv2.SCOPE_ONE, "(objectclass=role)",
                                   new String[] { "role" , "member" }, false );
            while ( results.hasMoreElements() ) {
                LDAPAttribute attr;
                String[]      values;
                String        roleName;
                
                entry = results.next();
                attr = entry.getAttribute( "role" );
                if ( attr != null ) {
                    values = attr.getStringValueArray();
                    for ( int i = 0 ; i < values.length ; ++i ) {
                        roleName = values[ i ];
                        attr = entry.getAttribute( "member" );
                        if ( attr != null )
                            addRole( roleName, attr.getStringValueArray() );
                    }
                }
            }
        } catch ( LDAPException except ) {
            try {
                if ( conn.isConnected() )
                    conn.disconnect();
            } catch ( Exception ex2 ) { }
            // No roles. OK.
            if ( except.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT )
                return;
            throw except;
        }
    }
    

    public int getLDAPPort()
    {
        return _url.getPort();
    }
    
    public String getLDAPHost()
    {
        return _url.getHost();
    }

    public String getLDAPUrl()
    {
        return _url.toString();
    }
    
    
    public boolean isDefaultRealm()
    {
        return getRealmName().equals( DefaultRealmName );
    }
    
    
    public String getDN( String name )
    {
        return _dnMaskStart + name + _dnMaskEnd;
    }
    
    
    public void store( String authDN, String password )
        throws LDAPException
    {
        LDAPConnection conn;
        
        conn = new LDAPConnection();
        try {
            conn.connect( _url.getHost(), _url.getPort() );
            conn.authenticate( authDN, password );
            
            String[] roleNames;
            String[] members;
            String   dn;
            
            roleNames = listRoles();
            for ( int i = 0 ; i < roleNames.length ; ++i ) {
                dn = "role=" + roleNames[ i ] + "," + _rolesRDN;
                members = listMembers( roleNames[ i ] );
                if ( members.length == 0 ) {
                    try {
                        conn.delete( dn );
                    } catch ( LDAPException except ) {
                        if ( except.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT )
                            ; // OK
                        else
                            throw except;
                    }
                } else {
                    LDAPEntry entry;
                    
                    try {
                        entry = conn.read( dn );
                        conn.modify( dn, new LDAPModification( LDAPModification.REPLACE,
                                                               new LDAPAttribute( "member", members ) ) );
                    } catch ( LDAPException except ) {
                        if ( except.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT ) {
                            entry = new LDAPEntry( dn );
                            entry.getAttributeSet().add( new LDAPAttribute( "role", roleNames[ i ] ) );
                            entry.getAttributeSet().add( new LDAPAttribute( "member", members ) );
                            conn.add( entry );
                        } else
                            throw except;
                    }
                }
            }
            
        } finally {
            try {
                if ( conn.isConnected() )
                    conn.disconnect();
            } catch ( Exception ex2 ) { }
        }
    }
    
    
}


