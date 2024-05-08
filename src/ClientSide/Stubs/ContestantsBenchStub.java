package ClientSide.Stubs;

import Communication.Message;
import static Communication.Message.MessageType.COACH_STATE_CHANGE;
import Interfaces.InterfaceCoach;
import Interfaces.InterfaceContestant;
import Interfaces.InterfaceContestantsBench;
import Interfaces.InterfaceReferee;
import Interfaces.Tuple;
import Game.ServerConfigs;
import static java.lang.System.out;
import java.util.Set;

import ClientSide.ClientCom;

/**
 * This is an passive class that describes the ContestantsBench. This class
 * connects to a server and messages the according invocation.
 */
public class ContestantsBenchStub implements InterfaceContestantsBench {

    private final int team; // team number of this Stub

    /**
     * Private constructor to be used in the doubleton
     *
     * @param team identifier
     */
    public ContestantsBenchStub(int team) {
        this.team = team;
    }

    /**
     * Initiates the connection to the Server according to the server
     * configuration
     *
     * @return ClientCom with the opened connection
     */
    private ClientCom initiateConnection() {
        ClientCom con = new ClientCom(ServerConfigs.CONTESTANTS_BENCH_ADDRESS,
                ServerConfigs.CONTESTANTS_BENCH_PORT);

        if (!con.open()) {
            out.println("Couldn't initiate connection to "
                    + ServerConfigs.CONTESTANTS_BENCH_ADDRESS + ":"
                    + ServerConfigs.CONTESTANTS_BENCH_PORT);
        }

        return con;
    }

    @Override
    public void addContestant() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.CB_ADD_CONTESTANT,
                contestant.getContestantState(),
                contestant.getContestantTeam(),
                contestant.getContestantId(),
                contestant.getStrength());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.CONTESTANT_STATE_CHANGE) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        contestant.setContestantState(inMessage.getContestantState());
        contestant.setStrength(inMessage.getStrength());

        con.close();
    }

    @Override
    public Set<Tuple<Integer, Integer>> getBench() {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.CB_GET_BENCH,
                coach.getCoachState(),
                coach.getCoachTeam());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.BENCH) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        return inMessage.getBench();
    }

    @Override
    public void getContestant() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.CB_GET_CONTESTANT,
                contestant.getContestantState(),
                contestant.getContestantTeam(),
                contestant.getContestantId(),
                contestant.getStrength());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }
    }

    @Override
    public Set<Integer> getSelectedContestants() {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.CB_GET_SELECTED_CONTESTANTS,
                coach.getCoachState(),
                coach.getCoachTeam());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.SELECTED_CONTESTANTS) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        return inMessage.getSelectedContestants();
    }

    @Override
    public void pickYourTeam() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.CB_PICK_YOUR_TEAM,
                referee.getRefereeState());

        outMessage.setTeam(team);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void setSelectedContestants(Set<Integer> selected) {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.CB_SET_SELECTED_CONTESTANTS,
                coach.getCoachState(),
                coach.getCoachTeam());

        outMessage.setSelectedContestants(selected);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();

    }

    @Override
    public void waitForNextTrial() {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.CB_WAIT_FOR_NEXT_TRIAL,
                coach.getCoachState(),
                coach.getCoachTeam());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != COACH_STATE_CHANGE) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        coach.setCoachState(inMessage.getCoachState());
    }

    @Override
    public void updateContestantStrength(int id, int delta) {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.CB_UPDATE_CONTESTANT_STRENGTH,
                coach.getCoachState(),
                coach.getCoachTeam());

        outMessage.setNumbers(new int[]{id, delta});

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        coach.setCoachState(inMessage.getCoachState());

        con.close();
    }

    @Override
    public void interrupt() {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.INTERRUPT);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public boolean shutdown() {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.SHUTDOWN);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();

        return false;
    }

    @Override
    public void waitForEveryoneToStart() {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.WAIT_FOR_EVERYONE_TO_START);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }
}
