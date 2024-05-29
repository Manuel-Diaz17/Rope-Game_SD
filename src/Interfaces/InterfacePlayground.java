package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface that defines the operations available over the objects that
 * represent the playground
 */
public interface InterfacePlayground extends Remote{

    /**
     * The method adds a contestant to the playground
     * @param id of the contestant
     * @param team of the contestant
     * @param status of the contestant
     * @param strength of the contestant
     * @return new contestant state id
     * @throws java.rmi.RemoteException
     */
    public int addContestant(int id, int team, int status, int strength) throws RemoteException;
    /**
     * Checks if all contestants are ready to pull the rope
     *
     * @return true if every Contestant is in place to pull the rope
     * @throws java.rmi.RemoteException
     */
    public boolean areAllContestantsReady() throws RemoteException;

    /**
     * Synchronisation point for waiting for the teams to be ready
     * @param team number
     * @return new coach state id
     * @throws java.rmi.RemoteException
     */
    public int checkTeamPlacement(int team) throws RemoteException;

    /**
     * The method removes the contestant from the playground
     * @param id of the contestant
     * @param team of the contestant
     * @throws java.rmi.RemoteException
     */
    public void getContestant(int id, int team) throws RemoteException;

    /**
     * The method returns the flag position in relation to the middle. Middle =
     * 0.
     *
     * @return position of the flag
     * @throws java.rmi.RemoteException
     */
    public int getFlagPosition() throws RemoteException;

    /**
     * Gets the last flag position
     *
     * @return flag position before the current position
     * @throws java.rmi.RemoteException
     */
    public int getLastFlagPosition() throws RemoteException;

    /**
     * Checks if everyone pulled the rope
     */
    void allHavePulled();

    /**
     * Contestant pulls the rope
     * @throws java.rmi.RemoteException
     */
    public void pullRope() throws RemoteException;

    /**
     * Synchronisation point for signalling the result is asserted
     * @throws java.rmi.RemoteException
     */
    public void resultAsserted() throws RemoteException;

    /**
     * Sets the flag position
     *
     * @param flagPosition position of the flag
     * @throws java.rmi.RemoteException
     */
    void setFlagPosition(int flagPosition) throws RemoteException;

    /**
     * Referee instructs the Contestants to start pulling the rope
     * 
     * @return new referee state id
     * @throws java.rmi.RemoteException
     */
    public int startPulling() throws RemoteException;

    /**
     * Synchronisation point for watching the trial in progress
     * 
     * @param team
     * @return updated coach state id
     * @throws java.rmi.RemoteException
     */
    public int watchTrial(int team) throws RemoteException;

    /**
     * Checks if the game should be shut down
     *
     * @return true if the game must be shut down
     * @throws java.rmi.RemoteException
     */
    boolean shutdown() throws RemoteException;
}
