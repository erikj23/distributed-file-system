
import java.util.ArrayList;
import java.util.List;

class ServerCacheEntry
extends CacheEntry
{
    String owner_address;
    List<String> reader_addresses;
    ServerState state;
    
    ServerCacheEntry(String file_name, String writer_address)
    {
        super(file_name);
        this.owner_address = writer_address;
        this.reader_addresses = new ArrayList<String>();
        this.state = ServerState.NOT_SHARED;
    }

}
