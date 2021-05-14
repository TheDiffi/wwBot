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
        if (endangeredPlayers.size() == 0) {
            game.mainChannel.createMessage("Test: No one died, right?");

        } else {
            for (Player victim : endangeredPlayers) {

                Globals.sleepWCatch(game.avgDelaytime);

                if (game.gameState.killPlayer(victim, victim.role.deathDetails.killer)) {
                    game.mainChannel.createMessage("Test: No one died, right?");
                }
            }
        }
    }

    private void calculateEndangeredPlayers() {

        // Säufer
        if (mapExistingRoles.containsKey("Säufer")) {
            var säufer = (RoleSäufer) mapExistingRoles.get("Säufer").get(0).role;
            var player = mapExistingRoles.get("Säufer").get(0);

            //removes the säufer from the list if he is being attacked by WW
            //TODO: it didnt send the säufer survives mssg
            if (säufer.deathDetails.deathState == DeathState.AT_RISK && säufer.deathDetails.killer.equalsIgnoreCase("Werwolf")) {
                player.role.deathDetails.deathState = DeathState.SAVED;
                MessagesWW.säuferSurvives(game, player);
            }

            // adds the säufer to the list if the person he's with gets killed (even by e.g.
            // witch)
            if (säufer.drinkingAt.role.deathDetails.deathState == DeathState.AT_RISK) {
                player.role.deathDetails.deathState = DeathState.AT_RISK;
                player.role.deathDetails.killer = "Säufer";

            }
        }

        for (var player : game.livingPlayers.values()) {
            if (player.role.deathDetails.deathState == DeathState.AT_RISK) {
                endangeredPlayers.add(player);
            }
        }

    }
}