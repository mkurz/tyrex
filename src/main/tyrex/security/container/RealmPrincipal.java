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
 * $Id: RealmPrincipal.java,v 1.5 2001/03/19 17:39:02 arkin Exp $
 */


package tyrex.security.container;


import java.io.Serializable;
import java.security.Principal;


/**
 * A realm principal. Holds the principal's name and realm, if known.
 * The container will use this prinicipal to return the prinicipal's
 * name to the application.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $ $Date: 2001/03/19 17:39:02 $
 */
public final class RealmPrincipal
    implements Principal, Serializable
{


    /**
     * The principal 'anyone' indicates an unknown prinicipal.
     */
    public static final RealmPrincipal ANYONE = new RealmPrincipal( "anyone", null );
    
    
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
