package SharingRegions;

import java.util.ArrayList;
import java.util.List;

import Entities.Contestant;

public class Playground {
    private static Playground instance;
    private int flagPosition;
    private List<Contestant>[] teams;

    public static Playground getInstance() {
        if (instance == null) {
            instance = new Playground();
        }
        return instance;
    }
    
    private Playground() {
        this.teams = new List[2];
        this.teams[0] = new ArrayList<>();
        this.teams[1] = new ArrayList<>();
    }

    public void addContestantToTeam(int teamId, Contestant contestant) {
        this.teams[teamId-1].add(contestant);
    }

    public Contestant getContestantFromTeam(int teamId, int contestantId){
        for(Contestant contestant : this.teams[teamId-1]){
            if(contestant.getId() == contestantId){
                this.teams[teamId-1].remove(contestant);
                return contestant;
            }
        }
        return null;
    }
    
    public int getFlagPosition(){
        return this.flagPosition;
    }

    public void setFlagPosition(int flagPosition) {
        this.flagPosition = flagPosition;
    }

}
