package tyrex.security.container;


import java.io.Serializable;
import java.security.Principal;


/**
 * A realm principal. Holds the principal's name and realm, if known.
 * The container will use this prinicipal to return the prinicipal's
 * name to the application.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/02/23 21:22:19 $
 */
public final class RealmPrincipal
    implements Principal, Serializable
{


    /**
     * The principal 'anyone' indicates an unknown prinicipal.
     */
    public static final RealmPrincipal Anyone = new RealmPrincipal( "anyone", null );
    
    
    /**
     * The principal's name.
     */
    private String  _name;
    
    
    /**
     * The principal's realm.
     */
    private String  _realm;
    
    
    /**
     * Construct a new realm principal.
     *
     * @param name The principal's name
     * @param realm The prinicipal's realm, null if not known
     */
    public RealmPrincipal( String name, String realm )
    {
        if ( name == null )
            throw new IllegalArgumentException( "Argument 'name' is null" );
        _name = name;
        _realm = realm;
    }
    
    
    /**
     * Returns the name of the principal.
     *
     * @return The prinicipal's name
     */
    public String getName()
    {
        return _name;
    }
    
    
    /**
     * Returns the realm of the prinicipal. If the principal's
     * realm is unknown, returns null.
     *
     * @return The principal's realm or null
     */
    public String getRealm()
    {
        return _realm;
    }
    
    
    public String toString()
    {
        return ( _realm == null ? _name : _name + "@" + _realm );
    }
    
    
    public int hashCode()
    {
        return ( _realm == null ? _name.hashCode() : _name.hashCode() + _realm.hashCode() );
    }
    
    
    public boolean equals( Object object )
    {
        if ( object == this )
            return true;
        if ( object == null )
            return false;
        if ( ! ( object instanceof RealmPrincipal ) )
            return false;
        return _name.equals( ( (RealmPrincipal) object )._name ) &&
            ( _realm == null && ( (RealmPrincipal) object )._realm == null ) ||
            ( _realm != null && _realm.equals( ( (RealmPrincipal) object )._realm ) );
    }
    

}
