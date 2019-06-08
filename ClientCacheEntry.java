
class ClientCacheEntry
extends CacheEntry
{
    Boolean is_owner;
    Mode mode;
    
    ClientCacheEntry(String file_name, String mode)
    {
        super(file_name);
        this.mode = Mode.valueOf(mode);
        this.is_owner = this.mode == Mode.READ_WRITE ? true : false;
    }
}
