package ClientSide.Entities;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import Interfaces.InterfaceContestantsBench;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfacePlayground;
import Interfaces.InterfaceRefereeSite;
import Interfaces.InterfaceRefereeSite.TrialScore;
import Others.InterfaceCoach;
import Others.Tuple;

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

        this.team = team;               // team assignment

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

        try {
            informationRepository.updateCoach(team, state.getId());
            int waitForNextTrial = bench.waitForNextTrial(team, state.getId());

            while (!((BooleanSupplier) () -> {
                boolean isMatchEnded = false;
                refereeSite.isMatchEnded();
                return isMatchEnded;
            }).getAsBoolean()) {
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
        } catch (RemoteException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        
    }

    /**
     * The coach selects the players for the round based on
     * a random strategy
     */

    public Set<Integer> pickTeam(Set<Tuple<Integer, Integer>> contestants,
        Set<Integer> selectedContestants,
        List<TrialScore> trialPoints) {

        Set<Integer> pickedTeam = new HashSet<>();

        List<Tuple<Integer, Integer>> contestantsList = new LinkedList<>(contestants); // List of Contestants

        // choose strategy:

        // Strongest by sorting list of contestants
        //contestants.sort(comparator);


        // Random by shuffling the list of contestants
        Collections.shuffle(contestantsList);

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
    private void callContestants() throws RemoteException{
        Set<Integer> pickedContestants = this.pickTeam(
                ((Supplier<Set<Tuple<Integer, Integer>>>) () -> {
                    Set<Tuple<Integer, Integer>> getBenches = null;
                    try {
                        getBenches = bench.getBench(team);
                    } catch (RemoteException ex) {
                        Logger.getLogger(Coach.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return getBenches;

                }).get(),
                ((Supplier<Set<Integer>>) () -> {
                    Set<Integer> selectedContestants = null;
                    try {
                        selectedContestants = bench.getSelectedContestants(team);
                    } catch (RemoteException ex) {
                        Logger.getLogger(Coach.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return selectedContestants;

                }).get(),
                ((Supplier<List<TrialScore>>) () -> {
                    List<TrialScore> trialPoints = null;
                    trialPoints = refereeSite.getTrialPoints();
                    return trialPoints;

                }).get());

        bench.setSelectedContestants(team, pickedContestants);

        int checkTeamPlacement = playground.checkTeamPlacement(team);
        state = CoachState.getStateById(checkTeamPlacement);
    }

    /**
     * Informs the Referee and watches the trial
     */
    private void informReferee() throws RemoteException{
        refereeSite.informReferee();
        int watchTrial = playground.watchTrial(team);
        state = CoachState.getStateById(watchTrial);
    }

    /**
     * The coach updates his players which have played and game and updates
     * their strength
     */
    private void reviewNotes() throws RemoteException{
        Set<Tuple<Integer, Integer>> contestants = bench.getBench(team);
        Set<Integer> selectedContestants = bench.getSelectedContestants(team);

        for (int i = 1; i <= 5; i++) {
            if (selectedContestants.contains(i)) {
                bench.updateContestantStrength(i, team, -1);
            } else {
                bench.updateContestantStrength(i, team, 1);
            }
        }

        int waitForNextTrial = bench.waitForNextTrial(team, state.getId());
        state = CoachState.getStateById(waitForNextTrial);
    }

    @Override
    public int compareTo(InterfaceCoach coach) {
        return getCoachTeam() - coach.getCoachTeam();
    }

}
