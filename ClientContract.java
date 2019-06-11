
import java.rmi.Remote;
import java.rmi.RemoteException;

interface ClientContract
extends Remote
{
    boolean Invalidate() throws RemoteException;
    boolean WriteBack() throws RemoteException;
}
