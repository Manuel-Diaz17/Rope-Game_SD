package SharingRegions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import Entities.Coach;
import Entities.Coach.CoachState;
import Entities.Contestant;
import Entities.Contestant.ContestantState;
import Entities.Referee;
import Entities.Referee.RefereeState;

public class Playground {
    private static Playground instance;

    private final Lock lock;
    private final Condition startTrial;
    private final Condition teamsInPosition;
    private final Condition finishedPulling;
    private final Condition resultAssert;
    private int pullCounter;

    private int flagPosition;
    private int lastFlagPosition;
    private final List<Contestant>[] teams;

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
    }

    public void addContestant() {
        Contestant contestant = (Contestant) Thread.currentThread();

        lock.lock();

        try {
            this.teams[contestant.getTeam()-1].add(contestant);

            contestant.setContestantState(ContestantState.STAND_IN_POSITION);
            GeneralInformationRepository.getInstance().setTeamPlacement();
            GeneralInformationRepository.getInstance().printLineUpdate();

            if(isTeamInPlace(contestant.getTeam())) {
                this.teamsInPosition.signalAll();
            }
            startTrial.await();

        } catch (InterruptedException ex) {
            // TODO: Treat exception
        } 

        lock.unlock();
    }


    // public void checkTeamPlacement() {
    //     Coach coach = (Coach) Thread.currentThread();

    //     lock.lock();

    //     coach.setCoachState(CoachState.ASSEMBLE_TEAM);
    //     GeneralInformationRepository.getInstance().printLineUpdate();

    //     try {
    //         lock.lock();
    //         coach.setCoachState(CoachState.ASSEMBLE_TEAM);
    //         while (!isTeamInPlace(coach.getTeam())) {
    //             this.teamsInPosition.await();
    //         }
    //     } catch (InterruptedException ex) {
    //         lock.unlock();
    //         return;
    //     } finally {
    //         lock.unlock();
    //     }
    // }

    public void checkTeamPlacement() {
        Coach coach = (Coach) Thread.currentThread();

        lock.lock();

        coach.setCoachState(CoachState.ASSEMBLE_TEAM);

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


    // public void watchTrial() {
    //     Coach coach = (Coach) Thread.currentThread();
    //     lock.lock();

    //     coach.setCoachState(CoachState.WATCH_TRIAL);
    //     GeneralInformationRepository.getInstance().printLineUpdate();

    //     try {
    //         coach.setCoachState(CoachState.WATCH_TRIAL);
    //         this.resultAssert.await();
    //     } catch (InterruptedException ex) {
    //         lock.unlock();
    //         return;
    //     } finally {
    //         lock.unlock();
    //     }
    // }

    public void watchTrial() {
        Coach coach = (Coach) Thread.currentThread();

        lock.lock();

        coach.setCoachState(CoachState.WATCH_TRIAL);
        GeneralInformationRepository.getInstance().printLineUpdate();

        try {
            this.resultAssert.await();
        } catch (InterruptedException ex) {
            lock.unlock();
            return;
        }
        lock.unlock();
    }


    public void pullRope() {
        lock.lock();
        
        try {
            long waitTime = (long) (1 + Math.random() * (3 - 1));

            Thread.currentThread().sleep(waitTime);

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


    public void resultAsserted() {
        lock.lock();
        try{
            this.pullCounter = 0;
            
            this.resultAssert.signalAll();
        }
        finally{
        lock.unlock();
        }
    }

    
    public void startPulling() {

        Referee referee = (Referee) Thread.currentThread();
        
        lock.lock();
        
        this.startTrial.signalAll();
        
        referee.setRefereeState(RefereeState.WAIT_FOR_TRIAL_CONCLUSION);
        GeneralInformationRepository.getInstance().printLineUpdate();
        
        if(pullCounter != 2 * 3)
            try {
                finishedPulling.await();
            } catch (InterruptedException ex) {
                lock.unlock();
                return;
            }
        
        
        lock.unlock();
    }
    


    // public void getContestant() {
    //     Contestant contestant = (Contestant) Thread.currentThread();
    
    //     lock.lock();
    //     try {
    //         int teamIndex = contestant.getTeam() - 1;
    //         if (teamIndex >= 0 && teamIndex < teams.length) {
    //             teams[teamIndex].remove(contestant);
    //         } else {System.out.println("Contestant team index out of bounds.");
    //         }
    //     } finally {
    //         lock.unlock();
    //     }
    // }


    public void getContestant(){
        Contestant contestant = (Contestant) Thread.currentThread();
        
        lock.lock();
        
        teams[contestant.getTeam()-1].remove(contestant);
        
        lock.unlock();
    }
    
    
    public int getFlagPosition() {
        lock.lock();
        try {
            return this.flagPosition;
        } finally {
            lock.unlock();
        }
    }
    

    public void setFlagPosition(int flagPosition) {
        this.lastFlagPosition = flagPosition;
        this.flagPosition = flagPosition;
    }


    public int getLastFlagPosition() {
        lock.lock();
        try {
            return this.lastFlagPosition;
        } finally {
            lock.unlock();
        }
    }
    

    public List<Contestant>[] getTeams() {
        List<Contestant>[] teams = new List[2];

        lock.lock();
        try {
            teams[0] = new ArrayList<>(this.teams[0]);
            teams[1] = new ArrayList<>(this.teams[1]);
        } finally {
            lock.unlock();
        }
        return teams;
    }
    

    
    public void allHavePulled() {
        lock.lock();
        try {
            this.finishedPulling.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
        }
        lock.unlock();
    }

    private void flagPositionUpdate() {
        int team1 = 0;
        int team2 = 0;

        for(Contestant contestant : this.teams[0]) {
            team1 += contestant.getStrength();
        }

        for(Contestant contestant : this.teams[1]) {
            team2 += contestant.getStrength();
        }

        lastFlagPosition = flagPosition;

        if(team1 > team2) {
            this.flagPosition--;
        } else if(team1 < team2) {
            this.flagPosition++;
        }
    }

    private boolean isTeamInPlace(int teamId) {
        return this.teams[teamId-1].size() == 3;    
    }
}
