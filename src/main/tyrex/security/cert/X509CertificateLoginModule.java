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
 * $Id: X509CertificateLoginModule.java,v 1.3 2000/09/08 23:05:51 mohammed Exp $
 */


package tyrex.security.cert;


import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.Principal;
import java.security.KeyStoreException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.Subject;
import javax.security.auth.spi.LoginModule;


/**
 * Implements an X509 certificate validation login module.
 * <p>
 * This module will read the client certificates associated with the
 * subject and determine whether at least one of them was issued by
 * a trusted party. These certificates will further be validated and
 * against a CRL list. The principal of all the validated certificates
 * are added to the subject.
 * <p>
 * A login exception is reported only if the client certificate failed
 * validation (indicates a forged certificate), has expired, or has
 * been listed as revoked in the CRL.
 * <p>
 * This module should be used in one of two modes:
 * <ul>
 * <li>Optional - if a trusted certificate is found, the certificate's
 * principal will be associated with the subject
 * <li>Required - if no trusted certificate is found, authentication
 * will fail
 * </ul>
 * <p>
 * The following options are supported:
 * <ul>
 * <li><tt>key-store</tt> The name of the trusted certificate key store
 * (if missing defaults to "JKS")
 * <li><tt>trusted-certs</tt> A comma separated list of all the trusted
 * certificates (if missing, uses all trusted certificates in the key
 * store)
 * <li><tt>crl-class</tt> The name of a class implementing an X509 CRL
 * <li><tt>log-errors</tt> If set, configuration errors are logged to
 * the console
 * </ul>
 * <p>
 * By using this module with no configuration options, the default key
 * store for the JVM will be used (typically JKS) and all the trusted
 * certificates in that key store will be used. If at least one client
 * certificate is found that is trusted, the login will succeed. If no
 * client certificate is found that is trusted, the login will fail.
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/08 23:05:51 $
 */
