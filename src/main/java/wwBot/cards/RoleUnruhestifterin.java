

package wwBot.cards;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.Interfaces.PrivateCommand;

public class RoleUnruhestifterin extends Role {
    public boolean abilityUsed = false;

    RoleUnruhestifterin() {
        super("Unruhestifterin");
    }

    @Override
    public void executePreWW(Player unruhestifterin,Game game, AutoState state) {
        MessagesMain.callPriester(unruhestifterin);

        PrivateCommand unruhestifterinCommand = (event, parameters, msgChannel) -> {
            if (parameters.size() != 1) {
                MessagesMain.errorWrongAnswer(msgChannel);
                return false;

                // NO
            } else if (parameters.get(0).equalsIgnoreCase("no")) {
                MessagesMain.confirm(msgChannel);
                
                state.setDoneNight(unruhestifterin);

                return true;

                // YES: if the priest chooses to use his ability he gets granted access to the "bless" Command
            } else if (parameters.get(0).equalsIgnoreCase("yes")) {
                MessagesMain.confirm(msgChannel);
                
                state.villageAgitated = true;
                abilityUsed = true;

                state.setDoneNight(unruhestifterin);

                return true;

            } else {
                MessagesMain.errorWrongAnswer(msgChannel);
                return false;
            }
        };
        game.addPrivateCommand(unruhestifterin.user.getId(), unruhestifterinCommand);


    }
}


