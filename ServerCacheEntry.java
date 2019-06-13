
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ServerCacheEntry
extends CacheEntry
implements Serializable
{
    private static final long serialVersionUID = 3950852135815365213L;

    List<String> active_reader_addresses;
    List<String> closable_addresses;
    FileContents contents;
    ServerState state;
    String owner_address;
    ClientContract client_object;

    private int access_port;
    
    ServerCacheEntry(String user_address, int access_port, String file_name, 
        FileContents contents, Mode mode)
    {
        // call parent constructor
        super(file_name);
        System.err.println("new server cache entry");// ! debug
        
        // set initial empty reader addresses
        this.active_reader_addresses = new ArrayList<String>();
        this.closable_addresses = new ArrayList<String>();

        // set contents
        this.contents = contents;

        this.access_port = access_port;

        this.state = ServerState.NOT_SHARED;

        this.Update(mode, user_address);

        this.Renew(user_address);
    }

    void Update(Mode mode, String user_address)
    {
        //
        if(state == ServerState.NOT_SHARED)
        {
            state = (mode == Mode.READ) ? ServerState.READ_SHARED : 
                ServerState.WRITE_SHARED; 
        }

        //
        else if(state == ServerState.READ_SHARED)
        {
            if(mode == Mode.READ_WRITE) state = ServerState.WRITE_SHARED;
        }

        //
        if(state == ServerState.WRITE_SHARED)
        {   
            // set new owner
            owner_address = user_address;
            
            // remove new owner from reader set
            if(active_reader_addresses.contains(owner_address)) 
                active_reader_addresses.remove(owner_address);
        }

        // add to readers on read
        else active_reader_addresses.add(user_address);
    }

    void ReleaseOwnership()
    {
        // renew current owner remote object
        System.err.println(Arrays.toString(active_reader_addresses.toArray()));// ! debug
        System.err.println(Arrays.toString(closable_addresses.toArray()));// ! debug
        System.err.printf("renew[%s]\n", owner_address);// ! debug

        if(owner_address == null) Renew(active_reader_addresses.get(0));
        else Renew(owner_address);

        // force write back and client state change
        try
        {
            client_object.WriteBack();   
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
        
        // transfer client
        if(owner_address != null) closable_addresses.add(owner_address);
        
        // and remove from readerlist
        active_reader_addresses.remove(owner_address);
    }

    void CloseInactives()
    {
        System.err.println("close inactives");// ! debug
        
        ClientContract remote;
        
        for(String open_client : closable_addresses)
        {   // remove open clientss
            active_reader_addresses.remove(open_client);
            
            //
            remote = Renew(open_client);
            
            //
            try 
            {
                // shutdown waiting client
                remote.Shutdown();
            }
            catch (Exception error)
            {   // * will always get unmarshalling error on shutdown
                //error.printStackTrace();
            }
        }
        System.err.println(Arrays.toString(active_reader_addresses.toArray()));// ! debug
        System.err.println(Arrays.toString(closable_addresses.toArray()));// ! debug
    }

    ClientContract Renew(String user_address)
    {   
        return client_object = 
            (ClientContract)Utility.Lookup(Utility.LOOKUP_CLIENT, user_address,
                access_port);
    }
}
