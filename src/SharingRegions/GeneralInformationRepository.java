package SharingRegions;

import java.util.List;
import static java.lang.System.out;

import Entities.Coach;
import Entities.Contestant;
import Entities.Referee;

public class GeneralInformationRepository {
    public void printHeader(){
        out.println("Game of the Rope - Description of the internal state");
    }

    public void printGameHeader(){
        out.println("Ref Coa 1 Cont 1 Cont 2 Cont 3 Cont 4 Cont 5 Coa 2 Cont 1 Cont 2 Cont 3 Cont 4 Cont 5 Trial");
        out.println("Sta  Stat Sta SG Sta SG Sta SG Sta SG Sta SG Stat Sta SG Sta SG Sta SG Sta SG Sta SG 3 2 1 . 1 2 3 NB PS");
    }

    public void printGameResult(Coach coach1, Coach coach2, Referee referee){

        out.print(referee.getRefereeState().getId() + "  ");
        out.print(coach1.getCoachState().getId() + " ");

        ContestantsBench contestantbench = ContestantsBench.getInstance(1);
        List<Contestant> bench = contestantbench.getBench();
        for(Contestant contestant : bench)
            out.print(contestant.getContestantState().getId() + " ");

        contestantbench = ContestantsBench.getInstance(2);
        bench = contestantbench.getBench();
        for(Contestant contestant : bench)
            out.print(contestant.getContestantState().getId() + " ");

        RefereeSite refereesite = RefereeSite.getInstance();
        // MISSING PRINT: "contestant identification at the position ? at the end of the rope for present trial"
        out.print(refereesite.getTrialRound() + " ");

        Playground playground = Playground.getInstance();
        out.print(playground.getLastFlagPosition());
    }

    public void printGameNumber(){

        out.print("Game ");
        out.println(gamenumber);
    }

    public void printGameWinner(){

    }

    public void printLegend() {
        out.println("Legend:");
        out.println("Ref Sta – state of the referee");
        out.println("Coa # Stat - state of the coach of team # (# - 1 .. 2)");
        out.println("Cont # Sta – state of the contestant # (# - 1 .. 5) of team whose coach was listed to the immediate left");
        out.println("Cont # SG – strength of the contestant # (# - 1 .. 5) of team whose coach was listed to the immediate left");
        out.println("TRIAL – ? – contestant identification at the position ? at the end of the rope for present trial (? - 1 .. 3)");
        out.println("TRIAL – NB – trial number");
        out.println("TRIAL – PS – position of the centre of the rope at the beginning of the trial");
    }
}
