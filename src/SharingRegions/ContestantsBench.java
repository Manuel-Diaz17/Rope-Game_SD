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
import Entities.Contestant;

public class ContestantsBench {
    private static final ContestantsBench[] instances = new ContestantsBench[2];

    private final int team;                                                               // Team identifier
    private final Lock lock;
    private final Condition allPlayersSeated;
    private final Condition playersSelected;
    private final Condition waitForNextTrial;
    private final Condition waitForCoach;

    private final Set<Contestant> bench;                                                 // Structure that contains the players in the bench
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
    
    public void addContestant(){
        Contestant contestant = (Contestant) Thread.currentThread();

        lock.lock();

        bench.add(contestant);


        if(allPlayersAreSeated()) {
            allPlayersSeated.signal();
        }

        try {
            do {
                playersSelected.await();
            } while(!playerIsSelected() && !RefereeSite.getInstance().isMatchEnded());
        } catch (InterruptedException ex) {
            lock.unlock();
            return;
        }

        lock.unlock();
    }
    
    public void getContestant(){
        Contestant contestant = (Contestant) Thread.currentThread();

        lock.lock();
        
        bench.remove(contestant);

        lock.unlock();

    }

    public Set<Contestant> getBench() {
        Set<Contestant> be;

        lock.lock();
        try {
            while(!allPlayersAreSeated()) {
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

    public Set<Integer> getSelectedContestants() {
        Set<Integer> selected = null;

        lock.lock();

        selected = new TreeSet<>(this.selectedContestants);

        lock.unlock();

        return selected;
    }

    public void setSelectedContestants(Set<Integer> pickedContestants) {
        lock.lock();

        selectedContestants.clear();
        selectedContestants.addAll(pickedContestants);

        playersSelected.signalAll();

        lock.unlock();
    }

    public void pickYourTeam(){
        lock.lock();

        try {
            while(!coachWaiting)
                waitForCoach.await();
        } catch (InterruptedException ex) {
        }

        waitForNextTrial.signal();

        lock.unlock();
    }

    public void waitForNextTrial() {
        Coach coach = (Coach) Thread.currentThread();

        lock.lock();

        coach.setCoachState(CoachState.WAIT_FOR_REFEREE_COMMAND);
        GeneralInformationRepository.getInstance().printLineUpdate();

        coachWaiting = true;
        waitForCoach.signal();

        try {
            waitForNextTrial.await();
        } catch (InterruptedException ex) {}

        coachWaiting = false;

        lock.unlock();
    }

    private boolean playerIsSelected() {
        Contestant contestant = (Contestant) Thread.currentThread();
        boolean result;

        lock.lock();

        result = selectedContestants.contains(contestant.getContestantId());

        lock.unlock();

        return result;
    }

    private boolean allPlayersAreSeated() {
        return bench.size() == 5;
    }
}