public final class X509CertificateLoginModule
    implements LoginModule
{


    /**
     * Option names for the login module.
     */
    public static class Options
    {
        
        /**
         * The key store name (<tt>key-store</tt>). If this option is not
         * specified, the default key store is used (typically JKS).
         */
        public static final String KeyStore = "key-store";
        
        /**
         * The trusted certificate list (<tt>trusted-certs</tt>). If this
         * option is not specified, all the trusted certificates in the
         * key store are used.
         */
        public static final String TrustedCerts = "trusted-certs";
        
        /**
         * The CRL class (<tt>crl-class</tt>). If this option is specified
         * the named class is used to obtain an X590 CRL implementation.
         */
        public static final String CRLClass ="crl-class";
        
        /**
         * Log errors (<tt>log-errors</tt>). If this option is specified,
         * initialization errors are logged to the console.
         */
        public static final String LogErrors = "log-errors";
        
    }
    

    /**
     * The default key store name.
     */
    private static final String DefaultKeyStore = "JKS";
    
    
    /**
     * The name of this module.
     */
    private static final String ModuleName = "X509CertificateLoginModule";
    
    
    /**
     * The subject we are authenticating.
     */
    private Subject     _subject;
    
    
    /**
     * The subject DNs from the authenticated certificates.
     */
    private Vector      _subjectDN;
    
    
    /**
     * A list of issuer certificates used to authenticate the subject certificates.
     * The subjectDN of each issuer is used as the key, X509Certificate is the value.
     */
    private Hashtable   _trusted;
    
    
    /**
     * An X590 CRL for certificate revocation. May be null.
     */
    private X509CRL     _crl;
    
    
    public void initialize( Subject subject, CallbackHandler handler,
                            Map sharedState, Map options )
    {
        _subject = subject;
        
        // Need to synchornize since we will be placing stuff into the shared
        // state in order to not reload it.
        synchronized ( sharedState ) {
            
            // Determine the key store to use. The key store is loaded only once
            // and placed in the shared state of the login module.
            String   keyStoreName;
            KeyStore keyStore;
            
            keyStoreName = (String) options.get( Options.KeyStore );
            if ( keyStoreName == null )
                keyStoreName = DefaultKeyStore;
            keyStore = (KeyStore) sharedState.get( "key-store-" + keyStoreName );
            if ( keyStore == null ) {
                try {
                    keyStore = KeyStore.getInstance( keyStoreName );
                    sharedState.put( "key-store-" + keyStoreName, keyStore );
                } catch ( KeyStoreException except ) {
                    // We have a problem, the key store is not found. We can't proceed,
                    // this module will always return false.
                    if ( options.get( Options.LogErrors ) != null )
                        System.out.println( ModuleName + " error: key store " + keyStoreName +
                                            " could not be loaded: " + except );
                    _trusted = new Hashtable();
                    return;
                }
            }
            
            // Determine the list of trusted cerificates against which we certify
            // each certificate. The trusted list is loaded only once and placed in
            // the shared state of the login module.
            String          trusted;
            StringTokenizer token;
            
            trusted = (String) options.get( Options.TrustedCerts );
            if ( trusted != null ) {
                _trusted = (Hashtable) sharedState.get( "trusted-certs-" + keyStoreName + "-" + trusted );
                if ( _trusted == null ) {
                    _trusted = new Hashtable();
                    token = new StringTokenizer( trusted, ", " );
                    while ( token.hasMoreTokens() ) {
                        String      alias;
                        Certificate cert;
                        
                        alias = token.nextToken();
                        try {
                            if ( keyStore.isCertificateEntry( alias ) ) {
                                cert = keyStore.getCertificate( alias );
                                if ( cert != null && cert instanceof X509Certificate )
                                    _trusted.put( ( (X509Certificate) cert ).getIssuerDN(), cert );
                            }
                        } catch ( KeyStoreException except ) {
                            if ( options.get( Options.LogErrors ) != null )
                                System.out.println( ModuleName + " error: error accessing key store " + keyStoreName +
                                                    " could not be loaded: " + except );
                            return;
                        }
                    }
                    sharedState.put( "trusted-certs-" + keyStoreName + "-" + trusted, _trusted );
                }
            } else {
                _trusted = (Hashtable) sharedState.get( "trusted-certs-" + keyStoreName + "-all" );
                if ( _trusted == null ) {
                    Enumeration aliases;
                    
                    try {
                        _trusted = new Hashtable();
                        aliases = keyStore.aliases();
                        while ( aliases.hasMoreElements() ) {
                            String      alias;
                            Certificate cert;
                            
                            alias = (String) aliases.nextElement();
                            if ( keyStore.isCertificateEntry( alias ) ) {
                                cert = keyStore.getCertificate( alias );
                                if ( cert != null && cert instanceof X509Certificate )
                                    _trusted.put( ( (X509Certificate) cert ).getIssuerDN(), cert );
                            }
                        }
                        sharedState.put( "trusted-certs-" + keyStoreName + "-all", _trusted );
                    } catch (KeyStoreException except ) {
                        if ( options.get( Options.LogErrors ) != null )
                            System.out.println( ModuleName + " error: error accessing key store " + keyStoreName +
                                                " could not be loaded: " + except );
                    }
                }
            }
            
            // Determine the CRL to use. The CRL is loaded only once
            // and placed in the shared state of the login module.
            String crlClassName;
            
            crlClassName = (String) options.get( Options.CRLClass );
            if ( crlClassName != null ) {
                _crl = (X509CRL) sharedState.get( "crl-" + crlClassName );
                if ( _crl == null ) {
                    try {
                        _crl = (X509CRL) Class.forName( crlClassName ).newInstance();
                        sharedState.put( "crl-" + crlClassName, _crl );
                    } catch ( Exception except ) {
                        if ( options.get( Options.LogErrors ) != null )
                            System.out.println( ModuleName + " error: error loading CRL class " + crlClassName + ": " + except );
                    }
                }
            }
            
        }
    }
    
    
    public boolean login()
        throws LoginException
    {
        Set      certs;
        Iterator iter;
        X509CRL  crl;
        
        if ( _subject == null )
            return false;
        certs = _subject.getPublicCredentials( X509Certificate.class );
        
        // If we found no certificates return false (i.e. this module failed, ignore it).
        if ( certs.size() == 0 )
            return false;
        
        // Iterate through all the X509 certificates. At the end we are
        // only interested in one certificate.
        iter = certs.iterator();
        while ( iter.hasNext() ) {
            X509Certificate cert;
            X509Certificate issuer;
            
            cert = (X509Certificate) iter.next();
            // Get a suitable issuer for this certificate. If issuer is not found,
            // we skip this check. If issuer is found, we check the certificate.
            issuer = (X509Certificate) _trusted.get( cert.getIssuerDN() );
            if ( issuer != null ) {
                // Certificate was issued by a party which we are interested
                // in evaluating.
                
                // Step one: check the validity of the certificate.
                try {
                    ( (X509Certificate) cert ).checkValidity();
                } catch ( CertificateException except ) {
                    throw new LoginException( "The certificate for " + cert.getSubjectDN().getName() + " has expired" );
                }
                // Step two: make sure the issuer verifies.
                try {
                    cert.verify( issuer.getPublicKey() );
                } catch ( CertificateException except ) {
                    throw new LoginException( "The certificate for " + cert.getSubjectDN().getName() +
                                              " was not signed by " + issuer.getSubjectDN().getName() );
                } catch ( GeneralSecurityException except ) {
                    throw new LoginException( "Certificate verification error: " + except.toString() );
                }
                // Step three: make sure the certificate has not been revoked;
                if ( _crl != null && _crl.isRevoked( cert ) )
                    throw new LoginException( "The certificate for " + cert.getSubjectDN().getName() + " has been revoked" );
                
                // Step four: record the subject of the certificate
                if ( _subjectDN == null )
                    _subjectDN = new Vector();
                _subjectDN.add( cert.getSubjectDN() );
            }
        }
        
        // If we found no subjects (that is, no certificates we trust) return false
        // (i.e. this module failed, ignore it).
        return ( _subjectDN != null );
    }
    
    
    public boolean commit()
        throws LoginException
    {
        // Add the subject DNs from all the certificate certificates
        // to the principals list.
        if ( _subjectDN != null ) {
            _subject.getPrincipals().add( _subjectDN );
            return true;
        }
        return false;
    }
    
    
    public boolean abort()
        throws LoginException
    {
        if ( _subjectDN != null ) {
            _subjectDN.clear();
            return true;
        }
        return false;
    }
    
    
    public boolean logout()
        throws LoginException
    {
        // Remove the subject DNs from all the certificate certificates
        // from the principals list.
        if ( _subjectDN != null ) {
            _subject.getPrincipals().remove( _subjectDN );
            _subjectDN.clear();
            return true;
        }
        return false;
    }
    
    
}
