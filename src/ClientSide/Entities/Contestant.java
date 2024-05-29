package ClientSide.Entities;

import java.rmi.RemoteException;

import Interfaces.InterfaceContestant;
import Interfaces.InterfaceContestantsBench;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfacePlayground;
import Interfaces.InterfaceRefereeSite;
import Interfaces.Tuple;

/**
 * This is active class Contestant which implements the InterfaceContestant
 */
public class Contestant extends Thread implements Comparable<InterfaceContestant>, InterfaceContestant {

    private final InterfaceContestantsBench bench; // bench interface to be used
    private final InterfacePlayground playground; // playground interface to be used
    private final InterfaceRefereeSite refereeSite; // refereeSite interface to be used
    private final InterfaceGeneralInformationRepository informationRepository; // general Information Repository interface to be used

    // contestant definition
    private ContestantState state;
    private int strength;
    private int team;
    private int id;

    /**
     * Creates a Contestant instantiation for running in a distributed
     * environment
     *
     * @param name of the contestant
     * @param team of the contestant
     * @param id of the contestant
     * @param strength of the contestant
     * @param bench interface to be used
     * @param playground interface to be used
     * @param refereeSite interface to be used
     * @param informationRepository interface to be used
     */
    public Contestant(String name, int team, int id, int strength,
            InterfaceContestantsBench bench,
            InterfacePlayground playground,
            InterfaceRefereeSite refereeSite,
            InterfaceGeneralInformationRepository informationRepository) {

        super(name);

        state = ContestantState.SEAT_AT_THE_BENCH;

        this.team = team;
        this.id = id;
        this.strength = strength;

        this.bench = bench;
        this.playground = playground;
        this.refereeSite = refereeSite;
        this.informationRepository = informationRepository;

    }

    @Override
    public ContestantState getContestantState() {
        return state;
    }

    @Override
    public void setContestantState(ContestantState state) {
        this.state = state;
    }

    @Override
    public int getContestantTeam() {
        return team;
    }

    @Override
    public void setContestantTeam(int team) {
        this.team = team;
    }

    @Override
    public int getContestantId() {
        return id;
    }

    @Override
    public void setContestantId(int id) {
        this.id = id;
    }

    @Override
    public int getContestantStrength() {
        return strength;
    }

    @Override
    public void setContestantStrength(int strength) {
        this.strength = strength;
    }

    @Override
    public void run() {
        try {
            informationRepository.updateContestant(id, team, state.getId(), strength);
            Tuple<Integer, Integer> addContestant = bench.addContestant(id, team, state.getId(), strength);

            while (!refereeSite.isMatchEnded()) {
                switch (state) {
                    case SEAT_AT_THE_BENCH:
                        followCoachAdvice();
                        break;
                    case STAND_IN_POSITION:
                        getReady();
                        break;
                    case DO_YOUR_BEST:
                        pullTheRope();
                        seatDown();
                        break;
                }
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
    

    /**
     * Contestant checks if is selected to the game. If so, goes to the
     * playground
     */
    private void followCoachAdvice() throws RemoteException{
        bench.getContestant(id, team);

        if (!refereeSite.isMatchEnded()) {
            int addContestant = playground.addContestant(id, team, state.getId(), strength);
            state = ContestantState.getStateById(addContestant);
        }
    }

    /**
     * Contestant gets ready. Changes the Contestant state to DO_YOUR_BEST
     */
    private void getReady() throws RemoteException{
        setContestantState(ContestantState.DO_YOUR_BEST);
        informationRepository.updateContestant(id, team, state.getId(), strength);
    }

    /**
     * Contestant pulls the rope
     */
    private void pullTheRope()throws RemoteException{
        playground.pullRope();
    }

    /**
     * If contestant was playing moves to his bench and changes his state to
     * SEAT_AT_THE_BENCH
     */
    private void seatDown() throws RemoteException{
        playground.getContestant(id, team);
        Tuple<Integer, Integer> addContestant = bench.addContestant(id, team, state.getId(), strength);

        state = ContestantState.getStateById(addContestant.getLeft());
        strength = addContestant.getRight();
    }

    @Override
    public int compareTo(InterfaceContestant contestant) {
        return getContestantId() - contestant.getContestantId();
    }

}
