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


package tyrex.resource;


import java.io.Serializable;
import tyrex.tm.TransactionDomain;


/**
 * 
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version $Revision: 1.2 $
 */
public abstract class BaseConfiguration
    implements Serializable
{


    /**
     * The resource name.
     */
    private String               _name;


    /**
     * The JAR file name.
     */
    private String               _jar;


    /**
     * Additional class paths for dependent files.
     */
    private String               _paths;


    /**
     * The connection pool resource limits.
     */
    private ResourceLimits       _limits;


    /**
     * True if two-phase commit is supported.
     */
    private boolean              _twoPhase = true;


    /**
     * The configured resource manager factory.
     */
    private Object               _factory;


    /**
     * The transaction domain for which this configuration is loaded.
     */
    private TransactionDomain    _txDomain;


    public BaseConfiguration()
    {
    }


    /**
     * Sets the name for this resource manager. The name is used for logging
     * and by visual tools. It must be short and unique.
     *
     * @param name The resource manager name
     */
    public void setName( String name )
    {
        _name = name;
    }


    /**
     * Returns the name for this resource manager.
     *
     * @return The resource manager name
     */
    public String getName()
    {
        return _name;
    }


    /**
     * Sets the JAR file name. The JAR file name must accessible
     * relative to the current directory.
     *
     * @param jar The JAR file name
     */
    public void setJAR( String jar )
    {
        _jar = jar;
    }


    /**
     * Returns the JAR file name.
     *
     * @return The JAR file name
     */
    public String getJAR()
    {
        return _jar;
    }


    /**
     * Sets additional path names. This is a colon separated list of paths
     * that point to directories and JARs containing dependent files and
     * resources used by the resource manager.
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
     * is used to configure the connection pool,
     *
     * @param limits The connection pool limits
     */
    public void setLimits( ResourceLimits limits )
    {
        _limits = limits;
    }


    /**
     * Returns the connection pool limits.
     *
     * @return The connection pool limits
     */
    public ResourceLimits getLimits()
    {
        return _limits;
    }


    /**
     * Sets the two-phase commit support flag. If this value is true,
     * connections support two-phase commit.
     * <p>
     * This flag is valid only if connections support the XA interface for
     * distributed transaction demarcation. The default is always true.
     *
     * @param twoPhase True if connections support two-phase commit
     */
    public void setTwoPhase( boolean twoPhase )
    {
        _twoPhase = twoPhase;
    }


    /**
     * Returns the two-phase commit support flag. If this value is true,
     * connections support two-phase commit.
     *
     * @return True if connections support two-phase commit
     */
    public boolean getTwoPhase()
    {
        return _twoPhase;
    }


    public Object getFactory()
    {
        return _factory;
    }


    public void setFactory( Object factory )
    {
        _factory = factory;
    }


    public abstract Object createFactory()
        throws Exception;


    public TransactionDomain getTransactionDomain()
    {
        return _txDomain;
    }


    public void setTransactionDomain( TransactionDomain txDomain )
    {
        _txDomain = txDomain;
    }


}
