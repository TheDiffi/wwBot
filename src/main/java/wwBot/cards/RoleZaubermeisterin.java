
package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.Interfaces.PrivateCommand;

public class RoleZaubermeisterin extends Role {
    private int seherinnenFound;

    RoleZaubermeisterin() {
        super("Zaubermeisterin");
    }

    @Override
    public void executePreWW(Player zaubermeisterin, Game game, AutoState state) {
        var amountSeherinnen = game.gameState.mapExistingRoles.get("Seherin").size();

        if (!(seherinnenFound == amountSeherinnen)) {
            MessagesMain.callZaubermeisterin(zaubermeisterin);

            PrivateCommand zaubermeisterinCommand = (event, parameters, msgChannel) -> {
                var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

                if (player != null) {

                    if (player.role.name.equals("Seherin")) {
                        seherinnenFound++;
                    }

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
}