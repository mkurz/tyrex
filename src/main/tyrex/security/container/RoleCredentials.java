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
 * $Id: RoleCredentials.java,v 1.5 2001/03/19 17:39:02 arkin Exp $
 */


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
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $ $Date: 2001/03/19 17:39:02 $
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
    private static final int TABLE_SIZE = 29;
    
    
    /**
     * Construct a new credential with the given list of roles.
     *
     * @param roleNames Array of zero or more roles
     */
    public RoleCredentials( String[] roleNames )
    {
        _roles = new RoleEntry[ TABLE_SIZE ];
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
        _roles = new RoleEntry[ TABLE_SIZE ];
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
