package wwBot.WerwolfGame.cards;

import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.AutoState;
import wwBot.WerwolfGame.GameStates.MainState.DeathState;

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
