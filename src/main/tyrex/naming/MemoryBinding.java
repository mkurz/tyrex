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
 * $Id: MemoryBinding.java,v 1.9 2001/05/02 00:57:05 mohammed Exp $
 */


package tyrex.naming;


import java.io.PrintWriter;
import java.util.NoSuchElementException;
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
 * Name/value bindings for use inside {@link MemoryContext}.
 * This one is never constructed directly but through {@link
 * MemoryContext}, {@link MemoryContextFactory} and related classes.
 * <p>
 * Provides heirarchial storage for name/value binding in memory
 * that is exposed through the JNDI context model. Each context
 * (not in the tree) is represented by one instance of {@link
 * MemoryBinding}, with each sub-context (child node) or bound
 * value represented by a name/value pair.
 * <p>
 * This object is thread-safe.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.9 $ $Date: 2001/05/02 00:57:05 $
 */
public final class MemoryBinding
{


    /**
     * The initial capacity for the hashtable.
     */
    public static final int   INITIAL_CAPACITY    = 11;


    /**
     * The maximum capacity for the hashtable.
     */
    public static final int   MAXIMUM_CAPACITY    = 191;


    /**
     * The load factor for the hashtable.
     */
    public static final float LOAD_FACTOR         = 0.75f;



    /**
     * The path of this binding.
     */
    private String            _name = "";


    /**
     * The parent memory binding.
     */
    protected MemoryBinding  _parent;


    /**
     * The number of bindings in the hash table.
     */
    private int              _count;


    /**
     * The threshold for resizing the hash table.
     */
    private int              _threshold;


    /**
     * The hashtable of memory binding entries.
     */
    private BindingEntry[]   _hashTable;


    public MemoryBinding()
    {
        _hashTable = new BindingEntry[ INITIAL_CAPACITY ];
        _threshold = (int)( INITIAL_CAPACITY * LOAD_FACTOR );
    }


    public Context getContext()
    
    {
        return new MemoryContext( this, null );
    }


    public synchronized Object get( String name )
    {
        int          hashCode;
        int          index;
        BindingEntry entry;

        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        hashCode = name.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _hashTable.length;
        entry = _hashTable[ index ];
        while ( entry != null ) {
            if ( entry._hashCode == hashCode && entry._name.equals( name ) )
                return entry._value;
            entry = entry._next;
        }
        return null;
    }


    public synchronized void put( String name, Object value )
    {
        int          hashCode;
        int          index;
        BindingEntry entry;
        BindingEntry next;

        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        if ( value == null )
            throw new IllegalArgumentException( "Argument value is null" );

        if ( value instanceof MemoryBinding ) {
            ( (MemoryBinding) value )._parent = this;
            ( (MemoryBinding) value )._name = name;
        }

        hashCode = name.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _hashTable.length;
        entry = _hashTable[ index ];
        if ( entry == null ) {
            entry = new BindingEntry( name, hashCode, value );
            _hashTable[ index ] = entry;
            ++_count;
        } else {
            if ( entry._hashCode == hashCode && entry._name.equals( name ) ) {
                entry._value = value;
                return;
            } else {
                next = entry._next;
                while ( next != null ) {
                    if ( next._hashCode == hashCode && next._name.equals( name ) ) {
                        next._value = value;
                        return;
                    }
                    entry = next;
                    next = next._next;
                }
                entry._next = new BindingEntry( name, hashCode, value );
                ++_count;
            }
        }
        if ( _count >= _threshold )
            rehash();
    }


