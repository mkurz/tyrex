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
 */
package ots;

import junit.framework.*;


/**
 * This test case is inherited by all OTS test cases. It provides a simple way to directly have 
 * an ORB instance.
 */
public abstract class OTSTestCase extends TestCase
{
    protected org.omg.CORBA.ORB _client_orb;
    
    protected org.omg.CORBA.ORB _server_orb;
    
    private Thread _thread;
    
    public OTSTestCase( String test_name )
    {
        super( test_name );
    }
    
    public void setUp()
    {
        _client_orb = org.omg.CORBA.ORB.init( new String[] { "-ORBProfile=tyrex" }, null ); 
        
        _server_orb = org.omg.CORBA.ORB.init( new String[] { "-ORBProfile=tyrex" }, null );

        _thread = new Thread(   new Runnable() 
                                {
                                  public void run()
                                  {
                                     _server_orb.run();
                                  }
                                }     );
                             
        _thread.setDaemon(true);
        _thread.start();
    }
        
    public void tearDown()
    {
        try
        {
        _client_orb.shutdown( true );
        _server_orb.shutdown( true );
        
        try 
        {
            _thread.join(20000);
        }
        catch(InterruptedException ex) {}
    
        if( _thread.isAlive() )
            System.err.println("ERR : Unable to stop server orb");
        }
        catch ( java.lang.Exception ex )
        {
        }
    }
    
    public org.omg.CORBA.Object forceMarshal(org.omg.CORBA.Object obj)
    {
        return _client_orb.string_to_object(_server_orb.object_to_string(obj) );
    }
    
    public org.omg.CORBA.Object getObject( org.omg.PortableServer.Servant srv )
    {
        try
        {
            org.omg.PortableServer.POA rootPOA = (org.omg.PortableServer.POA)_server_orb.resolve_initial_references("RootPOA");

            org.omg.CORBA.Object obj = srv._this_object( _server_orb );

            rootPOA.the_POAManager().activate();

            return forceMarshal(obj);
        }
        catch ( Exception ex )
        {
            System.out.println("[ERR : Reference Exchnange ] " + ex.toString());
            return null;
        }
    }
}
