package wwBot.GameStates;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Interfaces.Command;

import java.awt.Color;

public class PostGameState extends GameState {
    //IDEA: parse every morning a summary of the night (including: death state, who saved who, ect. ) into a json or smthing and use these stats in post state


    PostGameState(Game game, int winner) {
        super(game);
        registerGameCommands();
        
    }

    public void start() {
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

        // zeigt die verfügbaren commands
        Command showCommandsCommand = (event, parameters, msgChannel) -> {
            var mssg = MessagesMain.getCommandsMain();
            mssg += "\n" + MessagesMain.getCommandsGame();
            mssg += "\n" + MessagesMain.getCommandsPostGame();
            mssg += "\n" + MessagesMain.getHelpInfo();

            Globals.createEmbed(msgChannel, Color.CYAN, "Commands", mssg);
        };
        gameStateCommands.put("showCommands", showCommandsCommand);
        gameStateCommands.put("lsCommands", showCommandsCommand);


    }

    public void close() {

    }

}