package wwBot.WerwolfGame.cards;

import wwBot.Globals;
import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;

public class RolePriester extends Role {
    public boolean usedAbility = false;
    public Player protectedPlayer;
    public boolean abilityActive = false;

    RolePriester() {
        super("Priester");
    }

    @Override
    public void executeFirstNight(Player player, Game game, AutoState state) {
        executePreWW(player, game, state);
    }

    @Override
    public void executePreWW(Player priester, Game game, AutoState state) {

        // if the priest has not yet used his ability, he gets the chance to do so. If
        // he already used it and it did not trigger (vanish) yet, the protectedPlayer
        // gets to live once during ifDiesCheck();
        if (!usedAbility) {

            MessagesWW.callPriester(priester);

            PrivateCommand priesterCommand = (event, parameters, msgChannel) -> {
                if (parameters.size() != 1) {
                    MessagesWW.errorWrongAnswer(msgChannel);
                    return false;

                    // NO
                } else if (parameters.get(0).equalsIgnoreCase("no") || parameters.get(0).equalsIgnoreCase("nein")) {
                    MessagesWW.confirm(msgChannel);

                    state.setDoneNight(priester);

                    return true;

                    // YES: if the priest chooses to use his ability he gets granted access to the
                    // "bless" Command
                } else if (parameters.get(0).equalsIgnoreCase("yes") || parameters.get(0).equalsIgnoreCase("ja")) {
                    MessagesWW.confirmPriester(priester);

                    // this Command saves the priests player of choice as the protected player
                    PrivateCommand blessCommand = (event2, parameters2, msgChannel2) -> {
                        var player = Globals.commandPlayerFinder(event2, parameters2, msgChannel2, game);

                        if (player != null) {
                            protectedPlayer = player;
                            usedAbility = true;
                            abilityActive = true;

                            MessagesWW.confirm(msgChannel);

                            state.setDoneNight(priester);
                            return true;

                        } else {
                            return false;
                        }

                    };
                    game.addPrivateCommand(priester.user.getId(), blessCommand);

                    return true;

                } else {
                    MessagesWW.errorWrongAnswer(msgChannel);
                    return false;
                }
            };
            game.addPrivateCommand(priester.user.getId(), priesterCommand);

        } else{
            
            state.setDoneNight(priester);

        }
    }
}
