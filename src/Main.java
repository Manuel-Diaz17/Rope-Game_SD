
import ClientSide.Coach;
import ClientSide.Contestant;
import ClientSide.Referee;
import ServerSide.ContestantsBench;
import ServerSide.GeneralInformationRepository;
import ServerSide.Playground;
import ServerSide.RefereeSite;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ContestantsBench.getInstances();
        RefereeSite.getInstance();
        Playground.getInstance();
        GeneralInformationRepository informationRepository = GeneralInformationRepository.getInstance();

        Contestant[][] contestants = new Contestant[2][5];
        Contestant team1contestant1 = new Contestant("T1 player1",1,1, 10 + (int)(Math.random() * 20 - 10));
        Contestant team1contestant2 = new Contestant("T1 Player2",1,2, 10 + (int)(Math.random() * 20 - 10));
        Contestant team1contestant3 = new Contestant("T1 Player3",1,3, 10 + (int)(Math.random() * 20 - 10));
        Contestant team1contestant4 = new Contestant("T1 Player4",1,4, 10 + (int)(Math.random() * 20 - 10));
        Contestant team1contestant5 = new Contestant("T1 Player5",1,5, 10 + (int)(Math.random() * 20 - 10));

        System.out.println("Jogadores da equipa 1 adicionados.");

        Contestant team2contestant1 = new Contestant("T2 Player1",2,6, 10 + (int)(Math.random() * 20 - 10));
        Contestant team2contestant2 = new Contestant("T2 Player2",2,7, 10 + (int)(Math.random() * 20 - 10));
        Contestant team2contestant3 = new Contestant("T2 Player3",2,8, 10 + (int)(Math.random() * 20 - 10));
        Contestant team2contestant4 = new Contestant("T2 Player4",2,9, 10 + (int)(Math.random() * 20 - 10));
        Contestant team2contestant5 = new Contestant("T2 Player5",2,10, 10 + (int)(Math.random() * 20 - 10));

        System.out.println("Jogadores da equipa 2 adicionados.");

        Referee referee = new Referee("Referee");

        Coach[] coaches = new Coach[2];
        Coach coach1 = new Coach("T1 Coach", 1);
        Coach coach2 = new Coach("T2 Coach", 2);

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

        informationRepository.addCoach(coach1);
        informationRepository.addCoach(coach2);
        informationRepository.addReferee(referee);

        informationRepository.printHeader();

        coach1.start();
        team1contestant1.start();
        team1contestant2.start();
        team1contestant3.start();
        team1contestant4.start();
        team1contestant5.start();

        coach2.start();
        team2contestant1.start();
        team2contestant2.start();
        team2contestant3.start();
        team2contestant4.start();
        team2contestant5.start();

        System.out.println("XXXXXXXXXXXXXXXX");

        referee.start();

        System.out.println("XXXXXXXXXXXXXXXX");

        referee.join();

        System.out.println("XXXXXXXXXXXXXXXX");


        
        for (int i = 0; i < coaches.length; i++) {
            if (coaches[i] != null) {
                while (coaches[i].isAlive())
                    coaches[i].join();
        
                for (int j = 0; j < contestants[i].length; j++) {
                    if (contestants[i][j] != null) {
                        while (contestants[i][j].isAlive())
                            contestants[i][j].join();
                    }
                }
            }
        }

        System.out.println("\nO jogo terminou corretamente.");
    }
}
