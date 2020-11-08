

package wwBot.WerwolfGame.cards;

import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;

public class RoleUnruhestifterin extends Role {
    public boolean abilityUsed = false;

    RoleUnruhestifterin() {
        super("Unruhestifterin");
    }

    @Override
    public void executePreWW(Player unruhestifterin,Game game, AutoState state) {
        MessagesWW.callUnruhestifterin(unruhestifterin);

        PrivateCommand unruhestifterinCommand = (event, parameters, msgChannel) -> {
            if (parameters.size() != 1) {
                MessagesWW.errorWrongAnswer(msgChannel);
                return false;

                // NO
            } else if (parameters.get(0).equalsIgnoreCase("no") || parameters.get(0).equalsIgnoreCase("nein")) {
                MessagesWW.confirm(msgChannel);
                
                state.setDoneNight(unruhestifterin);

                return true;

                // YES
            } else if (parameters.get(0).equalsIgnoreCase("yes") || parameters.get(0).equalsIgnoreCase("ja")) {
                MessagesWW.confirm(msgChannel);
                
                state.villageAgitated = true;
                abilityUsed = true;

                state.setDoneNight(unruhestifterin);

                return true;

            } else {
                MessagesWW.errorWrongAnswer(msgChannel);
                return false;
            }
        };
        game.addPrivateCommand(unruhestifterin.user.getId(), unruhestifterinCommand);


    }
}


