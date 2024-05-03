package Entities;

import java.util.ArrayList;
import java.util.List;

import Interfaces.InterfaceContestantsBench;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfacePlayground;
import Interfaces.InterfaceRefereeSite;
import Interfaces.InterfaceRefereeSite.GameScore;
import Interfaces.InterfaceRefereeSite.TrialScore;
import Interfaces.InterfaceReferee;

import SharingRegions.ContestantsBench;
import SharingRegions.GeneralInformationRepository;
import SharingRegions.Playground;
import SharingRegions.RefereeSite;

public class Referee extends Thread implements InterfaceReferee{
    private RefereeState state;     // Referee state

    private final InterfaceRefereeSite refereeSite; // referee site interface to be used
    private final InterfacePlayground playground; // playground interface to be used
    private final InterfaceGeneralInformationRepository informationRepository; // general Information Repository interface to be used
    private final List<InterfaceContestantsBench> benchs; // list of benches to be used
    
    /**
     * Referee initialisation
     *
     * @param name of the referee
     */
    public Referee(String name) {
        super(name);

        state = RefereeState.START_OF_THE_MATCH;
        benchs = new ArrayList<>();

        for (int i = 1; i <= 2; i++) {
            benchs.add(new ContestantsBenchStub(i));
        }

        playground = new PlaygroundStub();
        refereeSite = new RefereeSiteStub();
        informationRepository = new GeneralInformationRepositoryStub();
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
        informationRepository.updateReferee();
        informationRepository.printHeader();

        while(state != END_OF_THE_MATCH) {
            switch(state) {
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
                    System.out.println("WAIT_FOR_TRIAL_CONCLUSION");
                    //System.out.println("isGameEnd():" + isGameEnd());
                    if(isGameEnd()) {
                        declareGameWinner();
                    } else {
                        callTrial();
                    }
                    break;
                case END_OF_A_GAME:
                    if(isMatchEnd()) {
                        declareMatchWinner();
                    } else {
                        announceNewGame();
                    }
                    break;
                case END_OF_THE_MATCH:
                    break;
            }
        }
    }

    /**
     * Announces a new game. It also sets trial points, flag, etc to original
     * positions for that a new game takes place.
     */
    private void announceNewGame() {
        refereeSite.resetTrialPoints();
        playground.setFlagPosition(0);

        informationRepository.setFlagPosition(0);
        informationRepository.setTrialNumber(1);
        informationRepository.setGameNumber(refereeSite.getGamePoints().size() + 1);
        informationRepository.printGameHeader();

        setRefereeState(RefereeState.START_OF_A_GAME);
        informationRepository.updateReferee();
        informationRepository.printLineUpdate();
    }

    /**
     * Wakes up both coaches, so they can select their teams. Changes the state
     * to TEAMS_READY and blocks waiting for the coaches to wake him.
     */
    private void callTrial() {
        informationRepository.setTrialNumber(refereeSite.getTrialPoints().size() + 1);

        for(InterfaceContestantsBench bench : benchs)
            bench.pickYourTeam();

        refereeSite.bothTeamsReady();
    }

    /**
     * Is waked up by the coaches and starts the trial. Changes his state to
     * WAIT_FOR_TRIAL_CONCLUSION and blocks waiting for all the players to have
     * pulled the rope.
     */
    private void startTrial() {
        playground.startPulling();
    }

    /**
     * Decides the trial winner and steps the flag accordingly
     *
     * @return true if all trials over, false if more trials to play
     */
    private void assertTrialDecision() {
        int lastFlagPosition = playground.getLastFlagPosition();
        int flagPosition = playground.getFlagPosition();
        System.out.println("lastFlagPosition: " + lastFlagPosition);
        System.out.println("flagPosition: " + flagPosition);
        System.out.println("Difference: " + (flagPosition - lastFlagPosition));

        if(flagPosition - lastFlagPosition == 0) {
            System.out.println("DRAW");
            refereeSite.addTrialPoint(TrialScore.DRAW);
        } else if(flagPosition - lastFlagPosition < 0) {
            System.out.println(" VICTORY_TEAM_1");
            refereeSite.addTrialPoint(TrialScore.VICTORY_TEAM_1);
        } else {
            System.out.println("VICTORY_TEAM_2");
            refereeSite.addTrialPoint(TrialScore.VICTORY_TEAM_2);
        }

        informationRepository.setFlagPosition(flagPosition);     
        informationRepository.printLineUpdate();

        playground.resultAsserted();
    }

