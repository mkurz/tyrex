package tyrex.security.container;


import java.util.Vector;
import java.security.AccessController;
import javax.security.auth.AuthPermission;


/**
 * Credential listing the principal's roles. These credentials are used by
 * the container to determine if a principal is member of a given role, and
 * also return that information to the application.
 * <p>
 * Role credentials are considered public.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/08/28 19:01:49 $
 */
public final class RoleCredentials
{


    /**
     * Hashtable holding list of all roles in which the principal is a member.
     * Set to null when the credentials are destroyed.
     */
    private transient RoleEntry[]  _roles;
    
    
    /**
     * The default size of the role hashtable. Must be a prime number.
     */
    private static final int RoleTableSize = 29;
    
    
    /**
     * Construct a new credential with the given list of roles.
     *
     * @param roleNames Array of zero or more roles
     */
    public RoleCredentials( String[] roleNames )
    {
        _roles = new RoleEntry[ RoleTableSize ];
        for ( int i = 0 ; i < roleNames.length ; ++i ) {
            addRole( roleNames[ i ] );
        }
    }
    
    
    /**
     * Construct a new credential consolidating roles from a list of
     * credentials.
     *
     * @param creds Array of zero or more credentials
     */
    public RoleCredentials( RoleCredentials[] creds )
    {
        _roles = new RoleEntry[ RoleTableSize ];	
        for ( int i = 0 ; i < creds.length ; ++i ) {
            for ( int j = 0 ; j < creds[ i ]._roles.length ; ++j ) {
                RoleEntry entry;
                
                entry = creds[ i ]._roles[ j ];
                while ( entry != null ) {
                    addRole( entry.roleName );
                    entry = entry.next;
                }
            }
        }
    }
    
    
    /**
     * Returns true if the principal is a member of the named role.
     *
     * @param roleName Role name
     * @return True if member of role
     */
    public boolean isInRole( String roleName )
    {
        RoleEntry entry;
        int       hash;
        
        if ( _roles == null )
            throw new IllegalStateException( "These credentials have been destroyed" );
        hash = ( roleName.hashCode() & 0x7FFFFFFF ) % _roles.length;
        entry = _roles[ hash ];
        while ( entry != null && ! entry.roleName.equals( roleName ) )
            entry = entry.next;
        return ( entry != null );
    }
    
    
    /**
     * Returns a list of all the role names.
     *
     * @return Array of zero or more roles
     */
    public String[] listRoles()
    {
        Vector roles;
        
        if ( _roles == null )
            throw new IllegalStateException( "These credentials have been destroyed" );
        roles = new Vector();
        for ( int i = 0 ; i < _roles.length ; ++i ) {
            RoleEntry entry;
            
            entry = _roles[ i ];
            while ( entry != null ) {
                roles.add( entry.roleName );
                entry = entry.next;
            }
        }
        return (String[]) roles.toArray( new String[ roles.size() ] );
    }
    
    
    /**
     * Destroy the credentials.
     */
    public void destroy()
    {
        AccessController.checkPermission( new AuthPermission( "destroyCredentials" ) );
        // Role names are not sensitive information, sufficient to
        // just dispose of the array
        _roles = null;
    }
    
    
    /**
     * Returns true if these credentials have been destroyed.
     */
    public boolean isDestroyed()
    {
        return ( _roles == null );
    }
    
    
    private void addRole( String roleName )
    {
        int       hash;
        RoleEntry entry;
        
        hash = ( roleName.hashCode() & 0x7FFFFFFF ) % _roles.length;
        entry = _roles[ hash ];
        while ( entry != null && ! entry.roleName.equals( roleName ) )
            entry = entry.next;
        if ( entry == null ) {
            entry = new RoleEntry( roleName, _roles[ hash ] );
            _roles[ hash ] = entry;
        }
    }
    
    
    static class RoleEntry
    {
        
        String roleName;
        
        RoleEntry next;
        
        RoleEntry( String roleName, RoleEntry next )
        {
            this.roleName = roleName;
            this.next = next;
        }
        
    }
    
    
}
