
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

class ServerCacheEntry
extends CacheEntry
implements Serializable
{
    private static final long serialVersionUID = 3950852135815365213L;

    Set<String> active_reader_addresses;
    Set<String> closable_addresses;
    FileContents contents;
    ServerState state;
    String owner_address;
    ClientContract client_object; // todo add initialization

    private int access_port;
    
    ServerCacheEntry(String user_address, int access_port, String file_name, 
        FileContents contents, Mode mode)
    {
        // call parent constructor
        super(file_name);
        System.err.println("new server cache entry");// ! debug
        
        // set initial empty reader addresses
        this.active_reader_addresses = new HashSet<String>();
        this.closable_addresses = new HashSet<String>();

        // set contents
        this.contents = contents;

        this.access_port = access_port;

        this.Update(mode, user_address);

        this.Renew(user_address);
    }

    void Update(Mode mode, String user_address)
    {
        state = (mode == Mode.READ) ? ServerState.READ_SHARED : 
            ServerState.WRITE_SHARED; 
        
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
        Renew(owner_address);

        // force write back and client state change
        System.err.printf("writeback[%s]\n", owner_address);// ! debug
        
        try
        {
            client_object.WriteBack();   
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
        
        // transfer client
        closable_addresses.add(owner_address);

        // and remove from readerlist
        if(active_reader_addresses.contains(owner_address))
            active_reader_addresses.remove(owner_address);
    }

    void CloseInactives()
    {
        ClientContract remote;

        for(String open_client : closable_addresses)
        {
            //
            remote = Renew(open_client);

            //
            try 
            {
                // shutdown waiting client
                remote.Shutdown();
            }
            catch (Exception error)
            {   // * will always get unmarshalling error
                //error.printStackTrace();
            }
        }
    }

    ClientContract Renew(String user_address)
    {   
        return client_object = 
            (ClientContract)Utility.Lookup(Utility.LOOKUP_CLIENT, user_address,
                access_port);
    }
}
