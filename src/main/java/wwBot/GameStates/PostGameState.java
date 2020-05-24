package wwBot.GameStates;

import wwBot.Game;
import wwBot.Globals;
import wwBot.Interfaces.Command;

public class PostGameState extends GameState {

    PostGameState(Game game, int winner) {
        super(game);
        registerGameCommands();
        Globals.createMessage(game.mainChannel, "Statistics Coming Soon ", false);
        Globals.createMessage(game.mainChannel, "Vergiss nicht das Spiel mit \"&DeleteGame\" zu beenden ^^", false);
    }

    // loads the Commands available in this GameState into the map gameStateCommands
    private void registerGameCommands() {

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {

            msgChannel.createMessage("TODO: add help Command in Post Game State").block();
        };
        gameStateCommands.put("help", helpCommand);
        gameStateCommands.put("hilfe", helpCommand);

    }

}