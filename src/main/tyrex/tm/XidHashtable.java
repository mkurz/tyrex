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
 * $Id: XidHashtable.java,v 1.2 2000/09/08 23:06:13 mohammed Exp $
 */


package tyrex.tm;


import java.util.Enumeration;
import java.util.NoSuchElementException;
import tyrex.util.Messages;


/**
 * Efficient implementation of a simple hashtable using global
 * identifiers as keys. Since global identifiers are held as byte
 * arrays, they cannot be used as keys in a regular hashtable (hashing
 * function and equality tests won't work). This class implements a
 * hash table that can perform optimizing hashing and accurate equality
 * tests on the global identifiers.
 * <p>
 * This hashtable does not implement the {@link java.util.Dictionary}
 * interface since it deals with a specific type of keys that are
 * not generic objects. Another difference is that this hashtable
 * allows {@link #put} with a null value.
 * <p>
 * This hashtable implements internal synchronization, so there is no
 * need to synchronize access directly. Synchronization is implemented
 * more efficiently than with {@link java.util.Hashtable} since we
 * assume that the same key will never be added or removed more than
 * once, so we only care to synchronize different keys (quite easy).
 * <p>
 * The enumerations returned from this hashtable are fail-safe but
 * not live. They can be used as entries are added/removed from the
 * hashtable, although such changes might not be reflected
 * immediately.
 * <p>
 * This hashtable is implemented for efficiency based on the local
 * implementation of global identifiers in {@link XidImpl}.
 * It cannot properly deal with any other form of Xid.
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/09/08 23:06:13 $
 * @see XidImpl
 */
public final class XidHashtable
{


    /**
     * Underneath a hashtable there is always an array. The array
     * holds a linked list of entries, all of which happen to have
     * the exact same hashcode (or a modulo of it)
     */
    private Entry[] _table;


    /**
     * Holds the number of elements stored in the table, not the
     * table size. Required to efficiently obtain the table's size.
     *
     * @see #size
     */
    private int     _size;


    /**
     * Determines the size of the hash table. This must be a prime
     * value and within range of the average number of Xids we
     * want to deal with. Potential values are:
     * <pre>
     * Threads  Prime
     * -------  -----
     *   256     239
     *   512     521
     *   1024    1103
     *   2048    2333
     *   4096    4049
     * </pre>
     */
    private static final int TABLE_SIZE = 4049;


    public XidHashtable()
    {
	this( TABLE_SIZE );
    }


    public XidHashtable( int tableSize )
    {
	_table = new Entry[ tableSize ];
    }


    public Object get( byte[] gxid )
    {
	int    hash;
	Entry  entry;

	hash = ( hashCode( gxid ) & 0x7FFFFFFF ) % _table.length;

	// Lookup the first entry that maps to the has code and
        // continue iterating to the last entry until a matching entry
        // is found. Even if the current entry is removed, we expect
        // entry.next to point to the next entry.
	entry = _table[ hash ];
	while ( entry != null && entry.gxid != gxid &&
		! equals( entry.gxid, gxid ) )
	    entry = entry.next;

	if ( entry != null )
	    return entry.value;
	else
	    return null;
    }


    public Object put( byte[] gxid, Object value )
    {
	int    hash;
	Entry  entry;

	hash = ( hashCode( gxid ) & 0x7FFFFFFF ) % _table.length;

	// This portion is idential to lookup, but if we find the entry
	// we change it's value and return.
	entry = _table[ hash ];
	while ( entry != null && entry.gxid != gxid &&
		! equals( entry.gxid, gxid ) )
	    entry = entry.next;
	if ( entry != null ) {
	    Object oldValue;

	    oldValue = entry.value;
	    entry.value = value;
	    if ( value == null )
		--_size;
	    return oldValue;
	}

	// No such entry found, so we must create it. Create first to
	// minimize contention period. (Object creation is such a
	// length operation)
	entry = new Entry();
	entry.value = value;
	entry.gxid = gxid;

	// This operation must be synchronized, otherwise, two concurrent
	// set() methods might insert only one entry. (Not even talking
	// about what remove() would cause).
	synchronized ( _table ) {
	    entry.next = _table[ hash ];
	    _table[ hash ] = entry;
	    ++_size;
	}
	return null;
    }


    Object remove( byte[] gxid )
    {
	int   hash;
	Entry entry;

	hash = ( hashCode( gxid ) & 0x7FFFFFFF ) % _table.length;

	// This operation must be synchronized because it messes
	// with the entry in the table, and set() likes to mess
	// with the same entry.
	synchronized ( _table ) {
	    entry = _table[ hash ];
	    // No such entry, quit. This is the entry, remove
	    // it and quit.
	    if ( entry == null )
		return null;
	    if ( entry.gxid == gxid || equals( entry.gxid, gxid ) ) {
		_table[ hash ] = entry.next;
		--_size;
		return entry.value;
	    }
	}

	// Not the first entry. We can only remove the next
	// entry by changing the next reference on this entry,
	// so we have to iterate on this entry to remove the
	// next entry and so our last entry is the one before
	// last. Sigh.
	while ( entry.next != null && entry.next.gxid != gxid &&
		! equals( entry.next.gxid, gxid ) )
	    entry = entry.next;
	// No need to synchronized, but keep in mind that get()
	// expect next to be current, even if the entry has been
	// removed, so don't reset next!
	if ( entry.next != null ) {
	    Object oldValue;

	    oldValue = entry.next.value;
	    entry.next = entry.next.next;
	    --_size;
	    return oldValue;
	}
	return null;
    }


