package ServerSide.Objects;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfaceRefereeSite.GameScore;
import Others.InterfaceContestant;
import Others.Tuple;
import Others.InterfaceCoach.CoachState;
import Others.InterfaceContestant.ContestantState;
import Others.InterfaceReferee.RefereeState;

/**
 * This is an passive class that logs entities activity
 */
public class GeneralInformationRepository implements InterfaceGeneralInformationRepository {

    private static GeneralInformationRepository instance; // singleton

    // locking condtions
    private final Lock lock;

    private PrintWriter printer;

    // variables to store current game status and update accordingly to changes
    private final List<Tuple<ContestantState, Integer>[]> teamsState;
    private final CoachState[] coachesState;        // coaches state tracking
    private RefereeState refereeState;              // referee state tracking
    private final List<Integer> team1Placement;     // list containing team contestants
    private final List<Integer> team2Placement;     // list containing team contestants
    private int gameNumber;                         // list containing scores of the game
    private int trialNumber;                        // list containing scores of the trial
    private int flagPosition;                       // current flag position
    private boolean headerPrinted;
    private int shutdownVotes;

    /**
     * Gets an instance of the general information repository
     *
     * @return general information repository instance
     */
    public static synchronized GeneralInformationRepository getInstance() {
        if (instance == null) {
            instance = new GeneralInformationRepository();
        }

        return instance;
    }

    /**
     * Public constructor for the singleton
     */
    public GeneralInformationRepository() {
        lock = new ReentrantLock();

        try {
            printer = new PrintWriter("gameResults.log");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneralInformationRepository.class.getName()).log(Level.SEVERE, null, ex);
            printer = null;
        }

        headerPrinted = false;

        teamsState = new LinkedList<>();
        teamsState.add(new Tuple[5]);
        teamsState.add(new Tuple[5]);

        coachesState = new CoachState[2];

        gameNumber = 1;
        trialNumber = 1;

        team1Placement = new LinkedList<>();
        team2Placement = new LinkedList<>();

        flagPosition = 0;

