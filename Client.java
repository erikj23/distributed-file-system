// Hello this is a test
import java.net.InetAddress;
import java.rmi.Naming;
import java.util.Scanner;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.File;                            // Used by in_cache

class Client
extends UnicastRemoteObject
implements ClientContract, Serializable
{
    private static final long serialVersionUID = -5653266189145749167L;
    private static final String RMI_BIND = "rmi://localhost:%d/client";
    
    private final String RMI_LOOKUP = "rmi://%s:%d/server";
    private final String CHMOD = "chmod %d %s";
    private final String PROGRAM = "emacs %s %s";
    
    private ServerContract server_object;
    private String local_host_name;    
    private ClientCacheEntry cache_entry;

    Client(String server_address, int server_port) throws RemoteException
    {
        // set up server_object
        try
        {
            // get local host addresss
            local_host_name = InetAddress.getLocalHost().getHostName();
            
            // get remote reference
            server_object = (ServerContract)Naming.lookup(String.format(
                RMI_LOOKUP,
                server_address,
                server_port));
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }

    void execute()
    {
        FileContents contents;

        //
        try
        {   // ! (1) user input
            // create cache entry
            cache_entry = new ClientCacheEntry(
                prompt_for("file name"),
                prompt_for("mode"));

            
            // ! (2) file caching
            if(in_cache(cache_entry.file_name))
            {
                // check if valid
            }            
            else 
            {   
                // ! (3) download new file
                contents = server_object.download(
                    local_host_name,
                    cache_entry.file_name,
                    cache_entry.mode.toString());

                // store file into disk    
                cache(contents);
            }

            // ! (4) open with emacs
            run_emacs();
        }
        catch(Exception error)
        {

        }
    }

    String prompt_for(String sentence)
    {
        try
        {
            Scanner input = new Scanner(System.in);

            System.out.printf("%s: ", sentence);
            return input.nextLine().toUpperCase();

        }
        catch(Exception error)
        {
            error.printStackTrace();
        }

        return null;
    }

    public boolean invalidate() throws RemoteException
    {
        return false;
    }

    public boolean write_back() throws RemoteException
    {
        return false;
    }

    /**
     * in_cache checks the tmp directory for the specified file.
     * @param file_name - File we are looking for in the /tmp directory
     * @return - True if the file is tmp, false otherwise
     */
    boolean in_cache(String file_name)
    {
        String filePathName = "/tmp/";
        File check = new File(filePathName + filetolookfor);
        return check.exists();
    }

    void cache(FileContents contents)
    {

    }

    void run_emacs()
    {
        //
        try 
        {
            // get current runtime object
            Runtime runtime = Runtime.getRuntime();
            
            // manage process
            Process process;
            
            // execute chmod
            process = runtime.exec(
                String.format(
                    CHMOD, 
                    cache_entry.mode.permission,
                    cache_entry.file_name));

            // execute program
            process = runtime.exec(
                String.format(
                    PROGRAM, 
                    cache_entry.file_name, 
                    cache_entry.mode.options));
            
            // wait for above program to terminate
            process.waitFor();
        } 
        catch (Exception error)
        {
            
            error.printStackTrace();
        }
    }


    public static void main(String[] arguments)
    {
        // verify argument quantity
        if (arguments.length != 2)
        {
            System.out.println("usage:java Client server_address server_port");
            System.exit(-1);
        }
        
        // get server address
        String server_address = arguments[0];
        
        // get server port number
        int server_port = Integer.parseInt(arguments[1]);

        //
        try
        {
            // create registry for this remote client object
            Utility.StartRegistry(server_port);

            // start client and set up initial download
            Client client = new Client(server_address, server_port);
            
            // bind object to name in registry
            Naming.rebind(String.format(RMI_BIND, server_port), client);
    
            client.execute();
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }
}
