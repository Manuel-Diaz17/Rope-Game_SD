package Entities;

import SharingRegions.ContestantsBench;
import SharingRegions.GeneralInformationRepository;
import SharingRegions.Playground;
import SharingRegions.RefereeSite;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.text.PlainDocument;

public class Coach extends Thread implements Comparable<Coach>{
    private CoachState state;
    private final int team;

    private Comparator<Contestant> comparator = new Comparator<Contestant>() {
        @Override
        public int compare(Contestant contestant1, Contestant contestant2) {
            return contestant1.getStrength()- contestant2.getStrength();
        }
    }; 

    public Coach(String name, int team) {
        super(name);

        this.state = CoachState.WAIT_FOR_REFEREE_COMMAND;
        this.team = team;
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

    @Override
    public void run(){
        ContestantsBench.getInstance().waitForNextTrial();
        while(!RefereeSite.getInstance().isMatchEnded()) {
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

    public Set<Integer> pickTeam(ContestantsBench bench, RefereeSite site) {
        Set<Integer> pickedTeam = new HashSet<>();

        List<Contestant> contestants = new LinkedList<>(bench.getBench());
        contestants.sort(comparator);

        for(Contestant contestant : contestants) {
            if(pickedTeam.size() == 3) {
                break;
            }

            pickedTeam.add(contestant.getContestantId());
        }

        return pickedTeam;
    }

    private void callContestants() {
        // Contestants bench
        ContestantsBench bench = ContestantsBench.getInstance();

        // Referee site
        RefereeSite site = RefereeSite.getInstance();

        // Picking team
        Set<Integer> pickedContestants = this.pickTeam(bench, site);

        // Setting the selected team
        bench.setSelectedContestants(pickedContestants);

        Playground.getInstance().checkTeamPlacement();
    }

    private void informReferee() {
        RefereeSite.getInstance().informReferee();

        Playground.getInstance().watchTrial();
    }

    private void reviewNotes() {
        ContestantsBench bench = ContestantsBench.getInstance();
        Set<Integer> selectedContestants = bench.getSelectedContestants();
        Set<Contestant> allContestants = bench.getBench();

        if(allContestants != null) {
            for(Contestant contestant : allContestants) {
                if(selectedContestants.contains(contestant.getContestantId())) {
                    contestant.setStrength(contestant.getStrength() - 1);
                } else {
                    contestant.setStrength(contestant.getStrength() + 1);
                }
            }
        }

        ContestantsBench.getInstance().waitForNextTrial();
    }

    @Override
    public int compareTo(Coach coach) {
        return this.team - coach.team;
    }

    public enum CoachState {
        WAIT_FOR_REFEREE_COMMAND (1, "WFRC"),
        ASSEMBLE_TEAM (2, "AT"),
        WATCH_TRIAL (3, "WT");

        private final int id;
        private final String state;

        CoachState(int id, String state) {
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