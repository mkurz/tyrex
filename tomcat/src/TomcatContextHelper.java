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
 * Copyright 2000 (C) Intalio Technologies Inc. All Rights Reserved.
 *
 * $Id: TomcatContextHelper.java,v 1.1 2000/09/25 21:15:30 mohammed Exp $
 */


import java.util.Hashtable;
import java.util.Enumeration;
import java.io.IOException;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.CompositeName;
import javax.naming.OperationNotSupportedException;
import javax.naming.InvalidNameException;
import javax.naming.NameAlreadyBoundException;
import javax.transaction.UserTransaction;
import javax.sql.DataSource;
import tyrex.conf.Resources;
import tyrex.conf.Resource;
import tyrex.conf.EnvEntry;
import tyrex.naming.MemoryContext;
import tyrex.util.Logger;
import tyrex.util.Messages;


/**
 *
 *
 * @author <a href="arkin@exoffice.com">Assaf Arkin</a>
 * @version $Revision: 1.1 $ $Date: 2000/09/25 21:15:30 $
 */
public final class TomcatContextHelper
{
    /**
     * No instances
     */
    private TomcatContextHelper()
    {
    }


    /**
     * Create the memory context for use with Tomcat
     *
     * @return the new memory context for use with Tomcat
     */
    static MemoryContext createMemoryContext()
    {
	try {
	    MemoryContext ctx = new MemoryContext( null );
	    ctx.createSubcontext( "comp" );
	    ctx.createSubcontext( "comp/env" );
        return ctx;
	} catch ( NamingException except ) {
	    // This should never happen.
	    throw new RuntimeException( "Internal error: " + except.toString() );
	}
    }


    static void addEnvEntry( Context ctx, String name, String type, String value )
	throws NamingException
    {
	Context sub_ctx;
	Name    comp;
	int     i;
	Object  obj;

	comp = new CompositeName( name );
	while ( ! comp.isEmpty() && comp.get( 0 ).length() == 0 )
	    comp = comp.getSuffix( 1 );
	if ( comp.isEmpty() )
	    throw new InvalidNameException( name + " is empty" );
	sub_ctx = ctx.createSubcontext( "comp/env" );
	for ( i = 0 ; i < comp.size() - 1 ; ++i ) {
	    sub_ctx = sub_ctx.createSubcontext( comp.get( i ) );
	}
	if ( type.equals( Integer.class.getName() ) ) {
	    obj = new Integer( value );
	} else if ( type.equals( Short.class.getName() ) ) {
	    obj = new Short( value );
	} else if ( type.equals( Byte.class.getName() ) ) {
	    obj = new Byte( value );
	} else if ( type.equals( Float.class.getName() ) ) {
	    obj = new Float( value );
	} else if ( type.equals( Double.class.getName() ) ) {
	    obj = new Double( value );
	} else if ( type.equals( Boolean.class.getName() ) ) {
	    obj = new Boolean( value );
	} else if ( type.equals( String.class.getName() ) ) {
	    obj = value;
	} else
	    throw new OperationNotSupportedException( "Type is not a supported primitive data type" );
	sub_ctx.rebind( comp.get( i ), obj );
    }


    static void addEnvEntries( Context ctx, String appName )
    {
	Enumeration enum;
	EnvEntry    entry;

	enum = Resources.getResources().listEnvEntries();
	while ( enum.hasMoreElements() ) {
	    entry = (EnvEntry) enum.nextElement();
	    if ( entry.isVisible( appName ) ) {
		try {
		    addEnvEntry( ctx, entry.getEnvEntryName(), entry.getEnvEntryType(),
				 entry.getEnvEntryValue() );
		} catch ( NamingException except ) {
		    Logger.getSystemLogger().println( except );
		}
	    }
	}
    }
    
    
    static void addUserTransaction( Context ctx, UserTransaction ut )
	throws NamingException
    {
	if ( ut == null ) {
	    ctx.unbind( "comp/UserTransaction" );
	} else {
	    ctx.rebind( "comp/UserTransaction", ut );
	}
    }
    
    
    static void addResources( Context ctx, String appName )
    {
	Enumeration enum;
	Resource    resource;
	Object      factory;
	
	enum = Resources.getResources().listResources();
	while ( enum.hasMoreElements() ) {
	    resource = (Resource) enum.nextElement();
	    
        if ( resource.isVisible( appName ) ) {
		    
		    factory = resource.createResourceFactory();
            if ( factory instanceof DataSource ) {
			    try {
				addDataSource( ctx, resource.getResName(),
					       (DataSource) factory );
			    } catch ( NameAlreadyBoundException except ) {
				// This happens if another data source was
				// previously bound to the same name. Ignore.
			    } catch ( NamingException except ) {
				// This typically happens if name is invalid
				Logger.getSystemLogger().println(
				    Messages.format( "tyrex.enc.errorBindingResource",
						     resource.getResName(), except ) );
			    }
			}
        }
	    
	}
    }


    static void addResource( Context ctx, String appName, String resName, String resType,
                             boolean appAuth )
    {
	Enumeration enum;
	Resource    resource;
	Object      factory;
	
	enum = Resources.getResources().listResources();
	while ( enum.hasMoreElements() ) {
	    resource = (Resource) enum.nextElement();
	    
	    if ( resource.getResName().equals( resName ) &&
		 resource.isApplicationAuth() == appAuth ) {
		if ( resource.isVisible( appName ) ) {
		    
		    if ( resType.equals( "javax.sql.DataSource" ) ) {

			factory = resource.createResourceFactory();
			if ( factory == null || ! ( factory instanceof DataSource ) ) {
			    Logger.getSystemLogger().println(
			        Messages.format( "tyrex.enc.resourceNotSameType",
						 resType, resource.getResType() ) );
			} else {
			    try {
				addDataSource( ctx, resource.getResName(),
					       (DataSource) factory );
			    } catch ( NameAlreadyBoundException except ) {
				// This happens if another data source was
				// previously bound to the same name. Ignore.
			    } catch ( NamingException except ) {
				// This typically happens if name is invalid
				Logger.getSystemLogger().println(
				    Messages.format( "tyrex.enc.errorBindingResource",
						     resource.getResName(), except ) );
			    }
			}

		    } else {
			Logger.getSystemLogger().println( 
			    Messages.format( "tyrex.enc.resourceNotSupported",
		            resource.getResName(), resource.getResType() ) );
		    }
		    
		}
	    }
	}
    }
    
    static void addDataSource( Context ctx, String name, DataSource ds )
	throws NamingException
    {
	Context sub_ctx;
	Name    comp;
	int     i;
	Object  obj;
    comp = new CompositeName( name );
	while ( ! comp.isEmpty() && comp.get( 0 ).length() == 0 )
	    comp = comp.getSuffix( 1 );
	if ( comp.isEmpty() )
	    throw new InvalidNameException( name + " is empty" );
	sub_ctx = ctx.createSubcontext( "comp/env" );
	for ( i = 0 ; i < comp.size() - 1 ; ++i ) {
	    sub_ctx = sub_ctx.createSubcontext( comp.get( i ) );
	}
	if ( ds == null ) {
	    sub_ctx.unbind( comp.get( i ) );
	} else {
	    // This will throw an exception if a data source already
	    // bound with the same name. First come basis.
	    sub_ctx.bind( comp.get( i ), ds );
	}
    }


}
