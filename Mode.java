
enum Mode
{
    READ("-f view-mode", 400),
    READ_WRITE("", 600);

    String options;
    int permission;

    Mode(String options, int permission)
    {
        this.options = options;
        this.permission = permission;
    }
}
