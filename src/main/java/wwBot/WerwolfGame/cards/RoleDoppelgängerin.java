package wwBot.WerwolfGame.cards;

import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;

public class RoleDoppelgängerin extends Role {
    public Player boundTo;
    public boolean transformed = false;

    RoleDoppelgängerin() {
        super("Doppelgängerin");
    }

    @Override
    public void executeFirstNight(Player dp, Game game, AutoState state) {
        MessagesWW.triggerDoppelgängerin(game, dp);

        // registers a private commands which asks the player for the name of a player
        PrivateCommand dpCommand = (event, parameters, msgChannel) -> {
            if (parameters == null || !(parameters.size() == 1)) {
                MessagesWW.errorWrongSyntax(msgChannel);
                return false;

            }
            // finds the players
            var foundPlayer = game.findPlayerByName(parameters.get(0));

            if (foundPlayer != null) {
                //checks if player chose himself
                if (foundPlayer.user.getId().equals(dp.user.getId())) {
                    MessagesWW.errorChoseSelf(msgChannel);
                    return false;

                } else {
                    // saves the player in boundTo
                    boundTo = foundPlayer;

                    // sends the mssg
                    MessagesWW.doppelgängerinSuccess(game, dp, foundPlayer);

                    state.setDoneNight(dp);
                    return true;
                }
            }

            MessagesWW.errorPlayerNotFound(msgChannel);
            return false;
        };
        game.addPrivateCommand(dp.user.getId(), dpCommand);

    }
}