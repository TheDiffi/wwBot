package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.Interfaces.PrivateCommand;

public class RoleAuraSeherin extends Role {

    RoleAuraSeherin() {
        super("Aura-Seherin");
    }

    @Override
    public void execute(Game game, Player auraSeherin) {
        MessagesMain.callAuraSeherin(auraSeherin);
        auraSeherin.user.getPrivateChannel().block().createMessage("TEST");

        PrivateCommand seherCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null) {
                // sends the mssg
                MessagesMain.showAuraSeherin(auraSeherin, player, game);

                setDone(game, "Aura-Seherin");
                return true;

            } else {
                return false;
            }

        };
        game.addPrivateCommand(auraSeherin.user.getId(), seherCommand);
    }
}