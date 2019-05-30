
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

class ServerRemote
extends UnicastRemoteObject
implements ServerContract, Serializable
{
    private static final long serialVersionUID = 5781815840427285195L;
    
    ServerRemote() throws RemoteException{}

    public void function() throws RemoteException{}
}
