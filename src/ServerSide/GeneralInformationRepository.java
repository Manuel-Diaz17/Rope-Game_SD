package ServerSide;

import java.util.LinkedList;
import java.util.List;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import Interfaces.InterfaceCoach;
import Interfaces.InterfaceCoach.CoachState;
import Interfaces.InterfaceContestant;
import Interfaces.InterfaceContestant.ContestantState;
import Interfaces.InterfaceGeneralInformationRepository;
import Interfaces.InterfaceReferee;
import Interfaces.InterfaceReferee.RefereeState;
import Interfaces.InterfaceRefereeSite.GameScore;
import Interfaces.Tuple;

/**
 * This is an passive class that logs entities activity
 */
public class GeneralInformationRepository implements InterfaceGeneralInformationRepository{
    private static GeneralInformationRepository instance;

    private final Lock lock;
    private PrintWriter printer;

    private final List<Tuple<ContestantState, Integer>[]> teams;
    private final CoachState[] coaches;
    private RefereeState refereeState;

    private final List<Integer> team1Placement;
    private final List<Integer> team2Placement;

    private int gameNumber;
    private int trialNumber;

    private int flagPosition;

    private boolean headerPrinted;
    private int shutdownVotes;

    /**
     * Gets an instance of the general information repository
     *
     * @return general information repository instance
     */
    public static synchronized GeneralInformationRepository getInstance() {
        if(instance == null)
            instance = new GeneralInformationRepository();

        return instance;
    }

    /**
     * Private constructor for the singleton
     */
    private GeneralInformationRepository() {
        lock = new ReentrantLock();

        try {
            printer = new PrintWriter("gameResults.log");
        } catch (FileNotFoundException ex) {
            printer = null;
        }

        headerPrinted = false;

        teams = new LinkedList<>();
        teams.add(new Tuple[5]);
        teams.add(new Tuple[5]);

        coaches = new CoachState[2];

        gameNumber = 1;
        trialNumber = 1;

        team1Placement = new LinkedList<>();
        team2Placement = new LinkedList<>();

        flagPosition = 0;

        shutdownVotes = 0;
    }

