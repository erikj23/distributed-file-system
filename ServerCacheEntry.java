
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class ServerCacheEntry
extends CacheEntry
implements Serializable
{
    private static final long serialVersionUID = 3950852135815365213L;

    List<String> reader_addresses;
    FileContents contents;
    ServerState state;
    String owner_address;
    ClientContract client_object;
    
    ServerCacheEntry(String user_address, String file_name, 
        FileContents contents, Mode mode)
    {
        // call parent constructor
        super(file_name);

        // set initail empty reader addresses
        this.reader_addresses = new ArrayList<String>();

        // set contents
        this.contents = contents;

        this.Update(mode, user_address);
    }

    void Update(Mode mode, String user_address)
    {
        this.state = (mode == Mode.READ) ? ServerState.READ_SHARED : 
            ServerState.WRITE_SHARED; 
            
        // set owner if mode requested is write
        if(this.state == ServerState.WRITE_SHARED) 
            this.owner_address = user_address;
        
        // else leave empty
        else
        {
            this.owner_address = null;
            this.reader_addresses.add(user_address);
        }
    }

    void Release()
    {
        this.state = ServerState.NOT_SHARED;
    }
}
