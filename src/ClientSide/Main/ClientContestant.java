package ClientSide.Main;

import static java.lang.System.out;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

import ClientSide.Entities.Contestant;
import Interfaces.InterfaceContestantsBench;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfacePlayground;
import Interfaces.InterfaceRefereeSite;

/**
 *    Client side of the Rope Game (contestant).
 *
 *    Implementation of a client-server model of type 2 (server replication).
 *    Communication is based on Java RMI.
 */
public class ClientContestant {
    
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
        Contestant [][] contestant = new Contestant [2][5]; 

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
            Logger.getLogger(ClientContestant.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        for (int i = 0; i < 2; i++){
            for (int j = 0; j < 5; j++){
                int strength = randomStrength();

                contestant[i][j] = new Contestant ("Contestant" + (i+1) + ":" + j+1, i+1, j+1, strength,
                benchStub, playgroundStub, refsiteStub, girStub);
            }
        }
            

        /* start of the simulation */

        for (int i = 0; i < 2; i++){
            for (int j = 0; j < 5; j++){
                out.println("Contestant" + (i+1) + ":" + j+1 + " started.");

                contestant[i][j].start ();
            }
        }

        for (int i = 0; i < 2; i++){
            for (int j = 0; j < 5; j++){
                try {
                    contestant[i][j].join();
                } catch (InterruptedException e) {}
                out.println("The contestant " + (i+1) + ":" + j+1 + " has terminated.");
            }
        }

        girStub.shutdown();
        playgroundStub.shutdown();
        benchStub.shutdown();
        refsiteStub.shutdown();

    }

    /**
     * Function to generate a random strength when a player is instantiated
     *
     * @return a strength for a player instantiation
     */
    private static int randomStrength() {
        return 10 + (int) (Math.random() * (20 - 10));
    }
}
