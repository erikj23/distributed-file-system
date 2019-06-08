
enum Mode
{
    READ("--eval '(setq buffer-read-only t)'", 400),
    READ_WRITE("", 600);

    String options;
    int permission;

    Mode(String options, int permission)
    {
        this.options = options;
        this.permission = permission
    }
}
