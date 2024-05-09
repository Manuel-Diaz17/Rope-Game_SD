package ServerSide.SharedRegions;

import Communication.Message;
import static Communication.Message.MessageType.*;
import Communication.MessageException;
import Interfaces.InterfaceReferee;

/**
 * Interface server implementation for contestants bench access
 */
public class RefereeSiteInterface implements InterfaceServer {

    private final RefereeSite refereeSite;

    /**
     * Constructor that initiates the interface
     */
    public RefereeSiteInterface() {
        this.refereeSite = RefereeSite.getInstance();
    }

    @Override
    public Message processAndReply(Message inMessage) throws MessageException {
        Message outMessage;

        switch (inMessage.getType()) {
            case RS_ADD_GAME_POINT: {
                refereeSite.addGamePoint(inMessage.getGamePoint());
                outMessage = new Message(OK);
                break;
            }
            case RS_ADD_TRIAL_POINT: {
                refereeSite.addTrialPoint(inMessage.getTrialPoint());
                outMessage = new Message(OK);
                break;
            }
            case RS_BOTH_TEAMS_READY: {
                InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();
                refereeSite.bothTeamsReady();
                outMessage = new Message(REFEREE_STATE_CHANGE);
                outMessage.setRefereeState(referee.getRefereeState());
                break;
            }
            case RS_GET_GAME_POINTS: {
                outMessage = new Message(GAME_POINTS);
                outMessage.setGamePoints(refereeSite.getGamePoints());
                break;
            }
            case RS_GET_REMAINING_GAMES: {
                outMessage = new Message(REMAINING_GAMES);
                outMessage.setRemainingGames(refereeSite.getRemainingGames());
                break;
            }
            case RS_GET_REMAINING_TRIALS: {
                outMessage = new Message(REMAINING_TRIALS);
                outMessage.setRemainingTrials(refereeSite.getRemainingTrials());
                break;
            }
            case RS_GET_TRIAL_POINTS: {
                outMessage = new Message(TRIAL_POINTS);
                outMessage.setTrialPoints(refereeSite.getTrialPoints());
                break;
            }
            case RS_HAS_MATCH_ENDED: {
                outMessage = new Message(BOOLEAN);
                outMessage.setHasMatchEnded(refereeSite.isMatchEnded());
                break;
            }
            case RS_INFORM_REFEREE: {
                refereeSite.informReferee();
                outMessage = new Message(OK);
                break;
            }
            case RS_RESET_TRIAL_POINTS: {
                refereeSite.resetTrialPoints();
                outMessage = new Message(OK);
                break;
            }
            case RS_SET_HAS_MATCH_ENDED: {
                refereeSite.setIsMatchEnded(inMessage.getHasMatchEnded());
                outMessage = new Message(OK);
                break;
            }
            default:
                throw new MessageException("Method in RS not found", inMessage);

        }

        return outMessage;
    }

    @Override
    public boolean goingToShutdown() {
        return refereeSite.shutdown();
    }
}
