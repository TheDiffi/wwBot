package wwBot.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import wwBot.Card;
import wwBot.Command;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;
import wwBot.PrivateCommand;

public class SemiMainGameState extends GameState {

    public Map<Snowflake, Player> mapPlayers = new HashMap<Snowflake, Player>();
    public Map<Snowflake, Player> livingPlayers = new HashMap<Snowflake, Player>();
    public Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>(
            String.CASE_INSENSITIVE_ORDER);
    // -1 = 1stNight; 0 = Day; 1 = Night;
    public DayPhase dayPhase = DayPhase.FIRST_NIGHT;
    public Day day = null;
    public Night night = null;
    public Morning morning = null;
    public TextChannel wwChat = null;
    public User userModerator;

    SemiMainGameState(wwBot.Game game) {
        super(game);
        registerStateCommands();
        mapPlayers = game.mapPlayers;
        userModerator = game.userModerator;

        reloadGameLists();

        MessagesMain.onGameStart(game);

        greetMod(game);

    }

    private void greetMod(Game game) {

        userModerator.getPrivateChannel().block().createMessage(
                "Willkommen Moderator! \nDeine Aufgabe ist es das Spiel für beide Parteien so fair wie möglich zu machen! \nDu kannst diesen Textkanal für Notizen benutzen.\nDu kannst nun mit dem Command \"Ready\" die erste Nacht Starten.")
                .block();

        printLivingRoles(userModerator.getPrivateChannel().block());

        PrivateCommand readyCommand = (event, parameters, msgChannel) -> {
            if (parameters != null && parameters.get(0).equalsIgnoreCase("Ready")) {
                initiateFirstNight();
                return true;

            } else {
                return false;
            }
        };
        game.addPrivateCommand(userModerator.getId(), readyCommand);
    }

