
import java.net.InetAddress;
import java.rmi.Naming;

class Client
{
    
    private int server_port;
    private String server_address;
    private ServerRemote server_object;
    private ClientRemote client_object;
    // might need to be in client remote
    private String local_host_address;
    private FileContents file_contents;

    Client(String[] arguments)
    {
        //
        try
        {   
            //
            local_host_address = InetAddress.getLocalHost().getHostAddress();

            // get server port from arguments
            server_port = Integer.parseInt(arguments[1]);
            
            // use naming lookup to obtain remote reference from network
            server_object = (ServerRemote)Naming.lookup(
                String.format(
                    "rmi://%s:%d/server", 
                    server_address, 
                    server_port));
                    
            
            client_object = new ClientRemote(
                prompt("sentence", "follow"), 
                prompt("", ""));
            

            //
            file_contents = 
                server_object.download(local_host_address, "file_name", "mode");
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }

    String prompt(String sentence, String follow)
    {
        return null;
    }

    public static void main(String[] arguments)
    {   
        // verify arguments
        if (arguments.length != 2)
        {
            System.out.println("usage:java Client server_address server_port");
            System.exit(-1);
        }

        // start client
        Client client = new Client(arguments);
    }
}
