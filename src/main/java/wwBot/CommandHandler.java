package wwBot;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;
import wwBot.WerwolfGame.Game;
import wwBot.WerwolfGame.MessagesWW;
import wwBot.smallerGames.AmongUs;

public class CommandHandler {
    public static String WWprefix = Main.prefix;
    public static String amongUsPrefix = "AU";
    public static HashMap<Snowflake, Game> mapRunningGames = Main.mapRunningGames;

    public static void handleCommands(MessageCreateEvent event) {
        // messageContent speichert den Inhalt der Message in einer Variable
        // command teilt diesen Inhalt bei einem Leerzeichen und speicher dies in einer
        // Liste
        String messageContent = event.getMessage().getContent().orElse("");
        List<String> parameters = Arrays.asList(messageContent.split(" "));

        // handles the Memes ^.^
        handleMemes(event, parameters);

        // prüft ob die Nachricht keine DM(DirectMessage) ist
        if (event.getGuildId().isPresent() && event.getMessage().getContent().isPresent()) {

            if (messageContent.startsWith(amongUsPrefix)) {
                AmongUs.commands(event);
            }

            if (messageContent.startsWith(WWprefix)) {
                var server = event.getGuild().block();
                var serverId = event.getGuildId().get();
                var channel = event.getMessage().getChannel().block();

                // replies with Pong!
                if (parameters.get(0).equalsIgnoreCase(WWprefix + "ping")) {
                    event.getMessage().getChannel().block().createMessage("Pong! MAIN").block();

                }

                // überprüft ob auf diesem server bereits ein Game läuft, falls nein erstellt er
                // ein neues und fügt es zur Map runningGames hinzu
                if (parameters.get(0).equalsIgnoreCase(WWprefix + "NewGame")) {

                    if (!mapRunningGames.containsKey(serverId)) {
                        var game = new Game(server, channel);
                        mapRunningGames.put(serverId, game);

                    } else if (mapRunningGames.containsKey(serverId)) {
                        event.getMessage().getChannel().block().createMessage("use **" + WWprefix
                                + "DeleteGame** to delete the current game before starting a new one").block();
                    }

                }

                // überprüft ob auf diesem server bereits ein Game läuft, falls ja, löscht er es
                else if (parameters.get(0).equalsIgnoreCase(WWprefix + "DeleteGame")) {

                    if (!mapRunningGames.containsKey(serverId)) {
                        event.getMessage().getChannel().block().createMessage("No Game To Delete Found").block();

                    } else if (mapRunningGames.containsKey(serverId)) {
                        if (mapRunningGames.get(serverId).closeGame()) {
                            mapRunningGames.remove(serverId);
                            event.getMessage().getChannel().block().createEmbed(spec -> {
                                spec.setColor(Color.RED).setTitle("Game Deleted");

                            }).block();
                        } else {
                            event.getMessage().getChannel().block().createMessage("Game could not be closed");
                        }
                    }

                }
                // erklärt den Bot
                else if (parameters.get(0).equalsIgnoreCase(WWprefix + "explanation")) {
                    MessagesWW.sendGameExplanation(channel);

                }

                // falls ein Spiel existiert (also gerade läuft), und der Command auf dem
                // Server, auf dem das Spiel läuft, geschrieben wurde, wird die Eingabe zum Game
                // weitergeleited
                else if (mapRunningGames.containsKey(serverId)) {
                    var game = mapRunningGames.get(serverId);

                    // ------------------------
                    game.handleCommands(event, event.getMessage().getChannel().block());
                    // ------------------------

                }
                // ruft help für main auf(nur wenn noch kein Spiel läuft)
                else if (parameters.get(0).equalsIgnoreCase(WWprefix + "help")) {
                    MessagesWW.sendHelpMain(channel);

                }
                // prints a list of the commands of this class
                else if (parameters.get(0).equalsIgnoreCase(WWprefix + "showCommands")
                        || parameters.get(0).equalsIgnoreCase(WWprefix + "lsCommands")) {
                    Globals.createEmbed(channel, Color.CYAN, "Commands", MessagesWW.getCommandsMain());
                } else {
                    MessagesWW.errorCommandNotFound(channel);
                }
            }

            // falls die Nachricht eine DM ist, wird überprüft ob sich der Speler in einem
            // Game befindet
        } else if (!event.getGuildId().isPresent() && event.getMessage().getContent().isPresent()) {
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
                    if (messageContent.startsWith(WWprefix)) {
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
                            MessagesWW.errorNoAccessToCommand(game, event.getMessage().getChannel().block());

                        }
                    }
                }

            } else {
                event.getMessage().getChannel().block().createMessage("no Game found").block();
            }
        }
    }

    private static void handleMemes(MessageCreateEvent event, List<String> parameters) {
        if (event.getMessage().getContent().isPresent()) {
            var content = event.getMessage().getContent().get();
            if (event.getMessage().getContent().get()
                    .contains("Hey @WerwolfBot! lets do this game together, shall we?")) {
                event.getMessage().getChannel().block()
                        .createMessage("Oh, Heeey " + event.getMember().get().getMention()
                                + "! Didiididn't see you the-re 😳. Yeah.. let's do this~! *blushes*")
                        .block();
            }

            if (parameters.get(0).equalsIgnoreCase("F")) {

                event.getMessage().getChannel().block().createEmbed(spec -> {
                    spec.setImage("https://i.imgur.com/9aJeWxK.jpg");

                }).block();

            }

            if (parameters.get(0).equalsIgnoreCase("Hello") && parameters.get(1).equalsIgnoreCase("There")) {

                event.getMessage().getChannel().block()
                        .createMessage("https://tenor.com/view/grevious-general-kenobi-star-wars-gif-11406339").block();

            }

            if (parameters.get(0).equalsIgnoreCase("No") && parameters.get(1).equalsIgnoreCase("U")) {

                event.getMessage().getChannel().block()
                        .createMessage("https://tenor.com/view/uno-no-u-reverse-card-reflect-glitch-gif-14951171")
                        .block();

            }

            if (parameters.get(0).equalsIgnoreCase("I") && parameters.get(1).equalsIgnoreCase("am")
                    && parameters.get(2).equalsIgnoreCase("speed")) {

                event.getMessage().getChannel().block()
                        .createMessage("https://tenor.com/view/racing-speeding-switchinglanes-drivingcrazy-gif-8850377")
                        .block();

            }

            if (parameters.get(0).equalsIgnoreCase("howdy")) {
                var a = 1;
                try {
                    if (parameters.size() > 1 && parameters.get(1) != null) {
                        a = Integer.parseInt(parameters.get(1)) < 30 ? Integer.parseInt(parameters.get(1)) : 1 ;
                    } 
                } catch (Exception e) {
                    e.printStackTrace();
                }
                

                for (int i = 0; i < a; i++) {
                    event.getMessage().getChannel().block()
                            .createMessage("https://tenor.com/view/aaa-cowboy-aaa-cowboy-music-video-gif-15142935")
                            .block();
                }

            }

            // test (remove after)
            if (event.getMessage().getContent().get().indexOf("I ") != -1
                    || event.getMessage().getContent().get().indexOf("I'") != -1) {

                event.getMessage().getChannel().block().createMessage(spec -> {
                    var mssg = "> ..." + content.substring(content.indexOf("I") / 2);
                    var youmeanttosay = content.substring(content.indexOf("I"), content.length());
                    while (youmeanttosay.indexOf("me") != -1 || youmeanttosay.indexOf("mine") != -1
                            || youmeanttosay.indexOf("my") != -1 || youmeanttosay.indexOf("I") != -1
                            || youmeanttosay.indexOf("am") != -1) {
                        if (youmeanttosay.indexOf("me") != -1) {
                            youmeanttosay = youmeanttosay.replaceAll("me", "us");
                        }
                        if (youmeanttosay.indexOf("mine") != -1) {
                            youmeanttosay = youmeanttosay.replaceAll("mine", "ours");
                        }
                        if (youmeanttosay.indexOf("my") != -1) {
                            youmeanttosay = youmeanttosay.replaceAll("my", "our");
                        }
                        if (youmeanttosay.indexOf("I") != -1) {
                            youmeanttosay = youmeanttosay.replace("I", "WE");
                        }
                        if (youmeanttosay.indexOf("i") != -1) {
                            youmeanttosay = youmeanttosay.replace(" i ", " we ");
                        }
                        if (youmeanttosay.indexOf("am") != -1) {
                            youmeanttosay = youmeanttosay.replace("am", "are");
                        }
                        if (youmeanttosay.indexOf("'m") != -1) {
                            youmeanttosay = youmeanttosay.replace("'m", "'re");
                        }
                    }
                    mssg += "\nI think you meant to say: " + youmeanttosay;

                    spec.setContent(mssg);
                    spec.setEmbed(b -> {
                        b.setImage("https://i.imgur.com/8doX74q.jpg");

                    });
                }).block();

            } else if (event.getMessage().getContent().get().indexOf("idk") != -1) {
                event.getMessage().delete().block();
                event.getMessage().getChannel().block()
                        .createMessage(event.getMember().get().getMention() + " **WE** don't know").block();
            } else if (event.getMessage().getContent().get().indexOf("idc") != -1) {
                event.getMessage().delete().block();
                event.getMessage().getChannel().block()
                        .createMessage(event.getMember().get().getMention() + " **WE** don't care").block();
            }
        }
    }

}