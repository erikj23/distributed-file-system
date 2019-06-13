
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

    Server() throws RemoteException
    {   
        System.err.println("new server");// ! debug
        
        // create cache entries structure
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
    
    public synchronized FileContents Download(String user_address, 
        String file_name, Mode mode)
    throws RemoteException
    {
        System.err.printf("download[%s]\n", user_address);// ! debug
        // manage entry
        ServerCacheEntry cache_entry;

        // does server currently contain an entry
        if(cache_entries.containsKey(file_name))
        {   
            System.err.println("in map");// ! debug
            // get entry with file name
            cache_entry = cache_entries.get(file_name);

            // prepare transfer of ownership
            cache_entry.ReleaseOwnership();
                
            // set new owner and state
            cache_entry.Update(mode, user_address);

            System.err.printf("contents[%s]\n", new String(cache_entries.get(file_name).contents.get()));// ! debug
            return cache_entries.get(file_name).contents;
        }

        // create new cache entry
        else
        {   
            System.err.println("not in map");// ! debug
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
            System.err.printf("contents[%s]\n", new String(cache_entries.get(file_name).contents.get())); // ! debug
            return cache_entry.contents;
        }
    }

    public boolean Upload(String user_address, String file_name, 
        FileContents contents)
    throws RemoteException
    {
        ServerCacheEntry cache_entry = cache_entries.get(file_name);
        System.err.printf("upload[%s@%s]\n", user_address, cache_entry.state);// ! debug
        
        // not shared -> no upload
        if(cache_entry.state == ServerState.NOT_SHARED) return false;

        // read shared -> no upload
        else if(cache_entry.state == ServerState.READ_SHARED) return false;

        // owner change or write shared -> invalidate and write contents
        else
        {   
            ClientContract remote;
            
            

            // invalidate active readers 
            for(String reader : cache_entry.active_reader_addresses)
            {   System.err.println(reader);// ! debug
                remote = cache_entry.Renew(reader);
                if(remote != null) remote.Invalidate();
            }
            
            // empty reader set
            cache_entry.active_reader_addresses.clear();

            // store new contents
            cache_entry.contents = contents;

            return true;
        }
    }

    public void Clean(String file_name) throws RemoteException
    {
        ServerCacheEntry cache_entry = cache_entries.get(file_name);
        cache_entry.CloseInactives();
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
            
            // create a remote server object that implements server interface
            Server server = new Server();
            
            // bind name to object in registry
            Naming.rebind(
                String.format(Utility.BIND_SERVER, access_port), server);
        }
        catch (Exception error)
        {
            error.printStackTrace();
        }
        // todo test with exit
        //System.exit(0);
    }
}
