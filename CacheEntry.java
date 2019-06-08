

class CacheEntry
{
    String file_name;
    State state;

    CacheEntry(String file_name)
    {
        this.file_name = file_name;
        this.state = State.NOT_SHARED;
    }
}
