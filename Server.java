
import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Vector;

class Server
extends UnicastRemoteObject
implements ServerContract, Serializable
{
    // serializable
    private static final long serialVersionUID = 5781815840427285195L;

    private static int access_port;
    
    // instance variables
    private String local_host_name;
    private List<ServerCacheEntry> cache_entries; 

    Server() throws RemoteException
    {
        cache_entries = new Vector<ServerCacheEntry>();
        
        //
        try
        {
            // get local host name
            local_host_name = InetAddress.getLocalHost().getHostName();
            Utility.Log(local_host_name);
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }

    void execute()
    {

    }
    
    public FileContents download(String address, String file_name, String mode)
    throws RemoteException
    {
        // send
        // create send
        // ! debug
        return new FileContents("testing text\n".getBytes());
    }

    public boolean upload(String address, String file_name, 
        FileContents contents)
    throws RemoteException
    {
        
        return false;
    }

    public static void main(String[] arguments)
    {
        // verify argument quantity
        if (arguments.length != 1)
        {
            System.err.println("usage: java Server access_port");
            System.exit(-1);
        }

        // get access port
        access_port = Integer.parseInt(arguments[0]);
        Utility.Log(arguments[0]);

        try
        {
            // start registery on localhost on this port
            Utility.StartRegistry(access_port);
            Utility.Log("registry started");

            // create a remote server object that implements server interface
            Server server = new Server();
            Utility.Log("remote object created");

            // bind name to object in registry
            Naming.rebind(
                String.format(Utility.BIND_SERVER, access_port), server);
            Utility.Log("bound");
            
        }
        catch (Exception error)
        {
            error.printStackTrace();
        }
        // todo test with exit
        //System.exit(0);
    }
}
