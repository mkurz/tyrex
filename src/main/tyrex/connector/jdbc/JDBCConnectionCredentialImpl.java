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
 * $Id: JDBCConnectionCredentialImpl.java,v 1.2 2000/09/08 23:04:44 mohammed Exp $
 */


package tyrex.connector.jdbc;


/**
 * Credential used by a {@link tyrex.security.auth.Subject} 
 * to login into JDBC data source.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.2 $ $Date: 2000/09/08 23:04:44 $
 */
public final class JDBCConnectionCredentialImpl
    implements JDBCConnectionCredential
{
    /**
     * The name of the data source. Can be null.
     */
    private final String dataSourceName;

    /**
     * The user name. Can be null.
     */
    private final String  userName;


    /**
     * The password. Can be null.
     */
    private final String  password;


    /**
     * The hash code of the credential
     */
    private final int hashCode;


    /**
     * Construct new properties with the specified data source name, 
     * user name and password.
     *
     * @param dataSourceName the name of the data source. Can be null.
     * @param userName the name of the user. Can be null.
     * @param password the password. Can be null.
     */
    public JDBCConnectionCredentialImpl(String dataSourceName, String userName, String password)
    {
        this.dataSourceName = dataSourceName;
        this.userName       = userName;
        this.password       = password;
        this.hashCode       = computeHashCode();

    }

    
    /**
     * Return the name of data source to be
     * accessed using this credential. Can be null.
     *
     * @return the name of data source to be
     *      accessed using this credential.
     */
    public String getDataSourceName()
    {
        return dataSourceName;
    }

    
    /**
     * Return the user name used to access the
     * data source. Can be null.
     *
     * @return the user name used to access the
     * data source.
     */
    public String getUserName()
    {
        return userName;
    }


    /**
     * Return the password used to access the data
     * source. Can be null.
     *
     * @return the password used to access the data
     * source.
     */
    public String getPassword()
    {
        return password;
    }


    /**
     * Return true if this specified object is equal to this
     * credential.
     *
     * @return true if this specified object is equal to this
     * credential.
     */
    public boolean equals(Object other)
    {
        if (other == this)
            return true;
        return ( other instanceof JDBCConnectionCredential &&
                 stringEquals(((JDBCConnectionCredential)other).getDataSourceName(), dataSourceName) &&
                 stringEquals(((JDBCConnectionCredential)other).getUserName(), userName) &&
                 stringEquals(((JDBCConnectionCredential)other).getPassword(), password));
    }


    /**
     * Return true if the specified items are equal. It treats
     * null and the empty string as the same.
     *
     * @return true if the specified items are equal.
     */
    private boolean stringEquals(String obj1, String obj2)
    {
        if ((null == obj1) || (0 == obj1.length())) {
            return (null == obj2) || (0 == obj2.length());
        }
        if ((null != obj2) && 
            (obj1.length() == obj2.length())) {
            return obj1.equals(obj2);
        }
        return false;
    }


    /**
     * Return the hash code of this credential.
     *
     * @return the hash code of this credential.
     */
    public int hashCode()
    {
        return hashCode;
    }


    /**
     * Compute the hash code from the combination of
     * data source name, user name and password in
     * the instance. This method is only called once.
     * This method does not distinguishe between null and
     " the empty string.
     *
     * @return the computed hash code
     */
    private int computeHashCode()
    {
        // treat the data source name, user name and password
        // as one big string and use the hash function for
        // string - username separator password separator data 
        // source name
        // s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]

        // the character to add as the separator
        char separator = '#';
        // the running hash code
        int hashCode = 0;
        // the exponent of 31 to add to each char
        int exp = 1;

        // loop over the data source name in reverse
        if (null != dataSourceName) {
            for (int i = dataSourceName.length(); --i >= 0;) {
                hashCode += dataSourceName.charAt(i) * exp;
                exp *= 31;
            }
        }
        // add the separator
        hashCode += separator * exp;
        exp *= 31;

        // loop over the password in reverse
        if (null != password) {
            for (int i = password.length(); --i >= 0;) {
                hashCode += password.charAt(i) * exp;
                exp *= 31;
            }
        }
        // add the separator
        hashCode += separator * exp;
        exp *= 31;

        // loop over the password in reverse
        if (null !=userName) {
            for (int i = userName.length(); --i >= 0;) {
                hashCode += userName.charAt(i) * exp;
                exp *= 31;
            }
        }
        // return the hash code
        return hashCode;
    }

    /*
    public static void main (String args[]) {
        System.out.println("hash " + new JDBCConnectionCredentialImpl("abc", null, null).hashCode());
        System.out.println("hash " + new JDBCConnectionCredentialImpl("ab", "c", null).hashCode());
        System.out.println("hash " + new JDBCConnectionCredentialImpl("a", "bc", null).hashCode());
        System.out.println("hash " + new JDBCConnectionCredentialImpl("ab", null, "c").hashCode());
        System.out.println("hash " + new JDBCConnectionCredentialImpl("a", "b", "c").hashCode());
    }*/
}

