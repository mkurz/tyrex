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
 * $Id: WeakList.java,v 1.1 2000/01/11 00:33:46 roro Exp $
 */


package tyrex.util;


import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Array;


/**
 * List of weak references allows objects to be tracked and claimed by
 * the garbage collector. This is a simple implementation based on an
 * array and users {@link WeakReference} for its entries.
 * <p>
 * The base assumption about this list is that it holds objects used
 * by the application that require extra processing. When the application
 * releases object, we are no longer interested in processing it. This
 * list has preference to not hold or return objects that have been
 * garbage collected (even if not finalized yet).
 * <p>
 * There is no direct access to the list, the only way to retrieve
 * entries is through {@link #list}. Since unreferenced entires may be
 * removed at any time, implementing indexed access is a bit hard.
 * Instead, {@link #list} produces an array that points to all the
 * referenced elements, and that array can be accessed by index. For as
 * long as this array is referenced, no entries will be claimed.
 * <p>
 * Certain optimization decisions have been made in this list based
 * on its projected usage, in particular:
 * <ul>
 * <li>The internal array is not created until the first entry is
 * placed in it -- the assumption is that many lists will go empty
 * for the duration of their life
 * <li>The array is initialized with a place for {@link #INITIAL_SIZE}
 * entries whiche seems a good compromise
 * <li>When entries are removed from the array, their location in the
 * table is set to null and reused later on, the array is not compacted
 * <li>Unreferenced entries are discarded first, before performing any
 * operation on the table
 * </ul>
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/01/11 00:33:46 $
 * @see WeakReference
 */
public class WeakList
{


    /**
     * The table of references. Initially null until the first entry
     * is added to it. Entries in the table may be null.
     */
    private Reference[]           _table;


    /**
     * The queue is used to poll enqueued reference so we can
     * discard them whenever the table is accessed.
     */
    private static ReferenceQueue _queue = new ReferenceQueue();


    /**
     * This is the initial size of the array. When the first entry is
     * added, the array is created to hold that many entries. The
     * larger this number is, the more memory we waste but the less
     * array resizing we need if usage does grow within these bounds.
     * 2 looks like a reasonable compromise for most cases.
     */
    public static final int INITIAL_SIZE = 2;


    /**
     * Adds a new object to the list.
     *
     * @param obj The object to add
     */
    public void add( Object obj )
    {
	Reference[] newTable;
	int         i;

	if ( obj == null )
	    throw new NullPointerException( "Argument 'obj' is null" );
	// If table is null, create a new one with the initial size.
	if ( _table == null ) {
	    _table = new Reference[ INITIAL_SIZE ];
	    _table[ 0 ] = new WeakReference( obj, _queue );
	} else {
	    // Discard all unreferenced entries from the table first.
	    // Look for empty places in the array and use them.
	    // We assume the array is small enough and this is more
	    // efficient than resizing it.
	    processQueue();
	    for ( i = 0 ; i < _table.length ; ++i )
		if ( _table[ i ] == null || _table[ i ].get() == null ) {
		    _table[ i ] = new WeakReference( obj, _queue );
		    return;
		}
	    newTable = new Reference[ _table.length + 1 ];
	    System.arraycopy( _table, 0, newTable, 0, _table.length );
	    newTable[ _table.length ] = new WeakReference( obj, _queue );
	    _table = newTable;
	}
    }


    /**
     * Removes an object from the list. If the object was in the list,
     * it is returned. Returns null if the object was not in the list.
     *
     * @param obj The object to remove
     * @return The removed object, null if the object was not there
     */
    public Object remove( Object obj )
    {
	Reference   ref;
	int         i;

	if ( obj == null )
	    throw new NullPointerException( "Argument 'obj' is null" );
	if ( _table == null )
	    return null;
	// Kill all unreferenced entries first, so it will accurately
	// report an entry that was garbage collected with a null
	// return. Note that we just nullify the entry in the array,
	// we don't attempt to compact the array.
	processQueue();
	for ( i = 0 ; i < _table.length ; ++i ) {
	    ref = _table[ i ];
	    if ( ref != null && ref.get() == obj ) {
		_table[ i ] = null;
		return obj;
	    }
	}
	return null;
    }


    /**
     * Clears the contents of this list.
     */
    public void clear()
    {
	int i;

	if ( _table != null ) {
	    for ( i = 0 ; i < _table.length ; ++i )
		_table[ i ] = null;
	    _table = null;
	}
	processQueue();
    }


