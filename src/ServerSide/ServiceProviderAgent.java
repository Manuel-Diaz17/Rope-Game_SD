package ServerSide;

import Communication.Message;
import Communication.MessageException;
import Interfaces.InterfaceCoach;
import Interfaces.InterfaceContestant;
import Interfaces.InterfaceReferee;
import ServerSide.SharedRegions.InterfaceServer;

import static java.lang.System.out;
import java.util.logging.Level;
import java.util.logging.Logger;

import ClientSide.Entities.Contestant;

/**
 * This class implements all Coach, Contestant and Referee interfaces. The
 * purpose is to serve the incoming messages and forward to the right passive
 * class implementation of the InterfaceServer.
 */
public class ServiceProviderAgent extends Thread implements InterfaceCoach,
        InterfaceContestant,
        InterfaceReferee,
        Comparable<InterfaceContestant> {

    private final ServerCom sconi;
    private final InterfaceServer servInterface;

    private static int serviceProviderAgentId = 0;  // spa initialisation counter

    // referee personalization
    private Enum state;

    // coach personalization
    private int team;

    // contestant personalization
    private int contestantId;
    private int strength;

    /**
     * Initialisation of the server interface
     *
     * @param sconi connection accepted by the main server
     * @param servInterface server interface to be provided
     */
    ServiceProviderAgent(ServerCom sconi,
            InterfaceServer servInterface) {

        super(Integer.toString(serviceProviderAgentId++));
        this.sconi = sconi;
        this.servInterface = servInterface;
        this.state = null;
        this.team = 0;
        this.contestantId = 0;
        this.strength = 0;
    }

    @Override
    public void run() {
        Message inMessage, outMessage = null;

        Thread.currentThread().setName("SPA-" + Integer.toString(serviceProviderAgentId));

        inMessage = (Message) sconi.readObject();

        out.println(Thread.currentThread().getName() + ": " + inMessage.getType());

        if (inMessage.getType() == Message.MessageType.SHUTDOWN) {
            boolean shutdown = servInterface.goingToShutdown();

            outMessage = new Message(Message.MessageType.OK);

            sconi.writeObject(outMessage);
            sconi.close();

            if (shutdown) {
                System.exit(0);
            }
        } else {
            // TODO: validate message
            this.state = inMessage.getState();
            this.team = inMessage.getTeam();
            this.contestantId = inMessage.getContestantId();
            this.strength = inMessage.getStrength();

            try {
                outMessage = servInterface.processAndReply(inMessage);
            } catch (MessageException ex) {
                Logger.getLogger(ServiceProviderAgent.class.getName()).log(Level.SEVERE, null, ex);
            }

            sconi.writeObject(outMessage);
            sconi.close();
        }

    }

    // Coach methods
    @Override
    public CoachState getCoachState() {
        return (CoachState) state;
    }

    @Override
    public void setCoachState(CoachState state) {
        this.state = state;
    }

    @Override
    public int getTeam() {
        return team;
    }

    @Override
    public void setTeam(int team) {
        this.team = team;
    }

    // Contestant methods
    @Override
    public int getContestantId() {
        return contestantId;
    }

    @Override
    public void setContestantId(int id) {
        contestantId = id;
    }

    @Override
    public ContestantState getContestantState() {
        return (ContestantState) state;
    }

    @Override
    public void setContestantState(Contestant.ContestantState state) {
        this.state = state;
    }

    @Override
    public int getStrength() {
        return strength;
    }

    @Override
    public void setStrength(int strength) {
        this.strength = strength;
    }

    // Referee methods
    @Override
    public RefereeState getRefereeState() {
        return (RefereeState) state;
    }

    @Override
    public void setRefereeState(RefereeState state) {
        this.state = state;
    }

    @Override
    public int compareTo(InterfaceContestant contestant) {
        return getContestantId() - contestant.getContestantId();
    }

}
