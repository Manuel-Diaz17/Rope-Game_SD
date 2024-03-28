package SharingRegions;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.out;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import Entities.Coach;
import Entities.Contestant;
import Entities.Referee;
import Entities.Referee.RefereeState;
import SharingRegions.RefereeSite.GameScore;
import SharingRegions.RefereeSite.TrialScore;

public class GeneralInformationRepository {
    private static GeneralInformationRepository instance;

    private final Lock lock;
    private PrintWriter printer;

    private final Set<Contestant>[] teams;
    private final Set<Coach> coaches;
    private RefereeState refereeState;

    private final List<Integer> team1Placement;
    private final List<Integer> team2Placement;

    private int gameNumber;
    private int trialNumber;

    private int flagPosition;

    public static synchronized GeneralInformationRepository getInstance() {
        if(instance == null)
            instance = new GeneralInformationRepository();

        return instance;
    }

    private GeneralInformationRepository() {
        lock = new ReentrantLock();

        try {
            printer = new PrintWriter("gameResults.log");
        } catch (FileNotFoundException ex) {
            printer = null;
        }

        teams = new Set[2];
        teams[0] = new TreeSet<>();
        teams[1] = new TreeSet<>();
        coaches = new TreeSet<>();

        gameNumber = 1;
        trialNumber = 1;

        team1Placement = new LinkedList<>();
        team2Placement = new LinkedList<>();

        flagPosition = 0;
    }

    public void addReferee(Referee referee) {
        lock.lock();

        refereeState = referee.getRefereeState();

        lock.unlock();
    }

    public void addContestant(Contestant contestant) {
        lock.lock();

        this.teams[contestant.getTeam()-1].add(contestant);

        lock.unlock();
    }

    public void addCoach(Coach coach) {
        lock.lock();

        this.coaches.add(coach);

        lock.unlock();
    }

    public void setGameNumber(int gameNumber) {
        lock.lock();

        this.gameNumber = gameNumber;

        lock.unlock();
    }

    public void setTrialNumber(int trialNumber) {
        lock.lock();

        this.trialNumber = trialNumber;

        lock.unlock();
    }

    public void setFlagPosition(int flagPosition) {
        lock.lock();

        this.flagPosition = flagPosition;
        lock.unlock();
    }

    public  void setTeamPlacement() {
        Contestant contestant = (Contestant) Thread.currentThread();

        lock.lock();

        if(contestant.getTeam() == 1)
            team1Placement.add(contestant.getContestantId());
        else if(contestant.getTeam() == 2)
            team2Placement.add(contestant.getContestantId());

        lock.unlock();
    }

    public void resetTeamPlacement() {
        lock.lock();

        team1Placement.clear();
        team2Placement.clear();

        lock.unlock();
    }

    public void printGameHeader() {
        lock.lock();

        printer.printf("Game %1d%n", gameNumber);
        printColumnHeader();
        printer.flush();

        lock.unlock();
    }

    public void printLineUpdate() {
        Thread thread = Thread.currentThread();
        
        lock.lock();
        
        if(thread.getClass() == Contestant.class)
            addContestant((Contestant) thread);
        else if(thread.getClass() == Coach.class)
            addCoach((Coach) thread);
        else
            addReferee((Referee) thread);

        printActiveEntitiesStates();
        printTrialResult(trialNumber, flagPosition);

        printer.flush();

        lock.unlock();
    }

    public void printGameResult(GameScore score) {
        lock.lock();

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
                    printGameWinnerByPoints(gameNumber, 1);
                    break;
                case DRAW:
                    printGameDraw(gameNumber);
                    break;
        }

        lock.unlock();
    }

    public void printMatchWinner(int team, int score1, int score2) {
        lock.lock();

        printer.printf("Match was won by team %d (%d-%d).%n", team, score1, score2);
        printer.flush();

        lock.unlock();
    }

    public void printMatchDraw() {
        lock.lock();

        printer.printf("Match was a draw.%n");
        printer.flush();

        lock.unlock();
    }

    public void printLegend() {
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

    public void printHeader(){
        lock.lock();

        printer.printf("Game of the Rope - Description of the internal state%n");
        printer.printf("%n");
        printColumnHeader();       
        printActiveEntitiesStates();
        printEmptyResult();
        printer.flush();

        lock.unlock();
    }

    private void printColumnHeader() {
        lock.lock();

        printer.printf("Ref Coa 1 Cont 1 Cont 2 Cont 3 Cont 4 Cont 5 Coa 2 Cont 1 Cont 2 Cont 3 Cont 4 Cont 5 Trial%n");
        printer.printf("Sta  Stat Sta SG Sta SG Sta SG Sta SG Sta SG  Stat Sta SG Sta SG Sta SG Sta SG Sta SG 3 2 1 . 1 2 3 NB PS%n");
        printer.flush();

        lock.unlock();
    }

    private void printActiveEntitiesStates() {
        lock.lock();

        printer.printf("%3s", refereeState);

        // Printing teams state
        for(Coach coach : coaches) {
            printer.printf("  %4s", coach.getState());
            for(Contestant contestant : teams[coach.getTeam()-1]) {
                //esta linha está com os tuplos, tenho que voltar atrás para não os usar
                printer.printf(" %3s %2d", teamsState.get(i)[j].getLeft(), teamsState.get(i)[j].getRight());            }
        }

        lock.unlock();
    }

    private void printEmptyResult() {
        lock.lock();

        printer.printf(" - - - . - - - -- --%n");    
        printer.flush();

        lock.unlock();
    }

    private void printTrialResult(int trialNumber, int flagPosition) {
        lock.lock();

        for(int i = 0; i < 3; i++) {
            if(i >= team1Placement.size())
                printer.printf(" -");
            else 
                printer.printf(" %1d", team1Placement.get(i));        }
        
        printer.printf(" .");

        for(int i = 0; i < 3; i++) {
            if(i >= team2Placement.size())
                printer.printf(" -");
            else 
                printer.printf(" %1d", team2Placement.get(i));        }

        printer.printf(" %2d %2d%n", trialNumber, flagPosition);

        lock.unlock();
    }

    private void printGameWinnerByKnockOut(int game, int team, int trials) {
        lock.lock();
        
        printer.printf("Game %d was won by team %d by knock out in %d trials.%n", game, team, trials);
        printer.flush();

        lock.unlock();
    }

    private void printGameWinnerByPoints(int game, int team) {
        lock.lock();
        
        printer.printf("Game %d was won by team %d by points.%n", game, team);
        printer.flush();

        lock.unlock();
    }

    private void printGameDraw(int game) {
        lock.lock();
        
        printer.printf("Game %d was a draw.%n", game);
        printer.flush();

        lock.unlock();
    }    

    public void close() {
        lock.lock();

        printer.flush();
        printer.close();

        lock.unlock();
    }
}
