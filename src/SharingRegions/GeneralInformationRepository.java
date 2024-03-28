package SharingRegions;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.System.out;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import Entities.Coach;
import Entities.Contestant;
import Entities.Referee;
import SharingRegions.RefereeSite.GameScore;
import SharingRegions.RefereeSite.TrialScore;

public class GeneralInformationRepository {
    private static GeneralInformationRepository instance;

    private PrintWriter printer;

    private final Set<Contestant>[] teams;
    private final Set<Coach> coaches;
    private Referee referee;

    private final List<Integer> team1Placement;
    private final List<Integer> team2Placement;

    private final List<GameScore> gameScore;
    private final List<TrialScore> trialScore;

    private int flagPosition;

    public static synchronized GeneralInformationRepository getInstance() {
        if(instance == null)
            instance = new GeneralInformationRepository();

        return instance;
    }

    private GeneralInformationRepository() {

        try {
            printer = new PrintWriter("gameResults.log");
        } catch (FileNotFoundException ex) {
            printer = null;
        }

        teams = new Set[2];
        teams[0] = new TreeSet<>();
        teams[1] = new TreeSet<>();
        coaches = new TreeSet<>();

        gameScore = new LinkedList<>();
        trialScore = new LinkedList<>();

        team1Placement = new LinkedList<>();
        team2Placement = new LinkedList<>();

        flagPosition = 0;
    }

    public synchronized void addReferee(Referee referee) {
        this.referee = referee;
    }

    public synchronized void addContestant(Contestant contestant) {
        this.teams[contestant.getTeam()-1].add(contestant);
    }

    public synchronized void addCoach(Coach coach) {
        this.coaches.add(coach);
    }

    public synchronized void setGameScore(List<GameScore> gameScore) {
        this.gameScore.clear();
        this.gameScore.addAll(gameScore);
    }

    public synchronized void setTrialScore(List<TrialScore> trialScore) {
        this.trialScore.clear();
        this.trialScore.addAll(trialScore);
    }

    public synchronized void setFlagPosition(int flagPosition) {
        this.flagPosition = flagPosition;
    }

    public synchronized void setTeamPlacement() {
        Contestant contestant = (Contestant) Thread.currentThread();

        if(contestant.getTeam() == 1)
            team1Placement.add(contestant.getContestantId());
        else if(contestant.getTeam() == 2)
            team2Placement.add(contestant.getContestantId());
    }

    public synchronized void resetTeamPlacement() {
        team1Placement.clear();
        team2Placement.clear();
    }

    public synchronized void printGameHeader() {
        printer.printf("Game %1d%n", gameScore.size() + 1);
        printColumnHeader();
        printer.flush();
    }

    public synchronized void printLineUpdate() {
        printActiveEntitiesStates();
        printTrialResult(trialScore.size() + 1, flagPosition);
    }

    public synchronized void printGameResult() {
        switch(gameScore.get(gameScore.size()-1)) {
            case VICTORY_TEAM_1_BY_KNOCKOUT:
                printGameWinnerByKnockOut(gameScore.size(), 1, trialScore.size());
                break;
            case VICTORY_TEAM_1_BY_POINTS:
                printGameWinnerByPoints(gameScore.size(), 1);
                break;
            case VICTORY_TEAM_2_BY_KNOCKOUT:
                printGameWinnerByKnockOut(gameScore.size(), 2, trialScore.size());
                break;
            case VICTORY_TEAM_2_BY_POINTS:
                printGameWinnerByPoints(gameScore.size(), 1);
                break;
            case DRAW:
                printGameDraw(gameScore.size());
                break;
        }
    }

    public synchronized void printMatchWinner(int team, int score1, int score2) {
        printer.printf("Match was won by team %d (%d-%d).%n", team, score1, score2);
        printer.flush();
    }

    public synchronized void printMatchDraw() {
        printer.printf("Match was a draw.%n");
        printer.flush();
    }

    public synchronized void printLegend() {
        printer.printf("Legend:%n");
        printer.printf("Ref Sta – state of the referee%n");
        printer.printf("Coa # Stat - state of the coach of team # (# - 1 .. 2)%n");
        printer.printf("Cont # Sta – state of the contestant # (# - 1 .. 5) of team whose coach was listed to the immediate left%n");
        printer.printf("Cont # SG – strength of the contestant # (# - 1 .. 5) of team whose coach was listed to the immediate left%n");
        printer.printf("TRIAL – ? – contestant identification at the position ? at the end of the rope for present trial (? - 1 .. 3)%n");
        printer.printf("TRIAL – NB – trial number%n");
        printer.printf("TRIAL – PS – position of the centre of the rope at the beginning of the trial%n");
        printer.flush();
    }

    public synchronized void printHeader(){
        printer.printf("Game of the Rope - Description of the internal state%n");
        printer.printf("%n");
        printColumnHeader();       
        printActiveEntitiesStates();
        printEmptyResult();
        printer.flush();
    }

    private synchronized void printColumnHeader() {
        printer.printf("Ref Coa 1 Cont 1 Cont 2 Cont 3 Cont 4 Cont 5 Coa 2 Cont 1 Cont 2 Cont 3 Cont 4 Cont 5 Trial%n");
        printer.printf("Sta  Stat Sta SG Sta SG Sta SG Sta SG Sta SG  Stat Sta SG Sta SG Sta SG Sta SG Sta SG 3 2 1 . 1 2 3 NB PS%n");
        printer.flush();
    }

    private synchronized void printActiveEntitiesStates() {
        // Printing referee state
        printer.printf("%3s", referee.getRefereeState());

        // Printing teams state
        for(Coach coach : coaches) {
            printer.printf("  %4s", coach.getCoachState());

            for(Contestant contestant : teams[coach.getTeam()-1]) {
                printer.printf(" %3s %2d", contestant.getContestantState(), contestant.getStrength());
            }
        }

        printer.flush();
    }

    private synchronized void printEmptyResult() {
        printer.printf(" - - - . - - - -- --%n");    
        printer.flush();
    }

    private synchronized void printTrialResult(int trialNumber, int flagPosition) {
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

        printer.flush();
    }

    private synchronized void printGameWinnerByKnockOut(int game, int team, int trials) {
        printer.printf("Game %d was won by team %d by knock out in %d trials.%n", game, team, trials);
        printer.flush();
    }

    private synchronized void printGameWinnerByPoints(int game, int team) {
        printer.printf("Game %d was won by team %d by points.%n", game, team);
        printer.flush();
    }

    private synchronized void printGameDraw(int game) {
        printer.printf("Game %d was a draw.%n", game);
        printer.flush();
    }    

    public synchronized void close() {
        printer.flush();
        printer.close();
    }
}
