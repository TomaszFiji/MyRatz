
Based off the 1996 game Rats! by Sean O'Connor, built in Java and JavaFX.

## How to play

1. Pick or create a profile
2. Choose a map to play on (you'll need to beat the previous levels to play the next)
3. Kill the rats before they overwhelm you!

# Installing, Compiling and Executing
run project as maven project with goals: compile javafx:run
```sh
$ git clone https://github.com/TomaszFiji/MyRatz
$ cd MyRatz
$ mvn clean javafx:run
```



# Design
_Please note, as this was a coursework project, the final result was a modified version of design, and the design was not updated post-submission (it was completed before starting the programming)_

[Link to Design Documentation](https://chambray-comb-aa7.notion.site/Classes-762c7942fd6642d287cf4291f3afceba) - This contains UML diagrams, CRC cards for every class.

# Authors
#### [Greenfoot5](https://github.com/Greenfoot5) - Project Manager, Programmer, Minutes
- Helped coordinate the project, from the general design of classes to the end, bringing it all together
- Created the Main Menu and MOTD classes
- Enforcing Javadoc and Coding Conventions specification of the coursework
- Responsible for meeting minutes

#### [Vilija](https://github.com/cornerOfTheMoon) - Programmer, Artist
- Designed and implemented LevelController class
- Implemented GameObject class
- Designed all sprites in the game

#### [Jems-M](https://github.com/Jems-M) - Programmer, Level Design
- Designed and implemented all Rat classes and LevelFileReader
- Created level file formats
- Designed all the levels currently in the game

#### [Nhysalotep](https://github.com/Nhysalotep) - Programmer, SoundFX
- Designed and implemented all Tile classes
- Produced several sounds for the game, including death rat spawns and sterilisation

#### [TomaszFiji](https://github.com/TomaszFiji) - Programmer, SoundFX
- Designed and implemented Profiles and their classes/files
- Developed a High Score system
- Designed a style sheet for various menus including main menu and level selection
- Designed and implemented everything connected to multi player mode

#### [translibrius](https://github.com/translibrius) - Programmer, UI/UX
- Designed and implemented all Power classes, along with the music and sound player (SeaShantySimulator)
- Produced or found sounds and music for the game
