package wwBot;

import java.util.HashMap;
import java.util.Map;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class Main {

    private static final Map<String, Command> commands = new HashMap<>();

    public static void main(String[] args) throws Exception {


        // Commands with the prefix !
        commands.put("ping", event -> {
            event.getMessage().getChannel().block().createMessage("Pong!").block();

        });


        DiscordClient client = DiscordClientBuilder
                .create("NzAyOTk2NzAwODIxODQ4MTE0.XqMWkg.tNGZ3Yhq9dyAQDumpZf5dhT9sBo").build();



        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
            final String content = event.getMessage().getContent().orElse("");
            for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                // We will be using ! as our "prefix" to any command in the system.
                if (content.startsWith('?' + entry.getKey())) {
                    try {
                        entry.getValue().execute(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                        break;
                }
            }
        });


        /* client.getEventDispatcher().on(MessageCreateEvent.class)
            .filter(message -> message.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
            .subscribe(event -> {

                reactMessage(event);
        }); */
        
    
        client.login().block();
    
    }


    /* public static void reactMessage(MessageCreateEvent event){

        String messageContent = event.getMessage().getContent().orElse("");

        if(messageContent.equalsIgnoreCase("!ping")){
            event.getMessage().getChannel().block().createMessage("Pong!").block();
        }

    } */
    
    
    






}
 



interface Command {
    void execute(MessageCreateEvent event) throws Exception;
}




