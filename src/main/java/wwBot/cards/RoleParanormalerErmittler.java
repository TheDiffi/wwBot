
package wwBot.cards;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.Player;

public class RoleParanormalerErmittler extends Role {
    public boolean usedAbility = false;

    RoleParanormalerErmittler() {
        super("ParanormalerErmittler");

    }

    @Override
    public void execute(Game game, Player ermittler) {
        MessagesMain.callErmittler(ermittler);

        //TODO: figure this out
        
    }
}