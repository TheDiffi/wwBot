package wwBot.smallerGames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import wwBot.Globals;

public class AmongUs {

    List<User> players = new ArrayList<>();
    static boolean muted = false;

    public static void commands(MessageCreateEvent event) {
        var serverId = event.getGuildId().get();

        String messageContent = event.getMessage().getContent().orElse("");
        List<String> parameters = Arrays.asList(messageContent.split(" "));

        // replies with Pong!
        if (parameters.get(1).equalsIgnoreCase("ping")) {
            event.getMessage().getChannel().block().createMessage("Pong! Among Us").block();

        }

        // gets all users in the authors voice channel & mutes them
        if (parameters.get(1).equalsIgnoreCase("mute")) {
            Globals.setMuteAllPlayers(Globals.getUsersFromJoinedVoicechannel(serverId, event), true, serverId);

        }

        // gets all users in the authors voice channel & mutes them
        if (parameters.get(1).equalsIgnoreCase("unmute")) {
            Globals.setMuteAllPlayers(Globals.getUsersFromJoinedVoicechannel(serverId, event), false, serverId);

        }

        // gets all users in the authors voice channel & mutes them
        if (parameters.get(1).equalsIgnoreCase("next")) {

            muted = muted ? false : true;
            Globals.setMuteAllPlayers(Globals.getUsersFromJoinedVoicechannel(serverId, event), muted, serverId);
        }
    }

}