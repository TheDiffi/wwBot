package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.Interfaces.PrivateCommand;

public class RolePriester extends Role {
    public boolean usedAbility = false;
    public Player protectedPlayer;
    public boolean abilityActive = false;

    RolePriester() {
        super("Priester");
    }

    @Override
    public void executePreWW(Player priester, Game game, AutoState state) {

        // if the priest has not yet used his ability, he gets the chance to do so. If
        // he already used it and it did not trigger (vanish) yet, the protectedPlayer
        // gets to live once during ifDiesCheck();
        if (!usedAbility) {

            MessagesMain.callPriester(priester);

            PrivateCommand priesterCommand = (event, parameters, msgChannel) -> {
                if (parameters.size() != 1) {
                    MessagesMain.errorWrongAnswer(msgChannel);
                    return false;

                    // NO
                } else if (parameters.get(0).equalsIgnoreCase("no") || parameters.get(0).equalsIgnoreCase("nein")) {
                    MessagesMain.confirm(msgChannel);

                    state.setDoneNight(priester);

                    return true;

                    // YES: if the priest chooses to use his ability he gets granted access to the
                    // "bless" Command
                } else if (parameters.get(0).equalsIgnoreCase("yes") || parameters.get(0).equalsIgnoreCase("ja")) {
                    MessagesMain.confirmPriester(priester);

                    // this Command saves the priests player of choice as the protected player
                    PrivateCommand blessCommand = (event2, parameters2, msgChannel2) -> {
                        var player = Globals.commandPlayerFinder(event2, parameters2, msgChannel2, game);

                        if (player != null) {
                            protectedPlayer = player;
                            usedAbility = true;

                            state.setDoneNight(priester);
                            return true;

                        } else {
                            return false;
                        }

                    };
                    game.addPrivateCommand(priester.user.getId(), blessCommand);

                    return true;

                } else {
                    MessagesMain.errorWrongAnswer(msgChannel);
                    return false;
                }
            };
            game.addPrivateCommand(priester.user.getId(), priesterCommand);

        }
    }
}
