
import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

class Server
extends UnicastRemoteObject
implements ServerContract, Serializable
{
    // serializable
    private static final long serialVersionUID = 5781815840427285195L;

    // static strings
    private static final String CACHE_PATH = "./.cache/%s";

    // static variables
    private static int access_port;
    
    // instance variables
    private String local_host_name;
    //private List<ServerCacheEntry> cache_entries; 
    private Map<String, ServerCacheEntry> cache_entries;

    Server() throws RemoteException
    {
        // create cache entries structure
        //cache_entries = new Vector<ServerCacheEntry>(0);
        cache_entries = new HashMap<String, ServerCacheEntry>();
        // set up local host
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
    
    public FileContents Download(String user_address, String file_name, 
        Mode mode)
    throws RemoteException
    {
        // manage entry
        ServerCacheEntry cache_entry;

        // does server currently contain an entry
        if(cache_entries.containsKey(file_name))
        {   // ! dont forget to add to readers list!!
            // get entry with that file name
            cache_entry = cache_entries.get(file_name);

            // not shared -> set r/w
            if(cache_entry.state == ServerState.NOT_SHARED)
                // set update reader list
                cache_entry.Update(mode, user_address);
            
            // read shared -> set r/w (no current owner)
            else if(cache_entry.state == ServerState.READ_SHARED)
                // set state and update reader list
                cache_entry.Update(mode, user_address);
                
            // write shared -> 
            else if(cache_entry.state == ServerState.WRITE_SHARED)
            {

            }
            
            return cache_entries.get(file_name).contents;
        }

        // create new cache entry
        else
        {   
            FileContents contents;
    
            // does the file system have the file on disk
            if(Utility.OnDisk(CACHE_PATH, file_name))
            {
                // get contents from disk
                contents = Utility.GetFileOnDisk(CACHE_PATH, file_name);
                
                // create entry
                cache_entry = new ServerCacheEntry(user_address, file_name, 
                    contents, mode);

                // put entry in map
                cache_entries.put(file_name, cache_entry);
            }

            // create empty file and add to entry
            else
            {
                contents = new FileContents("testing text\n".getBytes());

                cache_entry = new ServerCacheEntry(user_address, file_name, 
                    contents, mode);

                cache_entries.put(file_name, cache_entry);
            }
        }

        return cache_entry.contents;
    }

    public boolean Upload(String address, String file_name, 
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
