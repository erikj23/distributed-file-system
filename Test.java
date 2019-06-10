
import java.io.File;

class Test 
{
    public static void main(String[] arguments) 
    {
        // create file instance
        File check = new File("./Mode.java");
        
        System.out.println(check.exists());

    }

}
