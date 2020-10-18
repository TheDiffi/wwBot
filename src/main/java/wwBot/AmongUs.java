package wwBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.User;

public class AmongUs {

    List<User> players = new ArrayList<>();

    

    public static void commands(MessageCreateEvent event) {
        var serverId = event.getGuildId().get();
        var channel = event.getMessage().getChannel().block();

        String messageContent = event.getMessage().getContent().orElse("");
        List<String> parameters = Arrays.asList(messageContent.split(" "));

        // replies with Pong!
        if (parameters.get(0).equalsIgnoreCase("AU" + "ping")) {
            event.getMessage().getChannel().block().createMessage("Pong! Among Us").block();

        }

        
        // gets all users in the authors voice channel & mutes them
        if (parameters.get(0).equalsIgnoreCase("AU" + "mute")) {
            var voiceStates = event.getMessage().getAuthor().get().asMember(serverId).block().getVoiceState().block().getChannel().block().getVoiceStates().collectList().block();
            List<User> listUsers = new ArrayList<>();
            for (VoiceState voiceState : voiceStates) {
                listUsers.add(voiceState.getUser().block());
            }
        
            Globals.setMuteAllPlayers(listUsers, true, serverId);
            event.getMessage().getChannel().block().createMessage("Pong! Among Us").block();
        }
    }

}