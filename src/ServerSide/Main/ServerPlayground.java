package ServerSide.Main;

import static java.lang.System.out;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfacePlayground;
import Interfaces.Register;
import ServerSide.Objects.Playground;

/**
 *    Instantiation and registering of the playground object.
 *
 *    Implementation of a client-server model of type 2 (server replication).
 *    Communication is based on Java RMI.
 */
public class ServerPlayground {
    
    /**
    *  Main method.
    *
    *        args[0] - port number for listening to service requests
    *        args[1] - name of the platform where is located the RMI registering service
    *        args[2] - port number where the registering service is listening to service requests
    */
    public static void main (String[] args) {

        int portNumb = -1;                                             // port number for listening to service requests
        String rmiRegHostName;                                         // name of the platform where is located the RMI registering service
        int rmiRegPortNumb = -1;                                       // port number where the registering service is listening to service requests

        if (args.length != 3)
        { 
            out.println("Wrong number of parameters!"); 
            System.exit (1);
        }
        try
        { 
            portNumb = Integer.parseInt (args[0]);
        }
        catch (NumberFormatException e)
        { 
            out.println("args[0] is not a number!");
            System.exit (1);
        }
        if ((portNumb < 4000) || (portNumb >= 65536))
           { 
                out.println("args[0] is not a valid port number!");
                System.exit (1);
           }
        rmiRegHostName = args[1];
        try
        { 
            rmiRegPortNumb = Integer.parseInt (args[2]);
        }
        catch (NumberFormatException e)
        { 
            out.println("args[2] is not a number!");
            System.exit (1);
        }
        if ((rmiRegPortNumb < 4000) || (rmiRegPortNumb >= 65536))
           { 
                out.println("args[2] is not a valid port number!");
                System.exit (1);
           }

        /* create and install the security manager */

        //if (System.getSecurityManager () == null)
        //    System.setSecurityManager (new SecurityManager ());
        out.println("Security manager was installed!");

        /* get a remote reference to the general repository object */
        
        String nameEntryGeneralRepos = "GeneralRepository";
        InterfaceGeneralInformationRepository girStub = null;
        Registry registry = null;

        try
        { 
            registry = LocateRegistry.getRegistry (rmiRegHostName, rmiRegPortNumb);
        }
        catch (RemoteException e)
        { 
            out.println("RMI registry creation exception: " + e.getMessage ());
            e.printStackTrace ();
            System.exit (1);
        }
        out.println("RMI registry was created!");

        try {
            registry = LocateRegistry.getRegistry(rmiRegHostName, rmiRegPortNumb);
            girStub = (InterfaceGeneralInformationRepository) registry.lookup(nameEntryGeneralRepos);
        } catch (RemoteException e) {
            System.out.println("Excepção na localização do General Information Repository: " + e.getMessage() + "!");
            e.printStackTrace();
            System.exit(1);
        } catch (NotBoundException e) {
            System.out.println("O General Information Repository não está registado: " + e.getMessage() + "!");
            e.printStackTrace();
            System.exit(1);
        }

        /* instantiate a playground object */

        Playground playground = null;
        InterfacePlayground playgroundStub = null;

        playground = new Playground(girStub);
        
        try {
            playgroundStub = (InterfacePlayground) UnicastRemoteObject.exportObject(playground, portNumb);
        } catch (RemoteException e) {
            System.out.println("Playground stub generation exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Playground stub was generated!");

        /* register it with the general registry service */

        String nameEntryBase = "RegisterHandler";
        String nameEntryObject = "Playground";
        Register reg = null;

        try {
            reg = (Register) registry.lookup(nameEntryBase);
        } catch (RemoteException e) {
            System.out.println("RegisterRemoteObject lookup exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (NotBoundException e) {
            System.out.println("RegisterRemoteObject not bound exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        try {
            reg.bind(nameEntryObject, playgroundStub);
        } catch (RemoteException e) {
            System.out.println("Playground registration exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (AlreadyBoundException e) {
            System.out.println("Playground already bound exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        out.println("Playground object was registered!");
    }
}
