package ClientSide.Entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import Interfaces.InterfaceCoach;
import Interfaces.InterfaceContestantsBench;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfacePlayground;
import Interfaces.InterfaceRefereeSite;
import Interfaces.Tuple;

/**
 * This is active class Coach which implements the InterfaceCoach
 */
public class Coach extends Thread implements Comparable<InterfaceCoach>, InterfaceCoach {

    private final InterfaceContestantsBench bench; // bench interface to be used
    private final InterfaceRefereeSite refereeSite; // refereeSite interface to be used
    private final InterfacePlayground playground;  // playground interface to be used
    private final InterfaceGeneralInformationRepository informationRepository; // general Information Repository interface to be used

    // coach definition
    private CoachState state;
    private int team;

    /**
     * Creates a Coach instantiation for running in a distributed environment
     *
     * @param name of the coach
     * @param team of the coach
     * @param strategy to be used by the coach
     * @param bench interface to be used
     * @param refereeSite interface to be used
     * @param playground interface to be used
     * @param informationRepository interface to be used
     */
    public Coach(String name, int team,
            InterfaceContestantsBench bench,
            InterfaceRefereeSite refereeSite,
            InterfacePlayground playground,
            InterfaceGeneralInformationRepository informationRepository) {

        super(name);                    // giving name to thread

        // initial state
        state = CoachState.WAIT_FOR_REFEREE_COMMAND;

        this.team = team;               // team assignement

        this.bench = bench;
        this.refereeSite = refereeSite;
        this.playground = playground;
        this.informationRepository = informationRepository;

    }

    @Override
    public CoachState getCoachState() {
        return state;
    }

    @Override
    public void setCoachState(CoachState state) {
        this.state = state;
    }

    @Override
    public int getCoachTeam() {
        return team;
    }

    @Override
    public void setCoachTeam(int team) {
        this.team = team;
    }

    @Override
    public void run() {
        informationRepository.updateCoach();
        bench.waitForNextTrial();

        while (!refereeSite.isMatchEnded()) {
            switch (state) {
                case WAIT_FOR_REFEREE_COMMAND:
                    callContestants();
                    break;
                case ASSEMBLE_TEAM:
                    informReferee();
                    break;
                case WATCH_TRIAL:
                    reviewNotes();
                    break;
            }
        }
    }

    /**
     * The coach selects the players for the round based on
     * a random strategy
     */

    public Set<Integer> pickTeam(InterfaceContestantsBench bench, InterfaceRefereeSite site) {
        Set<Integer> pickedTeam = new HashSet<>();

        List<Tuple<Integer, Integer>> contestants = new LinkedList<>(bench.getBench()); // List of Contestants

        // choose strategy:

        // Strongest by sorting list of contestants
        //contestants.sort(comparator);


        // Random by shuffling the list of contestants
        Collections.shuffle(contestants);

        for (Tuple<Integer, Integer> cont : contestants) {
            if (pickedTeam.size() == 3) {
                break;
            }

            pickedTeam.add(cont.getLeft());
        }

        return pickedTeam;
    }

    /**
     * The coach decides which players are selected for next round and updates
     * selected contestants array at the bench
     */
    private void callContestants() {
        Set<Integer> pickedContestants = this.pickTeam(bench, refereeSite);
        bench.setSelectedContestants(pickedContestants);
        playground.checkTeamPlacement();
    }

    /**
     * Informs the Referee and watches the trial
     */
    private void informReferee() {
        refereeSite.informReferee();
        playground.watchTrial();
    }

    /**
     * The coach updates his players which have played and game and updates
     * their strength
     */
    private void reviewNotes() {
        Set<Tuple<Integer, Integer>> contestants = bench.getBench();
        Set<Integer> selectedContestants = bench.getSelectedContestants();

        for (int i = 1; i <= 5; i++) {
            if (selectedContestants.contains(i)) {
                bench.updateContestantStrength(i, -1);
            } else {
                bench.updateContestantStrength(i, 1);
            }
        }

        bench.waitForNextTrial();
    }

    @Override
    public int compareTo(InterfaceCoach coach) {
        return getCoachTeam() - coach.getCoachTeam();
    }

}
