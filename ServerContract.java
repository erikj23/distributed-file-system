
import java.rmi.Remote;
import java.rmi.RemoteException;

interface ServerContract
extends Remote
{
    FileContents Download(String user_address, String file_name, Mode mode) 
    throws RemoteException;
    
    boolean Upload(String user_address, String file_name, FileContents contents) 
    throws RemoteException;
}
