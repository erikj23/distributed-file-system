
import java.net.InetAddress;
import java.rmi.Naming;

class Server
{
    private final String RMI_BIND = "rmi://localhost:%d/server";
    private int host_port;
    private String local_host_name;

    Server(String[] arguments)
    {
        //
        try
        {
            // get local host name
            local_host_name = InetAddress.getLocalHost().getHostName();
            Utility.Log(local_host_name);

            // get port from command line
            host_port = Integer.parseInt(arguments[0]);
            Utility.Log(arguments[0]);

            // start registery on localhost on this port
            Utility.StartRegistry(host_port);
            Utility.Log("registry started");

            // create a remote server object that implements server interface
            ServerRemote server_object = new ServerRemote();
            Utility.Log("remote object created");

            // bind name to object in registry
            Naming.rebind(String.format(RMI_BIND, host_port), server_object);
            Utility.Log("bound");
        }
        catch(Exception error)
        {
            error.printStackTrace();
        }
    }

    public static void main(String[] arguments)
    {
        // verify argument quantity
        if (arguments.length != 1)
        {
            System.err.println("usage: java Server host_port");
            System.exit(-1);
        }

        // start a server
        Server server = new Server(arguments);
    }
}
