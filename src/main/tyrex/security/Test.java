package tyrex.security;


import java.util.Set;
import java.util.Iterator;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;


public class Test
{


    public static void main( String[] args )
    {
        try {
            
            LoginContext ctx;
            Subject      subject;
            String       user = args[0];
            String       password = args[1];
            
            subject = new Subject();
            subject.getPrivateCredentials().add( new NamePasswordCredentials( user, password.toCharArray(), null ) );
            ctx = new LoginContext( "tyrex.security.LDAP", subject );
            ctx.login();
            subject.setReadOnly();
            
            Set      set;
            Iterator iter;
            
            set = subject.getPrincipals();
            iter = set.iterator();
            while ( iter.hasNext() )
                System.out.println( "Principal: " + iter.next() );
            set = subject.getPublicCredentials();
            while ( iter.hasNext() )
                System.out.println( "PubCred: " + iter.next() );
            set = subject.getPrivateCredentials();
            while ( iter.hasNext() )
                System.out.println( "PrivCred: " + iter.next() );
            
        } catch ( Exception except ) {
            System.out.println( except );
            except.printStackTrace();
        }
    }
    
    
    
}
