

package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.Interfaces.PrivateCommand;

public class RoleZaubermeisterin extends Role {

    RoleZaubermeisterin() {
        super("Zaubermeisterin");
    }

    @Override
    public void execute(Game game, Player zaubermeisterin) {
        MessagesMain.callZaubermeisterin(zaubermeisterin);

        PrivateCommand zaubermeisterinCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null) {
                // sends the mssg
                MessagesMain.showZaubermeisterin(zaubermeisterin, player);

                setDone(game, "Zaubermeisterin");
                return true;

            } else {
                return false;
            }

        };
        game.addPrivateCommand(zaubermeisterin.user.getId(), zaubermeisterinCommand);
    }
}