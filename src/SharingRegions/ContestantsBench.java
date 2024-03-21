package SharingRegions;

import java.util.ArrayList;
import java.util.List;

import Entities.Contestant;

public class ContestantsBench {
    private static final ContestantsBench[] instances = new ContestantsBench[2];

    private List<Contestant> bench;
    private int selectedContestants[];
    private int team;

    public static synchronized ContestantsBench getInstance(int id) {
        if (instances[id-1] == null) {
            instances[id-1] = new ContestantsBench(id);
        }
        return instances[id-1];
    }

    private ContestantsBench(int team) {
        this.team = team;
        this.bench = new ArrayList<>();
        this.selectedContestants = new int[3];
    }

    public int getTeam() {
        return team;
    }
    
    public void addContestant(Contestant contestant){
        bench.add(contestant);
    }
    
    public Contestant getContestant(int id){
        for (Contestant contestant : bench) {
            if (contestant.getId() == id) {
                bench.remove(contestant);
                return contestant;
            }
        }
        return null;
    }
}
