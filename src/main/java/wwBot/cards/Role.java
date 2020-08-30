package wwBot.cards;

import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;
import wwBot.GameStates.AutoState;
import wwBot.GameStates.MainState.DeathState;

public class Role {
    public Card specs;
    public String name;
    public boolean alive = true;
    public DeathState deathState = DeathState.ALIVE;
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
            case "Leibwächter":
                return new RoleLeibwächter();
            case "AlteVettel":
                return new RoleAlteVettel();
            case "Märtyrerin":
                return new RoleMärtyrerin();
            case "AuraSeherin":
                return new RoleAuraSeherin();

            default:
                return new Role(name);
        }
    }

    public void execute(Game game, Player player) {
    }

    public void setDone(Game game, String role) {
        // sets this roles state to done
        var state = (AutoState) game.gameState;
        state.firstNight.endChecks.replace(role, true);
        state.firstNight.endNightCheck();
    }

}