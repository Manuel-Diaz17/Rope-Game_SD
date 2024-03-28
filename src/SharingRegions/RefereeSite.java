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
        lock.lock();
        try {
            return new LinkedList<>(this.gameStatus);
        } finally {
            lock.unlock();
        }
    }
    
    public void addGamePoint(GameScore score) {
        lock.lock();
        try{
            this.gameStatus.add(score);
            this.trialStatus.clear();
        }finally{
            lock.unlock();
        }
    }


    public List<TrialScore> getTrialPoints() {
        lock.lock();
        try {
            return new LinkedList<>(this.trialStatus);
        } finally {
            lock.unlock();
        }
    }
    

    public void resetTrialPoints(){
        lock.lock();
        try {
            this.trialStatus.clear();
        } finally {
            lock.unlock();
        }
    }

    public void addTrialPoint(TrialScore score) {
        lock.lock();
        try {
            this.trialStatus.add(score);
        } finally {
            lock.unlock();
        }
    }

    public int getRemainingTrials() {
        lock.lock();
        try {
            return 6 - this.trialStatus.size();
        } finally {
            lock.unlock();
        }
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
        lock.lock();
        try {
            return 3 - this.gameStatus.size();
        } finally {
            lock.unlock();
        }
    }


    public void informReferee() {
        lock.lock();
        try {
            informRefereeCounter++;
            if (informRefereeCounter == 2)
                informReferee.signal();
        } finally {
            lock.unlock();
        }
    }
    

    public void bothTeamsReady(){
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
        boolean hasEnded;

        lock.lock();

        hasEnded = isMatchEnded;

        lock.unlock();
        return hasEnded;
    }

    public void setIsMatchEnded(boolean isMatchEnded) {
        lock.lock();

        this.isMatchEnded = isMatchEnded;

        lock.unlock();
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
