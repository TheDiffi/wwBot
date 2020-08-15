package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.Interfaces.PrivateCommand;

public class RoleSeher extends Role {

    RoleSeher() {
        super("Seher");
    }

    @Override
    public void execute(Game game, Player seher) {
        /// TODO: send mssg
        seher.user.getPrivateChannel().block().createMessage("TEST");

        PrivateCommand seherCommand = (event, parameters, msgChannel) -> {
            if (parameters == null || parameters.size() > 2) {
                MessagesMain.errorWrongSyntax(game, msgChannel);
                return false;
            } else {
                // finds the player
                var found = Globals.findPlayerByName(Globals.removeDash(parameters.get(0)), game.mapPlayers, game);

                if (found == null) {
                    MessagesMain.errorPlayerNotFound(msgChannel);
                    return false;
                }

                else {
                    // sends the mssg
                    MessagesMain.showSeher(seher, found, game);

                    // sets this roles state to done
                    var state = (AutoState) game.gameState;
                    state.firstNight.endChecks.replace("Seher", true);
                    state.firstNight.endNightCheck();
                    return true;
                }
            }
        };
        game.addPrivateCommand(seher.user.getId(), seherCommand);
    }
}