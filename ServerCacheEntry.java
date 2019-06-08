
import java.util.ArrayList;
import java.util.List;

class ServerCacheEntry
extends CacheEntry
{
    String writer_address;
    List<String> reader_addresses;
    
    ServerCacheEntry(String file_name, String writer_address)
    {
        super(file_name);
        this.writer_address = writer_address;
        this.reader_addresses = new ArrayList<String>();
    }

}
