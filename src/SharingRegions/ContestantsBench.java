package SharingRegions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import Entities.Coach;
import Entities.Contestant;

public class ContestantsBench {
    private static final ContestantsBench[] instances = new ContestantsBench[2];

    private final int team;                                                               // Team identifier
    private final Lock lock;
    private final Condition allPlayersSeated;
    private final Condition playersSelected;

    private final Set<Contestant> bench;                                                 // Structure that contains the players in the bench
    private final Set<Contestant> selectedContestants; 

    public static synchronized ContestantsBench getInstance() {
        int team = -1;

        if(Thread.currentThread().getClass() == Contestant.class) {
            team = ((Contestant) Thread.currentThread()).getContestantTeam();
        } else if(Thread.currentThread().getClass() == Coach.class) {
            team = ((Coach) Thread.currentThread()).getCoachTeam();
        }

        if(instances[team-1] == null) {
            instances[team-1] = new ContestantsBench(team);
        }

        return instances[team-1];
    }

    private ContestantsBench(int team) {
        this.lock = new ReentrantLock();
        this.allPlayersSeated = this.lock.newCondition();
        this.playersSelected = this.lock.newCondition();

        this.team = team;
        this.bench = new TreeSet<>();
        this.selectedContestants = new TreeSet<>();
    }

    public int getTeam() {
        return team;
    }
    
    public void addContestant(){
        Contestant contestant = (Contestant) Thread.currentThread();

        lock.lock();

        bench.add(contestant);

        if(allPlayersAreSeated()) {
            allPlayersSeated.signal();
        }

        try {
            bench.add(contestant);

            while(playerIsSelected()) {
                playersSelected.await();
            }
        } catch (InterruptedException ex) {
            lock.unlock();
        }
    }
    
    public boolean getContestant(){
        Contestant contestant = (Contestant) Thread.currentThread();
        boolean result;

        lock.lock();
        
        result = bench.remove(contestant);

        lock.unlock();

        return result;
    }

    public Set<Contestant> getBench() {
        Set<Contestant> be = null;

        lock.lock();
        try {
            while(allPlayersAreSeated() != true) {
                allPlayersSeated.await();
            }
        } catch (InterruptedException ex) {
            lock.unlock();
            return null;
        }

        be = new TreeSet<>(this.bench);

        lock.unlock();

        return be;
    }

    public Set<Contestant> getSelectedContestants() {
        Set<Contestant> selected = null;

        lock.lock();

        selected = new TreeSet<>(this.selectedContestants);

        lock.unlock();

        return selected;
    }

    public void setSelectedContestants(Set<Contestant> selected) {
        lock.lock();

        selectedContestants.clear();
        selectedContestants.addAll(selected);


        playersSelected.signalAll();

        lock.unlock();
    }

    private boolean playerIsSelected() {
        Contestant contestant = (Contestant) Thread.currentThread();
        boolean result;

        lock.lock();

        result = selectedContestants.contains(contestant);

        lock.unlock();

        return result;

    }

    private boolean allPlayersAreSeated() {
        return bench.size() == 5;
    }
}
