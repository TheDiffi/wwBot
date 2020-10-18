package wwBot;

import java.util.HashMap;
import java.util.Map;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;

public class ReactionHandler {

    static Map<Message, Message> list = new HashMap<>();

    public static void reactionAdded(ReactionAddEvent event) {
        
        Message x = event.getChannel().block().createMessage("reacted! " + event.getEmoji().asUnicodeEmoji().get().getRaw()).block();
        list.put(event.getMessage().block(), x);
	}

	public static void reactionRemoved(ReactionRemoveEvent event) {
        var msg = event.getMessage().block();
        if(list != null && list.containsKey(msg)){
            list.get(msg).delete().block();
            list.remove(msg);
        }
	}

}
