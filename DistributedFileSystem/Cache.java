import java.util.Vector;

public class Cache 
{
    private String file_name;
    private Vector<String> active_readers;
    private String owner;
    private int state;
    private FileContents content;
    public Cache(String file_name, String reader, String owner, int state, FileContents content) {
        this.file_name = file_name;
        this.active_readers = new Vector<String>();
        this.owner = "";
        this.state = state;
        this.content = content;
    }
}