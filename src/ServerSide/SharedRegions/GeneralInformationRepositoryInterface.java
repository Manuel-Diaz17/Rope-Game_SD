package ServerSide.SharedRegions;

import Communication.Message;
import static Communication.Message.MessageType.OK;
import Communication.MessageException;
import Interfaces.InterfaceContestant;

/**
 * Interface server implementation for general information repository access
 */
public class GeneralInformationRepositoryInterface implements InterfaceServer {

    private final GeneralInformationRepository ir;

    /**
     * Constructor that initiates the interface
     */
    public GeneralInformationRepositoryInterface() {
        ir = GeneralInformationRepository.getInstance();
    }

    @Override
    public Message processAndReply(Message inMessage) throws MessageException {
        Message outMessage;

        switch (inMessage.getType()) {
            case GIR_UPDATE_COACH: {
                ir.updateCoach();
                outMessage = new Message(OK);
                break;
            }
            case GIR_UPDATE_CONTESTANT: {
                ir.updateContestant();
                outMessage = new Message(OK);
                break;
            }
            case GIR_UPDATE_CONTESTANT_STRENGTH: {
                InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();
                ir.updateContestantStrength(contestant.getContestantTeam(),
                        contestant.getContestantId(),
                        contestant.getContestantStrength());
                outMessage = new Message(OK);
                break;
            }
            case GIR_UPDATE_REFEREE: {
                ir.updateReferee();
                outMessage = new Message(OK);
                break;
            }
            case GIR_CLOSE:
                ir.close();
                outMessage = new Message(OK);
                break;
            case GIR_PRINT_GAME_HEADER:
                ir.printGameHeader();
                outMessage = new Message(OK);
                break;
            case GIR_PRINT_GAME_RESULT:
                ir.printGameResult(inMessage.getGameResult());
                outMessage = new Message(OK);
                break;
            case GIR_PRINT_HEADER:
                ir.printHeader();
                outMessage = new Message(OK);
                break;
            case GIR_PRINT_LEGEND:
                ir.printLegend();
                outMessage = new Message(OK);
                break;
            case GIR_PRINT_LINE_UPDATE:
                ir.printLineUpdate();
                outMessage = new Message(OK);
                break;
            case GIR_PRINT_MATCH_DRAW:
                ir.printMatchDraw();
                outMessage = new Message(OK);
                break;
            case GIR_PRINT_MATCH_WINNER:
                int[] numbers = inMessage.getNumbers();
                ir.printMatchWinner(numbers[0], numbers[1], numbers[2]);
                outMessage = new Message(OK);
                break;
            case GIR_RESET_TEAM_PLACEMENT:
                ir.resetTeamPlacement();
                outMessage = new Message(OK);
                break;
            case GIR_SET_FLAG_POSITION:
                ir.setFlagPosition(inMessage.getFlagPostion());
                outMessage = new Message(OK);
                break;
            case GIR_SET_GAME_NUMBER:
                ir.setGameNumber(inMessage.getGameNumber());
                outMessage = new Message(OK);
                break;
            case GIR_SET_TEAM_PLACEMENT:
                ir.setTeamPlacement();
                outMessage = new Message(OK);
                break;
            case GIR_SET_TRIAL_NUMBER:
                ir.setTrialNumber(inMessage.getTrialNumber());
                outMessage = new Message(OK);
                break;
            default:
                throw new MessageException("Method in IR Interface not found", inMessage);
        }

        return outMessage;
    }

    @Override
    public boolean goingToShutdown() {
        return ir.shutdown();
    }
}
