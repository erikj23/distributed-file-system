
import java.rmi.Remote;
import java.rmi.RemoteException;

interface ClientContract
extends Remote
{
    boolean invalidate();
    boolean writeback();
}
