package wwBot.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import wwBot.Command;
import wwBot.Globals;
import wwBot.Player;
import wwBot.PrivateCommand;

public class SemiMainGameState extends GameState {

    public Map<Snowflake, Player> mapPlayers = new HashMap<Snowflake, Player>();
    public Map<Snowflake, Player> livingPlayers = new HashMap<Snowflake, Player>();
    public Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>(
            String.CASE_INSENSITIVE_ORDER);
    public boolean isDay = false;
    public Day day = new Day(game);
    public Night night = new Night(game);

    public User userModerator;

    SemiMainGameState(wwBot.Game game) {
        super(game);
        registerStateCommands();
        mapPlayers = game.mapPlayers;
        userModerator = game.userModerator;

        reloadGameLists();

        MessagesMain.onGameStart(game);

        userModerator.getPrivateChannel().block().createMessage(
                "Willkommen Moderator! \n Deine Aufgabe ist es das spiel für beide parteien so fair wie möglich zu machen! \n Du kannst diesen Textkanal für Notizen benutzen.\nDu kannst nun mit dem Command \"Ready\" die erste Nacht Starten.")
                .block();
        PrivateCommand readyCommand = (event, parameters, msgChannel) -> {
            if (parameters != null && parameters.get(0).equalsIgnoreCase("Ready")) {
                initiateFirstNight();

                return true;
            } else {
                return false;
            }
        };
        game.mapPrivateCommands.put(userModerator.getId(), readyCommand);

    }

    @Override
    public boolean handleCommand(String requestedCommand, MessageCreateEvent event, List<String> parameters,
            MessageChannel runningInChannel) {
        var found = super.handleCommand(requestedCommand, event, parameters, runningInChannel);

        if (livingPlayers.containsKey(event.getMessage().getAuthor().get().getId())) {
            // checks the Command map of the current DayPhase
            if (isDay) {
                var foundCommand = day.mapCommands.get(requestedCommand);
                if (foundCommand != null) {
                    foundCommand.execute(event, parameters, runningInChannel);
                    found = true;
                } else if (!found && night.mapCommands.containsKey(requestedCommand)) {
                    event.getMessage().getChannel().block().createMessage("This command is only available during Night")
                            .block();
                    found = true;
                }
            } else if (!isDay) {
                var foundCommand = night.mapCommands.get(requestedCommand);
                if (foundCommand != null) {
                    foundCommand.execute(event, parameters, runningInChannel);
                    found = true;
                } else if (!found && day.mapCommands.containsKey(requestedCommand)) {
                    event.getMessage().getChannel().block().createMessage("This command is only available during Day")
                            .block();
                    found = true;
                }

            }
        } else {
            event.getMessage().getChannel().block().createMessage("Only living Players have accssess to this Command")
                    .block();
        }

        return found;
    }

    private void initiateFirstNight() {
        var listRolesToBeCalled = new ArrayList<Player>();
        var uniqueRolesInThisPhase = new ArrayList<String>();
        uniqueRolesInThisPhase.add("Günstling");
        uniqueRolesInThisPhase.add("Amor");
        uniqueRolesInThisPhase.add("Doppelgängerin");
        for (String role : uniqueRolesInThisPhase) {
            if (mapExistingRoles.containsKey(role)) {
                listRolesToBeCalled.add(mapExistingRoles.get(role).get(0));
            }
        }

        if (mapExistingRoles.containsKey("Werwolf")) {
            for (Player player : mapExistingRoles.get("Werwolf")) {
                listRolesToBeCalled.add(player);
                // Todo: create private messageChannel and add this player
            }
        } else if (mapExistingRoles.containsKey("Seher")) {
            for (Player player : mapExistingRoles.get("Seher")) {
                listRolesToBeCalled.add(player);
            }
        }
        MessagesMain.firstNightManual(game);
        var mssg = "";
        for (Player player : listRolesToBeCalled) {
            mssg += player.user.getUsername() + ": ist " + player.role.name + "\n";
        }
        mssg += "Tipp: benutz &showCard <NameDerKarte> um dir die Details der Karte nochmals anzusehen";
        Globals.createEmbed(userModerator.getPrivateChannel().block(), Color.DARK_GRAY,
                "Diese Rollen müssen in dieser Nacht aufgerufen werden:", mssg);

        if (mapExistingRoles.get("Günstling") != null) {
            var playerWithThisRole = mapExistingRoles.get("Günstling").get(0);
            var privateChannel = playerWithThisRole.user.getPrivateChannel().block();

            mssg = "";
            mssg += "Die Werwölfe sind: ";
            for (int i = 0; i < mapExistingRoles.get("Werwolf").size(); i++) {
                mssg += mapExistingRoles.get("Werwolf").get(i).user.getUsername() + " ";
            }
            if (mapExistingRoles.containsKey("Wolfsjunges")) {
                mssg += mapExistingRoles.get("Werwolf").get(0).user.getUsername() + " ";
            }

            Globals.createEmbed(privateChannel, Color.GREEN, "Günstling", mssg);
        }

        endFirstNight();

    }

