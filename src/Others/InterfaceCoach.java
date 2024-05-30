package Others;

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
    int getCoachTeam();

    /**
     * Sets the current Coach team
     *
     * @param team to set
     */
    void setCoachTeam(int team);

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
         * Get coach state id
         * 
         * @return coach state id
         */
        public int getId() {
            return this.id;
        }
        
        /**
         * Get coach state
         * 
         * @return string describing coach state
         */
        public String getState() {
            return state;
        }

        /**
         * Get coach state by id 
         * 
         * @param id
         * @return coach state
         */
        public static CoachState getStateById(int id) {
            for (CoachState st : CoachState.values())
                if (st.getId() == id)
                    return st;
    
            return null;
        }

        /**
         * Converts current Coach state to String
         *
         * @return string describing Contestant state
         */
        @Override
        public String toString() {
            return state;
        }
    }
}
