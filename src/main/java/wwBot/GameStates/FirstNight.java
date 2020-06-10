package wwBot.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;
import wwBot.Interfaces.Command;
import wwBot.Interfaces.PrivateCommand;

public class FirstNight {
    GameState gameState;
    Game game;
    public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>(String.CASE_INSENSITIVE_ORDER);

    FirstNight(Game getGame) {
        game = getGame;
        game.gameState = gameState;
        game.gameState.mapExistingRoles = mapExistingRoles;
        initiateFirstNight();

    }

    private void initiateFirstNight() {

        gameState.setMuteAllPlayers(game.livingPlayers, true);
        gameState.wwChat = gameState.createWerwolfChat();

        // generates which Roles need to be called
        var listRolesToBeCalled = firstNightRoles();
        MessagesMain.firstNightMod(game, listRolesToBeCalled);

        specificCardInteractions();

        endFirstNight();

    }

    private void specificCardInteractions() {
        if (mapExistingRoles.get("Günstling") != null) {
            var privateChannel = mapExistingRoles.get("Günstling").get(0).user.getPrivateChannel().block();

            MessagesMain.günstlingMessage(privateChannel, mapExistingRoles, game);
        }

        if (mapExistingRoles.get("Amor") != null) {
            MessagesMain.triggerAmor(game);

            Command setLoveCommand = (event, parameters, msgChannel) -> {
                if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                    if (parameters != null && parameters.size() == 2) {
                        // finds the players
                        var player1 = Globals.findPlayerByName(Globals.removeDash(parameters.get(0)), game.mapPlayers,
                                game);
                        var player2 = Globals.findPlayerByName(Globals.removeDash(parameters.get(1)), game.mapPlayers,
                                game);

                        if (player1 != null && player2 != null) {
                            if (player1 != player2) {
                                player1.inLoveWith = player2;
                                player2.inLoveWith = player1;
                                MessagesMain.amorSuccess(game, game.userModerator.getPrivateChannel().block(), player1,
                                        player2);

                            } else {
                                MessagesMain.errorPlayersIdentical(msgChannel);
                            }
                        } else {
                            MessagesMain.errorPlayerNotFound(msgChannel);
                        }
                    } else {
                        MessagesMain.errorWrongSyntax(game, msgChannel);
                    }
                } else {
                    MessagesMain.errorModOnlyCommand(msgChannel);
                }
            };
            mapCommands.put("inLove", setLoveCommand);
            mapCommands.put("Love", setLoveCommand);
        }



    }

    private ArrayList<Player> firstNightRoles() {
        var uniqueRolesInThisPhase = new ArrayList<String>();
        var list = new ArrayList<Player>();

        uniqueRolesInThisPhase.add("Günstling");
        uniqueRolesInThisPhase.add("Amor");
        uniqueRolesInThisPhase.add("Doppelgängerin");
        for (String role : uniqueRolesInThisPhase) {
            if (mapExistingRoles.containsKey(role)) {
                list.add(mapExistingRoles.get(role).get(0));
            }
        }

        if (mapExistingRoles.containsKey("Werwolf")) {
            for (Player player : mapExistingRoles.get("Werwolf")) {
                list.add(player);
            }
        }

        if (mapExistingRoles.containsKey("Seher")) {
            for (Player player : mapExistingRoles.get("Seher")) {
                list.add(player);
            }
        }
        return list;
    }

    private void endFirstNight() {
        Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.orange,
                "Wenn bu bereit bist die erste Nacht zu beenden tippe den Command \"Sonnenaufgang\"",
                "PS: niemand stirbt in der ersten Nacht");

        // Sonnenaufgang lässt den ersten Tag starten und beginnt den Zyklus
        PrivateCommand sonnenaufgangCommand = (event, parameters, msgChannel) -> {
            if (parameters != null && parameters.get(0).equalsIgnoreCase("Sonnenaufgang")
                    && event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                gameState.setMuteAllPlayers(game.livingPlayers, false);
                gameState.deleteWerwolfChat();
                gameState.changeDayPhase();
                return true;

            } else {
                return false;
            }
        };

        game.addPrivateCommand(game.userModerator.getId(), sonnenaufgangCommand);
    }

}