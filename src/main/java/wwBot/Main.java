package wwBot;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;
import wwBot.GameStates.MessagesMain;

public class Main {

    public static Map<Snowflake, Game> mapRunningGames = new HashMap<Snowflake, Game>();
    public static String prefix;

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

        // prüft ob die Nachricht keine DM(DirectMessage) ist
        if (event.getGuildId().isPresent()) {

            if (messageContent.startsWith(prefix)) {
                var serverId = event.getGuildId().get();
                var channel = event.getMessage().getChannel().block();

                // replies with Pong!
                if (parameters.get(0).equalsIgnoreCase(prefix + "ping")) {
                    event.getMessage().getChannel().block().createMessage("Pong! MAIN").block();

                }

                // überprüft ob auf diesem server bereits ein Game läuft, falls nein erstellt er
                // ein neues und fügt es zur Map runningGames hinzu
                if (parameters.get(0).equalsIgnoreCase(prefix + "NewGame")) {

                    if (!mapRunningGames.containsKey(serverId)) {
                        var game = new Game(serverId, channel);
                        mapRunningGames.put(serverId, game);

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
                else if (mapRunningGames.containsKey(serverId)) {
                    var game = mapRunningGames.get(serverId);
                    
                    game.handleCommands(event);
                }
                // ruft help für main auf(nur wenn noch kein Spiel läuft)
                else if (parameters.get(0).equalsIgnoreCase(prefix + "help")) {
                    MessagesMain.helpMain(event);
                }
                // prints a list of the commands of this class
                if (parameters.get(0).equalsIgnoreCase(prefix + "showCommands")) {
                    MessagesMain.showCommandsMain(event);
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
                // prüft ob der spieler in genau einem spiel ist (Player/Moderator)
                for (Game tempGame : tempListGame) {
                    if (tempGame.mapPlayers.containsKey(userId)) {
                        isInGame++;
                        game = tempGame;
                    } else if (tempGame.userModerator != null && tempGame.userModerator.getId().equals(userId)) {
                        isInGame++;
                        game = tempGame;
                    }
                }

                if (isInGame == 0 && game.userModerator.getId().equals(event.getMessage().getAuthor().get().getId())) {
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
                        if (game.mapPrivateCommands != null && game.mapPrivateCommands.size() > 0
                                && (game.mapPrivateCommands.containsKey(userId))) {
                            // looks at every command registered for this Id
                            for (int i = 0; i < game.mapPrivateCommands.get(userId).size(); i++) {
                                var success = game.mapPrivateCommands.get(userId).get(i).execute(event, parameters,
                                        event.getMessage().getChannel().block());
                                if (success) {
                                    game.mapPrivateCommands.get(userId).remove(i);
                                }
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

}
