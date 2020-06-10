package wwBot.cards;

import wwBot.Card;
import wwBot.Globals;
import wwBot.Player;

public class Role {
    public Card specs;
    public String name;
    public boolean friendly;
    public boolean alive = true;
    public Player inLoveWith;

    Role(String name) {
        specs = Globals.mapRegisteredCardsSpecs.get(name);
        name = specs.name;
        friendly = specs.friendly;
    }

    public static Role CreateRole(String name) {
        switch (name) {
            case "Hexe":
                return new RoleHexe();
            case "Doppelgängerin":
                return new RoleDoppelgängerin();
            case "Priester":
                return new RolePriester();
            case "Säufer":
                return new RoleSäufer();

            default:
                return new Role(name);
        }
    }

}