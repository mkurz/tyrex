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
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: MemoryBinding.java,v 1.4 2000/08/28 19:01:49 mohammed Exp $
 */


package tyrex.naming;


import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import javax.naming.Binding;


/**
 * Name/value bindings for use inside {@link MemoryContext}.
 * This one is never constructed directly but through {@link
 * MemoryContext}, {@link MemoryContextFactory} and related classes.
 * <p>
 * Provides heirarchial storage for name/value binding in memory
 * that is exposed through the JNDI context model. Each context
 * (not in the tree) is represented by one instance of {@link
 * MemoryBinding}, with each sub-context (child node) or bound
 * value represented by a name/value pair.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $ $Date: 2000/08/28 19:01:49 $
 */
final class MemoryBinding
    extends Dictionary
{


    /**
     * The path of this binding.
     */
    private String    _name = "";


    /**
     * The name/value bindings in this space.
     */
    private final Hashtable _bindings = new Hashtable();


    /**
     * The parent space.
     */
    protected MemoryBinding _parent;


    MemoryBinding()
    {
    }


    public Object get( Object key )
    {
	return _bindings.get( key );
    }


    public Object put( Object key, Object value )
    {
	if ( value instanceof MemoryBinding )
	    ( (MemoryBinding) value ).setContext( this, key.toString() );
	return _bindings.put( key, value );
    }


    public Object remove( Object key )
    {
	Object value;

	value = _bindings.remove( key );
	if ( value instanceof MemoryBinding ) {
	    ( (MemoryBinding) value ).setContext( null, "" );
	}
	return value;
    }


    public int size()
    {
	return _bindings.size();
    }


    public boolean isEmpty()
    {
	return _bindings.isEmpty();
    }


    public Enumeration elements()
    {
	return _bindings.elements();
    }


    public Enumeration keys()
    {
	return _bindings.keys();
    }


    public String getName()
    {
	if ( _parent != null && _parent.getName().length() > 0 ) {
	    return _parent.getName() + MemoryContext.NameSeparator + _name;
	}
	return _name;
    }


    public void setName( String name )
    {
	_name = name;
    }


    public boolean isRoot()
    {
        return ( _parent == null );
    }


    /**
     * Called when binding these bindings to a parent binding.
     *
     * @param binding The parent binding
     * @param name The name of this binding
     */
    protected void setContext( MemoryBinding parent, String name )
    {
	_parent = parent;
	_name = name;
    }


    /**
     * Called when destroying the subcontext and binding associated
     * with it.
     */
    public void destroy()
    {
	_bindings.clear();
    }


    /**
     * Returns an array of all the name/value bindings in this binding,
     * exclusing sub-contexts. Each entry is of type {@link Binding} as
     * defined in the JNDI API.
     */
    public Binding[] getBinding()
    {
	Vector      list;
	Enumeration enum;
	String      key;

	list = new Vector();
	enum = _bindings.keys();
	while ( enum.hasMoreElements() ) {
	    key = (String) enum.nextElement();
	    if ( ! ( _bindings.get( key ) instanceof MemoryBinding ) ) {
		list.addElement( new Binding( key, _bindings.get( key ) ) );
	    }
	}
	return (Binding[]) list.toArray( new Binding[ list.size() ] );
    }


    /**
     * Returns an array of all the sub-contexts of this binding.
     */
    public MemoryBinding[] getContext()
    {
	Vector      list;
	Enumeration enum;
	String      key;

	list = new Vector();
	enum = _bindings.keys();
	while ( enum.hasMoreElements() ) {
	    key = (String) enum.nextElement();
	    if ( _bindings.get( key ) instanceof MemoryBinding ) {
		list.addElement( _bindings.get( key ) );
	    }
	}
	return (MemoryBinding[]) list.toArray( new MemoryBinding[ list.size() ] );
    }


    void debug( PrintWriter writer )
    {
	debug( writer, 0 );
    }


    private void debug( PrintWriter writer, int level )
    {
	Enumeration enum;
	String      key;
	Object      value;
	int         i;

	for ( i = 0 ; i < level ; ++i )
	    writer.print( "  " );
	if ( this instanceof MemoryBinding )
	    writer.println( "MemoryBinding: " + getName() );
	else
	    writer.println( "ThreadedBinding: " + getName() );
	enum = keys();
	if ( ! enum.hasMoreElements() ) {
	    for ( i = 0 ; i < level ; ++i )
		writer.print( "  " );
	    writer.println( "Empty" );
	} else {
	    while ( enum.hasMoreElements() ) {
		key = (String) enum.nextElement();
		value = get( key );
		for ( i = 0 ; i < level ; ++i )
		    writer.print( "  " );
		writer.println( "  " + key + " = " + value );
		if ( value instanceof MemoryBinding ) {
		    ( (MemoryBinding) value ).debug( writer, level + 1 );
		}
	    }
	}
    }


}

