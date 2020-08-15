package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.Interfaces.PrivateCommand;

public class RoleAmor extends Role {

    RoleAmor() {
        super("Amor");
    }

    @Override
    public void execute(Game game, Player amor) {
        MessagesMain.triggerAmor(game);

        PrivateCommand amorCommand = (event, parameters, msgChannel) -> {

            if (parameters == null || parameters.size() != 2) {
                MessagesMain.errorWrongSyntax(game, msgChannel);
                return false;
            } else {
                // finds the players
                var player1 = Globals.findPlayerByName(Globals.removeDash(parameters.get(0)), game.mapPlayers, game);
                var player2 = Globals.findPlayerByName(Globals.removeDash(parameters.get(1)), game.mapPlayers, game);

                if (player1 == null || player2 == null) {
                    MessagesMain.errorPlayerNotFound(msgChannel);
                    return false;

                } else if (player1 == player2) {
                    MessagesMain.errorPlayersIdentical(msgChannel);
                    return false;

                } else {
                    // sets the "inLoveWith" variables
                    player1.role.inLoveWith = player2;
                    player2.role.inLoveWith = player1;

                    // sends a mssg
                    MessagesMain.amorSuccess(game, player1, player2);

                    // sets this roles state to done
                    var state = (AutoState) game.gameState;
                    state.firstNight.endChecks.replace("Amor", true);
                    state.firstNight.endNightCheck();
                    return true;

                }
            }
        };
        game.addPrivateCommand(amor.user.getId(), amorCommand);

    }
}