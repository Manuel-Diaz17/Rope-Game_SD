package ServerSide.Main;

import static java.lang.System.out;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Interfaces.InterfaceContestantsBench;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfaceRefereeSite;
import Interfaces.Register;
import ServerSide.Objects.ContestantsBench;

/**
 *    Instantiation and registering of a contestants bench object.
 *
 *    Implementation of a client-server model of type 2 (server replication).
 *    Communication is based on Java RMI.
 */
 public class ServerContestantsBench {

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

        if (System.getSecurityManager () == null)
            System.setSecurityManager (new SecurityManager ());
        out.println("Security manager was installed!");

        /* get a remote reference to the general repository and referee site objects */

        String nameEntryGeneralRepos = "GeneralRepository";
        String nameEntryRefereeSite = "RefereeSite";
        InterfaceGeneralInformationRepository girStub = null;
        InterfaceRefereeSite refSiteStub = null;
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
            girStub = (InterfaceGeneralInformationRepository) registry.lookup (nameEntryGeneralRepos);
        } catch (RemoteException e) { 
            System.out.println("GeneralRepos lookup exception: " + e.getMessage () + "!");
            e.printStackTrace();
            System.exit (1);
        } catch (NotBoundException e) { 
            System.out.println("GeneralRepos not bound exception: " + e.getMessage () + "!");
            e.printStackTrace();
            System.exit(1);
        }

        try { 
            registry = LocateRegistry.getRegistry(rmiRegHostName, rmiRegPortNumb);
            refSiteStub = (InterfaceRefereeSite) registry.lookup(nameEntryRefereeSite);
        } catch (RemoteException e) { 
            System.out.println("Referee site lookup exception: " + e.getMessage () + "!");
            e.printStackTrace();
            System.exit (1);
        } catch (NotBoundException e) { 
            System.out.println("Referee site not bound exception: " + e.getMessage () + "!");
            e.printStackTrace();
            System.exit(1);
        }

        /* instantiate a contestants bench object */

        ContestantsBench bench = null;
        InterfaceContestantsBench benchStub = null;
        
        bench = new ContestantsBench(refSiteStub, girStub);
        
        try {
            benchStub = (InterfaceContestantsBench) UnicastRemoteObject.exportObject((Remote) bench, portNumb);
        } catch (RemoteException e) {
            System.out.println("Contestants bench stub generation exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Contestants bench stub was generated!");

        /* register it with the general registry service */

        String nameEntryBase = "RegisterHandler";
        String nameEntryObject = "ContestantsBench";
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
            reg.bind(nameEntryObject, benchStub);
        } catch (RemoteException e) {
            System.out.println("Contestants bench registration exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (AlreadyBoundException e) {
            System.out.println("Contestants bench already bound exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        out.println("Contestants bench object was registered!");
    }
 }