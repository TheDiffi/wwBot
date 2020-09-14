package wwBot.cards;

import wwBot.GameStates.MainState.DeathState;

public class DeathDetails {
    public boolean alive;
    public DeathState deathState;
    public String killer = null;

    //TODO: überprüfe ob killer gesetzt werden
    DeathDetails(){
        alive = true;
        deathState = DeathState.ALIVE;
        killer = null;
    }
}