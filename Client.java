
import java.io.File;                            // used by in_cache
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;                     // used by write_back
import java.nio.file.Paths;                     // used by write_back
import java.io.Serializable;                    // for rmi
import java.net.InetAddress;                    // used by constructor
import java.rmi.Naming;                         // for rmi
import java.rmi.RemoteException;                // for rmi
import java.rmi.server.UnicastRemoteObject;     // for rmi
import java.util.Scanner;                       // used by prompt_for

class Client
extends UnicastRemoteObject
implements ClientContract, Serializable
{
    // serializable
    private static final long serialVersionUID = -5653266189145749167L;

    // static strings
    private static final String RMI_BIND = "rmi://localhost:%d/client";
    private static final String RMI_LOOKUP = "rmi://%s:%d/server";
    private static final String PATH = "/tmp/%s";
    private static final String PROGRAM = "notepad.exe %s %s";
    private static final String CHMOD = "chmod %d %s";
    private static final String STATE_FILE = "client.state";

    //
    private static Scanner input = new Scanner(System.in);
    
    // instance variables
    private String local_host_name;    
    private ServerContract server_object;
    private ClientCacheEntry cache_entry;

    Client(String server_address, int server_port) throws RemoteException
    {
        // set up server object
        try
        {
            // get local host addresss
            this.local_host_name = InetAddress.getLocalHost().getHostName();
            
            // get remote reference
            this.server_object = (ServerContract)Naming.lookup(String.format(
                RMI_LOOKUP,
                server_address,
                server_port));

            // ! (1) user input
            this.cache_entry = new ClientCacheEntry(
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
        System.err.println("execute");

        FileContents contents;
        
        // run client
        try
        {   
            // restore previous state if any
            //restore_state();

            // ! (2) file caching
            if(in_cache(cache_entry.file_name))
            {   
                // ! (5) accessing cached file
                // write owned -> run emacs
                if(cache_entry.state == ClientState.WRITE_OWNED);
                
                // read shared + read -> run emacs
                if(cache_entry.state == ClientState.READ_SHARED &&
                    cache_entry.mode == Mode.READ);
                    
                // read shared + write -> obtain ownership -> run emacs
                if(cache_entry.state == ClientState.READ_SHARED &&
                    cache_entry.mode == Mode.READ_WRITE)
                {   
                    //
                    contents = server_object.download(
                        local_host_name,
                        cache_entry.file_name,
                        cache_entry.mode.toString());

                    //
                    cache_entry.state = ClientState.WRITE_OWNED;
                }
            }
            else 
            {   
                // invalid -> download new file
                if(cache_entry.state == ClientState.INVALID)
                {
                    // ! (3) download new file
                    contents = server_object.download(
                        local_host_name,
                        cache_entry.file_name,
                        cache_entry.mode.toString());

                    // state transition
                    cache_entry.state = cache_entry.mode == Mode.READ_WRITE ? 
                        ClientState.WRITE_OWNED : ClientState.READ_SHARED;
                }

                // 
                else
                {   
                    // ! (6) file replacement
                    // read shared -> nothing
                    if(cache_entry.state == ClientState.READ_SHARED);

                    // write owned -> upload
                    if(cache_entry.state == ClientState.WRITE_OWNED)
                        write_back();
                    
                    // then download
                    contents = server_object.download(
                        local_host_name,
                        cache_entry.file_name,
                        cache_entry.mode.toString());
                    
                    // state transition
                    cache_entry.state = cache_entry.mode == Mode.READ_WRITE ? 
                        ClientState.WRITE_OWNED : ClientState.READ_SHARED;
                }
                // store file into disk
                cache(contents);
            }
            // ! (4) open with emacs
            run_emacs();
            
            // save state in disk
            save_state();
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
        
        System.err.println("exit execute");
    }

    void save_state()
    {
        System.err.println("save_state");

        String path = String.format(PATH, STATE_FILE);

        try(ObjectOutputStream state_stream = new ObjectOutputStream(
            new FileOutputStream(path)))
        {
            state_stream.writeObject(cache_entry);
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }

    /**
     * in_cache checks the tmp directory for the specified file.
     * @param file_name - File we are looking for in the /tmp directory
     * @return - True if the file is tmp, false otherwise
     */
    boolean in_cache(String file_name)
    {
        // create file instance
        File check = new File(String.format(PATH, file_name));
        
        return check.exists();
    }

    private void cache(FileContents contents)
    {
        String path = String.format(PATH, cache_entry.file_name);
        
        // get file stream for this file
        try(FileOutputStream stream = new FileOutputStream(path))
        {   
            // write all file contents to disk
            stream.write(contents.get());
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }

    private void restore_state()
    {
        System.err.println("restore_state");

        String path = String.format(PATH, STATE_FILE);
        
        if(in_cache(STATE_FILE))
        {
            try(ObjectInputStream state_stream = new ObjectInputStream(
                new FileInputStream(path)))
            {
                cache_entry.state = 
                    ((ClientCacheEntry)state_stream.readObject()).state;
            }
            catch(Exception error)
            {
                error.printStackTrace();
            }
        }
    }

    private String prompt_for(String sentence)
    {
        try
        {
            System.out.printf("%s: ", sentence);
            String line = input.next();
            return line.toLowerCase();
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
        return null;
    }

    private void run_emacs()
    {
        System.err.println("run_emacs");

        String path = String.format(PATH, cache_entry.file_name);
        // run emacs
        try 
        {
            // get current runtime object
            Runtime runtime = Runtime.getRuntime();
            
            // manage process
            Process process;
            
            // execute chmod
            process = runtime.exec(
                String.format(CHMOD, cache_entry.mode.permission, path));

            // execute program
            process = runtime.exec(
                String.format(PROGRAM, path, cache_entry.mode.options));
            
            // wait for above process to terminate
            process.waitFor();
        } 
        catch (Exception error)
        {
            error.printStackTrace();
        }
    }

    public boolean invalidate() throws RemoteException
    {   
        System.err.println("invalidate");

        cache_entry.state = ClientState.INVALID;
        
        return true;
    }

    public boolean write_back() throws RemoteException
    {
        System.err.println("write_back");

        // get bytes from file
        FileContents contents;
        
        // write contents back to server
        try
        {
            contents = new FileContents(Files.readAllBytes(
                Paths.get(String.format(PATH, cache_entry.file_name))));
            
            // send to server
            server_object.upload(local_host_name, cache_entry.file_name, contents);

            return true;
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
        return false;
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

        // create client
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

        System.exit(0);
    }
}
