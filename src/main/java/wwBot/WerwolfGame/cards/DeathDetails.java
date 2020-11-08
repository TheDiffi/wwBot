package wwBot.WerwolfGame.cards;

import wwBot.WerwolfGame.GameStates.MainState.DeathState;

public class DeathDetails {
    public boolean alive;
    public DeathState deathState;
    public String killer = null;

    DeathDetails(){
        alive = true;
        deathState = DeathState.ALIVE;
        killer = null;
    }
}