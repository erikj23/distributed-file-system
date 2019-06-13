
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class Utility
{
    // static strings
    static final String BIND_CLIENT = "rmi://localhost:%d/client";
    static final String BIND_SERVER = "rmi://localhost:%d/server";
    static final String LOOKUP_SERVER = "rmi://%s:%d/server";
    static final String LOOKUP_CLIENT = "rmi://%s:%d/client";

    /**
     * OnDisk checks the tmp directory for the specified file.
     * @param path_format - format string for directory to look in
     * @param file_name - file we are looking for in the /tmp directory
     * @return - true if the file is tmp, false otherwise
     */
    static boolean OnDisk(String path_format, String file_name)
    {
        // create file instance
        File check = new File(String.format(path_format, file_name));
        
        return check.exists();
    }

    static FileContents GetFileOnDisk(String path_format, String file_name)
    {
        //
        String path = String.format(path_format, file_name);
        System.err.printf("get[%s]\n", path);// ! debug
        
        //
        try
        {
            //
            return new FileContents(Files.readAllBytes(Paths.get(path)));
        }
        catch (Exception error)
        {
            error.printStackTrace();
        }

        return null;
    }

    static void StartRegistry(int host_port)
    {
        System.err.println("registry started");// ! debug

        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                // try to fetch or create registry
                try
                {
                    // find registry
                    Registry registry = LocateRegistry.getRegistry(host_port);
                    
                    // to throw exceptions
                    registry.list();
                }
                catch(Exception error)
                {
                    try
                    {
                        // create registry
                        Registry registry = LocateRegistry.createRegistry(host_port);
                    }
                    catch(Exception nested_error)
                    {
                        nested_error.printStackTrace();
                    }
                }        
            }
        });

        thread.start();
    }

    static Remote Lookup(String target_format, String address, int access_port)
    {
        System.err.printf("lookup[%s]\n", address);// ! debug

        try
        {
            return Naming.lookup(String.format(target_format, address, 
                access_port));
        }
        catch(Exception error)
        {
            error.printStackTrace(System.out);
        }

        return null;
    }

    static void Log(String output)
    {
        System.err.println(output);
    }
}