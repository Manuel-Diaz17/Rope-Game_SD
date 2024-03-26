package SharingRegions;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RefereeSite {
    private static RefereeSite instance;

    private Lock lock;

    private int[] gamePoints, trialPoints;
    private int gameRound, trialRound;

    public static RefereeSite getInstance() {
        if(instance == null) {
            instance = new RefereeSite();
        }
        return instance;
    }

    private RefereeSite() {
        this.lock = new ReentrantLock();

        this.gameRound = 0;
        this.trialRound = 0;
        this.gamePoints = new int[2];
        this.trialPoints = new int[2];
    }

    public int[] getGamePoints() {
        lock.lock();

        try {
            return gamePoints;
        } finally {
            lock.unlock();
        }
    }

    public void setGamePoints(int[] gamePoints) {
        lock.lock();

        try {
            this.gamePoints = gamePoints;
        } finally {
            lock.unlock();
        }
    }

    public int[] getTrialPoints() {
        lock.lock();

        try {
            return trialPoints;
        } finally {
            lock.unlock();
        }
    }

    public void setTrialPoints(int[] trialPoints) {
        lock.lock();

        try {
            this.trialPoints = trialPoints;
        } finally {
            lock.unlock();
        }
    }

    public int getGameRound() {
        lock.lock();

        try {
            return gameRound;
        } finally {
            lock.unlock();
        }
    }

    public void setGameRound(int gameRound) {
        lock.lock();

        try {
            this.gameRound = gameRound;
        } finally {
            lock.unlock();
        }
    }

    public int getTrialRound() {
        lock.lock();

        try {
            return trialRound;
        } finally {
            lock.unlock();
        }
    }

    public void setTrialRound(int trialRound) {
        lock.lock();

        try {
            this.trialRound = trialRound;
        } finally {
            lock.unlock();
        }
    }

}
