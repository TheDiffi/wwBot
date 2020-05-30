package wwBot.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.object.util.Snowflake;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;
import wwBot.Interfaces.Command;
import wwBot.Interfaces.PrivateCommand;

// ! WORK IN PROGRESS !

public class MainGameState extends GameState {

    public Map<Snowflake, Player> mapPlayers = new HashMap<Snowflake, Player>();
    public List<Boolean> nightRolesDone = new ArrayList<>();
    public Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>(
            String.CASE_INSENSITIVE_ORDER);

    MainGameState(Game game) {
        super(game);
        registerStateCommands();
        mapPlayers = game.mapPlayers;

        // loads living Players for the first time
        var livingPlayers = game.livingPlayers;
        for (var player : mapPlayers.entrySet()) {
            if (player.getValue().alive) {
                livingPlayers.put(player.getKey(), player.getValue());
            }
        }

        loadLivingRoles(livingPlayers);

        MessagesMain.onGameStart(game);

        // prüft ob die rolle bei den existierenden Rollen dabei ist und führt falls
        // true die Bedingungen des Günstling aus
        if (mapExistingRoles.get("Günstling") != null) {
            var playerWithThisRole = mapExistingRoles.get("Günstling").get(0);
            var privateChannel = playerWithThisRole.user.getPrivateChannel().block();

            var mssg = "";
            mssg += "Die Werwölfe sind: ";
            for (int i = 0; i < mapExistingRoles.get("Werwolf").size(); i++) {
                mssg += mapExistingRoles.get("Werwolf").get(i).name + " ";
            }
            if (mapExistingRoles.containsKey("Wolfsjunges")) {
                mssg += mapExistingRoles.get("Werwolf").get(0).name + " ";
            }

            Globals.createEmbed(privateChannel, Color.GREEN, "Günstling", mssg);
        }

        if (mapExistingRoles.get("Amor") != null) {
            var playerWithThisRole = mapExistingRoles.get("Amor").get(0);
            var privateChannel = playerWithThisRole.user.getPrivateChannel().block();

            // Amor in Love function
            var mssg = "Flüstere mir nun zwei Namen zu und ich lasse deine Liebespfeile ihr Ziel treffen. Doch gib Acht! Ich verstehe dich nur wenn du die Namen in **einer Nachricht** und mit **einem Leerzeichen** dazwischen schreibst. ";
            Globals.createEmbed(privateChannel, Color.GREEN,
                    "Es kitzelt dich in den Fingern und du weißt, es ist Zeit deinen Bogen auszupacken ", mssg);

            PrivateCommand amorCommand = (event, parameters, msgChannel) -> {
                var success = false;

                if (parameters != null && parameters.size() == 2 && parameters.get(0) != parameters.get(1)) {
                    var person1 = Globals.removeDash(parameters.get(0));
                    var person2 = Globals.removeDash(parameters.get(1));
                    Player firstLover = null;
                    Player secondLover = null;

                    firstLover = Globals.findPlayerByName(person1, mapPlayers, game);
                    secondLover = Globals.findPlayerByName(person2, mapPlayers, game);

                    if (firstLover != null && secondLover != null) {
                        success = true;
                        firstLover.inLoveWith = secondLover;
                        secondLover.inLoveWith = firstLover;
                        Globals.createEmbed(msgChannel, Color.PINK, "ERFOLG!", "" + firstLover.user.getUsername()
                                + " und " + secondLover.name + " haben sich unsterblich verliebt");
                        game.mainChannel.createMessage("Des Amors Liebespfeile haben ihr Ziel gefunden").block();
                        firstLover.user.getPrivateChannel().block().createMessage("Du fällst mit **" + secondLover.name
                                + "** in eine unsterbliche Liebe. \n Eure Liebe ist do groß, dass ihr euch kein Leben ohne einander vorstellen könnt und deshalb sterbt sobald euer Partner stirbt")
                                .block();
                        secondLover.user.getPrivateChannel().block().createMessage("Du triffst dich mit **"
                                + firstLover.name
                                + "** und verliebst dich Unsterblich in sie/ihn \n Eure Liebe ist do groß, dass ihr euch kein Leben ohne einander vorstellen könnt und deshalb sterbt sobald euer Partner stirbt")
                                .block();

                    } else {
                        Globals.createEmbed(msgChannel, Color.RED, "Error: Zwei identische Usernames gefunden",
                                "Tut mir leid, ich verstehe dich nicht. \n Deine Nachricht sollte aussehen: \n<Username> <Username> \n Achte darauf, dass du die Namen der Spieler richtig geschrieben hast und keine überflüssigen Leerzeichen gesetzt hast. Probiere es noch einmal!");
                        success = false;

                    }

                } else {
                    msgChannel.createMessage(
                            "Tut mir leid ich verstehe dich nicht \n Deine Nachricht sollte aussehen \n<Username>LEERZEICHEN<Username> \n Achte darauf, dass du die Namen der Spieler richtig geschrieben hast und keine überflüssigen Leerzeichen gesetzt hast. Probiere es noch einmal")
                            .block();
                    success = false;
                }
                return success;

            };
            game.addPrivateCommand(playerWithThisRole.user.getId(), amorCommand);

        }

        // überprüft ob jeder boolean der liste true ist
        var isDone = true;
        for (boolean bool1 : nightRolesDone) {
            if (!bool1) {
                isDone = false;
            }
        }
        // falls alle player fertig sind wird die nächste phase gestartet
        if (isDone) {

            // wwPhase.start();
        }

    }

    // läd jede noch Player der noch lebt als nach der Rolle geordnet in eine Map
    // mit dem Rollennamen als Key (Value = Liste wo alle Player mit derselben Rolle
    // vorhanden sind)
    private void loadLivingRoles(Map<Snowflake, Player> livingPlayers) {
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
        // Zyklus fortfährt
        mapExistingRoles.put("Werwolf", listWerwölfe);
        mapExistingRoles.put("Seher", listSeher);
        mapExistingRoles.put("Dorfbewohner", listDorfbewohner);

    }

    // loads the Commands available in this GameState into the map gameStateCommands
    private void registerStateCommands() {

        // ping testet ob der bot antwortet
        Command pingCommand = (event, parameters, msgChannel) -> {

            msgChannel.createMessage("Pong! Game").block();

        };
        gameStateCommands.put("ping", pingCommand);

        // zeigt die verfügbaren commands
        Command showCommandsCommand = (event, parameters, msgChannel) -> {
            var mssg = "";
            for (var command : gameStateCommands.entrySet()) {
                mssg += "\n" + command.getKey();
            }
            msgChannel.createMessage(mssg).block();
        };
        gameStateCommands.put("showCommands", showCommandsCommand);

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {

            msgChannel.createMessage("TODO: add help Command in Main State").block();
        };
        gameStateCommands.put("help", helpCommand);
        gameStateCommands.put("hilfe", helpCommand);

    }

}
