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
 * $Id: JDBC.java,v 1.3 2000/09/08 23:03:11 mohammed Exp $
 */


package jdbc;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import org.exolab.jtf.CWBaseApplication;
import org.exolab.jtf.CWTestCategory;
import org.exolab.jtf.CWTestCase;
import org.exolab.jtf.CWVerboseStream;
import org.exolab.exceptions.CWClassConstructorException;
import tyrex.tm.Tyrex;
import tyrex.tm.TyrexTransaction;


public class JDBC
    extends CWTestCategory
{

    /**
     * The system property name that specifies the 
     * fully qualified {@link JDBCHelper} class names.
     * <P>
     * The class names are separated by ','
     *
     * @see JDBCHelper
     * @see #JDBC_HELPER_PROPERTY_DELIMITER
     */
    public final static String JDBC_HELPER_PROPERTY_NAME = "jdbchelper";


    /**
     * The delimiter for separating fully qualified 
     * {@link JDBCHelper} class names.
     *
     * @see JDBCHelper
     * @see #JDBC_HELPER_PROPERTY_NAME
     */
    public final static char JDBC_HELPER_PROPERTY_DELIMITER = ',';

    
    public JDBC()
        throws CWClassConstructorException
    {
        super( "jdbc", "JDBC Test");
        
        CWTestCase tc;
        String name;
        JDBCHelper[] helpers = getJDBCHelpers();
        JDBCHelper helper;
        int caseCount;

        if ( null == helpers ) {
            throw new CWClassConstructorException( "JDBC Helpers not found" );    
        }
        
        for (int i = 0; i < helpers.length; i++) {
            caseCount = 0;
            helper = helpers[ i ];
            name = helper.toString() + "-" + i + "-" + ++caseCount;
            tc = new SimpleTestCase( name, helper );
            add( tc.name(), tc, true );
            
            name = helper.toString() + "-" + i + "-" + ++caseCount;
            tc = new EnlistedResourceTestCase( name, helper );
            //add( tc.name(), tc, true );        
        }
    }
    

    /**
     * Return the array of fully qualified 
     * {@link JDBCHelper} class names listed in the 
     * system property {@link JDBC_HELPER_PROPERTY_NAME}.
     * <P>
     * If there are no class names return null.
     *
     * @return the array of fully qualified
     *      {@link JDBCHelper} class names listed in the
     *      system property {@link JDBC_HELPER_PROPERTY_NAME}
     * @see JDBCHelper
     * @see #JDBC_HELPER_PROPERTY_NAME
     * @see #JDBC_HELPER_PROPERTY_DELIMITER
     */
    protected String[] getJDBCHelperClassNames()
    {
        int previousIndex;
        int nextIndex;
        int length;
        String className;
        ArrayList classNames = new ArrayList();

        String helperClassNames = System.getProperty(JDBC_HELPER_PROPERTY_NAME);

        if ( null == helperClassNames ) {
            return null;    
        }

        nextIndex = 0;
        previousIndex = 0;
        length = helperClassNames.length();
        
        while ( ( nextIndex < length ) && 
                ( -1 != ( nextIndex = helperClassNames.indexOf( JDBC_HELPER_PROPERTY_DELIMITER, nextIndex ) ) ) ) {
            className = helperClassNames.substring( previousIndex, nextIndex ).trim();

            if ( 0 != className.length() ) {
                classNames.add( className );    
            }

            ++nextIndex;
            previousIndex = nextIndex;
        }

        if ( previousIndex < length) {
            // add the last one
            className = helperClassNames.substring( previousIndex, length ).trim();
    
            if ( 0 != className.length() ) {
                classNames.add( className );    
            }    
        }
        
        if ( classNames.isEmpty() ) {
            return null;    
        }

        return ( String[] )classNames.toArray( new String[ classNames.size() ] );
    }

    
    /**
     * Return the array of {@link JDBCHelper} instances created
     * from the fully qualified class names listed in the 
     * system property {@link JDBC_HELPER_PROPERTY_NAME}.
     *
     * @return the array of {@link JDBCHelper} instances 
     *      created from the fully qualified class names listed in the
     *      system property {@link JDBC_HELPER_PROPERTY_NAME}
     * @see JDBCHelper
     * @see #JDBC_HELPER_PROPERTY_NAME
     * @see #getJDBCHelperClassNames
     */
    protected JDBCHelper[] getJDBCHelpers()
        throws CWClassConstructorException
    {
        try {
            JDBCHelper[] helpers;
            // get the class names
            String[] helperClassNames = getJDBCHelperClassNames();

            if ( null == helperClassNames ) {
                return null;    
            }
            
            helpers = new JDBCHelper[ helperClassNames.length ];

            for ( int i = 0; i < helperClassNames.length; ++i) {
                helpers[ i ] = (JDBCHelper)Class.forName(helperClassNames[ i ]).newInstance();
            }

            return helpers;

        } catch ( Exception e ) {
            throw new CWClassConstructorException( e.toString() );
        }
    }


    /**
     * Return true if the current transaction can be committed
     * using 1PC.
     *
     * @param transactionManager the transaction manager
     * @return true if the current transaction can be committed
     *      using 1PC.
     */
    private static boolean canUseOnePhaseCommit( TransactionManager transactionManager )
        throws Exception
    {
        // get the current transaction
        Transaction transaction = transactionManager.getTransaction();

        return ( null == transaction ) 
                ? false 
                : ( ( TyrexTransaction ) transaction ).canUseOnePhaseCommit();
    }
    

    
    public static void main (String args[]) {

        class JDBCTest extends CWBaseApplication
        {
            private final Vector categories;

            public JDBCTest(String s)
                throws CWClassConstructorException
            {
                super(s);

                categories = new Vector();
                categories.addElement("jdbc.JDBC");
            }
        
            protected String getApplicationName()
            {
                return "JDBCTest";
            }
        
            protected Enumeration getCategoryClassNames()
            {
                return categories.elements();
            }
        }

        try
        {
            JDBCTest test = new JDBCTest("jdbc test");
            test.run(args);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }
    
}




