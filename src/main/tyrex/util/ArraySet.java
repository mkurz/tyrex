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
 * $Id: ArraySet.java,v 1.2 2000/09/08 23:06:38 mohammed Exp $
 */


package tyrex.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Implementation of a simple Set based on an array. Does not
 * allow multiple entries, does not support null entries.
 */
public class ArraySet
    extends AbstractSet
{
    
    
    /**
     * The table of all objects in this set.
     */
    protected Object[]  _table = null;
    
    
    /**
     * Construct a new empty set.
     */
    public ArraySet()
    {
    }
    
    
    /**
     * Construct a new set from the specified set and linked to a subject. 
     * The subject
     * is used to assure the set is not modifiable if the subject
     * is set to read only. The type is optional, if specified
     * the set will only contain elements of the specified type.
     *
     * @param subject the subject
     * @param type the type
     * @param set the set used to populate the created set
     */
    public ArraySet( Collection collection )
    {
        addAll(collection);
    }
    
    
    public Iterator iterator()
    {
        return new ArraySetIterator( this );
    }
    
    
    public int size()
    {
        return ( _table == null ? 0 : _table.length );
    }
    
    
    /**
     * Add the items from the specified collection 
     * to the set. 
     *
     * @param collection the collection
     * @return True if items from the specified collection
     * were added to the set.
     */
    public synchronized boolean addAll( Collection collection )
    {
        if ((null == collection) || (0 == collection.size())) {
            return false;    
        }
        else if ( null == _table ) {
            // check for duplicates?
            boolean checkForDuplicates = !(collection instanceof Set);
            // the item in the collection to add
            Object item = null;
            // the index in the table to add the next item in the collection
            int index = 0;
            // loop counter
            int j;
            // make the table the required length
            _table = new Object[collection.size()];
            // loop over the collection adding the items
            outer:
            for (Iterator i = collection.iterator(); i.hasNext();) {
                // get the item
                item = i.next();

                if (!canAdd(item)) {
                    continue;    
                }
                // doing this check should not slow the code
                // down too much 
                if (checkForDuplicates) {
                    for (j = 0; j < index; j++) {
                        if (_table[j].equals(item)) {
                            continue outer;
                        }
                    }
                }
                // add the item
                _table[index++] = item;
            }

            // the index cannot be zero at this point
            // because the initial table was null and
            // collection has items (initial if statement)

            // we may need to shrink the array
            if (index != _table.length) {
                // make a new array
                Object[] newTable = new Object[index];

                if (1 == index) {
                    newTable[0] = _table[0];    
                }
                else {
                    // copy the elements
                    for (j = 0; j < index; j++) {
                        newTable[j] = _table[j];    
                    }
                }
                // set the table
                _table = newTable;
                newTable = null;
            }

            return true;
        }
        else {
            return super.addAll(collection);
        }
    }


    public synchronized boolean add( Object obj )
    {
        Object[] newTable;

        if (!canAdd(obj)) {
            return false;    
        }
        
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
     * Return true if the specified argument can be added
     * to the set.
     * <p>
     * The default implementation returns true.
     *
     * @param object the object
     * @return true if the specified argument can be added
     *      to the set.
     */
    protected boolean canAdd(Object object)
    {
        return true;
    }

    
    /**
     * Returns the element at the specified index, or null if the
     * index is out of bounds. Used by the iterator.
     */
    protected synchronized Object get( int index )
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
    protected class ArraySetIterator
        implements Iterator
    {
        
        
        /**
         * The set which this iterator iterates.
         */
        private ArraySet    _set;
        
        
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
        ArraySetIterator( ArraySet set )
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
