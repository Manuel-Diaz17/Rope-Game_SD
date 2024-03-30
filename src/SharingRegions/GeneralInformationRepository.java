package SharingRegions;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import Entities.Coach;
import Entities.Contestant;
import Entities.Referee;
import Entities.Referee.RefereeState;
import SharingRegions.RefereeSite.GameScore;

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
        try {
            refereeState = referee.getRefereeState();
        } finally {
            lock.unlock();
        }
    }
    
    public void addContestant(Contestant contestant) {
        lock.lock();
        try {
            this.teams[contestant.getTeam() - 1].add(contestant);
        } finally {
            lock.unlock();
        }
    }
    
    public void addCoach(Coach coach) {
        lock.lock();
        try {
            this.coaches.add(coach);
        } finally {
            lock.unlock();
        }
    }
    
    public void setGameNumber(int gameNumber) {
        lock.lock();
        try {
            this.gameNumber = gameNumber;
        } finally {
            lock.unlock();
        }
    }
    
    public void setTrialNumber(int trialNumber) {
        lock.lock();
        try {
            this.trialNumber = trialNumber;
        } finally {
            lock.unlock();
        }
    }
    

    public void setFlagPosition(int flagPosition) {
        lock.lock();
        try {
            this.flagPosition = flagPosition;
        } finally {
            lock.unlock();
        }
        
    }

    public  void setTeamPlacement() {
        Contestant contestant = (Contestant) Thread.currentThread();

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

    public void resetTeamPlacement() {
        lock.lock();
        try {
            team1Placement.clear();
            team2Placement.clear();
        } finally {
            lock.unlock();
        }
        
    }

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

    public void printLineUpdate() {
        Thread thread = Thread.currentThread();

        lock.lock();
        try {
            if (thread instanceof Contestant)
                addContestant((Contestant) thread);
            else if (thread instanceof Coach)
                addCoach((Coach) thread);
            else if (thread instanceof Referee)
                addReferee((Referee) thread);
        
            printActiveEntitiesStates();
            printTrialResult(trialNumber, flagPosition);
        
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

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

    public void printMatchWinner(int team, int score1, int score2) {
        lock.lock();
        try {
            printer.printf("Match was won by team %d (%d-%d).%n", team, score1, score2);
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

    public void printMatchDraw() {
        lock.lock();
        try {
            printer.printf("Match was a draw.%n");
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

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

    private void printActiveEntitiesStates() {
        lock.lock();
        try {
            printer.printf("%3s", refereeState);
        
            // Printing teams state
            for (Coach coach : coaches) {
                printer.printf("  %4s", coach.getCoachState());
        
                for (Contestant contestant : teams[coach.getTeam() - 1]) {
                    printer.printf(" %3s %2d", contestant.getContestantState(), contestant.getStrength());
                }
            }
        } finally {
            lock.unlock();
        }
        
    }

    private void printEmptyResult() {
        lock.lock();
        try {
            printer.printf(" - - - . - - - -- --%n");
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

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

    private void printGameWinnerByKnockOut(int game, int team, int trials) {
        lock.lock();
        try {
            printer.printf("Game %d was won by team %d by knock out in %d trials.%n", game, team, trials);
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

    private void printGameWinnerByPoints(int game, int team) {
        lock.lock();
        try {
            printer.printf("Game %d was won by team %d by points.%n", game, team);
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }

    private void printGameDraw(int game) {
        lock.lock();
        try {
            printer.printf("Game %d was a draw.%n", game);
            printer.flush();
        } finally {
            lock.unlock();
        }
        
    }    

    public void close() {
        lock.lock();
        try {
            printer.flush();
            printer.close();
        } finally {
            lock.unlock();
        }
        
    }
}
