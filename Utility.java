
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class Utility
{
    static void StartRegistry(int host_port) throws RemoteException
    {
        // try to fetch or create registry
        try
        {
            // find registry
            Registry registry = LocateRegistry.getRegistry(host_port);
            
             // to throw exceptions
            registry.list();
        }
        catch (RemoteException e)
        {
            // create registry
            Registry registry = LocateRegistry.createRegistry(host_port);
        }
    }

    static void Log(String output)
    {
        System.err.println(output);
    }
}