
class Test 
{
    public static void main(String[] arguments) 
    {
        Process process;
        try 
        {
            Runtime runtime = Runtime.getRuntime();
            process = runtime.exec("code Test.java");
        } 
        catch (Exception error)
        {
            
            error.printStackTrace();
        }

    }

}
