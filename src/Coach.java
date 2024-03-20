public class Coach extends Thread{
    private CoachState state;
    private int team;

    public Coach(String name, int team) {
        super(name);

        this.state = CoachState.WAIT_FOR_REFEREE_COMMAND;

        this.team = team;
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

    // TODO: Implement
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