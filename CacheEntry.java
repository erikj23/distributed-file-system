
import java.util.ArrayList;
import java.util.List;

class CacheEntry
{
    String file_name;
    String writer_address;
    List<String> reader_addresses;
    State state;

    CacheEntry(String file_name, String writer_address)
    {
        this.file_name = file_name;
        this.writer_address = writer_address;
        this.reader_addresses = new ArrayList<String>();
        this.state = State.NOT_SHARED;
    }
}
