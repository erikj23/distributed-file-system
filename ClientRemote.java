
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

class ClientRemote
extends UnicastRemoteObject
implements ClientContract, Serializable
{
    private static final long serialVersionUID = -5653266189145749167L;

    ClientRemote() throws RemoteException{}

    @Override
    public boolean invalidate() {
        return false;
    }

    @Override
    public boolean writeback() {
        return false;
    }

}
