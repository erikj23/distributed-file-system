
class ClientCacheEntry
extends CacheEntry
{
    Mode mode;
    Boolean is_owner;
    ClientState state;

    ClientCacheEntry(String file_name, String mode)
    {
        // call parent constructor
        super(file_name);
        
        //
        this.mode = Mode.valueOf(mode.toUpperCase().startsWith("W") ?
            "READ_WRITE" : "READ");
        
        //
        this.is_owner = this.mode == Mode.READ_WRITE ? true : false;
        
        //
        this.state = ClientState.INVALID;
    }
}
