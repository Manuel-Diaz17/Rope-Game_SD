package ClientSide;

import Interfaces.InterfaceContestant;
import Interfaces.InterfaceContestantsBench;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfacePlayground;
import Interfaces.InterfaceRefereeSite;
import ServerSide.ContestantsBench;
import ServerSide.GeneralInformationRepository;
import ServerSide.Playground;
import ServerSide.RefereeSite;

public class Contestant extends Thread implements Comparable<InterfaceContestant>, InterfaceContestant {
    private ContestantState state;
    private int team;
    private int id;
    private int strength;

    private final InterfaceContestantsBench bench; // bench interface to be used
    private final InterfacePlayground playground; // playground interface to be used
    private final InterfaceRefereeSite refereeSite; // refereeSite interface to be used
    private final InterfaceGeneralInformationRepository informationRepository; // general Information Repository interface to be used

    /**
     * Creates a Contestant instantiation for running in a distributed
     * environment
     *
     * @param name of the contestant
     * @param team of the contestant
     * @param id of the contestant
     * @param strength of the contestant
     */
    public Contestant(String name, int team, int id, int strength) {
        super(name);

        state = ContestantState.SEAT_AT_THE_BENCH;

        this.team = team;
        this.id = id;
        this.strength = strength;

        bench = new ContestantsBenchStub(team);
        playground = new PlaygroundStub();
        refereeSite = new RefereeSiteStub();
        informationRepository = new GeneralInformationRepositoryStub();
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
    public int getContestantId() {
        return id;
    }

    @Override
    public void setContestantId(int id) {
        this.id = id;
    }

    public int getTeam() {
        return team;
    }

    @Override
    public void setTeam(int team) {
        this.team = team;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    @Override
    public void run() {
        informationRepository.updateContestant();
        bench.addContestant();

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
    }

    /**
     * Contestant checks if is selected to the game. If so, goes to the
     * playground
     */
    private void followCoachAdvice() {
        bench.getContestant();

        if(!refereeSite.isMatchEnded())
            playground.addContestant();
    }

     /**
     * Contestant gets ready. Changes the Contestant state to DO_YOUR_BEST
     */
    private void getReady() {
        setContestantState(ContestantState.DO_YOUR_BEST);
        informationRepository.updateContestant();
        informationRepository.printLineUpdate();
    }

    /**
     * Contestant pulls the rope
     */
    private void pullTheRope() {
        playground.pullRope();
    }

    /**
     * If contestant was playing moves to his bench and changes his state to
     * SEAT_AT_THE_BENCH
     */
    private void seatDown() {
        playground.getContestant();

        bench.addContestant();
    }

    @Override
    public int compareTo(InterfaceContestant contestant) {
        return getContestantId() - contestant.getContestantId();
    }
    
}