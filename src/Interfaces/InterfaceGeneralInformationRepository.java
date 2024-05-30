package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ServerSide.Objects.RefereeSite;

/**
 * Interface that defines the operations available over the objects that
 * represent the general information repository.
 */
public interface InterfaceGeneralInformationRepository extends Remote{

    /**
     * Adds a Coach to General Information Repository
     * 
     * @param team
     * @param status
     * @throws java.rmi.RemoteException
     */
    void updateCoach(int team, int status) throws RemoteException;

    /**
     * Adds a Referee to General Information Repository
     * @param id
     * @param team
     * @param status
     * @param strenght
     * @throws java.rmi.RemoteException
     */
    void updateContestant(int id, int team, int status, int strength) throws RemoteException;

    /**
     * Updates the stored meta data about the strength of a contestant
     *
     * @param team of the contestant
     * @param id of the contestant
     * @param strength of the contestant
     * @throws java.rmi.RemoteException
     */
    void updateContestantStrength(int team, int id, int strength) throws RemoteException;

    /**
     * Adds a Referee to General Information Repository
     * 
     * @param status
     * @throws java.rmi.RemoteException
     */
    void updateReferee(int status) throws RemoteException;

    /**
     * Prints an line with updated information about game state
     * 
     * @throws java.rmi.RemoteException
     */
    void printLineUpdate() throws RemoteException;

    /**
     * Closes log file
     * 
     * @throws java.rmi.RemoteException
     */
    void close() throws RemoteException;

    /**
     * Print game header
     * 
     * @throws java.rmi.RemoteException
     */
    void printGameHeader() throws RemoteException;

    /**
     * Fully prints the game result
     * @throws java.rmi.RemoteException
     *
     * @param score to be printed
     * @throws java.rmi.RemoteException
     */
    void printGameResult(RefereeSite.GameScore score) throws RemoteException;

    /**
     * Print general information repository header
     * 
     * @throws java.rmi.RemoteException
     */
    void printHeader() throws RemoteException;

    /**
     * Prints game logger legend
     * 
     * @throws java.rmi.RemoteException
     */
    void printLegend() throws RemoteException;

    /**
     * Prints that was a draw
     * 
     * @throws java.rmi.RemoteException
     */
    void printMatchDraw() throws RemoteException;

    /**
     * Print Match winner
     *
     * @param team that won
     * @param score1 score team 1
     * @param score2 score team 2
     * @throws java.rmi.RemoteException
     */
    void printMatchWinner(int team, int score1, int score2) throws RemoteException;

    /**
     * Resets team placement
     * 
     * @param id
     * @param team
     * @throws java.rmi.RemoteException
     */
    void resetTeamPlacement(int id, int team) throws RemoteException;

    /**
     * Sets flag position
     *
     * @param flagPosition to set
     * @throws java.rmi.RemoteException
     */
    void setFlagPosition(int flagPosition) throws RemoteException;

    /**
     * Sets a game number
     *
     * @param gameNumber to set
     * @throws java.rmi.RemoteException
     */
    void setGameNumber(int gameNumber) throws RemoteException;

    /**
     * Sets a team placement
     * 
     * @param id
     * @param team
     * @throws java.rmi.RemoteException
     */
    void setTeamPlacement(int id, int team) throws RemoteException;

    /**
     * Sets a trial score score
     *
     * @param trialNumber to set
     * @throws java.rmi.RemoteException
     */
    void setTrialNumber(int trialNumber) throws RemoteException;

    /**
     * Checks if the game should be shut down
     *
     * @return true if the game must be shut down
     * @throws java.rmi.RemoteException
     */
    boolean shutdown() throws RemoteException;
}
