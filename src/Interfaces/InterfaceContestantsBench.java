package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

import Others.Tuple;

/**
 * Interface that defines the operations available over the objects that
 * represent the contestants bench
 */
public interface InterfaceContestantsBench extends Remote{

    /**
     * Adds a contestant to the bench
     *
     * @param id of the contestant
     * @param team of the contestant
     * @param state Id of the contestant
     * @param strength of the contestant
     * @return Tuple ContestantState Id, Strength
     * @throws RemoteException
     */
    public Tuple<Integer, Integer> addContestant(int id, int team, int state, int strength) throws RemoteException;

    /**
     * This method returns the bench which contains the contestants
     *
     * @param team bench to get
     * @return Set with contestants iD, strength. State
     * is SEAT_AT_THE_BENCH
     * @throws java.rmi.RemoteException
     */
    public Set<Tuple<Integer, Integer>> getBench(int team) throws RemoteException;

    /**
     * Removes a contestant from the bench.
     *
     * @param id of the contestant
     * @param team of the contestant
     * @throws java.rmi.RemoteException
     */
    public void getContestant(int id, int team) throws RemoteException;

    /**
     * Gets the selected contestants to play
     *
     * @param team of the selected contestants
     * @return set with the selected contestants iDs
     * @throws java.rmi.RemoteException
     */
    public Set<Integer> getSelectedContestants(int team) throws RemoteException;

    /**
     * Synchronisation point where the Referee waits for the Coaches to pick the
     * teams
     *
     * @param team of the coach waiting
     * @throws java.rmi.RemoteException
     */
    public void pickYourTeam(int team) throws RemoteException;

    /**
     * Set selected contestants array. This arrays should be filled with the IDs
     * of the players for the next round.
     *
     * @param team of the selected contestants
     * @param selected iDs for the selected players
     * @throws java.rmi.RemoteException
     */
    public void setSelectedContestants(int team, Set<Integer> selected) throws RemoteException;

    /**
     * Synchronisation point where Coaches wait for the next trial instructed by
     * the Referee
     *
     * @param team of the coach waiting
     * @return coach state iD
     * @throws java.rmi.RemoteException
     */
    public int waitForNextTrial(int team, int status) throws RemoteException;

    /**
     * Updates the contestant strength
     *
     * @param id of the contestants to be updated
     * @param team of the contestant to be updated
     * @param delta difference to be applied to the contestant
     * @throws java.rmi.RemoteException
     */
    public void updateContestantStrength(int id, int team, int delta) throws RemoteException;

    /**
     * Sends an interrupt to shut down the game
     *
     * @param team to be interrupted
     * @throws java.rmi.RemoteException
     */
    public void interrupt(int team) throws RemoteException;

    /**
     * Checks if the game should be shut down
     *
     * @return true if the game must be shut down
     * @throws java.rmi.RemoteException
     */
    public boolean shutdown() throws RemoteException;

    /**
     * The referee waits for everyone before starting first game
     *
     * @param team
     * @throws java.rmi.RemoteException
     */
    public void waitForEveryoneToStart(int team) throws RemoteException;

}
