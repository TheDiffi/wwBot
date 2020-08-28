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
    public void execute(Game game, Player player) {
             var state = (AutoState) game.gameState;
             MessagesMain.günstlingMessage(player.user.getPrivateChannel().block(),
                    state.mapExistingRoles, game);

             // sets this roles state to done
             state.firstNight.endChecks.replace("Günstling", true);
             state.firstNight.endNightCheck();
           
    }
}