    @Override
    public void updateReferee() {
        InterfaceReferee referee = (InterfaceReferee) Thread.currentThread();

        lock.lock();
        try {
            refereeState = referee.getRefereeState();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void updateContestant() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        lock.lock();

        int team = contestant.getTeam() - 1;
        int id = contestant.getContestantId() - 1;

        try {
            this.teams.get(team)[id] = new Tuple<>(contestant.getContestantState(), contestant.getStrength());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateContestantStrength(int team, int id, int strength) {
        lock.lock();

        ContestantState state = teams.get(team - 1)[id - 1].getLeft();

        this.teams.get(team - 1)[id - 1] = new Tuple<>(state, strength);

        lock.unlock();
    }
    
    @Override
    public void updateCoach() {
        InterfaceCoach coach = (InterfaceCoach) Thread.currentThread();

        lock.lock();

        int team = coach.getTeam() - 1;

        try {
            this.coaches[team] = coach.getCoachState();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void setGameNumber(int gameNumber) {
        lock.lock();
        try {
            this.gameNumber = gameNumber;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setTrialNumber(int trialNumber) {
        lock.lock();
        try {
            this.trialNumber = trialNumber;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void setFlagPosition(int flagPosition) {
        lock.lock();
        try {
            this.flagPosition = flagPosition;
        } finally {
            lock.unlock();
        }
        
    }

    @Override
    public  void setTeamPlacement() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        lock.lock();
        try {
            if (contestant.getTeam() == 1)
                team1Placement.add(contestant.getContestantId());
            else if (contestant.getTeam() == 2)
                team2Placement.add(contestant.getContestantId());
        } finally {
            lock.unlock();
        }
        
    }

    @Override
    public void resetTeamPlacement() {
        InterfaceContestant contestant = (InterfaceContestant) Thread.currentThread();

        lock.lock();
        try {
            if (contestant.getTeam() == 1)
                team1Placement.remove(team1Placement.indexOf(contestant.getContestantId()));
            else if (contestant.getTeam() == 2)
                team2Placement.remove(team2Placement.indexOf(contestant.getContestantId()));
        } finally {
            lock.unlock();
        }
        
    }

    @Override
    public void printGameHeader() {
        lock.lock();
        try {
            printer.printf("Game %1d%n", gameNumber);
            printColumnHeader();
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

    @Override
    public void printLineUpdate() {
        lock.lock();
        try {
            if (headerPrinted){
                printActiveEntitiesStates();
                printTrialResult(trialNumber, flagPosition);
            
                printer.flush();
            }
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void printGameResult(GameScore score) {
        lock.lock();
        try {
            switch(score) {
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
                    printGameWinnerByPoints(gameNumber, 2);
                    break;
                case DRAW:
                    printGameDraw(gameNumber);
                    break;
            }
        } finally {
            lock.unlock();
        }
        
    }

    @Override
    public void printMatchWinner(int team, int score1, int score2) {
        lock.lock();
        try {
            printer.printf("Match was won by team %d (%d-%d).%n", team, score1, score2);
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

    @Override
    public void printMatchDraw() {
        lock.lock();
        try {
            printer.printf("Match was a draw.%n");
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

    @Override
    public void printLegend() {
        lock.lock();
        try {
            printer.printf("Legend:%n");
            printer.printf("Ref Sta - state of the referee%n");
            printer.printf("Coa # Stat - state of the coach of team # (# - 1 .. 2)%n");
            printer.printf("Cont # Sta - state of the contestant # (# - 1 .. 5) of team whose coach was listed to the immediate left%n");
            printer.printf("Cont # SG - strength of the contestant # (# - 1 .. 5) of team whose coach was listed to the immediate left%n");
            printer.printf("TRIAL - ? - contestant identification at the position ? at the end of the rope for present trial (? - 1 .. 3)%n");
            printer.printf("TRIAL - NB - trial number%n");
            printer.printf("TRIAL - PS - position of the centre of the rope at the beginning of the trial%n");
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

    @Override
    public void printHeader(){
        lock.lock();
        try {
            printer.printf("Game of the Rope - Description of the internal state%n");
            printer.printf("%n");
            printColumnHeader();
            printActiveEntitiesStates();
            printEmptyResult();
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

    /**
     * Prints game column header
     */
    private void printColumnHeader() {
        lock.lock();
        try {
            printer.printf("Ref Coa 1 Cont 1 Cont 2 Cont 3 Cont 4 Cont 5 Coa 2 Cont 1 Cont 2 Cont 3 Cont 4 Cont 5 Trial%n");
            printer.printf("Sta  Stat Sta SG Sta SG Sta SG Sta SG Sta SG  Stat Sta SG Sta SG Sta SG Sta SG Sta SG 3 2 1 . 1 2 3 NB PS%n");
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

    /**
     * Prints active entities states
     *
     * @return a single string with all states
     */
    private void printActiveEntitiesStates() {
        lock.lock();
        try {
            printer.printf("%3s", refereeState);
        
            // Printing teams state
            for (int i = 0; i < coaches.length; i++) {
                printer.printf("  %4s", coaches[i]);
        
                for (int j = 0; j < teams.get(i).length; j++) {
                    printer.printf(" %3s %2d", teams.get(i)[j].getLeft(), teams.get(i)[j].getRight());
                }
            }
        } finally {
            lock.unlock();
        }
        
    }

    /**
     * Prints an empty result
     */
    private void printEmptyResult() {
        lock.lock();
        try {
            printer.printf(" - - - . - - - -- --%n");
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

    /**
     * Prints trial result
     *
     * @param trialNumber number of the trial
     * @param flagPosition position of the flag
     * @return a String with all the information in a single string
     */
    private void printTrialResult(int trialNumber, int flagPosition) {
        lock.lock();
        try {
            for(int i = 0; i < 3; i++) {
                if(i >= team1Placement.size())
                    printer.printf(" -");
                else 
                    printer.printf(" %1d", team1Placement.get(i));
            }
            
            printer.printf(" .");
            
            for(int i = 0; i < 3; i++) {
                if(i >= team2Placement.size())
                    printer.printf(" -");
                else 
                    printer.printf(" %1d", team2Placement.get(i));
            }
            
            printer.printf(" %2d %2d%n", trialNumber, flagPosition);
        } finally {
            lock.unlock();
        }
        
    }

    /**
     * Prints a game winner by knock out
     *
     * @param game number of the game
     * @param team number of the team
     * @param trials in how many trials
     */
    private void printGameWinnerByKnockOut(int game, int team, int trials) {
        lock.lock();
        try {
            printer.printf("Game %d was won by team %d by knock out in %d trials.%n", game, team, trials);
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

     /**
     * Prints that a game was won by points
     *
     * @param game number of the game
     * @param team that won the game
     */
    private void printGameWinnerByPoints(int game, int team) {
        lock.lock();
        try {
            printer.printf("Game %d was won by team %d by points.%n", game, team);
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

    /**
     * Print that the game was a draw
     *
     * @param game number that was a draw
     */
    private void printGameDraw(int game) {
        lock.lock();
        try {
            printer.printf("Game %d was a draw.%n", game);
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }    

    @Override
    public void close() {
        lock.lock();
        try {
            printer.flush();
            printer.close();
        } finally {
            lock.unlock();
        }
        
    }

    @Override
    public boolean shutdown() {
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
