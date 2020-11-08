
package wwBot.WerwolfGame.cards;

import java.util.Arrays;

import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;

public class RoleParanormalerErmittler extends Role {
    public boolean usedAbility = false;

    RoleParanormalerErmittler() {
        super("ParanormalerErmittler");

    }

    @Override
    public void executePreWW(Player ermittler, Game game, AutoState state) {

        if (!usedAbility) {

            MessagesWW.callErmittler(ermittler);

            PrivateCommand ermittlerCommand = (event, parameters, msgChannel) -> {
                if (parameters.size() != 1) {
                    MessagesWW.errorWrongAnswer(msgChannel);
                    return false;

                    // NO
                } else if (parameters.get(0).equalsIgnoreCase("no")) {
                    MessagesWW.confirm(msgChannel);

                    state.setDoneNight(ermittler);

                    return true;

                    // YES: if the priest chooses to use his ability he gets granted access to the
                    // "bless" Command
                } else if (parameters.get(0).equalsIgnoreCase("yes")) {
                    MessagesWW.confirmErmittler(ermittler);

                    // this Command saves the priests player of choice as the protected player
                    PrivateCommand investigateCommand = (event2, parameters2, msgChannel2) -> {

                        if (parameters == null || parameters.size() != 3) {
                            MessagesWW.errorWrongSyntax(msgChannel);
                            return false;
                        } else {
                            // finds the players
                            var player1 = game.findPlayerByName(parameters.get(0));
                            var player2 = game.findPlayerByName(parameters.get(1));
                            var player3 = game.findPlayerByName(parameters.get(2));

                            if (player1 == null || player2 == null || player3 == null) {
                                MessagesWW.errorMultiplePlayersNotFound(msgChannel);
                                return false;

                            } else if (player1 == player2 || player1 == player3 || player2 == player3) {
                                MessagesWW.errorPlayersIdentical(msgChannel);
                                return false;

                                // on succsess
                            } else {

                                var tempList = Arrays.asList(player1, player2, player3);
                                tempList.remove((int) (Math.random() * 3));

                                // sends a mssg
                                MessagesWW.ermittlerSuccess(game, tempList, ermittler);

                                usedAbility = true;
                                state.setDoneNight(ermittler);
                                return true;

                            }
                        }

                    };
                    game.addPrivateCommand(ermittler.user.getId(), investigateCommand);

                    return true;

                } else {
                    MessagesWW.errorWrongAnswer(msgChannel);
                    return false;
                }
            };
            game.addPrivateCommand(ermittler.user.getId(), ermittlerCommand);

        }
    }
}