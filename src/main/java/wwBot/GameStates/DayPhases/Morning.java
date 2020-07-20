package wwBot.GameStates.DayPhases;

import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Interfaces.Command;

public class Morning {

    public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    Game game;

    public Morning(Game getGame) {
        game = getGame;
        registerDayCommands();

    }

    private void registerDayCommands() {

        // replys with pong!
        Command pingCommand = (event, parameters, msgChannel) -> {
            event.getMessage().getChannel().block().createMessage("Pong! Morning").block();
        };
        mapCommands.put("ping", pingCommand);

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {
            // replies to the moderator
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                MessagesMain.helpMorningMod(msgChannel);
            } else {
                MessagesMain.helpMorning(msgChannel);
            }

        };
        mapCommands.put("help", helpCommand);
        mapCommands.put("hilfe", helpCommand);

        // zeigt die verfügbaren commands
        Command showCommandsCommand = (event, parameters, msgChannel) -> {
            var mssg = MessagesMain.showCommandsMain();
            mssg = "\n" + MessagesMain.showCommandsGame();
            mssg = "\n" + MessagesMain.showCommandsSemiMainGameState();
            mssg = "\n" + MessagesMain.showCommandsMorning();
            msgChannel.createMessage(mssg);
        };
        mapCommands.put("showCommands", showCommandsCommand);

        // ends the Morning and begins the Day
        Command startVotingPhaseCommand = (event, parameters, msgChannel) -> {

            // compares the Snowflake of the Author to the Snowflake of the Moderator
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "Confirmed!", "");
                game.gameState.changeDayPhase();

            } else {
                MessagesMain.errorModOnlyCommand(msgChannel);
            }
        };
        mapCommands.put("endMorning", startVotingPhaseCommand);
    }

}