
import java.io.IOException;
import java.io.Serializable;

public class FileContents
implements Serializable
{   // for serializable
    private static final long serialVersionUID = -8990351271322235625L;
    // file contents
    private byte[] contents;
    
    public FileContents(byte[] contents)
    {
        this.contents = contents;
    }

    public void print() throws IOException
    {
        System.out.println("FileContents = " + contents);
    }
    
    public byte[] get()
    {
        return contents;
    }
}
