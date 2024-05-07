package ServerSide.SharedRegions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ClientSide.Stubs.GeneralInformationRepositoryStub;
import Interfaces.InterfaceCoach;
import Interfaces.InterfaceCoach.CoachState;
import Interfaces.InterfaceContestant.ContestantState;
import Interfaces.InterfaceContestant;
import Interfaces.InterfacePlayground;
import Interfaces.InterfaceReferee;
import Interfaces.InterfaceReferee.RefereeState;

/**
 * General Description: This is an passive class that describes the Playground
 */
public class Playground implements InterfacePlayground{
    private static Playground instance;

    private final Lock lock;
    private final Condition startTrial;
    private final Condition teamsInPosition;
    private final Condition finishedPulling;
    private final Condition resultAssert;

    private int pullCounter;
    private int flagPosition;
    private int lastFlagPosition;
    private int shutdownVotes;

    private final List<InterfaceContestant>[] teams;
    private final GeneralInformationRepositoryStub informationRepository;

    /**
     * The method returns the Playground object. This method is thread-safe and
     * uses the implicit monitor of the class.
     *
     * @return playground object to be used
     */
    public static Playground getInstance() {
        if (instance == null) {
            instance = new Playground();
        }
        return instance;
    }
    
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
            this.teams[contestant.getTeam()-1].add(contestant);

            contestant.setContestantState(ContestantState.STAND_IN_POSITION);
            informationRepository.updateContestant();
            informationRepository.setTeamPlacement();
            informationRepository.printLineUpdate();

            if(isTeamInPlace(contestant.getTeam())) {
                this.teamsInPosition.signalAll();
            }
            startTrial.await();

        } catch (InterruptedException ex) {
            // TODO: Treat exception
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
            while(!isTeamInPlace(coach.getTeam())) {
                this.teamsInPosition.await();
            }
        } catch (InterruptedException ex) {
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
            lock.unlock();
            return;
        }

        lock.unlock();
    }

    @Override
    public void pullRope() {
        lock.lock();
        
        try {

            this.pullCounter++;

            if(this.pullCounter == 2 * 3) {
                flagPositionUpdate();
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
    public void resultAsserted() {
        lock.lock();
        try {
            pullCounter = 0;
            resultAssert.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void startPulling() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();
        
        lock.lock();
        
        this.startTrial.signalAll();
        
        referee.setRefereeState(RefereeState.WAIT_FOR_TRIAL_CONCLUSION);
        informationRepository.updateReferee();
        informationRepository.printLineUpdate();
        
        if(pullCounter != 2 * 3)   // 3 is equal to players in the playground
            try {
                finishedPulling.await();
            } catch (InterruptedException ex) {
                lock.unlock();
                return;
            }
        
        
        lock.unlock();
    }
    
    @Override
    public void getContestant(){
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();
        
        lock.lock();
        
        Iterator<InterfaceContestant> it = teams[contestant.getTeam() - 1].iterator();

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
        lock.lock();
        try {
            return this.flagPosition;
        } finally {
            lock.unlock();
        }
    }
    
    
    @Override
    public void setFlagPosition(int flagPosition) {
        this.lastFlagPosition = flagPosition;
        this.flagPosition = flagPosition;
    }

    @Override
    public int getLastFlagPosition() {
        lock.lock();
        try {
            return this.lastFlagPosition;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public List<InterfaceContestant>[] getTeams() {
        List<InterfaceContestant>[] teams = new List[2];

        lock.lock();

        teams[0] = new ArrayList<>(this.teams[0]); 
        teams[1] = new ArrayList<>(this.teams[1]);

        lock.unlock();

        return teams;
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
    

    /**
     * Updates the flag position accordingly with the teams joint forces
     */
    private void flagPositionUpdate() {
        int team1 = 0;
        int team2 = 0;

        for(InterfaceContestant contestant : this.teams[0]) {
            team1 += contestant.getStrength();
        }

        for(InterfaceContestant contestant : this.teams[1]) {
            team2 += contestant.getStrength();
        }

        lastFlagPosition = flagPosition;

        if(team1 > team2) {
            this.flagPosition--;
        } else if(team1 < team2) {
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
        return this.teams[teamId-1].size() == 3;    
    }
}
