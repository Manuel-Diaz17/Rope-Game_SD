package ServerSide;

import Game.ServerConfigs;
import ServerSide.SharedRegions.ContestantsBenchInterface;
import ServerSide.SharedRegions.GeneralInformationRepositoryInterface;
import ServerSide.SharedRegions.InterfaceServer;
import ServerSide.SharedRegions.PlaygroundInterface;
import ServerSide.SharedRegions.RefereeSiteInterface;

import static java.lang.System.out;

/**
 * This class implements the Server of RopeGame client-server architecture. It
 * also implements initialization of the passive ententies.
 */
public class ServerRopeGame {

    /**
     * Initializes the Server of RopeGame Client/Server implementation
     * <ul>
     * <li>Contestants Bench: CB
     * <li>Playground: PG
     * <li>Referee site: RS
     * <li>General Information Repository: GR
     * </ul>
     *
     * @param args --help for more info
     */
    public static void main(String[] args) {
        ServerCom scon = null, sconi;
        ServiceProviderAgent sps;
        InterfaceServer servInterface = null;

        if (args.length == 1) {
            switch (args[0]) {
                case "CB":
                    scon = new ServerCom(ServerConfigs.CONTESTANTS_BENCH_PORT);
                    servInterface = new ContestantsBenchInterface();
                    break;
                case "PG":
                    scon = new ServerCom(ServerConfigs.PLAYGROUND_PORT);
                    servInterface = new PlaygroundInterface();
                    break;
                case "RS":
                    scon = new ServerCom(ServerConfigs.REFEREE_SITE_PORT);
                    servInterface = new RefereeSiteInterface();
                    break;
                case "GR":
                    scon = new ServerCom(ServerConfigs.GENERAL_INFORMATION_REPOSITORY_PORT);
                    servInterface = new GeneralInformationRepositoryInterface();
                    break;
                default:
                    break;
            }
        }

        if (servInterface == null) {
            System.out.println("Server didn't start - bad argument - [CB | PG | RS | GR]");
            System.exit(1);
        }

        scon.start();
        out.println("Server " + args[0] +" started and listening for connections...");

        while (true) {
            sconi = scon.accept();
            sps = new ServiceProviderAgent(sconi, servInterface);
            sps.start();
        }
    }

}
