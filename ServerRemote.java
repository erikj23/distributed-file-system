
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
    
    @Override
    public FileContents download(String myIpName, String filename, String mode)
    throws RemoteException
    {
        // send
        // create send
        return null;
    }

    @Override
    public boolean upload(String myIpName, String filename, 
        FileContents contents)
    throws RemoteException
    {
        
        return false;
    }
}
