package wwBot.WerwolfGame.cards;

import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;
import wwBot.WerwolfGame.GameStates.DayPhases.Auto.Day;

public class RoleMärtyrerin extends Role {

    RoleMärtyrerin() {
        super("Märtyrerin");
    }


    @Override
    public void execute(Player mostVoted, Game game, AutoState state) {
        var player = game.gameState.mapExistingRoles.get("Märtyrerin").get(0);

        MessagesWW.remindMärtyrerin(game, player, mostVoted);
            PrivateCommand sacrifice = (event, parameters, msgChannel) -> {
                if (parameters.size() != 1) {
                    MessagesWW.errorWrongAnswer(msgChannel);
                    return false;

                } else if (parameters.get(0).equalsIgnoreCase("no")) {
                    MessagesWW.confirm(msgChannel);
                    ((Day) state.dayPhase).lynchPlayer(mostVoted, false);

                    return true;

                } else if (parameters.get(0).equalsIgnoreCase("yes")) {
                    MessagesWW.confirm(msgChannel);
                    ((Day) state.dayPhase).lynchPlayer(player, true);

                    return true;

                } else {
                    MessagesWW.errorWrongAnswer(msgChannel);
                    return false;
                }

            };
            game.addPrivateCommand(player.user.getId(), sacrifice);

    }
}