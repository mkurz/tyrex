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
 * $Id: WeakList.java,v 1.5 2001/03/12 19:20:21 arkin Exp $
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
 * for the duration of their life/</li>
 * <li>The array is initialized with a place for {@link #INITIAL_SIZE}
 * entries whiche seems a good compromise.</li>
 * <li>When entries are removed from the array, their location in the
 * table is set to null and reused later on, the array is not compacted
 * <li>Unreferenced entries are discarded first, before performing any
 * operation on the table.</li>
 * </ul>
 * <p>
 * This object is not thread-safe.
 *
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.5 $ $Date: 2001/03/12 19:20:21 $
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
    public static final int       INITIAL_SIZE = 2;


    /**
     * Adds a new object to the list.
     *
     * @param object The object to add
     */
    public void add( Object object )
    {
        Reference[] table;
        Reference[] newTable;
        int         length;

        if ( object == null )
            throw new IllegalArgumentException( "Argument object is null" );
        // If table is null, create a new one with the initial size.
        if ( _table == null ) {
            _table = new Reference[ INITIAL_SIZE ];
            _table[ 0 ] = new WeakReference( object, _queue );
        } else {
            // Discard all unreferenced entries from the table first.
            // Look for empty places in the array and use them.
            // We assume the array is small enough and this is more
            // efficient than resizing it.
            processQueue();
            table = _table;
            length = table.length;
            for ( int i = length ; i-- > 0 ; )
                if ( table[ i ] == null || table[ i ].get() == null ) {
                    table[ i ] = new WeakReference( object, _queue );
                    return;
                }
            newTable = new Reference[ length * 2 ];
            for ( int i = length ; i-- > 0 ; )
                newTable[ i ] = table[ i ];
            newTable[ length ] = new WeakReference( object, _queue );
            _table = newTable;
        }
    }


    /**
     * Removes an object from the list. If the object was in the list,
     * it is returned. Returns null if the object was not in the list.
     *
     * @param object The object to remove
     * @return The removed object, null if the object was not there
     */
    public Object remove( Object object )
    {
        Reference   ref;
        Reference[] table;

        if ( object == null )
            throw new IllegalArgumentException( "Argument object is null" );
        table = _table;
        if ( table == null )
            return null;
        // Kill all unreferenced entries first, so it will accurately
        // report an entry that was garbage collected with a null
        // return. Note that we just nullify the entry in the array,
        // we don't attempt to compact the array.
        processQueue();
        for ( int i = table.length ; i-- > 0 ; ) {
            ref = table[ i ];
            if ( ref != null && ref.get() == object ) {
                table[ i ] = null;
                return object;
            }
        }
        return null;
    }


    /**
     * Clears the contents of this list.
     */
    public void clear()
    {
        Reference[] table;

        table = _table;
        if ( table != null ) {
            for ( int i = table.length ; i-- > 0 ; )
                table[ i ] = null;
            table = null;
        }
        processQueue();
    }


    /**
     * Returns true if the element is contained in the list and has not
     * been garbage collected yet.
     *
     * @param object The object to test
     * @return True if the object is contained in the list
     */
    public boolean contains( Object object )
    {
        Reference   ref;
        Reference[] table;

        if ( object == null )
            throw new IllegalArgumentException( "Argument object is null" );
        table = _table;
        if ( table == null )
            return false;
        // Kill all unreferenced entries first, so it will accurately
        // report an entry that was garbage collected with a null
        // return. Note that we just nullify the entry in the array,
        // we don't attempt to compact the array.
        processQueue();
        for ( int i = table.length ; i-- > 0 ; ) {
            ref = table[ i ];
            if ( ref != null && ref.get() == object )
                return true;
        }
        return false;
    }


    /**
     * Returns an array representing the contents of this list.
     * <p>
     * Only objects that are referenced will be returned in the
     * array, and these objects will be referenced by the array
     * and not discarded for as long as the returned array is
     * referenced.
     * <p>
     * This is the only way to access the list by index and operate
     * on it without safety checks.
     * <p>
     * The returned array is a sparse array, it may contain null
     * entries for objects that no longer exist.
     *
     * @return An array representing the contents of the list
     */
    public Object[] list()
    {
        Reference   ref;
        Reference[] table;
        Object[]    list;
        Object      object;
        int         index;
        int         length;
        
        table = _table;
        if ( table == null )
            return new Object[ 0 ];
        length = table.length;
        list = new Object[ length ];
        index = 0;
        for ( int i = length ; i-- > 0 ; ) {
            ref = table[ i ];
            if ( ref != null ) {
                object = ref.get();
                if ( object != null ) {
                    list[ index] = object;
                    ++index;
                }
            }
        }
        // The list might not be the same size of the array if we
        // lost entries recently, so update the returned array to
        // reflect the true size.
        if ( index == 0 )
            return new Object[ 0 ];
        else
            return list;
    }


    /**
     * Returns an array representing the contents of this list.
     * <p>
     * Only objects that are referenced will be returned in the
     * array, and these objects will be referenced by the array
     * and not discarded for as long as the returned array is
     * referenced.
     * <p>
     * This is the only way to access the list by index and operate
     * on it without safety checks.
     * <p>
     * The returned array is a sparse array, it may contain null
     * entries for objects that no longer exist.
     *
     * @param type The object type requested
     * @return An array representing the contents of the list
     */
    public Object[] list( Class type )
    {
        Reference   ref;
        Object[]    list;
        Reference[] table;
        Object      object;
        int         index;
        int         length;

        if ( type == null )
            throw new IllegalArgumentException( "Argument type is null" );
        table = _table;
        if ( table == null )
            return (Object[]) Array.newInstance( type, 0 );
        length = table.length;
        list = (Object[]) Array.newInstance( type, length );
        index = 0;
        for ( int i = length ; i-- > 0 ; ) {
            ref = table[ i ];
            if ( ref != null ) {
                object = ref.get();
                if ( object != null && type.isAssignableFrom( object.getClass() ) ) {
                    list[ index] = object;
                    ++index;
                }
            }
        }
        // The list might not be the same size of the array if we
        // lost entries recently, so update the returned array to
        // reflect the true size.
        if ( index == 0 )
            return (Object[]) Array.newInstance( type, 0 );
        else
            return list;
    }


    /**
     * Used internally to discard entries in the array which hold
     * references to garbage collected objects.
     */
    private void processQueue()
    {
        Reference[] table;
        Reference   ref;
        
        ref = _queue.poll();
        table = _table;
        while ( ref != null ) {
            for ( int i = table.length ; i-- > 0 ; )
                if ( table[ i ] == ref ) {
                    table[ i ] = null;
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
            WeakList wl = new WeakList();
            
            System.out.println( "First try: one and two" );
            wl.add( one );
            wl.add( two );
            list = (Integer[]) wl.list( Integer.class );
            for ( int i = 0 ; i < list.length ; ++i ) {
                System.out.println( list[ i ] );
                list[ i ] = null;
            }
            list = null;
            two = null;
            Runtime.getRuntime().gc();
            System.out.println( "Second try: one only" );
            list = (Integer[]) wl.list( Integer.class );
            for ( int i = 0 ; i < list.length ; ++i ) {
                System.out.println( list[ i ] );
                list[ i ] = null;
            }
            
            list = null;
            one = null;
            Runtime.getRuntime().gc();
            System.out.println( "Third try: empty" );
            list = (Integer[]) wl.list( Integer.class );
            for ( int i = 0 ; i < list.length ; ++i )
                System.out.println( list[ i ] );
        } catch ( Exception except ) {
            System.out.println( except );
            except.printStackTrace();
        }
    }


}

