/**
 * Copyright (C) 2000, Intalio Inc.
 *
 * The program(s) herein may be used and/or copied only with the
 * written permission of Intalio Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */


package tyrex.tm.jca.dd;


import java.util.Vector;


/**
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version $Revision: 1.1 $
 */
public class DDResourceAdapter
{


    public final static String NO_TRANSACTION    = "no_transaction";


    public final static String LOCAL_TRANSACTION = "local_transaction";


    public final static String XA_TRANSACTION    = "xa_transaction";


    public final static String TRUE              = "true";


    public final static String FALSE             = "false";


    private String        _managedFactoryClass;


    private String        _factoryInterface;


    private String        _factoryImplClass;


    private String        _connectionInterface;


    private String        _connectionImpl;


    private String        _transactionSupport;


    private Vector        _configProperties;


    private Vector        _authMechanism;


    private String        _reauthenticationSupport;


    private Vector        _permission;


    /**
     * Specifies the fully qualified name of the Java class that implements
     * the <tt>javax.resource.spi.ManagedConnectionFactory</tt> interface.
     *
     * @return The name of the managed connection factory class
     */
    public String getManagedconnectionfactoryClass()
    {
        return _managedFactoryClass;
    }


    public void setManagedconnectionfactoryClass( String className )
    {
        _managedFactoryClass = className;
    }


    /**
     * Specifies the fully qualified name of the connection factory interface
     * supported by this resource adapter.
     *
     * @return The name of the connection factory interface
     */
    public String getConnectionfactoryInterface()
    {
        return _factoryInterface;
    }


    public void setConnectionfactoryInterface( String interfaceName )
    {
        _factoryInterface = interfaceName;
    }


    /**
     * Specifies the fully qualified name of the connection factory class that
     * implements the resource adapter specific connection factory interface.
     *
     * @return The name of the connection factory class
     */
    public String getConnectionfactoryImplClass()
    {
        return _factoryImplClass;
    }


    public void setConnectionfactoryImplClass( String className )
    {
        _factoryImplClass = className;
    }


    /**
     * Specifies the fully qualified name of the connection interface supported
     * by this resource adapter.
     *
     * @return The name of the connection interface
     */
    public String getConnectionInterface()
    {
        return _connectionInterface;
    }


    public void setConnectionInterface( String interfaceName )
    {
        _connectionInterface = interfaceName;
    }


    /**
     * Specifies the fully qualified name of the connection class that implements
     * the resource adapter specific connection interface.
     *
     * @return The name of the connection class
     */
    public String getConnectionImplClass()
    {
        return _connectionImpl;
    }


    public void setConnectionImplClass( String className )
    {
        _connectionImpl = className;
    }


    /**
     * The level of transaction support provided by the resource adapter.
     * Valid values are {@link #NO_TRANSACTION}, {@link LOCAL_TRANSACTION}
     * or {@link #XA_TRANSACTION}.
     *
     * @return Level of transaction support
     */
    public String getTransactionSupport()
    {
        return _transactionSupport;
    }


    public void setTransactionSupport( String transaction )
    {
        _transactionSupport = transaction;
    }


    public Vector getConfigProperty()
    {
        return _configProperties;
    }


    public void setConfigProperty( Vector vector )
    {
        _configProperties = vector;
    }


    /**
     * Specifies an authentication mechanism supported by the resource adapter.
     *
     * @return An authentication mechanism
     */
    public Vector getAuthMechanism()
    {
        return _authMechanism;
    }


    public void setAuthMechanism( Vector authMechanism )
    {
        _authMechanism = authMechanism;
    }


    /**
     * Specifies whether the resource adapter implementation supports
     * re-authentication of existing managed connection instance. Valid values
     * are {@link #TRUE} and {@link #FALSE}.
     *
     * @return Support for re-authentication of existing managed connection
     */
    public String getReauthenticationSupport()
    {
        return _reauthenticationSupport;
    }


    public void setReauthenticationSupport( String reauthentication )
    {
        _reauthenticationSupport = reauthentication;
    }


    /**
     * Specifies a security permission that is required by the resource adapter code.
     *
     * @return A  security permission
     */
    public Vector getSecurityPermission()
    {
        return _permission;
    }


    public void setSecurityPermission( Vector permission )
    {
        _permission = permission;
    }


}
