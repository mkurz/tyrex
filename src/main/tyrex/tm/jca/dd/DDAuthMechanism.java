/**
 * Copyright (C) 2000, Intalio Inc.
 *
 * The program(s) herein may be used and/or copied only with the
 * written permission of Intalio Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */


package tyrex.tm.jca.dd;


/**
 * @author <a href="jdaniel@intalio.com">Jerome Daniel</a>
 * @version $Revision: 1.1 $
 */
public class DDAuthMechanism
{


    private String _description;


    private String _mechType;


    private String _credentialInterface;


    public String getDescription()
    {
        return _description;
    }


    public void setDescription( String description )
    {
        _description = description;
    }


    /**
     * Specifies the type of the authentication mechanism.
     *
     * @return The type of the authentication mechanism
     */
    public String getAuthMechType()
    {
        return _mechType;
    }


    public void setAuthMechType( String mechType )
    {
        _mechType = mechType;
    }


    /**
     * Specifies the interface that the resource adapter implementation supports
     * for the representation of the security credentials.
     *
     * @return The interface that the resource adapter implementation supports
     */
    public String getCredentialInterface()
    {
        return _credentialInterface;
    }


    public void setCredentialInterface( String credentialInterface )
    {
        _credentialInterface = credentialInterface;
    }


}
