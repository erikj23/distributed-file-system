/**
 * Emir Dzaferovic, Erik Malnado
 * CSS 434
 * Final Program
 */
import java.io.*;
import java.net.PortUnreachableException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

public class FileServer
extends UnicastRemoteObject
implements ServerInterface
{
    private static final long serialVersionUID = 5029542218734280636L;

    private Vector<File> files = null;
    private int port = 0;

    public FileServer(int port) throws RemoteException {
        this.port = port;
        this.files = new Vector<>();
    }
    public static void main(String[] args) {
        try {
            if (args.length != 1)
            {
                System.out.println("java server port");
                System.exit(-1);
            }
            int port = Integer.parseInt(args[0]);
            StartRegistry(port);
            FileServer server_object = new FileServer(port);
            Naming.rebind("rmi://localhost:" + port + "/fileserver", server_object);
            System.out.println("Server bootup...");
        }
        catch (Exception error) {
            error.printStackTrace();
        }
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

    public FileContents download(String client, String filename, String mode) throws RemoteException 
    {
        System.out.printf("Download[%s@%s]\n", client, mode);
        File file = null;
        byte[] fileContent = null;
        for (File f : files) 
        {
            if (f.filename.equals(filename)) { file = f; }
        }
        if (file == null) 
        {
            file = new File(filename, this.port);
            this.files.add(file);
        }
        return file.download(client, mode);
    }

    public boolean upload(String client, String filename, FileContents contents) throws RemoteException 
    {
        System.out.printf("Upload[%s]\n", client);
        File file = null;
        for (File f : files) 
        {
            if (filename.equals(f.filename)) 
            {
                file = f;
                break;
            }
        }
        return (file != null) && file.upload(client, contents);
    }

    enum State { NOT_SHARED, READ_SHARED, WRITE_SHARED, OWNERSHIP_CHANGE }

    private class File 
    {
        private State state;
        private String filename;
        private byte[] bytes = null;
        private Vector<String> active_readers = null;
        private String owner = null;
        private int port = 0;

        private Object write_lock = null;
        private Object contention_lock = null;

        public File(String file, int portNum) 
        {
            write_lock = new Object();
            contention_lock = new Object();
            state = State.NOT_SHARED;
            filename = file;
            active_readers = new Vector<String>();
            owner = null;
            port = portNum;
            bytes = readFile();
        }

        private byte[] readFile() 
        {
            byte[] bytes = null;
            try 
            {
                FileInputStream file = new FileInputStream(filename);
                bytes = new byte[file.available()];
                file.read(bytes);
                file.close();
            } catch (Exception e) { return null; } 
            return bytes;
        }

        private boolean writeFile() 
        {
            try 
            {
                FileOutputStream file = new FileOutputStream(filename);
                file.write(bytes);
                file.flush();
                file.close();
            } catch (Exception error) { return false; }
            return true;
        }

        private void removeReader(String client)  { active_readers.remove(client); }

        public FileContents download(String client, String mode) 
        {
            try 
            {
                synchronized (write_lock) 
                { if(state == State.OWNERSHIP_CHANGE) { write_lock.wait(); } }
                
                State previousState = state;
                switch (state) 
                {
                    case NOT_SHARED:
                        if (mode.equals("r")) 
                        {
                            state = State.READ_SHARED;
                            active_readers.add(client);
                        } else if (mode.equals("w")) 
                        {
                            state = State.WRITE_SHARED;
                            if (owner != null)
                                throw new Exception();
                            else
                                owner = client;
                        }
                        break;
                    case READ_SHARED:
                        removeReader(client);
                        if (mode.equals("r")) 
                        {
                            active_readers.add(client);
                        }
                        else if (mode.equals("w")) 
                        {
                            state = State.WRITE_SHARED;
                            if (owner != null)
                                throw new Exception();
                            else
                                owner = client;
                        }
                        break;
                    case WRITE_SHARED:
                        removeReader(client);
                        if (mode.equals("r")) 
                        {
                            active_readers.add(client);
                        }
                        else if (mode.equals("w")) 
                        {
                            state = State.OWNERSHIP_CHANGE;
                            ClientInterface currentOwner = (ClientInterface) Naming.lookup("rmi://" + owner + ":" + port + "/fileclient");
                            currentOwner.writeback();
                            synchronized (contention_lock) { contention_lock.wait(); }
                            owner = client;
                        }
                        break;
                }

                FileContents contents = new FileContents(bytes);
                if (previousState == State.WRITE_SHARED) { synchronized (write_lock) { write_lock.notify(); } }
                return contents;
            } catch (Exception error) { return null; }
        }

        public boolean upload(String client, FileContents contents) {
            System.out.printf("Upload[%s]\n", client);
            try 
            {
                ClientInterface clientInterface = null;
                for (String reader : active_readers) 
                {
                    clientInterface = (ClientInterface) Naming.lookup("rmi://" + reader + ":" + port + "/fileclient");
                    if (clientInterface != null) { clientInterface.invalidate(); }
                }
                active_readers.removeAllElements();
                State prev_state = state;
                bytes = contents.get();
                switch (state) 
                {
                    case WRITE_SHARED:
                        state = State.NOT_SHARED;
                        owner = null;
                        writeFile();
                        break;
                    case OWNERSHIP_CHANGE:
                        state = State.WRITE_SHARED;
                        owner = client;
                        synchronized (contention_lock) { contention_lock.notify(); }
                        break;
                }
                return true;
            } catch (Exception error) { return false; }
        }
    }
}
