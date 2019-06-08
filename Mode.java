
enum Mode
{
    READ("--eval '(setq buffer-read-only t)'"),
    READ_WRITE("");

    String options;

    Mode(String options)
    {
        this.options = options;
    }
}
