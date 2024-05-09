package ClientSide.Stubs;

import Communication.Message;
import Game.ServerConfigs;
import Interfaces.InterfaceCoach;
import Interfaces.InterfaceContestant;
import Interfaces.InterfacePlayground;
import Interfaces.InterfaceReferee;

import static java.lang.System.out;
import java.util.List;

import ClientSide.ClientCom;

/**
 * This is an passive class that describes the Playground. This class connects
 * to a server and messages the according invocation.
 */
public class PlaygroundStub implements InterfacePlayground {

    /**
     * Initiates the connection to the Server according to the server
     * configuration
     *
     * @return ClientCom with the opened connection
     */
    private ClientCom initiateConnection() {
        ClientCom con = new ClientCom(ServerConfigs.PLAYGROUND_ADDRESS,
                ServerConfigs.PLAYGROUND_PORT);

        if (!con.open()) {
            out.println("Couldn't initiate connection to "
                    + ServerConfigs.PLAYGROUND_ADDRESS + ":"
                    + ServerConfigs.PLAYGROUND_PORT);
        }

        return con;
    }

    @Override
    public void addContestant() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.PG_ADD_CONTESTANT,
                contestant.getContestantState(),
                contestant.getContestantTeam(),
                contestant.getContestantId(),
                contestant.getContestantStrength());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.CONTESTANT_STATE_CHANGE) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        contestant.setContestantState(inMessage.getContestantState());

        con.close();

    }

    @Override
    public boolean areAllContestantsReady() {
        //TODO: not used? delete from interface, etc
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkTeamPlacement() {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        // coach team
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.PG_CHECK_TEAM_PLACEMENT,
                coach.getCoachState(),
                coach.getCoachTeam());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.COACH_STATE_CHANGE) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        coach.setCoachState(inMessage.getCoachState());

        con.close();
    }

    @Override
    public void getContestant() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.PG_GET_CONTESTANT,
                contestant.getContestantState(),
                contestant.getContestantTeam(),
                contestant.getContestantId(),
                contestant.getContestantStrength());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

    }

    @Override
    public int getFlagPosition() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.PG_GET_FLAG_POSITION,
                referee.getRefereeState());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.FLAG_POSITION) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();

        return inMessage.getFlagPostion();
    }

    @Override
    public int getLastFlagPosition() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.PG_GET_LAST_FLAG_POSITION,
                referee.getRefereeState());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.LAST_FLAG_POSITION) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();

        return inMessage.getLastFlagPostion();
    }

    @Override
    public List<InterfaceContestant>[] getTeams() {
        // unused function?
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void allHavePulled() {
        // unused function?
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pullRope() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.PG_PULL_ROPE,
                contestant.getContestantState(),
                contestant.getContestantTeam(),
                contestant.getContestantId(),
                contestant.getContestantStrength());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.print("Message type error");
        }
    }

    @Override
    public void resultAsserted() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.PG_RESULT_ASSERTED,
                referee.getRefereeState());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void setFlagPosition(int flagPosition) {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.PG_SET_FLAG_POSITION,
                referee.getRefereeState());

        outMessage.setFlagPosition(flagPosition);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void startPulling() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        // Referee state changes
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.PG_START_PULLING,
                referee.getRefereeState());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.REFEREE_STATE_CHANGE) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        referee.setRefereeState(inMessage.getRefereeState());

        con.close();
    }

    @Override
    public void watchTrial() {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        // coach team
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.PG_WATCH_TRIAL,
                coach.getCoachState(),
                coach.getCoachTeam());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.COACH_STATE_CHANGE) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();

        coach.setCoachState(inMessage.getCoachState());
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
}
