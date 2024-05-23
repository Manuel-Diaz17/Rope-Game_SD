package Interfaces;

import ServerSide.Objects.RefereeSite;

/**
 * Interface that defines the operations available over the objects that
 * represent the general information repository.
 */
public interface InterfaceGeneralInformationRepository {

    /**
     * Adds a Coach to General Information Repository
     */
    void updateCoach();

    /**
     * Adds a Referee to General Information Repository
     */
    void updateContestant();

    /**
     * Updates the stored meta data about the strength of a contestant
     *
     * @param team of the contestant
     * @param id of the contestant
     * @param strength of the contestant
     */
    void updateContestantStrength(int team, int id, int strength);

    /**
     * Adds a Referee to General Information Repository
     */
    void updateReferee();

    /**
     * Prints an line with updated information about game state
     */
    void printLineUpdate();

    /**
     * Closes log file
     */
    void close();

    /**
     * Print game header
     */
    void printGameHeader();

    /**
     * Fully prints the game result
     *
     * @param score to be printed
     */
    void printGameResult(RefereeSite.GameScore score);

    /**
     * Print general information repository header
     */
    void printHeader();

    /**
     * Prints game logger legend
     */
    void printLegend();

    /**
     * Prints that was a draw
     */
    void printMatchDraw();

    /**
     * Print Match winner
     *
     * @param team that won
     * @param score1 score team 1
     * @param score2 score team 2
     */
    void printMatchWinner(int team, int score1, int score2);

    /**
     * Resets team placement
     */
    void resetTeamPlacement();

    /**
     * Sets flag position
     *
     * @param flagPosition to set
     */
    void setFlagPosition(int flagPosition);

    /**
     * Sets a game number
     *
     * @param gameNumber to set
     */
    void setGameNumber(int gameNumber);

    /**
     * Sets a team placement
     */
    void setTeamPlacement();

    /**
     * Sets a trial score score
     *
     * @param trialNumber to set
     */
    void setTrialNumber(int trialNumber);

    /**
     * Checks if the game should be shut down
     *
     * @return true if the game must be shut down
     */
    boolean shutdown();
}
