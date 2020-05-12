package wwBot;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;

public class Main {

    public static Map<Snowflake, Game> mapRunningGames = new HashMap<Snowflake, Game>();
    static String prefix;

    public static void main(String[] args) throws Exception {

        // inizialisiert die Global klasse
        Globals.loadGlobals();

        // speichert den Prefix in einer Variable
        prefix = "&";

        // loads the Bot api
        DiscordClient client = DiscordClientBuilder
                .create("NzA3NjUzNTk1NjQxOTM4MDMx.XrL8Cw.SRxT4UisfP6doLQoc-ZdgI5CtYY").build();

        // looks at every message and calls "handleCommands"

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .subscribe(event -> {
                    try {
                        handleCommands(event);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        event.getMessage().getChannel().block()
                                .createMessage(
                                        "avoided critical Exception, pls don't repeat what u did *laughs in pain*")
                                .block();
                    }
                });

        client.login().block();
    }

    private static void handleCommands(MessageCreateEvent event) {

        // messageContent speichert den Inhalt der Message in einer Variable
        // command teilt diesen Inhalt bei einem Leerzeichen und speicher dies in einer
        // Liste
        String messageContent = event.getMessage().getContent().orElse("");
        List<String> parameters = Arrays.asList(messageContent.split(" "));

        // prüft ob die Nachricht eine DM ist
        if (event.getGuildId().isPresent()) {

            if (messageContent.startsWith(prefix)) {
                var serverId = event.getGuildId().get();
                var channel = event.getMessage().getChannel().block();

                // help printet alle commands aus
                if (parameters.get(0).equalsIgnoreCase(prefix + "help")) {
                    event.getMessage().getChannel().block().createMessage("TODO: fill help in MAIN").block();

                }
                if (parameters.get(0).equalsIgnoreCase(prefix + "ping")) {
                    event.getMessage().getChannel().block().createMessage("Pong! MAIN").block();

                }

                // überprüft ob auf diesem server bereits ein Game läuft, falls nein erstellt er
                // ein neues und fügt es zur Map runningGames hinzu
                else if (parameters.get(0).equalsIgnoreCase(prefix + "NewGame")) {

                    if (!mapRunningGames.containsKey(serverId)) {

                        var game = new Game(serverId, channel);
                        mapRunningGames.put(serverId, game);
                        gameStartMessage(event.getMessage().getChannel().block(), prefix);

                    } else if (mapRunningGames.containsKey(serverId)) {
                        event.getMessage().getChannel().block().createMessage(
                                "use **" + prefix + "DeleteGame** to delete the current game before starting a new one")
                                .block();
                    }

                }

                // überprüft ob auf diesem server bereits ein Game läuft, falls ja, löscht er es
                else if (parameters.get(0).equalsIgnoreCase(prefix + "DeleteGame")) {

                    if (!mapRunningGames.containsKey(serverId)) {
                        event.getMessage().getChannel().block().createMessage("No Game To Delete Found").block();
                    } else if (mapRunningGames.containsKey(serverId)) {
                        mapRunningGames.remove(serverId);
                        event.getMessage().getChannel().block().createEmbed(spec -> {
                            spec.setColor(Color.RED).setTitle("Game Deleted").setDescription("");

                        }).block();
                    }

                }

                // falls ein Spiel existiert (also gerade läuft), und der Command auf dem
                // Server, auf dem das Spiel läuft, geschrieben wurde, wird die Eingabe zum Game
                // weitergeleited
                if (mapRunningGames.containsKey(serverId)) {
                    var game = mapRunningGames.get(serverId);
                    game.handleCommands(event);
                }
            }
            // falls die Nachricht eine DM ist, wird überprüft ob sich der Speler in einem
            // Game befindet
        } else {
            var userId = event.getMessage().getAuthor().get().getId();
            // prüft ob überhaupt ein game läuft
            if (!mapRunningGames.isEmpty()) {

                Game game = null;
                var isInGame = 0;
                var tempListGame = mapRunningGames.values();
                // prüft ob der spieler in genau einem spiel ist
                for (Game tempGame : tempListGame) {
                    if (tempGame.mapPlayers.containsKey(userId)) {
                        isInGame++;
                        game = tempGame;
                    }
                }
                if (isInGame == 0) {
                    event.getMessage().getChannel().block()
                            .createMessage(
                                    "It looks like you are not in a game or that your game is still in lobby phase")
                            .block();

                } else if (isInGame > 1) {
                    event.getMessage().getChannel().block().createMessage("It looks like you are in **" + isInGame
                            + "** Games. You should only be in one game at a time.").block();
                    // falls der spieler in einem Spiel ist
                } else if (game != null && isInGame == 1) {
                    if (messageContent.startsWith(prefix)) {
                        game.handleCommands(event);
                    } else {
                        // überprüft, ob in der map dieser User ist, d.h. ob das programm auf eine
                        // Antwort "wartet"
                        if (game.mapPrivateCommands != null && game.mapPrivateCommands.containsKey(userId)) {
                            var success = game.mapPrivateCommands.get(userId).execute(event, parameters,
                                    event.getMessage().getChannel().block());
                            if (success) {
                                game.mapPrivateCommands.remove(userId);
                            }
                        } else {
                            event.getMessage().getChannel().block().createMessage("you have no access to this command")
                                    .block();
                        }
                    }
                }

            } else {
                event.getMessage().getChannel().block().createMessage("no Game found").block();
            }

        }

    }

    // erzeugt und sendet ein Embed und eine Nachricht. Wird zu spielstart
    // aufgerufen
    public static void gameStartMessage(MessageChannel channel, String prefix) {

        Globals.createEmbed(channel, Color.GREEN, "Created New Game!", "");

        Globals.createMessageBuilder(channel,
                "Guten Abend liebe Dorfbewohner. \n Ich, euer Moderator, werde euch helfen die Werwolfinvasion zu stoppen.",
                true);
        Globals.createMessageBuilder(channel,
                "`Ihr könnt dem Dorf beitreten indem ihr \""
                + prefix + "join\" eingebt. Sobald alle Dorfbewohner bereit sind könnt ihr euch mit \"" + prefix
                + "buildDeck\" ein Deck vorschlagen lassen.`",
                false);
                
    }

}
