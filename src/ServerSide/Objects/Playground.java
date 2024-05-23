package ServerSide.Objects;

import Interfaces.InterfaceCoach;
import Interfaces.InterfaceContestant;
import Interfaces.InterfacePlayground;
import Interfaces.InterfaceReferee;
import Interfaces.InterfaceCoach.CoachState;
import Interfaces.InterfaceContestant.ContestantState;
import Interfaces.InterfaceReferee.RefereeState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ClientSide.Stubs.GeneralInformationRepositoryStub;

/**
 * General Description: This is an passive class that describes the Playground
 *
 * @author Eduardo Sousa - eduardosousa@ua.pt
 * @author Guilherme Cardoso - gjc@ua.pt
 * @version 2016-2
 */
public class Playground implements InterfacePlayground {

    private static Playground instance;         // singleton

    // locking and waiting condtions
    private final Lock lock;
    private final Condition startTrial;         // condition for waiting the trial start
    private final Condition teamsInPosition;    // condition for waiting to the teams to be in position
    private final Condition finishedPulling;    // condition for waiting the contestants finished pulling the rope
    private final Condition resultAssert;       // condition for waiting for the result to be asserted

    private int pullCounter;                    // how many pulls the contestants made
    private int flagPosition;                   // current flag position
    private int lastFlagPosition;               // last flag position
    private int shutdownVotes;                  // count if all votes are met to shutdown

    private final List<InterfaceContestant>[] teams;  // list containing the Contestant in both teams
    private final GeneralInformationRepositoryStub informationRepository;

    /**
     * The method returns the Playground object. This method is thread-safe and
     * uses the implicit monitor of the class.
     *
     * @return playground object to be used
     */
    public static synchronized Playground getInstance() {
        if (instance == null) {
            instance = new Playground();
        }

        return instance;
    }

    /**
     * Private constructor to be used in the singleton
     */
    private Playground() {
        this.flagPosition = 0;
        this.lastFlagPosition = 0;
        this.lock = new ReentrantLock();
        this.startTrial = this.lock.newCondition();
        this.teamsInPosition = this.lock.newCondition();
        this.finishedPulling = this.lock.newCondition();
        this.resultAssert = this.lock.newCondition();
        this.pullCounter = 0;
        this.teams = new List[2];
        this.teams[0] = new ArrayList<>();
        this.teams[1] = new ArrayList<>();
        this.informationRepository = new GeneralInformationRepositoryStub();
        this.shutdownVotes = 0;
    }

    @Override
    public void addContestant() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        lock.lock();

        try {
            this.teams[contestant.getContestantTeam() - 1].add(contestant);

            contestant.setContestantState(ContestantState.STAND_IN_POSITION);
            informationRepository.updateContestant();
            informationRepository.setTeamPlacement();
            informationRepository.printLineUpdate();

            if (isTeamInPlace(contestant.getContestantTeam())) {
                this.teamsInPosition.signalAll();
            }

            startTrial.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
        }

        lock.unlock();
    }

    @Override
    public void checkTeamPlacement() {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        lock.lock();

        coach.setCoachState(CoachState.ASSEMBLE_TEAM);
        informationRepository.updateCoach();
        informationRepository.printLineUpdate();

        try {
            while (!isTeamInPlace(coach.getCoachTeam())) {
                this.teamsInPosition.await();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
            lock.unlock();
            return;
        }

        lock.unlock();
    }

    @Override
    public void watchTrial() {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        lock.lock();

        coach.setCoachState(CoachState.WATCH_TRIAL);
        informationRepository.updateCoach();
        informationRepository.printLineUpdate();

        try {
            this.resultAssert.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
            lock.unlock();
            return;
        }

        lock.unlock();
    }

    @Override
    public void pullRope() {
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
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
            lock.unlock();
            return;
        }

        lock.unlock();
    }

    @Override
    public void resultAsserted() {
        lock.lock();

        this.pullCounter = 0;

        this.resultAssert.signalAll();

        lock.unlock();
    }

    @Override
    public void startPulling() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        lock.lock();

        this.startTrial.signalAll();

        referee.setRefereeState(RefereeState.WAIT_FOR_TRIAL_CONCLUSION);
        informationRepository.updateReferee();
        informationRepository.printLineUpdate();

        if (pullCounter != 2 * 3) {
            try {
                finishedPulling.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
                lock.unlock();
                return;
            }
        }

        lock.unlock();
    }

    @Override
    public void getContestant() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        lock.lock();

        Iterator<InterfaceContestant> it = teams[contestant.getContestantTeam() - 1].iterator();

        while (it.hasNext()) {
            InterfaceContestant temp = it.next();

            if (temp.getContestantId() == contestant.getContestantId()) {
                it.remove();
                break;
            }
        }

        informationRepository.resetTeamPlacement();
        informationRepository.printLineUpdate();

        lock.unlock();
    }

    @Override
    public int getFlagPosition() {
        int result;

        lock.lock();

        result = this.flagPosition;

        lock.unlock();

        return result;
    }

    @Override
    public int getLastFlagPosition() {
        int result;

        lock.lock();

        result = this.lastFlagPosition;

        lock.unlock();

        return result;
    }

    @Override
    public void setFlagPosition(int flagPosition) {
        this.lastFlagPosition = flagPosition;
        this.flagPosition = flagPosition;
    }

    @Override
    public void allHavePulled() {
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
    public List<InterfaceContestant>[] getTeams() {
        List<InterfaceContestant>[] teamslist = new List[2];

        lock.lock();

        teamslist[0] = new ArrayList<>(this.teams[0]);
        teamslist[1] = new ArrayList<>(this.teams[1]);

        lock.unlock();

        return teamslist;
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
     * Updates the flag position accordingly with the teams joint forces
     */
    private void updateFlagPosition() {
        int team1 = 0;
        int team2 = 0;

        for (InterfaceContestant contestant : this.teams[0]) {
            team1 += contestant.getContestantStrength();
        }

        for (InterfaceContestant contestant : this.teams[1]) {
            team2 += contestant.getContestantStrength();
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
