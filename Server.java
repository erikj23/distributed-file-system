
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Vector;

class Server
{
    private int hosting_port;
    private String local_host_name;
    private List<CacheEntry> cache_entries;
    
    Server(String[] arguments)
    {
        try
        {   
            //
            this.local_host_name = InetAddress.getLocalHost().getHostName();
            Log(local_host_name);

            //
            hosting_port = Integer.parseInt(arguments[0]);
            Log(arguments[0]);

            //
            cache_entries = new Vector<CacheEntry>();
            Log("vector created");

            // start registery on localhost on this port
            StartRegistry(hosting_port);
            Log("registry started");
            
            // create a new object that implements server interface
            ServerContract server_object = new ServerRemote();
            Log("remote object created");

            // bind name to server object
            Naming.rebind(String.format("rmi://localhost:%d/server", hosting_port), server_object);
            Log("bound");
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }
    
    void Log(String output)
    {
        System.err.println(output);
    }
    
    void StartRegistry(int hosting_port) throws RemoteException
    {
        // try to fetch or create registry
        try
        {
            Registry registry = LocateRegistry.getRegistry(hosting_port);
            registry.list();
        }
        catch (RemoteException e)
        {
            Registry registry = LocateRegistry.createRegistry(hosting_port);
        }
    }
    
    public static void main(String[] arguments)
    {   
        // verify argument quantity
        if (arguments.length != 1)
        {
            System.err.println("usage: java Server hosting_port");
            System.exit(-1);
        }

        // start a server
        Server server = new Server(arguments);
    }
}
