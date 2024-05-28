package Interfaces;

import java.rmi.Remote;
import java.util.List;

/**
 * Interface that defines the operations available over the objects that
 * represent the playground
 */
public interface InterfacePlayground extends Remote{

    /**
     * The method adds a contestant to the playground
     */
    void addContestant();

    /**
     * Checks if all contestants are ready to pull the rope
     *
     * @return true if every Contestant is in place to pull the rope
     */
    boolean areAllContestantsReady();

    /**
     * Synchronisation point for waiting for the teams to be ready
     */
    void checkTeamPlacement();

    /**
     * The method removes the contestant from the playground
     */
    void getContestant();

    /**
     * The method returns the flag position in relation to the middle. Middle =
     * 0.
     *
     * @return position of the flag
     */
    int getFlagPosition();

    /**
     * Gets the last flag position
     *
     * @return flag position before the current position
     */
    int getLastFlagPosition();

    /**
     * Gets the current teams in the playground
     *
     * @return list containing both teams Contestants in the playground
     */
    List<InterfaceContestant>[] getTeams();

    /**
     * Checks if everyone pulled the rope
     */
    void allHavePulled();

    /**
     * Contestant pulls the rope
     */
    void pullRope();

    /**
     * Synchronisation point for signalling the result is asserted
     */
    void resultAsserted();

    /**
     * Sets the flag position
     *
     * @param flagPosition position of the flag
     */
    void setFlagPosition(int flagPosition);

    /**
     * Referee instructs the Contestants to start pulling the rope
     */
    void startPulling();

    /**
     * Synchronisation point for watching the trial in progress
     */
    void watchTrial();

    /**
     * Checks if the game should be shut down
     *
     * @return true if the game must be shut down
     */
    boolean shutdown();
}
