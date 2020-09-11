

package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.Interfaces.PrivateCommand;

public class RoleZaubermeisterin extends Role {

    RoleZaubermeisterin() {
        super("Zaubermeisterin");
    }

    @Override
    public void executePreWW(Player zaubermeisterin, Game game, AutoState state) {
        MessagesMain.callZaubermeisterin(zaubermeisterin);

        PrivateCommand zaubermeisterinCommand = (event, parameters, msgChannel) -> {
            var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

            if (player != null) {
                // sends the mssg
                MessagesMain.showZaubermeisterin(zaubermeisterin, player);

                state.setDoneNight(zaubermeisterin);
                return true;

            } else {
                return false;
            }

        };
        game.addPrivateCommand(zaubermeisterin.user.getId(), zaubermeisterinCommand);
    }
}