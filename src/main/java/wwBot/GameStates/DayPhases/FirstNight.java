package wwBot.GameStates.DayPhases;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import wwBot.Game;
import wwBot.Globals;
import wwBot.MessagesMain;
import wwBot.Player;
import wwBot.GameStates.GameState;
import wwBot.Interfaces.Command;
import wwBot.Interfaces.PrivateCommand;
import wwBot.cards.RoleDoppelgängerin;

public class FirstNight {
    GameState gameState;
    Game game;
    public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>(String.CASE_INSENSITIVE_ORDER);

    public FirstNight(Game getGame) {
        game = getGame;
        gameState = game.gameState;
        mapExistingRoles = game.gameState.mapExistingRoles;
        initiateFirstNight();

    }

    private void initiateFirstNight() {
        // sets mute and creates the WWchat
        gameState.setMuteAllPlayers(game.livingPlayers, true);
        gameState.wwChat = gameState.createWerwolfChat();

        // generates which Roles need to be called
        var listRolesToBeCalled = firstNightRoles();
        MessagesMain.firstNightMod(game, listRolesToBeCalled);

        // specific cards like the amor are handeled
        specificCardInteractions();

        endFirstNight();

    }

    private void specificCardInteractions() {
        // the Günstling gets a list with all the WW
        if (mapExistingRoles.get("Günstling") != null) {
            var privateChannel = mapExistingRoles.get("Günstling").get(0).user.getPrivateChannel().block();
            MessagesMain.günstlingMessage(privateChannel, mapExistingRoles, game);

        }

        // if there is a amor, the moderator gets access to a command, which sets the
        // "inLoveWith" variable of two players to eachother
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

                        // sets the "inLoveWith" variables
                        if (player1 != null && player2 != null) {
                            if (player1 != player2) {
                                player1.role.inLoveWith = player2;
                                player2.role.inLoveWith = player1;
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
            mapCommands.put("Amor", setLoveCommand);
        }

        // if there is a amor, the moderator gets access to a command, which sets the
        // "inLoveWith" variable of two players to eachother
        if (mapExistingRoles.get("Doppelgängerin") != null) {

            MessagesMain.triggerDoppelgängerin(game);
            Command setDoppelgängerinCommand = (event, parameters, msgChannel) -> {
                if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                    if (parameters != null && parameters.size() == 1) {
                        // finds the players
                        var foundPlayer = Globals.findPlayerByName(Globals.removeDash(parameters.get(0)),
                                game.mapPlayers, game);

                        if (foundPlayer != null) {
                            var dp = mapExistingRoles.get("Doppelgängerin").get(0);

                            // sets the variable
                            var dpRole = (RoleDoppelgängerin) dp.role;
                            dpRole.boundTo = foundPlayer;
                            MessagesMain.doppelgängerinSuccess(game, dp, foundPlayer);
                            
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
            mapCommands.put("clone", setDoppelgängerinCommand);
            mapCommands.put("Doppelgängerin", setDoppelgängerinCommand);

        }

    }

    private void endFirstNight() {
        Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.orange,
                "Wenn bu bereit bist die erste Nacht zu beenden tippe den Command \"Sonnenaufgang\"",
                "PS: niemand stirbt in der ersten Nacht");

        // Sonnenaufgang lässt den ersten Tag starten und beginnt den Zyklus
        PrivateCommand sonnenaufgangCommand = (event, parameters, msgChannel) -> {
            if (parameters != null && parameters.get(0).equalsIgnoreCase("Sonnenaufgang")
                    && event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                // unmutes, deletes the WWChat and changes the DayPhase
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

}