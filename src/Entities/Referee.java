package Entities;

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
        boolean finishedTrial = false;
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

    // TODO: Implement
    private void announceNewGame() {}

    // TODO: Implement
    private void callTrial() {}

    // TODO: Implement
    private void startTrial() {}

    // TODO: Implement
    private boolean assertTrialDecision() {
        return true;
    }

    // TODO: Implement
    private void declareGameWinner() {}

    // TODO: Implement
    private void declareMatchWinner() {}

    // TODO: Implement
    private boolean isMatchEnd(){
        return true;
    }

    // TODO: Implement
    private boolean isGameEnd(){
        return true;
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
