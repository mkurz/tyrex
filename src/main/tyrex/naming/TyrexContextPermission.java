package tyrex.naming;


import java.security.BasicPermission;


public class TyrexContextPermission
    extends BasicPermission
{


    public TyrexContextPermission( String path )
    {
	super( path );
    }


}
