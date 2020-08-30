package wwBot.cards;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.Player;

public class RoleGünstling extends Role {

    RoleGünstling() {
        super("Günstling");
    }

    @Override
    public void execute(Game game, Player player) {
        MessagesMain.günstlingMessage(player.user.getPrivateChannel().block(), game.gameState.mapExistingRoles, game);

        setDone(game, "Günstling");
    }
}