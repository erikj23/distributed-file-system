
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

class Client
extends UnicastRemoteObject
implements ClientContract, Serializable
{
    // serializable
    private static final long serialVersionUID = -5653266189145749167L;

    // static strings
    private static final String CACHE_PATH = "/tmp/%s";
    private static final String PROGRAM = "emacs %s %s";
    private static final String CHMOD = "chmod %d %s";
    //private static String file_name;
    private static String state_file;
    
    // for user input
    private static Scanner input = new Scanner(System.in);
    
    // instance variables
    private String user_name;
    //private String file_name;
    private String local_host_name;
    private ServerContract server_object;
    private ClientCacheEntry cache_entry;
    
    Client(String server_address, int access_port) throws RemoteException
    {
        System.err.println("new client");// ! debug
        // set up server object
        try // todo change client.state to localhost+username.state
        {
            // get local host addresss
            this.local_host_name = InetAddress.getLocalHost().getHostName();

            // get remote reference
            this.server_object = (ServerContract)Naming.lookup(String.format(
                Utility.LOOKUP_SERVER,
                server_address,
                access_port));
                
            // static asignment
            state_file = String.format("%s.state", local_host_name);
            
            // targets for instance
            this.user_name = System.getProperty("user.name");
                            
            // ! (1) user input
            this.cache_entry = new ClientCacheEntry(PromptFor("file name"), PromptFor("mode[r/w]"));
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }

    void Execute()
    {
        System.err.println("execute");// ! debug

        FileContents contents = null;
        // todo upload on not in cache
        // run client
        try
        {
            // restore previous state if any
            RestoreState();
            
            // 
            cache_entry.Update();

            // ! (2) file caching
            if(Utility.OnDisk(CACHE_PATH, user_name))
            {
                //
                if(cache_entry.state == ClientState.INVALID)
                {
                    //
                    contents = server_object.Download(
                        local_host_name,
                        cache_entry.file_name,
                        cache_entry.mode);

                    // store file into disk
                    System.err.printf("1contents[%s]\n", new String(contents.get()));// ! debug
                }

                //
                else if(cache_entry.state == ClientState.READ_SHARED) 
                {
                    // ? delete
                    if(cache_entry.mode == Mode.READ_WRITE)
                    {
                        contents = server_object.Download(
                            local_host_name,
                            cache_entry.file_name,
                            cache_entry.mode);

                        // store file into disk
                        System.err.printf("2contents[%s]\n", new String(contents.get()));// ! debug
                    }
                }

                else
                {
                    contents = server_object.Download(
                        local_host_name,
                        cache_entry.file_name,
                        cache_entry.mode);
                }
            }

            // not cached
            else
            {
                //
                if(cache_entry.state == ClientState.WRITE_OWNED)
                    LocalWriteBack(cache_entry.file_name);

                //
                contents = server_object.Download(
                    local_host_name,
                    cache_entry.file_name,
                    cache_entry.mode);
                    
                // store file into disk
                System.err.printf("3contents[%s]\n", new String(contents.get()));// ! debug
            }  
            
            // ! (4) open with emacs
            RunEmacs(contents);

            // save state in disk
            SaveState();
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }

        // attempt to shutdown
        try 
        {
            // shutdown if no one depends on this client
            Shutdown();
        }
        catch (Exception error)
        {
            // error.printStackTrace();
        }
    }

    void SaveState()
    {
        System.err.printf("save state %s\n", cache_entry.state); // ! debug

        String path = String.format(CACHE_PATH, state_file);

        try(ObjectOutputStream stream = new ObjectOutputStream(
            new FileOutputStream(path)))
        {
            stream.writeObject(cache_entry);
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }

    private void Cache(FileContents contents)
    {
        String path = String.format(CACHE_PATH, user_name);
        System.out.printf("cache %s\n", path); // ! debug
        // get file stream for this file
        try(FileOutputStream stream = new FileOutputStream(path))
        {
            // write all file contents to disk
            stream.write(contents.get());
        }
        catch(Exception error)
        {   // * permission error will trigger stack trace
            error.printStackTrace();
        }
    }

    private void RestoreState()
    {
        String path = String.format(CACHE_PATH, state_file);
        
        if(Utility.OnDisk(CACHE_PATH, state_file))
        {
            try(ObjectInputStream stream = new ObjectInputStream(
                new FileInputStream(path)))
            {
                // retrieve object from disk
                ClientCacheEntry disk_entry = 
                    (ClientCacheEntry)stream.readObject();

                //if(disk_entry != null) 
                //    if(disk_entry.state == ClientState.WRITE_OWNED)
                //        LocalWriteBack(disk_entry.file_name);
                //
                // copy values over
                cache_entry.state = disk_entry.state;

            }
            catch(Exception error)
            {
                error.printStackTrace();
            }
        }
    }

    private String PromptFor(String sentence)
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

    private void RunEmacs(FileContents contents)
    {
        System.err.println("run emacs");// ! debug

        String path = String.format(CACHE_PATH, user_name);
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
            
            // store file into disk
            if(contents != null) Cache(contents);  
            
            // execute program
            process = runtime.exec(
                String.format(PROGRAM, path, cache_entry.mode.options));

            // wait for above process to terminate
            process.waitFor();
                
            //if(cache_entry.state == ClientState.WRITE_OWNED) 
                //LocalWriteBack(cache_entry.file_name);
            // clean open clients
            // server_object.Clean(cache_entry.file_name);
        }
        catch (Exception error)
        {
            error.printStackTrace();
        }
    }

    boolean LocalWriteBack(String name_on_server)
    {
        System.err.println("local write back");// ! debug

        // get bytes from file
        FileContents contents;

        // write contents back to server
        try
        {
            contents = 
                Utility.GetFileOnDisk(CACHE_PATH, user_name);
            
            // send to server
            server_object.Upload(
                local_host_name, name_on_server, contents);
            
            return true;
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
        return false;
    }

    public void Shutdown() throws RemoteException
    {
        if(cache_entry.state == ClientState.READ_SHARED) System.exit(0);
    }

    public boolean Invalidate() throws RemoteException
    {
        System.err.println("invalidate");// ! debug

        cache_entry.state = ClientState.INVALID;

        return true;
    }


    public boolean WriteBack() throws RemoteException
    {
        System.err.print("!");// ! debug 

        cache_entry.state = ClientState.READ_SHARED; // ? does rmi cache

        return LocalWriteBack(cache_entry.file_name);
    }

    public static void main(String[] arguments)
    {
        // verify argument quantity
        //if (arguments.length != 2)
        //{
        //    System.out.println("usage:java Client server_address access_port");
        //    System.exit(-1);
        //}

        // get server address
        String server_address = "cssmpi1.uwb.edu";

        // get access port number
        int access_port = 22384;

        // create client
        try
        {
            // create registry for this remote client object
            Utility.StartRegistry(access_port);

            // start client and set up initial download
            Client client = new Client(server_address, access_port);

            // bind object to name in registry
            Naming.rebind(
                String.format(Utility.BIND_CLIENT, access_port), client);

            client.Execute();
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }
}
