package wwBot.GameStates;

import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;

import wwBot.Game;
import wwBot.Globals;
import wwBot.Interfaces.Command;

public class Morning {

    public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    Game game;

    Morning(Game getGame) {
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
                MessagesMain.helpMorningMod(event);
            } else {
                MessagesMain.helpMorning(event);
            }

        };
        mapCommands.put("help", helpCommand);
        mapCommands.put("hilfe", helpCommand);

        // ends the Morning and begins the Day
        Command startVotingPhaseCommand = (event, parameters, msgChannel) -> {

            // compares the Snowflake of the Author to the Snowflake of the Moderator
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "Confirmed!", "");
                game.gameState.changeDayPhase();

            } else {
                msgChannel.createMessage("only the moderator can use this command");
            }
        };
        mapCommands.put("endMorning", startVotingPhaseCommand);
    }

}