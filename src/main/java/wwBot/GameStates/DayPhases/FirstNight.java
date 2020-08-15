package wwBot.GameStates.DayPhases;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.MainState.DayPhase;
import wwBot.Interfaces.PrivateCommand;

public class FirstNight {

    public Map<String, Boolean> endChecks = new HashMap<>();
    Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>(String.CASE_INSENSITIVE_ORDER);
    private Game game;

    FirstNight(Game getGame) {
        game = getGame;
        mapExistingRoles = game.gameState.mapExistingRoles;
        sendPrivateMessages();
    }

    private void sendPrivateMessages() {
        // TODO: Seher

        
        
        // Amor
        if (game.gameState.mapExistingRoles.containsKey("Amor")) {
            /// TODO: send mssg
            var player = mapExistingRoles.get("Amor").get(0);
            player.user.getPrivateChannel().block().createMessage("TEST");

            endChecks.put("Amor", false);
            player.role.execute(game, player);
        }

        // the Günstling gets a list with all the WW
        if (game.gameState.mapExistingRoles.containsKey("Günstling")) {
            MessagesMain.günstlingMessage(mapExistingRoles.get("Günstling").get(0).user.getPrivateChannel().block(),
                    mapExistingRoles, game);
            endChecks.put("Günstling", true);

        }

        // Doppelgängerin: recieves a plaver in the next mssg that player in her role
        if (game.gameState.mapExistingRoles.containsKey("Doppelgängerin")) {

            // TODO: send mssg
            var player = mapExistingRoles.get("Doppelgängerin").get(0);
            player.user.getPrivateChannel().block().createMessage("TEST");

            endChecks.put("Doppelgängerin", false);
            player.role.execute(game, player);

            /*
             * PrivateCommand dpCommand = (event, parameters, msgChannel) -> { if
             * (parameters == null || !(parameters.size() == 1)) {
             * MessagesMain.errorWrongSyntax(game, msgChannel); return false;
             * 
             * } // finds the players var foundPlayer =
             * Globals.findPlayerByName(Globals.removeDash(parameters.get(0)),
             * game.mapPlayers, game); if (foundPlayer != null) { var dp =
             * mapExistingRoles.get("Doppelgängerin").get(0);
             * 
             * // sets the variable var dpRole = (RoleDoppelgängerin) dp.role;
             * dpRole.boundTo = foundPlayer; MessagesMain.doppelgängerinSuccess(game, dp,
             * foundPlayer); return true; }
             * 
             * MessagesMain.errorPlayerNotFound(msgChannel);
             * endChecks.replace("Doppelgängerin", true); return false; };
             * game.addPrivateCommand(game.userModerator.getId(), dpCommand);
             */
        }

        endNightCheck();
    }

    // TODO: implement endNightCheck
    public void endNightCheck() {
        var check = true;
        for (Entry<String, Boolean> done : endChecks.entrySet()) {
            if (!done.getValue()) {
                check = false;
            }
        }

        if (check) {
            game.gameState.changeDayPhase(DayPhase.DAY);
        }
    }

}