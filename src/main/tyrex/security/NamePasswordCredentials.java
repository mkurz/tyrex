package tyrex.security;


import javax.security.auth.Destroyable;


/**
 * Credentials for name/password authentication. Can be used to authenticate
 * JDBC connections, LDAP connections, etc. The realm can be used to
 * determine where the credentials can be used.
 * <p>
 * The password is stored as an array of characters to prevent string interning
 * and allow it to be destroyed. Name/password credentials are considered private.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/02/23 21:22:56 $
 */
public final class NamePasswordCredentials
    implements Destroyable
{


    private String  _name;


    private char[]  _password;


    private String  _realm;


    /**
     * Constructs a new credential with the given name and password.
     *
     * @param user The name
     * @param password The password, null if unkonwn
     * @param realm The realm, null if unknown
     */
    public NamePasswordCredentials( String name, char[] password, String realm )
    {
        if ( name == null )
            throw new IllegalArgumentException( "Argument 'name' is null" );
        _name = name;
        _realm = realm;
        _password = (char[]) password.clone();
    }


    /**
     * Returns the name.
     *
     * @return The name
     */
    public String getName()
    {
        if ( _name == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return _name;
    }


    /**
     * Returns the realm.
     *
     * @return The realm
     */
    public String getRealm()
    {
        if ( _name == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return _realm;
    }


    /**
     * Returns the password. The password may be null.
     *
     * @return The password
     */
    public char[] getPassword()
    {
        if ( _name == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return (char[]) _password.clone();
    }


    /**
     * Destroy the credentials.
     */
    public void destroy()
    {
        _name = null;
        _realm = null;
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
        return ( _name == null );
    }


}
