package wwBot.WerwolfGame.cards;

import wwBot.Globals;
import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;

public class RoleAuraSeherin extends Role {

    RoleAuraSeherin() {
        super("Aura-Seherin");
    }

    @Override
    public void executePreWW(Player auraSeherin, Game game, AutoState state) {
        MessagesWW.callAuraSeherin(auraSeherin);
        auraSeherin.user.getPrivateChannel().block().createMessage("TEST");

        PrivateCommand seherCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null) {
                // sends the mssg
                MessagesWW.showAuraSeherin(auraSeherin, player);

                state.setDoneNight(auraSeherin);
                return true;

            } else {
                return false;
            }

        };
        game.addPrivateCommand(auraSeherin.user.getId(), seherCommand);
    }
}