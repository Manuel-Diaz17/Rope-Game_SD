package Others;

/**
 * Interface that defines the operations available over the objects that
 * represent the referee
 */
public interface InterfaceReferee {

    /**
     * Get the current referee state
     *
     * @return referee state
     */
    RefereeState getRefereeState();

    /**
     * Sets the current referee state
     *
     * @param state to set
     */
    void setRefereeState(RefereeState state);

    /**
     * Get referee state by id
     * 
     * @param id
     * @return referee state
     */
    static RefereeState getState(int id) {

        for (RefereeState st : RefereeState.values()) {
            if (st.getId() == id) {
                return st;
            }
        }

        return null;
    }

    /**
     * Enums of possible Referee states
     */
    public enum RefereeState {
        START_OF_THE_MATCH(1, "SOM"),
        START_OF_A_GAME(2, "SOG"),
        TEAMS_READY(3, "TRD"),
        WAIT_FOR_TRIAL_CONCLUSION(4, "WTC"),
        END_OF_A_GAME(5, "EOG"),
        END_OF_THE_MATCH(6, "EOM");

        private final int id;
        private final String state;

        /**
         * Create a referee state enum
         *
         * @param id of the enum referee state
         * @param state initials of the referee state
         */
        RefereeState(int id, String state) {
            this.id = id;
            this.state = state;
        }

        /**
         * Get referee state id
         * 
         * @return id
         */
        public int getId() {
            return this.id;
        }
        
        /**
         * 
         * @param id
         * @return referee state
         */
        public static RefereeState getStateById(int id) {
            for(RefereeState state : values())
                if(state.getId() == id)
                    return state;
            
            return null;
        }

        /**
         * Converts current referee state to String
         *
         * @return string describing referee sate
         */
        @Override
        public String toString() {
            return state;
        }
    }
}
