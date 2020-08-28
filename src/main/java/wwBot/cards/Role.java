package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;

public class Role {
    public Card specs;
    public String name;
    public boolean alive = true;
    public Player inLoveWith;

    Role(String roleName) {
        specs = Globals.mapRegisteredCardsSpecs.get(roleName);
        name = roleName;
    }

    public static Role createRole(String name) {
        switch (name) {
            case "Hexe":
                return new RoleHexe();
            case "Doppelgängerin":
                return new RoleDoppelgängerin();
            case "Priester":
                return new RolePriester();
            case "Säufer":
                return new RoleSäufer();
            case "Amor":
                return new RoleAmor();
            case "Seher":
                return new RoleSeher();
            case "Günstling":
                return new RoleGünstling();

            default:
                return new Role(name);
        }
    }

    public void execute(Game game, Player player) {
    }

}