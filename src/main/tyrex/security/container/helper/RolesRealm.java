package tyrex.security.container.helper;


import java.util.Vector;
import tyrex.security.container.RoleCredentials;


/**
 * Cached copy of a realm and all the roles in that realm. Implementations of
 * realms can extend this class and use it to add roles, list members in roles,
 * lookup roles and members in roles, and construct {@link RoleCredentials}.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/08 23:05:51 $
 */
public class RolesRealm
{


    /**
     * The name of this realm.
     */    
    private String _realmName;
    
    
    /**
     * A list of default roles. All members of this realm are automatically
     * enlisted in these roles. May be null.
     */
    private String[] _defaultRoles;
    
    
    /**
     * A list of all the roles and the members of each role. May be null.
     */
    private Role[]   _roles;
    
    
    /**
     * Construct a new realm.
     *
     * @param realmName The realm name
     * @param defaultRoles List of default roles that all members are listed
     *  in, or null if no default roles
     */
    protected RolesRealm( String realmName, String[] defaultRoles )
    {
        if ( realmName == null )
            throw new IllegalArgumentException( "Argument 'realmName' is null" );
        _realmName = realmName;
        _defaultRoles = defaultRoles;
    }
    
    
    /**
     * Returns the name of this realm.
     *
     * @return Name of realm
     */
    public String getRealmName()
    {
        return _realmName;
    }
    
    
    /**
     * Lists all the roles in this realm.
     *
     * @return Array of zero or more roles
     */
    public String[] listRoles()
    {
        String[] roleNames;
        
        roleNames = new String[ _roles.length ];
        for ( int i = 0 ; i < _roles.length ; ++i )
            roleNames[ i ] = _roles[ i ].getRoleName();
        return roleNames;
    }
    
    
    /**
     * Lists all the members in the role.
     *
     * @param roleName The role name
     * @return Array of zero or more members
     */
    public String[] listMembers( String roleName )
    {
        for ( int i = 0 ; i < _roles.length ; ++i ) {
            if ( _roles[ i ].getRoleName().equals( roleName ) ) {
                return _roles[ i ].listMembers();
            }
        }
        return new String[ 0 ];
    }
    
    
    /**
     * Add a role or members to an existing role. If the role did not exist before,
     * the role and all its members are added to the role list. If the role existed
     * before, the members are added to the existing list of members.
     *
     * @param roleName The role name
     * @param members The members list
     */
    protected void addRole( String roleName, String[] members )
    {
        if ( roleName == null || members == null )
            throw new IllegalArgumentException( "Argument 'roleName' or 'members' is null" );
        
        // If not roles existed before, add the new one.
        if ( _roles == null ) {
            _roles = new Role[ 1 ];
            _roles[ 0 ] = new Role( roleName, members );
            return;
        }
        
        // If role exist before, add new members.
        for ( int i = 0 ; i < _roles.length ; ++i ) {
            if ( _roles[ i ].getRoleName().equals( roleName ) ) {
                _roles[ i ].addMembers( members );
                return;
            }
        }
        
        // Add new role.
        Role[] roles;
        
        roles = new Role[ _roles.length + 1 ];
        for ( int i = 0 ; i < _roles.length ; ++i )
            roles[ i ] = _roles[ i ];
        roles[ _roles.length ] = new Role( roleName, members );
        _roles = roles;
    }
    
    
    /**
     * Returns a role credentials for the given member. The credentials
     * is constructed by accumulating all the roles in which the member
     * is listed as a member.
     *
     * @param member The member
     * @return Role credentials
     */
    public RoleCredentials getRoleCredentials( String member )
    {
        Vector inRoles = new Vector();
        
        if ( _roles != null ) {
            for ( int i = 0 ; i < _roles.length ; ++i ) {
                Role role;
                
                role = _roles[ i ];
                if ( _roles[ i ].isInRole( member ) ) {
                    inRoles.add( _roles[ i ].getRoleName() );
                }
            }
        }
        if ( _defaultRoles != null ) {
            for ( int i = 0 ; i < _defaultRoles.length ; ++i ) {
                inRoles.add( _defaultRoles[ i ] );
            }
        }
        return new RoleCredentials( (String[]) inRoles.toArray( new String[ inRoles.size() ] ) );
    }
    
    
    /**
     * Definition of a role is a name and members list.
     */
    static class Role
    {
        
        
        /**
         * The role name.
         */
        private String        _roleName;
        
        
        /**
         * Hashtable holding all members in the role.
         */
        private MemberEntry[] _members;
        
        
        /**
         * The default size of the member hashtable. Must be a prime number.
         */
        private static final int MemberTableSize = 29;
        
        
        /**
         * Construct a new role with the given role name and list of members.
         *
         * @param roleName The role name
         * @param members Array of zero or more members
         */
        Role( String roleName, String[] members )
        {
            _roleName = roleName;
            _members = new MemberEntry[ MemberTableSize ];
            addMembers( members );
        }
        
        
        /**
         * Returns the role name.
         *
         * @return The role name
         */
        String getRoleName()
        {
            return _roleName;
        }
        
        
        /**
         * Add members to the role.
         *
         * @param member Zero or more members
         */
        void addMembers( String[] members )
        {
            int         hash;
            MemberEntry entry;
            
            for ( int i = 0 ; i < members.length ; ++i ) {
                hash = ( members[ i ].hashCode() & 0x7FFFFFFF ) % _members.length;
                entry = _members[ hash ];
                while ( entry != null && ! entry.member.equals( members[ i ] ) )
                    entry = entry.next;
                if ( entry == null ) {
                    entry = new MemberEntry( members[ i ], _members[ hash ] );
                    _members[ hash ] = entry;
                }
            }
        }
        
        
        /**
         * Returns true if the named member is a member of this role.
         *
         * @param member Member name
         * @return True if listed in role
         */
        boolean isInRole( String member )
        {
            int         hash;
            MemberEntry entry;
            
            hash = ( member.hashCode() & 0x7FFFFFFF ) % _members.length;
            entry = _members[ hash ];
            while ( entry != null && ! entry.member.equals( member ) )
                entry = entry.next;
            return ( entry != null );
        }
        
        
        /**
         * Lists all the members in the role.
         *
         * @return An array of zero or more members
         */
        String[] listMembers()
        {
            Vector      members;
            MemberEntry entry;
            
            members = new Vector();
            for ( int i = 0 ; i < _members.length ; ++i ) {
                entry = _members[ i ];
                while ( entry != null ) {
                    members.add( entry.member );
                    entry = entry.next;
                }
            }
            return (String[]) members.toArray( new String[ members.size() ] );
        }
        
        
        /**
         * An entry for each member in the hashtable.
         */
        static class MemberEntry
        {
            
            String member;
            
            MemberEntry next;
            
            MemberEntry( String member, MemberEntry next )
            {
                this.member = member;
                this.next = next;
            }
            
        }
        
    }
    
    
}


