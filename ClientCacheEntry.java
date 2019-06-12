
import java.io.Serializable;

class ClientCacheEntry
extends CacheEntry
implements Serializable
{
    private static final long serialVersionUID = 3351149849265363371L;

	Mode mode;
    ClientState state;

    ClientCacheEntry(String file_name, String mode)
    {
        // call parent constructor
        super(file_name);
        
        // set mode
        this.mode = mode.startsWith("w") ? Mode.READ_WRITE : Mode.READ;
       
        this.state = ClientState.INVALID;
    }

    void Update()
    {
        if(mode == Mode.READ_WRITE) state = ClientState.WRITE_OWNED;
        else state = ClientState.READ_SHARED;
    }
}
