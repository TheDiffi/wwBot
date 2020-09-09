package wwBot.GameStates.DayPhases.Auto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.MainState.DeathState;
import wwBot.cards.RoleSäufer;

public class Morning extends AutoDayPhase {

    public List<Player> endangeredPlayers = new ArrayList<>();
    public Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>();
    Game game;

    public Morning(Game getGame) {
        game = getGame;
        mapExistingRoles = game.gameState.mapExistingRoles;
        MessagesMain.onMorningAuto(game);

        calculateEndangeredPlayers();
        
        killEndangeredPlayers();
        
        // TODO: end Morning
    }

    private void killEndangeredPlayers() {

        for (Player victim : endangeredPlayers) {
            game.gameState.killPlayer(victim, victim.role.deathDetails.killer);
        }

    }

    private void calculateEndangeredPlayers() {

        for (var entry : game.livingPlayers.entrySet()) {
            if (entry.getValue().role.deathDetails.deathState == DeathState.AT_RISK) {
                endangeredPlayers.add(entry.getValue());
            }
        }

        // Säufer
        if (mapExistingRoles.containsKey("Säufer")) {
            var säufer = (RoleSäufer) mapExistingRoles.get("Säufer").get(0).role;

            if (säufer.drinkingAt.role.deathDetails.deathState == DeathState.AT_RISK) {
                säufer.deathDetails.deathState = DeathState.AT_RISK;
                endangeredPlayers.add(mapExistingRoles.get("Säufer").get(0));
            }
        }

    }
}