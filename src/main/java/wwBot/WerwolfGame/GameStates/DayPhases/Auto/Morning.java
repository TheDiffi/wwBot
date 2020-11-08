package wwBot.WerwolfGame.GameStates.DayPhases.Auto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import wwBot.Globals;
import wwBot.Interfaces.Command;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.MainState.DayPhase;
import wwBot.WerwolfGame.GameStates.MainState.DeathState;
import wwBot.WerwolfGame.cards.RoleSäufer;

public class Morning extends AutoDayPhase {

    public List<Player> endangeredPlayers = new ArrayList<>();
    public Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>();
    Game game;

    public Morning(Game getGame) {
        game = getGame;
        mapExistingRoles = game.gameState.mapExistingRoles;

        // loads the Commands of the state
        registerCommands();

        calculateEndangeredPlayers();

        killEndangeredPlayers();

        game.gameState.changeDayPhase(DayPhase.DAY);
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
            MessagesWW.sendHelpMorning(msgChannel, true);
        };
        mapCommands.put("help", helpCommand);
        mapCommands.put("hilfe", helpCommand);

    }

    private void killEndangeredPlayers() {

        for (Player victim : endangeredPlayers) {
            
            Globals.sleepWCatch(1500);

            if (game.gameState.killPlayer(victim, victim.role.deathDetails.killer)) {
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