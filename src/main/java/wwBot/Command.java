package wwBot;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;

public interface Command {
    
    void execute(MessageCreateEvent event, List<String> parameter);

}