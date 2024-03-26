package Entities;

import SharingRegions.ContestantsBench;
import SharingRegions.RefereeSite;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Coach extends Thread{
    private CoachState state;
    private int team;

    private Comparator<Contestant> comparator = new Comparator<Contestant>() {
        @Override
        public int compare(Contestant contestant1, Contestant contestant2) {
            return contestant1.getStrength()- contestant2.getStrength();
        }
    }; 

    public Coach(String name, int team, CoachStrategy strategy) {
        super(name);

        this.state = CoachState.WAIT_FOR_REFEREE_COMMAND;
        this.team = team;
        this.strategy = strategy;
    }

    public CoachState getCoachState() {
        return state;
    }

    public void setCoachState(CoachState state) {
        this.state = state;
    }

    public int getTeam() {
        return team;
    }

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

    public int[] pickTeam(ContestantsBench bench, RefereeSite site) {
        int[] pickedTeam = new int[3];

        List<Contestant> contestants = bench.getBench();
        contestants.sort(comparator);

        for(int i = 0; i < 3; i++) {
            pickedTeam[i] = contestants.get(i).getContestantId();
        }

        return pickedTeam;
    }

    private void callContestants() {
        // Contestants bench
        ContestantsBench bench = ContestantsBench.getInstance();

        // Referee site
        RefereeSite site = RefereeSite.getInstance();

        // Picking team
        Set<Contestant> pickedContestants = this.pickTeam(bench, site);

        // Setting the selected team
        bench.setSelectedContestants(pickedContestants);

        // Updating coach state
        this.setCoachState(CoachState.ASSEMBLE_TEAM); 
    }

    // TODO: Implement
    private void informReferee() {}

    private void reviewNotes() {
        ContestantsBench bench = ContestantsBench.getInstance();
        Set<Contestant> selectedContestants = bench.getSelectedContestants();
        Set<Contestant> allContestants = bench.getBench();

        for(Contestant contestant : allContestants) {

            if(selectedContestants.contains(contestant)) {
                contestant.setStrength(contestant.getStrength() - 1);
            } else {
                contestant.setStrength(contestant.getStrength() + 1);
            }
        }

        // Updating coach state
        this.setCoachState(CoachState.WAIT_FOR_REFEREE_COMMAND);
    }

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