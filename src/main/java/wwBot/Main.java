package wwBot;

import java.util.HashMap;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.util.Snowflake;
import wwBot.WerwolfGame.Game;

public class Main {

    public static HashMap<Snowflake, Game> mapRunningGames = new HashMap<Snowflake, Game>();
    public static String prefix;
    public static DiscordClient client;

    public static void main(String[] args) throws Exception {

        // initialisiert die Global klasse
        Globals.loadGlobals();

        // speichert den Prefix in einer Variable
        prefix = "&";

        // loads the Bot api
        client = DiscordClientBuilder.create("Bot Token Here").build();

        // looks at every message and calls "handleCommands"
        client.getEventDispatcher().on(MessageCreateEvent.class).filter(message -> message.getMessage().getAuthor()
                .map(user -> !user.getId().equals(client.getSelfId().get())).orElse(false)).subscribe(event -> {
                    try {
                        CommandHandler.handleCommands(event);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        event.getMessage().getChannel().block()
                                .createMessage(
                                        "avoided critical Exception, pls don't repeat what u did *laughs in pain*")
                                .block();
                        event.getMessage().getChannel().block().createMessage(ex.toString()).block();
                    }
                });

        // Reaction Event prototype
        /*
         * client.getEventDispatcher().on(ReactionAddEvent.class).filter(message ->
         * message.getUser().blockOptional() .map(user ->
         * !user.getId().equals(client.getSelfId().get())).orElse(false)).subscribe(
         * event -> { try { ReactionHandler.reactionAdded(event); } catch (Exception ex)
         * { ex.printStackTrace(); event.getChannel().block() .createMessage(
         * "Exception ReactionAdd") .block(); } });
         * 
         * client.getEventDispatcher().on(ReactionRemoveEvent.class).filter(message ->
         * message.getUser().blockOptional() .map(user ->
         * !user.getId().equals(client.getSelfId().get())).orElse(false)).subscribe(
         * event -> { try { ReactionHandler.reactionRemoved(event); } catch (Exception
         * ex) { ex.printStackTrace(); event.getChannel().block() .createMessage(
         * "Exception ReactionRemove") .block(); }
         * 
         * });
         */

        System.out.println("--- READY ---");

        // DO NOT REMOVE
        client.login().block();
    }
}