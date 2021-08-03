package wwBot.WerwolfGame.GameStates.DayPhases.Semi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import wwBot.Globals;
import wwBot.Interfaces.Command;
import wwBot.Interfaces.PrivateCommand;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.WerwolfGame.Player;
import wwBot.WerwolfGame.GameStates.GameState;
import wwBot.WerwolfGame.GameStates.MainState.DayPhase;

public class FirstNightSemi {
    public Map<String, Command> mapCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
    Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>(String.CASE_INSENSITIVE_ORDER);
    GameState gameState;
    Game game;

    public FirstNightSemi(Game getGame) {
        game = getGame;
        gameState = game.gameState;
        mapExistingRoles = game.gameState.mapExistingRoles;

        registerNightCommands();

        // sends the first messages
        MessagesWW.onGameStartSemi(game);
        greetMod(game);

    }

    // greets the mod and waits for the mod to start the first night
    private void greetMod(Game game) {
        MessagesWW.greetMod(game);
        Globals.printPlayersMap(game.userModerator.getPrivateChannel().block(), game.mapPlayers, "Alle Spieler", true);

        PrivateCommand readyCommand = (event, parameters, msgChannel) -> {
            if (parameters != null && parameters.get(0).equalsIgnoreCase("Ready")) {
                initiateFirstNight();
                return true;

            } else {
                return false;
            }
        };
        game.addPrivateCommand(game.userModerator.getId(), readyCommand);

    }

    private void initiateFirstNight() {
        // sets mute and creates the WWchat
        Globals.setMuteAllPlayers(game.livingPlayers, true, game.server.getId());
        gameState.createWerwolfChat();

        // generates which Roles need to be called
        var listRolesToBeCalled = firstNightRoles();
        MessagesWW.onFirstNightSemi(game, listRolesToBeCalled);

        // specific cards like the amor are handeled
        specificCardInteractions();

        Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.orange,
                "Wenn bu bereit bist die erste Nacht zu beenden tippe den Command \"&endNight\"",
                "PS: niemand stirbt in der ersten Nacht");

    }

    private void specificCardInteractions() {
        // the Günstling gets a list with all the WW
        if (mapExistingRoles.get("Günstling") != null) {
            var privateChannel = mapExistingRoles.get("Günstling").get(0).user.getPrivateChannel().block();
            MessagesWW.günstlingMessage(privateChannel, mapExistingRoles, game);

        }

        // if there is a AMOR, the moderator gets access to a command, which sets the
        // "inLoveWith" variable of two players to eachother
        if (mapExistingRoles.get("Amor") != null) {
            MessagesWW.triggerAmor(game, null);
    
        }

        // Doppelgängerin
        if (mapExistingRoles.get("Doppelgängerin") != null) {
            MessagesWW.triggerDoppelgängerin(game, null);
            
        }

    }

    //assembles list of roles to act this night
    private ArrayList<Player> firstNightRoles() {
        var uniqueRolesInThisPhase = new ArrayList<String>();
        var list = new ArrayList<Player>();

        uniqueRolesInThisPhase.add("Günstling");
        uniqueRolesInThisPhase.add("Amor");
        uniqueRolesInThisPhase.add("Doppelgängerin");
        for (String name : uniqueRolesInThisPhase) {
            if (mapExistingRoles.containsKey(name)) {
                list.add(mapExistingRoles.get(name).get(0));
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
    // --------------- Commands -------------------

    public void registerNightCommands() {

        // replys with pong!
        Command pingCommand = (event, parameters, msgChannel) -> {
            event.getMessage().getChannel().block().createMessage("Pong! FirstNightPhase").block();
        };
        mapCommands.put("ping", pingCommand);

        // shows the available Commands in this Phase
        Command helpCommand = (event, parameters, msgChannel) -> {
            // replies to the moderator
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                MessagesWW.sendHelpFirstNightMod(msgChannel);
            } else {
                MessagesWW.sendHelpFirstNight(msgChannel, false);
            }
        };
        mapCommands.put("help", helpCommand);
        mapCommands.put("hilfe", helpCommand);

        // shows the moderator the list of players
        Command endNightCommand = (event, parameters, msgChannel) -> {

            // compares the Snowflake of the Author to the Snowflake of the Moderator
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                endFirstNight();
            } else {
                MessagesWW.errorModOnlyCommand(msgChannel);
            }
        };
        mapCommands.put("endNight", endNightCommand);
        mapCommands.put("next", endNightCommand);

    }

    private void endFirstNight() {
        Globals.createEmbed(game.userModerator.getPrivateChannel().block(), Color.GREEN, "Confirmed!", "Switching to Day");
        // unmutes, deletes the WWChat and changes the DayPhase
        Globals.setMuteAllPlayers(game.livingPlayers, false, game.server.getId());
        gameState.deleteWerwolfChat();
        gameState.changeDayPhase(DayPhase.DAY);

    }
}
