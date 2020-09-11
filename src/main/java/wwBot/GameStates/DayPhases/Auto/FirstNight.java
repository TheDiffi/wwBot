package wwBot.GameStates.DayPhases.Auto;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.GameStates.AutoState;
import wwBot.GameStates.MainState.DayPhase;
import wwBot.Interfaces.Command;

//---------------- FIRST NIGHT ----------------------------

public class FirstNight extends AutoDayPhase {

    private Game game;

    public FirstNight(Game getGame) {
        game = getGame;
        MessagesMain.onFirstNightAuto(game);

        // loads the Commands of the state
        registerCommands();

        initiateRoles();
    }

    // loads all of the following Commands into mapCommands
    public void registerCommands() {

        // replys with pong!
        Command pingCommand = (event, parameters, msgChannel) -> {
            event.getMessage().getChannel().block().createMessage("Pong! FirstNightPhase").block();
        };
        mapCommands.put("ping", pingCommand);

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {
            MessagesMain.sendHelpNight(msgChannel, true);
        };
        mapCommands.put("help", helpCommand);
        mapCommands.put("hilfe", helpCommand);

    }

    private void initiateRoles() {

        // executes for every single card
        for (var player : game.mapPlayers.values()) {

            var state = (AutoState) game.gameState;
            state.setPending(player);
            player.role.executeFirstNight(player, game, state);

        }

    }

    public void changeNightPhase() {
        game.gameState.changeDayPhase(DayPhase.DAY);

    }

}
