package wwBot.cards;

import wwBot.Game;
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
    public void executeFirstNight(Player dp,Game game, AutoState state){
        MessagesMain.triggerDoppelgängerin(game, dp);

        // registers a private commands which asks the player for the name of a player
        PrivateCommand dpCommand = (event, parameters, msgChannel) -> {
            if (parameters == null || !(parameters.size() == 1)) {
                MessagesMain.errorWrongSyntax(msgChannel);
                return false;

            }
            // finds the players
            var foundPlayer = game.findPlayerByName(parameters.get(0));
            if (foundPlayer != null) {
                // saves the player in boundTo
                boundTo = foundPlayer;

                //sends the mssg
                MessagesMain.doppelgängerinSuccess(game, dp, foundPlayer);

                state.setDoneNight(dp);
                return true;
            }

            MessagesMain.errorPlayerNotFound(msgChannel);
            return false;
        };
        game.addPrivateCommand(dp.user.getId(), dpCommand);
       
    }
}