package wwBot.GameStates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import discord4j.core.object.util.Snowflake;
import wwBot.Command;
import wwBot.Game;
import wwBot.Globals;
import wwBot.Player;
import wwBot.PrivateCommand;

public class MainGameState extends GameState {

    public List<Boolean> nightRolesDone = new ArrayList<>();
    public Map<String, List<Player>> mapExistingRoles = new TreeMap<String, List<Player>>(
            String.CASE_INSENSITIVE_ORDER);

    MainGameState(Game game) {
        super(game);
        registerStateCommands();

        //loads living Players for the first time
        var livingPlayers = game.livingPlayers;
        for (var player : game.mapPlayers.entrySet()) {
            if(player.getValue().alive){
                livingPlayers.put(player.getKey(), player.getValue());
            }
        }
        
        loadLivingRoles(livingPlayers);

        sendMessageOnFirstNight();
        // prüft ob die rolle bei den existierenden Rollen dabei ist und führt falls
        // true die Bedingungen des Günstling aus
        if (mapExistingRoles.get("Günstling") != null) {
            var playerWithThisRole = mapExistingRoles.get("Günstling").get(0);
            var privateChannel = playerWithThisRole.user.getPrivateChannel().block();

            var mssg = "";
            mssg += "Die Werwölfe sind: ";
            for (int i = 0; i < mapExistingRoles.get("Werwolf").size(); i++) {
                mssg += mapExistingRoles.get("Werwolf").get(i).user.getUsername() + " ";
            }

            Globals.createEmbed(privateChannel, Color.GREEN, "Günstling", mssg);
        }

        if (mapExistingRoles.get("Amor") != null) {
            var playerWithThisRole = mapExistingRoles.get("Amor").get(0);
            var privateChannel = playerWithThisRole.user.getPrivateChannel().block();
            
            // TODO: add Amor function
            var mssg = "Flüstere mir nun zwei Namen zu und ich lasse deine Liebespfeile ihr Ziel treffen. Doch gib Acht! Ich verstehe dich nur wenn du die Namen in **einer Nachricht** und mit **einem Leerzeichen** dazwischen schreibst. ";
            Globals.createEmbed(privateChannel, Color.GREEN,
                    "Es kitzelt dich in den Fingern und du weißt, es ist Zeit deinen Bogen auszupacken ", mssg);

            PrivateCommand amorCommand = (event, parameters, msgChannel) -> {
                var succsess = false;
                if (parameters != null && parameters.size() == 2 && parameters.get(0) != parameters.get(1)) {
                    Player person1 = null;
                    Player person2 = null;
                    int found = 0;

                    for (var player : game.mapPlayers.entrySet()) {
                        if (player.getValue().user.getUsername().equalsIgnoreCase(parameters.get(0))) {
                            person1 = player.getValue();
                            found++;
                        }
                    }
                    for (var player : game.mapPlayers.entrySet()) {
                        if (player.getValue().user.getUsername().equalsIgnoreCase(parameters.get(1))) {
                            person2 = player.getValue();
                            found++;

                        }
                    }
                    if (found == 2 && person1 != null && person2 != null) {
                        succsess = true;
                        person1.inLoveWith = person2;
                        person2.inLoveWith = person1;
                        Globals.createEmbed(msgChannel, Color.PINK, "ERFOLG!", "" + person1.user.getUsername() + " und "
                                + person2.user.getUsername() + " haben sich unsterblich verliebt");
                        game.runningInChannel.createMessage("Des Amors Liebespfeile haben ihr Ziel gefunden").block();
                        person1.user.getPrivateChannel().block().createMessage("Du fällst mit **"+ person2.user.getUsername() + "** in eine unsterbliche Liebe. \n Eure Liebe ist do groß, dass ihr euch kein Leben ohne einander vorstllen könnt und deshalb sterbt sobald euer Partner stirbt").block();
                        person2.user.getPrivateChannel().block().createMessage("Du triffst dich mit **"+ person1.user.getUsername() + "** und verliebst dich Unsterblich in sie/ihn \n Eure Liebe ist do groß, dass ihr euch kein Leben ohne einander vorstllen könnt und deshalb sterbt sobald euer Partner stirbt").block();
                        
                        Globals.createEmbed(msgChannel, Color.RED, "Error: Zwei identische Usernames gefunden", "Tut mir leid, ich verstehe dich nicht. \n Deine Nachricht sollte aussehen: \n<Username> <Username> \n Achte darauf, dass du die Namen der Spieler richtig geschriben hast und keine überflüssigen Leerzeichen gesetzt hast. Probiere es noch einmal!");
                        succsess = false;

                    }

                } else {
                    msgChannel.createMessage(
                            "Tut mir leid ich verstehe dich nicht \n Deine Nachricht sollte aussehen \n<Username>LEERZEICHEN<Username> \n Achte darauf, dass du die Namen der Spieler richtig geschriben hast und keine überflüssigen Leerzeichen gesetzt hast. Probiere es noch einmal").block();
                            succsess = false;
                }
                return succsess;
                
            };
            game.mapPrivateCommands.put(playerWithThisRole.user.getId(), amorCommand);

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
            // TODO: am Ende jeder Tag und Nach wird livingPlayers geupdatet
            // wwPhase.start();
        }

    }

    private void sendMessageOnFirstNight() {
        // verkündet den Start der ersten Nacht
        Globals.createMessageBuilder(game.runningInChannel, "Unser Dorf wird seit den Tagen des alten Rom von Mythen und Sagen über Werwölfe heimgesucht. Seit kurzem sind diese Mythen zur Wirklichkeit geworden.", true);
        Globals.createMessageBuilder(game.runningInChannel, " Im Mondschein bestimmen die Dorfbewohner das man dieser Situation ein Ende setzen muss. ", true);
        Globals.createMessageBuilder(game.runningInChannel, "Es wird angekündigt das von nun an an jedem Morgen ein Dorfbewohner durch Abstimmung gelyncht wird. Somit beginnt die erste Nacht", true);
        Globals.createMessageBuilder(game.runningInChannel, "`In dieser Phase erwachen all jene SpezialKarten, welche Nachts eine Funktion erfüllen. Falls deine Karte eine dieser Spezialkarten ist wirst du von mir eine PrivatNachricht mit weiteren Infos erhalten. Alle Spieler welche über Videochat verbunden sind sollten nachts ihre Webcam ausschalten um ihre Identität zu bewahren`", false);
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
        // Zykus fortfährt
        mapExistingRoles.put("Werwolf", listWerwölfe);
        mapExistingRoles.put("Seher", listSeher);
        mapExistingRoles.put("Dorfbewohner", listDorfbewohner);
        
    }


    

    // loads the Commands available in this GameState into the map gameStateCommands
    private void registerStateCommands() {
        // TODO: add all Commands needed here

        // help
        Command helpCommand = (event, parameters, msgChannel) -> {

            msgChannel.createMessage("TODO: add help Command in Main State").block();
        };
        gameStateCommands.put("help", helpCommand);

    }

    // TODO: add Day Night Cycle
}

