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
 */


package tyrex.resource;


import tyrex.tm.TransactionDomain;


/**
 * Base class for a resource configuration. Different resource
 * implementations extend this class with additional methods
 * for configuring and creating a resource.
 * 
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.4 $
 */
public abstract class ResourceConfig
{


    /**
     * The resource name.
     */
    protected String               _name;


    /**
     * The JAR file name.
     */
    protected String               _jar;


    /**
     * Additional class paths for dependent files.
     */
    protected String               _paths;


    /**
     * The connection pool limits.
     */
    protected PoolLimits           _limits;


    /**
     * True if two-phase commit is supported.
     */
    protected boolean              _twoPhase = true;


    /**
     * The configured resource manager factory.
     */
    protected Object               _factory;


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
    public void setLimits( PoolLimits limits )
    {
        _limits = limits;
    }


    /**
     * Returns the connection pool limits.
     *
     * @return The connection pool limits
     */
    public PoolLimits getLimits()
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


    /**
     * Called to set the factory object after it has been configured.
     *
     * @param factory The factory object
     */
    public void setFactory( Object factory )
    {
        _factory = factory;
    }


    /**
     * Called to return the factory object.
     *
     * @return The factory object
     */
    public Object getFactory()
    {
        return _factory;
    }


    /**
     * Called to create a new factory object for the purpose of
     * configuring it. This method will return a factory object that
     * will be configured from the resource configuration file, before
     * being added to this object with a subsequent call to {@link
     * #setFactory setFactory}.
     *
     * @return The factory object (never null)
     * @throws ResourceException An error occured while attempting
     * to create a new factory
     */
    public abstract Object createFactory()
        throws ResourceException;


    /**
     * Called to create a new resource from this resource configuration.
     *
     * @param txDomain The transaction domain in which the resource will
     * be used
     * @return The resource
     * @throws ResourceException An error occured while attempting to
     * create the resource
     */
    public abstract Resource createResource( TransactionDomain txDomain )
        throws ResourceException;


}
