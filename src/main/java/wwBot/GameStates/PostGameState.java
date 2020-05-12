package wwBot.GameStates;

import wwBot.Command;
import wwBot.Game;

public class PostGameState extends GameState {

    PostGameState(Game game){
        super(game);
        registerGameCommands();

    }

    //loads the Commands available in this GameState into the map gameStateCommands
    private void registerGameCommands(){
        //TODO: add al Commands needed here

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {
            
            msgChannel.createMessage("TODO: add help Command in Main State").block();
        };
        gameStateCommands.put("help", helpCommand);

        
    }
    
}