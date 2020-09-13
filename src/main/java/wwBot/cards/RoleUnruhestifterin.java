

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
        MessagesMain.callUnruhestifterin(unruhestifterin);

        PrivateCommand unruhestifterinCommand = (event, parameters, msgChannel) -> {
            if (parameters.size() != 1) {
                MessagesMain.errorWrongAnswer(msgChannel);
                return false;

                // NO
            } else if (parameters.get(0).equalsIgnoreCase("no") || parameters.get(0).equalsIgnoreCase("nein")) {
                MessagesMain.confirm(msgChannel);
                
                state.setDoneNight(unruhestifterin);

                return true;

                // YES
            } else if (parameters.get(0).equalsIgnoreCase("yes") || parameters.get(0).equalsIgnoreCase("ja")) {
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