    /**
     * Returns true if the element is contained in the list and has not
     * been garbage collected yet.
     *
     * @param obj The object to test
     * @return True if the object is contained in the list
     */
    public boolean contains( Object obj )
    {
	Reference   ref;
	int         i;

	if ( _table == null )
	    return false;
	// Kill all unreferenced entries first, so it will accurately
	// report an entry that was garbage collected with a null
	// return. Note that we just nullify the entry in the array,
	// we don't attempt to compact the array.
	processQueue();
	for ( i = 0 ; i < _table.length ; ++i ) {
	    ref = _table[ i ];
	    if ( ref != null && ref.get() == obj )
		return true;
	}
	return false;
    }


    /**
     * Returns an array representing the contents of this list. Only
     * objects that are referenced will be returned in the array,
     * and these objects will be referenced by the array and not
     * discarded for as long as the returned array is referenced.
     * This is the only way to access the list by index and operate
     * on it without safety checks.
     *
     * @return An array representing the contents of the list
     */
    public Object[] list()
    {
	Reference ref;
	int       i;
	Object[]  list;
	int       index;

	if ( _table == null )
	    return new Object[ 0 ];
	// First discard of all unreferenced entries.
	processQueue();
	list = new Object[ _table.length ];
	index = 0;
	for ( i = 0 ; i < _table.length ; ++i ) {
	    ref = _table[ i ];
	    if ( ref != null ) {
		list[ index] = ref.get();
		if ( list[ index ] != null )
		    ++index;
	    }
	}
	// The list might not be the same size of the array if we
	// lost entries recently, so update the returned array to
	// reflect the true size.
	if ( index == 0 )
	    return new Object[ 0 ];
	else if ( index == list.length )
	    return list;
	else {
	    Object[] newList;

	    newList = new Object[ index ];
	    System.arraycopy( list, 0, newList, 0, index );
	    return newList;
	}
    }


    /**
     * Returns an array representing the contents of this list. Only
     * objects that are referenced will be returned in the array,
     * and these objects will be referenced by the array and not
     * discarded for as long as the returned array is referenced.
     * This is the only way to access the list by index and operate
     * on it without safety checks.
     *
     * @return An array representing the contents of the list
     */
    public Object[] list( Class type )
    {
	Reference ref;
	int       i;
	Object[]  list;
	int       index;

	if ( _table == null )
	    return (Object[]) Array.newInstance( type, 0 );
	// First discard of all unreferenced entries.
	processQueue();
	list = (Object[]) Array.newInstance( type, _table.length );
	index = 0;
	for ( i = 0 ; i < _table.length ; ++i ) {
	    ref = _table[ i ];
	    if ( ref != null ) {
		list[ index] = ref.get();
		if ( list[ index ] != null )
		    ++index;
	    }
	}
	// The list might not be the same size of the array if we
	// lost entries recently, so update the returned array to
	// reflect the true size.
	if ( index == 0 )
	    return (Object[]) Array.newInstance( type, 0 );
	else if ( index == list.length )
	    return list;
	else {
	    Object[] newList;

	    newList = (Object[]) Array.newInstance( type, index );
	    System.arraycopy( list, 0, newList, 0, index );
	    return newList;
	}
    }

	

    /**
     * Used internally to discard entries in the array which hold
     * references to garbage collected objects.
     */
    private void processQueue()
    {
	Reference ref;
	int       i;

	ref = _queue.poll();
	while ( ref != null ) {
	    for ( i = 0 ; i < _table.length ; ++i )
		if ( _table[ i ] == ref ) {
		    _table[ i ] = null;
		    break;
		}
	    ref = _queue.poll();
	}
    }


    public static void main( String args[] )
    {
	try {
	    
	    Integer one = new Integer( 1 );
	    Integer two = new Integer( 2 );
	    Integer[] list;
	    int      i;
	    
	    WeakList wl = new WeakList();
	    wl.add( one );
	    wl.add( two );
	    list = (Integer[]) wl.list( Integer.class );
	    for ( i = 0 ; i < list.length ; ++i ) {
		System.out.println( list[ i ] );
		list[ i ] = null;
	    }
	    list = null;
	    two = null;
	    Runtime.getRuntime().gc();
	    list = (Integer[]) wl.list( Integer.class );
	    for ( i = 0 ; i < list.length ; ++i ) {
		System.out.println( list[ i ] );
		list[ i ] = null;
	    }
	    
	    list = null;
	    one = null;
	    Runtime.getRuntime().gc();
	    list = (Integer[]) wl.list( Integer.class );
	    for ( i = 0 ; i < list.length ; ++i )
		System.out.println( list[ i ] );
	    
	} catch ( Exception except ) {
	    System.out.println( except );
	    except.printStackTrace();
	}
    }


}

