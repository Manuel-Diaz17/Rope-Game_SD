package Game;

import java.security.SecureRandom;
import java.util.Scanner;

import Entities.Coach;
import Entities.Contestant;
import Entities.Referee;
import SharingRegions.ContestantsBench;
import SharingRegions.GeneralInformationRepository;

public class Main {
    public static void main(String[] args) {
        SecureRandom rand = new SecureRandom();

        Contestant team1contestant1 = new Contestant("T1 player1",1,1, (int) (rand.nextInt(2) * (3000 - 1000) + 1000));
        Contestant team1contestant2 = new Contestant("T1 Player2",1,2, (int) (rand.nextInt(2) * (3000 - 1000) + 1000));
        Contestant team1contestant3 = new Contestant("T1 Player3",1,3, (int) (rand.nextInt(2) * (3000 - 1000) + 1000));
        Contestant team1contestant4 = new Contestant("T1 Player4",1,4, (int) (rand.nextInt(2) * (3000 - 1000) + 1000));
        Contestant team1contestant5 = new Contestant("T1 Player5",1,5, (int) (rand.nextInt(2) * (3000 - 1000) + 1000));

        System.out.println("Jogadores da equipa 1 adicionados.");

        Contestant team2contestant1 = new Contestant("T2 Player1",2,6, (int) (rand.nextInt(2) * (3000 - 1000) + 1000));
        Contestant team2contestant2 = new Contestant("T2 Player2",2,7, (int) (rand.nextInt(2) * (3000 - 1000) + 1000));
        Contestant team2contestant3 = new Contestant("T2 Player3",2,8, (int) (rand.nextInt(2) * (3000 - 1000) + 1000));
        Contestant team2contestant4 = new Contestant("T2 Player4",2,9, (int) (rand.nextInt(2) * (3000 - 1000) + 1000));
        Contestant team2contestant5 = new Contestant("T2 Player5",2,10, (int) (rand.nextInt(2) * (3000 - 1000) + 1000));

        System.out.println("Jogadores da equipa 2 adicionados.");

        Referee rf = new Referee("Referee");

        Coach coach1 = new Coach("T1 Coach", 1, new MostStrengthStrategy());
        Coach coach2 = new Coach("T2 Coach", 2, new MostStrengthStrategy());

        Scanner sc = new Scanner(System.in);
        sc.next();

        GeneralInformationRepository informationRepository = GeneralInformationRepository.getInstance();

        informationRepository.addContestant(team1contestant1);
        informationRepository.addContestant(team1contestant2);
        informationRepository.addContestant(team1contestant3);
        informationRepository.addContestant(team1contestant4);
        informationRepository.addContestant(team1contestant5);

        informationRepository.addContestant(team2contestant1);
        informationRepository.addContestant(team2contestant2);
        informationRepository.addContestant(team2contestant3);
        informationRepository.addContestant(team2contestant4);
        informationRepository.addContestant(team2contestant5);

        team1contestant1.start();
        team1contestant2.start();
        team1contestant3.start();
        team1contestant4.start();
        team1contestant5.start();

        team2contestant1.start();
        team2contestant2.start();
        team2contestant3.start();
        team2contestant4.start();
        team2contestant5.start();

        informationRepository.addCoach(coach1);
        informationRepository.addCoach(coach2);
        informationRepository.addReferee(rf);

        informationRepository.printHeader();

        coach1.start();
        coach2.start();
        rf.start();

        rf.join();
        coach1.join();
        coach2.join();

        team1contestant1.join();
        team1contestant2.join();
        team1contestant3.join();
        team1contestant4.join();
        team1contestant5.join();

        team2contestant1.join();
        team2contestant2.join();
        team2contestant3.join();
        team2contestant4.join();
        team2contestant5.join();
    }
}
