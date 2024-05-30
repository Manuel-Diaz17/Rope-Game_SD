package ServerSide.Objects;

import Interfaces.InterfaceContestantsBench;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfaceRefereeSite;
import Others.Triple;
import Others.Tuple;
import Others.InterfaceCoach.CoachState;
import Others.InterfaceContestant.ContestantState;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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

    // conditions for waiting
    private final Lock lock;
    private final Condition[] allPlayersSeated;
    private final Condition[] playersSelected;
    private final Condition[] waitForNextTrial;
    private final Condition[] waitForCoach;

    // structure that contains the players in the bench
    private final List<Triple<Integer, ContestantState, Integer>>[] bench;

    // selected contestants to play the trial
    private final List<Integer>[] selectedContestants;

    private boolean[] coachWaiting; // sets if the coach is waiting
    private static int shutdownVotes; // counts if everyone's ready to shutdown

    // referee site implementation to be used
    private final InterfaceRefereeSite refereeSite;

    // general Information repository implementation to be used
    private final InterfaceGeneralInformationRepository informationRepository;


    /**
     * Public constructor to be used in the doubleton
     * 
     * @param refSiteInt
     * @param girInt
     */
    public ContestantsBench(InterfaceRefereeSite refSiteStub, InterfaceGeneralInformationRepository girStub) {
        lock = new ReentrantLock();
        
        allPlayersSeated = new Condition[2];
        playersSelected = new Condition[2];
        waitForNextTrial = new Condition[2];
        waitForCoach = new Condition[2];
        coachWaiting = new boolean[2];
        
        bench = new List[2];
        selectedContestants = new List[2];
        
        for(int i = 0; i < 2; i++) {
            allPlayersSeated[i] = lock.newCondition();
            playersSelected[i] = lock.newCondition();
            waitForNextTrial[i] = lock.newCondition();
            waitForCoach[i] = lock.newCondition();
            coachWaiting[i] = false;
            
            bench[i] = new ArrayList<>();
            selectedContestants[i] = new ArrayList<>();
        }
        
        refereeSite = refSiteStub;
        informationRepository = girStub;
        
        shutdownVotes = 0;

    }

    @Override
    public Tuple<Integer, Integer> addContestant(int id, int team, int state, int strength) throws RemoteException{

        lock.lock();

        bench[team-1].add(new Triple<>(id, ContestantState.SEAT_AT_THE_BENCH, strength));

        if (ContestantState.getStateById(state) != ContestantState.SEAT_AT_THE_BENCH) {
            informationRepository.updateContestant(id, team, state, strength);
            informationRepository.printLineUpdate();
        }

        if (allPlayersAreSeated(team)) {
            allPlayersSeated[team-1].signalAll();
        }

        try {
            do {
                playersSelected[team-1].await();
            } while (!playerIsSelected(id, team) && !refereeSite.isMatchEnded());
        } catch (InterruptedException ex) {
            Logger.getLogger(ContestantsBench.class.getName()).log(Level.SEVERE, null, ex);
            lock.unlock();
            return null;
        }

        Tuple<Integer, Integer> tmp = null;

        for (Triple<Integer, ContestantState, Integer> ct : bench[team-1]){
            if(ct.getFirst() == id)
                tmp = new Tuple<>(ct.getSecond().getId(), ct.getThird());
        }

        lock.unlock();

        return tmp;
    }

    @Override
    public void getContestant(int id, int team) throws RemoteException{

        lock.lock();

        Iterator<Triple<Integer, ContestantState, Integer>> it = bench[team-1].iterator();

        while (it.hasNext()) {
            Triple<Integer, ContestantState, Integer> temp = it.next();

            if (temp.getFirst() == id) {
                it.remove();
                break;
            }
        }

        lock.unlock();
    }

    @Override
    public Set<Tuple<Integer, Integer>> getBench(int team) throws RemoteException {

        Set<Tuple<Integer, Integer>> temp;

        lock.lock();

        try {
            while (!allPlayersAreSeated(team)) {
                allPlayersSeated[team-1].await();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ContestantsBench.class.getName()).log(Level.SEVERE, null, ex);
            lock.unlock();
            return null;
        }

        temp = new HashSet<>();

        for (Triple<Integer, ContestantState, Integer> contestant : bench[team-1])
            temp.add(new Tuple<>(contestant.getFirst(), contestant.getThird()));

        lock.unlock();

        return temp;
    }

    @Override
    public void setSelectedContestants(int team, Set<Integer> selected) throws RemoteException{

        lock.lock();

        selectedContestants[team-1].clear();
        selectedContestants[team-1].addAll(selected);

        playersSelected[team-1].signalAll();

        lock.unlock();
    }

    @Override
    public Set<Integer> getSelectedContestants(int team) throws RemoteException{

        Set<Integer> selected;

        lock.lock();

        selected = new TreeSet<>(selectedContestants[team-1]);

        lock.unlock();

        return selected;
    }

    @Override
    public void pickYourTeam(int team) throws RemoteException{

        lock.lock();

        try {
            while (!coachWaiting[team-1]) {
                waitForCoach[team-1].await();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ContestantsBench.class.getName()).log(Level.SEVERE, null, ex);
        }

        waitForNextTrial[team-1].signal();

        lock.unlock();
    }

    @Override
    public int waitForNextTrial(int team, int status) throws RemoteException{

        lock.lock();

        informationRepository.updateCoach(team, status);
        informationRepository.printLineUpdate();

        coachWaiting[team-1] = true;
        waitForCoach[team-1].signal();

        try {
            waitForNextTrial[team-1].await();
        } catch (InterruptedException ex) {
            Logger.getLogger(ContestantsBench.class.getName()).log(Level.SEVERE, null, ex);
        }

        coachWaiting[team-1] = false;

        lock.unlock();

        return CoachState.WAIT_FOR_REFEREE_COMMAND.getId();
    }

    @Override
    public void updateContestantStrength(int id, int team, int delta) throws RemoteException{

        lock.lock();

        Triple<Integer, ContestantState, Integer> sub = null;
        Iterator<Triple<Integer, ContestantState, Integer>> it = bench[team-1].iterator();

        while (it.hasNext()) {
            Triple<Integer, ContestantState, Integer> temp = it.next();

            if (temp.getFirst() == id) {
                it.remove();
                sub = new Triple<>(id, temp.getSecond(), temp.getThird() + delta);
                informationRepository.updateContestantStrength(team, id, temp.getThird() + delta);
                informationRepository.printLineUpdate();
                bench[team-1].add(sub);
                break;
            }
        }

        lock.unlock();
    }

    @Override
    public void interrupt(int team) throws RemoteException{

        lock.lock();

        while (!allPlayersAreSeated(team)) {
            try {
                allPlayersSeated[team-1].await();
            } catch (InterruptedException ex) {
                lock.unlock();
                return;
            }

        }

        playersSelected[team-1].signalAll();

        while (!coachWaiting[team-1]) {
            try {
                waitForCoach[team-1].await();
            } catch (InterruptedException ex) {
                lock.unlock();
                return;
            }

        }
        waitForNextTrial[team-1].signal();

        lock.unlock();
    }

    @Override
    public void waitForEveryoneToStart(int team) throws RemoteException{

        lock.lock();

        while (!allPlayersAreSeated(team)) {
            try {
                allPlayersSeated[team-1].await();
            } catch (InterruptedException ex) {
                lock.unlock();
            }

        }

        while (!coachWaiting[team-1]) {
            try {
                waitForCoach[team-1].await();
            } catch (InterruptedException ex) {
                lock.unlock();
            }

        }

        lock.unlock();
    }

    @Override
    public boolean shutdown() throws RemoteException{
        boolean result;

        lock.lock();

        shutdownVotes++;

        result = shutdownVotes == (1 + 2 * (1 + 5));

        lock.unlock();

        return result;
    }

    /**
     * Checks if the player is selected
     *
     * @param id
     * @param team
     * @return boolean
     */
    private boolean playerIsSelected(int id, int team) {

        boolean result = false;

        Iterator<Integer> it = selectedContestants[team-1].iterator();

        while (it.hasNext()) {
            if (id == it.next()) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Checks if all players are seated on bench.
     *
     * @param team
     * @return true if all players seated
     */
    private boolean allPlayersAreSeated(int team) {
        return bench[team-1].size() == 5;
    }
}
