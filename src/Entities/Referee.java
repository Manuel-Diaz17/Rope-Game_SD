package Entities;

import java.util.List;

import static Entities.Referee.RefereeState.END_OF_THE_MATCH;
import SharingRegions.ContestantsBench;
import SharingRegions.GeneralInformationRepository;
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

    private void announceNewGame() {
        RefereeSite.getInstance().resetTrialPoints();
        Playground.getInstance().setFlagPosition(0);

        GeneralInformationRepository.getInstance().setFlagPosition(0);
        GeneralInformationRepository.getInstance().setTrialNumber(1);
        GeneralInformationRepository.getInstance().setGameNumber(RefereeSite.getInstance().getGamePoints().size() + 1);
        GeneralInformationRepository.getInstance().printGameHeader();

        this.setRefereeState(RefereeState.START_OF_A_GAME);
        GeneralInformationRepository.getInstance().printLineUpdate();
    }

    private void callTrial() {
        GeneralInformationRepository.getInstance().setTrialNumber(RefereeSite.getInstance().getTrialPoints().size() + 1);

        List<ContestantsBench> benchs = ContestantsBench.getInstances();

        for(ContestantsBench bench : benchs)
            bench.pickYourTeam();

        RefereeSite.getInstance().bothTeamsReady();
    }

    private void startTrial() {
        Playground.getInstance().startPulling();
    }

    private void assertTrialDecision() {
        int lastFlagPosition = Playground.getInstance().getLastFlagPosition();
        int flagPosition = Playground.getInstance().getFlagPosition();
        System.out.println("lastFlagPosition: " + lastFlagPosition);
        System.out.println("flagPosition: " + flagPosition);
        System.out.println("Difference: " + (flagPosition - lastFlagPosition));

        if(flagPosition - lastFlagPosition == 0) {
            System.out.println("DRAW");
            RefereeSite.getInstance().addTrialPoint(TrialScore.DRAW);
        } else if(flagPosition - lastFlagPosition < 0) {
            System.out.println(" VICTORY_TEAM_1");
            RefereeSite.getInstance().addTrialPoint(TrialScore.VICTORY_TEAM_1);
        } else {
            System.out.println("VICTORY_TEAM_2");
            RefereeSite.getInstance().addTrialPoint(TrialScore.VICTORY_TEAM_2);
        }

        GeneralInformationRepository.getInstance().setFlagPosition(flagPosition);     
        GeneralInformationRepository.getInstance().printLineUpdate();
        GeneralInformationRepository.getInstance().resetTeamPlacement();

        Playground.getInstance().resultAsserted();
    }

    private void declareGameWinner() {
        List<TrialScore> trialPoints = RefereeSite.getInstance().getTrialPoints();
        int flagPosition = Playground.getInstance().getFlagPosition();

        switch (flagPosition) {
            case -4:
                RefereeSite.getInstance().addGamePoint(GameScore.VICTORY_TEAM_1_BY_KNOCKOUT);
                break;
            case 4:
                RefereeSite.getInstance().addGamePoint(GameScore.VICTORY_TEAM_2_BY_KNOCKOUT);
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
                    RefereeSite.getInstance().addGamePoint(GameScore.DRAW);
                } else if(team1 > team2){
                    RefereeSite.getInstance().addGamePoint(GameScore.VICTORY_TEAM_1_BY_POINTS);
                } else {
                    RefereeSite.getInstance().addGamePoint(GameScore.VICTORY_TEAM_2_BY_POINTS);
                }   
                break;
        }

        this.setRefereeState(RefereeState.END_OF_A_GAME);
        GeneralInformationRepository.getInstance().printLineUpdate();
        GeneralInformationRepository.getInstance().printGameResult(RefereeSite.getInstance().getGamePoints().get(RefereeSite.getInstance().getGamePoints().size()-1));
    }

    private void declareMatchWinner() {
        RefereeSite refereesite = RefereeSite.getInstance();

        int score1 = 0;
        int score2 = 0;

        for(GameScore score : refereesite.getGamePoints()) {
            if(score == GameScore.VICTORY_TEAM_1_BY_KNOCKOUT || score == GameScore.VICTORY_TEAM_1_BY_POINTS)
                score1++;
            else if(score == GameScore.VICTORY_TEAM_2_BY_KNOCKOUT || score == GameScore.VICTORY_TEAM_2_BY_POINTS)
                score2++;
        }

        this.setRefereeState(RefereeState.END_OF_THE_MATCH);
        GeneralInformationRepository.getInstance().printLineUpdate();

        if(score1 > score2)
            GeneralInformationRepository.getInstance().printMatchWinner(1, score1, score2);
        else if(score2 > score1)
            GeneralInformationRepository.getInstance().printMatchWinner(2, score1, score2);
        else
            GeneralInformationRepository.getInstance().printMatchDraw();

        refereesite.setIsMatchEnded(true);

    }

    private boolean isGameEnd() {
        if(Math.abs(Playground.getInstance().getFlagPosition()) >= 4)
        {
            //System.out.println("Acabou game");
            return true;
        }
        else if(RefereeSite.getInstance().getRemainingTrials() == 0)
        {
            //System.out.println("Acabou game");
            return true;
        }

        return false;
    }

    private boolean isMatchEnd(){
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

        private final int id;
        private final String state;

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

        @Override
        public String toString() {
            return state;
        }
    }
}
