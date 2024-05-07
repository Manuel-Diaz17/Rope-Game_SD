package Interfaces;

import Interfaces.InterfaceContestant.ContestantState;

/**
 * Interface that defines the operations available over the objects that
 * represent the contestant
 */
public interface InterfaceContestant {

    /**
     * Gets the Contestant id
     *
     * @return contestant id number
     */
    int getContestantId();

    /**
     * Sets the current Contestant id
     *
     * @param id to set
     */
    void setContestantId(int id);

    /**
     * Get the current Contestant state.
     *
     * @return contestant state
     */
    ContestantState getContestantState();

    /**
     * Sets the current Contestant state
     *
     * @param state to set
     */
    void setContestantState(ContestantState state);

    /**
     * Gets the Contestant strength
     *
     * @return contestant strength
     */
    int getStrength();

    /**
     * Sets the Contestant strength
     *
     * @param strength to set
     */
    void setStrength(int strength);

    /**
     * Gets the Contestant team number
     *
     * @return contestant team number
     */
    int getTeam();

    /**
     * Sets the current Contestant team
     *
     * @param team to set
     */
    void setTeam(int team);

    /**
     * Enums of possible contestant states
     */
    enum ContestantState {
        SEAT_AT_THE_BENCH(1, "STB"),
        STAND_IN_POSITION(2, "SIP"),
        DO_YOUR_BEST(3, "DYB");

        private final int id;
        private final String state;

        /**
         * Create a contestant state enum
         *
         * @param id of the enum contestant state
         * @param state initials of the contestant state
         */
        ContestantState(int id, String state) {
            this.id = id;
            this.state = state;
        }

        /**
         * Converts current contestant state to String
         *
         * @return string describing contestant sate
         */
        @Override
        public String toString() {
            return state;
        }
    }
}
