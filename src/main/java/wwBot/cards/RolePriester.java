package wwBot.cards;

import wwBot.Player;

public class RolePriester extends Role {
    public boolean usedAbility = false;
    public Player protectedPlayer;

    RolePriester() {
        super("Priester");
    }
}