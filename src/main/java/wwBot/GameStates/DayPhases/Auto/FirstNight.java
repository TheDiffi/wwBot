package wwBot.GameStates.DayPhases.Auto;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import wwBot.Game;
import wwBot.MessagesMain;
import wwBot.GameStates.MainState.DayPhase;

//---------------- FIRST NIGHT ----------------------------

public class FirstNight {

    public Map<String, Boolean> endChecks = new HashMap<>();
    private Game game;

    public FirstNight(Game getGame) {
        game = getGame;

        MessagesMain.onFirstNightAuto(game);
        
        sendPrivateMessages();
    }

    private void sendPrivateMessages() {

        // the Günstling gets a list with all the WW
        if (game.gameState.mapExistingRoles.containsKey("Günstling")) {

            initiateRole("Günstling");
            
        }
        // Seher
        if (game.gameState.mapExistingRoles.containsKey("Seher")) {

            initiateRole("Seher");
        }
        // Amor
        if (game.gameState.mapExistingRoles.containsKey("Amor")) {

            initiateRole("Amor");
        }
        // Doppelgängerin: recieves a plaver in the next mssg that player in her role
        if (game.gameState.mapExistingRoles.containsKey("Doppelgängerin")) {

            initiateRole("Doppelgängerin");

        }

        endNightCheck();
    }

    private void initiateRole(String roleName) {
        var player = game.gameState.mapExistingRoles.get(roleName).get(0);
        endChecks.put(roleName, false);
        player.role.execute(game, player);
    }

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