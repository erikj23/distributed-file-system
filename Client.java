
import java.net.InetAddress;
import java.rmi.Naming;
import java.util.Scanner;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

class Client
extends UnicastRemoteObject
implements ClientContract, Serializable
{
    private static final long serialVersionUID = -5653266189145749167L;
    private static final String RMI_BIND = "rmi://localhost:%d/client";
    
    private final String RMI_LOOKUP = "rmi://%s:%d/server";
    private final String PROGRAM = "emacs %s %s";
    
    private ServerContract server_object;
    private String local_host_name;    
    private ClientCacheEntry cache_entry;

    Client(String server_address, int server_port) throws RemoteException
    {
        //
        try
        {
            // get local host addresss
            local_host_name = InetAddress.getLocalHost().getHostName();
            
            // use naming lookup to obtain remote reference from network
            server_object = (ServerContract)Naming.lookup(String.format(
                RMI_LOOKUP,
                server_address,
                server_port));

            // create registry for this remote client object
            Utility.StartRegistry(server_port);

            // create client object
            cache_entry = new ClientCacheEntry(
                prompt_for("file name"),
                prompt_for("mode"));

            

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
        {
            // download file from server
            contents = server_object.download(
                local_host_name,
                cache_entry.file_name,
                cache_entry.mode.toString());

            // store file into disk
            cache(contents);

            // run the emacs client in mode requested
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

    void cache(FileContents contents)
    {

    }

    void chmod()
    {

    }

    void run_emacs()
    {
        //
        try 
        {
            // get current runtime object
            Runtime runtime = Runtime.getRuntime();
            
            // execute program
            Process process = runtime.exec(
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
        // verify arguments
        if (arguments.length != 2)
        {
            System.out.println("usage:java Client server_address server_port");
            System.exit(-1);
        }
        
        //
        String server_address = arguments[0];
        
        //
        int server_port = Integer.parseInt(arguments[1]);

        try
        {
            // start client and set up initial download
            Client client = new Client(server_address, server_port);
            
            Naming.rebind(String.format(RMI_BIND, server_port), client);
    
            client.execute();
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }
}
