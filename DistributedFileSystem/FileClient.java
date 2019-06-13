/**
 * Emir Dzaferovic, Erik Maldonado
 * CSS 434 
 * Final Program 4
 */
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class FileClient
extends UnicastRemoteObject
implements ClientInterface
{
    private static final long serialVersionUID = 1126934848607337921L;

    private static final String CACHE_PATH = "/tmp/%s";
    private static final String BIND_SERVER = "rmi://%s:%d/fileserver";
    private static final String BIND_CLIENT = "rmi://localhost:%d/fileclient";
    private static final String PROGRAM = "emacs -nw %s %s";

    private File file;
    private ServerInterface server_object;
    private String local_host;
    private String cache_file;
    private String file_name;
    private String access_mode;
    private State state;

    public FileClient(ServerInterface server_object, String local_host) 
    throws Exception 
    {
        
        this.cache_file = String.format(CACHE_PATH, 
            System.getProperty("user.name"));
        this.file = new File(this.cache_file);

        if (!this.file.exists()) 
        {
            this.file.createNewFile();
            this.file.setWritable(true, true); 
        }

        this.server_object = server_object;
        this.local_host = local_host;
        this.state = State.INVALID;
        this.file_name = "";
    }

    public static void main(String[] args) 
    {
        int port = 0;
        String local_host = "";
        try 
        {
            if (args.length == 2) 
            {
                port = Integer.parseInt(args[1]);
            } 
            else throw new Exception();

            local_host = InetAddress.getLocalHost().getHostName();
        } 
        catch(Exception e)
        { 
            System.exit(-1);
        }
        
        String server_address = args[0];

        try 
        {
            ServerInterface server_object = (ServerInterface)Naming.lookup(String.format(BIND_SERVER, server_address, port));
            StartRegistry(port);
            FileClient client = new FileClient(server_object, local_host);
            Naming.rebind(String.format(BIND_CLIENT, port), client);
            client.Prompt(); 
        } 
        catch (Exception e) { e.printStackTrace(); }
    }

    private static void StartRegistry(int port) throws RemoteException
    {
        try 
        {
            Registry registry = LocateRegistry.getRegistry(port);
            registry.list();
        } 
        catch (RemoteException e)
        {
            Registry registry = LocateRegistry.createRegistry(port);
        }
    }

    public void Prompt() throws Exception 
    {
        Scanner input = new Scanner(System.in);
        String file_name;
        String mode;

        while (true)
        {
            System.out.print("File Name: ");
            file_name = input.nextLine();
            System.out.print("Mode [r/w]: ");
            mode = input.nextLine();
            if (!this.Open(file_name, mode)) continue;
            RunEmacs();
            completeSession();
            System.out.println("Quit (extra credit) [y:n]");
            if (input.nextLine().toLowerCase().startsWith("n")) 
            {
                if (this.state == State.WRITE_OWNED) this.SaveState();
                System.exit(0);
            }
        }
    }

    private void SaveState() throws Exception 
    {
        FileContents currentContent = new FileContents(Files.readAllBytes(this.file.toPath()));
        this.server_object.upload(this.local_host, this.file_name, currentContent);
    }

    private boolean Open(String file_name, String mode) 
    {
        try 
        {
            if (!this.file_name.equals(file_name)) 
            {
                if (this.state == State.WRITE_OWNED) 
                {
                    FileContents currentContent = new FileContents(Files.readAllBytes(this.file.toPath()));
                    this.server_object.upload( this.local_host, this.file_name, currentContent);
                    this.state = State.INVALID;
                }
            }
            switch (this.state) 
            {
                case INVALID:
                    if (!this.downloadRequestedFile(file_name, mode)) 
                    {
                        return false;
                    }
                    break;

                case READ_SHARED:
                    if (this.file_name.equals(file_name)) 
                    {
                        if (mode.equals("w")) 
                        {
                            if (!this.downloadRequestedFile(file_name, mode)) 
                            {
                                return false;
                            }
                        }
                    } else 
                    {
                        if (!this.downloadRequestedFile(file_name, mode)) 
                        {
                            return false;
                        }
                    }

                    break;

                case WRITE_OWNED:
                    if (!this.file_name.equals(file_name)) 
                    {
                        if (!this.downloadRequestedFile(file_name, mode)) 
                        {
                            return false;
                        }

                    }
                    break;
                default:
                    System.out.println("This should never happen default case!");
                    return false;

            }
        }
        catch(Exception e) { return false; }
        return true;
    }

    private boolean downloadRequestedFile(String file_name, String mode) 
    {
        try 
        {
            FileContents result = this.server_object.download(this.local_host, file_name, mode);
            if (result == null) { return false; }
            // Chmod 600
            this.file.setWritable(true, true);

            FileOutputStream tempFileWriter = new FileOutputStream(this.file);
            tempFileWriter.write(result.get());
            tempFileWriter.close();

            this.file_name = file_name;

            if (mode.equals("w")) 
            {
                this.state = State.WRITE_OWNED; 
                this.access_mode = mode; 
            } else 
            {
                this.file.setReadOnly();
                this.state = State.READ_SHARED; 
                this.access_mode = mode;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return true;
    }

    private void RunEmacs() 
    {
        
        String run = String.format(PROGRAM, this.cache_file, this.state.options);
        System.err.printf("run emacs->[%s]\n", run);

        try 
        {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(run);
            process.waitFor();
        } 
        catch (Exception e) { e.printStackTrace(); }
    }

    private void completeSession() 
    {
        try 
        {
            if (this.state == State.RELEASE_OWNERSHIP) 
            {
                FileContents currentContent = new FileContents(Files.readAllBytes(this.file.toPath()));
                this.server_object.upload( this.local_host, this.file_name, currentContent);
                this.state = State.READ_SHARED;
                // Chmod this to 400 
                this.file.setReadOnly();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public boolean invalidate() throws RemoteException 
    {
        if (this.state == State.READ_SHARED) 
        {
            this.state = State.INVALID;
            return true;
        }
        return false;
    }

    public boolean writeback() throws RemoteException 
    {
        if (this.state == State.WRITE_OWNED) 
        {
            this.state = State.RELEASE_OWNERSHIP;
            return true;
        }
        return false;
    }

    private enum State 
    {
        INVALID(""), READ_SHARED("-f view-mode"), WRITE_OWNED(""), RELEASE_OWNERSHIP("");

        String options;

        State(String options)
        {
            this.options = options;
        }
    }
}
