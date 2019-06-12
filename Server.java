
import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, ServerCacheEntry> cache_entries;
    private Object lock; 

    Server() throws RemoteException
    {
        // create cache entries structure
        cache_entries = new HashMap<String, ServerCacheEntry>();

        // initialize new lock
        lock = new Object();

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
    
    public synchronized FileContents Download(String user_address, 
        String file_name, Mode mode)
    throws RemoteException
    {
        // manage entry
        ServerCacheEntry cache_entry;

        // does server currently contain an entry
        if(cache_entries.containsKey(file_name))
        {   
            // get entry with file name
            cache_entry = cache_entries.get(file_name);

            // not shared -> set r/w
            if(cache_entry.state == ServerState.NOT_SHARED)
                // update reader list
                cache_entry.Update(mode, user_address);
            
            // read shared -> set r/w (no current owner)
            else if(cache_entry.state == ServerState.READ_SHARED)
                // set state and update reader list
                cache_entry.Update(mode, user_address);
                
            // write shared -> write back
            else if(cache_entry.state == ServerState.WRITE_SHARED)
            {   
                // renew client before a write back
                cache_entry.Renew(user_address, access_port);

                // force write back and client state change
                cache_entry.client_object.WriteBack();

                // set state
                cache_entry.Update(mode, user_address);
            }
            System.err.println(new String(cache_entries.get(file_name).contents.get())); // ! debug
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
                cache_entry = new ServerCacheEntry(user_address, access_port, 
                    file_name, contents, mode);

                // put entry in map
                cache_entries.put(file_name, cache_entry);
            }

            // create empty file and add to entry
            else
            {
                contents = new FileContents(new byte[0]);

                cache_entry = new ServerCacheEntry(user_address, access_port,
                    file_name, contents, mode);

                cache_entries.put(file_name, cache_entry);
            }

            return cache_entry.contents;
        }
    }

    public boolean Upload(String user_address, String file_name, 
        FileContents contents)
    throws RemoteException
    {
        System.err.println("upload");// ! debug
        ServerCacheEntry cache_entry = cache_entries.get(file_name);
        
        // not shared -> no upload
        if(cache_entry.state == ServerState.NOT_SHARED) return false;

        // read shared -> no upload
        else if(cache_entry.state == ServerState.READ_SHARED) return false;

        // owner change or write shared -> invalidate and write contents
        else
        {
            ClientContract remote;
            System.err.println("before invalidation");// ! debug
            for(String reader : cache_entry.reader_addresses)
            {
                System.err.println(reader); // ! debug
                remote = (ClientContract)Utility.Lookup(Utility.LOOKUP_CLIENT,
                    reader, access_port);
                
                remote.Invalidate();
            }
            System.err.println("after invalidation");// ! debug
            // empty reader set
            cache_entry.reader_addresses.clear();


            cache_entry.contents = contents;

            return true;
        }
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
