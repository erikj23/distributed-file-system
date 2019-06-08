
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class Server
{
    private final String RMI_BASE = "rmi://localhost:%d/server";
    private int host_port;
    private String local_host_name;
    
    Server(String[] arguments)
    {
        //
        try
        {   
            // get local host name 
            local_host_name = InetAddress.getLocalHost().getHostName();
            Log(local_host_name);

            // get port from command line
            host_port = Integer.parseInt(arguments[0]);
            Log(arguments[0]);

            // start registery on localhost on this port
            StartRegistry(host_port);
            Log("registry started");
            
            // create a remote server object that implements server interface
            ServerContract server_object = new ServerRemote();
            Log("remote object created");

            // bind name to object in registry
            Naming.rebind(String.format(RMI_BASE, host_port), server_object);
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
    
    void StartRegistry(int host_port) throws RemoteException
    {
        // try to fetch or create registry
        try
        {
            Registry registry = LocateRegistry.getRegistry(host_port);
            registry.list();
        }
        catch (RemoteException e)
        {
            Registry registry = LocateRegistry.createRegistry(host_port);
        }
    }
    
    public static void main(String[] arguments)
    {   
        // verify argument quantity
        if (arguments.length != 1)
        {
            System.err.println("usage: java Server host_port");
            System.exit(-1);
        }

        // start a server
        Server server = new Server(arguments);
    }
}
