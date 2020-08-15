package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.Interfaces.PrivateCommand;

public class RoleDoppelgängerin extends Role {
    public Player boundTo;
    public boolean transformed = false;

    RoleDoppelgängerin() {
        super("Doppelgängerin");
    }

    @Override
    public void execute(Game game, Player dp){
        /// TODO: send mssg
        dp.user.getPrivateChannel().block().createMessage("TEST");

        // registers a private commands which asks the player for the name of a player
        PrivateCommand dpCommand = (event, parameters, msgChannel) -> {
            if (parameters == null || !(parameters.size() == 1)) {
                MessagesMain.errorWrongSyntax(game, msgChannel);
                return false;

            }
            // finds the players
            var foundPlayer = Globals.findPlayerByName(Globals.removeDash(parameters.get(0)), game.mapPlayers,
                    game);
            if (foundPlayer != null) {
                // saves the player in boundTo
                boundTo = foundPlayer;

                //sends the mssg
                MessagesMain.doppelgängerinSuccess(game, dp, foundPlayer);

                //sets this roles state to done
                var state = (AutoState) game.gameState;
                state.firstNight.endChecks.replace("Doppelgängerin", true);
                state.firstNight.endNightCheck();
                return true;
            }

            MessagesMain.errorPlayerNotFound(msgChannel);
            return false;
        };
        game.addPrivateCommand(dp.user.getId(), dpCommand);
       
    }
}