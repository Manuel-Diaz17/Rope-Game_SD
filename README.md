# Rope-Game_SD
 Project for the Distributed Systems class 2023/2024

# Table of Contents

1. [Introduction](#intro)
2. [Diagrams](#diagrams)
   - [Referee life cycle](#referee-life-cycle)
   - [Coaches life cycle](#coaches-life-cycle)
   - [Contestants life cycle](#contestants-life-cycle)
3. [How to run example 2](#how-to-run-example-2)
4. [How to run example 3](#tw-to-run-example-3)
5. [How to run example 4](#how-to-run-example-4)
9. [Documentation](#documentation)

## Introduction: <a name="intro"></a>
One of the most popular traditional games is the Game of the Rope. Two teams of contestants face
each other on a playground trying to decide which one is the strongest by pulling at opposite ends of a
rope. If both teams are equally strong, the rope does not move, a standstill occurs and there is a draw; if
however one of the teams is stronger than the other, the rope moves in the direction of the stronger team
so much faster as the strength difference is larger and this team wins.

A variation of this game will be assumed here. A match is composed of three games and each game
may take up to six trials. A game win is declared by asserting the position of a mark placed at the middle
of the rope after six trials. The game may end sooner if the produced shift is greater or equal to four
length units. We say in this case that the victory was won by knock out, otherwise, it will be a victory by
points.

A team has five elements, but only three compete at each trial. Member selection for the trial is carried
out by the team's coach. He decides who will join for next trial according to some predefined strategy.
Each contestant will loose one unit of strength when he is pulling the rope and will gain one unit when he
is seating at the bench. Somehow the coach perceives the physical state of each team member and may
use this information to substantiate his decision.


##  Diagrams of the Entities lyfe cycle:<a name="diagrams"></a>



### Referee life cycle:<a name="referee-life-cycle"></a>


### Coaches life cycle:<a name="coaches-life-cycle"></a>


### Contestants life cycle:<a name="contestants-life-cycle"></a>










## Documentation <a name="documentation"></a>



[Lab work n◦1](https://elearning.ua.pt/pluginfile.php/4438659/mod_resource/content/2/trab1.pdf)