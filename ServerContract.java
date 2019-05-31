
import java.rmi.Remote;
import java.rmi.RemoteException;

interface ServerContract
extends Remote
{
    FileContents download(String myIpName, String filename, String mode);
    boolean upload(String myIpName, String filename, FileContents contents);
}
