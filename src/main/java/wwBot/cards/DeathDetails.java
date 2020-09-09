package wwBot.cards;

import wwBot.GameStates.MainState.DeathState;

public class DeathDetails {
    public boolean alive = true;
    public DeathState deathState = DeathState.ALIVE;
    public String killer = null;

    DeathDetails(){
        
    }
}