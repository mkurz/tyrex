package tyrex.security.ldap;


import java.io.Serializable;
import java.security.Principal;


/**
 * The common name prinicipal. Provides the common name by which a user
 * is known (e.g. <tt>'Joe Smith'</tt>).
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/08 23:05:51 $
 */
public final class CommonNamePrincipal
    implements Principal, Serializable
{


    private String  _name;


    /**
     * Constructs a new common name principal.
     *
     * @param name The common name
     */
    public CommonNamePrincipal( String name )
    {
        if ( name == null )
            throw new IllegalArgumentException( "Argument 'name' is null" );
        _name = name;
    }
    
    
    public String getName()
    {
        return _name;
    }
    
    
    public String toString()
    {
        return _name;
    }
    
    
    public int hashCode()
    {
        return _name.hashCode();
    }
    
    
    public boolean equals( Object object )
    {
        if ( object == this )
            return true;
        if ( object == null )
            return false;
        if ( ! ( object instanceof CommonNamePrincipal ) )
            return false;
        return _name.equals( ( (CommonNamePrincipal) object )._name );
    }
    
    
}
