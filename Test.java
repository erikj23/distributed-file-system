
import java.io.File;

class Test 
{
    public static void main(String[] arguments) 
    {
        //test_emacs();
        
    }

    static void test_()
    {
        
    }
    static void test_emacs()
    {
        try 
        {
            // get current runtime object
            Runtime runtime = Runtime.getRuntime();
            
            // manage process
            Process process;
            
           // execute program
            process = runtime.exec("emacs");
                //String.format("emacs %s %s", "/tmp/test.txt", ""));
            
            // wait for above process to terminate
            process.waitFor();
        } 
        catch (Exception error)
        {
            error.printStackTrace();
        }
    }
}
