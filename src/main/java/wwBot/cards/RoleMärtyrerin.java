package wwBot.cards;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.GameStates.DayPhases.Auto.Day;
import wwBot.Interfaces.PrivateCommand;

public class RoleMärtyrerin extends Role {

    RoleMärtyrerin() {
        super("Märtyrerin");
    }


    @Override
    public void execute(Player mostVoted, Game game, AutoState state) {
        var player = game.gameState.mapExistingRoles.get("Märtyrerin").get(0);

        MessagesMain.remindMärtyrerin(game, player, mostVoted);
            PrivateCommand sacrifice = (event, parameters, msgChannel) -> {
                if (parameters.size() != 1) {
                    MessagesMain.errorWrongAnswer(msgChannel);
                    return false;

                } else if (parameters.get(0).equalsIgnoreCase("no")) {
                    MessagesMain.confirm(msgChannel);
                    ((Day) state.aDayPhase).lynchPlayer(mostVoted, false);

                    return true;

                } else if (parameters.get(0).equalsIgnoreCase("yes")) {
                    MessagesMain.confirm(msgChannel);
                    ((Day) state.aDayPhase).lynchPlayer(player, true);

                    return true;

                } else {
                    MessagesMain.errorWrongAnswer(msgChannel);
                    return false;
                }

            };
            game.addPrivateCommand(player.user.getId(), sacrifice);

    }
}