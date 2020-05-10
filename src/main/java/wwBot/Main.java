package wwBot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import java.awt.Color;

public class Main {

    public static Map<Snowflake, Game> runningGames = new HashMap<Snowflake, Game>();
    static String prefix;

    public static void main(String[] args) throws Exception {

        // inizialisiert die Global klasse
        Globals.loadGlobals();

        // speichert den Prefix in einer Variable
        prefix = "&";

        // loads the Bot api
        DiscordClient client = DiscordClientBuilder
                .create("NzA3NjUzNTk1NjQxOTM4MDMx.XrL8Cw.SRxT4UisfP6doLQoc-ZdgI5CtYY").build();

        // looks at every message
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .subscribe(event -> {

                    String messageContent = event.getMessage().getContent().orElse("");

                    // Schaut ob der erste Teil der Nachricht das Prefix ist
                    if (messageContent.startsWith(prefix)) {

                        handleCommands(event);

                    }

                });

        client.login().block();

    }




    private static void handleCommands(MessageCreateEvent event) {

        // messageContent speichert den Inhalt der Message in einer Variable
        // command teilt diesen Inhalt bei einem Leerzeichen und speicher dies in einer
        // Liste
        var serverId = event.getGuildId().get();
        String messageContent = event.getMessage().getContent().orElse("");
        List<String> parameters = Arrays.asList(messageContent.split(" "));

        // ping testet ob der bot antwortet
        if (parameters.get(0).equalsIgnoreCase(prefix + "ping")) {
            event.getMessage().getChannel().block().createMessage("Pong!").block();
        }
        // help printet alle commands aus
        else if (parameters.get(0).equalsIgnoreCase(prefix + "help")) {
            var delim = "\n";
            //var help = String.join(delim, )
            //event.getMessage().getChannel().block().createMessage(help).block();

        }

        // setPrefixTo
        else if (parameters.get(0).equalsIgnoreCase(prefix + "setPrefixTo")) {
            prefix = parameters.get(1);
            event.getMessage().getChannel().block().createMessage("Changed Prefix to: " + prefix).block();
        }

        // überprüft ob auf diesem server bereits ein Game läuft, falls nein erstellt er
        // ein neues und fügt es zur Map runningGames hinzu
        else if (parameters.get(0).equalsIgnoreCase(prefix + "NewGame")) {

            if (!runningGames.containsKey(serverId)) {

                var game = new Game(serverId);
                runningGames.put(serverId, game);
                gameStartMessage(event.getMessage().getChannel().block(), prefix);

            } else if (runningGames.containsKey(serverId)) {
                event.getMessage().getChannel().block()
                        .createMessage("delete the current game before starting a new one").block();
            }

        }

        // überprüft ob auf diesem server bereits ein Game läuft, falls ja, löscht er es
        else if (parameters.get(0).equalsIgnoreCase(prefix + "DeleteGame")) {

            if (!runningGames.containsKey(serverId)) {
                event.getMessage().getChannel().block().createMessage("No Game To Delete Found").block();
            } else if (runningGames.containsKey(serverId)) {
                runningGames.remove(serverId);
                event.getMessage().getChannel().block().createEmbed(spec -> {
                    spec.setColor(Color.RED).setTitle("Game Deleted").setDescription("");

                }).block();
            }

        }

        // falls ein Spiel existiert (also gerade läuft), und der Command auf dem
        // Server, auf dem das Spiel läuft, geschrieben wurde, wird die Eingabe zum Game
        // weitergeleited
        else if (runningGames.containsKey(serverId)) {
            var game = runningGames.get(serverId);
            game.handleCommands(event);
        }
    }

    // erzeugt und sendet ein Embed und eine Nachricht. Wird zu spielstart
    // aufgerufen
    public static void gameStartMessage(MessageChannel channel, String prefix) {

        channel.createEmbed(spec -> {
            spec.setColor(Color.GREEN).setTitle("New Game Created").setDescription("");

        }).block();

        channel.createMessage(messageSpec -> {
            messageSpec.setContent(
                    "Guten Abend liebe Dorfbewohner. \n Ich, euer Moderator, werde euch helfen die Werwolfinvasion zu stoppen.\n Ihr könnt dem Dorf beitreten indem ihr \""
                            + prefix + "join\" eingebt. Sobald alle Dorfbewohner bereit sind könnt ihr euch mit \""
                            + prefix + "buildDeck\" ein Deck vorschlagen lassen.")
                    .setTts(true);
        }).block();

    }

}
