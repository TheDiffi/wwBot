package wwBot.GameStates.DayPhases.Auto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.MainState.DeathState;
import wwBot.Interfaces.Command;
import wwBot.cards.RoleSäufer;

public class Morning extends AutoDayPhase {

    public List<Player> endangeredPlayers = new ArrayList<>();
    public Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>();
    Game game;

    public Morning(Game getGame) {
        game = getGame;
        mapExistingRoles = game.gameState.mapExistingRoles;
        MessagesMain.onMorningAuto(game);

        // loads the Commands of the state
        registerCommands();

        calculateEndangeredPlayers();

        killEndangeredPlayers();

    }

    // loads all of the following Commands into mapCommands
    public void registerCommands() {

        // replys with pong!
        Command pingCommand = (event, parameters, msgChannel) -> {
            event.getMessage().getChannel().block().createMessage("Pong! MorningPhase").block();
        };
        mapCommands.put("ping", pingCommand);

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {
            MessagesMain.sendHelpNight(msgChannel, true);
        };
        mapCommands.put("help", helpCommand);
        mapCommands.put("hilfe", helpCommand);

    }

    private void killEndangeredPlayers() {

        for (Player victim : endangeredPlayers) {
            
            Globals.sleepWCatch(1500);

            if (!game.gameState.killPlayer(victim, victim.role.deathDetails.killer)) {
                game.mainChannel.createMessage("Test: No one died, right?");
            }
        }
    }

    private void calculateEndangeredPlayers() {

        for (var player : game.livingPlayers.values()) {
            if (player.role.deathDetails.deathState == DeathState.AT_RISK) {
                endangeredPlayers.add(player);
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