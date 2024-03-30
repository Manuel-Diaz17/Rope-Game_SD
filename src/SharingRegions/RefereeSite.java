package SharingRegions;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import Entities.Referee;
import Entities.Referee.RefereeState;

public class RefereeSite {
    private static RefereeSite instance;

    private final Lock lock;

    private final Condition informReferee;
    private int informRefereeCounter;
    private boolean isMatchEnded;

    private List<TrialScore> trialStatus;
    private final List<GameScore> gameStatus;

    // Create instance
    public static synchronized RefereeSite getInstance() {
        if(instance == null) {
            instance = new RefereeSite();
        }
        return instance;
    }

    private RefereeSite() {
        lock = new ReentrantLock();

        trialStatus = new LinkedList<>();
        gameStatus = new LinkedList<>();

        informReferee = lock.newCondition();
        informRefereeCounter = 0;
        isMatchEnded = false;
    }


    public List<GameScore> getGamePoints() {
        List<GameScore> gamePoints;
        
        lock.lock();
        
        gamePoints = new LinkedList<>(this.gameStatus);
        
        lock.unlock();
        
        return gamePoints;
    }
    
    public void addGamePoint(GameScore score) {
        lock.lock();
        
        this.gameStatus.add(score);
        this.trialStatus.clear();
        
        lock.unlock();
    }


    public List<TrialScore> getTrialPoints() {
        List<TrialScore> trialPoints;
        
        lock.lock();
        
        trialPoints = new LinkedList<>(this.trialStatus);
        
        lock.unlock();
        
        return trialPoints;
    }
    

    public void resetTrialPoints(){
        lock.lock();
        
        this.trialStatus = new LinkedList<>();
        
        lock.unlock();
    }

    public void addTrialPoint(TrialScore score) {
        lock.lock();
        
        this.trialStatus.add(score);
        
        lock.unlock();
    }

    public int getRemainingTrials() {
        int remaining;
        
        lock.lock();
        
        remaining = 6 - this.trialStatus.size();
        
        lock.unlock();
        
        return remaining;
    }
    

    public int getTrialRound() {
        lock.lock();
        try {
            return this.trialStatus.size() + 1;
        } finally {
            lock.unlock();
        }
    }
    

    
    public int getRemainingGames() {
        int remaining;
        
        lock.lock();
        
        remaining = 3 - this.gameStatus.size();
        
        lock.unlock();
        
        return remaining;
    }


    public void informReferee() {
        lock.lock();
        
        informRefereeCounter++;
        
        if(informRefereeCounter == 2)
            informReferee.signal();
        
        lock.unlock();
    }
    
    public void bothTeamsReady() {
        Referee referee = (Referee) Thread.currentThread();
        
        lock.lock();
        try {
            referee.setRefereeState(RefereeState.TEAMS_READY);
            GeneralInformationRepository.getInstance().printLineUpdate();
            
            if(informRefereeCounter != 2)
                informReferee.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(RefereeSite.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        informRefereeCounter = 0;
        
        lock.unlock();
    }
    

    public boolean isMatchEnded() {
        lock.lock();
        try {
            return isMatchEnded;
        } finally {
            lock.unlock();
        }
    }
    
    public void setIsMatchEnded(boolean isMatchEnded) {
        lock.lock();
        try {
            this.isMatchEnded = isMatchEnded;
        } finally {
            lock.unlock();
        }
    }
    
    public enum TrialScore {
        DRAW(0, "D"),
        VICTORY_TEAM_1(1, "VT1"),
        VICTORY_TEAM_2(2, "VT2");

        private final int id;
        private final String status;

        private TrialScore(int id, String status) {
            this.id = id;
            this.status = status;
        }

        public int getId() {
            return this.id;
        }

        public String getStatus() {
            return this.status;
        }
    }

    public enum GameScore {
        DRAW(0, "D"),
        VICTORY_TEAM_1_BY_POINTS(1, "VT1PT"),
        VICTORY_TEAM_1_BY_KNOCKOUT(2, "VT1KO"),
        VICTORY_TEAM_2_BY_POINTS(3, "VT2PT"),
        VICTORY_TEAM_2_BY_KNOCKOUT(4, "VT2KO");

        private final int id;
        private final String status;

        private GameScore(int id, String status) {
            this.id = id;
            this.status = status;
        }

        public int getId() {
            return this.id;
        }

        public String getStatus() {
            return this.status;
        }
    }

}
