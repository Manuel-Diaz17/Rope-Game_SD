package SharingRegions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Entities.Coach;
import Entities.Contestant;

public class Playground {
    private static Playground instance;

    private Lock lock;
    private Condition startTrial;
    private Condition teamsInPosition;
    private Condition finishedPulling;
    private Condition resultAssert;
    private int pullCounter;

    private int flagPosition;
    private int lastFlagPosition;
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
        this.pullCounter = 0;

        this.teams = new List[2];
        this.teams[0] = new ArrayList<>();
        this.teams[1] = new ArrayList<>();
    }

    public void addContestant() {
        Contestant contestant = (Contestant) Thread.currentThread();

        lock.lock();
        try {
            this.teams[contestant.getContestantTeam()-1].add(contestant);

            if(isTeamInPlace(contestant.getContestantTeam()-1)) {
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
            // TODO: Treat exception
        } finally {
            lock.unlock();
        }
    }

    public void watchTrial() {
        lock.lock();

        try {
            this.resultAssert.await();
        } catch (InterruptedException ex) {
            // TODO: Treat exception
        } finally {
            lock.unlock();
        }
    }

    public void finishedPullingRope() {
        lock.lock();
        try {
            this.pullCounter++;

            if(haveAllPulled()) {
                this.finishedPulling.signal();
            }

            this.resultAssert.await();
        } catch (InterruptedException ex) {
            // TODO: Treat exception
        } finally {
            lock.unlock();
        }
    }

    public void resultAsserted() {
        lock.lock();

        try {
            this.pullCounter = 0;

            this.resultAssert.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean getContestant(){
        Contestant contestant = (Contestant) Thread.currentThread();
        boolean result;

        lock.lock();

        result = teams[contestant.getContestantTeam()-1].remove(contestant);

        lock.unlock();

        return result;
    }
    
    public int getFlagPosition(){
        int result;

        lock.lock();

        result = this.flagPosition;

        lock.unlock();

        return result;
    }

    public void setFlagPosition(int flagPosition) {
        lock.lock();

        this.lastFlagPosition = this.flagPosition;
        this.flagPosition = flagPosition;

        lock.unlock();
    }

    public int getLastFlagPosition() {
        int result;

        lock.lock();

        result = lastFlagPosition;

        lock.unlock();

        return result;
    }

    private boolean isTeamInPlace(int teamId) {
        return this.teams[teamId].size() == 3;    
    }

    private boolean haveAllPulled() {
        return this.pullCounter == (this.teams[0].size() + this.teams[1].size());
    }
}
