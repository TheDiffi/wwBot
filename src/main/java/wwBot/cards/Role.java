package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;
import wwBot.GameStates.AutoState;


public class Role {
    public Card specs;
    public String name;
    public DeathDetails deathDetails;
    public Player inLoveWith;

    Role(String roleName) {
        specs = Globals.mapRegisteredCardsSpecs.get(roleName);
        name = roleName;
        deathDetails = new DeathDetails();
    }

    public static Role createRole(String name) {
        switch (name) {
            case "Werwolf":
                return new RoleWerwolf(false);
            case "Wolfjunges":
                return new RoleWerwolf(true);
            case "Hexe":
                return new RoleZauberer("Hexe");
            case "Magier":
                return new RoleZauberer("Magier");
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
            case "Leibwächter":
                return new RoleLeibwächter();
            case "Alte-Vettel":
                return new RoleAlteVettel();
            case "Märtyrerin":
                return new RoleMärtyrerin();
            case "Aura-Seherin":
                return new RoleAuraSeherin();
            case "Zaubermeisterin":
                return new RoleZaubermeisterin();
            case "Paranormaler-Ermittler":
                return new RoleParanormalerErmittler();
            case "Unruhestifterin":
                return new RoleUnruhestifterin();

            default:
                return new Role(name);
        }
    }

    public void execute(Player player, Game game, AutoState state) {
        state.pending.remove(player);
    }

    public void executeFirstNight(Player player, Game game, AutoState state) {
        state.pending.remove(player);
    }

	public void executePreWW(Player player, Game game, AutoState state) {
        state.pending.remove(player);
    }
    
    public void executePostWW(Player player,Game game, AutoState state) {
        state.pending.remove(player);
    }


    

}