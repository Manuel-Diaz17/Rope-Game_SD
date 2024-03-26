package Entities;

public class Contestant extends Thread {
    private ContestantState state;
    private int team;
    private int id;
    private int strength;

    public Contestant(String name, int team, int id, int strength) {
        super(name);

        state = ContestantState.SEAT_AT_THE_BENCH;

        this.team = team;
        this.id = id;
        this.strength = strength;
    }   


    //__________________________________Getters________________________________________
    
    public ContestantState getContestantState() {
        return state;
    }

    public int getContestantStrength() {
        return strength;
    }

    public int getContestantTeam() {
        return team;
    }

    public int getContestantId() {
        return id;
    }


    //__________________________________Setters________________________________________

    public void setContestantState(ContestantState state) {
        this.state = state;
    }


    public void setContestantStrength(int strength) {
        this.strength = strength;
    }




    @Override
    public void run() {
        while(true) {
            switch(state) {
                case SEAT_AT_THE_BENCH:
                    followCoachAdvice();
                    break;
                case STAND_IN_POSITION:
                    getReady();
                    break;
                case DO_YOUR_BEST:
                    pullTheRope();
                    seatDown();
                    break;
            }
        }
    }

    // TODO: Implement
    private void followCoachAdvice() {
        state = ContestantState.STAND_IN_POSITION;
    }

    // TODO: Implement
    private void getReady() {}

    // TODO: Implement
    private void pullTheRope() {}

    // TODO: Implement
    private void seatDown() {}
    
    public enum ContestantState {
        SEAT_AT_THE_BENCH (1, "STB"),
        STAND_IN_POSITION (2, "SIP"),
        DO_YOUR_BEST (3, "DYB");

        private int id;
        private String state;

        ContestantState(int id, String state) {
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