package tyrex.security.ldap;


import javax.security.auth.Destroyable;


/**
 * Credentials for LDAP authentication. Can be used to authenticate an LDAP
 * connection. The LDAP URL can be used to specify the LDAP server (host and
 * port) for which these credentials apply.
 * <p>
 * The password is stored as an array of characters to prevent string interning
 * and allow it to be destroyed. LDAP credentials are considered private.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/02/23 21:22:19 $
 */
public final class LDAPCredentials
    implements Destroyable
{


    private String  _dn;
    
    
    private char[]  _password;
    
    
    private String  _url;
    
    
    /**
     * Constructs a new credential with the given name and password.
     *
     * @param url The LDAP URL
     * @param dn The account DN
     * @param password The password, null if unkonwn
     */
    public LDAPCredentials( String url, String dn, char[] password )
    {
        if ( url == null )
            throw new IllegalArgumentException( "Argument 'url' is null" );
        _url = url;
        if ( dn == null )
            throw new IllegalArgumentException( "Argument 'dn' is null" );
        _dn = dn;
        _password = (char[]) password.clone();
    }
    
    
    /**
     * Returns the account DN.
     *
     * @return The account DN
     */
    public String getDN()
    {
        if ( _dn == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return _dn;
    }
    
    
    /**
     * Returns the LDAP URL.
     *
     * @return The LDAP URL
     */
    public String getURL()
    {
        if ( _dn == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return _url;
    }
    
    
    /**
     * Returns the password. The password may be null.
     *
     * @return The password
     */
    public char[] getPassword()
    {
        if ( _dn == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return (char[]) _password.clone();
    }
    
    
    /**
     * Destroy the credentials.
     */
    public void destroy()
    {
        _dn = null;
        _url = null;
        if ( _password != null ) {
            for ( int i = 0 ; i < _password.length ; ++i )
                _password[ i ] = '\0';
            _password = null;
        }
    }
    
    
    /**
     * Returns true if these credentials have been destroyed.
     */
    public boolean isDestroyed()
    {
        return ( _dn == null );
    }
    
    
}
