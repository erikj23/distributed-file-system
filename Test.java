
import java.io.File;

class Test 
{
    public static void main(String[] arguments) 
    {
        try 
        {
            // get current runtime object
            Runtime runtime = Runtime.getRuntime();
            
            // manage process
            Process process;
            
           // execute program
            process = runtime.exec("code");
                //String.format("emacs %s %s", "/tmp/test.txt", ""));
            
            // wait for above process to terminate
            //process.waitFor();
        } 
        catch (Exception error)
        {
            error.printStackTrace();
        }

    }

}
