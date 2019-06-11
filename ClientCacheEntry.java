import java.io.Serializable;

class ClientCacheEntry
extends CacheEntry
implements Serializable
{
    private static final long serialVersionUID = 3351149849265363371L;

	Mode mode;
    Boolean is_owner;
    ClientState state;

    ClientCacheEntry(String file_name, String mode)
    {
        // call parent constructor
        super(file_name);
        
        // set mode
        this.mode = mode.startsWith("w") ? Mode.READ_WRITE : Mode.READ;
        
        // set owner status
        this.is_owner = this.mode == Mode.READ_WRITE ? true : false;
        
        // set state
        this.state = ClientState.INVALID;
    }
}
