package Entities;

public class Coach extends Thread{
    private CoachState state;
    private int team;

    // TODO defenir bem a estrategia
    private final CoachStrategy strategy;  

    public Coach(String name, int team, CoachStrategy strategy) {
        super(name);

        this.state = CoachState.WAIT_FOR_REFEREE_COMMAND;
        this.team = team;
        this.strategy = strategy;
    }

    
    //___________________________Getters___________________________
    public CoachState getCoachState() {
        return state;
    }

    public int getTeam() {
        return team;
    }

    //___________________________Setters____________________________
    public void setState(CoachState state) {
        this.state = state;
    }


    @Override
    public void run(){
        while(true) {
            switch(state) {
                case WAIT_FOR_REFEREE_COMMAND:
                    callContestants();
                    break;
                case ASSEMBLE_TEAM:
                    informReferee();
                    break;
                case WATCH_TRIAL:
                    reviewNotes();
                    break;
            }
        }
    }

    private void callContestants() {}

    // TODO: Implement
    private void informReferee() {}

    // TODO: Implement    @Override
    public int compareTo(Coach coach) {
        return this.team - coach.team;
    }
    private void reviewNotes() {}

    public enum CoachState {
        WAIT_FOR_REFEREE_COMMAND (1, "WFRC"),
        ASSEMBLE_TEAM (2, "AT"),
        WATCH_TRIAL (3, "WT");

        private int id;
        private String state;

        CoachState(int id, String state) {
            this.id = id;
            this.state = state;
        }

        public int getID() {
            return id;
        }

        public String getState() {
            return state;
        }
    }
}