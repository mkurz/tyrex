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
 * @version $Revision: 1.2 $ $Date: 2000/08/28 19:01:49 $
 */
public final class LDAPCredentials
    implements Destroyable
{


    private String  _dn;
    
    
    private char[]  _password;
    
    
    private String  _host;
    

    private int  _port;
    /**
     * Constructs a new credential with the given name and password.
     *
     * @param host The LDAP host
     * @param port The port on the host
     * @param dn The account DN
     * @param password The password, null if unkonwn
     */
    public LDAPCredentials( String host, int port, String dn, char[] password )
    {
        if ( host == null )
            throw new IllegalArgumentException( "Argument 'host' is null" );
        _host = host;
        _port = port;
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
     * Returns the LDAP Host.
     *
     * @return The LDAP Host
     */
    public String getHost()
    {
        if ( _dn == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return _host;
    }

    /**
     * Returns the LDAP port on the host.
     *
     * @param the LDAP port on the host.
     */
    public int getPort()
    {
        if ( _dn == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return _port;
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
        _host = null;
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
