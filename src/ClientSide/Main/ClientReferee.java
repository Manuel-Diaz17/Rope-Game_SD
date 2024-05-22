package ClientSide.Main;

import static java.lang.System.out;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

import ClientSide.Entities.Referee;
import Interfaces.InterfaceContestantsBench;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfacePlayground;
import Interfaces.InterfaceRefereeSite;

/**
 *    Client side of the Rope Game (referee).
 *
 *    Implementation of a client-server model of type 2 (server replication).
 *    Communication is based on Java RMI.
 */
public class ClientReferee {
    
    /**
    *  Main method.
    *
    *    @param args runtime arguments
    *        args[0] - name of the platform where is located the RMI registering service
    *        args[1] - port number where the registering service is listening to service requests
    */
    public static void main (String [] args) throws RemoteException {

        String rmiRegHostName;                                         // name of the platform where is located the RMI registering service
        int rmiRegPortNumb = -1;                                       // port number where the registering service is listening to service requests

        /* getting problem runtime parameters */

        if (args.length != 2)
        { 
            out.println("Wrong number of parameters!");
            System.exit (1);
        }
        rmiRegHostName = args[0];
        try
        { 
            rmiRegPortNumb = Integer.parseInt (args[1]);
        }
        catch (NumberFormatException e)
        { 
            out.println("args[1] is not a number!");
            System.exit (1);
        }
        if ((rmiRegPortNumb < 4000) || (rmiRegPortNumb >= 65536))
        { 
            out.println("args[1] is not a valid port number!");
            System.exit (1);
        }

        /* problem initialization */
        InterfaceGeneralInformationRepository girStub = null;
        InterfacePlayground playgroundStub = null;
        InterfaceContestantsBench benchStub = null;
        InterfaceRefereeSite refsiteStub = null;
        Registry registry = null;

        try {
            registry = LocateRegistry.getRegistry(rmiRegHostName, rmiRegPortNumb);
        } catch (RemoteException e) {
            out.println("RMI registry creation exception: " + e.getMessage ());
            e.printStackTrace ();
            System.exit (1);
        }

        try {
            girStub = (InterfaceGeneralInformationRepository) registry.lookup(rmiRegHostName);
            playgroundStub = (InterfacePlayground) registry.lookup(rmiRegHostName);
            benchStub = (InterfaceContestantsBench) registry.lookup(rmiRegHostName);
            refsiteStub = (InterfaceRefereeSite) registry.lookup(rmiRegHostName);
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(ClientReferee.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        Referee referee = new Referee("Referee", benchStub, playgroundStub, refsiteStub, girStub);

        /* start of the simulation */

        out.println("Referee started");

        referee.start ();

        try {
            referee.join();
        } catch (InterruptedException e) {}
        out.println("The referee has terminated.");

        girStub.shutdown();
        playgroundStub.shutdown();
        benchStub.shutdown();
        refsiteStub.shutdown();

    }
}
