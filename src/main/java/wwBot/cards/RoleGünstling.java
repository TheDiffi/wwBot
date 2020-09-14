package wwBot.cards;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;

public class RoleGünstling extends Role {

    RoleGünstling() {
        super("Günstling");
    }

    @Override
    public void executeFirstNight(Player player, Game game, AutoState state) {
        MessagesMain.günstlingMessage(player.user.getPrivateChannel().block(), game.gameState.mapExistingRoles, game);

        state.setDoneNight(player);
    }
}