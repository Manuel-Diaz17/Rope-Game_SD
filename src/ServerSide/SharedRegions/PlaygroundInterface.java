package ServerSide.SharedRegions;

import Communication.Message;
import static Communication.Message.MessageType.*;
import Communication.MessageException;
import Interfaces.InterfaceCoach;
import Interfaces.InterfaceContestant;
import Interfaces.InterfaceReferee;

/**
 * Interface server implementation for contestants bench access
 */
public class PlaygroundInterface implements InterfaceServer {

    private final Playground pg;

    /**
     * Constructor that initiates the interface
     */
    public PlaygroundInterface() {
        this.pg = Playground.getInstance();
    }

    @Override
    public Message processAndReply(Message inMessage) throws MessageException {
        Message outMessage;

        switch (inMessage.getType()) {
            case PG_ADD_CONTESTANT: {
                InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();
                pg.addContestant();
                outMessage = new Message(CONTESTANT_STATE_CHANGE);
                outMessage.setContestantState(contestant.getContestantState());
                break;
            }
            case PG_CHECK_TEAM_PLACEMENT: {
                InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();
                pg.checkTeamPlacement();
                outMessage = new Message(COACH_STATE_CHANGE);
                outMessage.setContestantState(coach.getCoachState());
                break;
            }
            case PG_GET_CONTESTANT: {
                pg.getContestant();
                outMessage = new Message(OK);
                break;
            }
            case PG_GET_FLAG_POSITION: {
                outMessage = new Message(FLAG_POSITION);
                outMessage.setFlagPosition(pg.getFlagPosition());
                break;
            }
            case PG_GET_LAST_FLAG_POSITION: {
                outMessage = new Message(LAST_FLAG_POSITION);
                outMessage.setLastFlagPosition(pg.getLastFlagPosition());
                break;
            }
            case PG_PULL_ROPE: {
                pg.pullRope();
                outMessage = new Message(OK);
                break;
            }
            case PG_RESULT_ASSERTED: {
                pg.resultAsserted();
                outMessage = new Message(OK);
                break;
            }
            case PG_SET_FLAG_POSITION: {
                pg.setFlagPosition(inMessage.getFlagPostion());
                outMessage = new Message(OK);
                break;
            }
            case PG_START_PULLING: {
                InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();
                pg.startPulling();
                outMessage = new Message(REFEREE_STATE_CHANGE);
                outMessage.setRefereeState(referee.getRefereeState());
                break;
            }
            case PG_WATCH_TRIAL: {
                InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();
                pg.watchTrial();
                outMessage = new Message(COACH_STATE_CHANGE);
                outMessage.setCoachState(coach.getCoachState());
                break;
            }
            default:
                throw new MessageException("Method in PG not found", inMessage);
        }

        return outMessage;
    }

    @Override
    public boolean goingToShutdown() {
        return pg.shutdown();
    }
}
