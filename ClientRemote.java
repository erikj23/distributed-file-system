
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

class ClientRemote
extends UnicastRemoteObject
implements ClientContract, Serializable
{
    private static final long serialVersionUID = -5653266189145749167L;
    
    ClientCacheEntry cache_entry;

    ClientRemote(String file_name, String mode) throws RemoteException
    {
        cache_entry = new ClientCacheEntry(file_name, mode);
    }

    @Override
    public boolean invalidate() throws RemoteException
    {
        System.out.println("invalidate");
        return false;
    }

    @Override
    public boolean write_back() throws RemoteException
    {
        System.out.println("writeback");
        return false;
    }

}
