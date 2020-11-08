package wwBot.WerwolfGame.cards;

import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;

public class RoleAmor extends Role {

    RoleAmor() {
        super("Amor");
    }

    @Override
    public void executeFirstNight(Player amor,Game game, AutoState state) {
        MessagesWW.triggerAmor(game, amor);

        PrivateCommand amorCommand = (event, parameters, msgChannel) -> {

            if (parameters == null || parameters.size() != 2) {
                MessagesWW.errorWrongSyntax(msgChannel);
                return false;
            } else {
                // finds the players
                var player1 = game.findPlayerByName(parameters.get(0));
                var player2 = game.findPlayerByName(parameters.get(1));

                if (player1 == null || player2 == null) {
                    MessagesWW.errorPlayerNotFound(msgChannel);
                    return false;

                } else if (player1 == player2) {
                    MessagesWW.errorPlayersIdentical(msgChannel);
                    return false;

                } else {
                    // sets the "inLoveWith" variables
                    player1.role.inLoveWith = player2;
                    player2.role.inLoveWith = player1;

                    // sends a mssg
                    MessagesWW.confirm(amor.user.getPrivateChannel().block());
                    MessagesWW.amorSuccess(game, player1, player2);

                    state.setDoneNight(amor);
                    return true;

                }
            }
        };
        game.addPrivateCommand(amor.user.getId(), amorCommand);

    }
}