
package wwBot.WerwolfGame.cards;

import wwBot.Globals;
import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;

public class RoleZaubermeisterin extends Role {
    private int seherinnenFound;

    RoleZaubermeisterin() {
        super("Zaubermeisterin");
    }

    @Override
    public void executePreWW(Player zaubermeisterin, Game game, AutoState state) {
        var amountSeherinnen = game.gameState.mapExistingRoles.get("Seherin").size();

        if (!(seherinnenFound == amountSeherinnen)) {
            MessagesWW.callZaubermeisterin(zaubermeisterin);

            PrivateCommand zaubermeisterinCommand = (event, parameters, msgChannel) -> {
                var player = Globals.commandPlayerFinder(event, parameters, msgChannel, game);

                if (player != null) {

                    if (player.role.name.equals("Seherin")) {
                        seherinnenFound++;
                    }

                    // sends the mssg
                    MessagesWW.showZaubermeisterin(zaubermeisterin, player);

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