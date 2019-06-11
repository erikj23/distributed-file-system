
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class ServerCacheEntry
extends CacheEntry
implements Serializable
{
    private static final long serialVersionUID = 3950852135815365213L;

    String owner_address;
    ClientContract client_object;
    List<String> reader_addresses;
    ServerState state;
    
    ServerCacheEntry(String file_name, String writer_address)
    {
        // call parent constructor
        super(file_name);

        // set owner address
        this.owner_address = writer_address;

        // set initail empty reader addresses
        this.reader_addresses = new ArrayList<String>();

        // set state
        this.state = ServerState.NOT_SHARED;
    }
}
