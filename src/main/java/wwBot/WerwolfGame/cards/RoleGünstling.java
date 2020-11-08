package wwBot.WerwolfGame.cards;

import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;

public class RoleGünstling extends Role {

    RoleGünstling() {
        super("Günstling");
    }

    @Override
    public void executeFirstNight(Player player, Game game, AutoState state) {
        MessagesWW.günstlingMessage(player.user.getPrivateChannel().block(), game.gameState.mapExistingRoles, game);

        state.setDoneNight(player);
    }
}