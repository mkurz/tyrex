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
 */


package tyrex.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

///////////////////////////////////////////////////////////////////////////////
// ArrayEnumeration
///////////////////////////////////////////////////////////////////////////////

/**
 * This class provides a way to enumerate over an array.
 * The array is not cloned.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public final class ArrayEnumeration implements Enumeration
{
    /**
     * The array to enumerate over
     */
    private final Object[] array;


    /**
     * The end index of the array exclusive
     */
    private final int end;


    /**
     * The current index to return
     */
    private int index;
    

    /**
     * Create the ArrayEnumeration with the specified array
     *
     * @param array the array to enumerate over
     */
    public ArrayEnumeration(Object[] array)
    {
        this(array, 0, array.length);
    }

    
    /**
     * Create the ArrayEnumeration with the specified arguments.
     *
     * @param array the array to enumerate over
     * @param start the start index inclusive
     * @param end the end index exclusive
     */
    public ArrayEnumeration(Object[] array, int start, int end)
    {
        if (null == array) {
            throw new IllegalArgumentException("The argument 'array' is null.");
        }
        if ((start < 0)     || 
            (start > end)   || 
            (start >= array.length)) {
            throw new IllegalArgumentException("The argument 'start' " + start + " is invalid.");    
        }
        if (end > array.length) {
            throw new IllegalArgumentException("The argument 'end' " + end + " is invalid.");    
        }
        this.array = array;
        this.index = start;
        this.end = end;
    }

    /**
     * Return true if the enumeration has more elements to return.
     *
     * @return true if the enumeration has more elements to return.
     */
    public boolean hasMoreElements()
    {
        return (index < end);
    }


    /**
     * Return the next element in the enumeration.
     *
     * @return the next element in the enumeration.
     * @throws NoSuchElementException if there are no more
     *      elements.
     */
    public Object nextElement()
    {
        if (!hasMoreElements()) {
            throw new NoSuchElementException("No more elements in the enumeration.");    
        }

        return array[index++];
    }


    /*
    public static void main (String args[]) {
        for (Enumeration e = new ArrayEnumeration(args, 0, args.length - 1); e.hasMoreElements();) {
            System.out.println(e.nextElement());
        }
    }*/
}
