
import java.rmi.Remote;
import java.rmi.RemoteException;

interface ServerContract
extends Remote
{
    FileContents download(String address, String file_name, String mode) 
    throws RemoteException;
    
    boolean upload(String address, String file_name, FileContents contents) 
    throws RemoteException;
}
