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
 * $Id: Subject.java,v 1.1 2000/02/23 21:22:19 arkin Exp $
 */


package tyrex.security.auth;


import java.io.Serializable;
import java.util.Set;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.AbstractSet;
import java.security.Principal;


/**
 * A subject groups together several principals and credentials
 * representing the same entity. This implementation is modeled after
 * the JAAS <tt>Subject</tt>, but downgraded for Java 1.2. Will be
 * replaced by JAAS when we move to 1.3. Please refer to the JAAS
 * JavaDoc for details on how to use this class.
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/02/23 21:22:19 $
 */
public final class Subject
    implements Serializable
{


    /**
     * True if the subject is read-only.
     */
    private boolean        _readOnly;
    
    
    /**
     * The set of prinicipals associated with this subject.
     * Serializable.
     */
    private Set            _princs;
    
    
    /**
     * The set of public credentials associated with this subject.
     * Non serializable.
     */
    private transient Set _pubCreds;
    
    
    /**
     * The set of private credentials associated with this subject.
     * Non serializable.
     */
    private transient Set _privCreds;
    
    
    public Subject()
    {
        _princs = new SimpleSet( this, Principal.class );
        _pubCreds = new SimpleSet( this, null );
        _privCreds = new SimpleSet( this, null );
    }
    
    
    public Subject( boolean readOnly, Set principals, Set pubCreds, Set privCreds )
    {
        _readOnly = readOnly;
        _princs = principals;
        _pubCreds = pubCreds;
        _privCreds = privCreds;
    }
    
    
    public Set getPrincipals()
    {
        return _princs;
    }
    
    
    public Set getPrincipals( Class cls )
    {
        return typeSubset( _princs.iterator(), cls );
    }
    
    
    public Set getPrivateCredentials()
    {
        return _privCreds;
    }
    
    
    public Set getPrivateCredentials( Class cls )
    {
        return typeSubset( _privCreds.iterator(), cls );
    }
    
    
    public Set getPublicCredentials()
    {
        return _pubCreds;
    }
    
    
    public Set getPublicCredentials( Class cls )
    {
        return typeSubset( _pubCreds.iterator(), cls );
    }
    
    
    public boolean isReadOnly()
    {
        return _readOnly;
    }
    
    
    public void setReadOnly()
    {
        _readOnly = true;
    }
    
    
    /**
     * Returns a new set containing just the objects of a particular
     * type from the implementation. The set includes any object that
     * extends or implements the selected type.
     */
    private Set typeSubset( Iterator iter, Class type )
    {
        Set    set;
        Object obj;
        
        set = new SimpleSet();
        while ( iter.hasNext() ) {
            obj = iter.next();
            if ( type.isAssignableFrom( obj.getClass() ) )
                set.add( obj );
        }
        return set;
    }
    
    
    /**
     * Implementation of a simple Set based on an array. Does not
     * allow multiple entries, does not support null entries, can do
     * type checking, and check whether the Subject is read only.
     * <p>
     * Considering that we do not expect too many credentials or
     * principals, this set is based on a simple array. When using
     * many credentials/principals per subject, might be more
     * efficient to use a hashtable, or better still an array based
     * on the type of each entry.
     */
    class SimpleSet
        extends AbstractSet
    {
        
        
        /**
         * The table of all objects in this set.
         */
        private Object[]  _table = null;
        
        
        
        /**
         * The type of elements supported by this set (base class or
         * interface) or null if no type checking is required.
         */
        private Class     _type = null;
        
        
        /**
         * Reference to the subject that holds this set, assures
         * that the set is not modifiable if the subject is set
         * to read only.
         */
        private Subject   _subject;
        
        
        /**
         * Construct a new empty set.
         */
        SimpleSet()
        {
        }
        
        
        /**
         * Construct a new empty set linked to a subject. The subject
         * is used to assure the set is not modifiable if the subject
         * is set to read only. The type is optional, if specified
         * the set will only contain elements of the specified type.
         */
        SimpleSet( Subject subject, Class type )
        {
            _subject = subject;
            _type = type;
        }
        
        
        public Iterator iterator()
        {
            return new SimpleSetIterator( this );
        }
        
        
        public int size()
        {
            return ( _table == null ? 0 : _table.length );
        }
        
        
        public synchronized boolean add( Object obj )
        {
            Object[] newTable;
            
            if ( obj == null )
                throw new IllegalArgumentException( "Argument 'obj' is null" );
            if ( _subject != null && _subject.isReadOnly() )
                throw new IllegalStateException( "Cannot modify this set: the subject has been set read only" );
            if ( _type != null && ! _type.isAssignableFrom( obj.getClass() ) )
                throw new ClassCastException( "Object to add does not implement " + _type.getName() );
            if ( _table == null ) {
                _table = new Object[ 1 ];
                _table[ 0 ] = obj;
                return true;
            }
            for ( int i = 0 ; i < _table.length ; ++i ) {
                if ( _table[ i ].equals( obj ) )
                    return false;
            }
            newTable = new Object[ _table.length + 1 ];
            for ( int i = 0 ; i < _table.length ; ++i )
                newTable[ i ] = _table[ i ];
            newTable[ _table.length ] = obj;
            _table = newTable;
            return true;
        }
        
        
        public synchronized boolean remove( Object obj )
        {
            Object[] newTable;
            
            if ( _subject != null && _subject.isReadOnly() )
                throw new IllegalStateException( "Cannot modify this set: the subject has been set read only" );
            if ( _table == null )
                return false;
            if ( _table.length == 1 ) {
                if ( _table[ 0 ] == obj ) {
                    _table = null;
                    return true;
                }
                return false;
            }	 
            for ( int i = 0 ; i < _table.length ; ++i ) {
                if ( _table[ i ].equals( obj ) ) {
                    _table[ i ] = _table[ _table.length - 1 ];
                    newTable = new Object[ _table.length - 1 ];
                    for ( int j = 0 ; j < _table.length - 1 ; ++j )
                        newTable[ j ] = _table[ j ];
                    _table = newTable;
                    return true;
                }
            }
            return false;
        }
        
        
        /**
         * Returns the element at the specified index, or null if the
         * index is out of bounds. Used by the iterator.
         */
        synchronized Object get( int index )
        {
            if ( _table == null )
                return null;
            if ( index < _table.length )
                return ( _table[ index ] );
            return null;
        }
        
        
        /**
         * AbstractSet requires an implementation for the iterator.
         */
        class SimpleSetIterator
            implements Iterator
        {
            
            
            /**
             * The set which this iterator iterates.
             */
            private SimpleSet    _set;
            
            
            /**
             * The index inside the set table of objects.
             */
            private int         _index = 0;
            
            
            /**
             * The last object returned from <tt>next</tt>.
             */
            private Object      _lastObject = null;
            
            
            /**
             * Construct a new iterator for the give set.
             */
            SimpleSetIterator( SimpleSet set )
            {
                _set = set;
            }
            
            
            public boolean hasNext()
            {
                // size() is implemented efficiently
                return _index < _set.size();
            }
            
            
            public Object next()
            {
                // Get the last object first, get() will return null if
                // past the end of the object table. The last object is
                // required by remove().
                _lastObject = _set.get( _index );
                if ( _lastObject == null )
                    throw new NoSuchElementException( "The iterator has no more elements" );
                ++_index;
                return _lastObject;
            }
            
            
            public void remove()
            {
                if ( _lastObject == null )
                    throw new IllegalStateException( "Method remove called without prior call to next" );
                // Depends on Set.remove() to actually remove the object
                // and handle synchronization. (Note: AbstractSet.remove()
                // is actually dependent on the iterator, this implementation
                // has it reversed just for the heck of it).
                _set.remove( _lastObject );
            }
            
            
        }
        
        
    }
    
       
}
