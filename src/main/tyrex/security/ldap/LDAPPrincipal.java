package tyrex.security.ldap;


import java.io.Serializable;
import java.security.Principal;


/**
 * The LDAP DN prinicipal. Provides the LDAP distinguished name by which
 * an account is known (e.g. <tt>'uid=Joe.Smith,ou=People,dc=acme,dc=com'</tt>).
 * Immutable and serializable.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/08/28 19:01:49 $
 */
public final class LDAPPrincipal
    implements Principal
{


    private String  _dn;
    
    
    public LDAPPrincipal( String dn )
    {
        if ( dn == null )
            throw new IllegalArgumentException( "Argument 'dn' is null" );
        _dn = dn;
    }
    
    
    public String getName()
    {
        return _dn;
    }
    
    
    public String toString()
    {
        return "DN: " + _dn;
    }
    
    
    public int hashCode()
    {
        return _dn.hashCode();
    }
    
    
    public boolean equals( Object object )
    {
        if ( object == this )
            return true;
        if ( object == null )
            return false;
        if ( ! ( object instanceof LDAPPrincipal ) )
            return false;
        return _dn.equals( ( (LDAPPrincipal) object )._dn );
    }
    
    
}
