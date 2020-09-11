package wwBot.cards;

import wwBot.Game;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.GameStates.MainState.DeathState;

public class RoleHarterBursche extends Role {

    public boolean isDying;

    RoleHarterBursche() {
        super("Harter-Bursche");
    }

    @Override
    public void executePreWW(Player bursche, Game game, AutoState state) {
        if (isDying) {
            bursche.role.deathDetails.deathState = DeathState.AT_RISK;
        }
    }

}
