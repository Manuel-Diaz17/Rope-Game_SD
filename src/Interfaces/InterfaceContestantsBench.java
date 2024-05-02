package Others;

import java.util.Set;

/**
 * Interface that defines the operations available over the objects that
 * represent the contestants bench
 *
 * @author Eduardo Sousa - eduardosousa@ua.pt
 * @author Guilherme Cardoso - gjc@ua.pt
 * @version 2016-2
 */
public interface InterfaceContestantsBench {

    /**
     * The method adds a contestant to the bench
     */
    void addContestant();

    /**
     * This method returns the bench which contains the contestants
     *
     * @return list of the contestants in the bench
     */
    Set<Tuple<Integer, Integer>> getBench();

    /**
     * The method removes a contestant from the bench.
     */
    void getContestant();

    /**
     * Gets the selected contestants to play
     *
     * @return set with the selected contestants
     */
    Set<Integer> getSelectedContestants();

    /**
     * Synchronisation point where the Referee waits for the Coaches to pick the
     * teams
     */
    void pickYourTeam();

    /**
     * Set selected contestants array. This arrays should be filled with the IDs
     * of the players for the next round.
     *
     * @param selected identifiers for the selected players
     */
    void setSelectedContestants(Set<Integer> selected);

    /**
     * Synchronisation point where Coaches wait for the next trial instructed by
     * the Referee
     */
    void waitForNextTrial();

    /**
     * Updates the contestant strength
     *
     * @param id of the contestants to be updated
     * @param delta difference to be applied to the contestant
     */
    void updateContestantStrength(int id, int delta);

    /**
     * Sends an interrupt to shut down the game
     */
    void interrupt();

    /**
     * Checks if the game should be shut down
     *
     * @return true if the game must be shut down
     */
    boolean shutdown();
    
    /**
     * The referee waits for everyone before starting first game
     */
    void waitForEveryoneToStart();
}
