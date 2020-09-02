

package wwBot.cards;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.Interfaces.PrivateCommand;

public class RoleUnruhestifterin extends Role {
    public boolean abilityActive = false;
    public boolean abilityUsed = false;

    RoleUnruhestifterin() {
        super("Unruhestifterin");
    }

    @Override
    public void execute(Game game, Player unruhestifterin) {
        MessagesMain.callPriester(unruhestifterin);

        PrivateCommand unruhestifterinCommand = (event, parameters, msgChannel) -> {
            if (parameters.size() != 1) {
                MessagesMain.errorWrongAnswer(msgChannel);
                return false;

                // NO
            } else if (parameters.get(0).equalsIgnoreCase("no")) {
                MessagesMain.confirm(msgChannel);
                
                setDone(game, "Unruhestifterin");

                return true;

                // YES: if the priest chooses to use his ability he gets granted access to the "bless" Command
            } else if (parameters.get(0).equalsIgnoreCase("yes")) {
                MessagesMain.confirm(msgChannel);
                
                abilityActive = true;
                abilityUsed = true;

                setDone(game, "Unruhestifterin");

                return true;

            } else {
                MessagesMain.errorWrongAnswer(msgChannel);
                return false;
            }
        };
        game.addPrivateCommand(unruhestifterin.user.getId(), unruhestifterinCommand);


    }
}

