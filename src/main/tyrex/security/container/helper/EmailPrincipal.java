package tyrex.security.container.helper;


import java.io.Serializable;
import java.security.Principal;


/**
 * An email principal. Holds the principal's e-mail address.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/08 23:05:51 $
 */
public final class EmailPrincipal
    implements Principal, Serializable
{


    /**
     * The email address.
     */
    private String  _email;
    

    /**
     * Construct a new email principal.
     *
     * @param email The email address
     */
    public EmailPrincipal( String email )
    {
        if ( email == null )
            throw new IllegalArgumentException( "Argument 'email' is null" );
        _email = email;
    }
    
    
    /**
     * Returns the email of the principal.
     *
     * @return The prinicipal's email
     */
    public String getName()
    {
        return _email;
    }
    
    
    public String toString()
    {
        return _email;
    }
    
    
    public int hashCode()
    {
        return _email.hashCode();
    }
    
    
    public boolean equals( Object object )
    {
        if ( object == this )
            return true;
        if ( object == null )
            return false;
        if ( ! ( object instanceof EmailPrincipal ) )
            return false;
        return _email.equals( ( (EmailPrincipal) object )._email );
    }
    

}
