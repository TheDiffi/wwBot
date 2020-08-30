package wwBot.cards;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.Interfaces.PrivateCommand;

public class RoleMärtyrerin extends Role {

    RoleMärtyrerin() {
        super("Märtyrerin");
    }

    @Override
    public void execute(Game game, Player mostVoted) {
        var player = game.gameState.mapExistingRoles.get("Märtyrerin").get(0);
        var state = (AutoState) game.gameState;

        MessagesMain.remindMärtyrerin(game, player, mostVoted);
            PrivateCommand sacrifice = (event, parameters, msgChannel) -> {
                if (parameters.size() != 1) {
                    MessagesMain.errorWrongAnswer(msgChannel);
                    return false;

                } else if (parameters.get(0).equalsIgnoreCase("no")) {
                    MessagesMain.sendApproval(msgChannel);
                    state.day.lynchPlayer(mostVoted, false);

                    return true;

                } else if (parameters.get(0).equalsIgnoreCase("yes")) {
                    MessagesMain.sendApproval(msgChannel);
                    state.day.lynchPlayer(player, true);

                    return true;

                } else {
                    MessagesMain.errorWrongAnswer(msgChannel);
                    return false;
                }

            };
            game.addPrivateCommand(player.user.getId(), sacrifice);

    }
}