    public synchronized Object remove( String name )
    { 
        int          hashCode;
        int          index;
        BindingEntry entry;
        BindingEntry next;

        if ( name == null )
            throw new IllegalArgumentException( "Argument name is null" );
        hashCode = name.hashCode();
        index = ( hashCode & 0x7FFFFFFF ) % _hashTable.length;
        entry = _hashTable[ index ];
        if ( entry == null )
            return null;
        if ( entry._hashCode == hashCode && entry._name.equals( name ) ) {
            _hashTable[ index ] = entry._next;
            --_count;
            return entry._value;
        }
        next = entry._next;
        while ( next != null ) {
            if ( next._hashCode == hashCode && next._name.equals( name ) ) {
                entry._next = next._next;
                --_count;
                return next._value;
            }
            entry = next;
            next = next._next;
        }
        return null;
    }


    public String getName()
    {
        if ( _parent != null && _parent.getName().length() > 0 )
            return _parent.getName() + MemoryContext.NameSeparator + _name;
        return _name;
    }


    public boolean isRoot()
    {
        return ( _parent == null );
    }


    public boolean isEmpty()
    {
        return _count == 0;
    }


    /**
     * Called when destroying the subcontext and binding associated
     * with it.
     */
    public void destroy()
    {
        _hashTable = null;
    }


    /**
     * Returns an array of all the name/value bindings in this binding,
     * exclusing sub-contexts. Each entry is of type {@link Binding} as
     * defined in the JNDI API.
     */
    /*
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
    */


    /**
     * Returns an array of all the sub-contexts of this binding.
     */
    /*
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
    */


    void debug( PrintWriter writer )
    {
        debug( writer, 0 );
    }


    private synchronized void debug( PrintWriter writer, int level )
    {
        BindingEntry  entry;
        Object        value;

        for ( int j = level ; j-- > 0 ; )
            writer.print( "  " );
        if ( this instanceof MemoryBinding )
            writer.println( "MemoryBinding: " + getName() );
        else
            writer.println( "ThreadedBinding: " + getName() );
        if ( _count == 0 )
            writer.println( "Empty" );
        else {
            for ( int i = _hashTable.length ; i-- > 0 ; ) {
                entry = _hashTable[ i ];
                while ( entry != null ) {
                    for ( int j = level ; j-- > 0 ; )
                        writer.print( "  " );
                    value = entry._value;
                    if ( value instanceof MemoryBinding )
                        ( (MemoryBinding) value ).debug( writer, level + 1 );
                    else
                        writer.println( "  " + entry._name + " = " + value );
                }
            }
        }
    }
    
    
    protected NamingEnumeration enumerate( Context context, boolean nameOnly )
    {
        return new MemoryBindingEnumeration( context, nameOnly );
    }


    private void rehash()
    {
        int             newSize;
        BindingEntry[]  newTable;
        BindingEntry    entry;
        BindingEntry    next;
        int             index;

        newSize = _hashTable.length * 2 + 1;
        // Prevent the hash table from being resized beyond some maximum capacity limit.
        if ( newSize > MAXIMUM_CAPACITY ) {
            _threshold = Integer.MAX_VALUE;
            return;
        }

        newTable = new BindingEntry[ newSize ];
        for ( int i = _hashTable.length ; i-- > 0 ; ) {
            entry = _hashTable[ i ];
            while ( entry != null ) {
                next = entry._next;
                index = ( entry._hashCode & 0x7FFFFFFF) % newSize;
                entry._next = newTable[ index ];
                newTable[ index ] = entry;
                entry = next;
            }
        }
        _hashTable = newTable;
        _threshold = (int)( newSize * LOAD_FACTOR );
    }


    /**
     * Name to value binding entry in the memory binding hashtable.
     */
    private static class BindingEntry
    {


        /**
         * The binding name.
         */
        final String  _name;


        /**
         * The binding name hash code.
         */
        final int     _hashCode;


        /**
         * The bound value.
         */
        Object        _value;


        /**
         * The next binding in the hash table entry.
         */
        BindingEntry  _next;


        BindingEntry( String name, int hashCode, Object value )
        {
            _name = name;
            _hashCode = hashCode;
            _value = value;
        }


    }


