package Interfaces;

import Interfaces.InterfaceCoach.CoachState;

/**
 * Interface that defines the operations available over the objects that
 * represent the coach
 */

public interface InterfaceCoach {

    /**
     * Get the current Coach state
     *
     * @return coach state
     */
    CoachState getCoachState();

    /**
     * Sets the current Coach state
     *
     * @param state to set
     */
    void setCoachState(CoachState state);

    /**
     * Gets the coach team number
     *
     * @return coach team number
     */
    int getTeam();

    /**
     * Sets the current Coach team
     *
     * @param team to set
     */
    void setTeam(int team);

    /**
     * Enums of possible Coach states
     */
    public enum CoachState {
        WAIT_FOR_REFEREE_COMMAND(1, "WFRC"),
        ASSEMBLE_TEAM(2, "AETM"),
        WATCH_TRIAL(3, "WHTL");

        private final int id;
        private final String state;

        /**
         * Create a CoachState enum
         *
         * @param id of the enum coach state
         * @param state initials of the coach state
         */
        CoachState(int id, String state) {
            this.id = id;
            this.state = state;
        }

        /**
         * Converts current Coach state to String
         *
         * @return string describing Contestant sate
         */
        @Override
        public String toString() {
            return state;
        }
    }
}
