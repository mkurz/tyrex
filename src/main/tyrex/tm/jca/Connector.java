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


package tyrex.tm.jca;


/**
 * 
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version $Revision: 1.1 $
 */
public class Connector
    implements java.io.Serializable
{


    /**
     * The connector name.
     */
    protected String               _name;


    /**
     * The RAR (Resource Adapter Archive) file name.
     */
    protected String               _rar;


    /**
     * Additional class paths for dependent files.
     */
    protected String               _paths;


    /**
     * The connection pool limits.
     */
    protected Limits               _limits;


    /**
     * Sets the name for this connector. The name is used for logging and
     * by visual tools. It must be short and unique amongst connectors.
     *
     * @param name The connector name
     */
    public void setName( String name )
    {
        _name = name;
    }


    /**
     * Returns the name for this connector.
     *
     * @return The connector name
     */
    public String getName()
    {
        return _name;
    }


    /**
     * Sets the RAR file name. The RAR (Resource Adapter Archive) contains
     * the connector's deployment descriptor and main classes. The RAR file
     * name must accessible relative to the current directory.
     *
     * @param rar The RAR file name
     */
    public void setRAR( String rar )
    {
        _rar = rar;
    }


    /**
     * Returns the RAR file name.
     *
     * @return The RAR file name
     */
    public String getRAR()
    {
        return _rar;
    }


    /**
     * Sets additional path names. This is a colon separated list of paths
     * that point to directories and JARs containing dependent files and
     * resources used by the connector.
     *
     * @param paths Additional path names
     */
    public void setPaths( String paths )
    {
        _paths = paths;
    }


    /**
     * Returns the additional path names.
     *
     * @return The additional path names
     */
    public String getPaths()
    {
        return _paths;
    }


    /**
     * Sets the connection pool limist. This is an optimal element that
     * is used to configure the connection pool, and specify the
     * transactional behavior.
     *
     * @param descriptor The connector descriptor
     */
    public void setLimits( Limits limits )
    {
        _limits = limits;
    }


    /**
     * Returns the connection pool limits.
     *
     * @return The connection pool limits
     */
    public Limits getLimits()
    {
        return _limits;
    }

    private Object _object;


    public Object getConfig()
    {
        System.out.println( "getConfig" );
        return _object;
    }

    public void setConfig( Object object )
    {
        System.out.println( "setConfig" );
        _object = object;
    }

    public Object createConfig()
    {
        System.out.println( "createConfig" );
        return new com.sybase.jdbc2.jdbc.SybDataSource();
    }




}
