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
 * Contributions by MetaBoss team are Copyright (c) 2003-2004, Softaris Pty. Ltd. All Rights Reserved.
 *
 * $Id: LDAPLoginModule.java,v 1.6 2004/04/30 06:33:03 metaboss Exp $
 */


package tyrex.security.ldap;


import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import netscape.ldap.LDAPAttribute;
import netscape.ldap.LDAPConnection;
import netscape.ldap.LDAPEntry;
import netscape.ldap.LDAPException;
import tyrex.security.NamePasswordCredentials;
import tyrex.security.container.RoleCredentials;
import tyrex.security.container.helper.EmailPrincipal;
import tyrex.util.logging.Logger;


public class LDAPLoginModule
    implements LoginModule
{


    /**
     * The LDAP URL (<tt>ldap-url</tt>). The URL of the LDAP server includes
     * the server's host name and port number (if not the default), but no
     * root DN. For example, <tt>ldap://intalio.com</tt>.
     */
    public static final String OPTION_LDAP_URL = "ldap-url";
        
        
    /**
     * The DN mask (<tt>dn-mask</tt>). The mask for constructing a DN given
     * the account name, using an asterisk to represent the account name.
     * For example, <tt>uid=*,ou=People,dc=intalio,dc=com</tt>.
     */
    public static final String OPTION_DN_MASK = "dn-mask";
        
        
    /**
     * The roles RDN (<tt>roles-rdn</tt>). The relative DN underneath which
     * all roles are listed. For example, <tt>ou=Roles,dc=intalio,dc=com</tt>.
     */
    public static final String OPTION_ROLES_RDN = "roles-rdn";
        
        
    /**
     * The name of the realm (<tt>realm</tt>). This module configuration
     * represents a realm and only users in that realm are authenticated.
     * This option may be null if the realm is unknown. For example,
     * <tt>intalio.com</tt>.
     */
    public static final String OPTION_REALM = "realm";
    
    
    /**
     * Log errors (<tt>log-errors</tt>). If this option is specified,
     * initialization errors are logged to the console.
     */
    public static final String OPTION_LOG_ERRORS = "log-errors";
    
    
    private static final String MODULE_NAME = "LDAPLoginModule";
    
    
    /**
     *
     */
    private Subject             _subject;
    
    
    private LDAPRealm            _realm;
    
    
    /**
     *
     */
    private Map                 _options;
    
    
    private LDAPCredentials       _ldapCreds;
    
    
    private RoleCredentials       _roleCreds;
    
    
    /**
     * A list of all the principals generated during login and added to the subject.
     */
    private Vector                _princs = new Vector();
    
    
    
    
    
    public void initialize( Subject subject, CallbackHandler handler,
                            Map sharedState, Map options )
    {
        _subject = subject;
        _options = options;
        
        String realmName;
        
        realmName = (String) _options.get( OPTION_REALM );
        if ( realmName == null )
            realmName = LDAPRealm.DEFAULT_REALM_NAME;
        synchronized ( sharedState ) {
            _realm = (LDAPRealm) sharedState.get( realmName );
            if ( _realm == null ) {
                try {
                    _realm = new LDAPRealm( realmName, (String) _options.get( OPTION_LDAP_URL ),
                                            (String) _options.get( OPTION_DN_MASK ),
                                            (String) _options.get( OPTION_ROLES_RDN ) );
                } catch ( Exception except ) {
                    
                    if ( options.get( OPTION_LOG_ERRORS ) != null ) {
                        Logger.security.error( MODULE_NAME + " error: cannot load LDAP realm " + realmName, except );
                        except.printStackTrace();
                    }
                    return;
                }
                sharedState.put( realmName, _realm );
            }
        }
    }
    
    
    public boolean login()
        throws LoginException
    {
        if ( _subject == null )
            return false;
        if ( _realm == null )
            return false;
        
        Iterator        iter;
        LDAPCredentials ldapCreds;
        
        // Step one: look for LDAP credentials, if found use them to  authenticate and return.
        // The credentials must match against the same LDAP URL used in the options.
        iter = _subject.getPrivateCredentials( LDAPCredentials.class ).iterator();
        while ( iter.hasNext() ) {
            
            ldapCreds = (LDAPCredentials) iter.next();
            if ( ldapCreds.getHost().equals( _realm.getLDAPHost() ) && ( ldapCreds.getPort() == _realm.getLDAPPort() ) )
                if ( loginWithCred( ldapCreds ) )
                    return true;
        }
        
        // Step two: look for name/password credentials that we can use, if found use them
        // to construct an LDAP credential and authenticate using it. The LDAP credential
        // will be added to the subject.
        iter = _subject.getPrivateCredentials( NamePasswordCredentials.class ).iterator();
        while ( iter.hasNext() ) {
            NamePasswordCredentials nameCreds;
            
            nameCreds = (NamePasswordCredentials) iter.next();
            if ( ( nameCreds.getRealm() == null && _realm.isDefaultRealm() ) ||
                 ( nameCreds.getRealm() != null && nameCreds.getRealm().equals( _realm.getRealmName() ) ) ) {
                ldapCreds = new LDAPCredentials( _realm.getLDAPHost(), _realm.getLDAPPort(), _realm.getDN( nameCreds.getName() ),
                                                 nameCreds.getPassword() );
                if ( loginWithCred( ldapCreds ) ) {
                    _ldapCreds = ldapCreds;
                    return true;
                }
            }
        }
        
        /*
          // Step three: use the callback to obtain name/password that we can use,
          // if found authenticate and return
          NamePasswordCallback[] callbacks;
          
          callbacks = new NamePasswordCallback[ 1 ];
          callbacks[ 0 ] = new NamePasswordCallback();
          callbacks[ 0 ].setRealm( _realm );
          _handler.handle( callbacks );
          
          _ldapCreds = new LDAPCredentials( options.get( "ldap-url" ), getDN( callbacks[ 0 ].getName() ),
          callbacks[ 0 ].getPassword() );
          return loginWithCred( _ldapCreds );
        */
        return false;
    }
    
    
    private boolean loginWithCred( LDAPCredentials ldapCreds )
        throws LoginException
    {
        LDAPConnection          conn;
        LDAPEntry               entry;
        LDAPAttribute           attr;
        
        conn = new LDAPConnection();
        // dn = "uid=" + upc.getUser() + ",ou=people,dc=intalio,dc=com";
        try {
            conn.connect( ldapCreds.getHost(), ldapCreds.getPort() );
            conn.authenticate( ldapCreds.getDN(), new String( ldapCreds.getPassword() ) );
            entry = conn.read( ldapCreds.getDN(), new String[] { "cn", "mail" } );
            // Add an LDAP prinicipal
            _princs.add( new LDAPPrincipal( ldapCreds.getDN() ) );
            // If the common name is known, add a common name prinicipal
            attr = entry.getAttribute( "cn" );
            if ( attr != null && attr.getStringValueArray().length > 0 )
                _princs.add( new CommonNamePrincipal( attr.getStringValueArray()[ 0 ] ) );
            // If the e-mail address is known, add an e-mail prinicipal
            attr = entry.getAttribute( "email" );
            if ( attr != null && attr.getStringValueArray().length > 0 )
                _princs.add( new EmailPrincipal( attr.getStringValueArray()[ 0 ] ) );
            conn.disconnect();
            
            // No get the roles credentials from the LDAP realm
            _roleCreds = _realm.getRoleCredentials( ldapCreds.getDN() );
            
            // Success!
            return true;
        } catch ( LDAPException except ) {
            try {
                if ( conn.isConnected() )
                    conn.disconnect();
            } catch ( Exception ex2 ) { }
            // Account does not exist: fail to login.
            if ( except.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT )
                return false;
            // Account has no password, password is invalid, or insufficient credentials --
            // report the login error.
            if ( except.getLDAPResultCode() == LDAPException.INAPPROPRIATE_AUTHENTICATION )
                throw new LoginException( "Account " + ldapCreds.getDN() + " has no password" );
            if ( except.getLDAPResultCode() == LDAPException.INVALID_CREDENTIALS )
                throw new LoginException( "Account " + ldapCreds.getDN() + " has invalid password" );
            if ( except.getLDAPResultCode() == LDAPException.INSUFFICIENT_ACCESS_RIGHTS )
                throw new FailedLoginException( "No credentials to access account " + ldapCreds.getDN() );
            throw new LoginException( except.getMessage() );
        }
    }
    
    
    public boolean commit()
        throws LoginException
    {
        if ( _subject == null )
            return false;
        if ( _roleCreds != null )
            _subject.getPublicCredentials().add( _roleCreds );
        if ( _ldapCreds != null )
            _subject.getPrivateCredentials().add( _ldapCreds );
        _subject.getPrincipals().addAll( _princs );
        return true;
    }
    
    
    public boolean abort()
        throws LoginException
    {
        if ( _subject == null )
            return false;
        
        // Destroy the credentials, just release principals
        if ( _roleCreds != null ) {
            _roleCreds.destroy();
            _roleCreds = null;
        }
        if ( _ldapCreds != null ) {
            _ldapCreds.destroy();
            _ldapCreds = null;
        }
        _princs = null;
        _subject = null;
        return true;
    }
    
    
    public boolean logout()
        throws LoginException
    {
        if ( _subject == null )
            return false;
        
        // First, destory all the credentials
        if ( _roleCreds != null ) {
            _roleCreds.destroy();
            _subject.getPublicCredentials().remove( _roleCreds );
            _roleCreds = null;
        }
        if ( _ldapCreds != null ) {
            _ldapCreds.destroy();
            _subject.getPrivateCredentials().remove( _ldapCreds );
            _ldapCreds = null;
        }
        // Remove all the principals from the subject
        _subject.getPrincipals().remove( _princs );
        _subject = null;
        return true;
    }
    
    
    
}
