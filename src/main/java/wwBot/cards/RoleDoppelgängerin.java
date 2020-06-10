package wwBot.cards;

import wwBot.Player;

public class RoleDoppelgängerin extends Role {
    public Player boundTo;
    public boolean transformed = false;

    RoleDoppelgängerin() {
        super("Doppelgängerin");
    }
}