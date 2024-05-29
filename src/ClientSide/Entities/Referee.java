package ClientSide.Entities;

import static Interfaces.InterfaceReferee.RefereeState.END_OF_THE_MATCH;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import Interfaces.InterfaceContestantsBench;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfacePlayground;
import Interfaces.InterfaceReferee;
import Interfaces.InterfaceRefereeSite;
import Interfaces.InterfaceRefereeSite.GameScore;
import Interfaces.InterfaceRefereeSite.TrialScore;
import Interfaces.Tuple;

/**
 * This is an active class implements the Referee and his interactions in the
 * passive classes
 */
public class Referee extends Thread implements InterfaceReferee {

    private final InterfaceRefereeSite refereeSite; // referee site interface to be used
    private final InterfacePlayground playground; // playground interface to be used
    private final InterfaceGeneralInformationRepository informationRepository; // general Information Repository interface to be used
    private final InterfaceContestantsBench bench; // list of benches to be used

    // referee definition
    private RefereeState state;

    /**
     * Referee initialisation
     *
     * @param name of the referee
     * @param bench interface
     * @param playground interface
     * @param refereeSite interface
     * @param informationRepository interface
     */
    public Referee(String name,
            InterfaceContestantsBench bench,
            InterfacePlayground playground,
            InterfaceRefereeSite refereeSite,
            InterfaceGeneralInformationRepository informationRepository) {

        super(name);

        state = RefereeState.START_OF_THE_MATCH;

        this.bench = bench;

        this.playground = playground;
        this.refereeSite = refereeSite;
        this.informationRepository = informationRepository;
    }

    @Override
    public RefereeState getRefereeState() {
        return state;
    }

    @Override
    public void setRefereeState(RefereeState state) {
        this.state = state;
    }

