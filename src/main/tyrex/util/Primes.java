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
 * Copyright 2000, 2001 (C) Intalio Inc. All Rights Reserved.
 *
 */


package tyrex.util;


/**
 * This class provides a single service. It returns the next prime number
 * that is larger than the required value. The prime table ranges between
 * 11 and 2893249.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2001/02/28 18:28:54 $
 */
public final class Primes
{


    private static final int[] _primes = new int[] {
        11, 13, 29, 37, 47, 59, 71, 89, 107, 131, 163, 197, 239, 293,
        353, 431, 521, 631, 761, 919, 1103, 1327, 1597, 1931, 2333, 2801,
        3371, 4049, 4861, 5839, 7013, 8419, 10103, 12143, 14591, 17519,
        21023, 25229, 30293, 36353, 43627, 52361, 62851, 75431, 90523,
        108631, 130363, 156437, 187751, 225307, 270371, 324449, 389357,
        467237, 560689, 672827, 807403, 968897, 1162687, 1395263, 1674319,
        2009191, 2411033, 2893249
    };


    /**
     * Returns the lowest prime number that is higher than the specified
     * value.
     *
     * @param value The specified value
     * @return The lowest prime number higher than the value
     */
    public static int nextPrime( int value )
    {
        for ( int i = 0 ; i < _primes.length ; ++i )
            if ( value <= _primes[ i ] )
                return _primes[ i ];
        return _primes[ _primes.length - 1 ];
    }
    
    
}
