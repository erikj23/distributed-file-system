
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class Utility
{
    // static strings
    static final String BIND_CLIENT = "rmi://localhost:%d/client";
    static final String BIND_SERVER = "rmi://localhost:%d/server";
    static final String LOOKUP_SERVER = "rmi://%s:%d/server";
    static final String LOOKUP_CLIENT = "rmi://%s:%d/client";


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
        catch(Exception error)
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