    /**
     * Naming enumeration supporting {@link NamClassPair} and {@link Binding},
     * created based of a {@link MemoryBinding}.
     *
     * @author <a href="arkin@intalio.com">Assaf Arkin</a>
     * @version $Revision: 1.9 $ $Date: 2001/05/02 00:57:05 $
     * @see MemoryBinding
     */
    private final class MemoryBindingEnumeration
        implements NamingEnumeration
    {
        
        
        /**
         * Holds a reference to the next entry to be returned by
         * {@link next}. Becomes null when there are no more
         * entries to return.
         */
        private BindingEntry    _entry;
        
        
        /**
         * Index to the current position in the hash table.
         */
        private int             _index;


        /**
         * True to return an enumeration of {@link NameClassPair},
         * false to return an enumeration of {@link Binding}
         */
        private final boolean   _nameOnly;


        /**
         * The context is required to create a duplicate.
         */
        private final Context   _context;


        MemoryBindingEnumeration( Context context, boolean nameOnly )
        {
            if ( context == null )
                throw new IllegalArgumentException( "Argument context is null" );
            _context = context;
            _nameOnly = nameOnly;
            _index = _hashTable.length;
        }



        public boolean hasMoreElements()
        {
            return hasMore();
        }
        
        
        public Object nextElement()
        {
            return next();
        }
        
    
        public void close()
        {
            _entry = null;
            _index = -1;
        }
    

        public boolean hasMore()
        {
            BindingEntry   entry;
            int            index;
            BindingEntry[] table;
            
            if ( _entry != null ) {
                return true;    
            }
            
            table = _hashTable;
            index = _index;
            while ( index > 0 ) {
                entry = table[ --index ];
                if ( entry != null ) {
                    _entry = entry;
                    _index = index;
                    return true;
                }
            }
            _entry = null;
            _index = -1;
            return false;
        }
        
        
        public Object next()
            throws NoSuchElementException
        {
            BindingEntry  entry;
            Object        value;
            
            entry = nextEntry();
            value = entry._value;
            if ( value instanceof MemoryBinding ) {
                if ( _nameOnly )
                    return new NameClassPair( entry._name, MemoryContext.class.getName(), true );
                else {
                    try {
                        // If another context, must use lookup to create a duplicate.
                        value = _context.lookup( entry._name );
                        return new Binding( entry._name, value.getClass().getName(), value, true );
                    } catch ( NamingException except ) {
                        // Skip this entry and go immediately to next one.
                        return next();
                    }
                }
            } else if ( value instanceof Reference ) {
                if ( _nameOnly )
                    return new NameClassPair( entry._name, ( (Reference) value ).getClassName(), true );
                else {
                    try {
                        value = NamingManager.getObjectInstance( value, new CompositeName( entry._name ), _context, null );
                        return new Binding( entry._name, value.getClass().getName(), value, true );
                    } catch ( Exception except ) {
                        // Skip this entry and go immediately to next one.
                        return next();
                    }
                }
            } else if ( ! ( value instanceof LinkRef )  ) {
                if ( _nameOnly )
                    return new NameClassPair( entry._name, value.getClass().getName(), true );
                else
                    return new Binding( entry._name, value.getClass().getName(), value, true );
            }
            // Skip this entry and go immediately to next one.
            return next();
        }
        
        
        private BindingEntry nextEntry()
            throws NoSuchElementException
        {
            BindingEntry   entry;
            int            index;
            BindingEntry[] table;
            
            entry = _entry;
            if ( entry != null ) {
                _entry = entry._next;
                return entry;
            }
            table = _hashTable;
            index = _index;
            while ( entry == null && index > 0 ) {
                entry = table[ --index ];
                if ( entry != null ) {
                    _entry = entry._next;
                    _index = index;
                    return entry;
                }
            }
            _entry = null;
            _index = -1;
            throw new NoSuchElementException( "No more elements in collection" );
        }
        
        
    }


}

