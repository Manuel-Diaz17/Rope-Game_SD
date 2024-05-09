package ServerSide.SharedRegions;

import ClientSide.Stubs.GeneralInformationRepositoryStub;
import ClientSide.Stubs.RefereeSiteStub;
import Interfaces.InterfaceCoach;
import Interfaces.InterfaceContestant;
import Interfaces.InterfaceContestantsBench;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfaceRefereeSite;
import Interfaces.Tuple;
import Interfaces.InterfaceCoach.CoachState;
import Interfaces.InterfaceContestant.ContestantState;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is an passive class that describes the contestants bench for each team
 */
public class ContestantsBench implements InterfaceContestantsBench {

    // doubleton containing the two teams benches
    private static final ContestantsBench[] instances = new ContestantsBench[2];

    // conditions for waiting
    private final Lock lock;
    private final Condition allPlayersSeated;
    private final Condition playersSelected;
    private final Condition waitForNextTrial;
    private final Condition waitForCoach;

    // structure that contains the players in the bench
    private final Set<InterfaceContestant> bench;

    // selected contestants to play the trial
    private final Set<Integer> selectedContestants;

    private boolean coachWaiting; // sets if the coach is waiting
    private static int shutdownVotes; // counts if everyone's ready to shutdown

    // referee site implementation to be used
    private final InterfaceRefereeSite refereeSite;

    // general Information repository implementation to be used
    private final InterfaceGeneralInformationRepository informationRepository;

    /**
     * Gets all the instances of the Contestants Bench
     *
     * @return list containing contestants benches
     */
    public static synchronized List<ContestantsBench> getInstances() {
        List<ContestantsBench> temp = new LinkedList<>();

        for (int i = 0; i < instances.length; i++) {
            if (instances[i] == null) {
                instances[i] = new ContestantsBench();
            }

            temp.add(instances[i]);
        }

        return temp;
    }

    /**
     * Private constructor to be used in the doubleton
     */
    private ContestantsBench() {
        lock = new ReentrantLock();
        allPlayersSeated = lock.newCondition();
        playersSelected = lock.newCondition();
        waitForNextTrial = lock.newCondition();
        waitForCoach = lock.newCondition();
        bench = new TreeSet<>();
        selectedContestants = new TreeSet<>();
        refereeSite = new RefereeSiteStub();
        informationRepository = new GeneralInformationRepositoryStub();
        shutdownVotes = 0;
        coachWaiting = false;
    }

    @Override
    public void addContestant() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        lock.lock();

        bench.add(contestant);

        if (contestant.getContestantState() != ContestantState.SEAT_AT_THE_BENCH) {
            contestant.setContestantState(ContestantState.SEAT_AT_THE_BENCH);
            informationRepository.updateContestant();
            informationRepository.printLineUpdate();
        }

        if (allPlayersAreSeated()) {
            allPlayersSeated.signalAll();
        }

        try {
            do {
                playersSelected.await();
            } while (!playerIsSelected() && !refereeSite.isMatchEnded());
        } catch (InterruptedException ex) {
            Logger.getLogger(ContestantsBench.class.getName()).log(Level.SEVERE, null, ex);
            lock.unlock();
            return;
        }

        lock.unlock();
    }

    @Override
    public void getContestant() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        lock.lock();

        Iterator<InterfaceContestant> it = bench.iterator();

        while (it.hasNext()) {
            InterfaceContestant temp = it.next();

            if (temp.getContestantId() == contestant.getContestantId()) {
                it.remove();
            }
        }

        lock.unlock();
    }

    @Override
    public Set<Tuple<Integer, Integer>> getBench() {
        Set<Tuple<Integer, Integer>> temp;

        lock.lock();

        try {
            while (!allPlayersAreSeated()) {
                allPlayersSeated.await();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ContestantsBench.class.getName()).log(Level.SEVERE, null, ex);
            lock.unlock();
            return null;
        }

        temp = new HashSet<>();

        for (InterfaceContestant contestant : bench) {
            temp.add(new Tuple<>(contestant.getContestantId(), contestant.getContestantStrength()));
        }

        lock.unlock();

        return temp;
    }

    @Override
    public void setSelectedContestants(Set<Integer> selected) {
        lock.lock();

        selectedContestants.clear();
        selectedContestants.addAll(selected);

        playersSelected.signalAll();

        lock.unlock();
    }

    @Override
    public Set<Integer> getSelectedContestants() {
        Set<Integer> selected = null;

        lock.lock();

        selected = new TreeSet<>(this.selectedContestants);

        lock.unlock();

        return selected;
    }

    @Override
    public void pickYourTeam() {
        lock.lock();

        try {
            while (!coachWaiting) {
                waitForCoach.await();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ContestantsBench.class.getName()).log(Level.SEVERE, null, ex);
        }

        waitForNextTrial.signal();

        lock.unlock();
    }

    @Override
    public void waitForNextTrial() {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        lock.lock();

        coach.setCoachState(CoachState.WAIT_FOR_REFEREE_COMMAND);
        informationRepository.updateCoach();
        informationRepository.printLineUpdate();

        coachWaiting = true;
        waitForCoach.signal();

        try {
            waitForNextTrial.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(ContestantsBench.class.getName()).log(Level.SEVERE, null, ex);
        }

        coachWaiting = false;

        lock.unlock();
    }

    @Override
    public void updateContestantStrength(int id, int delta) {
        lock.lock();

        for (InterfaceContestant contestant : bench) {
            if (contestant.getContestantId() == id) {
                contestant.setContestantStrength(contestant.getContestantStrength() + delta);
                informationRepository.updateContestantStrength(contestant.getContestantTeam(),
                        contestant.getContestantId(), contestant.getContestantStrength());
                informationRepository.printLineUpdate();
            }
        }

        lock.unlock();
    }

    @Override
    public void interrupt() {
        lock.lock();

        while (!allPlayersAreSeated()) {
            try {
                allPlayersSeated.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(ContestantsBench.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        playersSelected.signalAll();

        while (!coachWaiting) {
            try {
                waitForCoach.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(ContestantsBench.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        waitForNextTrial.signal();

        lock.unlock();
    }

    @Override
    public void waitForEveryoneToStart() {
        lock.lock();

        while (!allPlayersAreSeated()) {
            try {
                allPlayersSeated.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(ContestantsBench.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        while (!coachWaiting) {
            try {
                waitForCoach.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(ContestantsBench.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        lock.unlock();
    }

    @Override
    public boolean shutdown() {
        boolean result;

        lock.lock();

        shutdownVotes++;

        result = shutdownVotes == (1 + 2 * (1 + 5));

        lock.unlock();

        return result;
    }

    /**
     * Gets the selected contestants array.
     *
     * @return integer array of the selected contestants for the round
     */
    private boolean playerIsSelected() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();
        boolean result;

        lock.lock();

        result = selectedContestants.contains(contestant.getContestantId());

        lock.unlock();

        return result;
    }

    /**
     * Checks if all players are seated on bench.
     *
     * @return true if all players seated
     */
    private boolean allPlayersAreSeated() {
        return bench.size() == 5;
    }
}
