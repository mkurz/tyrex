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
 *    permission of Intalio Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio Technologies. Exolab is a registered
 *    trademark of Intalio Technologies.
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
 * Copyright 2001 (C) Intalio Technologies Inc. All Rights Reserved.
 *
 */


package tyrex.resource;

/////////////////////////////////////////////////////////////////////
// ReuseOptions
/////////////////////////////////////////////////////////////////////

/**
 * This class defines the reuse options.
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public final class ReuseOptions {

    /**
     * The reuse option that states the resources are to be
     * reused after they are currently used. The value is 0.
     */
    public static final int REUSE_ON                        = 0;

    /**
     * The reuse option that states the resources are not to be
     * reused. If the resource is enlisted in a transaction then
     * the resource is destroyed after the transaction commits
     * or rolls back and the resource is finished being used. 
     * If the resource is not enlisted in a transaction then the 
     * resource is destroyed after it is used.
     */
    public static final int REUSE_OFF                       = 1;

    /**
     * The reuse option that states the if resources are enlisted
     * in a transaction then resources can be reused only after
     * the transaction commits or rolls back. Otherwise if the
     * resources are not enlisted in a transaction then the resource
     * can be reused after it is used.
     */
    public static final int REUSE_TRANSACTION               = 2;

    /**
     * The reuse option that states if the resources are enlisted
     * in a transaction then resources are destroyed after
     * the transaction commits or rolls back. Otherwise if the
     * resources are not enlisted in a transaction then the resource
     * can be reused after it is used.
     */
    public static final int REUSE_TRANSACTION_OFF           = 3;

    /**
     * The reuse option that states if the resources are enlisted
     * in a transaction then resources are reused after
     * the transaction commits or rolls back. Otherwise if the
     * resources are not enlisted in a transaction then the resource
     * is destroyed after the resource is used.
     */
    public static final int REUSE_NO_TRANSACTION_OFF        = 4;

    /**
     * The name of the {@link REUSE_ON} option. The value is "on".
     */
    public static final String REUSE_ON_NAME                = "on";

    /**
     * The name of the {@link REUSE_OFF} option. The value is "off".
     */
    public static final String REUSE_OFF_NAME               = "off";

    /**
     * The name of the {@link REUSE_TRANSACTION} option. The value 
     * is "transaction".
     */
    public static final String REUSE_TRANSACTION_NAME       = "transaction";

    /**
     * The name of the {@link REUSE_TRANSACTION_OFF} option. The 
     * value is "transaction_off".
     */
    public static final String REUSE_TRANSACTION_OFF_NAME   = "transaction_off";

    /**
     * The name of the {@link REUSE_NO_TRANSACTION_OFF} option. The 
     * value is "no_transaction_off".
     */
    public static final String REUSE_NO_TRANSACTION_OFF_NAME   = "no_transaction_off";

    /**
     * No instances
     */
    private ReuseOptions() {
    }

    /**
     * Convert the specified string to the reuse option.
     *
     * @param string. One of {@link #REUSE_ON_NAME}, 
     *      {@link #REUSE_OFF_NAME}, {@link #REUSE_TRANSACTION_NAME}
     *      or {@link #REUSE_TRANSACTION_OFF_NAME}.
     * @return One of {@link #REUSE_ON}, {@link #REUSE_OFF},
     *      {@link #REUSE_TRANSACTION} or 
     *      {@link #REUSE_TRANSACTION_OFF}.
     * @throws IllegalArgumentException if the string is not 
     *      recognised or if it's null.
     */
    public static int fromString(String string) {
        if (null == string) {
            throw new IllegalArgumentException("The argument 'string' is null.");
        }
        if (string.equals(REUSE_ON_NAME)) {
            return REUSE_ON;    
        }
        if (string.equals(REUSE_OFF_NAME)) {
            return REUSE_OFF;    
        }
        if (string.equals(REUSE_TRANSACTION_NAME)) {
            return REUSE_TRANSACTION;    
        }
        if (string.equals(REUSE_TRANSACTION_OFF_NAME)) {
            return REUSE_TRANSACTION_OFF;    
        }
        if (string.equals(REUSE_NO_TRANSACTION_OFF_NAME)) {
            return REUSE_NO_TRANSACTION_OFF;    
        }
        throw new IllegalArgumentException("Unknown reuse option: " + string);
    }

    /**
     * Convert the specified option to the reuse option name.
     *
     * @param option. One of {@link #REUSE_ON}, 
     *      {@link #REUSE_OFF}, {@link #REUSE_TRANSACTION}
     *      or {@link #REUSE_TRANSACTION_OFF}.
     * @return One of {@link #REUSE_ON_NAME}, {@link #REUSE_OFF_NAME},
     *      {@link #REUSE_TRANSACTION_NAME} or 
     *      {@link #REUSE_TRANSACTION_OFF_NAME}.
     * @throws IllegalArgumentException if the option is not 
     *      recognised.
     */
    public static String toString(int option) {
        switch (option) {
            case REUSE_ON:                  return REUSE_ON_NAME;
            case REUSE_OFF:                 return REUSE_OFF_NAME;
            case REUSE_TRANSACTION:         return REUSE_TRANSACTION_NAME;
            case REUSE_NO_TRANSACTION_OFF:  return REUSE_NO_TRANSACTION_OFF_NAME;
            default: throw new IllegalArgumentException("Unknown reuse option: " + option);
        }
    }

    /**
     * Validate that the option is recognized
     *
     * @param option. One of {@link #REUSE_ON}, 
     *      {@link #REUSE_OFF}, {@link #REUSE_TRANSACTION}
     *      or {@link #REUSE_TRANSACTION_OFF}.
     * @throws IllegalArgumentException if the option is not 
     *      recognised.
     */
    public static void validate(int option) {
        if (!isValid(option)) {
            throw new IllegalArgumentException("Unknown reuse option: " + option);
        }
    }

    /**
     * Return true if the reuse option is valid. Return false
     * if the reuse option is invalid.
     *
     * @param option the option
     * @return true if the reuse option is valid. Return false
     *      if the reuse option is invalid.
     */
    public static boolean isValid(int option) {
        return  (REUSE_ON == option) || 
                (REUSE_OFF == option) ||
                (REUSE_TRANSACTION == option) || 
                (REUSE_NO_TRANSACTION_OFF == option);
    }

}
