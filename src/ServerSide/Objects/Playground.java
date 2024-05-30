package ServerSide.Objects;

import Interfaces.InterfacePlayground;
import Interfaces.InterfaceGeneralInformationRepository;
import Others.Triple;
import Others.InterfaceCoach.CoachState;
import Others.InterfaceContestant.ContestantState;
import Others.InterfaceReferee.RefereeState;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * General Description: This is an passive class that describes the Playground
 */
public class Playground implements InterfacePlayground {

    // locking and waiting conditions
    private final Lock lock;
    private final Condition startTrial;         // condition for waiting the trial start
    private final Condition teamsInPosition;    // condition for waiting to the teams to be in position
    private final Condition finishedPulling;    // condition for waiting the contestants finished pulling the rope
    private final Condition resultAssert;       // condition for waiting for the result to be asserted

    private int pullCounter;                    // how many pulls the contestants made
    private int flagPosition;                   // current flag position
    private int lastFlagPosition;               // last flag position
    private int shutdownVotes;                  // count if all votes are met to shutdown

    private final List<Triple<Integer, ContestantState, Integer>>[] teams;  // list containing the Contestant in both teams
    private final InterfaceGeneralInformationRepository informationRepository;

    /**
    * Public constructor to be used in the singleton
    *
    * @param girStub
    */
    public Playground(InterfaceGeneralInformationRepository girStub) {
        lock = new ReentrantLock();
        startTrial = lock.newCondition();
        teamsInPosition = lock.newCondition();
        finishedPulling = lock.newCondition();
        resultAssert = lock.newCondition();

        flagPosition = 0;
        lastFlagPosition = 0;
        pullCounter = 0;
        teams = new List[2];

        for (int i = 0; i < 2; i++) {
            teams[i] = new ArrayList<>();
        }

        informationRepository = girStub;

        shutdownVotes = 0;
    }

    @Override
    public int addContestant(int id, int team, int state, int strength) throws RemoteException{

        lock.lock();

        try {
            teams[team - 1].add(new Triple<>(id, ContestantState.STAND_IN_POSITION, strength));

            informationRepository.updateContestant(id, team, ContestantState.STAND_IN_POSITION.getId(), strength);
            informationRepository.setTeamPlacement(id, team);
            informationRepository.printLineUpdate();

            if (isTeamInPlace(team)) {
                teamsInPosition.signalAll();
            }

            startTrial.await();
        } catch (InterruptedException ex) {
            lock.unlock();
            return (Integer) null;
        }

        lock.unlock();

        return ContestantState.STAND_IN_POSITION.getId();
    }

    @Override
    public int checkTeamPlacement(int team) throws RemoteException{

        lock.lock();

        informationRepository.updateCoach(team, CoachState.ASSEMBLE_TEAM.getId());
        informationRepository.printLineUpdate();

        try {
            while (!isTeamInPlace(team)) {
                teamsInPosition.await();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
            lock.unlock();
            return (Integer) null;
        }

        lock.unlock();

        return CoachState.ASSEMBLE_TEAM.getId();
    }

    @Override
    public int watchTrial(int team) throws RemoteException{

        lock.lock();

        informationRepository.updateCoach(team, CoachState.WATCH_TRIAL.getId());
        informationRepository.printLineUpdate();

        try {
            resultAssert.await();
        } catch (InterruptedException ex) {
            lock.unlock();
            return (Integer) null;
        }

        lock.unlock();

        return CoachState.WATCH_TRIAL.getId();
    }

    @Override
    public void pullRope() throws RemoteException{

        lock.lock();

        try {
            long waitTime = (long) (1 + Math.random() * (3 - 1));

            Thread.currentThread().sleep(waitTime);

            this.pullCounter++;

            if (this.pullCounter == 2 * 3) {
                updateFlagPosition();
                this.finishedPulling.signal();
            }

            this.resultAssert.await();
        } catch (InterruptedException ex) {
            lock.unlock();
            return;
        }

        lock.unlock();
    }

    @Override
    public void resultAsserted() throws RemoteException{
        lock.lock();

        this.pullCounter = 0;

        this.resultAssert.signalAll();

        lock.unlock();
    }

    @Override
    public int startPulling() throws RemoteException{

        lock.lock();

        startTrial.signalAll();

        informationRepository.updateReferee(RefereeState.WAIT_FOR_TRIAL_CONCLUSION.getId());
        informationRepository.printLineUpdate();

        if (pullCounter != 2 * 3) {
            try {
                finishedPulling.await();
            } catch (InterruptedException ex) {
                lock.unlock();
                return (Integer) null;
            }
        }

        lock.unlock();

        return RefereeState.WAIT_FOR_TRIAL_CONCLUSION.getId();
    }

    @Override
    public void getContestant(int id, int team) throws RemoteException{

        lock.lock();

        Iterator<Triple<Integer, ContestantState, Integer>> it = teams[team - 1].iterator();

        while (it.hasNext()) {
            Triple<Integer, ContestantState, Integer> temp = it.next();

            if (temp.getFirst() == id) {
                it.remove();
                break;
            }
        }

        informationRepository.resetTeamPlacement(id, team);
        informationRepository.printLineUpdate();

        lock.unlock();
    }

    @Override
    public int getFlagPosition() throws RemoteException{
        int result;

        lock.lock();

        result = this.flagPosition;

        lock.unlock();

        return result;
    }

    @Override
    public int getLastFlagPosition() throws RemoteException{
        int result;

        lock.lock();

        result = this.lastFlagPosition;

        lock.unlock();

        return result;
    }

    @Override
    public void setFlagPosition(int flagPosition) throws RemoteException{

        lock .lock();

        this.lastFlagPosition = flagPosition;
        this.flagPosition = flagPosition;

        lock.unlock();
    }

    @Override
    public void allHavePulled() throws RemoteException{
        lock.lock();
        try {
            this.finishedPulling.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
        }
        lock.unlock();
    }

    @Override
    public boolean areAllContestantsReady() {
        return (teams[0].size() + teams[1].size()) == 3 * 2;
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
     * Updates the flag position accordingly with the teams joint forces
     */
    private void updateFlagPosition() {
        int team1 = 0;
        int team2 = 0;

        // id, state, strength
        for (Triple<Integer, ContestantState, Integer> contestant : this.teams[0]) {
            team1 += contestant.getThird();
        }

        for (Triple<Integer, ContestantState, Integer> contestant : this.teams[1]) {
            team2 += contestant.getThird();
        }

        lastFlagPosition = flagPosition;

        if (team1 > team2) {
            this.flagPosition--;
        } else if (team1 < team2) {
            this.flagPosition++;
        }
    }

    /**
     * Checks if the team is in place
     *
     * @param teamId team id to check if the team is in place
     * @return true if team in place and ready.
     */
    private boolean isTeamInPlace(int teamId) {
        return this.teams[teamId - 1].size() == 3;
    }
}
