package tyrex.security.container;


import javax.security.auth.Destroyable;


/**
 * Credentials for a resource manager authentication.
 * <p>
 * The password is stored as an array of characters to prevent string interning
 * and allow it to be destroyed. Resource credentials are considered private.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/08 23:05:51 $
 */
public final class ResourceCredentials
    implements Destroyable
{


    private String  _name;
    
    
    private char[]  _password;
    
    
    private String  _resName;
    
    
    /**
     * Constructs a new credential with the given name and password.
     *
     * @param resName The resource name
     * @param user The name
     * @param password The password, null if unkonwn
     */
    public ResourceCredentials( String resName, String name, char[] password )
    {
        if ( resName == null )
            throw new IllegalArgumentException( "Argument 'resName' is null" );
        _resName = resName;
        if ( name == null )
            throw new IllegalArgumentException( "Argument 'name' is null" );
        _name = name;
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
     * Returns the resource name.
     *
     * @return The resource name
     */
    public String getResourceName()
    {
        if ( _name == null )
            throw new IllegalArgumentException( "This credentials have been destroyed" );
        return _resName;
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
        _resName = null;
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
