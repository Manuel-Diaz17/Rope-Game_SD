package ClientSide.Stubs;

import Communication.Message;
import Interfaces.InterfaceCoach;
import Interfaces.InterfaceContestant;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfaceReferee;
import ServerSide.SharedRegions.RefereeSite;
import Game.ServerConfigs;

import static java.lang.System.out;

import ClientSide.ClientCom;

/**
 * This is an passive class that describes the General Information Repository.
 * This class connects to a server and messages the according invocation.
 */
public class GeneralInformationRepositoryStub implements InterfaceGeneralInformationRepository {

    /**
     * Initiates the connection to the Server according to the server
     * configuration
     *
     * @return ClientCom with the opened connection
     */
    private ClientCom initiateConnection() {
        ClientCom con = new ClientCom(ServerConfigs.GENERAL_INFORMATION_REPOSITORY_ADDRESS,
                ServerConfigs.GENERAL_INFORMATION_REPOSITORY_PORT);

        if (!con.open()) {
            out.println("Couldn't initiate connection to "
                    + ServerConfigs.GENERAL_INFORMATION_REPOSITORY_ADDRESS + ":"
                    + ServerConfigs.GENERAL_INFORMATION_REPOSITORY_PORT);
        }

        return con;
    }

    @Override
    public void updateCoach() {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_UPDATE_COACH,
                coach.getCoachState(),
                coach.getCoachTeam());

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void updateContestant() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_UPDATE_CONTESTANT,
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

        con.close();
    }

    @Override
    public void updateContestantStrength(int team, int id, int strength) {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_UPDATE_CONTESTANT_STRENGTH,
                null,
                team,
                id,
                strength);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void updateReferee() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_UPDATE_REFEREE,
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
    public void close() {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_CLOSE);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void printGameHeader() {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_PRINT_GAME_HEADER);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void printGameResult(RefereeSite.GameScore score) {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_PRINT_GAME_RESULT);

        outMessage.setGameResult(score);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void printHeader() {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_PRINT_HEADER);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void printLegend() {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_PRINT_LEGEND);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void printLineUpdate() {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_PRINT_LINE_UPDATE);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void printMatchDraw() {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_PRINT_MATCH_DRAW);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void printMatchWinner(int team, int score1, int score2) {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_PRINT_MATCH_WINNER);

        int numbers[] = {team, score1, score2};

        outMessage.setNumbers(numbers);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void resetTeamPlacement() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_RESET_TEAM_PLACEMENT,
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

        con.close();
    }

    @Override
    public void setFlagPosition(int flagPosition) {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_SET_FLAG_POSITION);

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
    public void setGameNumber(int gameNumber) {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_SET_GAME_NUMBER);

        outMessage.setGameNumber(gameNumber);

        con.writeObject(outMessage);

        inMessage = (Message) con.readObject();

        if (inMessage.getType() != Message.MessageType.OK) {
            out.println("Returned message with wrong type");
            System.exit(1);
        }

        con.close();
    }

    @Override
    public void setTeamPlacement() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_SET_TEAM_PLACEMENT,
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

        con.close();
    }

    @Override
    public void setTrialNumber(int trialNumber) {
        ClientCom con = initiateConnection();

        Message inMessage, outMessage;

        outMessage = new Message(Message.MessageType.GIR_SET_TRIAL_NUMBER);

        outMessage.setTrialNumber(trialNumber);

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
