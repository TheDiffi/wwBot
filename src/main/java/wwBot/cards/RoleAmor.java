package wwBot.cards;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.Interfaces.PrivateCommand;

public class RoleAmor extends Role {

    RoleAmor() {
        super("Amor");
    }

    @Override
    public void executeFirstNight(Player amor,Game game, AutoState state) {
        MessagesMain.triggerAmor(game, amor);

        PrivateCommand amorCommand = (event, parameters, msgChannel) -> {

            if (parameters == null || parameters.size() != 2) {
                MessagesMain.errorWrongSyntax(msgChannel);
                return false;
            } else {
                // finds the players
                var player1 = game.findPlayerByName(parameters.get(0));
                var player2 = game.findPlayerByName(parameters.get(1));

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

                    state.setDone(amor);
                    return true;

                }
            }
        };
        game.addPrivateCommand(amor.user.getId(), amorCommand);

    }
}