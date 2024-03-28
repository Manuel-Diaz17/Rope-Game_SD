package Game;

import Entities.Coach;
import Entities.Contestant;
import Entities.Referee;
import SharingRegions.ContestantsBench;
import SharingRegions.GeneralInformationRepository;

public class Main {
    public static void main(String[] args) {
        ContestantsBench bench = ContestantsBench.getInstance(1);
        Contestant player1 = new Contestant("Tiago", 1, 0,  50);
        Contestant player2 = new Contestant("João",  1, 1, 150);
        Contestant player3 = new Contestant("Tito",  1, 2, 200);
        Contestant player4 = new Contestant("Tojo",  1, 3, 250);

        bench.addContestant(player1);
        bench.addContestant(player2);
        bench.addContestant(player3);
        bench.addContestant(player4);

        System.out.println("Testing");

        bench.sort();

        System.out.println("Testing2");

        Coach coach = new Coach("Mourinho",1);

        coach.callContestants();

        System.out.println("testing3");

        coach.reviewNotes();

        System.out.println("testing3");

        Referee referee = new Referee("Arbitruuu");

        GeneralInformationRepository gir = new GeneralInformationRepository();
        gir.printHeader();
        gir.printGameHeader();
        gir.printGameResult(coach, coach, referee);
        gir.printLegend();
    }
}
