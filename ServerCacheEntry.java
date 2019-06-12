
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

class ServerCacheEntry
extends CacheEntry
implements Serializable
{
    private static final long serialVersionUID = 3950852135815365213L;

    Set<String> reader_addresses;
    FileContents contents;
    ServerState state;
    String owner_address;
    ClientContract client_object; // todo add initialization
    
    ServerCacheEntry(String user_address, int access_port, String file_name, 
        FileContents contents, Mode mode)
    {
        // call parent constructor
        super(file_name);

        // set initial empty reader addresses
        this.reader_addresses = new HashSet<String>();

        // set contents
        this.contents = contents;

        this.Update(mode, user_address);

        this.Renew(user_address, access_port);
    }

    void Update(Mode mode, String user_address)
    {
        state = (mode == Mode.READ) ? ServerState.READ_SHARED : 
            ServerState.WRITE_SHARED; 
            
        if(state == ServerState.WRITE_SHARED)
        {    
            // set new owner
            owner_address = user_address;
            
            // remove owner from reader set
            if(reader_addresses.contains(owner_address)) 
                reader_addresses.remove(owner_address);
        }
        else reader_addresses.add(user_address);
    }

    void Release()
    {
        state = ServerState.NOT_SHARED;
    }

    void Renew(String user_address, int access_port)
    {
        client_object = (ClientContract)Utility.Lookup(Utility.LOOKUP_CLIENT,
                    user_address, access_port);
    }
}
