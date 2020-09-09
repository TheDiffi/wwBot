
package wwBot.cards;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.AutoState;

public class RoleParanormalerErmittler extends Role {
    public boolean usedAbility = false;

    RoleParanormalerErmittler() {
        super("ParanormalerErmittler");

    }

    @Override
    public void executePreWW(Player ermittler, Game game, AutoState state) {
        MessagesMain.callErmittler(ermittler);

        //TODO: figure this out
        
    }
}