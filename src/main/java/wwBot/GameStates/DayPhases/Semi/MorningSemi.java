package wwBot.GameStates.DayPhases.Semi;

import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.GameStates.MainState.DayPhase;
import wwBot.Interfaces.Command;

public class MorningSemi  {

    public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    Game game;

    public MorningSemi (Game getGame) {
        game = getGame;
        registerMorningCommands();
        MessagesMain.onMorningSemi(game);

    }

    private void registerMorningCommands() {

        // replys with pong!
        Command pingCommand = (event, parameters, msgChannel) -> {
            event.getMessage().getChannel().block().createMessage("Pong! Morning").block();
        };
        mapCommands.put("ping", pingCommand);

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {
            // replies to the moderator
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                MessagesMain.sendHelpMorningMod(msgChannel);
            } else {
                MessagesMain.sendHelpMorning(msgChannel);
            }

        };
        mapCommands.put("help", helpCommand);
        mapCommands.put("hilfe", helpCommand);


        // ends the Morning and begins the Day
        Command startVotingPhaseCommand = (event, parameters, msgChannel) -> {

            // compares the Snowflake of the Author to the Snowflake of the Moderator
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "Confirmed!", "");
                game.gameState.changeDayPhase(DayPhase.DAY);

            } else {
                MessagesMain.errorModOnlyCommand(msgChannel);
            }
        };
        mapCommands.put("endMorning", startVotingPhaseCommand);
    }

}