
import java.io.Serializable;

class CacheEntry
implements Serializable
{
    private static final long serialVersionUID = 4633270602881918086L;
    
	String file_name;

    CacheEntry(String file_name)
    {
        this.file_name = file_name;
    }
}
