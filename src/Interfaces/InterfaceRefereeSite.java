package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface that defines the operations available over the objects that
 * represent the referee site
 */
public interface InterfaceRefereeSite extends Remote{

    /**
     * The method allows to set the game points for both team
     *
     * @param score game points of both teams
     * @throws java.rmi.RemoteException
     */
    void addGamePoint(GameScore score) throws RemoteException;

    /**
     * The method allows to set the trial points for both team
     *
     * @param score trial points of both teams
     * @throws java.rmi.RemoteException
     */
    void addTrialPoint(TrialScore score) throws RemoteException;

    /**
     * Synchronization point where the Referee waits for both teams to be ready
     * 
     * @throws java.rmi.RemoteException
     */
    int bothTeamsReady() throws RemoteException;

    /**
     * The method returns the game points in the form of an array
     *
     * @return game points
     * @throws java.rmi.RemoteException
     */
    List<GameScore> getGamePoints() throws RemoteException;

    /**
     * Gets how many games are remaining to play
     *
     * @return number of remaining games left
     * @throws java.rmi.RemoteException
     */
    int getRemainingGames() throws RemoteException;

    /**
     * Gets how many trials are remaining to play
     *
     * @return number of remaining trials left
     * @throws java.rmi.RemoteException
     */
    int getRemainingTrials() throws RemoteException;

    /**
     * The method returns the trial points in the form of an array
     *
     * @return trial points.
     * @throws java.rmi.RemoteException
     */
    List<TrialScore> getTrialPoints() throws RemoteException;

    /**
     * Checks if the match has ended
     *
     * @return true if no more matches to play. False if otherwise
     * @throws java.rmi.RemoteException
     */
    boolean isMatchEnded() throws RemoteException;

    /**
     * Synchronisation point where the Coaches inform the Referee that they're
     * ready
     * 
     * @throws java.rmi.RemoteException
     */
    void informReferee() throws RemoteException;

    /**
     * Resets the trial points
     * 
     * @throws java.rmi.RemoteException
     */
    void resetTrialPoints() throws RemoteException;

    /**
     * Changes the information at RefereeSite if the match as ended
     *
     * @param hasMatchEnded true if match ended
     * @throws java.rmi.RemoteException
     */
    void setIsMatchEnded(boolean hasMatchEnded) throws RemoteException;

    /**
     * Checks if the game should be shut down
     *
     * @return true if the game must be shut down
     * @throws java.rmi.RemoteException
     */
    boolean shutdown() throws RemoteException;

    /**
     * Enums that describe the trial score
     */
    public enum TrialScore {
        DRAW(0, "D"),
        VICTORY_TEAM_1(1, "VT1"),
        VICTORY_TEAM_2(2, "VT2");

        private final int id;
        private final String status;

        /**
         * Initializes the trial score enum
         *
         * @param id of the trial
         * @param status of the trial
         */
        private TrialScore(int id, String status) {
            this.id = id;
            this.status = status;
        }
    }

    /**
     * Enums that describe the game score
     */
    public enum GameScore {
        DRAW(0, "D"),
        VICTORY_TEAM_1_BY_POINTS(1, "VT1PT"),
        VICTORY_TEAM_1_BY_KNOCKOUT(2, "VT1KO"),
        VICTORY_TEAM_2_BY_POINTS(3, "VT2PT"),
        VICTORY_TEAM_2_BY_KNOCKOUT(4, "VT2KO");

        private final int id;
        private final String status;

        /**
         * Initializes the game score
         *
         * @param id of the score
         * @param status of the score
         */
        private GameScore(int id, String status) {
            this.id = id;
            this.status = status;
        }

    }
}
