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
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: LoginContext.java,v 1.1 2000/02/23 21:22:19 arkin Exp $
 */


package tyrex.security.auth;


import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.callback.CallbackHandler;


/**
 * A subject groups together several principals and credentials
 * representing the same entity. This implementation is modeled after
 * the JAAS <tt>Subject</tt>, but downgraded for Java 1.2. Will be
 * replaced by JAAS when we move to 1.3. Please refer to the JAAS
 * JavaDoc for details on how to use this class.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/02/23 21:22:19 $
 */
public class LoginContext
{


    /**
     * The name of the other application, in case the named application was not found.
     */
    private static final String Other = "other";


    /**
     * The subject of this context.
     */
    private Subject               _subject;


    /**
     * The callback handler for this context.
     */
    private CallbackHandler       _handler;
    

    /**
     * True if login succeeded and the subject should be returned.
     */
    private boolean               _succeeded;
    
    
    /**
     * List of all the modules used in this context.
     */
    private LoginModule[]         _modules;
    
    
    /**
     * List of all the application configuration used in this context.
     */
    private AppConfigurationEntry[]  _configs;
    
    
    /**
     * Shared state for login modules. The login module name is used as the
     * key and the shared state (Map) is the value.
     */
    private Hashtable             _sharedStates = new Hashtable();
    
    
    public LoginContext( String appName )
        throws LoginException
    {
        this( appName, new Subject() );
    }
    
    
    public LoginContext( String appName, Subject subject )
        throws LoginException
    {
        if ( subject == null )
            throw new IllegalArgumentException( "Argument 'subject' is null" );
        _subject = subject;
        
        // Determine the configuration entries from the login configuation.
        // If the application is not found, look for the 'other' login.
        _configs = Configuration.getConfiguration().getAppConfigurationEntry( appName );
        if ( _configs == null ) {
            _configs = Configuration.getConfiguration().getAppConfigurationEntry( Other );
            if ( _configs == null )
                throw new LoginException( "The application configuration '" + appName +
                                          "' was not found in the configuration and no 'other' was found" );
        }
        
        // Load and initialze the modules taking part in this context.
        _modules = new LoginModule[ _configs.length ];
        for ( int i = 0 ; i < _modules.length ; ++i ) {
            try {
                _modules[ i ] = (LoginModule) Class.forName( _configs[ i ].getLoginModuleName() ).newInstance();
            } catch ( Exception except ) {
                // Class not found, class cast, instantiation, illegal access
                throw new LoginException( except.getMessage() );
            }
        }
        for ( int i = 0 ; i < _modules.length ; ++i ) {
            _modules[ i ].initialize( _subject, _handler, getSharedState( _configs[ i ].getLoginModuleName() ),
                                      _configs[ i ].getOptions() );
            // Note: LoginException will terminate the constructor here
        }
    }
    
    
    public Subject getSubject()
    {
        return ( _succeeded ? _subject : null );
    }
    
    
    public synchronized void login()
        throws LoginException
    {
        LoginException failure;
        boolean        succeed;
        
        // Phase one: login
        failure = null;
        succeed = false;
        for ( int i = 0 ; i < _modules.length ; ++i ) {
            try {
                // Module reports false if it is to be ignored,
                // throws exception if it fails
                if ( _modules[ i ] != null ) {
                    if ( ! _modules[ i ].login() )
                        _modules[ i ] = null;
                    else 
                        // At least one module must succeed
                        succeed = true;
                }
                // If sufficient succeeds, no point in going forward
                if ( _configs[ i ].getControlFlag() == AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT )
                    break;
            } catch ( LoginException except ) {
                if ( _configs[ i ].getControlFlag() == AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT ||
                     _configs[ i ].getControlFlag() == AppConfigurationEntry.LoginModuleControlFlag.REQUIRED ) {
                    // Authentication still continues down the module list,
                    // but will fail;
                    if ( failure == null )
                        failure = except;
                } else if ( _configs[ i ].getControlFlag() == AppConfigurationEntry.LoginModuleControlFlag.REQUISITE ) {
                    if ( failure == null )
                        failure = except;
                    break;
                } else if ( _configs[ i ].getControlFlag() == AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL ) {
                    // We don't care if optional failed, only if it succeeds.
                }
            }
        }
        
        // Failure is either a reported failure from sufficient/required/requisite,
        // or no module reporting success.
        if ( failure == null && succeed ) {
            // Phase two: commit
            for ( int i = 0 ; i < _modules.length ; ++i ) {
                try {
                    if ( _modules[ i ] != null ) {
                        if ( ! _modules[ i ].commit() )
                            _modules[ i ] = null;
                    }
                } catch ( LoginException except ) {
                    if ( failure != null )
                        failure = except;
                }
            }
            // The definition of success: no failure to commit
            if ( failure == null ) {
                _succeeded = true;
                return;
            }
        }
        
        
        // Abort
        failure = null;
        for ( int i = 0 ; i < _modules.length ; ++i ) {
            try {
                if ( _modules[ i ] != null )
                    _modules[ i ].abort();
            } catch ( LoginException except ) {
                if ( failure != null )
                    failure = except;
            }
        }
        // We might reach this point simply because no module reported success
        if ( failure == null )
            throw new LoginException( "No module reported successful authentication" );
        throw failure;
    }
    
    
    public synchronized void logout()
        throws LoginException
    {
        LoginException failure;
        
        failure = null;
        for ( int i = 0 ; i < _modules.length ; ++i ) {
            try {
                if ( _modules[ i ] != null )
                    _modules[ i ].logout();
            } catch ( LoginException except ) {
                if ( failure != null )
                    failure = except;
            }
        }
        if ( failure != null )
            throw failure;
    }
    
    
    private Map getSharedState( String type )
    {
        Map sharedState;
        
        sharedState = (Map) _sharedStates.get( type );
        if ( sharedState == null ) {
            sharedState = new HashMap();
            _sharedStates.put( type, sharedState );
        }
        return sharedState;
    }
    
    
}
