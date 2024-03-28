package Entities;

import java.util.List;

import Entities.Referee.RefereeState;
import SharingRegions.Playground;
import SharingRegions.RefereeSite;
import SharingRegions.RefereeSite.GameScore;
import SharingRegions.RefereeSite.TrialScore;

public class Referee extends Thread{
    private RefereeState state;     // Referee state
    
    public Referee(String name) {
        super(name);  
        state = RefereeState.START_OF_THE_MATCH;
    }

    public RefereeState getRefereeState() {
        return state;
    }

    public void setRefereeState(RefereeState state) {
        this.state = state;
    }

    @Override
    public void run() {
        while(true) {
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
                    
                    if(isGameWinner()) {
                        declareGameWinner();
                    } else {
                        callTrial();
                    }
                    break;
                case END_OF_A_GAME:
                    if(isMatchWinner()) {
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

    private void announceNewGame() {
        RefereeSite refereesite = RefereeSite.getInstance();
        Playground playground = Playground.getInstance();

        refereesite.resetTrialPoints();
        playground.setFlagPosition(0);

        this.setRefereeState(RefereeState.START_OF_A_GAME);

        Playground.getInstance().coachPickYourTeam();
    }

    private void callTrial() {
        RefereeSite.getInstance().bothTeamsReady();

        this.setRefereeState(RefereeState.TEAMS_READY);
    }

    private void startTrial() {
        Playground.getInstance().startPulling();

        this.setRefereeState(RefereeState.WAIT_FOR_TRIAL_CONCLUSION);
    }

    private void assertTrialDecision() {
        Playground playground = Playground.getInstance();
        RefereeSite site = RefereeSite.getInstance();

        playground.allHavePulled();

        int flagPosition = playground.getFlagPosition();
        
        if(flagPosition == 0) {
            site.addTrialPoint(TrialScore.DRAW);
        } else if(flagPosition < 0) {
            site.addTrialPoint(TrialScore.VICTORY_TEAM_1);
        } else {
            site.addTrialPoint(TrialScore.VICTORY_TEAM_2);
        }
        playground.resultAsserted();
    }

    private void declareGameWinner() {
        RefereeSite refereesite = RefereeSite.getInstance();

        List<TrialScore> trialPoints = refereesite.getTrialPoints();

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
            refereesite.addGamePoint(GameScore.DRAW);
            // TODO: logger: draw
        } else if(team1 > team2){
            if(refereesite.getRemainingTrials() == 0) {
                refereesite.addGamePoint(GameScore.VICTORY_TEAM_1_BY_POINTS);
            } else {
                refereesite.addGamePoint(GameScore.VICTORY_TEAM_1_BY_KNOCKOUT);
            }
            // TODO: logger: team1 wins the Game
        } else {
            if(refereesite.getRemainingTrials() == 0) {
                refereesite.addGamePoint(GameScore.VICTORY_TEAM_1_BY_POINTS);
            } else {
                refereesite.addGamePoint(GameScore.VICTORY_TEAM_1_BY_KNOCKOUT);
            }
            // TODO logger: team2 wins the Game
        }

        this.setRefereeState(RefereeState.END_OF_A_GAME);
    }

    private void declareMatchWinner() {
        RefereeSite refereesite = RefereeSite.getInstance();

        this.setRefereeState(RefereeState.END_OF_THE_MATCH);
    }

    private boolean isGameWinner() {
        RefereeSite site = RefereeSite.getInstance();
        List<TrialScore> trialScore = site.getTrialPoints();

        if(site.getRemainingTrials() == 0)
            return true;

            if(trialScore.size() >= (Math.floor(6/2) + 1)) {
                int team1 = 0;
                int team2 = 0;
    
                for(TrialScore score : trialScore) {
                    if(score == TrialScore.VICTORY_TEAM_1) {
                        team1++;
                    } else {
                        team2++;
                    }
                }
    
                if(Math.abs(team1 - team2) > site.getRemainingTrials())
                    return true;
            }
    
            return false;
    }

    private boolean isMatchWinner(){
        RefereeSite site = RefereeSite.getInstance();
        List<GameScore> gamePoints = site.getGamePoints();

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
                site.getRemainingGames() == 0;
    }

    public enum RefereeState {
        START_OF_THE_MATCH (1, "SOM"),
        START_OF_A_GAME (2, "SOG"),
        TEAMS_READY (3, "TR"),
        WAIT_FOR_TRIAL_CONCLUSION (4, "WFTC"),
        END_OF_A_GAME (5, "EOG"),
        END_OF_THE_MATCH (6, "EOM");

        private int id;
        private String state;

        RefereeState(int id, String state) {
            this.id = id;
            this.state = state;
        }

        public int getId() {
            return id;
        }

        public String getState() {
            return state;
        }
    }
}
