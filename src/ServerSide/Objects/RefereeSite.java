package ServerSide.Objects;

import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfaceRefereeSite;
import Others.InterfaceReferee.RefereeState;

import static java.lang.System.out;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is an passive class that describes the Referee Site
 */
public class RefereeSite implements InterfaceRefereeSite {

    private static RefereeSite instance;        // singleton

    // locking and waiting condtions
    private final Lock lock;
    private final Condition informReferee;      // condition for referee wait for the coaches

    private int informRefereeCounter;           // counter of how many coaches informed the referee  
    private boolean hasMatchEnded;

    private List<TrialScore> trialStatus;       // current trial status
    private int shutdownVotes;                  // count if all votes are met to shutdown
    private final List<GameScore> gameStatus;   // current game status

    private final InterfaceGeneralInformationRepository informationRepository;


    /**
     * The method returns the RefereeSite object. The method is thread-safe and
     * uses the implicit monitor of the class.
     *
     * @param informationRepository interface to use
     * @return referee site object to be used
     */
    public static synchronized RefereeSite getInstance(InterfaceGeneralInformationRepository informationRepository) {
        if (instance == null) {
            instance = new RefereeSite(informationRepository);
        }

        return instance;
    }

    /**
     * Private constructor to be used in singleton
     */
    private RefereeSite(InterfaceGeneralInformationRepository informationRepository) {
        lock = new ReentrantLock();

        trialStatus = new LinkedList<>();
        gameStatus = new LinkedList<>();

        informReferee = lock.newCondition();
        informRefereeCounter = 0;
        hasMatchEnded = false;
        this.informationRepository = informationRepository;
        shutdownVotes = 0;
    }

    @Override
    public List<GameScore> getGamePoints() {
        List<GameScore> gamePoints;

        lock.lock();

        gamePoints = new LinkedList<>(this.gameStatus);

        lock.unlock();

        return gamePoints;
    }

    @Override
    public List<TrialScore> getTrialPoints() {
        List<TrialScore> trialPoints;

        lock.lock();

        trialPoints = new LinkedList<>(this.trialStatus);

        lock.unlock();

        return trialPoints;
    }

    @Override
    public void resetTrialPoints() {
        lock.lock();

        this.trialStatus = new LinkedList<>();

        lock.unlock();
    }

    @Override
    public int getRemainingTrials() {
        int remaining;

        lock.lock();

        remaining = 6 - this.trialStatus.size();

        lock.unlock();

        return remaining;
    }

    @Override
    public int getRemainingGames() {
        int remaining;

        lock.lock();

        remaining = 3 - this.gameStatus.size();

        lock.unlock();

        return remaining;
    }

    @Override
    public void addGamePoint(GameScore score) {
        lock.lock();

        this.gameStatus.add(score);
        this.trialStatus.clear();

        lock.unlock();
    }

    @Override
    public void addTrialPoint(TrialScore score) {
        lock.lock();

        this.trialStatus.add(score);

        lock.unlock();
    }

    @Override
    public int bothTeamsReady() {

        lock.lock();

        try {
            informationRepository.printLineUpdate();

            if (informRefereeCounter != 2) {
                informReferee.await();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(RefereeSite.class.getName()).log(Level.SEVERE, null, ex);
        }

        informRefereeCounter = 0;

        lock.unlock();

        return RefereeState.TEAMS_READY.getId();
    }

    @Override
    public void informReferee() {
        lock.lock();

        informRefereeCounter++;

        if (informRefereeCounter == 2) {
            informReferee.signal();
        }

        lock.unlock();
    }

    @Override
    public boolean isMatchEnded() {
        boolean hasEnded;

        lock.lock();

        hasEnded = hasMatchEnded;

        lock.unlock();

        return hasEnded;
    }

    @Override
    public void setIsMatchEnded(boolean hasMatchEnded) {
        lock.lock();

        out.println("setting has match ended");

        this.hasMatchEnded = hasMatchEnded;

        lock.unlock();
    }

    @Override
    public boolean shutdown() {
        boolean result = false;

        lock.lock();

        shutdownVotes++;

        result = shutdownVotes == (1 + 2 * (1 + 5));

        lock.unlock();

        return result;
    }

}
