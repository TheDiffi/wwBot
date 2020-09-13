
package wwBot.cards;

import java.util.Arrays;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.Interfaces.PrivateCommand;

public class RoleParanormalerErmittler extends Role {
    public boolean usedAbility = false;

    RoleParanormalerErmittler() {
        super("ParanormalerErmittler");

    }

    @Override
    public void executePreWW(Player ermittler, Game game, AutoState state) {

        if (!usedAbility) {

            MessagesMain.callErmittler(ermittler);

            PrivateCommand ermittlerCommand = (event, parameters, msgChannel) -> {
                if (parameters.size() != 1) {
                    MessagesMain.errorWrongAnswer(msgChannel);
                    return false;

                    // NO
                } else if (parameters.get(0).equalsIgnoreCase("no")) {
                    MessagesMain.confirm(msgChannel);

                    state.setDoneNight(ermittler);

                    return true;

                    // YES: if the priest chooses to use his ability he gets granted access to the
                    // "bless" Command
                } else if (parameters.get(0).equalsIgnoreCase("yes")) {
                    MessagesMain.confirmErmittler(ermittler);

                    // this Command saves the priests player of choice as the protected player
                    PrivateCommand investigateCommand = (event2, parameters2, msgChannel2) -> {

                        if (parameters == null || parameters.size() != 3) {
                            MessagesMain.errorWrongSyntax(msgChannel);
                            return false;
                        } else {
                            // finds the players
                            var player1 = game.findPlayerByName(parameters.get(0));
                            var player2 = game.findPlayerByName(parameters.get(1));
                            var player3 = game.findPlayerByName(parameters.get(2));

                            if (player1 == null || player2 == null || player3 == null) {
                                MessagesMain.errorMultiplePlayersNotFound(msgChannel);
                                return false;

                            } else if (player1 == player2 || player1 == player3 || player2 == player3) {
                                MessagesMain.errorPlayersIdentical(msgChannel);
                                return false;

                                // on succsess
                            } else {

                                var tempList = Arrays.asList(player1, player2, player3);
                                tempList.remove((int) (Math.random() * 3));

                                // sends a mssg
                                MessagesMain.ermittlerSuccess(game, tempList, ermittler);

                                usedAbility = true;
                                state.setDoneNight(ermittler);
                                return true;

                            }
                        }

                    };
                    game.addPrivateCommand(ermittler.user.getId(), investigateCommand);

                    return true;

                } else {
                    MessagesMain.errorWrongAnswer(msgChannel);
                    return false;
                }
            };
            game.addPrivateCommand(ermittler.user.getId(), ermittlerCommand);

        }
    }
}