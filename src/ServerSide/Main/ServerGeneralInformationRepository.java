package ServerSide.Main;

import static java.lang.System.out;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.Register;
import ServerSide.Objects.GeneralInformationRepository;

/**
 *    Instantiation and registering of the general information repository object.
 *
 *    Implementation of a client-server model of type 2 (server replication).
 *    Communication is based on Java RMI.
 */
public class ServerGeneralInformationRepository {
    
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

        /* instantiate a general repository object */
        GeneralInformationRepository gir = new GeneralInformationRepository();
        InterfaceGeneralInformationRepository girStub = null;

        try
        { 
            girStub = (InterfaceGeneralInformationRepository) UnicastRemoteObject.exportObject(gir, portNumb);
        }
        catch (RemoteException e)
        { 
            out.println("General Repository stub generation exception: " + e.getMessage ());
            e.printStackTrace ();
            System.exit (1);
        }
        out.println("General Repository Stub was generated!");

        /* register it with the general registry service */

        String nameEntryBase = "RegisterHandler";
        String nameEntryObject = "GeneralRepository";
        Registry registry = null;
        Register reg = null;

        try {
            registry = LocateRegistry.getRegistry(rmiRegHostName, rmiRegPortNumb);
        } catch (RemoteException e) {
            System.out.println("RMI registry creation exception: " + e.getMessage());
            System.exit(1);
        }
        System.out.println("RMI registry was created!");

        try {
            reg = (Register) registry.lookup(nameEntryBase);
        } catch (RemoteException e) {
            System.out.println("RegisterRemoteObject lookup exception: " + e.getMessage());
            System.exit(1);
        } catch (NotBoundException e) {
            System.out.println("RegisterRemoteObject not bound exception: " + e.getMessage());
            System.exit(1);
        }

        try {
            reg.bind(nameEntryObject, girStub);
        } catch (RemoteException e) {
            System.out.println("General Repository registration exception: " + e.getMessage());
            System.exit(1);
        } catch (AlreadyBoundException e) {
            System.out.println("General Repository already bound exception: " + e.getMessage());
            System.exit(1);
        }

        System.out.println("General Repository object was registered!");
    }
}
