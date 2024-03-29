package SharingRegions;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Entities.Coach;
import Entities.Coach.CoachState;
import Entities.Contestant.ContestantState;
import Entities.Contestant;

public class ContestantsBench {
    private static final ContestantsBench[] instances = new ContestantsBench[2];

    private final int team;
    private final Lock lock;
    private final Condition allPlayersSeated;
    private final Condition playersSelected;
    private final Condition waitForNextTrial;
    private final Condition waitForCoach;

    private final Set<Contestant> bench;
    private final Set<Integer> selectedContestants;

    private boolean coachWaiting;

    public static synchronized ContestantsBench getInstance() {
        int team = -1;

        if(Thread.currentThread().getClass() == Contestant.class) {
            team = ((Contestant) Thread.currentThread()).getTeam();
        } else if(Thread.currentThread().getClass() == Coach.class) {
            team = ((Coach) Thread.currentThread()).getTeam();
        }

        if(instances[team-1] == null) {
            instances[team-1] = new ContestantsBench(team);
        }

        return instances[team-1];
    }

    public static synchronized List<ContestantsBench> getInstances() {
        List<ContestantsBench> temp = new LinkedList<>();

        for(int i = 0; i < instances.length; i++) {
            if(instances[i] == null) {
                instances[i] = new ContestantsBench(i);
            }

            temp.add(instances[i]);
        }

        return temp;
    }

    private ContestantsBench(int team) {
        this.team = team;
        lock = new ReentrantLock();

        allPlayersSeated = lock.newCondition();
        playersSelected = lock.newCondition();
        waitForNextTrial = lock.newCondition();
        waitForCoach = lock.newCondition();

        bench = new TreeSet<>();
        selectedContestants = new TreeSet<>();
    }

    public int getTeam() {
        return team;
    }
    
    public void addContestant() {
        Contestant contestant = (Contestant) Thread.currentThread();
        lock.lock();
        try {
            bench.add(contestant);
            contestant.setContestantState(ContestantState.SEAT_AT_THE_BENCH);
            if (allPlayersAreSeated()) {
                allPlayersSeated.signal();
            }
            do {
                playersSelected.await();
            } while (!playerIsSelected());
        } catch (InterruptedException ex) {
            // Tratar a interrupção conforme necessário
            System.out.println("Interrupted while adding contestant.");
        } finally {
            lock.unlock();
        }
    }
    
    public void getContestant() {
        Contestant contestant = (Contestant) Thread.currentThread();
        lock.lock();
        try {
            bench.remove(contestant);
        } finally {
            lock.unlock();
        }
    }

    public Set<Contestant> getBench() {
        Set<Contestant> bench = null;
        lock.lock();
        try {
            while (!allPlayersAreSeated()) {
                allPlayersSeated.await();
            }
            bench = new TreeSet<>(this.bench);
        } catch (InterruptedException ex) {
            // Tratar a interrupção conforme necessário
            System.out.println("Interrupted while getting bench.");
        } finally {
            lock.unlock();
        }
        return bench;
    }


    public Set<Integer> getSelectedContestants() {
        lock.lock();
        try {
            return new TreeSet<>(this.selectedContestants);
        } finally {
            lock.unlock();
        }
    }


    public void setSelectedContestants(Set<Integer> pickedContestants) {
        lock.lock();
        try{
        selectedContestants.clear();
        selectedContestants.addAll(pickedContestants);
        playersSelected.signalAll();
        }finally{
            lock.unlock();
        }
    }


    public void pickYourTeam() {
        lock.lock();
        try {
            while (!coachWaiting) {
                waitForCoach.await();
            }
            waitForNextTrial.signal();
        } catch (InterruptedException ex) {} 
        finally {
            lock.unlock();
        }
    }


    public void waitForNextTrial() {
        Coach coach = (Coach) Thread.currentThread();
        lock.lock();
        try {
            coach.setCoachState(CoachState.WAIT_FOR_REFEREE_COMMAND);
            coachWaiting = true;
            waitForCoach.signal();
            waitForNextTrial.await();
        } catch (InterruptedException ex) {} 
        
        finally {
            coachWaiting = false;
            lock.unlock();
        }
    }


    private boolean playerIsSelected() {
        Contestant contestant = (Contestant) Thread.currentThread();
        lock.lock();
        try {
            return selectedContestants.contains(contestant.getContestantId());
        } finally {
            lock.unlock();
        }
    }

    private boolean allPlayersAreSeated() {
        return bench.size() == 5;
    }
}
