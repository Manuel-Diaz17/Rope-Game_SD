package SharingRegions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import Entities.Coach;
import Entities.Contestant;

public class Playground {
    private static Playground instance;

    private Lock lock;
    private Condition startTrial;
    private Condition teamsInPosition;
    private Condition finishedPulling;
    private Condition resultAssert;
    private Condition waitForNextTrial;
    private int pullCounter;

    private int flagPosition;
    private List<Contestant>[] teams;

    public static Playground getInstance() {
        if (instance == null) {
            instance = new Playground();
        }
        return instance;
    }
    
    private Playground() {
        this.flagPosition = 0;
        this.lock = new ReentrantLock();
        this.startTrial = this.lock.newCondition();
        this.teamsInPosition = this.lock.newCondition();
        this.finishedPulling = this.lock.newCondition();
        this.resultAssert = this.lock.newCondition();
        this.waitForNextTrial = this.lock.newCondition();
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

            if(isTeamInPlace(contestant.getTeam()-1)) {
                this.teamsInPosition.signalAll();
            }
            startTrial.await();

        } catch (InterruptedException ex) {
            // TODO: Treat exception
        } finally {
            lock.unlock();
        }
    }

    public void checkTeamPlacement() {
        Coach coach = (Coach) Thread.currentThread();
        lock.lock();

        try {
            while(!isTeamInPlace(coach.getTeam()-1)) {
                this.teamsInPosition.await();
            }
        } catch (InterruptedException ex) {
            lock.unlock();
            return;
        }
        lock.unlock();
    }

    public void watchTrial() {
        lock.lock();

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
            long waitTime = (long) (1000 + Math.random() * (3000 - 1000));

            Thread.currentThread().wait(waitTime);

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

        this.pullCounter = 0;

        this.resultAssert.signalAll();

        lock.unlock(); 
    }

    public void startPulling() {
        this.startTrial.signalAll();
    }

    public void getContestant(){
        Contestant contestant = (Contestant) Thread.currentThread();

        lock.lock();

        teams[contestant.getTeam()-1].remove(contestant);

        lock.unlock();
    }
    
    public int getFlagPosition(){
        int result;

        lock.lock();

        result = this.flagPosition;

        lock.unlock();

        return result;
    }

    public void setFlagPosition(int flagPosition) {
        this.flagPosition = flagPosition;
    }

    private boolean isTeamInPlace(int teamId) {
        return this.teams[teamId].size() == 3;    
    }

    public void allHavePulled() {
        try {
            this.finishedPulling.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Playground.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Contestant>[] getTeams() {
        List<Contestant>[] teams = new List[2];

        lock.lock();

        teams[0] = new ArrayList<>(this.teams[0]);
        teams[1] = new ArrayList<>(this.teams[1]);

        lock.unlock();

        return teams;
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

        if(team1 > team2) {
            this.flagPosition--;
        } else if(team1 < team2) {
            this.flagPosition++;
        }
    }

    public void waitForNextTrial() {
        lock.lock();

        try {
            waitForNextTrial.await();
        } catch (InterruptedException ex) {}

        lock.unlock();
    }

    public void coachPickYourTeam(){
        waitForNextTrial.signalAll();
    }
}
