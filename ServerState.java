
enum ServerState
{
    NOT_SHARED, // give to whoever
    READ_SHARED, // give to whoever + invalidate if write
    WRITE_SHARED, // change ownership
    OWNERSHIP_CHANGE;
}
