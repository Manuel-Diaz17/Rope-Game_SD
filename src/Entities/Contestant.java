package Entities;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import SharingRegions.ContestantsBench;
import SharingRegions.GeneralInformationRepository;
import SharingRegions.Playground;
import SharingRegions.RefereeSite;

public class Contestant extends Thread implements Comparable<Contestant>{
    private ContestantState state;
    private final int team;
    private final int id;
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

    public void setContestantState(ContestantState state) {
        this.state = state;
    }

    public int getContestantId() {
        return id;
    }

    public int getTeam() {
        return team;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    @Override
    public void run() {
        seatDown();
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
        GeneralInformationRepository.getInstance().setTeamPlacement();
        GeneralInformationRepository.getInstance().printLineUpdate();
    }

    private void getReady() {
        this.setContestantState(ContestantState.DO_YOUR_BEST);
        GeneralInformationRepository.getInstance().printLineUpdate();
    }

    private void pullTheRope() {
        Playground.getInstance().pullRope();
    }

    private void seatDown() {
        Playground.getInstance().getContestant();

        ContestantsBench.getInstance().addContestant();
        GeneralInformationRepository.getInstance().printLineUpdate();
    }

    @Override
    public int compareTo(Contestant contestant) {
        return this.id - contestant.id;
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

        @Override
        public String toString() {
            return state;
        }
    }
}