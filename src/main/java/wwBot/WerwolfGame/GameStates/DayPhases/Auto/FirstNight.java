package wwBot.WerwolfGame.GameStates.DayPhases.Auto;

import wwBot.Globals;
import wwBot.Interfaces.Command;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.GameStates.AutoState;
import wwBot.WerwolfGame.GameStates.MainState.DayPhase;

//---------------- FIRST NIGHT ----------------------------

public class FirstNight extends AutoDayPhase {

    private Game game;
    private AutoState state;


    public FirstNight(Game getGame) {
        game = getGame;
        state = (AutoState) game.gameState;
        MessagesWW.onFirstNightAuto(game);

        // loads the Commands of the state
        registerCommands();

        initiateRoles();

        Globals.sleepWCatch(game.avgDelaytime);
        MessagesWW.erwachenSpieler(game.mainChannel, state.pending);

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
            MessagesWW.sendHelpNight(msgChannel, true);
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

    public void nextNightPhase() {
        game.gameState.deleteWerwolfChat();
        game.gameState.changeDayPhase(DayPhase.DAY);

    }

}