    private void endFirstNight() {
        Globals.createEmbed(userModerator.getPrivateChannel().block(), Color.GREEN,
                "Wenn bu bereit bist die erste Nacht zu beenden tippe den Command \"Sonnenaufgang\"",
                "PS: niemand stirbt in der ersten Nacht");
        // Sonnenaufgang lässt den ersten Tag starten und beginnt den Zyklus
        PrivateCommand sonnenaufgangCommand = (event, parameters, msgChannel) -> {
            if (parameters != null && parameters.get(0).equalsIgnoreCase("Sonnenaufgang")
                    && event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
                changeDayPhase();

                return true;
            } else {
                return false;
            }
        };
        game.mapPrivateCommands.put(userModerator.getId(), sonnenaufgangCommand);
    }

    private void reloadGameLists() {
        // reloands the living Players
        livingPlayers.clear();
        for (var player : game.mapPlayers.entrySet()) {
            if (player.getValue().alive) {
                livingPlayers.put(player.getKey(), player.getValue());
            }
        }
        game.livingPlayers = livingPlayers;

        // läd jede noch Player der noch lebt als nach der Rolle geordnet in eine Map
        // mit dem Rollennamen als Key (Value = Liste wo alle Player mit derselben Rolle
        // vorhanden sind)
        mapExistingRoles.clear();
        var listWerwölfe = new ArrayList<Player>();
        var listDorfbewohner = new ArrayList<Player>();
        var listSeher = new ArrayList<Player>();

        for (var entry : livingPlayers.entrySet()) {
            // prüft ob WW, Dorfbewohner oder Seher, falls nichts von dem bekommt die Rolle
            // ihre eigene Liste
            if (entry.getValue().role.name.equalsIgnoreCase("Werwolf") && entry.getValue().alive) {
                listWerwölfe.add(entry.getValue());
            } else if (entry.getValue().role.name.equalsIgnoreCase("Seher")) {
                listSeher.add(entry.getValue());
            } else if (entry.getValue().role.name.equalsIgnoreCase("Dorfbewohner")) {
                listDorfbewohner.add(entry.getValue());
            } else {
                var tempList = Arrays.asList(entry.getValue());
                mapExistingRoles.put(entry.getValue().role.name, tempList);
                // siehe kommentar unten
            }
        }
        // für jede hinzugefügte Rolle wird auch ein Eintrag in nightRolesDone
        // hinzugefügt, jeder Eintrag muss später auf true gesetzt werden, damit der
        // Zykus fortfährt
        mapExistingRoles.put("Werwolf", listWerwölfe);
        mapExistingRoles.put("Seher", listSeher);
        mapExistingRoles.put("Dorfbewohner", listDorfbewohner);

    }

    private void registerStateCommands() {

        // shows the current Deck to the user
        Command showDeckCommand = (event, parameters, msgChannel) -> {
            // prints the deck
            msgChannel.createMessage(Globals.cardListToString(game.deck, "Deck")).block();

        };
        gameStateCommands.put("showDeck", showDeckCommand);

        // shows the moderator the list of players (alive or all)
        Command printListCommand = (event, parameters, msgChannel) -> {

            // compares the Snowflake of the Author to the Snowflake of the Moderator
            if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
                // checks if the syntax is correct
                if (parameters != null && parameters.size() != 0) {
                    var param = parameters.get(0);
                    // if the user typed "Players" it prints a list of all players, if he typed
                    // "Living" it prints only the living players
                    if (param.equalsIgnoreCase("Players")) {
                        printPlayers(userModerator.getPrivateChannel().block(), mapPlayers);
                    } else if (param.equalsIgnoreCase("Living")) {
                        printPlayers(userModerator.getPrivateChannel().block(), livingPlayers);
                    }
                } else {
                    userModerator.getPrivateChannel().block()
                            .createMessage("Wrong syntax! try \"&showList Players\" or \"&showList Living\"").block();

                }
            } else {
                event.getMessage().getChannel().block().createMessage("only the moderator can use this command")
                        .block();
            }

        };
        gameStateCommands.put("printList", printListCommand);

    }

    public void printPlayers(MessageChannel msgChannel, Map<Snowflake, Player> map) {
        var mssgList = "";
        for (var playerset : map.entrySet()) {
            var player = playerset.getValue();
            mssgList += "*" + player.user.getUsername() + "*  ";
            mssgList += "ist ->  " + player.role.name;
            mssgList += "   am Leben: " + Boolean.toString(player.alive) + "\n";
        }
        Globals.createEmbed(msgChannel, Color.DARK_GRAY, "Liste aller Spieler", mssgList);
    }

    public void printPlayers(MessageChannel msgChannel, List<Player> list) {
        var mssgList = "";
        for (var player : list) {
            mssgList += player.user.getUsername() + ": ";
            mssgList += "ROLE(" + player.role.name + ") ";
            mssgList += Boolean.toString(player.alive) + "\n";
        }
        Globals.createEmbed(msgChannel, Color.DARK_GRAY, "Liste aller Spieler", mssgList);
    }

    @Override
    public void changeDayPhase() {
        reloadGameLists();
        if (isDay) {
            isDay = false;
            night = new Night(game);

        } else if (!isDay) {
            isDay = true;
            day = new Day(game);
        }

    }

}