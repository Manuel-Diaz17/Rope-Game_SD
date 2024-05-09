package Game;

/**
 * This class starts the Rope Game. It instantiates all the active and passive
 * entities.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        Thread server, client;

        if (args.length == 1) {
            if (!args[0].contains("RF")) {
                server = new Thread(() -> {
                    ServerSide.ServerRopeGame.main(new String[]{args[0]});
                });
                server.setName("ServerMainThread");
                server.start();
                server.join();
            }
        }

        client = new Thread(() -> {
            ClientSide.ClientRopeGame.main(args);
        });
        client.setName("ClientMainThread");
        client.start();
        client.join();

    }
}