    public int size()
    {
	return _size;
    }


    public Enumeration keys()
    {
	return new XidHashtableEnumeration( _table, false );
    }


    public Enumeration elements()
    {
	return new XidHashtableEnumeration( _table, true );
    }


    /**
     * We use this equality test internally, since there's no other
     * way to compare two byte array (which is what we're using as
     * keys).
     *
     * @param one A global xid
     * @param two A global xid
     * @return True if both are identical
     */
    private boolean equals( byte[] one, byte[] two )
    {
	int i;

	for ( i = 0 ; i < XidImpl.GLOBAL_XID_LENGTH ; ++i )
	    if ( one[ i ] != two[ i ] )
		return false;
	return true;
    }


    /**
     * We use this hashing function internally, since there's no
     * other way to obtain a unique hash for an array of bytes
     * based on it's content (hashCode() works on the Object itself).
     * The hash code is calculated for the unique part of the
     *
     * @param gxid The global transaction identifier
     * @return Unique hash code (could be a negative value)
     */
    private int hashCode( byte[] gxid )
    {
	int i;
	int hash;

	// The truely unique part of the global xid is the unique
	// transaction identifier. Note that we're squeezing 8
	// (more for otid) bytes of identifier into four bytes
	// of result (the machine identifier is equal for all Xids).
	hash = 0;
	for ( i = XidImpl.GLOBAL_XID_LENGTH - XidImpl.MACHINE_ID_LENGTH ;
	      i-- > 0 ; )
	    hash = ( hash << 4 ) + gxid[ i ];
	return hash;
    }




    /**
     * Each entry in the table has a key (gxid), a value or null
     * (we don't remove on null) and a reference to the next entry in
     * the same table position.
     */
    static class Entry
    {

	Object  value;

	Entry   next;

	byte[]  gxid;

    }

    /**
     * Used to enumerate the keys and values in the table. This
     * enumerator is designed to allow us to traverse the table
     * while entries are added and removed from it without the need
     * to synchronize. It has the same good-state-keeping demands
     * as {@link XidHashtable#get}.
     * <p>
     * While this enumeration does not break if entries are added/
     * removed to the table, it is not live either and these changes
     * might (but most probably will) not be reflected in the
     * enumeration.
     */
    static class XidHashtableEnumeration
	implements Enumeration
    {
	

	/**
	 * Holds a reference to the table.
	 */
	private Entry[]  _table;
	
	
	/**
	 * Holds a reference to the next entry to be returned by
	 * {@link nextElement}. Becomes null when there are no more
	 * entries in the table.
	 */
	private Entry    _entry;
	
	
	/**
	 * Index to the current position in the table. This is the
	 * index where we retrieved {@link #_entry} from.
	 */
	private int      _index;
	
	
	/**
	 * True if an enumeration of elements, false if an
	 * enumeration of keys.
	 */
	private boolean _element;
	
	
	XidHashtableEnumeration( Entry[] table, boolean element )
	{
	    _table = table;
	    _index = 0;
	    _element = element;
	    _entry = _table[ 0 ];
	}
	
	
	public boolean hasMoreElements()
	{
	    // We are pointing to the next entry to return from
	    // nextElement(), so if _entry is not null there is
	    // a next element. If _entry is null we have to
	    // iterate over the table to find the next _entry in
	    // there. When we reach the end of the table, there
	    // are no more elements. This means a full iteration
	    // over the table, but there is no alternative.
	    while ( _entry == null && _index < _table.length - 1 ) {
		++_index;
		_entry = _table[ _index ];
	    }
	    return ( _entry != null );
	}
	
	
	public Object nextElement()
	{
	    Object value;
	    
	    // We are pointing to the next entry to return, so if
	    // _entry is not null we return it. Otherwise, we
	    // lookup the next possible entry (see hasMoreElements()).
	    while ( _entry == null && _index < _table.length - 1 ) {
		++_index;
		_entry = _table[ _index ];
	    }
	    // We retrieve the entry's element or key and return it,
	    // just after moving _entry to the next entry. We have
	    // the same consistency requirements as the get() method.
	    // We might return an entry that has been removed from
	    // the table, although the possibily is very low. This is
	    // not a problem!
	    if ( _entry != null ) {
		value = _element ? _entry.value : _entry.gxid;
		_entry = _entry.next;
		return value;
	    }
	    throw new NoSuchElementException( Messages.message( "tyrex.misc.noMoreXid" ) );
	}
	
	
    }
    
    
}
