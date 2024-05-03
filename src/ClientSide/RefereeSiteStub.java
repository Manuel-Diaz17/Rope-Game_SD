package ClientSide;

import Communication.Message;
import Interfaces.InterfaceCoach;
import Interfaces.InterfaceReferee;
import Interfaces.InterfaceRefereeSite;
import Game.ServerConfigs;
import ServerSide.RefereeSite;
import static java.lang.System.out;
import java.util.List;

/**
 * This is an passive class that describes the RefereeSite. This class connects
 * to a server and messages the according invocation.
 */
public class RefereeSiteStub implements InterfaceRefereeSite {

    /**
     * Initiates the connection to the Server according to the server
     * configuration
     *
     * @return ClientCom with the opened connection
     */
    private ClientCom initiateConnection() {
        ClientCom con = new ClientCom(ServerConfigs.REFEREE_SITE_ADDRESS,
                ServerConfigs.REFEREE_SITE_PORT);

        if (!con.open()) {
            out.println("Couldn't initiate connection to "
                    + ServerConfigs.REFEREE_SITE_ADDRESS + ":"
                    + ServerConfigs.REFEREE_SITE_PORT);
        }

        return con;
    }

    @Override
    public void addGamePoint(RefereeSite.GameScore score) {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.RS_ADD_GAME_POINT,
                referee.getRefereeState());

        outMessage.setGamePoint(score);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void addTrialPoint(RefereeSite.TrialScore score) {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.RS_ADD_TRIAL_POINT,
                referee.getRefereeState());

        outMessage.setTrialScore(score);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void bothTeamsReady() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.RS_BOTH_TEAMS_READY,
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
    public List<RefereeSite.GameScore> getGamePoints() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.RS_GET_GAME_POINTS,
                referee.getRefereeState());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.GAME_POINTS) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();

        List gamePoints = inMessage.getGamePoints();

        return gamePoints;
    }

    @Override
    public int getRemainingGames() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.RS_GET_REMAINING_GAMES,
                referee.getRefereeState());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.REMAINING_GAMES) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();

        return inMessage.getRemainingGames();
    }

    @Override
    public int getRemainingTrials() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.RS_GET_REMAINING_TRIALS,
                referee.getRefereeState());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.REMAINING_TRIALS) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();

        return inMessage.getRemainingtTrials();
    }

    @Override
    public List<RefereeSite.TrialScore> getTrialPoints() {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.RS_GET_TRIAL_POINTS);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.TRIAL_POINTS) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();

        List<RefereeSite.TrialScore> trialPoints = inMessage.getTrialPoints();

        return trialPoints;

    }

    @Override
    public boolean isMatchEnded() {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.RS_HAS_MATCH_ENDED);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.BOOLEAN) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();

        return inMessage.getHasMatchEnded();
    }

    @Override
    public void informReferee() {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.RS_INFORM_REFEREE,
                coach.getCoachState(),
                coach.getTeam());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void resetTrialPoints() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.RS_RESET_TRIAL_POINTS,
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
    public void setIsMatchEnded(boolean hasMatchEnded) {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.RS_SET_HAS_MATCH_ENDED,
                referee.getRefereeState());

        outMessage.setHasMatchEnded(hasMatchEnded);

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
}