    /**
     * Decides the Game winner and sets the gamePoints accordingly
     *
     * @return true if more games to play, false if all games ended
     */
    private void declareGameWinner() {
        List<TrialScore> trialPoints = refereeSite.getTrialPoints();
        int flagPosition = playground.getFlagPosition();

        switch (flagPosition) {
            case -4:
                refereeSite.addGamePoint(GameScore.VICTORY_TEAM_1_BY_KNOCKOUT);
                break;
            case 4:
                refereeSite.addGamePoint(GameScore.VICTORY_TEAM_2_BY_KNOCKOUT);
                break;
            default:
                int team1 = 0;
                int team2 = 0;

                for(TrialScore score : trialPoints){
                    if(score == TrialScore.VICTORY_TEAM_1) {
                        team1++;
                    } else if(score == TrialScore.VICTORY_TEAM_2) {
                        team2++;
                    }
                }

                if(team1 == team2){
                    refereeSite.addGamePoint(GameScore.DRAW);
                } else if(team1 > team2){
                    refereeSite.addGamePoint(GameScore.VICTORY_TEAM_1_BY_POINTS);
                } else {
                    refereeSite.addGamePoint(GameScore.VICTORY_TEAM_2_BY_POINTS);
                }   
                break;
        }

        setRefereeState(RefereeState.END_OF_A_GAME);
        informationRepository.updateReferee();
        informationRepository.printLineUpdate();
        informationRepository.printGameResult(refereeSite.getGamePoints().get(refereeSite.getGamePoints().size()-1));
    }

    /**
     * Declares the match winner and sets the game score accordingly. Wakes up
     * all other active entities and sends them home.
     */
    private void declareMatchWinner() {
        int score1 = 0;
        int score2 = 0;

        for(GameScore score : refereesite.getGamePoints()) {
            if(score == GameScore.VICTORY_TEAM_1_BY_KNOCKOUT || score == GameScore.VICTORY_TEAM_1_BY_POINTS)
                score1++;
            else if(score == GameScore.VICTORY_TEAM_2_BY_KNOCKOUT || score == GameScore.VICTORY_TEAM_2_BY_POINTS)
                score2++;
        }

        setRefereeState(RefereeState.END_OF_THE_MATCH);
        informationRepository.updateReferee();
        informationRepository.printLineUpdate();

        if(score1 > score2)
            informationRepository.printMatchWinner(1, score1, score2);
        else if(score2 > score1)
            informationRepository.printMatchWinner(2, score1, score2);
        else
            informationRepository.printMatchDraw();

        refereesite.setIsMatchEnded(true);

    }

    /**
     * Checks if the game has ended
     *
     * @return true if game has ended false if more games to play
     */
    private boolean isGameEnd() {
        if(Math.abs(playground.getFlagPosition()) >= 4)
        {
            //System.out.println("Acabou game");
            return true;
        }
        else if(refereeSite.getRemainingTrials() == 0)
        {
            //System.out.println("Acabou game");
            return true;
        }

        return false;
    }

    /**
     * Checks if the match has ended
     *
     * @return true if match as ended
     */
    private boolean isMatchEnd(){
        List<GameScore> gamePoints = refereeSite.getGamePoints();

        int team1 = 0;
        int team2 = 0;

        for(GameScore score : gamePoints){
            if(score == GameScore.VICTORY_TEAM_1_BY_POINTS || 
                    score == GameScore.VICTORY_TEAM_1_BY_KNOCKOUT) {
                team1++;
            } else if(score == GameScore.VICTORY_TEAM_2_BY_POINTS || 
                    score == GameScore.VICTORY_TEAM_2_BY_KNOCKOUT) {
                team2++;
            }
        }

        return team1 == (Math.floor(3/2)+1) || 
                team2 == (Math.floor(3/2)+1) || 
                refereeSite.getRemainingGames() == 0;
    }
}
