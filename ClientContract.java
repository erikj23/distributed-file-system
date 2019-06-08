
import java.rmi.Remote;
import java.rmi.RemoteException;

interface ClientContract
extends Remote
{
    boolean invalidate() throws RemoteException;
    boolean write_back() throws RemoteException;
}