        shutdownVotes = 0;
    }

    @Override
    public void updateReferee(int status) throws RemoteException{

        lock.lock();

        refereeState = RefereeState.getStateById(status);

        lock.unlock();
    }

    @Override
    public void updateContestant(int id, int team, int status, int strength) throws RemoteException{

        lock.lock();

        this.teamsState.get(team-1)[id-1] = new Tuple<>(
                ContestantState.getStateById(status), 
                strength);

        lock.unlock();
    }

    @Override
    public void updateContestantStrength(int team, int id, int strength) throws RemoteException{

        lock.lock();

        ContestantState state = teamsState.get(team - 1)[id - 1].getLeft();

        this.teamsState.get(team - 1)[id - 1] = new Tuple<>(state, strength);

        lock.unlock();
    }

    @Override
    public void updateCoach(int team, int status) throws RemoteException{

        lock.lock();

        this.coachesState[team-1] = CoachState.getStateById(status);

        lock.unlock();
    }

    @Override
    public void setGameNumber(int gameNumber) throws RemoteException{
        lock.lock();

        this.gameNumber = gameNumber;

        lock.unlock();
    }

    @Override
    public void setTrialNumber(int trialNumber) throws RemoteException{
        lock.lock();

        this.trialNumber = trialNumber;

        lock.unlock();
    }

    @Override
    public void setFlagPosition(int flagPosition) throws RemoteException{
        lock.lock();

        this.flagPosition = flagPosition;

        lock.unlock();
    }

    @Override
    public void setTeamPlacement(int id, int team) throws RemoteException{

        lock.lock();

        switch (team) {
            case 1:
                team1Placement.add(id);
                break;
            case 2:
                team2Placement.add(id);
                break;
            default:
                System.out.println("Error: team number");
                break;
        }

        lock.unlock();
    }

    @Override
    public void resetTeamPlacement(int id, int team) throws RemoteException{
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        lock.lock();

        switch (team) {
            case 1:
                team1Placement.remove(team1Placement.indexOf(id));
                break;
            case 2:
                team2Placement.remove(team2Placement.indexOf(id));
                break;
            default:
                System.out.println("Error: team number");
                break;
        }

        lock.unlock();
    }

    @Override
    public void printGameHeader() throws RemoteException{
        lock.lock();

        printer.printf("Game %1d%n", gameNumber);
        printColumnHeader();
        printer.flush();

        lock.unlock();
    }

    @Override
    public void printLineUpdate() throws RemoteException{
        lock.lock();

        if (headerPrinted) {
            printActiveEntitiesStates();
            printTrialResult(trialNumber, flagPosition);

            printer.flush();
        }

        lock.unlock();
    }

    @Override
    public void printGameResult(GameScore score) throws RemoteException{
        lock.lock();

        switch (score) {
            case VICTORY_TEAM_1_BY_KNOCKOUT:
                printGameWinnerByKnockOut(gameNumber, 1, trialNumber);
                break;
            case VICTORY_TEAM_1_BY_POINTS:
                printGameWinnerByPoints(gameNumber, 1);
                break;
            case VICTORY_TEAM_2_BY_KNOCKOUT:
                printGameWinnerByKnockOut(gameNumber, 2, trialNumber);
                break;
            case VICTORY_TEAM_2_BY_POINTS:
                printGameWinnerByPoints(gameNumber, 1);
                break;
            case DRAW:
                printGameDraw(gameNumber);
                break;
        }

        lock.unlock();
    }

    @Override
    public void printMatchWinner(int team, int score1, int score2) throws RemoteException{
        lock.lock();

        printer.printf("Match was won by team %d (%d-%d).%n", team, score1, score2);
        printer.flush();

        lock.unlock();
    }

    @Override
    public void printMatchDraw() throws RemoteException{
        lock.lock();

        printer.printf("Match was a draw.%n");
        printer.flush();

        lock.unlock();
    }

    @Override
    public void printLegend() throws RemoteException{
        lock.lock();

        printer.printf("Legend:%n");
        printer.printf("Ref Sta – state of the referee%n");
        printer.printf("Coa # Stat - state of the coach of team # (# - 1 .. 2)%n");
        printer.printf("Cont # Sta – state of the contestant # (# - 1 .. 5) of team whose coach was listed to the immediate left%n");
        printer.printf("Cont # SG – strength of the contestant # (# - 1 .. 5) of team whose coach was listed to the immediate left%n");
        printer.printf("TRIAL – ? – contestant identification at the position ? at the end of the rope for present trial (? - 1 .. 3)%n");
        printer.printf("TRIAL – NB – trial number%n");
        printer.printf("TRIAL – PS – position of the centre of the rope at the beginning of the trial%n");
        printer.flush();

        lock.unlock();
    }

    @Override
    public void printHeader() throws RemoteException{
        lock.lock();

        printer.printf("Game of the Rope - Description of the internal state%n");
        printer.printf("%n");
        printColumnHeader();
        printActiveEntitiesStates();
        printEmptyResult();
        printer.flush();

        headerPrinted = true;

        lock.unlock();
    }

    /**
     * Prints game column header
     * 
     */
    private void printColumnHeader() throws RemoteException{
        lock.lock();

        printer.printf("Ref Coa 1 Cont 1 Cont 2 Cont 3 Cont 4 Cont 5 Coa 2 Cont 1 Cont 2 Cont 3 Cont 4 Cont 5 Trial%n");
        printer.printf("Sta  Stat Sta SG Sta SG Sta SG Sta SG Sta SG  Stat Sta SG Sta SG Sta SG Sta SG Sta SG 3 2 1 . 1 2 3 NB PS%n");
        printer.flush();

        lock.unlock();
    }

    /**
     * Prints active entities states
     */
    private void printActiveEntitiesStates() throws RemoteException{
        lock.lock();

        printer.printf("%3s", refereeState);

        // Printing teams state
        for (int i = 0; i < coachesState.length; i++) {
            printer.printf("  %4s", coachesState[i]);

            for (int j = 0; j < teamsState.get(i).length; j++) {
                printer.printf(" %3s %2d", teamsState.get(i)[j].getLeft(), teamsState.get(i)[j].getRight());
            }
        }

        lock.unlock();
    }

    /**
     * Prints an empty result
     */
    private void printEmptyResult() throws RemoteException{
        lock.lock();

        printer.printf(" - - - . - - - -- --%n");
        printer.flush();

        lock.unlock();
    }

    /**
     * Prints trial result
     *
     * @param trialNumber number of the trial
     * @param flagPosition position of the flag
     */
    private void printTrialResult(int trialNumber, int flagPosition) throws RemoteException{
        lock.lock();
        for (int i = 0; i < 3; i++) {
            if (i >= team1Placement.size()) {
                printer.printf(" -");
            } else {
                printer.printf(" %1d", team1Placement.get(i));
            }
        }

        printer.printf(" .");

        for (int i = 0; i < 3; i++) {
            if (i >= team2Placement.size()) {
                printer.printf(" -");
            } else {
                printer.printf(" %1d", team2Placement.get(i));
            }
        }

        printer.printf(" %2d %2d%n", trialNumber, flagPosition);

        lock.unlock();
    }

    /**
     * Prints a game winner by knock out
     *
     * @param game number of the game
     * @param team number of the team
     * @param trials in how many trials
     */
    private void printGameWinnerByKnockOut(int game, int team, int trials) throws RemoteException{
        lock.lock();

        printer.printf("Game %d was won by team %d by knock out in %d trials.%n", game, team, trials);
        printer.flush();

        lock.unlock();
    }

    /**
     * Prints that a game was won by points
     *
     * @param game number of the game
     * @param team that won the game
     */
    private void printGameWinnerByPoints(int game, int team) throws RemoteException{
        lock.lock();

        printer.printf("Game %d was won by team %d by points.%n", game, team);
        printer.flush();

        lock.unlock();
    }

    /**
     * Print that the game was a draw
     *
     * @param game number that was a draw
     */
    private void printGameDraw(int game) throws RemoteException{
        lock.lock();

        printer.printf("Game %d was a draw.%n", game);
        printer.flush();

        lock.unlock();
    }

    @Override
    public void close() throws RemoteException{
        lock.lock();

        printer.flush();
        printer.close();

        lock.unlock();
    }

    @Override
    public boolean shutdown() throws RemoteException{
        boolean result = false;

        lock.lock();

        shutdownVotes++;

        if (shutdownVotes == (1 + 2 * (1 + 5))) {
            result = true;
            close();
        }

        lock.unlock();

        return result;
    }
}
