
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Vector;

class ServerRemote
extends UnicastRemoteObject
implements ServerContract, Serializable
{
    private static final long serialVersionUID = 5781815840427285195L;
    
    List<ServerCacheEntry> cache_entries; 

    ServerRemote() throws RemoteException
    {
        cache_entries = new Vector<ServerCacheEntry>();
    }
    
    public FileContents download(String address, String file_name, String mode)
    throws RemoteException
    {
        // send
        // create send
        return new FileContents("testing text\n".getBytes());
    }

    public boolean upload(String address, String file_name, 
        FileContents contents)
    throws RemoteException
    {
        
        return false;
    }
}