    @Override
    public void run() {
        try {
            informationRepository.updateReferee(state.getId());
            informationRepository.printHeader();

            while (state != END_OF_THE_MATCH) {
                switch (state) {
                    case START_OF_THE_MATCH:
                        announceNewGame();
                        break;
                    case START_OF_A_GAME:
                        callTrial();
                        break;
                    case TEAMS_READY:
                        startTrial();
                        break;
                    case WAIT_FOR_TRIAL_CONCLUSION:
                        assertTrialDecision();
    
                        if (isGameEnd()) {
                            declareGameWinner();
                        } else {
                            callTrial();
                        }
                        break;
                    case END_OF_A_GAME:
                        if (isMatchEnd()) {
                            declareMatchWinner();
                        } else {
                            announceNewGame();
                        }
                        break;
                    case END_OF_THE_MATCH:
                        break;
                }
            }

            bench.interrupt(1);
            bench.interrupt(2);
            informationRepository.close();
        } catch (RemoteException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Announces a new game. It also sets trial points, flag, etc to original
     * positions for that a new game takes place.
     */
    private void announceNewGame() throws RemoteException{
        refereeSite.resetTrialPoints();

        playground.setFlagPosition(0);

        informationRepository.setFlagPosition(0);

        informationRepository.setTrialNumber(1);

        informationRepository.setGameNumber(
                ((Supplier<Integer>) () -> {
                    List<GameScore> gamePoints = null;
                    try {
                        gamePoints = refereeSite.getGamePoints();
                    } catch (RemoteException ex) {
                        Logger.getLogger(Referee.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return gamePoints.size() + 1;
                }).get());
        
        informationRepository.printGameHeader();
        setRefereeState(RefereeState.START_OF_A_GAME);
        informationRepository.updateReferee(state.getId());
    }

    /**
     * Wakes up both coaches, so they can select their teams. Changes the state
     * to TEAMS_READY and blocks waiting for the coaches to wake him.
     */
    private void callTrial() throws RemoteException{
        informationRepository.setTrialNumber(((Supplier<Integer>) () -> {
            List<TrialScore> trialPoints = null;
            try {
                trialPoints = refereeSite.getTrialPoints();
            } catch (RemoteException ex) {
                Logger.getLogger(Referee.class.getName()).log(Level.SEVERE, null, ex);
            }
            return trialPoints.size();
        }).get() + 1);

        bench.pickYourTeam(1);
        bench.pickYourTeam(2);

        int bothTeamsReady = refereeSite.bothTeamsReady();
        state = InterfaceReferee.getState(bothTeamsReady);
    }

    /**
     * Is waked up by the coaches and starts the trial. Changes his state to
     * WAIT_FOR_TRIAL_CONCLUSION and blocks waiting for all the players to have
     * pulled the rope.
     */
    private void startTrial() throws RemoteException{
        int startPulling = playground.startPulling();

        state = InterfaceReferee.getState(startPulling);
    }

    /**
     * Decides the trial winner and steps the flag accordingly
     */
    private void assertTrialDecision() throws RemoteException{
        int lastFlagPosition = playground.getLastFlagPosition();
        int flagPosition = playground.getFlagPosition();

        if (flagPosition - lastFlagPosition == 0) {
            refereeSite.addTrialPoint(TrialScore.DRAW);
        } else if (flagPosition - lastFlagPosition < 0) {
            refereeSite.addTrialPoint(TrialScore.VICTORY_TEAM_1);
        } else {
            refereeSite.addTrialPoint(TrialScore.VICTORY_TEAM_2);
        }

        informationRepository.setFlagPosition(flagPosition);

        playground.resultAsserted();
    }

    /**
     * Decides the Game winner and sets the gamePoints accordingly
     */
    private void declareGameWinner() throws RemoteException{
        List<TrialScore> trialPoints = refereeSite.getTrialPoints();
        int flagPosition = playground.getFlagPosition();

        switch (flagPosition) {
            // To the left
            case -4:
                refereeSite.addGamePoint(GameScore.VICTORY_TEAM_1_BY_KNOCKOUT);
                break;
            // To the right
            case 4:
                refereeSite.addGamePoint(GameScore.VICTORY_TEAM_2_BY_KNOCKOUT);
                break;
            default:
                int team1 = 0;
                int team2 = 0;

                for (TrialScore score : trialPoints) {
                    if (score == TrialScore.VICTORY_TEAM_1) {
                        team1++;
                    } else if (score == TrialScore.VICTORY_TEAM_2) {
                        team2++;
                    }
                }

                if (team1 == team2) {
                    refereeSite.addGamePoint(GameScore.DRAW);
                } else if (team1 > team2) {
                    refereeSite.addGamePoint(GameScore.VICTORY_TEAM_1_BY_POINTS);
                } else {
                    refereeSite.addGamePoint(GameScore.VICTORY_TEAM_2_BY_POINTS);
                }
                break;
        }

        setRefereeState(RefereeState.END_OF_A_GAME);
        informationRepository.updateReferee(state.getId());
        
        informationRepository.printGameResult(((Supplier<GameScore>) () -> {
            List<GameScore> gamePoints = null;
            try {
                gamePoints = refereeSite.getGamePoints();
            } catch (RemoteException ex) {
                Logger.getLogger(Referee.class.getName()).log(Level.SEVERE, null, ex);
            }
            return gamePoints.get(gamePoints.size() - 1);

        }).get());
    }

    /**
     * Declares the match winner and sets the game score accordingly. Wakes up
     * all other active entities and sends them home.
     */
    private void declareMatchWinner() throws RemoteException{
        int score1 = 0;
        int score2 = 0;

        for (GameScore score
                : ((Supplier<List<GameScore>>) () -> {
                    List<GameScore> gamePoints = null;
                    gamePoints = refereeSite.getGamePoints();
                    return gamePoints;
                }).get()) {
            if (score == GameScore.VICTORY_TEAM_1_BY_KNOCKOUT || score == GameScore.VICTORY_TEAM_1_BY_POINTS) {
                score1++;
            } else if (score == GameScore.VICTORY_TEAM_2_BY_KNOCKOUT || score == GameScore.VICTORY_TEAM_2_BY_POINTS) {
                score2++;
            }
        }

        setRefereeState(RefereeState.END_OF_THE_MATCH);
        informationRepository.updateReferee();

        if (score1 > score2) {
            informationRepository.printMatchWinner(1, score1, score2);
        } else if (score2 > score1) {
            informationRepository.printMatchWinner(2, score1, score2);
        } else {
            informationRepository.printMatchDraw();
        }

        refereeSite.setIsMatchEnded(true);
    }

    /**
     * Checks if the game has ended
     *
     * @return true if game has ended false if more games to play
     */
    private boolean isGameEnd() {
        if (Math.abs(playground.getFlagPosition()) >= 4) {
            return true;
        } else if (refereeSite.getRemainingTrials() == 0) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the match has ended
     *
     * @return true if match as ended
     */
    private boolean isMatchEnd() {
        List<GameScore> gamePoints = refereeSite.getGamePoints();

        int team1 = 0;
        int team2 = 0;

        for (GameScore score : gamePoints) {
            if (score == GameScore.VICTORY_TEAM_1_BY_POINTS
                    || score == GameScore.VICTORY_TEAM_1_BY_KNOCKOUT) {
                team1++;
            } else if (score == GameScore.VICTORY_TEAM_2_BY_POINTS
                    || score == GameScore.VICTORY_TEAM_2_BY_KNOCKOUT) {
                team2++;
            }
        }

        return team1 == (Math.floor(3 / 2) + 1)
                || team2 == (Math.floor(3 / 2) + 1)
                || refereeSite.getRemainingGames() == 0;
    }

}
