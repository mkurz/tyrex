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
 * Copyright 2000 (C) Intalio Inc. All Rights Reserved.
 *
 * $Id: MemoryBindingEnumeration.java,v 1.4 2000/09/08 23:05:19 mohammed Exp $
 */


package tyrex.naming;


import java.util.Vector;
import java.util.Enumeration;
import java.util.Dictionary;
import javax.naming.Reference;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.CompositeName;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;


/**
 * Naming enumeration supporting {@link NamClassPair} and {@link Binding},
 * created based of a {@link MemoryBinding}.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $ $Date: 2000/09/08 23:05:19 $
 * @see MemoryBinding
 */
final class MemoryBindingEnumeration
    implements NamingEnumeration
{
 

    /**
     * An enumeration of the memory binding collection.
     */
    private final Enumeration _enum;
    
    
    /**
     * Construct a new enumeration.
     * 
     * @param bindings The memory bindings to enumerate
     * @param names True to return an enumeration of {@link NameClassPair},
     *  false to return an enumeration of {@link Binding}
     * @param parent The parent context is required to create sub-contexts
     *  clones in the returned enumeration
     */
    MemoryBindingEnumeration( MemoryBinding bindings, boolean names, Context parent )
    {
        Vector      noContexts;
        Enumeration enum;
        Object      object;
        String      key;
        
        synchronized ( bindings ) {
            noContexts = new Vector( bindings.size() );
            enum = bindings.keys();
            while ( enum.hasMoreElements() ) {
                key = (String) enum.nextElement();
                object = bindings.get( key );
                if ( object instanceof MemoryBinding ) {
                    if ( names )
                        noContexts.addElement( new NameClassPair( key, MemoryContext.class.getName(), true ) );
                    else {
                        try {
                            // If another context, must use lookup to create a duplicate.
                            object = parent.lookup( key );
                            noContexts.addElement( new Binding( key, object.getClass().getName(), object, true ) );
                        } catch ( NamingException except ) { }
                    }
                } else if ( object instanceof Reference ) {
                    if ( names )
                        noContexts.addElement( new NameClassPair( key, ( (Reference) object ).getClassName(), true ) );
                    else {
                        try {
                            object = NamingManager.getObjectInstance( object, new CompositeName( key ), parent, null );
                            noContexts.addElement( new Binding( key, object.getClass().getName(), object, true ) );
                        } catch ( Exception except ) { }
                    }
                } else if ( ! ( object instanceof LinkRef )  ) {
                    if ( names )
                        noContexts.addElement( new NameClassPair( key, object.getClass().getName(), true ) );
                    else
                        noContexts.addElement( new Binding( key, object.getClass().getName(), object, true ) );
                }
            }
            _enum = noContexts.elements();
        }
    }

    
    public boolean hasMoreElements()
    {
        return _enum.hasMoreElements();
    }

    
    public boolean hasMore()
    {
        return _enum.hasMoreElements();
    }

    
    public Object nextElement()
    {
        return _enum.nextElement();
    }

    
    public Object next()
    {
        return _enum.nextElement();
    }

    
    public void close()
    {
    }
    

}



