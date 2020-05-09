package wwBot.GameStates;

import wwBot.Game;

public class MainGameState extends GameState {

    MainGameState(Game game){
        super(game);
        registerGameCommands();

    }

    //loads the Commands available in this GameState into the map gameStateCommands
    private void registerGameCommands(){
        //TODO: add al Commands needed here


    }

    //TODO: add Day Night Cycle
}