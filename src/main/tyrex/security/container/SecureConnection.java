package tyrex.security.container;


import java.util.Vector;
import java.security.AccessController;
import javax.security.auth.AuthPermission;


/**
 * Credentials indicating a secure connection. This credentials inform the container
 * that it has established a secure connection with the client. It does not tell the
 * container anything about the client (e.g. whether the certificate is trusted),
 * only that the server has used it's secure connection capabilities. The definition
 * of what a secure connection is depends on the container configuration, e.g. some
 * containers may regard 48-bit SSL as secure, others may require 128-bit and an IPSEC
 * connection, etc.
 *
 * @author <a href="arkin@intalio.com">Assaf Arkin</a>
 * @version $Revision: 1.3 $ $Date: 2000/09/08 23:05:51 $
 */
public final class SecureConnection
{


    private int  _keySize;


    /**
     * Constructs a new secure connection credential with the specified key size.
     *
     * @param keySize The key size
     */
    public SecureConnection( int keySize )
    {
        _keySize = keySize;
    }


    /**
     * Returns the key size. This is the key size of the server certificate
     * used to established the secure connection (e.g. 48, 128). The key size
     * may depend on the capabilities of the client.
     *
     * @return The key size
     */
    public int getKeySize()
    {
        return _keySize;
    }
    

}
