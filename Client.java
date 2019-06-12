
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
    private String local_host_name;
    private ServerContract server_object;
    private ClientCacheEntry cache_entry;
    
    Client(String server_address, int access_port) throws RemoteException
    {
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

            // ! (1) user input
            this.cache_entry = new ClientCacheEntry(
                    PromptFor("file name"), PromptFor("mode[r/w]"));

            //System.getProperty("user.name");
            state_file = String.format("%s.state", local_host_name);
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }

    void Execute2()
    {
        System.err.println("execute");// ! debug

        FileContents contents;
        // todo refactor logic to mainly utilize state for branching
        // run client
        try
        {
            System.err.println(cache_entry.state.toString()); // ! debug
            // restore previous state if any
            RestoreState();
            System.err.println(cache_entry.state.toString()); // ! debug

            // ! (2) file caching
            if(Utility.OnDisk(CACHE_PATH, cache_entry.file_name))
            {
                // ! (5) accessing cached file
                // write owned -> run emacs
                if(cache_entry.state == ClientState.WRITE_OWNED);

                // read shared + read -> run emacs
                else if(cache_entry.state == ClientState.READ_SHARED &&
                    cache_entry.mode == Mode.READ);

                // read shared + write -> obtain ownership -> run emacs
                else if(cache_entry.state == ClientState.READ_SHARED &&
                    cache_entry.mode == Mode.READ_WRITE)
                {
                    //
                    contents = server_object.Download(
                        local_host_name,
                        cache_entry.file_name,
                        cache_entry.mode);

                    //
                    cache_entry.state = ClientState.WRITE_OWNED;
                }
            }

            // not cached
            else
            {
                // invalid -> download new file
                if(cache_entry.state == ClientState.INVALID)
                {
                    // ! (3) download new file
                    contents = server_object.Download(
                        local_host_name,
                        cache_entry.file_name,
                        cache_entry.mode);

                    // state transition
                    cache_entry.state = cache_entry.mode == Mode.READ_WRITE ?
                        ClientState.WRITE_OWNED : ClientState.READ_SHARED;
                }

                // valid file
                else
                {
                    // ! (6) file replacement
                    // read shared -> nothing
                    if(cache_entry.state == ClientState.READ_SHARED);

                    // write owned -> upload
                    else if(cache_entry.state == ClientState.WRITE_OWNED)
                        LocalWriteBack();

                    // then download
                    contents = server_object.Download(
                        local_host_name,
                        cache_entry.file_name,
                        cache_entry.mode);

                    // state transition
                    cache_entry.state = cache_entry.mode == Mode.READ_WRITE ?
                        ClientState.WRITE_OWNED : ClientState.READ_SHARED;
                }
                // store file into disk
                Cache(contents);
            }
            // ! (4) open with emacs
            RunEmacs();

            // save state in disk
            SaveState();
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }

        System.err.println("exit execute");// ! debug
    }

    void Execute()
    {
        System.err.println("execute");// ! debug

        FileContents contents;
        // todo refactor logic to mainly utilize state for branching
        // run client
        try
        {
            System.err.println(cache_entry.state.toString()); // ! debug
            // restore previous state if any
            RestoreState();
            System.err.println(cache_entry.state.toString()); // ! debug

            // ! (2) file caching
            if(Utility.OnDisk(CACHE_PATH, cache_entry.file_name))
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
                    Cache(contents);  
                }

                //
                else if(cache_entry.state == ClientState.READ_SHARED) 
                    // ? delete
                    if(cache_entry.mode == Mode.READ_WRITE)
                    {
                        contents = server_object.Download(
                            local_host_name,
                            cache_entry.file_name,
                            cache_entry.mode);

                        // store file into disk
                        Cache(contents);  
                    }

                // write back anyway and open
                else LocalWriteBack();

                cache_entry.Update();
            }

            // not cached
            else
            {
                //
                if(cache_entry.state == ClientState.WRITE_OWNED)
                    LocalWriteBack();

                //
                contents = server_object.Download(
                    local_host_name,
                    cache_entry.file_name,
                    cache_entry.mode);
                    
                // store file into disk
                Cache(contents);  
            }  
            
            // ! (4) open with emacs
            RunEmacs();

            // save state in disk
            SaveState();
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }

        System.err.println("exit execute");// ! debug
    }



    void SaveState()
    {
        System.err.println("save state");// ! debug

        String path = String.format(CACHE_PATH, state_file);

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

    private void Cache(FileContents contents)
    {
        String path = String.format(CACHE_PATH, cache_entry.file_name);
        System.out.println(path); // ! debug
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

    private void RestoreState()
    {
        System.err.println("restore state");// ! debug

        String path = String.format(CACHE_PATH, state_file);

        if(Utility.OnDisk(CACHE_PATH, state_file))
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

    private void RunEmacs()
    {
        System.err.println("run emacs");// ! debug

        String path = String.format(CACHE_PATH, cache_entry.file_name);
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

    public boolean Invalidate() throws RemoteException
    {
        System.err.println("invalidate");// ! debug

        cache_entry.state = ClientState.INVALID;

        return true;
    }

    boolean LocalWriteBack()
    {
        System.err.println("local write back");// ! debug

        // get bytes from file
        FileContents contents;

        // write contents back to server
        try
        {
            System.err.println("before retrieve");// ! debug
            contents = 
                Utility.GetFileOnDisk(CACHE_PATH, cache_entry.file_name);
            contents.print(); // ! debug
            System.err.println("retrieve | before upload");// ! debug
            // send to server
            server_object.Upload(
                local_host_name, cache_entry.file_name, contents);
            System.err.println("upload");// ! debug
            return true;
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
        return false;
    }

    public boolean WriteBack() throws RemoteException
    {
        System.err.print("!");// ! debug 

        cache_entry.state = ClientState.READ_SHARED; // ? does rmi cache

        return LocalWriteBack();
    }

    public static void main(String[] arguments)
    {
        // verify argument quantity
        if (arguments.length != 2)
        {
            System.out.println("usage:java Client server_address access_port");
            System.exit(-1);
        }

        // get server address
        String server_address = arguments[0];

        // get access port number
        int access_port = Integer.parseInt(arguments[1]);

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

        System.exit(0);
    }
}