    @Override
    public boolean handleCommand(String requestedCommand, MessageCreateEvent event, List<String> parameters,
            MessageChannel runningInChannel) {
        var found = false;
        if (livingPlayers.containsKey(event.getMessage().getAuthor().get().getId())
                || event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {

            // checks the Command map of the current DayPhase
            if (dayPhase == DayPhase.MORNING) {
                var foundCommand = morning.mapCommands.get(requestedCommand);
                if (foundCommand != null) {
                    foundCommand.execute(event, parameters, runningInChannel);
                    found = true;
                } else if (!found && day != null && day.mapCommands.containsKey(requestedCommand)) {
                    event.getMessage().getChannel().block().createMessage("This command is only available during Day")
                            .block();
                    found = true;
                } else {
                    found = false;
                }
            } else if (dayPhase == DayPhase.DAY) {
                var foundCommand = day.mapCommands.get(requestedCommand);
                if (foundCommand != null) {
                    foundCommand.execute(event, parameters, runningInChannel);
                    found = true;
                } else if (!found && night != null && night.mapCommands.containsKey(requestedCommand)) {
                    event.getMessage().getChannel().block().createMessage("This command is only available during Night")
                            .block();
                    found = true;
                } else {
                    found = false;
                }
            } else if (dayPhase == DayPhase.NORMALE_NIGHT) {
                var foundCommand = night.mapCommands.get(requestedCommand);
                if (foundCommand != null) {
                    foundCommand.execute(event, parameters, runningInChannel);
                    found = true;
                } else if (!found && morning != null && morning.mapCommands.containsKey(requestedCommand)) {
                    event.getMessage().getChannel().block()
                            .createMessage("This command is only available during the Morning").block();
                    found = true;
                } else {
                    found = false;
                }
            }

            if (!found) {
                found = super.handleCommand(requestedCommand, event, parameters, runningInChannel);
            }

        } else {
            event.getMessage().getChannel().block().createMessage("Only living Players have accssess to this Command")
                    .block();
        }

        return found;
    }

    private void registerStateCommands() {
        var mapAvailableCards = Globals.mapAvailableCards;

        // ping testet ob der bot antwortet
        Command pingCommand = (event, parameters, msgChannel) -> {
            msgChannel.createMessage("Pong! SemiMainGameState").block();

        };
        gameStateCommands.put("ping", pingCommand);

        // zeigt die verfügbaren commands
        Command showCommandsCommand = (event, parameters, msgChannel) -> {
            var mssg = "";
            for (var command : gameStateCommands.entrySet()) {
                mssg += "\n*&" + command.getKey() + "*";
            }
            msgChannel.createMessage(mssg).block();
        };
        gameStateCommands.put("showCommands", showCommandsCommand);

        // prints the living players and their role
        Command listPlayersCommand = (event, parameters, msgChannel) -> {

            // compares the Snowflake of the Author to the Snowflake of the Moderator
            if (event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {
                printLivingRoles(userModerator.getPrivateChannel().block());
            } else {
                msgChannel.createMessage("only the moderator can use this command");
            }
        };
        gameStateCommands.put("listPlayers", listPlayersCommand);

        // lets the moderator kill a person and checks the consequences
        Command killCommand = (event, parameters, msgChannel) -> {
            // only the moderator can use this command
            if (event.getMessage().getAuthor().get().getId().equals(game.userModerator.getId())) {
                if (parameters.size() == 2) {
                    // finds the requested Player
                    var unluckyPlayer = Globals.findPlayerByName(parameters.get(0), game.livingPlayers);
                    // gets the cause
                    var causedBy = parameters.get(1);
                    // finds the cause (role)
                    var causedByRole = mapAvailableCards.get(causedBy);
                    if (unluckyPlayer != null && (causedByRole != null || causedBy.equalsIgnoreCase("null"))) {
                        killPlayer(unluckyPlayer, causedByRole);
                        event.getMessage().getChannel().block().createMessage("Erfolg!").block();
                    } else {
                        event.getMessage().getChannel().block().createMessage(
                                "Ich verstehe dich nicht 😕\nDein Command sollte so aussehen: \n&kill <PlayerDerSterbenSoll> <RolleWelchenDenSpielerTötet> \nFalls du dir nicht sicher bist, wodurch der Spieler getötet wurde, schreibe \"null\" (Nicht immer ist die der Verntwortliche gemeint, sondern die Rolle, welche zu diesem Tod geführt hat z.B. bei Liebe -> Amor)")
                                .block();
                    }

                } else {
                    event.getMessage().getChannel().block().createMessage(
                            "Ich verstehe dich nicht 😕\nDein Command sollte so aussehen: \n&kill <PlayerDerSterbenSoll> <RolleWelchenDenSpielerTötet> \nFalls du dir nicht sicher bist, wodurch der Spieler getötet wurde, schreibe \"null\" (Nicht immer ist die der Verntwortliche gemeint, sondern die Rolle, welche zu diesem Tod geführt hat z.B. bei Liebe -> Amor)")
                            .block();

                }
            } else {
                event.getMessage().getChannel().block().createMessage("You have no permission for this command")
                        .block();
            }
        };
        gameStateCommands.put("kill", killCommand);

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

    public void killPlayer(Player unluckyPlayer, Card causedByRole) {
        var dies = true;

        // checks if the player dies
        dies = checkIfDies(unluckyPlayer, causedByRole, dies);

        if (dies) {
            // kills player
            unluckyPlayer.alive = false;
            game.deadPlayers.add(unluckyPlayer);

            // reveals the players death and identity
            checkDeathMessages(unluckyPlayer, causedByRole);

            Globals.printCard(unluckyPlayer.role.name, game.mainChannel);

            // calculates the consequences

            checkConsequences(unluckyPlayer, causedByRole);
        }

    }

    private void checkConsequences(Player unluckyPlayer, Card causedByRole) {
        var mapAvailableCards = Globals.mapAvailableCards;

        // recieves true if the game ended
        var gameEnded = checkIfGameEnds();

        // if the game did not end, checkConsequences continues
        if (!gameEnded) {

            if (unluckyPlayer.role.name.equalsIgnoreCase("Seher")) {
                // looks if there is a Zauberlehrling in the game
                for (var player : game.livingPlayers.entrySet()) {
                    // if he finds a Lehrling he is the new Seher
                    if (player.getValue().role.name.equalsIgnoreCase("SeherLehrling")) {
                        player.getValue().role = mapAvailableCards.get("Seher");
                        MessagesMain.seherlehrlingWork(game, unluckyPlayer);
                    }
                }

            } else if (unluckyPlayer.role.name.equalsIgnoreCase("Aussätzige")) {
                // if killed by Werwölfe
                if (causedByRole != null && causedByRole.name.equalsIgnoreCase("Werwolf")) {
                    // if the dying player is the Aussätzige, the Werwölfe kill noone the next night
                    MessagesMain.verfluchtenMutation(game, unluckyPlayer);
                    Globals.createMessage(game.userModerator.getPrivateChannel().block(),
                            "Die Aussätzige ist gestorben! Vergiss nicht, in der nächsten Nacht dürfen die Werwölfe niemanden töten",
                            false);
                }

            } else if (unluckyPlayer.role.name.equalsIgnoreCase("Wolfsjunges")) {
                // if not killed by Werwölfe (does not make sense but ok.)
                if (causedByRole != null && !causedByRole.name.equalsIgnoreCase("Werwolf")) {
                    // if the Wolfsjunges dies, the WW can kill two players in the following night.
                    Globals.createMessage(game.userModerator.getPrivateChannel().block(),
                            "Das Wolfsjunges ist gestorben! Vergiss nicht, in der nächsten Nacht dürfen die Werwölfe zwei Personen töten.",
                            false);
                }
            } else if (unluckyPlayer.role.name.equalsIgnoreCase("Jäger")) {
                MessagesMain.jägerDeath(game, unluckyPlayer);

                PrivateCommand jägerCommand = (event, parameters, msgChannel) -> {
                    if (parameters != null) {
                        var player = Globals.findPlayerByName(parameters.get(0), game.livingPlayers);
                        // if a player is found
                        if (player != null) {
                            killPlayer(unluckyPlayer, mapAvailableCards.get("Jäger"));
                            return true;
                        } else {
                            event.getMessage().getChannel().block()
                                    .createMessage("Ich konnte diesen Spieler leider nicht finden").block();
                            return false;

                        }
                    } else {
                        return false;
                    }
                };
                game.addPrivateCommand(unluckyPlayer.user.getId(), jägerCommand);

            }
        }
    }

    private void initiateFirstNight() {

        setMuteAllPlayers(game.livingPlayers, true);

        var listRolesToBeCalled = new ArrayList<Player>();
        var uniqueRolesInThisPhase = new ArrayList<String>();

        wwChat = createWerwolfChat();

        // generates which Roles need to be called

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
            }
        }

        if (mapExistingRoles.containsKey("Seher")) {
            for (Player player : mapExistingRoles.get("Seher")) {
                listRolesToBeCalled.add(player);
            }
        }

        MessagesMain.firstNightMod(game, listRolesToBeCalled);

        if (mapExistingRoles.get("Günstling") != null) {
            var privateChannel = mapExistingRoles.get("Günstling").get(0).user.getPrivateChannel().block();

            MessagesMain.günstlingMessage(privateChannel, mapExistingRoles);
        }

        endFirstNight();

    }

    private void endFirstNight() {
        Globals.createEmbed(userModerator.getPrivateChannel().block(), Color.orange,
                "Wenn bu bereit bist die erste Nacht zu beenden tippe den Command \"Sonnenaufgang\"",
                "PS: niemand stirbt in der ersten Nacht");

        // Sonnenaufgang lässt den ersten Tag starten und beginnt den Zyklus
        PrivateCommand sonnenaufgangCommand = (event, parameters, msgChannel) -> {
            if (parameters != null && parameters.get(0).equalsIgnoreCase("Sonnenaufgang")
                    && event.getMessage().getAuthor().get().getId().equals(userModerator.getId())) {

                deleteWerwolfChat();
                changeDayPhase();
                return true;

            } else {
                return false;
            }
        };
        
        game.addPrivateCommand(userModerator.getId(), sonnenaufgangCommand);
    }

    public void setMuteAllPlayers(Map<Snowflake, Player> mapPlayers, boolean isMuted) {
        // mutes all players at night
        for (var player : mapPlayers.entrySet()) {
            try {
                player.getValue().user.asMember(game.server.getId()).block().edit(a -> {
                    a.setMute(isMuted).setDeafen(false);
                }).block();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // creates a private MessageChannel and puts all the WW and the Moderator ob the
    // Whitelist
    @Override
    public TextChannel createWerwolfChat() {

        if (wwChat != null) {
            deleteWerwolfChat();
        }

        var defaultRole = game.server.getRoles().toStream().filter(r -> r.getName().equals("@everyone")).findFirst()
                .get();

        var wwChat = game.server.createTextChannel(spec -> {
            var overrides = new HashSet<PermissionOverwrite>();
            overrides.add(PermissionOverwrite.forRole(defaultRole.getId(), PermissionSet.none(),
                    PermissionSet.of(Permission.VIEW_CHANNEL)));
            for (var player : mapExistingRoles.get("Werwolf")) {
                overrides.add(PermissionOverwrite.forMember(player.user.asMember(game.server.getId()).block().getId(),
                        PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()));
            }
            if (!game.gameRuleAutomatic) {
                overrides.add(
                        PermissionOverwrite.forMember(game.userModerator.asMember(game.server.getId()).block().getId(),
                                PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()));
            }

            spec.setPermissionOverwrites(overrides);
            spec.setName("Werwolf-Chat");
        }).block();
        return wwChat;
    }

    // if present, deletes the wwChat
    @Override
    public void deleteWerwolfChat() {

        if (game.server.getChannelById(wwChat.getId()).block() != null) {
            game.server.getChannelById(wwChat.getId()).block().delete();
        }
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

    // collects every "good" and every "bad" role in a list and compares the size.
    // If the are equaly or less "good" than "bad" roles, the ww won
    private boolean checkIfGameEnds() {
        var amountGoodPlayers = 0;
        var amountBadPlayers = 0;
        var amountWW = 0;
        for (var playerEntry : game.livingPlayers.entrySet()) {
            if (playerEntry.getValue().role.friendly) {
                amountGoodPlayers++;
            } else if (!playerEntry.getValue().role.friendly) {
                amountBadPlayers++;
            } else if (!playerEntry.getValue().role.name.equalsIgnoreCase("Werwolf")) {
                amountWW++;
            }
        }
        if (amountWW < 1) {
            // int winner: 1 = Dorfbewohner, 2 = Werwölfe
            game.gameState.endMainGame(1);
            return true;
        } else if (amountBadPlayers >= amountGoodPlayers) {
            game.gameState.endMainGame(2);
            return true;
        } else {
            return false;
        }
    }

    // checks the conditions if the player dies
    private boolean checkIfDies(Player unluckyPlayer, Card causedByRole, Boolean dies) {
        if (unluckyPlayer.role.name.equals("Verfluchter") && causedByRole.name.equals("Werwolf")) {
            dies = false;
            Globals.createMessage(game.mainChannel, "Der Verfluchte hat Mutiert", true);
        }
        return dies;
    }

    private void checkDeathMessages(Player player, Card cause) {

        if (cause.name.equalsIgnoreCase("Werwolf")) {
            MessagesMain.deathByWW(game, player);
        } else if (cause.name.equalsIgnoreCase("Hexe") || cause.name.equalsIgnoreCase("Magier")) {
            MessagesMain.deathByMagic(game, player);
        } else if (cause.name.equalsIgnoreCase("Amor")) {
            MessagesMain.deathByLove(game, player);
        } else if (cause.name.equalsIgnoreCase("Jäger")) {
            MessagesMain.deathByGunshot(game, player);
        } else if (cause.name.equalsIgnoreCase("Dorfbewohner")) {
            MessagesMain.deathByLynchen(game, player);
        } else {
            MessagesMain.death(game, player);

        }
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

    private void printLivingRoles(MessageChannel channel) {
        var mssgPlayerList = "";
        for (var entry : game.livingPlayers.entrySet()) {

            mssgPlayerList += entry.getValue().user.getUsername() + " ist ein/e " + entry.getValue().role.name + "\n";
        }
        Globals.createEmbed(userModerator.getPrivateChannel().block(), Color.WHITE, "Spielerliste", mssgPlayerList);
    }

    @Override
    public void changeDayPhase() {
        reloadGameLists();
        // transitions to Night
        if (dayPhase == DayPhase.DAY) {
            setMuteAllPlayers(game.livingPlayers, true);
            dayPhase = DayPhase.NORMALE_NIGHT;
            night = new Night(game);
            createWerwolfChat();

            // transitions to Morning
        } else if (dayPhase == DayPhase.NORMALE_NIGHT) {
            setMuteAllPlayers(game.livingPlayers, false);
            deleteWerwolfChat();
            MessagesMain.onMorningSemi(game);
            dayPhase = DayPhase.MORNING;
            morning = new Morning(game);

            // transitions to Day
        } else if (dayPhase == DayPhase.MORNING || dayPhase == DayPhase.FIRST_NIGHT) {
            MessagesMain.onDaySemi(game);
            dayPhase = DayPhase.DAY;
            day = new Day(game);
        }

    }

    @Override
    public void endMainGame(int winner) {
        // sends gameover message
        if (winner == 1) {
            Globals.createEmbed(game.mainChannel, Color.GREEN, "GAME END: DIE DORFBEWOHNER GEWINNEN!", "");
        } else if (winner == 2) {
            Globals.createEmbed(game.mainChannel, Color.RED, "GAME END: DIE WERWÖLFE GEWINNEN!", "");
        }
        // changes gamestate
        game.changeGameState(new PostGameState(game, winner));
    }

}

enum DayPhase {
    FIRST_NIGHT, NORMALE_NIGHT, DAY, MORNING
}