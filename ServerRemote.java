
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

class ServerRemote
extends UnicastRemoteObject
implements ServerContract, Serializable
{
    private static final long serialVersionUID = 5781815840427285195L;
    
    ServerRemote() throws RemoteException{}
    
    @Override
    public FileContents download(String myIpName, String filename, String mode) {
        return null;
    }

    @Override
    public boolean upload(String myIpName, String filename, FileContents contents) {
        return false;
    }
}
