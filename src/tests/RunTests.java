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
 */

package tests;

//import java.security.AccessControlContext;
//import java.security.AccessController;                                
//import java.security.DomainCombiner;
//import java.security.Permission;
//import java.security.PermissionCollection;
//import java.security.PrivilegedAction;
//import java.security.ProtectionDomain;
//import java.util.Enumeration;
//import tyrex.naming.NamingPermission;
//import tyrex.tm.TyrexPermission;

/////////////////////////////////////////////////////////////////////
// RunTests
/////////////////////////////////////////////////////////////////////

/**
 * This class runs the {@link TestHarness} with the argument "-execute".
 * It also installs a security manager to ignore the following permissions:
 * {@link tyrex.tm.TyrexPermission ) and {@link tyrex.naming.NamingPermission}
 *
 * @author <a href="mohammed@intalio.com">Riad Mohammed</a>
 */
public final class RunTests 
{
    public static void main (final String args[]) 
    {
        // Set up the verbose mode
            for(int i=0;i<args.length;i++) if(args[i].equals("-verbose")) VerboseStream.verbose=true;
     
        TestHarness.main(args);
        /*
        // Code for running tests without changing policy file - only works
        // for jdk 1.3
        PermissionCollection permissionCollection = new PermissionCollection()
                                                        {
                                                            private final Enumeration enumeration = new Enumeration()
                                                                    {
                                                                        public boolean hasMoreElements() {
                                                                            return false;
                                                                        }
                                                                        public java.lang.Object nextElement() {
                                                                            return null;
                                                                        }
                                                                    };

                                                            
                                                            public void add(Permission permission) 
                                                            {
                                                            }
                                                            
                                                            public Enumeration elements() 
                                                            {
                                                                return enumeration;
                                                            }
                                                                       
                                                            public boolean implies(Permission permission) 
                                                            {
                                                                return true;
                                                            }
                                                        };

        final ProtectionDomain[] protectionDomains = new ProtectionDomain[]{new ProtectionDomain(null, permissionCollection)};

        AccessController.doPrivileged(new PrivilegedAction()
                                        {
                                            public Object run() 
                                            {
                                                TestHarness.main((null == args) || (0 == args.length)
                                                                    ? new String[]{"-execute"} 
                                                                    : args);
                                                return null;
                                            }
                                        },
                                        //new AccessControlContext(protectionDomains)
                                        new AccessControlContext(AccessController.getContext(), 
                                                                 new DomainCombiner()
                                                                 {
                                                                     public ProtectionDomain[] combine(ProtectionDomain[] currentDomains, 
                                                                                                ProtectionDomain[] assignedDomains)
                                                                     {
                                                                        return protectionDomains;
                                                                     }
                                                                 })
                                        );
        */
    }
}
