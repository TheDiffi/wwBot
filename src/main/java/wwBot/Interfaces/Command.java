package wwBot.Interfaces;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;

public interface Command {
    
    void execute(MessageCreateEvent event, List<String> parameters, MessageChannel msgChannel);

}