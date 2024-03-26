package Entities;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import SharingRegions.ContestantsBench;
import SharingRegions.Playground;
import SharingRegions.RefereeSite;

public class Contestant extends Thread{
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

    public ContestantState getContestantState() {
        return state;
    }

    public int getContestantId() {
        return id;
    }

    public int getStrength() {
        return strength;
    }

    public int getContestantTeam() {
        return team;
    }

    public void setContestantState(ContestantState state) {
        this.state = state;
    }


    public void setStrength(int strength) {
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

    private void followCoachAdvice() {
        ContestantsBench.getInstance().getContestant();
        Playground.getInstance().addContestant();

        state = ContestantState.STAND_IN_POSITION;
    }

    private void getReady() {
        this.state = ContestantState.DO_YOUR_BEST;
    }

    private void pullTheRope() {
        try {
            Thread.sleep((long) (Math.random()*3000));
        } catch (InterruptedException ex) {
            // TODO: Treat exception
        }
        Playground.getInstance().finishedPullingRope();
    }

    private void seatDown() {
        Playground.getInstance().getContestant();
        ContestantsBench.getInstance().addContestant();

        this.state = ContestantState.SEAT_AT_THE_BENCH;
    }
    
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