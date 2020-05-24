package wwBot;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
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

        client.getEventDispatcher().on(MessageCreateEvent.class).filter(message -> message.getMessage().getAuthor()
                .map(user -> !user.getId().equals(client.getSelfId().get())).orElse(false)).subscribe(event -> {
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

        handleMemes(event, parameters);

        // test (remove after)
        if (parameters.get(0).equalsIgnoreCase(prefix + "test")) {
            event.getMessage().getChannel().block().createMessage("test").block();




            var guild = event.getGuild().block();
            var defaultRole = guild.getRoles().toStream().filter(r -> r.getName().equals("@everyone")).findFirst()
                    .get();

            var overrides = new HashSet<PermissionOverwrite>();
            overrides.add(PermissionOverwrite.forRole(defaultRole.getId(), PermissionSet.none(),
                    PermissionSet.of(Permission.VIEW_CHANNEL)));
            overrides.add(PermissionOverwrite.forMember(event.getMember().get().getId(),
                    PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()));

            var channel = event.getGuild().block().createTextChannel(spec -> {

                spec.setPermissionOverwrites(overrides);
                spec.setName("testchannel");
            }).block();

            channel.edit(spec->{
                spec.setPermissionOverwrites(overrides);
            });






            event.getMember().get().edit(a -> a.setMute(true)).block();
            event.getMessage().getChannel().block().createEmbed(spec -> {
                spec.setImage("https://i.imgur.com/9aJeWxK.jpg").setFooter("test4",
                        "https://cdn.discordapp.com/attachments/545307459691085828/709058905237356554/Werwolf.jpg")
                        .setTitle("title")
                        .setAuthor("by me",
                                "https://discord.com/developers/docs/resources/channel#channel-object-channel-types",
                                "https://cdn.discordapp.com/attachments/545307459691085828/708094976990642326/Werwolf_bild.png")
                        .setThumbnail(
                                "https://cdn.discordapp.com/attachments/545307459691085828/708094976990642326/Werwolf_bild.png");
            }).block();
        }

        // prüft ob die Nachricht keine DM(DirectMessage) ist
        if (event.getGuildId().isPresent()) {

            if (messageContent.startsWith(prefix)) {
                var server = event.getGuild().block();
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
                        var game = new Game(server, channel);
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

                    game.handleCommands(event, event.getMessage().getChannel().block());
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
                        game.handleCommands(event, event.getMessage().getChannel().block());
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
                            MessagesMain.errorNoAccessToCommand(game, event.getMessage().getChannel().block());

                        }
                    }
                }

            } else {
                event.getMessage().getChannel().block().createMessage("no Game found").block();
            }

        }

    }

    private static void handleMemes(MessageCreateEvent event, List<String> parameters) {
        var content = event.getMessage().getContent().get();
        if (event.getMessage().getContent().get().contains("Hey @WerwolfBot! lets do this game together, shall we?")) {
            event.getMessage().getChannel().block().createMessage("Oh, Heeey " + event.getMember().get().getMention()
                    + "! Didiididn't see you the-re 😳. Yeah.. let's do this~! *blushes*").block();
        }

        if (parameters.get(0).equalsIgnoreCase("F")) {

            event.getMessage().getChannel().block().createEmbed(spec -> {
                spec.setImage("https://i.imgur.com/9aJeWxK.jpg");

            }).block();

        }

        // test (remove after)
        if (event.getMessage().getContent().get().indexOf("I ") != -1) {

            event.getMessage().getChannel().block().createMessage(spec -> {
                var mssg = "> ..." + content.substring(content.indexOf("I") / 2);
                var youmeanttosay = content.substring(content.indexOf("I"), content.length());
                mssg += "\nI think you meant to say: " + youmeanttosay.replace("I", "WE");
                spec.setContent(mssg);
                spec.setEmbed(b -> {
                    b.setImage(
                            "https://cdn.discordapp.com/attachments/545307459691085828/714142918226477136/download.jpg");

                });
            }).block();

        }
    }

}
